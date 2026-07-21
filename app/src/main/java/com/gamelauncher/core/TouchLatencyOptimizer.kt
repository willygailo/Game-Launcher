package com.gamelauncher.core

import android.content.Context
import android.os.Build
import android.os.PerformanceHintManager
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TouchLatencyOptimizer — reduces touch input latency during intense gaming.
 *
 * Utilizes:
 * 1. ADPF (Android Dynamic Performance Framework) PerformanceHintSession for CPU core touch boost (API 31+)
 * 2. Vendor input touch sampling rate property tweaks via Shizuku/ADB
 */
@Singleton
class TouchLatencyOptimizer @Inject constructor(
    private val shizukuShellManager: ShizukuShellManager
) {
    companion object {
        private const val TAG = "TouchLatencyOptimizer"
        private const val DEFAULT_TARGET_NANO = 16_666_666L // ~60fps baseline in nanoseconds
    }

    private var hintSession: Any? = null

    /**
     * Enables high-performance touch response mode.
     */
    suspend fun enableTouchOptimizations() {
        if (shizukuShellManager.isAvailable()) {
            shizukuShellManager.executeAny(
                listOf(
                    "setprop persist.vendor.qti.input.touch_rate 240",
                    "setprop debug.input.velocity_tracker_strategy lsq2",
                    "settings put global touch_responsiveness_level max"
                )
            )
        }
    }

    suspend fun enableHighFrequencyTouch() {
        if (shizukuShellManager.isAvailable()) {
            shizukuShellManager.executeCommand("settings put system touch_prediction_enabled 1")
        }
    }

    suspend fun enableGameModeTouch() {
        if (shizukuShellManager.isAvailable()) {
            shizukuShellManager.executeCommand("settings put global game_touch_optimization 1")
        }
    }

    /**
     * Disables touch optimizations when gaming session ends.
     */
    suspend fun disableTouchOptimizations() {
        if (shizukuShellManager.isAvailable()) {
            shizukuShellManager.executeCommand("settings put global touch_responsiveness_level default")
        }
    }

    suspend fun disableHighFrequencyTouch() {
        if (shizukuShellManager.isAvailable()) {
            shizukuShellManager.executeCommand("settings put system touch_prediction_enabled 0")
        }
    }

    suspend fun disableGameModeTouch() {
        if (shizukuShellManager.isAvailable()) {
            shizukuShellManager.executeCommand("settings put global game_touch_optimization 0")
        }
    }

    /**
     * Enables ADPF touch hint session on Android 12+.
     */
    fun enableAdpfTouchHint(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val hintManager = context.getSystemService(Context.PERFORMANCE_HINT_SERVICE) as? PerformanceHintManager
                if (hintManager != null && hintSession == null) {
                    val tids = intArrayOf(android.os.Process.myTid())
                    hintSession = hintManager.createHintSession(tids, DEFAULT_TARGET_NANO)
                    Log.d(TAG, "ADPF PerformanceHintSession created successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create ADPF HintSession", e)
            }
        }
    }

    /**
     * Reports active workload to ADPF to ensure CPU remains boosted during touch inputs.
     */
    fun reportWorkload(actualDurationNs: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                (hintSession as? PerformanceHintManager.Session)?.reportActualWorkDuration(actualDurationNs)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to report workload to ADPF", e)
            }
        }
    }
}
