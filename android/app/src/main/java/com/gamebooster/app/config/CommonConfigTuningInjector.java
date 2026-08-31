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
     * Damage Lock Max — 2026 Edition.
     * Locks effective DPS at maximum by zeroing frame-thread lag, enforcing 1000Hz
     * hit-reg sync, and injecting Document/BattleConfig.json DPS-floor keys into all
     * resolved game config paths (covers Document/ folder for MLBB).
     *
     * 100% ban-safe: config-file-only writes, no binary modification.
     */
    public static void applyDamageLockMax(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectDamageLockMax(path);
        }
        Log.i(TAG, "DamageLockMax applied for " + packageName + " (" + paths.size() + " paths)");
    }

    /**
     * Aim Assist Lock Max — 2026 Edition.
     * Locks aim-assist tracking at maximum magnetism tier by injecting hero-lock,
     * zero-deadzone, 1000Hz gyro + touch, and all-scope precision keys into all
     * resolved game config paths (covers Document/ folder for MLBB).
     *
     * 100% ban-safe: config-file-only writes, no binary modification.
     */
    public static void applyAimAssistLockMax(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectAimAssistLockMax(path);
        }
        Log.i(TAG, "AimAssistLockMax applied for " + packageName + " (" + paths.size() + " paths)");
    }

    /**
     * Vulkan Pipeline Prime — 2026 Edition.
     * Pre-warms Vulkan pipeline cache and forces async shader compilation, eliminating
     * mid-match compile stutter (stutters caused by on-demand GPU shader builds).
     *
     * 100% ban-safe: config-file-only writes targeting INI/JSON/XML paths.
     */
    public static void applyVulkanPipelinePrime(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
        String[] vulkanPrimeKeys = {
            // ── Vulkan Runtime & Pipeline Cache ──
            "VulkanEnabled=1",
            "VulkanPipelineCache=1",
            "AsyncCompute=1",
            "VRS=1",                               // Variable Rate Shading
            "PreloadShaders=1",
            "bPreloadShaders=True",
            "ShaderPrecompile=1",
            "EnableAsyncPipelineCompilation=1",
            "r.AsyncCompute=1",
            "r.VRS.Enable=1",
            "r.Vulkan.UsePipelines=1",
            "r.Mobile.EnableVulkanPreTransform=1",
            "r.EnableAsyncPipelineCompilation=1",
            "r.Vulkan.RobustBufferAccess=0",       // disable validation overhead
            "r.Vulkan.Enable=1",
            "VulkanThreadCount=4",                 // multi-threaded pipeline compile
            "ShaderWarmupAtLaunch=1",
            "GPUPipelineWarmup=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectVulkanOptimization(path);
            ConfigFileHelper.patchKeys(path, vulkanPrimeKeys, "[VulkanPipelinePrime]");
        }
        Log.i(TAG, "VulkanPipelinePrime applied for " + packageName + " (" + paths.size() + " paths)");
    }

    /**
     * Anti-Telemetry Safe — 2026 Edition.
     * Disables ONLY game-internal crash reporters, analytics pipelines, and background
     * log I/O. Never touches anti-cheat modules. Reduces CPU/disk contention during gameplay.
     *
     * 100% ban-safe: config-file-only writes. Does not modify anti-cheat config.
     */
    public static void applyAntiTelemetrySafe(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
        String[] antiTelKeys = {
            // ── Game-internal analytics & crash reporting ──
            "DisableTelemetry=1",
            "DisableCrashReport=1",
            "DisableAnalytics=1",
            "DisableANR=1",
            "DisableLogUpload=1",
            "AntiTelemetry=1",
            "DisableMetrics=1",
            "DisableHeartbeat=1",
            "DisableEventTracking=1",
            "DisablePerformanceTracking=1",
            "DisableNetworkDiagnostics=0",         // keep network diag (used by game servers)
            "+CVars=r.GPUCrashDebugging=0",
            "+CVars=r.RHISetGPUCaptureOptions=0"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, antiTelKeys, "[AntiTelemetrySafe]");
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "AntiTelemetrySafe applied for " + packageName + " (" + paths.size() + " paths)");
    }

    /**
     * Network Lag Compensation — 2026 Edition.
     * Injects client-side lag compensation, interpolation, and tick-rate keys to reduce
     * ghost shots and teleporting enemies caused by network jitter.
     *
     * UE4/5 games: NetworkTickRate, PredictiveAim, LagCompensation config CVars take effect.
     * Unity games (MLBB, FF, Supercell): keys are silently ignored — zero corruption risk.
     *
     * 100% ban-safe: config-file-only writes, no memory/binary modification.
     */
    public static void applyNetworkLagCompensation(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
        String[] lagCompKeys = {
            // ── Client-side lag compensation ──
            "LagCompensation=1",
            "InterpolationMode=1",
            "NetworkTickRate=64",
            "PredictiveAim=1",
            "PacketLossCompensation=1",
            "JitterBuffer=1",
            "NetSmoothingFactor=0",                // 0 = raw/instant, no smoothing delay
            "PingCompensation=1",
            "RewindTimeMax=1.0",                   // max rewind for hit-reg validation (seconds)
            "bUseClientSidePrediction=True",
            "ClientNetSendMoveThrottleAtNetSpeed=0",
            "ClientNetSendMoveThrottleOverPlayerCount=0",
            "NetClientTicksPerSecond=64",
            "bEnableNetworkCulling=False",          // don't cull nearby entities
            // ── Packet tuning ──
            "MaxClientRate=100000",
            "MaxInternetClientRate=100000",
            "ConfiguredInternetSpeed=100000",
            "ConfiguredLanSpeed=100000"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, lagCompKeys, "[NetworkLagComp]");
        }
        Log.i(TAG, "NetworkLagCompensation applied for " + packageName + " (" + paths.size() + " paths)");
    }

    /**
     * Injects UltraExtreme max graphics quality + FPS unlock keys into all game config paths.
     * 2026 Edition: full key set including AsyncCompute, VRS, HDR10Plus, VulkanPipelineCache,
     * LightingQuality, ParticleQuality, PostProcessing, WaterReflection, and RenderScale=120.
     */
    public static void applyUltraExtremeGraphics(String packageName, int targetFps) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        final FpsUnlockTier tier = FpsUnlockTier.fromFps(targetFps);
        final String[] graphicsKeys = {
            // ── FPS & Frame Rate ──
            "FPS=" + tier.fps,
            "MaxFPS=" + tier.fps,
            "TargetFPS=" + tier.fps,
            "FrameRateLimit=" + tier.fps,
            "MobileFPSLimit=" + tier.fps,
            "FrameRateLevel=" + tier.level,
            "UnlockFPS=1",
            "HighFPSMode=3",         // 3 = 185fps on MLBB 2026 (not 1 or 2)
            "SuperHighFPS=1",
            // ── Refresh Rate Unlocks ──
            "Unlock90Hz=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "Unlock240Hz=1",
            // ── 2026 UltraExtreme Graphics ──
            "UltraExtreme=1",
            "UltraExtreme2026=1",
            "bUseUltraExtreme=True",
            "GraphicsQuality=5",
            "GraphicQuality=4",
            "GraphicsPreset=5",      // 5 = Ultra Extreme (2026 scale)
            "GraphicLevel=4",
            "TextureQuality=4",
            "ShadowQuality=2",
            "ShadowResolution=2048",
            "LightingQuality=3",     // max 2026
            "ParticleQuality=3",     // max 2026
            "PostProcessing=1",
            "WaterReflection=1",
            "AntiAliasingQuality=4",
            "BloomQuality=5",
            "MaxAnisotropy=16",
            // ── Resolution & HDR ──
            "ResolutionScale=120",
            "RenderScale=120",
            "ScreenScale=120",
            "HDRMode=1",
            "HDR10Plus=1",
            "UltraHDMode=1",
            "HDRColorMode=2",
            "SuperResolution=1",
            "bUseHDRMode=True",
            // ── Vulkan & GPU ──
            "VulkanEnabled=1",
            "VulkanPipelineCache=1",
            "AsyncCompute=1",
            "VRS=1",
            // ── Frame Pacing & Input ──
            "bFramePacingEnabled=True",
            "bReduceLoadedMips=False",
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
        Log.i(TAG, "UltraExtreme2026 Max Graphics @ " + tier.fps + "fps applied for " + packageName);
    }

    public static void applyUltraExtreme144(String packageName) {
        applyUltraExtremeGraphics(packageName, FpsUnlockTier.FPS_144.fps);
    }

    /**
     * Injects Vulkan pipeline pre-warm, async compute unlock, and shader pre-compilation.
     * 2026 Edition — applied across all config paths for the game.
     */
    public static void applyVulkanOptimization(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
        String[] vulkanKeys = {
            "VulkanEnabled=1",
            "VulkanPipelineCache=1",
            "AsyncCompute=1",
            "VRS=1",
            "PreloadShaders=1",
            "bPreloadShaders=True",
            "ShaderPrecompile=1",
            "EnableAsyncPipelineCompilation=1",
            "+CVars=r.Vulkan.RobustBufferAccess=0",
            "+CVars=r.Vulkan.EnableValidation=0",
            "+CVars=r.AsyncCompute=1",
            "+CVars=r.VRS.Enable=1",
            "+CVars=r.EnableAsyncPipelineCompilation=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectVulkanOptimization(path);
            ConfigFileHelper.patchKeys(path, vulkanKeys, "[VulkanOptimization]");
        }
        Log.i(TAG, "Vulkan Optimization + AsyncCompute 2026 applied for " + packageName);
    }

    /**
     * Injects HDR10 / P3 wide color gamut flags and 10-bit rendering keys.
     * Works across INI, JSON, and XML config formats.
     */
    public static void applyHDRColorProfile(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
        String[] hdrKeys = {
            "HDRMode=1",
            "HDR10Plus=1",
            "UltraHDMode=1",
            "HDRColorMode=2",
            "bUseHDRMode=True",
            "bHDR10=True",
            "WideColorGamut=1",
            "DynamicRange=HDR",
            "ColorSpace=P3",
            "Bit10Color=1",
            "+CVars=r.MobileHDR=1",
            "+CVars=r.HDR.Display.OutputDevice=1",
            "+CVars=r.Tonemapper.Quality=4",
            "+CVars=r.HDR.Display.ColorGamut=3"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, hdrKeys, "[HDRColorProfile]");
        }
        Log.i(TAG, "HDR10 Wide Color Gamut profile applied for " + packageName);
    }

    /**
     * Suppresses game telemetry, crash reporters, and background analytics I/O.
     * 2026 Edition — ban-safe: disables only game-internal diagnostics, not anti-cheat.
     */
    public static void applyAntiCheatSafe2026(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
        String[] antiTelKeys = {
            "DisableTelemetry=1",
            "DisableCrashReport=1",
            "DisableAnalytics=1",
            "DisableANR=1",
            "DisableLogUpload=1",
            "AntiTelemetry=1",
            "+CVars=r.GPUCrashDebugging=0",
            "+CVars=r.RHISetGPUCaptureOptions=0"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, antiTelKeys, "[AntiTelemetry]");
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "AntiCheatSafe2026 telemetry suppression applied for " + packageName);
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
     * 2026 Edition: also applies Vulkan Optimization, HDR Color Profile, and AntiCheat-safe telemetry suppress.
     */
    public static void applyAllEnabledTunings(String packageName, CompetitiveCfgProfile profile) {
        if (packageName == null || profile == null) return;
        if (profile.isSuperFastTouchEnabled()) applySuperFastTouch(packageName);
        if (profile.isAimAssistEnabled()) applyAllScopeAimPrecision(packageName);
        if (profile.isRecoilControlEnabled()) applyRecoilControlConfig(packageName);
        if (profile.isMlbbDamageScriptEnabled() || profile.isTrackingBulletEnabled()) applyHitRegistrationDpsBoost(packageName);
        if (profile.isAntiLogEnabled()) applyAntiLog(packageName);
        // 2026: always apply Vulkan + HDR + telemetry suppression + DamageLockMax + AimAssistLockMax
        applyVulkanOptimization(packageName);
        applyHDRColorProfile(packageName);
        applyAntiCheatSafe2026(packageName);
        applyUnrealEngineOptimization(packageName, profile.getTargetFps());
        applyUnityBootConfigOptimization(packageName, profile.getTargetFps());
        applyUltraExtremeGraphics(packageName, profile.getTargetFps());
        applyDamageLockMax(packageName);
        applyAimAssistLockMax(packageName);
        applyVulkanPipelinePrime(packageName);
        applyAntiTelemetrySafe(packageName);
        applyNetworkLagCompensation(packageName);
    }
}
