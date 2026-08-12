package com.gamebooster.app.feature.performance.thermal;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

public class XiaomiThermalStrategy implements ThermalInterface {

    private static final String TAG = "XiaomiThermalStrategy";

    @Override
    public String applyThermalOptimization() {
        Log.d(TAG, "Applying Xiaomi HyperOS Joyose Thermal Throttling Mitigation...");

        StringBuilder sb = new StringBuilder();
        sb.append("disable joyose thermal limit: ").append(ShizukuExecutor.executeShizukuCommand("setprop persist.sys.thermal.limit 0")).append("\n");
        sb.append("disable thermal limit prop: ").append(ShizukuExecutor.executeShizukuCommand("setprop sys.thermal.enable 0")).append("\n");
        sb.append("appops Joyose background: ").append(ShizukuExecutor.executeShizukuCommand("cmd appops set com.xiaomi.joyose RUN_IN_BACKGROUND ignore")).append("\n");

        return sb.toString().trim();
    }

    @Override
    public String resetThermalSettings() {
        Log.d(TAG, "Resetting Xiaomi Thermal Throttling...");
        return ShizukuExecutor.executeShizukuCommand("setprop sys.thermal.enable 1");
    }

    @Override
    public String getStrategyName() {
        return "Xiaomi HyperOS Joyose Thermal Control Mitigation";
    }

    @Override
    public boolean isSupported() {
        return ShizukuExecutor.hasShizukuPermission();
    }
}
