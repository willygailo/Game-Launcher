package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * PubgConfigPatcher manages internal config files for PUBG Mobile, BGMI, and regional variants.
 *
 * Two patching modes:
 *  - patch()            → standard patch: in-memory key/CVar upserting
 *  - patchCompetitive() → competitive force-write: overwrites all paths atomically via ConfigFileHelper
 */
public class PubgConfigPatcher {

    private static final String TAG = "PubgConfigPatcher";

    // ─── Standard Patch ───────────────────────────────────────────────────────

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        patchActiveSavBinary(packageName, forcedFps);
        Log.i(TAG, "PUBGM patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL PUBGM/BGMI config paths unconditionally.
     * Includes full UE4 CVar injection for 120 / 144 / 165 / 185 FPS, frame rate limits, and content scale.
     *
     * @return true if at least one path was written
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final FpsUnlockTier tier = FpsUnlockTier.fromFps(forcedFps);
        final int pubgFpsLevel = tier.level;

        String content = "[UserCustom DeviceProfile]\n" +
                "+CVars=r.PUBGDeviceFPS=" + pubgFpsLevel + "\n" +
                "+CVars=r.PUBGMaxFPS=" + forcedFps + "\n" +
                "+CVars=r.PUBGFrameRateLimit=" + forcedFps + "\n" +
                "+CVars=r.MobileFPSLimit=" + forcedFps + "\n" +
                "+CVars=r.FrameRateLimit=" + forcedFps + "\n" +
                "+CVars=r.PUBGHDRMode=1\n" +
                "+CVars=r.MobileHDR=1\n" +
                "+CVars=r.PUBGQualityLevel=4\n" +
                "+CVars=r.PUBGSDKQualityLevel=4\n" +
                "+CVars=r.Tonemapper.Quality=4\n" +
                "+CVars=r.HDR.Display.OutputDevice=1\n" +
                "+CVars=r.MobileContentScaleFactor=1.0\n" +
                "+CVars=r.MobileTonemapperFilm=1\n" +
                "+CVars=r.PUBGTPPViewRange=100.00\n" +
                "+CVars=r.PUBGFPPViewRange=150.00\n" +
                "+CVars=r.SprintSensitivity=150\n" +
                "+CVars=r.Vsync=0\n" +
                "+CVars=r.Unlock120Hz=1\n" +
                "+CVars=r.Unlock144Hz=1\n" +
                "+CVars=r.Unlock165Hz=1\n" +
                "+CVars=r.Unlock185Hz=1\n" +
                "+CVars=r.SuppressLogs=1\n" +
                "+CVars=r.DisableDebugLog=1\n" +
                "+CVars=r.EnableCrashReporting=0\n" +
                "+CVars=r.Telemetry=0\n" +
                "+CVars=a.DisableAnalytics=1\n" +
                "+CVars=r.LogFilter=0\n" +
                "+CVars=r.TouchBoostHz=" + forcedFps + "\n" +
                "+CVars=r.MobileTouchBoostRate=" + forcedFps + "\n" +
                "+CVars=r.GyroSampleRate=1000\n" +
                "+CVars=r.GyroSensitivityRatio=2.5\n" +
                "+CVars=r.GyroZeroDelay=1\n" +
                "+CVars=r.GyroLatencyMode=0\n" +
                "+CVars=r.GyroSmoothFactor=1\n" +
                "+CVars=r.GyroStabilization=1\n" +
                "FrameRateLevel=" + pubgFpsLevel + "\n" +
                "FPS=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "UnlockFPS=1\n" +
                "Unlock120FPS=1\n" +
                "Unlock144FPS=1\n" +
                "Unlock165FPS=1\n" +
                "Unlock185FPS=1\n" +
                "Ultra144FPS=1\n" +
                "Ultra165FPS=1\n" +
                "Ultra185FPS=1\n" +
                "UltraExtreme=1\n" +
                "bUseUltraExtreme=True\n" +
                "GraphicsQuality=5\n" +
                "GraphicQuality=4\n" +
                "HDRMode=1\n" +
                "UltraHDMode=1\n" +
                "SuperResolution=1\n" +
                "bUseHDRMode=True\n" +
                "bUseHighQualityBloom=True\n" +
                "bUseAntiAliasing=True\n" +
                "bDisableAnalytics=True\n" +
                "bDisableBugReporting=True\n" +
                "SprintSensitivity=150\n" +
                "TPPFieldOfView=100\n" +
                "FPPFieldOfView=150\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.writeContentAtomic(path, content)) {
                written++;
            }
        }
        patchActiveSavBinary(packageName, forcedFps);
        deployPakPatch(packageName);
        purgeGameCacheAndLogs(packageName);
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "PUBGM competitive HDR " + forcedFps + "FPS force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Deploys game_patch_*.pak file to PUBGM Saved/Paks directory for 120/144/165/185 FPS unlock.
     */
    public static boolean deployPakPatch(String pkg) {
        if (pkg == null) return false;
        String pakFileName = "game_patch_4.5.0.21377.pak";
        String[] candidateSources = {
            "/storage/emulated/0/" + pakFileName,
            "/sdcard/" + pakFileName,
            "/storage/emulated/0/Download/" + pakFileName,
            "/sdcard/Download/" + pakFileName,
            "/storage/emulated/0/GameLauncher/" + pakFileName,
            "/sdcard/GameLauncher/" + pakFileName
        };

        String foundSource = null;
        for (String src : candidateSources) {
            if (ShizukuFileManager.fileExists(src)) {
                foundSource = src;
                break;
            }
        }

        if (foundSource == null) {
            return false;
        }

        String[] targetDirs = {
            "/storage/emulated/0/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Paks",
            "/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Paks"
        };

        boolean deployed = false;
        for (String dir : targetDirs) {
            ShizukuFileManager.makeDirectory(dir);
            String dest = dir + "/" + pakFileName;
            String copyCmd = "cp -f \"" + foundSource + "\" \"" + dest + "\" && chmod 666 \"" + dest + "\"";
            CommandExecutor.executeSystemCommand(copyCmd);
            if (ShizukuFileManager.fileExists(dest)) {
                deployed = true;
                Log.i(TAG, "Successfully deployed " + pakFileName + " to " + dest);
            }
        }
        return deployed;
    }

    /**
     * Purges temporary conflict caches and logs so new config / pak applies cleanly.
     */
    public static void purgeGameCacheAndLogs(String pkg) {
        if (pkg == null) return;
        String[] cleanCmds = {
            "rm -rf /storage/emulated/0/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Logs/* 2>/dev/null",
            "rm -rf /storage/emulated/0/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Pandora/* 2>/dev/null",
            "rm -rf /storage/emulated/0/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/light_data/* 2>/dev/null",
            "rm -rf /storage/emulated/0/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/UpdateInfo/* 2>/dev/null",
            "rm -rf /storage/emulated/0/Android/data/" + pkg + "/files/TGPA/* 2>/dev/null",
            "rm -rf /sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Logs/* 2>/dev/null",
            "rm -rf /sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Pandora/* 2>/dev/null",
            "rm -rf /sdcard/Android/data/" + pkg + "/files/TGPA/* 2>/dev/null"
        };
        for (String cmd : cleanCmds) {
            CommandExecutor.executeSystemCommand(cmd);
        }
    }

    // ─── Delegated Common Tuning Injectors ───────────────────────────────────

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

    public static void applyFastCooldownConfig(String packageName) {
        CommonConfigTuningInjector.applyFastCooldownConfig(packageName);
    }

    public static void applyShield1500Config(String packageName) {
        CommonConfigTuningInjector.applyShield1500Config(packageName);
    }

    public static void applyDroneViewUltraConfig(String packageName) {
        CommonConfigTuningInjector.applyDroneViewUltraConfig(packageName);
    }

    public static void applyDroneViewConfig(String packageName) {
        CommonConfigTuningInjector.applyDroneViewConfig(packageName);
    }

    public static void applyArmorDefConfig(String packageName) {
        CommonConfigTuningInjector.applyArmorDefConfig(packageName);
    }

    public static void applySpeedBoostConfig(String packageName) {
        CommonConfigTuningInjector.applySpeedBoostConfig(packageName);
    }

    public static void applyTrackingBulletConfig(String packageName) {
        CommonConfigTuningInjector.applyTrackingBulletConfig(packageName);
    }

    public static void applyAntiLog(String packageName) {
        CommonConfigTuningInjector.applyAntiLog(packageName);
    }

// ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    /**
     * Patches Active.sav binary savegame file directly using byte manipulation.
     * Enforces FPSLevel, BattleFPS, and LobbyFPS to target levels (10=185fps, 9=165fps, 8=144fps, 7=120fps).
     */
    public static void patchActiveSavBinary(String pkg, int targetFps) {
        if (pkg == null) return;
        final int fpsLevel = FpsUnlockTier.fromFps(targetFps).level;
        String[] savPaths = {
            "/storage/emulated/0/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav",
            "/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav",
            "/data/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav",
            "/data/user/0/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav"
        };
        for (String sav : savPaths) {
            try {
                if (!ShizukuFileManager.fileExists(sav)) continue;
                byte[] data = ShizukuFileManager.readFileBytes(sav);
                if (data != null && data.length > 0) {
                    boolean modified = patchBinarySavField(data, "FPSLevel", fpsLevel);
                    modified |= patchBinarySavField(data, "BattleFPS", fpsLevel);
                    modified |= patchBinarySavField(data, "LobbyFPS", fpsLevel);
                    if (modified) {
                        ShizukuFileManager.uploadBytes(sav, data, "666");
                    }
                }
            } catch (Throwable t) {
                Log.w(TAG, "patchActiveSavBinary error for " + sav + ": " + t.getMessage());
            }
        }
        Log.i(TAG, "PUBGM Active.sav binary enforced level " + fpsLevel + " (" + targetFps + " FPS) for " + pkg);
    }

    private static boolean patchBinarySavField(byte[] data, String fieldName, int value) {
        if (data == null || fieldName == null) return false;
        byte[] pattern = fieldName.getBytes(StandardCharsets.UTF_8);
        int idx = indexOfBytes(data, pattern);
        if (idx != -1) {
            // In Active.sav, the value byte usually appears 9-10 bytes after the field name ASCII bytes
            for (int offset = idx + pattern.length; offset < Math.min(data.length, idx + pattern.length + 16); offset++) {
                if (data[offset] >= 1 && data[offset] <= 10) {
                    data[offset] = (byte) value;
                    return true;
                }
            }
        }
        return false;
    }

    private static int indexOfBytes(byte[] source, byte[] target) {
        if (source == null || target == null || source.length < target.length) return -1;
        for (int i = 0; i <= source.length - target.length; i++) {
            boolean match = true;
            for (int j = 0; j < target.length; j++) {
                if (source[i + j] != target[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }

    private static boolean applyPatch(String path, int targetFps) {
        final FpsUnlockTier tier = FpsUnlockTier.fromFps(targetFps);
        final int pubgFpsLevel = tier.level;
        String[] cvars = {
            "+CVars=r.PUBGDeviceFPS=" + pubgFpsLevel,
            "+CVars=r.PUBGMaxFPS=" + targetFps,
            "+CVars=r.PUBGFrameRateLimit=" + targetFps,
            "+CVars=r.MobileFPSLimit=" + targetFps,
            "+CVars=r.FrameRateLimit=" + targetFps,
            "+CVars=r.PUBGHDRMode=1",
            "+CVars=r.MobileHDR=1",
            "+CVars=r.PUBGQualityLevel=4",
            "+CVars=r.PUBGSDKQualityLevel=4",
            "+CVars=r.Unlock120Hz=1",
            "+CVars=r.Unlock144Hz=1",
            "+CVars=r.Unlock165Hz=1",
            "+CVars=r.Unlock185Hz=1",
            "+CVars=r.Vsync=0",
            "FrameRateLevel=" + pubgFpsLevel,
            "FPS=" + targetFps,
            "TargetFPS=" + targetFps,
            "MaxFPS=" + targetFps,
            "bUseHDRMode=True",
            "bUseAntiAliasing=True"
        };
        return ConfigFileHelper.patchKeys(path, cvars, "[UserCustom DeviceProfile]");
    }
}
