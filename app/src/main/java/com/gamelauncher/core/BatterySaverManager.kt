package com.gamelauncher.core

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BatterySaverManager — triple-layer battery saver killer.
 *
 * Layer 1: Settings.Global "low_power" = 0  (needs WRITE_SECURE_SETTINGS)
 * Layer 2: PowerManager.setPowerSaveModeEnabled(false) via reflection (needs root or manufacturer SDK)
 * Layer 3: Root shell: settings put global low_power 0 + dumpsys battery unplug/reset
 *
 * Also handles:
 *  - Battery optimization exemption request (REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
 *  - Adaptive battery disable (Settings.Global "adaptive_battery_management_enabled" = 0)
 *  - Doze mode suspend (dumpsys deviceidle disable  — root only)
 *  - Background app restriction whitelist for the game package
 */
@Singleton
class BatterySaverManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rootShellManager: RootShellManager
) {

    private val _isBatterySaverActive = MutableStateFlow(false)
    val isBatterySaverActive: StateFlow<Boolean> = _isBatterySaverActive

    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging

    // Store original states for clean restore
    @Volatile private var originalLowPower: Int? = null
    @Volatile private var originalAdaptiveBattery: Int? = null
    @Volatile private var wasDisabledByUs: Boolean = false

    // ── Status Query ───────────────────────────────────────────────────

    fun refreshBatteryStatus() {
        try {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                _batteryLevel.value = (level * 100 / scale)
            }
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val pm = context.getSystemService(PowerManager::class.java)
            _isBatterySaverActive.value = pm?.isPowerSaveMode ?: false
        } catch (_: Exception) {}
    }

    fun isBatterySaverCurrentlyOn(): Boolean {
        return try {
            val pm = context.getSystemService(PowerManager::class.java)
            pm?.isPowerSaveMode ?: false
        } catch (_: Exception) { false }
    }

    fun isAdaptiveBatteryOn(): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, "adaptive_battery_management_enabled", 1) == 1
        } catch (_: Exception) { false }
    }

    // ── Disable Battery Saver (main entry point) ───────────────────────

    /**
     * Tries all available methods in order. Returns true if at least one succeeded.
     * Call this right before startBoost() so it's already off when the game launches.
     */
    suspend fun disableBatterySaver(): Boolean = withContext(Dispatchers.IO) {
        var anySuccess = false
        wasDisabledByUs = false

        // Check if it's even on before we waste calls
        if (!isBatterySaverCurrentlyOn()) {
            // Still save original so restore is a no-op
            originalLowPower = 0
            return@withContext true
        }

        refreshBatteryStatus()

        // ── Layer 1: Settings.Global (WRITE_SECURE_SETTINGS) ──────────
        val hasSS = hasSecureSettingsPermission()
        if (hasSS) {
            try {
                originalLowPower = Settings.Global.getInt(
                    context.contentResolver, "low_power", 0
                )
                originalAdaptiveBattery = Settings.Global.getInt(
                    context.contentResolver, "adaptive_battery_management_enabled", 1
                )

                val r1 = Settings.Global.putInt(context.contentResolver, "low_power", 0)
                // Also kill adaptive battery — it re-enables battery saver automatically
                Settings.Global.putInt(
                    context.contentResolver, "adaptive_battery_management_enabled", 0
                )
                // Android 12+ low_power_sticky
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Settings.Global.putInt(context.contentResolver, "low_power_sticky", 0)
                    Settings.Global.putInt(context.contentResolver, "automatic_power_saver_mode", 0)
                }
                if (r1) {
                    anySuccess = true
                    wasDisabledByUs = true
                }
            } catch (_: Exception) {}
        }

        // ── Layer 2: PowerManager reflection ──────────────────────────
        try {
            val pm = context.getSystemService(PowerManager::class.java)
            pm?.let {
                val method = it.javaClass.getMethod("setPowerSaveModeEnabled", Boolean::class.java)
                method.invoke(it, false)
                anySuccess = true
                wasDisabledByUs = true
            }
        } catch (_: Exception) {}

        // ── Layer 3: Root shell (nuclear option) ───────────────────────
        if (!anySuccess && rootShellManager.isRootAvailable()) {
            anySuccess = disableBatterySaverRoot()
        }

        // Final verification
        val stillOn = isBatterySaverCurrentlyOn()
        if (stillOn && rootShellManager.isRootAvailable()) {
            // Nuclear: force via dumpsys
            disableBatterySaverRoot()
            anySuccess = true
        }

        _isBatterySaverActive.value = isBatterySaverCurrentlyOn()
        anySuccess
    }

    private suspend fun disableBatterySaverRoot(): Boolean {
        if (!rootShellManager.isRootAvailable()) return false
        val commands = listOf(
            "settings put global low_power 0",
            "settings put global low_power_sticky 0",
            "settings put global automatic_power_saver_mode 0",
            "settings put global adaptive_battery_management_enabled 0",
            "dumpsys battery unplug",
            "dumpsys deviceidle disable",
            "dumpsys deviceidle step active",
            // Samsung-specific: force performance mode (Galaxy devices)
            "am broadcast -a com.samsung.android.bixby.agent.action.DEVICE_PERFORMANCE_MODE_CHANGED --ei mode 1",
            // MIUI-specific battery saver kill
            "settings put global POWER_SAVING_MODE 0",
            "settings put global power_save_mode 0"
        )
        var success = false
        for (cmd in commands) {
            val (ok, _) = rootShellManager.executeCommand(cmd)
            if (ok) success = true
        }
        wasDisabledByUs = true
        return success
    }

    // ── Restore Battery Saver ──────────────────────────────────────────

    suspend fun restoreBatterySaver() = withContext(Dispatchers.IO) {
        if (!wasDisabledByUs) return@withContext

        try {
            val hasSS = hasSecureSettingsPermission()
            if (hasSS) {
                originalLowPower?.let {
                    Settings.Global.putInt(context.contentResolver, "low_power", it)
                }
                originalAdaptiveBattery?.let {
                    Settings.Global.putInt(
                        context.contentResolver, "adaptive_battery_management_enabled", it
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Settings.Global.putInt(context.contentResolver, "low_power_sticky", 0)
                }
            }
        } catch (_: Exception) {}

        if (rootShellManager.isRootAvailable()) {
            rootShellManager.executeCommand("settings put global low_power ${originalLowPower ?: 0}")
            rootShellManager.executeCommand("dumpsys battery reset")
            rootShellManager.executeCommand("dumpsys deviceidle enable")
        }

        originalLowPower = null
        originalAdaptiveBattery = null
        wasDisabledByUs = false
        _isBatterySaverActive.value = isBatterySaverCurrentlyOn()
    }

    // ── Battery Optimization (Doze) Exemption ─────────────────────────

    /**
     * Checks if the app itself is battery-optimized (Doze will kill our service).
     * Returns an Intent to open the system dialog — caller should startActivity().
     * Only pops up if we're NOT already whitelisted.
     */
    fun getBatteryOptimizationExemptionIntent(): Intent? {
        return try {
            val pm = context.getSystemService(PowerManager::class.java) ?: return null
            val pkg = context.packageName
            if (!pm.isIgnoringBatteryOptimizations(pkg)) {
                Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = android.net.Uri.parse("package:$pkg")
                }
            } else null // Already whitelisted, no need
        } catch (_: Exception) { null }
    }

    fun isIgnoringBatteryOptimizations(): Boolean {
        return try {
            val pm = context.getSystemService(PowerManager::class.java) ?: return false
            pm.isIgnoringBatteryOptimizations(context.packageName)
        } catch (_: Exception) { false }
    }

    /**
     * Root-only: whitelist a game package from battery optimization so Android
     * won't kill it in the background or throttle it via Doze.
     */
    suspend fun whitelistGameFromDoze(packageName: String): Boolean = withContext(Dispatchers.IO) {
        if (!rootShellManager.isRootAvailable()) return@withContext false
        val cmds = listOf(
            "dumpsys deviceidle whitelist +$packageName",
            "dumpsys deviceidle tempwhitelist -d 3600000 $packageName"
        )
        var ok = false
        for (cmd in cmds) {
            val (success, _) = rootShellManager.executeCommand(cmd)
            if (success) ok = true
        }
        ok
    }

    // ── Thermal Throttle Suspend (Root) ───────────────────────────────

    /**
     * Tells the thermal daemon to back off throttling.
     * WARNING: Only use during gaming — prolonged use without restore = device damage.
     */
    suspend fun suspendThermalThrottling(): Boolean = withContext(Dispatchers.IO) {
        if (!rootShellManager.isRootAvailable()) return@withContext false
        val cmds = listOf(
            "stop thermal-engine",
            "stop thermald",
            "stop mi_thermald",
            // Qualcomm/Snapdragon thermal disable
            "echo 0 > /sys/class/thermal/thermal_zone0/mode",
            "echo 0 > /sys/module/msm_thermal/parameters/enabled",
            // Samsung Exynos thermal
            "echo 0 > /sys/power/cpufreq_min_limit",
            "echo 0 > /sys/power/cpufreq_max_limit"
        )
        var ok = false
        for (cmd in cmds) {
            val (success, _) = rootShellManager.executeCommand(cmd)
            if (success) ok = true
        }
        ok
    }

    suspend fun restoreThermalThrottling(): Boolean = withContext(Dispatchers.IO) {
        if (!rootShellManager.isRootAvailable()) return@withContext false
        val cmds = listOf(
            "start thermal-engine",
            "start thermald",
            "start mi_thermald",
            "echo 1 > /sys/module/msm_thermal/parameters/enabled"
        )
        var ok = false
        for (cmd in cmds) {
            val (success, _) = rootShellManager.executeCommand(cmd)
            if (success) ok = true
        }
        ok
    }

    // ── Helpers ────────────────────────────────────────────────────────

    private fun hasSecureSettingsPermission(): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
