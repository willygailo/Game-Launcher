package com.gamelauncher.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.gamelauncher.core.DndManager
import com.gamelauncher.core.FpsMonitor
import com.gamelauncher.core.GameLauncherApp
import com.gamelauncher.core.GameOptimizationCoordinator
import com.gamelauncher.core.NetworkManager
import com.gamelauncher.core.PerformanceManager
import com.gamelauncher.core.SupportedGames
import com.gamelauncher.core.TouchLatencyOptimizer
import com.gamelauncher.data.preference.SettingsPreferences
import com.gamelauncher.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GameBoosterService : Service() {

    @Inject lateinit var performanceManager: PerformanceManager
    @Inject lateinit var networkManager: NetworkManager
    @Inject lateinit var optimizationCoordinator: GameOptimizationCoordinator
    @Inject lateinit var dndManager: DndManager
    @Inject lateinit var touchLatencyOptimizer: TouchLatencyOptimizer
    @Inject lateinit var settingsPreferences: SettingsPreferences
    @Inject lateinit var fpsMonitor: FpsMonitor

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var gameName: String = ""
    private var notificationPendingIntent: PendingIntent? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START_BOOST -> {
                val pkg = intent.getStringExtra(EXTRA_PACKAGE) ?: ""
                val targetFps = intent.getIntExtra(EXTRA_TARGET_FPS, 60)
                val enableDnd = intent.getBooleanExtra(EXTRA_ENABLE_DND, true)
                val enableTouch = intent.getBooleanExtra(EXTRA_ENABLE_TOUCH, true)
                val enableNetwork = intent.getBooleanExtra(EXTRA_ENABLE_NETWORK, true)
                startBoost(pkg, targetFps, enableDnd, enableTouch, enableNetwork)
            }
            ACTION_STOP_BOOST -> {
                stopBoost()
                stopSelf()
            }
            ACTION_TOGGLE_DND -> {
                serviceScope.launch {
                    if (dndManager.isDndPermissionGranted()) dndManager.enableGamingDnd()
                    else dndManager.disableGamingDnd()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startBoost(
        pkg: String,
        targetFps: Int,
        enableDnd: Boolean,
        enableTouch: Boolean,
        enableNetwork: Boolean
    ) {
        gameName = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
        }.getOrDefault("Selected Game")

        val gameInfo = SupportedGames.findGame(pkg)
        val actualTargetFps = gameInfo?.maxFps ?: targetFps

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationPendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, GameLauncherApp.CHANNEL_BOOSTER)
            .setContentTitle("Game Boost Active")
            .setContentText("Optimizing $gameName @ ${actualTargetFps} FPS")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(notificationPendingIntent)
            .setOngoing(true)
            .build()

        try {
            startForeground(1, notification)
        } catch (e: Exception) {
            stopSelf()
            return
        }

        fpsMonitor.startTracking()

        serviceScope.launch {
            val thermalAware = settingsPreferences.thermalAwareBoost.first()
            val result = if (thermalAware)
                optimizationCoordinator.startThermalAwareOptimization(pkg)
            else
                optimizationCoordinator.startOptimization(pkg)

            if (enableDnd) dndManager.enableGamingDnd()
            if (enableTouch) {
                touchLatencyOptimizer.enableTouchOptimizations()
                touchLatencyOptimizer.enableHighFrequencyTouch()
                touchLatencyOptimizer.enableGameModeTouch()
            }
            if (enableNetwork) networkManager.acquireWifiLowLatencyLock("GameBoost")

            val maxHz = performanceManager.getSupportedRefreshRates().maxOrNull() ?: 60f
            performanceManager.lockRefreshRate(maxHz)
            performanceManager.lockFps(actualTargetFps)
            performanceManager.boostThreadPriority()
            performanceManager.maximizeCpuGpuPerformance()
            performanceManager.startPerformanceSession(actualTargetFps)
            performanceManager.disableAnimations()

            // Start live notification updates
            launchNotificationUpdater(gameName, result.targetFps)
        }
    }

    private fun launchNotificationUpdater(name: String, targetFps: Int) {
        serviceScope.launch {
            val nm = getSystemService(android.app.NotificationManager::class.java)
            while (isActive) {
                val currentFps = fpsMonitor.fps.value.toInt()
                val currentHz = performanceManager.getCurrentRefreshRate().toInt()

                val notificationText = buildString {
                    append(name)
                    if (currentFps > 0) append("  |  $currentFps FPS")
                    if (currentHz > 0) append("  @  ${currentHz}Hz")
                }

                val notification = NotificationCompat.Builder(this@GameBoosterService, GameLauncherApp.CHANNEL_BOOSTER)
                    .setContentTitle("Game Boost Active")
                    .setContentText(notificationText)
                    .setStyle(NotificationCompat.BigTextStyle()
                        .bigText("$name\nFPS: $currentFps | Hz: ${currentHz}Hz\nTarget: ${targetFps}FPS"))
                    .setSmallIcon(android.R.drawable.ic_menu_compass)
                    .setContentIntent(notificationPendingIntent)
                    .setOngoing(true)
                    .build()

                try { nm?.notify(1, notification) } catch (_: Exception) {}
                delay(2000)
            }
        }
    }

    private fun stopBoost() {
        fpsMonitor.stopTracking()
        serviceScope.launch {
            optimizationCoordinator.stopOptimization()
            dndManager.disableGamingDnd()
            touchLatencyOptimizer.disableTouchOptimizations()
            touchLatencyOptimizer.disableHighFrequencyTouch()
            touchLatencyOptimizer.disableGameModeTouch()
            networkManager.releaseWifiLock()
            performanceManager.restoreThreadPriority()
            performanceManager.stopPerformanceSession()
            performanceManager.restoreAnimations()
            performanceManager.restoreFps()
            val defaultHz = performanceManager.getSupportedRefreshRates().firstOrNull() ?: 60f
            performanceManager.lockRefreshRate(defaultHz)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        stopBoost()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_BOOST = "START_BOOST"
        const val ACTION_STOP_BOOST = "STOP_BOOST"
        const val ACTION_TOGGLE_DND = "TOGGLE_DND"
        const val EXTRA_PACKAGE = "PACKAGE"
        const val EXTRA_TARGET_FPS = "TARGET_FPS"
        const val EXTRA_ENABLE_DND = "ENABLE_DND"
        const val EXTRA_ENABLE_TOUCH = "ENABLE_TOUCH"
        const val EXTRA_ENABLE_NETWORK = "ENABLE_NETWORK"
    }
}
