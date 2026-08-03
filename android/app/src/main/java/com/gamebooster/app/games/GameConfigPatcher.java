package com.gamebooster.app.games;

import android.util.Log;
import com.gamebooster.app.root.CommandExecutor;
import java.util.ArrayList;
import java.util.List;

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
            return new PatchResult(false, "FPS config patching not required for " + packageName + " (handled via Game Mode API)");
        }

        int patchedFiles = 0;
        for (String path : configPaths) {
            if (patchOrWriteConfigFile(path, targetFps)) {
                patchedFiles++;
            }
        }

        if (patchedFiles > 0) {
            Log.d(TAG, "Successfully auto-configured " + patchedFiles + " game config files for " + packageName + " -> " + targetFps + " FPS/Hz");
            return new PatchResult(true, "Auto-configured " + packageName + " game setting files for " + targetFps + " FPS/Hz");
        } else {
            return new PatchResult(false, "Could not update config files for " + packageName);
        }
    }

    private static boolean patchOrWriteConfigFile(String path, int targetFps) {
        if (path == null) return false;

        String parentDir = path.substring(0, path.lastIndexOf('/'));
        // 1. Ensure parent directory exists via Shizuku shell
        CommandExecutor.executeSystemCommand("mkdir -p " + parentDir);

        // 2. Check if file exists
        String checkCmd = "test -f " + path + " && echo EXISTS";
        String checkResult = CommandExecutor.executeSystemCommand(checkCmd);
        boolean exists = checkResult.contains("EXISTS");

        int frameRateLevel = targetFps >= 120 ? 9 : (targetFps >= 90 ? 6 : 3);

        if (!exists) {
            // Write fresh INI configuration file
            String createCmd = String.format("printf '[Graphics]\\nFPS=%d\\nFrameRate=%d\\nHighFPSMode=1\\nFrameRateLevel=%d\\nHighFrameRate=1\\nMaxFrameRate=%d\\nFPS_MODE=2\\nHIGH_FPS=1\\nTargetFPS=%d\\n' > %s",
                    targetFps, targetFps, frameRateLevel, targetFps, targetFps, path);
            String createRes = CommandExecutor.executeSystemCommand(createCmd);
            return CommandExecutor.isSuccessOutput(createRes) || checkResult.contains("EXISTS");
        } else {
            // In-place sed update for FPS, FrameRate, HighFPSMode, FrameRateLevel, MaxFrameRate
            CommandExecutor.executeSystemCommand("sed -i 's/^FPS=.*/FPS=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^fps=.*/fps=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^FrameRate=.*/FrameRate=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^HighFPSMode=.*/HighFPSMode=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^HighFrameRate=.*/HighFrameRate=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^FrameRateLevel=.*/FrameRateLevel=" + frameRateLevel + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^MaxFrameRate=.*/MaxFrameRate=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^FPS_MODE=.*/FPS_MODE=2/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^HIGH_FPS=.*/HIGH_FPS=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^TargetFPS=.*/TargetFPS=" + targetFps + "/' " + path);

            // Append if keys missing
            String grepCmd = "grep -i 'FPS' " + path;
            String grepRes = CommandExecutor.executeSystemCommand(grepCmd);
            if (!grepRes.contains("FPS=") && !grepRes.contains("fps=")) {
                String appendCmd = String.format("printf '\\nFPS=%d\\nFrameRate=%d\\nHighFPSMode=1\\nFrameRateLevel=%d\\nMaxFrameRate=%d\\n' >> %s",
                        targetFps, targetFps, frameRateLevel, targetFps, path);
                CommandExecutor.executeSystemCommand(appendCmd);
            }
            return true;
        }
    }

    private static List<String> getConfigPathsForPackage(String pkg) {
        List<String> paths = new ArrayList<>();
        if (pkg == null) return paths;

        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
            // Mobile Legends (Global, VNG, KR, JP)
            paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/Com/MobileLegendsSettings.ini");
            paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/HighFPSConfig.ini");
        } else if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("vng.pubgmobile")) {
            // PUBG Mobile (Global, BGMI, KR, VNG, Lite, New State)
            paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini");
            paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini");
        } else if (pkg.contains("cod") || pkg.contains("callofduty")) {
            // Call of Duty Mobile (Global, Garena, KR, CN)
            paths.add("/sdcard/Android/data/" + pkg + "/files/GraphicsSettings.ini");
        } else if (pkg.contains("freefire")) {
            // Free Fire & Free Fire MAX
            paths.add("/sdcard/Android/data/" + pkg + "/files/FFGraphicsSettings.ini");
        } else if (pkg.contains("wildrift") || pkg.contains("league")) {
            // Wild Rift
            paths.add("/sdcard/Android/data/" + pkg + "/files/SaveData/Local/Settings");
        } else if (pkg.contains("sgameglobal") || pkg.contains("honorofkings") || pkg.contains("kgtw") || pkg.contains("kgvn")) {
            // Honor of Kings & Arena of Valor
            paths.add("/sdcard/Android/data/" + pkg + "/files/GameSettings.ini");
        } else if (pkg.contains("genshin") || pkg.contains("mihoyo")) {
            // Genshin Impact & HoYoverse
            paths.add("/sdcard/Android/data/" + pkg + "/files/GameSetting.ini");
        } else if (pkg.contains("roblox") || pkg.contains("bloodstrike") || pkg.contains("farlight") || pkg.contains("standoff")) {
            // Roblox & Action Shooters
            paths.add("/sdcard/Android/data/" + pkg + "/files/GameSettings.ini");
        }

        return paths;
    }
}
