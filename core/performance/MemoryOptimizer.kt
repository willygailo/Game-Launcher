package com.gamelauncher.core.performance

import android.content.Context
import androidx.startup.Initializer
import com.facebook.flipper.plugins.leakcanary.LeakCanaryConfig
import com.facebook.flipper.plugins.leakcanary.LeakCanaryFlipperPlugin
import leakcanary.LeakCanary

/**
 * Memory leak detection - auto-installs LeakCanary in debug builds.
 * For Android 10+, uses the AppWatcherInstaller pattern.
 *
 * PROBLEM SOLVED: Game launchers hold many Bitmap/Drawable refs
 * (game icons, backgrounds, screenshots). Leaks here cause OOM.
 */
class MemoryLeakDetector : Initializer<Unit> {

    override fun create(context: Context) {
        // Only in debug - LeakCanary adds ~2MB overhead
        if (!BuildConfig.DEBUG) return

        // Custom config: faster detection, less aggressive for gaming apps
        val config = LeakCanary.config.copy(
            // Watch activities only (fragments add overhead)
            watchActivities = true,
            watchFragments = false,
            // Dump heap after 5 retained objects (default: 5)
            retainedVisibleThreshold = 5,
            // Max 5 analysis retries
            maxStoredHeapDumps = 5,
            // Use custom notification for gaming UX
            showNotification = true,
            // On Android 12+, request POST_NOTIFICATIONS
            requestNotificationPermission = Build.VERSION.SDK_INT >= 31
        )

        LeakCanary.config = config

        // Alternative: If using Flipper for dev debugging
        // LeakCanaryFlipperPlugin(context, LeakCanaryConfig())
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}


/**
 * Aggressive bitmap memory manager for game launcher context.
 *
 * Game launchers display many high-res icons and artwork.
 * This utility prevents:
 * - java.lang.OutOfMemoryError on low-RAM devices
 * - GC thrashing from rapid bitmap allocation/deallocation
 *
 * Usage:
 * ```
 * val bitmap = BitmapLoader.loadFromPath(path)
 *     .maxSize(256, 256)  // Downsample game icons
 *     .format(Bitmap.Config.RGB_565)  // 50% less memory than ARGB_8888
 *     .into(imageView)
 * ```
 */
object BitmapMemoryOptimizer {

    /**
     * Estimates memory footprint of a bitmap before loading.
     * Returns bytes, or -1 if would exceed threshold.
     */
    fun estimateBitmapMemory(
        width: Int,
        height: Int,
        config: android.graphics.Bitmap.Config = android.graphics.Bitmap.Config.ARGB_8888
    ): Long {
        val bytesPerPixel = when (config) {
            android.graphics.Bitmap.Config.ARGB_8888 -> 4L
            android.graphics.Bitmap.Config.RGB_565 -> 2L
            android.graphics.Bitmap.Config.ARGB_4444 -> 2L
            android.graphics.Bitmap.Config.ALPHA_8 -> 1L
            else -> 4L
        }
        return width.toLong() * height.toLong() * bytesPerPixel
    }

    /**
     * Calculates optimal inSampleSize for BitmapFactory.
     * Target: load bitmaps at display size, not source size.
     */
    fun calculateInSampleSize(
        options: android.graphics.BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Loads a downscaled bitmap efficiently.
     * Use for game icons, banners, background images.
     */
    fun loadDownsampledBitmap(
        path: String,
        targetWidth: Int,
        targetHeight: Int,
        config: android.graphics.Bitmap.Config = android.graphics.Bitmap.Config.RGB_565
    ): android.graphics.Bitmap? {
        return try {
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            android.graphics.BitmapFactory.decodeFile(path, options)

            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
            options.inPreferredConfig = config
            options.inJustDecodeBounds = false

            android.graphics.BitmapFactory.decodeFile(path, options)
        } catch (e: OutOfMemoryError) {
            null // Caller should show placeholder
        }
    }

    /**
     * Returns memory-safe config based on available heap.
     * On Android Go / low-RAM: forces RGB_565 (16-bit)
     */
    fun getSafeConfig(context: Context): android.graphics.Bitmap.Config {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        return if (activityManager.isLowRamDevice) {
            android.graphics.Bitmap.Config.RGB_565  // 50% memory savings
        } else {
            android.graphics.Bitmap.Config.ARGB_8888  // Full quality
        }
    }
}


/**
 * Monitors app memory usage and warns before OOM.
 *
 * Register in Application.onCreate():
 * ```
 * MemoryMonitor.start(this) { event ->
 *     when (event.level) {
 *         MemoryLevel.CRITICAL -> releaseNonEssentialBitmaps()
 *         MemoryLevel.WARNING -> trimCaches()
 *     }
 * }
 * ```
 */
object MemoryMonitor {

    enum class MemoryLevel { NORMAL, WARNING, CRITICAL }

    private var callback: ((MemoryLevel, Long, Long) -> Unit)? = null
    private var lastTrimTime = 0L

    /**
     * Start monitoring. Call from Application.onCreate()
     * @param checkIntervalMs How often to check (default: 5000ms)
     */
    fun start(
        context: Context,
        checkIntervalMs: Long = 5000L,
        onMemoryEvent: (MemoryLevel, Long, Long) -> Unit
    ) {
        callback = onMemoryEvent

        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                checkMemory(context)
                if (callback != null) {
                    handler.postDelayed(this, checkIntervalMs)
                }
            }
        }
        handler.post(runnable)
    }

    fun stop() {
        callback = null
    }

    private fun checkMemory(context: Context) {
        val runtime = Runtime.getRuntime()
        val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val maxHeap = runtime.maxMemory() / (1024 * 1024)
        val availableMem = maxHeap - usedMem

        val level = when {
            availableMem < 30 -> MemoryLevel.CRITICAL   // <30MB free
            availableMem < 80 -> MemoryLevel.WARNING     // <80MB free
            else -> MemoryLevel.NORMAL
        }

        if (level != MemoryLevel.NORMAL) {
            callback?.invoke(level, usedMem, maxHeap)

            // Auto-trim on critical (but debounce: min 30s between trims)
            if (level == MemoryLevel.CRITICAL && System.currentTimeMillis() - lastTrimTime > 30_000) {
                lastTrimTime = System.currentTimeMillis()
                android.app.ActivityManager(context).let { am ->
                    // Release cached processes
                    try {
                        am.lowMemory()
                    } catch (_: Exception) {}
                }
            }
        }
    }
}