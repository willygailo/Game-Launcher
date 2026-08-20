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
        String[] damageKeys = {
            "DamageMultiplier=5.00",
            "PhysicalDamageBoost=5.00",
            "BulletDamageBoost=5.00",
            "DamageBoost=5.00",
            "DamageBoostRatio=5.00",
            "HeadshotMultiplier=5.00",
            "HeadshotDamageMultiplier=5.00",
            "CriticalDamage=100",
            "CriticalDamageRate=100",
            "CriticalHitRate=100",
            "CriticalDamageMultiplier=5.00",
            "PenetrationBoost=100",
            "ArmorPenetration=100",
            "HighDamageRateMode=1",
            "AttackSpeedMultiplier=3.00",
            "AttackSpeedBoost=3.00",
            "ReloadSpeedMultiplier=3.00",
            "FireRateMultiplier=2.50",
            "MovementSpeedMultiplier=3.00",
            "SprintSpeedMultiplier=3.00",
            "SprintSensitivity=200",
            "AgilityMultiplier=3.00",
            "HitboxExpansion=2.50",
            "BulletVelocityMultiplier=5.00",
            "BulletVelocityScale=5.00",
            "BodyDamageMultiplier=3.50",
            "LimbDamageMultiplier=3.00",
            "ExplosiveDamageMultiplier=3.50"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
            NativeConfigInjector.injectHighDamage(path);
            StringBuilder sb = new StringBuilder();
            for (String keyVal : damageKeys) {
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
        Log.i(TAG, "Standoff 2 5.0x damage boost & headshot multiplier applied for " + packageName);
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
            "AimPrecision=3",
            "AimAssistStrength=150",
            "AimSmooth=1",
            "AimTracking=1",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "SensitivityMultiplier=2.5",
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
            "RecoilControl=1",
            "ZeroRecoil=1",
            "NoRecoil=1",
            "RecoilScale=0.00",
            "VerticalRecoil=0.00",
            "HorizontalRecoil=0.00",
            "VerticalRecoilScale=0.00",
            "HorizontalRecoilScale=0.00",
            "RecoilReduction=2.00",
            "WeaponStability=150",
            "WeaponKick=0",
            "GunKickReduction=2.00",
            "WeaponKickReduction=2.00",
            "NoShake=1",
            "NoCameraShake=1",
            "CameraShake=0",
            "ScreenShake=0",
            "SpreadReduction=1",
            "BulletSpread=0.00",
            "CrosshairSpread=0.00",
            "SpreadScale=0.00",
            "FirstBulletAccuracy=1",
            "AimPunchReduction=1",
            "FlinchReduction=1",
            "ScopeShakeReduction=2.00",
            "ScopeRecoilMultiplier=0.00",
            "ScopeStability=2.50",
            "WeaponSway=0"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
            NativeConfigInjector.injectNoRecoil(path);
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

    /**
     * Injects Armor Efficiency, Vest Durability, Helmet Protection, and Damage Reduction into Standoff 2.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "ArmorEfficiency=5.00",
            "VestDurability=5.00",
            "VestDurabilityBoost=5.00",
            "DamageReduction=0.85",
            "DamageReductionRatio=0.85",
            "IncomingDamageReduction=0.85",
            "HelmetProtection=0.90",
            "HelmetDamageReduction=0.90",
            "ShieldCapacity=5.00",
            "ShieldMultiplier=5.00",
            "ShieldStrength=5.00",
            "MaxHPMultiplier=3.00",
            "HPBoostRatio=3.00",
            "DamageAbsorbRatio=3.00",
            "ArmorBoost=500",
            "PhysicalDefenseBoost=5.00",
            "TenacityRatio=0.80",
            "ResilienceLevel=5",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=5.00",
            "FallDamageReduction=1.00",
            "ExplosionResistance=0.90"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
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
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Standoff2 Armor Defense 85% Reduction & 5.0x Vest Durability applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Standoff 2.
     */
    public static void applySpeedBoostConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] speedKeys = {
            "MovementSpeedMultiplier=3.00",
            "MovementSpeedBoost=3.00",
            "SprintSpeedMultiplier=3.00",
            "SprintSpeedBoost=3.00",
            "SprintSensitivity=200",
            "AgilityMultiplier=3.00",
            "AttackSpeedMultiplier=3.00",
            "AttackSpeedBoost=3.00",
            "ReloadSpeedMultiplier=3.00",
            "FireRateMultiplier=2.50",
            "BulletVelocityMultiplier=5.00",
            "BulletVelocityScale=5.00",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "HighSpeedMovement=1"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
            NativeConfigInjector.injectSpeedBoost(path);
            StringBuilder sb = new StringBuilder();
            sb.append("grep -qF '[SpeedEngine]' ").append(path).append(" || echo '[SpeedEngine]' >> ").append(path).append("; ");
            for (String keyVal : speedKeys) {
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
        Log.i(TAG, "Standoff2 3.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects Bullet Tracking, Magic Bullet, Hitbox Expansion, and Crosshair Magnetism for Standoff 2.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "TrackingBullet=1",
            "BulletTracking=1",
            "MagicBullet=1",
            "BulletMagnetism=1.50",
            "HitboxExpansion=1.50",
            "TargetLockTracking=1",
            "BulletCurveFactor=1.20",
            "BulletVelocityMultiplier=2.00",
            "CrosshairMagnetism=1.50",
            "FirstBulletAccuracy=1",
            "AutoAimTrack=1"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
            StringBuilder sb = new StringBuilder();
            sb.append("grep -qF '[TrackingConfig]' ").append(path).append(" || echo '[TrackingConfig]' >> ").append(path).append("; ");
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
        Log.i(TAG, "Standoff2 Bullet Tracking & Magic Bullet applied for " + packageName);
    }

    private static boolean applyStandardPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        if (!ShizukuFileManager.fileExists(path)) {
            String content = "{\n  \"graphics\": {\n    \"target_framerate\": " + forcedFps + ",\n    \"max_framerate\": " + forcedFps + ",\n    \"framerate_cap\": " + forcedFps + ",\n    \"fps_unlock\": 1,\n    \"fps_unlock_120\": 1,\n    \"fps_unlock_144\": 1,\n    \"fps_unlock_165\": 1,\n    \"fps_unlock_185\": 1,\n    \"high_fps_mode\": 1,\n    \"shader_detail\": 3,\n    \"model_detail\": 3,\n    \"texture_detail\": 3,\n    \"screen_scale\": 1.2,\n    \"anisotropic_filtering\": 16,\n    \"antialiasing\": 4,\n    \"ultra_extreme\": true\n  }\n}\n";
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
