package com.gamebooster.app.feature.gameprofiles.patcher;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuFileBridge;

public class CodMobileConfigPatcher {

    private static final String TAG = "CodmConfigPatcher";

    private static final String[] CODM_PACKAGES = {
            "com.activision.callofduty.shooter",
            "com.garena.game.codm",
            "com.vng.codmvn",
            "com.activision.callofduty.warzone"
    };

    public static boolean patchCodMobileConfig(int targetHz) {
        boolean anyPatched = false;

        String[] keys = new String[]{
                "GraphicsQuality",
                "MaxFrameRate",
                "AntiAliasing",
                "Bloom",
                "DepthOfField",
                "Ragdoll"
        };

        String[] values = new String[]{
                "3",
                targetHz >= 120 ? "4" : "3",
                "1",
                "1",
                "0",
                "1"
        };

        for (String pkg : CODM_PACKAGES) {
            String basePath = "/sdcard/Android/data/" + pkg + "/files/game_config.ini";
            try {
                boolean ok = ShizukuFileBridge.updateIniKeys(basePath, keys, values, "[DisplaySettings]");
                if (ok) {
                    anyPatched = true;
                    Log.i(TAG, "Successfully patched CODM config.ini for " + pkg);
                }
            } catch (Throwable t) {
                Log.e(TAG, "Error patching CODM config for " + pkg, t);
            }
        }

        return anyPatched;
    }

    public static boolean patch(String pkg, int targetFps) {
        return patchCodMobileConfig(targetFps);
    }
}
