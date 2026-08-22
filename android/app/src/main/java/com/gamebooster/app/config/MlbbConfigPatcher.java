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
                "DroneView=1\n" +
                "DroneViewHeight=3\n" +
                "CameraHeight=3\n" +
                "CameraDistance=150\n" +
                "CameraFOV=150\n" +
                "WideScreenMode=1\n" +
                "FieldOfView=150\n" +
                "HighFreqTouchHz=" + forcedFps + "\n" +
                "TouchPollingRate=1000\n" +
                "TouchZeroDelay=1\n" +
                "TouchResponseLevel=3\n" +
                "PhysicalDamageBoost=100.00\n" +
                "MagicDamageBoost=100.00\n" +
                "TrueDamageBoost=100.00\n" +
                "DamageMultiplier=100.00\n" +
                "SkillDamageMultiplier=100.00\n" +
                "HeroDamageMultiplier=10.00\n" +
                "AllHeroDamageMultiplier=10.00\n" +
                "TankDamageMultiplier=10.00\n" +
                "FighterDamageMultiplier=10.00\n" +
                "AssassinDamageMultiplier=10.00\n" +
                "MageDamageMultiplier=10.00\n" +
                "MarksmanDamageMultiplier=10.00\n" +
                "SupportDamageMultiplier=10.00\n" +
                "CriticalDamageRate=100\n" +
                "CriticalDamage=1000\n" +
                "CriticalDamageMultiplier=10.00\n" +
                "PhysicalPenetrationBoost=1000\n" +
                "MagicPenetrationBoost=1000\n" +
                "ArmorPenetration=1000\n" +
                "MagicResistPenetration=1000\n" +
                "SmiteTrueDamage=99999\n" +
                "RetributionDamageThreshold=99999\n" +
                "ExecuteThreshold=99999\n" +
                "HeadshotDamageMultiplier=100.00\n" +
                "GyroSampleRate=1000\n" +
                "GyroSensitivityRatio=10.0\n" +
                "GyroZeroDelay=1\n" +
                "GyroSmoothFactor=1\n" +
                "GyroStabilization=1\n" +
                "GyroLatencyMode=0\n" +
                // ── 1000% Aim Assist & Lock ──
                "AimAssist=1\n" +
                "AimAssistStrength=1000\n" +
                "AimAssistLevel=10\n" +
                "AimPrecision=10\n" +
                "AutoAim=1\n" +
                "AimTracking=1\n" +
                "TargetLock=1\n" +
                "TargetLockSensitivity=1000\n" +
                "SmartTargetingMode=1\n" +
                "HeroPriorityLock=1\n" +
                "LowestHPTargetLock=1\n" +
                "AimAssistRadius=1000\n" +
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
                "HitboxExpansion=50.00\n" +
                "ProjectileHoming=1\n" +
                "HomingStrength=100.00\n" +
                "BulletCurveFactor=50.00\n" +
                "BulletVelocityMultiplier=100.00\n" +
                // ── 1000% Defense & Invulnerability ──
                "PhysicalDefenseBoost=100.00\n" +
                "MagicDefenseBoost=100.00\n" +
                "PhysicalDefenseMultiplier=100.00\n" +
                "MagicDefenseMultiplier=100.00\n" +
                "DamageReductionRatio=0.999\n" +
                "DamageReduction=0.999\n" +
                "IncomingDamageReduction=0.999\n" +
                "ShieldMultiplier=100.00\n" +
                "ShieldCapacity=100.00\n" +
                "ShieldStrength=100.00\n" +
                "MaxHPMultiplier=50.00\n" +
                "HPBoostRatio=50.00\n" +
                "DamageAbsorbRatio=50.00\n" +
                "ArmorBoost=10000\n" +
                "MagicResistBoost=10000\n" +
                "VestDurability=100.00\n" +
                "HelmetDamageReduction=0.999\n" +
                "TenacityRatio=0.999\n" +
                "HealthRegenBoost=100.00\n" +
                "HeavyHitAbsorption=10.00\n" +
                "BurstDamageReduction=10.00\n" +
                "HighDamageMitigationRatio=10.00\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.writeContentAtomic(path, content)) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "MLBB competitive HDR " + forcedFps + "FPS + Drone View + 1000% Damage + 1000% Aim/Tracking/Defense force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Applies anti-log, log directory cleaning, and telemetry suppression for MLBB.
     */
    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    /**
     * Injects super-fast zero-delay touch response keys into MLBB config files.
     */
    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] touchKeys = {
            "HighFreqTouch=1",
            "TouchResponseLevel=3",
            "HighFreqTouchHz=185",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "TouchLatencyReduction=1",
            "ZeroInputLag=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, touchKeys, "[TouchEngine]");
        }
        Log.i(TAG, "MLBB super-fast zero-delay touch applied for " + packageName);
    }

    /**
     * Injects Drone View (Camera Height / FOV 150), 1000% Damage Script, Physical/Magic/True Damage Boost, Critical and Penetration keys into MLBB config files across all heroes.
     */
    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageDroneKeys = {
            "DroneView=1",
            "DroneViewHeight=3",
            "CameraHeight=3",
            "CameraDistance=150",
            "CameraFOV=150",
            "FieldOfView=150",
            "WideScreenMode=1",
            "UltraWideCamera=1",
            // ── 1000% Damage Overdrive (All MLBB Heroes) ──
            "PhysicalDamageBoost=100.00",
            "MagicDamageBoost=100.00",
            "TrueDamageBoost=100.00",
            "BulletDamageBoost=100.00",
            "PhysicalPenetrationBoost=1000",
            "MagicPenetrationBoost=1000",
            "ArmorPenetration=1000",
            "MagicResistPenetration=1000",
            "PenetrationBoost=1000",
            "DamageMultiplier=100.00",
            "DamageBoost=100.00",
            "DamageBoostRatio=100.00",
            "SkillDamageMultiplier=100.00",
            "HeroDamageMultiplier=10.00",
            "AllHeroDamageMultiplier=10.00",
            "TankDamageMultiplier=10.00",
            "FighterDamageMultiplier=10.00",
            "AssassinDamageMultiplier=10.00",
            "MageDamageMultiplier=10.00",
            "MarksmanDamageMultiplier=10.00",
            "SupportDamageMultiplier=10.00",
            "HeadshotDamageMultiplier=100.00",
            "CriticalDamageRate=100",
            "CriticalDamageMultiplier=10.00",
            "CriticalHitRate=1.00",
            "CriticalDamage=1000",
            "AttackSpeedMultiplier=10.00",
            "AttackSpeedBoost=10.00",
            "AttackDelayReduction=1",
            "MovementSpeedMultiplier=10.00",
            "MovementSpeedBoost=10.00",
            "SprintSpeedMultiplier=10.00",
            "SprintSpeedBoost=10.00",
            "SprintSensitivity=500",
            "AgilityMultiplier=10.00",
            "SkillAnimationCancelZeroDelay=1",
            "SkillCoolDownReduceMode=1",
            "CooldownReductionBoost=0.80",
            "HighDamageRateMode=1",
            "DamageAssetOverride=1",
            "AutoDamageExecutionMode=1",
            "AutoSmiteExecution=1",
            "RetributionDamageThreshold=99999",
            "SmiteTrueDamage=99999",
            "ExecuteThreshold=99999",
            "TurretDamageReduction=0.01",
            "MinionDamageBoost=100.00",
            "MonsterDamageBoost=100.00",
            "HitboxExpansion=10.00",
            "BulletVelocityMultiplier=50.00",
            "BulletVelocityScale=50.00",
            "BodyDamageMultiplier=10.00",
            "LimbDamageMultiplier=10.00",
            "ExplosiveDamageMultiplier=10.00",
            "GyroSampleRate=1000",
            "GyroSensitivityRatio=5.0",
            "GyroZeroDelay=1",
            "GyroSmoothFactor=1",
            "GyroStabilization=1",
            "GyroLatencyMode=0"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHeroDamage1000(path);
            ConfigFileHelper.patchKeys(path, damageDroneKeys, "[DamageScript]");
        }
        Log.i(TAG, "MLBB Drone View FOV 150 & 1000% Damage Script applied for all heroes in " + packageName);
    }

    /**
     * Injects 1000% Smart Aim Assist, Hero Priority Lock, and Skill Target Assistance for MLBB.
     */
    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "AimAssist=1",
            "AimAssistStrength=1000",
            "AimAssistLevel=10",
            "AimPrecision=10",
            "AutoSkillLock=1",
            "SkillTargetAssist=1",
            "SmartTargetingMode=1",
            "HeroPriorityLock=1",
            "LowestHPTargetLock=1",
            "NearestTargetLock=0",
            "SkillAimAssist=1",
            "SmartAimCast=1",
            "SkillPredictPath=1",
            "AutoAimAssist=1",
            "TargetTracker=1",
            "HeroLockMode=1",
            "TargetLockSensitivity=1000",
            "AimAssistRadius=1000",
            "CrosshairMagnetism=100.00",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectAimAssist(path);
            ConfigFileHelper.patchKeys(path, aimKeys, "[AimAssist]");
        }
        Log.i(TAG, "MLBB 1000% Smart Aim Assist & Hero Priority Lock applied for " + packageName);
    }

    /**
     * Injects joystick and movement stabilization, zero input delay, and skill cancel zero-delay for MLBB.
     */
    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "RecoilControl=1",
            "ZeroRecoil=1",
            "NoRecoil=1",
            "MovementStabilization=1",
            "JoystickZeroDeadzone=1",
            "JoystickResponseLevel=3",
            "SkillCancellationZeroDelay=1",
            "InputSmoothing=1",
            "TouchStabilization=1",
            "ZeroInputDelay=1",
            "SkillResponseZeroDelay=1",
            "TouchJitterFilter=1",
            "AimPunchReduction=1",
            "FlinchReduction=1",
            "WeaponStability=500"
        };
        for (String path : paths) {
            NativeConfigInjector.injectNoRecoil(path);
            ConfigFileHelper.patchKeys(path, recoilKeys, "[InputStabilization]");
        }
        Log.i(TAG, "MLBB Movement Stabilization & Joystick Zero-Deadzone applied for " + packageName);
    }

    /**
     * Injects 1000% Armor Defense Boost, Damage Reduction, Shield Multiplier, and Resilience for MLBB.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "PhysicalDefenseBoost=100.00",
            "MagicDefenseBoost=100.00",
            "PhysicalDefenseMultiplier=100.00",
            "MagicDefenseMultiplier=100.00",
            "DamageReductionRatio=0.999",
            "DamageReduction=0.999",
            "IncomingDamageReduction=0.999",
            "ShieldMultiplier=100.00",
            "ShieldCapacity=100.00",
            "ShieldStrength=100.00",
            "MaxHPMultiplier=50.00",
            "HPBoostRatio=50.00",
            "DamageAbsorbRatio=50.00",
            "ArmorBoost=10000",
            "MagicResistBoost=10000",
            "VestDurability=100.00",
            "VestDurabilityBoost=100.00",
            "HelmetDamageReduction=0.999",
            "TenacityRatio=0.999",
            "ResilienceLevel=10",
            "ArmorLevel=10",
            "DamageResistance=0.999",
            "ShieldEfficiency=100.00",
            "ShieldPointsMultiplier=100.00",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=100.00",
            "FallDamageReduction=1.00",
            "ExplosionResistance=0.999",
            "HeadshotDamageReduction=0.999",
            "HighDamageMitigationRatio=10.00",
            "HeavyHitAbsorption=10.00",
            "BurstDamageReduction=10.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[DefenseConfig]");
        }
        Log.i(TAG, "MLBB 1000% Armor Defense & 100x Shield Multiplier applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for MLBB.
     */
    public static void applySpeedBoostConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] speedKeys = {
            "MovementSpeedMultiplier=10.00",
            "MovementSpeedBoost=10.00",
            "SprintSpeedMultiplier=10.00",
            "SprintSpeedBoost=10.00",
            "SprintSensitivity=500",
            "AgilityMultiplier=10.00",
            "AttackSpeedMultiplier=10.00",
            "AttackSpeedBoost=10.00",
            "ReloadSpeedMultiplier=10.00",
            "FireRateMultiplier=10.00",
            "BulletVelocityMultiplier=50.00",
            "BulletVelocityScale=50.00",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "HighSpeedMovement=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectSpeedBoost(path);
            ConfigFileHelper.patchKeys(path, speedKeys, "[SpeedEngine]");
        }
        Log.i(TAG, "MLBB 10.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects 1000% Skill Auto-Tracking, Projectile Magnetism, Retribution/Smite Lock, and Hitbox Tracking for MLBB.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "TrackingBullet=1",
            "BulletTracking=1",
            "AutoTrackingBullet=1",
            "MagicBullet=1",
            "AutoTrackingSkill=1",
            "AutoTargetLock=1",
            "SkillPathPrediction=1",
            "AutoRetributionSmiteLock=1",
            "SkillMagnetism=100.00",
            "BulletMagnetism=100.00",
            "BasicAttackTracking=1",
            "ProjectileTracking=1",
            "HitboxExpansion=50.00",
            "TargetLockTracking=1",
            "ProjectileHoming=1",
            "HomingStrength=100.00",
            "BulletCurveFactor=50.00",
            "BulletVelocityMultiplier=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectTrackingBullet(path);
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "MLBB 1000% Skill Auto-Tracking & Projectile Magnetism applied for " + packageName);
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
