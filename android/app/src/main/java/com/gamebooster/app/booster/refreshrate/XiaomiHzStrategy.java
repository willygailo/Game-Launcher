package com.gamebooster.app.booster.refreshrate;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class XiaomiHzStrategy implements RefreshRateInterface {

    private static final String TAG = "XiaomiHzStrategy";

    @Override
    public String forceRefreshRate(int targetHz) {
        Log.d(TAG, "Forcing Xiaomi MIUI / HyperOS refresh rate -> " + targetHz + "Hz");

        String hz = String.valueOf(targetHz);
        StringBuilder sb = new StringBuilder();

        sb.append("user_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system user_refresh_rate " + hz)).append("\n");
        sb.append("display_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system display_refresh_rate " + hz)).append("\n");
        sb.append("dfps level: ").append(ShizukuExecutor.executeShizukuCommand("setprop persist.vendor.dfps.level " + hz)).append("\n");

        sb.append("cmd window set-app-refresh-rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd window set-app-refresh-rate global " + targetHz)).append("\n");
        sb.append("device_config game_overlay: ").append(ShizukuExecutor.executeShizukuCommand("device_config put game_overlay global mode=2,fps=" + targetHz + ":mode=3,fps=" + targetHz)).append("\n");

        return sb.toString().trim();
    }

    @Override
    public String resetRefreshRate() {
        Log.d(TAG, "Resetting Xiaomi HyperOS refresh rate...");
        StringBuilder sb = new StringBuilder();
        sb.append("reset dfps level: ").append(ShizukuExecutor.executeShizukuCommand("setprop persist.vendor.dfps.level 0")).append("\n");
        return sb.toString().trim();
    }

    @Override
    public String getStrategyName() {
        return "Xiaomi HyperOS / MIUI Display DFPS Refresh Rate";
    }

    @Override
    public boolean isSupported() {
        return ShizukuExecutor.hasShizukuPermission();
    }
}
