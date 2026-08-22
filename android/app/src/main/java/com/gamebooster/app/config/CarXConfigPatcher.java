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
                "NitroMultiplier=1.90\n" +
                "TorqueBoost=1.90\n" +
                "DriftScoreMultiplier=2.0\n" +
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
                "    \"nitro_boost\": 1.90,\n" +
                "    \"torque_multiplier\": 1.90,\n" +
                "    \"drift_multiplier\": 2.0\n" +
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
        Log.i(TAG, "CarX competitive UltraExtreme " + forcedFps + "FPS force-write: " + written + " paths");
        return written > 0;
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] boostKeys = {
            "NitroMultiplier=5.00",
            "TorqueBoost=5.00",
            "DriftScoreMultiplier=5.00",
            "ThrottleResponse=3.00",
            "EnginePowerMultiplier=5.00",
            "TopSpeedBoost=3.00",
            "AccelerationMultiplier=5.00",
            "DamageMultiplier=5.00",
            "PhysicalDamageBoost=5.00",
            "BulletDamageBoost=5.00",
            "DamageBoost=5.00",
            "HighDamageRateMode=1",
            "MovementSpeedMultiplier=3.00",
            "SprintSpeedMultiplier=3.00",
            "SprintSensitivity=200",
            "AgilityMultiplier=3.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, boostKeys, "[EngineTune]");
        }
        Log.i(TAG, "CarX/Racing 5.0x Nitro & Torque boost applied for " + packageName);
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
            "SteeringAssist=1",
            "SteeringAssistStrength=150",
            "CounterSteerAssist=1",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "SteeringSensitivity=150",
            "AutoSteering=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, aimKeys, "[SteeringAssist]");
        }
        Log.i(TAG, "CarX Steering Assist & Gyro 1000Hz applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "DriftStability=150",
            "TireGripBoost=2.5",
            "ChassisStability=150",
            "CameraShake=0",
            "NoCameraShake=1",
            "ZeroCameraShake=1",
            "TractionControl=1",
            "RecoilControl=1",
            "ZeroRecoil=1",
            "NoRecoil=1",
            "RecoilScale=0.00",
            "WeaponStability=150",
            "ScopeStability=2.50"
        };
        for (String path : paths) {
            NativeConfigInjector.injectNoRecoil(path);
            ConfigFileHelper.patchKeys(path, recoilKeys, "[StabilityEngine]");
        }
        Log.i(TAG, "CarX Drift Stability & Chassis Balance applied for " + packageName);
    }

    /**
     * Injects Chassis Durability, Collision Damage Reduction, and Impact Absorption into CarX/Racing games.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "ChassisDurability=5.00",
            "CollisionDamageReduction=0.00",
            "BodyIntegrity=5.00",
            "ImpactAbsorption=1.00",
            "ArmorBoost=500",
            "DamageReductionRatio=0.85",
            "DamageReduction=0.85",
            "IncomingDamageReduction=0.85",
            "PhysicalDefenseBoost=5.00",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=5.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[Durability]");
        }
        Log.i(TAG, "CarX Chassis Durability 5.0x & Impact Absorption applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for CarX / Racing games.
     */
    public static void applySpeedBoostConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] speedKeys = {
            "MovementSpeedMultiplier=3.00",
            "MovementSpeedBoost=3.00",
            "SprintSpeedMultiplier=3.00",
            "SprintSpeedBoost=3.00",
            "SprintSensitivity=200",
            "AgilityMultiplier=3.00",
            "AttackSpeedMultiplier=3.00",
            "AttackSpeedBoost=3.00",
            "ReloadSpeedMultiplier=3.00",
            "FireRateMultiplier=2.50",
            "BulletVelocityMultiplier=5.00",
            "BulletVelocityScale=5.00",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "HighSpeedMovement=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectSpeedBoost(path);
            ConfigFileHelper.patchKeys(path, speedKeys, "[SpeedEngine]");
        }
        Log.i(TAG, "CarX 3.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects Racing Line Tracking, Apex Assist, Auto Counter-Steer, and Drift Tracking for CarX/Racing games.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "RacingLineTracking=1",
            "ApexAssist=1",
            "AutoCounterSteer=1",
            "DriftTrackingAssist=1",
            "SteeringMagnetism=1.50",
            "TireGripTracking=1",
            "TargetLockTracking=1",
            "TrackingBullet=1",
            "BulletTracking=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "CarX Racing Line Tracking & Apex Assist applied for " + packageName);
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
