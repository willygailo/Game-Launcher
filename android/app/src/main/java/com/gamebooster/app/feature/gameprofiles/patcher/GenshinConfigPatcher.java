package com.gamebooster.app.feature.gameprofiles.patcher;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuFileBridge;

public class GenshinConfigPatcher {

    private static final String TAG = "GenshinConfigPatcher";

    private static final String[] GENSHIN_PACKAGES = {
            "com.miHoYo.GenshinImpact",
            "com.cognosphere.GenshinImpact"
    };

    public static boolean patchGenshinConfig(int targetHz) {
        boolean anyPatched = false;

        String[] keys = new String[]{
                "TargetFrameRateValue",
                "GraphicsQualityLevel",
                "ShadowQuality",
                "VisualEffects",
                "SFXQuality"
        };

        String fpsVal = targetHz >= 120 ? "120" : "60";

        String[] values = new String[]{
                fpsVal,
                "4",
                "3",
                "3",
                "3"
        };

        for (String pkg : GENSHIN_PACKAGES) {
            String basePath = "/sdcard/Android/data/" + pkg + "/files/config.ini";
            try {
                boolean ok = ShizukuFileBridge.updateIniKeys(basePath, keys, values, "[General]");
                if (ok) {
                    anyPatched = true;
                    Log.i(TAG, "Successfully patched Genshin Impact config.ini for " + pkg);
                }
            } catch (Throwable t) {
                Log.e(TAG, "Error patching Genshin config for " + pkg, t);
            }
        }

        return anyPatched;
    }

    public static boolean patch(String pkg, int targetFps) {
        return patchGenshinConfig(targetFps);
    }
}
