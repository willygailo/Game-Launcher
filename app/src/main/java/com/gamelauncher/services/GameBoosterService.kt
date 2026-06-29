package com.gamelauncher.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.gamelauncher.core.BatterySaverManager
import com.gamelauncher.core.DevicePerformancePlanner
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
    @Inject lateinit var batterySaverManager: BatterySaverManager  // NEW
    @Inject lateinit var devicePerformancePlanner: DevicePerformancePlanner

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
                val disableBatterySaver = intent.getBooleanExtra(EXTRA_DISABLE_BATTERY_SAVER, true)
                startBoost(pkg, targetFps, enableDnd, enableTouch, enableNetwork, disableBatterySaver)
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
        enableNetwork: Boolean,
        disableBatterySaver: Boolean
    ) {
        // Validate input parameters
        if (pkg.isBlank()) {
            Log.e(TAG, "Invalid package name: cannot be blank")
            stopSelf()
            return
        }

        var gameName: String = ""
        try {
            gameName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
        } catch (e: Exception) {
            gameName = pkg
            Log.e(TAG, "Failed to get game name for $pkg: ${e.message}")
        }
        gameName = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
        }.getOrDefault("Selected Game")

        val gameInfo = SupportedGames.findGame(pkg)
        val initialPlan = devicePerformancePlanner.planForGame(
            gameInfo = gameInfo,
            requestedFps = targetFps.takeIf { it > 0 },
            requestedHz = null,
            forceMaxRefreshRate = true
        )
        val actualTargetFps = initialPlan.targetFps

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationPendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, GameLauncherApp.CHANNEL_BOOSTER)
            .setContentTitle("🚀 Game Boost Active")
            .setContentText("Optimizing $gameName @ ${actualTargetFps} FPS")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(notificationPendingIntent)
            .setOngoing(true)
            .build()

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                @Suppress("DEPRECATION")
                startForeground(1, notification)
            }
        } catch (e: Exception) {
            stopSelf()
            return
        }

        fpsMonitor.startTracking()

        // Start live network monitoring right away
        networkManager.startMonitoring()

            serviceScope.launch(Dispatchers.IO) {
                // ── STEP 1: Kill battery saver FIRST (before any other opt) ──
                if (disableBatterySaver) {
                    var bsKilled = false
                    try {
                        bsKilled = batterySaverManager.disableBatterySaver()
                        // Also whitelist the game from Doze
                        if (bsKilled) {
                            batterySaverManager.whitelistGameFromDoze(pkg)
                            // Give OS time to settle battery saver state
                            kotlinx.coroutines.delay(200)
                        }
                    } catch (e: Exception) {
                        // Log error but continue with other optimizations
                        Log.e(TAG, "Battery saver disable failed: ${e.message}")
                    }
                }

            // ── STEP 2: Main optimization pipeline ─────────────────────
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

            // ── STEP 3: Network — WiFi + Data dual support ──────────────
            if (enableNetwork) {
                networkManager.acquireWifiLowLatencyLock("GameBoost")
                // Force mobile data always-on if we have WRITE_SECURE_SETTINGS
                performanceManager.forceMobileDataAlwaysOn()
            }

            // ── STEP 4: Device-planned FPS/Hz request ───────────────────
            if (settingsPreferences.forceMaxHzOnBoost.first()) {
                val refreshLocked = performanceManager.lockRefreshRate(result.targetHz)
                if (!refreshLocked) {
                    Log.i(TAG, "Refresh request limited by Android/OEM permissions")
                }
            }
            performanceManager.lockFps(result.targetFps)
            performanceManager.boostThreadPriority()
            performanceManager.startPerformanceSession(result.targetFps)

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
                val netSummary = networkManager.getNetworkSummary()
                val netScore = networkManager.networkQualityScore.value
                val isBsOff = !batterySaverManager.isBatterySaverCurrentlyOn()
                val battLevel = batterySaverManager.batteryLevel.value
                val isCharging = batterySaverManager.isCharging.value

                val battLabel = when {
                    isCharging -> "⚡ ${battLevel}%"
                    battLevel <= 20 -> "🔋 ${battLevel}%"
                    else -> "🔋 ${battLevel}%"
                }
                val bsLabel = if (isBsOff) "⚡ Boost On" else "🔋 Saver!"

                val notificationText = buildString {
                    append(name)
                    if (currentFps > 0) append("  |  $currentFps FPS")
                    if (currentHz > 0) append("  @  ${currentHz}Hz")
                }

                val bigText = buildString {
                    appendLine("$name")
                    appendLine("FPS: $currentFps / Target: ${targetFps}FPS | Hz: ${currentHz}Hz")
                    appendLine("Network: $netSummary (Quality: $netScore/100)")
                    appendLine("$battLabel | $bsLabel")
                }

                val notification = NotificationCompat.Builder(
                    this@GameBoosterService, GameLauncherApp.CHANNEL_BOOSTER
                )
                    .setContentTitle("🚀 Game Boost Active")
                    .setContentText(notificationText)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
                    .setSmallIcon(android.R.drawable.ic_menu_compass)
                    .setContentIntent(notificationPendingIntent)
                    .setOngoing(true)
                    .build()

                try { nm?.notify(1, notification) } catch (_: Exception) {}

                // Refresh battery status every cycle
                batterySaverManager.refreshBatteryStatus()

                delay(2000)
            }
        }
    }

    private fun stopBoost() {
        fpsMonitor.stopTracking()
        networkManager.stopMonitoring()
        serviceScope.launch {
            // Restore battery saver state
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
        private const val TAG = "GameBoosterService"
        const val ACTION_START_BOOST = "START_BOOST"
        const val ACTION_STOP_BOOST = "STOP_BOOST"
        const val ACTION_TOGGLE_DND = "TOGGLE_DND"
        const val EXTRA_PACKAGE = "PACKAGE"
        const val EXTRA_TARGET_FPS = "TARGET_FPS"
        const val EXTRA_ENABLE_DND = "ENABLE_DND"
        const val EXTRA_ENABLE_TOUCH = "ENABLE_TOUCH"
        const val EXTRA_ENABLE_NETWORK = "ENABLE_NETWORK"
        const val EXTRA_DISABLE_BATTERY_SAVER = "DISABLE_BATTERY_SAVER"  // NEW
    }
}
