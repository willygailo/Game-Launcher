// app/src/main/java/com/gamelauncher/core/GameOptimizationCoordinator.kt
package com.gamelauncher.core

import android.content.Context
import android.os.Build
import android.os.PowerManager
import com.gamelauncher.core.shizuku.IShellExecutor
import com.gamelauncher.data.preference.SettingsPreferences
import com.gamelauncher.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameOptimizationCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val performanceManager: PerformanceManager,
    private val deviceManager: DeviceManager,
    private val networkManager: NetworkManager,
    private val dndManager: DndManager,
    private val touchLatencyOptimizer: TouchLatencyOptimizer,
    private val socManager: SocManager,
    private val devicePerformancePlanner: DevicePerformancePlanner,
    private val settingsPreferences: SettingsPreferences,
    private val gameDao: com.gamelauncher.data.local.GameDao,
    private val batterySaverManager: BatterySaverManager,
    private val shellExecutor: IShellExecutor,
    @ApplicationScope private val appScope: CoroutineScope
) {
    data class OptimizationResult(
        val success: Boolean,
        val appliedOptimizations: List<String>,
        val errors: List<String> = emptyList(),
        val targetFps: Int = 60,
        val targetHz: Float = 60f
    )

    private val _optimizationActive = AtomicBoolean(false)
    @Volatile private var currentGamePackage: String? = null

    suspend fun startOptimization(packageName: String): OptimizationResult {
        if (!_optimizationActive.compareAndSet(false, true)) {
            return OptimizationResult(true, listOf("Already optimized"))
        }

        currentGamePackage = packageName

        val appliedOptimizations = mutableListOf<String>()
        val errors = mutableListOf<String>()

        val gameModel = try { gameDao.getGameByPackageName(packageName) } catch (e: Exception) {
            errors.add("Failed to load game data: ${e.message}")
            gameDao.getGameByPackageName(packageName)
        }
        val gameInfo = try { SupportedGames.findGame(packageName) } catch (e: Exception) {
            errors.add("Failed to find game info: ${e.message}")
            SupportedGames.GameInfo(packageName, "Unknown", "Global", 60)
        }

        val socInfo = try { socManager.getSocInfo() } catch (e: Exception) {
            errors.add("Failed to get SOC info: ${e.message}")
            SocInfo()
        }

        val thermalStatus = try { deviceManager.getThermalStatus() } catch (e: Exception) {
            errors.add("Failed to get thermal status: ${e.message}")
            PowerManager.THERMAL_STATUS_NONE
        }

        try {
            batterySaverManager.whitelistGameFromDoze(packageName)
            appliedOptimizations.add("⏫ Doze Whitelist: $packageName")
        } catch (e: Exception) {
            errors.add("Doze whitelist exception: ${e.message}")
        }

        val requestedFps = getRequestedFps(gameModel)
        val shouldForceMaxRefresh = (gameModel?.forceMaxRefreshRate ?: true) &&
            settingsPreferences.forceMaxHzOnBoost.first()
        val framePlan = devicePerformancePlanner.planForGame(
            gameInfo = gameInfo,
            requestedFps = requestedFps,
            requestedHz = gameModel?.targetHz,
            forceMaxRefreshRate = shouldForceMaxRefresh,
            thermalStatusOverride = thermalStatus
        )
        val targetFps = framePlan.targetFps
        val targetHz = framePlan.targetHz
        val stableHz = framePlan.targetHz
        appliedOptimizations.add("Device Plan: ${targetFps}FPS @ ${stableHz.toInt()}Hz (${framePlan.reason})")

        try {
            performanceManager.optimizeNonRoot(packageName)
            appliedOptimizations.add("Non-Root Performance Mode")

            shellExecutor.writeSetting("global", "game_driver_opt_in_apps", packageName)
            appliedOptimizations.add("GameManager Performance Mode Force (Max FPS)")

            val maxHz = performanceManager.getSupportedRefreshRates().maxOrNull() ?: 60f
            shellExecutor.setPeakRefreshRate(maxHz)
            shellExecutor.setMinRefreshRate(maxHz)
            shellExecutor.writeSetting("system", "user_refresh_rate", maxHz.toString())
            shellExecutor.writeSetting("system", "miui_refresh_rate", maxHz.toString())
            shellExecutor.writeSetting("system", "high_refresh_rate", "1")
            shellExecutor.writeSetting("secure", "refresh_rate_mode", "2")
            appliedOptimizations.add("Unlocked Max Hz Panel Target (${maxHz.toInt()}Hz)")

            val thermalOk = shellExecutor.setThermalOverride(true)
            if (thermalOk) {
                appliedOptimizations.add("Thermal Service Override Active (No Throttling)")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    val gameManager = context.getSystemService(android.app.GameManager::class.java)
                    if (gameManager != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val gameState = android.app.GameState(false, android.app.GameState.MODE_CONTENT)
                            gameManager.setGameState(gameState)
                            appliedOptimizations.add("GameManager Local Priority Active")
                        }
                    }
                } catch (_: Exception) {}
            }

            performanceManager.boostThreadPriority()
            appliedOptimizations.add("Thread Priority Boost")

            performanceManager.startPerformanceSession(targetFps)
            appliedOptimizations.add("ADPF Performance Session (${targetFps} FPS)")

            val refreshResult = performanceManager.lockRefreshRate(stableHz)
            if (refreshResult) {
                appliedOptimizations.add("Refresh Rate Locked (${stableHz.toInt()}Hz)")
            } else {
                appliedOptimizations.add("Refresh Rate (limited)")
            }

            performanceManager.lockFps(targetFps)
            appliedOptimizations.add("FPS Target Locked (${targetFps} FPS)")

            if (thermalStatus <= PowerManager.THERMAL_STATUS_LIGHT) {
                val dndResult = dndManager.enableGamingDnd()
                if (dndResult) {
                    appliedOptimizations.add("Do Not Disturb Enabled")
                } else if (!dndManager.isDndPermissionGranted()) {
                    errors.add("DND permission not granted")
                }

                val touchBoost = gameModel?.touchLatencyBoost ?: true
                if (touchBoost) {
                    touchLatencyOptimizer.enableTouchOptimizations()
                    touchLatencyOptimizer.enableHighFrequencyTouch()
                    appliedOptimizations.add("Touch Latency Optimized")
                }

                performanceManager.disableAnimations()
                appliedOptimizations.add("Animations Disabled")
            } else {
                appliedOptimizations.add("Thermal Protection: Limited optimizations")
            }

            networkManager.acquireWifiLowLatencyLock("GameBoost")
            appliedOptimizations.add("Low Latency Network Mode")

            val ramAggressiveness = gameModel?.ramAggressiveness ?: "NORMAL"
            if (ramAggressiveness != "LIGHT") {
                val memFreed = deviceManager.killBackgroundApps()
                if (memFreed > 0) {
                    appliedOptimizations.add("Memory Cleaned (${memFreed}MB freed)")
                }
                if (ramAggressiveness == "AGGRESSIVE" || ramAggressiveness == "EXTREME") {
                    performanceManager.triggerHeapCompaction()
                    appliedOptimizations.add("Heap Compaction Applied")
                }
                if (ramAggressiveness == "EXTREME") {
                    deviceManager.killBackgroundApps()
                }
            }

            if (thermalStatus >= PowerManager.THERMAL_STATUS_CRITICAL) {
                errors.add("Device is overheating - performance limited")
            }

        } catch (e: Exception) {
            errors.add("Error: ${e.message}")
        }

        return OptimizationResult(
            success = appliedOptimizations.isNotEmpty(),
            appliedOptimizations = appliedOptimizations,
            errors = errors,
            targetFps = targetFps,
            targetHz = targetHz
        )
    }

    suspend fun startThermalAwareOptimization(packageName: String): OptimizationResult {
        val thermalStatus = deviceManager.getThermalStatus()
        return if (thermalStatus >= PowerManager.THERMAL_STATUS_CRITICAL) {
            OptimizationResult(
                success = true,
                appliedOptimizations = listOf("Thermal Safe Mode: boost skipped — device overheating"),
                errors = listOf("Device is in CRITICAL thermal state — full boost suppressed"),
                targetFps = 30,
                targetHz = 30f
            )
        } else {
            startOptimization(packageName)
        }
    }

    suspend fun stopOptimization(): OptimizationResult {
        if (!_optimizationActive.compareAndSet(true, false)) {
            return OptimizationResult(true, listOf("Not active"))
        }

        val restoredOptimizations = mutableListOf<String>()
        val errors = mutableListOf<String>()

        try {
            performanceManager.restoreNonRoot()
            restoredOptimizations.add("Non-Root Settings Restored")

            performanceManager.restoreThreadPriority()
            restoredOptimizations.add("Thread Priority Restored")

            performanceManager.stopPerformanceSession()
            restoredOptimizations.add("ADPF Session Stopped")

            performanceManager.restoreRefreshRate()
            restoredOptimizations.add("Refresh Rate Restored")

            dndManager.disableGamingDnd()
            restoredOptimizations.add("DND Disabled")

            touchLatencyOptimizer.disableTouchOptimizations()
            touchLatencyOptimizer.disableHighFrequencyTouch()
            restoredOptimizations.add("Touch Latency Restored")

            networkManager.releaseWifiLock()
            restoredOptimizations.add("Network Lock Released")

            performanceManager.restoreAnimations()
            restoredOptimizations.add("Animations Restored")

            try {
                batterySaverManager.restoreBatterySaver()
                restoredOptimizations.add("⚡ Battery Saver State Restored")
            } catch (e: Exception) {
                errors.add("Failed to restore battery saver: ${e.message}")
            }

            try {
                shellExecutor.setThermalOverride(false)
                restoredOptimizations.add("Thermal Engines Resumed")
            } catch (e: Exception) {
                errors.add("Failed to resume thermal engines: ${e.message}")
            }
        } catch (e: Exception) {
            errors.add("Error during optimization stop: ${e.message}")
        }

        currentGamePackage = null

        return OptimizationResult(success = errors.isEmpty(), appliedOptimizations = restoredOptimizations, errors = errors)
    }

    private fun getRequestedFps(gameModel: com.gamelauncher.data.model.GameModel?): Int? {
        if (gameModel == null) return null
        val maxPerformanceRequested = gameModel.highPerformanceMode &&
            (gameModel.graphicsMode == "PERFORMANCE" || gameModel.graphicsMode == "BALANCED")
        if (maxPerformanceRequested) return null
        return when (gameModel.graphicsMode) {
            "PERFORMANCE" -> null
            "BALANCED" -> 90
            "BATTERY_SAVER" -> 30
            "CUSTOM" -> gameModel.targetFps
            else -> gameModel.targetFps.takeIf { it != 60 }
        }
    }

    fun isOptimizationActive(): Boolean = _optimizationActive.get()
    fun getCurrentGamePackage(): String? = currentGamePackage

    fun getSupportedFps(): List<Int> {
        val rates = performanceManager.getSupportedRefreshRates()
        val maxRate = rates.maxOrNull()?.toInt() ?: 60
        return listOf(30, 45, 60, 90, 120, 144, 165, 180, 200, 240).filter { it <= maxRate }
    }

    fun getSupportedRefreshRates(): List<Float> = performanceManager.getSupportedRefreshRates()

    suspend fun getDeviceThermalStatus(): Int = deviceManager.getThermalStatus()

    fun isThermalThrottling(): Boolean {
        return try {
            val powerManager = context.getSystemService(PowerManager::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val status = powerManager?.currentThermalStatus ?: PowerManager.THERMAL_STATUS_NONE
                status > PowerManager.THERMAL_STATUS_LIGHT
            } else false
        } catch (_: Exception) { false }
    }

    fun getThermalStatusString(): String {
        return try {
            val pm = context.getSystemService(PowerManager::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (pm?.currentThermalStatus) {
                    PowerManager.THERMAL_STATUS_NONE -> "Normal"
                    PowerManager.THERMAL_STATUS_LIGHT -> "Light"
                    PowerManager.THERMAL_STATUS_MODERATE -> "Moderate"
                    PowerManager.THERMAL_STATUS_SEVERE -> "Severe"
                    PowerManager.THERMAL_STATUS_CRITICAL -> "Critical"
                    PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency"
                    else -> "Unknown"
                }
            } else "Normal"
        } catch (_: Exception) { "Unknown" }
    }
}
