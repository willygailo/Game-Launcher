package com.gamelauncher.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.core.ImmersiveModeManager
import com.gamelauncher.core.NetworkManager
import com.gamelauncher.core.PerformanceManager
import com.gamelauncher.data.preference.SettingsPreferences
import com.gamelauncher.services.GameBoosterService
import com.gamelauncher.services.OverlayService
import com.gamelauncher.services.GameDetectorService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsPreferences: SettingsPreferences,
    private val performanceManager: PerformanceManager,
    private val immersiveModeManager: ImmersiveModeManager,
    private val networkManager: NetworkManager
) : ViewModel() {

    val globalAutoBoost: StateFlow<Boolean> = settingsPreferences.globalAutoBoost.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
    
    val isOverlayEnabled: StateFlow<Boolean> = settingsPreferences.isOverlayEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun setGlobalAutoBoost(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setGlobalAutoBoost(enabled)
            if (enabled) {
                performanceManager.boostThreadPriority()
            } else {
                performanceManager.restoreThreadPriority()
            }
        }
    }
    
    fun setOverlayEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setOverlayEnabled(enabled)
            if (enabled) {
                val intent = Intent(context, OverlayService::class.java)
                context.startService(intent)
            } else {
                val intent = Intent(context, OverlayService::class.java).apply {
                    action = OverlayService.ACTION_STOP
                }
                context.startService(intent)
            }
        }
    }

    fun stopAllBoosts() {
        viewModelScope.launch {
            // Stop all performance boosting
            performanceManager.restoreThreadPriority()
            performanceManager.restoreAnimations()
            performanceManager.stopPerformanceSession()
            
            // Stop WiFi lock
            networkManager.releaseWifiLock()
            
            // Restore brightness if locked
            try {
                immersiveModeManager.restoreBrightness()
            } catch (e: Exception) {}
            
            // Stop DND
            try {
                immersiveModeManager.disableGamingDnd()
            } catch (e: Exception) {}
            
            // Stop Game Booster Service
            try {
                val boostIntent = Intent(context, GameBoosterService::class.java).apply {
                    action = GameBoosterService.ACTION_STOP_BOOST
                }
                context.startService(boostIntent)
            } catch (e: Exception) {}
            
            // Stop Overlay Service
            try {
                val overlayIntent = Intent(context, OverlayService::class.java).apply {
                    action = OverlayService.ACTION_STOP
                }
                context.startService(overlayIntent)
            } catch (e: Exception) {}
            
            // Stop Game Detector
            try {
                val detectorIntent = Intent(context, GameDetectorService::class.java).apply {
                    action = GameDetectorService.ACTION_STOP_DETECTOR
                }
                context.startService(detectorIntent)
            } catch (e: Exception) {}
            
            // Reset preferences
            settingsPreferences.setGlobalAutoBoost(false)
            settingsPreferences.setOverlayEnabled(false)
        }
    }

    fun requestWriteSettingsPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Try alternative
            val altIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(altIntent)
        }
    }
}