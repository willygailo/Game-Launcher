package com.gamelauncher.core

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.PerformanceHintManager
import android.os.Process
import android.view.Display
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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
    private var performanceSession: AutoCloseable? = null

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

    // ── PerformanceHintManager (Android 12+ / API 31+) ───────────────────
    fun startPerformanceSession(targetFpsHz: Int = 60) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        try {
            val phm = context.getSystemService(PerformanceHintManager::class.java) ?: return
            val periodNs = (1_000_000_000L / targetFpsHz)
            val tids = intArrayOf(Process.myTid())
            performanceSession?.close()
            performanceSession = phm.createHintSession(tids, periodNs)
        } catch (_: Exception) {}
    }

    fun reportFrameTime(actualFrameNs: Long) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        try {
            val session = performanceSession ?: return
            // PerformanceHintManager.Session.reportActualWorkDuration
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

    // ── Display Refresh Rate ─────────────────────────────────────────────
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

    // ── Enhanced GPU Info for All Vendors ───────────────────────────────
    fun getGpuRenderer(): String {
        val socInfo = socManager.getSocInfo()
        
        val paths = mutableListOf(
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

    // ── Animation Scale Toggle ───────────────────────────────────────────
    fun disableAnimations() {
        // Global animation scale requires elevated privileges on most devices.
        runRootCommand("settings put global window_animation_scale 0")
        runRootCommand("settings put global transition_animation_scale 0")
        runRootCommand("settings put global animator_duration_scale 0")
    }

    fun restoreAnimations() {
        runRootCommand("settings put global window_animation_scale 1")
        runRootCommand("settings put global transition_animation_scale 1")
        runRootCommand("settings put global animator_duration_scale 1")
    }

    // ── Root Hardware Tuning ─────────────────────────────────────────────
    suspend fun setCpuGovernor(governor: String): Boolean = withContext(Dispatchers.IO) {
        if (!rootShellManager.isRootAvailable()) return@withContext false
        val cmd = "for i in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo $governor > \$i; done"
        val (success, _) = rootShellManager.executeCommand(cmd)
        success
    }

    suspend fun optimizeStorageFstrim(): Boolean = withContext(Dispatchers.IO) {
        if (!rootShellManager.isRootAvailable()) return@withContext false
        val (success, _) = rootShellManager.executeCommand("sm fstrim")
        success
    }

    // ── Hidden Power Unlock (Android 15/16 Deep System Tweaks) ───
    suspend fun unlockHiddenPower(): Boolean = withContext(Dispatchers.IO) {
        if (!rootShellManager.isRootAvailable()) return@withContext false

        // 1. Extreme I/O Tweaks (Faster load times)
        rootShellManager.executeCommand("echo deadline > /sys/block/sda/queue/scheduler")
        rootShellManager.executeCommand("echo deadline > /sys/block/sdb/queue/scheduler")
        rootShellManager.executeCommand("echo deadline > /sys/block/mmcblk0/queue/scheduler")
        rootShellManager.executeCommand("echo 0 > /sys/block/sda/queue/iostats")
        rootShellManager.executeCommand("echo 0 > /sys/block/sdb/queue/iostats")
        rootShellManager.executeCommand("echo 2048 > /sys/block/sda/queue/read_ahead_kb")
        rootShellManager.executeCommand("echo 2048 > /sys/block/sdb/queue/read_ahead_kb")

        // 2. RAM & VM Aggressive Tweaks (Keep games in memory, no swap overhead)
        rootShellManager.executeCommand("echo 0 > /proc/sys/vm/swappiness")
        rootShellManager.executeCommand("echo 100 > /proc/sys/vm/vfs_cache_pressure")
        rootShellManager.executeCommand("echo 10 > /proc/sys/vm/dirty_ratio")
        rootShellManager.executeCommand("echo 5 > /proc/sys/vm/dirty_background_ratio")
        rootShellManager.executeCommand("echo 0 > /proc/sys/vm/page-cluster")
        rootShellManager.executeCommand("echo 4096 > /proc/sys/vm/min_free_kbytes")
        
        // 3. Network TCP Tweaks for Gaming (Low Latency PUBG/COD/ML)
        rootShellManager.executeCommand("echo bbr > /proc/sys/net/ipv4/tcp_congestion_control")
        rootShellManager.executeCommand("echo 1 > /proc/sys/net/ipv4/tcp_fastopen")
        rootShellManager.executeCommand("echo 1 > /proc/sys/net/ipv4/tcp_low_latency")

        // 4. Disable Logging & Tracing (Frees CPU cycles)
        rootShellManager.executeCommand("echo 0 > /sys/module/logger/parameters/log_mode")
        rootShellManager.executeCommand("echo 0 > /sys/kernel/debug/tracing/tracing_on")
        rootShellManager.executeCommand("stop logd")
        rootShellManager.executeCommand("stop statsd")
        
        // 5. Hardware & Graphics Deep Overrides (Hidden Props)
        val hiddenProps = listOf(
            "debug.hwui.overdraw" to "false",
            "debug.performance.tuning" to "1",
            "video.accelerate.hw" to "1",
            "ro.kernel.android.checkjni" to "0",
            "ro.kernel.checkjni" to "0",
            "profiler.force_disable_err_rpt" to "1",
            "profiler.force_disable_ulog" to "1",
            "ro.config.nocheckin" to "1",
            "ro.sys.fw.bg_apps_limit" to "3",
            "persist.sys.purgeable_assets" to "1",
            "ro.hwui.texture_cache_size" to "144",
            "ro.hwui.layer_cache_size" to "96",
            "ro.hwui.path_cache_size" to "64",
            "ro.hwui.drop_shadow_cache_size" to "12",
            "debug.rs.default-CPU-driver" to "1",
            "debug.egl.profiler" to "0",
            "debug.egl.hw" to "1",
            "debug.sf.hw" to "1",
            "debug.sf.latch_unsignaled" to "1",
            // Unlocks 100% hidden power by forcing maximum performance state on hw modules
            "ro.config.hw_perf" to "true",
            "ro.config.hw_quickpoweron" to "true",
            "ro.media.enc.jpeg.quality" to "100"
        )
        for ((key, value) in hiddenProps) {
            rootShellManager.executeCommand("setprop $key $value")
            rootShellManager.executeCommand("resetprop $key $value")
        }

        true
    }

    // ── Max Performance & FPS/HZ Lock ──────────────────────────────
    suspend fun maximizeCpuGpuPerformance(): Boolean = withContext(Dispatchers.IO) {
        if (!rootShellManager.isRootAvailable()) return@withContext false

        var success = true
        // Unlock deep hidden power tweaks first
        success = success && unlockHiddenPower()

        // Set CPU governor to performance
        success = success && setCpuGovernor("performance")

        // Enhance 8 core or more CPU
        val coreCount = Runtime.getRuntime().availableProcessors()
        for (i in 0 until coreCount) {
            rootShellManager.executeCommand("echo performance > /sys/devices/system/cpu/cpu$i/cpufreq/scaling_governor")
            val (_, maxFreqStr) = rootShellManager.executeCommand("cat /sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq")
            if (maxFreqStr.isNotBlank() && maxFreqStr.trim().toLongOrNull() != null) {
                rootShellManager.executeCommand("echo ${maxFreqStr.trim()} > /sys/devices/system/cpu/cpu$i/cpufreq/scaling_max_freq")
                rootShellManager.executeCommand("echo ${maxFreqStr.trim()} > /sys/devices/system/cpu/cpu$i/cpufreq/scaling_min_freq")
            }
        }
        
        // Disable thermal throttling (if possible)
        rootShellManager.executeCommand("echo 0 > /sys/class/thermal/thermal_zone*/mode")

        // GPU max performance - vendor-specific paths
        val socInfo = socManager.getSocInfo()
        when (socInfo.gpuVendor) {
            GpuVendor.ADRENO -> {
                rootShellManager.executeCommand("echo performance > /sys/class/kgsl/kgsl-3d0/devfreq/governor")
                rootShellManager.executeCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/force_no_nap")
                rootShellManager.executeCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/force_bus_on")
                rootShellManager.executeCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/force_rail_on")
                rootShellManager.executeCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/force_clk_on")
                rootShellManager.executeCommand("echo 10000000 > /sys/class/kgsl/kgsl-3d0/idle_timer")
            }
            GpuVendor.MALI -> {
                rootShellManager.executeCommand("echo performance > /sys/devices/platform/mali.0/devfreq/mali.0/governor")
                rootShellManager.executeCommand("echo 1 > /sys/module/mali/parameters/force_no_nap")
                rootShellManager.executeCommand("echo 0 > /sys/module/mali_kbase/parameters/mali_gpu_clock_off")
            }
            GpuVendor.IMMORTAL -> {
                rootShellManager.executeCommand("echo performance > /sys/devices/platform/mali.0/devfreq/mali.0/governor")
            }
            GpuVendor.POWERVR -> {
                rootShellManager.executeCommand("echo performance > /sys/kernel/gpu/gpu_governor")
            }
            GpuVendor.UNKNOWN -> {
                // Try all common paths
                rootShellManager.executeCommand("echo performance > /sys/class/kgsl/kgsl-3d0/devfreq/governor")
                rootShellManager.executeCommand("echo performance > /sys/devices/platform/mali.0/devfreq/mali.0/governor")
                rootShellManager.executeCommand("echo performance > /sys/kernel/gpu/gpu_governor")
            }
        }

        success
    }

    fun lockRefreshRate(targetHz: Float): Boolean {
        return runCatching {
            val dm = context.getSystemService(DisplayManager::class.java) ?: return false
            val display = dm.getDisplay(Display.DEFAULT_DISPLAY) ?: return false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val mode = display.supportedModes.firstOrNull { it.refreshRate == targetHz }
                    ?: display.supportedModes.maxByOrNull { it.refreshRate }
                mode?.let {
                    // Try root method first (most reliable)
                    if (runCatching {
                        Runtime.getRuntime().exec(arrayOf("su", "-c", "echo test")).waitFor() == 0
                    }.getOrDefault(false)) {
                        runRootCommand("cmd display set-refresh-rate ${targetHz.toInt()}")
                        return true
                    }
                    // Fallback: Try Settings (works on some OEMs)
                    try {
                        android.provider.Settings.System.putFloat(
                            context.contentResolver,
                            "peak_refresh_rate",
                            targetHz
                        )
                        android.provider.Settings.System.putFloat(
                            context.contentResolver,
                            "min_refresh_rate",
                            targetHz
                        )
                    } catch (_: Exception) {}
                }
            }
            true
        }.getOrDefault(false)
    }

    suspend fun lockFps(targetFps: Int) = withContext(Dispatchers.IO) {
        startPerformanceSession(targetFps)
        // Additional FPS lock for games via system properties
        if (rootShellManager.isRootAvailable()) {
            val props = listOf(
                // Max FPS Unlock (Android 15/16 + Gaming)
                "debug.vr.fps" to "$targetFps",
                "persist.vr.fps" to "$targetFps",
                "ro.surface_flinger.max_refresh_rate" to "$targetFps",
                "ro.min_freq_0" to "$targetFps",
                "ro.vendor.display.sv.120" to "1",
                "ro.vendor.display.sv.144" to "1",
                "ro.vendor.display.sv.165" to "1",
                "ro.vendor.dfps.enable" to "true",
                "persist.sys.nvfps" to "1",
                "persist.sys.app.fps" to "$targetFps",
                "persist.vendor.dfps.level" to "$targetFps",
                "ro.vendor.df.effect.conflict" to "1",
                
                // Enhanced CPU/GPU Render
                "debug.hwui.renderer" to "vulkan", // Skia Vulkan for modern games
                "debug.hwui.skia_atrace_enabled" to "false",
                "debug.hwui.render_thread" to "true",
                "ro.config.hw_perf" to "true",
                "ro.config.hw_quickpoweron" to "true",
                "persist.sys.composition.type" to "gpu",
                "persist.sys.ui.hw" to "1",
                "debug.egl.hw" to "1",
                "debug.hwui.use_buffer_age" to "false",
                
                // ML / Game specific enhancements (PUBG, COD, etc.)
                "ro.vendor.qti.core_ctl_max_cpu" to "8",
                "ro.vendor.qti.core_ctl_min_cpu" to "8",
                "ro.surface_flinger.supports_background_blur" to "0",
                "debug.gralloc.gfx_ubwc_disable" to "0",
                "persist.sys.gaming_mode" to "1",
                "persist.sys.perf.topApp" to "1",

                // Display Settings
                "ro.surface_flinger.max_frame_buffer_acquired_buffers" to "3",
                "debug.sf.showupdates" to "0",
                "debug.sf.showcpu" to "0",
                "debug.sf.showbackground" to "0",
                "debug.sf.showfps" to "0",
                "ro.vendor.gki.defconfig" to "performance",
                "vendor.display.disable_dynamic_fps" to "1",
                "vendor.display.enable_optimize_refresh" to "0",
                "ro.surface_flinger.use_smart_90_for_video" to "0",
                "ro.surface_flinger.set_idle_timer_ms" to "0",
                "ro.surface_flinger.set_touch_timer_ms" to "0",
                "ro.surface_flinger.set_display_power_timer_ms" to "0"
            )
            for ((key, value) in props) {
                rootShellManager.executeCommand("setprop $key $value")
                rootShellManager.executeCommand("resetprop $key $value") // If Magisk/KernelSU resetprop is available
            }
        }
    }

    private fun runRootCommand(command: String): Boolean {
        return runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            process.waitFor() == 0
        }.getOrDefault(false)
    }
}
