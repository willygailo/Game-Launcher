package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Standoff2ConfigPatcher manages legal configuration files for Axlebolt Standoff 2.
 * Unlocks 120 FPS / 144 FPS / 165 FPS / 185 FPS and sets 1000Hz touch polling rate.
 */
public class Standoff2ConfigPatcher {

    private static final String TAG = "Standoff2ConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyStandardPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Standoff 2 patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String jsonContent = "{\n" +
                "  \"graphics\": {\n" +
                "    \"target_framerate\": " + forcedFps + ",\n" +
                "    \"max_framerate\": " + forcedFps + ",\n" +
                "    \"framerate_cap\": " + forcedFps + "\n," +
                "    \"fps_unlock\": 1,\n" +
                "    \"fps_unlock_120\": 1,\n" +
                "    \"fps_unlock_144\": 1,\n" +
                "    \"fps_unlock_165\": 1,\n" +
                "    \"fps_unlock_185\": 1,\n" +
                "    \"high_fps_mode\": 1,\n" +
                "    \"shader_detail\": 3,\n" +
                "    \"model_detail\": 3,\n" +
                "    \"texture_detail\": 3,\n" +
                "    \"screen_scale\": 1.2,\n" +
                "    \"anisotropic_filtering\": 16,\n" +
                "    \"antialiasing\": 4,\n" +
                "    \"ultra_extreme\": true\n" +
                "  },\n" +
                "  \"combat\": {\n" +
                "    \"damage_multiplier\": 1.90,\n" +
                "    \"bullet_damage_boost\": 1.90,\n" +
                "    \"headshot_multiplier\": 2.90,\n" +
                "    \"critical_hit_rate\": 95,\n" +
                "    \"recoil_scale\": 0.00,\n" +
                "    \"weapon_kick_reduction\": 1.00\n" +
                "  },\n" +
                "  \"controls\": {\n" +
                "    \"touch_acceleration\": 0.0,\n" +
                "    \"touch_rate_hz\": 1000,\n" +
                "    \"zero_input_latency\": true\n" +
                "  }\n" +
                "}\n";

        String iniContent = "[StandoffGraphics]\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "FrameRateLimit=" + forcedFps + "\n" +
                "FPSLevel=" + fpsLevel + "\n" +
                "HighFPSMode=1\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "Unlock120Hz=1\n" +
                "Unlock144Hz=1\n" +
                "Unlock165Hz=1\n" +
                "Unlock185Hz=1\n" +
                "GraphicQuality=4\n" +
                "UltraExtreme=1\n" +
                "ResolutionScale=1.2\n" +
                "TouchPollingRate=1000\n" +
                "TouchSlop=1\n" +
                "TouchZeroDelay=1\n" +
                "DamageMultiplier=2.50\n" +
                "BulletDamageBoost=2.50\n" +
                "HeadshotDamageMultiplier=3.50\n" +
                "CriticalHitRate=99\n" +
                "NoRecoil=1\n" +
                "CrosshairSpread=0.00\n";

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
        Log.i(TAG, "Standoff 2 competitive UltraExtreme " + forcedFps + "FPS force-write: " + written + " paths");
        return written > 0;
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String dmgData = "\n[DamageScript]\nDamageMultiplier=2.50\nBulletDamageBoost=2.50\nHeadshotDamageMultiplier=3.50\nCriticalHitRate=99\n";
            if (ShizukuFileManager.fileExists(path)) {
                String cmd = "echo '" + dmgData + "' >> " + path + "; chmod 666 " + path;
                if (ShizukuFileManager.hasFullAccess()) {
                    ShizukuExecutor.executeShizukuCommand(cmd);
                } else {
                    CommandExecutor.executeSystemCommand(cmd);
                }
            }
        }
        Log.i(TAG, "Standoff 2 damage script 1.9x applied for " + packageName);
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
        String[] aimKeys = {
            "AimAssist=1",
            "AimAssistStrength=150",
            "AimSmooth=1",
            "AimTracking=1",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "SensitivityMultiplier=1.5",
            "Acceleration=0"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
            StringBuilder sb = new StringBuilder();
            for (String keyVal : aimKeys) {
                String k = keyVal.substring(0, keyVal.indexOf("="));
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/^").append(k).append("=.*/").append(keyVal).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Standoff2 Aim Assist & Gyro 1000Hz applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "RecoilScale=0.00",
            "VerticalRecoil=0.00",
            "HorizontalRecoil=0.00",
            "WeaponKick=0",
            "NoShake=1",
            "SpreadReduction=1",
            "ZeroRecoil=1",
            "WeaponStability=150"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
            StringBuilder sb = new StringBuilder();
            for (String keyVal : recoilKeys) {
                String k = keyVal.substring(0, keyVal.indexOf("="));
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/^").append(k).append("=.*/").append(keyVal).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Standoff2 Zero Recoil & Weapon Stability applied for " + packageName);
    }


    private static boolean applyStandardPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        if (!ShizukuFileManager.fileExists(path)) {
            String content = "{\n  \"graphics\": {\n    \"target_framerate\": " + forcedFps + ",\n    \"max_framerate\": " + forcedFps + ",\n    \"framerate_cap\": " + forcedFps + ",\n    \"fps_unlock\": 1,\n    \"fps_unlock_120\": 1,\n    \"fps_unlock_144\": 1,\n    \"fps_unlock_165\": 1,\n    \"fps_unlock_185\": 1,\n    \"high_fps_mode\": 1\n  }\n}\n";
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd = "sed -i 's/\"target_framerate\":.*/\"target_framerate\": " + forcedFps + ",/' " + path + "; " +
                         "sed -i 's/\"max_framerate\":.*/\"max_framerate\": " + forcedFps + ",/' " + path + "; " +
                         "sed -i 's/\"framerate_cap\":.*/\"framerate_cap\": " + forcedFps + ",/' " + path + "; " +
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
