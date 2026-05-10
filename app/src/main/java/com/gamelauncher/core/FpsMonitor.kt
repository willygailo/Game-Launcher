package com.gamelauncher.core

import android.os.Build
import android.view.Choreographer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FpsMonitor @Inject constructor() {
    private var lastFrameTimeNanos = 0L
    private var frameCount = 0
    private var lastFpsReport = 0L
    private var currentFps = 0f

    fun getFpsFlow(): Flow<Float> = callbackFlow {
        val choreographer = Choreographer.getInstance()
        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (lastFrameTimeNanos == 0L) {
                    lastFrameTimeNanos = frameTimeNanos
                    lastFpsReport = System.currentTimeMillis()
                    choreographer.postFrameCallback(this)
                    return
                }

                frameCount++
                val now = System.currentTimeMillis()
                if (now - lastFpsReport >= 1000) {
                    currentFps = frameCount * 1000f / (now - lastFpsReport)
                    frameCount = 0
                    lastFpsReport = now
                    trySend(currentFps)
                }
                choreographer.postFrameCallback(this)
            }
        }

        choreographer.postFrameCallback(callback)
        awaitClose {
            choreographer.removeFrameCallback(callback)
        }
    }
}
