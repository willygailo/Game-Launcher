package com.gamebooster.app.feature.gameprofiles.patcher;

import android.util.Log;
import com.gamebooster.app.platform.shell.CommandExecutor;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

public class StarRailConfigPatcher {

    private static final String TAG = "StarRailConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        String pkg = packageName.toLowerCase().trim();

        Log.d(TAG, "Patching Honkai: Star Rail config for " + pkg + " -> " + targetFps + " FPS/Hz");

        String[] configPaths = new String[] {
                "/sdcard/Android/data/" + pkg + "/files/GraphicsSettings.json",
                "/sdcard/Android/data/" + pkg + "/files/shared_prefs/" + pkg + ".v2.playerprefs.xml",
                "/data/data/" + pkg + "/files/GraphicsSettings.json"
        };

        boolean patched = false;
        for (String path : configPaths) {
            if (patchPath(path, targetFps)) {
                patched = true;
            }
        }

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
            String content = String.format("{\\n  \"FPS\": %d,\\n  \"TargetFrameRate\": %d,\\n  \"GraphicsLevel\": 3\\n}\\n",
                    targetFps, targetFps);
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
            ShizukuExecutor.executeShizukuCommand("printf '" + content + "' > " + path);
        } else {
            CommandExecutor.executeSystemCommand("sed -i 's/\"FPS\":.*/\"FPS\": " + targetFps + ",/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/\"TargetFrameRate\":.*/\"TargetFrameRate\": " + targetFps + "/' " + path);

            ShizukuExecutor.executeShizukuCommand("sed -i 's/\"FPS\":.*/\"FPS\": " + targetFps + ",/' " + path);
            ShizukuExecutor.executeShizukuCommand("sed -i 's/\"TargetFrameRate\":.*/\"TargetFrameRate\": " + targetFps + "/' " + path);
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
