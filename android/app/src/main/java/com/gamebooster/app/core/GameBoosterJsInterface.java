package com.gamebooster.app.core;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.device.DeviceInfoChannel;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuManager;
import com.gamebooster.app.shizuku.ShizukuPermissionEnforcer;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.SpoofProfile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Set;

public class GameBoosterJsInterface {

    private final Context context;

    public GameBoosterJsInterface(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public String executeShizukuCmd(String command) {
        if (command == null || command.trim().isEmpty()) return "ERROR: Empty command";
        return CommandExecutor.executeSystemCommand(command.trim());
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
            ShizukuPermissionEnforcer.enforceAllPermissions(context);
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
        SpoofProfile profile = DeviceSpooferEngine.getAllProfiles().get(profileId);
        if (profile == null) return false;
        return DeviceSpooferEngine.applyProfile(context, profile, packageName);
    }

    @JavascriptInterface
    public String performDeepSearchJson() {
        if (context == null) return "[]";
        try {
            Set<String> discovered = com.gamebooster.app.search.DeepSearchScanner.performDeepSearch(context);
            JSONArray array = new JSONArray();
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
            JSONArray array = new JSONArray();
            for (SpoofProfile p : DeviceSpooferEngine.getAllProfiles().values()) {
                JSONObject obj = new JSONObject();
                obj.put("id", p.id);
                obj.put("name", p.displayName);
                obj.put("model", p.model);
                obj.put("brand", p.brand);
                obj.put("brandLabel", p.brandLabel);
                obj.put("gpu", p.glRenderer);
                obj.put("soc", p.socModel);
                obj.put("ramMb", p.ramTotalMb);
                obj.put("androidVersion", p.androidVersion);
                array.put(obj);
            }
            return array.toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    @JavascriptInterface
    public boolean isShizukuActive() {
        return ShizukuManager.isShizukuRunningAndGranted();
    }

    @JavascriptInterface
    public boolean applyPrecisionAimProfile() {
        if (context == null) return false;
        com.gamebooster.app.core.settings.SettingsManager sm = new com.gamebooster.app.core.settings.SettingsManager(context);
        com.gamebooster.app.core.profile.ProfileManager pm = new com.gamebooster.app.core.profile.ProfileManager(context);
        return sm.applyProfile(pm.getGeneralGamingProfile());
    }

    @JavascriptInterface
    public boolean restoreOriginalInputSettings() {
        if (context == null) return false;
        com.gamebooster.app.core.settings.SettingsManager sm = new com.gamebooster.app.core.settings.SettingsManager(context);
        return sm.restoreOriginalValues();
    }

    @JavascriptInterface
    public void toggleCrosshairOverlay(boolean enable) {
        if (context == null) return;
        if (enable) {
            com.gamebooster.app.overlay.CrosshairOverlayService.startOverlay(context);
        } else {
            com.gamebooster.app.overlay.CrosshairOverlayService.stopOverlay(context);
        }
    }

    @JavascriptInterface
    public String calculateSensitivityJson(int dpi, double screenSize, float gyroMultiplier) {
        try {
            com.gamebooster.app.ui.sensitivity.SensitivityModel m =
                    com.gamebooster.app.ui.sensitivity.SensitivityCalculator.calculate(dpi, screenSize, gyroMultiplier);
            JSONObject obj = new JSONObject();
            obj.put("freeLook", m.freeLook);
            obj.put("noScope3rdPerson", m.noScope3rdPerson);
            obj.put("noScope1stPerson", m.noScope1stPerson);
            obj.put("tppFov", m.tppFov);
            obj.put("fppFov", m.fppFov);
            obj.put("sprintSensitivity", m.sprintSensitivity);
            obj.put("aimAssistStrength", m.aimAssistStrength);
            obj.put("redDotHolo", m.redDotHolo);
            obj.put("scope2x", m.scope2x);
            obj.put("scope4x", m.scope4x);
            obj.put("gyroNoScope", m.gyroNoScope);
            obj.put("gyroRedDot", m.gyroRedDot);
            obj.put("gyro4x", m.gyro4x);
            return obj.toString();
        } catch (Exception e) {
            return "{}";
        }
    }
}
