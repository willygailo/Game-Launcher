package com.gamelauncher.ui.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.core.BenchmarkManager
import com.gamelauncher.core.BenchmarkResult
import com.gamelauncher.core.DeviceManager
import com.gamelauncher.core.FpsMonitor
import com.gamelauncher.core.ImmersiveModeManager
import com.gamelauncher.core.NetworkManager
import com.gamelauncher.core.PerformanceManager
import com.gamelauncher.core.RootShellManager
import com.gamelauncher.data.local.GameDao
import com.gamelauncher.data.model.DeviceSpecs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
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
    private val fpsMonitor: FpsMonitor,
    private val gameDao: GameDao,
    private val benchmarkManager: BenchmarkManager
) : ViewModel() {

    private val _deviceSpecs = MutableStateFlow(DeviceSpecs())
    val deviceSpecs: StateFlow<DeviceSpecs> = _deviceSpecs.asStateFlow()

    private val _isDndEnabled = MutableStateFlow(false)
    val isDndEnabled: StateFlow<Boolean> = _isDndEnabled.asStateFlow()

    private val _isBrightnessLocked = MutableStateFlow(false)
    val isBrightnessLocked: StateFlow<Boolean> = _isBrightnessLocked.asStateFlow()

    private val _brightnessLevel = MutableStateFlow(1f)
    val brightnessLevel: StateFlow<Float> = _brightnessLevel.asStateFlow()

    private val _isRootAvailable = MutableStateFlow(false)
    val isRootAvailable: StateFlow<Boolean> = _isRootAvailable.asStateFlow()

    private val _totalSessions = MutableStateFlow(0)
    val totalSessions: StateFlow<Int> = _totalSessions.asStateFlow()

    private val _totalPlayTimeMinutes = MutableStateFlow(0L)
    val totalPlayTimeMinutes: StateFlow<Long> = _totalPlayTimeMinutes.asStateFlow()

    private val _coreOnlineStatus = MutableStateFlow<List<Boolean>>(emptyList())
    val coreOnlineStatus: StateFlow<List<Boolean>> = _coreOnlineStatus.asStateFlow()

    private val _benchmarkResult = MutableStateFlow<BenchmarkResult?>(null)
    val benchmarkResult: StateFlow<BenchmarkResult?> = _benchmarkResult.asStateFlow()

    private val _isBenchmarking = MutableStateFlow(false)
    val isBenchmarking: StateFlow<Boolean> = _isBenchmarking.asStateFlow()

    init {
        startMonitoring()
        startFpsMonitoring()
        checkRootStatus()
        refreshPermissionStates()
        loadSessionStats()
    }

    private fun loadSessionStats() {
        viewModelScope.launch {
            val allSessions = gameDao.getAllSessions().collect { sessions ->
                _totalSessions.value = sessions.size
                _totalPlayTimeMinutes.value = sessions.sumOf { it.durationMs } / 60_000
            }
        }
    }

    private fun startFpsMonitoring() {
        fpsMonitor.startTracking()
        viewModelScope.launch {
            fpsMonitor.fps.collect { fps ->
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
            while (isActive) {
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
                    adpfPreferredRate = performanceManager.getAdpfPreferredRate(),
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
        val level = (_brightnessLevel.value * 255f).toInt().coerceIn(0, 255)
        val success = immersiveModeManager.lockBrightness(level)
        if (success) {
            _isBrightnessLocked.value = true
        }
    }

    fun disableBrightness() {
        immersiveModeManager.restoreBrightness()
        _isBrightnessLocked.value = false
    }

    fun setBrightnessLevel(level: Float) {
        _brightnessLevel.value = level
        if (_isBrightnessLocked.value) {
            val intLevel = (level * 255f).toInt().coerceIn(0, 255)
            immersiveModeManager.lockBrightness(intLevel)
        }
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

    fun runBenchmark() {
        viewModelScope.launch {
            _isBenchmarking.value = true
            _benchmarkResult.value = benchmarkManager.runBenchmark()
            _isBenchmarking.value = false
        }
    }

    fun refreshCoreStatus() {
        viewModelScope.launch {
            _coreOnlineStatus.value = deviceManager.getPerCoreOnlineStatus()
        }
    }

    fun toggleCore(coreIndex: Int, online: Boolean) {
        viewModelScope.launch {
            deviceManager.setCoreOnline(coreIndex, online)
            _coreOnlineStatus.value = deviceManager.getPerCoreOnlineStatus()
        }
    }
}
