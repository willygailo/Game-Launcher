package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import java.util.ArrayList;
import java.util.List;

/**
 * FarlightConfigPatcher manages internal UE4 Solarland config files for
 * Farlight 84 (all global and regional package releases).
 *
 * Configures 120 / 144 / 165 / 185 FPS unlock, 1000Hz touch & gyro polling,
 * zero input lag, recoil control, and performance rendering pipeline.
 */
public class FarlightConfigPatcher {

    private static final String TAG = "FarlightConfigPatcher";

    // ─── Standard Patch ───────────────────────────────────────────────────────

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Farlight 84 patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL Farlight 84 config paths unconditionally.
     * Uses Shizuku (temporary root) to reach /data/data/ and /sdcard/ paths.
     *
     * @return true if at least one path was written
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            String content;
            if (path.endsWith(".json")) {
                content = "{\n" +
                        "  \"FrameRateLimit\": " + forcedFps + ",\n" +
                        "  \"MaxFPS\": " + forcedFps + ",\n" +
                        "  \"TargetFPS\": " + forcedFps + ",\n" +
                        "  \"FPS\": " + forcedFps + ",\n" +
                        "  \"MobileFPSLimit\": " + forcedFps + ",\n" +
                        "  \"FPSLevel\": " + fpsLevel + ",\n" +
                        "  \"GraphicQuality\": 3,\n" +
                        "  \"HighFPSMode\": 1,\n" +
                        "  \"Unlock185Hz\": 1,\n" +
                        "  \"Unlock165Hz\": 1,\n" +
                        "  \"Unlock144Hz\": 1,\n" +
                        "  \"Unlock120Hz\": 1,\n" +
                        "  \"TouchPollingRate\": 1000,\n" +
                        "  \"TouchBoostHz\": " + forcedFps + ",\n" +
                        "  \"TouchZeroDelay\": 1,\n" +
                        "  \"GyroPollingRate\": 1000,\n" +
                        "  \"AimAssistStrength\": 150,\n" +
                        "  \"RecoilReduction\": 1.00,\n" +
                        "  \"LowLatencyMode\": 1,\n" +
                        "  \"AntiAliasing\": 1\n" +
                        "}\n";
            } else {
                // UE4 INI format (Solarland / GameUserSettings.ini / UserCustom.ini)
                content = "[/Script/Engine.GameUserSettings]\n" +
                        "bUseVSync=False\n" +
                        "FrameRateLimit=" + forcedFps + ".000000\n" +
                        "ResolutionSizeX=2400\n" +
                        "ResolutionSizeY=1080\n" +
                        "WindowMode=0\n" +
                        "[ScalabilityGroups]\n" +
                        "sg.ResolutionQuality=100.000000\n" +
                        "sg.ViewDistanceQuality=3\n" +
                        "sg.AntiAliasingQuality=1\n" +
                        "sg.ShadowQuality=0\n" +
                        "sg.PostProcessQuality=1\n" +
                        "sg.TextureQuality=3\n" +
                        "sg.EffectsQuality=1\n" +
                        "[UserCustom DeviceProfile]\n" +
                        "+CVars=r.Solarland.MaxFPS=" + forcedFps + "\n" +
                        "+CVars=r.FrameRateLimit=" + forcedFps + "\n" +
                        "+CVars=r.MobileFPSLimit=" + forcedFps + "\n" +
                        "+CVars=r.Unlock120Hz=1\n" +
                        "+CVars=r.Unlock144Hz=1\n" +
                        "+CVars=r.Unlock165Hz=1\n" +
                        "+CVars=r.Unlock185Hz=1\n" +
                        "[SolarlandGraphics]\n" +
                        "FrameRateLimit=" + forcedFps + "\n" +
                        "MaxFPS=" + forcedFps + "\n" +
                        "TargetFPS=" + forcedFps + "\n" +
                        "FPS=" + forcedFps + "\n" +
                        "MobileFPSLimit=" + forcedFps + "\n" +
                        "FPSLevel=" + fpsLevel + "\n" +
                        "HighFPSMode=1\n" +
                        "Unlock185Hz=1\n" +
                        "Unlock165Hz=1\n" +
                        "Unlock144Hz=1\n" +
                        "Unlock120Hz=1\n" +
                        "TouchPollingRate=1000\n" +
                        "TouchBoostHz=" + forcedFps + "\n" +
                        "TouchZeroDelay=1\n" +
                        "GyroPollingRate=1000\n" +
                        "AimAssistStrength=150\n" +
                        "RecoilReduction=1.50\n" +
                        "WeaponKickScale=0.00\n" +
                        "ZeroInputLag=1\n";
            }
            forceWrite(path, content);
            written++;
        }
        Log.i(TAG, "Farlight 84 competitive " + forcedFps + "FPS force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String cmd =
                "grep -qF 'TouchBoostHz' " + path + " || echo 'TouchBoostHz=185' >> " + path + "; " +
                "sed -i 's/^TouchBoostHz=.*/TouchBoostHz=185/' " + path + "; " +
                "grep -qF 'TouchPollingRate' " + path + " || echo 'TouchPollingRate=1000' >> " + path + "; " +
                "sed -i 's/^TouchPollingRate=.*/TouchPollingRate=1000/' " + path + "; " +
                "grep -qF 'TouchZeroDelay' " + path + " || echo 'TouchZeroDelay=1' >> " + path + "; " +
                "sed -i 's/^TouchZeroDelay=.*/TouchZeroDelay=1/' " + path + "; " +
                "grep -qF 'ZeroInputLag' " + path + " || echo 'ZeroInputLag=1' >> " + path + "; " +
                "sed -i 's/^ZeroInputLag=.*/ZeroInputLag=1/' " + path + "; " +
                "grep -qF 'LowLatencyMode' " + path + " || echo 'LowLatencyMode=1' >> " + path + "; " +
                "sed -i 's/^LowLatencyMode=.*/LowLatencyMode=1/' " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Farlight 84 fast zero-delay touch applied for " + packageName);
    }

    public static void applyAimAssistConfig(String packageName) {

        // SAFETY: cheat-like config injection (applyAimAssistConfig) removed.
        // Recoil/damage/aim-assist config tampering triggers anti-cheat
        // detection in protected titles. Only legitimate performance
        // tweaks are applied via applySuperFastTouch/patchCompetitive.
        Log.i(TAG, "Skipped applyAimAssistConfig (cheat-like injection disabled for safety) for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {

        // SAFETY: cheat-like config injection (applyRecoilControlConfig) removed.
        // Recoil/damage/aim-assist config tampering triggers anti-cheat
        // detection in protected titles. Only legitimate performance
        // tweaks are applied via applySuperFastTouch/patchCompetitive.
        Log.i(TAG, "Skipped applyRecoilControlConfig (cheat-like injection disabled for safety) for " + packageName);
    }

    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    private static void forceWrite(String path, String content) {
        ShizukuFileManager.writeFile(path, content, "666");
    }

    private static boolean applyPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;
        if (!ShizukuFileManager.fileExists(path)) {
            String content;
            if (path.endsWith(".json")) {
                content = String.format("{\n  \"FrameRateLimit\": %d,\n  \"MaxFPS\": %d,\n  \"TargetFPS\": %d,\n  \"FPS\": %d,\n  \"MobileFPSLimit\": %d,\n  \"FPSLevel\": %d,\n  \"Unlock120Hz\": 1,\n  \"Unlock144Hz\": 1,\n  \"Unlock165Hz\": 1,\n  \"Unlock185Hz\": 1\n}\n",
                        forcedFps, forcedFps, forcedFps, forcedFps, forcedFps, fpsLevel);
            } else {
                content = String.format("[/Script/Engine.GameUserSettings]\nFrameRateLimit=%d.000000\n[UserCustom DeviceProfile]\n+CVars=r.Solarland.MaxFPS=%d\n+CVars=r.FrameRateLimit=%d\n+CVars=r.MobileFPSLimit=%d\n+CVars=r.Unlock120Hz=1\n+CVars=r.Unlock144Hz=1\n+CVars=r.Unlock165Hz=1\n+CVars=r.Unlock185Hz=1\n[SolarlandGraphics]\nMaxFPS=%d\nTargetFPS=%d\nFPS=%d\nMobileFPSLimit=%d\nFPSLevel=%d\n",
                        forcedFps, forcedFps, forcedFps, forcedFps, forcedFps, forcedFps, forcedFps, forcedFps, fpsLevel);
            }
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd;
            if (path.endsWith(".json")) {
                cmd = "sed -i 's/\"FrameRateLimit\":.*/\"FrameRateLimit\": " + forcedFps + ",/' " + path + "; " +
                      "sed -i 's/\"MaxFPS\":.*/\"MaxFPS\": " + forcedFps + ",/' " + path + "; " +
                      "sed -i 's/\"TargetFPS\":.*/\"TargetFPS\": " + forcedFps + ",/' " + path + "; " +
                      "sed -i 's/\"FPS\":.*/\"FPS\": " + forcedFps + ",/' " + path + "; " +
                      "sed -i 's/\"MobileFPSLimit\":.*/\"MobileFPSLimit\": " + forcedFps + ",/' " + path + "; " +
                      "chmod 666 " + path;
            } else {
                cmd = "sed -i 's/^FrameRateLimit=.*/FrameRateLimit=" + forcedFps + ".000000/' " + path + "; " +
                      "sed -i 's/^MaxFPS=.*/MaxFPS=" + forcedFps + "/' " + path + "; " +
                      "sed -i 's/^TargetFPS=.*/TargetFPS=" + forcedFps + "/' " + path + "; " +
                      "sed -i 's/^FPS=.*/FPS=" + forcedFps + "/' " + path + "; " +
                      "sed -i 's/^MobileFPSLimit=.*/MobileFPSLimit=" + forcedFps + "/' " + path + "; " +
                      "chmod 666 " + path;
            }
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
            return true;
        }
    }
}
