package com.gamebooster.app.feature.performance.refreshrate;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

public class AsusRogHzStrategy implements RefreshRateInterface {

    private static final String TAG = "AsusRogHzStrategy";

    @Override
    public String forceRefreshRate(int targetHz) {
        Log.d(TAG, "Forcing Asus ROG Phone Game Genie refresh rate -> " + targetHz + "Hz");

        String hz = String.valueOf(targetHz);
        StringBuilder sb = new StringBuilder();

        sb.append("asus_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system asus_refresh_rate " + hz)).append("\n");
        sb.append("fps_mode: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system fps_mode " + hz)).append("\n");
        sb.append("cmd window set-app-refresh-rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd window set-app-refresh-rate global " + targetHz)).append("\n");

        return sb.toString().trim();
    }

    @Override
    public String resetRefreshRate() {
        Log.d(TAG, "Resetting Asus ROG refresh rate...");
        return ShizukuExecutor.executeShizukuCommand("cmd settings delete system asus_refresh_rate");
    }

    @Override
    public String getStrategyName() {
        return "Asus ROG Game Genie Refresh Rate";
    }

    @Override
    public boolean isSupported() {
        return ShizukuExecutor.hasShizukuPermission();
    }
}
