package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * WarzoneConfigPatcher handles configuration patching for Call of Duty: Warzone Mobile
 * (com.activision.callofduty.warzone, com.activision.warzone, etc.).
 */
public class WarzoneConfigPatcher {

    private static final String TAG = "WarzonePatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        String pkg = packageName.toLowerCase().trim();

        Log.d(TAG, "Patching Warzone Mobile graphics & FPS config for " + pkg + " -> " + targetFps + " FPS/Hz");

        String[] configPaths = new String[] {
                "/sdcard/Android/data/" + pkg + "/files/GameUserSettings.ini",
                "/sdcard/Android/data/" + pkg + "/files/codm_settings.ini",
                "/data/data/" + pkg + "/files/GameUserSettings.ini"
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
                    "[WarzoneUserSettings]\\nFrameRateLimit=%d\\nMaxFPS=%d\\nMobileTargetFPS=%d\\nGraphicQuality=2\\nTouchSamplingFrequency=1000\\n",
                    targetFps, targetFps, targetFps
            );
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
            ShizukuExecutor.executeShizukuCommand("printf '" + content + "' > " + path);
        } else {
            CommandExecutor.executeSystemCommand("sed -i 's/^FrameRateLimit=.*/FrameRateLimit=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^MaxFPS=.*/MaxFPS=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^MobileTargetFPS=.*/MobileTargetFPS=" + targetFps + "/' " + path);

            ShizukuExecutor.executeShizukuCommand("sed -i 's/^FrameRateLimit=.*/FrameRateLimit=" + targetFps + "/' " + path);
            ShizukuExecutor.executeShizukuCommand("sed -i 's/^MaxFPS=.*/MaxFPS=" + targetFps + "/' " + path);
            ShizukuExecutor.executeShizukuCommand("sed -i 's/^MobileTargetFPS=.*/MobileTargetFPS=" + targetFps + "/' " + path);
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
