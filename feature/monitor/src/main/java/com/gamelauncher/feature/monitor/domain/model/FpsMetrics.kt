package com.gamelauncher.feature.monitor.domain.model

/**
 * FpsMetrics — Encapsulates real-time Choreographer frame pacing measurements.
 *
 * @property currentFps Current calculated frames per second
 * @property targetFps Target screen refresh rate (e.g. 60, 90, 120 Hz)
 * @property frameTimeMs Average frame render duration in milliseconds
 * @property jankFrameCount Count of dropped frames exceeding frame budget threshold
 */
data class FpsMetrics(
    val currentFps: Int,
    val targetFps: Int = 60,
    val frameTimeMs: Float = 16.6f,
    val jankFrameCount: Int = 0
)
