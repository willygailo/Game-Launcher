package com.gamebooster.app.core;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.device.DeviceInfoChannel;
import com.gamebooster.app.games.GameProfileAutoConfigurator;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import org.json.JSONObject;

public class GameBoosterJsInterface {

    private final Context context;

    public GameBoosterJsInterface(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public String executeShizukuCmd(String command) {
        if (command == null || command.trim().isEmpty()) return "ERROR: Empty command";
        return ShizukuExecutor.executeShizukuCommand(command.trim());
    }

    @JavascriptInterface
    public boolean applyProfile(String profileName) {
        if (context == null || profileName == null) return false;
        PerformanceChannel.Profile profile;
        if ("EXTREME".equalsIgnoreCase(profileName)) {
            profile = PerformanceChannel.Profile.EXTREME_PERFORMANCE;
        } else if ("BALANCED".equalsIgnoreCase(profileName)) {
            profile = PerformanceChannel.Profile.BALANCED;
        } else {
            profile = PerformanceChannel.Profile.PERFORMANCE;
        }
        return PerformanceChannel.applyProfile(context, profile);
    }

    @JavascriptInterface
    public String getDeviceMetricsJson() {
        if (context == null) return "{}";
        try {
            DeviceInfoChannel.Metrics m = DeviceInfoChannel.getMetrics(context);
            JSONObject obj = new JSONObject();
            obj.put("deviceSummary", m.deviceSummary);
            obj.put("usedRamMb", m.usedRamMb);
            obj.put("totalRamMb", m.totalRamMb);
            obj.put("ramUsagePct", m.ramUsagePct);
            obj.put("batteryTempC", m.batteryTempC);
            return obj.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    @JavascriptInterface
    public void grantShizukuPermissions() {
        if (context != null) {
            ShizukuExecutor.grantAppPermissionsViaShizuku(context);
        }
    }

    @JavascriptInterface
    public void setTargetFps(int fps) {
        if (context != null) {
            GameProfileAutoConfigurator.setTargetFpsHz(context, fps);
        }
    }
}
