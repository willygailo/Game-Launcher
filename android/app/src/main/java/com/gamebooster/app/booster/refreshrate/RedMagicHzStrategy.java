package com.gamebooster.app.booster.refreshrate;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class RedMagicHzStrategy implements RefreshRateInterface {

    private static final String TAG = "RedMagicHzStrategy";

    @Override
    public String forceRefreshRate(int targetHz) {
        Log.d(TAG, "Forcing RedMagic / Nubia refresh rate -> " + targetHz + "Hz");

        String hz = String.valueOf(targetHz);
        StringBuilder sb = new StringBuilder();

        sb.append("nubia_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system nubia_refresh_rate " + hz)).append("\n");
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
