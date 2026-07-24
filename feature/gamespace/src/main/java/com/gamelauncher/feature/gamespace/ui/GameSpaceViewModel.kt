package com.gamelauncher.feature.gamespace.ui

import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.view.Display
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.core.device.DeviceProfileDetector
import com.gamelauncher.core.device.OemBrand
import com.gamelauncher.core.oemflags.OemFlag
import com.gamelauncher.core.oemflags.OemFlagProbeEngine
import com.gamelauncher.core.oemflags.ProbeStatus
import com.gamelauncher.core.settings.SecureSettingsRepository
import com.gamelauncher.core.settings.SettingsKeys
import com.gamelauncher.core.settings.SettingsPreferences
import com.gamelauncher.core.shizuku.IShellExecutor
import com.gamelauncher.core.shizuku.IShizukuManager
import com.gamelauncher.core.shizuku.ShizukuState
import com.gamelauncher.core.shizuku.ShizukuStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class GameSpaceUiState(
    val selectedTab: Int = 0, // 0: GAMES, 1: TWEAKS, 2: NETWORK, 3: TELEMETRY, 4: READINESS
    val detectedOemBrand: OemBrand = OemBrand.GENERIC,
    val isShizukuReady: Boolean = false,
    val isOverlayRunning: Boolean = false,
    val isLoading: Boolean = true,
    val flags: List<OemFlag> = emptyList(),
    val activeProfileMode: String = "BALANCED",
    val deviceName: String = "Android device",
    val androidVersion: String = "Android",
    val chipsetFamily: String = "Unknown chipset",
    val supportedRefreshRates: List<Float> = listOf(60f),
    val selectedRefreshRate: Float = 60f,
    val games: List<GameLibraryItem> = emptyList(),
    val activeGamePackageName: String? = null,
    val selectedDnsProvider: String = "default",
    val overlayPermissionGranted: Boolean = false,
    val dndPermissionGranted: Boolean = false,
    val usageStatsGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val showResetConfirmationDialog: Boolean = false,
    val statusMessage: String? = null,
    val romBuildInfo: String = ""
)

data class GameLibraryItem(
    val label: String,
    val packageName: String
)

/** One unified state owner for the ROG-inspired Game Space home. */
@HiltViewModel
class GameSpaceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val detector: DeviceProfileDetector,
    private val probeEngine: OemFlagProbeEngine,
    private val shizukuStateRepository: ShizukuStateRepository,
    private val shizukuManager: IShizukuManager,
    private val shellExecutor: IShellExecutor,
    private val settingsPreferences: SettingsPreferences,
    private val settingsRepository: SecureSettingsRepository,
    private val runtimePermissionManager: com.gamelauncher.core.permissions.RuntimePermissionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameSpaceUiState())
    val uiState: StateFlow<GameSpaceUiState> = _uiState.asStateFlow()

    init {
        observeShizukuState()
        observeProfile()
        refreshState()
    }

    private fun observeShizukuState() {
        viewModelScope.launch {
            shizukuStateRepository.state.collect { state ->
                val isConnected = state is ShizukuState.Connected
                if (isConnected) {
                    try {
                        runtimePermissionManager.grantPermissionViaShizuku("android.permission.WRITE_SECURE_SETTINGS")
                        runtimePermissionManager.grantPermissionViaShizuku("android.permission.PACKAGE_USAGE_STATS")
                        runtimePermissionManager.setAppOpViaShizuku("SYSTEM_ALERT_WINDOW")
                    } catch (_: Exception) {}
                }
                _uiState.value = _uiState.value.copy(
                    isShizukuReady = isConnected
                )
                checkPermissions()
                val probedFlags = probeEngine.probeDeviceFlags()
                val buildInfo = probeEngine.getRomBuildInfo()
                _uiState.value = _uiState.value.copy(
                    flags = probedFlags,
                    romBuildInfo = buildInfo
                )
            }
        }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            settingsPreferences.performanceMode.collect { stored ->
                _uiState.value = _uiState.value.copy(
                    activeProfileMode = when (stored) {
                        "ECO" -> "ECO"
                        "TURBO", "ROG_ULTRA", "PRO" -> "TURBO"
                        else -> "BALANCED"
                    }
                )
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex.coerceIn(0, 4))
    }

    fun refreshState() {
        viewModelScope.launch {
            val rates = supportedRefreshRates()
            val games = withContext(Dispatchers.IO) { installedGames() }
            val currentDnsMode = settingsRepository.getString(SettingsKeys.Scope.GLOBAL, "private_dns_mode") ?: "off"
            val currentDnsHost = settingsRepository.getString(SettingsKeys.Scope.GLOBAL, "private_dns_specifier") ?: ""

            val activeDnsId = when {
                currentDnsHost?.contains("cloudflare") == true || currentDnsHost == "one.one.one.one" -> "cloudflare"
                currentDnsHost?.contains("adguard") == true -> "adguard"
                currentDnsHost?.contains("dns.google") == true -> "google"
                else -> "default"
            }

            _uiState.value = _uiState.value.copy(
                detectedOemBrand = detector.detectOemBrand(),
                isShizukuReady = shizukuStateRepository.isConnected,
                deviceName = listOf(Build.MANUFACTURER, Build.MODEL)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                    .ifBlank { "Android device" },
                androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                chipsetFamily = detectChipsetFamily(),
                supportedRefreshRates = rates,
                selectedRefreshRate = rates.minByOrNull { kotlin.math.abs(it - currentRefreshRate()) } ?: rates.first(),
                games = games,
                selectedDnsProvider = activeDnsId,
                isLoading = true
            )

            checkPermissions()

            val probedFlags = probeEngine.probeDeviceFlags()
            _uiState.value = _uiState.value.copy(flags = probedFlags, isLoading = false)
        }
    }

    fun checkPermissions() {
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true
        val hasDnd = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(NotificationManager::class.java)?.isNotificationPolicyAccessGranted == true
        } else true

        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
        val usageMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps?.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps?.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        val hasUsage = usageMode == AppOpsManager.MODE_ALLOWED

        val hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true

        _uiState.value = _uiState.value.copy(
            overlayPermissionGranted = hasOverlay,
            dndPermissionGranted = hasDnd,
            usageStatsGranted = hasUsage,
            notificationPermissionGranted = hasNotifications
        )
    }

    fun requestShizukuPermission() = runCatching { shizukuManager.requestPermission() }

    fun toggleOverlayService() {
        val currentState = _uiState.value.isOverlayRunning
        if (!currentState) {
            if (!_uiState.value.overlayPermissionGranted) {
                _uiState.value = _uiState.value.copy(statusMessage = "Overlay permission required.")
                return
            }
            startOverlayServiceInternal()
        } else {
            stopOverlayServiceInternal()
        }
    }

    private fun startOverlayServiceInternal() {
        try {
            val intent = Intent().setClassName(context.packageName, "com.gamelauncher.services.OverlayService")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            _uiState.value = _uiState.value.copy(isOverlayRunning = true, statusMessage = "Overlay HUD started.")
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(statusMessage = "Failed to start overlay service: ${e.localizedMessage}")
        }
    }

    private fun stopOverlayServiceInternal() {
        try {
            val intent = Intent().setClassName(context.packageName, "com.gamelauncher.services.OverlayService")
                .setAction("STOP_OVERLAY")
            context.startService(intent)
            context.stopService(intent)
            _uiState.value = _uiState.value.copy(isOverlayRunning = false, statusMessage = "Overlay HUD stopped.")
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(statusMessage = "Stopped overlay service.")
        }
    }

    fun toggleFlag(flag: OemFlag, enable: Boolean) {
        viewModelScope.launch {
            val success = probeEngine.applyFlagState(flag, enable)
            _uiState.value = _uiState.value.copy(
                statusMessage = if (success) "Applied ${flag.title}" else "Failed to update ${flag.title}"
            )
        }
    }

    fun applyAllTweaks() {
        viewModelScope.launch {
            var appliedCount = 0
            _uiState.value.flags.forEach { flag ->
                if (flag.status is ProbeStatus.Supported) {
                    val ok = probeEngine.applyFlagState(flag, true)
                    if (ok) appliedCount++
                }
            }
            _uiState.value = _uiState.value.copy(
                flags = probeEngine.probeDeviceFlags(),
                statusMessage = "Applied $appliedCount performance & setprop debug tweaks."
            )
        }
    }

    fun promptResetAllTweaks() {
        _uiState.value = _uiState.value.copy(showResetConfirmationDialog = true)
    }

    fun dismissResetDialog() {
        _uiState.value = _uiState.value.copy(showResetConfirmationDialog = false)
    }

    fun confirmResetAllTweaks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showResetConfirmationDialog = false, isLoading = true)
            val ok = probeEngine.resetAllFlags()
            _uiState.value = _uiState.value.copy(
                flags = probeEngine.probeDeviceFlags(),
                isLoading = false,
                statusMessage = if (ok) "Reverted verified OEM settings to baseline" else "Reset completed with warnings"
            )
        }
    }

    fun selectProfileMode(mode: String) {
        viewModelScope.launch {
            val profile = when (mode) {
                "ECO" -> "ECO"
                "TURBO" -> "TURBO"
                else -> "BALANCED"
            }
            settingsPreferences.setPerformanceMode(profile)
            val rates = _uiState.value.supportedRefreshRates
            val rate = when (profile) {
                "ECO" -> rates.minOrNull() ?: 60f
                "TURBO" -> rates.maxOrNull() ?: 60f
                else -> rates.minByOrNull { kotlin.math.abs(it - 60f) } ?: rates.first()
            }
            requestSupportedRefreshRate(rate)
        }
    }

    fun selectRefreshRate(hz: Float) {
        if (hz !in _uiState.value.supportedRefreshRates) return
        viewModelScope.launch { requestSupportedRefreshRate(hz) }
    }

    fun selectDnsProvider(providerId: String) {
        viewModelScope.launch {
            val hostname = when (providerId) {
                "cloudflare" -> "one.one.one.one"
                "adguard" -> "dns.adguard-dns.com"
                "google" -> "dns.google"
                else -> null
            }
            val success = if (hostname != null) {
                settingsRepository.putString(SettingsKeys.Scope.GLOBAL, "private_dns_specifier", hostname) &&
                        settingsRepository.putString(SettingsKeys.Scope.GLOBAL, "private_dns_mode", "hostname")
            } else {
                settingsRepository.putString(SettingsKeys.Scope.GLOBAL, "private_dns_mode", "off")
            }

            _uiState.value = _uiState.value.copy(
                selectedDnsProvider = providerId,
                statusMessage = if (success) "Applied DNS profile ($providerId)" else "DNS requires Shizuku or WRITE_SECURE_SETTINGS"
            )
        }
    }

    fun launchGame(packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent == null) {
            _uiState.value = _uiState.value.copy(statusMessage = "This game no longer has a launch activity.")
            return
        }

        // Apply active performance profile before launch
        viewModelScope.launch {
            selectProfileMode(_uiState.value.activeProfileMode)
            if (!_uiState.value.isOverlayRunning && _uiState.value.overlayPermissionGranted) {
                startOverlayServiceInternal()
            }
        }

        runCatching { context.startActivity(intent) }
            .onSuccess {
                _uiState.value = _uiState.value.copy(
                    activeGamePackageName = packageName,
                    statusMessage = "Game launched with active profile & overlay HUD."
                )
            }
            .onFailure {
                _uiState.value = _uiState.value.copy(statusMessage = "Unable to launch this game.")
            }
    }

    private suspend fun requestSupportedRefreshRate(hz: Float) {
        val peak = shellExecutor.setPeakRefreshRate(hz)
        val min = shellExecutor.setMinRefreshRate(hz)
        _uiState.value = _uiState.value.copy(
            selectedRefreshRate = hz,
            statusMessage = if (peak || min) {
                "Requested ${hz.toInt()}Hz. Display panel & Android system remain in control."
            } else {
                "${hz.toInt()}Hz needs Shizuku or WRITE_SECURE_SETTINGS permission."
            }
        )
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    private fun supportedRefreshRates(): List<Float> = runCatching {
        val display = context.getSystemService(DisplayManager::class.java)
            ?.getDisplay(Display.DEFAULT_DISPLAY)
        display?.supportedModes.orEmpty()
            .map { (it.refreshRate * 100).toInt() / 100f }
            .filter { it >= 30f }
            .distinct()
            .sorted()
            .ifEmpty { listOf(60f) }
    }.getOrDefault(listOf(60f))

    private fun currentRefreshRate(): Float = runCatching {
        context.getSystemService(DisplayManager::class.java)
            ?.getDisplay(Display.DEFAULT_DISPLAY)?.refreshRate ?: 60f
    }.getOrDefault(60f)

    private fun installedGames(): List<GameLibraryItem> = runCatching {
        context.packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            .asSequence()
            .filter { it.category == ApplicationInfo.CATEGORY_GAME }
            .map { app ->
                GameLibraryItem(
                    label = context.packageManager.getApplicationLabel(app).toString(),
                    packageName = app.packageName
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }.getOrDefault(emptyList())

    private fun detectChipsetFamily(): String {
        val hardware = "${Build.HARDWARE} ${Build.BOARD} ${Build.SOC_MANUFACTURER}".lowercase()
        return when {
            hardware.contains("qcom") || hardware.contains("qualcomm") -> "Qualcomm Snapdragon"
            hardware.contains("mt") || hardware.contains("mediatek") -> "MediaTek Dimensity / Helio"
            hardware.contains("unisoc") || hardware.contains("ums") -> "UNISOC"
            hardware.contains("exynos") -> "Samsung Exynos"
            hardware.contains("tensor") -> "Google Tensor"
            hardware.contains("kirin") || hardware.contains("hisilicon") -> "HiSilicon Kirin"
            else -> Build.HARDWARE.ifBlank { "Unknown chipset" }
        }
    }
}
