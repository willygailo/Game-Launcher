package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import java.util.ArrayList;
import java.util.List;

/**
 * GenshinWildRiftConfigPatcher manages internal config JSON files for League of Legends: Wild Rift,
 * Genshin Impact, and Honkai: Star Rail across Android 12 to 16.
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

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/Config/GameSettings.json");
        paths.add("/data/data/" + pkg + "/files/Config/GameSettings.json");
        return paths;
    }

    private static boolean applyPatch(String path, int targetFps) {
        ensureDirectory(path);
        String checkCmd = "test -f " + path + " && echo EXISTS";
        String checkRes = ShizukuExecutor.hasShizukuPermission() 
                ? ShizukuExecutor.executeShizukuCommand(checkCmd)
                : CommandExecutor.executeSystemCommand(checkCmd);

        if (checkRes == null || !checkRes.contains("EXISTS")) {
            String content = String.format("{\\n  \"fps\": %d,\\n  \"max_fps\": %d,\\n  \"target_frame_rate\": %d\\n}\\n",
                    targetFps, targetFps, targetFps);
            runCommand("printf '" + content + "' > " + path);
        } else {
            runCommand("sed -i 's/\"fps\":.*/\"fps\": " + targetFps + ",/' " + path);
            runCommand("sed -i 's/\"max_fps\":.*/\"max_fps\": " + targetFps + ",/' " + path);
            runCommand("sed -i 's/\"target_frame_rate\":.*/\"target_frame_rate\": " + targetFps + "/' " + path);
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
