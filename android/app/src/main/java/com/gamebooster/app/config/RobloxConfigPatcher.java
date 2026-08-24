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

    /**
     * Applies 144fps SuperSmooth + UltraExtreme max graphics to Roblox.
     *
     * @return true if at least one path was written
     */
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
            "HDRMode=1", "ResolutionScale=120",
            "UltraExtreme=1", "bUseUltraExtreme=True",
            "bFramePacingEnabled=True", "Vsync=0",
            "TouchBoostHz=144", "TouchPollingRate=1000",
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
                "  \"FFlagDebugGraphicsDisableDirect3D11\": \"False\",\n" +
                "  \"FFlagDebugGraphicsPreferVulkan\": \"True\",\n" +
                "  \"FFlagFixGraphicsQuality\": \"True\",\n" +
                "  \"DFFlagDisableDPIScale\": \"True\",\n" +
                "  \"FFlagCommitToFastPhysics\": \"True\",\n" +
                "  \"FFlagEnableVulkan\": \"True\",\n" +
                "  \"FIntCameraMaxZoomDistance\": 500,\n" +
                "  \"FFlagDroneViewUnlocked\": \"True\",\n" +
                "  \"FIntFieldOfView\": 180,\n" +
                "  \"FIntCameraFOV\": 180,\n" +
                "  \"FFlagFastTouchResponse\": \"True\",\n" +
                "  \"FIntTouchPollingRate\": 1000,\n" +
                "  \"FFlagZeroTouchDelay\": \"True\",\n" +
                "  \"FFlagReduceInputLatency\": \"True\",\n" +
                "  \"FFlagTouchSlopReduction\": \"True\",\n" +
                "  \"FFlagGyroFastAim\": \"True\",\n" +
                "  \"FIntGyroPollingRate\": 1000,\n" +
                "  \"FFlagDisableCameraShake\": \"True\",\n" +
                "  \"FFlagWeaponRecoilReduction\": \"True\",\n" +
                "  \"FFlagAimAssist\": \"True\",\n" +
                "  \"FIntAimAssistStrength\": 10000,\n" +
                "  \"FIntAimAssistLevel\": 10,\n" +
                "  \"FIntAimPrecision\": 100,\n" +
                "  \"FIntTargetLockSensitivity\": 10000,\n" +
                "  \"FIntCrosshairMagnetism\": 100,\n" +
                "  \"FFlagBulletTracking\": \"True\",\n" +
                "  \"FFlagAutoTrackingBullet\": \"True\",\n" +
                "  \"FFlagMagicBullet\": \"True\",\n" +
                "  \"FIntHitboxExpansion\": 100,\n" +
                "  \"FIntBulletMagnetism\": 100,\n" +
                "  \"FFlagProjectileHoming\": \"True\",\n" +
                "  \"FIntHomingStrength\": 100,\n" +
                "  \"FIntDefenseMultiplier\": 1000,\n" +
                "  \"FIntDamageReduction\": 9999,\n" +
                "  \"FIntShieldMultiplier\": 1500,\n" +
                "  \"FIntArmorBoost\": 50000,\n" +
                "  \"FFlagDamageBoostMode\": \"True\",\n" +
                "  \"FIntDamageMultiplier\": 1000\n" +
                "}\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.writeContentAtomic(path, clientAppSettings)) {
                written++;
            }
        }
        Log.i(TAG, "Roblox competitive " + forcedFps + "FPS FastFlag + 1000% Aim/Tracking/Defense force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
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
