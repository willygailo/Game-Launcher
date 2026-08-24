package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * WutheringWavesConfigPatcher — Graphic & FPS Optimization Patcher for Wuthering Waves (Kuro Games).
 */
public class WutheringWavesConfigPatcher {

    private static final String TAG = "WutheringWavesConfigPatcher";

    public static List<String> getConfigPaths(String packageName) {
        List<String> paths = new ArrayList<>();
        if (packageName == null) return paths;

        String pkg = packageName.trim();
        paths.add("/sdcard/Android/data/" + pkg + "/files/Saved/Config/Android/Engine.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/Saved/Config/Android/GameUserSettings.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/Saved/Config/Android/Scalability.ini");
        paths.add("/storage/emulated/0/Android/data/" + pkg + "/files/Saved/Config/Android/Engine.ini");
        paths.add("/storage/emulated/0/Android/data/" + pkg + "/files/Saved/Config/Android/GameUserSettings.ini");
        return paths;
    }

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;

        for (String path : paths) {
            if (applyPatch(path, forcedFps)) {
                patched++;
            }
        }
        Log.i(TAG, "Wuthering Waves patched " + patched + " configs @ " + forcedFps + " FPS");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);

        String engineIniContent = "[/Script/Engine.Engine]\n" +
                "bSmoothFrameRate=False\n" +
                "MinSmoothedFrameRate=60\n" +
                "MaxSmoothedFrameRate=" + forcedFps + "\n\n" +
                "[/Script/Engine.RendererSettings]\n" +
                "r.VSync=0\n" +
                "r.FinishCurrentFrame=0\n" +
                "r.OneFrameThreadLag=0\n" +
                "r.MaxFPS=" + forcedFps + "\n" +
                "r.FrameRateLimit=" + forcedFps + "\n" +
                "r.MobileContentScaleFactor=1.0\n" +
                "r.Streaming.PoolSize=0\n" +
                "r.RenderTargetPoolMin=1024\n\n" +
                "[SystemSettings]\n" +
                "r.MaxFPS=" + forcedFps + "\n" +
                "r.FrameRateLimit=" + forcedFps + "\n" +
                "r.MobileFPSLimit=" + forcedFps + "\n" +
                "r.Unlock120Hz=1\n" +
                "r.Unlock144Hz=1\n" +
                "r.Unlock165Hz=1\n" +
                "r.Unlock185Hz=1\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.writeContentAtomic(path, engineIniContent)) {
                written++;
            }
        }
        return written > 0;
    }

    private static boolean applyPatch(String path, int forcedFps) {
        String[] keys = new String[]{
                "r.MaxFPS=" + forcedFps,
                "r.FrameRateLimit=" + forcedFps,
                "r.MobileFPSLimit=" + forcedFps,
                "r.VSync=0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[SystemSettings]");
    }
}
