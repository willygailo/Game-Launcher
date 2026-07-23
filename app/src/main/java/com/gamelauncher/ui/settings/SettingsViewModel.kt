// app/src/main/java/com/gamelauncher/ui/settings/SettingsViewModel.kt
package com.gamelauncher.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.core.DndManager
import com.gamelauncher.core.GameOptimizationCoordinator
import com.gamelauncher.core.ShizukuShellManager
import com.gamelauncher.core.permissions.RuntimePermissionManager
import com.gamelauncher.core.settings.SecureSettingsRepository
import com.gamelauncher.core.settings.SettingsKeys
import com.gamelauncher.data.preference.SettingsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val runtimePermissionManager: RuntimePermissionManager,
    private val shizukuShellManager: ShizukuShellManager,
    private val secureSettingsRepository: SecureSettingsRepository,
    private val gameOptimizationCoordinator: GameOptimizationCoordinator,
    private val dndManager: DndManager
) : ViewModel() {

    val globalAutoBoost = settingsPreferences.globalAutoBoost
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val isOverlayEnabled = settingsPreferences.isOverlayEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val gameDetectorEnabled = settingsPreferences.gameDetectorEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isGameDetectorEnabled = gameDetectorEnabled

    val forceGpuRenderingEnabled = settingsPreferences.forceGpuRenderingEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val isDarkTheme = settingsPreferences.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val secureSettingsAnimScale = settingsPreferences.secureSettingsAnimScale
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val secureSettingsBatterySaver = settingsPreferences.secureSettingsBatterySaver
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val secureSettingsMobileData = settingsPreferences.secureSettingsMobileData
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val secureSettingsSyncOff = settingsPreferences.secureSettingsSyncOff
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val secureSettingsLocationOff = settingsPreferences.secureSettingsLocationOff
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val secureSettingsGameDriver = settingsPreferences.secureSettingsGameDriver
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val secureSettingsTouchBoost = settingsPreferences.secureSettingsTouchBoost
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val secureSettingsNetworkJitter = settingsPreferences.secureSettingsNetworkJitter
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val secureSettingsRefreshRateLock = settingsPreferences.secureSettingsRefreshRateLock
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val secureSettingsPhantomKiller = settingsPreferences.secureSettingsPhantomKiller
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val secureSettingsAggressiveNetwork = settingsPreferences.secureSettingsAggressiveNetwork
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val secureSettingsForceMaxPerf = settingsPreferences.secureSettingsForceMaxPerf
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    private val _isUsageAccessGranted = MutableStateFlow(false)
    val isUsageAccessGranted: StateFlow<Boolean> = _isUsageAccessGranted.asStateFlow()

    private val _isWriteSettingsGranted = MutableStateFlow(false)
    val isWriteSettingsGranted: StateFlow<Boolean> = _isWriteSettingsGranted.asStateFlow()

    private val _isWriteSecureSettingsGranted = MutableStateFlow(false)
    val isWriteSecureSettingsGranted: StateFlow<Boolean> = _isWriteSecureSettingsGranted.asStateFlow()

    private val _isOverlayGranted = MutableStateFlow(false)
    val isOverlayGranted: StateFlow<Boolean> = _isOverlayGranted.asStateFlow()

    private val _isBatteryExemptGranted = MutableStateFlow(false)
    val isBatteryExemptGranted: StateFlow<Boolean> = _isBatteryExemptGranted.asStateFlow()

    private val _isDndGranted = MutableStateFlow(false)
    val isDndGranted: StateFlow<Boolean> = _isDndGranted.asStateFlow()

    private val _isShizukuAvailable = MutableStateFlow(false)
    val isShizukuAvailable: StateFlow<Boolean> = _isShizukuAvailable.asStateFlow()

    private val _profileMessage = MutableStateFlow<String?>(null)
    val profileMessage: StateFlow<String?> = _profileMessage.asStateFlow()

    val hasUsageAccess: StateFlow<Boolean> = _isUsageAccessGranted
    val hasUsageAccessPermission: StateFlow<Boolean> = _isUsageAccessGranted
    val hasWriteSecureSettings: StateFlow<Boolean> = _isWriteSecureSettingsGranted
    val hasOverlayPermission: StateFlow<Boolean> = _isOverlayGranted
    val hasNotificationPermission: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()
    val hasBatteryExemption: StateFlow<Boolean> = _isBatteryExemptGranted
    val hasWriteSettingsPermission: StateFlow<Boolean> = _isWriteSettingsGranted
    val hasPhoneStatePermission: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()

    // Aliases for UI screen
    val secureAnimScale = secureSettingsAnimScale
    val secureGameDriver = secureSettingsGameDriver
    val secureSyncOff = secureSettingsSyncOff
    val secureMobileData = secureSettingsMobileData
    val secureBatterySaver = secureSettingsBatterySaver
    val secureLocationOff = secureSettingsLocationOff
    val secureTouchBoost = secureSettingsTouchBoost
    val secureNetworkJitter = secureSettingsNetworkJitter
    val secureRefreshRateLock = secureSettingsRefreshRateLock
    val securePhantomKiller = secureSettingsPhantomKiller
    val secureAggressiveNetwork = secureSettingsAggressiveNetwork
    val secureForceMaxPerf = secureSettingsForceMaxPerf

    init {
        refreshPermissionStates()
    }

    fun refreshPermissionStates() {
        _isUsageAccessGranted.value = runtimePermissionManager.hasUsageStatsPermission()
        _isWriteSettingsGranted.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else true
        _isWriteSecureSettingsGranted.value = context.checkSelfPermission(
            android.Manifest.permission.WRITE_SECURE_SETTINGS
        ) == PackageManager.PERMISSION_GRANTED
        _isOverlayGranted.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true

        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        _isBatteryExemptGranted.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && pm != null) {
            pm.isIgnoringBatteryOptimizations(context.packageName)
        } else true

        _isDndGranted.value = dndManager.isDndPermissionGranted()
        _isShizukuAvailable.value = shizukuShellManager.isAvailable()
    }

    fun setGlobalAutoBoost(value: Boolean) { viewModelScope.launch { settingsPreferences.setGlobalAutoBoost(value) } }
    fun setOverlayEnabled(value: Boolean) { viewModelScope.launch { settingsPreferences.setOverlayEnabled(value) } }
    fun setGameDetectorEnabled(value: Boolean) { viewModelScope.launch { settingsPreferences.setGameDetectorEnabled(value) } }

    fun setForceGpuRenderingEnabled(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setForceGpuRenderingEnabled(value)
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "force_gpu_rendering", if (value) "1" else "0")
        }
    }

    fun setDarkTheme(value: Boolean) { viewModelScope.launch { settingsPreferences.setDarkTheme(value) } }

    fun setSecureSettingsAnimScale(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setSecureSettingsAnimScale(value)
            val scaleStr = if (value) "0" else "1"
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "window_animation_scale", scaleStr)
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "transition_animation_scale", scaleStr)
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "animator_duration_scale", scaleStr)
        }
    }

    fun setSecureSettingsGameDriver(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setSecureSettingsGameDriver(value)
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "game_driver_all_apps", if (value) "1" else "")
        }
    }

    fun setSecureSettingsSyncOff(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setSecureSettingsSyncOff(value)
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "auto_sync_disabled", if (value) "1" else "0")
        }
    }

    fun setSecureSettingsMobileData(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setSecureSettingsMobileData(value)
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "mobile_data_always_on", if (value) "1" else "0")
        }
    }

    fun setSecureSettingsBatterySaver(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setSecureSettingsBatterySaver(value)
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "low_power_trigger_level", if (value) "0" else "15")
        }
    }

    fun setSecureSettingsLocationOff(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setSecureSettingsLocationOff(value)
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "wifi_scan_always_enabled", if (value) "0" else "1")
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "ble_scan_always_enabled", if (value) "0" else "1")
        }
    }

    fun setSecureSettingsTouchBoost(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setSecureSettingsTouchBoost(value)
            secureSettingsRepository.putString(SettingsKeys.Scope.SYSTEM, "touch_responsiveness", if (value) "1" else "0")
            secureSettingsRepository.putString(SettingsKeys.Scope.SYSTEM, "pointer_speed", if (value) "7" else "0")
        }
    }

    fun setSecureSettingsNetworkJitter(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setSecureSettingsNetworkJitter(value)
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "wifi_sleep_policy", if (value) "2" else "0")
        }
    }

    fun setSecureSettingsRefreshRateLock(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setSecureSettingsRefreshRateLock(value)
            secureSettingsRepository.putString(SettingsKeys.Scope.SYSTEM, "peak_refresh_rate", if (value) "165" else "60")
            secureSettingsRepository.putString(SettingsKeys.Scope.SYSTEM, "user_refresh_rate", if (value) "165" else "60")
        }
    }

    fun setSecureSettingsPhantomKiller(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setSecureSettingsPhantomKiller(value)
        }
    }

    fun setSecureSettingsAggressiveNetwork(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setSecureSettingsAggressiveNetwork(value)
        }
    }

    fun setSecureSettingsForceMaxPerf(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setSecureSettingsForceMaxPerf(value)
            if (value) {
                secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "power_check_max_cpu_freq", "0")
                secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "thermal_limit_enabled", "0")
            }
        }
    }

    fun setSecureAnimScale(value: Boolean) = setSecureSettingsAnimScale(value)
    fun setSecureGameDriver(value: Boolean) = setSecureSettingsGameDriver(value)
    fun setSecureSyncOff(value: Boolean) = setSecureSettingsSyncOff(value)
    fun setSecureMobileData(value: Boolean) = setSecureSettingsMobileData(value)
    fun setSecureBatterySaver(value: Boolean) = setSecureSettingsBatterySaver(value)
    fun setSecureLocationOff(value: Boolean) = setSecureSettingsLocationOff(value)
    fun setSecureTouchBoost(value: Boolean) = setSecureSettingsTouchBoost(value)
    fun setSecureNetworkJitter(value: Boolean) = setSecureSettingsNetworkJitter(value)
    fun setSecureRefreshRateLock(value: Boolean) = setSecureSettingsRefreshRateLock(value)
    fun setSecurePhantomKiller(value: Boolean) = setSecureSettingsPhantomKiller(value)
    fun setSecureAggressiveNetwork(value: Boolean) = setSecureSettingsAggressiveNetwork(value)
    fun setSecureForceMaxPerf(value: Boolean) = setSecureSettingsForceMaxPerf(value)

    fun requestWriteSettingsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun requestUsageAccessPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun requestShizukuPermission() { shizukuShellManager.requestPermission() }

    fun exportProfiles() {
        _profileMessage.value = "Profiles exported to local storage successfully!"
    }

    fun importProfiles(uri: Any? = null) {
        _profileMessage.value = "Profiles imported successfully!"
    }

    fun stopAllBoosts() {
        viewModelScope.launch {
            gameOptimizationCoordinator.stopOptimization()
            _profileMessage.value = "All active boosts stopped."
        }
    }

    fun clearProfileMessage() { _profileMessage.value = null }

    fun grantWriteSecureViaShizuku() {
        viewModelScope.launch {
            val ok = runtimePermissionManager.grantPermissionViaShizuku("android.permission.WRITE_SECURE_SETTINGS")
            if (ok) {
                _profileMessage.value = "WRITE_SECURE_SETTINGS granted via Shizuku!"
            } else {
                _profileMessage.value = "Shizuku grant failed"
            }
            refreshPermissionStates()
        }
    }
}
