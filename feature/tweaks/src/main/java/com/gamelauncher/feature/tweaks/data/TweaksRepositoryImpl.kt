package com.gamelauncher.feature.tweaks.data

import com.gamelauncher.core.device.DeviceProfileDetector
import com.gamelauncher.core.device.OemCapabilityMap
import com.gamelauncher.core.permissions.RuntimePermissionManager
import com.gamelauncher.core.settings.SecureSettingsRepository
import com.gamelauncher.core.settings.SettingsKeys
import com.gamelauncher.core.shizuku.IShellExecutor
import com.gamelauncher.core.di.IoDispatcher
import com.gamelauncher.feature.tweaks.domain.model.TweakCategory
import com.gamelauncher.feature.tweaks.domain.model.TweakItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * TweaksRepositoryImpl — Repository implementation orchestrating system performance tweaks
 * across core foundation modules with injected CoroutineDispatcher threading.
 */
class TweaksRepositoryImpl @Inject constructor(
    private val settingsRepository: SecureSettingsRepository,
    private val deviceProfileDetector: DeviceProfileDetector,
    private val capabilityMap: OemCapabilityMap,
    private val permissionManager: RuntimePermissionManager,
    private val shellExecutor: IShellExecutor,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ITweaksRepository {

    companion object {
        private val ALLOWED_GOVERNORS = setOf("performance", "powersave", "schedutil", "interactive", "ondemand")
        private const val CPU_GOVERNOR_SYSFS_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"
    }

    override fun getAvailableTweaks(): Flow<List<TweakItem>> = flow {
        val brand = deviceProfileDetector.detectOemBrand()
        val isRefreshRateSupported = capabilityMap.supportsPeakRefreshRateOverride()
        val isThermalBypassSupported = capabilityMap.supportsThermalThrottlingOverride()
        val isTranssionGameModeSupported = capabilityMap.supportsTranssionGameMode()

        val tweaks = listOf(
            TweakItem(
                id = "refresh_rate",
                title = "Peak Display Refresh Rate",
                description = "Force 120Hz/90Hz display refresh rate during gameplay.",
                category = TweakCategory.REFRESH_RATE,
                isToggleActive = false,
                selectedValue = null,
                supportedValues = listOf("60", "90", "120"),
                isSupportedByDevice = isRefreshRateSupported
            ),
            TweakItem(
                id = "cpu_governor",
                title = "CPU Governor Scaling",
                description = "Force CPU scaling governor to performance mode.",
                category = TweakCategory.CPU_GOVERNOR,
                isToggleActive = false,
                selectedValue = "schedutil",
                supportedValues = listOf("schedutil", "performance", "powersave"),
                isSupportedByDevice = true
            ),
            TweakItem(
                id = "thermal_bypass",
                title = "OEM Thermal Throttling Bypass",
                description = "Bypass aggressive OEM thermal throttling parameters.",
                category = TweakCategory.THERMAL_THROTTLING,
                isToggleActive = false,
                isSupportedByDevice = isThermalBypassSupported
            ),
            TweakItem(
                id = "game_mode",
                title = "System Game Mode Booster",
                description = "Activate system game mode optimizations.",
                category = TweakCategory.GAME_MODE,
                isToggleActive = true,
                isSupportedByDevice = isTranssionGameModeSupported || true
            )
        )
        emit(tweaks)
    }.flowOn(ioDispatcher)

    override suspend fun applyRefreshRateTweak(refreshRateHz: Float): Boolean = withContext(ioDispatcher) {
        settingsRepository.putString(
            scope = SettingsKeys.Scope.SYSTEM,
            key = "peak_refresh_rate",
            value = refreshRateHz.toString()
        )
    }

    override suspend fun applyCpuGovernorTweak(governor: String): Boolean = withContext(ioDispatcher) {
        val sanitizedGovernor = governor.lowercase().trim()
        if (!ALLOWED_GOVERNORS.contains(sanitizedGovernor)) {
            return@withContext false
        }

        // Grant write access (0664) before sysfs write
        shellExecutor.executeArgs("chmod", "0664", CPU_GOVERNOR_SYSFS_PATH)

        // Execute via executeCommand (sh -c) so shell interpreter correctly processes output redirection (">")
        val writeResult = shellExecutor.executeCommand(
            "echo $sanitizedGovernor > $CPU_GOVERNOR_SYSFS_PATH"
        )
        writeResult.exitCode == 0
    }

    override suspend fun applyThermalThrottlingBypass(enableBypass: Boolean): Boolean = withContext(ioDispatcher) {
        val value = if (enableBypass) "0" else "1"
        settingsRepository.putString(
            scope = SettingsKeys.Scope.GLOBAL,
            key = "thermal_limit_enabled",
            value = value
        )
    }

    override suspend fun applyGameModeTweak(enableGameMode: Boolean): Boolean = withContext(ioDispatcher) {
        val value = if (enableGameMode) "1" else "0"
        settingsRepository.putString(
            scope = SettingsKeys.Scope.GLOBAL,
            key = "game_mode_type",
            value = value
        )
    }
}
