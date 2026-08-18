package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import java.util.ArrayList;
import java.util.List;

/**
 * ValorantConfigPatcher manages internal UE4 config files and user settings for
 * Valorant Mobile (CN Server Project C and Global versions).
 *
 * Configures 120 / 144 / 165 / 185 FPS unlock, 1000Hz touch & gyro polling,
 * zero input lag, recoil control, and performance rendering pipeline.
 */
public class ValorantConfigPatcher {

    private static final String TAG = "ValorantConfigPatcher";

    // ─── Standard Patch ───────────────────────────────────────────────────────

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        int forcedFps = targetFps > 0 ? targetFps : 185;
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Valorant Mobile patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL Valorant Mobile config paths unconditionally.
     * Uses Shizuku (temporary root) to reach /data/data/ and /sdcard/ paths.
     *
     * @return true if at least one path was written
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = targetFps > 0 ? targetFps : 185;
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            String content;
            if (path.endsWith(".json")) {
                content = "{\n" +
                        "  \"MaxFrameRate\": " + forcedFps + ",\n" +
                        "  \"TargetFPS\": " + forcedFps + ",\n" +
                        "  \"GraphicQuality\": 4,\n" +
                        "  \"FPSLimit\": " + forcedFps + ",\n" +
                        "  \"FrameRateLimit\": " + forcedFps + ".000000,\n" +
                        "  \"MobileFPSLimit\": " + forcedFps + ",\n" +
                        "  \"FPSLevel\": " + fpsLevel + ",\n" +
                        "  \"Unlock185Hz\": 1,\n" +
                        "  \"Unlock165Hz\": 1,\n" +
                        "  \"Unlock144Hz\": 1,\n" +
                        "  \"Unlock120Hz\": 1,\n" +
                        "  \"HighFPSMode\": 1,\n" +
                        "  \"TouchBoostHz\": " + forcedFps + ",\n" +
                        "  \"TouchPollingRate\": 1000,\n" +
                        "  \"TouchZeroDelay\": 1,\n" +
                        "  \"GyroSampleRate\": 1000,\n" +
                        "  \"SuperResolution\": 1,\n" +
                        "  \"FieldOfView\": 120,\n" +
                        "  \"FPP_FOV\": 120,\n" +
                        "  \"CrosshairBloom\": 0,\n" +
                        "  \"AimAssist\": 1,\n" +
                        "  \"AimAssistStrength\": 100,\n" +
                        "  \"RecoilScale\": 0.00,\n" +
                        "  \"WeaponKickReduction\": 1.00,\n" +
                        "  \"BulletSpreadReduction\": 1.00,\n" +
                        "  \"HeadshotDamageMultiplier\": 2.50,\n" +
                        "  \"AntiAliasing\": 1,\n" +
                        "  \"VulkanEnabled\": 1,\n" +
                        "  \"LowLatencyMode\": 1\n" +
                        "}\n";
            } else {
                // UE4 INI format (UserCustom.ini / GameUserSettings.ini)
                content = "[/Script/Engine.GameUserSettings]\n" +
                        "bUseVSync=False\n" +
                        "FrameRateLimit=" + forcedFps + ".000000\n" +
                        "ResolutionSizeX=2400\n" +
                        "ResolutionSizeY=1080\n" +
                        "LastUserConfirmedResolutionSizeX=2400\n" +
                        "LastUserConfirmedResolutionSizeY=1080\n" +
                        "WindowMode=0\n" +
                        "bUseDesiredScreenHeight=False\n" +
                        "[ScalabilityGroups]\n" +
                        "sg.ResolutionQuality=100.000000\n" +
                        "sg.ViewDistanceQuality=3\n" +
                        "sg.AntiAliasingQuality=1\n" +
                        "sg.ShadowQuality=0\n" +
                        "sg.PostProcessQuality=1\n" +
                        "sg.TextureQuality=3\n" +
                        "sg.EffectsQuality=1\n" +
                        "sg.FoliageQuality=0\n" +
                        "sg.ShadingQuality=2\n" +
                        "[UserCustom DeviceProfile]\n" +
                        "+CVars=r.FrameRateLimit=" + forcedFps + "\n" +
                        "+CVars=r.MobileFPSLimit=" + forcedFps + "\n" +
                        "+CVars=r.MobileContentScaleFactor=1.0\n" +
                        "+CVars=r.Unlock120Hz=1\n" +
                        "+CVars=r.Unlock144Hz=1\n" +
                        "+CVars=r.Unlock165Hz=1\n" +
                        "+CVars=r.Unlock185Hz=1\n" +
                        "[ValorantMobileGraphics]\n" +
                        "MaxFPS=" + forcedFps + "\n" +
                        "TargetFPS=" + forcedFps + "\n" +
                        "FrameRateLimit=" + forcedFps + "\n" +
                        "MobileFPSLimit=" + forcedFps + "\n" +
                        "FPSLevel=" + fpsLevel + "\n" +
                        "Unlock185Hz=1\n" +
                        "Unlock165Hz=1\n" +
                        "Unlock144Hz=1\n" +
                        "Unlock120Hz=1\n" +
                        "HighFPSMode=1\n" +
                        "TouchPollingRate=1000\n" +
                        "TouchBoostHz=" + forcedFps + "\n" +
                        "TouchZeroDelay=1\n" +
                        "GyroPollingRate=1000\n" +
                        "AimAssistStrength=100\n" +
                        "RecoilReduction=1.00\n" +
                        "WeaponKickScale=0.00\n" +
                        "ZeroInputLag=1\n" +
                        "VulkanPipeline=1\n";
            }
            forceWrite(path, content);
            written++;
        }
        Log.i(TAG, "Valorant Mobile competitive " + forcedFps + "FPS force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
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
        Log.i(TAG, "Valorant fast zero-delay touch applied for " + packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String cmd =
                "grep -qF 'AimAssist' " + path + " || echo 'AimAssist=1' >> " + path + "; " +
                "sed -i 's/^AimAssist=.*/AimAssist=1/' " + path + "; " +
                "grep -qF 'AimAssistStrength' " + path + " || echo 'AimAssistStrength=100' >> " + path + "; " +
                "sed -i 's/^AimAssistStrength=.*/AimAssistStrength=100/' " + path + "; " +
                "grep -qF 'GyroPollingRate' " + path + " || echo 'GyroPollingRate=1000' >> " + path + "; " +
                "sed -i 's/^GyroPollingRate=.*/GyroPollingRate=1000/' " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Valorant aim assist & 1000Hz gyro applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String cmd =
                "grep -qF 'RecoilScale' " + path + " || echo 'RecoilScale=0.00' >> " + path + "; " +
                "sed -i 's/^RecoilScale=.*/RecoilScale=0.00/' " + path + "; " +
                "grep -qF 'RecoilReduction' " + path + " || echo 'RecoilReduction=1.00' >> " + path + "; " +
                "sed -i 's/^RecoilReduction=.*/RecoilReduction=1.00/' " + path + "; " +
                "grep -qF 'WeaponKickScale' " + path + " || echo 'WeaponKickScale=0.00' >> " + path + "; " +
                "sed -i 's/^WeaponKickScale=.*/WeaponKickScale=0.00/' " + path + "; " +
                "grep -qF 'WeaponKickReduction' " + path + " || echo 'WeaponKickReduction=1.00' >> " + path + "; " +
                "sed -i 's/^WeaponKickReduction=.*/WeaponKickReduction=1.00/' " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Valorant recoil & weapon kick reduction applied for " + packageName);
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
        final int forcedFps = targetFps > 0 ? targetFps : 185;
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;
        if (!ShizukuFileManager.fileExists(path)) {
            String content;
            if (path.endsWith(".json")) {
                content = String.format("{\n  \"MaxFrameRate\": %d,\n  \"TargetFPS\": %d,\n  \"FPSLimit\": %d,\n  \"FrameRateLimit\": %d.000000,\n  \"MobileFPSLimit\": %d,\n  \"FPSLevel\": %d,\n  \"Unlock120Hz\": 1,\n  \"Unlock144Hz\": 1,\n  \"Unlock165Hz\": 1,\n  \"Unlock185Hz\": 1\n}\n",
                        forcedFps, forcedFps, forcedFps, forcedFps, forcedFps, fpsLevel);
            } else {
                content = String.format("[/Script/Engine.GameUserSettings]\nFrameRateLimit=%d.000000\n[UserCustom DeviceProfile]\n+CVars=r.FrameRateLimit=%d\n+CVars=r.MobileFPSLimit=%d\n+CVars=r.Unlock120Hz=1\n+CVars=r.Unlock144Hz=1\n+CVars=r.Unlock165Hz=1\n+CVars=r.Unlock185Hz=1\n[ValorantMobileGraphics]\nMaxFPS=%d\nTargetFPS=%d\nFrameRateLimit=%d\nMobileFPSLimit=%d\nFPSLevel=%d\n",
                        forcedFps, forcedFps, forcedFps, forcedFps, forcedFps, forcedFps, forcedFps, fpsLevel);
            }
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd;
            if (path.endsWith(".json")) {
                cmd = "sed -i 's/\"MaxFrameRate\":.*/\"MaxFrameRate\": " + forcedFps + ",/' " + path + "; " +
                      "sed -i 's/\"TargetFPS\":.*/\"TargetFPS\": " + forcedFps + ",/' " + path + "; " +
                      "sed -i 's/\"FPSLimit\":.*/\"FPSLimit\": " + forcedFps + ",/' " + path + "; " +
                      "sed -i 's/\"FrameRateLimit\":.*/\"FrameRateLimit\": " + forcedFps + ".000000,/' " + path + "; " +
                      "sed -i 's/\"MobileFPSLimit\":.*/\"MobileFPSLimit\": " + forcedFps + ",/' " + path + "; " +
                      "chmod 666 " + path;
            } else {
                cmd = "sed -i 's/^FrameRateLimit=.*/FrameRateLimit=" + forcedFps + ".000000/' " + path + "; " +
                      "sed -i 's/^MaxFPS=.*/MaxFPS=" + forcedFps + "/' " + path + "; " +
                      "sed -i 's/^TargetFPS=.*/TargetFPS=" + forcedFps + "/' " + path + "; " +
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
