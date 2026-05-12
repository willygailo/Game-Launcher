package com.gamelauncher.core.performance

import android.os.Build
import android.os.Trace
import androidx.startup.Initializer
import com.gamelauncher.BuildConfig
import timber.log.Timber

/**
 * Tracks app startup time using Systrace.
 * Helps identify bottlenecks during cold/warm start.
 * Usage: Check Android Studio Profiler > System Trace
 */
class AppStartupTracer : Initializer<Unit> {

    override fun create(context: android.content.Context) {
        if (!BuildConfig.DEBUG) return

        val startTime = System.nanoTime()

        // Trace section visible in systrace / profiler
        Trace.beginSection("GameLauncher_AppInit")

        try {
            // Will be populated by each Initializer via traceSection()
            Timber.d("StartupTracer: App initialization started")
        } finally {
            Trace.endSection()
            val duration = (System.nanoTime() - startTime) / 1_000_000
            Timber.d("StartupTracer: Total init time = ${duration}ms")
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    companion object {
        /**
         * Call this in each initializer or heavy operation to create
         * trace sections visible in Android Studio Profiler
         */
        fun traceSection(name: String, block: () -> Unit) {
            if (!BuildConfig.DEBUG) {
                block()
                return
            }
            Trace.beginSection(name)
            try {
                block()
            } finally {
                Trace.endSection()
            }
        }

        /**
         * Checks if the device is low-RAM (Android Go edition or < 2GB)
         * On these devices, defer non-critical work aggressively
         */
        fun isLowRamDevice(context: android.content.Context): Boolean {
            val am = context.getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            return am.isLowRamDevice
        }

        /**
         * Returns max concurrent background work slots based on API level
         */
        fun getMaxConcurrentWorkers(): Int {
            return when {
                Build.VERSION.SDK_INT >= 34 -> 6  // API 34+ has better threading
                Build.VERSION.SDK_INT >= 31 -> 4
                Build.VERSION.SDK_INT >= 29 -> 3  // Android 10
                else -> 2
            }
        }
    }
}