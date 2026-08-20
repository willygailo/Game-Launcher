package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * WildRiftConfigPatcher manages legal configuration files for League of Legends: Wild Rift (all regions).
 * Unlocks 90 FPS / 120 FPS / 144 FPS / 165 FPS / 185 FPS and 1000Hz touch input response.
 */
public class WildRiftConfigPatcher {

    private static final String TAG = "WildRiftConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyStandardPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Wild Rift patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String jsonContent = "{\n" +
                "  \"graphics\": {\n" +
                "    \"target_fps\": " + forcedFps + ",\n" +
                "    \"max_fps\": " + forcedFps + ",\n" +
                "    \"fps_level\": " + fpsLevel + ",\n" +
                "    \"fpsUnlock\": true,\n" +
                "    \"fps_unlock\": true,\n" +
                "    \"unlock_120\": true,\n" +
                "    \"unlock_144\": true,\n" +
                "    \"unlock_165\": true,\n" +
                "    \"unlock_185\": true,\n" +
                "    \"resolution\": 4,\n" +
                "    \"quality\": 4,\n" +
                "    \"character_quality\": 4,\n" +
                "    \"effects_quality\": 4,\n" +
                "    \"shadow_quality\": 4,\n" +
                "    \"ultra_extreme\": true,\n" +
                "    \"resolution_scale\": 1.2,\n" +
                "    \"post_processing\": true,\n" +
                "    \"vsync\": false\n" +
                "  },\n" +
                "  \"combat\": {\n" +
                "    \"physical_damage_boost\": 1.90,\n" +
                "    \"magic_damage_boost\": 1.90,\n" +
                "    \"true_damage_boost\": 1.90,\n" +
                "    \"critical_damage_rate\": 95,\n" +
                "    \"drone_view\": true,\n" +
                "    \"camera_fov\": 150,\n" +
                "    \"camera_distance\": 150\n" +
                "  },\n" +
                "  \"input\": {\n" +
                "    \"touch_polling_hz\": 1000,\n" +
                "    \"zero_latency_mode\": true\n" +
                "  }\n" +
                "}\n";

        String iniContent = "[WildRiftGraphics]\n" +
                "FPSLevel=" + fpsLevel + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "FPS=" + forcedFps + "\n" +
                "HighFPSMode=1\n" +
                "UnlockFPS=1\n" +
                "Unlock120=1\n" +
                "Unlock144=1\n" +
                "Unlock165=1\n" +
                "Unlock185=1\n" +
                "GraphicQuality=4\n" +
                "UltraExtreme=1\n" +
                "ResolutionScale=1.2\n" +
                "DroneView=1\n" +
                "CameraFOV=150\n" +
                "CameraDistance=150\n" +
                "PhysicalDamageBoost=1.90\n" +
                "MagicDamageBoost=1.90\n" +
                "TrueDamageBoost=1.90\n" +
                "DamageMultiplier=2.50\n" +
                "CriticalDamageRate=99\n" +
                "TouchPollingRate=1000\n" +
                "TouchSlop=1\n" +
                "TouchZeroDelay=1\n";

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
        Log.i(TAG, "Wild Rift competitive UltraExtreme " + forcedFps + "FPS force-write: " + written + " paths");
        return written > 0;
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String dmgData = "\n[DamageBoost]\nPhysicalDamageBoost=1.90\nMagicDamageBoost=1.90\nTrueDamageBoost=1.90\nDamageMultiplier=2.50\nCriticalDamageRate=99\n";
            if (ShizukuFileManager.fileExists(path)) {
                String cmd = "echo '" + dmgData + "' >> " + path + "; chmod 666 " + path;
                if (ShizukuFileManager.hasFullAccess()) {
                    ShizukuExecutor.executeShizukuCommand(cmd);
                } else {
                    CommandExecutor.executeSystemCommand(cmd);
                }
            }
        }
        Log.i(TAG, "Wild Rift damage boost 1.9x applied for " + packageName);
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
            "SmartTargeting=1",
            "TargetLock=1",
            "SkillTargetAssist=1",
            "AutoSkillAim=1",
            "TouchSensitivity=150"
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
        Log.i(TAG, "WildRift Smart Target Assist applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "InputSmoothing=1",
            "SkillResponseZeroDelay=1",
            "TouchStabilization=1",
            "ZeroInputLag=1"
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
        Log.i(TAG, "WildRift Input Smoothing & Stabilization applied for " + packageName);
    }

    /**
     * Injects Armor Defense, Magic Resistance, Damage Reduction, and Shield Multiplier into Wild Rift.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "PhysicalArmor=2.50",
            "MagicResistance=2.50",
            "DamageReductionRatio=0.50",
            "ShieldMultiplier=2.00",
            "MaxHPMultiplier=1.50",
            "ArmorBoost=150",
            "DamageAbsorbRatio=1.50",
            "TenacityRatio=0.50"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
            StringBuilder sb = new StringBuilder();
            sb.append("grep -qF '[DefenseConfig]' ").append(path).append(" || echo '[DefenseConfig]' >> ").append(path).append("; ");
            for (String keyVal : armorKeys) {
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
        Log.i(TAG, "WildRift Armor Defense & Damage Reduction applied for " + packageName);
    }

    /**
     * Injects Skill Auto-Tracking, Target Lock, Smite Execution, and Skill Magnetism for Wild Rift.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "SkillTracking=1",
            "AutoTargetLock=1",
            "TargetLockTracking=1",
            "AutoSmiteExecution=1",
            "SkillMagnetism=1.50",
            "PredictPath=1",
            "HitboxExpansion=1.50",
            "TrackingBullet=1",
            "BulletTracking=1",
            "MagicBullet=1"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
            StringBuilder sb = new StringBuilder();
            for (String keyVal : trackingKeys) {
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
        Log.i(TAG, "WildRift Skill Auto-Tracking & Smite Execution applied for " + packageName);
    }

    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    private static boolean applyStandardPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;
        if (!ShizukuFileManager.fileExists(path)) {
            String content = "{\n  \"graphics\": {\n    \"target_fps\": " + forcedFps + ",\n    \"max_fps\": " + forcedFps + ",\n    \"fps_level\": " + fpsLevel + ",\n    \"fpsUnlock\": true,\n    \"unlock_120\": true,\n    \"unlock_144\": true,\n    \"unlock_165\": true,\n    \"unlock_185\": true,\n    \"resolution\": 4,\n    \"quality\": 4,\n    \"character_quality\": 4,\n    \"effects_quality\": 4,\n    \"shadow_quality\": 4,\n    \"ultra_extreme\": true,\n    \"resolution_scale\": 1.2,\n    \"post_processing\": true,\n    \"vsync\": false\n  }\n}\n";
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd = "sed -i 's/\"target_fps\":.*/\"target_fps\": " + forcedFps + ",/' " + path + "; " +
                         "sed -i 's/\"max_fps\":.*/\"max_fps\": " + forcedFps + ",/' " + path + "; " +
                         "sed -i 's/\"fps_level\":.*/\"fps_level\": " + fpsLevel + ",/' " + path + "; " +
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
