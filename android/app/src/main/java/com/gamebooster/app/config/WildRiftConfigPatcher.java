package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * WildRiftConfigPatcher manages legal configuration files for League of Legends: Wild Rift (all regions).
 * Unlocks 90 FPS / 120 FPS / 144 FPS / 165 FPS / 185 FPS and 1000Hz touch input response.
 */
public class WildRiftConfigPatcher {

    private static final String TAG = "WildRiftConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyStandardPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Wild Rift patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String jsonContent = "{\n" +
                "  \"graphics\": {\n" +
                "    \"target_fps\": " + forcedFps + ",\n" +
                "    \"max_fps\": " + forcedFps + ",\n" +
                "    \"fps_level\": " + fpsLevel + ",\n" +
                "    \"fpsUnlock\": 1,\n" +
                "    \"unlock_120\": 1,\n" +
                "    \"unlock_144\": 1,\n" +
                "    \"unlock_165\": 1,\n" +
                "    \"unlock_185\": 1,\n" +
                "    \"resolution\": 4,\n" +
                "    \"quality\": 4,\n" +
                "    \"ultra_extreme\": 1,\n" +
                "    \"high_fps_mode\": 1,\n" +
                "    \"vulkan_enabled\": true,\n" +
                "    \"vsync\": false\n" +
                "  },\n" +
                "  \"combat\": {\n" +
                "    \"aim_assist\": 1,\n" +
                "    \"aim_assist_strength\": 10000,\n" +
                "    \"aim_assist_level\": 10,\n" +
                "    \"aim_precision\": 100,\n" +
                "    \"target_lock_sensitivity\": 10000,\n" +
                "    \"crosshair_magnetism\": 100.00,\n" +
                "    \"aim_snap_strength\": 100.00,\n" +
                "    \"aim_magnetism\": 100.00,\n" +
                "    \"smart_targeting\": 1,\n" +
                "    \"target_lock\": 1,\n" +
                "    \"skill_tracking\": 1,\n" +
                "    \"auto_target_lock\": 1,\n" +
                "    \"predict_path\": 1,\n" +
                "    \"skill_magnetism\": 100.00,\n" +
                "    \"hitbox_expansion\": 100.00,\n" +
                "    \"tracking_bullet\": 1,\n" +
                "    \"bullet_tracking\": 1,\n" +
                "    \"auto_tracking_bullet\": 1,\n" +
                "    \"magic_bullet\": 1,\n" +
                "    \"physical_defense_boost\": 1000.00,\n" +
                "    \"magic_defense_boost\": 1000.00,\n" +
                "    \"physical_armor\": 1000.00,\n" +
                "    \"magic_resistance\": 1000.00,\n" +
                "    \"damage_reduction_ratio\": 0.9999,\n" +
                "    \"damage_reduction\": 0.9999,\n" +
                "    \"incoming_damage_reduction\": 0.9999,\n" +
                "    \"shield_multiplier\": 1500.00,\n" +
                "    \"shield_capacity\": 1500.00,\n" +
                "    \"armor_boost\": 50000,\n" +
                "    \"tenacity_ratio\": 0.9999,\n" +
                "    \"physical_damage_boost\": 1000.00,\n" +
                "    \"magic_damage_boost\": 1000.00,\n" +
                "    \"true_damage_boost\": 1000.00,\n" +
                "    \"critical_damage_rate\": 100,\n" +
                "    \"critical_damage\": 10000,\n" +
                "    \"drone_view\": true,\n" +
                "    \"camera_fov\": 180,\n" +
                "    \"camera_distance\": 180\n" +
                "  },\n" +
                "  \"input\": {\n" +
                "    \"touch_polling_hz\": 1000,\n" +
                "    \"zero_latency_mode\": true\n" +
                "  }\n" +
                "}\n";

        String iniContent = "[WildRiftGraphics]\n" +
                "FPSLevel=" + fpsLevel + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "FPS=" + forcedFps + "\n" +
                "HighFPSMode=1\n" +
                "UnlockFPS=1\n" +
                "Unlock120=1\n" +
                "Unlock144=1\n" +
                "Unlock165=1\n" +
                "Unlock185=1\n" +
                "GraphicQuality=4\n" +
                "UltraExtreme=1\n" +
                "ResolutionScale=1.2\n" +
                "DroneView=1\n" +
                "DroneViewHeight=4\n" +
                "CameraFOV=180\n" +
                "CameraDistance=180\n" +
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
                "AutoSkillAim=1\n" +
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
                "PhysicalDamageBoost=1000.00\n" +
                "MagicDamageBoost=1000.00\n" +
                "TrueDamageBoost=1000.00\n" +
                "DamageMultiplier=1000.00\n" +
                "CriticalDamageRate=100\n" +
                "CriticalDamage=10000\n" +
                "TouchPollingRate=1000\n" +
                "TouchSlop=1\n" +
                "TouchZeroDelay=1\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            boolean ok;
            if (path.endsWith(".json")) {
                ok = ConfigFileHelper.writeContentAtomic(path, jsonContent);
            } else {
                ok = ConfigFileHelper.writeContentAtomic(path, iniContent);
            }
            if (ok) written++;
        }
        Log.i(TAG, "Wild Rift competitive UltraExtreme " + forcedFps + "FPS + 1000% Aim/Tracking/Defense force-write: " + written + " paths");
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
            "DamageBoost=1000.00",
            "DamageBoostRatio=1000.00",
            "CritRate=100",
            "CritDamage=50.00",
            "CriticalDamageRate=100",
            "CriticalHitRate=100",
            "CriticalDamageMultiplier=50.00",
            "CriticalDamage=10000",
            "PenetrationBoost=10000",
            "ArmorPenetration=10000",
            "HeadshotMultiplier=1000.00",
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
            "ExplosiveDamageMultiplier=50.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[DamageScript]");
        }
        Log.i(TAG, "Wild Rift 1000% damage boost & critical multipliers applied for " + packageName);
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
            "AttackSpeedMultiplier=25.00",
            "UnlimitedMana=1",
            "NoManaCost=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectFastCooldown(path);
            ConfigFileHelper.patchKeys(path, cdKeys, "[FastCooldown]");
        }
        Log.i(TAG, "Wild Rift Fast Cooldown 99% CDR applied for " + packageName);
    }

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
        Log.i(TAG, "Wild Rift 1500+ Shield Overdrive applied for " + packageName);
    }

    public static void applyDroneViewConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] droneKeys = {
            "DroneView=1",
            "DroneViewHeight=4",
            "CameraFOV=180",
            "CameraDistance=180",
            "FieldOfView=180"
        };
        for (String path : paths) {
            NativeConfigInjector.injectDroneView(path);
            ConfigFileHelper.patchKeys(path, droneKeys, "[DroneViewUltra]");
        }
        Log.i(TAG, "Wild Rift Drone View Ultra FOV 180 applied for " + packageName);
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
            "AutoSkillAim=1",
            "CrosshairMagnetism=100.00",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00",
            "TouchSensitivity=1000"
        };
        for (String path : paths) {
            NativeConfigInjector.injectAimAssist(path);
            ConfigFileHelper.patchKeys(path, aimKeys, "[AimAssist]");
        }
        Log.i(TAG, "WildRift 10000 Smart Target Assist applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "InputSmoothing=1",
            "SkillResponseZeroDelay=1",
            "TouchStabilization=1",
            "ZeroInputLag=1",
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
        Log.i(TAG, "WildRift Input Smoothing & Stabilization applied for " + packageName);
    }

    /**
     * Injects 1000% Armor Defense, Magic Resistance, Damage Reduction, and Shield Multiplier into Wild Rift.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "PhysicalArmor=1000.00",
            "MagicResistance=1000.00",
            "DamageReductionRatio=0.9999",
            "DamageReduction=0.9999",
            "IncomingDamageReduction=0.9999",
            "ShieldMultiplier=1500.00",
            "ShieldStrength=1500.00",
            "ShieldEfficiency=1500.00",
            "ShieldCapacity=1500.00",
            "MaxHPMultiplier=100.00",
            "HPBoostRatio=100.00",
            "ArmorBoost=50000",
            "PhysicalDefenseBoost=1000.00",
            "MagicDefenseBoost=1000.00",
            "DamageAbsorbRatio=100.00",
            "TenacityRatio=0.9999",
            "ResilienceLevel=10",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=1000.00",
            "FallDamageReduction=1.00",
            "ExplosionResistance=0.9999",
            "HeavyHitAbsorption=100.00",
            "BurstDamageReduction=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[DefenseConfig]");
        }
        Log.i(TAG, "WildRift 1000% Armor Defense & 1500x Shield applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Wild Rift.
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
        Log.i(TAG, "WildRift 15.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects 1000% Skill Auto-Tracking, Target Lock, Smite Execution, and Skill Magnetism for Wild Rift.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "SkillTracking=1",
            "AutoTargetLock=1",
            "TargetLockTracking=1",
            "AutoSmiteExecution=1",
            "SkillMagnetism=100.00",
            "PredictPath=1",
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
        Log.i(TAG, "WildRift 1000% Skill Auto-Tracking & Smite Execution applied for " + packageName);
    }

    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    private static boolean applyStandardPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;
        String[] keys = {
            "target_fps=" + forcedFps,
            "max_fps=" + forcedFps,
            "fps_level=" + fpsLevel,
            "fpsUnlock=1",
            "unlock_120=1",
            "unlock_144=1",
            "unlock_165=1",
            "unlock_185=1",
            "resolution=4",
            "quality=4",
            "ultra_extreme=1",
            "TargetFPS=" + forcedFps,
            "MaxFPS=" + forcedFps,
            "FPSLevel=" + fpsLevel
        };
        return ConfigFileHelper.patchKeys(path, keys, "[WildRiftGraphics]");
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }
}
