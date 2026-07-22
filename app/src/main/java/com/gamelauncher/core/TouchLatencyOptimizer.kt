// app/src/main/java/com/gamelauncher/core/TouchLatencyOptimizer.kt
package com.gamelauncher.core

import android.content.Context
import android.os.Build
import android.os.PerformanceHintManager
import android.util.Log
import com.gamelauncher.core.shizuku.IShellExecutor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TouchLatencyOptimizer — reduces touch input latency during intense gaming.
 * Uses typed IShellExecutor AIDL writes.
 */
@Singleton
class TouchLatencyOptimizer @Inject constructor(
    private val shellExecutor: IShellExecutor
) {
    companion object {
        private const val TAG = "TouchLatencyOptimizer"
        private const val DEFAULT_TARGET_NANO = 16_666_666L // ~60fps baseline in nanoseconds
    }

    private var hintSession: Any? = null

    suspend fun enableTouchOptimizations() {
        shellExecutor.writeSetting("global", "touch_responsiveness_level", "max")
    }

    suspend fun enableHighFrequencyTouch() {
        shellExecutor.writeSetting("system", "touch_prediction_enabled", "1")
    }

    suspend fun enableGameModeTouch() {
        shellExecutor.writeSetting("global", "game_touch_optimization", "1")
    }

    suspend fun disableTouchOptimizations() {
        shellExecutor.writeSetting("global", "touch_responsiveness_level", "default")
    }

    suspend fun disableHighFrequencyTouch() {
        shellExecutor.writeSetting("system", "touch_prediction_enabled", "0")
    }

    suspend fun disableGameModeTouch() {
        shellExecutor.writeSetting("global", "game_touch_optimization", "0")
    }

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
