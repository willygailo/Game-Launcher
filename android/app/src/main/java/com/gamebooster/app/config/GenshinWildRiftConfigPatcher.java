package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import java.util.ArrayList;
import java.util.List;

/**
 * GenshinWildRiftConfigPatcher manages internal config JSON files for League of Legends: Wild Rift,
 * Genshin Impact, and Honkai: Star Rail across Android 12 to 16.
 * Supports unlocking Ultra graphics quality, 120/165 FPS, and resolution scaling.
 */
public class GenshinWildRiftConfigPatcher {

    private static final String TAG = "GenshinWildRiftPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, targetFps)) patched++;
        }
        Log.i(TAG, "Genshin/WildRift patch: " + patched + " files for " + packageName + " @ " + targetFps + "fps");
        return patched > 0;
    }

    /**
     * Competitive Force-Write for Wild Rift, Genshin Impact & Honkai: Star Rail.
     * Force-overwrites Ultra Graphics JSON (fps, max_fps, target_frame_rate, graphics_quality=4, render_resolution=2).
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        String content = String.format("{\n" +
                        "  \"fps\": %d,\n" +
                        "  \"max_fps\": %d,\n" +
                        "  \"target_frame_rate\": %d,\n" +
                        "  \"graphics_quality\": 4,\n" +
                        "  \"shadow_quality\": 3,\n" +
                        "  \"render_resolution\": 2,\n" +
                        "  \"post_processing\": 3,\n" +
                        "  \"anti_aliasing\": 1,\n" +
                        "  \"unlock_165hz\": 1\n" +
                        "}\n",
                targetFps, targetFps, targetFps);

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            forceWrite(path, content);
            written++;
        }
        Log.i(TAG, "Genshin/WildRift competitive Ultra graphics force-write: " + written + " paths @ " + targetFps + "fps for " + packageName);
        return written > 0;
    }

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/Config/GameSettings.json");
        paths.add("/data/data/" + pkg + "/files/Config/GameSettings.json");
        return paths;
    }

    private static void forceWrite(String path, String content) {
        ensureDirectory(path);
        String escaped = content.replace("'", "'\\''");
        String writeCmd = "printf '" + escaped + "' > " + path;
        runCommand(writeCmd);
    }

    private static boolean applyPatch(String path, int targetFps) {
        ensureDirectory(path);
        String checkCmd = "test -f " + path + " && echo EXISTS";
        String checkRes = ShizukuExecutor.hasShizukuPermission() 
                ? ShizukuExecutor.executeShizukuCommand(checkCmd)
                : CommandExecutor.executeSystemCommand(checkCmd);

        if (checkRes == null || !checkRes.contains("EXISTS")) {
            String content = String.format("{\\n  \"fps\": %d,\\n  \"max_fps\": %d,\\n  \"target_frame_rate\": %d,\\n  \"graphics_quality\": 4\\n}\\n",
                    targetFps, targetFps, targetFps);
            runCommand("printf '" + content + "' > " + path);
        } else {
            runCommand("sed -i 's/\"fps\":.*/\"fps\": " + targetFps + ",/' " + path);
            runCommand("sed -i 's/\"max_fps\":.*/\"max_fps\": " + targetFps + ",/' " + path);
            runCommand("sed -i 's/\"target_frame_rate\":.*/\"target_frame_rate\": " + targetFps + "/' " + path);
            runCommand("sed -i 's/\"graphics_quality\":.*/\"graphics_quality\": 4/' " + path);
        }
        return true;
    }

    private static void runCommand(String cmd) {
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommand(cmd);
        } else {
            CommandExecutor.executeSystemCommand(cmd);
        }
    }

    private static void ensureDirectory(String path) {
        if (path == null) return;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            String parentDir = path.substring(0, lastSlash);
            runCommand("mkdir -p " + parentDir);
        }
    }
}
