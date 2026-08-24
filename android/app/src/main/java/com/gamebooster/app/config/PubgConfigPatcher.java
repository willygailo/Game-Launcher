package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
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

        String[] keys = new String[] {
                "+CVars=r.PUBGDeviceFPS=" + pubgFpsLevel,
                "+CVars=r.PUBGMaxFPS=" + forcedFps,
                "+CVars=r.PUBGFrameRateLimit=" + forcedFps,
                "+CVars=r.MobileFPSLimit=" + forcedFps,
                "+CVars=r.FrameRateLimit=" + forcedFps,
                "+CVars=r.PUBGHDRMode=1",
                "+CVars=r.MobileHDR=1",
                "+CVars=r.PUBGQualityLevel=4",
                "+CVars=r.PUBGSDKQualityLevel=4",
                "+CVars=r.Tonemapper.Quality=4",
                "+CVars=r.HDR.Display.OutputDevice=1",
                "+CVars=r.MobileContentScaleFactor=1.0",
                "+CVars=r.MobileTonemapperFilm=1",
                "+CVars=r.PUBGTPPViewRange=100.00",
                "+CVars=r.PUBGFPPViewRange=150.00",
                "+CVars=r.SprintSensitivity=150",
                "+CVars=r.Vsync=0",
                "+CVars=r.Unlock120Hz=1",
                "+CVars=r.Unlock144Hz=1",
                "+CVars=r.Unlock165Hz=1",
                "+CVars=r.Unlock185Hz=1",
                "+CVars=r.SuppressLogs=1",
                "+CVars=r.DisableDebugLog=1",
                "+CVars=r.EnableCrashReporting=0",
                "+CVars=r.Telemetry=0",
                "+CVars=a.DisableAnalytics=1",
                "+CVars=r.LogFilter=0",
                "+CVars=r.TouchBoostHz=" + forcedFps,
                "+CVars=r.MobileTouchBoostRate=" + forcedFps,
                "+CVars=r.GyroSampleRate=1000",
                "+CVars=r.GyroSensitivityRatio=2.5",
                "+CVars=r.GyroZeroDelay=1",
                "+CVars=r.GyroLatencyMode=0",
                "+CVars=r.GyroSmoothFactor=1",
                "+CVars=r.GyroStabilization=1",
                "FrameRateLevel=" + pubgFpsLevel,
                "FPS=" + forcedFps,
                "TargetFPS=" + forcedFps,
                "MaxFPS=" + forcedFps,
                "UnlockFPS=1",
                "Unlock120FPS=1",
                "Unlock144FPS=1",
                "Unlock165FPS=1",
                "Unlock185FPS=1",
                "Ultra144FPS=1",
                "Ultra165FPS=1",
                "Ultra185FPS=1",
                "UltraExtreme=1",
                "bUseUltraExtreme=True",
                "GraphicsQuality=5",
                "GraphicQuality=4",
                "HDRMode=1",
                "UltraHDMode=1",
                "SuperResolution=1",
                "bUseHDRMode=True",
                "bUseHighQualityBloom=True",
                "bUseAntiAliasing=True",
                "bDisableAnalytics=True",
                "bDisableBugReporting=True",
                "SprintSensitivity=150",
                "TPPFieldOfView=100",
                "FPPFieldOfView=150"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[UserCustom DeviceProfile]")) {
                written++;
            }
        }
        patchActiveSavBinary(packageName, forcedFps);
        deployPakPatch(packageName);
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "PUBGM competitive HDR " + forcedFps + "FPS non-destructive in-place merge: " + written + " paths @ " + forcedFps + "fps for " + packageName);
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
            "/sdcard/GameLauncher/" + pakFileName,
            "/storage/emulated/0/Game-Launcher/" + pakFileName,
            "/sdcard/Game-Launcher/" + pakFileName,
            "/storage/emulated/0/Documents/" + pakFileName,
            "/sdcard/Documents/" + pakFileName,
            "/storage/emulated/0/Documents/Game-Launcher/" + pakFileName,
            "/sdcard/Documents/Game-Launcher/" + pakFileName,
            "/data/local/tmp/" + pakFileName
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
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(copyCmd);
            } else {
                CommandExecutor.executeSystemCommand(copyCmd);
            }
            if (ShizukuFileManager.fileExists(dest)) {
                deployed = true;
                Log.i(TAG, "Successfully deployed " + pakFileName + " to " + dest);
            }
        }
        return deployed;
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
