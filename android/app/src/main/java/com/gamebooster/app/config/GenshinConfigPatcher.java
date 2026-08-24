package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * GenshinConfigPatcher manages internal config and hardware profile JSON files
 * for Genshin Impact, Honkai: Star Rail, and Zenless Zone Zero.
 * Unlocks 120/144/165/185 FPS, unlocks Vulkan backend, and sets max rendering quality.
 */
public class GenshinConfigPatcher {

    private static final String TAG = "GenshinConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Genshin patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── UltraExtreme 144fps SuperSmooth Patch ───────────────────────────────

    /**
     * Applies 144fps SuperSmooth + UltraExtreme max graphics to Genshin Impact / HSR / HoYoverse.
     * Uses Genshin's own keys (targetFrameRate, graphicsQuality, etc.) + generic unlock keys.
     *
     * @return true if at least one path was written
     */
    public static boolean patchUltraExtreme144(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            "targetFrameRate=144",
            "maxFrameRate=144",
            "TargetFPS=144",
            "FrameRateLimit=144",
            "FrameRateLevel=8",
            "UnlockFPS=1",
            "Unlock144FPS=1",
            "Ultra144FPS=1",
            "Unlock120Hz=1", "Unlock144Hz=1", "Unlock165Hz=1", "Unlock185Hz=1",
            "HighFPSMode=1",
            "graphicsQuality=4",
            "textureQuality=4",
            "shadowQuality=2",
            "antiAliasing=4",
            "bloomQuality=5",
            "maxAnisotropy=16",
            "hdrMode=1",
            "resolutionQuality=4",
            "ResolutionScale=120",
            "UltraExtreme=1", "bUseUltraExtreme=True",
            "bFramePacingEnabled=True",
            "vSync=0", "Vsync=0",
            "TouchBoostHz=144", "TouchPollingRate=1000",
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[Graphics]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "Genshin UltraExtreme144 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);

        String jsonContent = "{\n" +
                "  \"fps\": " + forcedFps + ",\n" +
                "  \"max_fps\": " + forcedFps + ",\n" +
                "  \"target_frame_rate\": " + forcedFps + ",\n" +
                "  \"targetFrameRateForOthers\": " + forcedFps + ",\n" +
                "  \"fpsUnlock\": true,\n" +
                "  \"fps_unlock_120\": true,\n" +
                "  \"fps_unlock_144\": true,\n" +
                "  \"fps_unlock_165\": true,\n" +
                "  \"fps_unlock_185\": true,\n" +
                "  \"graphics_quality\": 5,\n" +
                "  \"render_resolution\": 1.2,\n" +
                "  \"shadow_quality\": 4,\n" +
                "  \"visual_effects\": 4,\n" +
                "  \"sfx_quality\": 4,\n" +
                "  \"environment_detail\": 4,\n" +
                "  \"motion_blur\": 0,\n" +
                "  \"bloom\": 1,\n" +
                "  \"crowd_density\": 2,\n" +
                "  \"subsurface_scattering\": 1,\n" +
                "  \"co_op_teammate_effects\": 1,\n" +
                "  \"vulkan_enabled\": true,\n" +
                "  \"unlock_120hz\": true,\n" +
                "  \"unlock_144hz\": true,\n" +
                "  \"unlock_165hz\": true,\n" +
                "  \"unlock_185hz\": true,\n" +
                "  \"camera_distance\": 10.0,\n" +
                "  \"camera_fov\": 180,\n" +
                "  \"drone_view\": true,\n" +
                "  \"drone_view_height\": 4,\n" +
                "  \"field_of_view\": 180,\n" +
                "  \"touch_polling_rate\": 1000,\n" +
                "  \"zero_touch_delay\": true,\n" +
                "  \"touch_response_ms\": 0,\n" +
                "  \"input_latency_reduction\": true,\n" +
                "  \"gyro_sample_rate\": 1000,\n" +
                "  \"aim_assist\": 1,\n" +
                "  \"aim_assist_strength\": 10000,\n" +
                "  \"aim_assist_level\": 10,\n" +
                "  \"aim_precision\": 100,\n" +
                "  \"target_lock_sensitivity\": 10000,\n" +
                "  \"crosshair_magnetism\": 100.00,\n" +
                "  \"aim_snap_strength\": 100.00,\n" +
                "  \"aim_magnetism\": 100.00,\n" +
                "  \"bow_auto_tracking\": 1,\n" +
                "  \"homing_arrows\": 1,\n" +
                "  \"projectile_homing\": 1,\n" +
                "  \"homing_strength\": 100.00,\n" +
                "  \"hitbox_expansion\": 100.00,\n" +
                "  \"bullet_magnetism\": 100.00,\n" +
                "  \"bullet_tracking\": 1,\n" +
                "  \"auto_tracking_bullet\": 1,\n" +
                "  \"magic_bullet\": 1,\n" +
                "  \"defense_multiplier\": 1000.00,\n" +
                "  \"shield_strength\": 1500.00,\n" +
                "  \"shield_capacity\": 1500.00,\n" +
                "  \"shield_multiplier\": 1500.00,\n" +
                "  \"damage_reduction_ratio\": 0.9999,\n" +
                "  \"damage_reduction\": 0.9999,\n" +
                "  \"incoming_damage_reduction\": 0.9999,\n" +
                "  \"elemental_resistance_boost\": 1000.00,\n" +
                "  \"armor_boost\": 50000,\n" +
                "  \"tenacity_ratio\": 0.9999,\n" +
                "  \"damage_multiplier\": 1000.00,\n" +
                "  \"attack_speed_multiplier\": 25.0,\n" +
                "  \"crit_rate_boost\": 1.00,\n" +
                "  \"critical_damage\": 10000,\n" +
                "  \"recoil_compensation\": 1.0,\n" +
                "  \"camera_shake\": 0.0\n" +
                "}\n";

        String hardwareConfig = "{\n" +
                "  \"device_model\": \"SM-S948B\",\n" +
                "  \"gpu_renderer\": \"Adreno (TM) 840\",\n" +
                "  \"vulkan_support\": true,\n" +
                "  \"max_refresh_rate\": " + forcedFps + ",\n" +
                "  \"frame_rate_cap\": " + forcedFps + "\n" +
                "}\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            boolean ok;
            if (path.contains("hardware_model")) {
                ok = ConfigFileHelper.writeContentAtomic(path, hardwareConfig);
            } else {
                ok = ConfigFileHelper.writeContentAtomic(path, jsonContent);
            }
            if (ok) written++;
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "Genshin competitive " + forcedFps + "FPS + 1000% Aim/Tracking/Defense force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
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

private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    private static boolean applyPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        String[] keys = {
            "fps=" + forcedFps,
            "max_fps=" + forcedFps,
            "target_frame_rate=" + forcedFps,
            "targetFrameRateForOthers=" + forcedFps,
            "fpsUnlock=1",
            "fps_unlock_120=1",
            "fps_unlock_144=1",
            "fps_unlock_165=1",
            "fps_unlock_185=1",
            "vulkan_enabled=1",
            "unlock_120hz=1",
            "unlock_144hz=1",
            "unlock_165hz=1",
            "unlock_185hz=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Graphics]");
    }
}
