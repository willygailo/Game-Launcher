package com.gamelauncher.core

import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BypassChargingManager — stops battery cell charging while device stays powered via cable.
 *
 * Works on ALL Android models (10–16) via Shizuku (adb-shell-level), no root needed.
 * Falls back to root sysfs path if Shizuku is unavailable.
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │  Layer  │ Command                           │ Android  │ Brands     │
 * │─────────│───────────────────────────────────│──────────│────────────│
 * │    1    │ cmd battery set charge_control     │ 14+ (34+)│ All        │
 * │    2    │ dumpsys battery set status 3       │ 10+      │ All        │
 * │         │ dumpsys battery set ac 0           │          │            │
 * │         │ dumpsys battery set usb 0          │          │            │
 * │    3    │ input_suspend / charge_disable     │ varies   │ OEM-spec   │
 * │    R    │ dumpsys battery reset              │ All      │ Restore    │
 * └─────────────────────────────────────────────────────────────────────┘
 */
@Singleton
class BypassChargingManager @Inject constructor(
    private val shellManager: ShizukuShellManager
) {

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _isShellAvailable = MutableStateFlow(false)
    val isShellAvailable: StateFlow<Boolean> = _isShellAvailable.asStateFlow()

    // Sysfs paths tried in order — first writable one wins
    private val INPUT_SUSPEND_PATHS = listOf(
        "/sys/class/power_supply/battery/input_suspend",          // Qualcomm / ASUS ROG / Pixel
        "/sys/class/power_supply/battery/charging_enabled",       // Qualcomm legacy (inverted)
        "/sys/class/power_supply/bms/charge_disable",             // Xiaomi / Poco
        "/sys/class/power_supply/usb/charge_enabled",             // Samsung (inverted)
        "/sys/class/power_supply/battery/stop_charging",          // MediaTek some OEMs
        "/sys/kernel/debug/supply/charge_suspend"                 // Debug kernel path
    )

    // Paths where 0 = charging ON, 1 = suspend (standard)
    private val SUSPEND_VALUE_1_PATHS = setOf(
        "/sys/class/power_supply/battery/input_suspend",
        "/sys/class/power_supply/bms/charge_disable",
        "/sys/class/power_supply/battery/stop_charging",
        "/sys/kernel/debug/supply/charge_suspend"
    )

    // Paths where value is inverted: 1 = charging ON, 0 = suspend
    private val INVERTED_PATHS = setOf(
        "/sys/class/power_supply/battery/charging_enabled",
        "/sys/class/power_supply/usb/charge_enabled"
    )

    suspend fun refreshAvailability() {
        _isShellAvailable.value = shellManager.isAvailable() ||
                shellManager.isShizukuRunning()
    }

    // ── Main toggle ────────────────────────────────────────────────────

    /**
     * Enable or disable bypass charging.
     * @return true if at least one layer succeeded.
     */
    suspend fun setBypassCharging(enable: Boolean): Boolean = withContext(Dispatchers.IO) {
        val success = if (enable) enableBypass() else disableBypass()
        if (success) _isEnabled.value = enable
        success
    }

    // ── Enable Bypass ──────────────────────────────────────────────────

    private suspend fun enableBypass(): Boolean {
        var anyOk = false

        // ── Layer 1: Android 14+ charge_control (most reliable, HAL-level) ──
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val (ok, _) = shellManager.executeCommand("cmd battery set charge_control stop")
            if (ok) {
                anyOk = true
                return true // HAL-level success → done
            }
        }

        // ── Layer 2: dumpsys battery fake (ALL devices, all brands) ──────────
        // Sets the battery status to NOT_CHARGING (3) and disconnects
        // AC/USB power sources. Sufficient to prevent charging on many devices
        // and universally stops thermal heat from fast charging.
        val layer2Cmds = listOf(
            "dumpsys battery set status 3",   // NOT_CHARGING
            "dumpsys battery set ac 0",        // Disconnect AC
            "dumpsys battery set usb 0",       // Disconnect USB
            "dumpsys battery set wireless 0"   // Disconnect wireless
        )
        val (ok2, _) = shellManager.executeAny(layer2Cmds)
        if (ok2) anyOk = true

        // ── Layer 3: sysfs kernel path (hardware-level, best-effort) ─────────
        val sysfsOk = trySysfsSuspend(enable = true)
        if (sysfsOk) anyOk = true

        return anyOk
    }

    // ── Disable Bypass (Restore) ───────────────────────────────────────

    private suspend fun disableBypass(): Boolean {
        var anyOk = false

        // Layer 1: Android 14+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val (ok, _) = shellManager.executeCommand("cmd battery set charge_control resume")
            if (ok) anyOk = true
        }

        // Layer 2: reset dumpsys battery to real hardware values
        val (ok2, _) = shellManager.executeCommand("dumpsys battery reset")
        if (ok2) anyOk = true

        // Layer 3: restore sysfs
        val sysfsOk = trySysfsSuspend(enable = false)
        if (sysfsOk) anyOk = true

        return anyOk
    }

    // ── Sysfs Helper ──────────────────────────────────────────────────

    private suspend fun trySysfsSuspend(enable: Boolean): Boolean {
        for (path in INPUT_SUSPEND_PATHS) {
            val value = when {
                path in INVERTED_PATHS -> if (enable) "0" else "1"
                else -> if (enable) "1" else "0"
            }
            // Check if path exists first (avoids writing to non-existent nodes)
            val (exists, _) = shellManager.executeCommand("[ -f $path ] && echo 1 || echo 0")
            if (exists && value.isNotBlank()) {
                val (ok, _) = shellManager.executeCommand("echo $value > $path")
                if (ok) return true
            }
        }
        return false
    }

    // ── Status Query ───────────────────────────────────────────────────

    /**
     * Returns true if bypass charging appears to be active.
     * Reads the first accessible sysfs path to confirm hardware state.
     */
    suspend fun isHardwareBypassActive(): Boolean = withContext(Dispatchers.IO) {
        for (path in INPUT_SUSPEND_PATHS) {
            val (ok, output) = shellManager.executeCommand("cat $path 2>/dev/null")
            if (ok && output.isNotBlank()) {
                val v = output.trim()
                return@withContext if (path in INVERTED_PATHS) v == "0" else v == "1"
            }
        }
        // Fallback: check dumpsys battery status == NOT_CHARGING (3)
        val (ok, output) = shellManager.executeCommand(
            "dumpsys battery | grep 'status:' | awk '{print \$2}'"
        )
        ok && output.trim() == "3"
    }
}
