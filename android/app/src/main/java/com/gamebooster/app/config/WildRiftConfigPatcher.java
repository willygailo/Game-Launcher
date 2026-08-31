package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * WildRiftConfigPatcher manages legal configuration files for League of Legends: Wild Rift (all regions).
 * Unlocks 90 FPS / 120 FPS / 144 FPS / 165 FPS / 185 FPS and 1000Hz touch input response.
 */
public class WildRiftConfigPatcher {


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

    private static final String TAG = "WildRiftConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyStandardPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Wild Rift patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── UltraExtreme 144fps SuperSmooth Patch ───────────────────────────────

    public static boolean patchUltraExtreme144(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            "FpsCapValue=144",
            "TargetFPS=144",
            "MaxFrameRate=144",
            "FrameRateLimit=144",
            "FrameRateLevel=8",
            "UnlockFPS=1",
            "Unlock144FPS=1",
            "Ultra144FPS=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "HighFPSMode=3",
            "GraphicQuality=5",
            "TextureQuality=4",
            "ShadowQuality=2",
            "ShadowResolution=2048",
            "AntiAliasingQuality=4",
            "BloomQuality=5",
            "MaxAnisotropy=16",
            "HDRMode=1",
            "UltraHDMode=1",
            "ResolutionScale=100",
            "UltraExtreme=1",
            "bUseUltraExtreme=True",
            "bFramePacingEnabled=True",
            "Vsync=0",
            "SkillCastSampleRate=1000",
            "SkillCastZeroDelay=1",
            "AttackSpeedAnimationBuffer=1000",
            "AutoAttackCancelOptimization=1",
            "SmartTargetLock=1",
            "ItemQuickBuyLatency=0",
            "TouchBoostHz=144",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[Graphics]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "WildRift UltraExtreme144 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    public static boolean patchUltraExtreme185(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            "FpsCapValue=185",
            "TargetFPS=185",
            "MaxFrameRate=185",
            "FrameRateLimit=185",
            "FrameRateLevel=10",
            "UnlockFPS=1",
            "Unlock144FPS=1",
            "Unlock165FPS=1",
            "Unlock185FPS=1",
            "Ultra144FPS=1",
            "Ultra165FPS=1",
            "Ultra185FPS=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "HighFPSMode=3",
            "GraphicQuality=5",
            "TextureQuality=4",
            "ShadowQuality=2",
            "ShadowResolution=2048",
            "AntiAliasingQuality=4",
            "BloomQuality=5",
            "MaxAnisotropy=16",
            "HDRMode=1",
            "UltraHDMode=1",
            "ResolutionScale=100",
            "UltraExtreme=1",
            "bUseUltraExtreme=True",
            "bFramePacingEnabled=True",
            "Vsync=0",
            "SkillCastSampleRate=1000",
            "SkillCastZeroDelay=1",
            "AttackSpeedAnimationBuffer=1000",
            "AutoAttackCancelOptimization=1",
            "SmartTargetLock=1",
            "ItemQuickBuyLatency=0",
            "TouchBoostHz=185",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[Graphics]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "WildRift UltraExtreme185 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String jsonContent = "{\n" +
                "  \"graphics\": {\n" +
                "    \"target_fps\": " + forcedFps + ",\n" +
                "    \"max_fps\": " + forcedFps + ",\n" +
                "    \"fps_level\": " + fpsLevel + ",\n" +
                "    \"fpsUnlock\": 1,\n" +
                "    \"unlock_120\": 1,\n" +
                "    \"unlock_144\": 1,\n" +
                "    \"unlock_165\": 1,\n" +
                "    \"unlock_185\": 1,\n" +
                "    \"resolution\": 4,\n" +
                "    \"quality\": 4,\n" +
                "    \"ultra_extreme\": 1,\n" +
                "    \"high_fps_mode\": 1,\n" +
                "    \"vulkan_enabled\": true,\n" +
                "    \"vsync\": false\n" +
                "  },\n" +
                "  \"combat\": {\n" +
                "    \"skill_cast_rate_hz\": 1000,\n" +
                "    \"animation_buffer_hz\": 1000,\n" +
                "    \"smart_targeting\": true,\n" +
                "    \"item_quick_buy_latency\": 0\n" +
                "  },\n" +
                "  \"input\": {\n" +
                "    \"touch_polling_hz\": 1000,\n" +
                "    \"zero_latency_mode\": true\n" +
                "  }\n" +
                "}\n";

        String iniContent = "[WildRiftGraphics]\n" +
                "FPSLevel=" + fpsLevel + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "FPS=" + forcedFps + "\n" +
                "HighFPSMode=1\n" +
                "UnlockFPS=1\n" +
                "Unlock120=1\n" +
                "Unlock144=1\n" +
                "Unlock165=1\n" +
                "Unlock185=1\n" +
                "GraphicQuality=4\n" +
                "UltraExtreme=1\n" +
                "SkillCastSampleRate=1000\n" +
                "SkillCastZeroDelay=1\n" +
                "AttackSpeedAnimationBuffer=1000\n" +
                "AutoAttackCancelOptimization=1\n" +
                "SmartTargetLock=1\n" +
                "ItemQuickBuyLatency=0\n" +
                "TouchPollingRate=1000\n" +
                "TouchSlop=1\n" +
                "TouchZeroDelay=1\n" +
                "ZeroInputLag=1\n";

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
        Log.i(TAG, "Wild Rift competitive UltraExtreme " + forcedFps + "FPS force-write: " + written + " paths");
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
            "target_fps=" + forcedFps,
            "max_fps=" + forcedFps,
            "fps_level=" + fpsLevel,
            "fpsUnlock=1",
            "unlock_120=1",
            "unlock_144=1",
            "unlock_165=1",
            "unlock_185=1",
            "resolution=4",
            "quality=4",
            "ultra_extreme=1",
            "TargetFPS=" + forcedFps,
            "MaxFPS=" + forcedFps,
            "FPSLevel=" + fpsLevel,
            "SkillCastSampleRate=1000",
            "AttackSpeedAnimationBuffer=1000",
            "ItemQuickBuyLatency=0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[WildRiftGraphics]");
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }
}
