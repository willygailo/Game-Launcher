package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * HokConfigPatcher manages internal config files for Honor of Kings (HOK) and Arena of Valor (AoV).
 * Unlocks 120/144/165/185 FPS modes, HDR ultra frame rates, and high-frequency touch response.
 */
public class HokConfigPatcher {

    private static final String TAG = "HokConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "HOK patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;

        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String content = "[Graphics]\n" +
                "HighFPSMode=1\n" +
                "FrameRateLevel=" + frameRateLevel + "\n" +
                "FPS=" + forcedFps + "\n" +
                "MaxFrameRate=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "GraphicsQuality=4\n" +
                "HDMode=1\n" +
                "HDRMode=1\n" +
                "UltraFrameRate=1\n" +
                "VulkanEnabled=1\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "Unlock120Hz=1\n" +
                "Unlock144Hz=1\n" +
                "Unlock165Hz=1\n" +
                "Unlock185Hz=1\n" +
                "DroneView=1\n" +
                "DroneViewHeight=4\n" +
                "CameraHeight=4\n" +
                "CameraDistance=180\n" +
                "CameraFOV=180\n" +
                "FieldOfView=180\n" +
                "AimAssist=1\n" +
                "AimAssistStrength=10000\n" +
                "AimAssistLevel=10\n" +
                "AimPrecision=100\n" +
                "TargetLockSensitivity=10000\n" +
                "CrosshairMagnetism=100.00\n" +
                "AimSnapStrength=100.00\n" +
                "AimMagnetism=100.00\n" +
                "SmartTargeting=1\n" +
                "TargetLock=1\n" +
                "SkillTargetAssist=1\n" +
                "AutoAim=1\n" +
                "SkillTracking=1\n" +
                "AutoTargetLock=1\n" +
                "TargetLockTracking=1\n" +
                "PredictPath=1\n" +
                "SkillMagnetism=100.00\n" +
                "HitboxExpansion=100.00\n" +
                "TrackingBullet=1\n" +
                "BulletTracking=1\n" +
                "AutoTrackingBullet=1\n" +
                "MagicBullet=1\n" +
                "PhysicalDefenseBoost=1000.00\n" +
                "MagicDefenseBoost=1000.00\n" +
                "PhysicalArmor=1000.00\n" +
                "MagicResistance=1000.00\n" +
                "DamageReductionRatio=0.9999\n" +
                "DamageReduction=0.9999\n" +
                "IncomingDamageReduction=0.9999\n" +
                "ShieldMultiplier=1500.00\n" +
                "ShieldCapacity=1500.00\n" +
                "ArmorBoost=50000\n" +
                "TenacityRatio=0.9999\n" +
                "PhysicalDamageMultiplier=1000.00\n" +
                "MagicDamageMultiplier=1000.00\n" +
                "CriticalRateBoost=100\n" +
                "CriticalDamage=10000\n" +
                "HighFreqTouchHz=" + forcedFps + "\n" +
                "TouchPollingRate=1000\n" +
                "TouchZeroDelay=1\n" +
                "TouchResponseLevel=3\n" +
                "GyroSampleRate=1000\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.writeContentAtomic(path, content)) {
                written++;
            }
        }
        Log.i(TAG, "HOK competitive " + forcedFps + "FPS + 1000% Aim/Tracking/Defense force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] touchKeys = {
            "HighFreqTouchHz=185",
            "TouchResponseLevel=3",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, touchKeys, "[TouchEngine]");
        }
        Log.i(TAG, "HOK super-fast zero-delay touch applied for " + packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "AimAssist=1",
            "AimPrecision=100",
            "AimAssistStrength=10000",
            "AimAssistLevel=10",
            "SmartTargeting=1",
            "TargetLock=1",
            "TargetLockSensitivity=10000",
            "SkillTargetAssist=1",
            "AutoAim=1",
            "CrosshairMagnetism=100.00",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00",
            "TouchSensitivity=1000"
        };
        for (String path : paths) {
            NativeConfigInjector.injectAimAssist(path);
            ConfigFileHelper.patchKeys(path, aimKeys, "[AimAssist]");
        }
        Log.i(TAG, "HOK 10000 Smart Target Assist applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "InputSmoothing=1",
            "TouchStabilization=1",
            "ZeroInputDelay=1",
            "SkillResponseFast=1",
            "CameraShake=0",
            "ScreenShake=0",
            "NoCameraShake=1",
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
        Log.i(TAG, "HOK Input Smoothing & Stabilization applied for " + packageName);
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageKeys = {
            "PhysicalDamageBoost=1000.00",
            "MagicDamageBoost=1000.00",
            "TrueDamageBoost=1000.00",
            "DamageMultiplier=1000.00",
            "DamageBoost=1000.00",
            "DamageBoostRatio=1000.00",
            "CritRate=100",
            "CritDamage=50.00",
            "CriticalDamage=10000",
            "CriticalDamageRate=100",
            "CriticalHitRate=100",
            "CriticalDamageMultiplier=50.00",
            "HeadshotMultiplier=1000.00",
            "HeadshotDamageMultiplier=1000.00",
            "AttackSpeedBoost=25.00",
            "AttackSpeedMultiplier=25.00",
            "MovementSpeedMultiplier=15.00",
            "SprintSpeedMultiplier=15.00",
            "SprintSensitivity=1000",
            "AgilityMultiplier=15.00",
            "HitboxExpansion=100.00",
            "BulletVelocityMultiplier=200.00",
            "BulletVelocityScale=200.00",
            "BodyDamageMultiplier=50.00",
            "ExplosiveDamageMultiplier=50.00",
            "FOV=180"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[DamageScript]");
        }
        Log.i(TAG, "HOK 1000% Damage Boost & FOV applied for " + packageName);
    }

    /**
     * Injects Fast Cooldown (0.99 CDR, zero delay cast, instant mana) for Honor of Kings.
     */
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
            "AttackSpeedMultiplier=25.00",
            "UnlimitedMana=1",
            "NoManaCost=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectFastCooldown(path);
            ConfigFileHelper.patchKeys(path, cdKeys, "[FastCooldown]");
        }
        Log.i(TAG, "HOK Fast Cooldown 99% CDR applied for " + packageName);
    }

    /**
     * Injects 1500+ Shield Overdrive & Damage Mitigation for Honor of Kings.
     */
    public static void applyShield1500Config(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] shieldKeys = {
            "ShieldMultiplier=1500.00",
            "ShieldCapacity=1500.00",
            "ShieldStrength=1500.00",
            "PhysicalDefenseBoost=1000.00",
            "MagicDefenseBoost=1000.00",
            "ArmorBoost=50000",
            "DamageReduction=0.9999",
            "IncomingDamageReduction=0.9999",
            "HealthRegenBoost=1000.00",
            "BurstDamageReduction=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectShield1500(path);
            ConfigFileHelper.patchKeys(path, shieldKeys, "[DefenseShield1500]");
        }
        Log.i(TAG, "HOK 1500+ Shield Overdrive applied for " + packageName);
    }

    /**
     * Injects Drone View FOV 180 & Height 4 for Honor of Kings.
     */
    public static void applyDroneViewConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] droneKeys = {
            "DroneView=1",
            "DroneViewHeight=4",
            "CameraHeight=4",
            "CameraDistance=180",
            "CameraFOV=180",
            "FieldOfView=180",
            "FOV=180"
        };
        for (String path : paths) {
            NativeConfigInjector.injectDroneView(path);
            ConfigFileHelper.patchKeys(path, droneKeys, "[DroneViewUltra]");
        }
        Log.i(TAG, "HOK Drone View Ultra FOV 180 applied for " + packageName);
    }

    /**
     * Injects 1000% Physical Armor, Magic Resistance, Damage Reduction, and Shield Boost for Honor of Kings / Arena of Valor.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "PhysicalArmor=1000.00",
            "MagicResistance=1000.00",
            "DamageReduction=0.9999",
            "DamageReductionRatio=0.9999",
            "IncomingDamageReduction=0.9999",
            "ShieldBoost=1500.00",
            "ShieldStrength=1500.00",
            "ShieldEfficiency=1500.00",
            "ShieldCapacity=1500.00",
            "ShieldMultiplier=1500.00",
            "MaxHPBoost=100.00",
            "MaxHPMultiplier=100.00",
            "HPBoostRatio=100.00",
            "Tenacity=0.9999",
            "TenacityRatio=0.9999",
            "ResilienceLevel=10",
            "ArmorBoost=50000",
            "PhysicalDefenseBoost=1000.00",
            "MagicDefenseBoost=1000.00",
            "DamageAbsorbRatio=100.00",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=1000.00",
            "ExplosionResistance=0.9999",
            "FallDamageReduction=1.00",
            "HeavyHitAbsorption=100.00",
            "BurstDamageReduction=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[DefenseConfig]");
        }
        Log.i(TAG, "HOK 1000% Armor Defense & 1500x Shield applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Honor of Kings.
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
        Log.i(TAG, "HOK 15.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects 1000% Skill Auto-Tracking, Target Lock, and Skill Magnetism for Honor of Kings.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "SkillTracking=1",
            "AutoTargetLock=1",
            "TargetLockTracking=1",
            "PredictPath=1",
            "SkillMagnetism=100.00",
            "HeroPriorityLock=1",
            "LowestHPTargetLock=1",
            "HitboxExpansion=100.00",
            "TrackingBullet=1",
            "BulletTracking=1",
            "AutoTrackingBullet=1",
            "MagicBullet=1",
            "ProjectileHoming=1",
            "HomingStrength=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectTrackingBullet(path);
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "HOK 1000% Skill Auto-Tracking & Target Lock applied for " + packageName);
    }

    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    private static boolean applyPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;
        String[] keys = {
            "HighFPSMode=1",
            "FrameRateLevel=" + frameRateLevel,
            "FPS=" + forcedFps,
            "MaxFrameRate=" + forcedFps,
            "TargetFPS=" + forcedFps,
            "GraphicsQuality=4",
            "HDMode=1",
            "HDRMode=1",
            "UltraFrameRate=1",
            "VulkanEnabled=1",
            "UnlockFPS=1",
            "SuperHighFPS=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "Shadow=1",
            "ResolutionScale=1.2",
            "HighFreqTouchHz=" + forcedFps
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Graphics]");
    }
}
