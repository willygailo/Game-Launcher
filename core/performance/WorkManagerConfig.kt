package com.gamelauncher.core.performance.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.gamelauncher.BuildConfig
import com.gamelauncher.R
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Centralized WorkManager configuration for the game launcher.
 *
 * Handles Android 10-16 background execution limits:
 * - Android 10 (Q): Background starts restriction
 * - Android 12 (S): Exact alarm permission
 * - Android 13 (T): Foreground service type requirement
 * - Android 14 (U): Foreground service types for data transfer/sync
 * - Android 16: Stricter battery optimization
 *
 *
 * Key changes from original:
 * - Foreground service types properly declared per API level
 * - Work constraints respect battery optimization
 * - Periodic work uses flex intervals for batching
 */
object WorkManagerConfig {

    // Unique work names (prevent duplicate scheduling)
    const val SYNC_PLAYER_DATA = "sync_player_data"
    const val CLEAN_CACHE = "clean_cache"
    const val UPLOAD_GAME_RESULT = "upload_game_result"
    const val DAILY_REWARDS_CHECK = "daily_rewards_check"
    const val NOTIFICATION_SYNC = "notification_sync"

    // Channel IDs for foreground services
    const val CHANNEL_SYNC = "game_sync"
    const val CHANNEL_UPLOAD = "game_upload"
    const val CHANNEL_NOTIFICATION = "game_notifications"

    /**
     * Creates constraints appropriate for the current API level.
     * On battery saver / Doze mode: work is deferred automatically.
     */
    fun defaultConstraints(
        requiresNetwork: Boolean = true,
        requiresBatteryNotLow: Boolean = true
    ): Constraints {
        val builder = Constraints.Builder()
            .setRequiresBatteryNotLow(requiresBatteryNotLow)
            .setRequiresStorageNotLow(true)

        if (requiresNetwork) {
            builder.setRequiredNetworkType(NetworkType.CONNECTED)
        }

        // Android 12+: Only schedule exact alarms when truly needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setRequiresBatteryNotLow(true)
        }

        return builder.build()
    }

    /**
     * One-time work request with retry and backoff.
     * Use for: uploading game results, critical syncs
     */
    fun oneTimeUploadRequest(
        inputData: Data,
        existingPolicy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP
    ): OneTimeWorkRequest {
        return OneTimeWorkRequest.Builder(UploadGameResultWorker::class.java)
            .setInputData(inputData)
            .setConstraints(defaultConstraints())
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag("upload")
            .build()
    }

    /**
     * Periodic sync work.
     * Android 10+: Minimum interval is 15 minutes.
     * Use flex interval to batch work and reduce wake-ups.
     */
    fun periodicSyncRequest(
        intervalMinutes: Long = 60L,
        flexMinutes: Long = 15L  // Within this window, system batches execution
    ): PeriodicWorkRequest {
        val actualInterval = intervalMinutes.coerceAtLeast(15L) // API 29+ minimum
        val actualFlex = (actualInterval * 0.25).coerceAtLeast(15.0).toLong()

        return PeriodicWorkRequest.Builder(
            SyncPlayerDataWorker::class.java,
            actualInterval, TimeUnit.MINUTES,
            actualFlex, TimeUnit.MINUTES
        )
            .setConstraints(
                defaultConstraints(
                    requiresNetwork = true,
                    requiresBatteryNotLow = false // Allow on low battery for progress sync
                )
            )
            .addTag("periodic_sync")
            .build()
    }

    /**
     * Cache cleanup work - runs even on low battery.
     */
    fun cacheCleanupRequest(): PeriodicWorkRequest {
        return PeriodicWorkRequest.Builder(
            CleanCacheWorker::class.java,
            12, TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresStorageNotLow(true)
                    .setRequiresBatteryNotLow(false) // Run even on low battery
                    .build()
            )
            .addTag("cache_cleanup")
            .build()
    }

    /**
     * Initializes WorkManager channels and logging.
     * Call from Application.onCreate()
     */
    fun initialize(context: Context) {
        createNotificationChannels(context)
        configureLogging()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannels(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channels = listOf(
            NotificationChannel(
                CHANNEL_SYNC,
                "Game Sync",
                NotificationManager.IMPORTANCE_LOW // Don't disturb players
            ).apply {
                description = "Background game data synchronization"
                setShowBadge(false)
            },
            NotificationChannel(
                CHANNEL_UPLOAD,
                "Result Upload",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Uploading game results"
                setShowBadge(false)
            },
            NotificationChannel(
                CHANNEL_NOTIFICATION,
                "Game Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Game events, rewards, updates"
                setShowBadge(true)
                enableVibration(true)
                enableLights(true)
            }
        )

        notificationManager.createNotificationChannels(channels)
    }

    private fun configureLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun log(
                    priority: Int,
                    tag: String?,
                    message: String,
                    t: Throwable?
                ) {
                    super.log(priority, "WorkManager-$tag", message, t)
                }
            })
        }
    }
}


/**
 * Hilt Worker Factory - injects dependencies into Workers.
 *
 *
 * Usage in Application:
 * ```
 * @HiltAndroidApp
 * class GameLauncherApp : Application(), Configuration.Provider {
 *     @Inject lateinit var workerFactory: HiltWorkerFactory
 *
 *     override fun getWorkManagerConfiguration() =
 *         Configuration.Builder()
 *             .setWorkerFactory(workerFactory)
 *             .build()
 * }
 * ```
 */
// Note: This class requires Hilt annotation processing.
// In the Dagger module, bind like:
// @Binds @IntoMap @WorkerKey(CleanCacheWorker::class)
// abstract fun bindCleanCacheWorker(factory: CleanCacheWorker.Factory): ChildWorkerFactory


/**
 * Manages foreground service lifecycle for Android 10-16.
 *
 * Starting Android 14 (API 34), foreground service types must be declared
 * BEFORE starting the service. This helper handles all API level differences.
 */
object ForegroundServiceHelper {

    /**
     * Starts a foreground service with the correct type for the API level.
     *
     *
     * On Android 14+: Uses foregroundServiceType parameter
     * On Android 13+: Requires POST_NOTIFICATIONS permission at runtime
     * On Android 10+: Background start restrictions apply
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun startForegroundServiceCompat(
        context: Context,
        intent: android.content.Intent,
        serviceType: ServiceType = ServiceType.SYNC
    ) {
        // Android 14+: Must specify foreground service type before start
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val foregroundServiceType = when (serviceType) {
                ServiceType.SYNC -> android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                ServiceType.UPLOAD -> android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_TRANSFER
                ServiceType.NOTIFICATION -> android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_TRANSFER
                ServiceType.LOCATION -> android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                else -> 0
            }
            context.startForegroundService(intent, foregroundServiceType)
        } else {
            @Suppress("DEPRECATION")
            context.startForegroundService(intent)
        }
    }

    enum class ServiceType {
        SYNC, UPLOAD, NOTIFICATION, LOCATION, SPECIAL_USE
    }
}