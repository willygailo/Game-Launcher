package com.gamebooster.app.feature.gameprofiles.patcher;

import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuFileBridge;

public class PubgMobileConfigPatcher {

    private static final String TAG = "PubgConfigPatcher";

    private static final String[] PUBG_PACKAGES = {
            "com.tencent.ig",
            "com.pubg.imobile",
            "com.pubg.krmobile",
            "com.vng.pubgmobile",
            "com.tencent.iglite",
            "com.pubg.newstate"
    };

    public static boolean patchPubgMobileConfig(int targetHz) {
        boolean anyPatched = false;

        String[] keys = new String[]{
                "sg.ShadowQuality",
                "sg.TextureQuality",
                "sg.AntiAliasingQuality",
                "sg.EffectsQuality",
                "sg.PostProcessQuality",
                "sg.ResolutionQuality",
                "FrameRateLock",
                "bUseVSync"
        };

        String frameRateLockVal = "FRL_" + (targetHz >= 120 ? targetHz : 120);

        String[] values = new String[]{
                "3",
                "3",
                "3",
                "3",
                "3",
                "100",
                frameRateLockVal,
                "False"
        };

        for (String pkg : PUBG_PACKAGES) {
            String basePath = "/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini";
            try {
                boolean ok = ShizukuFileBridge.updateIniKeys(basePath, keys, values, "[/Script/Engine.GameUserSettings]");
                if (ok) {
                    anyPatched = true;
                    Log.i(TAG, "Successfully patched UE4 GameUserSettings.ini for " + pkg);
                }
            } catch (Throwable t) {
                Log.e(TAG, "Error patching PUBG config for " + pkg, t);
            }
        }

        return anyPatched;
    }

    public static boolean patch(String pkg, int targetFps) {
        return patchPubgMobileConfig(targetFps);
    }
}
