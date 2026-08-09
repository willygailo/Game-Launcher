package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuFileBridge;
import java.util.ArrayList;
import java.util.List;

/**
 * RobloxConfigPatcher manages ClientAppSettings.json for Roblox.
 * Package: com.roblox.client
 *
 * Injects DFIntTaskSchedulerTargetFps to unlock up to 165 FPS and force MAX graphics quality level 10.
 */
public class RobloxConfigPatcher {

    private static final String TAG = "RobloxConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;

        List<String> paths = getConfigPaths(packageName);
        int count = 0;

        String jsonContent = String.format(
                "{\n" +
                "  \"DFIntTaskSchedulerTargetFps\": %d,\n" +
                "  \"FIntTargetFps\": %d,\n" +
                "  \"DFIntFpsCap\": %d,\n" +
                "  \"FFIntDebugForceGraphicsQuality\": 10,\n" +
                "  \"FFIntGraphicsQualityLevel\": 10,\n" +
                "  \"FIntGraphicsQualityLevel\": 10,\n" +
                "  \"FFIntRenderShadowQuality\": 5,\n" +
                "  \"FFIntRenderDistance\": 10\n" +
                "}\n",
                targetFps, targetFps, targetFps
        );

        for (String path : paths) {
            if (ShizukuFileBridge.writeContent(path, jsonContent, false)) {
                count++;
            }
        }

        Log.i(TAG, "Roblox patch applied for " + packageName + ": " + count + " paths @ " + targetFps + " FPS (Max 165 FPS Unlock)");
        return count > 0;
    }

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/ClientSettings/ClientAppSettings.json");
        paths.add("/data/data/" + pkg + "/files/ClientSettings/ClientAppSettings.json");
        paths.add("/sdcard/Android/data/" + pkg + "/files/ClientAppSettings.json");
        return paths;
    }
}
