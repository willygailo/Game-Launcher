package com.gamebooster.app.core;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.device.DeviceInfoChannel;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import org.json.JSONObject;
import org.json.JSONArray;

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
    public boolean setMasterBoostEnabled(boolean enable) {
        if (context == null) return false;
        com.gamebooster.app.config.ManualSettingsPreferences.setMasterBoostEnabled(context, enable);
        if (!enable) {
            com.gamebooster.app.tweaks.TweakManagerRepository.revertAllTweaks(context);
        } else {
            com.gamebooster.app.core.settings.SettingsStateRestorer.restoreAllSettings(context);
        }
        return true;
    }

    @JavascriptInterface
    public boolean isMasterBoostEnabled() {
        if (context == null) return false;
        return com.gamebooster.app.config.ManualSettingsPreferences.isMasterBoostEnabled(context);
    }

    @JavascriptInterface
    public boolean revertAllToAospDefaults() {
        if (context == null) return false;
        com.gamebooster.app.config.ManualSettingsPreferences.setMasterBoostEnabled(context, false);
        com.gamebooster.app.tweaks.TweakManagerRepository.revertAllTweaks(context);
        return true;
    }

    @JavascriptInterface
    public boolean isShizukuConnected() {
        return com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission();
    }

    @JavascriptInterface
    public boolean forceGrantAllPermissions() {
        if (context == null) return false;
        com.gamebooster.app.shizuku.ShizukuExecutor.grantAppPermissionsViaShizuku(context);
        return true;
    }

    @JavascriptInterface
    public boolean forceApplyGameConfig(String pkg, String relativeFilePath, String rawContent) {
        if (context == null || pkg == null) return false;
        com.gamebooster.app.config.GameConfigPatcher.PatchResult res =
                com.gamebooster.app.config.GameConfigPatcher.forceApplyUserConfig(pkg, relativeFilePath, rawContent);
        return res.success;
    }

    @JavascriptInterface
    public boolean putSystemSetting(String key, String value) {
        return com.gamebooster.app.engine.SystemPropertiesEngine.putSystemSetting(key, value);
    }

    @JavascriptInterface
    public boolean putGlobalSetting(String key, String value) {
        return com.gamebooster.app.engine.SystemPropertiesEngine.putGlobalSetting(key, value);
    }

    @JavascriptInterface
    public boolean putSecureSetting(String key, String value) {
        return com.gamebooster.app.engine.SystemPropertiesEngine.putSecureSetting(key, value);
    }

    @JavascriptInterface
    public boolean setSystemProperty(String key, String value) {
        return com.gamebooster.app.engine.SystemPropertiesEngine.setSystemProperty(key, value);
    }

    @JavascriptInterface
    public boolean applyZeroTouchDelay(String packageName) {
        return com.gamebooster.app.booster.TouchLatencyChannel.applyZeroTouchDelayForPackage(packageName);
    }

    @JavascriptInterface
    public String getAndroidVersionInfoJson() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("apiVersion", com.gamebooster.app.device.UniversalDeviceAdapter.getAndroidApiVersion());
            obj.put("versionName", com.gamebooster.app.device.UniversalDeviceAdapter.getAndroidVersionName());
            obj.put("oemBrand", com.gamebooster.app.device.UniversalDeviceAdapter.getOemBrand().name());
            obj.put("chipsetVendor", com.gamebooster.app.device.UniversalDeviceAdapter.getChipsetVendor().name());
            return obj.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    @JavascriptInterface
    public String getGameConfigPathsJson(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return "[]";
        String pkg = packageName.toLowerCase().trim();
        JSONArray arr = new JSONArray();

        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
            arr.put("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/Config/UserSystem.ini");
            arr.put("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/Config/DamageSystem.ini");
            arr.put("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/Config/AimAssist.ini");
            arr.put("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/Com/MobileLegendsSettings.ini");
        } else if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile")) {
            arr.put("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini");
            arr.put("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserEngine.ini");
            arr.put("/sdcard/Android/data/" + pkg + "/files/UE4Game/PUBGM/PUBGM/Saved/Config/Android/UserCustom.ini");
        } else if (pkg.contains("cod") || pkg.contains("callofduty")) {
            arr.put("/sdcard/Android/data/" + pkg + "/files/Config/UserSetting.json");
            arr.put("/sdcard/Android/data/" + pkg + "/files/GraphicsSettings.ini");
        } else {
            arr.put("/sdcard/Android/data/" + pkg + "/files/GameSettings.ini");
            arr.put("/data/data/" + pkg + "/files/GameSettings.ini");
        }
        return arr.toString();
    }
}
