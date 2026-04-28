package com.gamelauncher.core

import android.view.Choreographer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks real-time FPS using the Choreographer.
 */
@Singleton
class FPSManager @Inject constructor() : Choreographer.FrameCallback {

    private val _fps = MutableStateFlow(0f)
    val fps: StateFlow<Float> = _fps

    private var isTracking = false
    private var lastFrameTimeNanos: Long = 0
    private var frameCount = 0
    private var lastFpsCalculationTimeNanos: Long = 0

    fun startTracking() {
        if (isTracking) return
        isTracking = true
        lastFrameTimeNanos = System.nanoTime()
        lastFpsCalculationTimeNanos = lastFrameTimeNanos
        frameCount = 0
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun stopTracking() {
        isTracking = false
        Choreographer.getInstance().removeFrameCallback(this)
        _fps.value = 0f
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!isTracking) return

        frameCount++
        val timeSinceLastCalc = frameTimeNanos - lastFpsCalculationTimeNanos
        
        // Calculate FPS every 500ms
        if (timeSinceLastCalc >= 500_000_000L) {
            val currentFps = (frameCount * 1_000_000_000f) / timeSinceLastCalc
            _fps.value = currentFps
            
            frameCount = 0
            lastFpsCalculationTimeNanos = frameTimeNanos
        }

        Choreographer.getInstance().postFrameCallback(this)
    }
}
