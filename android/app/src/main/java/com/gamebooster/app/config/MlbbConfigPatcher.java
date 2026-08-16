package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import java.util.ArrayList;
import java.util.List;

/**
 * MlbbConfigPatcher manages internal config files for Mobile Legends: Bang Bang (all versions).
 *
 * Two patching modes:
 *  - patch()            → standard patch: create-if-missing or sed-update
 *  - patchCompetitive() → competitive force-write: ALWAYS overwrites all paths, no fallback,
 *                         executed via Shizuku for full data/data access (temporary root)
 */
public class MlbbConfigPatcher {

    private static final String TAG = "MlbbConfigPatcher";

    // ─── Standard Patch ───────────────────────────────────────────────────────

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        int forcedFps = targetFps > 0 ? targetFps : 185;
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "MLBB patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL MLBB config paths unconditionally.
     * Uses Shizuku (temporary root) to reach /data/data/ paths.
     * Locks 120 / 144 / 165 / 185 FPS and Ultra-High FrameRateLevel (10=185fps, 9=165fps, 8=144fps, 7=120fps).
     *
     * @return true if at least one path was written
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = targetFps > 0 ? targetFps : 185;
        final int frameRateLevel = forcedFps >= 185 ? 10 : (forcedFps >= 165 ? 9 : (forcedFps >= 144 ? 8 : (forcedFps >= 120 ? 7 : (forcedFps >= 90 ? 6 : 5))));

        String content = "[Graphics]\n" +
                "HighFPSMode=1\n" +
                "FrameRateLevel=" + frameRateLevel + "\n" +
                "GraphicsQuality=4\n" +
                "HDMode=1\n" +
                "HDRMode=1\n" +
                "UltraHDMode=1\n" +
                "Shadow=1\n" +
                "FPS=" + forcedFps + "\n" +
                "MaxFrameRate=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "HighFrameRate=1\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "Unlock185Hz=1\n" +
                "Unlock165Hz=1\n" +
                "DroneView=1\n" +
                "DroneViewHeight=3\n" +
                "CameraHeight=3\n" +
                "CameraDistance=150\n" +
                "CameraFOV=150\n" +
                "WideScreenMode=1\n" +
                "FieldOfView=150\n" +
                "HighFreqTouchHz=" + forcedFps + "\n" +
                "TouchPollingRate=1000\n" +
                "TouchZeroDelay=1\n" +
                "TouchResponseLevel=3\n" +
                "PhysicalDamageBoost=1.90\n" +
                "MagicDamageBoost=1.90\n" +
                "TrueDamageBoost=1.90\n" +
                "DamageMultiplier=1.90\n" +
                "CriticalDamageRate=95\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            forceWrite(path, content);
            written++;
        }
        Log.i(TAG, "MLBB competitive HDR " + forcedFps + "FPS + Drone View force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Injects super-fast zero-delay touch response keys into MLBB config files.
     * Optimized for 165Hz panels — sets HighFreqTouchHz=165, TouchPollingRate=1000, TouchZeroDelay=1, and max touch response level.
     */
    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String cmd =
                "grep -qF 'HighFreqTouch' " + path + " || echo 'HighFreqTouch=1' >> " + path + "; " +
                "sed -i 's/^HighFreqTouch=.*/HighFreqTouch=1/' " + path + "; " +
                "grep -qF 'TouchResponseLevel' " + path + " || echo 'TouchResponseLevel=3' >> " + path + "; " +
                "sed -i 's/^TouchResponseLevel=.*/TouchResponseLevel=3/' " + path + "; " +
                "grep -qF 'HighFreqTouchHz' " + path + " || echo 'HighFreqTouchHz=165' >> " + path + "; " +
                "sed -i 's/^HighFreqTouchHz=.*/HighFreqTouchHz=165/' " + path + "; " +
                "grep -qF 'TouchPollingRate' " + path + " || echo 'TouchPollingRate=1000' >> " + path + "; " +
                "sed -i 's/^TouchPollingRate=.*/TouchPollingRate=1000/' " + path + "; " +
                "grep -qF 'TouchZeroDelay' " + path + " || echo 'TouchZeroDelay=1' >> " + path + "; " +
                "sed -i 's/^TouchZeroDelay=.*/TouchZeroDelay=1/' " + path + "; " +
                "grep -qF 'TouchLatencyReduction' " + path + " || echo 'TouchLatencyReduction=1' >> " + path + "; " +
                "sed -i 's/^TouchLatencyReduction=.*/TouchLatencyReduction=1/' " + path + "; " +
                "grep -qF 'ZeroInputLag' " + path + " || echo 'ZeroInputLag=1' >> " + path + "; " +
                "sed -i 's/^ZeroInputLag=.*/ZeroInputLag=1/' " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "MLBB super-fast zero-delay touch applied for " + packageName);
    }

    /**
     * Injects Drone View (Camera Height / FOV 150), Damage Script 90+, Physical/Magic/True Damage Boost, and Penetration keys into MLBB config files.
     * Uses Shizuku ADB temporary root access for /data/data/ and /sdcard/ file locations.
     */
    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageDroneKeys = {
            "DroneView=1",
            "DroneViewHeight=3",
            "CameraHeight=3",
            "CameraDistance=150",
            "CameraFOV=150",
            "FieldOfView=150",
            "WideScreenMode=1",
            "UltraWideCamera=1",
            "PhysicalDamageBoost=1.90",
            "MagicDamageBoost=1.90",
            "TrueDamageBoost=1.90",
            "PhysicalPenetrationBoost=95",
            "MagicPenetrationBoost=95",
            "DamageMultiplier=1.90",
            "CriticalDamageRate=95",
            "CriticalDamageMultiplier=2.90",
            "SkillCoolDownReduceMode=1",
            "HighDamageRateMode=1",
            "DamageAssetOverride=1",
            "AutoDamageExecutionMode=1",
            "AutoSkillLock=1",
            "SkillTargetAssist=1",
            "SmartTargetingMode=1"
        };
        for (String path : paths) {
            ensureDirectory(path);
            StringBuilder sb = new StringBuilder();
            sb.append("grep -qF '[DamageScript]' ").append(path).append(" || echo '[DamageScript]' >> ").append(path).append("; ");
            sb.append("grep -qF '[CameraConfig]' ").append(path).append(" || echo '[CameraConfig]' >> ").append(path).append("; ");
            for (String keyVal : damageDroneKeys) {
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
        Log.i(TAG, "MLBB Drone View FOV 150 & 90+ damage script config applied via Shizuku for " + packageName);
    }


    // ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/Config/UserSystem.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/Config/DamageSystem.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/HighFPSConfig.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/Com/MobileLegendsSettings.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/Config/HighFPS.xml");
        paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/Config/Performance.xml");
        paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/Config/Setting.xml");
        paths.add("/sdcard/Android/data/" + pkg + "/files/" + pkg + ".v2.playerprefs.xml");
        paths.add("/data/data/" + pkg + "/shared_prefs/" + pkg + ".v2.playerprefs.xml");
        paths.add("/data/data/" + pkg + "/shared_prefs/com.mobile.legends.v2.playerprefs.xml");
        paths.add("/data/data/" + pkg + "/files/dragon2017/assets/Com/MobileLegendsSettings.ini");
        paths.add("/data/data/" + pkg + "/files/dragon2017/assets/UI/Config/UserSystem.ini");
        paths.add("/data/data/" + pkg + "/files/dragon2017/assets/UI/Config/DamageSystem.ini");

        // Deep Search discovered paths via Shizuku
        if (ShizukuExecutor.hasShizukuPermission()) {
            try {
                String cmd = "find /sdcard/Android/data/" + pkg + "/files/ /data/data/" + pkg + "/files/ /data/data/" + pkg + "/shared_prefs/ -type f \\( -name \"*.ini\" -o -name \"*.xml\" \\) 2>/dev/null";
                String output = ShizukuExecutor.executeShizukuCommand(cmd);
                if (output != null && !output.isEmpty()) {
                    for (String line : output.split("\n")) {
                        line = line.trim();
                        if (!line.isEmpty() && !paths.contains(line)) {
                            paths.add(line);
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }
        return paths;
    }

    private static void forceWrite(String path, String content) {
        ShizukuFileManager.writeFile(path, content, "666");
    }

    private static boolean applyPatch(String path, int targetFps) {
        final int forcedFps = targetFps > 0 ? targetFps : 185;
        final int frameRateLevel = forcedFps >= 185 ? 10 : (forcedFps >= 165 ? 9 : (forcedFps >= 144 ? 8 : (forcedFps >= 120 ? 7 : (forcedFps >= 90 ? 6 : 5))));
        if (!ShizukuFileManager.fileExists(path)) {
            String content = String.format(
                    "[Graphics]\nHighFPSMode=1\nFrameRateLevel=%d\nGraphicsQuality=4\nHDMode=1\nShadow=1\nFPS=%d\nMaxFrameRate=%d\nTargetFPS=%d\nHighFrameRate=1\nHighFreqTouchHz=%d\n",
                    frameRateLevel, forcedFps, forcedFps, forcedFps, forcedFps
            );
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd = "sed -i 's/^HighFPSMode=.*/HighFPSMode=1/' " + path + "; " +
                         "sed -i 's/^FrameRateLevel=.*/FrameRateLevel=" + frameRateLevel + "/' " + path + "; " +
                         "sed -i 's/^GraphicsQuality=.*/GraphicsQuality=4/' " + path + "; " +
                         "sed -i 's/^HDMode=.*/HDMode=1/' " + path + "; " +
                         "sed -i 's/^Shadow=.*/Shadow=1/' " + path + "; " +
                         "sed -i 's/^FPS=.*/FPS=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^MaxFrameRate=.*/MaxFrameRate=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^TargetFPS=.*/TargetFPS=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^HighFrameRate=.*/HighFrameRate=1/' " + path + "; " +
                         "chmod 666 " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
            return true;
        }
    }

    private static void ensureDirectory(String path) {
        ShizukuFileManager.ensureParentDirectory(path);
    }
}
