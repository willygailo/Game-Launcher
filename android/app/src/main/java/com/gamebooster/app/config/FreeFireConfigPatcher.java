package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import java.util.ArrayList;
import java.util.List;

/**
 * FreeFireConfigPatcher manages internal config files for Garena Free Fire and Free Fire MAX.
 * Unlocks 120/144/165 FPS, high-frequency touch, and max graphic presets.
 */
public class FreeFireConfigPatcher {

    private static final String TAG = "FreeFireConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = 185; // hard-locked
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "FreeFire patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = 185; // hard-locked
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String content = "[FFGraphics]\n" +
                "HighFPS=1\n" +
                "HighFPSMode=1\n" +
                "FPSMode=2\n" +
                "FrameRateLevel=" + frameRateLevel + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "GraphicLevel=3\n" +
                "Shadow=1\n" +
                "HighResolution=1\n" +
                "VulkanEnabled=1\n" +
                "Unlock120Hz=1\n" +
                "Unlock144Hz=1\n" +
                "Unlock165Hz=1\n" +
                "Unlock185Hz=1\n" +
                "AimAssist=1\n" +
                "AutoAimPrecision=1.0\n" +
                "SprintSensitivity=150\n" +
                "GeneralSensitivity=100\n" +
                "RedDotSensitivity=100\n" +
                "TPPFov=100\n" +
                "FPPFov=150\n" +
                "NoRecoil=1\n" +
                "RecoilReduction=1.00\n" +
                "AllWeaponRecoilFix=1\n" +
                "ScopeStabilization=1\n" +
                "Scope2xRecoil=0.00\n" +
                "Scope4xRecoil=0.00\n" +
                "SniperScopeRecoil=0.00\n" +
                "GunShakeReduction=1.00\n" +
                "DamageBoostRatio=1.90\n" +
                "HeadshotDamageMultiplier=2.90\n" +
                "BulletDamageBoost=1.90\n" +
                "CriticalHitRate=95\n" +
                "TouchResponseLevel=3\n" +
                "HighFreqTouchHz=" + forcedFps + "\n" +
                "TouchPollingRate=1000\n" +
                "TouchZeroDelay=1\n" +
                "GyroSampleRate=1000\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            forceWrite(path, content);
            written++;
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "FreeFire competitive " + forcedFps + "FPS force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Applies anti-log, report cleaner, and telemetry suppression for Free Fire.
     */
    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String cmd =
                "grep -qF 'TouchResponseLevel' " + path + " || echo 'TouchResponseLevel=3' >> " + path + "; " +
                "sed -i 's/^TouchResponseLevel=.*/TouchResponseLevel=3/' " + path + "; " +
                "grep -qF 'HighFreqTouchHz' " + path + " || echo 'HighFreqTouchHz=185' >> " + path + "; " +
                "sed -i 's/^HighFreqTouchHz=.*/HighFreqTouchHz=185/' " + path + "; " +
                "grep -qF 'TouchPollingRate' " + path + " || echo 'TouchPollingRate=1000' >> " + path + "; " +
                "sed -i 's/^TouchPollingRate=.*/TouchPollingRate=1000/' " + path + "; " +
                "grep -qF 'TouchZeroDelay' " + path + " || echo 'TouchZeroDelay=1' >> " + path + "; " +
                "sed -i 's/^TouchZeroDelay=.*/TouchZeroDelay=1/' " + path + "; " +
                "grep -qF 'TouchSlopReduction' " + path + " || echo 'TouchSlopReduction=1' >> " + path + "; " +
                "sed -i 's/^TouchSlopReduction=.*/TouchSlopReduction=1/' " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "FreeFire super-fast zero-delay touch applied for " + packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "AimAssist=1",
            "AutoAimPrecision=1.0",
            "HeadshotSensitivityBoost=2.0",
            "DragShotAssist=1",
            "SprintSensitivity=150",
            "GeneralSensitivity=100",
            "RedDotSensitivity=100",
            "TPPFov=100",
            "FPPFov=150",
            "TouchSlopReduction=1",
            "DamageBoostRatio=1.90",
            "HeadshotDamageMultiplier=2.90",
            "BulletDamageBoost=1.90",
            "CriticalHitRate=95",
            "GyroSensitivityBoost=2.0",
            "GyroZeroDelay=1",
            "GyroResponseRate=1000",
            "GyroAimAssist=1",
            "NoRecoil=1",
            "RecoilReduction=1.00",
            "AllWeaponRecoilFix=1",
            "ScopeStabilization=1",
            "Scope2xRecoil=0.00",
            "Scope4xRecoil=0.00",
            "SniperScopeRecoil=0.00",
            "GunShakeReduction=1.00",
            "CrosshairSpread=0.00"
        };
        for (String path : paths) {
            StringBuilder sb = new StringBuilder();
            sb.append("grep -qF '[AimControl]' ").append(path).append(" || echo '[AimControl]' >> ").append(path).append("; ");
            for (String keyVal : aimKeys) {
                String k = keyVal.substring(0, keyVal.indexOf("="));
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/^").append(k).append("=.*/").append(keyVal).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "FreeFire Aim Assist 100%, TPP 100, FPP 150, Sprint 150, 1000Hz Gyro, 90+ Damage & Zero Recoil applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        applyAimAssistConfig(packageName);
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    private static void forceWrite(String path, String content) {
        ShizukuFileManager.writeFile(path, content, "666");
    }

    private static boolean applyPatch(String path, int targetFps) {
        int forcedFps = 185; // hard-locked
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;
        if (!ShizukuFileManager.fileExists(path)) {
            String content = String.format(
                    "[FFGraphics]\nHighFPS=1\nHighFPSMode=1\nFPSMode=2\nFrameRateLevel=%d\nMaxFPS=%d\nTargetFPS=%d\nUnlockFPS=1\nSuperHighFPS=1\nUnlock120Hz=1\nUnlock144Hz=1\nUnlock165Hz=1\nUnlock185Hz=1\nGraphicLevel=3\nHighFreqTouchHz=%d\n",
                    frameRateLevel, forcedFps, forcedFps, forcedFps
            );
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd = "sed -i 's/^HighFPS=.*/HighFPS=1/' " + path + "; " +
                         "sed -i 's/^HighFPSMode=.*/HighFPSMode=1/' " + path + "; " +
                         "sed -i 's/^FPSMode=.*/FPSMode=2/' " + path + "; " +
                         "sed -i 's/^FrameRateLevel=.*/FrameRateLevel=" + frameRateLevel + "/' " + path + "; " +
                         "sed -i 's/^MaxFPS=.*/MaxFPS=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^TargetFPS=.*/TargetFPS=" + forcedFps + "/' " + path + "; " +
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
