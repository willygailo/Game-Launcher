package com.gamelauncher.core.performance

import android.os.Build
import android.view.Choreographer
import android.view.FrameMetrics
import android.view.Window
import androidx.annotation.RequiresApi
import com.facebook.flipper.plugins.fps.FpsPlugin
import leakcanary.internal.InternalLeakCanary
import timber.log.Timber

/**
 * Monitors frame rendering time to detect and log jank.
 *
 * On Android 10+ uses FrameMetrics API (precise per-frame timing).
 * On older devices, falls back to Choreographer frame callbacks.
 *
 * JANK threshold: >16ms per frame (drops below 60fps)
 * SEVERE jank: >32ms per frame (drops below 30fps)
 *
 * Usage: Call FrameJankMonitor.start(window) in Activity.onCreate()
 *
 * Game launchers are jank-sensitive because:
 * - Scrolling game lists with varying thumbnail sizes
 * - Animated backgrounds and transitions
 * - Real-time badge/notification overlays
 */
object FrameJankMonitor {

    private const val JANK_THRESHOLD_MS = 16L   // 60fps budget
    private const val SEVERE_JANK_MS = 32L      // 30fps threshold
    private const val REPORT_INTERVAL = 60      // Log every N janky frames

    private var jankCount = 0
    private var severeJankCount = 0
    private var totalFrames = 0L
    private var isMonitoring = false

    data class JankReport(
        val totalFrames: Long,
        val jankyFrames: Int,
        val severeJankyFrames: Int,
        val jankRate: Float,  // percentage
        val avgJankMs: Float
    )

    /**
     * Starts monitoring on a specific window.
     * Must be called on the main thread.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun start(window: Window, callback: ((JankReport) -> Unit)? = null) {
        if (isMonitoring) return
        isMonitoring = true

        try {
            window.addOnFrameMetricsAvailableListener(
                { _, frameMetrics, _ ->
                    onFrameMetrics(frameMetrics, callback)
                },
                // Use background handler to avoid adding load to main thread
                android.os.Handler(android.os.Looper.getMainLooper())
            )
        } catch (e: Exception) {
            // Fallback for devices where FrameMetrics isn't fully supported
            startChoreographerFallback(callback)
        }
    }

    fun stop(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                window.removeOnFrameMetricsAvailableListener { _, _, _ -> }
            } catch (_: Exception) {}
        }
        isMonitoring = false
    }

    /**
     * Gets a snapshot report and resets counters.
     */
    fun getReport(): JankReport {
        val report = JankReport(
            totalFrames = totalFrames,
            jankyFrames = jankCount,
            severeJankyFrames = severeJankCount,
            jankRate = if (totalFrames > 0) (jankCount * 100f / totalFrames) else 0f,
            avgJankMs = if (jankCount > 0) SEVERE_JANK_MS * severeJankCount / jankCount.toFloat() else 0f
        )
        // Reset
        jankCount = 0
        severeJankCount = 0
        totalFrames = 0
        return report
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun onFrameMetrics(
        frameMetrics: FrameMetrics,
        callback: ((JankReport) -> Unit)?
    ) {
        // FrameMetrics.TOTAL_INDEX includes all time from input to display
        val totalMs = frameMetrics.getMetric(FrameMetrics.TOTAL_INDEX) / 1_000_000f

        totalFrames++

        when {
            totalMs > SEVERE_JANK_MS -> {
                severeJankCount++
                if (totalFrames % REPORT_INTERVAL == 0L) {
                    Timber.w("JANK SEVERE: ${"%.1f".format(totalMs)}ms (frame #$totalFrames)")
                }
            }
            totalMs > JANK_THRESHOLD_MS -> {
                jankCount++
                if (totalFrames % (REPORT_INTERVAL * 2) == 0L) {
                    Timber.d("JANK: ${"%.1f".format(totalMs)}ms (frame #$totalFrames)")
                }
            }
        }

        // Report every 120 frames
        if (totalFrames % 120L == 0L) {
            callback?.invoke(getReport())
        }
    }

    /**
     * Fallback for devices that don't support FrameMetrics properly.
     */
    private fun startChoreographerFallback(callback: ((JankReport) -> Unit)?) {
        val frameTimestamps = mutableListOf<Long>()
        val maxSamples = 60 // Keep last 60 frames

        val frameCallback = object : Choreographer.FrameCallback {
            private var lastFrameTime = 0L

            override fun doFrame(frameTimeNanos: Long) {
                val frameTimeMs = frameTimeNanos / 1_000_000
                if (lastFrameTime > 0) {
                    val delta = frameTimeMs - lastFrameTime
                    totalFrames++
                    when {
                        delta > SEVERE_JANK_MS -> severeJankCount++
                        delta > JANK_THRESHOLD_MS -> jankCount++
                    }

                    frameTimestamps.add(delta)
                    if (frameTimestamps.size > maxSamples) frameTimestamps.removeAt(0)
                }
                lastFrameTime = frameTimeMs

                if (isMonitoring) {
                    Choreographer.getInstance().postFrameCallback(this)
                }

                // Report periodically
                if (totalFrames % 120L == 0L) {
                    callback?.invoke(getReport())
                }
            }
        }

        Choreographer.getInstance().postFrameCallback(frameCallback)
    }
}