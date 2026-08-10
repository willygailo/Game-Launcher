package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class FreeFireConfigPatcher {

    private static final String TAG = "FreeFireConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        String pkg = packageName.toLowerCase().trim();

        Log.d(TAG, "Patching Free Fire graphics & FPS config for " + pkg + " -> " + targetFps + " FPS/Hz");

        String[] configPaths = new String[] {
                "/sdcard/Android/data/" + pkg + "/files/FFGraphicsSettings.ini",
                "/sdcard/Android/data/" + pkg + "/files/com.dts.freefireth/files/FFGraphicsSettings.ini",
                "/data/data/" + pkg + "/files/FFGraphicsSettings.ini"
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
                    "[FFGraphics]\\nHighFPS=1\\nFPSMode=2\\nMaxFPS=%d\\nGraphicLevel=3\\nSmoothLevel=1\\nTouchPollingRate=240\\n",
                    targetFps
            );
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
            ShizukuExecutor.executeShizukuCommand("printf '" + content + "' > " + path);
        } else {
            CommandExecutor.executeSystemCommand("sed -i 's/^HighFPS=.*/HighFPS=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^FPSMode=.*/FPSMode=2/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^MaxFPS=.*/MaxFPS=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^GraphicLevel=.*/GraphicLevel=3/' " + path);

            ShizukuExecutor.executeShizukuCommand("sed -i 's/^HighFPS=.*/HighFPS=1/' " + path);
            ShizukuExecutor.executeShizukuCommand("sed -i 's/^FPSMode=.*/FPSMode=2/' " + path);
            ShizukuExecutor.executeShizukuCommand("sed -i 's/^MaxFPS=.*/MaxFPS=" + targetFps + "/' " + path);
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
