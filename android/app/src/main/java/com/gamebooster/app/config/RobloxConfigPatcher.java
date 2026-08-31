package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * RobloxConfigPatcher manages ClientAppSettings.json FastFlags and local graphics settings
 * for Roblox on Android.
 * Unlocks 120/144/165/185 FPS frame rate limits and enables high performance rendering.
 */
public class RobloxConfigPatcher {

    private static final String TAG = "RobloxConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Roblox patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── UltraExtreme 144fps SuperSmooth Patch ───────────────────────────────

    public static boolean patchUltraExtreme144(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            "MaxFPS=144",
            "TargetFPS=144",
            "FrameRateLimit=144",
            "FrameRateLevel=8",
            "UnlockFPS=1",
            "Unlock144FPS=1",
            "Ultra144FPS=1",
            "HighFPSMode=1",
            "Unlock120Hz=1", "Unlock144Hz=1", "Unlock165Hz=1", "Unlock185Hz=1",
            "QualityLevel=4", "TextureQuality=4", "ShadowQuality=2",
            "AntiAliasingQuality=4", "BloomQuality=5", "MaxAnisotropy=16",
            "HDRMode=1", "HDR10Plus=1", "ResolutionScale=120",
            "UltraExtreme=1", "bUseUltraExtreme=True",
            "bFramePacingEnabled=True", "Vsync=0",
            "TouchBoostHz=144", "TouchPollingRate=1000",
            "TouchZeroDelay=1", "ZeroInputLag=1"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[Graphics]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "Roblox UltraExtreme144 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    public static boolean patchUltraExtreme185(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            "MaxFPS=185",
            "TargetFPS=185",
            "FrameRateLimit=185",
            "FrameRateLevel=10",
            "UnlockFPS=1",
            "Unlock144FPS=1",
            "Unlock165FPS=1",
            "Unlock185FPS=1",
            "Ultra144FPS=1",
            "Ultra165FPS=1",
            "Ultra185FPS=1",
            "HighFPSMode=1",
            "Unlock120Hz=1", "Unlock144Hz=1", "Unlock165Hz=1", "Unlock185Hz=1",
            "QualityLevel=5", "TextureQuality=4", "ShadowQuality=2",
            "AntiAliasingQuality=4", "BloomQuality=5", "MaxAnisotropy=16",
            "HDRMode=1", "HDR10Plus=1", "ResolutionScale=120",
            "UltraExtreme=1", "bUseUltraExtreme=True",
            "bFramePacingEnabled=True", "Vsync=0",
            "TouchBoostHz=185", "TouchPollingRate=1000",
            "TouchZeroDelay=1", "ZeroInputLag=1"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[Graphics]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "Roblox UltraExtreme185 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);

        String clientAppSettings = "{\n" +
                "  \"DFIntTaskSchedulerTargetFps\": " + forcedFps + ",\n" +
                "  \"FIntTargetFPS\": " + forcedFps + ",\n" +
                "  \"FIntDesiredMaxFrameRate\": " + forcedFps + ",\n" +
                "  \"FFlagEnableHighFPS\": \"True\",\n" +
                "  \"FFlagUnlockFPS\": \"True\",\n" +
                "  \"FFlagTaskSchedulerLimitTargetFps\": \"False\",\n" +
                "  \"FFlagDebugGraphicsPreferVulkan\": \"True\",\n" +
                "  \"FFlagFixGraphicsQuality\": \"True\",\n" +
                "  \"DFFlagDisableDPIScale\": \"True\",\n" +
                "  \"FFlagCommitToFastPhysics\": \"True\",\n" +
                "  \"FFlagEnableVulkan\": \"True\",\n" +
                "  \"FFlagFastTouchResponse\": \"True\",\n" +
                "  \"FIntTouchPollingRate\": 1000,\n" +
                "  \"FFlagZeroTouchDelay\": \"True\",\n" +
                "  \"FFlagReduceInputLatency\": \"True\",\n" +
                "  \"FFlagTouchSlopReduction\": \"True\"\n" +
                "}\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.writeContentAtomic(path, clientAppSettings)) {
                written++;
            }
        }
        Log.i(TAG, "Roblox competitive " + forcedFps + "FPS FastFlag force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    // ─── Delegated Common Tuning Injectors ───────────────────────────────────

    public static void applySuperFastTouch(String packageName) {
        CommonConfigTuningInjector.applySuperFastTouch(packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        CommonConfigTuningInjector.applyAimAssistConfig(packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        CommonConfigTuningInjector.applyRecoilControlConfig(packageName);
    }

    public static void applyDamageScriptConfig(String packageName) {
        CommonConfigTuningInjector.applyDamageScriptConfig(packageName);
    }

    public static void applyFastCooldownConfig(String packageName) {
        CommonConfigTuningInjector.applyFastCooldownConfig(packageName);
    }

    public static void applyShield1500Config(String packageName) {
        CommonConfigTuningInjector.applyShield1500Config(packageName);
    }

    public static void applyDroneViewUltraConfig(String packageName) {
        CommonConfigTuningInjector.applyDroneViewUltraConfig(packageName);
    }

    public static void applyDroneViewConfig(String packageName) {
        CommonConfigTuningInjector.applyDroneViewConfig(packageName);
    }

    public static void applyArmorDefConfig(String packageName) {
        CommonConfigTuningInjector.applyArmorDefConfig(packageName);
    }

    public static void applySpeedBoostConfig(String packageName) {
        CommonConfigTuningInjector.applySpeedBoostConfig(packageName);
    }

    public static void applyTrackingBulletConfig(String packageName) {
        CommonConfigTuningInjector.applyTrackingBulletConfig(packageName);
    }

    public static void applyAntiLog(String packageName) {
        CommonConfigTuningInjector.applyAntiLog(packageName);
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    private static boolean applyPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        String[] keys = {
            "DFIntTaskSchedulerTargetFps=" + forcedFps,
            "FIntTargetFPS=" + forcedFps,
            "FIntDesiredMaxFrameRate=" + forcedFps,
            "FFlagEnableHighFPS=True",
            "FFlagUnlockFPS=True",
            "FFlagDebugGraphicsPreferVulkan=True",
            "FFlagFixGraphicsQuality=True",
            "DFFlagDisableDPIScale=True",
            "FFlagCommitToFastPhysics=True",
            "FFlagEnableVulkan=True"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Roblox]");
    }
}
