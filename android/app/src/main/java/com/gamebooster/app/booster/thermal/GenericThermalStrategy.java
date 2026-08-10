package com.gamebooster.app.booster.thermal;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class GenericThermalStrategy implements ThermalInterface {

    private static final String TAG = "GenericThermalStrategy";

    @Override
    public String applyThermalOptimization() {
        Log.d(TAG, "Applying Generic AOSP Fixed Performance Mode Thermal Optimization...");

        StringBuilder sb = new StringBuilder();
        sb.append("fixed performance mode: ").append(ShizukuExecutor.executeShizukuCommand("cmd power set-fixed-performance-mode-enabled true")).append("\n");
        sb.append("thermal engine prop: ").append(ShizukuExecutor.executeShizukuCommand("setprop set.thermal.engine 0")).append("\n");

        return sb.toString().trim();
    }

    @Override
    public String resetThermalSettings() {
        Log.d(TAG, "Resetting Generic Thermal Throttling...");
        return ShizukuExecutor.executeShizukuCommand("cmd power set-fixed-performance-mode-enabled false");
    }

    @Override
    public String getStrategyName() {
        return "Generic AOSP Fixed Performance Mode";
    }

    @Override
    public boolean isSupported() {
        return ShizukuExecutor.hasShizukuPermission();
    }
}
