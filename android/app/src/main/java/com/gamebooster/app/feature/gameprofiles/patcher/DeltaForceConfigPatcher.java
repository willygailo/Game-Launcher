package com.gamebooster.app.feature.gameprofiles.patcher;

import android.util.Log;
import com.gamebooster.app.platform.shell.CommandExecutor;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

/**
 * DeltaForceConfigPatcher handles configuration patching for Delta Force: Hawk Ops
 * (com.levelinfinite.deltaforce, com.tencent.tmgp.deltaforce, etc.).
 */
public class DeltaForceConfigPatcher {

    private static final String TAG = "DeltaForcePatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        String pkg = packageName.toLowerCase().trim();

        Log.d(TAG, "Patching Delta Force graphics & FPS config for " + pkg + " -> " + targetFps + " FPS/Hz");

        String[] configPaths = new String[] {
                "/sdcard/Android/data/" + pkg + "/files/DeltaForceConfig.ini",
                "/sdcard/Android/data/" + pkg + "/files/UE4Game/DeltaForce/DeltaForce/Saved/Config/Android/GameUserSettings.ini",
                "/data/data/" + pkg + "/files/DeltaForceConfig.ini"
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
                    "[/Script/Engine.GameUserSettings]\\nFrameRateLimit=%d.000000\\nFrameRate=%d\\nTargetFPS=%d\\nGraphicQuality=2\\n",
                    targetFps, targetFps, targetFps
            );
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
            ShizukuExecutor.executeShizukuCommand("printf '" + content + "' > " + path);
        } else {
            CommandExecutor.executeSystemCommand("sed -i 's/^FrameRateLimit=.*/FrameRateLimit=" + targetFps + ".000000/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^FrameRate=.*/FrameRate=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^TargetFPS=.*/TargetFPS=" + targetFps + "/' " + path);

            ShizukuExecutor.executeShizukuCommand("sed -i 's/^FrameRateLimit=.*/FrameRateLimit=" + targetFps + ".000000/' " + path);
            ShizukuExecutor.executeShizukuCommand("sed -i 's/^FrameRate=.*/FrameRate=" + targetFps + "/' " + path);
            ShizukuExecutor.executeShizukuCommand("sed -i 's/^TargetFPS=.*/TargetFPS=" + targetFps + "/' " + path);
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
