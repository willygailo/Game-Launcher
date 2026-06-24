package com.gamelauncher.ui.settings

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import com.gamelauncher.core.ProfileManager
import com.gamelauncher.core.startManagedService
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsPreferences: SettingsPreferences,
    private val performanceManager: PerformanceManager,
    private val immersiveModeManager: ImmersiveModeManager,
    private val networkManager: NetworkManager,
    private val profileManager: ProfileManager
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

    val isGameDetectorEnabled: StateFlow<Boolean> = settingsPreferences.gameDetectorEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val isDarkTheme: StateFlow<Boolean> = settingsPreferences.isDarkTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    // Secure settings toggles
    val secureAnimScale: StateFlow<Boolean> = settingsPreferences.secureSettingsAnimScale.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val secureGameDriver: StateFlow<Boolean> = settingsPreferences.secureSettingsGameDriver.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val secureSyncOff: StateFlow<Boolean> = settingsPreferences.secureSettingsSyncOff.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val secureMobileData: StateFlow<Boolean> = settingsPreferences.secureSettingsMobileData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val secureBatterySaver: StateFlow<Boolean> = settingsPreferences.secureSettingsBatterySaver.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val secureLocationOff: StateFlow<Boolean> = settingsPreferences.secureSettingsLocationOff.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val hasUsageAccessPermission: StateFlow<Boolean> = stateFlowFrom {
        hasUsageAccess()
    }

    val hasOverlayPermission: StateFlow<Boolean> = stateFlowFrom {
        canDrawOverlays()
    }

    val hasWriteSettingsPermission: StateFlow<Boolean> = stateFlowFrom {
        immersiveModeManager.hasWriteSettingsPermission()
    }

    val hasNotificationPermission: StateFlow<Boolean> = stateFlowFrom {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val pm = context.packageManager
            pm.checkPermission(
                android.Manifest.permission.POST_NOTIFICATIONS,
                context.packageName
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    val hasBatteryExemption: StateFlow<Boolean> = stateFlowFrom {
        val pm = context.getSystemService(PowerManager::class.java)
        pm?.isIgnoringBatteryOptimizations(context.packageName) == true
    }

    /** True when adb granted WRITE_SECURE_SETTINGS — we probe by trying a no-op write */
    val hasWriteSecureSettings: StateFlow<Boolean> = stateFlowFrom {
        try {
            val cr = context.contentResolver
            val cur = android.provider.Settings.Global.getInt(cr, "adb_enabled", 0)
            android.provider.Settings.Global.putInt(cr, "adb_enabled", cur) // write same value back
            true
        } catch (e: SecurityException) { false }
    }

    private val _profileMessage = MutableStateFlow<String?>(null)
    val profileMessage: StateFlow<String?> = _profileMessage.asStateFlow()

    fun clearProfileMessage() { _profileMessage.value = null }

    fun exportProfiles() {
        viewModelScope.launch {
            try {
                val uri = profileManager.exportProfiles()
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Export Game Profiles"))
                _profileMessage.value = "Profiles exported successfully"
            } catch (e: Exception) {
                _profileMessage.value = "Export failed: ${e.message}"
            }
        }
    }

    fun importProfiles(uri: Uri) {
        viewModelScope.launch {
            try {
                val count = profileManager.importProfiles(uri)
                _profileMessage.value = "Imported $count game profiles"
            } catch (e: Exception) {
                _profileMessage.value = "Import failed: ${e.message}"
            }
        }
    }

    private fun <T> stateFlowFrom(block: () -> T): StateFlow<T> {
        return kotlinx.coroutines.flow.MutableStateFlow(block()).also { flow ->
            viewModelScope.launch {
                while (isActive) {
                    kotlinx.coroutines.delay(2000)
                    flow.value = block()
                }
            }
        }
    }

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
                if (canDrawOverlays()) {
                    context.startManagedService(Intent(context, OverlayService::class.java))
                } else {
                    requestOverlayPermission()
                }
            } else {
                val intent = Intent(context, OverlayService::class.java).apply {
                    action = OverlayService.ACTION_STOP
                }
                context.startManagedService(intent)
            }
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setDarkTheme(enabled)
        }
    }

    fun setSecureAnimScale(enabled: Boolean) {
        viewModelScope.launch { settingsPreferences.setSecureSettingsAnimScale(enabled) }
    }

    fun setSecureGameDriver(enabled: Boolean) {
        viewModelScope.launch { settingsPreferences.setSecureSettingsGameDriver(enabled) }
    }

    fun setSecureSyncOff(enabled: Boolean) {
        viewModelScope.launch { settingsPreferences.setSecureSettingsSyncOff(enabled) }
    }

    fun setSecureMobileData(enabled: Boolean) {
        viewModelScope.launch { settingsPreferences.setSecureSettingsMobileData(enabled) }
    }

    fun setSecureBatterySaver(enabled: Boolean) {
        viewModelScope.launch { settingsPreferences.setSecureSettingsBatterySaver(enabled) }
    }

    fun setSecureLocationOff(enabled: Boolean) {
        viewModelScope.launch { settingsPreferences.setSecureSettingsLocationOff(enabled) }
    }

    fun setGameDetectorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setGameDetectorEnabled(enabled)
            if (enabled && !hasUsageAccess()) {
                requestUsageAccessPermission()
                return@launch
            }
            val intent = Intent(context, GameDetectorService::class.java).apply {
                action = if (enabled) {
                    GameDetectorService.ACTION_START_DETECTOR
                } else {
                    GameDetectorService.ACTION_STOP_DETECTOR
                }
            }
            context.startManagedService(intent)
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
                context.startManagedService(boostIntent)
            } catch (e: Exception) {}
            
            // Stop Overlay Service
            try {
                val overlayIntent = Intent(context, OverlayService::class.java).apply {
                    action = OverlayService.ACTION_STOP
                }
                context.startManagedService(overlayIntent)
            } catch (e: Exception) {}
            
            // Stop Game Detector
            try {
                val detectorIntent = Intent(context, GameDetectorService::class.java).apply {
                    action = GameDetectorService.ACTION_STOP_DETECTOR
                }
                context.startManagedService(detectorIntent)
            } catch (e: Exception) {}
            
            // Reset preferences
            settingsPreferences.setGlobalAutoBoost(false)
            settingsPreferences.setOverlayEnabled(false)
            settingsPreferences.setGameDetectorEnabled(false)
        }
    }

    fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun requestUsageAccessPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun requestBatteryOptimizationExemption() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }.onFailure {
            val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(fallback) }
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

    private fun canDrawOverlays(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
    }

    private fun hasUsageAccess(): Boolean {
        return try {
            val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)
            val now = System.currentTimeMillis()
            val stats = usageStatsManager?.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                now - 10000,
                now
            )
            stats != null && stats.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}
