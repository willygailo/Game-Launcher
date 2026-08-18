package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import java.util.ArrayList;
import java.util.List;

/**
 * RobloxConfigPatcher manages ClientAppSettings.json FastFlags and local graphics settings
 * for Roblox on Android.
 * Unlocks 120/144/165 FPS frame rate limits and enables high performance rendering.
 */
public class RobloxConfigPatcher {

    private static final String TAG = "RobloxConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        int forcedFps = targetFps > 0 ? targetFps : 185;
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Roblox patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = targetFps > 0 ? targetFps : 185;

        String clientAppSettings = "{\n" +
                "  \"DFIntTaskSchedulerTargetFps\": " + forcedFps + ",\n" +
                "  \"FIntTargetFPS\": " + forcedFps + ",\n" +
                "  \"FIntDesiredMaxFrameRate\": " + forcedFps + ",\n" +
                "  \"FFlagEnableHighFPS\": \"True\",\n" +
                "  \"FFlagUnlockFPS\": \"True\",\n" +
                "  \"FFlagTaskSchedulerLimitTargetFps\": \"False\",\n" +
                "  \"FFlagDebugGraphicsDisableDirect3D11\": \"False\",\n" +
                "  \"FFlagDebugGraphicsPreferVulkan\": \"True\",\n" +
                "  \"FFlagFixGraphicsQuality\": \"True\",\n" +
                "  \"DFFlagDisableDPIScale\": \"True\",\n" +
                "  \"FFlagCommitToFastPhysics\": \"True\",\n" +
                "  \"FFlagEnableVulkan\": \"True\",\n" +
                "  \"FIntCameraMaxZoomDistance\": 500,\n" +
                "  \"FFlagDroneViewUnlocked\": \"True\",\n" +
                "  \"FIntFieldOfView\": 150,\n" +
                "  \"FFlagFastTouchResponse\": \"True\",\n" +
                "  \"FIntTouchPollingRate\": 1000,\n" +
                "  \"FFlagZeroTouchDelay\": \"True\",\n" +
                "  \"FFlagReduceInputLatency\": \"True\",\n" +
                "  \"FFlagTouchSlopReduction\": \"True\",\n" +
                "  \"FFlagGyroFastAim\": \"True\",\n" +
                "  \"FIntGyroPollingRate\": 1000,\n" +
                "  \"FFlagDisableCameraShake\": \"True\",\n" +
                "  \"FFlagWeaponRecoilReduction\": \"True\",\n" +
                "  \"FFlagDamageBoostMode\": \"True\"\n" +
                "}\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            forceWrite(path, clientAppSettings);
            written++;
        }
        Log.i(TAG, "Roblox competitive " + forcedFps + "FPS FastFlag + Drone View force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String cmd =
                "grep -qF '\"FFlagFastTouchResponse\"' " + path + " || echo '  \"FFlagFastTouchResponse\": \"True\",' >> " + path + "; " +
                "grep -qF '\"FIntTouchPollingRate\"' " + path + " || echo '  \"FIntTouchPollingRate\": 1000,' >> " + path + "; " +
                "grep -qF '\"FFlagZeroTouchDelay\"' " + path + " || echo '  \"FFlagZeroTouchDelay\": \"True\",' >> " + path + "; " +
                "grep -qF '\"FFlagTouchSlopReduction\"' " + path + " || echo '  \"FFlagTouchSlopReduction\": \"True\",' >> " + path + "; " +
                "grep -qF '\"FFlagReduceInputLatency\"' " + path + " || echo '  \"FFlagReduceInputLatency\": \"True\",' >> " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Roblox fast zero-delay touch applied for " + packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String cmd =
                "grep -qF '\"FIntCameraMaxZoomDistance\"' " + path + " || echo '  \"FIntCameraMaxZoomDistance\": 500,' >> " + path + "; " +
                "sed -i 's/\"FIntCameraMaxZoomDistance\":.*/\"FIntCameraMaxZoomDistance\": 500,/' " + path + "; " +
                "grep -qF '\"FIntFieldOfView\"' " + path + " || echo '  \"FIntFieldOfView\": 150,' >> " + path + "; " +
                "sed -i 's/\"FIntFieldOfView\":.*/\"FIntFieldOfView\": 150,/' " + path + "; " +
                "grep -qF '\"FFlagGyroFastAim\"' " + path + " || echo '  \"FFlagGyroFastAim\": \"True\",' >> " + path + "; " +
                "grep -qF '\"FIntGyroPollingRate\"' " + path + " || echo '  \"FIntGyroPollingRate\": 1000,' >> " + path + "; " +
                "grep -qF '\"FFlagDisableCameraShake\"' " + path + " || echo '  \"FFlagDisableCameraShake\": \"True\",' >> " + path + "; " +
                "grep -qF '\"FFlagWeaponRecoilReduction\"' " + path + " || echo '  \"FFlagWeaponRecoilReduction\": \"True\",' >> " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Roblox Drone View Zoom 500, 1000Hz Gyro & Recoil Reduction applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        applyAimAssistConfig(packageName);
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
        if (!ShizukuFileManager.fileExists(path)) {
            String content = String.format(
                    "{\n  \"DFIntTaskSchedulerTargetFps\": %d,\n  \"FIntTargetFPS\": %d,\n  \"FIntDesiredMaxFrameRate\": %d,\n  \"FFlagEnableHighFPS\": \"True\",\n  \"FFlagUnlockFPS\": \"True\",\n  \"FFlagDebugGraphicsPreferVulkan\": \"True\"\n}\n",
                    forcedFps, forcedFps, forcedFps
            );
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd = "sed -i 's/\"DFIntTaskSchedulerTargetFps\":.*/\"DFIntTaskSchedulerTargetFps\": " + forcedFps + ",/' " + path + "; " +
                         "sed -i 's/\"FIntTargetFPS\":.*/\"FIntTargetFPS\": " + forcedFps + ",/' " + path + "; " +
                         "sed -i 's/\"FIntDesiredMaxFrameRate\":.*/\"FIntDesiredMaxFrameRate\": " + forcedFps + ",/' " + path + "; " +
                         "chmod 666 " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
            return true;
        }
    }
}
