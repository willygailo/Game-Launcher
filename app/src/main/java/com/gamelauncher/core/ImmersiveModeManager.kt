package com.gamelauncher.core

import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Immersive Gaming features such as Do Not Disturb,
 * Auto-Brightness disabling, and Volume Locking.
 */
@Singleton
class ImmersiveModeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val contentResolver: ContentResolver = context.contentResolver

    // State preservation to restore after gaming
    private var originalDndState: Int = NotificationManager.INTERRUPTION_FILTER_ALL
    private var originalBrightnessMode: Int = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
    private var originalBrightness: Int = 127
    
    // ── DND / Notification Blocker ───────────────────────────────────────
    fun hasDndPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
    }

    fun getDndPermissionIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun enableGamingDnd(): Boolean {
        if (!hasDndPermission()) return false
        return try {
            originalDndState = notificationManager.currentInterruptionFilter
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun disableGamingDnd(): Boolean {
        if (!hasDndPermission()) return false
        return try {
            notificationManager.setInterruptionFilter(originalDndState)
            true
        } catch (e: Exception) {
            try {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            } catch (_: Exception) {}
            false
        }
    }

    // ── Brightness Control ───────────────────────────────────────────────
    fun hasWriteSettingsPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true
        }
    }

    fun getWriteSettingsPermissionIntent(): Intent {
        return Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun lockBrightness(level: Int = 255): Boolean {
        if (!hasWriteSettingsPermission()) return false
        return try {
            originalBrightnessMode = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
            originalBrightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            
            // Disable Auto-Brightness
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            // Set max brightness
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, level.coerceIn(1, 255))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun restoreBrightness(): Boolean {
        if (!hasWriteSettingsPermission()) return false
        return try {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, originalBrightnessMode)
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, originalBrightness)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ── Volume Control ───────────────────────────────────────────────────
    fun optimizeMediaVolume(): Boolean {
        return try {
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            
            // If volume is too low, set it to a decent gaming level (e.g., 70%)
            val targetVolume = (maxVolume * 0.7).toInt()
            if (currentVolume < targetVolume) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
