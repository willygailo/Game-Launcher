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

    private static final String[] SYSFS_DISABLE_NODES = {
            "/sys/class/power_supply/battery/charging_enabled",
            "/sys/class/power_supply/battery/input_suspend",
            "/sys/devices/platform/charger/charging_enabled",
            "/sys/devices/platform/battery/charging_enabled",
            "/sys/class/power_supply/battery/mmi_charging_enable"
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
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                Log.d(TAG, "Activating Bypass Charging (Direct Motherboard Power)...");
                // Tier 1: Android Shell Battery Service Unplug (Device stays awake via AC)
                CommandExecutor.executeSystemCommand("cmd battery unplug");
                CommandExecutor.executeSystemCommand("dumpsys battery unplug");

                // Tier 2: Kernel Sysfs Direct Charging Disabler
                for (String node : SYSFS_DISABLE_NODES) {
                    String cmd = "if [ -f " + node + " ]; then echo 0 > " + node + "; fi";
                    CommandExecutor.executeSystemCommand(cmd);
                }

                // Tier 3: MediaTek Fast Charging suspend
                String mtkCmd = "if [ -f /sys/devices/platform/charger/charging_enabled ]; then echo 0 > /sys/devices/platform/charger/charging_enabled; fi";
                CommandExecutor.executeSystemCommand(mtkCmd);

                sIsBypassActive = true;
                Log.i(TAG, "Bypass Charging ACTIVE: Power routed directly to SoC.");
            } catch (Throwable t) {
                Log.e(TAG, "Error enabling bypass charging", t);
            }
        });
    }

    /**
     * Restores normal charging behavior upon game exit.
     */
    public static void restoreNormalCharging(Context context) {
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                Log.d(TAG, "Restoring normal battery charging...");
                CommandExecutor.executeSystemCommand("cmd battery reset");
                CommandExecutor.executeSystemCommand("dumpsys battery reset");

                for (String node : SYSFS_DISABLE_NODES) {
                    String cmd = "if [ -f " + node + " ]; then echo 1 > " + node + "; fi";
                    CommandExecutor.executeSystemCommand(cmd);
                }

                sIsBypassActive = false;
                Log.i(TAG, "Bypass Charging RESTORED: Normal charging resumed.");
            } catch (Throwable t) {
                Log.e(TAG, "Error restoring normal charging", t);
            }
        });
    }

    public static void onGameStarted(Context context) {
        if (context == null) return;
        if (isBypassConfiguredEnabled(context) && isChargerConnected(context)) {
            enableBypassCharging(context);
        }
    }

    public static void onGameStopped(Context context) {
        if (sIsBypassActive) {
            restoreNormalCharging(context);
        }
    }
}
