package com.gamelauncher.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.gamelauncher.core.GameLauncherApp
import com.gamelauncher.core.NetworkManager
import com.gamelauncher.core.PerformanceManager
import com.gamelauncher.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GameBoosterService : Service() {

    @Inject lateinit var performanceManager: PerformanceManager
    @Inject lateinit var networkManager: NetworkManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START_BOOST -> {
                val pkg = intent.getStringExtra(EXTRA_PACKAGE) ?: ""
                val targetFps = intent.getIntExtra(EXTRA_TARGET_FPS, 60)
                val wifiLock = intent.getBooleanExtra(EXTRA_WIFI_LOCK, true)
                startBoost(pkg, targetFps, wifiLock)
            }
            ACTION_STOP_BOOST -> {
                stopBoost()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startBoost(pkg: String, targetFps: Int, wifiLock: Boolean) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, GameLauncherApp.CHANNEL_BOOSTER)
            .setContentTitle("Game Boost Active")
            .setContentText("Optimizing performance for gaming")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1, notification)

        // Apply performance boosts
        performanceManager.boostThreadPriority()
        performanceManager.startPerformanceSession(targetFps)
        
        if (wifiLock) {
            networkManager.acquireWifiLowLatencyLock()
        }
    }

    private fun stopBoost() {
        performanceManager.restoreThreadPriority()
        performanceManager.stopPerformanceSession()
        networkManager.releaseWifiLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBoost()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_BOOST = "START_BOOST"
        const val ACTION_STOP_BOOST = "STOP_BOOST"
        const val EXTRA_PACKAGE = "PACKAGE"
        const val EXTRA_TARGET_FPS = "TARGET_FPS"
        const val EXTRA_WIFI_LOCK = "WIFI_LOCK"
    }
}
