package com.gamebooster.app.booster.thermal;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class SamsungThermalStrategy implements ThermalInterface {

    private static final String TAG = "SamsungThermalStrategy";

    @Override
    public String applyThermalOptimization() {
        Log.d(TAG, "Applying Samsung OneUI GOS Thermal Throttling Mitigation...");

        StringBuilder sb = new StringBuilder();
        sb.append("disable GOS auto control: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put secure game_auto_temperature_control 0")).append("\n");
        sb.append("clear GOS cache: ").append(ShizukuExecutor.executeShizukuCommand("pm clear com.samsung.android.game.gos")).append("\n");
        sb.append("disable thermal limit prop: ").append(ShizukuExecutor.executeShizukuCommand("setprop persist.sys.thermal.mitigation 0")).append("\n");

        return sb.toString().trim();
    }

    @Override
    public String resetThermalSettings() {
        Log.d(TAG, "Resetting Samsung Thermal Throttling...");
        return ShizukuExecutor.executeShizukuCommand("cmd settings put secure game_auto_temperature_control 1");
    }

    @Override
    public String getStrategyName() {
        return "Samsung OneUI GOS Thermal Control Mitigation";
    }

    @Override
    public boolean isSupported() {
        return ShizukuExecutor.hasShizukuPermission();
    }
}
