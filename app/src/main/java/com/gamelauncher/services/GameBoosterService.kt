// app/src/main/java/com/gamelauncher/services/GameBoosterService.kt
package com.gamelauncher.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.gamelauncher.R
import com.gamelauncher.core.AndroidGameModeApiManager
import com.gamelauncher.core.BatterySaverManager
import com.gamelauncher.core.DeviceManager
import com.gamelauncher.core.DndManager
import com.gamelauncher.core.GameOptimizationCoordinator
import com.gamelauncher.core.NetworkManager
import com.gamelauncher.core.PerformanceManager
import com.gamelauncher.core.ThermalWatcher
import com.gamelauncher.core.TouchLatencyOptimizer
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
    @Inject lateinit var deviceManager: DeviceManager
    @Inject lateinit var networkManager: NetworkManager
    @Inject lateinit var dndManager: DndManager
    @Inject lateinit var touchLatencyOptimizer: TouchLatencyOptimizer
    @Inject lateinit var optimizationCoordinator: GameOptimizationCoordinator
    @Inject lateinit var batterySaverManager: BatterySaverManager
    @Inject lateinit var thermalWatcher: ThermalWatcher
    @Inject lateinit var gameModeApiManager: AndroidGameModeApiManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "game_booster_channel"

        const val ACTION_START_BOOST = "com.gamelauncher.action.START_BOOST"
        const val ACTION_STOP_BOOST = "com.gamelauncher.action.STOP_BOOST"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_PACKAGE = EXTRA_PACKAGE_NAME
        const val EXTRA_TARGET_FPS = "extra_target_fps"
        const val EXTRA_ENABLE_NETWORK = "extra_enable_network"

        fun startBoost(context: Context, packageName: String) {
            val intent = Intent(context, GameBoosterService::class.java).apply {
                action = ACTION_START_BOOST
                putExtra(EXTRA_PACKAGE_NAME, packageName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopBoost(context: Context) {
            val intent = Intent(context, GameBoosterService::class.java).apply {
                action = ACTION_STOP_BOOST
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_BOOST -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                    ?: intent.getStringExtra(EXTRA_PACKAGE)
                    ?: ""
                startBoostInternal(packageName)
            }
            ACTION_STOP_BOOST -> {
                stopBoostInternal()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Game Booster Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active game optimization service"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun startBoostInternal(packageName: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Game Booster Active")
            .setContentText("Optimizing performance for $packageName")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        serviceScope.launch {
            batterySaverManager.disableBatterySaver()

            val thermalStatus = deviceManager.getThermalStatus()
            val isOverheating = thermalStatus >= android.os.PowerManager.THERMAL_STATUS_CRITICAL

            if (isOverheating) {
                optimizationCoordinator.startThermalAwareOptimization(packageName)
            } else {
                optimizationCoordinator.startOptimization(packageName)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                try { gameModeApiManager.forcePerformanceMode(packageName) } catch (_: Exception) {}
            }

            thermalWatcher.start { thermalStatusInt: Int ->
                if (thermalStatusInt >= android.os.PowerManager.THERMAL_STATUS_CRITICAL) {
                    val targetFps = 30
                    val targetHz = 30f
                    performanceManager.lockFps(targetFps)
                    performanceManager.lockRefreshRate(targetHz)
                } else if (thermalStatusInt <= android.os.PowerManager.THERMAL_STATUS_LIGHT) {
                    val targetFps = 60
                    val targetHz = performanceManager.getSupportedRefreshRates().maxOrNull() ?: 60f
                    performanceManager.lockFps(targetFps)
                    performanceManager.lockRefreshRate(targetHz)
                }
            }

            try {
                val overlayIntent = Intent(this@GameBoosterService, OverlayService::class.java)
                startService(overlayIntent)
            } catch (_: Exception) {}
        }
    }

    private fun stopBoostInternal() {
        try {
            val overlayIntent = Intent(this, OverlayService::class.java)
            stopService(overlayIntent)
        } catch (_: Exception) {}

        serviceScope.launch {
            batterySaverManager.restoreBatterySaver()

            optimizationCoordinator.stopOptimization()
            dndManager.disableGamingDnd()
            touchLatencyOptimizer.disableTouchOptimizations()
            touchLatencyOptimizer.disableHighFrequencyTouch()
            touchLatencyOptimizer.disableGameModeTouch()
            networkManager.releaseWifiLock()
            performanceManager.restoreThreadPriority()
            performanceManager.stopPerformanceSession()
            performanceManager.restoreAnimations()
            val defaultHz = performanceManager.getSupportedRefreshRates().firstOrNull() ?: 60f
            performanceManager.lockRefreshRate(defaultHz)
            thermalWatcher.stop()
            optimizationCoordinator.getCurrentGamePackage()?.let { pkg ->
                try { gameModeApiManager.restoreStandardMode(pkg) } catch (_: Exception) {}
            }
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
