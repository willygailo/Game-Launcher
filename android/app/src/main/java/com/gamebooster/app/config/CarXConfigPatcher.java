package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * CarXConfigPatcher manages legal configuration files for CarX Street, Asphalt 9/Legends Unite,
 * and high-fidelity racing games.
 * Unlocks 60 FPS / 90 FPS / 120 FPS / 144 FPS / 165 FPS / 185 FPS.
 */
public class CarXConfigPatcher {

    private static final String TAG = "CarXConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = targetFps > 0 ? targetFps : 185;
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyStandardPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "CarX/Racing patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = targetFps > 0 ? targetFps : 185;
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String iniContent = "[GraphicSettings]\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "FPSLevel=" + fpsLevel + "\n" +
                "FrameRateLimit=" + forcedFps + "\n" +
                "FPSLimit=0\n" +
                "HighFPSMode=1\n" +
                "UnlockFPS=1\n" +
                "UnlockHighFPS=1\n" +
                "Unlock120FPS=1\n" +
                "Unlock144FPS=1\n" +
                "Unlock165FPS=1\n" +
                "Unlock185FPS=1\n" +
                "GraphicQuality=4\n" +
                "UltraExtreme=1\n" +
                "HDRMode=1\n" +
                "Vsync=0\n" +
                "ResolutionScale=1.2\n" +
                "MotionBlur=0\n" +
                "ShadowQuality=2\n" +
                "DynamicResolution=0\n" +
                "NitroMultiplier=1.90\n" +
                "TorqueBoost=1.90\n" +
                "DriftScoreMultiplier=2.0\n" +
                "TouchPollingRate=1000\n" +
                "TouchSlop=1\n" +
                "TouchZeroDelay=1\n";

        String jsonContent = "{\n" +
                "  \"graphics\": {\n" +
                "    \"max_fps\": " + forcedFps + ",\n" +
                "    \"target_fps\": " + forcedFps + ",\n" +
                "    \"frame_rate_limit\": " + forcedFps + ",\n" +
                "    \"fps_level\": " + fpsLevel + ",\n" +
                "    \"fps_unlocked\": true,\n" +
                "    \"unlock_fps\": true,\n" +
                "    \"unlock_120\": true,\n" +
                "    \"unlock_144\": true,\n" +
                "    \"unlock_165\": true,\n" +
                "    \"unlock_185\": true,\n" +
                "    \"ultra_extreme\": true,\n" +
                "    \"graphic_quality\": \"ultra\",\n" +
                "    \"resolution_scale\": 1.2,\n" +
                "    \"hdr_enabled\": true,\n" +
                "    \"vsync\": false\n" +
                "  },\n" +
                "  \"performance\": {\n" +
                "    \"nitro_boost\": 1.90,\n" +
                "    \"torque_multiplier\": 1.90,\n" +
                "    \"drift_multiplier\": 2.0\n" +
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
        Log.i(TAG, "CarX competitive UltraExtreme " + forcedFps + "FPS force-write: " + written + " paths");
        return written > 0;
    }

    public static void applyDamageScriptConfig(String packageName) {
        // Racing Boost: Nitro & Torque acceleration
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String boostData = "\n[EngineTune]\nNitroMultiplier=1.90\nTorqueBoost=1.90\nDriftScoreMultiplier=2.0\nThrottleResponse=1.0\n";
            if (ShizukuFileManager.fileExists(path)) {
                String cmd = "echo '" + boostData + "' >> " + path + "; chmod 666 " + path;
                if (ShizukuFileManager.hasFullAccess()) {
                    ShizukuExecutor.executeShizukuCommand(cmd);
                } else {
                    CommandExecutor.executeSystemCommand(cmd);
                }
            }
        }
        Log.i(TAG, "CarX/Racing Nitro & Torque boost applied for " + packageName);
    }

    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String touchAppend = "\n[SteeringControls]\nTouchRate=1000\nSteeringSensitivity=1.1\nInputDeadZone=0.0\nTouchZeroDelay=1\n";
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
        // Gyro Steering Control
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String gyroData = "\n[GyroSteering]\nGyroSensitivity=1.0\nGyroSmooth=0\nGyroHz=1000\nAutoSteerAssist=1\n";
            if (ShizukuFileManager.fileExists(path)) {
                String cmd = "echo '" + gyroData + "' >> " + path + "; chmod 666 " + path;
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
            String driftData = "\n[DriftAssist]\nThrottleResponse=1.0\nBrakeResponse=1.0\nStabilizerControl=1.0\n";
            if (ShizukuFileManager.fileExists(path)) {
                String cmd = "echo '" + driftData + "' >> " + path + "; chmod 666 " + path;
                if (ShizukuFileManager.hasFullAccess()) {
                    ShizukuExecutor.executeShizukuCommand(cmd);
                } else {
                    CommandExecutor.executeSystemCommand(cmd);
                }
            }
        }
    }

    private static boolean applyStandardPatch(String path, int targetFps) {
        final int forcedFps = targetFps > 0 ? targetFps : 185;
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;
        if (!ShizukuFileManager.fileExists(path)) {
            String content = String.format(
                    "[GraphicSettings]\nTargetFPS=%d\nMaxFPS=%d\nFrameRateLimit=%d\nFPSLevel=%d\nHighFPSMode=1\nUnlockFPS=1\nUnlockHighFPS=1\nUnlock120FPS=1\nUnlock144FPS=1\nUnlock165FPS=1\nUnlock185FPS=1\n",
                    forcedFps, forcedFps, forcedFps, fpsLevel
            );
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd = "sed -i 's/^TargetFPS=.*/TargetFPS=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^MaxFPS=.*/MaxFPS=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^FrameRateLimit=.*/FrameRateLimit=" + forcedFps + "/' " + path + "; " +
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
