package com.gamebooster.app.bypasscharging;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class XiaomiBypassStrategy implements BypassChargingInterface {

    private static final String TAG = "XiaomiBypassStrategy";
    private boolean isBypassActive = false;

    @Override
    public String enableBypassCharging() {
        Log.d(TAG, "Enabling Xiaomi / POCO / Black Shark Bypass Charging...");

        StringBuilder results = new StringBuilder();

        // 1. MIUI / HyperOS Game Turbo Setting Toggle
        String res1 = ShizukuExecutor.executeShizukuCommand("cmd settings put system game_turbo_bypass_charge 1");
        results.append("game_turbo_bypass_charge=1: ").append(res1).append("\n");

        // 2. Vendor system property override
        String res2 = ShizukuExecutor.executeShizukuCommand("setprop persist.vendor.charge.bypass 1");
        results.append("persist.vendor.charge.bypass=1: ").append(res2).append("\n");

        // 3. Sysfs nodes for Xiaomi & Qualcomm battery PMIC
        String sysfs1 = ShizukuExecutor.executeShizukuCommand("echo 1 > /sys/class/power_supply/battery/input_suspend");
        results.append("input_suspend=1: ").append(sysfs1).append("\n");

        String sysfs2 = ShizukuExecutor.executeShizukuCommand("echo 1 > /sys/class/qcom-battery/bypass_charging_enable");
        results.append("bypass_charging_enable=1: ").append(sysfs2).append("\n");

        String sysfs3 = ShizukuExecutor.executeShizukuCommand("echo 0 > /sys/class/power_supply/battery/charging_enabled");
        results.append("charging_enabled=0: ").append(sysfs3).append("\n");

        isBypassActive = true;
        return results.toString().trim();
    }

    @Override
    public String disableBypassCharging() {
        Log.d(TAG, "Disabling Xiaomi Bypass Charging...");

        StringBuilder results = new StringBuilder();

        String res1 = ShizukuExecutor.executeShizukuCommand("cmd settings put system game_turbo_bypass_charge 0");
        results.append("game_turbo_bypass_charge=0: ").append(res1).append("\n");

        String res2 = ShizukuExecutor.executeShizukuCommand("setprop persist.vendor.charge.bypass 0");
        results.append("persist.vendor.charge.bypass=0: ").append(res2).append("\n");

        String sysfs1 = ShizukuExecutor.executeShizukuCommand("echo 0 > /sys/class/power_supply/battery/input_suspend");
        results.append("input_suspend=0: ").append(sysfs1).append("\n");

        String sysfs2 = ShizukuExecutor.executeShizukuCommand("echo 0 > /sys/class/qcom-battery/bypass_charging_enable");
        results.append("bypass_charging_enable=0: ").append(sysfs2).append("\n");

        String sysfs3 = ShizukuExecutor.executeShizukuCommand("echo 1 > /sys/class/power_supply/battery/charging_enabled");
        results.append("charging_enabled=1: ").append(sysfs3).append("\n");

        isBypassActive = false;
        return results.toString().trim();
    }

    @Override
    public boolean isSupported() {
        if (!ShizukuExecutor.hasShizukuPermission()) {
            return false;
        }
        String checkInputSuspend = ShizukuExecutor.executeShizukuCommand("ls /sys/class/power_supply/battery/input_suspend");
        String checkQcom = ShizukuExecutor.executeShizukuCommand("ls /sys/class/qcom-battery/bypass_charging_enable");
        return (checkInputSuspend != null && !checkInputSuspend.contains("No such file")) ||
               (checkQcom != null && !checkQcom.contains("No such file"));
    }

    @Override
    public String getStrategyName() {
        return "Xiaomi HyperOS / Game Turbo Smart Charge";
    }

    @Override
    public String getBypassStatus() {
        String inputSuspendVal = ShizukuExecutor.executeShizukuCommand("cat /sys/class/power_supply/battery/input_suspend");
        return "Strategy: " + getStrategyName() + " | Active: " + isBypassActive + " | input_suspend: " + inputSuspendVal;
    }
}
