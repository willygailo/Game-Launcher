package com.gamelauncher.core

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DndManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val audioManager = context.getSystemService(AudioManager::class.java)
    
    private var originalRingerMode: Int = AudioManager.RINGER_MODE_NORMAL

    fun isDndPermissionGranted(): Boolean {
        return try {
            notificationManager?.isNotificationPolicyAccessGranted ?: false
        } catch (_: Exception) { false }
    }

    fun openDndPermissionSettings() {
        try {
            val intent = android.content.Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    suspend fun enableGamingDnd(): Boolean = withContext(Dispatchers.IO) {
        if (!isDndPermissionGranted()) return@withContext false

        try {
            originalRingerMode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL

            notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            
            audioManager?.ringerMode = AudioManager.RINGER_MODE_SILENT

            true
        } catch (_: Exception) { false }
    }

    suspend fun disableGamingDnd(): Boolean = withContext(Dispatchers.IO) {
        try {
            notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            audioManager?.ringerMode = AudioManager.RINGER_MODE_NORMAL

            true
        } catch (_: Exception) { false }
    }

    suspend fun suppressAllNotifications(): Boolean = enableGamingDnd()

    suspend fun restoreAllNotifications(): Boolean = disableGamingDnd()
}