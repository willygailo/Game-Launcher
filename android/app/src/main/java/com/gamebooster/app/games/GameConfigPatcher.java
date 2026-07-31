package com.gamebooster.app.games;

import android.util.Log;
import com.gamebooster.app.root.CommandExecutor;

public class GameConfigPatcher {

    private static final String TAG = "GameConfigPatcher";

    public static final String PACKAGE_PUBG_GLOBAL = "com.tencent.ig";
    public static final String PACKAGE_PUBG_INDIA = "com.pubg.imobile";
    public static final String PACKAGE_PUBG_LITE = "com.tencent.iglite";
    public static final String PACKAGE_COD_MOBILE = "com.activision.callofduty.shooter";
    public static final String PACKAGE_MOBILE_LEGENDS = "com.mobile.legends";
    public static final String PACKAGE_FREE_FIRE = "com.dts.freefireth";
    public static final String PACKAGE_FREE_FIRE_MAX = "com.dts.freefiremax";
    public static final String PACKAGE_GENSHIN = "com.miHoYo.GenshinImpact";

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

        String configPath = getConfigPathForPackage(packageName);
        if (configPath == null) {
            return new PatchResult(false, "FPS override not supported for " + packageName + " (FPS option is gated in-app)");
        }

        // 1. File existence pre-check via Shizuku shell
        String checkCmd = "test -f " + configPath + " && echo EXISTS";
        String checkResult = CommandExecutor.executeSystemCommand(checkCmd);
        if (!checkResult.contains("EXISTS")) {
            Log.w(TAG, "Config file does not exist yet: " + configPath);
            return new PatchResult(false, "Config file not found for " + packageName + ". Launch game first to generate config.");
        }

        // 2. Perform sed in-place replacement and verify result
        String sedCmd = "sed -i 's/^FPS=.*/FPS=" + targetFps + "/' " + configPath + " && grep '^FPS=' " + configPath;
        String patchResult = CommandExecutor.executeSystemCommand(sedCmd);

        if (CommandExecutor.isSuccessOutput(patchResult) && patchResult.contains("FPS=" + targetFps)) {
            Log.d(TAG, "Successfully patched " + packageName + " FPS to " + targetFps);
            return new PatchResult(true, "Game FPS config updated to " + targetFps + " FPS");
        } else {
            // If FPS= key was missing, append it safely using printf across toybox/mksh shells
            String appendCmd = String.format("printf '\\nFPS=%d\\n' >> %s && grep '^FPS=' %s", targetFps, configPath, configPath);
            String appendResult = CommandExecutor.executeSystemCommand(appendCmd);
            if (CommandExecutor.isSuccessOutput(appendResult) && appendResult.contains("FPS=" + targetFps)) {
                return new PatchResult(true, "FPS config added to " + packageName);
            }
            return new PatchResult(false, "Failed to patch config file: " + patchResult);
        }
    }

    private static String getConfigPathForPackage(String packageName) {
        switch (packageName) {
            case PACKAGE_PUBG_GLOBAL:
                return "/sdcard/Android/data/com.tencent.ig/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini";
            case PACKAGE_PUBG_INDIA:
                return "/sdcard/Android/data/com.pubg.imobile/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini";
            case PACKAGE_PUBG_LITE:
                return "/sdcard/Android/data/com.tencent.iglite/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini";
            case PACKAGE_COD_MOBILE:
                return "/sdcard/Android/data/com.activision.callofduty.shooter/files/GraphicsSettings.ini";
            case PACKAGE_GENSHIN:
                return "/sdcard/Android/data/com.miHoYo.GenshinImpact/files/GameSetting.ini";
            default:
                return null;
        }
    }
}
