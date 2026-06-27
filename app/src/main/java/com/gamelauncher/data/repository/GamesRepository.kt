package com.gamelauncher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.gamelauncher.data.local.GameDao
import com.gamelauncher.data.model.GameModel
import com.gamelauncher.data.model.KnownGames
import com.gamelauncher.ml.GameClassifier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ScanProgress {
    object Idle : ScanProgress
    data class Scanning(val percentage: Int, val game: GameModel?) : ScanProgress
    object Completed : ScanProgress
}

@Singleton
open class GamesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gameDao: GameDao,
    private val gameClassifier: GameClassifier
) {
    val allGames: Flow<List<GameModel>> = gameDao.getAllGames()

    open fun scanAndSaveGames(): Flow<ScanProgress> = flow {
        emit(ScanProgress.Scanning(0, null))

        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        }

        val totalApps = resolveInfos.size
        if (totalApps == 0) {
            emit(ScanProgress.Completed)
            return@flow
        }

        // Delete games from DB that are no longer installed
        val installedPackages = resolveInfos.map { it.activityInfo.packageName }.toSet()
        val existingPackages = gameDao.getAllGamePackages()
        existingPackages
            .filterNot { it in installedPackages }
            .forEach { gameDao.deleteGameByPackage(it) }

        // Process each app progressively
        for ((index, resolveInfo) in resolveInfos.withIndex()) {
            val pkgName = resolveInfo.activityInfo.packageName
            val isKnown = KnownGames.LIST.find { it.first == pkgName }
            
            var discoveredGame: GameModel? = null

            try {
                val appInfo = pm.getApplicationInfo(pkgName, 0)
                val isSystemGame = appInfo.category == ApplicationInfo.CATEGORY_GAME

                val isClassifierGame = gameClassifier.isGame(pkgName, pm)

                if (isKnown != null) {
                    val knownGame = com.gamelauncher.core.SupportedGames.findGame(pkgName)
                    discoveredGame = GameModel(
                        packageName = pkgName,
                        name = isKnown.second,
                        isKnownGame = true,
                        customCategory = isKnown.third,
                        targetFps = knownGame?.maxFps?.coerceIn(30, 165) ?: 60
                    )
                } else if (isSystemGame) {
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    discoveredGame = GameModel(
                        packageName = pkgName,
                        name = appName,
                        isKnownGame = false,
                        customCategory = "Other"
                    )
                } else if (isClassifierGame && !isSystemApp(pm, appInfo)) {
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    discoveredGame = GameModel(
                        packageName = pkgName,
                        name = appName,
                        isKnownGame = false,
                        customCategory = "Other"
                    )
                }

                // If it is a game, save it progressively to Room
                if (discoveredGame != null) {
                    val existing = gameDao.getGameByPackageName(discoveredGame.packageName)
                    if (existing == null) {
                        gameDao.insertGame(discoveredGame)
                    } else {
                        gameDao.updateGame(
                            existing.copy(
                                name = discoveredGame.name,
                                isKnownGame = discoveredGame.isKnownGame,
                                customCategory = discoveredGame.customCategory
                            )
                        )
                    }
                }
            } catch (_: Exception) {}

            val progress = ((index + 1) * 100) / totalApps
            emit(ScanProgress.Scanning(progress, discoveredGame))
        }

        emit(ScanProgress.Completed)
    }.flowOn(Dispatchers.IO)

    private fun isSystemApp(pm: PackageManager, appInfo: ApplicationInfo): Boolean {
        return (appInfo.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0
    }

    open suspend fun updateGame(game: GameModel) = gameDao.updateGame(game)

    open suspend fun getGame(packageName: String): GameModel? = withContext(Dispatchers.IO) {
        gameDao.getGameByPackageName(packageName)
    }

    open suspend fun recordGameLaunch(packageName: String) = withContext(Dispatchers.IO) {
        val game = gameDao.getGameByPackageName(packageName)
        if (game != null) {
            gameDao.updateGame(
                game.copy(
                    lastLaunched = System.currentTimeMillis(),
                    totalBoostSessions = game.totalBoostSessions + 1
                )
            )
        }
    }
}
