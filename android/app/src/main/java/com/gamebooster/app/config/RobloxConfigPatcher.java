package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import java.util.ArrayList;
import java.util.List;

/**
 * RobloxConfigPatcher — Injects 165 FPS ClientAppSettings.json into Roblox.
 * Target package: `com.roblox.client`
 */
public class RobloxConfigPatcher {

    private static final String TAG = "RobloxConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        List<String> paths = getConfigPaths(packageName);
        
        String jsonContent = "{\n" +
                "  \"DFIntTaskSchedulerTargetFps\": " + targetFps + ",\n" +
                "  \"FFlagDebugGraphicsDisableDirect3D11\": \"False\",\n" +
                "  \"FFlagDebugGraphicsPreferVulkan\": \"True\",\n" +
                "  \"FIntDebugTextureManagerSkipMipLevels\": 0,\n" +
                "  \"DFIntTouchSensitivityLevel\": 3\n" +
                "}\n";

        int written = 0;
        for (String path : paths) {
            forceWrite(path, jsonContent);
            written++;
        }
        Log.i(TAG, "Roblox ClientAppSettings patch written: " + written + " paths @ " + targetFps + " FPS");
        return written > 0;
    }

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/ClientSettings/ClientAppSettings.json");
        paths.add("/sdcard/Android/data/" + pkg + "/files/ClientAppSettings/ClientAppSettings.json");
        paths.add("/data/data/" + pkg + "/files/ClientSettings/ClientAppSettings.json");
        paths.add("/data/data/" + pkg + "/files/ClientAppSettings/ClientAppSettings.json");
        return paths;
    }

    private static void forceWrite(String path, String content) {
        ensureDirectory(path);
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommandWithBase64(content, path);
        } else {
            try {
                String b64 = android.util.Base64.encodeToString(content.getBytes("UTF-8"), android.util.Base64.NO_WRAP);
                CommandExecutor.executeSystemCommand("echo '" + b64 + "' | base64 -d > '" + path + "'");
            } catch (Exception e) {
                Log.e(TAG, "forceWrite failed for " + path, e);
            }
        }
    }

    private static void ensureDirectory(String path) {
        if (path == null) return;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            String parentDir = path.substring(0, lastSlash);
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand("mkdir -p " + parentDir);
            } else {
                CommandExecutor.executeSystemCommand("mkdir -p " + parentDir);
            }
        }
    }
}
