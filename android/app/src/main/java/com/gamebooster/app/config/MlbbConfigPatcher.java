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
     * Injects Drone View (Camera Height 4 / FOV 180), 1000% Damage Script, Physical/Magic/True Damage Boost, Critical and Penetration keys into MLBB config files across all heroes.
     */
    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageDroneKeys = {
            "DroneView=1",
            "DroneViewHeight=4",
            "CameraHeight=4",
            "CameraDistance=180",
            "CameraFOV=180",
            "FieldOfView=180",
            "WideScreenMode=1",
            "UltraWideCamera=1",
            // ── 1000% Damage Overdrive (All MLBB Heroes) ──
            "PhysicalDamageBoost=1000.00",
            "MagicDamageBoost=1000.00",
            "TrueDamageBoost=1000.00",
            "BulletDamageBoost=1000.00",
            "PhysicalPenetrationBoost=10000",
            "MagicPenetrationBoost=10000",
            "ArmorPenetration=10000",
            "MagicResistPenetration=10000",
            "PenetrationBoost=10000",
            "DamageMultiplier=1000.00",
            "DamageBoost=1000.00",
            "DamageBoostRatio=1000.00",
            "SkillDamageMultiplier=1000.00",
            "HeroDamageMultiplier=50.00",
            "AllHeroDamageMultiplier=50.00",
            "TankDamageMultiplier=50.00",
            "FighterDamageMultiplier=50.00",
            "AssassinDamageMultiplier=50.00",
            "MageDamageMultiplier=50.00",
            "MarksmanDamageMultiplier=50.00",
            "SupportDamageMultiplier=50.00",
            "HeadshotDamageMultiplier=1000.00",
            "CriticalDamageRate=100",
            "CriticalDamageMultiplier=50.00",
            "CriticalHitRate=1.00",
            "CriticalDamage=10000",
            "AttackSpeedMultiplier=25.00",
            "AttackSpeedBoost=25.00",
            "AttackDelayReduction=1",
            "MovementSpeedMultiplier=15.00",
            "MovementSpeedBoost=15.00",
            "SprintSpeedMultiplier=15.00",
            "SprintSpeedBoost=15.00",
            "SprintSensitivity=1000",
            "AgilityMultiplier=15.00",
            "SkillAnimationCancelZeroDelay=1",
            "SkillCoolDownReduceMode=1",
            "CooldownReductionBoost=0.99",
            "HighDamageRateMode=1",
            "DamageAssetOverride=1",
            "AutoDamageExecutionMode=1",
            "AutoSmiteExecution=1",
            "RetributionDamageThreshold=999999",
            "SmiteTrueDamage=999999",
            "ExecuteThreshold=999999",
            "TurretDamageReduction=0.001",
            "MinionDamageBoost=1000.00",
            "MonsterDamageBoost=1000.00",
            "HitboxExpansion=100.00",
            "BulletVelocityMultiplier=200.00",
            "BulletVelocityScale=200.00",
            "BodyDamageMultiplier=50.00",
            "LimbDamageMultiplier=50.00",
            "ExplosiveDamageMultiplier=50.00",
            "GyroSampleRate=1000",
            "GyroSensitivityRatio=20.0",
            "GyroZeroDelay=1",
            "GyroSmoothFactor=1",
            "GyroStabilization=1",
            "GyroLatencyMode=0"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHeroDamage1000(path);
            ConfigFileHelper.patchKeys(path, damageDroneKeys, "[DamageScript]");
        }
        Log.i(TAG, "MLBB Drone View FOV 180 & 1000% Damage Script applied for all heroes in " + packageName);
    }

    /**
     * Injects Fast Cooldown (CDR 0.99, zero cast delay, instant energy/mana) for MLBB.
     */
    public static void applyFastCooldownConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] cdKeys = {
            "SkillCoolDownReduceMode=1",
            "CooldownReductionBoost=0.99",
            "CooldownReduction=0.99",
            "SkillCooldownMultiplier=0.01",
            "UltimateCooldownReduction=0.99",
            "PassiveCooldownReduction=0.99",
            "SpellCooldownReduction=0.99",
            "SkillAnimationCancelZeroDelay=1",
            "SkillResponseZeroDelay=1",
            "SkillCastZeroDelay=1",
            "InstantSkillRelease=1",
            "NoCastDelay=1",
            "AttackSpeedMultiplier=25.00",
            "AttackSpeedBoost=25.00",
            "AttackDelayReduction=1",
            "EnergyRegenRate=100.00",
            "ManaRegenRate=100.00",
            "UnlimitedEnergy=1",
            "UnlimitedMana=1",
            "NoManaCost=1",
            "NoEnergyCost=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectFastCooldown(path);
            ConfigFileHelper.patchKeys(path, cdKeys, "[FastCooldown]");
        }
        Log.i(TAG, "MLBB Fast Cooldown 99% CDR & Instant Cast applied for " + packageName);
    }

    /**
     * Injects 1500+ Shield Overdrive & Damage Mitigation for MLBB.
     */
    public static void applyShield1500Config(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] shieldKeys = {
            "ShieldMultiplier=1500.00",
            "ShieldCapacity=1500.00",
            "ShieldStrength=1500.00",
            "ShieldEfficiency=1500.00",
            "ShieldPointsMultiplier=1500.00",
            "PhysicalDefenseBoost=1000.00",
            "MagicDefenseBoost=1000.00",
            "PhysicalDefenseMultiplier=1000.00",
            "MagicDefenseMultiplier=1000.00",
            "DamageReductionRatio=0.9999",
            "DamageReduction=0.9999",
            "IncomingDamageReduction=0.9999",
            "DamageResistance=0.9999",
            "ArmorBoost=50000",
            "MagicResistBoost=50000",
            "MaxHPMultiplier=100.00",
            "HPBoostRatio=100.00",
            "DamageAbsorbRatio=100.00",
            "VestDurability=1000.00",
            "HelmetDamageReduction=0.9999",
            "TenacityRatio=0.9999",
            "HealthRegenBoost=1000.00",
            "HealthRegenRate=1000.00",
            "HeavyHitAbsorption=100.00",
            "BurstDamageReduction=100.00",
            "HighDamageMitigationRatio=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectShield1500(path);
            ConfigFileHelper.patchKeys(path, shieldKeys, "[DefenseShield1500]");
        }
        Log.i(TAG, "MLBB 1500+ Shield & God-Mode Defense applied for " + packageName);
    }

    /**
     * Injects Drone View Ultra (Camera FOV 180, Height 4.0) for MLBB.
     */
    public static void applyDroneViewUltraConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] droneKeys = {
            "DroneView=1",
            "DroneViewHeight=4",
            "CameraHeight=4",
            "CameraDistance=180",
            "CameraFOV=180",
            "FieldOfView=180",
            "WideScreenMode=1",
            "UltraWideCamera=1",
            "MapOverviewScale=2.0"
        };
        for (String path : paths) {
            NativeConfigInjector.injectDroneView(path);
            ConfigFileHelper.patchKeys(path, droneKeys, "[DroneViewUltra]");
        }
        Log.i(TAG, "MLBB Drone View Ultra FOV 180 applied for " + packageName);
    }

    /**
     * Injects 1000% Smart Aim Assist, Hero Priority Lock, and Skill Target Assistance for MLBB.
     */
    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "AimAssist=1",
            "AimAssistStrength=10000",
            "AimAssistLevel=10",
            "AimPrecision=100",
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
            "TargetLockSensitivity=10000",
            "AimAssistRadius=5000",
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
            "PhysicalDefenseBoost=1000.00",
            "MagicDefenseBoost=1000.00",
            "PhysicalDefenseMultiplier=1000.00",
            "MagicDefenseMultiplier=1000.00",
            "DamageReductionRatio=0.9999",
            "DamageReduction=0.9999",
            "IncomingDamageReduction=0.9999",
            "ShieldMultiplier=1500.00",
            "ShieldCapacity=1500.00",
            "ShieldStrength=1500.00",
            "MaxHPMultiplier=100.00",
            "HPBoostRatio=100.00",
            "DamageAbsorbRatio=100.00",
            "ArmorBoost=50000",
            "MagicResistBoost=50000",
            "VestDurability=1000.00",
            "VestDurabilityBoost=1000.00",
            "HelmetDamageReduction=0.9999",
            "TenacityRatio=0.9999",
            "ResilienceLevel=10",
            "ArmorLevel=10",
            "DamageResistance=0.9999",
            "ShieldEfficiency=1500.00",
            "ShieldPointsMultiplier=1500.00",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=1000.00",
            "HealthRegenRate=1000.00",
            "FallDamageReduction=1.00",
            "ExplosionResistance=0.9999",
            "HeadshotDamageReduction=0.9999",
            "HighDamageMitigationRatio=100.00",
            "HeavyHitAbsorption=100.00",
            "BurstDamageReduction=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[DefenseConfig]");
        }
        Log.i(TAG, "MLBB 1000% Armor Defense & 1500x Shield Multiplier applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for MLBB.
     */
    public static void applySpeedBoostConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] speedKeys = {
            "MovementSpeedMultiplier=15.00",
            "MovementSpeedBoost=15.00",
            "SprintSpeedMultiplier=15.00",
            "SprintSpeedBoost=15.00",
            "SprintSensitivity=1000",
            "AgilityMultiplier=15.00",
            "AttackSpeedMultiplier=25.00",
            "AttackSpeedBoost=25.00",
            "ReloadSpeedMultiplier=25.00",
            "FireRateMultiplier=25.00",
            "BulletVelocityMultiplier=200.00",
            "BulletVelocityScale=200.00",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "HighSpeedMovement=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectSpeedBoost(path);
            ConfigFileHelper.patchKeys(path, speedKeys, "[SpeedEngine]");
        }
        Log.i(TAG, "MLBB 15.0x Speed Boost & Movement Agility applied for " + packageName);
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
            "HitboxExpansion=100.00",
            "TargetLockTracking=1",
            "ProjectileHoming=1",
            "HomingStrength=100.00",
            "BulletCurveFactor=100.00",
            "BulletVelocityMultiplier=200.00"
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
