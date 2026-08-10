package com.gamebooster.app.bypasscharging;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class GenericSysfsBypassStrategy implements BypassChargingInterface {

    private static final String TAG = "GenericSysfsBypass";
    private boolean isBypassActive = false;

    @Override
    public String enableBypassCharging() {
        Log.d(TAG, "Enabling Generic / Pixel Sysfs & ADB Battery Bypass Charging...");

        StringBuilder results = new StringBuilder();

        // 1. Sysfs multi-node attempt
        String sys1 = ShizukuExecutor.executeShizukuCommand("echo 0 > /sys/class/power_supply/battery/charging_enabled");
        results.append("charging_enabled=0: ").append(sys1).append("\n");

        String sys2 = ShizukuExecutor.executeShizukuCommand("echo 1 > /sys/class/power_supply/battery/input_suspend");
        results.append("input_suspend=1: ").append(sys2).append("\n");

        String sys3 = ShizukuExecutor.executeShizukuCommand("echo 1 > /sys/class/power_supply/battery/store_mode");
        results.append("store_mode=1: ").append(sys3).append("\n");

        String sys4 = ShizukuExecutor.executeShizukuCommand("echo 0 > /sys/class/power_supply/battery/charge_control_limit_max");
        results.append("charge_control_limit_max=0: ").append(sys4).append("\n");

        // 2. ADB Battery Subsystem HAL fallback (Software Direct Power / Mock)
        String bat1 = ShizukuExecutor.executeShizukuCommand("cmd battery set ac 1");
        results.append("cmd battery set ac 1: ").append(bat1).append("\n");

        String bat2 = ShizukuExecutor.executeShizukuCommand("dumpsys battery unplug");
        results.append("dumpsys battery unplug: ").append(bat2).append("\n");

        String bat3 = ShizukuExecutor.executeShizukuCommand("dumpsys battery set status 1");
        results.append("dumpsys battery status=1: ").append(bat3).append("\n");

        isBypassActive = true;
        return results.toString().trim();
    }

    @Override
    public String disableBypassCharging() {
        Log.d(TAG, "Disabling Generic Sysfs & ADB Battery Bypass Charging...");

        StringBuilder results = new StringBuilder();

        String sys1 = ShizukuExecutor.executeShizukuCommand("echo 1 > /sys/class/power_supply/battery/charging_enabled");
        results.append("charging_enabled=1: ").append(sys1).append("\n");

        String sys2 = ShizukuExecutor.executeShizukuCommand("echo 0 > /sys/class/power_supply/battery/input_suspend");
        results.append("input_suspend=0: ").append(sys2).append("\n");

        String sys3 = ShizukuExecutor.executeShizukuCommand("echo 0 > /sys/class/power_supply/battery/store_mode");
        results.append("store_mode=0: ").append(sys3).append("\n");

        // Reset ADB battery service
        String batReset = ShizukuExecutor.executeShizukuCommand("dumpsys battery reset");
        results.append("dumpsys battery reset: ").append(batReset).append("\n");

        isBypassActive = false;
        return results.toString().trim();
    }

    @Override
    public boolean isSupported() {
        return ShizukuExecutor.hasShizukuPermission();
    }

    @Override
    public String getStrategyName() {
        return "Generic Android Sysfs & ADB Battery HAL Fallback";
    }

    @Override
    public String getBypassStatus() {
        String dumpsysVal = ShizukuExecutor.executeShizukuCommand("dumpsys battery");
        return "Strategy: " + getStrategyName() + " | Active: " + isBypassActive + " | Dumpsys info: " +
                (dumpsysVal != null && dumpsysVal.length() > 100 ? dumpsysVal.substring(0, 100) + "..." : dumpsysVal);
    }
}
