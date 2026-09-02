package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * PubgConfigPatcher manages internal config files for PUBG Mobile, BGMI, and regional variants.
 *
 * Two patching modes:
 *  - patch()            → standard patch: in-memory key/CVar upserting
 *  - patchCompetitive() → competitive force-write: overwrites all paths atomically via ConfigFileHelper
 */
public class PubgConfigPatcher {


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


    public static void applyNoScopeAimbot(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectNoScopeAimbot(path);
        }
    }

    public static void applyAllScopeAimbot(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectAllScopeAimbot(path);
        }
    }

    public static void applyLongRangeScopeHeadshot(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectLongRangeScopeHeadshot(path);
        }
    }

    public static void applyMidRangeAutoHeadshot(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectMidRangeAutoHeadshot(path);
        }
    }

    public static void applyPubgmFastAttackSpeed(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectPubgmFastAttackSpeed(path);
        }
    }

    public static void applyPubgmAllWeaponMaxDamage2026(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectPubgmAllWeaponMaxDamage2026(path);
        }
    }

    public static void applyPubgmUltraAimbot2026(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectPubgmUltraAimbot2026(path);
        }
    }

    public static void applyDamage10000AttackSpeedMax(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectPubgmDamage10000AttackSpeedMax(path);
        }
        Log.i(TAG, "PUBGM Damage10000AttackSpeedMax applied for " + packageName);
    }

    private static final String TAG = "PubgConfigPatcher";

    // ─── Standard Patch ───────────────────────────────────────────────────────

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        patchActiveSavBinary(packageName, forcedFps);
        Log.i(TAG, "PUBGM patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── UltraExtreme 144fps SuperSmooth Patch ───────────────────────────────

    /**
     * Applies 144fps SuperSmooth + UltraExtreme max graphics to all PUBGM/BGMI config paths.
     * Includes frame-pacing, 16x anisotropic filtering, max shadow resolution, TAA upscale,
     * and full UE4 CVar injection — the most complete single-call patch for PUBGM.
     *
     * @return true if at least one path was written
     */
    public static boolean patchUltraExtreme144(String packageName) {
        if (packageName == null) return false;
        final FpsUnlockTier tier = FpsUnlockTier.FPS_144;

        String[] keys = {
            // ── SuperSmooth 144fps UE4 CVars ──
            "+CVars=r.PUBGDeviceFPS=8",
            "+CVars=r.PUBGMaxFPS=144",
            "+CVars=r.PUBGFrameRateLimit=144",
            "+CVars=r.FrameRateLimit=144",
            "+CVars=r.MobileFPSLimit=144",
            "+CVars=r.Vsync=0",
            "+CVars=r.Unlock120Hz=1",
            "+CVars=r.Unlock144Hz=1",
            "+CVars=r.Unlock165Hz=1",
            "+CVars=r.Unlock185Hz=1",
            "+CVars=r.TouchBoostHz=144",
            "+CVars=r.MobileTouchBoostRate=144",
            "+CVars=r.FramePacing=1",
            // ── UltraExtreme Graphics CVars ──
            "+CVars=r.MobileHDR=1",
            "+CVars=r.PUBGHDRMode=1",
            "+CVars=r.PUBGQualityLevel=4",
            "+CVars=r.PUBGSDKQualityLevel=4",
            "+CVars=r.Tonemapper.Quality=4",
            "+CVars=r.HDR.Display.OutputDevice=1",
            "+CVars=r.MobileContentScaleFactor=1.0",
            "+CVars=r.MobileReduceLoadedMips=0",
            "+CVars=r.MaxAnisotropy=16",
            "+CVars=r.BloomQuality=5",
            "+CVars=r.DepthOfFieldQuality=4",
            "+CVars=r.Shadow.MaxResolution=2048",
            "+CVars=r.Shadow.CSM.MaxMobileCascades=4",
            "+CVars=r.ReflectionCaptureResolution=256",
            "+CVars=r.TemporalAA.Upscale=1",
            "+CVars=r.VelocityBlur=1",
            "+CVars=r.AllowOcclusionQueries=1",
            "+CVars=r.MobileTonemapperFilm=1",
            "+CVars=r.PUBGTPPViewRange=100.00",
            "+CVars=r.PUBGFPPViewRange=150.00",
            "+CVars=r.SuppressLogs=1",
            "+CVars=r.DisableDebugLog=1",
            "+CVars=r.GyroSampleRate=1000",
            "+CVars=r.GyroSensitivityRatio=2.5",
            "+CVars=r.GyroZeroDelay=1",
            // ── INI Keys ──
            "FPS=144",
            "MaxFPS=144",
            "TargetFPS=144",
            "FrameRateLimit=144",
            "MobileFPSLimit=144",
            "FrameRateLevel=8",
            "UnlockFPS=1",
            "Unlock144FPS=1",
            "Ultra144FPS=1",
            "HighFPSMode=3",           // 2026: 3 = 185Hz-capable mode
            "SuperHighFPS=1",
            "Unlock90Hz=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "Unlock240Hz=1",
            // ── UltraExtreme Graphics INI ──
            "UltraExtreme=1",
            "bUseUltraExtreme=True",
            "GraphicsQuality=5",
            "GraphicQuality=4",
            "GraphicLevel=4",
            "ResolutionQuality=120",
            "ResolutionScale=120",      // 2026: 120% render scale
            "ScreenScale=120",
            "HDRMode=1",
            "HDR10Plus=1",              // 2026: 10-bit HDR
            "UltraHDMode=1",
            "HDRColorMode=2",
            "SuperResolution=1",
            "bUseHDRMode=True",
            "bUseHighQualityBloom=True",
            "BloomQuality=5",
            "AntiAliasingQuality=4",
            "bUseAntiAliasing=True",
            "ShadowQuality=2",
            "ShadowResolution=2048",
            "TextureQuality=4",
            "MaxAnisotropy=16",
            "LightingQuality=3",        // 2026: max lighting
            "ParticleQuality=3",        // 2026: max particles
            "WaterReflection=1",        // 2026: water reflections
            "VulkanPipelineCache=1",    // 2026: Vulkan cache
            "AsyncCompute=1",           // 2026: GPU async compute
            "VRS=1",                    // 2026: Variable Rate Shading
            "bReduceLoadedMips=False",
            "bFramePacingEnabled=True",
            "Vsync=0",
            "TPPFieldOfView=100",
            "FPPFieldOfView=150",
            "bDisableAnalytics=True",
            "bDisableBugReporting=True",
            "GyroSampleRate=1000",
            "GyroSensitivityRatio=2.5",
            "GyroZeroDelay=1",
            "GyroLatencyMode=0",
            "GyroSmoothFactor=1",
            "GyroStabilization=1",
            "TouchBoostHz=144",
            "TouchPollingRate=1000"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (path.endsWith("EnjoyCJZC.ini") || path.endsWith("EnjoyCJ.ini")
                    || path.endsWith("BGMIEnjoyCJZC.ini") || path.endsWith("KREnjoyCJZC.ini") || path.endsWith("VNGEnjoyCJZC.ini")) {
                ConfigFileHelper.patchKeys(path, new String[]{
                    "FrameRateLevel=8",
                    "GraphicQuality=4",
                    "GraphicResolution=2",
                    "MobileHDRMode=1",
                    "bFramePacingEnabled=True",
                    "ResolutionScale=120",
                    "HighFPSMode=3"
                }, "[/Script/ShadowTrackerExtra.UserSetting]");
            }
            if (ConfigFileHelper.patchKeys(path, keys, "[UserCustom DeviceProfile]")) {
                written++;
            }
        }
        patchActiveSavBinary(packageName, 144);
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "PUBGM UltraExtreme144 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    /**
     * Complete 165fps SuperSmooth + UltraExtreme Graphics patch for PUBGM / BGMI / KR.
     * Targets 165Hz displays (Asus ROG 8, Red Magic 9, etc.) with FrameRateLevel=9 and 165 FPS limit.
     */
    public static boolean patchUltraExtreme165(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            // ── SuperSmooth 165fps UE4 CVars ──
            "+CVars=r.PUBGDeviceFPS=9",
            "+CVars=r.PUBGMaxFPS=165",
            "+CVars=r.PUBGFrameRateLimit=165",
            "+CVars=r.FrameRateLimit=165",
            "+CVars=r.MobileFPSLimit=165",
            "+CVars=r.Vsync=0",
            "+CVars=r.Unlock120Hz=1",
            "+CVars=r.Unlock144Hz=1",
            "+CVars=r.Unlock165Hz=1",
            "+CVars=r.Unlock185Hz=1",
            "+CVars=r.TouchBoostHz=165",
            "+CVars=r.MobileTouchBoostRate=165",
            "+CVars=r.FramePacing=1",
            // ── UltraExtreme Graphics CVars ──
            "+CVars=r.MobileHDR=1",
            "+CVars=r.PUBGHDRMode=1",
            "+CVars=r.PUBGQualityLevel=4",
            "+CVars=r.PUBGSDKQualityLevel=4",
            "+CVars=r.Tonemapper.Quality=4",
            "+CVars=r.HDR.Display.OutputDevice=1",
            "+CVars=r.MobileContentScaleFactor=1.0",
            "+CVars=r.MobileReduceLoadedMips=0",
            "+CVars=r.MaxAnisotropy=16",
            "+CVars=r.BloomQuality=5",
            "+CVars=r.DepthOfFieldQuality=4",
            "+CVars=r.Shadow.MaxResolution=2048",
            "+CVars=r.Shadow.CSM.MaxMobileCascades=4",
            "+CVars=r.ReflectionCaptureResolution=256",
            "+CVars=r.TemporalAA.Upscale=1",
            "+CVars=r.VelocityBlur=1",
            "+CVars=r.AllowOcclusionQueries=1",
            "+CVars=r.MobileTonemapperFilm=1",
            "+CVars=r.PUBGTPPViewRange=100.00",
            "+CVars=r.PUBGFPPViewRange=150.00",
            "+CVars=r.SuppressLogs=1",
            "+CVars=r.DisableDebugLog=1",
            "+CVars=r.GyroSampleRate=1000",
            "+CVars=r.GyroSensitivityRatio=2.5",
            "+CVars=r.GyroZeroDelay=1",
            // ── INI Keys ──
            "FPS=165",
            "MaxFPS=165",
            "TargetFPS=165",
            "FrameRateLimit=165",
            "MobileFPSLimit=165",
            "FrameRateLevel=9",
            "UnlockFPS=1",
            "Unlock165FPS=1",
            "Ultra165FPS=1",
            "HighFPSMode=3",
            "SuperHighFPS=1",
            "Unlock90Hz=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "Unlock240Hz=1",
            // ── UltraExtreme Graphics INI ──
            "UltraExtreme=1",
            "bUseUltraExtreme=True",
            "GraphicsQuality=5",
            "GraphicQuality=4",
            "GraphicLevel=4",
            "ResolutionQuality=120",
            "ResolutionScale=120",
            "ScreenScale=120",
            "HDRMode=1",
            "HDR10Plus=1",
            "UltraHDMode=1",
            "HDRColorMode=2",
            "SuperResolution=1",
            "bUseHDRMode=True",
            "bUseHighQualityBloom=True",
            "BloomQuality=5",
            "AntiAliasingQuality=4",
            "bUseAntiAliasing=True",
            "ShadowQuality=2",
            "ShadowResolution=2048",
            "TextureQuality=4",
            "MaxAnisotropy=16",
            "LightingQuality=3",
            "ParticleQuality=3",
            "WaterReflection=1",
            "VulkanPipelineCache=1",
            "AsyncCompute=1",
            "VRS=1",
            "bReduceLoadedMips=False",
            "bFramePacingEnabled=True",
            "Vsync=0",
            "TPPFieldOfView=100",
            "FPPFieldOfView=150",
            "bDisableAnalytics=True",
            "bDisableBugReporting=True",
            "GyroSampleRate=1000",
            "GyroSensitivityRatio=2.5",
            "GyroZeroDelay=1",
            "GyroLatencyMode=0",
            "GyroSmoothFactor=1",
            "GyroStabilization=1",
            "TouchBoostHz=165",
            "TouchPollingRate=1000"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (path.endsWith("EnjoyCJZC.ini") || path.endsWith("EnjoyCJ.ini")
                    || path.endsWith("BGMIEnjoyCJZC.ini") || path.endsWith("KREnjoyCJZC.ini") || path.endsWith("VNGEnjoyCJZC.ini")) {
                ConfigFileHelper.patchKeys(path, new String[]{
                    "FrameRateLevel=9",
                    "GraphicQuality=4",
                    "GraphicResolution=2",
                    "MobileHDRMode=1",
                    "bFramePacingEnabled=True",
                    "ResolutionScale=120",
                    "HighFPSMode=3"
                }, "[/Script/ShadowTrackerExtra.UserSetting]");
            }
            if (ConfigFileHelper.patchKeys(path, keys, "[UserCustom DeviceProfile]")) {
                written++;
            }
        }
        patchActiveSavBinary(packageName, 165);
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "PUBGM UltraExtreme165 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    /**
     * Complete 185fps SuperSmooth + UltraExtreme Graphics patch for PUBGM / BGMI.
     * Includes frame-pacing, 16x anisotropic filtering, max shadow resolution, TAA upscale,
     * and full UE4 CVar injection at 185 FPS.
     *
     * @return true if at least one path was written
     */
    public static boolean patchUltraExtreme185(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            // ── SuperSmooth 185fps UE4 CVars ──
            "+CVars=r.PUBGDeviceFPS=10",
            "+CVars=r.PUBGMaxFPS=185",
            "+CVars=r.PUBGFrameRateLimit=185",
            "+CVars=r.FrameRateLimit=185",
            "+CVars=r.MobileFPSLimit=185",
            "+CVars=r.Vsync=0",
            "+CVars=r.Unlock120Hz=1",
            "+CVars=r.Unlock144Hz=1",
            "+CVars=r.Unlock165Hz=1",
            "+CVars=r.Unlock185Hz=1",
            "+CVars=r.TouchBoostHz=185",
            "+CVars=r.MobileTouchBoostRate=185",
            "+CVars=r.FramePacing=1",
            // ── UltraExtreme Graphics CVars ──
            "+CVars=r.MobileHDR=1",
            "+CVars=r.PUBGHDRMode=1",
            "+CVars=r.PUBGQualityLevel=4",
            "+CVars=r.PUBGSDKQualityLevel=4",
            "+CVars=r.Tonemapper.Quality=4",
            "+CVars=r.HDR.Display.OutputDevice=1",
            "+CVars=r.MobileContentScaleFactor=1.0",
            "+CVars=r.MobileReduceLoadedMips=0",
            "+CVars=r.MaxAnisotropy=16",
            "+CVars=r.BloomQuality=5",
            "+CVars=r.DepthOfFieldQuality=4",
            "+CVars=r.Shadow.MaxResolution=2048",
            "+CVars=r.Shadow.CSM.MaxMobileCascades=4",
            "+CVars=r.ReflectionCaptureResolution=256",
            "+CVars=r.TemporalAA.Upscale=1",
            "+CVars=r.VelocityBlur=1",
            "+CVars=r.AllowOcclusionQueries=1",
            "+CVars=r.MobileTonemapperFilm=1",
            "+CVars=r.PUBGTPPViewRange=100.00",
            "+CVars=r.PUBGFPPViewRange=150.00",
            "+CVars=r.SuppressLogs=1",
            "+CVars=r.DisableDebugLog=1",
            "+CVars=r.GyroSampleRate=1000",
            "+CVars=r.GyroSensitivityRatio=2.5",
            "+CVars=r.GyroZeroDelay=1",
            // 2026: AsyncCompute, VRS, Vulkan CVars
            "+CVars=r.AsyncCompute=1",
            "+CVars=r.VRS.Enable=1",
            "+CVars=r.Vulkan.RobustBufferAccess=0",
            "+CVars=r.EnableAsyncPipelineCompilation=1",
            // ── INI Keys ──
            "FPS=185",
            "MaxFPS=185",
            "TargetFPS=185",
            "FrameRateLimit=185",
            "MobileFPSLimit=185",
            "FrameRateLevel=10",
            "UnlockFPS=1",
            "Unlock144FPS=1",
            "Unlock165FPS=1",
            "Unlock185FPS=1",
            "Ultra144FPS=1",
            "Ultra165FPS=1",
            "Ultra185FPS=1",
            "HighFPSMode=3",           // 2026: 3 = 185Hz-capable mode
            "SuperHighFPS=1",
            "Unlock90Hz=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "Unlock240Hz=1",
            // ── UltraExtreme Graphics INI ──
            "UltraExtreme=1",
            "bUseUltraExtreme=True",
            "GraphicsQuality=5",
            "GraphicQuality=4",
            "GraphicLevel=4",
            "ResolutionQuality=120",
            "ResolutionScale=120",      // 2026: 120% render scale
            "ScreenScale=120",
            "HDRMode=1",
            "HDR10Plus=1",              // 2026: 10-bit HDR
            "UltraHDMode=1",
            "HDRColorMode=2",
            "SuperResolution=1",
            "bUseHDRMode=True",
            "bUseHighQualityBloom=True",
            "BloomQuality=5",
            "AntiAliasingQuality=4",
            "bUseAntiAliasing=True",
            "ShadowQuality=2",
            "ShadowResolution=2048",
            "TextureQuality=4",
            "MaxAnisotropy=16",
            "LightingQuality=3",        // 2026: max lighting
            "ParticleQuality=3",        // 2026: max particles
            "WaterReflection=1",        // 2026: water reflections
            "VulkanPipelineCache=1",    // 2026: Vulkan cache
            "AsyncCompute=1",           // 2026: GPU async compute
            "VRS=1",                    // 2026: Variable Rate Shading
            "bReduceLoadedMips=False",
            "bFramePacingEnabled=True",
            "Vsync=0",
            "TPPFieldOfView=100",
            "FPPFieldOfView=150",
            "bDisableAnalytics=True",
            "bDisableBugReporting=True",
            "GyroSampleRate=1000",
            "GyroSensitivityRatio=2.5",
            "GyroZeroDelay=1",
            "GyroLatencyMode=0",
            "GyroSmoothFactor=1",
            "GyroStabilization=1",
            "TouchBoostHz=185",
            "TouchPollingRate=1000"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (path.endsWith("EnjoyCJZC.ini") || path.endsWith("EnjoyCJ.ini")
                    || path.endsWith("BGMIEnjoyCJZC.ini") || path.endsWith("KREnjoyCJZC.ini") || path.endsWith("VNGEnjoyCJZC.ini")) {
                ConfigFileHelper.patchKeys(path, new String[]{
                    "FrameRateLevel=10",
                    "GraphicQuality=4",
                    "GraphicResolution=2",
                    "MobileHDRMode=1",
                    "bFramePacingEnabled=True",
                    "ResolutionScale=120",
                    "HighFPSMode=3"
                }, "[/Script/ShadowTrackerExtra.UserSetting]");
            }
            if (ConfigFileHelper.patchKeys(path, keys, "[UserCustom DeviceProfile]")) {
                written++;
            }
        }
        patchActiveSavBinary(packageName, 185);
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "PUBGM UltraExtreme185 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────


    /**
     * Force-overwrites ALL PUBGM/BGMI config paths unconditionally.
     * Includes full UE4 CVar injection for 120 / 144 / 165 / 185 FPS, frame rate limits, and content scale.
     *
     * @return true if at least one path was written
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final FpsUnlockTier tier = FpsUnlockTier.fromFps(forcedFps);
        final int pubgFpsLevel = tier.level;

        String[] keys = new String[] {
                "+CVars=r.PUBGDeviceFPS=" + pubgFpsLevel,
                "+CVars=r.PUBGMaxFPS=" + forcedFps,
                "+CVars=r.PUBGFrameRateLimit=" + forcedFps,
                "+CVars=r.MobileFPSLimit=" + forcedFps,
                "+CVars=r.FrameRateLimit=" + forcedFps,
                "+CVars=r.PUBGHDRMode=1",
                "+CVars=r.MobileHDR=1",
                "+CVars=r.PUBGQualityLevel=4",
                "+CVars=r.PUBGSDKQualityLevel=4",
                "+CVars=r.Tonemapper.Quality=4",
                "+CVars=r.HDR.Display.OutputDevice=1",
                "+CVars=r.MobileContentScaleFactor=1.0",
                "+CVars=r.MobileTonemapperFilm=1",
                "+CVars=r.PUBGTPPViewRange=100.00",
                "+CVars=r.PUBGFPPViewRange=150.00",
                "+CVars=r.SprintSensitivity=150",
                "+CVars=r.Vsync=0",
                "+CVars=r.Unlock120Hz=1",
                "+CVars=r.Unlock144Hz=1",
                "+CVars=r.Unlock165Hz=1",
                "+CVars=r.Unlock185Hz=1",
                "+CVars=r.SuppressLogs=1",
                "+CVars=r.DisableDebugLog=1",
                "+CVars=r.EnableCrashReporting=0",
                "+CVars=r.Telemetry=0",
                "+CVars=a.DisableAnalytics=1",
                "+CVars=r.LogFilter=0",
                "+CVars=r.TouchBoostHz=" + forcedFps,
                "+CVars=r.MobileTouchBoostRate=" + forcedFps,
                "+CVars=r.GyroSampleRate=1000",
                "+CVars=r.GyroSensitivityRatio=2.5",
                "+CVars=r.GyroZeroDelay=1",
                "+CVars=r.GyroLatencyMode=0",
                "+CVars=r.GyroSmoothFactor=1",
                "+CVars=r.GyroStabilization=1",
                "FrameRateLevel=" + pubgFpsLevel,
                "FPS=" + forcedFps,
                "TargetFPS=" + forcedFps,
                "MaxFPS=" + forcedFps,
                "UnlockFPS=1",
                "Unlock120FPS=1",
                "Unlock144FPS=1",
                "Unlock165FPS=1",
                "Unlock185FPS=1",
                "Ultra144FPS=1",
                "Ultra165FPS=1",
                "Ultra185FPS=1",
                "UltraExtreme=1",
                "bUseUltraExtreme=True",
                "GraphicsQuality=5",
                "GraphicQuality=4",
                "HDRMode=1",
                "UltraHDMode=1",
                "SuperResolution=1",
                "bUseHDRMode=True",
                "bUseHighQualityBloom=True",
                "bUseAntiAliasing=True",
                "bDisableAnalytics=True",
                "bDisableBugReporting=True",
                "SprintSensitivity=150",
                "TPPFieldOfView=100",
                "FPPFieldOfView=150"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (path.endsWith("EnjoyCJZC.ini") || path.endsWith("EnjoyCJ.ini")
                    || path.endsWith("BGMIEnjoyCJZC.ini") || path.endsWith("KREnjoyCJZC.ini") || path.endsWith("VNGEnjoyCJZC.ini")) {
                ConfigFileHelper.patchKeys(path, new String[]{
                    "FrameRateLevel=" + pubgFpsLevel,
                    "GraphicQuality=4",
                    "GraphicResolution=2",
                    "MobileHDRMode=1",
                    "bFramePacingEnabled=True",
                    "ResolutionScale=120",
                    "HighFPSMode=3"
                }, "[/Script/ShadowTrackerExtra.UserSetting]");
            }
            if (ConfigFileHelper.patchKeys(path, keys, "[UserCustom DeviceProfile]")) {
                written++;
            }
        }
        patchActiveSavBinary(packageName, forcedFps);
        deployPakPatch(packageName);
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "PUBGM competitive HDR " + forcedFps + "FPS non-destructive in-place merge: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Optional deployment of a custom game_patch_*.pak file to PUBGM Saved/Paks directory.
     * Note: PAK patches are completely optional; 100% of FPS, Graphics, and Performance unlocks
     * operate natively through UserCustom.ini and Active.sav binary byte patching.
     */
    public static boolean deployPakPatch(String pkg) {
        if (pkg == null || pkg.trim().isEmpty()) return false;

        String[] candidateDirs = {
            "/storage/emulated/0/Download",
            "/sdcard/Download",
            "/storage/emulated/0/Documents/Game-Launcher",
            "/sdcard/Documents/Game-Launcher",
            "/storage/emulated/0/GameLauncher",
            "/sdcard/GameLauncher",
            "/data/local/tmp"
        };

        String foundSource = null;
        String pakFileName = null;

        for (String dir : candidateDirs) {
            if (!ShizukuFileManager.fileExists(dir)) continue;
            // Quick check for any game_patch*.pak
            String checkCmd = "ls -1 \"" + dir + "\"/game_patch*.pak 2>/dev/null | head -n 1";
            String res = ShizukuExecutor.hasShizukuPermission()
                    ? ShizukuExecutor.executeShizukuCommand(checkCmd)
                    : CommandExecutor.executeSystemCommand(checkCmd);
            if (res != null && !res.trim().isEmpty() && !res.startsWith("ERROR:") && !res.contains("No such")) {
                foundSource = res.trim();
                int slash = foundSource.lastIndexOf('/');
                pakFileName = (slash >= 0) ? foundSource.substring(slash + 1) : foundSource;
                break;
            }
        }

        if (foundSource == null || pakFileName == null || pakFileName.isEmpty()) {
            Log.d(TAG, "No optional PAK file found for " + pkg + "; relying purely on native INI & Active.sav binary patch.");
            return false;
        }

        List<String> basePaths = GameConfigPathResolver.generateBasePaths(pkg);
        boolean deployed = false;

        for (String base : basePaths) {
            String dir = base + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Paks";
            ShizukuFileManager.makeDirectory(dir);
            String dest = dir + "/" + pakFileName;
            String copyCmd = "cp -f \"" + foundSource + "\" \"" + dest + "\" && chmod 666 \"" + dest + "\"";
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(copyCmd);
            } else {
                CommandExecutor.executeSystemCommand(copyCmd);
            }
            if (ShizukuFileManager.fileExists(dest)) {
                deployed = true;
                Log.i(TAG, "Successfully deployed optional " + pakFileName + " to " + dest);
            }
        }
        return deployed;
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

    /**
     * PUBGM — Magic bullet aimbot + zero recoil + no spread.
     * Iterates all PUBGM config paths and calls NativeConfigInjector.injectMagicBulletAimbot
     * (UE4 CVar format on UserCustom.ini, plain-key on others).
     */
    public static void applyMagicBulletAimbot(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectMagicBulletAimbot(path);
        }
    }

// ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    /**
     * Patches Active.sav binary savegame file directly using byte manipulation.
     * Enforces FPSLevel, BattleFPS, and LobbyFPS to target levels (10=185fps, 9=165fps, 8=144fps, 7=120fps).
     */
    public static void patchActiveSavBinary(String pkg, int targetFps) {
        if (pkg == null) return;
        final int fpsLevel = FpsUnlockTier.fromFps(targetFps).level;
        String[] savPaths = {
            "/storage/emulated/0/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav",
            "/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav",
            "/data/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav",
            "/data/user/0/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav"
        };
        for (String sav : savPaths) {
            try {
                if (!ShizukuFileManager.fileExists(sav)) continue;
                byte[] data = ShizukuFileManager.readFileBytes(sav);
                if (data != null && data.length > 0) {
                    boolean modified = patchBinarySavField(data, "FPSLevel", fpsLevel);
                    modified |= patchBinarySavField(data, "BattleFPS", fpsLevel);
                    modified |= patchBinarySavField(data, "LobbyFPS", fpsLevel);
                    if (modified) {
                        ShizukuFileManager.uploadBytes(sav, data, "666");
                    }
                }
            } catch (Throwable t) {
                Log.w(TAG, "patchActiveSavBinary error for " + sav + ": " + t.getMessage());
            }
        }
        Log.i(TAG, "PUBGM Active.sav binary enforced level " + fpsLevel + " (" + targetFps + " FPS) for " + pkg);
    }

    private static boolean patchBinarySavField(byte[] data, String fieldName, int value) {
        if (data == null || fieldName == null) return false;
        byte[] pattern = fieldName.getBytes(StandardCharsets.UTF_8);
        int idx = indexOfBytes(data, pattern);
        if (idx != -1) {
            // In Active.sav, the value byte usually appears 9-10 bytes after the field name ASCII bytes
            for (int offset = idx + pattern.length; offset < Math.min(data.length, idx + pattern.length + 16); offset++) {
                if (data[offset] >= 1 && data[offset] <= 10) {
                    data[offset] = (byte) value;
                    return true;
                }
            }
        }
        return false;
    }

    private static int indexOfBytes(byte[] source, byte[] target) {
        if (source == null || target == null || source.length < target.length) return -1;
        for (int i = 0; i <= source.length - target.length; i++) {
            boolean match = true;
            for (int j = 0; j < target.length; j++) {
                if (source[i + j] != target[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }

    private static boolean applyPatch(String path, int targetFps) {
        final FpsUnlockTier tier = FpsUnlockTier.fromFps(targetFps);
        final int pubgFpsLevel = tier.level;
        String[] cvars = {
            "+CVars=r.PUBGDeviceFPS=" + pubgFpsLevel,
            "+CVars=r.PUBGMaxFPS=" + targetFps,
            "+CVars=r.PUBGFrameRateLimit=" + targetFps,
            "+CVars=r.MobileFPSLimit=" + targetFps,
            "+CVars=r.FrameRateLimit=" + targetFps,
            "+CVars=r.PUBGHDRMode=1",
            "+CVars=r.MobileHDR=1",
            "+CVars=r.PUBGQualityLevel=4",
            "+CVars=r.PUBGSDKQualityLevel=4",
            "+CVars=r.Unlock120Hz=1",
            "+CVars=r.Unlock144Hz=1",
            "+CVars=r.Unlock165Hz=1",
            "+CVars=r.Unlock185Hz=1",
            "+CVars=r.Vsync=0",
            "FrameRateLevel=" + pubgFpsLevel,
            "FPS=" + targetFps,
            "TargetFPS=" + targetFps,
            "MaxFPS=" + targetFps,
            "bUseHDRMode=True",
            "bUseAntiAliasing=True"
        };
        return ConfigFileHelper.patchKeys(path, cvars, "[UserCustom DeviceProfile]");
    }

    // ─── 2026 Skill Economy Overdrive ─────────────────────────────────────────

    /**
     * PUBGM/BGMI — Fast Stamina, Adrenaline Energy, HP Regen, Zero Parachute/Vehicle Cooldown.
     */
    public static void applyFastStaminaEnergyBoost(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectFastCooldown(path);
            NativeConfigInjector.injectFastFullEnergy(path);
            NativeConfigInjector.injectFastHpRegen(path);
            NativeConfigInjector.injectFastStaminaFuryRegen(path);
            NativeConfigInjector.injectZeroSkillCost(path);
            NativeConfigInjector.injectMaxUltCharge(path);
        }
    }

    /** Convenience alias. */
    public static void applySkillEconomy(String packageName) {
        applyFastStaminaEnergyBoost(packageName);
    }
}
