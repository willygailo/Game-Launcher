package com.gamebooster.app.bypasscharging;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class RedMagicBypassStrategy implements BypassChargingInterface {

    private static final String TAG = "RedMagicBypassStrategy";
    private boolean isBypassActive = false;

    @Override
    public String enableBypassCharging() {
        Log.d(TAG, "Enabling RedMagic / Nubia Charge Separation...");

        StringBuilder results = new StringBuilder();

        String res1 = ShizukuExecutor.executeShizukuCommand("cmd settings put global nubia_charge_bypass 1");
        results.append("nubia_charge_bypass=1: ").append(res1).append("\n");

        String res2 = ShizukuExecutor.executeShizukuCommand("cmd settings put system charge_separation 1");
        results.append("charge_separation=1: ").append(res2).append("\n");

        String sysfs = ShizukuExecutor.executeShizukuCommand("echo 0 > /sys/class/power_supply/battery/charging_enabled");
        results.append("charging_enabled=0: ").append(sysfs).append("\n");

        isBypassActive = true;
        return results.toString().trim();
    }

    @Override
    public String disableBypassCharging() {
        Log.d(TAG, "Disabling RedMagic / Nubia Charge Separation...");

        StringBuilder results = new StringBuilder();

        String res1 = ShizukuExecutor.executeShizukuCommand("cmd settings put global nubia_charge_bypass 0");
        results.append("nubia_charge_bypass=0: ").append(res1).append("\n");

        String res2 = ShizukuExecutor.executeShizukuCommand("cmd settings put system charge_separation 0");
        results.append("charge_separation=0: ").append(res2).append("\n");

        String sysfs = ShizukuExecutor.executeShizukuCommand("echo 1 > /sys/class/power_supply/battery/charging_enabled");
        results.append("charging_enabled=1: ").append(sysfs).append("\n");

        isBypassActive = false;
        return results.toString().trim();
    }

    @Override
    public boolean isSupported() {
        if (!ShizukuExecutor.hasShizukuPermission()) {
            return false;
        }
        String checkSetting = ShizukuExecutor.executeShizukuCommand("cmd settings get global nubia_charge_bypass");
        return checkSetting != null && !checkSetting.contains("ERROR");
    }

    @Override
    public String getStrategyName() {
        return "RedMagic Nubia Charge Separation";
    }

    @Override
    public String getBypassStatus() {
        String statusVal = ShizukuExecutor.executeShizukuCommand("cmd settings get global nubia_charge_bypass");
        return "Strategy: " + getStrategyName() + " | Active: " + isBypassActive + " | nubia_charge_bypass: " + statusVal;
    }
}
