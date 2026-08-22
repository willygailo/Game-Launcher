package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * FreeFireConfigPatcher manages internal config files for Garena Free Fire and Free Fire MAX.
 * Unlocks 120/144/165/185 FPS, high-frequency touch, and max graphic presets.
 */
public class FreeFireConfigPatcher {

    private static final String TAG = "FreeFireConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "FreeFire patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String content = "[FFGraphics]\n" +
                "HighFPS=1\n" +
                "HighFPSMode=1\n" +
                "FPSMode=2\n" +
                "FrameRateLevel=" + frameRateLevel + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "GraphicLevel=3\n" +
                "Shadow=1\n" +
                "HighResolution=1\n" +
                "VulkanEnabled=1\n" +
                "Unlock120Hz=1\n" +
                "Unlock144Hz=1\n" +
                "Unlock165Hz=1\n" +
                "Unlock185Hz=1\n" +
                // ── 1000% Aim Assist & Smart Lock ──
                "AimAssist=1\n" +
                "AutoAimPrecision=100.0\n" +
                "AimAssistStrength=10000\n" +
                "AimAssistLevel=10\n" +
                "AimPrecision=100\n" +
                "AutoAim=1\n" +
                "AimTracking=1\n" +
                "TargetLock=1\n" +
                "TargetLockSensitivity=10000\n" +
                "AimAssistRadius=5000\n" +
                "CrosshairMagnetism=100.00\n" +
                "AimSnapStrength=100.00\n" +
                "AimMagnetism=100.00\n" +
                "SprintSensitivity=1000\n" +
                "GeneralSensitivity=1000\n" +
                "RedDotSensitivity=1000\n" +
                "TPPFov=180\n" +
                "FPPFov=180\n" +
                // ── All Guns & All Scopes Zero Recoil ──
                "NoRecoil=1\n" +
                "ZeroRecoil=1\n" +
                "RecoilControl=1\n" +
                "RecoilScale=0.00\n" +
                "VerticalRecoil=0.00\n" +
                "HorizontalRecoil=0.00\n" +
                "RecoilReduction=1.00\n" +
                "AllWeaponRecoilFix=1\n" +
                "ScopeStabilization=1\n" +
                "ScopeStability=5.00\n" +
                "IronSightRecoil=0.00\n" +
                "RedDotRecoil=0.00\n" +
                "Scope2xRecoil=0.00\n" +
                "Scope4xRecoil=0.00\n" +
                "SniperScopeRecoil=0.00\n" +
                "ThermalScopeRecoil=0.00\n" +
                "MP40RecoilScale=0.00\n" +
                "M1887RecoilScale=0.00\n" +
                "SCARRecoilScale=0.00\n" +
                "AKRecoilScale=0.00\n" +
                "GrozaRecoilScale=0.00\n" +
                "WoodpeckerRecoilScale=0.00\n" +
                "AWMRecoilScale=0.00\n" +
                "M82BRecoilScale=0.00\n" +
                "UMPRecoilScale=0.00\n" +
                "GunShakeReduction=1.00\n" +
                "NoCameraShake=1\n" +
                "WeaponStability=500\n" +
                // ── 1000% Damage Overdrive ──
                "DamageMultiplier=1000.00\n" +
                "PhysicalDamageBoost=1000.00\n" +
                "MagicDamageBoost=1000.00\n" +
                "TrueDamageBoost=1000.00\n" +
                "DamageBoostRatio=1000.00\n" +
                "HeadshotDamageMultiplier=1000.00\n" +
                "BulletDamageBoost=1000.00\n" +
                "CriticalDamage=10000\n" +
                "CriticalHitRate=100\n" +
                "ArmorPenetration=10000\n" +
                // ── 1000% Tracking Bullet ──
                "TrackingBullet=1\n" +
                "BulletTracking=1\n" +
                "AutoTrackingBullet=1\n" +
                "MagicBullet=1\n" +
                "BulletMagnetism=100.00\n" +
                "HitboxExpansion=100.00\n" +
                "ProjectileHoming=1\n" +
                "HomingStrength=100.00\n" +
                "BulletCurveFactor=100.00\n" +
                "BulletVelocityMultiplier=200.00\n" +
                // ── 1500+ Shield & Armor Defense ──
                "PhysicalDefenseBoost=1000.00\n" +
                "MagicDefenseBoost=1000.00\n" +
                "PhysicalDefenseMultiplier=1000.00\n" +
                "MagicDefenseMultiplier=1000.00\n" +
                "DamageReductionRatio=0.9999\n" +
                "DamageReduction=0.9999\n" +
                "IncomingDamageReduction=0.9999\n" +
                "ShieldMultiplier=1500.00\n" +
                "ShieldCapacity=1500.00\n" +
                "ShieldStrength=1500.00\n" +
                "MaxHPMultiplier=100.00\n" +
                "HPBoostRatio=100.00\n" +
                "DamageAbsorbRatio=100.00\n" +
                "ArmorBoost=50000\n" +
                "MagicResistBoost=50000\n" +
                "VestDurability=1000.00\n" +
                "VestDurabilityBoost=1000.00\n" +
                "HelmetDamageReduction=0.9999\n" +
                "TenacityRatio=0.9999\n" +
                "HealthRegenBoost=1000.00\n" +
                "HeavyHitAbsorption=100.00\n" +
                "BurstDamageReduction=100.00\n" +
                "HighDamageMitigationRatio=100.00\n" +
                "TouchResponseLevel=3\n" +
                "HighFreqTouchHz=" + forcedFps + "\n" +
                "TouchPollingRate=1000\n" +
                "TouchZeroDelay=1\n" +
                "GyroSampleRate=1000\n" +
                "GyroSensitivityRatio=20.0\n" +
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
        Log.i(TAG, "FreeFire competitive " + forcedFps + "FPS + 1000% Damage/Aim/Tracking/Defense force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Applies anti-log, report cleaner, and telemetry suppression for Free Fire.
     */
    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] touchKeys = {
            "TouchResponseLevel=3",
            "HighFreqTouchHz=185",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "TouchSlopReduction=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, touchKeys, "[TouchEngine]");
        }
        Log.i(TAG, "FreeFire super-fast zero-delay touch applied for " + packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "AimAssist=1",
            "AimPrecision=100",
            "AimAssistStrength=10000",
            "AimAssistLevel=10",
            "AutoAim=1",
            "AimTracking=1",
            "TargetLock=1",
            "TargetLockSensitivity=10000",
            "AimAssistRadius=5000",
            "ScopeAimAssist=1",
            "RedDotAimAssist=1",
            "SniperAimAssist=1",
            "CrosshairMagnetism=100.00",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00",
            "GyroSampleRate=1000",
            "GyroSensitivity=1000",
            "GyroSensitivityRatio=20.0",
            "GyroStabilization=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectAimAssist(path);
            ConfigFileHelper.patchKeys(path, aimKeys, "[AimAssist]");
        }
        Log.i(TAG, "FreeFire 10000 Aim Assist & Precision applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "RecoilControl=1",
            "ZeroRecoil=1",
            "NoRecoil=1",
            "RecoilScale=0.00",
            "VerticalRecoil=0.00",
            "HorizontalRecoil=0.00",
            "VerticalRecoilScale=0.00",
            "HorizontalRecoilScale=0.00",
            "VerticalRecoilMultiplier=0.00",
            "HorizontalRecoilMultiplier=0.00",
            "RecoilReduction=1.00",
            "WeaponStability=500",
            "ScreenShake=0",
            "CameraShake=0",
            "NoCameraShake=1",
            "GunKick=0",
            "GunKickReduction=1.00",
            "WeaponKickReduction=1.00",
            "AllGunsRecoilReduction=1.00",
            "ScopeShakeReduction=1.00",
            "ScopeRecoilMultiplier=0.00",
            "ScopeStability=5.00",
            "IronSightRecoil=0.00",
            "RedDotRecoil=0.00",
            "Scope2xRecoil=0.00",
            "Scope4xRecoil=0.00",
            "SniperScopeRecoil=0.00",
            "ThermalScopeRecoil=0.00",
            "MP40RecoilScale=0.00",
            "M1887RecoilScale=0.00",
            "SCARRecoilScale=0.00",
            "AKRecoilScale=0.00",
            "GrozaRecoilScale=0.00",
            "WoodpeckerRecoilScale=0.00",
            "AWMRecoilScale=0.00",
            "M82BRecoilScale=0.00",
            "UMPRecoilScale=0.00",
            "BulletSpread=0.00",
            "CrosshairSpread=0.00",
            "SpreadScale=0.00",
            "BulletSpreadReduction=1",
            "FirstBulletAccuracy=1",
            "AimPunchReduction=1",
            "FlinchReduction=1",
            "WeaponSway=0"
        };
        for (String path : paths) {
            NativeConfigInjector.injectScopeZeroRecoil(path);
            ConfigFileHelper.patchKeys(path, recoilKeys, "[WeaponStability]");
        }
        Log.i(TAG, "FreeFire Zero Recoil & Weapon Stability for ALL Guns/Scopes applied for " + packageName);
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageKeys = {
            "DamageMultiplier=1000.00",
            "PhysicalDamageBoost=1000.00",
            "MagicDamageBoost=1000.00",
            "TrueDamageBoost=1000.00",
            "BulletDamageBoost=1000.00",
            "DamageBoost=1000.00",
            "DamageBoostRatio=1000.00",
            "HeadshotMultiplier=1000.00",
            "HeadshotDamageMultiplier=1000.00",
            "CriticalDamage=10000",
            "CriticalHitRate=100",
            "CriticalDamageRate=100",
            "CriticalDamageMultiplier=50.00",
            "PenetrationBoost=10000",
            "ArmorPenetration=10000",
            "HighDamageRateMode=1",
            "AttackSpeedMultiplier=25.00",
            "AttackSpeedBoost=25.00",
            "ReloadSpeedMultiplier=25.00",
            "FireRateMultiplier=25.00",
            "MovementSpeedMultiplier=15.00",
            "SprintSpeedMultiplier=15.00",
            "SprintSensitivity=1000",
            "AgilityMultiplier=15.00",
            "HitboxExpansion=100.00",
            "BulletVelocityMultiplier=200.00",
            "BulletVelocityScale=200.00",
            "BodyDamageMultiplier=50.00",
            "LimbDamageMultiplier=50.00",
            "ExplosiveDamageMultiplier=50.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[DamageScript]");
        }
        Log.i(TAG, "FreeFire Damage Boost 1000% & Headshot Multiplier applied for " + packageName);
    }

    /**
     * Injects Fast Cooldown, Character Active Skill CDR & Fast Reload for Free Fire.
     */
    public static void applyFastCooldownConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] cdKeys = {
            "SkillCoolDownReduceMode=1",
            "CooldownReductionBoost=0.99",
            "CooldownReduction=0.99",
            "SkillCooldownMultiplier=0.01",
            "CharacterSkillCooldownReduction=0.99",
            "ActiveSkillCooldown=0.01",
            "ReloadSpeedMultiplier=25.00",
            "AttackSpeedMultiplier=25.00",
            "AttackDelayReduction=1",
            "InstantSkillRelease=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectFastCooldown(path);
            ConfigFileHelper.patchKeys(path, cdKeys, "[FastCooldown]");
        }
        Log.i(TAG, "FreeFire Fast Cooldown 99% CDR & Instant Reload applied for " + packageName);
    }

    /**
     * Injects 1500+ Shield Overdrive & Gloo Wall/Armor Boost for Free Fire.
     */
    public static void applyShield1500Config(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] shieldKeys = {
            "ShieldMultiplier=1500.00",
            "ShieldCapacity=1500.00",
            "ShieldStrength=1500.00",
            "GlooWallHPBoost=1500.00",
            "ArmorBoost=50000",
            "VestDurability=1000.00",
            "DamageReduction=0.9999",
            "IncomingDamageReduction=0.9999",
            "HelmetDamageReduction=0.9999",
            "HealthRegenBoost=1000.00",
            "BurstDamageReduction=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectShield1500(path);
            ConfigFileHelper.patchKeys(path, shieldKeys, "[DefenseShield1500]");
        }
        Log.i(TAG, "FreeFire 1500+ Shield Overdrive applied for " + packageName);
    }

    /**
     * Injects Drone View FOV 180 for Free Fire.
     */
    public static void applyDroneViewConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] droneKeys = {
            "TPPFov=180",
            "FPPFov=180",
            "FieldOfView=180",
            "CameraFOV=180",
            "DroneView=1",
            "DroneViewHeight=4"
        };
        for (String path : paths) {
            NativeConfigInjector.injectDroneView(path);
            ConfigFileHelper.patchKeys(path, droneKeys, "[DroneViewUltra]");
        }
        Log.i(TAG, "FreeFire Drone View FOV 180 applied for " + packageName);
    }

    /**
     * Injects 1000% Armor Defense, Vest Durability, Helmet Protection, and Shield Capacity for Free Fire.
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
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=1000.00",
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
        Log.i(TAG, "FreeFire 1000% Armor Defense & 1500x Vest Durability applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Free Fire.
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
        Log.i(TAG, "FreeFire 15.0x Speed Boost & Sprint Agility applied for " + packageName);
    }

    /**
     * Injects 1000% Auto Aim Track, Bullet Tracking, Headshot Tracking, and Hitbox Expansion for Free Fire.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "TrackingBullet=1",
            "BulletTracking=1",
            "AutoTrackingBullet=1",
            "MagicBullet=1",
            "AutoAimTrack=1",
            "HeadshotTracking=1",
            "HitboxExpansion=100.00",
            "BulletMagnetism=100.00",
            "TargetLockTracking=1",
            "CrosshairMagnetism=100.00",
            "BulletCurveFactor=100.00",
            "BulletVelocityMultiplier=200.00",
            "FirstBulletAccuracy=1",
            "ProjectileHoming=1",
            "HomingStrength=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectTrackingBullet(path);
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "FreeFire 1000% Auto Aim Track & Bullet Tracking applied for " + packageName);
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    private static boolean applyPatch(String path, int targetFps) {
        int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;
        String[] keys = {
            "HighFPS=1",
            "HighFPSMode=1",
            "FPSMode=2",
            "FrameRateLevel=" + frameRateLevel,
            "MaxFPS=" + forcedFps,
            "TargetFPS=" + forcedFps,
            "UnlockFPS=1",
            "SuperHighFPS=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "GraphicLevel=3",
            "Shadow=1",
            "HighResolution=1",
            "VulkanEnabled=1",
            "HighFreqTouchHz=" + forcedFps
        };
        return ConfigFileHelper.patchKeys(path, keys, "[FFGraphics]");
    }
}
