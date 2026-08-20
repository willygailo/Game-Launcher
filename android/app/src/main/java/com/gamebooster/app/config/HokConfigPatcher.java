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
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
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

        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String content = "[Graphics]\n" +
                "HighFPSMode=1\n" +
                "FrameRateLevel=" + frameRateLevel + "\n" +
                "FPS=" + forcedFps + "\n" +
                "MaxFrameRate=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "GraphicsQuality=4\n" +
                "HDMode=1\n" +
                "HDRMode=1\n" +
                "UltraFrameRate=1\n" +
                "VulkanEnabled=1\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "Unlock120Hz=1\n" +
                "Unlock144Hz=1\n" +
                "Unlock165Hz=1\n" +
                "Unlock185Hz=1\n" +
                "DroneView=1\n" +
                "DroneViewHeight=3\n" +
                "CameraHeight=3\n" +
                "CameraDistance=150\n" +
                "CameraFOV=150\n" +
                "FieldOfView=150\n" +
                "PhysicalDamageMultiplier=2.50\n" +
                "MagicDamageMultiplier=2.50\n" +
                "CriticalRateBoost=95\n" +
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
        String[] aimKeys = {
            "AimAssist=1",
            "SmartTargeting=1",
            "TargetLock=1",
            "SkillTargetAssist=1",
            "AutoAim=1",
            "TouchSensitivity=150"
        };
        for (String path : paths) {
            ensureDirectory(path);
            StringBuilder sb = new StringBuilder();
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
        Log.i(TAG, "HOK Smart Target Assist applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "InputSmoothing=1",
            "TouchStabilization=1",
            "ZeroInputDelay=1",
            "SkillResponseFast=1",
            "CameraShake=0",
            "ScreenShake=0",
            "NoCameraShake=1",
            "AimPunchReduction=1",
            "FlinchReduction=1",
            "ScopeShakeReduction=1.50",
            "ScopeStability=1.50",
            "WeaponSway=0",
            "RecoilControl=1",
            "ZeroRecoil=1",
            "NoRecoil=1",
            "SpreadScale=0.00",
            "CrosshairSpread=0.00"
        };
        for (String path : paths) {
            ensureDirectory(path);
            NativeConfigInjector.injectNoRecoil(path);
            StringBuilder sb = new StringBuilder();
            for (String keyVal : recoilKeys) {
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
        Log.i(TAG, "HOK Input Smoothing & Stabilization applied for " + packageName);
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageKeys = {
            "PhysicalDamageBoost=2.50",
            "MagicDamageBoost=2.50",
            "TrueDamageBoost=2.50",
            "DamageMultiplier=2.50",
            "DamageBoost=2.50",
            "CritRate=99",
            "CritDamage=3.50",
            "CriticalDamageRate=99",
            "CriticalHitRate=99",
            "CriticalDamageMultiplier=3.50",
            "HeadshotMultiplier=3.50",
            "AttackSpeedBoost=1.5",
            "HitboxExpansion=1.50",
            "BodyDamageMultiplier=2.00",
            "ExplosiveDamageMultiplier=2.00",
            "FOV=150"
        };
        for (String path : paths) {
            ensureDirectory(path);
            NativeConfigInjector.injectHighDamage(path);
            StringBuilder sb = new StringBuilder();
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
        Log.i(TAG, "HOK Damage Boost & FOV applied for " + packageName);
    }

    /**
     * Injects Physical Armor, Magic Resistance, Damage Reduction, and Shield Boost for Honor of Kings / Arena of Valor.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "PhysicalArmor=2.50",
            "MagicResistance=2.50",
            "DamageReduction=0.50",
            "DamageReductionRatio=0.50",
            "IncomingDamageReduction=0.50",
            "ShieldBoost=2.00",
            "ShieldStrength=2.50",
            "ShieldEfficiency=2.00",
            "MaxHPBoost=1.50",
            "MaxHPMultiplier=1.50",
            "Tenacity=0.50",
            "TenacityRatio=0.50",
            "ArmorBoost=150",
            "PhysicalDefenseBoost=2.50",
            "MagicDefenseBoost=2.50",
            "DamageAbsorbRatio=1.50",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=1.50"
        };
        for (String path : paths) {
            ensureDirectory(path);
            NativeConfigInjector.injectArmorDef(path);
            StringBuilder sb = new StringBuilder();
            sb.append("grep -qF '[DefenseConfig]' ").append(path).append(" || echo '[DefenseConfig]' >> ").append(path).append("; ");
            for (String keyVal : armorKeys) {
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
        Log.i(TAG, "HOK Armor Defense & Damage Reduction applied for " + packageName);
    }

    /**
     * Injects Skill Auto-Tracking, Target Lock, and Skill Magnetism for Honor of Kings.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "SkillTracking=1",
            "AutoTargetLock=1",
            "TargetLockTracking=1",
            "PredictPath=1",
            "SkillMagnetism=1.50",
            "HeroPriorityLock=1",
            "LowestHPTargetLock=1",
            "HitboxExpansion=1.50",
            "TrackingBullet=1",
            "BulletTracking=1",
            "MagicBullet=1"
        };
        for (String path : paths) {
            ensureDirectory(path);
            StringBuilder sb = new StringBuilder();
            sb.append("grep -qF '[TrackingConfig]' ").append(path).append(" || echo '[TrackingConfig]' >> ").append(path).append("; ");
            for (String keyVal : trackingKeys) {
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
        Log.i(TAG, "HOK Skill Auto-Tracking & Target Lock applied for " + packageName);
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
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;
        if (!ShizukuFileManager.fileExists(path)) {
            String content = String.format(
                    "[Graphics]\nHighFPSMode=1\nFrameRateLevel=%d\nFPS=%d\nMaxFrameRate=%d\nTargetFPS=%d\nGraphicsQuality=4\nHDMode=1\nHDRMode=1\nUltraFrameRate=1\nVulkanEnabled=1\nUnlockFPS=1\nSuperHighFPS=1\nUnlock120Hz=1\nUnlock144Hz=1\nUnlock165Hz=1\nUnlock185Hz=1\nShadow=1\nResolutionScale=1.2\nHighFreqTouchHz=%d\n",
                    frameRateLevel, forcedFps, forcedFps, forcedFps, forcedFps
            );
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd = "sed -i 's/^HighFPSMode=.*/HighFPSMode=1/' " + path + "; " +
                         "sed -i 's/^FrameRateLevel=.*/FrameRateLevel=" + frameRateLevel + "/' " + path + "; " +
                         "sed -i 's/^FPS=.*/FPS=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^MaxFrameRate=.*/MaxFrameRate=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^TargetFPS=.*/TargetFPS=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^GraphicsQuality=.*/GraphicsQuality=4/' " + path + "; " +
                         "sed -i 's/^HDMode=.*/HDMode=1/' " + path + "; " +
                         "sed -i 's/^HDRMode=.*/HDRMode=1/' " + path + "; " +
                         "sed -i 's/^UltraFrameRate=.*/UltraFrameRate=1/' " + path + "; " +
                         "sed -i 's/^UnlockFPS=.*/UnlockFPS=1/' " + path + "; " +
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
