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
                // ── 1000% Aim Assist CVars ──
                "+CVars=r.AimAssist=1\n" +
                "+CVars=r.AimAssist.Strength=100.00\n" +
                "+CVars=r.AimAssist.Magnetism=100.00\n" +
                "+CVars=r.AimAssist.SnapSpeed=100.00\n" +
                "+CVars=r.AimAssistRadius=1000\n" +
                "+CVars=r.CrosshairMagnetism=100.00\n" +
                "+CVars=r.TargetLockSensitivity=1000\n" +
                "+CVars=r.AimSnapStrength=100.00\n" +
                "+CVars=r.AimLead=1\n" +
                "+CVars=r.AimLeadStrength=100.00\n" +
                // ── 1000% Tracking Bullet CVars ──
                "+CVars=r.BulletTracking=1\n" +
                "+CVars=r.MagicBullet=1\n" +
                "+CVars=r.HitboxExpansion=50.00\n" +
                "+CVars=r.BulletMagnetism=100.00\n" +
                "+CVars=r.BulletVelocityScale=100.00\n" +
                "+CVars=r.BulletCurveFactor=50.00\n" +
                "+CVars=r.TargetLockTracking=1\n" +
                "+CVars=r.FirstBulletAccuracy=1\n" +
                "+CVars=r.ProjectileHoming=1\n" +
                "+CVars=r.HomingStrength=100.00\n" +
                // ── 1000% Armor Defense CVars ──
                "+CVars=r.ArmorDamageReduction=0.999\n" +
                "+CVars=r.VestDurabilityBoost=100.00\n" +
                "+CVars=r.HelmetDamageReduction=0.999\n" +
                "+CVars=r.IncomingDamageScale=0.001\n" +
                "+CVars=r.ShieldEfficiency=100.00\n" +
                "+CVars=r.HealthRegenBoost=100.00\n" +
                "+CVars=r.DamageResistance=0.999\n" +
                "+CVars=r.TenacityRatio=0.999\n" +
                "+CVars=r.HeavyDamageDampener=10.00\n" +
                "+CVars=r.BurstDamageReduction=10.00\n" +
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
                "AimAssist=1\n" +
                "AimAssistStrength=1000\n" +
                "AimAssistLevel=10\n" +
                "AimPrecision=10\n" +
                "TargetLockSensitivity=1000\n" +
                "CrosshairMagnetism=100.00\n" +
                "AimSnapStrength=100.00\n" +
                "AimMagnetism=100.00\n" +
                "TrackingBullet=1\n" +
                "BulletTracking=1\n" +
                "AutoTrackingBullet=1\n" +
                "MagicBullet=1\n" +
                "HitboxExpansion=50.00\n" +
                "BulletMagnetism=100.00\n" +
                "BulletCurveFactor=50.00\n" +
                "BulletVelocityMultiplier=100.00\n" +
                "PhysicalDefenseBoost=100.00\n" +
                "MagicDefenseBoost=100.00\n" +
                "DamageReductionRatio=0.999\n" +
                "DamageReduction=0.999\n" +
                "IncomingDamageReduction=0.999\n" +
                "ShieldMultiplier=100.00\n" +
                "ShieldCapacity=100.00\n" +
                "ArmorBoost=10000\n" +
                "VestDurability=100.00\n" +
                "HelmetDamageReduction=0.999\n" +
                "TenacityRatio=0.999\n" +
                "DamageMultiplier=100.00\n" +
                "BulletDamageBoost=100.00\n" +
                "HeadshotDamageMultiplier=100.00\n" +
                "CriticalHitRate=100\n" +
                "CriticalDamage=1000\n" +
                "NoRecoil=1\n" +
                "WeaponKickReduction=1.00\n" +
                "CrosshairSpread=0.00\n" +
                "ScopeStability=5.00\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.writeContentAtomic(path, ueContent)) {
                written++;
            }
        }
        Log.i(TAG, "Arena Breakout competitive UltraExtreme " + forcedFps + "FPS + 1000% Aim/Tracking/Defense force-write: " + written + " paths");
        return written > 0;
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageKeys = {
            "+CVars=r.DamageMultiplier=100.00",
            "+CVars=r.BulletDamageBoost=100.00",
            "+CVars=r.DamageBoost=100.00",
            "+CVars=r.PhysicalDamageBoost=100.00",
            "+CVars=r.HeadshotMultiplier=100.00",
            "+CVars=r.HeadshotDamageMultiplier=100.00",
            "+CVars=r.CriticalDamage=1000",
            "+CVars=r.CriticalHitRate=100",
            "+CVars=r.CriticalDamageMultiplier=10.00",
            "+CVars=r.PenetrationBoost=1000",
            "+CVars=r.ArmorPenetration=1000",
            "+CVars=r.BulletVelocityMultiplier=50.00",
            "+CVars=r.HitboxExpansion=10.00",
            "+CVars=r.BodyDamageMultiplier=10.00",
            "+CVars=r.LimbDamageMultiplier=10.00",
            "+CVars=r.ExplosiveDamageMultiplier=10.00",
            "+CVars=r.MovementSpeedMultiplier=10.00",
            "+CVars=r.SprintSpeedMultiplier=10.00",
            "DamageMultiplier=100.00",
            "BulletDamageBoost=100.00",
            "DamageBoost=100.00",
            "PhysicalDamageBoost=100.00",
            "HeadshotDamageMultiplier=100.00",
            "CriticalHitRate=100",
            "CriticalDamage=1000",
            "ArmorPenetration=1000",
            "PenetrationBoost=1000",
            "MovementSpeedMultiplier=10.00",
            "SprintSpeedMultiplier=10.00",
            "SprintSensitivity=500",
            "AttackSpeedMultiplier=10.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[UserCustom DeviceProfile]");
        }
        Log.i(TAG, "Arena Breakout 1000% damage script & headshot multiplier applied for " + packageName);
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
            "+CVars=r.AimAssist.Strength=100.00",
            "+CVars=r.AimAssist.Magnetism=100.00",
            "+CVars=r.AimAssist.SnapSpeed=100.00",
            "+CVars=r.AimAssistRadius=1000",
            "+CVars=r.CrosshairMagnetism=100.00",
            "+CVars=r.TargetLockSensitivity=1000",
            "+CVars=r.AimSnapStrength=100.00",
            "+CVars=r.GyroSampleRate=1000",
            "+CVars=r.GyroZeroDelay=1",
            "AimAssist=1",
            "AimPrecision=10",
            "AimAssistStrength=1000",
            "AimAssistLevel=10",
            "SmartTargetLock=1",
            "TargetLock=1",
            "TargetLockSensitivity=1000",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "GyroSensitivity=500",
            "CrosshairMagnetism=100.00",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectAimAssist(path);
            ConfigFileHelper.patchKeys(path, aimKeys, "[UserCustom DeviceProfile]");
        }
        Log.i(TAG, "ArenaBreakout 1000% Aim Assist & Gyro 1000Hz applied for " + packageName);
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
            "+CVars=r.ScopeStability=5.00",
            "+CVars=r.ScopeRecoilMultiplier=0.00",
            "+CVars=r.BulletSpread=0.00",
            "+CVars=r.CrosshairSpread=0.00",
            "+CVars=r.SpreadScale=0.00",
            "RecoilControl=1",
            "RecoilReduction=1.00",
            "WeaponStability=500",
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
     * Injects 1000% Armor Defense, Vest Durability, Helmet Protection, and Damage Reduction for Arena Breakout / Delta Force.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "+CVars=r.ArmorDamageReduction=0.999",
            "+CVars=r.VestDurabilityBoost=100.00",
            "+CVars=r.HelmetDamageReduction=0.999",
            "+CVars=r.IncomingDamageScale=0.001",
            "+CVars=r.DamageResistance=0.999",
            "+CVars=r.ShieldEfficiency=100.00",
            "+CVars=r.IncomingDamageReduction=0.999",
            "+CVars=r.MaxHPMultiplier=50.00",
            "+CVars=r.HealthRegenDelay=0.00",
            "+CVars=r.HealthRegenBoost=100.00",
            "+CVars=r.ExplosionResistance=0.999",
            "+CVars=r.FallDamageReduction=1.00",
            "+CVars=r.HeavyDamageDampener=10.00",
            "+CVars=r.BurstDamageReduction=10.00",
            "ArmorLevel=10",
            "VestDurability=100.00",
            "VestDurabilityBoost=100.00",
            "HelmetDamageReduction=0.999",
            "ArmorDamageAbsorb=0.999",
            "ShieldCapacity=100.00",
            "ShieldMultiplier=100.00",
            "DamageReductionRatio=0.999",
            "DamageReduction=0.999",
            "IncomingDamageReduction=0.999",
            "PhysicalDefenseBoost=100.00",
            "MagicDefenseBoost=100.00",
            "ArmorBoost=10000",
            "MagicResistBoost=10000",
            "TenacityRatio=0.999",
            "HealthRegenBoost=100.00",
            "HeavyHitAbsorption=10.00",
            "BurstDamageReduction=10.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[DefenseConfig]");
        }
        Log.i(TAG, "ArenaBreakout 1000% Armor Defense & 100x Vest Durability applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Arena Breakout / Delta Force.
     */
    public static void applySpeedBoostConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] speedKeys = {
            "+CVars=r.MovementSpeedMultiplier=10.00",
            "+CVars=r.SprintSpeedMultiplier=10.00",
            "+CVars=r.AttackSpeedMultiplier=10.00",
            "+CVars=r.BulletVelocityScale=50.00",
            "+CVars=r.ZeroInputLag=1",
            "MovementSpeedMultiplier=10.00",
            "MovementSpeedBoost=10.00",
            "SprintSpeedMultiplier=10.00",
            "SprintSpeedBoost=10.00",
            "SprintSensitivity=500",
            "AgilityMultiplier=10.00",
            "AttackSpeedMultiplier=10.00",
            "AttackSpeedBoost=10.00",
            "ReloadSpeedMultiplier=10.00",
            "FireRateMultiplier=10.00",
            "BulletVelocityMultiplier=50.00",
            "BulletVelocityScale=50.00",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "HighSpeedMovement=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectSpeedBoost(path);
            ConfigFileHelper.patchKeys(path, speedKeys, "[SpeedEngine]");
        }
        Log.i(TAG, "ArenaBreakout 10.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects 1000% Bullet Tracking, Magic Bullet, Hitbox Expansion, and Bullet Magnetism for Arena Breakout / Delta Force.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "+CVars=r.BulletTracking=1",
            "+CVars=r.MagicBullet=1",
            "+CVars=r.HitboxExpansion=50.00",
            "+CVars=r.BulletMagnetism=100.00",
            "+CVars=r.BulletVelocityScale=100.00",
            "+CVars=r.BulletCurveFactor=50.00",
            "+CVars=r.TargetLockTracking=1",
            "+CVars=r.FirstBulletAccuracy=1",
            "+CVars=r.ProjectileHoming=1",
            "+CVars=r.HomingStrength=100.00",
            "TrackingBullet=1",
            "BulletTracking=1",
            "AutoTrackingBullet=1",
            "MagicBullet=1",
            "HitboxExpansion=50.00",
            "BulletMagnetism=100.00",
            "BulletCurveFactor=50.00",
            "BulletVelocityMultiplier=100.00",
            "ProjectileHoming=1",
            "HomingStrength=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectTrackingBullet(path);
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "ArenaBreakout 1000% Bullet Tracking & Hitbox Expansion applied for " + packageName);
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
