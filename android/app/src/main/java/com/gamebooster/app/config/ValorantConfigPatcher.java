package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import java.util.ArrayList;
import java.util.List;

/**
 * ValorantConfigPatcher manages internal UE4 config files and user settings for
 * Valorant Mobile (CN Server Project C and Global versions).
 *
 * Configures 120 / 144 / 165 / 185 FPS unlock, 1000Hz touch & gyro polling,
 * zero input lag, recoil control, and performance rendering pipeline.
 */
public class ValorantConfigPatcher {

    private static final String TAG = "ValorantConfigPatcher";

    // ─── Standard Patch ───────────────────────────────────────────────────────

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Valorant Mobile patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL Valorant Mobile config paths unconditionally.
     * Uses Shizuku (temporary root) to reach /data/data/ and /sdcard/ paths.
     *
     * @return true if at least one path was written
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

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
                        "  \"FrameRateLimit\": " + forcedFps + ".000000,\n" +
                        "  \"MobileFPSLimit\": " + forcedFps + ",\n" +
                        "  \"FPSLevel\": " + fpsLevel + ",\n" +
                        "  \"Unlock185Hz\": 1,\n" +
                        "  \"Unlock165Hz\": 1,\n" +
                        "  \"Unlock144Hz\": 1,\n" +
                        "  \"Unlock120Hz\": 1,\n" +
                        "  \"HighFPSMode\": 1,\n" +
                        "  \"TouchBoostHz\": " + forcedFps + ",\n" +
                        "  \"TouchPollingRate\": 1000,\n" +
                        "  \"TouchZeroDelay\": 1,\n" +
                        "  \"GyroSampleRate\": 1000,\n" +
                        "  \"SuperResolution\": 1,\n" +
                        "  \"FieldOfView\": 120,\n" +
                        "  \"FPP_FOV\": 120,\n" +
                        "  \"CrosshairBloom\": 0,\n" +
                        "  \"AimAssist\": 1,\n" +
                        "  \"AimAssistStrength\": 150,\n" +
                        "  \"RecoilScale\": 0.00,\n" +
                        "  \"WeaponKickReduction\": 1.50,\n" +
                        "  \"BulletSpreadReduction\": 1.00,\n" +
                        "  \"HeadshotDamageMultiplier\": 2.50,\n" +
                        "  \"AntiAliasing\": 1,\n" +
                        "  \"VulkanEnabled\": 1,\n" +
                        "  \"LowLatencyMode\": 1\n" +
                        "}\n";
            } else {
                // UE4 INI format (UserCustom.ini / GameUserSettings.ini)
                content = "[/Script/Engine.GameUserSettings]\n" +
                        "bUseVSync=False\n" +
                        "FrameRateLimit=" + forcedFps + ".000000\n" +
                        "ResolutionSizeX=2400\n" +
                        "ResolutionSizeY=1080\n" +
                        "LastUserConfirmedResolutionSizeX=2400\n" +
                        "LastUserConfirmedResolutionSizeY=1080\n" +
                        "WindowMode=0\n" +
                        "bUseDesiredScreenHeight=False\n" +
                        "[ScalabilityGroups]\n" +
                        "sg.ResolutionQuality=100.000000\n" +
                        "sg.ViewDistanceQuality=3\n" +
                        "sg.AntiAliasingQuality=1\n" +
                        "sg.ShadowQuality=0\n" +
                        "sg.PostProcessQuality=1\n" +
                        "sg.TextureQuality=3\n" +
                        "sg.EffectsQuality=1\n" +
                        "sg.FoliageQuality=0\n" +
                        "sg.ShadingQuality=2\n" +
                        "[UserCustom DeviceProfile]\n" +
                        "+CVars=r.FrameRateLimit=" + forcedFps + "\n" +
                        "+CVars=r.MobileFPSLimit=" + forcedFps + "\n" +
                        "+CVars=r.MobileContentScaleFactor=1.0\n" +
                        "+CVars=r.Unlock120Hz=1\n" +
                        "+CVars=r.Unlock144Hz=1\n" +
                        "+CVars=r.Unlock165Hz=1\n" +
                        "+CVars=r.Unlock185Hz=1\n" +
                        "[ValorantMobileGraphics]\n" +
                        "MaxFPS=" + forcedFps + "\n" +
                        "TargetFPS=" + forcedFps + "\n" +
                        "FrameRateLimit=" + forcedFps + "\n" +
                        "MobileFPSLimit=" + forcedFps + "\n" +
                        "FPSLevel=" + fpsLevel + "\n" +
                        "Unlock185Hz=1\n" +
                        "Unlock165Hz=1\n" +
                        "Unlock144Hz=1\n" +
                        "Unlock120Hz=1\n" +
                        "HighFPSMode=1\n" +
                        "TouchPollingRate=1000\n" +
                        "TouchBoostHz=" + forcedFps + "\n" +
                        "TouchZeroDelay=1\n" +
                        "GyroPollingRate=1000\n" +
                        "AimAssistStrength=150\n" +
                        "RecoilReduction=1.50\n" +
                        "WeaponKickScale=0.00\n" +
                        "ZeroInputLag=1\n" +
                        "VulkanPipeline=1\n";
            }
            forceWrite(path, content);
            written++;
        }
        Log.i(TAG, "Valorant Mobile competitive " + forcedFps + "FPS force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String cmd =
                "grep -qF 'TouchBoostHz' " + path + " || echo 'TouchBoostHz=185' >> " + path + "; " +
                "sed -i 's/^TouchBoostHz=.*/TouchBoostHz=185/' " + path + "; " +
                "grep -qF 'TouchPollingRate' " + path + " || echo 'TouchPollingRate=1000' >> " + path + "; " +
                "sed -i 's/^TouchPollingRate=.*/TouchPollingRate=1000/' " + path + "; " +
                "grep -qF 'TouchZeroDelay' " + path + " || echo 'TouchZeroDelay=1' >> " + path + "; " +
                "sed -i 's/^TouchZeroDelay=.*/TouchZeroDelay=1/' " + path + "; " +
                "grep -qF 'ZeroInputLag' " + path + " || echo 'ZeroInputLag=1' >> " + path + "; " +
                "sed -i 's/^ZeroInputLag=.*/ZeroInputLag=1/' " + path + "; " +
                "grep -qF 'LowLatencyMode' " + path + " || echo 'LowLatencyMode=1' >> " + path + "; " +
                "sed -i 's/^LowLatencyMode=.*/LowLatencyMode=1/' " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Valorant fast zero-delay touch applied for " + packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "AimAssist=1",
            "AimAssistStrength=150",
            "AimAssistRadius=200",
            "CrosshairMagnetism=1.5",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "AimPrecision=3"
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
        Log.i(TAG, "Valorant Aim Assist & Gyro 1000Hz applied for " + packageName);
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
            "WeaponStability=150",
            "FirstBulletAccuracy=1",
            "SpreadScale=0.00",
            "BulletSpread=0.00",
            "CrosshairSpread=0.00",
            "GunKick=0",
            "GunKickReduction=1.50",
            "WeaponKickReduction=1.50",
            "CameraShake=0",
            "NoCameraShake=1",
            "AimPunchReduction=1",
            "FlinchReduction=1",
            "ScopeShakeReduction=1.50",
            "ScopeRecoilMultiplier=0.00",
            "ScopeStability=1.50",
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
        Log.i(TAG, "Valorant Zero Recoil & Weapon Stability applied for " + packageName);
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageKeys = {
            "+CVars=r.DamageMultiplier=5.00",
            "+CVars=r.BulletDamageScale=5.00",
            "+CVars=r.HeadshotMultiplier=5.00",
            "+CVars=r.HeadshotDamageMultiplier=5.00",
            "+CVars=r.WeaponDamageScale=5.00",
            "+CVars=r.CriticalDamage=100",
            "+CVars=r.CriticalDamageRate=100",
            "+CVars=r.CriticalHitRate=100",
            "+CVars=r.CriticalDamageMultiplier=5.00",
            "+CVars=r.PenetrationBoost=100",
            "+CVars=r.ArmorPenetration=100",
            "+CVars=r.HighDamageRateMode=1",
            "+CVars=r.HitboxExpansion=2.50",
            "+CVars=r.BulletVelocity=5.00",
            "+CVars=r.BulletVelocityMultiplier=5.00",
            "+CVars=r.BulletVelocityScale=5.00",
            "+CVars=r.BodyDamageMultiplier=3.50",
            "+CVars=r.LimbDamageMultiplier=3.00",
            "+CVars=r.ExplosiveDamageMultiplier=3.50",
            "+CVars=r.MovementSpeedMultiplier=3.00",
            "+CVars=r.SprintSpeedMultiplier=3.00",
            "DamageMultiplier=5.00",
            "PhysicalDamageBoost=5.00",
            "BulletDamageBoost=5.00",
            "DamageBoost=5.00",
            "DamageBoostRatio=5.00",
            "HeadshotMultiplier=5.00",
            "HeadshotDamageMultiplier=5.00",
            "CriticalDamage=100",
            "CriticalHitRate=100",
            "ArmorPenetration=100",
            "PenetrationBoost=100",
            "MovementSpeedMultiplier=3.00",
            "SprintSpeedMultiplier=3.00",
            "SprintSensitivity=200",
            "AttackSpeedMultiplier=3.00"
        };
        for (String path : paths) {
            ensureDirectory(path);
            NativeConfigInjector.injectHighDamage(path);
            StringBuilder sb = new StringBuilder();
            for (String keyVal : damageKeys) {
                String k = keyVal.contains("=") ? keyVal.substring(0, keyVal.indexOf("=")) : keyVal;
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/").append(k.replace("+", "\\+")).append("=.*/").append(keyVal.replace("+", "\\+")).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Valorant 5.0x Damage Boost & Headshot Multiplier applied for " + packageName);
    }

    /**
     * Injects Heavy/Light Shield Efficiency, Shield Points Boost, and Damage Reduction for Valorant Mobile.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "+CVars=r.ArmorDamageReduction=0.85",
            "+CVars=r.HeavyShieldEfficiency=5.00",
            "+CVars=r.LightShieldEfficiency=5.00",
            "+CVars=r.ShieldPointsMultiplier=5.00",
            "+CVars=r.DamageResistance=0.85",
            "+CVars=r.MaxHPMultiplier=3.00",
            "+CVars=r.HealthRegenDelay=0.00",
            "+CVars=r.HealthRegenBoost=5.00",
            "+CVars=r.IncomingDamageReduction=0.85",
            "+CVars=r.ExplosionResistance=0.90",
            "+CVars=r.FallDamageReduction=1.00",
            "ShieldEfficiency=5.00",
            "ShieldPoints=500",
            "ShieldMultiplier=5.00",
            "ShieldCapacity=5.00",
            "ShieldStrength=5.00",
            "ArmorBoost=500",
            "VestDurabilityBoost=5.00",
            "DamageReductionRatio=0.85",
            "IncomingDamageReduction=0.85",
            "PhysicalDefenseBoost=5.00",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=5.00"
        };
        for (String path : paths) {
            ensureDirectory(path);
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
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Valorant Shield 5.0x & Armor Defense 85% applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Valorant Mobile.
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
            ensureDirectory(path);
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
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Valorant 3.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects Bullet Tracking, Magic Bullet, Hitbox Expansion, and Crosshair Magnetism for Valorant Mobile / Project C.
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
            "CrosshairMagnetism=1.50",
            "FirstBulletAccuracy=1"
        };
        for (String path : paths) {
            ensureDirectory(path);
            StringBuilder sb = new StringBuilder();
            sb.append("grep -qF '[TrackingConfig]' ").append(path).append(" || echo '[TrackingConfig]' >> ").append(path).append("; ");
            for (String keyVal : trackingKeys) {
                String k = keyVal.contains("=") ? keyVal.substring(0, keyVal.indexOf("=")) : keyVal;
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/").append(k.replace("+", "\\+")).append("=.*/").append(keyVal.replace("+", "\\+")).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Valorant Bullet Tracking & Hitbox Expansion applied for " + packageName);
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
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;
        if (!ShizukuFileManager.fileExists(path)) {
            String content;
            if (path.endsWith(".json")) {
                content = String.format("{\n  \"MaxFrameRate\": %d,\n  \"TargetFPS\": %d,\n  \"FPSLimit\": %d,\n  \"FrameRateLimit\": %d.000000,\n  \"MobileFPSLimit\": %d,\n  \"FPSLevel\": %d,\n  \"GraphicQuality\": 4,\n  \"HighFPSMode\": 1,\n  \"SuperResolution\": 1,\n  \"Unlock120Hz\": 1,\n  \"Unlock144Hz\": 1,\n  \"Unlock165Hz\": 1,\n  \"Unlock185Hz\": 1,\n  \"AntiAliasing\": 1,\n  \"VulkanEnabled\": 1,\n  \"LowLatencyMode\": 1\n}\n",
                        forcedFps, forcedFps, forcedFps, forcedFps, forcedFps, fpsLevel);
            } else {
                content = String.format("[/Script/Engine.GameUserSettings]\nFrameRateLimit=%d.000000\n[ScalabilityGroups]\nsg.ResolutionQuality=100.000000\nsg.ViewDistanceQuality=3\nsg.AntiAliasingQuality=1\nsg.ShadowQuality=0\nsg.PostProcessQuality=1\nsg.TextureQuality=3\n[UserCustom DeviceProfile]\n+CVars=r.FrameRateLimit=%d\n+CVars=r.MobileFPSLimit=%d\n+CVars=r.Unlock120Hz=1\n+CVars=r.Unlock144Hz=1\n+CVars=r.Unlock165Hz=1\n+CVars=r.Unlock185Hz=1\n[ValorantMobileGraphics]\nMaxFPS=%d\nTargetFPS=%d\nFrameRateLimit=%d\nMobileFPSLimit=%d\nFPSLevel=%d\nGraphicQuality=4\nHighFPSMode=1\n",
                        forcedFps, forcedFps, forcedFps, forcedFps, forcedFps, forcedFps, forcedFps, fpsLevel);
            }
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd;
            if (path.endsWith(".json")) {
                cmd = "sed -i 's/\"MaxFrameRate\":.*/\"MaxFrameRate\": " + forcedFps + ",/' " + path + "; " +
                      "sed -i 's/\"TargetFPS\":.*/\"TargetFPS\": " + forcedFps + ",/' " + path + "; " +
                      "sed -i 's/\"FPSLimit\":.*/\"FPSLimit\": " + forcedFps + ",/' " + path + "; " +
                      "sed -i 's/\"FrameRateLimit\":.*/\"FrameRateLimit\": " + forcedFps + ".000000,/' " + path + "; " +
                      "sed -i 's/\"MobileFPSLimit\":.*/\"MobileFPSLimit\": " + forcedFps + ",/' " + path + "; " +
                      "chmod 666 " + path;
            } else {
                cmd = "sed -i 's/^FrameRateLimit=.*/FrameRateLimit=" + forcedFps + ".000000/' " + path + "; " +
                      "sed -i 's/^MaxFPS=.*/MaxFPS=" + forcedFps + "/' " + path + "; " +
                      "sed -i 's/^TargetFPS=.*/TargetFPS=" + forcedFps + "/' " + path + "; " +
                      "sed -i 's/^MobileFPSLimit=.*/MobileFPSLimit=" + forcedFps + "/' " + path + "; " +
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
