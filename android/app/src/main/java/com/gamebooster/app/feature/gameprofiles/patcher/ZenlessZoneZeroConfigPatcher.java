package com.gamebooster.app.feature.gameprofiles.patcher;

import android.util.Log;
import com.gamebooster.app.platform.shell.CommandExecutor;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

public class ZenlessZoneZeroConfigPatcher {

    private static final String TAG = "ZZZConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        String pkg = packageName.toLowerCase().trim();

        Log.d(TAG, "Patching Zenless Zone Zero config for " + pkg + " -> " + targetFps + " FPS/Hz");

        String[] configPaths = new String[] {
                "/sdcard/Android/data/" + pkg + "/files/LocalSave/GraphicsSettings.json",
                "/sdcard/Android/data/" + pkg + "/files/GraphicsSettings.json",
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
            String content = String.format("{\\n  \"fps\": %d,\\n  \"target_fps\": %d,\\n  \"performance_mode\": 3\\n}\\n",
                    targetFps, targetFps);
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
            ShizukuExecutor.executeShizukuCommand("printf '" + content + "' > " + path);
        } else {
            CommandExecutor.executeSystemCommand("sed -i 's/\"fps\":.*/\"fps\": " + targetFps + ",/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/\"target_fps\":.*/\"target_fps\": " + targetFps + ",/' " + path);

            ShizukuExecutor.executeShizukuCommand("sed -i 's/\"fps\":.*/\"fps\": " + targetFps + ",/' " + path);
            ShizukuExecutor.executeShizukuCommand("sed -i 's/\"target_fps\":.*/\"target_fps\": " + targetFps + ",/' " + path);
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
