package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.booster.TouchLatencyChannel;

/**
 * GameConfigPatcher acts as the main dispatcher for game internal configuration file updates.
 * Delegates cleanly to specialized game patchers (MlbbConfigPatcher, CodmConfigPatcher, PubgConfigPatcher,
 * FreeFireConfigPatcher, GenshinWildRiftConfigPatcher) for clean separation and zero path duplication.
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
        boolean patched = false;

        // Automatically activate 0.0 zero touch latency system properties
        TouchLatencyChannel.enableUltraTouchResponse();

        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
            patched = MlbbConfigPatcher.patchCompetitive(pkg, targetFps);
            MlbbConfigPatcher.applyDamageScriptConfig(pkg);
            MlbbConfigPatcher.applySuperFastTouch(pkg);
            MlbbConfigPatcher.applyAimAssistConfig(pkg);
        } else if (pkg.contains("cod") || pkg.contains("callofduty")) {
            patched = CodmConfigPatcher.patchCompetitive(pkg, targetFps);
            CodmConfigPatcher.applySuperFastTouch(pkg);
            CodmConfigPatcher.applyAimAssistConfig(pkg);
            CodmConfigPatcher.applyRecoilControlConfig(pkg);
        } else if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile") || pkg.contains("relevel")) {
            patched = PubgConfigPatcher.patchCompetitive(pkg, targetFps);
            PubgConfigPatcher.applyAimAssistConfig(pkg);
            PubgConfigPatcher.applyRecoilControlConfig(pkg);
        } else if (pkg.contains("freefire")) {
            patched = FreeFireConfigPatcher.patch(pkg, targetFps);
        } else if (pkg.contains("sgame") || pkg.contains("honorofkings") || pkg.contains("kgtw") || pkg.contains("kgvn") || pkg.contains("kgid")) {
            patched = HonorOfKingsConfigPatcher.patchCompetitive(pkg, targetFps);
        } else if (pkg.contains("roblox")) {
            patched = RobloxConfigPatcher.patch(pkg, targetFps);
        } else if (pkg.contains("wildrift") || pkg.contains("genshin") || pkg.contains("hkrpg")) {
            patched = GenshinWildRiftConfigPatcher.patch(pkg, targetFps);
        } else {
            patched = patchGenericConfig(pkg, targetFps);
        }

        if (patched) {
            Log.d(TAG, "Successfully auto-configured game config files for " + packageName + " -> " + targetFps + " FPS/Hz");
            return new PatchResult(true, "Auto-configured " + packageName + " game setting files for " + targetFps + " FPS/Hz");
        } else {
            return new PatchResult(false, "Could not update config files for " + packageName);
        }
    }

    private static boolean patchGenericConfig(String pkg, int targetFps) {
        String[] paths = new String[]{
            "/sdcard/Android/data/" + pkg + "/files/GameSettings.ini",
            "/data/data/" + pkg + "/files/GameSettings.ini"
        };
        boolean anyPatched = false;
        for (String path : paths) {
            ensureParentDirectory(path);
            String checkCmd = "test -f " + path + " && echo EXISTS";
            String checkRes = ShizukuExecutor.hasShizukuPermission() 
                    ? ShizukuExecutor.executeShizukuCommand(checkCmd) 
                    : CommandExecutor.executeSystemCommand(checkCmd);

            if (checkRes == null || !checkRes.contains("EXISTS")) {
                String content = String.format("[Graphics]\\nFPS=%d\\nFrameRate=%d\\nHighFPSMode=1\\nMaxFrameRate=%d\\n",
                        targetFps, targetFps, targetFps);
                runCommand("printf '" + content + "' > " + path);
            } else {
                runCommand("sed -i 's/^FPS=.*/FPS=" + targetFps + "/' " + path);
                runCommand("sed -i 's/^FrameRate=.*/FrameRate=" + targetFps + "/' " + path);
                runCommand("sed -i 's/^HighFPSMode=.*/HighFPSMode=1/' " + path);
                runCommand("sed -i 's/^MaxFrameRate=.*/MaxFrameRate=" + targetFps + "/' " + path);
            }
            anyPatched = true;
        }
        return anyPatched;
    }

    private static void runCommand(String cmd) {
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommand(cmd);
        } else {
            CommandExecutor.executeSystemCommand(cmd);
        }
    }

    private static void ensureParentDirectory(String path) {
        if (path == null) return;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            String parentDir = path.substring(0, lastSlash);
            runCommand("mkdir -p " + parentDir);
        }
    }

    public static PatchResult forceApplyUserConfig(String pkg, String relativeFilePath, String rawContent) {
        if (pkg == null || relativeFilePath == null || rawContent == null) {
            return new PatchResult(false, "Invalid parameters for CFG force apply");
        }
        String destPath1 = "/sdcard/Android/data/" + pkg + "/files/" + relativeFilePath;
        String destPath2 = "/data/data/" + pkg + "/files/" + relativeFilePath;

        ensureParentDirectory(destPath1);
        ensureParentDirectory(destPath2);

        String res1 = ShizukuExecutor.executeShizukuCommandWithBase64(rawContent, destPath1);
        String res2 = ShizukuExecutor.executeShizukuCommandWithBase64(rawContent, destPath2);

        runCommand("chmod 666 " + destPath1 + " || true");
        runCommand("chmod 666 " + destPath2 + " || true");

        boolean ok = CommandExecutor.isSuccessOutput(res1) || CommandExecutor.isSuccessOutput(res2);
        return new PatchResult(ok, ok ? "Successfully force-applied CFG to " + pkg : "CFG apply failed: " + res1);
    }
}
