// feature/tweaks/src/main/java/com/gamelauncher/feature/tweaks/data/TweaksRepositoryImpl.kt
package com.gamelauncher.feature.tweaks.data

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import com.gamelauncher.core.device.DeviceProfileDetector
import com.gamelauncher.core.device.OemCapabilityMap
import com.gamelauncher.core.di.IoDispatcher
import com.gamelauncher.core.shizuku.IShellExecutor
import com.gamelauncher.feature.tweaks.domain.model.TweakCategory
import com.gamelauncher.feature.tweaks.domain.model.TweakItem
import com.gamelauncher.feature.tweaks.domain.model.TweakResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * TweaksRepositoryImpl — Repository implementation orchestrating system performance tweaks
 * with read-back verification and OEM key routing.
 */
class TweaksRepositoryImpl @Inject constructor(
    private val deviceProfileDetector: DeviceProfileDetector,
    private val capabilityMap: OemCapabilityMap,
    private val shellExecutor: IShellExecutor,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context
) : ITweaksRepository {

    override fun getAvailableTweaks(): Flow<List<TweakItem>> = flow {
        val oemKeys = deviceProfileDetector.getTweakKeys()
        val detectedRates = querySupportedRefreshRates()
        val roundedRatesInt = detectedRates.map { it.roundToInt() }
        val rateStrings = roundedRatesInt.distinct().sorted().map { it.toString() }.ifEmpty { listOf("60", "90", "120", "144") }

        val tweaks = listOf(
            TweakItem(
                id = "refresh_rate",
                title = "Peak Display Refresh Rate",
                description = "Force high display refresh rate using target OEM key (${oemKeys.refreshRateKey}).",
                category = TweakCategory.REFRESH_RATE,
                isToggleActive = true,
                selectedValue = rateStrings.lastOrNull(),
                supportedValues = rateStrings,
                isSupportedByDevice = true
            ),
            TweakItem(
                id = "high_refresh_rate_blacklist",
                title = "Clear Refresh Rate Blacklist",
                description = "Remove app package restrictions blocking high FPS rendering.",
                category = TweakCategory.REFRESH_RATE,
                isToggleActive = false,
                isSupportedByDevice = true
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
                id = "game_driver_clear",
                title = "Clear Game Driver Restrictions",
                description = "Reset Game Driver opt-out and global override app configurations.",
                category = TweakCategory.GPU_RENDERING,
                isToggleActive = false,
                isSupportedByDevice = oemKeys.gameDriverSupported
            ),
            TweakItem(
                id = "thermal_bypass",
                title = "OEM Thermal Throttling Bypass",
                description = "Bypass framework thermal throttling via cmd thermalservice override-status.",
                category = TweakCategory.THERMAL_THROTTLING,
                isToggleActive = false,
                isSupportedByDevice = oemKeys.thermalOverrideSupported,
                badgeNote = "Requires on-device verification"
            ),
            TweakItem(
                id = "game_mode",
                title = "System Game Mode Booster",
                description = "Activate system game mode optimizations.",
                category = TweakCategory.GAME_MODE,
                isToggleActive = true,
                isSupportedByDevice = true
            ),
            TweakItem(
                id = "phantom_procs",
                title = "Disable Phantom Process Killing",
                description = "Prevent Android 12+ child process monitor from killing background games.",
                category = TweakCategory.MEMORY,
                isToggleActive = false,
                isSupportedByDevice = Build.VERSION.SDK_INT >= 31,
                badgeNote = "Requires on-device verification"
            ),
            TweakItem(
                id = "adaptive_battery",
                title = "Disable Adaptive Battery",
                description = "Disable OS power throttling on active background game threads.",
                category = TweakCategory.POWER,
                isToggleActive = false,
                isSupportedByDevice = true
            )
        )
        emit(tweaks)
    }.flowOn(ioDispatcher)

    private fun querySupportedRefreshRates(): List<Float> {
        return try {
            val dm = context.getSystemService(DisplayManager::class.java)
            val display = dm?.getDisplay(Display.DEFAULT_DISPLAY) ?: return listOf(60f, 90f, 120f, 144f)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val rawModes = display.supportedModes
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
            listOf(60f, 90f, 120f, 144f)
        }
    }

    override suspend fun applyRefreshRateTweak(refreshRateHz: Float): TweakResult = withContext(ioDispatcher) {
        val oemKeys = deviceProfileDetector.getTweakKeys()
        val key = oemKeys.refreshRateKey
        val targetValue = refreshRateHz.toString()

        val written = shellExecutor.writeSetting("system", key, targetValue)
        if (!written) {
            return@withContext TweakResult.Failed("Shizuku service write call failed for key '$key'")
        }

        val readValue = shellExecutor.readSetting("system", key)
        if (readValue == null) {
            return@withContext TweakResult.Failed("Shizuku read-back operation failed for key '$key'")
        }

        if (readValue.trim() == targetValue || readValue.toFloatOrNull() == refreshRateHz) {
            TweakResult.Confirmed
        } else {
            TweakResult.SilentlyIgnored(key)
        }
    }

    override suspend fun clearHighRefreshRateBlacklist(): TweakResult = withContext(ioDispatcher) {
        val key = "high_refresh_rate_blacklist"
        val written = shellExecutor.writeSetting("system", key, "")
        if (!written) {
            return@withContext TweakResult.Failed("Shizuku service write call failed for key '$key'")
        }

        val readValue = shellExecutor.readSetting("system", key)
        if (readValue == null) {
            return@withContext TweakResult.Failed("Shizuku read-back operation failed for key '$key'")
        }

        if (readValue.trim().isEmpty() || readValue.trim() == "null") {
            TweakResult.Confirmed
        } else {
            TweakResult.SilentlyIgnored(key)
        }
    }

    override suspend fun applyGpuRenderingTweak(enableGpuRendering: Boolean): TweakResult = withContext(ioDispatcher) {
        val key = "force_gpu_rendering"
        val targetValue = if (enableGpuRendering) "1" else "0"

        val written = shellExecutor.writeSetting("global", key, targetValue)
        if (!written) {
            return@withContext TweakResult.Failed("Shizuku service write call failed for key '$key'")
        }

        val readValue = shellExecutor.readSetting("global", key)
        if (readValue == null) {
            return@withContext TweakResult.Failed("Shizuku read-back operation failed for key '$key'")
        }

        if (readValue.trim() == targetValue) {
            TweakResult.Confirmed
        } else {
            TweakResult.SilentlyIgnored(key)
        }
    }

    override suspend fun clearGameDriverConfig(): TweakResult = withContext(ioDispatcher) {
        val key1 = "game_driver_all_apps"
        val key2 = "game_driver_opt_out_apps"

        val written1 = shellExecutor.writeSetting("global", key1, "")
        val written2 = shellExecutor.writeSetting("global", key2, "")

        if (!written1 || !written2) {
            return@withContext TweakResult.Failed("Shizuku service write call failed for Game Driver settings")
        }

        val read1 = shellExecutor.readSetting("global", key1)
        val read2 = shellExecutor.readSetting("global", key2)

        val clean1 = read1 == null || read1.trim().isEmpty() || read1.trim() == "null"
        val clean2 = read2 == null || read2.trim().isEmpty() || read2.trim() == "null"

        if (clean1 && clean2) {
            TweakResult.Confirmed
        } else {
            TweakResult.SilentlyIgnored(if (!clean1) key1 else key2)
        }
    }

    override suspend fun applyThermalThrottlingBypass(enableBypass: Boolean): TweakResult = withContext(ioDispatcher) {
        val key = "thermal_limit_enabled"
        val targetValue = if (enableBypass) "0" else "1"

        val written = shellExecutor.setThermalOverride(enableBypass)
        if (!written) {
            return@withContext TweakResult.Failed("Shizuku service thermal override call failed")
        }

        val readValue = shellExecutor.readSetting("global", key)
        if (readValue == null) {
            return@withContext TweakResult.Failed("Shizuku read-back operation failed for key '$key'")
        }

        if (readValue.trim() == targetValue) {
            TweakResult.Confirmed
        } else {
            TweakResult.SilentlyIgnored(key)
        }
    }

    override suspend fun applyGameModeTweak(enableGameMode: Boolean): TweakResult = withContext(ioDispatcher) {
        val key = "game_mode_type"
        val targetValue = if (enableGameMode) "1" else "0"

        val written = shellExecutor.writeSetting("global", key, targetValue)
        if (!written) {
            return@withContext TweakResult.Failed("Shizuku service write call failed for key '$key'")
        }

        val readValue = shellExecutor.readSetting("global", key)
        if (readValue == null) {
            return@withContext TweakResult.Failed("Shizuku read-back operation failed for key '$key'")
        }

        if (readValue.trim() == targetValue) {
            TweakResult.Confirmed
        } else {
            TweakResult.SilentlyIgnored(key)
        }
    }

    override suspend fun disablePhantomProcessKilling(disable: Boolean): TweakResult = withContext(ioDispatcher) {
        val key = "settings_enable_monitor_phantom_procs"
        val targetValue = if (disable) "false" else "true"

        return@withContext try {
            val written = shellExecutor.setDeviceConfig("activity_manager", key, targetValue)
            if (!written) {
                return@withContext TweakResult.Failed("Shizuku service device_config write call failed for key '$key'")
            }

            val readValue = shellExecutor.readDeviceConfig("activity_manager", key)
            if (readValue == null) {
                return@withContext TweakResult.Failed("Shizuku read-back operation failed for DeviceConfig key '$key'")
            }

            if (readValue.trim().equals(targetValue, ignoreCase = true)) {
                TweakResult.Confirmed
            } else {
                TweakResult.SilentlyIgnored(key)
            }
        } catch (e: SecurityException) {
            TweakResult.Failed("SecurityException: Write/Read denied for activity_manager DeviceConfig namespace (${e.localizedMessage})")
        } catch (e: Exception) {
            TweakResult.Failed("Failed to set DeviceConfig '$key': ${e.localizedMessage}")
        }
    }

    override suspend fun disableAdaptiveBattery(disable: Boolean): TweakResult = withContext(ioDispatcher) {
        val key = "adaptive_battery_management_enabled"
        val targetValue = if (disable) "0" else "1"

        val written = shellExecutor.writeSetting("global", key, targetValue)
        if (!written) {
            return@withContext TweakResult.Failed("Shizuku service write call failed for key '$key'")
        }

        val readValue = shellExecutor.readSetting("global", key)
        if (readValue == null) {
            return@withContext TweakResult.Failed("Shizuku read-back operation failed for key '$key'")
        }

        if (readValue.trim() == targetValue) {
            TweakResult.Confirmed
        } else {
            TweakResult.SilentlyIgnored(key)
        }
    }
}
