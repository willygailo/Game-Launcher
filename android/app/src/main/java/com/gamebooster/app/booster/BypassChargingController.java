package com.gamebooster.app.booster;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * BypassChargingController — Direct Motherboard Power & Battery Thermal Shield.
 *
 * Modeled after flagship OEM Game Space charge separation engines (Tecno, ROG, RedMagic):
 * When gaming while connected to a charger, decouples current from entering battery cells,
 * feeding power directly to the SoC. Eliminates battery heat, stops thermal throttling,
 * and maintains maximum CPU/GPU clock frequencies without battery wear.
 */
public final class BypassChargingController {

    private static final String TAG = "BypassChargingCtrl";
    private static final String PREF_NAME = "game_bypass_charging_prefs";
    private static final String KEY_BYPASS_ENABLED = "bypass_charging_enabled";

    private static volatile boolean sIsBypassActive = false;

    private static final String[] SYSFS_CHARGING_NODES = {
            "/sys/class/power_supply/battery/charging_enabled",
            "/sys/devices/platform/charger/charging_enabled",
            "/sys/devices/platform/battery/charging_enabled",
            "/sys/class/power_supply/battery/mmi_charging_enable",
            "/sys/class/power_supply/battery/battery_charging_enabled",
            "/sys/class/power_supply/bms/charging_enabled"
    };

    private static final String[] SYSFS_INPUT_SUSPEND_NODES = {
            "/sys/class/power_supply/battery/input_suspend"
    };

    private static final String[] SYSFS_LIMIT_MAX_NODES = {
            "/sys/class/power_supply/battery/charge_control_limit_max",
            "/sys/class/power_supply/bms/charge_control_limit_max"
    };

    private BypassChargingController() {}

    public static boolean isBypassConfiguredEnabled(Context context) {
        if (context == null) return false;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_BYPASS_ENABLED, false);
    }

    public static void setBypassConfiguredEnabled(Context context, boolean enabled) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_BYPASS_ENABLED, enabled).apply();
    }

    public static boolean isBypassCurrentlyActive() {
        return sIsBypassActive;
    }

    public static boolean isChargerConnected(Context context) {
        if (context == null) return false;
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            if (batteryStatus == null) return false;
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            boolean isPlugged = chargePlug == BatteryManager.BATTERY_PLUGGED_AC ||
                    chargePlug == BatteryManager.BATTERY_PLUGGED_USB ||
                    chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS;
            return isCharging || isPlugged;
        } catch (Throwable t) {
            Log.w(TAG, "Failed reading battery status: " + t.getMessage());
            return false;
        }
    }

    /**
     * Activates charge separation / bypass charging.
     */
    public static void enableBypassCharging(Context context) {
        // Disabled per user request to avoid battery charging issues / stuck percentage.
        // Always enforce normal physical charging.
        restoreNormalCharging(context);
    }

    /**
     * Restores normal charging behavior upon game exit or emergency reset.
     */
    public static void restoreNormalCharging(Context context) {
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                Log.d(TAG, "Restoring normal battery charging...");
                // Tier 1: Reset Android Battery Framework
                CommandExecutor.executeSystemCommand("cmd battery reset");
                CommandExecutor.executeSystemCommand("dumpsys battery reset");

                // Tier 2: Enable Charging in Kernel (1 = enable charging)
                for (String node : SYSFS_CHARGING_NODES) {
                    String cmd = "if [ -f " + node + " ]; then echo 1 > " + node + "; fi";
                    CommandExecutor.executeSystemCommand(cmd);
                }

                // Tier 3: Un-suspend input power (0 = normal charging)
                for (String node : SYSFS_INPUT_SUSPEND_NODES) {
                    String cmd = "if [ -f " + node + " ]; then echo 0 > " + node + "; fi";
                    CommandExecutor.executeSystemCommand(cmd);
                }

                // Tier 4: Restore MTK charger
                String mtkCmd = "if [ -f /sys/devices/platform/charger/charging_enabled ]; then echo 1 > /sys/devices/platform/charger/charging_enabled; fi";
                CommandExecutor.executeSystemCommand(mtkCmd);

                // Tier 5: Reset charge control limit to 100% and clear protect_battery limits
                for (String node : SYSFS_LIMIT_MAX_NODES) {
                    String cmd = "if [ -f " + node + " ]; then echo 100 > " + node + "; fi";
                    CommandExecutor.executeSystemCommand(cmd);
                }
                CommandExecutor.executeSystemCommand("settings put global protect_battery 0 2>/dev/null");
                CommandExecutor.executeSystemCommand("settings put global charge_control_limit 100 2>/dev/null");

                sIsBypassActive = false;
                Log.i(TAG, "Bypass Charging RESTORED: Normal charging resumed.");
            } catch (Throwable t) {
                Log.e(TAG, "Error restoring normal charging", t);
            }
        });
    }

    /**
     * Universal Emergency Battery Reset: Unconditionally forces Android framework and
     * Linux kernel charging circuits to normal charging mode. Safe to call anytime.
     */
    public static void resetBatterySafety(Context context) {
        restoreNormalCharging(context);
        if (context != null) {
            setBypassConfiguredEnabled(context, false);
            try {
                com.gamebooster.app.config.TweakPreferences.saveTweakState(context, "bypass_charging_shield", false);
            } catch (Throwable ignored) {}
        }
    }

    public static void onGameStarted(Context context) {
        if (context == null) return;
        if (isBypassConfiguredEnabled(context) && isChargerConnected(context)) {
            enableBypassCharging(context);
        }
    }

    public static void onGameStopped(Context context) {
        restoreNormalCharging(context);
    }
}
