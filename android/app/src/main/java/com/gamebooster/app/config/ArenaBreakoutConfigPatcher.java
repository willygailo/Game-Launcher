package com.gamebooster.app.config;

import android.util.Log;
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
            if (ConfigFileHelper.writeContentAtomic(path, ueContent)) {
                written++;
            }
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
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[UserCustom DeviceProfile]");
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
        String[] touchKeys = {
            "TouchRate=1000",
            "TouchResponse=1",
            "TouchSlopReduction=1",
            "TouchZeroDelay=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, touchKeys, "[TouchEngine]");
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
            ConfigFileHelper.patchKeys(path, aimKeys, "[UserCustom DeviceProfile]");
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
            NativeConfigInjector.injectNoRecoil(path);
            ConfigFileHelper.patchKeys(path, recoilKeys, "[UserCustom DeviceProfile]");
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
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[DefenseConfig]");
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
            NativeConfigInjector.injectSpeedBoost(path);
            ConfigFileHelper.patchKeys(path, speedKeys, "[SpeedEngine]");
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
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "ArenaBreakout Bullet Tracking & Hitbox Expansion applied for " + packageName);
    }

    private static boolean applyStandardPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;
        String[] keys = {
            "FrameRateLimit=" + forcedFps + ".000000",
            "MaxFPS=" + forcedFps,
            "TargetFPS=" + forcedFps,
            "FPSLevel=" + fpsLevel,
            "FrameRateLevel=" + fpsLevel,
            "+CVars=r.FrameRateLimit=" + forcedFps,
            "+CVars=r.MobileFPSLimit=" + forcedFps,
            "+CVars=r.PUBGDeviceFPS=" + fpsLevel,
            "+CVars=r.Unlock120Hz=1",
            "+CVars=r.Unlock144Hz=1",
            "+CVars=r.Unlock165Hz=1",
            "+CVars=r.Unlock185Hz=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[/Script/Engine.GameUserSettings]");
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }
}
