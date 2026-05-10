package com.gamelauncher.core

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.PowerManager
import android.view.Display
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        val errors: List<String> = emptyList()
    )

    private var isOptimizationActive = false
    private var currentGamePackage: String? = null

    suspend fun startOptimization(packageName: String): OptimizationResult {
        if (isOptimizationActive) {
            return OptimizationResult(true, listOf("Already optimized"))
        }

        isOptimizationActive = true
        currentGamePackage = packageName

        val appliedOptimizations = mutableListOf<String>()
        val errors = mutableListOf<String>()

        try {
            val socInfo = socManager.getSocInfo()
            val gameInfo = SupportedGames.findGame(packageName)
            val targetFps = gameInfo?.maxFps ?: detectMaxFps()
            val maxRefreshRate = performanceManager.getSupportedRefreshRates().maxOrNull() ?: 60f

            val cpuResult = performanceManager.maximizeCpuGpuPerformance()
            if (cpuResult) {
                appliedOptimizations.add("CPU/GPU Performance Boost (${socInfo.socType.name})")
            } else {
                errors.add("CPU/GPU boost not available (requires root)")
            }

            performanceManager.boostThreadPriority()
            appliedOptimizations.add("Thread Priority Boost")

            performanceManager.startPerformanceSession(targetFps)
            appliedOptimizations.add("Performance Session (${targetFps} FPS)")

            val refreshResult = performanceManager.lockRefreshRate(maxRefreshRate)
            if (refreshResult) {
                appliedOptimizations.add("Refresh Rate Locked (${maxRefreshRate}Hz)")
            } else {
                appliedOptimizations.add("Refresh Rate (limited - non-root)")
            }

            performanceManager.lockFps(targetFps)
            appliedOptimizations.add("FPS Target Locked (${targetFps} FPS)")

            val dndResult = dndManager.enableGamingDnd()
            if (dndResult) {
                appliedOptimizations.add("Do Not Disturb Enabled")
            } else if (!dndManager.isDndPermissionGranted()) {
                errors.add("DND permission not granted")
            }

            touchLatencyOptimizer.enableTouchOptimizations()
            touchLatencyOptimizer.enableHighFrequencyTouch()
            appliedOptimizations.add("Touch Latency Optimized")

            networkManager.acquireWifiLowLatencyLock("GameBoost")
            appliedOptimizations.add("Low Latency Network Mode")

            performanceManager.disableAnimations()
            appliedOptimizations.add("Animations Disabled")

            val memFreed = deviceManager.killBackgroundApps()
            if (memFreed > 0) {
                appliedOptimizations.add("Memory Cleaned (${memFreed}MB freed)")
            }

            applySocSpecificOptimizations(socInfo)

        } catch (e: Exception) {
            errors.add("Error: ${e.message}")
        }

        return OptimizationResult(
            success = appliedOptimizations.isNotEmpty(),
            appliedOptimizations = appliedOptimizations,
            errors = errors
        )
    }

    suspend fun stopOptimization(): OptimizationResult {
        if (!isOptimizationActive) {
            return OptimizationResult(true, listOf("Not active"))
        }

        val restoredOptimizations = mutableListOf<String>()

        try {
            performanceManager.setCpuGovernor("schedutil")
            restoredOptimizations.add("CPU Governor Restored")

            performanceManager.restoreThreadPriority()
            restoredOptimizations.add("Thread Priority Restored")

            performanceManager.stopPerformanceSession()
            restoredOptimizations.add("Performance Session Stopped")

            val defaultHz = performanceManager.getSupportedRefreshRates().firstOrNull() ?: 60f
            performanceManager.lockRefreshRate(defaultHz)
            restoredOptimizations.add("Refresh Rate Restored (${defaultHz}Hz)")

            dndManager.disableGamingDnd()
            restoredOptimizations.add("DND Disabled")

            touchLatencyOptimizer.disableTouchOptimizations()
            touchLatencyOptimizer.disableHighFrequencyTouch()
            restoredOptimizations.add("Touch Latency Restored")

            networkManager.releaseWifiLock()
            restoredOptimizations.add("Network Lock Released")

            performanceManager.restoreAnimations()
            restoredOptimizations.add("Animations Restored")

        } catch (e: Exception) {
        }

        isOptimizationActive = false
        currentGamePackage = null

        return OptimizationResult(
            success = true,
            appliedOptimizations = restoredOptimizations
        )
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
    }

    private suspend fun applyMediaTekOptimizations() {
        rootShellManager.executeCommand("echo 1 > /sys/module/mtk_vcore_debug/parameters/enable")
        rootShellManager.executeCommand("echo 1 > /sys/devices/system/cpu/cpu0/cpufreq/game_mode")
    }

    private suspend fun applyExynosOptimizations() {
        rootShellManager.executeCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/gpu_governor")
    }

    private suspend fun applyKirinOptimizations() {
        rootShellManager.executeCommand("echo 1 > /sys/class/dss/display/turbo")
    }

    private fun detectMaxFps(): Int {
        return try {
            val display = context.getSystemService(DisplayManager::class.java)
                ?.getDisplay(Display.DEFAULT_DISPLAY)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val maxRefresh = display?.supportedModes?.maxOfOrNull { it.refreshRate } ?: 60f
                maxRefresh.toInt()
            } else {
                60
            }
        } catch (_: Exception) { 60 }
    }

    fun isOptimizationActive(): Boolean = isOptimizationActive

    fun getCurrentGamePackage(): String? = currentGamePackage

    fun getSupportedFps(): List<Int> {
        return listOf(30, 45, 60, 90, 120, 144, 165)
    }

    fun getSupportedRefreshRates(): List<Float> {
        return performanceManager.getSupportedRefreshRates()
    }

    suspend fun getDeviceThermalStatus(): Int {
        return deviceManager.getThermalStatus()
    }

    fun isThermalThrottling(): Boolean {
        return try {
            val powerManager = context.getSystemService(PowerManager::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val status = powerManager?.currentThermalStatus ?: PowerManager.THERMAL_STATUS_NONE
                status > PowerManager.THERMAL_STATUS_LIGHT
            } else false
        } catch (_: Exception) { false }
    }
}