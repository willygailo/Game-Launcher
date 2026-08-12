package com.gamebooster.app.feature.performance.refreshrate;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

public class OnePlusOppoHzStrategy implements RefreshRateInterface {

    private static final String TAG = "OnePlusOppoHzStrategy";

    @Override
    public String forceRefreshRate(int targetHz) {
        Log.d(TAG, "Forcing OnePlus / OPPO / Realme refresh rate -> " + targetHz + "Hz");

        String hz = String.valueOf(targetHz);
        StringBuilder sb = new StringBuilder();

        sb.append("oneplus_screen_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put global oneplus_screen_refresh_rate " + hz)).append("\n");
        sb.append("oppo_display_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system oppo_display_refresh_rate " + hz)).append("\n");
        sb.append("cmd window set-app-refresh-rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd window set-app-refresh-rate global " + targetHz)).append("\n");

        return sb.toString().trim();
    }

    @Override
    public String resetRefreshRate() {
        Log.d(TAG, "Resetting OnePlus / OPPO refresh rate...");
        return ShizukuExecutor.executeShizukuCommand("cmd settings delete global oneplus_screen_refresh_rate");
    }

    @Override
    public String getStrategyName() {
        return "OnePlus / OPPO / Realme ColorOS Refresh Rate";
    }

    @Override
    public boolean isSupported() {
        return ShizukuExecutor.hasShizukuPermission();
    }
}
