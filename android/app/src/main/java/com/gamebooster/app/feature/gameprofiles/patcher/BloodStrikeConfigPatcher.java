package com.gamebooster.app.feature.gameprofiles.patcher;

import android.util.Log;
import com.gamebooster.app.platform.shell.CommandExecutor;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

/**
 * BloodStrikeConfigPatcher handles configuration patching for Blood Strike
 * (com.dunk.bloodstrike, com.payne.bloodstrike, etc.).
 * Applies high FPS frame rate limits and graphic quality settings.
 */
public class BloodStrikeConfigPatcher {

    private static final String TAG = "BloodStrikePatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        String pkg = packageName.toLowerCase().trim();

        Log.d(TAG, "Patching Blood Strike graphics & FPS config for " + pkg + " -> " + targetFps + " FPS/Hz");

        String[] configPaths = new String[] {
                "/sdcard/Android/data/" + pkg + "/files/UserSetting.ini",
                "/sdcard/Android/data/" + pkg + "/files/GameUserSettings.ini",
                "/sdcard/Android/data/" + pkg + "/files/settings.json",
                "/data/data/" + pkg + "/files/UserSetting.ini"
        };

        boolean patched = false;
        for (String path : configPaths) {
            if (patchPath(path, targetFps)) {
                patched = true;
            }
        }

        // Apply Shizuku game mode & frame rate overrides
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
                    "[BloodStrikeSettings]\\nHighFPS=1\\nFPSMode=2\\nMaxFPS=%d\\nTargetFrameRate=%d\\nGraphicQuality=2\\nTouchResponseMode=0\\n",
                    targetFps, targetFps
            );
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
            ShizukuExecutor.executeShizukuCommand("printf '" + content + "' > " + path);
        } else {
            CommandExecutor.executeSystemCommand("sed -i 's/^HighFPS=.*/HighFPS=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^FPSMode=.*/FPSMode=2/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^MaxFPS=.*/MaxFPS=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^TargetFrameRate=.*/TargetFrameRate=" + targetFps + "/' " + path);

            ShizukuExecutor.executeShizukuCommand("sed -i 's/^HighFPS=.*/HighFPS=1/' " + path);
            ShizukuExecutor.executeShizukuCommand("sed -i 's/^MaxFPS=.*/MaxFPS=" + targetFps + "/' " + path);
            ShizukuExecutor.executeShizukuCommand("sed -i 's/^TargetFrameRate=.*/TargetFrameRate=" + targetFps + "/' " + path);
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
