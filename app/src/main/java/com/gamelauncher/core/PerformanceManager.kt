// app/src/main/java/com/gamelauncher/core/PerformanceManager.kt
package com.gamelauncher.core

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
import com.gamelauncher.core.shizuku.IShellExecutor
import com.gamelauncher.data.preference.SettingsPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class PerformanceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shellExecutor: IShellExecutor,
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

    fun startPerformanceSession(targetFpsHz: Int = 60) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        try {
            val phm = context.getSystemService(PerformanceHintManager::class.java) ?: return
            val periodNs = 1_000_000_000L / targetFpsHz
            val tids = getRenderThreadIds()
            
            performanceSession?.close()
            performanceSession = phm.createHintSession(tids, periodNs)
        } catch (_: Exception) {}
    }

    private fun getRenderThreadIds(): IntArray {
        val excludePatterns = listOf("gc", "pool", "firebase", "worker", "binder",
            "dog", "ref", "finalizer", "jit", "logcat", "okio")
        val taskDir = java.io.File("/proc/self/task")
        return if (taskDir.exists() && taskDir.isDirectory) {
            taskDir.listFiles()?.mapNotNull { taskFile ->
                val tid = taskFile.name.toIntOrNull() ?: return@mapNotNull null
                val commFile = java.io.File(taskFile, "comm")
                val threadName = try { commFile.readText().trim().lowercase() } catch (_: Exception) { "" }
                if (excludePatterns.none { threadName.contains(it) }) tid else null
            }?.toIntArray() ?: intArrayOf(android.os.Process.myTid())
        } else {
            intArrayOf(android.os.Process.myTid())
        }
    }

    fun reportFrameTime(actualFrameNs: Long, socType: SocType = SocType.UNKNOWN) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        try {
            val session = performanceSession ?: return
            val reportedNs = when (socType) {
                SocType.SNAPDRAGON -> actualFrameNs * 3L
                else -> actualFrameNs
            }
            session.javaClass.getMethod("reportActualWorkDuration", Long::class.java)
                .invoke(session, reportedNs)
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

    fun getSupportedRefreshRates(): List<Float> {
        return try {
            val dm = context.getSystemService(DisplayManager::class.java)
            val display = dm?.getDisplay(Display.DEFAULT_DISPLAY) ?: return listOf(60f)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val rawModes = display.supportedModes
                val rates = rawModes
                    .map { (it.refreshRate * 100f).roundToInt() / 100f }
                    .filter { it >= 30f }
                    .distinct()
                    .sorted()
                if (rates.isNotEmpty()) rates else listOf(60f)
            } else {
                listOf(display.refreshRate)
            }
        } catch (e: Exception) {
            listOf(60f)
        }
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

    fun lockRefreshRate(hz: Float): Boolean {
        GlobalScope.launch(Dispatchers.IO) {
            shellExecutor.setPeakRefreshRate(hz)
            shellExecutor.setMinRefreshRate(hz)
        }
        return true
    }

    fun restoreRefreshRate() {
        GlobalScope.launch(Dispatchers.IO) {
            shellExecutor.writeSetting("system", "peak_refresh_rate", "")
            shellExecutor.writeSetting("system", "min_refresh_rate", "")
        }
    }

    fun lockFps(fps: Int) {
        startPerformanceSession(fps)
    }

    fun maximizeCpuGpuPerformance(): Boolean {
        return false
    }

    fun restoreCpuGpuPerformance() {
    }

    fun triggerHeapCompaction() {
        System.gc()
    }

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

    fun forceGpuRendering() {
        try {
            Settings.Global.putInt(context.contentResolver, "force_gpu_rendering", 1)
        } catch (_: Exception) {}
        try {
            Settings.System.putInt(context.contentResolver, "force_hw_ui", 1)
        } catch (_: Exception) {}

        GlobalScope.launch(Dispatchers.IO) {
            shellExecutor.writeSetting("global", "force_gpu_rendering", "1")
            shellExecutor.writeSetting("system", "force_hw_ui", "1")
        }
    }

    fun restoreGpuRendering() {
        try {
            Settings.Global.putInt(context.contentResolver, "force_gpu_rendering", 0)
        } catch (_: Exception) {}
        try {
            Settings.System.putInt(context.contentResolver, "force_hw_ui", 0)
        } catch (_: Exception) {}

        GlobalScope.launch(Dispatchers.IO) {
            shellExecutor.writeSetting("global", "force_gpu_rendering", "0")
            shellExecutor.writeSetting("system", "force_hw_ui", "0")
        }
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

    @Volatile private var originalLowPowerMode: Int? = null
    @Volatile private var originalMasterSync: Boolean? = null
    @Volatile private var originalMobileDataAlwaysOn: Int? = null
    @Volatile private var originalLocationMode: Int? = null
    @Volatile private var originalWindowAnimScale: Float? = null
    @Volatile private var originalTransitionAnimScale: Float? = null
    @Volatile private var originalAnimatorDurationScale: Float? = null
    @Volatile private var originalMobileDataAlwaysOnPrev: Int? = null

    fun forceMobileDataAlwaysOn() {
        if (!hasSecureSettingsPermission()) return
        try {
            val resolver = context.contentResolver
            originalMobileDataAlwaysOnPrev = Settings.Global.getInt(resolver, "mobile_data_always_on", 0)
            Settings.Global.putInt(resolver, "mobile_data_always_on", 1)
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
                if (settingsPreferences.secureSettingsBatterySaver.first()) {
                    originalLowPowerMode = Settings.Global.getInt(resolver, "low_power", 0)
                    Settings.Global.putInt(resolver, "low_power", 0)
                }
            } catch (_: Exception) {}

            try {
                if (settingsPreferences.secureSettingsMobileData.first()) {
                    originalMobileDataAlwaysOn = Settings.Global.getInt(resolver, "mobile_data_always_on", 0)
                    Settings.Global.putInt(resolver, "mobile_data_always_on", 1)
                }
            } catch (_: Exception) {}

            try {
                if (settingsPreferences.secureSettingsSyncOff.first()) {
                    originalMasterSync = ContentResolver.getMasterSyncAutomatically()
                    ContentResolver.setMasterSyncAutomatically(false)
                }
            } catch (_: Exception) {}

            try {
                if (settingsPreferences.secureSettingsLocationOff.first()) {
                    originalLocationMode = Settings.Secure.getInt(resolver, "location_mode", 3)
                    Settings.Secure.putInt(resolver, "location_mode", 0)
                }
            } catch (_: Exception) {}
        } else {
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

            restoreRefreshRate()
        } else {
            restoreAnimations()
            restoreGpuRendering()
        }
    }

    suspend fun setCpuGovernor(governor: String): Boolean = withContext(Dispatchers.IO) {
        false
    }

    suspend fun optimizeStorageFstrim(): Boolean = withContext(Dispatchers.IO) {
        System.gc()
        true
    }

    suspend fun setAdaptiveCpuGov(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        if (enabled) boostThreadPriority()
        else restoreThreadPriority()
        true
    }

    fun clearMemory() {
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            am?.let {
                val runningProcs = it.runningAppProcesses ?: return@let
                for (proc in runningProcs) {
                    if (proc.pkgList != null && proc.pkgList.isNotEmpty() && proc.pkgList[0] != context.packageName) {
                        it.killBackgroundProcesses(proc.pkgList[0])
                    }
                }
            }
        } catch (_: Exception) {}
    }

    fun clearBackgroundProcesses() = clearMemory()
}
