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

    // ─── UltraExtreme 144fps SuperSmooth Patch ───────────────────────────────

    /**
     * Applies 144fps SuperSmooth + UltraExtreme max graphics to Wild Rift.
     *
     * @return true if at least one path was written
     */
    public static boolean patchUltraExtreme144(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            "FpsCapValue=144",
            "TargetFPS=144",
            "MaxFrameRate=144",
            "FrameRateLimit=144",
            "FrameRateLevel=8",
            "UnlockFPS=1",
            "Unlock144FPS=1",
            "Ultra144FPS=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "HighFPSMode=1",
            "GraphicQuality=5",
            "TextureQuality=4",
            "ShadowQuality=2",
            "ShadowResolution=2048",
            "AntiAliasingQuality=4",
            "BloomQuality=5",
            "MaxAnisotropy=16",
            "HDRMode=1",
            "UltraHDMode=1",
            "ResolutionScale=120",
            "UltraExtreme=1",
            "bUseUltraExtreme=True",
            "bFramePacingEnabled=True",
            "Vsync=0",
            "TouchBoostHz=144",
            "TouchPollingRate=1000",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[Graphics]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "WildRift UltraExtreme144 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
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
