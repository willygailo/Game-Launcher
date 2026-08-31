package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * Standoff2ConfigPatcher manages legal configuration files for Axlebolt Standoff 2.
 * Unlocks 120 FPS / 144 FPS / 165 FPS / 185 FPS and sets 1000Hz touch polling rate.
 */
public class Standoff2ConfigPatcher {

    private static final String TAG = "Standoff2ConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyStandardPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Standoff 2 patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── UltraExtreme 144fps SuperSmooth Patch ───────────────────────────────

    public static boolean patchUltraExtreme144(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            "FPS=144",
            "MaxFPS=144",
            "TargetFPS=144",
            "FrameRateLimit=144",
            "FrameRateLevel=8",
            "UnlockFPS=1",
            "Unlock144FPS=1",
            "Ultra144FPS=1",
            "Unlock120Hz=1", "Unlock144Hz=1", "Unlock165Hz=1", "Unlock185Hz=1",
            "HighFPSMode=1", "SuperHighFPS=1",
            "QualityLevel=4", "TextureQuality=4", "ShadowQuality=2",
            "AntiAliasingQuality=4", "BloomQuality=5", "MaxAnisotropy=16",
            "HDRMode=1", "ResolutionScale=100",
            "UltraExtreme=1", "bUseUltraExtreme=True",
            "bFramePacingEnabled=True", "Vsync=0",
            "TouchBoostHz=144", "TouchPollingRate=1000",
            "GyroSampleRate=1000", "GyroZeroDelay=1"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[Graphics]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "Standoff2 UltraExtreme144 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    public static boolean patchUltraExtreme185(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            "FPS=185",
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
            "HighFPSMode=1", "SuperHighFPS=1",
            "QualityLevel=5", "TextureQuality=4", "ShadowQuality=2",
            "AntiAliasingQuality=4", "BloomQuality=5", "MaxAnisotropy=16",
            "HDRMode=1", "ResolutionScale=100",
            "UltraExtreme=1", "bUseUltraExtreme=True",
            "bFramePacingEnabled=True", "Vsync=0",
            "TouchBoostHz=185", "TouchPollingRate=1000",
            "GyroSampleRate=1000", "GyroZeroDelay=1"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[Graphics]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "Standoff2 UltraExtreme185 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String jsonContent = "{\n" +
                "  \"graphics\": {\n" +
                "    \"target_framerate\": " + forcedFps + ",\n" +
                "    \"max_framerate\": " + forcedFps + ",\n" +
                "    \"framerate_cap\": " + forcedFps + ",\n" +
                "    \"fps_unlock\": 1,\n" +
                "    \"fps_unlock_120\": 1,\n" +
                "    \"fps_unlock_144\": 1,\n" +
                "    \"fps_unlock_165\": 1,\n" +
                "    \"fps_unlock_185\": 1,\n" +
                "    \"high_fps_mode\": 1,\n" +
                "    \"shader_detail\": 3,\n" +
                "    \"model_detail\": 3,\n" +
                "    \"texture_detail\": 3,\n" +
                "    \"screen_scale\": 1.0,\n" +
                "    \"anisotropic_filtering\": 16,\n" +
                "    \"antialiasing\": 4,\n" +
                "    \"ultra_extreme\": true\n" +
                "  },\n" +
                "  \"controls\": {\n" +
                "    \"touch_acceleration\": 0.0,\n" +
                "    \"touch_rate_hz\": 1000,\n" +
                "    \"zero_input_latency\": true\n" +
                "  }\n" +
                "}\n";

        String iniContent = "[StandoffGraphics]\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "FrameRateLimit=" + forcedFps + "\n" +
                "FPSLevel=" + fpsLevel + "\n" +
                "HighFPSMode=1\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "Unlock120Hz=1\n" +
                "Unlock144Hz=1\n" +
                "Unlock165Hz=1\n" +
                "Unlock185Hz=1\n" +
                "GraphicQuality=4\n" +
                "UltraExtreme=1\n" +
                "ResolutionScale=1.0\n" +
                "TouchPollingRate=1000\n" +
                "TouchSlop=1\n" +
                "TouchZeroDelay=1\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            boolean ok;
            if (path.endsWith(".json")) {
                ok = ConfigFileHelper.writeContentAtomic(path, jsonContent);
            } else {
                ok = ConfigFileHelper.writeContentAtomic(path, iniContent);
            }
            if (ok) written++;
        }
        Log.i(TAG, "Standoff 2 competitive UltraExtreme " + forcedFps + "FPS force-write: " + written + " paths");
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

    private static boolean applyStandardPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        String[] keys = {
            "target_framerate=" + forcedFps,
            "max_framerate=" + forcedFps,
            "framerate_cap=" + forcedFps,
            "TargetFPS=" + forcedFps,
            "MaxFPS=" + forcedFps,
            "FrameRateLimit=" + forcedFps,
            "fps_unlock=1",
            "fps_unlock_120=1",
            "fps_unlock_144=1",
            "fps_unlock_165=1",
            "fps_unlock_185=1",
            "high_fps_mode=1",
            "ultra_extreme=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[StandoffGraphics]");
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }
}
