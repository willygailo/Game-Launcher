package com.gamebooster.app.feature.performance.refreshrate;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

public class OnePlusOppoHzStrategy implements RefreshRateInterface {

    private static final String TAG = "OnePlusOppoHzStrategy";

    @Override
    public String forceRefreshRate(int targetHz) {
        Log.d(TAG, "Forcing OnePlus / OPPO / Realme refresh rate -> " + targetHz + "Hz");

        String hzF = targetHz + ".0";
        String hz = String.valueOf(targetHz);
        StringBuilder sb = new StringBuilder();

        sb.append("peak_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system peak_refresh_rate " + hzF)).append("\n");
        sb.append("min_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system min_refresh_rate " + hzF)).append("\n");
        sb.append("user_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system user_refresh_rate " + hz)).append("\n");
        sb.append("oneplus_screen_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put global oneplus_screen_refresh_rate " + hz)).append("\n");
        sb.append("oppo_display_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system oppo_display_refresh_rate " + hz)).append("\n");
        sb.append("oplus_screen_refresh_rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put system oplus_customize_screen_refresh_rate " + hz)).append("\n");
        sb.append("oplus_display_level: ").append(ShizukuExecutor.executeShizukuCommand("cmd settings put secure oplus_customize_display_level 3")).append("\n");
        sb.append("oplus_display_rate_prop: ").append(ShizukuExecutor.executeShizukuCommand("setprop persist.sys.oplus.display.rate " + hz)).append("\n");
        sb.append("cmd window set-app-refresh-rate: ").append(ShizukuExecutor.executeShizukuCommand("cmd window set-app-refresh-rate global " + targetHz)).append("\n");

        return sb.toString().trim();
    }

    @Override
    public String resetRefreshRate() {
        Log.d(TAG, "Resetting OnePlus / OPPO refresh rate...");
        return ShizukuExecutor.executeShizukuCommand("cmd settings delete global oneplus_screen_refresh_rate");
    }

    @Override
    public String getStrategyName() {
        return "OnePlus / OPPO / Realme ColorOS Refresh Rate";
    }

    @Override
    public boolean isSupported() {
        return ShizukuExecutor.hasShizukuPermission();
    }
}
