package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import java.util.ArrayList;
import java.util.List;

/**
 * GameConfigPatcher creates and updates game-specific internal configuration files
 * (INI, JSON, XML, UserCustom) for Mobile Legends, Call of Duty Mobile, PUBG Mobile, BGMI,
 * Free Fire, Wild Rift, and Genshin Impact to force high FPS modes (90 FPS / 120 FPS / 144 FPS / 165 FPS).
 */
public class GameConfigPatcher {

    private static final String TAG = "GameConfigPatcher";

    public static class PatchResult {
        public final boolean success;
        public final String message;

        public PatchResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static PatchResult applyGameFpsPatch(String packageName, int targetFps) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return new PatchResult(false, "Invalid package name");
        }

        String pkg = packageName.toLowerCase().trim();
        List<String> configPaths = getConfigPathsForPackage(pkg);
        if (configPaths == null || configPaths.isEmpty()) {
            return new PatchResult(false, "FPS config patching not required for " + packageName);
        }

        int patchedFiles = 0;
        for (String path : configPaths) {
            if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
                if (patchMlbbConfig(path, targetFps)) patchedFiles++;
            } else if (pkg.contains("cod") || pkg.contains("callofduty")) {
                if (patchCodmConfig(path, targetFps)) patchedFiles++;
            } else if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile") || pkg.contains("gameloft")) {
                if (patchPubgConfig(path, targetFps)) patchedFiles++;
            } else if (pkg.contains("freefire")) {
                if (patchFreeFireConfig(path, targetFps)) patchedFiles++;
            } else if (pkg.contains("wildrift") || pkg.contains("genshin") || pkg.contains("hkrpg")) {
                if (patchWildRiftGenshinConfig(path, targetFps)) patchedFiles++;
            } else {
                if (patchGenericConfig(path, targetFps)) patchedFiles++;
            }
        }

        if (patchedFiles > 0) {
            Log.d(TAG, "Successfully auto-configured " + patchedFiles + " game config files for " + packageName + " -> " + targetFps + " FPS/Hz");
            return new PatchResult(true, "Auto-configured " + packageName + " game setting files for " + targetFps + " FPS/Hz");
        } else {
            return new PatchResult(false, "Could not update config files for " + packageName);
        }
    }

    private static void ensureParentDirectory(String path) {
        if (path == null) return;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            String parentDir = path.substring(0, lastSlash);
            CommandExecutor.executeSystemCommand("mkdir -p " + parentDir);
        }
    }

    private static boolean patchMlbbConfig(String path, int targetFps) {
        ensureParentDirectory(path);
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

    private static boolean patchCodmConfig(String path, int targetFps) {
        ensureParentDirectory(path);
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

    private static boolean patchPubgConfig(String path, int targetFps) {
        ensureParentDirectory(path);
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
            CommandExecutor.executeSystemCommand("sed -i 's/+CVars=r.PUBGDeviceFPS=.*/+CVars=r.PUBGDeviceFPS=" + pubgFpsLevel + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/+CVars=r.PUBGFrameRateLimit=.*/+CVars=r.PUBGFrameRateLimit=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/+CVars=r.MobileFPSLimit=.*/+CVars=r.MobileFPSLimit=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/FrameRateLevel=.*/FrameRateLevel=" + pubgFpsLevel + "/' " + path);
        }
        return true;
    }

    private static boolean patchFreeFireConfig(String path, int targetFps) {
        ensureParentDirectory(path);
        String checkCmd = "test -f " + path + " && echo EXISTS";
        String checkRes = CommandExecutor.executeSystemCommand(checkCmd);

        if (!checkRes.contains("EXISTS")) {
            String content = String.format(
                    "[FFGraphics]\\nHighFPS=1\\nFPSMode=2\\nMaxFPS=%d\\nGraphicLevel=3\\n",
                    targetFps
            );
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
        } else {
            CommandExecutor.executeSystemCommand("sed -i 's/^HighFPS=.*/HighFPS=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^FPSMode=.*/FPSMode=2/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^MaxFPS=.*/MaxFPS=" + targetFps + "/' " + path);
        }
        return true;
    }

    private static boolean patchWildRiftGenshinConfig(String path, int targetFps) {
        ensureParentDirectory(path);
        String checkCmd = "test -f " + path + " && echo EXISTS";
        String checkRes = CommandExecutor.executeSystemCommand(checkCmd);

        if (!checkRes.contains("EXISTS")) {
            String content = String.format("{\\n  \"fps\": %d,\\n  \"max_fps\": %d,\\n  \"target_frame_rate\": %d\\n}\\n",
                    targetFps, targetFps, targetFps);
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
        } else {
            CommandExecutor.executeSystemCommand("sed -i 's/\"fps\":.*/\"fps\": " + targetFps + ",/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/\"max_fps\":.*/\"max_fps\": " + targetFps + ",/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/\"target_frame_rate\":.*/\"target_frame_rate\": " + targetFps + "/' " + path);
        }
        return true;
    }

    private static boolean patchGenericConfig(String path, int targetFps) {
        ensureParentDirectory(path);
        String checkCmd = "test -f " + path + " && echo EXISTS";
        String checkRes = CommandExecutor.executeSystemCommand(checkCmd);

        if (!checkRes.contains("EXISTS")) {
            String content = String.format("[Graphics]\\nFPS=%d\\nFrameRate=%d\\nHighFPSMode=1\\nMaxFrameRate=%d\\n",
                    targetFps, targetFps, targetFps);
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
        } else {
            CommandExecutor.executeSystemCommand("sed -i 's/^FPS=.*/FPS=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^FrameRate=.*/FrameRate=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^HighFPSMode=.*/HighFPSMode=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^MaxFrameRate=.*/MaxFrameRate=" + targetFps + "/' " + path);
        }
        return true;
    }

    private static List<String> getConfigPathsForPackage(String pkg) {
        List<String> paths = new ArrayList<>();
        if (pkg == null) return paths;

        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
            paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/Config/UserSystem.ini");
            paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/HighFPSConfig.ini");
            paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/Com/MobileLegendsSettings.ini");
            paths.add("/data/data/" + pkg + "/files/dragon2017/assets/Com/MobileLegendsSettings.ini");
        } else if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile")) {
            paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini");
            paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini");
            paths.add("/data/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini");
        } else if (pkg.contains("cod") || pkg.contains("callofduty")) {
            paths.add("/sdcard/Android/data/" + pkg + "/files/Config/UserSetting.json");
            paths.add("/sdcard/Android/data/" + pkg + "/files/com.activision.callofduty.shooter.v2.playerprefs.xml");
            paths.add("/sdcard/Android/data/" + pkg + "/files/GraphicsSettings.ini");
            paths.add("/data/data/" + pkg + "/files/GraphicsSettings.ini");
        } else if (pkg.contains("freefire")) {
            paths.add("/sdcard/Android/data/" + pkg + "/files/FFGraphicsSettings.ini");
            paths.add("/data/data/" + pkg + "/files/FFGraphicsSettings.ini");
        } else if (pkg.contains("wildrift") || pkg.contains("genshin") || pkg.contains("hkrpg")) {
            paths.add("/sdcard/Android/data/" + pkg + "/files/Config/GameSettings.json");
            paths.add("/data/data/" + pkg + "/files/Config/GameSettings.json");
        } else {
            paths.add("/sdcard/Android/data/" + pkg + "/files/GameSettings.ini");
            paths.add("/data/data/" + pkg + "/files/GameSettings.ini");
        }

        return paths;
    }
}
