package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * MlbbConfigPatcher manages internal config files for Mobile Legends: Bang Bang (all versions).
 *
 * Two patching modes:
 *  - patch()            → standard patch: in-memory key upserting
 *  - patchCompetitive() → competitive force-write: overwrites all paths atomically via ConfigFileHelper
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
     * Applies 144fps SuperSmooth + UltraExtreme max graphics to MLBB.
     * Injects full quality and FPS keys into [Graphics] section of all config paths.
     *
     * @return true if at least one path was written
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
            "ResolutionScale=120",
            "UltraExtreme=1",
            "bUseUltraExtreme=True",
            "bUseHighQualityBloom=True",
            "bUseAntiAliasing=True",
            "bReduceLoadedMips=False",
            "Shadow=1",
            // ── SuperSmooth Frame Pacing ──
            "bFramePacingEnabled=True",
            "Vsync=0",
            "HighFreqTouchHz=144",
            "TouchBoostHz=144",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "GyroSampleRate=1000",
            "GyroSensitivityRatio=20.0",
            "GyroZeroDelay=1",
            "GyroSmoothFactor=1",
            "GyroStabilization=1",
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

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────


    /**
     * Force-overwrites ALL MLBB config paths unconditionally.
     * Uses ConfigFileHelper atomic write with mode 666.
     * Sets FrameRateLevel and forced FPS unconditionally.
     *
     * @return true if at least one path was written
     */
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
                "DisableLogging=1\n" +
                "DisableCrashlytics=1\n" +
                "DisableTelemetry=1\n" +
                "AntiLog=1\n" +
                "LogcatDisable=1\n" +
                // ── Drone View Ultra FOV 180 ──
                "DroneView=1\n" +
                "DroneViewHeight=4\n" +
                "CameraHeight=4\n" +
                "CameraDistance=180\n" +
                "CameraFOV=180\n" +
                "WideScreenMode=1\n" +
                "FieldOfView=180\n" +
                "UltraWideCamera=1\n" +
                "HighFreqTouchHz=" + forcedFps + "\n" +
                "TouchPollingRate=1000\n" +
                "TouchZeroDelay=1\n" +
                "TouchResponseLevel=3\n" +
                // ── 1000% Ultra Overdrive Damage ──
                "PhysicalDamageBoost=1000.00\n" +
                "MagicDamageBoost=1000.00\n" +
                "TrueDamageBoost=1000.00\n" +
                "DamageMultiplier=1000.00\n" +
                "SkillDamageMultiplier=1000.00\n" +
                "HeroDamageMultiplier=50.00\n" +
                "AllHeroDamageMultiplier=50.00\n" +
                "TankDamageMultiplier=50.00\n" +
                "FighterDamageMultiplier=50.00\n" +
                "AssassinDamageMultiplier=50.00\n" +
                "MageDamageMultiplier=50.00\n" +
                "MarksmanDamageMultiplier=50.00\n" +
                "SupportDamageMultiplier=50.00\n" +
                "BurstDamageMultiplier=1000.00\n" +
                "CriticalDamageRate=100\n" +
                "CriticalDamage=10000\n" +
                "CriticalDamageMultiplier=50.00\n" +
                "PhysicalPenetrationBoost=10000\n" +
                "MagicPenetrationBoost=10000\n" +
                "ArmorPenetration=10000\n" +
                "MagicResistPenetration=10000\n" +
                "SmiteTrueDamage=999999\n" +
                "RetributionDamageThreshold=999999\n" +
                "ExecuteThreshold=999999\n" +
                "ExecuteTrueDamageThreshold=999999\n" +
                "JungleMonsterSmiteEfficiency=100.00\n" +
                "TurretArmorBypass=100.00\n" +
                "MinionWaveClearMultiplier=100.00\n" +
                "ElementalDamageMultiplier=1000.00\n" +
                "FireDamageMultiplier=1000.00\n" +
                "IceDamageMultiplier=1000.00\n" +
                "LightningDamageMultiplier=1000.00\n" +
                "PoisonDamageMultiplier=1000.00\n" +
                "TrueDamagePenetration=100.00\n" +
                "LethalityBoost=1000\n" +
                "ArmorShredRatio=100.00\n" +
                "HeadshotDamageMultiplier=1000.00\n" +
                // ── Fast Cooldown & Instant Cast ──
                "SkillCoolDownReduceMode=1\n" +
                "CooldownReductionBoost=0.99\n" +
                "CooldownReduction=0.99\n" +
                "SkillCooldownMultiplier=0.01\n" +
                "UltimateCooldownReduction=0.99\n" +
                "PassiveCooldownReduction=0.99\n" +
                "SpellCooldownReduction=0.99\n" +
                "SkillAnimationCancelZeroDelay=1\n" +
                "SkillResponseZeroDelay=1\n" +
                "SkillCastZeroDelay=1\n" +
                "InstantSkillRelease=1\n" +
                "NoCastDelay=1\n" +
                "AttackSpeedMultiplier=25.00\n" +
                "AttackSpeedBoost=25.00\n" +
                "AttackDelayReduction=1\n" +
                "EnergyRegenRate=100.00\n" +
                "ManaRegenRate=100.00\n" +
                "UnlimitedEnergy=1\n" +
                "UnlimitedMana=1\n" +
                "NoManaCost=1\n" +
                "NoEnergyCost=1\n" +
                // ── Fast Movement / Agility ──
                "MovementSpeedMultiplier=15.00\n" +
                "MovementSpeedBoost=15.00\n" +
                "SprintSpeedMultiplier=15.00\n" +
                "SprintSpeedBoost=15.00\n" +
                "SprintSensitivity=1000\n" +
                "AgilityMultiplier=15.00\n" +
                "GyroSampleRate=1000\n" +
                "GyroSensitivityRatio=20.0\n" +
                "GyroZeroDelay=1\n" +
                "GyroSmoothFactor=1\n" +
                "GyroStabilization=1\n" +
                "GyroLatencyMode=0\n" +
                // ── 1000% Aim Assist & Lock ──
                "AimAssist=1\n" +
                "AimAssistStrength=10000\n" +
                "AimAssistLevel=10\n" +
                "AimPrecision=100\n" +
                "AutoAim=1\n" +
                "AimTracking=1\n" +
                "TargetLock=1\n" +
                "TargetLockSensitivity=10000\n" +
                "SmartTargetingMode=1\n" +
                "HeroPriorityLock=1\n" +
                "LowestHPTargetLock=1\n" +
                "AimAssistRadius=5000\n" +
                "CrosshairMagnetism=100.00\n" +
                "AimSnapStrength=100.00\n" +
                "AimMagnetism=100.00\n" +
                // ── 1000% Tracking & Skill Homing ──
                "TrackingBullet=1\n" +
                "BulletTracking=1\n" +
                "AutoTrackingBullet=1\n" +
                "MagicBullet=1\n" +
                "AutoTrackingSkill=1\n" +
                "SkillMagnetism=100.00\n" +
                "BulletMagnetism=100.00\n" +
                "HitboxExpansion=100.00\n" +
                "ProjectileHoming=1\n" +
                "HomingStrength=100.00\n" +
                "BulletCurveFactor=100.00\n" +
                "BulletVelocityMultiplier=200.00\n" +
                // ── 1500+ Shield & Invulnerability ──
                "ShieldMultiplier=1500.00\n" +
                "ShieldCapacity=1500.00\n" +
                "ShieldStrength=1500.00\n" +
                "ShieldEfficiency=1500.00\n" +
                "ShieldPointsMultiplier=1500.00\n" +
                "PhysicalDefenseBoost=1000.00\n" +
                "MagicDefenseBoost=1000.00\n" +
                "PhysicalDefenseMultiplier=1000.00\n" +
                "MagicDefenseMultiplier=1000.00\n" +
                "DamageReductionRatio=0.9999\n" +
                "DamageReduction=0.9999\n" +
                "IncomingDamageReduction=0.9999\n" +
                "DamageResistance=0.9999\n" +
                "MaxHPMultiplier=100.00\n" +
                "HPBoostRatio=100.00\n" +
                "DamageAbsorbRatio=100.00\n" +
                "ArmorBoost=50000\n" +
                "MagicResistBoost=50000\n" +
                "VestDurability=1000.00\n" +
                "HelmetDamageReduction=0.9999\n" +
                "TenacityRatio=0.9999\n" +
                "HealthRegenBoost=1000.00\n" +
                "HealthRegenRate=1000.00\n" +
                "HeavyHitAbsorption=100.00\n" +
                "BurstDamageReduction=100.00\n" +
                "HighDamageMitigationRatio=100.00\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.writeContentAtomic(path, content)) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "MLBB competitive HDR " + forcedFps + "FPS + Drone View FOV 180 + 1000% Damage + Fast CD + 1000% Aim/Tracking + 1500+ Shield force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Applies anti-log, log directory cleaning, and telemetry suppression for MLBB.
     */

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
            "HighFreqTouchHz=" + forcedFps
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Graphics]");
    }
}
