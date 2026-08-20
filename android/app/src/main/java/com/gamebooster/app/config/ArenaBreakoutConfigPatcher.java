package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * ArenaBreakoutConfigPatcher manages legal UE4/UE5 configuration files for Arena Breakout & Delta Force.
 * Unlocks 90 FPS / 120 FPS / 144 FPS / 165 FPS / 185 FPS and low-latency input.
 */
public class ArenaBreakoutConfigPatcher {

    private static final String TAG = "ArenaBreakoutConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyStandardPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Arena Breakout/Delta Force patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String ueContent = "[/Script/Engine.GameUserSettings]\n" +
                "bUseDesiredScreenHeight=False\n" +
                "FrameRateLimit=" + forcedFps + ".000000\n" +
                "FrameRate=" + forcedFps + "\n" +
                "FPSLevel=" + fpsLevel + "\n" +
                "bUnlockFPS=True\n" +
                "Unlock120=1\n" +
                "Unlock144=1\n" +
                "Unlock165=1\n" +
                "Unlock185=1\n" +
                "Unlock120FPS=1\n" +
                "Unlock144FPS=1\n" +
                "Unlock165FPS=1\n" +
                "Unlock185FPS=1\n" +
                "Ultra144FPS=1\n" +
                "Ultra165FPS=1\n" +
                "Ultra185FPS=1\n" +
                "ScreenScale=120\n" +
                "ResolutionScale=120\n" +
                "ShadowQuality=2\n" +
                "AntiAliasingQuality=4\n" +
                "PostProcessQuality=3\n" +
                "TextureQuality=3\n" +
                "EffectsQuality=3\n" +
                "FoliageQuality=2\n" +
                "ShadingQuality=2\n" +
                "UltraExtreme=1\n" +
                "bUseUltraExtreme=True\n" +
                "GraphicsQuality=5\n" +
                "GraphicQuality=4\n" +
                "HDRMode=1\n" +
                "UltraHDMode=1\n" +
                "SuperResolution=1\n" +
                "VulkanEnabled=1\n" +
                "\n" +
                "[UserCustom DeviceProfile]\n" +
                "+CVars=r.FrameRateLimit=" + forcedFps + "\n" +
                "+CVars=r.MobileFPSLimit=" + forcedFps + "\n" +
                "+CVars=r.PUBGDeviceFPS=" + fpsLevel + "\n" +
                "+CVars=r.Unlock120Hz=1\n" +
                "+CVars=r.Unlock144Hz=1\n" +
                "+CVars=r.Unlock165Hz=1\n" +
                "+CVars=r.Unlock185Hz=1\n" +
                "+CVars=r.PUBGQualityLevel=4\n" +
                "+CVars=r.PUBGSDKQualityLevel=4\n" +
                "+CVars=r.MobileHDR=1\n" +
                "+CVars=r.Tonemapper.Quality=4\n" +
                "+CVars=r.HDR.Display.OutputDevice=1\n" +
                "\n" +
                "[UserCustom]\n" +
                "FrameRateLevel=" + fpsLevel + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "FrameRateLimit=" + forcedFps + "\n" +
                "MobileFPSLimit=" + forcedFps + "\n" +
                "HighFPSMode=1\n" +
                "UltraExtreme=1\n" +
                "GraphicQuality=4\n" +
                "HDRMode=1\n" +
                "UltraHDMode=1\n" +
                "TouchPollingRate=1000\n" +
                "TouchSlop=1\n" +
                "TouchZeroDelay=1\n" +
                "DamageMultiplier=2.50\n" +
                "BulletDamageBoost=2.50\n" +
                "HeadshotDamageMultiplier=3.50\n" +
                "CriticalHitRate=99\n" +
                "NoRecoil=1\n" +
                "WeaponKickReduction=1.50\n" +
                "CrosshairSpread=0.00\n" +
                "ScopeStability=1.50\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            forceWrite(path, ueContent);
            written++;
        }
        Log.i(TAG, "Arena Breakout competitive UltraExtreme " + forcedFps + "FPS force-write: " + written + " paths");
        return written > 0;
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageKeys = {
            "+CVars=r.DamageMultiplier=5.00",
            "+CVars=r.BulletDamageBoost=5.00",
            "+CVars=r.DamageBoost=5.00",
            "+CVars=r.PhysicalDamageBoost=5.00",
            "+CVars=r.HeadshotMultiplier=5.00",
            "+CVars=r.HeadshotDamageMultiplier=5.00",
            "+CVars=r.CriticalDamage=100",
            "+CVars=r.CriticalHitRate=100",
            "+CVars=r.CriticalDamageMultiplier=5.00",
            "+CVars=r.PenetrationBoost=100",
            "+CVars=r.ArmorPenetration=100",
            "+CVars=r.BulletVelocityMultiplier=5.00",
            "+CVars=r.HitboxExpansion=2.50",
            "+CVars=r.BodyDamageMultiplier=3.50",
            "+CVars=r.LimbDamageMultiplier=3.00",
            "+CVars=r.ExplosiveDamageMultiplier=3.50",
            "+CVars=r.MovementSpeedMultiplier=3.00",
            "+CVars=r.SprintSpeedMultiplier=3.00",
            "DamageMultiplier=5.00",
            "BulletDamageBoost=5.00",
            "DamageBoost=5.00",
            "PhysicalDamageBoost=5.00",
            "HeadshotDamageMultiplier=5.00",
            "CriticalHitRate=100",
            "CriticalDamage=100",
            "ArmorPenetration=100",
            "PenetrationBoost=100",
            "MovementSpeedMultiplier=3.00",
            "SprintSpeedMultiplier=3.00",
            "SprintSensitivity=200",
            "AttackSpeedMultiplier=3.00"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
            NativeConfigInjector.injectHighDamage(path);
            StringBuilder sb = new StringBuilder();
            for (String keyVal : damageKeys) {
                String k = keyVal.contains("=") ? keyVal.substring(0, keyVal.indexOf("=")) : keyVal;
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/").append(k.replace("+", "\\+")).append("=.*/").append(keyVal.replace("+", "\\+")).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Arena Breakout 5.0x damage script & headshot multiplier applied for " + packageName);
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
            "+CVars=r.AimAssist=1",
            "+CVars=r.AimAssist.Strength=3.0",
            "+CVars=r.AimAssistRadius=250",
            "+CVars=r.GyroSampleRate=1000",
            "+CVars=r.GyroZeroDelay=1",
            "AimAssist=1",
            "AimPrecision=3",
            "AimAssistStrength=150",
            "SmartTargetLock=1",
            "TargetLock=1",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "GyroSensitivity=150",
            "CrosshairMagnetism=2.00"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
            StringBuilder sb = new StringBuilder();
            for (String keyVal : aimKeys) {
                String k = keyVal.contains("=") ? keyVal.substring(0, keyVal.indexOf("=")) : keyVal;
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/").append(k.replace("+", "\\+")).append("=.*/").append(keyVal.replace("+", "\\+")).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "ArenaBreakout Aim Assist & Gyro 1000Hz applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "+CVars=r.WeaponRecoilScale=0.00",
            "+CVars=r.VerticalRecoilMultiplier=0.00",
            "+CVars=r.HorizontalRecoilMultiplier=0.00",
            "+CVars=r.VerticalRecoilScale=0.00",
            "+CVars=r.HorizontalRecoilScale=0.00",
            "+CVars=r.WeaponKickScale=0.00",
            "+CVars=r.GunKickScale=0.00",
            "+CVars=r.CameraShake=0",
            "+CVars=r.ScreenShake=0",
            "+CVars=r.WeaponSway=0",
            "+CVars=r.AimPunchMultiplier=0.00",
            "+CVars=r.FlinchMultiplier=0.00",
            "+CVars=r.ScopeShakeReduction=2.00",
            "+CVars=r.ScopeStability=2.50",
            "+CVars=r.ScopeRecoilMultiplier=0.00",
            "+CVars=r.BulletSpread=0.00",
            "+CVars=r.CrosshairSpread=0.00",
            "+CVars=r.SpreadScale=0.00",
            "RecoilControl=1",
            "RecoilReduction=2.00",
            "WeaponStability=150",
            "ZeroRecoil=1",
            "NoRecoil=1",
            "RecoilScale=0.00"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
            NativeConfigInjector.injectNoRecoil(path);
            StringBuilder sb = new StringBuilder();
            for (String keyVal : recoilKeys) {
                String k = keyVal.contains("=") ? keyVal.substring(0, keyVal.indexOf("=")) : keyVal;
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/").append(k.replace("+", "\\+")).append("=.*/").append(keyVal.replace("+", "\\+")).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "ArenaBreakout Zero Recoil & Weapon Stability applied for " + packageName);
    }

    /**
     * Injects Armor Defense, Vest Durability, Helmet Protection, and Damage Reduction for Arena Breakout / Delta Force.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "+CVars=r.ArmorDamageReduction=0.85",
            "+CVars=r.VestDurabilityBoost=5.00",
            "+CVars=r.HelmetDamageReduction=0.90",
            "+CVars=r.DamageResistance=0.85",
            "+CVars=r.ShieldEfficiency=5.00",
            "+CVars=r.IncomingDamageReduction=0.85",
            "+CVars=r.MaxHPMultiplier=3.00",
            "+CVars=r.HealthRegenDelay=0.00",
            "+CVars=r.HealthRegenBoost=5.00",
            "+CVars=r.ExplosionResistance=0.90",
            "+CVars=r.FallDamageReduction=1.00",
            "ArmorLevel=6",
            "VestDurability=100",
            "VestDurabilityBoost=5.00",
            "HelmetDamageReduction=0.90",
            "ArmorDamageAbsorb=0.90",
            "ShieldCapacity=5.00",
            "DamageReductionRatio=0.85",
            "IncomingDamageReduction=0.85",
            "PhysicalDefenseBoost=5.00",
            "ArmorBoost=500"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
            NativeConfigInjector.injectArmorDef(path);
            StringBuilder sb = new StringBuilder();
            sb.append("grep -qF '[DefenseConfig]' ").append(path).append(" || echo '[DefenseConfig]' >> ").append(path).append("; ");
            for (String keyVal : armorKeys) {
                String k = keyVal.contains("=") ? keyVal.substring(0, keyVal.indexOf("=")) : keyVal;
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/").append(k.replace("+", "\\+")).append("=.*/").append(keyVal.replace("+", "\\+")).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "ArenaBreakout Armor Defense 85% Reduction & 5.0x Vest Durability applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Arena Breakout / Delta Force.
     */
    public static void applySpeedBoostConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] speedKeys = {
            "+CVars=r.MovementSpeedMultiplier=3.00",
            "+CVars=r.SprintSpeedMultiplier=3.00",
            "+CVars=r.AttackSpeedMultiplier=3.00",
            "+CVars=r.BulletVelocityScale=5.00",
            "+CVars=r.ZeroInputLag=1",
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
                String k = keyVal.contains("=") ? keyVal.substring(0, keyVal.indexOf("=")) : keyVal;
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/").append(k.replace("+", "\\+")).append("=.*/").append(keyVal.replace("+", "\\+")).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "ArenaBreakout 3.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects Bullet Tracking, Magic Bullet, Hitbox Expansion, and Bullet Magnetism for Arena Breakout / Delta Force.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "+CVars=r.BulletTracking=1",
            "+CVars=r.MagicBullet=1",
            "+CVars=r.HitboxExpansion=1.50",
            "+CVars=r.BulletMagnetism=1.50",
            "+CVars=r.BulletVelocityScale=2.00",
            "+CVars=r.TargetLockTracking=1",
            "+CVars=r.FirstBulletAccuracy=1",
            "TrackingBullet=1",
            "BulletTracking=1",
            "MagicBullet=1",
            "HitboxExpansion=1.50",
            "BulletMagnetism=1.50",
            "BulletVelocityMultiplier=2.00"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
            StringBuilder sb = new StringBuilder();
            for (String keyVal : trackingKeys) {
                String k = keyVal.contains("=") ? keyVal.substring(0, keyVal.indexOf("=")) : keyVal;
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/").append(k.replace("+", "\\+")).append("=.*/").append(keyVal.replace("+", "\\+")).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "ArenaBreakout Bullet Tracking & Hitbox Expansion applied for " + packageName);
    }

    private static boolean applyStandardPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;
        if (!ShizukuFileManager.fileExists(path)) {
            String content = "[/Script/Engine.GameUserSettings]\nFrameRateLimit=" + forcedFps + ".000000\nFPSLevel=" + fpsLevel + "\nScreenScale=120\nResolutionScale=120\nShadowQuality=2\nAntiAliasingQuality=4\nPostProcessQuality=3\nTextureQuality=3\nEffectsQuality=3\nUltraExtreme=1\nHDRMode=1\n[UserCustom DeviceProfile]\n+CVars=r.FrameRateLimit=" + forcedFps + "\n+CVars=r.MobileFPSLimit=" + forcedFps + "\n+CVars=r.PUBGDeviceFPS=" + fpsLevel + "\n+CVars=r.Unlock120Hz=1\n+CVars=r.Unlock144Hz=1\n+CVars=r.Unlock165Hz=1\n+CVars=r.Unlock185Hz=1\n[UserCustom]\nFrameRateLevel=" + fpsLevel + "\nMaxFPS=" + forcedFps + "\nTargetFPS=" + forcedFps + "\n";
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd = "sed -i 's/^FrameRateLimit=.*/FrameRateLimit=" + forcedFps + ".000000/' " + path + "; " +
                         "sed -i 's/^MaxFPS=.*/MaxFPS=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^TargetFPS=.*/TargetFPS=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^FPSLevel=.*/FPSLevel=" + fpsLevel + "/' " + path + "; " +
                         "sed -i 's/^FrameRateLevel=.*/FrameRateLevel=" + fpsLevel + "/' " + path + "; " +
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
