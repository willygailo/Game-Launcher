package com.gamelauncher.domain.usecase

import android.content.Context
import android.content.Intent
import android.os.Build
import com.gamelauncher.data.local.GameDao
import com.gamelauncher.data.model.GameModel
import com.gamelauncher.data.model.GamingSession
import com.gamelauncher.data.repository.GamesRepository
import com.gamelauncher.services.GameBoosterService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LaunchGameAndBoostUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gamesRepository: GamesRepository,
    private val gameDao: GameDao
) {
    suspend operator fun invoke(game: GameModel) = withContext(Dispatchers.IO) {
        // 1. Record session
        try {
            gamesRepository.recordGameLaunch(game.packageName)
            gameDao.insertSession(
                GamingSession(
                    packageName = game.packageName,
                    gameName = game.name,
                    startTime = System.currentTimeMillis(),
                    wasBoosted = game.highPerformanceMode
                )
            )
        } catch (_: Exception) {}

        // 2. Start Booster Service if needed
        if (game.highPerformanceMode) {
            val boostIntent = Intent(context, GameBoosterService::class.java).apply {
                action = GameBoosterService.ACTION_START_BOOST
                putExtra(GameBoosterService.EXTRA_PACKAGE, game.packageName)
                putExtra(GameBoosterService.EXTRA_TARGET_FPS, game.targetFps)
                putExtra(GameBoosterService.EXTRA_ENABLE_NETWORK, game.wifiLockEnabled)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(boostIntent)
                } else {
                    context.startService(boostIntent)
                }
            } catch (_: Exception) {}
        }

        // 3. Launch the game
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(game.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
        } catch (_: Exception) {}
    }
}
