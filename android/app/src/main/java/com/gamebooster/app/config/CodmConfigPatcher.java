package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import java.util.ArrayList;
import java.util.List;

/**
 * CodmConfigPatcher manages internal config files for Call of Duty Mobile (all versions).
 */
public class CodmConfigPatcher {

    private static final String TAG = "CodmConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, targetFps)) patched++;
        }
        Log.i(TAG, "CODM Config Patcher completed: " + patched + " files updated for " + packageName + " -> " + targetFps + " FPS");
        return patched > 0;
    }

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/Config/UserSetting.json");
        paths.add("/sdcard/Android/data/" + pkg + "/files/com.activision.callofduty.shooter.v2.playerprefs.xml");
        paths.add("/sdcard/Android/data/" + pkg + "/files/GraphicsSettings.ini");
        paths.add("/data/data/" + pkg + "/files/GraphicsSettings.ini");
        paths.add("/data/data/" + pkg + "/files/Config/UserSetting.json");
        return paths;
    }

    private static boolean applyPatch(String path, int targetFps) {
        ensureDirectory(path);
        String checkCmd = "test -f " + path + " && echo EXISTS";
        String checkRes = CommandExecutor.executeSystemCommand(checkCmd);

        if (!checkRes.contains("EXISTS")) {
            String content = String.format(
                    "{\\n  \"MaxFrameRate\": %d,\\n  \"GraphicQuality\": 4,\\n  \"FPSLimit\": %d,\\n  \"SuperResolution\": 1,\\n  \"FieldOfView\": 90\\n}\\n",
                    targetFps, targetFps
            );
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
        } else {
            CommandExecutor.executeSystemCommand("sed -i 's/\"MaxFrameRate\":.*/\"MaxFrameRate\": " + targetFps + ",/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/\"FPSLimit\":.*/\"FPSLimit\": " + targetFps + ",/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/\"GraphicQuality\":.*/\"GraphicQuality\": 4,/' " + path);
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
