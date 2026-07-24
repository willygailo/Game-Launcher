// app/src/main/java/com/gamelauncher/core/FPSManager.kt
package com.gamelauncher.core

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.view.Display
import com.gamelauncher.core.shizuku.IShellExecutor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * FPSManager — frame timing for this process plus a best-effort display-rate request.
 * It never disables Android thermal protection and it never claims to unlock a game's
 * internal frame cap.
 */
@Singleton
class FPSManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val displayManager: DisplayManager,
    private val shellExecutor: IShellExecutor
) : Choreographer.FrameCallback {

    private val _fps = MutableStateFlow(0f)
    val fps: StateFlow<Float> = _fps

    private val _avgFps = MutableStateFlow(0f)
    val avgFps: StateFlow<Float> = _avgFps

    private val _frameJankCount = MutableStateFlow(0L)
    val frameJankCount: StateFlow<Long> = _frameJankCount

    private val _maxHzAvailable = MutableStateFlow(60f)
    val maxHzAvailable: StateFlow<Float> = _maxHzAvailable

    private val _currentHz = MutableStateFlow(60f)
    val currentHz: StateFlow<Float> = _currentHz

    private val _frameDropAlert = MutableStateFlow(false)
    val frameDropAlert: StateFlow<Boolean> = _frameDropAlert

    @Volatile private var isTracking = false
    @Volatile private var targetFps: Int = 60

    private var lastFrameTimeNanos: Long = 0L
    private var lastFpsCalcTimeNanos: Long = 0L
    private var lastAvgCalcTimeNanos: Long = 0L
    private var frameCount: Int = 0
    private var avgWindowCount: Int = 0

    private val targetPeriodNs get() = if (targetFps > 0) 1_000_000_000L / targetFps else 16_666_666L

    private val FPS_SAMPLE_WINDOW_NS = 500_000_000L   // 500ms
    private val AVG_SAMPLE_WINDOW_NS = 2_000_000_000L // 2s

    private var jankCount = 0L
    private val mainHandler = Handler(Looper.getMainLooper())
    private val trackerScope = CoroutineScope(Dispatchers.IO)

    fun getSupportedRefreshRates(): List<Float> {
        return try {
            val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY) ?: return listOf(60f)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                display.supportedModes.map { it.refreshRate }.distinct().sortedDescending()
            } else {
                listOf(display.refreshRate)
            }
        } catch (_: Exception) { listOf(60f) }
    }

    fun getMaxRefreshRate(): Float {
        val rates = getSupportedRefreshRates()
        val max = rates.maxOrNull() ?: 60f
        _maxHzAvailable.value = max
        return max
    }

    fun getCurrentRefreshRate(): Float {
        return try {
            val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            val rate = display?.refreshRate ?: 60f
            _currentHz.value = rate
            rate
        } catch (_: Exception) { 60f }
    }

    fun forceMaxHz(): Boolean {
        val maxHz = getMaxRefreshRate()
        return forceHz(maxHz)
    }

    fun forceHz(targetHz: Float): Boolean {
        val supported = getSupportedRefreshRates()
        val nearest = supported.minByOrNull { abs(it - targetHz) } ?: 60f
        var success = false

        try {
            android.provider.Settings.System.putFloat(
                getContentResolver(), "peak_refresh_rate", nearest
            )
            android.provider.Settings.System.putFloat(
                getContentResolver(), "min_refresh_rate", nearest
            )
            success = true
        } catch (_: Exception) {}

        trackerScope.launch {
            shellExecutor.setPeakRefreshRate(nearest)
            shellExecutor.setMinRefreshRate(nearest)
        }

        success = true
        if (success) _currentHz.value = nearest
        return success
    }

    fun restoreDefaultHz() {
        try {
            android.provider.Settings.System.putFloat(
                getContentResolver(), "min_refresh_rate", 60f
            )
        } catch (_: Exception) {}
        
        trackerScope.launch {
            shellExecutor.writeSetting("system", "peak_refresh_rate", "")
            shellExecutor.writeSetting("system", "min_refresh_rate", "")
        }
    }

    fun startTracking(targetFps: Int = 60) {
        if (isTracking) return
        this.targetFps = targetFps
        isTracking = true
        lastFrameTimeNanos = System.nanoTime()
        lastFpsCalcTimeNanos = lastFrameTimeNanos
        lastAvgCalcTimeNanos = lastFrameTimeNanos
        frameCount = 0
        avgWindowCount = 0
        jankCount = 0L
        _frameJankCount.value = 0L
        _frameDropAlert.value = false
        mainHandler.post { Choreographer.getInstance().postFrameCallback(this) }
    }

    fun stopTracking() {
        isTracking = false
        mainHandler.post { Choreographer.getInstance().removeFrameCallback(this) }
        _fps.value = 0f
        _avgFps.value = 0f
        _frameDropAlert.value = false
    }

    fun isTracking(): Boolean = isTracking

    override fun doFrame(frameTimeNanos: Long) {
        if (!isTracking) return

        if (lastFrameTimeNanos > 0L) {
            val frameDelta = frameTimeNanos - lastFrameTimeNanos
            if (frameDelta > targetPeriodNs * 2) {
                jankCount++
                _frameJankCount.value = jankCount
            }
        }
        lastFrameTimeNanos = frameTimeNanos
        frameCount++
        avgWindowCount++

        val sinceLastCalc = frameTimeNanos - lastFpsCalcTimeNanos
        if (sinceLastCalc >= FPS_SAMPLE_WINDOW_NS) {
            val rawFps = (frameCount * 1_000_000_000f) / sinceLastCalc
            _fps.value = rawFps

            val dropThreshold = targetFps * 0.85f
            _frameDropAlert.value = rawFps < dropThreshold && targetFps > 0

            frameCount = 0
            lastFpsCalcTimeNanos += FPS_SAMPLE_WINDOW_NS
            if (frameTimeNanos - lastFpsCalcTimeNanos > FPS_SAMPLE_WINDOW_NS) {
                lastFpsCalcTimeNanos = frameTimeNanos
            }
        }

        val sinceAvgCalc = frameTimeNanos - lastAvgCalcTimeNanos
        if (sinceAvgCalc >= AVG_SAMPLE_WINDOW_NS) {
            val avg = (avgWindowCount * 1_000_000_000f) / sinceAvgCalc
            _avgFps.value = avg
            avgWindowCount = 0
            
            lastAvgCalcTimeNanos += AVG_SAMPLE_WINDOW_NS
            if (frameTimeNanos - lastAvgCalcTimeNanos > AVG_SAMPLE_WINDOW_NS) {
                lastAvgCalcTimeNanos = frameTimeNanos
            }
        }

        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun getContentResolver() = context.contentResolver

    fun getFpsStatusString(): String {
        val fpsInt = _fps.value.toInt()
        val hzInt = _currentHz.value.toInt()
        val janks = _frameJankCount.value
        return buildString {
            append("$fpsInt FPS")
            if (hzInt > 0) append(" | ${hzInt}Hz")
            if (janks > 0) append(" | $janks janks")
        }
    }

    fun getNearestSupportedHz(target: Float): Float =
        getSupportedRefreshRates().minByOrNull { abs(it - target) } ?: 60f
}
