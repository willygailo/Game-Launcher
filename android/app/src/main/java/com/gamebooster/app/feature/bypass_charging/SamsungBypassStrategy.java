package com.gamebooster.app.feature.bypass_charging;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

public class SamsungBypassStrategy implements BypassChargingInterface {

    private static final String TAG = "SamsungBypassStrategy";
    private boolean isBypassActive = false;

    @Override
    public String enableBypassCharging() {
        Log.d(TAG, "Enabling Samsung Pause USB Power Delivery / Bypass Charging...");

        StringBuilder results = new StringBuilder();
        
        // 1. Samsung Game Booster "Pause USB Power Delivery" toggle
        String res1 = ShizukuExecutor.executeShizukuCommand("cmd settings put system pass_through 1");
        results.append("pass_through=1: ").append(res1).append("\n");

        String res2 = ShizukuExecutor.executeShizukuCommand("cmd settings put global pause_pd_charging 1");
        results.append("pause_pd_charging=1: ").append(res2).append("\n");

        String res3 = ShizukuExecutor.executeShizukuCommand("cmd settings put global bypass_charging 1");
        results.append("bypass_charging=1: ").append(res3).append("\n");

        // 2. Kernel Slate Mode (Bypass mode on Exynos / Snapdragon Samsung PMICs)
        String sysfsRes = ShizukuExecutor.executeShizukuCommand("echo 1 > /sys/class/power_supply/battery/batt_slate_mode");
        results.append("batt_slate_mode=1: ").append(sysfsRes).append("\n");

        isBypassActive = true;
        return results.toString().trim();
    }

    @Override
    public String disableBypassCharging() {
        Log.d(TAG, "Disabling Samsung Bypass Charging...");

        StringBuilder results = new StringBuilder();

        String res1 = ShizukuExecutor.executeShizukuCommand("cmd settings put system pass_through 0");
        results.append("pass_through=0: ").append(res1).append("\n");

        String res2 = ShizukuExecutor.executeShizukuCommand("cmd settings put global pause_pd_charging 0");
        results.append("pause_pd_charging=0: ").append(res2).append("\n");

        String res3 = ShizukuExecutor.executeShizukuCommand("cmd settings put global bypass_charging 0");
        results.append("bypass_charging=0: ").append(res3).append("\n");

        String sysfsRes = ShizukuExecutor.executeShizukuCommand("echo 0 > /sys/class/power_supply/battery/batt_slate_mode");
        results.append("batt_slate_mode=0: ").append(sysfsRes).append("\n");

        isBypassActive = false;
        return results.toString().trim();
    }

    @Override
    public boolean isSupported() {
        if (!ShizukuExecutor.hasShizukuPermission()) {
            return false;
        }
        String checkSetting = ShizukuExecutor.executeShizukuCommand("cmd settings get system pass_through");
        String checkSysfs = ShizukuExecutor.executeShizukuCommand("ls /sys/class/power_supply/battery/batt_slate_mode");
        return (checkSetting != null && !checkSetting.contains("ERROR")) || (checkSysfs != null && !checkSysfs.contains("No such file"));
    }

    @Override
    public String getStrategyName() {
        return "Samsung OneUI Pause USB Power Delivery";
    }

    @Override
    public String getBypassStatus() {
        String passThroughStatus = ShizukuExecutor.executeShizukuCommand("cmd settings get system pass_through");
        return "Strategy: " + getStrategyName() + " | Active: " + isBypassActive + " | pass_through: " + passThroughStatus;
    }
}
