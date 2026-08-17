package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import java.util.ArrayList;
import java.util.List;

/**
 * HokConfigPatcher manages internal config files for Honor of Kings (HOK) and Arena of Valor (AoV).
 * Unlocks 120/144/165 FPS modes, HDR ultra frame rates, and high-frequency touch response.
 */
public class HokConfigPatcher {

    private static final String TAG = "HokConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        int forcedFps = targetFps > 0 ? targetFps : 185;
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "HOK patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;

        final int frameRateLevel = 3;
        final int forcedFps = targetFps > 0 ? targetFps : 185;

        String content = "[Graphics]\n" +
                "HighFPSMode=1\n" +
                "FPSMode=" + forcedFps + "\n" +
                "FrameRateLevel=" + frameRateLevel + "\n" +
                "FPS=" + forcedFps + "\n" +
                "MaxFrameRate=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "HighFrameRate=1\n" +
                "GraphicsQuality=4\n" +
                "HDMode=1\n" +
                "HDQuality=1\n" +
                "HDRMode=1\n" +
                "UltraFrameRate=1\n" +
                "VulkanEnabled=1\n" +
                "VulkanEnable=1\n" +
                "ThreadPacing=1\n" +
                "Unlock185Hz=1\n" +
                "Unlock165Hz=1\n" +
                "DroneView=1\n" +
                "DroneViewHeight=3\n" +
                "CameraHeight=3\n" +
                "CameraDistance=150\n" +
                "CameraFOV=150\n" +
                "FieldOfView=150\n" +
                "PhysicalDamageMultiplier=1.90\n" +
                "MagicDamageMultiplier=1.90\n" +
                "CriticalRateBoost=95\n" +
                "PhysicalPenetrationBoost=95\n" +
                "MagicPenetrationBoost=95\n" +
                "ArcanaDamageBoost=1.90\n" +
                "HeroEmblemDamageBoost=1.90\n" +
                "AllHeroEmblemMax=1\n" +
                "EmblemPhysicalAttackBoost=95\n" +
                "EmblemMagicPowerBoost=95\n" +
                "EmblemCooldownReduction=45\n" +
                "EmblemMovementSpeedBoost=25\n" +
                "HighFreqTouchHz=" + forcedFps + "\n" +
                "TouchPollingRate=1000\n" +
                "TouchZeroDelay=1\n" +
                "TouchResponseLevel=3\n" +
                "GyroSampleRate=1000\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            forceWrite(path, content);
            written++;
        }
        Log.i(TAG, "HOK competitive " + forcedFps + "FPS + Drone View force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String cmd =
                "grep -qF 'HighFreqTouchHz' " + path + " || echo 'HighFreqTouchHz=185' >> " + path + "; " +
                "sed -i 's/^HighFreqTouchHz=.*/HighFreqTouchHz=185/' " + path + "; " +
                "grep -qF 'TouchResponseLevel' " + path + " || echo 'TouchResponseLevel=3' >> " + path + "; " +
                "sed -i 's/^TouchResponseLevel=.*/TouchResponseLevel=3/' " + path + "; " +
                "grep -qF 'TouchPollingRate' " + path + " || echo 'TouchPollingRate=1000' >> " + path + "; " +
                "sed -i 's/^TouchPollingRate=.*/TouchPollingRate=1000/' " + path + "; " +
                "grep -qF 'TouchZeroDelay' " + path + " || echo 'TouchZeroDelay=1' >> " + path + "; " +
                "sed -i 's/^TouchZeroDelay=.*/TouchZeroDelay=1/' " + path + "; " +
                "grep -qF 'ZeroInputLag' " + path + " || echo 'ZeroInputLag=1' >> " + path + "; " +
                "sed -i 's/^ZeroInputLag=.*/ZeroInputLag=1/' " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "HOK super-fast zero-delay touch applied for " + packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageAimKeys = {
            "DroneView=1",
            "DroneViewHeight=3",
            "CameraHeight=3",
            "CameraDistance=150",
            "CameraFOV=150",
            "FieldOfView=150",
            "PhysicalDamageMultiplier=1.90",
            "MagicDamageMultiplier=1.90",
            "PhysicalPenetrationBoost=95",
            "MagicPenetrationBoost=95",
            "ArcanaDamageBoost=1.90",
            "HeroEmblemDamageBoost=1.90",
            "AllHeroEmblemMax=1",
            "EmblemPhysicalAttackBoost=95",
            "EmblemMagicPowerBoost=95",
            "EmblemCooldownReduction=45",
            "EmblemMovementSpeedBoost=25",
            "AutoAimLock=1",
            "SkillShotAssist=1",
            "TargetLockPrecision=100",
            "CriticalRateBoost=95",
            "SkillDelayZero=1",
            "GyroAimAssist=1",
            "GyroZeroDelay=1",
            "GyroResponseRate=1000"
        };
        for (String path : paths) {
            StringBuilder sb = new StringBuilder();
            sb.append("grep -qF '[CombatAssist]' ").append(path).append(" || echo '[CombatAssist]' >> ").append(path).append("; ");
            sb.append("grep -qF '[HeroEmblemConfig]' ").append(path).append(" || echo '[HeroEmblemConfig]' >> ").append(path).append("; ");
            sb.append("grep -qF '[ArcanaConfig]' ").append(path).append(" || echo '[ArcanaConfig]' >> ").append(path).append("; ");
            sb.append("grep -qF '[CameraConfig]' ").append(path).append(" || echo '[CameraConfig]' >> ").append(path).append("; ");
            for (String keyVal : damageAimKeys) {
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
        Log.i(TAG, "HOK Drone View FOV 150, Hero Emblem / Arcana Damage applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        applyAimAssistConfig(packageName);
    }

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/SGameSettings.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/GraphicSettings.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/Config/GameUserSettings.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/Config/Setting.xml");
        paths.add("/sdcard/Android/data/" + pkg + "/files/Config/HighFPS.xml");
        paths.add("/data/data/" + pkg + "/files/SGameSettings.ini");
        paths.add("/data/data/" + pkg + "/files/GraphicSettings.ini");
        paths.add("/data/data/" + pkg + "/files/Config/GameUserSettings.ini");
        paths.add("/data/data/" + pkg + "/shared_prefs/" + pkg + "_preferences.xml");
        paths.add("/data/data/" + pkg + "/shared_prefs/" + pkg + ".v2.playerprefs.xml");

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
        final int frameRateLevel = 3;
        final int forcedFps = targetFps > 0 ? targetFps : 185;
        if (!ShizukuFileManager.fileExists(path)) {
            String content = String.format(
                    "[Graphics]\nHighFPSMode=1\nFrameRateLevel=%d\nFPS=%d\nMaxFrameRate=%d\nTargetFPS=%d\nHDMode=1\nUltraFrameRate=1\nHighFreqTouchHz=%d\n",
                    frameRateLevel, forcedFps, forcedFps, forcedFps, forcedFps
            );
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd = "sed -i 's/^HighFPSMode=.*/HighFPSMode=1/' " + path + "; " +
                         "sed -i 's/^FrameRateLevel=.*/FrameRateLevel=" + frameRateLevel + "/' " + path + "; " +
                         "sed -i 's/^FPS=.*/FPS=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^MaxFrameRate=.*/MaxFrameRate=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^TargetFPS=.*/TargetFPS=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^UltraFrameRate=.*/UltraFrameRate=1/' " + path + "; " +
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
