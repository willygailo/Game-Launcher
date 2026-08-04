package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import java.util.ArrayList;
import java.util.List;

/**
 * PubgConfigPatcher manages internal config files for PUBG Mobile, BGMI, and regional variants.
 */
public class PubgConfigPatcher {

    private static final String TAG = "PubgConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, targetFps)) patched++;
        }
        Log.i(TAG, "PUBG Config Patcher completed: " + patched + " files updated for " + packageName + " -> " + targetFps + " FPS");
        return patched > 0;
    }

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini");
        paths.add("/data/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini");
        paths.add("/data/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini");
        return paths;
    }

    private static boolean applyPatch(String path, int targetFps) {
        ensureDirectory(path);
        int pubgFpsLevel = targetFps >= 165 ? 9 : (targetFps >= 120 ? 7 : (targetFps >= 90 ? 6 : 5));
        String checkCmd = "test -f " + path + " && echo EXISTS";
        String checkRes = CommandExecutor.executeSystemCommand(checkCmd);

        if (!checkRes.contains("EXISTS")) {
            String content = String.format(
                    "[UserCustom DeviceProfile]\\n+CVars=r.PUBGDeviceFPS=%d\\n+CVars=r.PUBGFrameRateLimit=%d\\n+CVars=r.MobileFPSLimit=%d\\nFrameRateLevel=%d\\n",
                    pubgFpsLevel, targetFps, targetFps, pubgFpsLevel
            );
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
        } else {
            // Append CVars if they don't exist yet — sed cannot add new lines
            String[][] cvars = {
                {"+CVars=r.PUBGDeviceFPS",    "+CVars=r.PUBGDeviceFPS="    + pubgFpsLevel},
                {"+CVars=r.PUBGFrameRateLimit", "+CVars=r.PUBGFrameRateLimit=" + targetFps},
                {"+CVars=r.MobileFPSLimit",   "+CVars=r.MobileFPSLimit="   + targetFps}
            };
            for (String[] cvar : cvars) {
                CommandExecutor.executeSystemCommand(
                    "grep -qF '" + cvar[0] + "' " + path
                    + " || echo '" + cvar[1] + "' >> " + path);
            }
            // Now update values in existing lines via sed
            CommandExecutor.executeSystemCommand("sed -i 's/+CVars=r.PUBGDeviceFPS=.*/+CVars=r.PUBGDeviceFPS=" + pubgFpsLevel + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/+CVars=r.PUBGFrameRateLimit=.*/+CVars=r.PUBGFrameRateLimit=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/+CVars=r.MobileFPSLimit=.*/+CVars=r.MobileFPSLimit=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/FrameRateLevel=.*/FrameRateLevel=" + pubgFpsLevel + "/' " + path);
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
