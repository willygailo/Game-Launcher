package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import java.util.ArrayList;
import java.util.List;

/**
 * GameConfigPatcher delegates configuration patching to dedicated game-specific patcher classes
 * (MlbbConfigPatcher, PubgConfigPatcher, CodmConfigPatcher, HokConfigPatcher, GenshinConfigPatcher,
 * FreeFireConfigPatcher, WildRiftConfigPatcher, StarRailConfigPatcher, ZenlessZoneZeroConfigPatcher,
 * WutheringWavesConfigPatcher, ArenaOfValorConfigPatcher, NewStateConfigPatcher, RobloxConfigPatcher)
 * and applies ultra-fast zero touch delay tweaks across Android 13, 14, 15, and 16.
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

        // 1. Apply global touch ultra-fast zero delay tweaks for Android 13, 14, 15, 16
        TouchUltraFastNoDelayPatcher.applyTouchNoDelay(pkg);

        int patchedFiles = 0;
        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
            if (MlbbConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("cod") || pkg.contains("callofduty")) {
            if (CodmConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("pubg.newstate")) {
            if (NewStateConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile")) {
            if (PubgConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("sgame") || pkg.contains("levelinfinite")) {
            if (HokConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("freefire")) {
            if (FreeFireConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("wildrift")) {
            if (WildRiftConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("hkrpg") || pkg.contains("starrail")) {
            if (StarRailConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("nap") || pkg.contains("zenless")) {
            if (ZenlessZoneZeroConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("wutheringwaves") || pkg.contains("kurogame")) {
            if (WutheringWavesConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("kgtw") || pkg.contains("kgvn") || pkg.contains("aov")) {
            if (ArenaOfValorConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("genshin")) {
            if (GenshinConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("roblox")) {
            if (RobloxConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else {
            List<String> configPaths = getConfigPathsForPackage(pkg);
            for (String path : configPaths) {
                if (patchGenericConfig(path, targetFps)) patchedFiles++;
            }
        }

        if (patchedFiles > 0) {
            Log.d(TAG, "Successfully auto-configured " + patchedFiles + " game config files for " + packageName + " -> " + targetFps + " FPS/Hz");
            return new PatchResult(true, "Auto-configured " + packageName + " game setting files for " + targetFps + " FPS/Hz with zero touch delay");
        } else {
            return new PatchResult(true, "Applied touch zero-delay & high FPS refresh rate for " + packageName);
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

    private static boolean patchGenericConfig(String path, int targetFps) {
        ensureParentDirectory(path);
        String checkCmd = "test -f " + path + " && echo EXISTS";
        String checkRes = CommandExecutor.executeSystemCommand(checkCmd);

        if (!checkRes.contains("EXISTS")) {
            String content = String.format("[Graphics]\\nFPS=%d\\nFrameRate=%d\\nHighFPSMode=1\\nMaxFrameRate=%d\\nTouchResponse=Fast\\n",
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

        paths.add("/sdcard/Android/data/" + pkg + "/files/GameSettings.ini");
        paths.add("/data/data/" + pkg + "/files/GameSettings.ini");

        return paths;
    }
}
