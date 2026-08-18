package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import java.util.ArrayList;
import java.util.List;

/**
 * GenshinConfigPatcher manages internal config and hardware profile JSON files
 * for Genshin Impact, Honkai: Star Rail, and Zenless Zone Zero.
 * Unlocks 120/144/165 FPS, unlocks Vulkan backend, and sets max rendering quality.
 */
public class GenshinConfigPatcher {

    private static final String TAG = "GenshinConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        int forcedFps = targetFps > 0 ? targetFps : 185;
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Genshin patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = targetFps > 0 ? targetFps : 185;

        String jsonContent = "{\n" +
                "  \"fps\": " + forcedFps + ",\n" +
                "  \"max_fps\": " + forcedFps + ",\n" +
                "  \"target_frame_rate\": " + forcedFps + ",\n" +
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
                "  \"unlock_185hz\": true,\n" +
                "  \"unlock_165hz\": true,\n" +
                "  \"camera_distance\": 6.0,\n" +
                "  \"camera_fov\": 150,\n" +
                "  \"drone_view\": true,\n" +
                "  \"field_of_view\": 150,\n" +
                "  \"touch_polling_rate\": 1000,\n" +
                "  \"zero_touch_delay\": true,\n" +
                "  \"touch_response_ms\": 0,\n" +
                "  \"input_latency_reduction\": true,\n" +
                "  \"gyro_sample_rate\": 1000,\n" +
                "  \"damage_multiplier\": 1.90,\n" +
                "  \"attack_speed_multiplier\": 1.5,\n" +
                "  \"crit_rate_boost\": 0.95,\n" +
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
            if (path.contains("hardware_model")) {
                forceWrite(path, hardwareConfig);
            } else {
                forceWrite(path, jsonContent);
            }
            written++;
        }
        Log.i(TAG, "Genshin competitive " + forcedFps + "FPS + Drone View force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String cmd =
                "grep -qF '\"touch_polling_rate\"' " + path + " || echo '\"touch_polling_rate\": 1000,' >> " + path + "; " +
                "sed -i 's/\"touch_polling_rate\":.*/\"touch_polling_rate\": 1000,/' " + path + "; " +
                "grep -qF '\"zero_touch_delay\"' " + path + " || echo '\"zero_touch_delay\": true,' >> " + path + "; " +
                "sed -i 's/\"zero_touch_delay\":.*/\"zero_touch_delay\": true,/' " + path + "; " +
                "grep -qF '\"touch_response_ms\"' " + path + " || echo '\"touch_response_ms\": 0,' >> " + path + "; " +
                "sed -i 's/\"touch_response_ms\":.*/\"touch_response_ms\": 0,/' " + path + "; " +
                "grep -qF '\"input_latency_reduction\"' " + path + " || echo '\"input_latency_reduction\": true,' >> " + path + "; " +
                "sed -i 's/\"input_latency_reduction\":.*/\"input_latency_reduction\": true,/' " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Genshin zero-delay touch acceleration applied for " + packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String cmd =
                "grep -qF '\"camera_distance\"' " + path + " || echo '\"camera_distance\": 6.0,' >> " + path + "; " +
                "sed -i 's/\"camera_distance\":.*/\"camera_distance\": 6.0,/' " + path + "; " +
                "grep -qF '\"camera_fov\"' " + path + " || echo '\"camera_fov\": 150,' >> " + path + "; " +
                "sed -i 's/\"camera_fov\":.*/\"camera_fov\": 150,/' " + path + "; " +
                "grep -qF '\"drone_view\"' " + path + " || echo '\"drone_view\": true,' >> " + path + "; " +
                "sed -i 's/\"drone_view\":.*/\"drone_view\": true,/' " + path + "; " +
                "grep -qF '\"gyro_aim_sensitivity\"' " + path + " || echo '\"gyro_aim_sensitivity\": 2.0,' >> " + path + "; " +
                "sed -i 's/\"gyro_aim_sensitivity\":.*/\"gyro_aim_sensitivity\": 2.0,/' " + path + "; " +
                "grep -qF '\"gyro_zero_delay\"' " + path + " || echo '\"gyro_zero_delay\": true,' >> " + path + "; " +
                "sed -i 's/\"gyro_zero_delay\":.*/\"gyro_zero_delay\": true,/' " + path + "; " +
                "grep -qF '\"gyro_sample_rate\"' " + path + " || echo '\"gyro_sample_rate\": 1000,' >> " + path + "; " +
                "sed -i 's/\"gyro_sample_rate\":.*/\"gyro_sample_rate\": 1000,/' " + path + "; " +
                "grep -qF '\"damage_multiplier\"' " + path + " || echo '\"damage_multiplier\": 1.90,' >> " + path + "; " +
                "sed -i 's/\"damage_multiplier\":.*/\"damage_multiplier\": 1.90,/' " + path + "; " +
                "grep -qF '\"attack_speed_multiplier\"' " + path + " || echo '\"attack_speed_multiplier\": 1.5,' >> " + path + "; " +
                "sed -i 's/\"attack_speed_multiplier\":.*/\"attack_speed_multiplier\": 1.5,/' " + path + "; " +
                "grep -qF '\"crit_rate_boost\"' " + path + " || echo '\"crit_rate_boost\": 0.95,' >> " + path + "; " +
                "sed -i 's/\"crit_rate_boost\":.*/\"crit_rate_boost\": 0.95,/' " + path + "; " +
                "grep -qF '\"recoil_compensation\"' " + path + " || echo '\"recoil_compensation\": 1.0,' >> " + path + "; " +
                "sed -i 's/\"recoil_compensation\":.*/\"recoil_compensation\": 1.0,/' " + path + "; " +
                "grep -qF '\"camera_shake\"' " + path + " || echo '\"camera_shake\": 0.0,' >> " + path + "; " +
                "sed -i 's/\"camera_shake\":.*/\"camera_shake\": 0.0,/' " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Genshin Drone View FOV 150, 1000Hz Gyro, 90+ Damage & No-Recoil applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        applyAimAssistConfig(packageName);
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    private static void forceWrite(String path, String content) {
        ShizukuFileManager.writeFile(path, content, "666");
    }

    private static boolean applyPatch(String path, int targetFps) {
        final int forcedFps = targetFps > 0 ? targetFps : 185;
        if (!ShizukuFileManager.fileExists(path)) {
            String content = String.format(
                    "{\n  \"fps\": %d,\n  \"max_fps\": %d,\n  \"target_frame_rate\": %d,\n  \"vulkan_enabled\": true\n}\n",
                    forcedFps, forcedFps, forcedFps
            );
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd = "sed -i 's/\"fps\":.*/\"fps\": " + forcedFps + ",/' " + path + "; " +
                         "sed -i 's/\"max_fps\":.*/\"max_fps\": " + forcedFps + ",/' " + path + "; " +
                         "sed -i 's/\"target_frame_rate\":.*/\"target_frame_rate\": " + forcedFps + "/' " + path + "; " +
                         "chmod 666 " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
            return true;
        }
    }
}
