package com.gamelauncher.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.gamelauncher.core.startManagedService
import com.gamelauncher.data.preference.SettingsPreferences
import com.gamelauncher.di.ApplicationScope
import com.gamelauncher.services.GameDetectorService
import com.gamelauncher.services.OverlayService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var settingsPreferences: SettingsPreferences

    // Injected application-scoped supervised CoroutineScope.
    // Avoids leaking a raw CoroutineScope that can't be cancelled.
    @Inject @ApplicationScope lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val bootAction = intent.action ?: return
        if (bootAction !in setOf(
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_LOCKED_BOOT_COMPLETED,
                "android.intent.action.QUICKBOOT_POWERON"
            )
        ) return

        val pendingResult = goAsync()
        appScope.launch {
            try {
                val shouldEnableOverlay = settingsPreferences.isOverlayEnabled.first()
                val shouldEnableDetector = settingsPreferences.gameDetectorEnabled.first()

                if (shouldEnableDetector) {
                    context.startManagedService(
                        Intent(context, GameDetectorService::class.java).apply {
                            this.action = GameDetectorService.ACTION_START_DETECTOR
                        }
                    )
                }

                val hasOverlayPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                        Settings.canDrawOverlays(context)
                if (shouldEnableOverlay && hasOverlayPermission) {
                    context.startManagedService(Intent(context, OverlayService::class.java))
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
