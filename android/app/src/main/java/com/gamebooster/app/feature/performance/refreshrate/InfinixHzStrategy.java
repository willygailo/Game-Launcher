package com.gamebooster.app.feature.performance.refreshrate;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

/**
 * Refresh rate strategy tailored specifically for Infinix XOS devices.
 * Controls XOS display modes, Dar-Link game engine 3.0/4.0 properties, and bypass charging.
 */
public class InfinixHzStrategy implements RefreshRateInterface {

    private static final String TAG = "InfinixHzStrategy";

    @Override
    public String forceRefreshRate(int targetHz) {
        return forceRefreshRate(targetHz, null);
    }

    @Override
    public String forceRefreshRate(int targetHz, String packageName) {
        Log.d(TAG, "Forcing Infinix XOS refresh rate -> " + targetHz + "Hz" + (packageName != null ? " for " + packageName : ""));

        String hzF = targetHz + ".0";
        String hz = String.valueOf(targetHz);
        StringBuilder sb = new StringBuilder();

        // Infinix XOS System Settings & Game Booster Keys
        sb.append("infinix_refresh_rate_mode: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system infinix_refresh_rate_mode " + hz)).append("\n");
        sb.append("xos_display_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system xos_display_refresh_rate " + hz)).append("\n");
        sb.append("infinix_screen_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system infinix_screen_refresh_rate " + hz)).append("\n");
        sb.append("infinix_game_mode: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system infinix_game_mode 1")).append("\n");

        // Infinix XOS / Dar-Link System Properties & Thermal Bypass
        sb.append("persist.sys.phx.fps: ").append(ShizukuExecutor.executeShizukuCommand("setprop persist.sys.phx.fps " + hz)).append("\n");
        sb.append("persist.sys.game.fps: ").append(ShizukuExecutor.executeShizukuCommand("setprop persist.sys.game.fps " + hz)).append("\n");
        sb.append("sys.infinix.fps: ").append(ShizukuExecutor.executeShizukuCommand("setprop sys.infinix.fps " + hz)).append("\n");
        sb.append("persist.sys.darlink.mode: ").append(ShizukuExecutor.executeShizukuCommand("setprop persist.sys.darlink.mode 1")).append("\n");
        sb.append("sys.bypass.charging: ").append(ShizukuExecutor.executeShizukuCommand("setprop sys.bypass.charging 1")).append("\n");
        sb.append("sys.oem.fps_limit: ").append(ShizukuExecutor.executeShizukuCommand("setprop sys.oem.fps_limit 0")).append("\n");

        // Universal AOSP display settings
        sb.append("peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system peak_refresh_rate " + hzF)).append("\n");
        sb.append("min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system min_refresh_rate " + hzF)).append("\n");
        sb.append("user_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system user_refresh_rate " + hz)).append("\n");

        // Global Settings
        sb.append("global_peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put global peak_refresh_rate " + hzF)).append("\n");
        sb.append("global_min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put global min_refresh_rate " + hzF)).append("\n");
        sb.append("sf fps_override: ").append(ShizukuExecutor.executeShizukuCommand("setprop debug.sf.fps_override " + hz)).append("\n");

        // Per-Game Configuration
        if (packageName != null && !packageName.trim().isEmpty() && !"global".equalsIgnoreCase(packageName.trim())) {
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
        Log.d(TAG, "Resetting Infinix XOS refresh rate...");
        StringBuilder sb = new StringBuilder();
        sb.append("reset peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system peak_refresh_rate")).append("\n");
        sb.append("reset min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system min_refresh_rate")).append("\n");
        sb.append("reset global peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete global peak_refresh_rate")).append("\n");
        sb.append("reset global min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete global min_refresh_rate")).append("\n");
        sb.append("reset infinix_refresh_rate_mode: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system infinix_refresh_rate_mode")).append("\n");
        sb.append("reset xos_display_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system xos_display_refresh_rate")).append("\n");
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
        return "Infinix XOS Dar-Link Refresh Rate Strategy";
    }

    @Override
    public boolean isSupported() {
        return ShizukuExecutor.hasShizukuPermission();
    }
}

