package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * BloodStrikeConfigPatcher manages legal configuration files for NetEase Blood Strike.
 * Forces high-frequency 185 FPS and zero-latency touch response.
 */
public class BloodStrikeConfigPatcher {


    // ── 2026 Lock Methods ─────────────────────────────────────────────────────

    /**
     * Damage Lock Max — 2026 Edition.
     * Locks DPS at maximum via config-file injection into all resolved game paths.
     * Ban-safe: config-file writes only.
     */
    public static void applyDamageLockMax(String packageName) {
        CommonConfigTuningInjector.applyDamageLockMax(packageName);
    }

    /**
     * Aim Assist Lock Max — 2026 Edition.
     * Locks aim tracking at maximum magnetism + zero deadzone via config injection.
     * Ban-safe: config-file writes only.
     */
    public static void applyAimAssistLockMax(String packageName) {
        CommonConfigTuningInjector.applyAimAssistLockMax(packageName);
    }

    /**
     * Vulkan Pipeline Prime — 2026 Edition.
     * Pre-warms Vulkan pipeline cache + async shader compile. Eliminates mid-match stutter.
     * Ban-safe: config-file writes only.
     */
    public static void applyVulkanPipelinePrime(String packageName) {
        CommonConfigTuningInjector.applyVulkanPipelinePrime(packageName);
    }

    /**
     * Anti-Telemetry Safe — 2026 Edition.
     * Disables game-internal analytics/crash reporters only. Never touches anti-cheat.
     * Ban-safe: config-file writes only.
     */
    public static void applyAntiTelemetrySafe(String packageName) {
        CommonConfigTuningInjector.applyAntiTelemetrySafe(packageName);
    }

    /**
     * Network Lag Compensation — 2026 Edition.
     * Client-side lag comp + 64-tick + jitter buffer keys. UE4/5 games only (silently ignored on Unity).
     * Ban-safe: config-file writes only.
     */
    public static void applyNetworkLagCompensation(String packageName) {
        CommonConfigTuningInjector.applyNetworkLagCompensation(packageName);
    }

    private static final String TAG = "BloodStrikeConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyStandardPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Blood Strike patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── UltraExtreme 144fps SuperSmooth Patch ───────────────────────────────

    public static boolean patchUltraExtreme144(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            "+CVars=r.FrameRateLimit=144",
            "+CVars=r.MobileFPSLimit=144",
            "+CVars=r.Vsync=0",
            "+CVars=r.FramePacing=1",
            "+CVars=r.Unlock144Hz=1",
            "+CVars=r.Unlock120Hz=1",
            "+CVars=r.Unlock165Hz=1",
            "+CVars=r.Unlock185Hz=1",
            "+CVars=r.MobileHDR=1",
            "+CVars=r.MaxAnisotropy=16",
            "+CVars=r.BloomQuality=5",
            "+CVars=r.Shadow.MaxResolution=2048",
            "+CVars=r.TemporalAA.Upscale=1",
            "+CVars=r.MobileContentScaleFactor=1.0",
            "+CVars=r.MobileReduceLoadedMips=0",
            "MaxFPS=144",
            "TargetFPS=144",
            "FrameRateLimit=144",
            "UnlockFPS=1",
            "Unlock144FPS=1",
            "Ultra144FPS=1",
            "Unlock120Hz=1", "Unlock144Hz=1", "Unlock165Hz=1", "Unlock185Hz=1",
            "ShadingQuality=4", "TextureQuality=4", "ShadowQuality=2",
            "AntiAliasingQuality=4", "BloomQuality=5", "MaxAnisotropy=16",
            "HDRMode=1", "HDR10Plus=1", "ResolutionScale=120",
            "UltraExtreme=1", "bUseUltraExtreme=True",
            "bFramePacingEnabled=True", "Vsync=0",
            "TouchBoostHz=144", "TouchPollingRate=1000",
            "GyroSampleRate=1000", "GyroZeroDelay=1"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[UserCustom DeviceProfile]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "BloodStrike UltraExtreme144 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    public static boolean patchUltraExtreme185(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            "+CVars=r.FrameRateLimit=185",
            "+CVars=r.MobileFPSLimit=185",
            "+CVars=r.Vsync=0",
            "+CVars=r.FramePacing=1",
            "+CVars=r.Unlock144Hz=1",
            "+CVars=r.Unlock120Hz=1",
            "+CVars=r.Unlock165Hz=1",
            "+CVars=r.Unlock185Hz=1",
            "+CVars=r.MobileHDR=1",
            "+CVars=r.MaxAnisotropy=16",
            "+CVars=r.BloomQuality=5",
            "+CVars=r.Shadow.MaxResolution=2048",
            "+CVars=r.TemporalAA.Upscale=1",
            "+CVars=r.MobileContentScaleFactor=1.0",
            "+CVars=r.MobileReduceLoadedMips=0",
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
            "Unlock120Hz=1", "Unlock144Hz=1", "Unlock165Hz=1", "Unlock185Hz=1",
            "ShadingQuality=4", "TextureQuality=4", "ShadowQuality=2",
            "AntiAliasingQuality=4", "BloomQuality=5", "MaxAnisotropy=16",
            "HDRMode=1", "HDR10Plus=1", "ResolutionScale=120",
            "UltraExtreme=1", "bUseUltraExtreme=True",
            "bFramePacingEnabled=True", "Vsync=0",
            "TouchBoostHz=185", "TouchPollingRate=1000",
            "GyroSampleRate=1000", "GyroZeroDelay=1"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[UserCustom DeviceProfile]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "BloodStrike UltraExtreme185 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String iniContent = "[GraphicsSettings]\n" +
                "FPSLevel=" + fpsLevel + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "FrameRateLimit=" + forcedFps + "\n" +
                "MobileFPSLimit=" + forcedFps + "\n" +
                "HighFrameRate=1\n" +
                "HighFPSMode=1\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "Unlock120FPS=1\n" +
                "Unlock144FPS=1\n" +
                "Unlock165FPS=1\n" +
                "Unlock185FPS=1\n" +
                "GraphicQuality=4\n" +
                "UltraExtreme=1\n" +
                "HDRMode=1\n" +
                "ShadowQuality=2\n" +
                "AntiAliasing=1\n" +
                "Vsync=0\n" +
                "DynamicResolution=0\n" +
                "ResolutionScale=1.0\n" +
                "TouchPollingRate=1000\n" +
                "TouchSlop=1\n" +
                "TouchZeroDelay=1\n";

        String jsonContent = "{\n" +
                "  \"graphics\": {\n" +
                "    \"target_fps\": " + forcedFps + ",\n" +
                "    \"max_fps\": " + forcedFps + ",\n" +
                "    \"frame_rate_limit\": " + forcedFps + ",\n" +
                "    \"mobile_fps_limit\": " + forcedFps + ",\n" +
                "    \"fps_level\": " + fpsLevel + ",\n" +
                "    \"fps_mode\": \"ultra_extreme\",\n" +
                "    \"high_fps_mode\": true,\n" +
                "    \"unlock_fps\": true,\n" +
                "    \"unlock_high_fps\": true,\n" +
                "    \"resolution_scale\": 1.0,\n" +
                "    \"graphic_quality\": \"ultra\",\n" +
                "    \"hdr_enabled\": true,\n" +
                "    \"vsync\": false\n" +
                "  },\n" +
                "  \"input\": {\n" +
                "    \"touch_hz\": 1000,\n" +
                "    \"touch_latency_reduction\": true,\n" +
                "    \"gyro_sampling_hz\": 1000\n" +
                "  }\n" +
                "}\n";

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
        Log.i(TAG, "Blood Strike competitive UltraExtreme " + forcedFps + "FPS force-write: " + written + " paths");
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
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;
        String[] keys = {
            "MaxFPS=" + forcedFps,
            "TargetFPS=" + forcedFps,
            "FrameRateLimit=" + forcedFps,
            "MobileFPSLimit=" + forcedFps,
            "FPSLevel=" + fpsLevel,
            "GraphicQuality=4",
            "UltraExtreme=1",
            "HighFPSMode=1",
            "UnlockFPS=1",
            "Unlock120FPS=1",
            "Unlock144FPS=1",
            "Unlock165FPS=1",
            "Unlock185FPS=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[GraphicsSettings]");
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }
}
