// app/src/main/java/com/gamelauncher/core/BatterySaverManager.kt
package com.gamelauncher.core

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.gamelauncher.core.permissions.RuntimePermissionManager
import com.gamelauncher.core.shizuku.IShellExecutor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BatterySaverManager — Manages power saver state and optimization exemption.
 * Non-root policy: All privileged writes route via Shizuku IShellExecutor AIDL.
 */
@Singleton
class BatterySaverManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shellExecutor: IShellExecutor,
    private val runtimePermissionManager: RuntimePermissionManager
) {

    private val _isBatterySaverActive = MutableStateFlow(false)
    val isBatterySaverActive: StateFlow<Boolean> = _isBatterySaverActive

    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging

    @Volatile private var originalLowPower: Int? = null
    @Volatile private var originalAdaptiveBattery: Int? = null
    @Volatile private var wasDisabledByUs: Boolean = false

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

    suspend fun disableBatterySaver(): Boolean = withContext(Dispatchers.IO) {
        var anySuccess = false
        wasDisabledByUs = false

        if (!isBatterySaverCurrentlyOn()) {
            originalLowPower = 0
            return@withContext true
        }

        refreshBatteryStatus()

        try {
            originalLowPower = Settings.Global.getInt(context.contentResolver, "low_power", 0)
            originalAdaptiveBattery = Settings.Global.getInt(context.contentResolver, "adaptive_battery_management_enabled", 1)

            val r1 = Settings.Global.putInt(context.contentResolver, "low_power", 0)
            Settings.Global.putInt(context.contentResolver, "adaptive_battery_management_enabled", 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Settings.Global.putInt(context.contentResolver, "low_power_sticky", 0)
                Settings.Global.putInt(context.contentResolver, "automatic_power_saver_mode", 0)
            }
            if (r1) {
                anySuccess = true
                wasDisabledByUs = true
            }
        } catch (_: Exception) {}

        if (!anySuccess) {
            val ok = shellExecutor.writeSetting("global", "low_power", "0")
            if (ok) {
                anySuccess = true
                wasDisabledByUs = true
            }
        }

        val finalCheck = try {
            val pm = context.getSystemService(PowerManager::class.java)
            pm?.isPowerSaveMode ?: false
        } catch (_: Exception) { false }

        _isBatterySaverActive.value = finalCheck
        anySuccess
    }

    suspend fun restoreBatterySaver() = withContext(Dispatchers.IO) {
        if (!wasDisabledByUs) return@withContext

        try {
            originalLowPower?.let { Settings.Global.putInt(context.contentResolver, "low_power", it) }
            originalAdaptiveBattery?.let { Settings.Global.putInt(context.contentResolver, "adaptive_battery_management_enabled", it) }
        } catch (_: Exception) {}

        shellExecutor.writeSetting("global", "low_power", (originalLowPower ?: 0).toString())

        originalLowPower = null
        originalAdaptiveBattery = null
        wasDisabledByUs = false
        _isBatterySaverActive.value = isBatterySaverCurrentlyOn()
    }

    fun getBatteryOptimizationExemptionIntent(): Intent? {
        return try {
            val pm = context.getSystemService(PowerManager::class.java) ?: return null
            val pkg = context.packageName
            if (!pm.isIgnoringBatteryOptimizations(pkg)) {
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = android.net.Uri.parse("package:$pkg")
                }
            } else null
        } catch (_: Exception) { null }
    }

    fun isIgnoringBatteryOptimizations(): Boolean {
        return try {
            val pm = context.getSystemService(PowerManager::class.java) ?: return false
            pm.isIgnoringBatteryOptimizations(context.packageName)
        } catch (_: Exception) { false }
    }

    suspend fun whitelistGameFromDoze(packageName: String): Boolean = withContext(Dispatchers.IO) {
        runtimePermissionManager.grantPermissionViaShizuku("android.permission.PACKAGE_USAGE_STATS")
    }

    suspend fun suspendThermalThrottling(): Boolean = withContext(Dispatchers.IO) {
        shellExecutor.setThermalOverride(true)
    }

    suspend fun restoreThermalThrottling(): Boolean = withContext(Dispatchers.IO) {
        shellExecutor.setThermalOverride(false)
    }
}
