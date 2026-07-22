package com.gamelauncher.ui.dashboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import com.gamelauncher.core.GameLauncherApp
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.core.BenchmarkManager
import com.gamelauncher.core.BenchmarkResult
import com.gamelauncher.core.BypassChargingManager
import com.gamelauncher.core.DeviceManager
import com.gamelauncher.core.DndManager
import com.gamelauncher.core.FpsMonitor
import com.gamelauncher.core.ImmersiveModeManager
import com.gamelauncher.core.NetworkManager
import com.gamelauncher.core.PerformanceManager
import com.gamelauncher.core.RootShellManager
import com.gamelauncher.core.ShizukuShellManager
import com.gamelauncher.core.ThermalWatcher
import com.gamelauncher.data.local.GameDao
import com.gamelauncher.data.model.DeviceSpecs
import com.gamelauncher.data.preference.SettingsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.gamelauncher.ui.theme.PerformanceMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceManager: DeviceManager,
    private val performanceManager: PerformanceManager,
    private val networkManager: NetworkManager,
    private val immersiveModeManager: ImmersiveModeManager,
    private val dndManager: DndManager,
    private val rootShellManager: RootShellManager,
    private val fpsMonitor: FpsMonitor,
    private val gameDao: GameDao,
    private val benchmarkManager: BenchmarkManager,
    private val bypassChargingManager: BypassChargingManager,
    private val shizukuShellManager: ShizukuShellManager,
    private val thermalWatcher: ThermalWatcher,
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    val performanceMode: StateFlow<PerformanceMode> = settingsPreferences.performanceMode
        .map { modeStr ->
            try {
                PerformanceMode.valueOf(modeStr)
            } catch (e: Exception) {
                PerformanceMode.BALANCED
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PerformanceMode.BALANCED
        )

    fun setPerformanceMode(mode: PerformanceMode) {
        viewModelScope.launch {
            settingsPreferences.setPerformanceMode(mode.name)
            when (mode) {
                PerformanceMode.PRO -> {
                    performanceManager.setHighPerformanceMode()
                }
                PerformanceMode.BALANCED -> {
                    // Standard performance mode
                }
                PerformanceMode.ECO -> {
                    // Eco power saving mode
                }
            }
        }
    }

    private val _deviceSpecs = MutableStateFlow(DeviceSpecs())
    val deviceSpecs: StateFlow<DeviceSpecs> = _deviceSpecs.asStateFlow()

    private val _isDndEnabled = MutableStateFlow(false)
    val isDndEnabled: StateFlow<Boolean> = _isDndEnabled.asStateFlow()

    private var pendingDndEnable: Boolean = false

    private val _isGpuRenderingEnabled = MutableStateFlow(false)
    val isGpuRenderingEnabled: StateFlow<Boolean> = _isGpuRenderingEnabled.asStateFlow()

    val thermalStatus: StateFlow<Int> = thermalWatcher.thermalStatus

    private val _isMonitoringPaused = MutableStateFlow(false)

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

    private val _hasWriteSecure = MutableStateFlow(false)
    val hasWriteSecureSettings: StateFlow<Boolean> = _hasWriteSecure.asStateFlow()

    private val shizukuStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == GameLauncherApp.ACTION_SHIZUKU_CHANGED) {
                refreshBypassChargingState()
            }
        }
    }

    init {
        networkManager.startMonitoring()
        startMonitoring()
        startFpsMonitoring()
        checkRootStatus()
        refreshPermissionStates()
        loadSessionStats()
        checkWriteSecure()
        refreshBypassChargingState()
        observeGpuPref()

        val filter = IntentFilter(GameLauncherApp.ACTION_SHIZUKU_CHANGED)
        androidx.core.content.ContextCompat.registerReceiver(
            context,
            shizukuStateReceiver,
            filter,
            androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun observeGpuPref() {
        viewModelScope.launch {
            settingsPreferences.forceGpuRenderingEnabled.collect { enabled ->
                _isGpuRenderingEnabled.value = enabled
            }
        }
    }

    private fun checkWriteSecure() {
        viewModelScope.launch {
            _hasWriteSecure.value = try {
                val cr = context.contentResolver
                val cur = android.provider.Settings.Global.getInt(cr, "adb_enabled", 0)
                android.provider.Settings.Global.putInt(cr, "adb_enabled", cur)
                true
            } catch (e: SecurityException) { false }
        }
    }

    private fun loadSessionStats() {
        viewModelScope.launch {
            gameDao.getAllSessions().collect { sessions ->
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
        val granted = dndManager.isDndPermissionGranted()
        if (granted && pendingDndEnable) {
            pendingDndEnable = false
            enableDnd()
        } else {
            _isDndEnabled.value = granted && dndManager.isGamingDndActive()
        }
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
                if (_isMonitoringPaused.value) {
                    delay(500)
                    continue
                }
                val (ramTotal, ramUsed, ramFree) = deviceManager.getRamInfo()
                val socInfo = deviceManager.getSocInfo()
                val networkSnapshot = networkManager.getNetworkSnapshot()
                
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
                    networkType = networkSnapshot.summary,
                    networkStrengthDbm = if (networkSnapshot.isWifiConnected) {
                        networkSnapshot.wifiSignalDbm
                    } else {
                        networkSnapshot.cellularSignalDbm
                    },
                    wifiLinkSpeedMbps = networkSnapshot.wifiLinkSpeedMbps,
                    networkQualityScore = networkSnapshot.qualityScore,
                    hasValidatedInternet = networkSnapshot.hasValidatedInternet,
                    isNetworkMetered = networkSnapshot.isMetered,
                    wifiLabel = networkSnapshot.wifiLabel,
                    wifiSignalBars = networkSnapshot.wifiSignalBars,
                    wifiBandLabel = networkSnapshot.wifiBandLabel,
                    cellularLabel = networkSnapshot.cellularLabel,
                    cellularSignalDbm = networkSnapshot.cellularSignalDbm,
                    cellularSignalBars = networkSnapshot.cellularSignalBars,
                    is5G = networkSnapshot.is5G,
                    is5GPlus = networkSnapshot.is5GPlus,
                    networkDownstreamKbps = networkSnapshot.downstreamKbps,
                    networkUpstreamKbps = networkSnapshot.upstreamKbps,
                    displayRefreshRateHz = performanceManager.getCurrentRefreshRate(),
                    supportedRefreshRates = performanceManager.getSupportedRefreshRates(),
                    timestamp = System.currentTimeMillis()
                )
                delay(1000)
            }
        }
    }

    fun pauseMonitoring() { _isMonitoringPaused.value = true }
    fun resumeMonitoring() { _isMonitoringPaused.value = false }

    fun optimizeRam() {
        viewModelScope.launch {
            val freedGC = deviceManager.optimizeMemory()
            val freedApps = deviceManager.killBackgroundApps()
            val totalFreed = maxOf(freedGC, freedApps)
            _deviceSpecs.value = _deviceSpecs.value.copy(freedRamMb = totalFreed)
        }
    }

    // DND Methods
    fun requestDndPermission() {
        pendingDndEnable = true
        dndManager.openDndPermissionSettings()
    }

    fun enableDnd() {
        viewModelScope.launch {
            val success = dndManager.enableGamingDnd()
            if (success) {
                _isDndEnabled.value = true
            }
        }
    }

    fun disableDnd() {
        viewModelScope.launch {
            dndManager.disableGamingDnd()
            _isDndEnabled.value = false
        }
    }

    // Brightness Methods
    fun requestBrightnessPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {}
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

    // Toggle methods
    fun toggleDnd(enabled: Boolean) {
        if (enabled) {
            if (dndManager.isDndPermissionGranted()) {
                enableDnd()
            } else {
                requestDndPermission()
            }
        } else {
            pendingDndEnable = false
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

    fun toggleGpuRendering(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setForceGpuRenderingEnabled(enabled)
            if (enabled) {
                performanceManager.forceGpuRendering()
            } else {
                performanceManager.restoreGpuRendering()
            }
            _isGpuRenderingEnabled.value = enabled
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

    val isBypassShellAvailable: StateFlow<Boolean> = bypassChargingManager.isShellAvailable
    val isBypassChargingEnabled: StateFlow<Boolean> = bypassChargingManager.isEnabled

    private fun refreshBypassChargingState() {
        viewModelScope.launch {
            bypassChargingManager.refreshAvailability()
        }
    }

    fun toggleBypassCharging(enable: Boolean) {
        viewModelScope.launch {
            bypassChargingManager.setBypassCharging(enable)
        }
    }

    fun requestShizukuPermission() {
        shizukuShellManager.requestPermission()
    }

    fun autoGrantAllPermissionsWithShizuku(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val packageName = context.packageName
            val (success, message) = shizukuShellManager.grantAllPermissions(packageName)
            refreshPermissionStates()
            onResult(success, message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        networkManager.stopMonitoring()
        try { context.unregisterReceiver(shizukuStateReceiver) } catch (_: Exception) {}
    }
}
