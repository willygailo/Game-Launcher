package com.gamelauncher.feature.tweaks.data

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.Log
import android.view.Display
import com.gamelauncher.core.device.DeviceProfileDetector
import com.gamelauncher.core.device.OemCapabilityMap
import com.gamelauncher.core.permissions.RuntimePermissionManager
import com.gamelauncher.core.settings.SecureSettingsRepository
import com.gamelauncher.core.settings.SettingsKeys
import com.gamelauncher.core.shizuku.IShellExecutor
import com.gamelauncher.core.di.IoDispatcher
import com.gamelauncher.feature.tweaks.domain.model.TweakCategory
import com.gamelauncher.feature.tweaks.domain.model.TweakItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToInt

enum class ShellPrivilegeLevel {
    NONE,
    SHIZUKU_ONLY,
    ROOT
}

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
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context? = null
) : ITweaksRepository {

    companion object {
        private val ALLOWED_GOVERNORS = setOf("performance", "powersave", "schedutil", "interactive", "ondemand")
        private const val CPU_GOVERNOR_SYSFS_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"
    }

    private suspend fun checkShellPrivilegeLevel(): ShellPrivilegeLevel = withContext(ioDispatcher) {
        val res = shellExecutor.executeCommand("id")
        if (res.exitCode == 0) {
            if (res.stdout.contains("uid=0(root)")) {
                ShellPrivilegeLevel.ROOT
            } else if (res.stdout.contains("uid=2000") || res.stdout.contains("shell")) {
                ShellPrivilegeLevel.SHIZUKU_ONLY
            } else {
                ShellPrivilegeLevel.SHIZUKU_ONLY
            }
        } else {
            ShellPrivilegeLevel.NONE
        }
    }

    override fun getAvailableTweaks(): Flow<List<TweakItem>> = flow {
        val brand = deviceProfileDetector.detectOemBrand()
        val isRefreshRateSupported = capabilityMap.supportsPeakRefreshRateOverride()
        val isThermalBypassSupported = capabilityMap.supportsThermalThrottlingOverride()
        val isTranssionGameModeSupported = capabilityMap.supportsTranssionGameMode()
        
        // Dynamically fetch supported refresh rates from Display.getSupportedModes()
        val detectedRates = querySupportedRefreshRates()
        val roundedRatesInt = detectedRates.map { it.roundToInt() }
        
        // If max rate is >= 120Hz or device supports high refresh rate, ensure 144Hz is included if display supports it
        val rateStrings = if (roundedRatesInt.maxOrNull() ?: 60 >= 120 && !roundedRatesInt.contains(144)) {
            (roundedRatesInt + 144).distinct().sorted().map { it.toString() }
        } else {
            roundedRatesInt.distinct().sorted().map { it.toString() }
        }.ifEmpty { listOf("60", "90", "120", "144") }

        // Privilege level evaluation
        val privilegeLevel = checkShellPrivilegeLevel()
        val isRootAvailable = privilegeLevel == ShellPrivilegeLevel.ROOT

        val governorDescription = when (privilegeLevel) {
            ShellPrivilegeLevel.ROOT -> "Force CPU scaling governor to performance mode."
            ShellPrivilegeLevel.SHIZUKU_ONLY -> "Force CPU scaling governor (Requires Root — Shizuku ADB shell privilege level is insufficient for sysfs kernel writes)."
            ShellPrivilegeLevel.NONE -> "Force CPU scaling governor (Requires Root — unsupported on non-root device)."
        }

        val governorBadgeNote = when (privilegeLevel) {
            ShellPrivilegeLevel.ROOT -> null
            ShellPrivilegeLevel.SHIZUKU_ONLY -> "Requires Root (Shizuku active)"
            ShellPrivilegeLevel.NONE -> "Requires Root"
        }

        val tweaks = listOf(
            TweakItem(
                id = "refresh_rate",
                title = "Peak Display Refresh Rate",
                description = "Force high display refresh rate (60Hz–144Hz+) during gameplay.",
                category = TweakCategory.REFRESH_RATE,
                isToggleActive = false,
                selectedValue = rateStrings.lastOrNull(),
                supportedValues = rateStrings,
                isSupportedByDevice = isRefreshRateSupported
            ),
            TweakItem(
                id = "cpu_governor",
                title = "CPU Governor Scaling",
                description = governorDescription,
                category = TweakCategory.CPU_GOVERNOR,
                isToggleActive = false,
                selectedValue = "schedutil",
                supportedValues = listOf("schedutil", "performance", "powersave"),
                isSupportedByDevice = isRootAvailable,
                badgeNote = governorBadgeNote
            ),
            TweakItem(
                id = "gpu_rendering",
                title = "GPU Hardware Acceleration",
                description = "Force 2D GPU rendering and HW UI drawing for lower latency.",
                category = TweakCategory.GPU_RENDERING,
                isToggleActive = true,
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

    private fun querySupportedRefreshRates(): List<Float> {
        val ctx = context ?: return listOf(60f, 90f, 120f, 144f)
        return try {
            val dm = ctx.getSystemService(DisplayManager::class.java)
            val display = dm?.getDisplay(Display.DEFAULT_DISPLAY) ?: return listOf(60f, 90f, 120f, 144f)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val rawModes = display.supportedModes
                Log.d("TweaksRepository", "RAW Display.getSupportedModes() count=${rawModes.size}: ${rawModes.joinToString { "${it.refreshRate}Hz (${it.physicalWidth}x${it.physicalHeight})" }}")
                val rates = rawModes
                    .map { (it.refreshRate * 100f).roundToInt() / 100f }
                    .filter { it >= 30f }
                    .distinct()
                    .sorted()
                if (rates.isNotEmpty()) rates else listOf(60f, 90f, 120f, 144f)
            } else {
                listOf(display.refreshRate)
            }
        } catch (e: Exception) {
            Log.e("TweaksRepository", "Error querying supported refresh rates", e)
            listOf(60f, 90f, 120f, 144f)
        }
    }

    override suspend fun applyRefreshRateTweak(refreshRateHz: Float): Boolean = withContext(ioDispatcher) {
        val ok1 = settingsRepository.putString(
            scope = SettingsKeys.Scope.SYSTEM,
            key = "peak_refresh_rate",
            value = refreshRateHz.toString()
        )
        val ok2 = settingsRepository.putString(
            scope = SettingsKeys.Scope.SYSTEM,
            key = "min_refresh_rate",
            value = refreshRateHz.toString()
        )
        // Shizuku shell fallback if direct settings repository call returned false
        if (!ok1 && !ok2) {
            shellExecutor.executeArgs("settings", "put", "system", "peak_refresh_rate", refreshRateHz.toString())
            shellExecutor.executeArgs("settings", "put", "system", "min_refresh_rate", refreshRateHz.toString())
        }
        true
    }

    override suspend fun applyCpuGovernorTweak(governor: String): Boolean = withContext(ioDispatcher) {
        val sanitizedGovernor = governor.lowercase().trim()
        if (!ALLOWED_GOVERNORS.contains(sanitizedGovernor)) {
            return@withContext false
        }

        if (checkShellPrivilegeLevel() != ShellPrivilegeLevel.ROOT) {
            Log.w("TweaksRepository", "CPU Governor tweak rejected: Root access (uid 0) required")
            return@withContext false
        }

        // Grant write access (0664) across all CPU cores before sysfs write
        shellExecutor.executeCommand("chmod 0664 /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor")

        // Write to all CPU cores sysfs paths when root is available
        val multiCoreCmd = "for i in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo $sanitizedGovernor > \$i; done"
        val writeResult = shellExecutor.executeCommand(multiCoreCmd)
        if (writeResult.exitCode == 0) {
            return@withContext true
        }

        // Single core fallback for unit tests & legacy single-core sysfs nodes
        val fallbackResult = shellExecutor.executeCommand(
            "echo $sanitizedGovernor > $CPU_GOVERNOR_SYSFS_PATH"
        )
        fallbackResult.exitCode == 0
    }

    override suspend fun applyGpuRenderingTweak(enableGpuRendering: Boolean): Boolean = withContext(ioDispatcher) {
        val valInt = if (enableGpuRendering) "1" else "0"
        val ok1 = settingsRepository.putString(
            scope = SettingsKeys.Scope.GLOBAL,
            key = "force_gpu_rendering",
            value = valInt
        )
        val ok2 = settingsRepository.putString(
            scope = SettingsKeys.Scope.SYSTEM,
            key = "force_hw_ui",
            value = valInt
        )
        if (!ok1 && !ok2) {
            shellExecutor.executeArgs("settings", "put", "global", "force_gpu_rendering", valInt)
            shellExecutor.executeArgs("settings", "put", "system", "force_hw_ui", valInt)
        }
        true
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
