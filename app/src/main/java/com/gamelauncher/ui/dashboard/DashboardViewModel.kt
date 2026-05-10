package com.gamelauncher.ui.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.core.DeviceManager
import com.gamelauncher.core.FpsMonitor
import com.gamelauncher.core.ImmersiveModeManager
import com.gamelauncher.core.NetworkManager
import com.gamelauncher.core.PerformanceManager
import com.gamelauncher.core.RootShellManager
import com.gamelauncher.data.model.DeviceSpecs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceManager: DeviceManager,
    private val performanceManager: PerformanceManager,
    private val networkManager: NetworkManager,
    private val immersiveModeManager: ImmersiveModeManager,
    private val rootShellManager: RootShellManager,
    private val fpsMonitor: FpsMonitor
) : ViewModel() {

    private val _deviceSpecs = MutableStateFlow(DeviceSpecs())
    val deviceSpecs: StateFlow<DeviceSpecs> = _deviceSpecs.asStateFlow()

    private val _isDndEnabled = MutableStateFlow(false)
    val isDndEnabled: StateFlow<Boolean> = _isDndEnabled.asStateFlow()

    private val _isBrightnessLocked = MutableStateFlow(false)
    val isBrightnessLocked: StateFlow<Boolean> = _isBrightnessLocked.asStateFlow()

    private val _isRootAvailable = MutableStateFlow(false)
    val isRootAvailable: StateFlow<Boolean> = _isRootAvailable.asStateFlow()

    init {
        startMonitoring()
        startFpsMonitoring()
        checkRootStatus()
        refreshPermissionStates()
    }

    private fun startFpsMonitoring() {
        viewModelScope.launch {
            fpsMonitor.getFpsFlow().collect { fps ->
                _deviceSpecs.value = _deviceSpecs.value.copy(currentFps = fps)
            }
        }
    }

    fun refreshPermissionStates() {
        _isDndEnabled.value = immersiveModeManager.isGamingDndActive()
        _isBrightnessLocked.value = immersiveModeManager.isBrightnessLocked()
    }

    private fun checkRootStatus() {
        viewModelScope.launch {
            _isRootAvailable.value = rootShellManager.isRootAvailable()
        }
    }

    private fun startMonitoring() {
        viewModelScope.launch {
            while (true) {
                val (ramTotal, ramUsed, ramFree) = deviceManager.getRamInfo()
                val socInfo = deviceManager.getSocInfo()
                
                _deviceSpecs.value = _deviceSpecs.value.copy(
                    socName = socInfo.socName,
                    architecture = socInfo.architecture,
                    deviceRating = deviceManager.getDeviceRating(),
                    isGamingOptimized = socInfo.isGamingOptimized,

                    cpuUsagePercent = deviceManager.getCpuUsagePercent(),
                    cpuFreqMhz = deviceManager.getCpuFreqMhz(),
                    cpuCoreCount = deviceManager.getCoreCount(),
                    cpuGovernor = deviceManager.getCpuGovernor(),
                    gpuUsagePercent = performanceManager.getGpuUsagePercent(),
                    gpuFreqMhz = performanceManager.getGpuFreqMhz(),
                    gpuRenderer = performanceManager.getGpuRenderer(),
                    ramTotalMb = ramTotal,
                    ramUsedMb = ramUsed,
                    ramFreeMb = ramFree,
                    batteryLevel = deviceManager.getBatteryLevelInt(),
                    batteryTemperature = deviceManager.getBatteryTemperatureFloat(),
                    batteryChargingStatus = deviceManager.getBatteryStatusString(),
                    batteryHealth = deviceManager.getBatteryHealth(),
                    batteryVoltage = deviceManager.getBatteryVoltage(),
                    thermalStatus = deviceManager.getThermalStatus(),
                    networkType = networkManager.getNetworkType(),
                    networkStrengthDbm = networkManager.getWifiSignalDbm(),
                    wifiLinkSpeedMbps = networkManager.getWifiLinkSpeedMbps(),
                    displayRefreshRateHz = performanceManager.getCurrentRefreshRate(),
                    supportedRefreshRates = performanceManager.getSupportedRefreshRates()
                )
                delay(1000)
            }
        }
    }

    fun optimizeRam() {
        viewModelScope.launch {
            // Force garbage collection first
            val freedGC = deviceManager.optimizeMemory()
            
            // Then try to kill background apps
            val freedApps = deviceManager.killBackgroundApps()
            
            val totalFreed = maxOf(freedGC, freedApps)
            _deviceSpecs.value = _deviceSpecs.value.copy(freedRamMb = totalFreed)
        }
    }

    // DND Methods
    fun requestDndPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Permission denied or not found
        }
    }

    fun enableDnd() {
        val success = immersiveModeManager.enableGamingDnd()
        if (success) {
            _isDndEnabled.value = true
        }
    }

    fun disableDnd() {
        immersiveModeManager.disableGamingDnd()
        _isDndEnabled.value = false
    }

    // Brightness Methods
    fun requestBrightnessPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Permission denied or not found
        }
    }

    fun enableBrightness() {
        val success = immersiveModeManager.lockBrightness(255)
        if (success) {
            _isBrightnessLocked.value = true
        }
    }

    fun disableBrightness() {
        immersiveModeManager.restoreBrightness()
        _isBrightnessLocked.value = false
    }

    // Legacy toggle methods
    fun toggleDnd(enabled: Boolean) {
        if (enabled) {
            if (immersiveModeManager.hasDndPermission()) {
                enableDnd()
            } else {
                requestDndPermission()
            }
        } else {
            disableDnd()
        }
    }

    fun toggleBrightnessLock(enabled: Boolean) {
        if (enabled) {
            if (immersiveModeManager.hasWriteSettingsPermission()) {
                enableBrightness()
            } else {
                requestBrightnessPermission()
            }
        } else {
            disableBrightness()
        }
    }

    fun triggerFstrim() {
        viewModelScope.launch {
            performanceManager.optimizeStorageFstrim()
        }
    }
}
