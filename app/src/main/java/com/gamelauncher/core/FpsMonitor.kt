package com.gamelauncher.core

import android.view.Choreographer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class FrameMetrics(
    val fps: Float = 0f,
    val jankCount: Int = 0,
    val severeJankCount: Int = 0,
    val frameTimeMs: Float = 0f,
    val worstFrameTimeMs: Float = 0f,
    val frametimeStability: Float = 1f
)

@Singleton
class FpsMonitor @Inject constructor() : Choreographer.FrameCallback {

    private val _fps = MutableStateFlow(0f)
    val fps: StateFlow<Float> = _fps

    private var isTracking = false
    private var lastFrameTimeNanos = 0L
    private var frameCount = 0
    private var lastFpsReportNs = 0L
    private var currentFps = 0f
    private var jankCount = 0
    private var severeJankCount = 0
    private var totalFrameTimeMs = 0f
    private var worstFrameTimeMs = 0f
    private var frameTimeSamples = mutableListOf<Float>()

    fun startTracking() {
        if (isTracking) return
        isTracking = true
        lastFrameTimeNanos = 0L
        frameCount = 0
        currentFps = 0f
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun stopTracking() {
        isTracking = false
        Choreographer.getInstance().removeFrameCallback(this)
        _fps.value = 0f
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!isTracking) return

        if (lastFrameTimeNanos == 0L) {
            lastFrameTimeNanos = frameTimeNanos
            lastFpsReportNs = frameTimeNanos
            Choreographer.getInstance().postFrameCallback(this)
            return
        }

        val frameIntervalNs = frameTimeNanos - lastFrameTimeNanos
        val frameIntervalMs = frameIntervalNs / 1_000_000f

        if (frameIntervalMs > 16.67f && frameCount > 5) {
            jankCount++
            if (frameIntervalMs > 50f) severeJankCount++
        }

        totalFrameTimeMs += frameIntervalMs
        if (frameIntervalMs > worstFrameTimeMs) worstFrameTimeMs = frameIntervalMs
        if (frameTimeSamples.size < 60) frameTimeSamples.add(frameIntervalMs)

        val timeSinceLastReportNs = frameTimeNanos - lastFpsReportNs
        frameCount++
        if (timeSinceLastReportNs >= 1_000_000_000L) {
            val rawFps = (frameCount * 1_000_000_000f) / timeSinceLastReportNs
            
            currentFps = rawFps
            _fps.value = currentFps
            frameCount = 0
            
            lastFpsReportNs += 1_000_000_000L
            if (frameTimeNanos - lastFpsReportNs > 1_000_000_000L) {
                lastFpsReportNs = frameTimeNanos
            }
        }

        lastFrameTimeNanos = frameTimeNanos
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun getDetailedMetrics(): FrameMetrics {
        val avgFrameTime = if (frameTimeSamples.isNotEmpty())
            frameTimeSamples.average().toFloat() else 0f
        val stability = if (frameTimeSamples.isNotEmpty()) {
            val mean = frameTimeSamples.average()
            val variance = frameTimeSamples.map { (it - mean) * (it - mean) }.average()
            val stddev = kotlin.math.sqrt(variance).toFloat()
            (1f - (stddev / mean.toFloat()).coerceIn(0f, 1f))
        } else 1f

        val metrics = FrameMetrics(
            fps = currentFps,
            jankCount = jankCount,
            severeJankCount = severeJankCount,
            frameTimeMs = avgFrameTime,
            worstFrameTimeMs = worstFrameTimeMs,
            frametimeStability = stability
        )
        resetCounters()
        return metrics
    }

    fun resetCounters() {
        jankCount = 0
        severeJankCount = 0
        totalFrameTimeMs = 0f
        worstFrameTimeMs = 0f
        frameTimeSamples.clear()
    }
}
