package com.gamebooster.app.feature.performance.refreshrate;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

public class RedMagicHzStrategy implements RefreshRateInterface {

    private static final String TAG = "RedMagicHzStrategy";

    @Override
    public String forceRefreshRate(int targetHz) {
        Log.d(TAG, "Forcing RedMagic / Nubia refresh rate -> " + targetHz + "Hz");

        String hzF = targetHz + ".0";
        String hz = String.valueOf(targetHz);
        StringBuilder sb = new StringBuilder();

        sb.append("peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system peak_refresh_rate " + hzF)).append("\n");
        sb.append("min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system min_refresh_rate " + hzF)).append("\n");
        sb.append("user_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system user_refresh_rate " + hz)).append("\n");
        sb.append("nubia_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system nubia_refresh_rate " + hz)).append("\n");
        sb.append("redmagic_game_mode: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system redmagic_game_mode 1")).append("\n");
        sb.append("nubia_game_mode_prop: ").append(ShizukuExecutor.executeShizukuCommand("setprop sys.nubia.game.mode 1")).append("\n");
        sb.append("nubia_hz_prop: ").append(ShizukuExecutor.executeShizukuCommand("setprop persist.sys.nubia.hz " + hz)).append("\n");
        sb.append("cmd window set-app-refresh-rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd window set-app-refresh-rate global " + targetHz)).append("\n");

        return sb.toString().trim();
    }

    @Override
    public String resetRefreshRate() {
        Log.d(TAG, "Resetting RedMagic refresh rate...");
        return ShizukuExecutor.executeShizukuCommand("cmd settings delete system nubia_refresh_rate");
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
