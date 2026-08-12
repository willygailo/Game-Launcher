package com.gamebooster.app.feature.bypass_charging;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

public class OnePlusOppoBypassStrategy implements BypassChargingInterface {

    private static final String TAG = "OnePlusOppoBypass";
    private boolean isBypassActive = false;

    @Override
    public String enableBypassCharging() {
        Log.d(TAG, "Enabling OnePlus / OPPO / Realme Smart Bypass Charging...");

        StringBuilder results = new StringBuilder();

        String res1 = ShizukuExecutor.executeShizukuCommand("cmd settings put global oppo_charging_enabled 0");
        results.append("oppo_charging_enabled=0: ").append(res1).append("\n");

        String res2 = ShizukuExecutor.executeShizukuCommand("cmd settings put system bypass_charging 1");
        results.append("bypass_charging=1: ").append(res2).append("\n");

        String sysfs1 = ShizukuExecutor.executeShizukuCommand("echo 0 > /sys/class/power_supply/battery/mmi_charging_enable");
        results.append("mmi_charging_enable=0: ").append(sysfs1).append("\n");

        String sysfs2 = ShizukuExecutor.executeShizukuCommand("echo 1 > /sys/class/power_supply/battery/input_suspend");
        results.append("input_suspend=1: ").append(sysfs2).append("\n");

        isBypassActive = true;
        return results.toString().trim();
    }

    @Override
    public String disableBypassCharging() {
        Log.d(TAG, "Disabling OnePlus / OPPO / Realme Smart Bypass Charging...");

        StringBuilder results = new StringBuilder();

        String res1 = ShizukuExecutor.executeShizukuCommand("cmd settings put global oppo_charging_enabled 1");
        results.append("oppo_charging_enabled=1: ").append(res1).append("\n");

        String res2 = ShizukuExecutor.executeShizukuCommand("cmd settings put system bypass_charging 0");
        results.append("bypass_charging=0: ").append(res2).append("\n");

        String sysfs1 = ShizukuExecutor.executeShizukuCommand("echo 1 > /sys/class/power_supply/battery/mmi_charging_enable");
        results.append("mmi_charging_enable=1: ").append(sysfs1).append("\n");

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
        String checkSetting = ShizukuExecutor.executeShizukuCommand("cmd settings get global oppo_charging_enabled");
        String checkMmi = ShizukuExecutor.executeShizukuCommand("ls /sys/class/power_supply/battery/mmi_charging_enable");
        return (checkSetting != null && !checkSetting.contains("ERROR")) || (checkMmi != null && !checkMmi.contains("No such file"));
    }

    @Override
    public String getStrategyName() {
        return "OnePlus / OPPO / Realme ColorOS Smart Charge Bypass";
    }

    @Override
    public String getBypassStatus() {
        String settingVal = ShizukuExecutor.executeShizukuCommand("cmd settings get global oppo_charging_enabled");
        return "Strategy: " + getStrategyName() + " | Active: " + isBypassActive + " | oppo_charging_enabled: " + settingVal;
    }
}
