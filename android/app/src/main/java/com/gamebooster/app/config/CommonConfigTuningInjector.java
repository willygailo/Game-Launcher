package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * CommonConfigTuningInjector — Centralized Configuration & Combat Physics Injector.
 *
 * Unifies and deduplicates all tuning injection routines (Super Touch, Aim Assist,
 * Zero Recoil, Damage Script, Fast Cooldown, Shield 1500, Drone View, Armor Defense,
 * Speed Boost, Tracking Bullet, and Anti-Log) across all supported games.
 */
public final class CommonConfigTuningInjector {

    private static final String TAG = "CommonConfigTuning";

    private CommonConfigTuningInjector() {}

    private static List<String> getPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    /**
     * Injects 1000Hz Ultra-Fast Touch, 0ms Input Lag, and Zero Touch Slop.
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
            "TouchPressureThreshold=0.001",
            "TouchFilterSmoothing=1",
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
     * Injects Smart Aim Assist, Crosshair Magnetism, and Target Lock.
     */
    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
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
        Log.i(TAG, "Smart Aim Assist & Magnetism applied for " + packageName);
    }

    /**
     * Injects Recoil Control, Joystick Zero-Deadzone, and Movement Stabilization.
     */
    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
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
        Log.i(TAG, "Recoil Control & Input Stabilization applied for " + packageName);
    }

    /**
     * Injects Overdrive Damage Script and Critical Multipliers.
     */
    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
        String[] damageKeys = {
            "DamageBoost=1000.00",
            "PhysicalDamageBoost=1000.00",
            "MagicDamageBoost=1000.00",
            "TrueDamageBoost=1000.00",
            "DamageMultiplier=1000.00",
            "SkillDamageMultiplier=1000.00",
            "HeroDamageMultiplier=50.00",
            "AllHeroDamageMultiplier=50.00",
            "TankDamageMultiplier=50.00",
            "FighterDamageMultiplier=50.00",
            "AssassinDamageMultiplier=50.00",
            "MageDamageMultiplier=50.00",
            "MarksmanDamageMultiplier=50.00",
            "SupportDamageMultiplier=50.00",
            "BurstDamageMultiplier=1000.00",
            "CritDamageMultiplier=50.00",
            "WeakpointDamageMultiplier=50.00",
            "ArmorPiercingRatio=100.00",
            "WeaponBaseDamageMultiplier=1000.00",
            "HeavyAttackDamageScale=1000.00",
            "LightAttackDamageScale=1000.00",
            "ComboDamageMultiplier=1000.00",
            "JungleClearSpeedMultiplier=1000.00",
            "CriticalDamageRate=100",
            "CriticalDamage=10000",
            "CriticalDamageMultiplier=50.00",
            "CriticalChance=100",
            "ArmorPenetration=100.00",
            "MagicPenetration=100.00",
            "PhysicalPenetration=100.00",
            "HeadshotMultiplier=50.00",
            "RetributionDamageThreshold=999999",
            "SmiteTrueDamage=999999",
            "ExecuteThreshold=999999",
            "ExecuteTrueDamageThreshold=999999",
            "JungleMonsterSmiteEfficiency=100.00",
            "TurretArmorBypass=100.00",
            "MinionWaveClearMultiplier=100.00",
            "TurretDamageReduction=0.001",
            "MinionDamageBoost=1000.00",
            "MonsterDamageBoost=1000.00",
            "HitboxExpansion=100.00",
            "BulletVelocityMultiplier=200.00",
            "BulletVelocityScale=200.00",
            "BodyDamageMultiplier=50.00",
            "LimbDamageMultiplier=50.00",
            "ExplosiveDamageMultiplier=50.00",
            // ── Elemental & Lethality Overdrive ──
            "ElementalDamageMultiplier=1000.00",
            "FireDamageMultiplier=1000.00",
            "IceDamageMultiplier=1000.00",
            "LightningDamageMultiplier=1000.00",
            "PoisonDamageMultiplier=1000.00",
            "TrueDamagePenetration=100.00",
            "LethalityBoost=1000",
            "ArmorShredRatio=100.00",
            // ── FPS / Tactical Penetration & Wallbang ──
            "BulletPenetrationDepthMultiplier=50.00",
            "WallbangDamageMultiplier=100.00",
            "LimbShotDamageMultiplier=50.00",
            "VehicleDamageMultiplier=50.00",
            "NoDamageDropoff=1",
            "DamageDropoffRangeScale=100.00",
            // ── Hit Registration & Desync Compensation ──
            "HitRegistrationSync=1",
            "TickRate=128",
            "LagCompensationMode=1",
            "ClientPredictionAccuracy=100.00",
            "HitboxExpansionRadius=100.00",
            "GyroSampleRate=1000",
            "GyroSensitivityRatio=20.0",
            "GyroZeroDelay=1",
            "GyroSmoothFactor=1",
            "GyroStabilization=1",
            "GyroLatencyMode=0"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHeroDamage1000(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[DamageScript]");
        }
        Log.i(TAG, "Damage Script & Critical Boost applied for " + packageName);
    }

    /**
     * Injects Fast Cooldown (99% CDR, zero cast delay, instant energy/mana).
     */
    public static void applyFastCooldownConfig(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
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
        Log.i(TAG, "Fast Cooldown 99% CDR applied for " + packageName);
    }

    /**
     * Injects 1500+ Shield Overdrive & Defense Multipliers.
     */
    public static void applyShield1500Config(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
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
        Log.i(TAG, "1500+ Shield & God-Mode Defense applied for " + packageName);
    }

    /**
     * Injects Drone View (Camera FOV 180, Height 4.0).
     */
    public static void applyDroneViewConfig(String packageName) {
        applyDroneViewUltraConfig(packageName);
    }

    public static void applyDroneViewUltraConfig(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
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
        Log.i(TAG, "Drone View Ultra FOV 180 applied for " + packageName);
    }

    /**
     * Injects 1000% Armor Defense Boost & Damage Reduction.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
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
        Log.i(TAG, "Armor Defense & Shield Multiplier applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility.
     */
    public static void applySpeedBoostConfig(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
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
        Log.i(TAG, "Speed Boost & Agility applied for " + packageName);
    }

    /**
     * Injects Skill Auto-Tracking, Projectile Magnetism, and Hitbox Tracking.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = getPaths(packageName);
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
        Log.i(TAG, "Skill Auto-Tracking & Projectile Magnetism applied for " + packageName);
    }

    /**
     * Injects UltraExtreme max graphics quality + FPS unlock keys into all game config paths.
     * Uses FpsUnlockTier.getGraphicsMaxFlags() as the canonical quality key set, then appends
     * game-engine-agnostic FPS unlock and frame-pacing keys.
     *
     * @param packageName target game package
     * @param targetFps   desired FPS (resolved to nearest supported tier)
     */
    public static void applyUltraExtremeGraphics(String packageName, int targetFps) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        final FpsUnlockTier tier = FpsUnlockTier.fromFps(targetFps);
        final String[] graphicsKeys = {
            // ── UltraExtreme Quality ──
            "UltraExtreme=1",
            "bUseUltraExtreme=True",
            "GraphicsQuality=5",
            "GraphicQuality=4",
            "GraphicLevel=4",
            "ResolutionQuality=120",
            "ResolutionScale=120",
            "ScreenScale=120",
            // ── HDR + Tone Mapping ──
            "HDRMode=1",
            "UltraHDMode=1",
            "HDRColorMode=2",
            "SuperResolution=1",
            "bUseHDRMode=True",
            // ── Anti-Aliasing + Bloom ──
            "AntiAliasingQuality=4",
            "bUseAntiAliasing=True",
            "bUseHighQualityBloom=True",
            "BloomQuality=5",
            // ── Shadows + Textures ──
            "ShadowQuality=2",
            "ShadowDistance=3",
            "ShadowResolution=2048",
            "TextureQuality=4",
            "MaxAnisotropy=16",
            "bReduceLoadedMips=False",
            // ── FPS Unlock ──
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
            "Unlock" + tier.fps + "FPS=1",
            "Ultra" + tier.fps + "FPS=1",
            // ── Frame Pacing + SuperSmooth ──
            "bFramePacingEnabled=True",
            "Vsync=0",
            "TouchBoostHz=" + tier.fps,
            "TouchPollingRate=1000",
        };
        List<String> paths = getPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.applyUltraExtremeGraphics(path, tier.fps);
            ConfigFileHelper.patchKeys(path, graphicsKeys, "[GraphicsUltraExtreme]");
        }
        Log.i(TAG, "UltraExtreme Max Graphics @ " + tier.fps + "fps applied for " + packageName);
    }

    /**
     * Convenience shortcut: applies UltraExtreme graphics locked to the 144fps SuperSmooth tier.
     * Use this when the user explicitly selects the "144fps SuperSmooth" preset.
     */
    public static void applyUltraExtreme144(String packageName) {
        applyUltraExtremeGraphics(packageName, FpsUnlockTier.FPS_144.fps);
    }

    /**
     * Injects Unreal Engine 4/5 SystemSettings & 185 FPS unlocks into Engine.ini.
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
     * Injects Unity boot.config optimization flags (native GLES, no debugger, 185 FPS).
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
     * Injects Anti-Log / Privacy Guard keys into game configs.
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
        if (profile.isAimAssistEnabled()) applyAimAssistConfig(packageName);
        if (profile.isRecoilControlEnabled()) applyRecoilControlConfig(packageName);
        if (profile.isMlbbDamageScriptEnabled()) applyDamageScriptConfig(packageName);
        if (profile.isTrackingBulletEnabled()) applyTrackingBulletConfig(packageName);
        if (profile.isFastCooldownEnabled()) applyFastCooldownConfig(packageName);
        if (profile.isShield1500Enabled()) applyShield1500Config(packageName);
        if (profile.isDroneViewUltraEnabled()) applyDroneViewUltraConfig(packageName);
        if (profile.isArmorDefEnabled()) applyArmorDefConfig(packageName);
        if (profile.isAntiLogEnabled()) applyAntiLog(packageName);
        applyUnrealEngineOptimization(packageName, profile.getTargetFps());
        applyUnityBootConfigOptimization(packageName, profile.getTargetFps());
    }
}
