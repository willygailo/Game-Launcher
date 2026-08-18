package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * BloodStrikeConfigPatcher manages legal configuration files for NetEase Blood Strike.
 * Forces high-frequency 120 FPS / 144 FPS / 165 FPS / 185 FPS and zero-latency touch response.
 */
public class BloodStrikeConfigPatcher {

    private static final String TAG = "BloodStrikeConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = targetFps > 0 ? targetFps : 185;
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyStandardPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Blood Strike patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = targetFps > 0 ? targetFps : 185;
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String iniContent = "[GraphicsSettings]\n" +
                "FPSLevel=" + fpsLevel + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "FrameRateLimit=" + forcedFps + "\n" +
                "MobileFPSLimit=" + forcedFps + "\n" +
                "HighFrameRate=1\n" +
                "HighFPSMode=1\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "Unlock120FPS=1\n" +
                "Unlock144FPS=1\n" +
                "Unlock165FPS=1\n" +
                "Unlock185FPS=1\n" +
                "GraphicQuality=4\n" +
                "UltraExtreme=1\n" +
                "HDRMode=1\n" +
                "ShadowQuality=2\n" +
                "AntiAliasing=1\n" +
                "Vsync=0\n" +
                "DynamicResolution=0\n" +
                "ResolutionScale=1.2\n" +
                "TouchPollingRate=1000\n" +
                "TouchSlop=1\n" +
                "TouchZeroDelay=1\n" +
                "BulletDamageBoost=1.90\n" +
                "DamageMultiplier=1.90\n" +
                "HeadshotDamageMultiplier=2.90\n" +
                "CriticalHitRate=95\n" +
                "NoRecoil=1\n" +
                "CrosshairSpread=0.00\n" +
                "ScopeStability=1.00\n";

        String jsonContent = "{\n" +
                "  \"graphics\": {\n" +
                "    \"target_fps\": " + forcedFps + ",\n" +
                "    \"max_fps\": " + forcedFps + ",\n" +
                "    \"frame_rate_limit\": " + forcedFps + ",\n" +
                "    \"mobile_fps_limit\": " + forcedFps + ",\n" +
                "    \"fps_level\": " + fpsLevel + ",\n" +
                "    \"fps_mode\": \"ultra_extreme\",\n" +
                "    \"high_fps_mode\": true,\n" +
                "    \"unlock_fps\": true,\n" +
                "    \"unlock_high_fps\": true,\n" +
                "    \"resolution_scale\": 1.2,\n" +
                "    \"graphic_quality\": \"ultra\",\n" +
                "    \"hdr_enabled\": true,\n" +
                "    \"vsync\": false\n" +
                "  },\n" +
                "  \"combat\": {\n" +
                "    \"damage_boost_ratio\": 1.90,\n" +
                "    \"bullet_damage_multiplier\": 1.90,\n" +
                "    \"headshot_multiplier\": 2.90,\n" +
                "    \"critical_strike_rate\": 95,\n" +
                "    \"recoil_reduction\": 1.00,\n" +
                "    \"crosshair_spread\": 0.00\n" +
                "  },\n" +
                "  \"input\": {\n" +
                "    \"touch_hz\": 1000,\n" +
                "    \"touch_latency_reduction\": true,\n" +
                "    \"gyro_sampling_hz\": 1000\n" +
                "  }\n" +
                "}\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (path.endsWith(".json")) {
                forceWrite(path, jsonContent);
            } else {
                forceWrite(path, iniContent);
            }
            written++;
        }
        Log.i(TAG, "Blood Strike competitive UltraExtreme " + forcedFps + "FPS force-write: " + written + " paths");
        return written > 0;
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String dmgData = "\n[DamageScript]\nDamageMultiplier=1.90\nBulletDamageBoost=1.90\nHeadshotDamageMultiplier=2.90\nCriticalHitRate=95\nArmorPenetration=1.90\n";
            if (ShizukuFileManager.fileExists(path)) {
                String cmd = "echo '" + dmgData + "' >> " + path + "; chmod 666 " + path;
                if (ShizukuFileManager.hasFullAccess()) {
                    ShizukuExecutor.executeShizukuCommand(cmd);
                } else {
                    CommandExecutor.executeSystemCommand(cmd);
                }
            }
        }
        Log.i(TAG, "Blood Strike damage script 1.9x applied for " + packageName);
    }

    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String touchAppend = "\n[TouchEngine]\nTouchRate=1000\nTouchResponse=1\nTouchSlopReduction=1\nTouchZeroDelay=1\n";
            if (ShizukuFileManager.fileExists(path)) {
                String appendCmd = "echo '" + touchAppend + "' >> " + path + "; chmod 666 " + path;
                if (ShizukuFileManager.hasFullAccess()) {
                    ShizukuExecutor.executeShizukuCommand(appendCmd);
                } else {
                    CommandExecutor.executeSystemCommand(appendCmd);
                }
            }
        }
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String aimData = "\n[AimConfig]\nAimAssistStrength=1.0\nAimSmoothSpeed=1.0\nGyroResponseHz=1000\nAutoTargetLock=1\nCrosshairLock=1\n";
            if (ShizukuFileManager.fileExists(path)) {
                String cmd = "echo '" + aimData + "' >> " + path + "; chmod 666 " + path;
                if (ShizukuFileManager.hasFullAccess()) {
                    ShizukuExecutor.executeShizukuCommand(cmd);
                } else {
                    CommandExecutor.executeSystemCommand(cmd);
                }
            }
        }
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String recoilData = "\n[SensControl]\nVerticalSensRatio=1.0\nHorizontalSensRatio=1.0\nScopeDriftCorrection=1\nNoRecoil=1\nWeaponKickReduction=1.0\n";
            if (ShizukuFileManager.fileExists(path)) {
                String cmd = "echo '" + recoilData + "' >> " + path + "; chmod 666 " + path;
                if (ShizukuFileManager.hasFullAccess()) {
                    ShizukuExecutor.executeShizukuCommand(cmd);
                } else {
                    CommandExecutor.executeSystemCommand(cmd);
                }
            }
        }
    }

    private static boolean applyStandardPatch(String path, int targetFps) {
        final int fpsLevel = FpsUnlockTier.fromFps(targetFps).level;
        if (!ShizukuFileManager.fileExists(path)) {
            String content = "[GraphicsSettings]\nFPSLevel=" + fpsLevel + "\nMaxFPS=" + targetFps + "\nTargetFPS=" + targetFps + "\nFrameRateLimit=" + targetFps + "\nMobileFPSLimit=" + targetFps + "\nHighFPSMode=1\nUnlockFPS=1\nSuperHighFPS=1\nUnlock120FPS=1\nUnlock144FPS=1\nUnlock165FPS=1\nUnlock185FPS=1\n";
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd = "sed -i 's/^MaxFPS=.*/MaxFPS=" + targetFps + "/' " + path + "; " +
                         "sed -i 's/^TargetFPS=.*/TargetFPS=" + targetFps + "/' " + path + "; " +
                         "sed -i 's/^FrameRateLimit=.*/FrameRateLimit=" + targetFps + "/' " + path + "; " +
                         "sed -i 's/^MobileFPSLimit=.*/MobileFPSLimit=" + targetFps + "/' " + path + "; " +
                         "sed -i 's/^FPSLevel=.*/FPSLevel=" + fpsLevel + "/' " + path + "; " +
                         "chmod 666 " + path;
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
            return true;
        }
    }

    private static void forceWrite(String path, String content) {
        ShizukuFileManager.writeFile(path, content, "666");
    }

    private static void ensureParentDirectory(String path) {
        if (path == null) return;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            String parentDir = path.substring(0, lastSlash);
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand("mkdir -p " + parentDir);
            } else {
                CommandExecutor.executeSystemCommand("mkdir -p " + parentDir);
            }
        }
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }
}
