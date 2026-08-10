package com.gamebooster.app.bypasscharging;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class AsusRogBypassStrategy implements BypassChargingInterface {

    private static final String TAG = "AsusRogBypassStrategy";
    private boolean isBypassActive = false;

    @Override
    public String enableBypassCharging() {
        Log.d(TAG, "Enabling Asus ROG Bypass Charging...");

        StringBuilder results = new StringBuilder();

        String res1 = ShizukuExecutor.executeShizukuCommand("cmd settings put system bypass_charging 1");
        results.append("bypass_charging=1: ").append(res1).append("\n");

        String res2 = ShizukuExecutor.executeShizukuCommand("cmd settings put global asus_bypass_charging 1");
        results.append("asus_bypass_charging=1: ").append(res2).append("\n");

        String res3 = ShizukuExecutor.executeShizukuCommand("cmd settings put system bypass_mode 1");
        results.append("bypass_mode=1: ").append(res3).append("\n");

        String sysfs1 = ShizukuExecutor.executeShizukuCommand("echo 0 > /sys/class/power_supply/battery/charging_enabled");
        results.append("charging_enabled=0: ").append(sysfs1).append("\n");

        String sysfs2 = ShizukuExecutor.executeShizukuCommand("echo 1 > /sys/class/power_supply/battery/input_suspend");
        results.append("input_suspend=1: ").append(sysfs2).append("\n");

        isBypassActive = true;
        return results.toString().trim();
    }

    @Override
    public String disableBypassCharging() {
        Log.d(TAG, "Disabling Asus ROG Bypass Charging...");

        StringBuilder results = new StringBuilder();

        String res1 = ShizukuExecutor.executeShizukuCommand("cmd settings put system bypass_charging 0");
        results.append("bypass_charging=0: ").append(res1).append("\n");

        String res2 = ShizukuExecutor.executeShizukuCommand("cmd settings put global asus_bypass_charging 0");
        results.append("asus_bypass_charging=0: ").append(res2).append("\n");

        String res3 = ShizukuExecutor.executeShizukuCommand("cmd settings put system bypass_mode 0");
        results.append("bypass_mode=0: ").append(res3).append("\n");

        String sysfs1 = ShizukuExecutor.executeShizukuCommand("echo 1 > /sys/class/power_supply/battery/charging_enabled");
        results.append("charging_enabled=1: ").append(sysfs1).append("\n");

        String sysfs2 = ShizukuExecutor.executeShizukuCommand("echo 0 > /sys/class/power_supply/battery/input_suspend");
        results.append("input_suspend=0: ").append(sysfs2).append("\n");

        isBypassActive = false;
        return results.toString().trim();
    }

    @Override
    public boolean isSupported() {
        if (!ShizukuExecutor.hasShizukuPermission()) {
            return false;
        }
        String checkSetting = ShizukuExecutor.executeShizukuCommand("cmd settings get system bypass_charging");
        return checkSetting != null && !checkSetting.contains("ERROR");
    }

    @Override
    public String getStrategyName() {
        return "Asus ROG Phone Game Genie Direct Power Supply";
    }

    @Override
    public String getBypassStatus() {
        String bypassVal = ShizukuExecutor.executeShizukuCommand("cmd settings get system bypass_charging");
        return "Strategy: " + getStrategyName() + " | Active: " + isBypassActive + " | bypass_charging: " + bypassVal;
    }
}
