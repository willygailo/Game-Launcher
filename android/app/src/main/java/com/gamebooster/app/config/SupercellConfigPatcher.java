package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * SupercellConfigPatcher manages legal configuration files for Brawl Stars, Clash Royale, and Squad Busters.
 * Unlocks native 120 FPS / 144 FPS / 165 FPS / 185 FPS display rendering and 1000Hz touch polling rate.
 */
public class SupercellConfigPatcher {

    private static final String TAG = "SupercellConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyStandardPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Supercell patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String iniContent = "[SupercellEngine]\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "FPSLevel=" + fpsLevel + "\n" +
                "FPSCap=" + forcedFps + "\n" +
                "HighFPSMode=1\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "Unlock120Hz=1\n" +
                "Unlock144Hz=1\n" +
                "Unlock165Hz=1\n" +
                "Unlock185Hz=1\n" +
                "HighRefreshRate=1\n" +
                "GraphicQuality=4\n" +
                "UltraExtreme=1\n" +
                "HDRMode=1\n" +
                "ResolutionScale=1.2\n" +
                "TouchPollingRate=1000\n" +
                "TouchSlop=1\n" +
                "TouchZeroDelay=1\n" +
                "DroneView=1\n" +
                "DroneViewHeight=4\n" +
                "CameraDistance=10.0\n" +
                "CameraFOV=180\n" +
                "FieldOfView=180\n" +
                "FOV=180\n" +
                "AimAssist=1\n" +
                "AimAssistStrength=10000\n" +
                "AimAssistLevel=10\n" +
                "AimPrecision=100\n" +
                "AutoAimAssist=1\n" +
                "AimSnap=1\n" +
                "SmartTargeting=1\n" +
                "TargetLock=1\n" +
                "TargetLockSensitivity=10000\n" +
                "CrosshairMagnetism=100.00\n" +
                "AimSnapStrength=100.00\n" +
                "AimMagnetism=100.00\n" +
                "AutoAttackTracking=1\n" +
                "SuperAttackLock=1\n" +
                "ProjectileHoming=1\n" +
                "HomingStrength=100.00\n" +
                "AutoTargetLock=1\n" +
                "SkillMagnetism=100.00\n" +
                "HitboxExpansion=100.00\n" +
                "TrackingBullet=1\n" +
                "BulletTracking=1\n" +
                "AutoTrackingBullet=1\n" +
                "MagicBullet=1\n" +
                "ShieldMultiplier=1500.00\n" +
                "ShieldCapacity=1500.00\n" +
                "ShieldStrength=1500.00\n" +
                "ShieldEfficiency=1500.00\n" +
                "DefenseRatio=1000.00\n" +
                "DamageReduction=0.9999\n" +
                "DamageReductionRatio=0.9999\n" +
                "IncomingDamageReduction=0.9999\n" +
                "PhysicalDefenseBoost=1000.00\n" +
                "MagicDefenseBoost=1000.00\n" +
                "ArmorBoost=50000\n" +
                "TenacityRatio=0.9999\n" +
                "DamageMultiplier=1000.00\n" +
                "SuperAttackMultiplier=1000.00\n" +
                "CriticalStrikeRate=100\n" +
                "CriticalDamage=10000\n" +
                "AutoAimGuide=1\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.writeContentAtomic(path, iniContent)) {
                written++;
            }
        }
        Log.i(TAG, "Supercell competitive UltraExtreme " + forcedFps + "FPS + 1000% Aim/Tracking/Defense force-write: " + written + " paths");
        return written > 0;
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
            "SuperAttackMultiplier=1000.00",
            "HeadshotMultiplier=1000.00",
            "CriticalStrikeRate=100",
            "CriticalDamage=10000",
            "CriticalHitRate=100",
            "CriticalDamageMultiplier=50.00",
            "PenetrationBoost=10000",
            "ArmorPenetration=10000",
            "HitboxExpansion=100.00",
            "AttackSpeedBoost=25.00",
            "AttackSpeedMultiplier=25.00",
            "MovementSpeedMultiplier=15.00",
            "SprintSpeedMultiplier=15.00",
            "SprintSensitivity=1000",
            "AgilityMultiplier=15.00",
            "BulletVelocityMultiplier=200.00",
            "BulletVelocityScale=200.00",
            "BodyDamageMultiplier=50.00",
            "ExplosiveDamageMultiplier=50.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[DamageScript]");
        }
        Log.i(TAG, "Supercell 1000% damage boost & attack multipliers applied for " + packageName);
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] touchKeys = {
            "TouchRate=1000",
            "TouchResponse=1",
            "TouchSlopReduction=1",
            "TouchZeroDelay=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, touchKeys, "[TouchEngine]");
        }
        Log.i(TAG, "Supercell Input Smoothing & Stabilization applied for " + packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "AimAssist=1",
            "AimPrecision=100",
            "AimAssistStrength=10000",
            "AimAssistLevel=10",
            "AutoAimAssist=1",
            "AimSnap=1",
            "SmartTargeting=1",
            "TargetLock=1",
            "TargetLockSensitivity=10000",
            "CrosshairMagnetism=100.00",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00",
            "TouchSensitivity=1000"
        };
        for (String path : paths) {
            NativeConfigInjector.injectAimAssist(path);
            ConfigFileHelper.patchKeys(path, aimKeys, "[AimAssist]");
        }
        Log.i(TAG, "Supercell 10000 Auto-Aim Assist applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "InputZeroDelay=1",
            "MovementStabilization=1",
            "TouchSmoothing=1",
            "ZeroInputLag=1",
            "CameraShake=0",
            "NoCameraShake=1",
            "ScreenShake=0",
            "AimPunchReduction=1",
            "FlinchReduction=1",
            "ScopeShakeReduction=1.00",
            "ScopeStability=5.00",
            "WeaponSway=0",
            "RecoilControl=1",
            "ZeroRecoil=1",
            "NoRecoil=1",
            "SpreadScale=0.00",
            "CrosshairSpread=0.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectNoRecoil(path);
            ConfigFileHelper.patchKeys(path, recoilKeys, "[WeaponStability]");
        }
        Log.i(TAG, "Supercell Movement Stabilization applied for " + packageName);
    }

    public static void applyFastCooldownConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] cdKeys = {
            "SkillCoolDownReduceMode=1",
            "CooldownReductionBoost=0.99",
            "CooldownReduction=0.99",
            "SkillCooldownMultiplier=0.01",
            "SkillAnimationCancelZeroDelay=1",
            "SkillResponseZeroDelay=1",
            "SkillCastZeroDelay=1",
            "InstantSkillRelease=1",
            "SuperAttackAutoCharge=1",
            "UnlimitedSuper=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectFastCooldown(path);
            ConfigFileHelper.patchKeys(path, cdKeys, "[FastCooldown]");
        }
        Log.i(TAG, "Supercell Fast Cooldown 99% CDR & Instant Super applied for " + packageName);
    }

    public static void applyShield1500Config(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] shieldKeys = {
            "ShieldMultiplier=1500.00",
            "ShieldCapacity=1500.00",
            "ShieldStrength=1500.00",
            "DefenseRatio=1000.00",
            "ArmorBoost=50000",
            "DamageReduction=0.9999",
            "IncomingDamageReduction=0.9999"
        };
        for (String path : paths) {
            NativeConfigInjector.injectShield1500(path);
            ConfigFileHelper.patchKeys(path, shieldKeys, "[DefenseShield1500]");
        }
        Log.i(TAG, "Supercell 1500+ Shield Overdrive applied for " + packageName);
    }

    public static void applyDroneViewConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] droneKeys = {
            "DroneView=1",
            "DroneViewHeight=4",
            "CameraDistance=10.0",
            "CameraFOV=180",
            "FieldOfView=180",
            "FOV=180"
        };
        for (String path : paths) {
            NativeConfigInjector.injectDroneView(path);
            ConfigFileHelper.patchKeys(path, droneKeys, "[DroneViewUltra]");
        }
        Log.i(TAG, "Supercell Drone View Ultra FOV 180 applied for " + packageName);
    }

    /**
     * Injects 1000% Shield Multiplier, Defense Ratio, Damage Reduction, and HP Boost for Supercell games.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "ShieldMultiplier=1500.00",
            "ShieldCapacity=1500.00",
            "ShieldStrength=1500.00",
            "ShieldEfficiency=1500.00",
            "DefenseRatio=1000.00",
            "DamageReduction=0.9999",
            "DamageReductionRatio=0.9999",
            "IncomingDamageReduction=0.9999",
            "HPBoost=100.00",
            "HPBoostRatio=100.00",
            "MaxHPMultiplier=100.00",
            "DamageAbsorbRatio=100.00",
            "ArmorBoost=50000",
            "PhysicalDefenseBoost=1000.00",
            "MagicDefenseBoost=1000.00",
            "TenacityRatio=0.9999",
            "ResilienceLevel=10",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=1000.00",
            "ExplosionResistance=0.9999",
            "FallDamageReduction=1.00",
            "HeavyHitAbsorption=100.00",
            "BurstDamageReduction=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[CombatDefense]");
        }
        Log.i(TAG, "Supercell 1000% Shield & Defense Reduction applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Supercell games.
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
        Log.i(TAG, "Supercell 15.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects 1000% Auto Attack Tracking, Super Attack Lock, Projectile Homing, and Skill Magnetism for Supercell games.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "AutoAttackTracking=1",
            "SuperAttackLock=1",
            "ProjectileHoming=1",
            "HomingStrength=100.00",
            "AutoTargetLock=1",
            "SkillMagnetism=100.00",
            "HitboxExpansion=100.00",
            "BulletMagnetism=100.00",
            "TrackingBullet=1",
            "BulletTracking=1",
            "AutoTrackingBullet=1",
            "MagicBullet=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectTrackingBullet(path);
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "Supercell 1000% Auto Attack Tracking & Super Lock applied for " + packageName);
    }

    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    private static boolean applyStandardPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;
        String[] keys = {
            "TargetFPS=" + forcedFps,
            "MaxFPS=" + forcedFps,
            "FPSCap=" + forcedFps,
            "FPSLevel=" + fpsLevel,
            "GraphicQuality=4",
            "UltraExtreme=1",
            "HDRMode=1",
            "ResolutionScale=1.2",
            "HighFPSMode=1",
            "UnlockFPS=1",
            "SuperHighFPS=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "HighRefreshRate=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[SupercellEngine]");
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }
}
