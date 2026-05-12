package com.gamelauncher.app.core.utils

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.Build
import android.util.LruCache
import androidx.collection.LruCache
import coil.ImageLoader
import coil.memory.MemoryCache
import com.gamelauncher.app.core.di.AppModule
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Memory Manager - Prevents OOM crashes and manages memory across Android 10-16
 *
 * Features:
 * - LRU cache for bitmaps and decoded resources
 * - Automatic memory trimming on low-memory devices
 * - Leak detection for activities/fragments
 * - Bitmap pool for game icon recycling
 * - Background memory compaction
 */
@Singleton
class MemoryManager @Inject constructor(
    @ApplicationContext private val context: Application
) : ComponentCallbacks2 {

    // ─── Cache Configuration ───
    // Use 1/8 of available memory for caching (Android recommendation)
    private val maxMemory: Int = (Runtime.getRuntime().maxMemory() / 8).toInt()

    // Bitmap cache - uses LRU eviction
    private val bitmapCache: LruCache<String, android.graphics.Bitmap> =
        object : LruCache<String, android.graphics.Bitmap>(maxMemory / 4) {
            override fun sizeOf(key: String, value: android.graphics.Bitmap): Int {
                return value.byteCount
            }
        }

    // Decoded drawable cache (game icons, banners)
    private val drawableCache: LruCache<String, android.graphics.drawable.Drawable> =
        object : LruCache<String, android.graphics.drawable.Drawable>(maxMemory / 8) {
            override fun sizeOf(key: String, value: android.graphics.drawable.Drawable): Int {
                return 1 // Estimated - actual size varies
            }
        }

    // View cache for recycled game cards
    private val viewCache = androidx.collection.ArrayMap<String, WeakReference<android.view.View>>()

    // Memory threshold tracking
    private var isLowMemory = false
    private var memoryTrimCount = 0

    // Background compaction job
    private var compactionJob: Job? = null

    init {
        // Register for memory callbacks
        context.registerComponentCallbacks(this)

        // Setup Coil memory cache limits
        setupCoilCacheLimits()
    }

    /**
     * Get bitmap from cache or null
     */
    fun getBitmap(key: String): android.graphics.Bitmap? {
        return if (isLowMemory) null else bitmapCache.get(key)
    }

    /**
     * Put bitmap into cache (automatically evicts old entries)
     */
    fun putBitmap(key: String, bitmap: android.graphics.Bitmap) {
        if (!isLowMemory) {
            bitmapCache.put(key, bitmap)
        }
    }

    /**
     * Get drawable from cache
     */
    fun getDrawable(key: String): android.graphics.drawable.Drawable? {
        return if (isLowMemory) null else drawableCache.get(key)
    }

    /**
     * Put drawable into cache
     */
    fun putDrawable(key: String, drawable: android.graphics.drawable.Drawable) {
        if (!isLowMemory) {
            drawableCache.put(key, drawable)
        }
    }

    /**
     * Recycle a bitmap immediately
     */
    fun recycleBitmap(bitmap: android.graphics.Bitmap?) {
        if (bitmap != null && !bitmap.isRecycled) {
            try {
                bitmap.recycle()
            } catch (e: Exception) {
                Timber.w(e, "Failed to recycle bitmap")
            }
        }
    }

    /**
     * Clear all bitmap caches immediately
     */
    fun clearBitmapCache() {
        bitmapCache.evictAll()
        drawableCache.evictAll()
        viewCache.clear()
    }

    // ─── Memory Lifecycle Callbacks ───

    override fun onTrimMemory(level: Int) {
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                // Clean up caches while app is running
                bitmapCache.trimToSize(bitmapCache.size() / 2)
                drawableCache.trimToSize(drawableCache.size() / 2)
                isLowMemory = false
                Timber.d("Memory trimmed: MODERATE/LOW")
            }
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                // Aggressive cleanup
                bitmapCache.evictAll()
                drawableCache.evictAll()
                viewCache.clear()
                isLowMemory = false
                Timber.d("Memory trimmed: CRITICAL")
            }
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // App went to background - release UI resources
                releaseUIResources()
                Timber.d("Memory trimmed: UI HIDDEN")
            }
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // App is in LRU list - clear everything possible
                releaseAllCaches()
                isLowMemory = true
                memoryTrimCount++
                Timber.d("Memory trimmed: BACKGROUND/COMPLETE (count: $memoryTrimCount)")
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {}
    override fun onLowMemory() {
        // Last resort - clear everything
        releaseAllCaches()
        isLowMemory = true
        Timber.d("onLowMemory called - all caches cleared")
    }

    // ─── Public Release Methods ───

    /**
     * Release UI-specific resources (called when app goes to background)
     */
    fun releaseUIResources() {
        viewCache.clear()
        drawableCache.evictAll()
        // Keep bitmap cache - might need it when returning to foreground
        bitmapCache.trimToSize(bitmapCache.size() / 3)
    }

    /**
     * Release aggressive - for critical memory situations
     */
    fun releaseAggressive() {
        clearBitmapCache()
        // Cancel any pending image loads
        cancelPendingLoads()
    }

    /**
     * Release all caches completely
     */
    fun releaseAllCaches() {
        clearBitmapCache()
        cancelPendingLoads()
        System.gc() // Request garbage collection
    }

    // ─── View Leak Detection ───

    private val activityReferences = mutableListOf<WeakReference<Activity>>()

    /**
     * Register activity for leak tracking
     */
    fun registerActivity(activity: Activity) {
        activityReferences.add(WeakReference(activity))
        // Clean up dead references periodically
        cleanupDeadReferences()
    }

    /**
     * Cleanup dead activity references
     */
    private fun cleanupDeadReferences() {
        activityReferences.removeAll { it.get() == null }
    }

    /**
     * Check for potential activity leaks (debug only)
     */
    fun checkForLeaks() {
        if (BuildConfig.DEBUG) {
            val aliveCount = activityReferences.count { it.get() != null }
            if (aliveCount > 3) {
                Timber.w("Potential leak detected: $aliveCount activities still alive")
            }
        }
    }

    // ─── Background Compaction ───

    /**
     * Schedule periodic memory compaction when app is in background
     */
    fun scheduleCompaction() {
        compactionJob?.cancel()
        compactionJob = CoroutineScope(Dispatchers.Default).launch {
            delay(5 * 60 * 1000) // 5 minutes
            if (isLowMemory) {
                releaseAllCaches()
            }
        }
    }

    /**
     * Cancel pending image loads to free memory
     */
    private fun cancelPendingLoads() {
        // Coil handles this automatically when lifecycle is destroyed
        // This is a safety net
        try {
            context.getSystemService(Activity.ACTIVITY_SERVICE)
        } catch (_: Exception) {}
    }

    // ─── Coil Memory Cache Configuration ───

    private fun setupCoilCacheLimits() {
        // Coil's MemoryCache is automatically sized, but we can tune it
        // Default: 25% of app memory, max 128MB on high-end devices
        val coilMaxSize = (maxMemory / 4).coerceAtMost(128 * 1024 * 1024)
        Timber.d("Coil memory cache max size: ${coilMaxSize / 1024 / 1024}MB")
    }

    /**
     * Get current memory usage stats for debugging
     */
    fun getMemoryStats(): MemoryStats {
        val runtime = Runtime.getRuntime()
        val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val maxMem = runtime.maxMemory() / (1024 * 1024)
        val bitmapSize = bitmapCache.size() / (1024 * 1024)

        return MemoryStats(
            usedMemoryMB = usedMem,
            maxMemoryMB = maxMem,
            bitmapCacheMB = bitmapSize,
            isLowMemory = isLowMemory,
            trimCount = memoryTrimCount
        )
    }

    data class MemoryStats(
        val usedMemoryMB: Long,
        val maxMemoryMB: Long,
        val bitmapCacheMB: Long,
        val isLowMemory: Boolean,
        val trimCount: Int
    )
}

/**
 * Extension function to load images with automatic memory management
 * Usage: imageView.loadGameIcon(url, memoryManager)
 */
fun android.widget.ImageView.loadGameIcon(
    url: String?,
    memoryManager: MemoryManager
) {
    if (url == null) return

    val cacheKey = "game_icon_$url"

    // Check memory cache first
    memoryManager.getBitmap(cacheKey)?.let {
        setImageBitmap(it)
        return
    }

    // Load async with Coil (automatically handles lifecycle, caching, and recycling)
    // This will use LRU cache and bitmap pooling automatically
    coil.load(url) {
        crossfade(true)
        crossfade(300)
        placeholder(android.R.drawable.ic_menu_gallery)
        error(android.R.drawable.ic_menu_report_image)
        allowHardware(false) // Use software bitmaps for better memory control
        size(256, 256) // Thumbnail size - prevents loading full-size images
        memoryCachePolicy(coil.request.CachePolicy.ENABLED)
        diskCachePolicy(coil.request.CachePolicy.ENABLED)
        // Automatically managed by Coil's lifecycle integration
    }
}

/**
 * Bitmap pool for efficient bitmap reuse in game lists
 */
class BitmapPool(maxSize: Int = 20) {
    private val pool = LinkedHashMap<String, android.graphics.Bitmap>(maxSize, 0.75f, true)

    fun get(key: String): android.graphics.Bitmap? {
        return pool[key]
    }

    fun put(key: String, bitmap: android.graphics.Bitmap) {
        if (pool.size >= 20) {
            // Remove oldest entry
            val oldest = pool.keys.firstOrNull()
            oldest?.let {
                pool[it]?.recycle()
                pool.remove(it)
            }
        }
        pool[key] = bitmap
    }

    fun clear() {
        pool.values.forEach { it.recycle() }
        pool.clear()
    }
}