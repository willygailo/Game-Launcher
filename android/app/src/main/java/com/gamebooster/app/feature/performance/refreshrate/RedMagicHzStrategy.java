package com.gamebooster.app.feature.performance.refreshrate;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

public class RedMagicHzStrategy implements RefreshRateInterface {

    private static final String TAG = "RedMagicHzStrategy";

    @Override
    public String forceRefreshRate(int targetHz) {
        return forceRefreshRate(targetHz, null);
    }

    @Override
    public String forceRefreshRate(int targetHz, String packageName) {
        Log.d(TAG, "Forcing RedMagic / Nubia refresh rate -> " + targetHz + "Hz" + (packageName != null ? " for " + packageName : ""));

        String hzF = targetHz + ".0";
        String hz = String.valueOf(targetHz);
        StringBuilder sb = new StringBuilder();

        // System Settings
        sb.append("peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system peak_refresh_rate " + hzF)).append("\n");
        sb.append("min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system min_refresh_rate " + hzF)).append("\n");
        sb.append("user_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system user_refresh_rate " + hz)).append("\n");
        sb.append("nubia_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system nubia_refresh_rate " + hz)).append("\n");
        sb.append("redmagic_game_mode: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system redmagic_game_mode 1")).append("\n");

        // Global Settings
        sb.append("global_peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put global peak_refresh_rate " + hzF)).append("\n");
        sb.append("global_min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put global min_refresh_rate " + hzF)).append("\n");

        // RedMagic Properties
        sb.append("nubia_game_mode_prop: ").append(ShizukuExecutor.executeShizukuCommand("setprop sys.nubia.game.mode 1")).append("\n");
        sb.append("nubia_hz_prop: ").append(ShizukuExecutor.executeShizukuCommand("setprop persist.sys.nubia.hz " + hz)).append("\n");
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
        Log.d(TAG, "Resetting RedMagic refresh rate...");
        StringBuilder sb = new StringBuilder();
        sb.append("reset peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system peak_refresh_rate")).append("\n");
        sb.append("reset min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system min_refresh_rate")).append("\n");
        sb.append("reset global peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete global peak_refresh_rate")).append("\n");
        sb.append("reset global min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete global min_refresh_rate")).append("\n");
        sb.append("reset nubia_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system nubia_refresh_rate")).append("\n");
        sb.append("reset redmagic_game_mode: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings delete system redmagic_game_mode")).append("\n");
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
        return "RedMagic Nubia High Refresh Rate";
    }

    @Override
    public boolean isSupported() {
        return ShizukuExecutor.hasShizukuPermission();
    }
}

