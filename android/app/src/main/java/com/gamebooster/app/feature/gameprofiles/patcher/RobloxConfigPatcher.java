package com.gamebooster.app.feature.gameprofiles.patcher;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuFileBridge;

public class RobloxConfigPatcher {

    private static final String TAG = "RobloxConfigPatcher";

    private static final String ROBLOX_PACKAGE = "com.roblox.client";

    public static boolean patchRobloxConfig(int targetHz) {
        String path = "/sdcard/Android/data/" + ROBLOX_PACKAGE + "/files/ClientSettings/ClientAppSettings.json";
        try {
            int fps = targetHz > 0 ? targetHz : 120;
            String jsonContent = "{\n" +
                    "  \"FIntTaskSchedulerTargetFps\": " + fps + ",\n" +
                    "  \"FIntDebugForceMSAASamples\": 4,\n" +
                    "  \"FFlagDebugGraphicsDisableDirect3D11\": \"False\",\n" +
                    "  \"FFlagDebugGraphicsPreferVulkan\": \"True\"\n" +
                    "}\n";

            boolean ok = ShizukuFileBridge.writeContent(path, jsonContent, false);
            if (ok) {
                Log.i(TAG, "Successfully patched Roblox ClientAppSettings.json for " + fps + " FPS target");
                return true;
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error patching Roblox config", t);
        }
        return false;
    }

    public static boolean patch(String pkg, int targetFps) {
        return patchRobloxConfig(targetFps);
    }
}
