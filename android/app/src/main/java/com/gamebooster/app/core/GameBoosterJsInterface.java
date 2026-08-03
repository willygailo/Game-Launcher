package com.gamebooster.app.core;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.device.DeviceInfoChannel;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
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

    @JavascriptInterface
    public boolean applyDeviceSpoofProfile(String profileId, String packageName) {
        if (context == null || profileId == null) return false;
        com.gamebooster.app.spoofer.DeviceSpooferEngine.SpoofProfile profile =
                com.gamebooster.app.spoofer.DeviceSpooferEngine.getAllProfiles().get(profileId);
        if (profile == null) return false;
        return com.gamebooster.app.spoofer.DeviceSpooferEngine.applyProfile(context, profile, packageName);
    }

    @JavascriptInterface
    public String performDeepSearchJson() {
        if (context == null) return "[]";
        try {
            java.util.Set<String> discovered = com.gamebooster.app.search.DeepSearchScanner.performDeepSearch(context);
            org.json.JSONArray array = new org.json.JSONArray();
            for (String pkg : discovered) {
                array.put(pkg);
            }
            return array.toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    @JavascriptInterface
    public String getAvailableSpoofProfilesJson() {
        try {
            org.json.JSONArray array = new org.json.JSONArray();
            for (com.gamebooster.app.spoofer.DeviceSpooferEngine.SpoofProfile p : com.gamebooster.app.spoofer.DeviceSpooferEngine.getAllProfiles().values()) {
                JSONObject obj = new JSONObject();
                obj.put("id", p.id);
                obj.put("name", p.name);
                obj.put("model", p.model);
                obj.put("brand", p.brand);
                array.put(obj);
            }
            return array.toString();
        } catch (Exception e) {
            return "[]";
        }
    }
}
