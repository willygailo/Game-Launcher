package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import java.util.ArrayList;
import java.util.List;

/**
 * CodmConfigPatcher manages internal config files for Call of Duty Mobile (all versions/regions).
 *
 * Two patching modes:
 *  - patch()            → standard patch: create-if-missing or sed-update
 *  - patchCompetitive() → competitive force-write: ALWAYS overwrites all paths, no fallback,
 *                         executed via Shizuku for full data/data access (temporary root)
 */
public class CodmConfigPatcher {

    private static final String TAG = "CodmConfigPatcher";

    // ─── Standard Patch ───────────────────────────────────────────────────────

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, targetFps)) patched++;
        }
        Log.i(TAG, "CODM patch: " + patched + " files for " + packageName + " @ " + targetFps + "fps");
        return patched > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL CODM config paths unconditionally.
     * Writes proper JSON for UserSetting.json and INI for GraphicsSettings.ini.
     * Uses Shizuku (temporary root) to reach /data/data/ paths.
     *
     * @return true if at least one path was written
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;

        int codmFpsOption = targetFps >= 120 ? 5 : (targetFps >= 90 ? 4 : 3);
        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            String content;
            if (path.endsWith(".json")) {
                content = "{\n" +
                        "  \"MaxFrameRate\": " + targetFps + ",\n" +
                        "  \"GraphicQuality\": 0,\n" +
                        "  \"FPSLimit\": " + targetFps + ",\n" +
                        "  \"HDRMode\": 0,\n" +
                        "  \"TouchBoostHz\": 165,\n" +
                        "  \"TouchDelay\": 0.0,\n" +
                        "  \"SuperResolution\": 0,\n" +
                        "  \"FieldOfView\": 90,\n" +
                        "  \"AntiAliasing\": 0,\n" +
                        "  \"ShadowQuality\": 0\n" +
                        "}\n";
            } else if (path.endsWith(".xml")) {
                // Unity PlayerPrefs XML format
                content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n" +
                        "<map>\n" +
                        "  <int name=\"frame_rate\" value=\"" + codmFpsOption + "\" />\n" +
                        "  <int name=\"MaxFpsOption\" value=\"" + codmFpsOption + "\" />\n" +
                        "  <int name=\"bk_frame_rate\" value=\"" + targetFps + "\" />\n" +
                        "  <int name=\"graphic_quality\" value=\"0\" />\n" +
                        "  <int name=\"GraphicsQualityOption\" value=\"0\" />\n" +
                        "  <int name=\"hdr_mode\" value=\"0\" />\n" +
                        "  <int name=\"AntiAliasingOption\" value=\"0\" />\n" +
                        "  <int name=\"RealtimeShadowOption\" value=\"0\" />\n" +
                        "  <int name=\"BloomOption\" value=\"0\" />\n" +
                        "  <int name=\"DepthOfFieldOption\" value=\"0\" />\n" +
                        "</map>\n";
            } else {
                // INI and .cfg formats
                content = "[Graphics]\n" +
                        "frame_rate=" + targetFps + "\n" +
                        "MaxFrameRate=" + targetFps + "\n" +
                        "FPSLimit=" + targetFps + "\n" +
                        "graphic_quality=0\n" +
                        "GraphicQuality=0\n" +
                        "HDRMode=0\n" +
                        "TouchBoostHz=165\n" +
                        "TouchDelay=0.0\n" +
                        "AntiAliasing=0\n" +
                        "ShadowQuality=0\n";
            }
            forceWrite(path, content);
            written++;
        }
        Log.i(TAG, "CODM competitive config force-write: " + written + " paths @ " + targetFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Injects super-fast touch settings into CODM config files.
     * Sets TouchBoostHz=165 in both JSON and INI formats.
     */
    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String cmd;
            if (path.endsWith(".json")) {
                cmd = "grep -qF 'TouchBoostHz' " + path +
                      " || sed -i 's/}$/,\\n  \"TouchBoostHz\": 165\\n}/' " + path + "; " +
                      "sed -i 's/\"TouchBoostHz\":.*/\"TouchBoostHz\": 165,/' " + path;
            } else {
                cmd = "grep -qF 'TouchBoostHz' " + path + " || echo 'TouchBoostHz=165' >> " + path + "; " +
                      "sed -i 's/^TouchBoostHz=.*/TouchBoostHz=165/' " + path;
            }
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "CODM super-fast touch applied for " + packageName);
    }

    /**
     * Injects Aim Assist, Aimbot 80% Lock, Target Tracking, and Bullet Damage Boost into CODM config files.
     * Uses Shizuku ADB temporary root access for /data/data/ and /sdcard/ file locations.
     */
    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            ensureDirectory(path);
            String cmd;
            if (path.endsWith(".json")) {
                cmd = "grep -qF 'AimAssist' " + path +
                      " || sed -i 's/}$/,\\n  \"AimAssist\": 1,\\n  \"AimAssistStrength\": 80,\\n  \"AimbotLockRate\": 0.80,\\n  \"RotationalAimAssist\": 1,\\n  \"TargetLockSensitivity\": 100,\\n  \"AimMagnetism\": 1,\\n  \"DamageBoostRatio\": 1.80\\n}/' " + path;
            } else if (path.endsWith(".xml")) {
                cmd = "grep -qF 'AimAssist' " + path +
                      " || sed -i 's/<\\/map>/  <int name=\"AimAssist\" value=\"1\" \\/>\\n  <int name=\"AimAssistStrength\" value=\"80\" \\/>\\n  <float name=\"AimbotLockRate\" value=\"0.80\" \\/>\\n<\\/map>/' " + path;
            } else {
                cmd = "grep -qF 'AimAssist' " + path + " || echo 'AimAssist=1' >> " + path + "; " +
                      "grep -qF 'AimAssistStrength' " + path + " || echo 'AimAssistStrength=80' >> " + path + "; " +
                      "grep -qF 'AimbotLockRate' " + path + " || echo 'AimbotLockRate=0.80' >> " + path + "; " +
                      "grep -qF 'RotationalAimAssist' " + path + " || echo 'RotationalAimAssist=1' >> " + path + "; " +
                      "grep -qF 'TargetLockSensitivity' " + path + " || echo 'TargetLockSensitivity=100' >> " + path + "; " +
                      "grep -qF 'DamageBoostRatio' " + path + " || echo 'DamageBoostRatio=1.80' >> " + path + "; " +
                      "grep -qF 'AimMagnetism' " + path + " || echo 'AimMagnetism=1' >> " + path;
            }
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "CODM Aim Assist & Aimbot 80% Damage config applied via Shizuku for " + packageName);
    }

    /**
     * Injects Recoil Reduction & Weapon Shake Reduction keys into CODM config files.
     * Uses Shizuku ADB temporary root access for /data/data/ and /sdcard/ file locations.
     */
    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            ensureDirectory(path);
            String cmd;
            if (path.endsWith(".json")) {
                cmd = "grep -qF 'RecoilScale' " + path +
                      " || sed -i 's/}$/,\\n  \"RecoilScale\": 0.20,\\n  \"WeaponKickReduction\": 0.80,\\n  \"GunShakeMode\": 0\\n}/' " + path;
            } else if (path.endsWith(".xml")) {
                cmd = "grep -qF 'RecoilScale' " + path +
                      " || sed -i 's/<\\/map>/  <float name=\"RecoilScale\" value=\"0.20\" \\/>\\n  <float name=\"WeaponKickReduction\" value=\"0.80\" \\/>\\n<\\/map>/' " + path;
            } else {
                cmd = "grep -qF 'RecoilScale' " + path + " || echo 'RecoilScale=0.20' >> " + path + "; " +
                      "grep -qF 'WeaponKickReduction' " + path + " || echo 'WeaponKickReduction=0.80' >> " + path + "; " +
                      "grep -qF 'GunShakeMode' " + path + " || echo 'GunShakeMode=0' >> " + path;
            }
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "CODM Recoil Control 80% reduction applied via Shizuku for " + packageName);
    }


    // ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/GameBoosterPro/configs/codm_ultramax_165.cfg");
        paths.add("/sdcard/GameBoosterPro/configs/" + pkg + "_165.cfg");
        paths.add("/sdcard/Android/data/" + pkg + "/files/Config/UserSetting.json");
        paths.add("/sdcard/Android/data/" + pkg + "/files/" + pkg + ".v2.playerprefs.xml");
        paths.add("/sdcard/Android/data/" + pkg + "/files/GraphicsSettings.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/ControlsSettings.ini");
        paths.add("/data/data/" + pkg + "/files/GraphicsSettings.ini");
        paths.add("/data/data/" + pkg + "/files/ControlsSettings.ini");
        paths.add("/data/data/" + pkg + "/files/Config/UserSetting.json");
        paths.add("/data/data/" + pkg + "/shared_prefs/" + pkg + ".v2.playerprefs.xml");
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
        String checkRes = CommandExecutor.executeSystemCommand("test -f " + path + " && echo EXISTS");

        if (!checkRes.contains("EXISTS")) {
            String content;
            if (path.endsWith(".xml")) {
                content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n<map>\n  <int name=\"frame_rate\" value=\"" + targetFps + "\" />\n  <int name=\"graphic_quality\" value=\"3\" />\n</map>\n";
            } else {
                content = String.format(
                        "{\n  \"MaxFrameRate\": %d,\n  \"GraphicQuality\": 3,\n  \"FPSLimit\": %d,\n  \"SuperResolution\": 1,\n  \"FieldOfView\": 90\n}\n",
                        targetFps, targetFps
                );
            }
            forceWrite(path, content);
        } else {
            if (path.endsWith(".xml")) {
                CommandExecutor.executeSystemCommand("sed -i 's/name=\"frame_rate\" value=\".*\"/name=\"frame_rate\" value=\"" + targetFps + "\"/' " + path);
            } else {
                CommandExecutor.executeSystemCommand("sed -i 's/\"MaxFrameRate\":.*/\"MaxFrameRate\": " + targetFps + ",/' " + path);
                CommandExecutor.executeSystemCommand("sed -i 's/\"FPSLimit\":.*/\"FPSLimit\": " + targetFps + ",/' " + path);
            }
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
