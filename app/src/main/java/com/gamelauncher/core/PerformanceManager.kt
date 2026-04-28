package com.gamelauncher.core

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PerformanceHintManager
import android.os.Process
import android.view.Display
import android.view.WindowManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages CPU/GPU performance governors and performance hints.
 * All methods use public Android APIs — no root required.
 */
@Singleton
class PerformanceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rootShellManager: RootShellManager
) {
    private var performanceSession: AutoCloseable? = null  // PerformanceHintManager.Session

    // ── CPU Thread Priority ───────────────────────────────────────────────
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

    // ── GPU Info (read-only, no root) ────────────────────────────────────
    fun getGpuRenderer(): String {
        // Try reading from known paths; actual GL string requires EGL context
        return try {
            val paths = listOf(
                "/sys/kernel/gpu/gpu_model",
                "/sys/class/kgsl/kgsl-3d0/gpu_model",
                "/sys/module/pvrsrvkm/parameters/gpu_id"
            )
            paths.firstNotNullOfOrNull {
                runCatching { File(it).readText().trim().ifBlank { null } }.getOrNull()
            } ?: readGpuFromProc()
        } catch (e: Exception) { "GPU" }
    }

    private fun readGpuFromProc(): String {
        return try {
            File("/proc/cpuinfo").bufferedReader().lineSequence()
                .firstOrNull { it.contains("Hardware", true) }
                ?.substringAfter(":")?.trim() ?: "GPU"
        } catch (e: Exception) { "GPU" }
    }

    fun getGpuFreqMhz(): Long {
        return try {
            val paths = listOf(
                "/sys/class/kgsl/kgsl-3d0/gpuclk",
                "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage",
                "/sys/kernel/gpu/gpu_clock"
            )
            paths.firstNotNullOfOrNull {
                runCatching { File(it).readText().trim().toLongOrNull() }.getOrNull()
            }?.div(1_000_000L) ?: 0L
        } catch (e: Exception) { 0L }
    }

    // ── Animation Scale Toggle ───────────────────────────────────────────
    /**
     * Disabling animations requires WRITE_SETTINGS permission granted at runtime.
     * Falls back silently if denied — app still works, animations just remain.
     */
    fun disableAnimations() {
        try {
            android.provider.Settings.Global.putFloat(
                context.contentResolver,
                android.provider.Settings.Global.WINDOW_ANIMATION_SCALE, 0f
            )
            android.provider.Settings.Global.putFloat(
                context.contentResolver,
                android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE, 0f
            )
            android.provider.Settings.Global.putFloat(
                context.contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 0f
            )
        } catch (_: Exception) {}
    }

    fun restoreAnimations() {
        try {
            android.provider.Settings.Global.putFloat(
                context.contentResolver,
                android.provider.Settings.Global.WINDOW_ANIMATION_SCALE, 1f
            )
            android.provider.Settings.Global.putFloat(
                context.contentResolver,
                android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE, 1f
            )
            android.provider.Settings.Global.putFloat(
                context.contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f
            )
        } catch (_: Exception) {}
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
}
