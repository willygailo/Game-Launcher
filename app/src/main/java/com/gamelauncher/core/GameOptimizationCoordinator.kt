package com.gamelauncher.core

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.PowerManager
import android.view.Display
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.concurrent.Volatile
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
    private val socManager: SocManager
) {
    data class OptimizationResult(
        val success: Boolean,
        val appliedOptimizations: List<String>,
        val errors: List<String> = emptyList(),
        val targetFps: Int = 60,
        val targetHz: Float = 60f
    )

    @Volatile private var isOptimizationActive = false
    @Volatile private var currentGamePackage: String? = null

    suspend fun startOptimization(packageName: String): OptimizationResult {
        if (isOptimizationActive) {
            return OptimizationResult(true, listOf("Already optimized"))
        }

        isOptimizationActive = true
        currentGamePackage = packageName

        val appliedOptimizations = mutableListOf<String>()
        val errors = mutableListOf<String>()

        val socInfo = socManager.getSocInfo()
        val gameInfo = SupportedGames.findGame(packageName)
        val thermalStatus = deviceManager.getThermalStatus()
        val targetFps = getAdaptiveTargetFps(gameInfo, thermalStatus)
        val supportedRates = performanceManager.getSupportedRefreshRates()
        val maxRefreshRate = supportedRates.maxOrNull() ?: 60f
        val targetHz = getAdaptiveRefreshRate(maxRefreshRate, thermalStatus)
        val stableHz = performanceManager.getNearestSupportedRefreshRate(targetHz)

        try {
            val hasRoot = rootShellManager.isRootAvailable()

            if (hasRoot) {
                if (socInfo.isGamingOptimized) {
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
                performanceManager.optimizeNonRoot()
                appliedOptimizations.add("Non-Root Performance Mode")
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

                touchLatencyOptimizer.enableTouchOptimizations()
                touchLatencyOptimizer.enableHighFrequencyTouch()
                appliedOptimizations.add("Touch Latency Optimized")

                performanceManager.disableAnimations()
                appliedOptimizations.add("Animations Disabled")
            } else {
                appliedOptimizations.add("Thermal Protection: Limited optimizations")
            }

            networkManager.acquireWifiLowLatencyLock("GameBoost")
            appliedOptimizations.add("Low Latency Network Mode")

            val memFreed = deviceManager.killBackgroundApps()
            if (memFreed > 0) {
                appliedOptimizations.add("Memory Cleaned (${memFreed}MB freed)")
            }

            if (hasRoot) applySocSpecificOptimizations(socInfo)

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
        val result = startOptimization(packageName)
        val thermalStatus = deviceManager.getThermalStatus()
        if (thermalStatus >= PowerManager.THERMAL_STATUS_CRITICAL) {
            stopOptimization()
            val limitedResult = startOptimization(packageName)
            return limitedResult.copy(
                appliedOptimizations = limitedResult.appliedOptimizations + "Thermal Safe Mode Active"
            )
        }
        return result
    }

    suspend fun stopOptimization(): OptimizationResult {
        if (!isOptimizationActive) {
            return OptimizationResult(true, listOf("Not active"))
        }

        val restoredOptimizations = mutableListOf<String>()

        try {
            val hasRoot = rootShellManager.isRootAvailable()

            if (hasRoot) {
                performanceManager.setCpuGovernor("schedutil")
                restoredOptimizations.add("CPU Governor Restored")
                performanceManager.restoreCpuGpuPerformance()
                restoredOptimizations.add("GPU Settings Restored")
            } else {
                performanceManager.restoreNonRoot()
                restoredOptimizations.add("Non-Root Settings Restored")
            }

            performanceManager.restoreThreadPriority()
            restoredOptimizations.add("Thread Priority Restored")

            performanceManager.stopPerformanceSession()
            restoredOptimizations.add("ADPF Session Stopped")

            val defaultHz = performanceManager.getSupportedRefreshRates().firstOrNull() ?: 60f
            performanceManager.lockRefreshRate(defaultHz)
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
        } catch (e: Exception) {}

        isOptimizationActive = false
        currentGamePackage = null

        return OptimizationResult(success = true, appliedOptimizations = restoredOptimizations)
    }

    private suspend fun applySocSpecificOptimizations(socInfo: SocInfo) {
        if (!rootShellManager.isRootAvailable()) return
        withContext(Dispatchers.IO) {
            when (socInfo.socType) {
                SocType.SNAPDRAGON -> applySnapdragonOptimizations()
                SocType.MEDIATEK -> applyMediaTekOptimizations()
                SocType.EXYNOS -> applyExynosOptimizations()
                SocType.KIRIN -> applyKirinOptimizations()
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
    }

    private suspend fun applyMediaTekOptimizations() {
        rootShellManager.executeCommand("echo 1 > /sys/module/mtk_vcore_debug/parameters/enable")
        rootShellManager.executeCommand("echo 1 > /sys/devices/system/cpu/cpu0/cpufreq/game_mode")
        rootShellManager.executeCommand("echo 1 > /sys/kernel/ged/boost_gpu_enable")
    }

    private suspend fun applyExynosOptimizations() {
        rootShellManager.executeCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/gpu_governor")
        rootShellManager.executeCommand("echo 1 > /sys/devices/platform/gpu.0/devfreq/gpu.0/boost")
    }

    private suspend fun applyKirinOptimizations() {
        rootShellManager.executeCommand("echo 1 > /sys/class/dss/display/turbo")
        rootShellManager.executeCommand("echo 1 > /sys/kernel/hisi/npu/boost")
    }

    private fun getAdaptiveTargetFps(gameInfo: SupportedGames.GameInfo?, thermalStatus: Int): Int {
        val gameMax = gameInfo?.maxFps ?: 0
        val deviceMax = performanceManager.getMaxRefreshRate().toInt()
        val supported = performanceManager.getSupportedRefreshRates()
        val rawTarget = when {
            gameMax > 0 && gameMax <= deviceMax -> gameMax
            gameMax > 0 -> deviceMax
            deviceMax >= 165 -> 165
            deviceMax >= 144 -> 144
            deviceMax >= 120 -> 120
            deviceMax >= 90 -> 90
            else -> 60
        }
        val stableTarget = supported.minByOrNull { kotlin.math.abs(it - rawTarget.toFloat()) }?.toInt() ?: rawTarget
        return when {
            thermalStatus >= PowerManager.THERMAL_STATUS_SEVERE -> 30
            thermalStatus == PowerManager.THERMAL_STATUS_MODERATE -> minOf(stableTarget, 60)
            thermalStatus == PowerManager.THERMAL_STATUS_LIGHT -> minOf(stableTarget, 90)
            else -> stableTarget
        }
    }

    private fun getAdaptiveRefreshRate(maxRate: Float, thermalStatus: Int): Float {
        val supported = performanceManager.getSupportedRefreshRates()
        val stableRate = supported.minByOrNull { kotlin.math.abs(it - maxRate) } ?: maxRate
        return when {
            thermalStatus >= PowerManager.THERMAL_STATUS_SEVERE -> 30f
            thermalStatus == PowerManager.THERMAL_STATUS_MODERATE -> minOf(stableRate, 60f)
            thermalStatus == PowerManager.THERMAL_STATUS_LIGHT -> minOf(stableRate, 90f)
            else -> stableRate
        }
    }

    fun isOptimizationActive(): Boolean = isOptimizationActive
    fun getCurrentGamePackage(): String? = currentGamePackage

    fun getSupportedFps(): List<Int> = listOf(30, 45, 60, 90, 120, 144, 165, 240)

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
