package com.gamelauncher.core

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import rikka.shizuku.Shizuku

@HiltAndroidApp
class GameLauncherApp : Application() {

    companion object {
        const val CHANNEL_BOOSTER = "game_booster_channel"
        const val CHANNEL_OVERLAY = "fps_overlay_channel"
        const val CHANNEL_ALERTS  = "alerts_channel"
        const val ACTION_SHIZUKU_CHANGED = "com.gamelauncher.SHIZUKU_STATE_CHANGED"
    }

    // Shizuku binder state listeners — must be registered before pingBinder() works
    private val shizukuBinderReceived = Shizuku.OnBinderReceivedListener {
        Log.d("Shizuku", "✅ Shizuku binder connected")
        sendBroadcast(android.content.Intent(ACTION_SHIZUKU_CHANGED))
    }
    private val shizukuBinderDead = Shizuku.OnBinderDeadListener {
        Log.d("Shizuku", "💀 Shizuku binder disconnected")
        sendBroadcast(android.content.Intent(ACTION_SHIZUKU_CHANGED))
    }

    override fun onCreate() {
        super.onCreate()
        // Register Shizuku binder listeners early — required for pingBinder() to work
        Shizuku.addBinderReceivedListenerSticky(shizukuBinderReceived)
        Shizuku.addBinderDeadListener(shizukuBinderDead)
        createNotificationChannels()
    }

    override fun onTerminate() {
        Shizuku.removeBinderReceivedListener(shizukuBinderReceived)
        Shizuku.removeBinderDeadListener(shizukuBinderDead)
        super.onTerminate()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)

            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_BOOSTER,
                    "Game Booster",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Active game boost status" }
            )

            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_OVERLAY,
                    "FPS Overlay",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Floating FPS counter" }
            )

            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ALERTS,
                    "Performance Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Thermal and RAM warnings" }
            )
        }
    }
}

