package com.gamelauncher.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.gamelauncher.core.GameLauncherApp
import com.gamelauncher.core.PerformanceManager
import com.gamelauncher.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GameDetectorService : Service() {

    @Inject lateinit var performanceManager: PerformanceManager
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var detectorJob: Job? = null
    private var currentGame: String? = null
    private var isBoostActive = false

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_DETECTOR -> startDetector()
            ACTION_STOP_DETECTOR -> stopDetector()
            ACTION_GAME_DETECTED -> handleGameDetected(intent.getStringExtra(EXTRA_PACKAGE) ?: "")
        }
        return START_STICKY
    }

    private fun startDetector() {
        startForeground(NOTIFICATION_ID, createNotification("Game Detector Active", "Monitoring for games..."))
        
        detectorJob?.cancel()
        detectorJob = serviceScope.launch {
            while (isActive) {
                checkForegroundApp()
                delay(2000)
            }
        }
    }

    private fun stopDetector() {
        detectorJob?.cancel()
        stopBoost()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun checkForegroundApp() {
        try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val runningTasks = activityManager.getRunningTasks(1)
            if (runningTasks.isNotEmpty()) {
                val topPackage = runningTasks[0].topActivity?.packageName ?: return
                if (topPackage != currentGame && isKnownGame(topPackage)) {
                    handleGameDetected(topPackage)
                }
            }
        } catch (e: Exception) {
            // Permission denied or not available
        }
    }

    private fun isKnownGame(packageName: String): Boolean {
        val popularGames = listOf(
            "com.mobile.legends", "com.activision.callofduty.shooter",
            "com.tencent.ig", "com.garena.game.freefire",
            "com.garena.game.freefirees", "com.miHoYo.GenshinImpact",
            "com.riotgames.league.wildrift", "com.tencent.tmgp.sgame",
            "com.supercell.clashofclans", "com.supercell.clashroyale",
            "com.gameloft.android.ANMP.GloftA9HM", "com.ea.games.r3_row",
            "com.epicgames.fortnite", "com.square_enix.android_googleplay.ffbeww",
            "com.netease.mrzhna", "com.levelinfinite.sgame",
            "com.proximabeta.mfk", "com.vng.mlbbvn"
        )
        return packageName in popularGames || packageName.contains("game") || 
               packageName.contains("mobile") || packageName.contains("legend")
    }

    private fun handleGameDetected(packageName: String) {
        if (currentGame == packageName) return
        currentGame = packageName
        
        startBoost(packageName)
        
        val gameName = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, 0)
            ).toString()
        } catch (e: Exception) { packageName }
        
        updateNotification("Game Detected", "$gameName - Boost Active")
    }

    private fun startBoost(gamePackage: String) {
        if (isBoostActive) return
        isBoostActive = true
        
        performanceManager.boostThreadPriority()
        performanceManager.startPerformanceSession(120)
        performanceManager.disableAnimations()
    }

    private fun stopBoost() {
        if (!isBoostActive) return
        isBoostActive = false
        
        performanceManager.restoreThreadPriority()
        performanceManager.stopPerformanceSession()
        performanceManager.restoreAnimations()
        currentGame = null
    }

    private fun createNotification(title: String, text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, GameLauncherApp.CHANNEL_BOOSTER)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(title: String, text: String) {
        val notification = createNotification(title, text)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        detectorJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_DETECTOR = "START_DETECTOR"
        const val ACTION_STOP_DETECTOR = "STOP_DETECTOR"
        const val ACTION_GAME_DETECTED = "GAME_DETECTED"
        const val EXTRA_PACKAGE = "PACKAGE"
        const val NOTIFICATION_ID = 3
    }
}