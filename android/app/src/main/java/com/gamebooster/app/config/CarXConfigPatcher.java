package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * CarXConfigPatcher manages legal configuration files for CarX Street, Asphalt 9/Legends Unite,
 * and high-fidelity racing games.
 * Unlocks 60 FPS / 90 FPS / 120 FPS / 144 FPS / 165 FPS / 185 FPS.
 */
public class CarXConfigPatcher {

    private static final String TAG = "CarXConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyStandardPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "CarX/Racing patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String iniContent = "[GraphicSettings]\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "FPSLevel=" + fpsLevel + "\n" +
                "FrameRateLimit=" + forcedFps + "\n" +
                "FPSLimit=0\n" +
                "HighFPSMode=1\n" +
                "UnlockFPS=1\n" +
                "UnlockHighFPS=1\n" +
                "Unlock120FPS=1\n" +
                "Unlock144FPS=1\n" +
                "Unlock165FPS=1\n" +
                "Unlock185FPS=1\n" +
                "GraphicQuality=4\n" +
                "UltraExtreme=1\n" +
                "HDRMode=1\n" +
                "Vsync=0\n" +
                "ResolutionScale=1.2\n" +
                "MotionBlur=0\n" +
                "ShadowQuality=2\n" +
                "DynamicResolution=0\n" +
                "AimAssist=1\n" +
                "AimAssistStrength=1000\n" +
                "AimAssistLevel=10\n" +
                "AimPrecision=10\n" +
                "SteeringAssist=1\n" +
                "SteeringAssistStrength=1000\n" +
                "TargetLockSensitivity=1000\n" +
                "CrosshairMagnetism=100.00\n" +
                "AimSnapStrength=100.00\n" +
                "AimMagnetism=100.00\n" +
                "RacingLineTracking=1\n" +
                "ApexAssist=1\n" +
                "AutoCounterSteer=1\n" +
                "DriftTrackingAssist=1\n" +
                "SteeringMagnetism=100.00\n" +
                "HitboxExpansion=50.00\n" +
                "TrackingBullet=1\n" +
                "BulletTracking=1\n" +
                "AutoTrackingBullet=1\n" +
                "MagicBullet=1\n" +
                "ChassisDurability=100.00\n" +
                "CollisionDamageReduction=0.001\n" +
                "BodyIntegrity=100.00\n" +
                "ImpactAbsorption=10.00\n" +
                "DamageReductionRatio=0.999\n" +
                "DamageReduction=0.999\n" +
                "IncomingDamageReduction=0.999\n" +
                "PhysicalDefenseBoost=100.00\n" +
                "ArmorBoost=10000\n" +
                "TenacityRatio=0.999\n" +
                "NitroMultiplier=100.00\n" +
                "TorqueBoost=100.00\n" +
                "DriftScoreMultiplier=10.0\n" +
                "TouchPollingRate=1000\n" +
                "TouchSlop=1\n" +
                "TouchZeroDelay=1\n";

        String jsonContent = "{\n" +
                "  \"graphics\": {\n" +
                "    \"max_fps\": " + forcedFps + ",\n" +
                "    \"target_fps\": " + forcedFps + ",\n" +
                "    \"frame_rate_limit\": " + forcedFps + ",\n" +
                "    \"fps_level\": " + fpsLevel + ",\n" +
                "    \"fps_unlocked\": true,\n" +
                "    \"unlock_fps\": true,\n" +
                "    \"unlock_120\": true,\n" +
                "    \"unlock_144\": true,\n" +
                "    \"unlock_165\": true,\n" +
                "    \"unlock_185\": true,\n" +
                "    \"ultra_extreme\": true,\n" +
                "    \"graphic_quality\": \"ultra\",\n" +
                "    \"resolution_scale\": 1.2,\n" +
                "    \"hdr_enabled\": true,\n" +
                "    \"vsync\": false\n" +
                "  },\n" +
                "  \"performance\": {\n" +
                "    \"aim_assist\": 1,\n" +
                "    \"aim_assist_strength\": 1000,\n" +
                "    \"aim_assist_level\": 10,\n" +
                "    \"aim_precision\": 10,\n" +
                "    \"steering_assist\": 1,\n" +
                "    \"steering_assist_strength\": 1000,\n" +
                "    \"target_lock_sensitivity\": 1000,\n" +
                "    \"crosshair_magnetism\": 100.00,\n" +
                "    \"aim_snap_strength\": 100.00,\n" +
                "    \"aim_magnetism\": 100.00,\n" +
                "    \"racing_line_tracking\": 1,\n" +
                "    \"apex_assist\": 1,\n" +
                "    \"drift_tracking_assist\": 1,\n" +
                "    \"steering_magnetism\": 100.00,\n" +
                "    \"hitbox_expansion\": 50.00,\n" +
                "    \"tracking_bullet\": 1,\n" +
                "    \"bullet_tracking\": 1,\n" +
                "    \"auto_tracking_bullet\": 1,\n" +
                "    \"magic_bullet\": 1,\n" +
                "    \"chassis_durability\": 100.00,\n" +
                "    \"collision_damage_reduction\": 0.001,\n" +
                "    \"body_integrity\": 100.00,\n" +
                "    \"impact_absorption\": 10.00,\n" +
                "    \"damage_reduction_ratio\": 0.999,\n" +
                "    \"damage_reduction\": 0.999,\n" +
                "    \"incoming_damage_reduction\": 0.999,\n" +
                "    \"physical_defense_boost\": 100.00,\n" +
                "    \"armor_boost\": 10000,\n" +
                "    \"tenacity_ratio\": 0.999,\n" +
                "    \"nitro_boost\": 100.00,\n" +
                "    \"torque_multiplier\": 100.00,\n" +
                "    \"drift_multiplier\": 10.0\n" +
                "  }\n" +
                "}\n";

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
        Log.i(TAG, "CarX competitive UltraExtreme " + forcedFps + "FPS + 1000% Aim/Tracking/Defense force-write: " + written + " paths");
        return written > 0;
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] boostKeys = {
            "NitroMultiplier=100.00",
            "TorqueBoost=100.00",
            "DriftScoreMultiplier=10.00",
            "ThrottleResponse=10.00",
            "EnginePowerMultiplier=100.00",
            "TopSpeedBoost=10.00",
            "AccelerationMultiplier=100.00",
            "DamageMultiplier=100.00",
            "PhysicalDamageBoost=100.00",
            "BulletDamageBoost=100.00",
            "DamageBoost=100.00",
            "DamageBoostRatio=100.00",
            "HeadshotMultiplier=100.00",
            "HighDamageRateMode=1",
            "MovementSpeedMultiplier=10.00",
            "SprintSpeedMultiplier=10.00",
            "SprintSensitivity=500",
            "AgilityMultiplier=10.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, boostKeys, "[EngineTune]");
        }
        Log.i(TAG, "CarX/Racing 1000% Nitro, Torque & Engine Boost applied for " + packageName);
    }

    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] touchKeys = {
            "TouchRate=1000",
            "SteeringSensitivity=1.1",
            "InputDeadZone=0.0",
            "TouchZeroDelay=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, touchKeys, "[SteeringControls]");
        }
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "AimAssist=1",
            "AimPrecision=10",
            "AimAssistStrength=1000",
            "AimAssistLevel=10",
            "SteeringAssist=1",
            "SteeringAssistStrength=1000",
            "CounterSteerAssist=1",
            "CrosshairMagnetism=100.00",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00",
            "TargetLock=1",
            "TargetLockSensitivity=1000",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "SteeringSensitivity=500",
            "AutoSteering=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectAimAssist(path);
            ConfigFileHelper.patchKeys(path, aimKeys, "[SteeringAssist]");
        }
        Log.i(TAG, "CarX 1000% Steering/Aim Assist & Gyro 1000Hz applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "DriftStability=500",
            "TireGripBoost=10.0",
            "ChassisStability=500",
            "CameraShake=0",
            "NoCameraShake=1",
            "ZeroCameraShake=1",
            "TractionControl=1",
            "RecoilControl=1",
            "ZeroRecoil=1",
            "NoRecoil=1",
            "RecoilScale=0.00",
            "WeaponStability=500",
            "ScopeStability=5.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectNoRecoil(path);
            ConfigFileHelper.patchKeys(path, recoilKeys, "[StabilityEngine]");
        }
        Log.i(TAG, "CarX Drift Stability & Chassis Balance applied for " + packageName);
    }

    /**
     * Injects 1000% Chassis Durability, Collision Damage Reduction, and Impact Absorption into CarX/Racing games.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "ChassisDurability=100.00",
            "CollisionDamageReduction=0.001",
            "BodyIntegrity=100.00",
            "ImpactAbsorption=10.00",
            "ArmorBoost=10000",
            "DamageReductionRatio=0.999",
            "DamageReduction=0.999",
            "IncomingDamageReduction=0.999",
            "PhysicalDefenseBoost=100.00",
            "MagicDefenseBoost=100.00",
            "ShieldMultiplier=100.00",
            "DamageAbsorbRatio=50.00",
            "TenacityRatio=0.999",
            "ResilienceLevel=10",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=100.00",
            "ExplosionResistance=0.999",
            "FallDamageReduction=1.00",
            "HeavyHitAbsorption=10.00",
            "BurstDamageReduction=10.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[Durability]");
        }
        Log.i(TAG, "CarX 1000% Chassis Durability & Impact Absorption applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for CarX / Racing games.
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
        Log.i(TAG, "CarX 10.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects 1000% Racing Line Tracking, Apex Assist, Auto Counter-Steer, and Drift Tracking for CarX/Racing games.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "RacingLineTracking=1",
            "ApexAssist=1",
            "AutoCounterSteer=1",
            "DriftTrackingAssist=1",
            "SteeringMagnetism=100.00",
            "TireGripTracking=1",
            "TargetLockTracking=1",
            "HitboxExpansion=50.00",
            "BulletMagnetism=100.00",
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
        Log.i(TAG, "CarX 1000% Racing Line Tracking & Apex Assist applied for " + packageName);
    }

    private static boolean applyStandardPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;
        String[] keys = {
            "TargetFPS=" + forcedFps,
            "MaxFPS=" + forcedFps,
            "FrameRateLimit=" + forcedFps,
            "FPSLevel=" + fpsLevel,
            "GraphicQuality=4",
            "UltraExtreme=1",
            "HighFPSMode=1",
            "UnlockFPS=1",
            "UnlockHighFPS=1",
            "Unlock120FPS=1",
            "Unlock144FPS=1",
            "Unlock165FPS=1",
            "Unlock185FPS=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[GraphicSettings]");
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }
}
