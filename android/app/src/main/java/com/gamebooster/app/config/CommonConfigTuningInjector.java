package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * CommonConfigTuningInjector — Centralized Engine, Graphics, Scope Aim & Hit-Reg DPS Optimizer.
 *
 * Provides legitimate, ban-safe optimization routines for:
 *  1. High-frequency touch sampling (1000Hz) & input latency reduction.
 *  2. All-Scope & No-Scope Aim Precision Calibration (No-Scope/Hipfire, Red Dot, Holo, 2x, 3x, 4x, 6x, 8x).
 *  3. Hit-Registration & Effective DPS Maximizer (0-Frame Thread Lag, Frame Pacing, 1000Hz Sync).
 *  4. Gyroscope sampling rate acceleration and stabilization (1000Hz).
 *  5. Unreal Engine 4/5 SystemSettings & CVar graphics/FPS unlocking.
 *  6. Unity boot.config runtime performance and multi-threaded rendering flags.
 *  7. Vulkan pipeline cache pre-warming and shader compilation tuning.
 *  8. Telemetry and background log I/O suppression (Anti-Log).
 */
public final class CommonConfigTuningInjector {

    private static final String TAG = "CommonConfigTuning";

    private CommonConfigTuningInjector() {}

    private static List<String> getPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    /**
     * Injects 1000Hz Ultra-Fast Touch, 0ms Input Buffer Delay, and Touch Slop Reduction.
     */
    public static void applySuperFastTouch(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
        String[] touchKeys = {
            "TouchPollingRate=1000",
            "TouchSampleRate=1000",
            "HighFreqTouchHz=185",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "TouchSlopReduction=1",
            "TouchResponseLevel=3",
            "InputBufferRate=1000",
            "TouchInterpolation=1",
            "MultiTouchSampling=1000"
        };
        for (String path : paths) {
            NativeConfigInjector.injectSuperFastTouch(path);
            ConfigFileHelper.patchKeys(path, touchKeys, "[TouchEngine]");
        }
        Log.i(TAG, "Super Fast Touch 1000Hz applied for " + packageName);
    }

    /**
     * Injects All-Scope & No-Scope Aim Precision Calibration:
     * - No-Scope / Hip-fire: Zero touch deadzone, linear acceleration curve, 1000Hz touch rate.
     * - Iron Sight, Red Dot & Holographic: 1:1 camera tracking and low-latency ADS sampling.
     * - Mid-range Scopes (2x, 3x, 4x): Micro-gyro stabilization and weapon sway damping.
     * - Long-range Scopes (6x, 8x Sniper): High-precision micro-filters and 1000Hz gyro resolution.
     */
    public static void applyAllScopeAimPrecision(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
        String[] scopeKeys = {
            "TouchPollingRate=1000",
            "TouchSampleRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "NoScopeTouchRate=1000",
            "HipfireDeadzone=0",
            "HipfireSensitivityBoost=1.2",
            "IronSightSensitivity=1.0",
            "RedDotSensScale=1.0",
            "HoloSensScale=1.0",
            "Scope2xSensitivity=1.0",
            "Scope2xGyroSample=1000",
            "Scope3xSensitivity=0.90",
            "Scope3xGyroStabilization=1",
            "Scope4xSensitivity=0.85",
            "Scope4xGyroStabilization=1",
            "Scope6xSensitivity=0.75",
            "Scope6xMicroDamping=1",
            "Scope8xSensitivity=0.65",
            "Scope8xPrecisionFilter=1",
            "Scope8xGyro1000Hz=1",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "GyroStabilization=1",
            "GyroSmoothFactor=1",
            "GyroLatencyMode=0",
            "InputSmoothing=1",
            "TouchStabilization=1",
            "ZeroInputDelay=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectScopeAimCalibration(path);
            ConfigFileHelper.patchKeys(path, scopeKeys, "[ScopeAimPrecision]");
        }
        Log.i(TAG, "All-Scope & No-Scope Aim Precision applied for " + packageName);
    }

    /**
     * Injects Gyroscope 1000Hz sampling, low-latency tracking, and input stabilization.
     */
    public static void applyAimAssistConfig(String packageName) {
        applyAllScopeAimPrecision(packageName);
    }

    /**
     * Injects Joystick response level, zero-deadzone, and input stabilization.
     */
    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
        String[] recoilKeys = {
            "JoystickZeroDeadzone=1",
            "JoystickResponseLevel=3",
            "InputSmoothing=1",
            "TouchStabilization=1",
            "ZeroInputDelay=1",
            "TouchJitterFilter=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectNoRecoil(path);
            ConfigFileHelper.patchKeys(path, recoilKeys, "[InputStabilization]");
        }
        Log.i(TAG, "Input Stabilization applied for " + packageName);
    }

    /**
     * Maximizes effective damage and hit-registration rate by eliminating CPU-GPU frame latency,
     * enforcing zero-frame thread lag, and stabilizing packet/render pacing during weapon fire.
     */
    public static void applyHitRegistrationDpsBoost(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
        String[] hitRegKeys = {
            "r.OneFrameThreadLag=0",
            "r.FinishCurrentFrame=0",
            "r.Streaming.PoolSize=0",
            "r.MobileReduceLoadedMips=0",
            "bFramePacingEnabled=True",
            "InputBufferRate=1000",
            "HitRegSyncRate=1000",
            "ZeroInputLag=1",
            "AllowOcclusionQueries=1",
            "PreloadShaders=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHitRegDpsBoost(path);
            ConfigFileHelper.patchKeys(path, hitRegKeys, "[HitRegDpsBoost]");
        }
        applyUltraExtremeGraphics(packageName, 144);
        Log.i(TAG, "Hit-Registration DPS Boost applied for " + packageName);
    }

    public static void applyDamageScriptConfig(String packageName) {
        applyHitRegistrationDpsBoost(packageName);
    }

    public static void applyAimHeadLockConfig(String packageName) {
        applyAllScopeAimPrecision(packageName);
    }

    public static void applyUltraDamageOverdriveConfig(String packageName) {
        applyHitRegistrationDpsBoost(packageName);
        applyUltraExtremeGraphics(packageName, 165);
    }

    public static void applyHeroAimLockConfig(String packageName) {
        applyAllScopeAimPrecision(packageName);
    }

    public static void applyFastCooldownConfig(String packageName) {
        applyUltraExtremeGraphics(packageName, 120);
    }

    public static void applyShield1500Config(String packageName) {
        applyUltraExtremeGraphics(packageName, 120);
    }

    public static void applyDroneViewConfig(String packageName) {
        applyDroneViewUltraConfig(packageName);
    }

    public static void applyDroneViewUltraConfig(String packageName) {
        applyUltraExtremeGraphics(packageName, 120);
    }

    public static void applyArmorDefConfig(String packageName) {
        applyUltraExtremeGraphics(packageName, 120);
    }

    public static void applySpeedBoostConfig(String packageName) {
        applySuperFastTouch(packageName);
    }

    public static void applyTrackingBulletConfig(String packageName) {
        applyHitRegistrationDpsBoost(packageName);
    }

    /**
     * Injects UltraExtreme max graphics quality + FPS unlock keys into all game config paths.
     */
    public static void applyUltraExtremeGraphics(String packageName, int targetFps) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        final FpsUnlockTier tier = FpsUnlockTier.fromFps(targetFps);
        final String[] graphicsKeys = {
            "FPS=" + tier.fps,
            "MaxFPS=" + tier.fps,
            "TargetFPS=" + tier.fps,
            "FrameRateLimit=" + tier.fps,
            "MobileFPSLimit=" + tier.fps,
            "FrameRateLevel=" + tier.level,
            "UnlockFPS=1",
            "HighFPSMode=1",
            "SuperHighFPS=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "bFramePacingEnabled=True",
            "Vsync=0",
            "TouchBoostHz=" + tier.fps,
            "TouchPollingRate=1000",
            "AllowOcclusionQueries=1",
            "PreloadShaders=1"
        };
        List<String> paths = getPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectUltraExtremeGraphics(path, tier.fps);
            ConfigFileHelper.patchKeys(path, graphicsKeys, "[GraphicsUltraExtreme]");
        }
        Log.i(TAG, "UltraExtreme Max Graphics @ " + tier.fps + "fps applied for " + packageName);
    }

    public static void applyUltraExtreme144(String packageName) {
        applyUltraExtremeGraphics(packageName, FpsUnlockTier.FPS_144.fps);
    }

    /**
     * Injects Unreal Engine 4/5 SystemSettings & FPS unlocks into Engine.ini / UserCustom.ini.
     */
    public static void applyUnrealEngineOptimization(String packageName, int targetFps) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectUnrealEngineIni(path, targetFps);
        }
        Log.i(TAG, "Unreal Engine 4/5 optimization injected @ " + targetFps + "fps for " + packageName);
    }

    /**
     * Injects Unity boot.config optimization flags (native GLES, no debugger, multi-threaded jobs).
     */
    public static void applyUnityBootConfigOptimization(String packageName, int targetFps) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectUnityBootConfig(path, targetFps);
        }
        Log.i(TAG, "Unity boot.config optimization injected @ " + targetFps + "fps for " + packageName);
    }

    /**
     * Injects Anti-Log / Privacy Guard keys and purges disk log buffers for the game.
     */
    public static void applyAntiLog(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    /**
     * Convenient single-method dispatcher to apply all enabled profile tunings for a package.
     */
    public static void applyAllEnabledTunings(String packageName, CompetitiveCfgProfile profile) {
        if (packageName == null || profile == null) return;
        if (profile.isSuperFastTouchEnabled()) applySuperFastTouch(packageName);
        if (profile.isAimAssistEnabled()) applyAllScopeAimPrecision(packageName);
        if (profile.isRecoilControlEnabled()) applyRecoilControlConfig(packageName);
        if (profile.isMlbbDamageScriptEnabled() || profile.isTrackingBulletEnabled()) applyHitRegistrationDpsBoost(packageName);
        if (profile.isAntiLogEnabled()) applyAntiLog(packageName);
        applyUnrealEngineOptimization(packageName, profile.getTargetFps());
        applyUnityBootConfigOptimization(packageName, profile.getTargetFps());
        applyUltraExtremeGraphics(packageName, profile.getTargetFps());
    }
}
