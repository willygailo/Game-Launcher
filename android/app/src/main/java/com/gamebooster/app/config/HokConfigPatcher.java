package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuFileBridge;
import java.util.ArrayList;
import java.util.List;

/**
 * HokConfigPatcher manages internal config files for Honor of Kings (HOK Global & CN versions).
 * Packages: com.levelinfinite.sgameGlobal, com.tencent.tmgp.sgame
 *
 * Forces 120 FPS (FrameRateLevel=4, FPSLevel=4) & Ultra High Graphics (GraphicsLevel=5, ResolutionRate=4).
 */
public class HokConfigPatcher {

    private static final String TAG = "HokConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;

        List<String> paths = getConfigPaths(packageName);
        int frameRateLevel = targetFps >= 120 ? 4 : (targetFps >= 90 ? 3 : 2);
        int fpsLevel = targetFps >= 120 ? 4 : (targetFps >= 90 ? 3 : 2);

        String[] keys = new String[] {
            "HighFrameRate",
            "FrameRate",
            "FrameRateLevel",
            "FPSLevel",
            "GraphicsLevel",
            "ResolutionRate",
            "HDMode",
            "ShadowQuality",
            "ParticleQuality",
            "TargetFPS",
            "Unlock120Hz"
        };

        String[] values = new String[] {
            "1",
            String.valueOf(targetFps),
            String.valueOf(frameRateLevel),
            String.valueOf(fpsLevel),
            "5", // Ultra HD Graphics
            "4", // Extreme Resolution
            "1",
            "4",
            "4",
            String.valueOf(targetFps),
            "1"
        };

        int count = 0;
        for (String path : paths) {
            if (ShizukuFileBridge.updateIniKeys(path, keys, values, "[SystemConfig]")) {
                count++;
            }
        }
        Log.i(TAG, "HOK patch applied for " + packageName + ": " + count + " paths @ " + targetFps + " FPS (120Hz Max Graphics)");
        return count > 0;
    }

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/Resources/SystemConfig.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/Resources/GameConfig.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/SystemConfig.ini");
        paths.add("/data/data/" + pkg + "/files/Resources/SystemConfig.ini");
        paths.add("/data/data/" + pkg + "/files/SystemConfig.ini");
        return paths;
    }
}
