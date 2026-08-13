package com.gamebooster.app.feature.performance.refreshrate;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

/**
 * Refresh rate strategy tailored for Honor MagicOS and Huawei EMUI / HarmonyOS devices.
 * Forces display refresh rate limits and SurfaceFlinger settings via privileged Shizuku IPC.
 */
public class HonorHuaweiHzStrategy implements RefreshRateInterface {

    private static final String TAG = "HonorHuaweiHzStrategy";

    @Override
    public String forceRefreshRate(int targetHz) {
        Log.d(TAG, "Forcing Honor / Huawei refresh rate -> " + targetHz + "Hz");

        String hzF = targetHz + ".0";
        String hz = String.valueOf(targetHz);
        StringBuilder sb = new StringBuilder();

        // Honor & Huawei OEM display setting keys
        sb.append("honor_screen_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system honor_screen_refresh_rate " + hz)).append("\n");
        sb.append("hw_display_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system hw_display_refresh_rate " + hz)).append("\n");
        sb.append("huawei_user_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system huawei_user_refresh_rate " + hz)).append("\n");
        sb.append("hw_fps_limit: ").append(ShizukuExecutor.executeShizukuCommand("setprop persist.sys.hw.fps " + hz)).append("\n");

        // Universal AOSP fallbacks
        sb.append("peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system peak_refresh_rate " + hzF)).append("\n");
        sb.append("min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system min_refresh_rate " + hzF)).append("\n");
        sb.append("user_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system user_refresh_rate " + hz)).append("\n");

        // SurfaceFlinger IPC and app refresh rate lock
        sb.append("cmd window set-app-refresh-rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd window set-app-refresh-rate global " + targetHz)).append("\n");
        sb.append("SurfaceFlinger 1035: ").append(ShizukuExecutor.executeShizukuCommand("service call SurfaceFlinger 1035 i32 " + targetHz)).append("\n");

        return sb.toString().trim();
    }

    @Override
    public String resetRefreshRate() {
        Log.d(TAG, "Resetting Honor / Huawei refresh rate...");
        StringBuilder sb = new StringBuilder();
        sb.append("reset peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system peak_refresh_rate")).append("\n");
        sb.append("reset min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system min_refresh_rate")).append("\n");
        sb.append("reset honor_screen_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system honor_screen_refresh_rate")).append("\n");
        return sb.toString().trim();
    }

    @Override
    public String getStrategyName() {
        return "Honor MagicOS / Huawei EMUI Refresh Rate Strategy";
    }

    @Override
    public boolean isSupported() {
        return ShizukuExecutor.hasShizukuPermission();
    }
}
