package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import java.util.ArrayList;
import java.util.List;

/**
 * MlbbConfigPatcher manages internal config files for Mobile Legends: Bang Bang (all versions).
 */
public class MlbbConfigPatcher {

    private static final String TAG = "MlbbConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, targetFps)) patched++;
        }
        Log.i(TAG, "MLBB Config Patcher completed: " + patched + " files updated for " + packageName + " -> " + targetFps + " FPS");
        return patched > 0;
    }

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/Config/UserSystem.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/HighFPSConfig.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/Com/MobileLegendsSettings.ini");
        paths.add("/data/data/" + pkg + "/files/dragon2017/assets/Com/MobileLegendsSettings.ini");
        paths.add("/data/data/" + pkg + "/files/dragon2017/assets/UI/Config/UserSystem.ini");
        return paths;
    }

    private static boolean applyPatch(String path, int targetFps) {
        ensureDirectory(path);
        int frameRateLevel = targetFps >= 165 ? 9 : (targetFps >= 120 ? 9 : (targetFps >= 90 ? 6 : 3));
        String checkCmd = "test -f " + path + " && echo EXISTS";
        String checkRes = CommandExecutor.executeSystemCommand(checkCmd);

        if (!checkRes.contains("EXISTS")) {
            String content = String.format(
                    "[Graphics]\\nHighFPSMode=1\\nFrameRateLevel=%d\\nGraphicsQuality=4\\nHDMode=1\\nShadow=1\\nFPS=%d\\nMaxFrameRate=%d\\nTargetFPS=%d\\nHighFrameRate=1\\n",
                    frameRateLevel, targetFps, targetFps, targetFps
            );
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
        } else {
            CommandExecutor.executeSystemCommand("sed -i 's/^HighFPSMode=.*/HighFPSMode=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^FrameRateLevel=.*/FrameRateLevel=" + frameRateLevel + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^GraphicsQuality=.*/GraphicsQuality=4/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^HDMode=.*/HDMode=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^Shadow=.*/Shadow=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^FPS=.*/FPS=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^MaxFrameRate=.*/MaxFrameRate=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^HighFrameRate=.*/HighFrameRate=1/' " + path);
        }
        return true;
    }

    private static void ensureDirectory(String path) {
        if (path == null) return;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            String parentDir = path.substring(0, lastSlash);
            CommandExecutor.executeSystemCommand("mkdir -p " + parentDir);
        }
    }
}
