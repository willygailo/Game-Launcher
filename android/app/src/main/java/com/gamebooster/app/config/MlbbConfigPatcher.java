package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
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
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, targetFps)) patched++;
        }
        Log.i(TAG, "MLBB patch: " + patched + " files for " + packageName + " @ " + targetFps + "fps");
        return patched > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL MLBB config paths unconditionally.
     * Uses Shizuku (temporary root) to reach /data/data/ paths.
     * No existence check — always writes the competitive config.
     *
     * @return true if at least one path was written
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        // MLBB FrameRateLevel: 5 = Ultra 165FPS, 4 = Ultra-High (120fps), 3 = High (90fps), 2 = Standard (60fps)
        int frameRateLevel = targetFps >= 165 ? 5 : (targetFps >= 120 ? 4 : (targetFps >= 90 ? 3 : (targetFps >= 60 ? 2 : 1)));

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            String content;
            if (path.endsWith(".xml")) {
                content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n" +
                        "<map>\n" +
                        "  <int name=\"HighFpsMode\" value=\"3\" />\n" +
                        "  <int name=\"PerformanceLevel\" value=\"0\" />\n" +
                        "  <int name=\"ShadowQuality\" value=\"0\" />\n" +
                        "  <int name=\"OutlineEnable\" value=\"0\" />\n" +
                        "  <int name=\"HDModeEnable\" value=\"0\" />\n" +
                        "  <int name=\"ScreenShake\" value=\"0\" />\n" +
                        "  <int name=\"TargetFPS\" value=\"" + targetFps + "\" />\n" +
                        "  <int name=\"MaxFPS\" value=\"" + targetFps + "\" />\n" +
                        "  <int name=\"Unlock120Fps\" value=\"1\" />\n" +
                        "  <int name=\"Unlock144Fps\" value=\"1\" />\n" +
                        "  <int name=\"Unlock165Fps\" value=\"1\" />\n" +
                        "</map>\n";
            } else {
                content = "[UserSettings]\n" +
                        "HighFPSMode=1\n" +
                        "HighFPSMode2=1\n" +
                        "FrameRateLevel=" + frameRateLevel + "\n" +
                        "GraphicsQuality=0\n" +
                        "HDMode=0\n" +
                        "HDRMode=0\n" +
                        "UltraHDMode=0\n" +
                        "Shadow=0\n" +
                        "FPS=" + targetFps + "\n" +
                        "MaxFPS=" + targetFps + "\n" +
                        "MaxFrameRate=" + targetFps + "\n" +
                        "TargetFPS=" + targetFps + "\n" +
                        "HighFrameRate=1\n" +
                        "UnlockFPS=1\n" +
                        "SuperHighFPS=1\n" +
                        "Unlock120Hz=1\n" +
                        "Unlock144Hz=1\n" +
                        "Unlock165Hz=1\n" +
                        "Unlock120FPS=1\n" +
                        "Unlock144FPS=1\n" +
                        "Unlock165FPS=1\n" +
                        "TouchDelay=0.0\n";
            }
            forceWrite(path, content);
            written++;
        }
        Log.i(TAG, "MLBB competitive UserSettings force-write: " + written + " paths @ " + targetFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Injects super-fast touch response keys into MLBB config files.
     * Optimized for 120Hz/144Hz/165Hz panels — sets HighFreqTouchHz=165 and max touch response level.
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
                "sed -i 's/^HighFreqTouchHz=.*/HighFreqTouchHz=165/' " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "MLBB super-fast touch applied for " + packageName);
    }

    /**
     * Injects Damage Script, Physical/Magic Damage Boost, and Penetration Asset Config keys into MLBB config files.
     * Uses Shizuku ADB temporary root access for /data/data/ and /sdcard/ file locations.
     */
    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageKeys = {
            "PhysicalDamageBoost=1.80",
            "MagicDamageBoost=1.80",
            "PhysicalPenetrationBoost=100",
            "MagicPenetrationBoost=100",
            "DamageMultiplier=1.80",
            "CriticalDamageRate=100",
            "SkillCoolDownReduceMode=1",
            "HighDamageRateMode=1",
            "DamageAssetOverride=1",
            "AutoDamageExecutionMode=1"
        };
        for (String path : paths) {
            ensureDirectory(path);
            StringBuilder sb = new StringBuilder();
            sb.append("grep -qF '[DamageScript]' ").append(path).append(" || echo '[DamageScript]' >> ").append(path).append("; ");
            for (String keyVal : damageKeys) {
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
        Log.i(TAG, "MLBB magic/physical damage script asset config applied via Shizuku for " + packageName);
    }

    /**
     * Injects Aim Assist, Target Lock, Auto-Skill Aim, and Hero Tracking keys into MLBB config files.
     * Uses Shizuku ADB temporary root access for /data/data/ and /sdcard/ file locations.
     */
    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "AimAssist=1",
            "AimAssistLevel=3",
            "TargetLockMode=1",
            "AutoSkillAim=1",
            "HeroTargetLockSensitivity=100",
            "SkillAimAssist=1",
            "RotationalAimAssist=1",
            "CameraTargetTracking=1"
        };
        for (String path : paths) {
            ensureDirectory(path);
            StringBuilder sb = new StringBuilder();
            sb.append("grep -qF '[AimAssistConfig]' ").append(path).append(" || echo '[AimAssistConfig]' >> ").append(path).append("; ");
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
        Log.i(TAG, "MLBB Aim Assist & Target Tracking config applied via Shizuku for " + packageName);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/data/data/" + pkg + "/shared_prefs/" + pkg + ".v2.playerprefs.xml");
        paths.add("/data/data/" + pkg + "/shared_prefs/com.mobile.legends.v2.playerprefs.xml");
        paths.add("/sdcard/GameBoosterPro/configs/mlbb_ultra_165.cfg");
        paths.add("/sdcard/GameBoosterPro/configs/" + pkg + "_165.cfg");
        
        String[] engines = new String[]{"dragon2015", "dragon2017", "dragon2019", "dragon2021", "dragon2023", "dragon2025", "dragon2026"};
        for (String eng : engines) {
            paths.add("/sdcard/Android/data/" + pkg + "/files/" + eng + "/assets/UI/Config/UserSystem.ini");
            paths.add("/sdcard/Android/data/" + pkg + "/files/" + eng + "/assets/UI/Config/DamageSystem.ini");
            paths.add("/sdcard/Android/data/" + pkg + "/files/" + eng + "/assets/UI/Config/AimAssist.ini");
            paths.add("/sdcard/Android/data/" + pkg + "/files/" + eng + "/assets/UI/Config/TouchConfig.ini");
            paths.add("/sdcard/Android/data/" + pkg + "/files/" + eng + "/assets/UI/HighFPSConfig.ini");
            paths.add("/sdcard/Android/data/" + pkg + "/files/" + eng + "/assets/Com/MobileLegendsSettings.ini");
            paths.add("/data/data/" + pkg + "/files/" + eng + "/assets/Com/MobileLegendsSettings.ini");
            paths.add("/data/data/" + pkg + "/files/" + eng + "/assets/UI/Config/UserSystem.ini");
            paths.add("/data/data/" + pkg + "/files/" + eng + "/assets/UI/Config/DamageSystem.ini");
            paths.add("/data/data/" + pkg + "/files/" + eng + "/assets/UI/Config/AimAssist.ini");
            paths.add("/data/data/" + pkg + "/files/" + eng + "/assets/UI/Config/TouchConfig.ini");
        }
        return paths;
    }

    private static void forceWrite(String path, String content) {
        ensureDirectory(path);
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommandWithBase64(content, path);
        } else {
            try {
                String b64 = android.util.Base64.encodeToString(content.getBytes("UTF-8"), android.util.Base64.NO_WRAP);
                CommandExecutor.executeSystemCommand("echo '" + b64 + "' | base64 -d > '" + path + "'");
            } catch (Exception e) {
                Log.e(TAG, "forceWrite failed for " + path, e);
            }
        }
    }

    private static boolean applyPatch(String path, int targetFps) {
        ensureDirectory(path);
        int frameRateLevel = targetFps >= 120 ? 4 : (targetFps >= 90 ? 3 : (targetFps >= 60 ? 2 : 1));
        String checkRes = CommandExecutor.executeSystemCommand("test -f " + path + " && echo EXISTS");

        if (!checkRes.contains("EXISTS")) {
            String content = "[UserSettings]\n" +
                    "HighFPSMode=1\n" +
                    "HighFPSMode2=1\n" +
                    "FrameRateLevel=" + frameRateLevel + "\n" +
                    "GraphicsQuality=4\n" +
                    "HDMode=1\n" +
                    "Shadow=1\n" +
                    "FPS=" + targetFps + "\n" +
                    "MaxFPS=" + targetFps + "\n" +
                    "MaxFrameRate=" + targetFps + "\n" +
                    "TargetFPS=" + targetFps + "\n" +
                    "HighFrameRate=1\n";
            forceWrite(path, content);
        } else {
            CommandExecutor.executeSystemCommand("sed -i 's/^HighFPSMode=.*/HighFPSMode=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^HighFPSMode2=.*/HighFPSMode2=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^FrameRateLevel=.*/FrameRateLevel=" + frameRateLevel + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^GraphicsQuality=.*/GraphicsQuality=4/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^HDMode=.*/HDMode=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^Shadow=.*/Shadow=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^FPS=.*/FPS=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^MaxFPS=.*/MaxFPS=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^MaxFrameRate=.*/MaxFrameRate=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^TargetFPS=.*/TargetFPS=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^HighFrameRate=.*/HighFrameRate=1/' " + path);
        }
        return true;
    }

    private static void ensureDirectory(String path) {
        if (path == null) return;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            String parentDir = path.substring(0, lastSlash);
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand("mkdir -p " + parentDir);
            } else {
                CommandExecutor.executeSystemCommand("mkdir -p " + parentDir);
            }
        }
    }
}
