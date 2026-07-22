package com.gamelauncher.core

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
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
    
    private var originalFilter: Int = NotificationManager.INTERRUPTION_FILTER_ALL

    fun isDndPermissionGranted(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notificationManager?.isNotificationPolicyAccessGranted == true
            } else {
                true
            }
        } catch (_: Exception) { false }
    }

    fun openDndPermissionSettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun isGamingDndActive(): Boolean {
        if (!isDndPermissionGranted()) return false
        return runCatching {
            notificationManager?.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE
        }.getOrDefault(false)
    }

    suspend fun enableGamingDnd(): Boolean = withContext(Dispatchers.IO) {
        if (!isDndPermissionGranted()) return@withContext false

        try {
            originalFilter = runCatching {
                notificationManager?.currentInterruptionFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL
            }.getOrDefault(NotificationManager.INTERRUPTION_FILTER_ALL)

            // INTERRUPTION_FILTER_NONE blocks all notifications & calls as requested in v1.7.2
            notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            runCatching { audioManager?.ringerMode = AudioManager.RINGER_MODE_SILENT }

            true
        } catch (_: Exception) { false }
    }

    suspend fun disableGamingDnd(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (isDndPermissionGranted()) {
                notificationManager?.setInterruptionFilter(originalFilter)
            }
            runCatching { audioManager?.ringerMode = AudioManager.RINGER_MODE_NORMAL }
            true
        } catch (_: Exception) { false }
    }
}