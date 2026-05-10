package com.gamelauncher.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.gamelauncher.core.DndManager
import com.gamelauncher.core.GameLauncherApp
import com.gamelauncher.core.GameOptimizationCoordinator
import com.gamelauncher.core.PerformanceManager
import com.gamelauncher.core.SupportedGames
import com.gamelauncher.core.TouchLatencyOptimizer
import com.gamelauncher.data.preference.SettingsPreferences
import com.gamelauncher.ml.GameClassifier
import com.gamelauncher.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GameDetectorService : Service() {

    @Inject lateinit var performanceManager: PerformanceManager
    @Inject lateinit var gameClassifier: GameClassifier
    @Inject lateinit var optimizationCoordinator: GameOptimizationCoordinator
    @Inject lateinit var dndManager: DndManager
    @Inject lateinit var touchLatencyOptimizer: TouchLatencyOptimizer
    @Inject lateinit var settingsPreferences: SettingsPreferences
    @Inject lateinit var deviceManager: com.gamelauncher.core.DeviceManager
    @Inject lateinit var networkManager: com.gamelauncher.core.NetworkManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var detectorJob: Job? = null
    private var usageStatsManager: UsageStatsManager? = null
    private var currentGame: String? = null
    private var isBoostActive = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_DETECTOR -> startDetector()
            ACTION_STOP_DETECTOR -> stopDetector()
            ACTION_GAME_DETECTED -> intent.getStringExtra(EXTRA_PACKAGE)?.let(::handleGameDetected)
            ACTION_ENABLE_DND -> serviceScope.launch { dndManager.enableGamingDnd() }
            ACTION_DISABLE_DND -> serviceScope.launch { dndManager.disableGamingDnd() }
            else -> startDetector()
        }
        return START_STICKY
    }

    private fun startDetector() {
        startForeground(
            NOTIFICATION_ID,
            createNotification(
                title = "Game Detector Active",
                text = "Monitoring foreground apps..."
            )
        )

        if (usageStatsManager == null) {
            usageStatsManager = getSystemService(UsageStatsManager::class.java)
        }

        detectorJob?.cancel()
        detectorJob = serviceScope.launch {
            while (isActive) {
                monitorForegroundApp()
                delay(1500)
            }
        }
    }

    private fun stopDetector() {
        detectorJob?.cancel()
        stopBoost()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun monitorForegroundApp() {
        val packageName = getForegroundPackage()

        if (packageName.isBlank()) {
            updateNotification("Game Detector Active", "Grant Usage Access in Settings to auto-detect games")
            return
        }

        if (packageName == this.packageName) return

        if (packageName == currentGame) {
            return
        }

        if (isKnownGame(packageName)) {
            handleGameDetected(packageName)
        } else if (isBoostActive) {
            stopBoost()
            updateNotification("Game Detector Active", "Waiting for game launch...")
        }
    }

    private fun getForegroundPackage(): String {
        val now = System.currentTimeMillis()
        return runCatching {
            usageStatsManager?.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                now - 20_000,
                now
            )?.maxByOrNull { it.lastTimeUsed }?.packageName.orEmpty()
        }.getOrDefault("")
    }

    private fun isKnownGame(packageName: String): Boolean {
        return SupportedGames.isSupportedGame(packageName) || gameClassifier.isGame(packageName, packageManager)
    }

    private fun isGameCategory(packageName: String): Boolean {
        return runCatching {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME
            } else {
                @Suppress("DEPRECATION")
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }
        }.getOrDefault(false)
    }

    private fun handleGameDetected(packageName: String) {
        if (currentGame == packageName) return
        currentGame = packageName

        serviceScope.launch {
            startSmartBoost(packageName)
        }

        val gameName = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
        }.getOrDefault(packageName)

        val gameInfo = SupportedGames.findGame(packageName)
        val fpsText = if (gameInfo != null) "@ ${gameInfo.maxFps} FPS" else ""
        
        updateNotification("Game Detected", "$gameName - Boost Active $fpsText")
    }

    private suspend fun startSmartBoost(packageName: String) {
        if (isBoostActive) return
        isBoostActive = true

        val settings = settingsPreferences

        val globalAutoBoost = settings.globalAutoBoost.first()
        if (!globalAutoBoost) return

        val dndEnabled = settings.dndEnabled.first()
        val touchEnabled = settings.touchOptimizationEnabled.first()
        val memoryEnabled = settings.memoryCleanupEnabled.first()
        val networkEnabled = settings.networkOptimizationEnabled.first()

        val result = optimizationCoordinator.startOptimization(packageName)

        if (dndEnabled) {
            dndManager.enableGamingDnd()
        }

        if (touchEnabled) {
            touchLatencyOptimizer.enableTouchOptimizations()
            touchLatencyOptimizer.enableHighFrequencyTouch()
        }

        if (memoryEnabled) {
            deviceManager.killBackgroundApps()
        }

        val gameInfo = SupportedGames.findGame(packageName)
        val targetFps = gameInfo?.maxFps ?: 60
        
        performanceManager.startPerformanceSession(targetFps)
        performanceManager.boostThreadPriority()
        performanceManager.disableAnimations()

        val maxHz = performanceManager.getSupportedRefreshRates().maxOrNull() ?: 60f
        performanceManager.lockRefreshRate(maxHz)
        performanceManager.lockFps(targetFps)

        if (networkEnabled) {
            networkManager.acquireWifiLowLatencyLock("GameBoost")
        }
    }

    private fun startBoost() {
        if (isBoostActive) return
        isBoostActive = true

        performanceManager.boostThreadPriority()
        performanceManager.startPerformanceSession(120)
        performanceManager.disableAnimations()
    }

    private fun stopBoost() {
        if (!isBoostActive) return
        isBoostActive = false

        serviceScope.launch {
            optimizationCoordinator.stopOptimization()
            dndManager.disableGamingDnd()
            touchLatencyOptimizer.disableTouchOptimizations()
            touchLatencyOptimizer.disableHighFrequencyTouch()
            networkManager.releaseWifiLock()

            val defaultHz = performanceManager.getSupportedRefreshRates().firstOrNull() ?: 60f
            performanceManager.lockRefreshRate(defaultHz)
        }

        performanceManager.restoreThreadPriority()
        performanceManager.stopPerformanceSession()
        performanceManager.restoreAnimations()

        currentGame = null
    }

    private fun createNotification(title: String, text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
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
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        stopBoost()
        detectorJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_DETECTOR = "START_DETECTOR"
        const val ACTION_STOP_DETECTOR = "STOP_DETECTOR"
        const val ACTION_GAME_DETECTED = "GAME_DETECTED"
        const val ACTION_ENABLE_DND = "ENABLE_DND"
        const val ACTION_DISABLE_DND = "DISABLE_DND"
        const val EXTRA_PACKAGE = "PACKAGE"
        const val NOTIFICATION_ID = 3
    }
}