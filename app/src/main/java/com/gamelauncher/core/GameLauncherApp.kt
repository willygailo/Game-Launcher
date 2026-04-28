package com.gamelauncher.core

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GameLauncherApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
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

    companion object {
        const val CHANNEL_BOOSTER = "game_booster_channel"
        const val CHANNEL_OVERLAY = "fps_overlay_channel"
        const val CHANNEL_ALERTS  = "alerts_channel"
    }
}
