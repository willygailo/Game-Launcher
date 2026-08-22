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
                // ── 10000 Aim Assist CVars ──
                "+CVars=r.AimAssist=1\n" +
                "+CVars=r.AimAssist.Strength=100.00\n" +
                "+CVars=r.AimAssist.Magnetism=100.00\n" +
                "+CVars=r.AimAssist.SnapSpeed=100.00\n" +
                "+CVars=r.AimAssistRadius=5000\n" +
                "+CVars=r.CrosshairMagnetism=100.00\n" +
                "+CVars=r.TargetLockSensitivity=10000\n" +
                "+CVars=r.AimSnapStrength=100.00\n" +
                "+CVars=r.AimLead=1\n" +
                "+CVars=r.AimLeadStrength=100.00\n" +
                // ── 100 Hitbox & Tracking Bullet CVars ──
                "+CVars=r.BulletTracking=1\n" +
                "+CVars=r.MagicBullet=1\n" +
                "+CVars=r.HitboxExpansion=100.00\n" +
                "+CVars=r.BulletMagnetism=100.00\n" +
                "+CVars=r.BulletVelocityScale=200.00\n" +
                "+CVars=r.BulletCurveFactor=100.00\n" +
                "+CVars=r.TargetLockTracking=1\n" +
                "+CVars=r.FirstBulletAccuracy=1\n" +
                "+CVars=r.ProjectileHoming=1\n" +
                "+CVars=r.HomingStrength=100.00\n" +
                // ── 1500+ Shield & Armor Defense CVars ──
                "+CVars=r.ArmorDamageReduction=0.9999\n" +
                "+CVars=r.VestDurabilityBoost=1500.00\n" +
                "+CVars=r.HelmetDamageReduction=0.9999\n" +
                "+CVars=r.IncomingDamageScale=0.0001\n" +
                "+CVars=r.ShieldMultiplier=1500.00\n" +
                "+CVars=r.ShieldEfficiency=1500.00\n" +
                "+CVars=r.HealthRegenBoost=1000.00\n" +
                "+CVars=r.DamageResistance=0.9999\n" +
                "+CVars=r.TenacityRatio=0.9999\n" +
                "+CVars=r.HeavyDamageDampener=100.00\n" +
                "+CVars=r.BurstDamageReduction=100.00\n" +
                // ── Drone View FOV 180 ──
                "+CVars=r.CameraFOV=180\n" +
                "+CVars=r.FieldOfView=180\n" +
                "+CVars=r.DroneViewHeight=4\n" +
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
                "AimAssistStrength=10000\n" +
                "AimAssistLevel=10\n" +
                "AimPrecision=100\n" +
                "TargetLockSensitivity=10000\n" +
                "CrosshairMagnetism=100.00\n" +
                "AimSnapStrength=100.00\n" +
                "AimMagnetism=100.00\n" +
                "TrackingBullet=1\n" +
                "BulletTracking=1\n" +
                "AutoTrackingBullet=1\n" +
                "MagicBullet=1\n" +
                "HitboxExpansion=100.00\n" +
                "BulletMagnetism=100.00\n" +
                "BulletCurveFactor=100.00\n" +
                "BulletVelocityMultiplier=200.00\n" +
                "PhysicalDefenseBoost=1000.00\n" +
                "MagicDefenseBoost=1000.00\n" +
                "DamageReductionRatio=0.9999\n" +
                "DamageReduction=0.9999\n" +
                "IncomingDamageReduction=0.9999\n" +
                "ShieldMultiplier=1500.00\n" +
                "ShieldCapacity=1500.00\n" +
                "ArmorBoost=50000\n" +
                "VestDurability=1500.00\n" +
                "HelmetDamageReduction=0.9999\n" +
                "TenacityRatio=0.9999\n" +
                "DamageMultiplier=1000.00\n" +
                "BulletDamageBoost=1000.00\n" +
                "HeadshotDamageMultiplier=1000.00\n" +
                "CriticalHitRate=100\n" +
                "CriticalDamage=10000\n" +
                "DroneView=1\n" +
                "CameraFOV=180\n" +
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
            "+CVars=r.DamageMultiplier=1000.00",
            "+CVars=r.BulletDamageBoost=1000.00",
            "+CVars=r.DamageBoost=1000.00",
            "+CVars=r.PhysicalDamageBoost=1000.00",
            "+CVars=r.HeadshotMultiplier=1000.00",
            "+CVars=r.HeadshotDamageMultiplier=1000.00",
            "+CVars=r.CriticalDamage=10000",
            "+CVars=r.CriticalHitRate=100",
            "+CVars=r.CriticalDamageMultiplier=50.00",
            "+CVars=r.PenetrationBoost=10000",
            "+CVars=r.ArmorPenetration=10000",
            "+CVars=r.BulletVelocityMultiplier=200.00",
            "+CVars=r.HitboxExpansion=100.00",
            "+CVars=r.BodyDamageMultiplier=50.00",
            "+CVars=r.LimbDamageMultiplier=50.00",
            "+CVars=r.ExplosiveDamageMultiplier=50.00",
            "+CVars=r.MovementSpeedMultiplier=15.00",
            "+CVars=r.SprintSpeedMultiplier=15.00",
            "DamageMultiplier=1000.00",
            "BulletDamageBoost=1000.00",
            "DamageBoost=1000.00",
            "PhysicalDamageBoost=1000.00",
            "HeadshotDamageMultiplier=1000.00",
            "CriticalHitRate=100",
            "CriticalDamage=10000",
            "ArmorPenetration=10000",
            "PenetrationBoost=10000",
            "MovementSpeedMultiplier=15.00",
            "SprintSpeedMultiplier=15.00",
            "SprintSensitivity=1000",
            "AttackSpeedMultiplier=25.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[UserCustom DeviceProfile]");
        }
        Log.i(TAG, "Arena Breakout 1000% damage script & headshot multiplier applied for " + packageName);
    }

    public static void applyFastCooldownConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] cdKeys = {
            "+CVars=r.CooldownReduction=0.99",
            "+CVars=r.SkillResponseZeroDelay=1",
            "+CVars=r.InstantCast=1",
            "+CVars=r.ReloadSpeedMultiplier=25.00",
            "SkillCoolDownReduceMode=1",
            "CooldownReductionBoost=0.99",
            "SkillCooldownMultiplier=0.01",
            "ReloadSpeedMultiplier=25.00",
            "UnlimitedMana=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectFastCooldown(path);
            ConfigFileHelper.patchKeys(path, cdKeys, "[FastCooldown]");
        }
        Log.i(TAG, "Arena Breakout Fast Cooldown 99% CDR applied for " + packageName);
    }

    public static void applyShield1500Config(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] shieldKeys = {
            "+CVars=r.ArmorDamageReduction=0.9999",
            "+CVars=r.ShieldMultiplier=1500.00",
            "+CVars=r.VestDurabilityBoost=1500.00",
            "+CVars=r.DamageResistance=0.9999",
            "+CVars=r.IncomingDamageScale=0.0001",
            "ShieldMultiplier=1500.00",
            "ShieldCapacity=1500.00",
            "VestDurabilityBoost=1500.00",
            "VestDurability=1500.00",
            "ArmorBoost=50000",
            "DamageReduction=0.9999",
            "IncomingDamageReduction=0.9999"
        };
        for (String path : paths) {
            NativeConfigInjector.injectShield1500(path);
            ConfigFileHelper.patchKeys(path, shieldKeys, "[DefenseShield1500]");
        }
        Log.i(TAG, "Arena Breakout 1500+ Shield Overdrive applied for " + packageName);
    }

    public static void applyDroneViewConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] droneKeys = {
            "+CVars=r.CameraFOV=180",
            "+CVars=r.FieldOfView=180",
            "+CVars=r.DroneViewHeight=4",
            "DroneView=1",
            "DroneViewHeight=4",
            "CameraFOV=180",
            "FieldOfView=180",
            "FOV=180"
        };
        for (String path : paths) {
            NativeConfigInjector.injectDroneView(path);
            ConfigFileHelper.patchKeys(path, droneKeys, "[DroneViewUltra]");
        }
        Log.i(TAG, "Arena Breakout Drone View Ultra FOV 180 applied for " + packageName);
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
            "+CVars=r.AimAssistRadius=5000",
            "+CVars=r.CrosshairMagnetism=100.00",
            "+CVars=r.TargetLockSensitivity=10000",
            "+CVars=r.AimSnapStrength=100.00",
            "+CVars=r.GyroSampleRate=1000",
            "+CVars=r.GyroZeroDelay=1",
            "AimAssist=1",
            "AimPrecision=100",
            "AimAssistStrength=10000",
            "AimAssistLevel=10",
            "SmartTargetLock=1",
            "TargetLock=1",
            "TargetLockSensitivity=10000",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "GyroSensitivity=1000",
            "CrosshairMagnetism=100.00",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectAimAssist(path);
            ConfigFileHelper.patchKeys(path, aimKeys, "[UserCustom DeviceProfile]");
        }
        Log.i(TAG, "ArenaBreakout 10000 Aim Assist & Gyro 1000Hz applied for " + packageName);
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
            "+CVars=r.ArmorDamageReduction=0.9999",
            "+CVars=r.VestDurabilityBoost=1500.00",
            "+CVars=r.HelmetDamageReduction=0.9999",
            "+CVars=r.IncomingDamageScale=0.0001",
            "+CVars=r.DamageResistance=0.9999",
            "+CVars=r.ShieldEfficiency=1500.00",
            "+CVars=r.IncomingDamageReduction=0.9999",
            "+CVars=r.MaxHPMultiplier=100.00",
            "+CVars=r.HealthRegenDelay=0.00",
            "+CVars=r.HealthRegenBoost=1000.00",
            "+CVars=r.ExplosionResistance=0.9999",
            "+CVars=r.FallDamageReduction=1.00",
            "+CVars=r.HeavyDamageDampener=100.00",
            "+CVars=r.BurstDamageReduction=100.00",
            "ArmorLevel=10",
            "VestDurability=1500.00",
            "VestDurabilityBoost=1500.00",
            "HelmetDamageReduction=0.9999",
            "ArmorDamageAbsorb=0.9999",
            "ShieldCapacity=1500.00",
            "ShieldMultiplier=1500.00",
            "DamageReductionRatio=0.9999",
            "DamageReduction=0.9999",
            "IncomingDamageReduction=0.9999",
            "PhysicalDefenseBoost=1000.00",
            "MagicDefenseBoost=1000.00",
            "ArmorBoost=50000",
            "MagicResistBoost=50000",
            "TenacityRatio=0.9999",
            "HealthRegenBoost=1000.00",
            "HeavyHitAbsorption=100.00",
            "BurstDamageReduction=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[DefenseConfig]");
        }
        Log.i(TAG, "ArenaBreakout 1000% Armor Defense & 1500x Vest Durability applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Arena Breakout / Delta Force.
     */
    public static void applySpeedBoostConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] speedKeys = {
            "+CVars=r.MovementSpeedMultiplier=15.00",
            "+CVars=r.SprintSpeedMultiplier=15.00",
            "+CVars=r.AttackSpeedMultiplier=25.00",
            "+CVars=r.BulletVelocityScale=200.00",
            "+CVars=r.ZeroInputLag=1",
            "MovementSpeedMultiplier=15.00",
            "MovementSpeedBoost=15.00",
            "SprintSpeedMultiplier=15.00",
            "SprintSpeedBoost=15.00",
            "SprintSensitivity=1000",
            "AgilityMultiplier=15.00",
            "AttackSpeedMultiplier=25.00",
            "AttackSpeedBoost=25.00",
            "ReloadSpeedMultiplier=25.00",
            "FireRateMultiplier=25.00",
            "BulletVelocityMultiplier=200.00",
            "BulletVelocityScale=200.00",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "HighSpeedMovement=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectSpeedBoost(path);
            ConfigFileHelper.patchKeys(path, speedKeys, "[SpeedEngine]");
        }
        Log.i(TAG, "ArenaBreakout 15.0x Speed Boost & Movement Agility applied for " + packageName);
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
            "+CVars=r.HitboxExpansion=100.00",
            "+CVars=r.BulletMagnetism=100.00",
            "+CVars=r.BulletVelocityScale=200.00",
            "+CVars=r.BulletCurveFactor=100.00",
            "+CVars=r.TargetLockTracking=1",
            "+CVars=r.FirstBulletAccuracy=1",
            "+CVars=r.ProjectileHoming=1",
            "+CVars=r.HomingStrength=100.00",
            "TrackingBullet=1",
            "BulletTracking=1",
            "AutoTrackingBullet=1",
            "MagicBullet=1",
            "HitboxExpansion=100.00",
            "BulletMagnetism=100.00",
            "BulletCurveFactor=100.00",
            "BulletVelocityMultiplier=200.00",
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
