package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import java.util.ArrayList;
import java.util.List;

/**
 * FreeFireConfigPatcher manages internal config files for Free Fire and Free Fire MAX.
 * Supports all regional package names on Android 12 to 16.
 */
public class FreeFireConfigPatcher {

    private static final String TAG = "FreeFireConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, targetFps)) patched++;
        }
        Log.i(TAG, "Free Fire patch: " + patched + " files for " + packageName + " @ " + targetFps + "fps");
        return patched > 0;
    }

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/FFGraphicsSettings.ini");
        paths.add("/data/data/" + pkg + "/files/FFGraphicsSettings.ini");
        return paths;
    }

    private static boolean applyPatch(String path, int targetFps) {
        ensureDirectory(path);
        String checkCmd = "test -f " + path + " && echo EXISTS";
        String checkRes = ShizukuExecutor.hasShizukuPermission() 
                ? ShizukuExecutor.executeShizukuCommand(checkCmd)
                : CommandExecutor.executeSystemCommand(checkCmd);

        if (checkRes == null || !checkRes.contains("EXISTS")) {
            String content = String.format(
                    "[FFGraphics]\\nHighFPS=1\\nFPSMode=2\\nMaxFPS=%d\\nGraphicLevel=3\\n",
                    targetFps
            );
            runCommand("printf '" + content + "' > " + path);
        } else {
            runCommand("sed -i 's/^HighFPS=.*/HighFPS=1/' " + path);
            runCommand("sed -i 's/^FPSMode=.*/FPSMode=2/' " + path);
            runCommand("sed -i 's/^MaxFPS=.*/MaxFPS=" + targetFps + "/' " + path);
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
