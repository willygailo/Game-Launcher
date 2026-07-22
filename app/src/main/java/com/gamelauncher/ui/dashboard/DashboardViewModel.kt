// app/src/main/java/com/gamelauncher/ui/dashboard/DashboardViewModel.kt
package com.gamelauncher.ui.dashboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.core.BatterySaverManager
import com.gamelauncher.core.BypassChargingManager
import com.gamelauncher.core.DeviceManager
import com.gamelauncher.core.DndManager
import com.gamelauncher.core.FPSManager
import com.gamelauncher.core.GameOptimizationCoordinator
import com.gamelauncher.core.NetworkManager
import com.gamelauncher.core.PerformanceManager
import com.gamelauncher.core.ShizukuShellManager
import com.gamelauncher.core.permissions.RuntimePermissionManager
import com.gamelauncher.core.shizuku.ShizukuAvailability
import com.gamelauncher.core.shizuku.ShizukuState
import com.gamelauncher.data.local.GameDao
import com.gamelauncher.data.model.DeviceSpecs
import com.gamelauncher.data.model.GameModel
import com.gamelauncher.data.preference.SettingsPreferences
import com.gamelauncher.ui.theme.PerformanceMode
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
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceManager: DeviceManager,
    private val performanceManager: PerformanceManager,
    private val networkManager: NetworkManager,
    private val dndManager: DndManager,
    private val fpsManager: FPSManager,
    private val shizukuShellManager: ShizukuShellManager,
    private val shizukuAvailability: ShizukuAvailability,
    private val optimizationCoordinator: GameOptimizationCoordinator,
    private val settingsPreferences: SettingsPreferences,
    private val gameDao: GameDao,
    private val batterySaverManager: BatterySaverManager,
    private val bypassChargingManager: BypassChargingManager,
    private val runtimePermissionManager: RuntimePermissionManager
) : ViewModel() {

    private val _isBoosting = MutableStateFlow(false)
    val isBoosting: StateFlow<Boolean> = _isBoosting.asStateFlow()

    private val _boostResult = MutableStateFlow<GameOptimizationCoordinator.OptimizationResult?>(null)
    val boostResult: StateFlow<GameOptimizationCoordinator.OptimizationResult?> = _boostResult.asStateFlow()

    private val _isUsageAccessGranted = MutableStateFlow(false)
    val isUsageAccessGranted: StateFlow<Boolean> = _isUsageAccessGranted.asStateFlow()

    private val _isWriteSecureSettingsGranted = MutableStateFlow(false)
    val isWriteSecureSettingsGranted: StateFlow<Boolean> = _isWriteSecureSettingsGranted.asStateFlow()

    private val _isOverlayGranted = MutableStateFlow(false)
    val isOverlayGranted: StateFlow<Boolean> = _isOverlayGranted.asStateFlow()

    private val _isDndGranted = MutableStateFlow(false)
    val isDndGranted: StateFlow<Boolean> = _isDndGranted.asStateFlow()

    private val _isShizukuAvailable = MutableStateFlow(false)
    val isShizukuAvailable: StateFlow<Boolean> = _isShizukuAvailable.asStateFlow()

    val shizukuState: StateFlow<ShizukuState> = shizukuAvailability.state

    val games: StateFlow<List<GameModel>> = gameDao.getAllGames()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val cpuUsage: StateFlow<Float> = MutableStateFlow(0f)
    val cpuUsagePercent: StateFlow<Float> = cpuUsage
    val ramUsedMb: StateFlow<Long> = MutableStateFlow(0L)
    val ramTotalMb: StateFlow<Long> = MutableStateFlow(1L)
    val ramFreeMb: StateFlow<Long> = MutableStateFlow(1L)
    val currentFps: StateFlow<Float> = fpsManager.fps
    val currentHz: StateFlow<Float> = fpsManager.currentHz

    val batteryLevel: StateFlow<Int> = batterySaverManager.batteryLevel
    val isCharging: StateFlow<Boolean> = batterySaverManager.isCharging
    val isBatterySaverActive: StateFlow<Boolean> = batterySaverManager.isBatterySaverActive
    val batteryTemperature: StateFlow<Float> = MutableStateFlow(32.0f)

    val isBypassChargingActive: StateFlow<Boolean> = bypassChargingManager.isEnabled

    val deviceSpecs: StateFlow<DeviceSpecs> = MutableStateFlow(
        DeviceSpecs(
            socName = Build.HARDWARE,
            cpuCoreCount = Runtime.getRuntime().availableProcessors()
        )
    ).asStateFlow()

    val isDndEnabled: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
    val isBrightnessLocked: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
    val isGpuRenderingEnabled: StateFlow<Boolean> = settingsPreferences.forceGpuRenderingEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val performanceMode: StateFlow<PerformanceMode> = MutableStateFlow(PerformanceMode.BALANCED).asStateFlow()
    val freedRamMb: StateFlow<Long?> = MutableStateFlow(null).asStateFlow()

    private val shizukuStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            refreshPermissionStates()
        }
    }

    init {
        refreshPermissionStates()
        registerShizukuReceiver()
    }

    private fun registerShizukuReceiver() {
        try {
            val filter = IntentFilter().apply {
                addAction("rikka.shizuku.intent.action.UPDATE_STATE")
            }
            context.registerReceiver(shizukuStateReceiver, filter)
        } catch (_: Exception) {}
    }

    fun refreshPermissionStates() {
        _isUsageAccessGranted.value = runtimePermissionManager.hasUsageStatsPermission()
        _isWriteSecureSettingsGranted.value = context.checkSelfPermission(
            android.Manifest.permission.WRITE_SECURE_SETTINGS
        ) == PackageManager.PERMISSION_GRANTED
        _isOverlayGranted.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
        _isDndGranted.value = dndManager.isDndPermissionGranted()
        _isShizukuAvailable.value = shizukuShellManager.isAvailable()
        batterySaverManager.refreshBatteryStatus()
    }

    fun startOptimization(packageName: String) {
        viewModelScope.launch {
            _isBoosting.value = true
            val result = optimizationCoordinator.startOptimization(packageName)
            _boostResult.value = result
            _isBoosting.value = false
        }
    }

    fun stopOptimization() {
        viewModelScope.launch {
            _isBoosting.value = true
            val result = optimizationCoordinator.stopOptimization()
            _boostResult.value = result
            _isBoosting.value = false
        }
    }

    fun toggleBypassCharging(enable: Boolean) {
        viewModelScope.launch {
            bypassChargingManager.setBypassCharging(enable)
        }
    }

    fun optimizeRam() {
        viewModelScope.launch {
            performanceManager.clearMemory()
        }
    }

    fun setPerformanceMode(mode: PerformanceMode) {
        viewModelScope.launch {
            settingsPreferences.setPerformanceMode(mode.name)
        }
    }

    fun toggleDnd(enable: Boolean) {
        viewModelScope.launch {
            if (enable) dndManager.enableGamingDnd() else dndManager.disableGamingDnd()
        }
    }

    fun toggleBrightnessLock(enable: Boolean) {}

    fun toggleGpuRendering(enable: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setForceGpuRenderingEnabled(enable)
        }
    }

    fun requestShizukuPermission() {
        shizukuShellManager.requestPermission()
    }

    fun autoGrantAllPermissionsWithShizuku(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val p1 = runtimePermissionManager.grantPermissionViaShizuku("android.permission.WRITE_SECURE_SETTINGS")
            val p2 = runtimePermissionManager.setAppOpViaShizuku("GET_USAGE_STATS")
            val p3 = runtimePermissionManager.setAppOpViaShizuku("SYSTEM_ALERT_WINDOW")
            val success = p1 || p2 || p3
            refreshPermissionStates()
            onResult(success, if (success) "Permissions updated via Shizuku" else "Grant failed")
        }
    }

    override fun onCleared() {
        super.onCleared()
        networkManager.stopMonitoring()
        try { context.unregisterReceiver(shizukuStateReceiver) } catch (_: Exception) {}
    }
}
