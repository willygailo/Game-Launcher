package com.gamelauncher.core

import android.app.ActivityManager
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.PerformanceHintManager
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.view.Display
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlin.concurrent.Volatile
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rootShellManager: RootShellManager,
    private val socManager: SocManager
) {
    @Volatile private var performanceSession: AutoCloseable? = null
    @Volatile private var wakeLock: PowerManager.WakeLock? = null

    fun boostThreadPriority() {
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY)
        } catch (_: Exception) {}
    }

    fun restoreThreadPriority() {
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT)
        } catch (_: Exception) {}
    }

    fun acquireWakeLock() {
        try {
            val pm = context.getSystemService(PowerManager::class.java) ?: return
            if (wakeLock?.isHeld == true) return
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
                "GameLauncher:PerformanceLock"
            )
            wakeLock?.acquire(30_000L)
        } catch (_: Exception) {}
    }

    fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) wakeLock?.release()
            wakeLock = null
        } catch (_: Exception) {}
    }

    // ── ADPF v1: PerformanceHintManager (Android 12+ / API 31+) ─────
    fun startPerformanceSession(targetFpsHz: Int = 60) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        try {
            val phm = context.getSystemService(PerformanceHintManager::class.java) ?: return
            val periodNs = 1_000_000_000L / targetFpsHz
            val tids = intArrayOf(Process.myTid())
            performanceSession?.close()
            performanceSession = phm.createHintSession(tids, periodNs)
        } catch (_: Exception) {}
    }

    fun reportFrameTime(actualFrameNs: Long) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        try {
            val session = performanceSession ?: return
            session.javaClass.getMethod("reportActualWorkDuration", Long::class.java)
                .invoke(session, actualFrameNs)
        } catch (_: Exception) {}
    }

    fun stopPerformanceSession() {
        try {
            performanceSession?.close()
            performanceSession = null
        } catch (_: Exception) {}
    }

    // ── Display Refresh Rate ─────────────────────────────────────────
    fun getSupportedRefreshRates(): List<Float> {
        return try {
            val dm = context.getSystemService(DisplayManager::class.java)
            val display = dm?.getDisplay(Display.DEFAULT_DISPLAY) ?: return listOf(60f)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                display.supportedModes.map { it.refreshRate }.distinct().sorted()
            } else {
                listOf(display.refreshRate)
            }
        } catch (e: Exception) { listOf(60f) }
    }

    fun getCurrentRefreshRate(): Float {
        return try {
            val dm = context.getSystemService(DisplayManager::class.java)
            dm?.getDisplay(Display.DEFAULT_DISPLAY)?.refreshRate ?: 60f
        } catch (e: Exception) { 60f }
    }

    fun getMaxRefreshRate(): Float {
        return getSupportedRefreshRates().maxOrNull() ?: 60f
    }

    // ── Enhanced GPU Info for All Vendors ────────────────────────────
    fun getGpuRenderer(): String {
        val socInfo = socManager.getSocInfo()
        val paths = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpu_model",
            "/sys/class/kgsl/kgsl-3d0/model",
            "/sys/kernel/gpu/gpu_model",
            "/sys/devices/1c00000.mali/mali_model",
            "/sys/module/mali_dvfs/parameters/mali_model",
            "/sys/devices/platform/mali.0/devfreq/mali.0/cur_freq",
            "/sys/devices/platform/gpu.0/devfreq/gpu.0/cur_freq"
        )
        for (path in paths) {
            val gpu = runCatching { File(path).readText().trim() }.getOrNull()
            if (!gpu.isNullOrBlank()) {
                return formatGpuName(gpu, socInfo.gpuVendor)
            }
        }
        return when (socInfo.gpuVendor) {
            GpuVendor.ADRENO -> "Adreno GPU"
            GpuVendor.MALI -> "Mali GPU"
            GpuVendor.POWERVR -> "PowerVR GPU"
            GpuVendor.IMMORTAL -> "Immortalis GPU"
            GpuVendor.UNKNOWN -> "Unknown GPU"
        }
    }

    private fun formatGpuName(gpu: String, vendor: GpuVendor): String {
        val cleaned = gpu.trim()
        return when (vendor) {
            GpuVendor.ADRENO -> {
                val numPattern = Regex("(\\d+)")
                val match = numPattern.find(cleaned)
                if (match != null) "Adreno ${match.value}" else cleaned
            }
            GpuVendor.MALI -> {
                val maliPattern = Regex("Mali-G(\\d+)")
                val match = maliPattern.find(cleaned)
                if (match != null) "Mali-G${match.groupValues[1]}" else cleaned
            }
            else -> cleaned
        }
    }

    fun getGpuFreqMhz(): Long {
        val socInfo = socManager.getSocInfo()
        val paths = when (socInfo.gpuVendor) {
            GpuVendor.ADRENO -> listOf(
                "/sys/class/kgsl/kgsl-3d0/gpuclk",
                "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage"
            )
            GpuVendor.MALI -> listOf(
                "/sys/devices/1c00000.mali/devfreq/1c00000.mali/cur_freq",
                "/sys/devices/platform/mali.0/devfreq/mali.0/cur_freq"
            )
            else -> listOf(
                "/sys/class/kgsl/kgsl-3d0/gpuclk",
                "/sys/kernel/gpu/gpu_clock"
            )
        }
        return try {
            paths.firstNotNullOfOrNull { path ->
                runCatching { File(path).readText().trim().toLongOrNull() }.getOrNull()
            }?.let { freq ->
                if (freq > 1_000_000) freq / 1_000_000 else freq
            } ?: 0L
        } catch (e: Exception) { 0L }
    }

    fun getGpuUsagePercent(): Float {
        val socInfo = socManager.getSocInfo()
        val paths = when (socInfo.gpuVendor) {
            GpuVendor.ADRENO -> listOf(
                "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage",
                "/sys/class/kgsl/kgsl-3d0/utilization"
            )
            GpuVendor.MALI -> listOf(
                "/sys/devices/1c00000.mali/devfreq/1c00000.mali/load",
                "/sys/kernel/debug/mali0/utilization"
            )
            else -> emptyList()
        }
        return try {
            paths.firstNotNullOfOrNull { path ->
                runCatching { File(path).readText().trim().toFloatOrNull() }.getOrNull()
            }?.coerceIn(0f, 100f) ?: 0f
        } catch (e: Exception) { 0f }
    }

    // ── Animation Scale Toggle ───────────────────────────────────────
    fun disableAnimations() {
        try { Settings.Global.putFloat(context.contentResolver, "window_animation_scale", 0f) } catch (_: Exception) {}
        try { Settings.Global.putFloat(context.contentResolver, "transition_animation_scale", 0f) } catch (_: Exception) {}
        try { Settings.Global.putFloat(context.contentResolver, "animator_duration_scale", 0f) } catch (_: Exception) {}
    }

    fun restoreAnimations() {
        try { Settings.Global.putFloat(context.contentResolver, "window_animation_scale", 1f) } catch (_: Exception) {}
        try { Settings.Global.putFloat(context.contentResolver, "transition_animation_scale", 1f) } catch (_: Exception) {}
        try { Settings.Global.putFloat(context.contentResolver, "animator_duration_scale", 1f) } catch (_: Exception) {}
    }

    // ── Non-Root Performance Optimizations ──────────────────────────
    fun forceGpuRendering() {
        try {
            Settings.Global.putInt(context.contentResolver, "force_gpu_rendering", 1)
        } catch (_: Exception) {}
        try {
            Settings.System.putInt(context.contentResolver, "force_hw_ui", 1)
        } catch (_: Exception) {}
        try {
            Settings.Global.putString(context.contentResolver, "hwui.renderer", "opengl")
        } catch (_: Exception) {}
    }

    fun restoreGpuRendering() {
        try {
            Settings.Global.putInt(context.contentResolver, "force_gpu_rendering", 0)
        } catch (_: Exception) {}
        try {
            Settings.System.putInt(context.contentResolver, "force_hw_ui", 0)
        } catch (_: Exception) {}
    }

    fun setHighPerformanceMode() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val pm = context.getSystemService(PowerManager::class.java)
                pm?.let {
                    it.javaClass.getMethod("setPowerMode", Int::class.java, Boolean::class.java)
                        .invoke(it, 4, true)
                }
            }
        } catch (_: Exception) {}
    }

    fun optimizeNonRoot() {
        boostThreadPriority()
        acquireWakeLock()
        disableAnimations()
        forceGpuRendering()
        setHighPerformanceMode()
    }

    fun restoreNonRoot() {
        restoreThreadPriority()
        releaseWakeLock()
        restoreAnimations()
        restoreGpuRendering()
    }

    // ── Root Hardware Tuning ─────────────────────────────────────────
    suspend fun setCpuGovernor(governor: String): Boolean = withContext(Dispatchers.IO) {
        if (!rootShellManager.isRootAvailable()) return@withContext false
        val cmd = "for i in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo $governor > \$i; done"
        val (success, _) = rootShellManager.executeCommand(cmd)
        success
    }

    suspend fun optimizeStorageFstrim(): Boolean = withContext(Dispatchers.IO) {
        if (rootShellManager.isRootAvailable()) {
            val (success, _) = rootShellManager.executeCommand("sm fstrim")
            if (success) return@withContext true
        }
        System.gc()
        runCatching {
            val files = listOf("/cache", "/data")
            files.forEach { path ->
                runCatching { File(path).listFiles()?.firstOrNull()?.delete() }
            }
            true
        }.getOrDefault(false)
    }

    suspend fun setAdaptiveCpuGov(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        if (!rootShellManager.isRootAvailable()) {
            if (enabled) boostThreadPriority()
            else restoreThreadPriority()
            return@withContext true
        }
        val gov = if (enabled) "performance" else "schedutil"
        setCpuGovernor(gov)
    }

    // ── Hidden Power Unlock (Root Only) ─────────────────────────────
    suspend fun unlockHiddenPower(): Boolean = withContext(Dispatchers.IO) {
        if (!rootShellManager.isRootAvailable()) return@withContext false
        val commands = listOf(
            "echo deadline > /sys/block/sda/queue/scheduler",
            "echo deadline > /sys/block/sdb/queue/scheduler",
            "echo deadline > /sys/block/mmcblk0/queue/scheduler",
            "echo 0 > /sys/block/sda/queue/iostats",
            "echo 0 > /sys/block/sdb/queue/iostats",
            "echo 2048 > /sys/block/sda/queue/read_ahead_kb",
            "echo 2048 > /sys/block/sdb/queue/read_ahead_kb",
            "echo 2048 > /sys/block/mmcblk0/queue/read_ahead_kb",
            "echo 0 > /proc/sys/vm/swappiness",
            "echo 100 > /proc/sys/vm/vfs_cache_pressure",
            "echo 10 > /proc/sys/vm/dirty_ratio",
            "echo 5 > /proc/sys/vm/dirty_background_ratio",
            "echo 0 > /proc/sys/vm/page-cluster",
            "echo 4096 > /proc/sys/vm/min_free_kbytes",
            "echo 0 > /proc/sys/vm/compact_memory",
            "echo bbr > /proc/sys/net/ipv4/tcp_congestion_control",
            "echo 1 > /proc/sys/net/ipv4/tcp_fastopen",
            "echo 1 > /proc/sys/net/ipv4/tcp_low_latency",
            "echo 0 > /proc/sys/net/ipv4/tcp_slow_start_after_idle",
            "echo 0 > /sys/module/logger/parameters/log_mode",
            "echo 0 > /sys/kernel/debug/tracing/tracing_on"
        )
        for (cmd in commands) {
            rootShellManager.executeCommand(cmd)
        }
        val props = listOf(
            "debug.hwui.overdraw" to "false",
            "debug.performance.tuning" to "1",
            "ro.sys.fw.bg_apps_limit" to "3",
            "persist.sys.purgeable_assets" to "1",
            "ro.hwui.texture_cache_size" to "144",
            "ro.hwui.layer_cache_size" to "96",
            "ro.hwui.path_cache_size" to "64",
            "ro.hwui.drop_shadow_cache_size" to "12",
            "debug.sf.latch_unsignaled" to "1",
            "debug.hwui.target_cpu_time_percent" to "80",
            "persist.sys.gamemode" to "1"
        )
        for ((key, value) in props) {
            rootShellManager.executeCommand("setprop $key $value")
        }
        true
    }

    suspend fun restoreHiddenPower(): Boolean = withContext(Dispatchers.IO) {
        if (!rootShellManager.isRootAvailable()) return@withContext false
        val restoreProps = listOf(
            "debug.hwui.overdraw" to "false",
            "debug.performance.tuning" to "0",
            "debug.sf.latch_unsignaled" to "0",
            "persist.sys.gamemode" to "0",
            "vendor.perf.gaming.driver" to "0",
            "vendor.perf.gaming.scheduler" to "0"
        )
        for ((key, value) in restoreProps) {
            rootShellManager.executeCommand("setprop $key $value")
        }
        true
    }

    // ── Max Performance & FPS/HZ Lock ────────────────────────────────
    suspend fun maximizeCpuGpuPerformance(): Boolean = withContext(Dispatchers.IO) {
        if (!rootShellManager.isRootAvailable()) return@withContext false
        var success = true
        success = success && unlockHiddenPower()
        success = success && setCpuGovernor("performance")

        val coreCount = Runtime.getRuntime().availableProcessors()
        for (i in 0 until coreCount) {
            rootShellManager.executeCommand("echo performance > /sys/devices/system/cpu/cpu$i/cpufreq/scaling_governor")
            val (_, maxFreqStr) = rootShellManager.executeCommand("cat /sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq")
            if (maxFreqStr.isNotBlank() && maxFreqStr.trim().toLongOrNull() != null) {
                rootShellManager.executeCommand("echo ${maxFreqStr.trim()} > /sys/devices/system/cpu/cpu$i/cpufreq/scaling_max_freq")
                rootShellManager.executeCommand("echo ${maxFreqStr.trim()} > /sys/devices/system/cpu/cpu$i/cpufreq/scaling_min_freq")
            }
        }
        rootShellManager.executeCommand("echo 1 > /sys/devices/system/cpu/cpu0/cpufreq/boost")
        rootShellManager.executeCommand("echo 1 > /sys/module/cpu_boost/parameters/boost_ms")
        rootShellManager.executeCommand("echo 1 > /sys/module/cpu_boost/parameters/input_boost_ms")
        rootShellManager.executeCommand("echo 0 > /sys/class/thermal/thermal_zone*/mode")

        val socInfo = socManager.getSocInfo()
        when (socInfo.gpuVendor) {
            GpuVendor.ADRENO -> {
                rootShellManager.executeCommand("echo performance > /sys/class/kgsl/kgsl-3d0/devfreq/governor")
                rootShellManager.executeCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/force_no_nap")
                rootShellManager.executeCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/force_bus_on")
                rootShellManager.executeCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/force_rail_on")
                rootShellManager.executeCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/force_clk_on")
                rootShellManager.executeCommand("echo 10000000 > /sys/class/kgsl/kgsl-3d0/idle_timer")
                rootShellManager.executeCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/bus_split")
                rootShellManager.executeCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/throttling")
                rootShellManager.executeCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/popp")
                rootShellManager.executeCommand("echo 0 > /sys/class/kgsl/kgsl-3d0/pwrnap")
            }
            GpuVendor.MALI -> {
                rootShellManager.executeCommand("echo performance > /sys/devices/platform/mali.0/devfreq/mali.0/governor")
                rootShellManager.executeCommand("echo 1 > /sys/module/mali/parameters/force_no_nap")
                rootShellManager.executeCommand("echo 0 > /sys/module/mali_kbase/parameters/mali_gpu_clock_off")
                rootShellManager.executeCommand("echo 1 > /sys/module/mali_kbase/parameters/mali_job_cycle")
                rootShellManager.executeCommand("echo 1 > /sys/module/mali_kbase/parameters/mali_defer_job_submission")
            }
            GpuVendor.IMMORTAL -> {
                rootShellManager.executeCommand("echo performance > /sys/devices/platform/mali.0/devfreq/mali.0/governor")
                rootShellManager.executeCommand("echo 0 > /sys/module/mali_kbase/parameters/mali_gpu_clock_off")
            }
            GpuVendor.POWERVR -> {
                rootShellManager.executeCommand("echo performance > /sys/kernel/gpu/gpu_governor")
                rootShellManager.executeCommand("echo 1 > /sys/kernel/gpu/gpu_busy")
            }
            GpuVendor.UNKNOWN -> {
                rootShellManager.executeCommand("echo performance > /sys/class/kgsl/kgsl-3d0/devfreq/governor")
                rootShellManager.executeCommand("echo performance > /sys/devices/platform/mali.0/devfreq/mali.0/governor")
                rootShellManager.executeCommand("echo performance > /sys/kernel/gpu/gpu_governor")
            }
        }
        success
    }

    suspend fun restoreCpuGpuPerformance(): Boolean = withContext(Dispatchers.IO) {
        if (!rootShellManager.isRootAvailable()) return@withContext false
        setCpuGovernor("schedutil")
        val socInfo = socManager.getSocInfo()
        when (socInfo.gpuVendor) {
            GpuVendor.ADRENO -> {
                rootShellManager.executeCommand("echo 0 > /sys/class/kgsl/kgsl-3d0/force_no_nap")
                rootShellManager.executeCommand("echo 0 > /sys/class/kgsl/kgsl-3d0/force_bus_on")
                rootShellManager.executeCommand("echo 0 > /sys/class/kgsl/kgsl-3d0/force_rail_on")
                rootShellManager.executeCommand("echo 0 > /sys/class/kgsl/kgsl-3d0/force_clk_on")
                rootShellManager.executeCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/pwrnap")
            }
            GpuVendor.MALI -> {
                rootShellManager.executeCommand("echo 0 > /sys/module/mali/parameters/force_no_nap")
            }
            else -> {}
        }
        restoreHiddenPower()
        true
    }

    fun getNearestSupportedRefreshRate(targetHz: Float): Float {
        return getSupportedRefreshRates().minByOrNull { kotlin.math.abs(it - targetHz) } ?: 60f
    }

    fun lockRefreshRate(targetHz: Float): Boolean {
        return runCatching {
            val supported = getSupportedRefreshRates()
            val nearestHz = supported.minByOrNull { kotlin.math.abs(it - targetHz) } ?: 60f
            try {
                Settings.System.putFloat(context.contentResolver, "peak_refresh_rate", nearestHz)
                Settings.System.putFloat(context.contentResolver, "min_refresh_rate", nearestHz)
            } catch (_: Exception) {}
            true
        }.getOrDefault(false)
    }

    suspend fun lockFps(targetFps: Int) = withContext(Dispatchers.IO) {
        startPerformanceSession(targetFps)
        lockRefreshRate(targetFps.toFloat())
        if (rootShellManager.isRootAvailable()) {
            val props = listOf(
                "persist.sys.app.fps" to "$targetFps",
                "persist.vendor.dfps.level" to "$targetFps"
            )
            for ((key, value) in props) {
                rootShellManager.executeCommand("setprop $key $value")
            }
        }
    }

    suspend fun restoreFps() = withContext(Dispatchers.IO) {
        stopPerformanceSession()
        if (rootShellManager.isRootAvailable()) {
            val restoreProps = listOf(
                "persist.sys.app.fps" to "",
                "persist.vendor.dfps.level" to ""
            )
            for ((key, value) in restoreProps) {
                rootShellManager.executeCommand("setprop $key $value")
            }
        }
    }
}
