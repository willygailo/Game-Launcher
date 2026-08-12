package com.gamebooster.app.feature.performance.thermal;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

public class AsusRogThermalStrategy implements ThermalInterface {

    private static final String TAG = "AsusRogThermalStrategy";

    @Override
    public String applyThermalOptimization() {
        Log.d(TAG, "Applying Asus ROG X-Mode Thermal Threshold Optimization...");

        StringBuilder sb = new StringBuilder();
        sb.append("asus xmode thermal limit: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system asus_xmode_thermal 0")).append("\n");
        sb.append("asus performance thermal: ").append(ShizukuExecutor.executeShizukuCommand("setprop persist.sys.asus.thermal 0")).append("\n");

        return sb.toString().trim();
    }

    @Override
    public String resetThermalSettings() {
        Log.d(TAG, "Resetting Asus ROG Thermal Throttling...");
        return ShizukuExecutor.executeShizukuCommand("cmd settings put system asus_xmode_thermal 1");
    }

    @Override
    public String getStrategyName() {
        return "Asus ROG X-Mode Thermal Optimization";
    }

    @Override
    public boolean isSupported() {
        return ShizukuExecutor.hasShizukuPermission();
    }
}
