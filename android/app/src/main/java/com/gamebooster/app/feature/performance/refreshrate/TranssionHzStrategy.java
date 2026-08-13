package com.gamebooster.app.feature.performance.refreshrate;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

/**
 * Refresh rate strategy tailored for Transsion XOS / HiOS / myOS (Infinix, Tecno, itel) devices.
 * Forces high refresh rate panel targets up to 165Hz via OEM system settings and Dar-Link game engine properties.
 */
public class TranssionHzStrategy implements RefreshRateInterface {

    private static final String TAG = "TranssionHzStrategy";

    @Override
    public String forceRefreshRate(int targetHz) {
        Log.d(TAG, "Forcing Transsion XOS/HiOS refresh rate -> " + targetHz + "Hz");

        String hzF = targetHz + ".0";
        String hz = String.valueOf(targetHz);
        StringBuilder sb = new StringBuilder();

        // Transsion XOS / HiOS Dar-Link & Display System Properties
        sb.append("transsion_refresh_rate_mode: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system transsion_refresh_rate_mode " + hz)).append("\n");
        sb.append("persist.sys.phx.fps: ").append(ShizukuExecutor.executeShizukuCommand("setprop persist.sys.phx.fps " + hz)).append("\n");
        sb.append("persist.sys.game.fps: ").append(ShizukuExecutor.executeShizukuCommand("setprop persist.sys.game.fps " + hz)).append("\n");
        sb.append("sys.oem.fps_limit: ").append(ShizukuExecutor.executeShizukuCommand("setprop sys.oem.fps_limit 0")).append("\n");

        // Universal AOSP fallback properties
        sb.append("peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system peak_refresh_rate " + hzF)).append("\n");
        sb.append("min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system min_refresh_rate " + hzF)).append("\n");
        sb.append("user_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system user_refresh_rate " + hz)).append("\n");
        sb.append("cmd window set-app-refresh-rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd window set-app-refresh-rate global " + targetHz)).append("\n");
        sb.append("SurfaceFlinger 1035: ").append(ShizukuExecutor.executeShizukuCommand("service call SurfaceFlinger 1035 i32 " + targetHz)).append("\n");

        return sb.toString().trim();
    }

    @Override
    public String resetRefreshRate() {
        Log.d(TAG, "Resetting Transsion XOS/HiOS refresh rate...");
        StringBuilder sb = new StringBuilder();
        sb.append("reset peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system peak_refresh_rate")).append("\n");
        sb.append("reset min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system min_refresh_rate")).append("\n");
        return sb.toString().trim();
    }

    @Override
    public String getStrategyName() {
        return "Transsion XOS / HiOS Dar-Link Refresh Rate Strategy";
    }

    @Override
    public boolean isSupported() {
        return ShizukuExecutor.hasShizukuPermission();
    }
}
