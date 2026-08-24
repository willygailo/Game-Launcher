package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * DeltaForceConfigPatcher — Unreal Engine 5 Graphic & FPS Optimization Patcher for Delta Force Mobile.
 */
public class DeltaForceConfigPatcher {

    private static final String TAG = "DeltaForceConfigPatcher";

    public static List<String> getConfigPaths(String packageName) {
        List<String> paths = new ArrayList<>();
        if (packageName == null) return paths;

        String pkg = packageName.trim();
        paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/DeltaForce/DeltaForce/Saved/Config/Android/UserCustom.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/DeltaForce/DeltaForce/Saved/Config/Android/GameUserSettings.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/DeltaForce/DeltaForce/Saved/Config/Android/Engine.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/DeltaForce/DeltaForce/Saved/Config/Android/Scalability.ini");
        paths.add("/storage/emulated/0/Android/data/" + pkg + "/files/UE4Game/DeltaForce/DeltaForce/Saved/Config/Android/UserCustom.ini");
        paths.add("/storage/emulated/0/Android/data/" + pkg + "/files/UE4Game/DeltaForce/DeltaForce/Saved/Config/Android/Engine.ini");
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
        Log.i(TAG, "Delta Force UE5 patched " + patched + " configs @ " + forcedFps + " FPS");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);

        String userCustomContent = "[/Script/Engine.Engine]\n" +
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
                "r.Tonemapper.Quality=4\n" +
                "r.ShadowQuality=1\n" +
                "r.Streaming.PoolSize=0\n" +
                "r.RenderTargetPoolMin=1024\n" +
                "r.DefaultFeature.AntiAliasing=2\n" +
                "r.TemporalAA.Quality=2\n\n" +
                "[UserCustom DeviceProfile]\n" +
                "+CVars=r.MaxFPS=" + forcedFps + "\n" +
                "+CVars=r.FrameRateLimit=" + forcedFps + "\n" +
                "+CVars=r.MobileFPSLimit=" + forcedFps + "\n" +
                "+CVars=r.Unlock120Hz=1\n" +
                "+CVars=r.Unlock144Hz=1\n" +
                "+CVars=r.Unlock165Hz=1\n" +
                "+CVars=r.Unlock185Hz=1\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.writeContentAtomic(path, userCustomContent)) {
                written++;
            }
        }
        return written > 0;
    }

    private static boolean applyPatch(String path, int forcedFps) {
        String[] keys = new String[]{
                "+CVars=r.MaxFPS=" + forcedFps,
                "+CVars=r.FrameRateLimit=" + forcedFps,
                "+CVars=r.MobileFPSLimit=" + forcedFps,
                "r.VSync=0",
                "r.MaxFPS=" + forcedFps,
                "r.FrameRateLimit=" + forcedFps
        };
        return ConfigFileHelper.patchKeys(path, keys, "[UserCustom DeviceProfile]");
    }
}
