package com.gamelauncher.core

import android.Manifest
import android.app.ActivityManager
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.PerformanceHintManager
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.view.Display
import android.view.Surface
import com.gamelauncher.data.preference.SettingsPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlin.concurrent.Volatile
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rootShellManager: RootShellManager,
    private val socManager: SocManager,
    private val settingsPreferences: SettingsPreferences
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

    // ── ADPF v1 + v2: PerformanceHintManager (Android 12+ / API 31+) ─────
    fun startPerformanceSession(targetFpsHz: Int = 60) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        try {
            val phm = context.getSystemService(PerformanceHintManager::class.java) ?: return
            val periodNs = 1_000_000_000L / targetFpsHz
            
            // Gather all active thread IDs of our process dynamically to boost overlay, rendering, and database tasks together
            val taskDir = File("/proc/self/task")
            val tids = if (taskDir.exists() && taskDir.isDirectory) {
                taskDir.listFiles()?.mapNotNull { it.name.toIntOrNull() }?.toIntArray() ?: intArrayOf(Process.myTid())
            } else {
                intArrayOf(Process.myTid())
            }
            
            performanceSession?.close()
            performanceSession = phm.createHintSession(tids, periodNs)

            // ADPF v2: Set preferred update rate for SurfaceFlinger (Android 15+)
            if (Build.VERSION.SDK_INT >= 35) {
                try {
                    val sf = Class.forName("android.view.SurfaceControl")
                    val setRate = sf.getMethod("setDisplayPowerMode", Int::class.java, Int::class.java)
                } catch (_: Exception) {}
            }
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

    fun getAdpfPreferredRate(): Float {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return 60f
        return try {
            val phm = context.getSystemService(PerformanceHintManager::class.java) ?: return 60f
            phm.preferredUpdateRateNanos?.let { 1_000_000_000f / it } ?: 60f
        } catch (_: Exception) { 60f }
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

    // Cache for original system settings states (restored after boost stops)
    @Volatile private var originalLowPowerMode: Int? = null
    @Volatile private var originalMasterSync: Boolean? = null
    @Volatile private var originalMobileDataAlwaysOn: Int? = null
    @Volatile private var originalLocationMode: Int? = null
    @Volatile private var originalWindowAnimScale: Float? = null
    @Volatile private var originalTransitionAnimScale: Float? = null
    @Volatile private var originalAnimatorDurationScale: Float? = null
    @Volatile private var originalGameDriverOptInApps: String? = null
    @Volatile private var originalMobileDataAlwaysOnPrev: Int? = null

    // Extended non-root backup properties
    @Volatile private var originalWifiScanAlwaysEnabled: Int? = null
    @Volatile private var originalBleScanAlwaysEnabled: Int? = null
    @Volatile private var originalWifiPowerSave: Int? = null
    @Volatile private var originalWifiLatencyMode: Int? = null
    @Volatile private var originalWifiLowLatencyMode: Int? = null
    @Volatile private var originalPointerSpeed: Int? = null
    @Volatile private var originalTouchSensitivity: Int? = null
    @Volatile private var originalTouchReportRate: Int? = null
    @Volatile private var originalStylusTouchBoost: Int? = null
    @Volatile private var originalTouchBoost: Int? = null
    @Volatile private var originalPhantomProcsMonitor: String? = null
    @Volatile private var originalGameDriverAllApps: Int? = null
    @Volatile private var originalGameDriverFps: Int? = null
    @Volatile private var originalUpdatableDriverAllApps: Int? = null
    @Volatile private var originalUpdatableDriverOptInApps: String? = null
    
    // Refresh rate locking original states
    @Volatile private var originalSamsungRefreshMode: Int? = null
    @Volatile private var originalOneplusRefreshRate: Int? = null
    @Volatile private var originalXiaomiUserRefreshRate: Int? = null
    @Volatile private var originalXiaomiPeakRefreshRate: Float? = null
    @Volatile private var originalHuaweiSmartRefreshRate: Int? = null
    @Volatile private var originalRealmePeakRefreshRate: Float? = null
    @Volatile private var originalRealmeMinRefreshRate: Float? = null
    @Volatile private var originalAospPeakRefreshRate: Float? = null
    @Volatile private var originalAospMinRefreshRate: Float? = null

    /**
     * Force mobile data always-on so the device maintains a cellular background
     * connection even while on WiFi — prevents the WiFi→data handoff lag spike.
     * Requires WRITE_SECURE_SETTINGS.
     */
    fun forceMobileDataAlwaysOn() {
        if (!hasSecureSettingsPermission()) return
        try {
            val resolver = context.contentResolver
            originalMobileDataAlwaysOnPrev = Settings.Global.getInt(resolver, "mobile_data_always_on", 0)
            Settings.Global.putInt(resolver, "mobile_data_always_on", 1)
            // Also tell the network stack to keep WiFi + LTE both alive
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Settings.Global.putInt(resolver, "wifi_is_usable_score_cache_timeout_millis", 5000)
            }
        } catch (_: Exception) {}
    }

    fun restoreMobileDataAlwaysOn() {
        if (!hasSecureSettingsPermission()) return
        try {
            originalMobileDataAlwaysOnPrev?.let {
                Settings.Global.putInt(context.contentResolver, "mobile_data_always_on", it)
            }
            originalMobileDataAlwaysOnPrev = null
        } catch (_: Exception) {}
    }

    fun hasSecureSettingsPermission(): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
    }

    fun canWriteSystemSettings(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.System.canWrite(context)
    }

    suspend fun optimizeNonRoot(packageName: String) {
        boostThreadPriority()
        acquireWakeLock()
        
        val resolver = context.contentResolver
        val hasSecurePerm = hasSecureSettingsPermission()

        if (hasSecurePerm) {
            try {
                // 1. Disable Animations globally if enabled in preferences
                if (settingsPreferences.secureSettingsAnimScale.first()) {
                    originalWindowAnimScale = Settings.Global.getFloat(resolver, "window_animation_scale", 1.0f)
                    originalTransitionAnimScale = Settings.Global.getFloat(resolver, "transition_animation_scale", 1.0f)
                    originalAnimatorDurationScale = Settings.Global.getFloat(resolver, "animator_duration_scale", 1.0f)
                    
                    Settings.Global.putFloat(resolver, "window_animation_scale", 0.0f)
                    Settings.Global.putFloat(resolver, "transition_animation_scale", 0.0f)
                    Settings.Global.putFloat(resolver, "animator_duration_scale", 0.0f)
                }
            } catch (_: Exception) {}

            try {
                // 2. Force Battery/Power Saver Mode OFF
                if (settingsPreferences.secureSettingsBatterySaver.first()) {
                    originalLowPowerMode = Settings.Global.getInt(resolver, "low_power", 0)
                    Settings.Global.putInt(resolver, "low_power", 0)
                }
            } catch (_: Exception) {}

            try {
                // 3. Keep Mobile Data Always On (for fast WiFi-mobile handoff / dual-net stability)
                if (settingsPreferences.secureSettingsMobileData.first()) {
                    originalMobileDataAlwaysOn = Settings.Global.getInt(resolver, "mobile_data_always_on", 0)
                    Settings.Global.putInt(resolver, "mobile_data_always_on", 1)
                }
            } catch (_: Exception) {}

            try {
                // 4. Disable Master Auto-Sync temporarily to avoid background sync lag/ping spikes
                if (settingsPreferences.secureSettingsSyncOff.first()) {
                    originalMasterSync = ContentResolver.getMasterSyncAutomatically()
                    ContentResolver.setMasterSyncAutomatically(false)
                }
            } catch (_: Exception) {}

            try {
                // 5. Disable Location/GPS Scanning temporarily to avoid background localization CPU spikes
                if (settingsPreferences.secureSettingsLocationOff.first()) {
                    originalLocationMode = Settings.Secure.getInt(resolver, "location_mode", 3)
                    Settings.Secure.putInt(resolver, "location_mode", 0)
                }
            } catch (_: Exception) {}

            try {
                // 6. Force Game Driver for game package name & global gaming drivers
                if (settingsPreferences.secureSettingsGameDriver.first()) {
                    originalGameDriverOptInApps = Settings.Global.getString(resolver, "game_driver_opt_in_apps") ?: ""
                    val currentApps = originalGameDriverOptInApps ?: ""
                    if (!currentApps.split(",").contains(packageName)) {
                        val newApps = if (currentApps.isEmpty()) packageName else "$currentApps,$packageName"
                        Settings.Global.putString(resolver, "game_driver_opt_in_apps", newApps)
                    }

                    originalUpdatableDriverOptInApps = Settings.Global.getString(resolver, "updatable_driver_production_opt_in_apps") ?: ""
                    val currentUpdatableApps = originalUpdatableDriverOptInApps ?: ""
                    if (!currentUpdatableApps.split(",").contains(packageName)) {
                        val newUpdatableApps = if (currentUpdatableApps.isEmpty()) packageName else "$currentUpdatableApps,$packageName"
                        Settings.Global.putString(resolver, "updatable_driver_production_opt_in_apps", newUpdatableApps)
                    }

                    originalGameDriverAllApps = Settings.Global.getInt(resolver, "game_driver_all_apps", 0)
                    Settings.Global.putInt(resolver, "game_driver_all_apps", 1)

                    originalGameDriverFps = Settings.Global.getInt(resolver, "game_driver_fps", 0)
                    Settings.Global.putInt(resolver, "game_driver_fps", 1)

                    originalUpdatableDriverAllApps = Settings.Global.getInt(resolver, "updatable_driver_all_apps", 0)
                    Settings.Global.putInt(resolver, "updatable_driver_all_apps", 1)
                    
                    Settings.Global.putInt(resolver, "game_mode", 1)
                }
            } catch (_: Exception) {}

            try {
                // 7. Touch & Input Boost
                if (settingsPreferences.secureSettingsTouchBoost.first()) {
                    originalPointerSpeed = Settings.System.getInt(resolver, "pointer_speed", 5)
                    Settings.System.putInt(resolver, "pointer_speed", 7)

                    originalTouchSensitivity = Settings.System.getInt(resolver, "touch_sensitivity", 100)
                    Settings.System.putInt(resolver, "touch_sensitivity", 200)

                    originalTouchReportRate = Settings.System.getInt(resolver, "touch_report_rate", 0)
                    Settings.System.putInt(resolver, "touch_report_rate", 1000)

                    originalTouchBoost = Settings.Global.getInt(resolver, "touch_boost", 0)
                    Settings.Global.putInt(resolver, "touch_boost", 1)

                    originalStylusTouchBoost = Settings.Global.getInt(resolver, "stylus_touch_boost", 0)
                    Settings.Global.putInt(resolver, "stylus_touch_boost", 1)
                }
            } catch (_: Exception) {}

            try {
                // 8. Low Jitter Network (WiFi & Bluetooth Optimization)
                if (settingsPreferences.secureSettingsNetworkJitter.first()) {
                    originalWifiScanAlwaysEnabled = Settings.Global.getInt(resolver, "wifi_scan_always_enabled", 1)
                    Settings.Global.putInt(resolver, "wifi_scan_always_enabled", 0)

                    originalBleScanAlwaysEnabled = Settings.Global.getInt(resolver, "ble_scan_always_enabled", 1)
                    Settings.Global.putInt(resolver, "ble_scan_always_enabled", 0)

                    originalWifiPowerSave = Settings.Global.getInt(resolver, "wifi_power_save", 1)
                    Settings.Global.putInt(resolver, "wifi_power_save", 0)

                    originalWifiLatencyMode = Settings.Global.getInt(resolver, "wifi_latency_mode", 0)
                    Settings.Global.putInt(resolver, "wifi_latency_mode", 1)

                    originalWifiLowLatencyMode = Settings.Global.getInt(resolver, "wifi_low_latency_mode", 0)
                    Settings.Global.putInt(resolver, "wifi_low_latency_mode", 1)
                }
            } catch (_: Exception) {}

            try {
                // 9. Disable Phantom Process Killer monitor
                if (settingsPreferences.secureSettingsPhantomKiller.first()) {
                    originalPhantomProcsMonitor = Settings.Global.getString(resolver, "settings_enable_monitor_phantom_procs") ?: "true"
                    Settings.Global.putString(resolver, "settings_enable_monitor_phantom_procs", "false")
                }
            } catch (_: Exception) {}
        } else {
            // Fallback to basic non-root optimizations (which only write Settings.System if WRITE_SETTINGS is granted)
            disableAnimations()
            forceGpuRendering()
            setHighPerformanceMode()
        }
    }

    suspend fun restoreNonRoot() {
        restoreThreadPriority()
        releaseWakeLock()
        
        val resolver = context.contentResolver
        val hasSecurePerm = hasSecureSettingsPermission()

        if (hasSecurePerm) {
            try {
                originalWindowAnimScale?.let { Settings.Global.putFloat(resolver, "window_animation_scale", it) }
                originalTransitionAnimScale?.let { Settings.Global.putFloat(resolver, "transition_animation_scale", it) }
                originalAnimatorDurationScale?.let { Settings.Global.putFloat(resolver, "animator_duration_scale", it) }
                originalWindowAnimScale = null
                originalTransitionAnimScale = null
                originalAnimatorDurationScale = null
            } catch (_: Exception) {}

            try {
                originalLowPowerMode?.let { Settings.Global.putInt(resolver, "low_power", it) }
                originalLowPowerMode = null
            } catch (_: Exception) {}

            try {
                originalMobileDataAlwaysOn?.let { Settings.Global.putInt(resolver, "mobile_data_always_on", it) }
                originalMobileDataAlwaysOn = null
            } catch (_: Exception) {}

            try {
                originalMasterSync?.let { ContentResolver.setMasterSyncAutomatically(it) }
                originalMasterSync = null
            } catch (_: Exception) {}

            try {
                originalLocationMode?.let { Settings.Secure.putInt(resolver, "location_mode", it) }
                originalLocationMode = null
            } catch (_: Exception) {}

            try {
                originalGameDriverOptInApps?.let {
                    Settings.Global.putString(resolver, "game_driver_opt_in_apps", it)
                }
                originalGameDriverOptInApps = null

                originalUpdatableDriverOptInApps?.let {
                    Settings.Global.putString(resolver, "updatable_driver_production_opt_in_apps", it)
                }
                originalUpdatableDriverOptInApps = null

                originalGameDriverAllApps?.let { Settings.Global.putInt(resolver, "game_driver_all_apps", it) }
                originalGameDriverAllApps = null

                originalGameDriverFps?.let { Settings.Global.putInt(resolver, "game_driver_fps", it) }
                originalGameDriverFps = null

                originalUpdatableDriverAllApps?.let { Settings.Global.putInt(resolver, "updatable_driver_all_apps", it) }
                originalUpdatableDriverAllApps = null

                Settings.Global.putInt(resolver, "game_mode", 0)
            } catch (_: Exception) {}

            try {
                originalPointerSpeed?.let { Settings.System.putInt(resolver, "pointer_speed", it) }
                originalPointerSpeed = null

                originalTouchSensitivity?.let { Settings.System.putInt(resolver, "touch_sensitivity", it) }
                originalTouchSensitivity = null

                originalTouchReportRate?.let { Settings.System.putInt(resolver, "touch_report_rate", it) }
                originalTouchReportRate = null

                originalTouchBoost?.let { Settings.Global.putInt(resolver, "touch_boost", it) }
                originalTouchBoost = null

                originalStylusTouchBoost?.let { Settings.Global.putInt(resolver, "stylus_touch_boost", it) }
                originalStylusTouchBoost = null
            } catch (_: Exception) {}

            try {
                originalWifiScanAlwaysEnabled?.let { Settings.Global.putInt(resolver, "wifi_scan_always_enabled", it) }
                originalWifiScanAlwaysEnabled = null

                originalBleScanAlwaysEnabled?.let { Settings.Global.putInt(resolver, "ble_scan_always_enabled", it) }
                originalBleScanAlwaysEnabled = null

                originalWifiPowerSave?.let { Settings.Global.putInt(resolver, "wifi_power_save", it) }
                originalWifiPowerSave = null

                originalWifiLatencyMode?.let { Settings.Global.putInt(resolver, "wifi_latency_mode", it) }
                originalWifiLatencyMode = null

                originalWifiLowLatencyMode?.let { Settings.Global.putInt(resolver, "wifi_low_latency_mode", it) }
                originalWifiLowLatencyMode = null
            } catch (_: Exception) {}

            try {
                originalPhantomProcsMonitor?.let { Settings.Global.putString(resolver, "settings_enable_monitor_phantom_procs", it) }
                originalPhantomProcsMonitor = null
            } catch (_: Exception) {}
            
            // Also restore display refresh rate
            restoreRefreshRate()
        } else {
            restoreAnimations()
            restoreGpuRendering()
        }
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
            "ro.sys.fw.bg_apps_limit" to "60",
            "persist.sys.purgeable_assets" to "1",
            "ro.hwui.texture_cache_size" to "144",
            "ro.hwui.layer_cache_size" to "96",
            "ro.hwui.path_cache_size" to "64",
            "ro.hwui.drop_shadow_cache_size" to "12",
            "debug.sf.latch_unsignaled" to "1",
            "debug.hwui.target_cpu_time_percent" to "80",
            "persist.sys.gamemode" to "1",
            // SurfaceFlinger max FPS unlock props
            "debug.sf.frame_rate_multiple_threshold" to "0",
            "debug.sf.showupdates" to "0",
            "debug.sf.high_fps_early_phase_duration" to "1",
            "debug.sf.high_fps_late_phase_duration" to "1",
            "debug.sf.early_phase_in_ns" to "1000000",
            "debug.sf.late_phase_in_ns" to "1000000",
            "debug.sf.phase_offset_threshold_for_next_vsync" to "0",
            "vendor.display.enable_force_max_fps" to "1",
            "vendor.display.forced_max_fps" to "240",
            "persist.vendor.max_fps" to "240",
            "vendor.perf.gaming.driver" to "1",
            "vendor.perf.gaming.scheduler" to "1",
            "vendor.perf.gaming.opt" to "1",
            "persist.vendor.dfps.level.max" to "240",
            // Real no fake extra boosting properties
            "persist.sys.use_dithering" to "0",
            "persist.sys.ui.hw" to "1",
            "debug.egl.hw" to "1",
            "debug.egl.profiler" to "1",
            "debug.gr.num_framebuffers" to "3",
            "debug.composition.type" to "gpu",
            "persist.sys.composition.type" to "gpu",
            "wifi.supplicant_scan_interval" to "300",
            // Additional turbo props for max FPS unlock
            "debug.sf.disable_client_composition_cache" to "1",
            "debug.sf.enable_gl_backpressure" to "0",
            "ro.surface_flinger.max_frame_buffer_acquired_buffers" to "3",
            "debug.sf.recomputecrop" to "0",
            "persist.vendor.camera.preview.fps" to "240",
            // 5G/network performance tweaks
            "net.tcp.buffersize.5g" to "4096,87380,4194304,4096,65536,4194304",
            "net.tcp.buffersize.lte" to "4096,87380,4194304,4096,65536,4194304",
            "net.tcp.buffersize.wifi" to "524288,1048576,4194304,524288,1048576,4194304",
            "net.rmnet0.dns1" to "8.8.8.8",
            "net.rmnet0.dns2" to "8.8.4.4",
            // Battery saver system-level kill (root layer)
            "persist.sys.power_save_mode" to "0",
            "persist.sys.battery_saver" to "0",
            // Advanced extra tweaks to control 50% android system overhead
            "debug.sf.enable_hwc_vds" to "1",
            "debug.cpurend.vsync" to "false",
            "ro.surface_flinger.use_smart_90_for_video" to "0",
            "debug.performance.tuning" to "1",
            "video.accelerate.hw" to "1",
            "ro.vendor.qti.core_ctl_max_cpu" to "4",
            "ro.vendor.qti.sys.fw.bg_apps_limit" to "60",
            "persist.sys.performance" to "true",
            "persist.sys.performance.profile" to "1",
            "ro.kernel.android.checkjni" to "0",
            "ro.config.nocheckin" to "1",
            "profiler.force_disable_ulog" to "1",
            "profiler.force_disable_err_rpt" to "1",
            "debug.sf.disable_backoff" to "1",
            "windowsmgr.max_events_per_sec" to "275",
            "ro.min.fling_velocity" to "8000",
            "ro.max.fling_velocity" to "12000",
            "touch.presure.scale" to "0.001",
            // Explicitly disable any FPS limit
            "persist.sys.NV_FPSLIMIT" to "0",
            "debug.sf.max_fps" to "0",
            "persist.sys.app.fps" to "0",
            "vendor.display.forced_max_fps" to "240"
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
            "vendor.perf.gaming.scheduler" to "0",
            "vendor.display.enable_force_max_fps" to "0",
            "vendor.display.forced_max_fps" to "0",
            "persist.vendor.max_fps" to "",
            "debug.sf.frame_rate_multiple_threshold" to "",
            "persist.vendor.dfps.level.max" to "",
            // Restore extra boosting properties
            "persist.sys.use_dithering" to "1",
            "persist.sys.ui.hw" to "0",
            "debug.egl.hw" to "0",
            "debug.egl.profiler" to "0",
            "debug.gr.num_framebuffers" to "2",
            "debug.composition.type" to "",
            "persist.sys.composition.type" to "",
            "wifi.supplicant_scan_interval" to ""
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
        // GPU pre-emption boost for reduced latency
        rootShellManager.executeCommand("echo 0 > /sys/class/kgsl/kgsl-3d0/preempt_level")
        rootShellManager.executeCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/sync_fence")
        rootShellManager.executeCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/deep_nap")
        rootShellManager.executeCommand("echo 1 > /sys/module/adreno_idler/parameters/adreno_idler_active")
        // Force SurfaceFlinger to use max rate
        rootShellManager.executeCommand("service call SurfaceFlinger 1035 i32 1")

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
        val resolver = context.contentResolver
        val supported = getSupportedRefreshRates()
        val nearestHz = supported.minByOrNull { kotlin.math.abs(it - targetHz) } ?: 60f
        val canWriteSystem = canWriteSystemSettings()
        val canWriteSecure = hasSecureSettingsPermission()

        var changed = false

        runCatching {
            // AOSP standard. Settings.System needs the user-granted WRITE_SETTINGS app-op.
            try {
                if (canWriteSystem) {
                    if (originalAospPeakRefreshRate == null) {
                        originalAospPeakRefreshRate = Settings.System.getFloat(resolver, "peak_refresh_rate", 60f)
                    }
                    if (originalAospMinRefreshRate == null) {
                        originalAospMinRefreshRate = Settings.System.getFloat(resolver, "min_refresh_rate", 60f)
                    }
                    changed = Settings.System.putFloat(resolver, "peak_refresh_rate", nearestHz) || changed
                    changed = Settings.System.putFloat(resolver, "min_refresh_rate", nearestHz) || changed
                } else if (canWriteSecure) {
                    if (originalAospPeakRefreshRate == null) {
                        originalAospPeakRefreshRate = Settings.Secure.getFloat(resolver, "peak_refresh_rate", 60f)
                    }
                    if (originalAospMinRefreshRate == null) {
                        originalAospMinRefreshRate = Settings.Secure.getFloat(resolver, "min_refresh_rate", 60f)
                    }
                    changed = Settings.Secure.putFloat(resolver, "peak_refresh_rate", nearestHz) || changed
                    changed = Settings.Secure.putFloat(resolver, "min_refresh_rate", nearestHz) || changed
                }
            } catch (_: Exception) {}

            // Samsung
            try {
                if (canWriteSecure && originalSamsungRefreshMode == null) {
                    originalSamsungRefreshMode = Settings.Secure.getInt(resolver, "refresh_rate_mode", 0)
                }
                if (canWriteSecure) {
                    changed = Settings.Secure.putInt(resolver, "refresh_rate_mode", 1) || changed
                }
            } catch (_: Exception) {}

            // OnePlus / Oppo
            try {
                if (canWriteSecure && originalOneplusRefreshRate == null) {
                    originalOneplusRefreshRate = Settings.Secure.getInt(resolver, "oneplus_screen_refresh_rate", 0)
                }
                if (canWriteSecure) {
                    changed = Settings.Secure.putInt(resolver, "oneplus_screen_refresh_rate", 2) || changed
                }
            } catch (_: Exception) {}

            // Xiaomi
            try {
                if (canWriteSystem && originalXiaomiUserRefreshRate == null) {
                    originalXiaomiUserRefreshRate = Settings.System.getInt(resolver, "user_refresh_rate", 60)
                }
                if (canWriteSystem) {
                    changed = Settings.System.putInt(resolver, "user_refresh_rate", nearestHz.toInt()) || changed
                }

                if (canWriteSystem && originalXiaomiPeakRefreshRate == null) {
                    originalXiaomiPeakRefreshRate = Settings.System.getFloat(resolver, "peak_refresh_rate", 60f)
                }
                if (canWriteSystem) {
                    changed = Settings.System.putFloat(resolver, "peak_refresh_rate", nearestHz) || changed
                }
            } catch (_: Exception) {}

            // Huawei
            try {
                if (canWriteSecure && originalHuaweiSmartRefreshRate == null) {
                    originalHuaweiSmartRefreshRate = Settings.Secure.getInt(resolver, "hw_smart_refresh_rate_key", 1)
                }
                if (canWriteSecure) {
                    changed = Settings.Secure.putInt(resolver, "hw_smart_refresh_rate_key", 0) || changed
                }
            } catch (_: Exception) {}

            // Realme
            try {
                if (canWriteSecure && originalRealmePeakRefreshRate == null) {
                    originalRealmePeakRefreshRate = Settings.Secure.getFloat(resolver, "peak_refresh_rate", 60f)
                }
                if (canWriteSecure) {
                    changed = Settings.Secure.putFloat(resolver, "peak_refresh_rate", nearestHz) || changed
                }

                if (canWriteSecure && originalRealmeMinRefreshRate == null) {
                    originalRealmeMinRefreshRate = Settings.Secure.getFloat(resolver, "min_refresh_rate", 60f)
                }
                if (canWriteSecure) {
                    changed = Settings.Secure.putFloat(resolver, "min_refresh_rate", nearestHz) || changed
                }
            } catch (_: Exception) {}
        }

        val alreadyAtTarget = kotlin.math.abs(getCurrentRefreshRate() - nearestHz) < 0.5f
        return changed || alreadyAtTarget
    }

    fun restoreRefreshRate(): Boolean {
        val resolver = context.contentResolver
        val canWriteSystem = canWriteSystemSettings()
        val canWriteSecure = hasSecureSettingsPermission()
        var changed = false

        runCatching {
            // Restore AOSP
            try {
                originalAospPeakRefreshRate?.let {
                    if (canWriteSystem) changed = Settings.System.putFloat(resolver, "peak_refresh_rate", it) || changed
                    if (canWriteSecure) changed = Settings.Secure.putFloat(resolver, "peak_refresh_rate", it) || changed
                }
                originalAospMinRefreshRate?.let {
                    if (canWriteSystem) changed = Settings.System.putFloat(resolver, "min_refresh_rate", it) || changed
                    if (canWriteSecure) changed = Settings.Secure.putFloat(resolver, "min_refresh_rate", it) || changed
                }
            } catch (_: Exception) {}

            // Samsung
            try {
                originalSamsungRefreshMode?.let {
                    if (canWriteSecure) changed = Settings.Secure.putInt(resolver, "refresh_rate_mode", it) || changed
                }
            } catch (_: Exception) {}

            // OnePlus
            try {
                originalOneplusRefreshRate?.let {
                    if (canWriteSecure) changed = Settings.Secure.putInt(resolver, "oneplus_screen_refresh_rate", it) || changed
                }
            } catch (_: Exception) {}

            // Xiaomi
            try {
                originalXiaomiUserRefreshRate?.let {
                    if (canWriteSystem) changed = Settings.System.putInt(resolver, "user_refresh_rate", it) || changed
                }
                originalXiaomiPeakRefreshRate?.let {
                    if (canWriteSystem) changed = Settings.System.putFloat(resolver, "peak_refresh_rate", it) || changed
                }
            } catch (_: Exception) {}

            // Huawei
            try {
                originalHuaweiSmartRefreshRate?.let {
                    if (canWriteSecure) changed = Settings.Secure.putInt(resolver, "hw_smart_refresh_rate_key", it) || changed
                }
            } catch (_: Exception) {}

            // Realme
            try {
                originalRealmePeakRefreshRate?.let {
                    if (canWriteSecure) changed = Settings.Secure.putFloat(resolver, "peak_refresh_rate", it) || changed
                }
                originalRealmeMinRefreshRate?.let {
                    if (canWriteSecure) changed = Settings.Secure.putFloat(resolver, "min_refresh_rate", it) || changed
                }
            } catch (_: Exception) {}

            // Reset backup values
            originalAospPeakRefreshRate = null
            originalAospMinRefreshRate = null
            originalSamsungRefreshMode = null
            originalOneplusRefreshRate = null
            originalXiaomiUserRefreshRate = null
            originalXiaomiPeakRefreshRate = null
            originalHuaweiSmartRefreshRate = null
            originalRealmePeakRefreshRate = null
            originalRealmeMinRefreshRate = null
        }

        return changed
    }

    suspend fun lockFps(targetFps: Int) = withContext(Dispatchers.IO) {
        startPerformanceSession(targetFps)
        if (rootShellManager.isRootAvailable()) {
            val props = listOf(
                "persist.sys.app.fps" to "$targetFps",
                "persist.vendor.dfps.level" to "$targetFps",
                "persist.vendor.fps.max" to "$targetFps",
                "debug.ow.force_fps" to "$targetFps",
                "vendor.display.forced_max_fps" to "$targetFps",
                "persist.sys.NV_FPSLIMIT" to "$targetFps",
                "persist.sys.fps" to "$targetFps",
                "debug.sf.max_fps" to "$targetFps"
            )
            for ((key, value) in props) {
                rootShellManager.executeCommand("setprop $key $value")
            }
            // SurfaceFlinger seamless mode unlock via service call
            rootShellManager.executeCommand("service call SurfaceFlinger 1035 i32 1")
        }
    }

    suspend fun restoreFps() = withContext(Dispatchers.IO) {
        stopPerformanceSession()
        if (rootShellManager.isRootAvailable()) {
            val restoreProps = listOf(
                "persist.sys.app.fps" to "",
                "persist.vendor.dfps.level" to "",
                "persist.vendor.fps.max" to "",
                "debug.ow.force_fps" to "",
                "vendor.display.forced_max_fps" to "",
                "persist.sys.NV_FPSLIMIT" to "",
                "persist.sys.fps" to "",
                "debug.sf.max_fps" to ""
            )
            for ((key, value) in restoreProps) {
                rootShellManager.executeCommand("setprop $key $value")
            }
        }
    }

    fun triggerHeapCompaction() {
        try {
            System.gc()
            System.runFinalization()
            Runtime.getRuntime().gc()
        } catch (_: Exception) {}
    }
}
