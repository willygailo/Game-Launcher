package com.gamelauncher.core

import android.content.Context
import android.os.Build
import android.os.PowerManager
import com.gamelauncher.data.preference.SettingsPreferences
import com.gamelauncher.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val rootShellManager: RootShellManager,
    private val socManager: SocManager,
    private val devicePerformancePlanner: DevicePerformancePlanner,
    private val settingsPreferences: SettingsPreferences,
    private val gameDao: com.gamelauncher.data.local.GameDao,
    private val batterySaverManager: BatterySaverManager,
    private val shizukuShellManager: ShizukuShellManager,
    @ApplicationScope private val appScope: CoroutineScope
) {
    data class OptimizationResult(
        val success: Boolean,
        val appliedOptimizations: List<String>,
        val errors: List<String> = emptyList(),
        val targetFps: Int = 60,
        val targetHz: Float = 60f
    )

    // AtomicBoolean prevents race condition when two coroutines call startOptimization concurrently.
    // compareAndSet(false, true) is an atomic operation — only one caller proceeds, others get "Already optimized".
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
        // Load game data with fallback
        val gameInfo = try { SupportedGames.findGame(packageName) } catch (e: Exception) {
            errors.add("Failed to find game info: ${e.message}")
            SupportedGames.GameInfo(packageName, "Unknown", "Global", 60)
        }

        // Auto-detect SOC info with backup
        val socInfo = try { socManager.getSocInfo() } catch (e: Exception) {
            errors.add("Failed to get SOC info: ${e.message}")
            SocInfo()
        }

        // Get thermal status for throttling decisions
        val thermalStatus = try { deviceManager.getThermalStatus() } catch (e: Exception) {
            errors.add("Failed to get thermal status: ${e.message}")
            PowerManager.THERMAL_STATUS_NONE
        }

        // ── Battery Saver: owned by GameBoosterService.startBoost() ──
        // Whitelisting game from Doze is still done here since we have packageName context.
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
            val hasRoot = rootShellManager.isRootAvailable()

            if (hasRoot) {
                val forceGpu = gameModel?.gpuTuning ?: true
                if (forceGpu) {
                    val cpuResult = performanceManager.maximizeCpuGpuPerformance()
                    if (cpuResult) {
                        appliedOptimizations.add("CPU/GPU Performance Boost (${socInfo.socType.name})")
                    } else {
                        errors.add("CPU/GPU boost not available (requires root)")
                    }
                } else {
                    performanceManager.setAdaptiveCpuGov(true)
                    appliedOptimizations.add("CPU Performance Boost (${socInfo.socType.name})")
                }
            } else {
                performanceManager.optimizeNonRoot(packageName)
                appliedOptimizations.add("Non-Root Performance Mode")
            }

            if (shizukuShellManager.isAvailable() || hasRoot) {
                // Android 13-16 Game Mode performance override command
                shizukuShellManager.executeCommand("cmd game set --mode 2 $packageName")
                appliedOptimizations.add("GameManager Performance Mode Force")

                // Android 13-16 background apps standby sleep mode command
                shizukuShellManager.executeCommand("cmd package list packages | cut -f 2 -d ':' | grep -v $packageName | xargs -I {} am set-standby-bucket {} rare")
                appliedOptimizations.add("Background Standby Lock Active")

                // Background ART speed AOT compilation boost
                appScope.launch(Dispatchers.IO) {
                    shizukuShellManager.executeCommand("cmd package compile -m speed -f $packageName")
                }
                appliedOptimizations.add("ART Speed AOT Optimization Queued")
                
                // Suspend thermal throttling engines
                val (thermalOk, _) = shizukuShellManager.suspendThermalEngines()
                if (thermalOk) {
                    appliedOptimizations.add("Thermal Engines Suspended (Max Performance)")
                }
            }

            // GameManager local game state optimization for overlay priority (Android 12 to 16)
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

            if (hasRoot) {
                applySocSpecificOptimizations(socInfo)

                // Deep Network and Memory Optimizations
                rootShellManager.executeCommand("sysctl -w net.ipv4.tcp_congestion_control=bbr")
                rootShellManager.executeCommand("sysctl -w net.ipv4.tcp_window_scaling=1")
                appliedOptimizations.add("TCP BBR Congestion Control Active")

                rootShellManager.executeCommand("echo 3 > /proc/sys/vm/drop_caches")
                rootShellManager.executeCommand("sysctl -w vm.swappiness=0")
                appliedOptimizations.add("Extreme Memory Swappiness (0%)")
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

    /**
     * Thermal-aware optimization: checks thermal state BEFORE starting.
     * If device is already critical, starts with a stripped-down optimization profile
     * instead of starting full boost then immediately stopping and restarting.
     */
    suspend fun startThermalAwareOptimization(packageName: String): OptimizationResult {
        val thermalStatus = deviceManager.getThermalStatus()
        return if (thermalStatus >= PowerManager.THERMAL_STATUS_CRITICAL) {
            // Don't boost at all on critical thermal state — return a safe no-op result
            OptimizationResult(
                success = true,
                appliedOptimizations = listOf("Thermal Safe Mode: boost skipped — device overheating"),
                errors = listOf("Device is in CRITICAL thermal state — full boost suppressed"),
                targetFps = 30,
                targetHz = 30f
            )
        } else {
            val result = startOptimization(packageName)
            result
        }
    }

    suspend fun stopOptimization(): OptimizationResult {
        if (!_optimizationActive.compareAndSet(true, false)) {
            return OptimizationResult(true, listOf("Not active"))
        }

        val restoredOptimizations = mutableListOf<String>()
        val errors = mutableListOf<String>()

        try {
            val hasRoot = rootShellManager.isRootAvailable()

            if (hasRoot) {
                performanceManager.setCpuGovernor("schedutil")
                restoredOptimizations.add("CPU Governor Restored")
                performanceManager.restoreCpuGpuPerformance()
                restoredOptimizations.add("GPU Settings Restored")

                // Restore Deep Optimizations
                rootShellManager.executeCommand("sysctl -w net.ipv4.tcp_congestion_control=cubic")
                rootShellManager.executeCommand("sysctl -w vm.swappiness=60")
                restoredOptimizations.add("TCP Congestion & Memory Swappiness Restored")
            } else {
                performanceManager.restoreNonRoot()
                restoredOptimizations.add("Non-Root Settings Restored")
            }

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

            // Restore battery saver state with improved error handling
            try {
                batterySaverManager.restoreBatterySaver()
                restoredOptimizations.add("⚡ Battery Saver State Restored")
            } catch (e: Exception) {
                errors.add("Failed to restore battery saver: ${e.message}")
            }

            // Resume thermal engines that were suspended during gaming.
            // Critical: without this, device has no thermal protection until reboot.
            try {
                val (resumeOk, _) = shizukuShellManager.resumeThermalEngines()
                if (resumeOk) restoredOptimizations.add("Thermal Engines Resumed")
            } catch (e: Exception) {
                errors.add("Failed to resume thermal engines: ${e.message}")
            }
        } catch (e: Exception) {
            errors.add("Error during optimization stop: ${e.message}")
        }

        currentGamePackage = null

        return OptimizationResult(success = errors.isEmpty(), appliedOptimizations = restoredOptimizations, errors = errors)
    }

    private suspend fun applySocSpecificOptimizations(socInfo: SocInfo) {
        if (!rootShellManager.isRootAvailable()) return
        withContext(Dispatchers.IO) {
            when (socInfo.socType) {
                SocType.SNAPDRAGON -> applySnapdragonOptimizations()
                SocType.MEDIATEK -> applyMediaTekOptimizations()
                SocType.EXYNOS -> applyExynosOptimizations()
                SocType.KIRIN -> applyKirinOptimizations()
                SocType.TENSOR -> applyTensorOptimizations()
                SocType.UNISOC -> applyUnisocOptimizations()
                else -> {}
            }
        }
    }

    private suspend fun applySnapdragonOptimizations() {
        rootShellManager.executeCommand("echo 'high_performance' > /sys/class/devfreq/soc:qcom,cpu-llcc-bw/governor")
        rootShellManager.executeCommand("echo 1 > /sys/devices/system/cpu/cpu0/cpufreq/boost")
        rootShellManager.executeCommand("echo 1 > /sys/kernel/debug/sched_energy_aware")
        rootShellManager.executeCommand("echo 100 > /sys/class/devfreq/soc:qcom,cpu-llcc-bw/max_freq")
        rootShellManager.executeCommand("echo 100 > /sys/class/devfreq/soc:qcom,cpubw/max_freq")
        // Snapdragon 8 Elite Gen 2 / 8 Elite specific
        rootShellManager.executeCommand("echo 1 > /sys/class/devfreq/soc:qcom,compute-cdsb/governor")
        rootShellManager.executeCommand("echo 1 > /sys/devices/system/cpu/cpu0/cpufreq/mem_latency")
        rootShellManager.executeCommand("echo 1 > /sys/module/qti_cpu_boost/parameters/boost_enabled")
        rootShellManager.executeCommand("echo 1 > /sys/devices/platform/soc/*/qcom,cpufreq-hw/boost")
    }

    private suspend fun applyMediaTekOptimizations() {
        rootShellManager.executeCommand("echo 1 > /sys/module/mtk_vcore_debug/parameters/enable")
        rootShellManager.executeCommand("echo 1 > /sys/devices/system/cpu/cpu0/cpufreq/game_mode")
        rootShellManager.executeCommand("echo 1 > /sys/kernel/ged/boost_gpu_enable")
        rootShellManager.executeCommand("echo performance > /sys/class/misc/mtk-vpu/devfreq/mtk-vpu/governor")
        rootShellManager.executeCommand("echo 1 > /proc/cpufreq/cpufreq_power_mode")
        rootShellManager.executeCommand("echo 0 > /proc/cpufreq/cpufreq_cci_mode")
    }

    private suspend fun applyExynosOptimizations() {
        rootShellManager.executeCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/gpu_governor")
        rootShellManager.executeCommand("echo 1 > /sys/devices/platform/gpu.0/devfreq/gpu.0/boost")
    }

    private suspend fun applyKirinOptimizations() {
        rootShellManager.executeCommand("echo 1 > /sys/class/dss/display/turbo")
        rootShellManager.executeCommand("echo 1 > /sys/kernel/hisi/npu/boost")
    }

    private suspend fun applyTensorOptimizations() {
        rootShellManager.executeCommand("echo performance > /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor")
        rootShellManager.executeCommand("echo performance > /sys/class/devfreq/*mali*/governor")
        rootShellManager.executeCommand("echo 1 > /sys/devices/platform/vertex.0/boost")
        rootShellManager.executeCommand("echo 1 > /sys/devices/platform/edge.0/boost")
    }

    private suspend fun applyUnisocOptimizations() {
        rootShellManager.executeCommand("echo performance > /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor")
        rootShellManager.executeCommand("echo noop > /sys/block/mmcblk0/queue/scheduler")
        rootShellManager.executeCommand("echo 1 > /sys/class/misc/mali0/device/power_policy")
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
