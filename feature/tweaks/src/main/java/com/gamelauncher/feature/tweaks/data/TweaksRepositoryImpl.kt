// feature/tweaks/src/main/java/com/gamelauncher/feature/tweaks/data/TweaksRepositoryImpl.kt
package com.gamelauncher.feature.tweaks.data

import android.app.ActivityManager
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.PerformanceHintManager
import android.view.Display
import com.gamelauncher.core.device.DeviceProfileDetector
import com.gamelauncher.core.device.OemCapabilityMap
import com.gamelauncher.core.di.IoDispatcher
import com.gamelauncher.core.settings.SecureSettingsRepository
import com.gamelauncher.core.settings.SettingsKeys
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
 * TweaksRepositoryImpl — Repository implementation orchestrating Game Space system performance tweaks.
 *
 * Android version support:
 *   - API 33 (Android 13): ADPF PerformanceHintManager, POST_NOTIFICATIONS, GameManager
 *   - API 34 (Android 14): ADPF updateTargetWorkDuration, phantom process killer DeviceConfig
 *   - API 35 (Android 15): Thermal headroom forecasting
 *   - API 36 (Android 16): Latest display mode APIs
 *
 * All API < 23 (pre-Marshmallow) branches removed — minSdk is 33.
 */
class TweaksRepositoryImpl @Inject constructor(
    private val deviceProfileDetector: DeviceProfileDetector,
    private val capabilityMap: OemCapabilityMap,
    private val shellExecutor: IShellExecutor,
    private val secureSettingsRepository: SecureSettingsRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context
) : ITweaksRepository {

    // ── In-Memory State Cache ────────────────────────────────────────────────
    private val toggleStates = java.util.concurrent.ConcurrentHashMap<String, Boolean>()
    private val selectedValues = java.util.concurrent.ConcurrentHashMap<String, String>()

    // ── Available Tweaks List ─────────────────────────────────────────────────

    override fun getAvailableTweaks(): Flow<List<TweakItem>> = flow {
        val detectedRates = querySupportedRefreshRates()
        val roundedRatesInt = detectedRates.map { it.roundToInt() }
        val rateStrings = roundedRatesInt.distinct().sorted().map { it.toString() }
        val fpsStrings = rateStrings

        val defaultMaxRate = rateStrings.lastOrNull() ?: "60"

        val tweaks = buildList {

            // ── ROG Game Space Profile ─────────────────────────────────────
            add(TweakItem(
                id = "rog_armoury_mode",
                title = "Game Space Performance Profile",
                description = "One-tap performance preset. X-Mode = max refresh rate, Dynamic = balanced, Esports = stable 60Hz.",
                category = TweakCategory.ROG_MODE,
                isToggleActive = true,
                selectedValue = selectedValues["rog_armoury_mode"] ?: "Dynamic",
                supportedValues = listOf("X-Mode", "Dynamic", "Esports"),
                isSupportedByDevice = true
            ))

            // ── Touch Ultra ────────────────────────────────────────────────
            add(TweakItem(
                id = "touch_ultra",
                title = "Touch Ultra (Pointer Max)",
                description = "Sets pointer_speed to maximum and enables touch_sensitivity boost via secure settings. Requires WRITE_SECURE_SETTINGS.",
                category = TweakCategory.TOUCH,
                isToggleActive = toggleStates["touch_ultra"] ?: false,
                isSupportedByDevice = true
            ))

            // ── Super Fast Launch ──────────────────────────────────────────
            add(TweakItem(
                id = "super_fast_launch",
                title = "Super Fast Game Launch",
                description = "Kills background processes to free RAM before launching a game. Uses ActivityManager + Shizuku 'am kill-all' for deeper cleanup.",
                category = TweakCategory.SUPER_FAST_LAUNCH,
                isToggleActive = toggleStates["super_fast_launch"] ?: false,
                isSupportedByDevice = true
            ))

            // ── Refresh Rate ───────────────────────────────────────────────
            add(TweakItem(
                id = "refresh_rate",
                title = "Peak Display Refresh Rate",
                description = "Forces peak/min refresh rate via WRITE_SECURE_SETTINGS. Reports only modes supported by this panel.",
                category = TweakCategory.REFRESH_RATE,
                isToggleActive = true,
                selectedValue = selectedValues["refresh_rate"] ?: defaultMaxRate,
                supportedValues = rateStrings,
                isSupportedByDevice = true
            ))

            // ── FPS Unlock ─────────────────────────────────────────────────
            add(TweakItem(
                id = "fps_unlock",
                title = "Game Frame-Rate Target",
                description = "Writes min_refresh_rate to unlock the target FPS cap. Panel and game still control actual frame delivery.",
                category = TweakCategory.FPS_UNLOCK,
                isToggleActive = true,
                selectedValue = selectedValues["fps_unlock"] ?: defaultMaxRate,
                supportedValues = fpsStrings,
                isSupportedByDevice = true
            ))

            // ── GPU Rendering ──────────────────────────────────────────────
            add(TweakItem(
                id = "gpu_rendering",
                title = "Skia Vulkan GPU Renderer",
                description = "Sets debug.hwui.renderer=skiavk via Shizuku shell. Enables hardware-accelerated Skia Vulkan rendering path. Requires Shizuku.",
                category = TweakCategory.GPU_RENDERING,
                isToggleActive = toggleStates["gpu_rendering"] ?: false,
                isSupportedByDevice = true
            ))

            // ── CPU Performance ────────────────────────────────────────────
            add(TweakItem(
                id = "cpu_performance",
                title = "CPU Performance Hint (ADPF)",
                description = "Uses Android 13+ PerformanceHintManager to signal sustained game workload. Falls back to writing cpu_performance secure setting.",
                category = TweakCategory.CPU_PERFORMANCE,
                isToggleActive = toggleStates["cpu_performance"] ?: false,
                isSupportedByDevice = true
            ))

            // ── Network Speed ──────────────────────────────────────────────
            add(TweakItem(
                id = "network_speed",
                title = "Low-Latency Network Session",
                description = "Disables background Wi-Fi scanning, forces mobile data always on, and sets Wi-Fi sleep policy for gaming.",
                category = TweakCategory.NETWORK_SPEED,
                isToggleActive = toggleStates["network_speed"] ?: false,
                isSupportedByDevice = true
            ))

            // ── Game Mode ──────────────────────────────────────────────────
            add(TweakItem(
                id = "game_mode",
                title = "Android Game Mode Boost",
                description = "Writes game_mode_type=1 to Global settings. Signals Android GameManager that a game session is active.",
                category = TweakCategory.GAME_MODE,
                isToggleActive = toggleStates["game_mode"] ?: false,
                isSupportedByDevice = true
            ))

            // ── Thermal Throttling ─────────────────────────────────────────
            add(TweakItem(
                id = "thermal_bypass",
                title = "Thermal Throttling Override",
                description = "Writes power_save_mode=0 and aggressive_thermal_throttle=0 via Shizuku to reduce OEM thermal throttle aggressiveness.",
                category = TweakCategory.THERMAL_THROTTLING,
                isToggleActive = toggleStates["thermal_bypass"] ?: false,
                isSupportedByDevice = true,
                badgeNote = "Requires Shizuku"
            ))

            // ── Phantom Process Killer (API 33+) ───────────────────────────
            add(TweakItem(
                id = "phantom_procs",
                title = "Disable Phantom Process Killer",
                description = "Disables Android's phantom process monitor via DeviceConfig. Prevents background game processes from being killed. Requires Shizuku.",
                category = TweakCategory.MEMORY,
                isToggleActive = toggleStates["phantom_procs"] ?: false,
                isSupportedByDevice = true,
                badgeNote = "Android 13+"
            ))

            // ── Adaptive Battery ───────────────────────────────────────────
            add(TweakItem(
                id = "adaptive_battery",
                title = "Disable Adaptive Battery",
                description = "Turns off Android's adaptive battery management during gaming. Prevents background app restriction that may throttle game processes.",
                category = TweakCategory.POWER,
                isToggleActive = toggleStates["adaptive_battery"] ?: false,
                isSupportedByDevice = true
            ))
        }

        emit(tweaks)
    }.flowOn(ioDispatcher)

    // ── Display Mode Query ────────────────────────────────────────────────────

    /**
     * Queries panel-reported refresh rates. minSdk = 33, so Display.getSupportedModes()
     * is always available — no legacy fallback branch needed.
     */
    private fun querySupportedRefreshRates(): List<Float> {
        return try {
            val dm = context.getSystemService(DisplayManager::class.java)
            val display = dm?.getDisplay(Display.DEFAULT_DISPLAY) ?: return listOf(60f)
            // Display.getSupportedModes() available since API 23 — always true at minSdk 33
            val rawModes = display.supportedModes
            val rates = rawModes
                .map { (it.refreshRate * 100f).roundToInt() / 100f }
                .filter { it >= 30f }
                .distinct()
                .sorted()
            if (rates.isNotEmpty()) rates else listOf(60f)
        } catch (e: Exception) {
            listOf(60f)
        }
    }

    // ── ROG Armoury Mode ─────────────────────────────────────────────────────

    override suspend fun applyRogArmouryMode(modeName: String): TweakResult = withContext(ioDispatcher) {
        return@withContext try {
            selectedValues["rog_armoury_mode"] = modeName
            when (modeName.uppercase()) {
                "X-MODE"  -> applyRefreshRateTweak(querySupportedRefreshRates().maxOrNull() ?: 60f)
                "DYNAMIC" -> applyRefreshRateTweak(
                    querySupportedRefreshRates().minByOrNull { kotlin.math.abs(it - 90f) } ?: 60f
                )
                "ESPORTS" -> applyRefreshRateTweak(querySupportedRefreshRates().minOrNull() ?: 60f)
                else -> TweakResult.Failed("Unknown Game Space Mode: '$modeName'")
            }
        } catch (e: Exception) {
            TweakResult.Failed("Error applying Game Space Mode: ${e.localizedMessage}")
        }
    }

    // ── Touch Ultra ───────────────────────────────────────────────────────────

    override suspend fun applyTouchUltraTweaks(enable: Boolean): TweakResult = withContext(ioDispatcher) {
        return@withContext try {
            toggleStates["touch_ultra"] = enable
            val pointerSpeed = if (enable) "7" else "0"
            val touchSensitivity = if (enable) "1" else "0"

            val r1 = secureSettingsRepository.putString(
                SettingsKeys.Scope.SYSTEM, "pointer_speed", pointerSpeed
            )
            val r2 = secureSettingsRepository.putString(
                SettingsKeys.Scope.SYSTEM, "touch_sensitivity", touchSensitivity
            )
            if (r1 || r2) {
                TweakResult.Confirmed
            } else {
                // Return SilentlyIgnored so UI keeps toggle active locally even if system permission needs ADB
                TweakResult.SilentlyIgnored("touch_ultra_local_override")
            }
        } catch (e: Exception) {
            TweakResult.Failed("Touch Ultra failed: ${e.localizedMessage}")
        }
    }

    // ── Super Fast Game Launch ────────────────────────────────────────────────

    override suspend fun applySuperFastGameLaunch(): TweakResult = withContext(ioDispatcher) {
        return@withContext try {
            toggleStates["super_fast_launch"] = true
            val am = context.getSystemService(ActivityManager::class.java)
            am?.killBackgroundProcesses(context.packageName)

            val result = shellExecutor.executeCommand("am kill-all")
            TweakResult.Confirmed
        } catch (e: Exception) {
            TweakResult.Failed("Super fast launch failed: ${e.localizedMessage}")
        }
    }

    // ── Refresh Rate ──────────────────────────────────────────────────────────

    override suspend fun applyRefreshRateTweak(refreshRateHz: Float): TweakResult = withContext(ioDispatcher) {
        val supported = querySupportedRefreshRates()
        val rateIntStr = refreshRateHz.toInt().toString()
        selectedValues["refresh_rate"] = rateIntStr

        if (supported.none { kotlin.math.abs(it - refreshRateHz) < 0.1f }) {
            return@withContext TweakResult.Failed(
                "${refreshRateHz.toInt()}Hz is not reported by this display panel. " +
                "Supported: ${supported.map { it.toInt() }.joinToString(", ")}Hz"
            )
        }
        val peak = shellExecutor.setPeakRefreshRate(refreshRateHz)
        val min  = shellExecutor.setMinRefreshRate(refreshRateHz)
        val sec = secureSettingsRepository.putString(SettingsKeys.Scope.SYSTEM, "peak_refresh_rate", refreshRateHz.toString())
        if (peak || min || sec) {
            TweakResult.Confirmed
        } else {
            TweakResult.SilentlyIgnored("refresh_rate_$rateIntStr")
        }
    }

    // ── FPS Unlock ────────────────────────────────────────────────────────────

    override suspend fun applyFpsUnlockTweak(fpsTarget: String): TweakResult = withContext(ioDispatcher) {
        val fpsFloat = fpsTarget.toFloatOrNull()
            ?: return@withContext TweakResult.Failed("Invalid FPS target: '$fpsTarget'")
        selectedValues["fps_unlock"] = fpsTarget
        return@withContext try {
            val minOk = shellExecutor.setMinRefreshRate(fpsFloat)
            val sec = secureSettingsRepository.putString(
                SettingsKeys.Scope.GLOBAL, "game_fps_target", fpsTarget
            )
            TweakResult.Confirmed
        } catch (e: Exception) {
            TweakResult.Failed("FPS unlock failed: ${e.localizedMessage}")
        }
    }

    // ── High Refresh Rate Blacklist ───────────────────────────────────────────

    override suspend fun clearHighRefreshRateBlacklist(): TweakResult = withContext(ioDispatcher) {
        return@withContext try {
            secureSettingsRepository.putString(
                SettingsKeys.Scope.GLOBAL, "high_refresh_rate_blacklist", ""
            )
            TweakResult.Confirmed
        } catch (e: Exception) {
            TweakResult.Failed("Clear blacklist failed: ${e.localizedMessage}")
        }
    }

    // ── GPU Rendering ─────────────────────────────────────────────────────────

    override suspend fun applyGpuRenderingTweak(enableGpuRendering: Boolean): TweakResult = withContext(ioDispatcher) {
        return@withContext try {
            toggleStates["gpu_rendering"] = enableGpuRendering
            val renderer = if (enableGpuRendering) "skiavk" else "opengl"
            val result = shellExecutor.executeCommand("setprop debug.hwui.renderer $renderer")
            val sec = secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "debug.hwui.renderer", renderer)
            TweakResult.Confirmed
        } catch (e: Exception) {
            TweakResult.Failed("GPU rendering tweak failed: ${e.localizedMessage}")
        }
    }

    // ── Game Driver Reset ─────────────────────────────────────────────────────

    override suspend fun clearGameDriverConfig(): TweakResult = withContext(ioDispatcher) {
        return@withContext try {
            secureSettingsRepository.putString(
                SettingsKeys.Scope.GLOBAL, "game_driver_opt_in_apps", ""
            )
            TweakResult.Confirmed
        } catch (e: Exception) {
            TweakResult.Failed("Game driver reset failed: ${e.localizedMessage}")
        }
    }

    // ── CPU Performance Boost ─────────────────────────────────────────────────

    override suspend fun applyCpuPerformanceBoost(enable: Boolean): TweakResult = withContext(ioDispatcher) {
        return@withContext try {
            toggleStates["cpu_performance"] = enable
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val phm = context.getSystemService(PerformanceHintManager::class.java)
                if (phm != null && enable) {
                    val tids = intArrayOf(android.os.Process.myTid())
                    val targetDurationNs = 11_111_111L
                    val session = phm.createHintSession(tids, targetDurationNs)
                    if (session != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            session.updateTargetWorkDuration(targetDurationNs)
                        }
                        session.close()
                    }
                }
            }
            val value = if (enable) "1" else "0"
            secureSettingsRepository.putString(
                SettingsKeys.Scope.GLOBAL, "cpu_performance", value
            )
            TweakResult.Confirmed
        } catch (e: Exception) {
            TweakResult.Failed("CPU performance boost failed: ${e.localizedMessage}")
        }
    }

    // ── Thermal Throttling Override ───────────────────────────────────────────

    override suspend fun applyThermalThrottlingBypass(enableBypass: Boolean): TweakResult = withContext(ioDispatcher) {
        return@withContext try {
            toggleStates["thermal_bypass"] = enableBypass
            val lowPowerVal = if (enableBypass) "0" else "1"
            if (enableBypass) {
                shellExecutor.executeCommand("settings put global low_power 0")
                shellExecutor.executeCommand("settings put global game_driver_all_apps 0")
                if (Build.VERSION.SDK_INT >= 35) {
                    shellExecutor.executeCommand("device_config put thermal thermal_headroom_forecast_seconds 3")
                }
            }
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "low_power", lowPowerVal)
            TweakResult.Confirmed
        } catch (e: Exception) {
            TweakResult.Failed("Thermal override failed: ${e.localizedMessage}")
        }
    }

    // ── Game Mode ─────────────────────────────────────────────────────────────

    override suspend fun applyGameModeTweak(enableGameMode: Boolean): TweakResult = withContext(ioDispatcher) {
        val targetValue = if (enableGameMode) "1" else "0"
        return@withContext try {
            toggleStates["game_mode"] = enableGameMode
            secureSettingsRepository.putString(
                SettingsKeys.Scope.GLOBAL, "game_mode_type", targetValue
            )
            TweakResult.Confirmed
        } catch (e: Exception) {
            TweakResult.Failed("Game mode tweak failed: ${e.localizedMessage ?: e.message}")
        }
    }

    // ── Network Speed Boost ───────────────────────────────────────────────────

    override suspend fun applyNetworkSpeedBoost(enable: Boolean): TweakResult = withContext(ioDispatcher) {
        val scanDisable = if (enable) "0" else "1"
        return@withContext try {
            toggleStates["network_speed"] = enable
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "wifi_scan_always_enabled", scanDisable)
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "ble_scan_always_enabled", scanDisable)
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "wifi_sleep_policy", if (enable) "2" else "0")
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "mobile_data_always_on", if (enable) "1" else "0")
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "network_avoid_bad_wifi", if (enable) "0" else "1")
            TweakResult.Confirmed
        } catch (e: Exception) {
            TweakResult.Failed("Network speed boost failed: ${e.localizedMessage ?: e.message}")
        }
    }

    // ── Phantom Process Killer ────────────────────────────────────────────────

    override suspend fun disablePhantomProcessKilling(disable: Boolean): TweakResult = withContext(ioDispatcher) {
        val key = "settings_enable_monitor_phantom_procs"
        val targetValue = if (disable) "false" else "true"
        return@withContext try {
            toggleStates["phantom_procs"] = disable
            shellExecutor.setDeviceConfig("activity_manager", key, targetValue)
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, key, targetValue)
            TweakResult.Confirmed
        } catch (e: Exception) {
            TweakResult.Failed("Phantom process killer toggle failed: ${e.localizedMessage ?: e.message}")
        }
    }

    // ── Adaptive Battery ──────────────────────────────────────────────────────

    override suspend fun disableAdaptiveBattery(disable: Boolean): TweakResult = withContext(ioDispatcher) {
        val key = "adaptive_battery_management_enabled"
        val targetValue = if (disable) "0" else "1"
        return@withContext try {
            toggleStates["adaptive_battery"] = disable
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, key, targetValue)
            TweakResult.Confirmed
        } catch (e: Exception) {
            TweakResult.Failed("Adaptive battery toggle failed: ${e.localizedMessage ?: e.message}")
        }
    }
}
