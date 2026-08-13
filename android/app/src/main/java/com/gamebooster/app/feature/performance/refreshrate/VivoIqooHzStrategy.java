package com.gamebooster.app.feature.performance.refreshrate;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

/**
 * Refresh rate strategy tailored for Vivo OriginOS and iQOO FunTouchOS devices.
 * Forces high refresh rate panel targets up to 165Hz via OEM system settings and SurfaceFlinger.
 */
public class VivoIqooHzStrategy implements RefreshRateInterface {

    private static final String TAG = "VivoIqooHzStrategy";

    @Override
    public String forceRefreshRate(int targetHz) {
        Log.d(TAG, "Forcing Vivo / iQOO refresh rate -> " + targetHz + "Hz");

        String hzF = targetHz + ".0";
        String hz = String.valueOf(targetHz);
        StringBuilder sb = new StringBuilder();

        // Vivo / iQOO Ultra Game Mode & Display System Properties
        sb.append("vivo_screen_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system vivo_screen_refresh_rate 3")).append("\n");
        sb.append("iqoo_game_fps_target: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system iqoo_game_fps_target " + hz)).append("\n");
        sb.append("vivo_fps_limit: ").append(ShizukuExecutor.executeShizukuCommand("setprop persist.vivo.fps.limit " + hz)).append("\n");

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
        Log.d(TAG, "Resetting Vivo / iQOO refresh rate...");
        StringBuilder sb = new StringBuilder();
        sb.append("reset peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system peak_refresh_rate")).append("\n");
        sb.append("reset min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system min_refresh_rate")).append("\n");
        return sb.toString().trim();
    }

    @Override
    public String getStrategyName() {
        return "Vivo OriginOS / iQOO Ultra Game Mode Hz Strategy";
    }

    @Override
    public boolean isSupported() {
        return ShizukuExecutor.hasShizukuPermission();
    }
}
