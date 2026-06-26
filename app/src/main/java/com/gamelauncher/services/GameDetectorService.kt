package com.gamelauncher.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
import android.os.Build
import android.os.Process
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
import com.gamelauncher.data.local.GameDao
import com.gamelauncher.data.model.GamingSession
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
import java.io.File
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
    @Inject lateinit var gameDao: GameDao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var detectorJob: Job? = null
    private var usageStatsManager: UsageStatsManager? = null
    private var currentGame: String? = null
    private var isBoostActive = false
    private var currentSessionId: Long = 0L
    private var sessionStartTime: Long = 0L
    private var sessionStartBattery: Int = 0

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
        try {
            startForeground(
                NOTIFICATION_ID,
                createNotification("Game Detector Active", "Monitoring foreground apps...")
            )
        } catch (e: Exception) {
            stopSelf()
            return
        }
        if (usageStatsManager == null) {
            usageStatsManager = getSystemService(UsageStatsManager::class.java)
        }
        // Boost detection service priority for faster game detection
        try { Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO) } catch (_: Exception) {}

        detectorJob?.cancel()
        detectorJob = serviceScope.launch {
            while (isActive) {
                monitorForegroundApp()
                delay(800)
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
        if (packageName == currentGame) return

        if (isKnownGame(packageName)) {
            handleGameDetected(packageName)
        } else if (isBoostActive) {
            stopBoost()
            updateNotification("Game Detector Active", "Waiting for game launch...")
        }
    }

    private fun getForegroundPackage(): String {
        // Try /proc-based detection first (faster, no usage stats permission needed)
        val procResult = getForegroundFromProc()
        if (procResult.isNotBlank()) return procResult

        // Fallback to UsageStatsManager
        val now = System.currentTimeMillis()
        return runCatching {
            usageStatsManager?.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                now - 20_000,
                now
            )?.maxByOrNull { it.lastTimeUsed }?.packageName.orEmpty()
        }.getOrDefault("")
    }

    private fun getForegroundFromProc(): String {
        return try {
            // Read all processes, find the one with highest priority
            val procDir = File("/proc")
            val processes = procDir.listFiles { f -> f.name.all { it.isDigit() } } ?: return ""
            for (proc in processes.take(200)) {
                val cmdline = runCatching { File(proc, "cmdline").readText().trim() }.getOrDefault() ?: continue
                if (cmdline.isNotBlank() && !cmdline.startsWith("com.gamelauncher") && !cmdline.startsWith("system") && !cmdline.startsWith("u:") && cmdline.contains(".")) {
                    return cmdline
                }
            }
            ""
        } catch (_: Exception) {
            ""
        }
    }
        } catch (_: Exception) { "" }
    }

    private fun isKnownGame(packageName: String): Boolean {
        return SupportedGames.isSupportedGame(packageName) || gameClassifier.isGame(packageName, packageManager)
    }

    @Suppress("DEPRECATION")
    private fun isGameCategory(packageName: String): Boolean {
        return runCatching {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME
            } else {
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }
        }.getOrDefault(false)
    }

    private fun handleGameDetected(packageName: String) {
        if (currentGame == packageName) return
        currentGame = packageName

        serviceScope.launch { startSmartBoost(packageName) }

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

        val globalAutoBoost = settingsPreferences.globalAutoBoost.first()
        if (!globalAutoBoost) return

        val gameModel = gameDao.getGameByPackageName(packageName)
        val dndEnabled = settingsPreferences.dndEnabled.first()
        val touchEnabled = gameModel?.touchLatencyBoost ?: settingsPreferences.touchOptimizationEnabled.first()
        val memoryEnabled = settingsPreferences.memoryCleanupEnabled.first()
        val networkEnabled = gameModel?.wifiLockEnabled ?: settingsPreferences.networkOptimizationEnabled.first()
        val thermalAware = settingsPreferences.thermalAwareBoost.first()

        val result = if (thermalAware)
            optimizationCoordinator.startThermalAwareOptimization(packageName)
        else
            optimizationCoordinator.startOptimization(packageName)

        if (dndEnabled) dndManager.enableGamingDnd()
        if (touchEnabled) {
            touchLatencyOptimizer.enableTouchOptimizations()
            touchLatencyOptimizer.enableHighFrequencyTouch()
        }
        
        val ramAggressiveness = gameModel?.ramAggressiveness ?: "NORMAL"
        if (memoryEnabled && ramAggressiveness != "LIGHT") {
            deviceManager.killBackgroundApps()
            if (ramAggressiveness == "AGGRESSIVE" || ramAggressiveness == "EXTREME") {
                performanceManager.triggerHeapCompaction()
            }
            if (ramAggressiveness == "EXTREME") {
                deviceManager.killBackgroundApps()
            }
        }

        val gameInfo = SupportedGames.findGame(packageName)
        val targetFps = gameModel?.targetFps ?: (gameInfo?.maxFps ?: result.targetFps)

        performanceManager.startPerformanceSession(targetFps)
        performanceManager.boostThreadPriority()
        performanceManager.disableAnimations()

        val supportedRates = performanceManager.getSupportedRefreshRates()
        val maxHz = supportedRates.maxOrNull() ?: 60f
        val targetHz = if (gameModel?.forceMaxRefreshRate == true) maxHz else maxHz
        val stableHz = supportedRates.minByOrNull { kotlin.math.abs(it - targetHz) } ?: 60f
        performanceManager.lockRefreshRate(stableHz)
        performanceManager.lockFps(targetFps)

        if (networkEnabled) {
            networkManager.acquireWifiLowLatencyLock("GameBoost")
        }

        val name = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
        }.getOrDefault(packageName)

        val thermalStatus = deviceManager.getThermalStatus()
        val thermalWarning = if (thermalStatus >= android.os.PowerManager.THERMAL_STATUS_MODERATE)
            " [Thermal: ${optimizationCoordinator.getThermalStatusString()}]" else ""
        val errorText = if (result.errors.isNotEmpty() && result.errors.none { it.contains("overheat", true) })
            " | ${result.errors.size} optimizations unavailable" else ""
        updateNotification("Game Detected", "$name @ ${targetFps}FPS - Boost Active$thermalWarning$errorText")

        sessionStartTime = System.currentTimeMillis()
        sessionStartBattery = deviceManager.getBatteryLevel()
        currentSessionId = gameDao.insertSession(GamingSession(
            packageName = packageName,
            gameName = name,
            startTime = sessionStartTime,
            wasBoosted = true
        ))
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

        if (currentSessionId > 0L) {
            val endTime = System.currentTimeMillis()
            val batteryNow = deviceManager.getBatteryLevel()
            val (_, ramUsed, _) = deviceManager.getRamInfo()
            val sessionId = currentSessionId
            serviceScope.launch {
                gameDao.updateSession(GamingSession(
                    id = sessionId,
                    packageName = currentGame ?: "",
                    gameName = "",
                    startTime = sessionStartTime,
                    endTime = endTime,
                    durationMs = (endTime - sessionStartTime).coerceAtLeast(0),
                    avgRamUsage = ramUsed,
                    batteryDrain = (sessionStartBattery - batteryNow).coerceAtLeast(0),
                    wasBoosted = true
                ))
            }
            currentSessionId = 0L
        }

        currentGame = null
    }

    private fun createNotification(title: String, text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
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
