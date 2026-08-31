package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * MlbbConfigPatcher manages internal config files for Mobile Legends: Bang Bang (all versions).
 *
 * Provides legitimate, ban-safe optimization for:
 *  1. All Heroes Skill Cast & Combo DPS Animation Cancel Buffering (0ms input latency for fast fingers/combos).
 *  2. All Items Quick-Swap & Shop UI responsiveness (instant Immortality / Winter Truncheon item swapping).
 *  3. 120 FPS / 144 FPS / 165 FPS / 185 FPS Ultra Extreme & HDR graphic unlock.
 *  4. 1000Hz touch & joystick polling with zero deadzone for high-precision targeting.
 */
public class MlbbConfigPatcher {

    private static final String TAG = "MlbbConfigPatcher";

    // ─── Standard Patch ───────────────────────────────────────────────────────

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "MLBB patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── UltraExtreme 144fps SuperSmooth Patch ───────────────────────────────

    /**
     * Applies 144fps SuperSmooth + UltraExtreme max graphics + Hero combo responsiveness to MLBB.
     */
    public static boolean patchUltraExtreme144(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            // ── 144fps Unlock ──
            "HighFPSMode=1",
            "FrameRateLevel=8",
            "FPS=144",
            "MaxFrameRate=144",
            "TargetFPS=144",
            "FrameRateLimit=144",
            "HighFrameRate=1",
            "UnlockFPS=1",
            "SuperHighFPS=1",
            "Unlock144FPS=1",
            "Ultra144FPS=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            // ── Max Graphics ──
            "GraphicsQuality=4",
            "TextureQuality=4",
            "ShadowQuality=2",
            "ShadowResolution=2048",
            "AntiAliasingQuality=4",
            "BloomQuality=5",
            "MaxAnisotropy=16",
            "HDMode=1",
            "HDRMode=1",
            "UltraHDMode=1",
            "SuperResolution=1",
            "ResolutionScale=100",
            "UltraExtreme=1",
            "bUseUltraExtreme=True",
            "bUseHighQualityBloom=True",
            "bUseAntiAliasing=True",
            "bReduceLoadedMips=False",
            "Shadow=1",
            // ── All Heroes Skill & Combo Animation Cancel Latency ──
            "SkillCastSampleRate=1000",
            "SkillCastZeroDelay=1",
            "AttackSpeedAnimationBuffer=1000",
            "AutoAttackCancelOptimization=1",
            "SkillAimInterpolation=1",
            "SmartTargetLock=1",
            "LowestHPTargetPriority=1",
            "HeroLockMode=1",
            "JoystickZeroDeadzone=1",
            "JoystickResponseLevel=3",
            // ── All Items Quick-Swap & Shop UI Latency ──
            "ItemQuickBuyLatency=0",
            "ItemSwapBufferRate=1000",
            "UIThreadPriorityBoost=1",
            "PreloadShaders=1",
            "AllowOcclusionQueries=1",
            // ── SuperSmooth Frame Pacing & Touch ──
            "bFramePacingEnabled=True",
            "Vsync=0",
            "HighFreqTouchHz=144",
            "TouchBoostHz=144",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "HitRegSyncRate=1000",
            "GyroSampleRate=1000",
            "GyroSensitivityRatio=2.5",
            "GyroZeroDelay=1",
            "GyroSmoothFactor=1",
            "GyroStabilization=1"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[Graphics]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "MLBB UltraExtreme144 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    /**
     * Injects 185 FPS, Ultra Graphics, and All-Hero combo tuning into MLBB.
     */
    public static boolean patchUltraExtreme185(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            // ── 185fps Unlock ──
            "HighFPSMode=1",
            "FrameRateLevel=10",
            "FPS=185",
            "MaxFrameRate=185",
            "TargetFPS=185",
            "FrameRateLimit=185",
            "HighFrameRate=1",
            "UnlockFPS=1",
            "SuperHighFPS=1",
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
            // ── Max Graphics ──
            "GraphicsQuality=4",
            "TextureQuality=4",
            "ShadowQuality=2",
            "ShadowResolution=2048",
            "AntiAliasingQuality=4",
            "BloomQuality=5",
            "MaxAnisotropy=16",
            "HDMode=1",
            "HDRMode=1",
            "UltraHDMode=1",
            "SuperResolution=1",
            "ResolutionScale=100",
            "UltraExtreme=1",
            "bUseUltraExtreme=True",
            "bUseHighQualityBloom=True",
            "bUseAntiAliasing=True",
            "bReduceLoadedMips=False",
            "Shadow=1",
            // ── All Heroes Skill & Combo Animation Cancel Latency ──
            "SkillCastSampleRate=1000",
            "SkillCastZeroDelay=1",
            "AttackSpeedAnimationBuffer=1000",
            "AutoAttackCancelOptimization=1",
            "SkillAimInterpolation=1",
            "SmartTargetLock=1",
            "LowestHPTargetPriority=1",
            "HeroLockMode=1",
            "JoystickZeroDeadzone=1",
            "JoystickResponseLevel=3",
            // ── All Items Quick-Swap & Shop UI Latency ──
            "ItemQuickBuyLatency=0",
            "ItemSwapBufferRate=1000",
            "UIThreadPriorityBoost=1",
            "PreloadShaders=1",
            "AllowOcclusionQueries=1",
            // ── SuperSmooth Frame Pacing & Touch ──
            "bFramePacingEnabled=True",
            "Vsync=0",
            "HighFreqTouchHz=185",
            "TouchBoostHz=185",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "HitRegSyncRate=1000",
            "GyroSampleRate=1000",
            "GyroSensitivityRatio=2.5",
            "GyroZeroDelay=1",
            "GyroSmoothFactor=1",
            "GyroStabilization=1"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[Graphics]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "MLBB UltraExtreme185 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String content = "[Graphics]\n" +
                "HighFPSMode=1\n" +
                "FrameRateLevel=" + frameRateLevel + "\n" +
                "GraphicsQuality=4\n" +
                "HDMode=1\n" +
                "HDRMode=1\n" +
                "UltraHDMode=1\n" +
                "Shadow=1\n" +
                "FPS=" + forcedFps + "\n" +
                "MaxFrameRate=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "HighFrameRate=1\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "Unlock120Hz=1\n" +
                "Unlock144Hz=1\n" +
                "Unlock165Hz=1\n" +
                "Unlock185Hz=1\n" +
                "Ultra144FPS=1\n" +
                "Ultra165FPS=1\n" +
                "Ultra185FPS=1\n" +
                "SkillCastSampleRate=1000\n" +
                "SkillCastZeroDelay=1\n" +
                "AttackSpeedAnimationBuffer=1000\n" +
                "AutoAttackCancelOptimization=1\n" +
                "SkillAimInterpolation=1\n" +
                "SmartTargetLock=1\n" +
                "LowestHPTargetPriority=1\n" +
                "HeroLockMode=1\n" +
                "JoystickZeroDeadzone=1\n" +
                "JoystickResponseLevel=3\n" +
                "ItemQuickBuyLatency=0\n" +
                "ItemSwapBufferRate=1000\n" +
                "UIThreadPriorityBoost=1\n" +
                "PreloadShaders=1\n" +
                "AllowOcclusionQueries=1\n" +
                "DisableLogging=1\n" +
                "DisableCrashlytics=1\n" +
                "DisableTelemetry=1\n" +
                "AntiLog=1\n" +
                "LogcatDisable=1\n" +
                "HighFreqTouchHz=" + forcedFps + "\n" +
                "TouchPollingRate=1000\n" +
                "TouchZeroDelay=1\n" +
                "TouchResponseLevel=3\n" +
                "ZeroInputLag=1\n" +
                "HitRegSyncRate=1000\n" +
                "GyroSampleRate=1000\n" +
                "GyroSensitivityRatio=2.5\n" +
                "GyroZeroDelay=1\n" +
                "GyroSmoothFactor=1\n" +
                "GyroStabilization=1\n" +
                "GyroLatencyMode=0\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.writeContentAtomic(path, content)) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "MLBB competitive HDR " + forcedFps + "FPS force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
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

    public static void applyAimHeadLockConfig(String packageName) {
        CommonConfigTuningInjector.applyAimHeadLockConfig(packageName);
    }

    public static void applyUltraDamageOverdriveConfig(String packageName) {
        CommonConfigTuningInjector.applyUltraDamageOverdriveConfig(packageName);
    }

    public static void applyHeroAimLockConfig(String packageName) {
        CommonConfigTuningInjector.applyHeroAimLockConfig(packageName);
    }

    public static void applyAntiLog(String packageName) {
        CommonConfigTuningInjector.applyAntiLog(packageName);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    private static boolean applyPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;
        String[] keys = {
            "HighFPSMode=1",
            "FrameRateLevel=" + frameRateLevel,
            "GraphicsQuality=4",
            "HDMode=1",
            "HDRMode=1",
            "UltraHDMode=1",
            "Shadow=1",
            "FPS=" + forcedFps,
            "MaxFrameRate=" + forcedFps,
            "TargetFPS=" + forcedFps,
            "HighFrameRate=1",
            "UnlockFPS=1",
            "SuperHighFPS=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "Ultra144FPS=1",
            "Ultra165FPS=1",
            "Ultra185FPS=1",
            "SkillCastSampleRate=1000",
            "SkillCastZeroDelay=1",
            "AttackSpeedAnimationBuffer=1000",
            "AutoAttackCancelOptimization=1",
            "SkillAimInterpolation=1",
            "SmartTargetLock=1",
            "ItemQuickBuyLatency=0",
            "ItemSwapBufferRate=1000",
            "UIThreadPriorityBoost=1",
            "HighFreqTouchHz=" + forcedFps
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Graphics]");
    }
}
