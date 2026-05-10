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
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gameDao: GameDao,
    private val gameClassifier: GameClassifier
) {
    val allGames: Flow<List<GameModel>> = gameDao.getAllGames()

    suspend fun scanAndSaveGames() = withContext(Dispatchers.IO) {
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

        val installedGames = mutableListOf<GameModel>()

        for (resolveInfo in resolveInfos) {
            val pkgName = resolveInfo.activityInfo.packageName
            val isKnown = KnownGames.LIST.find { it.first == pkgName }
            
            // Check if app is categorized as game by Android system
            val appInfo = pm.getApplicationInfo(pkgName, 0)
            val isSystemGame = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME
            } else {
                @Suppress("DEPRECATION")
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }

            // Check using ML/heuristic classifier
            val isClassifierGame = gameClassifier.isGame(pkgName, pm)

            if (isKnown != null) {
                installedGames.add(GameModel(
                    packageName = pkgName,
                    name = isKnown.second,
                    isKnownGame = true,
                    customCategory = isKnown.third
                ))
            } else if (isSystemGame) {
                val appName = pm.getApplicationLabel(appInfo).toString()
                installedGames.add(GameModel(
                    packageName = pkgName,
                    name = appName,
                    isKnownGame = false,
                    customCategory = "Other"
                ))
            } else if (isClassifierGame && !isSystemApp(pm, appInfo)) {
                val appName = pm.getApplicationLabel(appInfo).toString()
                installedGames.add(GameModel(
                    packageName = pkgName,
                    name = appName,
                    isKnownGame = false,
                    customCategory = "Other"
                ))
            }
        }

        val installedPackages = installedGames.mapTo(linkedSetOf()) { it.packageName }
        val existingPackages = gameDao.getAllGamePackages()

        existingPackages
            .asSequence()
            .filterNot { it in installedPackages }
            .forEach { gameDao.deleteGameByPackage(it) }

        for (scannedGame in installedGames) {
            val existing = gameDao.getGameByPackageName(scannedGame.packageName)
            if (existing == null) {
                gameDao.insertGame(scannedGame)
            } else {
                gameDao.updateGame(
                    existing.copy(
                        name = scannedGame.name,
                        isKnownGame = scannedGame.isKnownGame,
                        customCategory = scannedGame.customCategory
                    )
                )
            }
        }
    }

    private fun isSystemApp(pm: PackageManager, appInfo: ApplicationInfo): Boolean {
        return (appInfo.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0
    }

    suspend fun updateGame(game: GameModel) = gameDao.updateGame(game)

    suspend fun getGame(packageName: String): GameModel? = withContext(Dispatchers.IO) {
        gameDao.getGameByPackageName(packageName)
    }

    suspend fun recordGameLaunch(packageName: String) = withContext(Dispatchers.IO) {
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
