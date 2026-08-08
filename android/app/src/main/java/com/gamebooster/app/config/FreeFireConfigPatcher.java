package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import java.util.ArrayList;
import java.util.List;

/**
 * FreeFireConfigPatcher manages internal graphics and FPS configuration files for Free Fire and Free Fire MAX.
 * Supports unlocking Ultra graphics quality, 120/165 FPS, and high-frequency touch across Android 12 to 16.
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

    /**
     * Competitive Force-Write for Free Fire & Free Fire MAX.
     * Force-overwrites Ultra Graphics (GraphicLevel=4, HighFPS=1, HighRes=1, Shadow=1, UltraHD=1).
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        String content = String.format(
                "[FFGraphics]\nHighFPS=1\nFPSMode=2\nMaxFPS=%d\nGraphicLevel=4\nShadow=1\nHighRes=1\nUltraHD=1\nTouchBoostHz=165\nUnlock165Hz=1\nTouchResponseSpeed=3\nZeroInputDelay=1\n",
                targetFps
        );
        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            forceWrite(path, content);
            written++;
        }
        Log.i(TAG, "Free Fire competitive Ultra graphics force-write: " + written + " paths @ " + targetFps + "fps for " + packageName);
        return written > 0;
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String cmd = "grep -qF 'TouchResponseSpeed' " + path + " || echo 'TouchResponseSpeed=3' >> " + path + "; " +
                         "sed -i 's/^TouchResponseSpeed=.*/TouchResponseSpeed=3/' " + path + "; " +
                         "grep -qF 'ZeroInputDelay' " + path + " || echo 'ZeroInputDelay=1' >> " + path + "; " +
                         "sed -i 's/^ZeroInputDelay=.*/ZeroInputDelay=1/' " + path;
            runCommand(cmd);
        }
        Log.i(TAG, "Free Fire super fast touch applied for " + packageName);
    }

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/FFGraphicsSettings.ini");
        paths.add("/data/data/" + pkg + "/files/FFGraphicsSettings.ini");
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
            String content = String.format(
                    "[FFGraphics]\\nHighFPS=1\\nFPSMode=2\\nMaxFPS=%d\\nGraphicLevel=4\\nShadow=1\\nHighRes=1\\n",
                    targetFps
            );
            runCommand("printf '" + content + "' > " + path);
        } else {
            runCommand("sed -i 's/^HighFPS=.*/HighFPS=1/' " + path);
            runCommand("sed -i 's/^FPSMode=.*/FPSMode=2/' " + path);
            runCommand("sed -i 's/^MaxFPS=.*/MaxFPS=" + targetFps + "/' " + path);
            runCommand("sed -i 's/^GraphicLevel=.*/GraphicLevel=4/' " + path);
            runCommand("sed -i 's/^Shadow=.*/Shadow=1/' " + path);
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
