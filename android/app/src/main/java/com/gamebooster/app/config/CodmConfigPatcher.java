package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * CodmConfigPatcher manages internal config files for Call of Duty Mobile (all versions/regions).
 *
 * Two patching modes:
 *  - patch()            → standard patch: in-memory key/JSON/XML upserting
 *  - patchCompetitive() → competitive force-write: overwrites all paths atomically via ConfigFileHelper
 */
public class CodmConfigPatcher {


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
            NativeConfigInjector.injectCodmNoScopeAimbot(path);
        }
    }

    public static void applyAllScopeAimbot(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectCodmAllScopeAimbot(path);
        }
    }

    public static void applyLongRangeHeadshot(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectCodmLongRangeHeadshot(path);
        }
    }

    public static void applyMidRangeHeadshot(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectCodmMidRangeHeadshot(path);
        }
    }

    public static void applyFastAttackSpeed(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectCodmFastAttackSpeed(path);
        }
    }

    public static void applyFastLootAndWeaponSwap(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectFastLootAndWeaponSwap(path);
        }
    }

    public static void applyInstantSprintTurbo(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectInstantSprintTurbo(path);
        }
    }

    public static void applyMultiRangeHeadshotCalibration(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectMultiRangeHeadshotCalibration(path);
        }
    }

    public static void applyUniversalZeroDelaySkillTapAllHero(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectUniversalZeroDelaySkillTapAllHero(path);
        }
    }

    public static void applyCodmMaxDamageAllWeapon2026(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectCodmMaxDamageAllWeapon2026(path);
        }
    }

    public static void applyCodmUltraConfigCheat2026(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectCodmUltraConfigCheat2026(path);
        }
    }

    public static void applyDamage10000AttackSpeedMax(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectCodmDamage10000AttackSpeedMax(path);
        }
        Log.i(TAG, "CODM Damage10000AttackSpeedMax applied for " + packageName);
    }

    public static void applyCodmBsaRemovalRangeOverdrive(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectCodmBsaRemovalRangeOverdrive(path);
        }
    }

    public static void applyFastLoadShaderBypass(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectCodmFastLoadShaderBypass(path);
        }
    }

    private static final String TAG = "CodmConfigPatcher";

    // ─── Standard Patch ───────────────────────────────────────────────────────

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "CODM patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── UltraExtreme 144fps SuperSmooth Patch ───────────────────────────────

    /**
     * Force-writes 144fps SuperSmooth + UltraExtreme max graphics for CODM.
     * Outputs correct JSON for UserSetting.json, XML for PlayerPrefs, and INI for GraphicsSettings.ini.
     */
    public static boolean patchUltraExtreme144(String packageName) {
        if (packageName == null) return false;

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            String content;
            if (path.endsWith(".json")) {
                content = "{\n"
                    + "  \"MaxFrameRate\": 144,\n"
                    + "  \"TargetFPS\": 144,\n"
                    + "  \"FPSLimit\": 144,\n"
                    + "  \"FrameRateLimit\": 144,\n"
                    + "  \"MobileFPSLimit\": 144,\n"
                    + "  \"GraphicQuality\": 4,\n"
                    + "  \"TextureQuality\": 4,\n"
                    + "  \"ShadowQuality\": 2,\n"
                    + "  \"ShadowResolution\": 2048,\n"
                    + "  \"AntiAliasingQuality\": 4,\n"
                    + "  \"BloomQuality\": 5,\n"
                    + "  \"MaxAnisotropy\": 16,\n"
                    + "  \"LightingQuality\": 3,\n"
                    + "  \"ParticleQuality\": 3,\n"
                    + "  \"WaterReflection\": 1,\n"
                    + "  \"AsyncCompute\": 1,\n"
                    + "  \"VRS\": 1,\n"
                    + "  \"VulkanPipelineCache\": 1,\n"
                    + "  \"HDRMode\": 1,\n"
                    + "  \"HDR10Plus\": 1,\n"
                    + "  \"HDRColorMode\": 2,\n"
                    + "  \"UltraHDMode\": 1,\n"
                    + "  \"SuperResolution\": 1,\n"
                    + "  \"ResolutionScale\": 120,\n"
                    + "  \"UltraExtreme\": 1,\n"
                    + "  \"bFramePacingEnabled\": 1,\n"
                    + "  \"Vsync\": 0,\n"
                    + "  \"Unlock90Hz\": 1,\n"
                    + "  \"Unlock120Hz\": 1,\n"
                    + "  \"Unlock144Hz\": 1,\n"
                    + "  \"Unlock165Hz\": 1,\n"
                    + "  \"Unlock185Hz\": 1,\n"
                    + "  \"Unlock240Hz\": 1,\n"
                    + "  \"Unlock144FPS\": 1,\n"
                    + "  \"Ultra144FPS\": 1,\n"
                    + "  \"TouchBoostHz\": 144,\n"
                    + "  \"TouchPollingRate\": 1000,\n"
                    + "  \"TouchZeroDelay\": 1,\n"
                    + "  \"GyroSampleRate\": 1000,\n"
                    + "  \"GyroSensitivityRatio\": 2.5,\n"
                    + "  \"GyroZeroDelay\": 1,\n"
                    + "  \"GyroSmoothFactor\": 1,\n"
                    + "  \"GyroStabilization\": 1,\n"
                    + "  \"GyroLatencyMode\": 0,\n"
                    + "  \"PreloadShaders\": 1,\n"
                    + "  \"AllowOcclusionQueries\": 1\n"
                    + "}\n";
            } else if (path.endsWith(".xml")) {
                content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n"
                    + "<map>\n"
                    + "  <int name=\"MaxFrameRate\" value=\"144\" />\n"
                    + "  <int name=\"TargetFPS\" value=\"144\" />\n"
                    + "  <int name=\"FPSLimit\" value=\"144\" />\n"
                    + "  <int name=\"FrameRateLimit\" value=\"144\" />\n"
                    + "  <int name=\"MobileFPSLimit\" value=\"144\" />\n"
                    + "  <int name=\"GraphicQuality\" value=\"4\" />\n"
                    + "  <int name=\"TextureQuality\" value=\"4\" />\n"
                    + "  <int name=\"ShadowQuality\" value=\"2\" />\n"
                    + "  <int name=\"ShadowResolution\" value=\"2048\" />\n"
                    + "  <int name=\"AntiAliasingQuality\" value=\"4\" />\n"
                    + "  <int name=\"BloomQuality\" value=\"5\" />\n"
                    + "  <int name=\"MaxAnisotropy\" value=\"16\" />\n"
                    + "  <int name=\"HDRMode\" value=\"1\" />\n"
                    + "  <int name=\"UltraHDMode\" value=\"1\" />\n"
                    + "  <int name=\"SuperResolution\" value=\"1\" />\n"
                    + "  <int name=\"ResolutionScale\" value=\"120\" />\n"
                    + "  <int name=\"UltraExtreme\" value=\"1\" />\n"
                    + "  <int name=\"Vsync\" value=\"0\" />\n"
                    + "  <int name=\"Unlock120Hz\" value=\"1\" />\n"
                    + "  <int name=\"Unlock144Hz\" value=\"1\" />\n"
                    + "  <int name=\"Unlock165Hz\" value=\"1\" />\n"
                    + "  <int name=\"Unlock185Hz\" value=\"1\" />\n"
                    + "  <int name=\"Unlock144FPS\" value=\"1\" />\n"
                    + "  <int name=\"Ultra144FPS\" value=\"1\" />\n"
                    + "  <int name=\"TouchBoostHz\" value=\"144\" />\n"
                    + "  <int name=\"TouchPollingRate\" value=\"1000\" />\n"
                    + "  <int name=\"GyroSampleRate\" value=\"1000\" />\n"
                    + "  <float name=\"GyroSensitivityRatio\" value=\"2.5\" />\n"
                    + "  <int name=\"GyroZeroDelay\" value=\"1\" />\n"
                    + "  <int name=\"PreloadShaders\" value=\"1\" />\n"
                    + "</map>\n";
            } else {
                content = "[Graphics]\n"
                    + "MaxFrameRate=144\n"
                    + "TargetFPS=144\n"
                    + "FPSLimit=144\n"
                    + "FrameRateLimit=144\n"
                    + "MobileFPSLimit=144\n"
                    + "FrameRateLevel=8\n"
                    + "UnlockFPS=1\n"
                    + "Unlock144FPS=1\n"
                    + "Ultra144FPS=1\n"
                    + "HighFPSMode=3\n"     // 2026: 3 = 185Hz-capable
                    + "Unlock90Hz=1\n"
                    + "Unlock120Hz=1\n"
                    + "Unlock144Hz=1\n"
                    + "Unlock165Hz=1\n"
                    + "Unlock185Hz=1\n"
                    + "Unlock240Hz=1\n"
                    + "GraphicQuality=4\n"
                    + "TextureQuality=4\n"
                    + "ShadowQuality=2\n"
                    + "ShadowResolution=2048\n"
                    + "AntiAliasingQuality=4\n"
                    + "BloomQuality=5\n"
                    + "MaxAnisotropy=16\n"
                    + "LightingQuality=3\n"
                    + "ParticleQuality=3\n"
                    + "WaterReflection=1\n"
                    + "VulkanPipelineCache=1\n"
                    + "AsyncCompute=1\n"
                    + "VRS=1\n"
                    + "HDRMode=1\n"
                    + "HDR10Plus=1\n"
                    + "HDRColorMode=2\n"
                    + "UltraHDMode=1\n"
                    + "SuperResolution=1\n"
                    + "ResolutionScale=120\n"
                    + "UltraExtreme=1\n"
                    + "bUseUltraExtreme=True\n"
                    + "bFramePacingEnabled=True\n"
                    + "Vsync=0\n"
                    + "TouchBoostHz=144\n"
                    + "TouchPollingRate=1000\n"
                    + "TouchZeroDelay=1\n"
                    + "GyroSampleRate=1000\n"
                    + "GyroSensitivityRatio=2.5\n"
                    + "GyroZeroDelay=1\n"
                    + "GyroSmoothFactor=1\n"
                    + "GyroStabilization=1\n"
                    + "GyroLatencyMode=0\n"
                    + "PreloadShaders=1\n";
            }
            if (ConfigFileHelper.writeContentAtomic(path, content)) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "CODM UltraExtreme144 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    /**
     * Injects 165 FPS and Ultra Graphics presets for CODM.
     * Targets 165Hz displays (Asus ROG, Red Magic, Black Shark).
     */
    public static boolean patchUltraExtreme165(String packageName) {
        if (packageName == null) return false;

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            String content;
            if (path.endsWith(".json")) {
                content = "{\n"
                    + "  \"MaxFrameRate\": 165,\n"
                    + "  \"TargetFPS\": 165,\n"
                    + "  \"FPSLimit\": 165,\n"
                    + "  \"FrameRateLimit\": 165,\n"
                    + "  \"MobileFPSLimit\": 165,\n"
                    + "  \"FrameRateLevel\": 9,\n"
                    + "  \"GraphicQuality\": 4,\n"
                    + "  \"TextureQuality\": 4,\n"
                    + "  \"ShadowQuality\": 2,\n"
                    + "  \"ShadowResolution\": 2048,\n"
                    + "  \"AntiAliasingQuality\": 4,\n"
                    + "  \"BloomQuality\": 5,\n"
                    + "  \"MaxAnisotropy\": 16,\n"
                    + "  \"LightingQuality\": 3,\n"
                    + "  \"ParticleQuality\": 3,\n"
                    + "  \"WaterReflection\": 1,\n"
                    + "  \"AsyncCompute\": 1,\n"
                    + "  \"VRS\": 1,\n"
                    + "  \"VulkanPipelineCache\": 1,\n"
                    + "  \"HDRMode\": 1,\n"
                    + "  \"HDR10Plus\": 1,\n"
                    + "  \"HDRColorMode\": 2,\n"
                    + "  \"UltraHDMode\": 1,\n"
                    + "  \"SuperResolution\": 1,\n"
                    + "  \"ResolutionScale\": 120,\n"
                    + "  \"UltraExtreme\": 1,\n"
                    + "  \"bFramePacingEnabled\": 1,\n"
                    + "  \"Vsync\": 0,\n"
                    + "  \"Unlock90Hz\": 1,\n"
                    + "  \"Unlock120Hz\": 1,\n"
                    + "  \"Unlock144Hz\": 1,\n"
                    + "  \"Unlock165Hz\": 1,\n"
                    + "  \"Unlock185Hz\": 1,\n"
                    + "  \"Unlock240Hz\": 1,\n"
                    + "  \"Unlock144FPS\": 1,\n"
                    + "  \"Unlock165FPS\": 1,\n"
                    + "  \"Ultra144FPS\": 1,\n"
                    + "  \"Ultra165FPS\": 1,\n"
                    + "  \"TouchBoostHz\": 165,\n"
                    + "  \"TouchPollingRate\": 1000,\n"
                    + "  \"TouchZeroDelay\": 1,\n"
                    + "  \"GyroSampleRate\": 1000,\n"
                    + "  \"GyroSensitivityRatio\": 2.5,\n"
                    + "  \"GyroZeroDelay\": 1,\n"
                    + "  \"GyroSmoothFactor\": 1,\n"
                    + "  \"GyroStabilization\": 1,\n"
                    + "  \"PreloadShaders\": 1,\n"
                    + "  \"AllowOcclusionQueries\": 1\n"
                    + "}\n";
            } else if (path.endsWith(".xml")) {
                content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n"
                    + "<map>\n"
                    + "  <int name=\"MaxFrameRate\" value=\"165\" />\n"
                    + "  <int name=\"TargetFPS\" value=\"165\" />\n"
                    + "  <int name=\"FPSLimit\" value=\"165\" />\n"
                    + "  <int name=\"FrameRateLimit\" value=\"165\" />\n"
                    + "  <int name=\"MobileFPSLimit\" value=\"165\" />\n"
                    + "  <int name=\"GraphicQuality\" value=\"4\" />\n"
                    + "  <int name=\"TextureQuality\" value=\"4\" />\n"
                    + "  <int name=\"ShadowQuality\" value=\"2\" />\n"
                    + "  <int name=\"ShadowResolution\" value=\"2048\" />\n"
                    + "  <int name=\"AntiAliasingQuality\" value=\"4\" />\n"
                    + "  <int name=\"BloomQuality\" value=\"5\" />\n"
                    + "  <int name=\"MaxAnisotropy\" value=\"16\" />\n"
                    + "  <int name=\"HDRMode\" value=\"1\" />\n"
                    + "  <int name=\"HDR10Plus\" value=\"1\" />\n"
                    + "  <int name=\"UltraHDMode\" value=\"1\" />\n"
                    + "  <int name=\"SuperResolution\" value=\"1\" />\n"
                    + "  <int name=\"ResolutionScale\" value=\"120\" />\n"
                    + "  <int name=\"UltraExtreme\" value=\"1\" />\n"
                    + "  <int name=\"Vsync\" value=\"0\" />\n"
                    + "  <int name=\"Unlock120Hz\" value=\"1\" />\n"
                    + "  <int name=\"Unlock144Hz\" value=\"1\" />\n"
                    + "  <int name=\"Unlock165Hz\" value=\"1\" />\n"
                    + "  <int name=\"Unlock185Hz\" value=\"1\" />\n"
                    + "  <int name=\"Unlock144FPS\" value=\"1\" />\n"
                    + "  <int name=\"Unlock165FPS\" value=\"1\" />\n"
                    + "  <int name=\"Ultra144FPS\" value=\"1\" />\n"
                    + "  <int name=\"Ultra165FPS\" value=\"1\" />\n"
                    + "  <int name=\"TouchBoostHz\" value=\"165\" />\n"
                    + "  <int name=\"TouchPollingRate\" value=\"1000\" />\n"
                    + "  <int name=\"GyroSampleRate\" value=\"1000\" />\n"
                    + "  <float name=\"GyroSensitivityRatio\" value=\"2.5\" />\n"
                    + "  <int name=\"GyroZeroDelay\" value=\"1\" />\n"
                    + "  <int name=\"PreloadShaders\" value=\"1\" />\n"
                    + "</map>\n";
            } else {
                content = "[Graphics]\n"
                    + "MaxFrameRate=165\n"
                    + "TargetFPS=165\n"
                    + "FPSLimit=165\n"
                    + "FrameRateLimit=165\n"
                    + "MobileFPSLimit=165\n"
                    + "FrameRateLevel=9\n"
                    + "UnlockFPS=1\n"
                    + "Unlock165FPS=1\n"
                    + "Ultra165FPS=1\n"
                    + "HighFPSMode=3\n"
                    + "Unlock90Hz=1\n"
                    + "Unlock120Hz=1\n"
                    + "Unlock144Hz=1\n"
                    + "Unlock165Hz=1\n"
                    + "Unlock185Hz=1\n"
                    + "Unlock240Hz=1\n"
                    + "GraphicQuality=4\n"
                    + "TextureQuality=4\n"
                    + "ShadowQuality=2\n"
                    + "ShadowResolution=2048\n"
                    + "AntiAliasingQuality=4\n"
                    + "BloomQuality=5\n"
                    + "MaxAnisotropy=16\n"
                    + "LightingQuality=3\n"
                    + "ParticleQuality=3\n"
                    + "WaterReflection=1\n"
                    + "VulkanPipelineCache=1\n"
                    + "AsyncCompute=1\n"
                    + "VRS=1\n"
                    + "HDRMode=1\n"
                    + "HDR10Plus=1\n"
                    + "HDRColorMode=2\n"
                    + "UltraHDMode=1\n"
                    + "SuperResolution=1\n"
                    + "ResolutionScale=120\n"
                    + "UltraExtreme=1\n"
                    + "bUseUltraExtreme=True\n"
                    + "bFramePacingEnabled=True\n"
                    + "Vsync=0\n"
                    + "TouchBoostHz=165\n"
                    + "TouchPollingRate=1000\n"
                    + "TouchZeroDelay=1\n"
                    + "GyroSampleRate=1000\n"
                    + "GyroSensitivityRatio=2.5\n"
                    + "GyroZeroDelay=1\n"
                    + "GyroSmoothFactor=1\n"
                    + "GyroStabilization=1\n"
                    + "GyroLatencyMode=0\n"
                    + "PreloadShaders=1\n";
            }
            if (ConfigFileHelper.writeContentAtomic(path, content)) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "CODM UltraExtreme165 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    /**
     * Injects 185 FPS and Ultra Graphics presets for CODM.
     */
    public static boolean patchUltraExtreme185(String packageName) {
        if (packageName == null) return false;

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            String content;
            if (path.endsWith(".json")) {
                content = "{\n"
                    + "  \"MaxFrameRate\": 185,\n"
                    + "  \"TargetFPS\": 185,\n"
                    + "  \"FPSLimit\": 185,\n"
                    + "  \"FrameRateLimit\": 185,\n"
                    + "  \"MobileFPSLimit\": 185,\n"
                    + "  \"GraphicQuality\": 4,\n"
                    + "  \"TextureQuality\": 4,\n"
                    + "  \"ShadowQuality\": 2,\n"
                    + "  \"ShadowResolution\": 2048,\n"
                    + "  \"AntiAliasingQuality\": 4,\n"
                    + "  \"BloomQuality\": 5,\n"
                    + "  \"MaxAnisotropy\": 16,\n"
                    + "  \"LightingQuality\": 3,\n"
                    + "  \"ParticleQuality\": 3,\n"
                    + "  \"WaterReflection\": 1,\n"
                    + "  \"AsyncCompute\": 1,\n"
                    + "  \"VRS\": 1,\n"
                    + "  \"VulkanPipelineCache\": 1,\n"
                    + "  \"HDRMode\": 1,\n"
                    + "  \"HDR10Plus\": 1,\n"
                    + "  \"HDRColorMode\": 2,\n"
                    + "  \"UltraHDMode\": 1,\n"
                    + "  \"SuperResolution\": 1,\n"
                    + "  \"ResolutionScale\": 120,\n"
                    + "  \"UltraExtreme\": 1,\n"
                    + "  \"bFramePacingEnabled\": 1,\n"
                    + "  \"Vsync\": 0,\n"
                    + "  \"Unlock90Hz\": 1,\n"
                    + "  \"Unlock120Hz\": 1,\n"
                    + "  \"Unlock144Hz\": 1,\n"
                    + "  \"Unlock165Hz\": 1,\n"
                    + "  \"Unlock185Hz\": 1,\n"
                    + "  \"Unlock240Hz\": 1,\n"
                    + "  \"Unlock144FPS\": 1,\n"
                    + "  \"Unlock165FPS\": 1,\n"
                    + "  \"Unlock185FPS\": 1,\n"
                    + "  \"Ultra144FPS\": 1,\n"
                    + "  \"Ultra165FPS\": 1,\n"
                    + "  \"Ultra185FPS\": 1,\n"
                    + "  \"TouchBoostHz\": 185,\n"
                    + "  \"TouchPollingRate\": 1000,\n"
                    + "  \"TouchZeroDelay\": 1,\n"
                    + "  \"GyroSampleRate\": 1000,\n"
                    + "  \"GyroSensitivityRatio\": 2.5,\n"
                    + "  \"GyroZeroDelay\": 1,\n"
                    + "  \"GyroSmoothFactor\": 1,\n"
                    + "  \"GyroStabilization\": 1,\n"
                    + "  \"PreloadShaders\": 1,\n"
                    + "  \"AllowOcclusionQueries\": 1\n"
                    + "}\n";
            } else if (path.endsWith(".xml")) {
                content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n"
                    + "<map>\n"
                    + "  <int name=\"MaxFrameRate\" value=\"185\" />\n"
                    + "  <int name=\"TargetFPS\" value=\"185\" />\n"
                    + "  <int name=\"FPSLimit\" value=\"185\" />\n"
                    + "  <int name=\"FrameRateLimit\" value=\"185\" />\n"
                    + "  <int name=\"MobileFPSLimit\" value=\"185\" />\n"
                    + "  <int name=\"GraphicQuality\" value=\"4\" />\n"
                    + "  <int name=\"TextureQuality\" value=\"4\" />\n"
                    + "  <int name=\"ShadowQuality\" value=\"2\" />\n"
                    + "  <int name=\"ShadowResolution\" value=\"2048\" />\n"
                    + "  <int name=\"AntiAliasingQuality\" value=\"4\" />\n"
                    + "  <int name=\"BloomQuality\" value=\"5\" />\n"
                    + "  <int name=\"MaxAnisotropy\" value=\"16\" />\n"
                    + "  <int name=\"HDRMode\" value=\"1\" />\n"
                    + "  <int name=\"UltraHDMode\" value=\"1\" />\n"
                    + "  <int name=\"SuperResolution\" value=\"1\" />\n"
                    + "  <int name=\"ResolutionScale\" value=\"120\" />\n"
                    + "  <int name=\"UltraExtreme\" value=\"1\" />\n"
                    + "  <int name=\"Vsync\" value=\"0\" />\n"
                    + "  <int name=\"Unlock120Hz\" value=\"1\" />\n"
                    + "  <int name=\"Unlock144Hz\" value=\"1\" />\n"
                    + "  <int name=\"Unlock165Hz\" value=\"1\" />\n"
                    + "  <int name=\"Unlock185Hz\" value=\"1\" />\n"
                    + "  <int name=\"Unlock144FPS\" value=\"1\" />\n"
                    + "  <int name=\"Unlock165FPS\" value=\"1\" />\n"
                    + "  <int name=\"Unlock185FPS\" value=\"1\" />\n"
                    + "  <int name=\"Ultra144FPS\" value=\"1\" />\n"
                    + "  <int name=\"Ultra165FPS\" value=\"1\" />\n"
                    + "  <int name=\"Ultra185FPS\" value=\"1\" />\n"
                    + "  <int name=\"TouchBoostHz\" value=\"185\" />\n"
                    + "  <int name=\"TouchPollingRate\" value=\"1000\" />\n"
                    + "  <int name=\"GyroSampleRate\" value=\"1000\" />\n"
                    + "  <float name=\"GyroSensitivityRatio\" value=\"2.5\" />\n"
                    + "  <int name=\"GyroZeroDelay\" value=\"1\" />\n"
                    + "  <int name=\"PreloadShaders\" value=\"1\" />\n"
                    + "</map>\n";
            } else {
                content = "[Graphics]\n"
                    + "MaxFrameRate=185\n"
                    + "TargetFPS=185\n"
                    + "FPSLimit=185\n"
                    + "FrameRateLimit=185\n"
                    + "MobileFPSLimit=185\n"
                    + "FrameRateLevel=10\n"
                    + "GraphicQuality=4\n"
                    + "TextureQuality=4\n"
                    + "ShadowQuality=2\n"
                    + "ShadowResolution=2048\n"
                    + "AntiAliasingQuality=4\n"
                    + "BloomQuality=5\n"
                    + "MaxAnisotropy=16\n"
                    + "HDRMode=1\n"
                    + "HDR10Plus=1\n"
                    + "HDRColorMode=2\n"
                    + "UltraHDMode=1\n"
                    + "SuperResolution=1\n"
                    + "ResolutionScale=120\n"
                    + "UltraExtreme=1\n"
                    + "bUseUltraExtreme=True\n"
                    + "bFramePacingEnabled=True\n"
                    + "Vsync=0\n"
                    + "Unlock120Hz=1\n"
                    + "Unlock144Hz=1\n"
                    + "Unlock165Hz=1\n"
                    + "Unlock185Hz=1\n"
                    + "Unlock144FPS=1\n"
                    + "Unlock165FPS=1\n"
                    + "Unlock185FPS=1\n"
                    + "Ultra144FPS=1\n"
                    + "Ultra165FPS=1\n"
                    + "Ultra185FPS=1\n"
                    + "TouchBoostHz=185\n"
                    + "TouchPollingRate=1000\n"
                    + "TouchZeroDelay=1\n"
                    + "GyroSampleRate=1000\n"
                    + "GyroSensitivityRatio=2.5\n"
                    + "GyroZeroDelay=1\n"
                    + "GyroSmoothFactor=1\n"
                    + "GyroStabilization=1\n"
                    + "GyroLatencyMode=0\n"
                    + "PreloadShaders=1\n";
            }
            if (ConfigFileHelper.writeContentAtomic(path, content)) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "CODM UltraExtreme185 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            String content;
            if (path.endsWith(".json")) {
                content = "{\n" +
                        "  \"MaxFrameRate\": " + forcedFps + ",\n" +
                        "  \"TargetFPS\": " + forcedFps + ",\n" +
                        "  \"GraphicQuality\": 4,\n" +
                        "  \"FPSLimit\": " + forcedFps + ",\n" +
                        "  \"FrameRateLimit\": " + forcedFps + ",\n" +
                        "  \"MobileFPSLimit\": " + forcedFps + ",\n" +
                        "  \"HDRMode\": 1,\n" +
                        "  \"HDRColorMode\": 2,\n" +
                        "  \"Unlock120Hz\": 1,\n" +
                        "  \"Unlock144Hz\": 1,\n" +
                        "  \"Unlock165Hz\": 1,\n" +
                        "  \"Unlock185Hz\": 1,\n" +
                        "  \"TouchBoostHz\": " + forcedFps + ",\n" +
                        "  \"TouchPollingRate\": 1000,\n" +
                        "  \"TouchZeroDelay\": 1,\n" +
                        "  \"GyroSampleRate\": 1000,\n" +
                        "  \"SuperResolution\": 1,\n" +
                        "  \"AntiAliasing\": 1,\n" +
                        "  \"ShadowQuality\": 2,\n" +
                        "  \"PreloadShaders\": 1\n" +
                        "}\n";
            } else if (path.endsWith(".xml")) {
                content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n" +
                        "<map>\n" +
                        "  <int name=\"MaxFrameRate\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"TargetFPS\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"FPSLimit\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"FrameRateLimit\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"MobileFPSLimit\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"GraphicQuality\" value=\"4\" />\n" +
                        "  <int name=\"HDRMode\" value=\"1\" />\n" +
                        "  <int name=\"Unlock120Hz\" value=\"1\" />\n" +
                        "  <int name=\"Unlock144Hz\" value=\"1\" />\n" +
                        "  <int name=\"Unlock165Hz\" value=\"1\" />\n" +
                        "  <int name=\"Unlock185Hz\" value=\"1\" />\n" +
                        "  <int name=\"TouchBoostHz\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"TouchPollingRate\" value=\"1000\" />\n" +
                        "  <int name=\"GyroSampleRate\" value=\"1000\" />\n" +
                        "  <int name=\"PreloadShaders\" value=\"1\" />\n" +
                        "</map>\n";
            } else {
                content = "[Graphics]\n" +
                        "MaxFrameRate=" + forcedFps + "\n" +
                        "TargetFPS=" + forcedFps + "\n" +
                        "FPSLimit=" + forcedFps + "\n" +
                        "FrameRateLimit=" + forcedFps + "\n" +
                        "MobileFPSLimit=" + forcedFps + "\n" +
                        "FrameRateLevel=" + FpsUnlockTier.getCodmFrameRateLevel(forcedFps) + "\n" +
                        "GraphicQuality=4\n" +
                        "HDRMode=1\n" +
                        "HDR10Plus=1\n" +
                        "HDRColorMode=2\n" +
                        "Unlock120Hz=1\n" +
                        "Unlock144Hz=1\n" +
                        "Unlock165Hz=1\n" +
                        "Unlock185Hz=1\n" +
                        "Unlock240Hz=1\n" +
                        "SuperResolution=1\n" +
                        "ResolutionScale=120\n" +
                        "VulkanPipelineCache=1\n" +
                        "AsyncCompute=1\n" +
                        "VRS=1\n" +
                        "TouchBoostHz=" + forcedFps + "\n" +
                        "TouchPollingRate=1000\n" +
                        "TouchZeroDelay=1\n" +
                        "GyroSampleRate=1000\n" +
                        "PreloadShaders=1\n" +
                        "AntiAliasing=1\n";
            }
            if (ConfigFileHelper.writeContentAtomic(path, content)) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "CODM competitive HDR " + forcedFps + "FPS force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
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

    /**
     * CODM — No recoil + no spread + aimbot precision injection.
     * Iterates all CODM config paths (UserSetting.json, PlayerPrefs.xml, GraphicsSettings.ini, etc.)
     * and injects zero-recoil/spread + max aim-assist keys via NativeConfigInjector.injectNoRecoilNoSpread.
     */
    public static void applyNoRecoilNoSpread(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectNoRecoilNoSpread(path);
        }
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    private static boolean applyPatch(String path, int targetFps) {
        String[] keys = {
            "MaxFrameRate=" + targetFps,
            "TargetFPS=" + targetFps,
            "FPSLimit=" + targetFps,
            "FrameRateLimit=" + targetFps,
            "MobileFPSLimit=" + targetFps,
            "GraphicQuality=4",
            "HDRMode=1",
            "HDRColorMode=2",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "SuperResolution=1",
            "TouchBoostHz=" + targetFps,
            "PreloadShaders=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Graphics]");
    }

    // ─── 2026 Skill Economy Overdrive ─────────────────────────────────────────

    /**
     * CODM — Zero operator skill / tactical / lethal cooldowns, max scorestreak charge,
     *          max HP regen, zero stamina cost, zero skill costs.
     */
    public static void applyFastCooldownAbilityRegen(String packageName) {
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
        applyFastCooldownAbilityRegen(packageName);
    }
}
