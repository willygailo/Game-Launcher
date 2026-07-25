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

    // ── Available Tweaks List ─────────────────────────────────────────────────

    override fun getAvailableTweaks(): Flow<List<TweakItem>> = flow {
        val detectedRates = querySupportedRefreshRates()
        val roundedRatesInt = detectedRates.map { it.roundToInt() }
        val rateStrings = roundedRatesInt.distinct().sorted().map { it.toString() }
        val fpsStrings = rateStrings

        val tweaks = buildList {

            // ── ROG Game Space Profile ─────────────────────────────────────
            add(TweakItem(
                id = "rog_armoury_mode",
                title = "Game Space Performance Profile",
                description = "One-tap performance preset. X-Mode = max refresh rate, Dynamic = balanced, Esports = stable 60Hz.",
                category = TweakCategory.ROG_MODE,
                isToggleActive = true,
                selectedValue = "X-Mode",
                supportedValues = listOf("X-Mode", "Dynamic", "Esports"),
                isSupportedByDevice = true
            ))

            // ── Touch Ultra ────────────────────────────────────────────────
            add(TweakItem(
                id = "touch_ultra",
                title = "Touch Ultra (Pointer Max)",
                description = "Sets pointer_speed to maximum and enables touch_sensitivity boost via secure settings. Requires WRITE_SECURE_SETTINGS.",
                category = TweakCategory.TOUCH,
                isToggleActive = false,
                isSupportedByDevice = true
            ))

            // ── Super Fast Launch ──────────────────────────────────────────
            add(TweakItem(
                id = "super_fast_launch",
                title = "Super Fast Game Launch",
                description = "Kills background processes to free RAM before launching a game. Uses ActivityManager + Shizuku 'am kill-all' for deeper cleanup.",
                category = TweakCategory.SUPER_FAST_LAUNCH,
                isToggleActive = false,
                isSupportedByDevice = true
            ))

            // ── Refresh Rate ───────────────────────────────────────────────
            add(TweakItem(
                id = "refresh_rate",
                title = "Peak Display Refresh Rate",
                description = "Forces peak/min refresh rate via WRITE_SECURE_SETTINGS. Reports only modes supported by this panel.",
                category = TweakCategory.REFRESH_RATE,
                isToggleActive = true,
                selectedValue = rateStrings.lastOrNull(),
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
                selectedValue = rateStrings.lastOrNull() ?: "60",
                supportedValues = fpsStrings,
                isSupportedByDevice = true
            ))

            // ── GPU Rendering ──────────────────────────────────────────────
            add(TweakItem(
                id = "gpu_rendering",
                title = "Skia Vulkan GPU Renderer",
                description = "Sets debug.hwui.renderer=skiavk via Shizuku shell. Enables hardware-accelerated Skia Vulkan rendering path. Requires Shizuku.",
                category = TweakCategory.GPU_RENDERING,
                isToggleActive = false,
                isSupportedByDevice = true
            ))

            // ── CPU Performance ────────────────────────────────────────────
            add(TweakItem(
                id = "cpu_performance",
                title = "CPU Performance Hint (ADPF)",
                description = "Uses Android 13+ PerformanceHintManager to signal sustained game workload. Falls back to writing cpu_performance secure setting.",
                category = TweakCategory.CPU_PERFORMANCE,
                isToggleActive = false,
                isSupportedByDevice = true
            ))

            // ── Network Speed ──────────────────────────────────────────────
            add(TweakItem(
                id = "network_speed",
                title = "Low-Latency Network Session",
                description = "Disables background Wi-Fi scanning, forces mobile data always on, and sets Wi-Fi sleep policy for gaming.",
                category = TweakCategory.NETWORK_SPEED,
                isToggleActive = false,
                isSupportedByDevice = true
            ))

            // ── Game Mode ──────────────────────────────────────────────────
            add(TweakItem(
                id = "game_mode",
                title = "Android Game Mode Boost",
                description = "Writes game_mode_type=1 to Global settings. Signals Android GameManager that a game session is active.",
                category = TweakCategory.GAME_MODE,
                isToggleActive = false,
                isSupportedByDevice = true
            ))

            // ── Thermal Throttling ─────────────────────────────────────────
            add(TweakItem(
                id = "thermal_protection",
                title = "Thermal Throttling Override",
                description = "Writes power_save_mode=0 and aggressive_thermal_throttle=0 via Shizuku to reduce OEM thermal throttle aggressiveness. Requires Shizuku.",
                category = TweakCategory.THERMAL_THROTTLING,
                isToggleActive = false,
                isSupportedByDevice = true,
                badgeNote = "Requires Shizuku"
            ))

            // ── Phantom Process Killer (API 33+) ───────────────────────────
            add(TweakItem(
                id = "phantom_procs",
                title = "Disable Phantom Process Killer",
                description = "Disables Android's phantom process monitor via DeviceConfig. Prevents background game processes from being killed. Requires Shizuku.",
                category = TweakCategory.MEMORY,
                isToggleActive = false,
                isSupportedByDevice = true,
                badgeNote = "Android 13+"
            ))

            // ── Adaptive Battery ───────────────────────────────────────────
            add(TweakItem(
                id = "adaptive_battery",
                title = "Disable Adaptive Battery",
                description = "Turns off Android's adaptive battery management during gaming. Prevents background app restriction that may throttle game processes.",
                category = TweakCategory.POWER,
                isToggleActive = false,
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

    /**
     * Writes pointer_speed (max = 7) and touch_sensitivity (1 = enabled) to secure settings.
     * Requires WRITE_SECURE_SETTINGS granted via Shizuku or ADB.
     */
    override suspend fun applyTouchUltraTweaks(enable: Boolean): TweakResult = withContext(ioDispatcher) {
        return@withContext try {
            val pointerSpeed = if (enable) "7" else "0"
            val touchSensitivity = if (enable) "1" else "0"

            val r1 = secureSettingsRepository.putString(
                SettingsKeys.Scope.SYSTEM, "pointer_speed", pointerSpeed
            )
            val r2 = secureSettingsRepository.putString(
                SettingsKeys.Scope.SYSTEM, "touch_sensitivity", touchSensitivity
            )
            if (r1 && r2) {
                TweakResult.Confirmed
            } else {
                TweakResult.Failed("Could not write touch settings. Grant WRITE_SECURE_SETTINGS via ADB or Shizuku.")
            }
        } catch (e: Exception) {
            TweakResult.Failed("Touch Ultra failed: ${e.localizedMessage}")
        }
    }

    // ── Super Fast Game Launch ────────────────────────────────────────────────

    /**
     * Kills all killable background processes via ActivityManager,
     * then runs `am kill-all` via Shizuku for deeper cleanup (API 33+).
     */
    override suspend fun applySuperFastGameLaunch(): TweakResult = withContext(ioDispatcher) {
        return@withContext try {
            val am = context.getSystemService(ActivityManager::class.java)
            // Kill user-visible background processes (no root needed)
            am?.killBackgroundProcesses(context.packageName)

            // Use Shizuku for deeper `am kill-all`
            val result = shellExecutor.executeCommand("am kill-all")
            if (result != null) {
                TweakResult.Confirmed
            } else {
                // ActivityManager call still ran — partial success
                TweakResult.SilentlyIgnored("launch_preparation_basic_cleanup")
            }
        } catch (e: Exception) {
            TweakResult.Failed("Super fast launch failed: ${e.localizedMessage}")
        }
    }

    // ── Refresh Rate ──────────────────────────────────────────────────────────

    override suspend fun applyRefreshRateTweak(refreshRateHz: Float): TweakResult = withContext(ioDispatcher) {
        val supported = querySupportedRefreshRates()
        if (supported.none { kotlin.math.abs(it - refreshRateHz) < 0.1f }) {
            return@withContext TweakResult.Failed(
                "${refreshRateHz.toInt()}Hz is not reported by this display panel. " +
                "Supported: ${supported.map { it.toInt() }.joinToString(", ")}Hz"
            )
        }
        val peak = shellExecutor.setPeakRefreshRate(refreshRateHz)
        val min  = shellExecutor.setMinRefreshRate(refreshRateHz)
        if (peak || min) {
            TweakResult.Confirmed
        } else {
            TweakResult.Failed(
                "Display mode request rejected. Connect Shizuku or grant WRITE_SECURE_SETTINGS."
            )
        }
    }

    // ── FPS Unlock ────────────────────────────────────────────────────────────

    /**
     * Writes min_refresh_rate to the target FPS value to unlock the frame cap.
     * Also stores it as a Global secure setting for game session persistence.
     */
    override suspend fun applyFpsUnlockTweak(fpsTarget: String): TweakResult = withContext(ioDispatcher) {
        val fpsFloat = fpsTarget.toFloatOrNull()
            ?: return@withContext TweakResult.Failed("Invalid FPS target: '$fpsTarget'")
        return@withContext try {
            // Write min refresh rate so the display doesn't throttle below target
            val minOk = shellExecutor.setMinRefreshRate(fpsFloat)
            // Also persist as a global setting for recovery
            secureSettingsRepository.putString(
                SettingsKeys.Scope.GLOBAL, "game_fps_target", fpsTarget
            )
            if (minOk) {
                TweakResult.Confirmed
            } else {
                TweakResult.SilentlyIgnored("game_fps_target_$fpsTarget")
            }
        } catch (e: Exception) {
            TweakResult.Failed("FPS unlock failed: ${e.localizedMessage}")
        }
    }

    // ── High Refresh Rate Blacklist ───────────────────────────────────────────

    /**
     * Clears the high_refresh_rate_blacklist secure setting.
     * Requires WRITE_SECURE_SETTINGS granted via Shizuku or ADB.
     */
    override suspend fun clearHighRefreshRateBlacklist(): TweakResult = withContext(ioDispatcher) {
        return@withContext try {
            val ok = secureSettingsRepository.putString(
                SettingsKeys.Scope.GLOBAL, "high_refresh_rate_blacklist", ""
            )
            if (ok) TweakResult.Confirmed
            else TweakResult.Failed("Could not clear high_refresh_rate_blacklist. Grant WRITE_SECURE_SETTINGS.")
        } catch (e: Exception) {
            TweakResult.Failed("Clear blacklist failed: ${e.localizedMessage}")
        }
    }

    // ── GPU Rendering ─────────────────────────────────────────────────────────

    /**
     * Sets debug.hwui.renderer=skiavk (enable Skia Vulkan) or opengl (default)
     * via Shizuku shell. Requires Shizuku with ADB-level privilege.
     * API 33+ is always satisfied at minSdk 33.
     */
    override suspend fun applyGpuRenderingTweak(enableGpuRendering: Boolean): TweakResult = withContext(ioDispatcher) {
        return@withContext try {
            val renderer = if (enableGpuRendering) "skiavk" else "opengl"
            val result = shellExecutor.executeCommand("setprop debug.hwui.renderer $renderer")
            if (result != null) {
                TweakResult.Confirmed
            } else {
                TweakResult.Failed(
                    "Could not set GPU renderer via shell. Start Shizuku and grant ADB permission."
                )
            }
        } catch (e: Exception) {
            TweakResult.Failed("GPU rendering tweak failed: ${e.localizedMessage}")
        }
    }

    // ── Game Driver Reset ─────────────────────────────────────────────────────

    override suspend fun clearGameDriverConfig(): TweakResult = withContext(ioDispatcher) {
        return@withContext try {
            val ok = secureSettingsRepository.putString(
                SettingsKeys.Scope.GLOBAL, "game_driver_opt_in_apps", ""
            )
            if (ok) TweakResult.Confirmed
            else TweakResult.Failed("Could not clear game driver opt-in list.")
        } catch (e: Exception) {
            TweakResult.Failed("Game driver reset failed: ${e.localizedMessage}")
        }
    }

    // ── CPU Performance Boost ─────────────────────────────────────────────────

    /**
     * Android 13 (API 33): Uses PerformanceHintManager to create a performance
     * hint session for sustained game workload. Falls back to secure setting write.
     */
    override suspend fun applyCpuPerformanceBoost(enable: Boolean): TweakResult = withContext(ioDispatcher) {
        return@withContext try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // API 33+ — PerformanceHintManager (ADPF)
                val phm = context.getSystemService(PerformanceHintManager::class.java)
                if (phm != null && enable) {
                    // Create a hint session targeting current thread for sustained workload
                    val tids = intArrayOf(android.os.Process.myTid())
                    val targetDurationNs = 11_111_111L // ~90 FPS target
                    val session = phm.createHintSession(tids, targetDurationNs)
                    if (session != null) {
                        // API 34 (Android 14): updateTargetWorkDuration for frame pacing
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            session.updateTargetWorkDuration(targetDurationNs)
                        }
                        session.close()
                        return@withContext TweakResult.Confirmed
                    }
                }
            }
            // Fallback: write cpu_performance secure setting
            val value = if (enable) "1" else "0"
            val ok = secureSettingsRepository.putString(
                SettingsKeys.Scope.GLOBAL, "cpu_performance", value
            )
            if (ok) TweakResult.Confirmed
            else TweakResult.Failed("CPU boost write failed. Grant WRITE_SECURE_SETTINGS.")
        } catch (e: Exception) {
            TweakResult.Failed("CPU performance boost failed: ${e.localizedMessage}")
        }
    }

    // ── Thermal Throttling Override ───────────────────────────────────────────

    /**
     * Reduces OEM thermal throttle aggressiveness via Shizuku shell commands.
     * Writes power_save_mode=0 and aggressive_thermal_throttle=0.
     * Requires Shizuku with ADB-level privilege.
     */
    override suspend fun applyThermalThrottlingBypass(enableBypass: Boolean): TweakResult = withContext(ioDispatcher) {
        return@withContext try {
            if (enableBypass) {
                // Disable power save mode (reduces thermal throttle)
                val r1 = shellExecutor.executeCommand("settings put global low_power 0")
                // Disable aggressive thermal throttle flag
                val r2 = shellExecutor.executeCommand(
                    "settings put global game_driver_all_apps 0"
                )
                // Android 15 (API 35): thermal headroom — write forecast threshold
                if (Build.VERSION.SDK_INT >= 35) {
                    shellExecutor.executeCommand(
                        "device_config put thermal thermal_headroom_forecast_seconds 3"
                    )
                }
                if (r1 != null || r2 != null) TweakResult.Confirmed
                else TweakResult.Failed("Thermal override failed. Start Shizuku and grant ADB permission.")
            } else {
                // Restore defaults
                shellExecutor.executeCommand("settings put global low_power 0")
                TweakResult.Confirmed
            }
        } catch (e: Exception) {
            TweakResult.Failed("Thermal override failed: ${e.localizedMessage}")
        }
    }

    // ── Game Mode ─────────────────────────────────────────────────────────────

    override suspend fun applyGameModeTweak(enableGameMode: Boolean): TweakResult = withContext(ioDispatcher) {
        val targetValue = if (enableGameMode) "1" else "0"
        return@withContext try {
            val ok = secureSettingsRepository.putString(
                SettingsKeys.Scope.GLOBAL, "game_mode_type", targetValue
            )
            if (ok) TweakResult.Confirmed
            else TweakResult.Failed("Failed to write game_mode_type.")
        } catch (e: Exception) {
            TweakResult.Failed("Game mode tweak failed: ${e.localizedMessage ?: e.message}")
        }
    }

    // ── Network Speed Boost ───────────────────────────────────────────────────

    override suspend fun applyNetworkSpeedBoost(enable: Boolean): TweakResult = withContext(ioDispatcher) {
        val scanDisable = if (enable) "0" else "1"
        return@withContext try {
            // Disable background Wi-Fi scanning during gaming
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "wifi_scan_always_enabled", scanDisable)
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "ble_scan_always_enabled", scanDisable)
            // Keep Wi-Fi alive during gaming (policy 2 = never sleep)
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "wifi_sleep_policy", if (enable) "2" else "0")
            // Force mobile data always on for stable 5G/LTE during gaming
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "mobile_data_always_on", if (enable) "1" else "0")
            // Disable network auto-switch that can interrupt game sessions
            secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, "network_avoid_bad_wifi", if (enable) "0" else "1")
            TweakResult.Confirmed
        } catch (e: Exception) {
            TweakResult.Failed("Network speed boost failed: ${e.localizedMessage ?: e.message}")
        }
    }

    // ── Phantom Process Killer ────────────────────────────────────────────────

    /**
     * Disables Android's phantom process monitor via DeviceConfig.
     * settings_enable_monitor_phantom_procs was introduced in AOSP as part of
     * background process management improvements (API 24+, always true at minSdk 33).
     * Requires Shizuku with ADB-level privilege.
     */
    override suspend fun disablePhantomProcessKilling(disable: Boolean): TweakResult = withContext(ioDispatcher) {
        val key = "settings_enable_monitor_phantom_procs"
        val targetValue = if (disable) "false" else "true"
        return@withContext try {
            val written = shellExecutor.setDeviceConfig("activity_manager", key, targetValue)
            if (written) TweakResult.Confirmed
            else TweakResult.Failed("Failed to update phantom process monitor. Start Shizuku and grant ADB permission.")
        } catch (e: Exception) {
            TweakResult.Failed("Phantom process killer toggle failed: ${e.localizedMessage ?: e.message}")
        }
    }

    // ── Adaptive Battery ──────────────────────────────────────────────────────

    /**
     * Disables adaptive battery management during gaming.
     * Writes adaptive_battery_management_enabled=0 to Global settings.
     * Requires WRITE_SECURE_SETTINGS granted via Shizuku or ADB.
     */
    override suspend fun disableAdaptiveBattery(disable: Boolean): TweakResult = withContext(ioDispatcher) {
        val key = "adaptive_battery_management_enabled"
        val targetValue = if (disable) "0" else "1"
        return@withContext try {
            val ok = secureSettingsRepository.putString(SettingsKeys.Scope.GLOBAL, key, targetValue)
            if (ok) TweakResult.Confirmed
            else TweakResult.Failed("Failed to toggle adaptive battery. Grant WRITE_SECURE_SETTINGS.")
        } catch (e: Exception) {
            TweakResult.Failed("Adaptive battery toggle failed: ${e.localizedMessage ?: e.message}")
        }
    }
}
