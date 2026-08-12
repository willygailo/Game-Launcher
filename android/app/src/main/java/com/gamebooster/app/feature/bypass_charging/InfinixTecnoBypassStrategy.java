package com.gamebooster.app.feature.bypass_charging;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

public class InfinixTecnoBypassStrategy implements BypassChargingInterface {

    private static final String TAG = "InfinixTecnoBypass";
    private boolean isBypassActive = false;

    @Override
    public String enableBypassCharging() {
        Log.d(TAG, "Enabling Infinix / Tecno Transsion Bypass Charge...");

        StringBuilder results = new StringBuilder();

        String res1 = ShizukuExecutor.executeShizukuCommand("cmd settings put global bypass_charge_enable 1");
        results.append("bypass_charge_enable=1: ").append(res1).append("\n");

        String res2 = ShizukuExecutor.executeShizukuCommand("cmd settings put system bypass_charge 1");
        results.append("bypass_charge=1: ").append(res2).append("\n");

        String res3 = ShizukuExecutor.executeShizukuCommand("cmd settings put global transsion_bypass_charge 1");
        results.append("transsion_bypass_charge=1: ").append(res3).append("\n");

        String sysfs1 = ShizukuExecutor.executeShizukuCommand("echo 1 > /sys/class/power_supply/battery/bypass_mode");
        results.append("bypass_mode=1: ").append(sysfs1).append("\n");

        String sysfs2 = ShizukuExecutor.executeShizukuCommand("echo 1 > /sys/class/power_supply/battery/input_suspend");
        results.append("input_suspend=1: ").append(sysfs2).append("\n");

        isBypassActive = true;
        return results.toString().trim();
    }

    @Override
    public String disableBypassCharging() {
        Log.d(TAG, "Disabling Infinix / Tecno Bypass Charge...");

        StringBuilder results = new StringBuilder();

        String res1 = ShizukuExecutor.executeShizukuCommand("cmd settings put global bypass_charge_enable 0");
        results.append("bypass_charge_enable=0: ").append(res1).append("\n");

        String res2 = ShizukuExecutor.executeShizukuCommand("cmd settings put system bypass_charge 0");
        results.append("bypass_charge=0: ").append(res2).append("\n");

        String res3 = ShizukuExecutor.executeShizukuCommand("cmd settings put global transsion_bypass_charge 0");
        results.append("transsion_bypass_charge=0: ").append(res3).append("\n");

        String sysfs1 = ShizukuExecutor.executeShizukuCommand("echo 0 > /sys/class/power_supply/battery/bypass_mode");
        results.append("bypass_mode=0: ").append(sysfs1).append("\n");

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
        String checkSetting = ShizukuExecutor.executeShizukuCommand("cmd settings get global bypass_charge_enable");
        String checkSysfs = ShizukuExecutor.executeShizukuCommand("ls /sys/class/power_supply/battery/bypass_mode");
        return (checkSetting != null && !checkSetting.contains("ERROR")) || (checkSysfs != null && !checkSysfs.contains("No such file"));
    }

    @Override
    public String getStrategyName() {
        return "Infinix XOS / Tecno HiOS Bypass Charge";
    }

    @Override
    public String getBypassStatus() {
        String settingVal = ShizukuExecutor.executeShizukuCommand("cmd settings get global bypass_charge_enable");
        return "Strategy: " + getStrategyName() + " | Active: " + isBypassActive + " | bypass_charge_enable: " + settingVal;
    }
}
