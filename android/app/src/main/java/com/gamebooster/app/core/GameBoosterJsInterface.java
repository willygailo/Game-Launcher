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
        com.gamebooster.app.spoofer.SpoofProfile profile =
                com.gamebooster.app.spoofer.DeviceSpooferEngine.getAllProfiles().get(profileId);
        if (profile == null) return false;
        return com.gamebooster.app.spoofer.DeviceSpooferEngine.applyProfile(context, profile, packageName);
    }

    @JavascriptInterface
    public String performDeepSearchJson() {
        if (context == null) return "[]";
        try {
            java.util.Set<String> discovered = com.gamebooster.app.games.search.DeepSearchScanner.performDeepSearch(context);
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
            for (com.gamebooster.app.spoofer.SpoofProfile p : com.gamebooster.app.spoofer.DeviceSpooferEngine.getAllProfiles().values()) {
                JSONObject obj = new JSONObject();
                obj.put("id", p.id);
                obj.put("name", p.displayName);
                obj.put("model", p.model);
                obj.put("brand", p.brand);
                obj.put("brandLabel", p.brandLabel);
                array.put(obj);
            }
            return array.toString();
        } catch (Exception e) {
            return "[]";
        }
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

    @JavascriptInterface
    public String enableBypassCharging() {
        return com.gamebooster.app.bypasscharging.BypassChargingManager.getInstance().enableBypassCharging();
    }

    @JavascriptInterface
    public String disableBypassCharging() {
        return com.gamebooster.app.bypasscharging.BypassChargingManager.getInstance().disableBypassCharging();
    }

    @JavascriptInterface
    public String getBypassStatusJson() {
        try {
            com.gamebooster.app.bypasscharging.BypassChargingInterface strategy =
                    com.gamebooster.app.bypasscharging.BypassChargingManager.getInstance().getCurrentStrategy();
            JSONObject obj = new JSONObject();
            obj.put("strategyName", strategy.getStrategyName());
            obj.put("isSupported", strategy.isSupported());
            obj.put("statusDetails", strategy.getBypassStatus());
            return obj.toString();
        } catch (Exception e) {
            return "{\"strategyName\":\"Unknown\",\"isSupported\":false,\"statusDetails\":\"Error fetching status\"}";
        }
    }

    @JavascriptInterface
    public String getGameSpoofStrategyJson(String packageName) {
        try {
            com.gamebooster.app.spoofer.games.GameSpooferInterface strategy =
                    com.gamebooster.app.spoofer.games.GameSpooferManager.getInstance().getStrategyForPackage(packageName);
            JSONObject obj = new JSONObject();
            obj.put("strategyName", strategy.getStrategyName());
            obj.put("profile", strategy.getSpoofProfile().toJsonObject());
            return obj.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    @JavascriptInterface
    public boolean applyGameSpoofForPackage(String packageName) {
        if (context == null || packageName == null) return false;
        return com.gamebooster.app.spoofer.games.GameSpooferManager.getInstance().applySpoofForPackage(context, packageName);
    }

    @JavascriptInterface
    public boolean setRefreshRateForOem(int hz) {
        if (context == null) return false;
        return com.gamebooster.app.booster.refreshrate.RefreshRateManager.getInstance().setRefreshRate(context, hz);
    }

    @JavascriptInterface
    public boolean setThermalMitigationForOem(boolean disableThrottling) {
        if (context == null) return false;
        return com.gamebooster.app.booster.thermal.ThermalManager.getInstance().setThermalMitigation(context, disableThrottling);
    }

    @JavascriptInterface
    public String getAllTweaksJson() {
        try {
            org.json.JSONArray array = new org.json.JSONArray();
            for (com.gamebooster.app.tweaks.TweakItem tweak : com.gamebooster.app.tweaks.TweakManagerRepository.getAllTweaks()) {
                array.put(tweak.toJsonObject());
            }
            return array.toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    @JavascriptInterface
    public boolean applyTweakById(String tweakId) {
        if (context == null || tweakId == null) return false;
        for (com.gamebooster.app.tweaks.TweakItem tweak : com.gamebooster.app.tweaks.TweakManagerRepository.getAllTweaks()) {
            if (tweak.getId().equalsIgnoreCase(tweakId)) {
                return com.gamebooster.app.tweaks.TweakManagerRepository.applyTweak(context, tweak);
            }
        }
        return false;
    }

    @JavascriptInterface
    public boolean revertTweakById(String tweakId) {
        if (context == null || tweakId == null) return false;
        for (com.gamebooster.app.tweaks.TweakItem tweak : com.gamebooster.app.tweaks.TweakManagerRepository.getAllTweaks()) {
            if (tweak.getId().equalsIgnoreCase(tweakId)) {
                return com.gamebooster.app.tweaks.TweakManagerRepository.revertTweak(context, tweak);
            }
        }
        return false;
    }
}


