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
        return forceRefreshRate(targetHz, null);
    }

    @Override
    public String forceRefreshRate(int targetHz, String packageName) {
        Log.d(TAG, "Forcing Vivo / iQOO refresh rate -> " + targetHz + "Hz" + (packageName != null ? " for " + packageName : ""));

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

        // Global Settings
        sb.append("global_peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put global peak_refresh_rate " + hzF)).append("\n");
        sb.append("global_min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put global min_refresh_rate " + hzF)).append("\n");
        sb.append("sf fps_override: ").append(ShizukuExecutor.executeShizukuCommand("setprop debug.sf.fps_override " + hz)).append("\n");

        // SurfaceFlinger IPC
        sb.append("SurfaceFlinger 1035: ").append(ShizukuExecutor.executeShizukuCommand("service call SurfaceFlinger 1035 i32 " + targetHz)).append("\n");

        // Per-Game Configuration & Vivo High Refresh App Whitelist
        if (packageName != null && !packageName.trim().isEmpty() && !"global".equalsIgnoreCase(packageName.trim())) {
            // Append package to Vivo high refresh rate apps list if not present
            String currentList = ShizukuExecutor.executeShizukuCommand("cmd settings get secure high_refresh_rate_apps_list");
            if (currentList == null || currentList.contains("null") || currentList.trim().isEmpty()) {
                ShizukuExecutor.executeShizukuCommand("cmd settings put secure high_refresh_rate_apps_list " + packageName);
            } else if (!currentList.contains(packageName)) {
                ShizukuExecutor.executeShizukuCommand("cmd settings put secure high_refresh_rate_apps_list \"" + currentList.trim() + "," + packageName + "\"");
            }

            sb.append("cmd window set-app-refresh-rate (").append(packageName).append(" @ ").append(targetHz).append("Hz): ")
                    .append(ShizukuExecutor.executeShizukuCommand("cmd window set-app-refresh-rate " + packageName + " " + targetHz)).append("\n");
            sb.append("cmd game set (").append(packageName).append(" @ ").append(targetHz).append("FPS): ")
                    .append(ShizukuExecutor.executeShizukuCommand("cmd game set --mode 2 --fps " + targetHz + " " + packageName)).append("\n");
            sb.append("device_config game_overlay (").append(packageName).append(" @ ").append(targetHz).append("FPS): ")
                    .append(ShizukuExecutor.executeShizukuCommand("device_config put game_overlay " + packageName + " mode=2,fps=" + targetHz)).append("\n");
        }

        return sb.toString().trim();
    }

    @Override
    public String resetRefreshRate() {
        return resetRefreshRate(null);
    }

    @Override
    public String resetRefreshRate(String packageName) {
        Log.d(TAG, "Resetting Vivo / iQOO refresh rate...");
        StringBuilder sb = new StringBuilder();
        sb.append("reset peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system peak_refresh_rate")).append("\n");
        sb.append("reset min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system min_refresh_rate")).append("\n");
        sb.append("reset global peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete global peak_refresh_rate")).append("\n");
        sb.append("reset global min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete global min_refresh_rate")).append("\n");
        sb.append("reset sf fps_override: ").append(ShizukuExecutor.executeShizukuCommand("setprop debug.sf.fps_override 0")).append("\n");

        if (packageName != null && !packageName.trim().isEmpty() && !"global".equalsIgnoreCase(packageName.trim())) {
            sb.append("cmd game reset (").append(packageName).append("): ")
                    .append(ShizukuExecutor.executeShizukuCommand("cmd game reset " + packageName)).append("\n");
            sb.append("device_config delete game_overlay (").append(packageName).append("): ")
                    .append(ShizukuExecutor.executeShizukuCommand("device_config delete game_overlay " + packageName)).append("\n");
        }

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

