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
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
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
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);

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
        String[] aimKeys = {
            "AimAssist=1",
            "AimAssistStrength=150",
            "CameraSensitivity=150",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
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
        Log.i(TAG, "Roblox Aim Assist & Gyro 1000Hz applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "RecoilControl=1",
            "ZeroRecoil=1",
            "NoRecoil=1",
            "RecoilScale=0.00",
            "VerticalRecoil=0.00",
            "HorizontalRecoil=0.00",
            "VerticalRecoilScale=0.00",
            "HorizontalRecoilScale=0.00",
            "RecoilReduction=1.50",
            "CameraShake=0",
            "NoCameraShake=1",
            "WeaponStability=150",
            "InputSmoothing=1",
            "ScopeShakeReduction=1.50",
            "ScopeStability=1.50",
            "ScopeRecoilMultiplier=0.00",
            "AimPunchReduction=1",
            "FlinchReduction=1",
            "GunKick=0",
            "WeaponKickReduction=1.50",
            "SpreadScale=0.00",
            "BulletSpread=0.00",
            "CrosshairSpread=0.00",
            "WeaponSway=0"
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
        Log.i(TAG, "Roblox Zero Recoil & Camera Shake Elimination applied for " + packageName);
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageKeys = {
            "DamageMultiplier=2.50",
            "PhysicalDamageBoost=2.50",
            "BulletDamageBoost=2.50",
            "DamageBoost=2.50",
            "DamageBoostRatio=2.50",
            "HeadshotMultiplier=3.50",
            "HeadshotDamageMultiplier=3.50",
            "CriticalDamage=99",
            "CriticalHitRate=99",
            "CriticalDamageRate=99",
            "CriticalDamageMultiplier=3.50",
            "PenetrationBoost=99",
            "ArmorPenetration=99",
            "HighDamageRateMode=1",
            "HitboxExpansion=1.50",
            "BulletVelocityMultiplier=2.00",
            "BodyDamageMultiplier=2.00",
            "LimbDamageMultiplier=1.50",
            "ExplosiveDamageMultiplier=2.00",
            "FOV=120"
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
        Log.i(TAG, "Roblox Damage Boost & FOV applied for " + packageName);
    }

    /**
     * Injects Defense Multiplier, Damage Reduction, Shield Multiplier, and HP Boost for Roblox.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "DefenseMultiplier=2.50",
            "DamageReductionRatio=0.50",
            "DamageReduction=0.50",
            "IncomingDamageReduction=0.50",
            "ShieldMultiplier=2.00",
            "ShieldCapacity=2.00",
            "ShieldStrength=2.50",
            "ShieldEfficiency=2.00",
            "MaxHPMultiplier=1.50",
            "HPBoostRatio=1.50",
            "ArmorBoost=150",
            "PhysicalDefenseBoost=2.50",
            "DamageAbsorbRatio=1.50",
            "TenacityRatio=0.50",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=1.50",
            "ExplosionResistance=0.50",
            "FallDamageReduction=0.00"
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
        Log.i(TAG, "Roblox Armor Defense & Shield Boost applied for " + packageName);
    }

    /**
     * Injects Bullet Tracking, Magic Bullet, Hitbox Expansion, and Target Lock for Roblox.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "FFlagBulletTracking=True",
            "FFlagHitboxExpansion=True",
            "FFlagMagicBullet=True",
            "FFlagTargetLockTracking=True",
            "BulletTracking=1",
            "TrackingBullet=1",
            "HitboxExpansion=1.50",
            "BulletMagnetism=1.50",
            "TargetLockTracking=1"
        };
        for (String path : paths) {
            ensureDirectory(path);
            StringBuilder sb = new StringBuilder();
            if (path.endsWith(".json")) {
                for (String keyVal : trackingKeys) {
                    String k = keyVal.substring(0, keyVal.indexOf("="));
                    String v = keyVal.substring(keyVal.indexOf("=") + 1);
                    sb.append("grep -qF '\"").append(k).append("\"' ").append(path)
                      .append(" || sed -i '2i \\  \"").append(k).append("\": \"").append(v).append("\",' ").append(path).append("; ");
                }
            } else {
                sb.append("grep -qF '[TrackingConfig]' ").append(path).append(" || echo '[TrackingConfig]' >> ").append(path).append("; ");
                for (String keyVal : trackingKeys) {
                    String k = keyVal.substring(0, keyVal.indexOf("="));
                    sb.append("grep -qF '").append(k).append("' ").append(path)
                      .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                    sb.append("sed -i 's/^").append(k).append("=.*/").append(keyVal).append("/' ").append(path).append("; ");
                }
            }
            String cmd = sb.toString();
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Roblox Bullet Tracking & Hitbox Expansion applied for " + packageName);
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
        if (!ShizukuFileManager.fileExists(path)) {
            String content = String.format(
                    "{\n  \"DFIntTaskSchedulerTargetFps\": %d,\n  \"FIntTargetFPS\": %d,\n  \"FIntDesiredMaxFrameRate\": %d,\n  \"FFlagEnableHighFPS\": \"True\",\n  \"FFlagUnlockFPS\": \"True\",\n  \"FFlagDebugGraphicsPreferVulkan\": \"True\",\n  \"FFlagFixGraphicsQuality\": \"True\",\n  \"DFFlagDisableDPIScale\": \"True\",\n  \"FFlagCommitToFastPhysics\": \"True\",\n  \"FFlagEnableVulkan\": \"True\"\n}\n",
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

    private static void ensureDirectory(String path) {
        ShizukuFileManager.ensureParentDirectory(path);
    }
}
