package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * HokConfigPatcher manages internal config files for Honor of Kings (HOK) and Arena of Valor (AoV).
 * Unlocks 120/144/165/185 FPS modes, HDR ultra frame rates, and high-frequency touch response.
 */
public class HokConfigPatcher {


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

    public static void applyNetworkLagCompensation(String packageName) {
        CommonConfigTuningInjector.applyNetworkLagCompensation(packageName);
    }

    public static void applyAutoSmiteObjective(String packageName) {
        if (packageName == null) return;
        List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectHokAutoSmiteObjective(path);
        }
    }

    public static void applyHokDamage10000AttackSpeedMax(String packageName) {
        if (packageName == null) return;
        List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectHokDamage10000AttackSpeedMax(path);
        }
    }

    private static final String TAG = "HokConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "HOK patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── UltraExtreme 144fps SuperSmooth Patch ───────────────────────────────

    public static boolean patchUltraExtreme144(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            "FrameRate=144",
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
            "HighFPSMode=1",
            "QualityLevel=4",
            "TextureQuality=4",
            "ShadowQuality=2",
            "ShadowResolution=2048",
            "AntiAliasingQuality=4",
            "BloomQuality=5",
            "MaxAnisotropy=16",
            "HDRMode=1",
            "UltraHDMode=1",
            "ResolutionScale=120",  // 2026: 120% supersampling
            "UltraExtreme=1",
            "bUseUltraExtreme=True",
            "bFramePacingEnabled=True",
            "Vsync=0",
            "SkillCastSampleRate=1000",
            "SkillCastZeroDelay=1",
            "AttackSpeedAnimationBuffer=1000",
            "AutoAttackCancelOptimization=1",
            "SmartTargetLock=1",
            "LowestHPTargetPriority=1",
            "ItemQuickBuyLatency=0",
            "ItemSwapBufferRate=1000",
            "TouchBoostHz=144", "TouchPollingRate=1000",
            "TouchZeroDelay=1", "ZeroInputLag=1",
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
        Log.i(TAG, "HOK UltraExtreme144 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    public static boolean patchUltraExtreme185(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            "FrameRate=185",
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
            "HighFPSMode=1",
            "QualityLevel=5",
            "TextureQuality=4",
            "ShadowQuality=2",
            "ShadowResolution=2048",
            "AntiAliasingQuality=4",
            "BloomQuality=5",
            "MaxAnisotropy=16",
            "HDRMode=1",
            "UltraHDMode=1",
            "ResolutionScale=120",  // 2026: 120% supersampling
            "UltraExtreme=1",
            "bUseUltraExtreme=True",
            "bFramePacingEnabled=True",
            "Vsync=0",
            "SkillCastSampleRate=1000",
            "SkillCastZeroDelay=1",
            "AttackSpeedAnimationBuffer=1000",
            "AutoAttackCancelOptimization=1",
            "SmartTargetLock=1",
            "LowestHPTargetPriority=1",
            "ItemQuickBuyLatency=0",
            "ItemSwapBufferRate=1000",
            "TouchBoostHz=185", "TouchPollingRate=1000",
            "TouchZeroDelay=1", "ZeroInputLag=1",
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
        Log.i(TAG, "HOK UltraExtreme185 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;

        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String content = "[Graphics]\n" +
                "HighFPSMode=1\n" +
                "FrameRateLevel=" + frameRateLevel + "\n" +
                "FPS=" + forcedFps + "\n" +
                "MaxFrameRate=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "GraphicsQuality=4\n" +
                "HDMode=1\n" +
                "HDRMode=1\n" +
                "UltraFrameRate=1\n" +
                "VulkanEnabled=1\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "Unlock120Hz=1\n" +
                "Unlock144Hz=1\n" +
                "Unlock165Hz=1\n" +
                "Unlock185Hz=1\n" +
                "SkillCastSampleRate=1000\n" +
                "SkillCastZeroDelay=1\n" +
                "AttackSpeedAnimationBuffer=1000\n" +
                "AutoAttackCancelOptimization=1\n" +
                "SmartTargetLock=1\n" +
                "LowestHPTargetPriority=1\n" +
                "ItemQuickBuyLatency=0\n" +
                "ItemSwapBufferRate=1000\n" +
                "HighFreqTouchHz=" + forcedFps + "\n" +
                "TouchPollingRate=1000\n" +
                "TouchZeroDelay=1\n" +
                "TouchResponseLevel=3\n" +
                "ZeroInputLag=1\n" +
                "HitRegSyncRate=1000\n" +
                "GyroSampleRate=1000\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.writeContentAtomic(path, content)) {
                written++;
            }
        }
        Log.i(TAG, "HOK competitive " + forcedFps + "FPS force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
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
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;
        String[] keys = {
            "HighFPSMode=1",
            "FrameRateLevel=" + frameRateLevel,
            "FPS=" + forcedFps,
            "MaxFrameRate=" + forcedFps,
            "TargetFPS=" + forcedFps,
            "GraphicsQuality=4",
            "HDMode=1",
            "HDRMode=1",
            "UltraFrameRate=1",
            "VulkanEnabled=1",
            "UnlockFPS=1",
            "SuperHighFPS=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "Shadow=1",
            "SkillCastSampleRate=1000",
            "SkillCastZeroDelay=1",
            "AttackSpeedAnimationBuffer=1000",
            "AutoAttackCancelOptimization=1",
            "ItemQuickBuyLatency=0",
            "HighFreqTouchHz=" + forcedFps
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Graphics]");
    }
}
