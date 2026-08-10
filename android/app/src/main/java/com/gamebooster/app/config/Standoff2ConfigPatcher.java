package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * Standoff2ConfigPatcher handles configuration patching for Standoff 2
 * (com.axlebolt.standoff2).
 */
public class Standoff2ConfigPatcher {

    private static final String TAG = "Standoff2Patcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        String pkg = packageName.toLowerCase().trim();

        Log.d(TAG, "Patching Standoff 2 graphics & FPS config for " + pkg + " -> " + targetFps + " FPS/Hz");

        String[] configPaths = new String[] {
                "/sdcard/Android/data/" + pkg + "/files/options.txt",
                "/sdcard/Android/data/" + pkg + "/files/settings.json",
                "/data/data/" + pkg + "/files/settings.json"
        };

        boolean patched = false;
        for (String path : configPaths) {
            if (patchPath(path, targetFps)) {
                patched = true;
            }
        }

        // Shizuku system & game mode overrides
        ShizukuExecutor.executeShizukuCommand("cmd game mode performance " + pkg);
        ShizukuExecutor.executeShizukuCommand("cmd game set --fps " + targetFps + " " + pkg);
        ShizukuExecutor.executeShizukuCommand("cmd window set-app-refresh-rate " + pkg + " " + targetFps);
        ShizukuExecutor.executeShizukuCommand("device_config put game_overlay " + pkg + " mode=2,fps=" + targetFps + ":mode=3,fps=" + targetFps);

        return patched;
    }

    private static boolean patchPath(String path, int targetFps) {
        ensureParentDir(path);
        String checkCmd = "test -f " + path + " && echo EXISTS";
        String checkRes = CommandExecutor.executeSystemCommand(checkCmd);

        if (!checkRes.contains("EXISTS")) {
            String content = String.format(
                    "{\\n  \\\"fps_limit\\\": %d,\\n  \\\"target_fps\\\": %d,\\n  \\\"max_fps\\\": %d,\\n  \\\"graphics_preset\\\": \\\"high\\\"\\n}\\n",
                    targetFps, targetFps, targetFps
            );
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
            ShizukuExecutor.executeShizukuCommand("printf '" + content + "' > " + path);
        } else {
            CommandExecutor.executeSystemCommand("sed -i 's/\"fps_limit\": [0-9]*/\"fps_limit\": " + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/\"target_fps\": [0-9]*/\"target_fps\": " + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/\"max_fps\": [0-9]*/\"max_fps\": " + targetFps + "/' " + path);

            ShizukuExecutor.executeShizukuCommand("sed -i 's/\"fps_limit\": [0-9]*/\"fps_limit\": " + targetFps + "/' " + path);
            ShizukuExecutor.executeShizukuCommand("sed -i 's/\"target_fps\": [0-9]*/\"target_fps\": " + targetFps + "/' " + path);
            ShizukuExecutor.executeShizukuCommand("sed -i 's/\"max_fps\": [0-9]*/\"max_fps\": " + targetFps + "/' " + path);
        }
        return true;
    }

    private static void ensureParentDir(String path) {
        if (path == null) return;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            String parent = path.substring(0, lastSlash);
            CommandExecutor.executeSystemCommand("mkdir -p " + parent);
            ShizukuExecutor.executeShizukuCommand("mkdir -p " + parent);
        }
    }
}
