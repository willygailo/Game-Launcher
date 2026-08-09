package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuFileBridge;
import java.util.ArrayList;
import java.util.List;

/**
 * GenshinConfigPatcher manages internal config files for Genshin Impact & Honkai Star Rail.
 * Packages: com.miHoYo.GenshinImpact, com.HoYoverse.hkrpg, com.miHoYo.hkrpg
 *
 * Unlocks 120 FPS & Highest Graphic Presets.
 */
public class GenshinConfigPatcher {

    private static final String TAG = "GenshinConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;

        List<String> paths = getConfigPaths(packageName);
        int count = 0;

        String jsonContent = String.format(
                "{\n" +
                "  \"fps\": %d,\n" +
                "  \"max_fps\": %d,\n" +
                "  \"target_frame_rate\": %d,\n" +
                "  \"graphics_quality\": 5,\n" +
                "  \"render_resolution\": 5,\n" +
                "  \"shadow_quality\": 5,\n" +
                "  \"visual_effects\": 5,\n" +
                "  \"sfx_quality\": 5,\n" +
                "  \"teammate_effects\": 1,\n" +
                "  \"motion_blur\": 0,\n" +
                "  \"bloom\": 1\n" +
                "}\n",
                targetFps, targetFps, targetFps
        );

        for (String path : paths) {
            if (path.endsWith(".json") || path.endsWith("setting_data")) {
                if (ShizukuFileBridge.writeContent(path, jsonContent, false)) count++;
            } else if (path.endsWith(".xml")) {
                String xmlContent = String.format(
                        "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n" +
                        "<map>\n" +
                        "    <int name=\"FPS\" value=\"%d\" />\n" +
                        "    <int name=\"TargetFPS\" value=\"%d\" />\n" +
                        "    <int name=\"GraphicsQuality\" value=\"5\" />\n" +
                        "    <int name=\"RenderScale\" value=\"5\" />\n" +
                        "</map>\n",
                        targetFps, targetFps
                );
                if (ShizukuFileBridge.writeContent(path, xmlContent, false)) count++;
            }
        }

        Log.i(TAG, "Genshin/StarRail patch applied for " + packageName + ": " + count + " paths @ " + targetFps + " FPS");
        return count > 0;
    }

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/setting_data");
        paths.add("/sdcard/Android/data/" + pkg + "/files/Config/GameSettings.json");
        paths.add("/data/data/" + pkg + "/shared_prefs/" + pkg + ".v2.playerprefs.xml");
        paths.add("/data/data/" + pkg + "/files/setting_data");
        return paths;
    }
}
