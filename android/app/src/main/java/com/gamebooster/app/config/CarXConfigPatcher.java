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

    // ─── UltraExtreme 144fps SuperSmooth Patch ───────────────────────────────

    /**
     * Applies 144fps SuperSmooth + UltraExtreme max graphics to CarX / Asphalt racing titles.
     *
     * @return true if at least one path was written
     */
    public static boolean patchUltraExtreme144(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            "TargetFPS=144",
            "MaxFPS=144",
            "FPSLimit=0",
            "FrameRateLimit=144",
            "FPSLevel=8",
            "HighFPSMode=1",
            "UnlockFPS=1",
            "UnlockHighFPS=1",
            "Unlock120FPS=1",
            "Unlock144FPS=1",
            "Unlock165FPS=1",
            "Unlock185FPS=1",
            "GraphicQuality=4",
            "UltraExtreme=1",
            "bUseUltraExtreme=True",
            "HDRMode=1",
            "Vsync=0",
            "ResolutionScale=1.2",
            "ShadowQuality=2",
            "DynamicResolution=0",
            "TouchPollingRate=1000",
            "TouchSlop=1",
            "TouchZeroDelay=1",
            "TouchBoostHz=144",
            // ── Racing Engine & Speed Agility ──
            "MovementSpeedMultiplier=15.00", "SprintSpeedMultiplier=15.00",
            "AgilityMultiplier=15.00", "ZeroInputLag=1", "SteeringResponseRate=1000",
            "ZeroDeadzone=1"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[GraphicSettings]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "CarX UltraExtreme144 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    /**
     * Injects 185 FPS and Ultra Graphics presets for CarX / Asphalt / Racing Games.
     */
    public static boolean patchUltraExtreme185(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            "TargetFPS=185",
            "MaxFPS=185",
            "FPSLimit=0",
            "FrameRateLimit=185",
            "FPSLevel=10",
            "HighFPSMode=1",
            "UnlockFPS=1",
            "UnlockHighFPS=1",
            "Unlock120FPS=1",
            "Unlock144FPS=1",
            "Unlock165FPS=1",
            "Unlock185FPS=1",
            "GraphicQuality=5",
            "UltraExtreme=1",
            "bUseUltraExtreme=True",
            "HDRMode=1",
            "Vsync=0",
            "ResolutionScale=1.2",
            "ShadowQuality=2",
            "DynamicResolution=0",
            "TouchPollingRate=1000",
            "TouchSlop=1",
            "TouchZeroDelay=1",
            "TouchBoostHz=185",
            // ── Racing Engine & Speed Agility ──
            "MovementSpeedMultiplier=15.00", "SprintSpeedMultiplier=15.00",
            "AgilityMultiplier=15.00", "ZeroInputLag=1", "SteeringResponseRate=1000",
            "ZeroDeadzone=1"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[GraphicSettings]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "CarX UltraExtreme185 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
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
                "AimAssistStrength=10000\n" +
                "AimAssistLevel=10\n" +
                "AimPrecision=100\n" +
                "SteeringAssist=1\n" +
                "SteeringAssistStrength=10000\n" +
                "TargetLockSensitivity=10000\n" +
                "CrosshairMagnetism=100.00\n" +
                "AimSnapStrength=100.00\n" +
                "AimMagnetism=100.00\n" +
                "RacingLineTracking=1\n" +
                "ApexAssist=1\n" +
                "AutoCounterSteer=1\n" +
                "DriftTrackingAssist=1\n" +
                "SteeringMagnetism=100.00\n" +
                "HitboxExpansion=100.00\n" +
                "TrackingBullet=1\n" +
                "BulletTracking=1\n" +
                "AutoTrackingBullet=1\n" +
                "MagicBullet=1\n" +
                "ChassisDurability=1500.00\n" +
                "CollisionDamageReduction=0.0001\n" +
                "BodyIntegrity=1500.00\n" +
                "ImpactAbsorption=100.00\n" +
                "DamageReductionRatio=0.9999\n" +
                "DamageReduction=0.9999\n" +
                "IncomingDamageReduction=0.9999\n" +
                "PhysicalDefenseBoost=1000.00\n" +
                "ArmorBoost=50000\n" +
                "ShieldMultiplier=1500.00\n" +
                "TenacityRatio=0.9999\n" +
                "NitroMultiplier=1000.00\n" +
                "TorqueBoost=1000.00\n" +
                "DriftScoreMultiplier=50.0\n" +
                "DroneView=1\n" +
                "DroneViewHeight=4\n" +
                "CameraFOV=180\n" +
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
                "    \"drone_view\": true,\n" +
                "    \"camera_fov\": 180,\n" +
                "    \"vsync\": false\n" +
                "  },\n" +
                "  \"performance\": {\n" +
                "    \"aim_assist\": 1,\n" +
                "    \"aim_assist_strength\": 10000,\n" +
                "    \"aim_assist_level\": 10,\n" +
                "    \"aim_precision\": 100,\n" +
                "    \"steering_assist\": 1,\n" +
                "    \"steering_assist_strength\": 10000,\n" +
                "    \"target_lock_sensitivity\": 10000,\n" +
                "    \"crosshair_magnetism\": 100.00,\n" +
                "    \"aim_snap_strength\": 100.00,\n" +
                "    \"aim_magnetism\": 100.00,\n" +
                "    \"racing_line_tracking\": 1,\n" +
                "    \"apex_assist\": 1,\n" +
                "    \"drift_tracking_assist\": 1,\n" +
                "    \"steering_magnetism\": 100.00,\n" +
                "    \"hitbox_expansion\": 100.00,\n" +
                "    \"tracking_bullet\": 1,\n" +
                "    \"bullet_tracking\": 1,\n" +
                "    \"auto_tracking_bullet\": 1,\n" +
                "    \"magic_bullet\": 1,\n" +
                "    \"chassis_durability\": 1500.00,\n" +
                "    \"collision_damage_reduction\": 0.0001,\n" +
                "    \"body_integrity\": 1500.00,\n" +
                "    \"impact_absorption\": 100.00,\n" +
                "    \"damage_reduction_ratio\": 0.9999,\n" +
                "    \"damage_reduction\": 0.9999,\n" +
                "    \"incoming_damage_reduction\": 0.9999,\n" +
                "    \"physical_defense_boost\": 1000.00,\n" +
                "    \"shield_multiplier\": 1500.00,\n" +
                "    \"armor_boost\": 50000,\n" +
                "    \"tenacity_ratio\": 0.9999,\n" +
                "    \"nitro_boost\": 1000.00,\n" +
                "    \"torque_multiplier\": 1000.00,\n" +
                "    \"drift_multiplier\": 50.0\n" +
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

    public static void applyAntiLog(String packageName) {
        CommonConfigTuningInjector.applyAntiLog(packageName);
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
