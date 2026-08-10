package com.gamebooster.app.bypasscharging;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class SonyBypassStrategy implements BypassChargingInterface {

    private static final String TAG = "SonyBypassStrategy";
    private boolean isBypassActive = false;

    @Override
    public String enableBypassCharging() {
        Log.d(TAG, "Enabling Sony Xperia H.S. Power Control...");

        StringBuilder results = new StringBuilder();

        String res1 = ShizukuExecutor.executeShizukuCommand("am broadcast -a com.sonymobile.gameenhancer.intent.action.POWER_PAUSE --ei state 1");
        results.append("Sony H.S. Intent=1: ").append(res1).append("\n");

        String res2 = ShizukuExecutor.executeShizukuCommand("cmd settings put system hs_power_control 1");
        results.append("hs_power_control=1: ").append(res2).append("\n");

        String sysfs1 = ShizukuExecutor.executeShizukuCommand("echo 0 > /sys/class/power_supply/battery/charging_enabled");
        results.append("charging_enabled=0: ").append(sysfs1).append("\n");

        String sysfs2 = ShizukuExecutor.executeShizukuCommand("echo 1 > /sys/class/power_supply/battery/input_suspend");
        results.append("input_suspend=1: ").append(sysfs2).append("\n");

        isBypassActive = true;
        return results.toString().trim();
    }

    @Override
    public String disableBypassCharging() {
        Log.d(TAG, "Disabling Sony Xperia H.S. Power Control...");

        StringBuilder results = new StringBuilder();

        String res1 = ShizukuExecutor.executeShizukuCommand("am broadcast -a com.sonymobile.gameenhancer.intent.action.POWER_PAUSE --ei state 0");
        results.append("Sony H.S. Intent=0: ").append(res1).append("\n");

        String res2 = ShizukuExecutor.executeShizukuCommand("cmd settings put system hs_power_control 0");
        results.append("hs_power_control=0: ").append(res2).append("\n");

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
        String checkHs = ShizukuExecutor.executeShizukuCommand("cmd settings get system hs_power_control");
        return checkHs != null && !checkHs.contains("ERROR");
    }

    @Override
    public String getStrategyName() {
        return "Sony Xperia H.S. (Heat Suppression) Power Control";
    }

    @Override
    public String getBypassStatus() {
        String hsVal = ShizukuExecutor.executeShizukuCommand("cmd settings get system hs_power_control");
        return "Strategy: " + getStrategyName() + " | Active: " + isBypassActive + " | hs_power_control: " + hsVal;
    }
}
