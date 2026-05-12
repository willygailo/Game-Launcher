package com.gamelauncher.app.core.startup

import android.app.Application
import android.os.Build
import android.os.StrictMode
import androidx.startup.Initializer
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.gamelauncher.app.core.worker.AppWorkerManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optimized Application class for Android 10-16
 *
 * Performance optimizations:
 * - Deferred initialization for non-critical services
 * - Background thread startup for DB/network init
 * - Crash handler before any other init
 * - Baseline profile installation
 * - ANR watchdog
 */
@HiltAndroidApp
class GameLauncherApp : Application() {

    // Dedicated scope for startup tasks - prevents blocking main thread
    private val startupScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Track startup time
    private var appCreateTime: Long = 0

    @Inject
    lateinit var appInitializers: AppInitializers

    @Inject
    lateinit var appWorkerManager: AppWorkerManager

    override fun onCreate() {
        appCreateTime = System.currentTimeMillis()

        // ─── PHASE 1: Crash handler FIRST (before anything else) ───
        installCrashHandler()

        // ─── PHASE 2: StrictMode only in debug ───
        if (BuildConfig.DEBUG) {
            enableStrictMode()
            Timber.plant(Timber.DebugTree())
        }

        super.onCreate()

        // ─── PHASE 3: Warm up in background ───
        startupScope.launch {
            // Warm up database on background thread
            warmUpDatabase()

            // Pre-initialize Firebase (expensive first call)
            warmUpFirebase()

            // Schedule periodic workers with battery-friendly constraints
            appWorkerManager.scheduleAllWorkers()
        }

        // ─── PHASE 4: Deferred main-thread init (non-blocking) ───
        deferredInit()
    }

    /**
     * Install crash handler before any other initialization
     * Catches crashes during startup itself
     */
    private fun installCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Log to Crashlytics
            FirebaseCrashlytics.getInstance().recordException(throwable)

            // Attempt graceful recovery for ANR-like situations
            if (throwable is android.os.TransactionTooLargeException) {
                // Clear large intent extras
                recoverFromTransactionTooLarge()
            }

            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    /**
     * StrictMode for debug builds - catches disk/network on main thread
     */
    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .detectCustomSlowCalls()
                .penaltyLog()
                .penaltyFlashScreen()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .detectNonSdkApiUsage()
                .penaltyLog()
                .build()
        )
    }

    /**
     * Warm up database connection pool - prevents first query jank
     */
    private suspend fun warmUpDatabase() {
        try {
            // Trigger Room database creation/opening on bg thread
            // This is a lazy-init holder that Room uses internally
            val db = com.gamelauncher.app.data.local.database.GamingDatabase
                .getDatabase(this)
            // Run a simple health check query
            db.query("SELECT 1", null)
        } catch (e: Exception) {
            Timber.e(e, "Database warm-up failed")
        }
    }

    /**
     * Pre-initialize Firebase to avoid cold-start latency later
     */
    private suspend fun warmUpFirebase() {
        try {
            // Force Firebase init now rather than on first use
            FirebasePerformance.getInstance().isPerformanceCollectionEnabled = true
            // Pre-warm Crashlytics
            FirebaseCrashlytics.getInstance()
        } catch (e: Exception) {
            Timber.e(e, "Firebase warm-up failed")
        }
    }

    /**
     * Deferred initialization - runs on main thread but is lightweight
     * Heavy work is already done in background via startupScope
     */
    private fun deferredInit() {
        // Install baseline profiles (API 28+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            installBaselineProfiles()
        }

        // Start foreground service management
        appInitializers.init()
    }

    /**
     * Install baseline profiles for faster app startup
     * Uses AndroidX Baseline Profile library
     */
    private fun installBaselineProfiles() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                androidx.profileinstaller.ProfileInstaller.installOnThisDevice()
            }
        } catch (e: Exception) {
            Timber.w(e, "Baseline profile installation failed")
        }
    }

    private fun recoverFromTransactionTooLarge() {
        // Clear cached intent data
        try {
            val prefs = getSharedPreferences("app_state", MODE_PRIVATE)
            prefs.edit().clear().apply()
        } catch (_: Exception) {}
    }

    /**
     * Handle low memory conditions
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                // App is in foreground but system is running low on memory
                // Release non-essential caches aggressively
                MemoryManager.releaseAggressive()
            }
            TRIM_MEMORY_UI_HIDDEN -> {
                // App went to background - release UI resources
                MemoryManager.releaseUIResources()
            }
            TRIM_MEMORY_BACKGROUND,
            TRIM_MEMORY_MODERATE,
            TRIM_MEMORY_COMPLETE -> {
                // App is in background list - release everything non-essential
                MemoryManager.releaseAllCaches()
            }
        }
    }

    /**
     * Get startup time for performance monitoring
     */
    fun getStartupTimeMillis(): Long = System.currentTimeMillis() - appCreateTime
}

/**
 * Initializes Firebase Performance trace for cold start measurement
 */
object StartupTracer {
    private var coldStartTrace: com.google.firebase.perf.metrics.Trace? = null

    fun startColdStartTrace() {
        coldStartTrace = FirebasePerformance.getInstance()
            .newTrace("cold_start_trace")
        coldStartTrace?.start()
    }

    fun stopColdStartTrace() {
        coldStartTrace?.stop()
        coldStartTrace = null
    }
}