package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class WutheringWavesConfigPatcher {

    private static final String TAG = "WuWaConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        String pkg = packageName.toLowerCase().trim();

        Log.d(TAG, "Patching Wuthering Waves config for " + pkg + " -> " + targetFps + " FPS/Hz");

        String[] configPaths = new String[] {
                "/sdcard/Android/data/" + pkg + "/files/UE4Game/Client/Client/Saved/Config/Android/GameUserSettings.ini",
                "/sdcard/Android/data/" + pkg + "/files/UE4Game/Client/Client/Saved/Config/Android/Engine.ini",
                "/data/data/" + pkg + "/files/UE4Game/Client/Client/Saved/Config/Android/GameUserSettings.ini"
        };

        boolean patched = false;
        for (String path : configPaths) {
            if (patchPath(path, targetFps)) {
                patched = true;
            }
        }

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
            String content = String.format("[/Script/Engine.GameUserSettings]\\nFrameRateLimit=%.6f\\nCustomFrameRateLimit=%d\\nGraphicQuality=4\\n",
                    (float) targetFps, targetFps);
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
            ShizukuExecutor.executeShizukuCommand("printf '" + content + "' > " + path);
        } else {
            CommandExecutor.executeSystemCommand("sed -i 's/^FrameRateLimit=.*/FrameRateLimit=" + String.format("%.6f", (float) targetFps) + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^CustomFrameRateLimit=.*/CustomFrameRateLimit=" + targetFps + "/' " + path);

            ShizukuExecutor.executeShizukuCommand("sed -i 's/^FrameRateLimit=.*/FrameRateLimit=" + String.format("%.6f", (float) targetFps) + "/' " + path);
            ShizukuExecutor.executeShizukuCommand("sed -i 's/^CustomFrameRateLimit=.*/CustomFrameRateLimit=" + targetFps + "/' " + path);
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
