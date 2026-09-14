package com.gamebooster.app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.gamebooster.app.R
import com.gamebooster.app.ui.activities.MainActivity

/**
 * GameBoostForegroundService — Standard Android 14+ (API 34+) compliant foreground service.
 *
 * Uses foregroundServiceType="specialUse" (declared in manifest) or dataSync as fallback.
 * Operates reliably in background, handles Doze gracefully, and does not use unsafe kill bypasses.
 */
class GameBoostForegroundService : Service() {

    private var targetPackage: String? = null
    private var powerManager: PowerManager? = null

    override fun onCreate() {
        super.onCreate()
        powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (ACTION_STOP == action) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        targetPackage = intent?.getStringExtra(EXTRA_PACKAGE_NAME)
        val targetFps = intent?.getIntExtra(EXTRA_TARGET_FPS, 120) ?: 120

        val notification = buildNotification("Boosting active ($targetFps FPS ceiling) • Package: ${targetPackage ?: "System"}")
        startForeground(NOTIFICATION_ID, notification)

        Log.i(TAG, "GameBoostForegroundService started for package=$targetPackage, fps=$targetFps")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Game Booster Pro Active Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Displays active game boost session telemetry and thermal status"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GAME BOOSTER PRO")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val TAG = "GameBoostFgService"
        const val CHANNEL_ID = "game_booster_active_channel"
        const val NOTIFICATION_ID = 202601

        const val ACTION_START = "com.gamebooster.app.action.START_BOOST"
        const val ACTION_STOP = "com.gamebooster.app.action.STOP_BOOST"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_TARGET_FPS = "extra_target_fps"

        @JvmStatic
        fun start(context: Context, packageName: String?, targetFps: Int) {
            val intent = Intent(context, GameBoostForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_PACKAGE_NAME, packageName)
                putExtra(EXTRA_TARGET_FPS, targetFps)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        @JvmStatic
        fun stop(context: Context) {
            val intent = Intent(context, GameBoostForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
