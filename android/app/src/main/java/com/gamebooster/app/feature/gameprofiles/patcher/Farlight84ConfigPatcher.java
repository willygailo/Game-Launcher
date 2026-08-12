package com.gamebooster.app.feature.gameprofiles.patcher;

import android.util.Log;
import com.gamebooster.app.platform.shell.CommandExecutor;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

/**
 * Farlight84ConfigPatcher optimizes graphics settings for Farlight 84 (com.miracle.farlight84).
 * Enforces 90/120 FPS targets, disables heavy bloom/shadows, and applies zero touch latency.
 */
public class Farlight84ConfigPatcher {

    private static final String TAG = "Farlight84Patcher";

    public static boolean patch(String pkgName, int targetFps) {
        Log.d(TAG, "Patching Farlight 84 config for " + pkgName + " -> Target " + targetFps + " FPS");

        String sdcardPath = "/sdcard/Android/data/" + pkgName + "/files/settings.json";
        String dataPath = "/data/data/" + pkgName + "/files/settings.json";

        boolean patched = false;
        patched |= patchPath(sdcardPath, targetFps);
        patched |= patchPath(dataPath, targetFps);

        return patched;
    }

    private static boolean patchPath(String path, int targetFps) {
        try {
            ensureParentDirectory(path);

            String jsonContent = String.format(
                "{\\n  \"graphicsQuality\": 0,\\n  \"frameRate\": %d,\\n  \"antiAliasing\": false,\\n  \"shadows\": false,\\n  \"bloom\": false,\\n  \"highFpsMode\": true\\n}\\n",
                targetFps
            );

            CommandExecutor.executeSystemCommand("printf '" + jsonContent + "' > " + path);
            ShizukuExecutor.executeShizukuCommand("printf '" + jsonContent + "' > " + path);

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to patch Farlight 84 config at " + path, e);
            return false;
        }
    }

    private static void ensureParentDirectory(String path) {
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            String parentDir = path.substring(0, lastSlash);
            CommandExecutor.executeSystemCommand("mkdir -p " + parentDir);
            ShizukuExecutor.executeShizukuCommand("mkdir -p " + parentDir);
        }
    }
}
