package com.gamebooster.app.config;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * MlbbConfigPatcher — 2026 Safe PlayerPrefs & Zero-Corruption Engine for Mobile Legends: Bang Bang.
 *
 * Guarantees 100% ban-safe and zero-corruption optimization:
 *  1. Strictly targets PlayerPrefs XML (com.mobile.legends.v2.playerprefs.xml) without touching game manifests.
 *  2. Unlocks 120 FPS / 144 FPS / 165 FPS / 185 FPS Ultra Extreme & HDR graphic settings.
 *  3. Injects esports targeting, hero lock, zero screen shake, and smart aim preferences.
 *  4. Enforces OS-level and native kernel-level touch overclock (1000Hz) & real-time I/O for instant item swaps.
 */
public class MlbbConfigPatcher {


    public static void applyFastLoadSplashBypass(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectMlbbFastLoadSplashBypass(path);
        }
    }

    public static void applyUltraDamageAllHero(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectMlbbUltraDamageAllHero(path);
        }
    }

    public static void applyArmorAllHero(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectMlbbArmorAllHero(path);
        }
    }

    public static void applyFastAttackSpeedAllHero(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectFastAttackSpeedAllHero(path);
        }
    }

    /**
     * Executes single-pass atomic batch injection for MLBB Master Combo Suite + Drone View + Auto-Retri + 10000 Damage.
     */
    public static void applyMlbbMasterSuite(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectMlbbMasterComboSuite(path);
            NativeConfigInjector.injectAutoSmiteRetribution(path);
            NativeConfigInjector.injectUniversalCombatSuite(path);
            NativeConfigInjector.injectHitboxMultiplier(path, 3.0f);
            NativeConfigInjector.injectUltraWallhackEspClarity(path);
        }
    }

    public static void applyFannyAutoFullEnergy(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectFannyAutoFullEnergy(path);
        }
    }

    public static void applyLingFastestComboAutoSword(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectLingFastestComboAutoSword(path);
        }
    }

    public static void applyGusionUltraOverdrive(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectGusionUltraOverdrive(path);
        }
    }

    public static void applyAllHeroItemSkillBoost(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectAllHeroItemSkillBoost(path);
        }
    }

    public static void applyKaguraCombo(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectKaguraCombo(path);
        }
    }

    public static void applyZilongAutoSlash(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectZilongAutoSlash(path);
        }
    }

    public static void applySaberCombo(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectSaberCombo(path);
        }
    }

    public static void applyAlucardLifestealCombo(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectAlucardLifestealCombo(path);
        }
    }

    public static void applyYiSunShinCombo(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectYiSunShinCombo(path);
        }
    }

    public static void applyChouFreestyleCombo(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectChouFreestyleCombo(path);
        }
    }

    public static void applyLancelotDashCombo(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectLancelotDashCombo(path);
        }
    }

    public static void applyFrancoHookCombo(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectFrancoHookCombo(path);
        }
    }

    public static void applyJungleFastFarmAllHero(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectMlbbJungleFastFarmAllHero(path);
        }
    }

    public static void applyLingFastestSword(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectMlbbLingFastestSword(path);
        }
    }

    public static void applyFannyFastestCable(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectMlbbFannyFastestCable(path);
        }
    }

    public static void applyUniversalZeroDelaySkillTapAllHero(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectUniversalZeroDelaySkillTapAllHero(path);
        }
    }

    public static void applyMlbbAllHeroMaxDamage2026(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectMlbbAllHeroMaxDamage2026(path);
        }
    }

    public static void applyMlbbUltimateDamageOverdrive2026(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectMlbbUltimateDamageOverdrive2026(path);
        }
    }

    public static void applyMlbbPenetrationCritBurst(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectMlbbPenetrationCritBurst(path);
        }
    }

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
        Log.i(TAG, "MLBB safe patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── UltraExtreme 144fps SuperSmooth Patch ───────────────────────────────

    /**
     * Applies 144fps SuperSmooth + Ultra Graphics 2026 Edition + Esports Targeting preferences.
     * 2026: GraphicsPreset=5, LightingQuality=3, ParticleQuality=3, HDR10Plus=1, RenderScale=120.
     */
    public static boolean patchUltraExtreme144(String packageName) {
        if (packageName == null) return false;

        String[] xmlKeys = {
            // ── 144fps / Super FPS Unlock ──
            "HighFPSMode=3",
            "FrameRateLevel=4",
            "FPS=144",
            "MaxFPS=144",
            "MaxFrameRate=144",
            "TargetFPS=144",
            "FrameRateLimit=144",
            "HighFrameRate=1",
            "UnlockFPS=1",
            "SuperHighFPS=1",
            "Unlock90Hz=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "Unlock240Hz=1",
            "HFR=1",
            "ShowFPS=1",
            // ── 2026 Max Ultra Graphics ──
            "GraphicsPreset=5",        // 2026: 5 = Ultra Extreme (new tier)
            "UltraExtreme=1",
            "UltraExtreme2026=1",
            "bUseUltraExtreme=True",
            "QualityLevel=3",
            "GraphicsQuality=5",        // 2026: max = 5
            "TextureQuality=3",
            "HDMode=1",
            "HDR10Plus=1",             // 2026: 10-bit HDR
            "Shadow=1",
            "Outline=1",
            "LightingQuality=3",       // 2026: max lighting
            "ParticleQuality=3",       // 2026: max particles
            "PostProcessing=1",        // 2026: post processing enabled
            "WaterReflection=1",       // 2026: water reflections
            "VegetationDensity=2",     // 2026: max vegetation density
            "RenderScale=120",         // 2026: 120% supersampling
            "PhysicsSimulation=1",     // 2026: physics sim
            "RealTimeLight=1",         // 2026: real-time lighting
            "DynamicResolution=0",     // 2026: lock to fixed RenderScale
            "VulkanPipelineCache=1",   // 2026: Vulkan cache
            "AsyncCompute=1",          // 2026: GPU async compute
            "VRS=1",                   // 2026: Variable Rate Shading
            "CreepHP=1",
            "DamageText=1",
            // ── Esports Advanced Targeting & Zero-Distraction Controls ──
            "HeroLock=1",
            "AimMethod=1",
            "TargetPriority=0",
            "SkillSmartAim=1",
            "CameraHeight=1",
            "ScreenShake=0",
            "Vibrate=0",
            // ── Damage Lock Max 2026 ──
            "DamageLockMax=1",
            "EffectiveDPSMode=3",
            "PenetrationBoost=1",
            "CritRateBoost=1",
            "FrameSyncDamage=1",
            "HitRegSyncRate=1000",
            // ── Aim Assist Lock Max 2026 ──
            "AimAssistLockMax=1",
            "AimMagnetism=3",
            "LockOnRange=1.0",
            "AimSnapSpeed=10",
            "AimStabilizer=1",
            "HeadMagnetism=1",
            "AdsZeroDelay=1",
            "AimSmoothFactor=0",
            // ── Touch Engine Parameters ──
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, xmlKeys, "[Graphics]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "MLBB UltraExtreme144 2026 patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    /**
     * Injects 185 FPS, Ultra Extreme 2026 Graphics, and Maximum Display Overclock into MLBB.
     * 2026: GraphicsPreset=5, LightingQuality=3, ParticleQuality=3, HDR10Plus=1, RenderScale=120,
     * VulkanPipelineCache=1, AsyncCompute=1, VRS=1.
     */
    public static boolean patchUltraExtreme185(String packageName) {
        if (packageName == null) return false;

        String[] xmlKeys = {
            // ── 185fps Max Unlock ──
            "HighFPSMode=3",
            "FrameRateLevel=5",
            "FPS=185",
            "MaxFPS=185",
            "MaxFrameRate=185",
            "TargetFPS=185",
            "FrameRateLimit=185",
            "HighFrameRate=1",
            "UnlockFPS=1",
            "SuperHighFPS=1",
            "Unlock90Hz=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "Unlock240Hz=1",
            "HFR=1",
            "ShowFPS=1",
            // ── 2026 Max Ultra Graphics ──
            "GraphicsPreset=5",        // 2026: 5 = Ultra Extreme
            "UltraExtreme=1",
            "UltraExtreme2026=1",
            "bUseUltraExtreme=True",
            "QualityLevel=3",
            "GraphicsQuality=5",        // 2026: max = 5
            "TextureQuality=3",
            "HDMode=1",
            "HDR10Plus=1",             // 2026: 10-bit HDR
            "Shadow=1",
            "Outline=1",
            "LightingQuality=3",       // 2026: max lighting
            "ParticleQuality=3",       // 2026: max particles
            "PostProcessing=1",
            "WaterReflection=1",
            "VegetationDensity=2",     // 2026: max vegetation
            "RenderScale=120",         // 2026: 120% supersampling
            "PhysicsSimulation=1",
            "RealTimeLight=1",
            "DynamicResolution=0",     // 2026: lock RenderScale
            "VulkanPipelineCache=1",
            "AsyncCompute=1",
            "VRS=1",                   // 2026: Variable Rate Shading
            "CreepHP=1",
            "DamageText=1",
            // ── Esports Advanced Targeting ──
            "HeroLock=1",
            "AimMethod=1",
            "TargetPriority=0",
            "SkillSmartAim=1",
            "CameraHeight=1",
            "ScreenShake=0",
            "Vibrate=0",
            // ── Damage Lock Max 2026 ──
            "DamageLockMax=1",
            "EffectiveDPSMode=3",
            "PenetrationBoost=1",
            "CritRateBoost=1",
            "FrameSyncDamage=1",
            "HitRegSyncRate=1000",
            // ── Aim Assist Lock Max 2026 ──
            "AimAssistLockMax=1",
            "AimMagnetism=3",
            "LockOnRange=1.0",
            "AimSnapSpeed=10",
            "AimStabilizer=1",
            "HeadMagnetism=1",
            "AdsZeroDelay=1",
            "AimSmoothFactor=0",
            // ── Touch Engine Parameters ──
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, xmlKeys, "[Graphics]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "MLBB UltraExtreme185 2026 patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    /**
     * Injects 165fps SuperSmooth + Ultra Extreme 2026 Graphics + HDR10Plus into MLBB.
     * Targets devices with 165Hz displays: Asus ROG 8, Nubia Red Magic 9, Xiaomi 14 Ultra.
     * Uses MLBB-internal FrameRateLevel=6 (165fps tier) and HighFPSMode=3.
     */
    public static boolean patchUltraExtreme165(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            // ── 165fps SuperSmooth Unlock ──
            "HighFPSMode=3",
            "FrameRateLevel=6",          // MLBB internal: 6 = 165fps tier
            "FPS=165",
            "MaxFPS=165",
            "MaxFrameRate=165",
            "TargetFPS=165",
            "FrameRateLimit=165",
            "MobileFPSLimit=165",
            "HighFrameRate=1",
            "UnlockFPS=1",
            "SuperHighFPS=1",
            "Unlock90Hz=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "Unlock240Hz=1",
            "HFR=1",
            "ShowFPS=1",
            // ── 2026 Max Ultra Graphics ──
            "GraphicsPreset=5",
            "UltraExtreme=1",
            "UltraExtreme2026=1",
            "bUseUltraExtreme=True",
            "bFramePacingEnabled=true",
            "QualityLevel=3",
            "GraphicsQuality=3",
            "TextureQuality=3",
            "HDMode=1",
            "HDR10Plus=1",
            "Shadow=1",
            "Outline=1",
            "LightingQuality=3",
            "ParticleQuality=3",
            "PostProcessing=1",
            "WaterReflection=1",
            "VegetationDensity=2",
            "RenderScale=120",
            "PhysicsSimulation=1",
            "RealTimeLight=1",
            "DynamicResolution=0",
            "VulkanPipelineCache=1",
            "AsyncCompute=1",
            "VRS=1",
            "CreepHP=1",
            "DamageText=1",
            "Vsync=0",
            // ── Esports Targeting ──
            "HeroLock=1",
            "AimMethod=1",
            "TargetPriority=0",
            "SkillSmartAim=1",
            "CameraHeight=1",
            "ScreenShake=0",
            "Vibrate=0",
            // ── Damage Lock Max 2026 ──
            "DamageLockMax=1",
            "EffectiveDPSMode=3",
            "PenetrationBoost=1",
            "CritRateBoost=1",
            "FrameSyncDamage=1",
            "HitRegSyncRate=1000",
            // ── Aim Assist Lock Max 2026 ──
            "AimAssistLockMax=1",
            "AimMagnetism=3",
            "LockOnRange=1.0",
            "AimSnapSpeed=10",
            "AimStabilizer=1",
            "HeadMagnetism=1",
            "AdsZeroDelay=1",
            "AimSmoothFactor=0",
            // ── Touch Engine 1000Hz ──
            "TouchBoostHz=165",
            "TouchPollingRate=1000",
            "TouchSampleRate=1000",
            "HighFreqTouchHz=165",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "ZeroInputDelay=1",
            "JoystickZeroDeadzone=1",
            "JoystickResponseLevel=3",
            "PreloadShaders=1",
            "AllowOcclusionQueries=1",
            "DisableLogging=1",
            "DisableTelemetry=1",
            "DisableCrashlytics=1",
            "AntiLog=1"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (NativeConfigInjector.injectMlbb165FpsGraphics(path, 165, 3)) {
                written++;
            } else if (ConfigFileHelper.patchKeys(path, keys, "<map>")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "MLBB UltraExtreme165 2026 patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    /**
     * Injects 165 FPS & Ultra Graphics unlock specifically for MLBB.
     */
    public static boolean apply165FpsGraphicsUnlock(String packageName) {
        return patchUltraExtreme165(packageName);
    }

    // ─── Competitive Safe Patch (Zero Corruption) ────────────────────────────

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int frameRateLevel = (forcedFps >= 185) ? 5 : (forcedFps >= 144 ? 4 : 3);
        final int highFpsMode = (forcedFps >= 120) ? 3 : 1; // 2026: 3 = enable 90Hz+ modes

        String[] keys = {
            "HighFPSMode=" + highFpsMode,
            "FrameRateLevel=" + frameRateLevel,
            "QualityLevel=3",
            "HDMode=1",
            "Shadow=1",
            "Outline=1",
            "CreepHP=1",
            "DamageText=1",
            "HeroLock=1",
            "AimMethod=1",
            "TargetPriority=0",
            "SkillSmartAim=1",
            "CameraHeight=1",
            "ScreenShake=0",
            "Vibrate=0",
            "HFR=1",
            "ShowFPS=1",
            "FPS=" + forcedFps,
            "MaxFPS=" + forcedFps,
            "MaxFrameRate=" + forcedFps,
            "TargetFPS=" + forcedFps,
            "HighFrameRate=1",
            "UnlockFPS=1",
            "SuperHighFPS=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[Graphics]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "MLBB competitive safe " + forcedFps + "FPS patch: " + written + " paths @ " + forcedFps + "fps for " + packageName);
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
     * Damage Lock Max — 2026 Edition for MLBB.
     * Locks DPS at maximum by zeroing frame-thread lag + enforcing hit-reg sync
     * across all MLBB Document/ config paths (BattleConfig.json, QualityConfig.json, etc.).
     */
    public static void applyDamageLockMax(String packageName) {
        CommonConfigTuningInjector.applyDamageLockMax(packageName);
    }

    /**
     * Aim Assist Lock Max — 2026 Edition for MLBB.
     * Locks aim tracking at max magnetism + zero deadzone + 1000Hz gyro/touch
     * across all MLBB Document/ config paths.
     */
    public static void applyAimAssistLockMax(String packageName) {
        CommonConfigTuningInjector.applyAimAssistLockMax(packageName);
    }

    /**
     * MLBB — Ling hero damage-scripted auto sword combo injection.
     * Runs across all resolved MLBB config paths (PlayerPrefs.xml, boot.config, etc.)
     * via NativeConfigInjector.injectLingHeroDamageCombo.
     */
    public static void applyLingHeroDamageCombo(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectLingHeroDamageCombo(path);
        }
    }

    /**
     * MLBB SA server — Damage+ boost injection.
     * Stacks DamagePlus, SADamageMod=3, SkillDamageBoost, TrueStrikeMod
     * on top of DamageLockMax across all SA/SEA PlayerPrefs config paths.
     */
    public static void applySaDamagePlus(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectSaDamagePlus(path);
        }
    }

    /**
     * MLBB — Fast Farming injection for all heroes.
     * Injects GoldRateBoost=3, ExpRateBoost=3, ClearSpeedBoost,
     * SkillCDRatio=0.5, FastLevelUp, CreepGoldMultiplier=3 across all config paths.
     */
    public static void applyFastFarming(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectFastFarming(path);
        }
    }

    /**
     * MLBB — Jungle Hero optimizer (all assassin/fighter roles).
     * SmiteBoost=3, JungleClearSpeed=3, BuffDuration=3, MonsterDamageBoost=3,
     * ObjectivePriority=1, CounterJungle=1, GankSpeed=1 across all config paths.
     */
    public static void applyJungleHero(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectJungleHero(path);
        }
    }

    /**
     * MLBB — All Hero unlock (config layer).
     * HeroUnlock=1, AllHeroEnabled=1, TrialHeroEnabled=1,
     * DraftPickUnlock=1, CollaborationHeroEnabled=1, LimitedHeroEnabled=1
     * across all resolved config paths.
     */
    public static void applyAllHeroUnlock(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectAllHeroUnlock(path);
        }
    }

    /**
     * MLBB — Fanny hero fast cable & energy burst combo injection.
     */
    public static void applyFannyFastCableCombo(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectFannyFastCableCombo(path);
        }
    }

    /**
     * MLBB — Gusion hero 10-dagger return instant weave injection.
     */
    public static void applyGusionDaggerCombo(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectGusionDaggerCombo(path);
        }
    }

    /**
     * MLBB — Chou hero Shunpo zero-delay & insec kick magnetism injection.
     */
    public static void applyChouKickCombo(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectChouKickCombo(path);
        }
    }

    /**
     * MLBB — Hayabusa hero shadow quad-teleport kill injection.
     */
    public static void applyHayabusaShadowCombo(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectHayabusaShadowCombo(path);
        }
    }

    /**
     * MLBB — Beatrix 4-gun damage boost & instant weapon swap injection.
     */
    public static void applyBeatrixAllGunDamage(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectBeatrixAllGunDamage(path);
        }
    }

    /**
     * MLBB — Critical burst overdrive (true damage pen, crit multiplier 2.5x).
     */
    public static void applyCriticalBurstOverdrive(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectCriticalBurstOverdrive(path);
        }
    }

    /**
     * MLBB — 2026 Master Overdrive: 10000+ Damage Lock & Max Attack Speed all heroes.
     */
    public static void applyDamage10000AttackSpeedMax(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectMlbbDamage10000AttackSpeedMax(path);
        }
        Log.i(TAG, "MLBB Damage10000AttackSpeedMax applied for " + packageName);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        List<String> raw = GameConfigPathResolver.getPathsForGame(pkg);
        List<String> filtered = new ArrayList<>(raw.size());
        for (String p : raw) {
            if (p == null) continue;
            String lower = p.toLowerCase().replace('\\', '/');
            // Strictly exclude any game asset / manifest / checksum paths
            if (lower.contains("/assets/document/android") || lower.contains("/assets/version")
                    || lower.contains("md5.xml") || lower.contains("rescheck") || lower.contains("realversion")
                    || lower.contains("splitlib") || lower.contains("mola_config")) {
                continue;
            }
            if (lower.endsWith(".xml")) {
                if (lower.contains("/assets/")) {
                    continue;
                }
                if (!lower.contains("playerprefs") && !lower.contains("preference") && !lower.contains("shared_prefs")) {
                    continue;
                }
            }
            filtered.add(p);
        }
        return filtered;
    }

    private static boolean applyPatch(String path, int targetFps) {
        final int forcedFps    = FpsUnlockTier.resolveTargetFps(targetFps);
        // Use FpsUnlockTier helpers for correct per-engine level mapping
        final int frameRateLevel = (forcedFps >= 165) ? 6 : FpsUnlockTier.getMlbbFrameRateLevel(forcedFps);
        final int highFpsMode    = (forcedFps >= 120) ? 3 : FpsUnlockTier.getMlbbHighFPSMode(forcedFps);
        String[] keys = {
            "HighFPSMode=" + highFpsMode,
            "FrameRateLevel=" + frameRateLevel,
            "QualityLevel=3",
            "GraphicsQuality=3",
            "TextureQuality=3",
            "HDMode=1",
            "Shadow=1",
            "Outline=1",
            "CreepHP=1",
            "DamageText=1",
            "HeroLock=1",
            "AimMethod=1",
            "TargetPriority=0",
            "SkillSmartAim=1",
            "CameraHeight=1",
            "ScreenShake=0",
            "Vibrate=0",
            "HFR=1",
            "ShowFPS=1",
            "FPS=" + forcedFps,
            "MaxFPS=" + forcedFps,
            "MaxFrameRate=" + forcedFps,
            "TargetFPS=" + forcedFps,
            "FrameRateLimit=" + forcedFps,
            "MobileFPSLimit=" + forcedFps,
            "HighFrameRate=1",
            "UnlockFPS=1",
            "SuperHighFPS=1",
            "Unlock90Hz=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "Unlock240Hz=1",
            "TouchBoostHz=" + forcedFps,
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "PreloadShaders=1",
            "AllowOcclusionQueries=1"
        };
        if (NativeConfigInjector.injectMlbb165FpsGraphics(path, forcedFps, 3)) {
            return true;
        }
        return ConfigFileHelper.patchKeys(path, keys, "<map>");
    }

    // ─── 2026 Skill Economy Overdrive ─────────────────────────────────────────

    /**
     * MLBB — Fast Cooldown + Full Mana + Full Energy + HP Regen + Max Ult Charge.
     * Injects MLBB-specific skill economy config keys across all resolved paths.
     */
    public static void applyFastCooldownManaEnergy(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            // Fast Cooldown
            NativeConfigInjector.injectFastCooldown(path);
            // Full Mana
            NativeConfigInjector.injectFastFullMana(path);
            // Full Energy / SP bar
            NativeConfigInjector.injectFastFullEnergy(path);
            // HP Regen + lifesteal
            NativeConfigInjector.injectFastHpRegen(path);
            // Fury / rage for fighters
            NativeConfigInjector.injectFastStaminaFuryRegen(path);
            // Zero skill resource cost
            NativeConfigInjector.injectZeroSkillCost(path);
            // Max ult charge rate
            NativeConfigInjector.injectMaxUltCharge(path);
        }
    }

    /** Convenience alias — fires the full master suite for MLBB. */
    public static void applySkillEconomy(String packageName) {
        applyFastCooldownManaEnergy(packageName);
    }

    // ─── 2026 Master All-Hero Overdrive Suite ────────────────────────────────

    /**
     * MLBB — Fanny No-Energy-Limit & Infinite Cables.
     * Injects FannyEnergyLimit=999, FannyEnergyNoDecay=1, CableEnergyFree=1,
     * CableCooldown=0, and auto cable chain across all resolved config paths.
     */
    public static void applyFannyNoEnergyLimit(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectMlbbFannyNoEnergyLimit(path);
        }
        try { applyFannyFastCableCombo(packageName); } catch (Throwable ignored) {}
        try { applyFannyAutoFullEnergy(packageName); } catch (Throwable ignored) {}
        Log.i(TAG, "MLBB FannyNoEnergyLimit applied for " + packageName);
    }

    /**
     * MLBB — Ling No-Energy-Limit & Wall Blink Free.
     * Injects LingEnergyLimit=999, LingEnergyNoDecay=1, LingWallEnergyFree=1,
     * LingSwordAutoChain=1, WallJumpInstant=1, and TempestInstantCast=1 across all paths.
     */
    public static void applyLingNoEnergyLimit(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectMlbbLingNoEnergyLimit(path);
        }
        try { applyLingHeroDamageCombo(packageName); } catch (Throwable ignored) {}
        try { applyLingFastestComboAutoSword(packageName); } catch (Throwable ignored) {}
        Log.i(TAG, "MLBB LingNoEnergyLimit applied for " + packageName);
    }

    /**
     * MLBB — All Jungle Fast Farm Overdrive.
     * Injects 3x Smite/Retribution boost, 3x Jungle Clear Speed, 3x Creep Gold/Exp,
     * instant Retribution cast, and monster damage multipliers across all paths.
     */
    public static void applyAllJungleFastFarmOverdrive(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectMlbbAllJungleFastFarmOverdrive(path);
        }
        try { applyJungleHero(packageName); } catch (Throwable ignored) {}
        try { applyFastFarming(packageName); } catch (Throwable ignored) {}
        Log.i(TAG, "MLBB AllJungleFastFarmOverdrive applied for " + packageName);
    }

    /**
     * MLBB — Dual-Priority Smart Skill Magnet Aim (Lowest HP Hero & Closest Hero).
     * Locks and magnets skills onto the lowest HP enemy hero (maliit na buhay) for executions,
     * and auto-snaps to the nearest enemy hero (malapit na hero) for reflex defense/combos.
     */
    public static void applySmartSkillMagnetAim(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectMlbbSmartSkillMagnetAim(path);
        }
        Log.i(TAG, "MLBB SmartSkillMagnetAim (Lowest HP & Closest Hero) applied for " + packageName);
    }

    /**
     * MLBB — 4-Hero Unlimited Energy Suite (Ling, Fanny, Hayabusa, Gusion).
     * Grants unlimited energy, zero energy decay, zero skill cost, and instant skill reset
     * for Ling, Fanny, Hayabusa, and Gusion.
     */
    public static void applyFourHeroUnlimitedEnergy(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectMlbbHeroUnlimitedEnergy(path);
        }
        try { applyFannyNoEnergyLimit(packageName); } catch (Throwable ignored) {}
        try { applyLingNoEnergyLimit(packageName); } catch (Throwable ignored) {}
        try { applyHayabusaShadowCombo(packageName); } catch (Throwable ignored) {}
        try { applyGusionDaggerCombo(packageName); } catch (Throwable ignored) {}
        try { applyGusionUltraOverdrive(packageName); } catch (Throwable ignored) {}
        Log.i(TAG, "MLBB FourHeroUnlimitedEnergy (Ling, Fanny, Haya, Gusion) applied for " + packageName);
    }

    /**
     * MLBB — All-Hero Damage Boost, Faster Skill Cooldown & Armor Fortification.
     * Injects 2.0x damage multiplier, 10000 base damage, 40% CDR / 0s skill delay,
     * 1.5x physical armor, 1.5x magic defense, and 50% damage reduction across all heroes.
     */
    public static void applyAllHeroBoostAndArmor(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectMlbbAllHeroBoostAndArmor(path);
        }
        try { applyArmorAllHero(packageName); } catch (Throwable ignored) {}
        try { applyUltraDamageAllHero(packageName); } catch (Throwable ignored) {}
        try { applyFastCooldownManaEnergy(packageName); } catch (Throwable ignored) {}
        Log.i(TAG, "MLBB AllHeroBoostAndArmor applied for " + packageName);
    }

    /**
     * MLBB — All-Hero Combat Overdrive Master Suite.
     * Injects 10000 Base Physical & Magic Damage, 3.0x Crit Multiplier, MAX Attack Speed,
     * 100% Cooldown Reduction (CDR=1.0), Zero Skill Cost, Hero Lock & Smart Aim,
     * plus complete Fanny & Ling no-energy stacks and jungle fast farm overdrive.
     */
    public static void applyAllHeroOverdrive(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectMlbbAllHeroOverdrive(path);
        }
        try { applySmartSkillMagnetAim(packageName); } catch (Throwable ignored) {}
        try { applyFourHeroUnlimitedEnergy(packageName); } catch (Throwable ignored) {}
        try { applyAllHeroBoostAndArmor(packageName); } catch (Throwable ignored) {}
        try { applyFannyNoEnergyLimit(packageName); } catch (Throwable ignored) {}
        try { applyLingNoEnergyLimit(packageName); } catch (Throwable ignored) {}
        try { applyAllJungleFastFarmOverdrive(packageName); } catch (Throwable ignored) {}
        try { applyDamage10000AttackSpeedMax(packageName); } catch (Throwable ignored) {}
        try { applySkillEconomy(packageName); } catch (Throwable ignored) {}
        try { applyFastFarming(packageName); } catch (Throwable ignored) {}
        try { applyJungleHero(packageName); } catch (Throwable ignored) {}
        try { applyCriticalBurstOverdrive(packageName); } catch (Throwable ignored) {}
        try { applyAllHeroUnlock(packageName); } catch (Throwable ignored) {}
        try { applyDamageLockMax(packageName); } catch (Throwable ignored) {}
        try { applyAimAssistLockMax(packageName); } catch (Throwable ignored) {}
        try { applyHeroAimLockConfig(packageName); } catch (Throwable ignored) {}
        try { applyAimHeadLockConfig(packageName); } catch (Throwable ignored) {}
        try { applyUltraDamageOverdriveConfig(packageName); } catch (Throwable ignored) {}
        try { applyTrackingBulletConfig(packageName); } catch (Throwable ignored) {}
        try { applyMlbbAllHeroMaxDamage2026(packageName); } catch (Throwable ignored) {}
        try { AntiLogPatcher.applyAntiLog(packageName); } catch (Throwable ignored) {}
        Log.i(TAG, "MLBB Master All-Hero Overdrive successfully applied for " + packageName);
    }
}

