package com.gamebooster.app.booster.refreshrate;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class SamsungHzStrategy implements RefreshRateInterface {

    private static final String TAG = "SamsungHzStrategy";

    @Override
    public String forceRefreshRate(int targetHz) {
        Log.d(TAG, "Forcing Samsung OneUI refresh rate -> " + targetHz + "Hz");

        String hzF = targetHz + ".0";
        String hz = String.valueOf(targetHz);

        StringBuilder sb = new StringBuilder();

        sb.append("peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system peak_refresh_rate " + hzF)).append("\n");
        sb.append("min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system min_refresh_rate " + hzF)).append("\n");
        sb.append("user_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system user_refresh_rate " + hz)).append("\n");
        sb.append("refresh_rate_mode: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system refresh_rate_mode 2")).append("\n");

        sb.append("cmd window set-app-refresh-rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd window set-app-refresh-rate global " + targetHz)).append("\n");
        sb.append("cmd game set fps: ").append(ShizukuExecutor.executeShizukuCommand("cmd game set --fps " + targetHz + " global")).append("\n");

        return sb.toString().trim();
    }

    @Override
    public String resetRefreshRate() {
        Log.d(TAG, "Resetting Samsung OneUI refresh rate...");
        StringBuilder sb = new StringBuilder();
        sb.append("reset peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system peak_refresh_rate")).append("\n");
        sb.append("reset min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system min_refresh_rate")).append("\n");
        sb.append("reset refresh_rate_mode: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system refresh_rate_mode 1")).append("\n");
        return sb.toString().trim();
    }

    @Override
    public String getStrategyName() {
        return "Samsung OneUI Motion Smoothness Refresh Rate";
    }

    @Override
    public boolean isSupported() {
        return ShizukuExecutor.hasShizukuPermission();
    }
}
