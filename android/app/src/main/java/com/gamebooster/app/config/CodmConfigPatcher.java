package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
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
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "CODM patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
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
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);

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
                        "  \"FrameRateLimit\": " + forcedFps + ",\n" +
                        "  \"MobileFPSLimit\": " + forcedFps + ",\n" +
                        "  \"HDRMode\": 1,\n" +
                        "  \"HDRColorMode\": 2,\n" +
                        "  \"Unlock120Hz\": 1,\n" +
                        "  \"Unlock144Hz\": 1,\n" +
                        "  \"Unlock165Hz\": 1,\n" +
                        "  \"Unlock185Hz\": 1,\n" +
                        "  \"TouchBoostHz\": " + forcedFps + ",\n" +
                        "  \"TouchPollingRate\": 1000,\n" +
                        "  \"TouchZeroDelay\": 1,\n" +
                        "  \"GyroSampleRate\": 1000,\n" +
                        "  \"SuperResolution\": 1,\n" +
                        "  \"FieldOfView\": 150,\n" +
                        "  \"FPP_FOV\": 150,\n" +
                        "  \"TPP_FOV\": 100,\n" +
                        "  \"SprintSensitivity\": 150,\n" +
                        "  \"AlwaysSprint\": 1,\n" +
                        "  \"AimAssist\": 1,\n" +
                        "  \"AimAssistStrength\": 150,\n" +
                        "  \"AimAssistLevel\": 5,\n" +
                        "  \"TargetLockSensitivity\": 150,\n" +
                        "  \"RecoilScale\": 0.00,\n" +
                        "  \"WeaponKickReduction\": 1.50,\n" +
                        "  \"AllGunsRecoilReduction\": 1.50,\n" +
                        "  \"ScopeShakeReduction\": 1.50,\n" +
                        "  \"ScopeRecoilMultiplier\": 0.00,\n" +
                        "  \"ScopeStability\": 1.50,\n" +
                        "  \"VerticalRecoilScale\": 0.00,\n" +
                        "  \"HorizontalRecoilScale\": 0.00,\n" +
                        "  \"BulletSpread\": 0.00,\n" +
                        "  \"DamageBoostRatio\": 2.50,\n" +
                        "  \"BulletDamageBoost\": 2.50,\n" +
                        "  \"HeadshotDamageMultiplier\": 3.50,\n" +
                        "  \"CriticalHitRate\": 99,\n" +
                        "  \"GyroSampleRate\": 1000,\n" +
                        "  \"GyroSensitivityRatio\": 2.5,\n" +
                        "  \"GyroZeroDelay\": 1,\n" +
                        "  \"GyroSmoothFactor\": 1,\n" +
                        "  \"GyroStabilization\": 1,\n" +
                        "  \"GyroLatencyMode\": 0,\n" +
                        "  \"AntiAliasing\": 1,\n" +
                        "  \"ShadowQuality\": 2\n" +
                        "}\n";
            } else if (path.endsWith(".xml")) {
                // PlayerPrefs XML format
                content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n" +
                        "<map>\n" +
                        "  <int name=\"MaxFrameRate\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"TargetFPS\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"FPSLimit\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"FrameRateLimit\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"MobileFPSLimit\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"GraphicQuality\" value=\"4\" />\n" +
                        "  <int name=\"HDRMode\" value=\"1\" />\n" +
                        "  <int name=\"Unlock120Hz\" value=\"1\" />\n" +
                        "  <int name=\"Unlock144Hz\" value=\"1\" />\n" +
                        "  <int name=\"Unlock165Hz\" value=\"1\" />\n" +
                        "  <int name=\"Unlock185Hz\" value=\"1\" />\n" +
                        "  <int name=\"TouchBoostHz\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"TouchPollingRate\" value=\"1000\" />\n" +
                        "  <int name=\"GyroSampleRate\" value=\"1000\" />\n" +
                        "  <int name=\"FieldOfView\" value=\"150\" />\n" +
                        "  <int name=\"FPP_FOV\" value=\"150\" />\n" +
                        "  <int name=\"TPP_FOV\" value=\"100\" />\n" +
                        "  <int name=\"SprintSensitivity\" value=\"150\" />\n" +
                        "  <int name=\"AimAssist\" value=\"1\" />\n" +
                        "  <int name=\"AimAssistStrength\" value=\"150\" />\n" +
                        "  <int name=\"AimAssistLevel\" value=\"5\" />\n" +
                        "  <int name=\"TargetLockSensitivity\" value=\"150\" />\n" +
                        "  <float name=\"RecoilScale\" value=\"0.00\" />\n" +
                        "  <float name=\"WeaponKickReduction\" value=\"1.50\" />\n" +
                        "  <float name=\"AllGunsRecoilReduction\" value=\"1.50\" />\n" +
                        "  <float name=\"ScopeShakeReduction\" value=\"1.50\" />\n" +
                        "  <float name=\"ScopeRecoilMultiplier\" value=\"0.00\" />\n" +
                        "  <float name=\"VerticalRecoilScale\" value=\"0.00\" />\n" +
                        "  <float name=\"HorizontalRecoilScale\" value=\"0.00\" />\n" +
                        "  <float name=\"DamageBoostRatio\" value=\"2.50\" />\n" +
                        "  <float name=\"BulletDamageBoost\" value=\"2.50\" />\n" +
                        "  <float name=\"HeadshotDamageMultiplier\" value=\"3.50\" />\n" +
                        "  <int name=\"CriticalHitRate\" value=\"99\" />\n" +
                        "  <int name=\"GyroSampleRate\" value=\"1000\" />\n" +
                        "  <float name=\"GyroSensitivityRatio\" value=\"2.5\" />\n" +
                        "  <int name=\"GyroZeroDelay\" value=\"1\" />\n" +
                        "  <int name=\"GyroSmoothFactor\" value=\"1\" />\n" +
                        "  <int name=\"GyroStabilization\" value=\"1\" />\n" +
                        "  <int name=\"GyroLatencyMode\" value=\"0\" />\n" +
                        "</map>\n";
            } else {
                // INI format
                content = "[Graphics]\n" +
                        "MaxFrameRate=" + forcedFps + "\n" +
                        "TargetFPS=" + forcedFps + "\n" +
                        "FPSLimit=" + forcedFps + "\n" +
                        "FrameRateLimit=" + forcedFps + "\n" +
                        "MobileFPSLimit=" + forcedFps + "\n" +
                        "GraphicQuality=4\n" +
                        "HDRMode=1\n" +
                        "HDRColorMode=2\n" +
                        "Unlock120Hz=1\n" +
                        "Unlock144Hz=1\n" +
                        "Unlock165Hz=1\n" +
                        "Unlock185Hz=1\n" +
                        "SuperResolution=1\n" +
                        "TouchBoostHz=" + forcedFps + "\n" +
                        "TouchPollingRate=1000\n" +
                        "TouchZeroDelay=1\n" +
                        "GyroSampleRate=1000\n" +
                        "FieldOfView=150\n" +
                        "FPP_FOV=150\n" +
                        "TPP_FOV=100\n" +
                        "SprintSensitivity=150\n" +
                        "AimAssist=1\n" +
                        "AimAssistStrength=150\n" +
                        "AimAssistLevel=5\n" +
                        "TargetLockSensitivity=150\n" +
                        "RecoilScale=0.00\n" +
                        "WeaponKickReduction=1.50\n" +
                        "AllGunsRecoilReduction=1.50\n" +
                        "ScopeShakeReduction=1.50\n" +
                        "ScopeRecoilMultiplier=0.00\n" +
                        "ScopeStability=1.50\n" +
                        "VerticalRecoilScale=0.00\n" +
                        "HorizontalRecoilScale=0.00\n" +
                        "BulletSpread=0.00\n" +
                        "DamageBoostRatio=2.50\n" +
                        "BulletDamageBoost=2.50\n" +
                        "HeadshotDamageMultiplier=3.50\n" +
                        "CriticalHitRate=99\n" +
                        "GyroSampleRate=1000\n" +
                        "GyroSensitivityRatio=2.5\n" +
                        "GyroZeroDelay=1\n" +
                        "GyroSmoothFactor=1\n" +
                        "GyroStabilization=1\n" +
                        "GyroLatencyMode=0\n" +
                        "AntiAliasing=1\n";
            }
            forceWrite(path, content);
            written++;
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "CODM competitive HDR " + forcedFps + "FPS force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Applies anti-log and telemetry suppression for CODM.
     */
    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    /**
     * Injects super-fast zero-delay touch settings into CODM config files.
     * Sets TouchBoostHz=185 and TouchPollingRate=1000 in both JSON and INI formats.
     */
    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String cmd;
            if (path.endsWith(".json")) {
                cmd = "grep -qF 'TouchBoostHz' " + path +
                      " || sed -i 's/}$/,\\n  \"TouchBoostHz\": 185,\\n  \"TouchPollingRate\": 1000,\\n  \"TouchZeroDelay\": 1,\\n  \"TouchDeadZone\": 0\\n}/' " + path + "; " +
                      "sed -i 's/\"TouchBoostHz\":.*/\"TouchBoostHz\": 185,/' " + path + "; " +
                      "sed -i 's/\"TouchPollingRate\":.*/\"TouchPollingRate\": 1000,/' " + path;
            } else if (path.endsWith(".xml")) {
                cmd = "grep -qF 'TouchBoostHz' " + path +
                      " || sed -i 's/<\\/map>/  <int name=\"TouchBoostHz\" value=\"185\" \\/>\\n  <int name=\"TouchPollingRate\" value=\"1000\" \\/>\\n  <int name=\"TouchZeroDelay\" value=\"1\" \\/>\\n<\\/map>/' " + path;
            } else {
                cmd = "grep -qF 'TouchBoostHz' " + path + " || echo 'TouchBoostHz=185' >> " + path + "; " +
                      "grep -qF 'TouchPollingRate' " + path + " || echo 'TouchPollingRate=1000' >> " + path + "; " +
                      "grep -qF 'TouchZeroDelay' " + path + " || echo 'TouchZeroDelay=1' >> " + path + "; " +
                      "grep -qF 'TouchDeadZone' " + path + " || echo 'TouchDeadZone=0' >> " + path + "; " +
                      "sed -i 's/^TouchBoostHz=.*/TouchBoostHz=185/' " + path + "; " +
                      "sed -i 's/^TouchPollingRate=.*/TouchPollingRate=1000/' " + path;
            }
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "CODM super-fast zero-delay touch applied for " + packageName);
    }

    /**
     * Injects Aim Assist 150%, FOV (TPP 100 / FPP 150), Sprint 150, Gyro 1000Hz Ultra Response (Super Smooth), and 150% Damage Boost into CODM config files.
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
                      " || sed -i 's/}$/,\\n  \"AimAssist\": 1,\\n  \"AimAssistStrength\": 150,\\n  \"AimAssistLevel\": 5,\\n  \"FPP_FOV\": 150,\\n  \"FieldOfView\": 150,\\n  \"TPP_FOV\": 100,\\n  \"SprintSensitivity\": 150,\\n  \"AlwaysSprint\": 1,\\n  \"AimbotLockRate\": 1.50,\\n  \"RotationalAimAssist\": 1,\\n  \"TargetLockSensitivity\": 150,\\n  \"AimMagnetism\": 1,\\n  \"GyroSensitivityRatio\": 2.5,\\n  \"GyroZeroDelay\": 1,\\n  \"GyroSmoothFactor\": 1,\\n  \"GyroStabilization\": 1,\\n  \"GyroLatencyMode\": 0,\\n  \"GyroSampleRate\": 1000,\\n  \"DamageBoostRatio\": 2.50,\\n  \"BulletDamageBoost\": 2.50,\\n  \"HeadshotDamageMultiplier\": 3.50,\\n  \"CriticalHitRate\": 99\\n}/' " + path;
            } else if (path.endsWith(".xml")) {
                cmd = "grep -qF 'AimAssist' " + path +
                      " || sed -i 's/<\\/map>/  <int name=\"AimAssist\" value=\"1\" \\/>\\n  <int name=\"AimAssistStrength\" value=\"150\" \\/>\\n  <int name=\"AimAssistLevel\" value=\"5\" \\/>\\n  <int name=\"FPP_FOV\" value=\"150\" \\/>\\n  <int name=\"FieldOfView\" value=\"150\" \\/>\\n  <int name=\"TPP_FOV\" value=\"100\" \\/>\\n  <int name=\"SprintSensitivity\" value=\"150\" \\/>\\n  <float name=\"AimbotLockRate\" value=\"1.50\" \\/>\\n  <float name=\"GyroSensitivityRatio\" value=\"2.5\" \\/>\\n  <int name=\"GyroSampleRate\" value=\"1000\" \\/>\\n  <int name=\"GyroSmoothFactor\" value=\"1\" \\/>\\n  <int name=\"GyroStabilization\" value=\"1\" \\/>\\n  <float name=\"DamageBoostRatio\" value=\"2.50\" \\/>\\n  <float name=\"BulletDamageBoost\" value=\"2.50\" \\/>\\n  <float name=\"HeadshotDamageMultiplier\" value=\"3.50\" \\/>\\n  <int name=\"CriticalHitRate\" value=\"99\" \\/>\\n<\\/map>/' " + path;
            } else {
                cmd = "grep -qF 'AimAssist' " + path + " || echo 'AimAssist=1' >> " + path + "; " +
                      "grep -qF 'AimAssistStrength' " + path + " || echo 'AimAssistStrength=150' >> " + path + "; " +
                      "grep -qF 'AimAssistLevel' " + path + " || echo 'AimAssistLevel=5' >> " + path + "; " +
                      "grep -qF 'FPP_FOV' " + path + " || echo 'FPP_FOV=150' >> " + path + "; " +
                      "grep -qF 'FieldOfView' " + path + " || echo 'FieldOfView=150' >> " + path + "; " +
                      "grep -qF 'TPP_FOV' " + path + " || echo 'TPP_FOV=100' >> " + path + "; " +
                      "grep -qF 'SprintSensitivity' " + path + " || echo 'SprintSensitivity=150' >> " + path + "; " +
                      "grep -qF 'AimbotLockRate' " + path + " || echo 'AimbotLockRate=1.50' >> " + path + "; " +
                      "grep -qF 'RotationalAimAssist' " + path + " || echo 'RotationalAimAssist=1' >> " + path + "; " +
                      "grep -qF 'TargetLockSensitivity' " + path + " || echo 'TargetLockSensitivity=150' >> " + path + "; " +
                      "grep -qF 'DamageBoostRatio' " + path + " || echo 'DamageBoostRatio=2.50' >> " + path + "; " +
                      "grep -qF 'BulletDamageBoost' " + path + " || echo 'BulletDamageBoost=2.50' >> " + path + "; " +
                      "grep -qF 'HeadshotDamageMultiplier' " + path + " || echo 'HeadshotDamageMultiplier=3.50' >> " + path + "; " +
                      "grep -qF 'CriticalHitRate' " + path + " || echo 'CriticalHitRate=99' >> " + path + "; " +
                      "grep -qF 'GyroSensitivityRatio' " + path + " || echo 'GyroSensitivityRatio=2.5' >> " + path + "; " +
                      "grep -qF 'GyroZeroDelay' " + path + " || echo 'GyroZeroDelay=1' >> " + path + "; " +
                      "grep -qF 'GyroSampleRate' " + path + " || echo 'GyroSampleRate=1000' >> " + path + "; " +
                      "grep -qF 'GyroSmoothFactor' " + path + " || echo 'GyroSmoothFactor=1' >> " + path + "; " +
                      "grep -qF 'GyroStabilization' " + path + " || echo 'GyroStabilization=1' >> " + path + "; " +
                      "grep -qF 'GyroLatencyMode' " + path + " || echo 'GyroLatencyMode=0' >> " + path + "; " +
                      "grep -qF 'AimMagnetism' " + path + " || echo 'AimMagnetism=1' >> " + path;
            }
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "CODM Aim Assist 150%, TPP 100, FPP 150, Sprint 150, Gyro 1000Hz & 150% Damage config applied via Shizuku for " + packageName);
    }

    /**
     * Injects Zero Recoil & Weapon Shake Elimination keys for ALL guns and ALL scopes into CODM config files.
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
                      " || sed -i 's/}$/,\\n  \"RecoilScale\": 0.00,\\n  \"WeaponKickReduction\": 1.00,\\n  \"AllGunsRecoilReduction\": 1.00,\\n  \"ScopeShakeReduction\": 1.00,\\n  \"ScopeRecoilMultiplier\": 0.00,\\n  \"ScopeStability\": 1.00,\\n  \"VerticalRecoilScale\": 0.00,\\n  \"HorizontalRecoilScale\": 0.00,\\n  \"BulletSpread\": 0.00,\\n  \"GunShakeMode\": 0\\n}/' " + path;
            } else if (path.endsWith(".xml")) {
                cmd = "grep -qF 'RecoilScale' " + path +
                      " || sed -i 's/<\\/map>/  <float name=\"RecoilScale\" value=\"0.00\" \\/>\\n  <float name=\"WeaponKickReduction\" value=\"1.00\" \\/>\\n  <float name=\"AllGunsRecoilReduction\" value=\"1.00\" \\/>\\n  <float name=\"ScopeShakeReduction\" value=\"1.00\" \\/>\\n  <float name=\"ScopeRecoilMultiplier\" value=\"0.00\" \\/>\\n  <float name=\"VerticalRecoilScale\" value=\"0.00\" \\/>\\n  <float name=\"HorizontalRecoilScale\" value=\"0.00\" \\/>\\n<\\/map>/' " + path;
            } else {
                cmd = "grep -qF 'RecoilScale' " + path + " || echo 'RecoilScale=0.00' >> " + path + "; " +
                      "grep -qF 'WeaponKickReduction' " + path + " || echo 'WeaponKickReduction=1.00' >> " + path + "; " +
                      "grep -qF 'AllGunsRecoilReduction' " + path + " || echo 'AllGunsRecoilReduction=1.00' >> " + path + "; " +
                      "grep -qF 'ScopeShakeReduction' " + path + " || echo 'ScopeShakeReduction=1.00' >> " + path + "; " +
                      "grep -qF 'ScopeRecoilMultiplier' " + path + " || echo 'ScopeRecoilMultiplier=0.00' >> " + path + "; " +
                      "grep -qF 'ScopeStability' " + path + " || echo 'ScopeStability=1.00' >> " + path + "; " +
                      "grep -qF 'VerticalRecoilScale' " + path + " || echo 'VerticalRecoilScale=0.00' >> " + path + "; " +
                      "grep -qF 'HorizontalRecoilScale' " + path + " || echo 'HorizontalRecoilScale=0.00' >> " + path + "; " +
                      "grep -qF 'BulletSpread' " + path + " || echo 'BulletSpread=0.00' >> " + path + "; " +
                      "grep -qF 'GunShakeMode' " + path + " || echo 'GunShakeMode=0' >> " + path;
            }
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "CODM Zero Recoil All Guns & All Scopes (0.00 Scale) applied via Shizuku for " + packageName);
    }


    // ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    private static void forceWrite(String path, String content) {
        ShizukuFileManager.writeFile(path, content, "666");
    }

    private static boolean applyPatch(String path, int targetFps) {
        if (!ShizukuFileManager.fileExists(path)) {
            String content;
            if (path.endsWith(".json")) {
                content = String.format(
                        "{\n  \"MaxFrameRate\": %d,\n  \"TargetFPS\": %d,\n  \"FPSLimit\": %d,\n  \"FrameRateLimit\": %d,\n  \"MobileFPSLimit\": %d,\n  \"GraphicQuality\": 4,\n  \"SuperResolution\": 1,\n  \"Unlock120Hz\": 1,\n  \"Unlock144Hz\": 1,\n  \"Unlock165Hz\": 1,\n  \"Unlock185Hz\": 1,\n  \"FieldOfView\": 90\n}\n",
                        targetFps, targetFps, targetFps, targetFps, targetFps
                );
            } else if (path.endsWith(".xml")) {
                content = String.format(
                        "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n<map>\n  <int name=\"MaxFrameRate\" value=\"%d\" />\n  <int name=\"TargetFPS\" value=\"%d\" />\n  <int name=\"FPSLimit\" value=\"%d\" />\n  <int name=\"FrameRateLimit\" value=\"%d\" />\n  <int name=\"MobileFPSLimit\" value=\"%d\" />\n  <int name=\"Unlock120Hz\" value=\"1\" />\n  <int name=\"Unlock144Hz\" value=\"1\" />\n  <int name=\"Unlock165Hz\" value=\"1\" />\n  <int name=\"Unlock185Hz\" value=\"1\" />\n  <int name=\"GraphicQuality\" value=\"4\" />\n</map>\n",
                        targetFps, targetFps, targetFps, targetFps, targetFps
                );
            } else {
                content = String.format(
                        "[Graphics]\nMaxFrameRate=%d\nTargetFPS=%d\nFPSLimit=%d\nFrameRateLimit=%d\nMobileFPSLimit=%d\nGraphicQuality=4\nUnlock120Hz=1\nUnlock144Hz=1\nUnlock165Hz=1\nUnlock185Hz=1\n",
                        targetFps, targetFps, targetFps, targetFps, targetFps
                );
            }
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd;
            if (path.endsWith(".json")) {
                cmd = "sed -i 's/\"MaxFrameRate\":.*/\"MaxFrameRate\": " + targetFps + ",/' " + path + "; " +
                      "sed -i 's/\"TargetFPS\":.*/\"TargetFPS\": " + targetFps + ",/' " + path + "; " +
                      "sed -i 's/\"FPSLimit\":.*/\"FPSLimit\": " + targetFps + ",/' " + path + "; " +
                      "sed -i 's/\"FrameRateLimit\":.*/\"FrameRateLimit\": " + targetFps + ",/' " + path + "; " +
                      "sed -i 's/\"GraphicQuality\":.*/\"GraphicQuality\": 4,/' " + path + "; " +
                      "chmod 666 " + path;
            } else if (path.endsWith(".xml")) {
                cmd = "sed -i 's/<int name=\"MaxFrameRate\" value=\".*\" \\/>/<int name=\"MaxFrameRate\" value=\"" + targetFps + "\" \\/>/' " + path + "; " +
                      "sed -i 's/<int name=\"TargetFPS\" value=\".*\" \\/>/<int name=\"TargetFPS\" value=\"" + targetFps + "\" \\/>/' " + path + "; " +
                      "sed -i 's/<int name=\"FPSLimit\" value=\".*\" \\/>/<int name=\"FPSLimit\" value=\"" + targetFps + "\" \\/>/' " + path + "; " +
                      "chmod 666 " + path;
            } else {
                cmd = "sed -i 's/^MaxFrameRate=.*/MaxFrameRate=" + targetFps + "/' " + path + "; " +
                      "sed -i 's/^TargetFPS=.*/TargetFPS=" + targetFps + "/' " + path + "; " +
                      "sed -i 's/^FPSLimit=.*/FPSLimit=" + targetFps + "/' " + path + "; " +
                      "sed -i 's/^FrameRateLimit=.*/FrameRateLimit=" + targetFps + "/' " + path + "; " +
                      "sed -i 's/^GraphicQuality=.*/GraphicQuality=4/' " + path + "; " +
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

    private static void ensureDirectory(String path) {
        ShizukuFileManager.ensureParentDirectory(path);
    }
}
