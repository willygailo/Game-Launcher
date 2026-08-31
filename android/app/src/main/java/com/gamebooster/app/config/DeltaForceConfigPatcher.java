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

    // ─── UltraExtreme 144fps SuperSmooth ────────────────────────────────────

    public static boolean patchUltraExtreme144(String packageName) {
        if (packageName == null) return false;

        String[] cvarKeys = {
            // FPS unlock
            "+CVars=r.MaxFPS=144",
            "+CVars=r.FrameRateLimit=144",
            "+CVars=r.MobileFPSLimit=144",
            "+CVars=r.Unlock120Hz=1",
            "+CVars=r.Unlock144Hz=1",
            "+CVars=r.Unlock165Hz=1",
            "+CVars=r.Unlock185Hz=1",
            // SuperSmooth
            "+CVars=r.VSync=0",
            "+CVars=r.OneFrameThreadLag=0",
            "+CVars=r.FinishCurrentFrame=0",
            "+CVars=r.DFR.Enabled=0",
            "+CVars=r.FramePacing=1",
            // UltraExtreme graphics
            "+CVars=r.MobileContentScaleFactor=1.0",
            "+CVars=r.ShadowQuality=5",
            "+CVars=r.Shadow.MaxResolution=2048",
            "+CVars=r.Tonemapper.Quality=4",
            "+CVars=r.DefaultFeature.AntiAliasing=4",
            "+CVars=r.TemporalAA.Quality=4",
            "+CVars=r.MaxAnisotropy=16",
            "+CVars=r.BloomQuality=5",
            "+CVars=r.ReflectionEnvironment=1",
            "+CVars=r.SSR.Quality=4",
            "+CVars=r.PostProcessAAQuality=6",
            "+CVars=r.TranslucencyLightingVolumeDim=64",
            "+CVars=r.LightFunctionQuality=2",
            "+CVars=r.DetailMode=2",
            "+CVars=r.Streaming.PoolSize=0",
            "+CVars=r.RenderTargetPoolMin=2048",
            "+CVars=r.HDR.EnableHDROutput=1",
            "+CVars=r.HDR.Display.OutputDevice=4",
            // Touch 1000Hz
            "+CVars=r.TouchBoostHz=144",
            "+CVars=r.TouchPollingRate=1000",
            "+CVars=r.GyroSampleRate=1000"
        };

        String[] rawKeys = {
            "r.MaxFPS=144",
            "r.FrameRateLimit=144",
            "r.MobileFPSLimit=144",
            "r.VSync=0",
            "r.OneFrameThreadLag=0",
            "r.FinishCurrentFrame=0",
            "r.FramePacing=1",
            "r.MobileContentScaleFactor=1.0",
            "r.ShadowQuality=5",
            "r.Shadow.MaxResolution=2048",
            "r.Tonemapper.Quality=4",
            "r.DefaultFeature.AntiAliasing=4",
            "r.TemporalAA.Quality=4",
            "r.MaxAnisotropy=16",
            "r.BloomQuality=5",
            "r.PostProcessAAQuality=6",
            "r.DetailMode=2",
            "r.HDR.EnableHDROutput=1",
            "r.HDR.Display.OutputDevice=4",
            "bSmoothFrameRate=False",
            "MaxSmoothedFrameRate=144"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            boolean isUserCustom = path.contains("UserCustom");
            String[] keys = isUserCustom ? cvarKeys : rawKeys;
            String section = isUserCustom ? "[UserCustom DeviceProfile]" : "[/Script/Engine.RendererSettings]";
            if (ConfigFileHelper.patchKeys(path, keys, section)) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "DeltaForce UltraExtreme144 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    public static boolean patchUltraExtreme185(String packageName) {
        if (packageName == null) return false;

        String[] cvarKeys = {
            // FPS unlock
            "+CVars=r.MaxFPS=185",
            "+CVars=r.FrameRateLimit=185",
            "+CVars=r.MobileFPSLimit=185",
            "+CVars=r.Unlock120Hz=1",
            "+CVars=r.Unlock144Hz=1",
            "+CVars=r.Unlock165Hz=1",
            "+CVars=r.Unlock185Hz=1",
            // SuperSmooth
            "+CVars=r.VSync=0",
            "+CVars=r.OneFrameThreadLag=0",
            "+CVars=r.FinishCurrentFrame=0",
            "+CVars=r.DFR.Enabled=0",
            "+CVars=r.FramePacing=1",
            // UltraExtreme graphics
            "+CVars=r.MobileContentScaleFactor=1.0",
            "+CVars=r.ShadowQuality=5",
            "+CVars=r.Shadow.MaxResolution=2048",
            "+CVars=r.Tonemapper.Quality=4",
            "+CVars=r.DefaultFeature.AntiAliasing=4",
            "+CVars=r.TemporalAA.Quality=4",
            "+CVars=r.MaxAnisotropy=16",
            "+CVars=r.BloomQuality=5",
            "+CVars=r.ReflectionEnvironment=1",
            "+CVars=r.SSR.Quality=4",
            "+CVars=r.PostProcessAAQuality=6",
            "+CVars=r.TranslucencyLightingVolumeDim=64",
            "+CVars=r.LightFunctionQuality=2",
            "+CVars=r.DetailMode=2",
            "+CVars=r.Streaming.PoolSize=0",
            "+CVars=r.RenderTargetPoolMin=2048",
            "+CVars=r.HDR.EnableHDROutput=1",
            "+CVars=r.HDR.Display.OutputDevice=4",
            // Touch 1000Hz
            "+CVars=r.TouchBoostHz=185",
            "+CVars=r.TouchPollingRate=1000",
            "+CVars=r.GyroSampleRate=1000"
        };

        String[] rawKeys = {
            "r.MaxFPS=185",
            "r.FrameRateLimit=185",
            "r.MobileFPSLimit=185",
            "r.VSync=0",
            "r.OneFrameThreadLag=0",
            "r.FinishCurrentFrame=0",
            "r.FramePacing=1",
            "r.MobileContentScaleFactor=1.0",
            "r.ShadowQuality=5",
            "r.Shadow.MaxResolution=2048",
            "r.Tonemapper.Quality=4",
            "r.DefaultFeature.AntiAliasing=4",
            "r.TemporalAA.Quality=4",
            "r.MaxAnisotropy=16",
            "r.BloomQuality=5",
            "r.PostProcessAAQuality=6",
            "r.DetailMode=2",
            "r.HDR.EnableHDROutput=1",
            "r.HDR.Display.OutputDevice=4",
            "bSmoothFrameRate=False",
            "MaxSmoothedFrameRate=185"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            boolean isUserCustom = path.contains("UserCustom");
            String[] keys = isUserCustom ? cvarKeys : rawKeys;
            String section = isUserCustom ? "[UserCustom DeviceProfile]" : "[/Script/Engine.RendererSettings]";
            if (ConfigFileHelper.patchKeys(path, keys, section)) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "DeltaForce UltraExtreme185 SuperSmooth patch: " + written + " paths for " + packageName);
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

    public static void applySuperFastTouch(String packageName) {
        CommonConfigTuningInjector.applySuperFastTouch(packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        CommonConfigTuningInjector.applyAimAssistConfig(packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        CommonConfigTuningInjector.applyRecoilControlConfig(packageName);
    }

    public static void applyDamageScriptConfig(String packageName) {
        CommonConfigTuningInjector.applyDamageScriptConfig(packageName);
    }

    public static void applyTrackingBulletConfig(String packageName) {
        CommonConfigTuningInjector.applyTrackingBulletConfig(packageName);
    }

    public static void applyArmorDefConfig(String packageName) {
        CommonConfigTuningInjector.applyArmorDefConfig(packageName);
    }

    public static void applySpeedBoostConfig(String packageName) {
        CommonConfigTuningInjector.applySpeedBoostConfig(packageName);
    }
}
