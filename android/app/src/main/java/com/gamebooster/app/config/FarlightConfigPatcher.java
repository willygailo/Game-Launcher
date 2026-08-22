package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * FarlightConfigPatcher manages internal UE4 Solarland config files for
 * Farlight 84 (all global and regional package releases).
 *
 * Configures 120 / 144 / 165 / 185 FPS unlock, 1000Hz touch & gyro polling,
 * zero input lag, recoil control, and performance rendering pipeline.
 */
public class FarlightConfigPatcher {

    private static final String TAG = "FarlightConfigPatcher";

    // ─── Standard Patch ───────────────────────────────────────────────────────

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Farlight 84 patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL Farlight 84 config paths unconditionally.
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
                        "  \"FrameRateLimit\": " + forcedFps + ",\n" +
                        "  \"MaxFPS\": " + forcedFps + ",\n" +
                        "  \"TargetFPS\": " + forcedFps + ",\n" +
                        "  \"FPS\": " + forcedFps + ",\n" +
                        "  \"MobileFPSLimit\": " + forcedFps + ",\n" +
                        "  \"FPSLevel\": " + fpsLevel + ",\n" +
                        "  \"GraphicQuality\": 3,\n" +
                        "  \"HighFPSMode\": 1,\n" +
                        "  \"Unlock185Hz\": 1,\n" +
                        "  \"Unlock165Hz\": 1,\n" +
                        "  \"Unlock144Hz\": 1,\n" +
                        "  \"Unlock120Hz\": 1,\n" +
                        "  \"TouchPollingRate\": 1000,\n" +
                        "  \"TouchBoostHz\": " + forcedFps + ",\n" +
                        "  \"TouchZeroDelay\": 1,\n" +
                        "  \"GyroPollingRate\": 1000,\n" +
                        "  \"AimAssist\": 1,\n" +
                        "  \"AimAssistStrength\": 10000,\n" +
                        "  \"AimAssistLevel\": 10,\n" +
                        "  \"AimPrecision\": 100,\n" +
                        "  \"TargetLockSensitivity\": 10000,\n" +
                        "  \"CrosshairMagnetism\": 100.00,\n" +
                        "  \"AimSnapStrength\": 100.00,\n" +
                        "  \"AimMagnetism\": 100.00,\n" +
                        "  \"TrackingBullet\": 1,\n" +
                        "  \"BulletTracking\": 1,\n" +
                        "  \"AutoTrackingBullet\": 1,\n" +
                        "  \"MagicBullet\": 1,\n" +
                        "  \"HitboxExpansion\": 100.00,\n" +
                        "  \"BulletMagnetism\": 100.00,\n" +
                        "  \"ProjectileHoming\": 1,\n" +
                        "  \"HomingStrength\": 100.00,\n" +
                        "  \"BulletCurveFactor\": 100.00,\n" +
                        "  \"BulletVelocityMultiplier\": 200.00,\n" +
                        "  \"PhysicalDefenseBoost\": 1000.00,\n" +
                        "  \"MagicDefenseBoost\": 1000.00,\n" +
                        "  \"DamageReductionRatio\": 0.9999,\n" +
                        "  \"DamageReduction\": 0.9999,\n" +
                        "  \"IncomingDamageReduction\": 0.9999,\n" +
                        "  \"ShieldMultiplier\": 1500.00,\n" +
                        "  \"ShieldCapacity\": 1500.00,\n" +
                        "  \"ArmorBoost\": 50000,\n" +
                        "  \"VestDurability\": 1500.00,\n" +
                        "  \"HelmetDamageReduction\": 0.9999,\n" +
                        "  \"TenacityRatio\": 0.9999,\n" +
                        "  \"DamageMultiplier\": 1000.00,\n" +
                        "  \"DamageBoostRatio\": 1000.00,\n" +
                        "  \"BulletDamageBoost\": 1000.00,\n" +
                        "  \"HeadshotDamageMultiplier\": 1000.00,\n" +
                        "  \"CriticalHitRate\": 100,\n" +
                        "  \"CriticalDamage\": 10000,\n" +
                        "  \"DroneView\": 1,\n" +
                        "  \"DroneViewHeight\": 4,\n" +
                        "  \"CameraFOV\": 180,\n" +
                        "  \"RecoilReduction\": 1.00,\n" +
                        "  \"LowLatencyMode\": 1,\n" +
                        "  \"AntiAliasing\": 1\n" +
                        "}\n";
            } else {
                // UE4 INI format (Solarland / GameUserSettings.ini / UserCustom.ini)
                content = "[/Script/Engine.GameUserSettings]\n" +
                        "bUseVSync=False\n" +
                        "FrameRateLimit=" + forcedFps + ".000000\n" +
                        "ResolutionSizeX=2400\n" +
                        "ResolutionSizeY=1080\n" +
                        "WindowMode=0\n" +
                        "[ScalabilityGroups]\n" +
                        "sg.ResolutionQuality=100.000000\n" +
                        "sg.ViewDistanceQuality=3\n" +
                        "sg.AntiAliasingQuality=1\n" +
                        "sg.ShadowQuality=0\n" +
                        "sg.PostProcessQuality=1\n" +
                        "sg.TextureQuality=3\n" +
                        "sg.EffectsQuality=1\n" +
                        "[UserCustom DeviceProfile]\n" +
                        "+CVars=r.Solarland.MaxFPS=" + forcedFps + "\n" +
                        "+CVars=r.FrameRateLimit=" + forcedFps + "\n" +
                        "+CVars=r.MobileFPSLimit=" + forcedFps + "\n" +
                        "+CVars=r.Unlock120Hz=1\n" +
                        "+CVars=r.Unlock144Hz=1\n" +
                        "+CVars=r.Unlock165Hz=1\n" +
                        "+CVars=r.Unlock185Hz=1\n" +
                        "+CVars=r.AimAssist=1\n" +
                        "+CVars=r.AimAssist.Strength=100.00\n" +
                        "+CVars=r.AimAssist.Magnetism=100.00\n" +
                        "+CVars=r.AimAssist.SnapSpeed=100.00\n" +
                        "+CVars=r.AimAssistRadius=5000\n" +
                        "+CVars=r.CrosshairMagnetism=100.00\n" +
                        "+CVars=r.TargetLockSensitivity=10000\n" +
                        "+CVars=r.AimSnapStrength=100.00\n" +
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
                        "+CVars=r.ArmorDamageReduction=0.9999\n" +
                        "+CVars=r.ShieldEfficiency=1500.00\n" +
                        "+CVars=r.ShieldMultiplier=1500.00\n" +
                        "+CVars=r.ShieldRechargeRate=1000.00\n" +
                        "+CVars=r.ShieldCapacityBoost=1500.00\n" +
                        "+CVars=r.DamageResistance=0.9999\n" +
                        "+CVars=r.VestDurabilityBoost=1500.00\n" +
                        "+CVars=r.HelmetDamageReduction=0.9999\n" +
                        "+CVars=r.MaxHPMultiplier=100.00\n" +
                        "+CVars=r.IncomingDamageScale=0.0001\n" +
                        "+CVars=r.HeavyDamageDampener=100.00\n" +
                        "+CVars=r.BurstDamageReduction=100.00\n" +
                        "+CVars=r.CameraFOV=180\n" +
                        "+CVars=r.FieldOfView=180\n" +
                        "+CVars=r.DroneViewHeight=4\n" +
                        "[SolarlandGraphics]\n" +
                        "FrameRateLimit=" + forcedFps + "\n" +
                        "MaxFPS=" + forcedFps + "\n" +
                        "TargetFPS=" + forcedFps + "\n" +
                        "FPS=" + forcedFps + "\n" +
                        "MobileFPSLimit=" + forcedFps + "\n" +
                        "FPSLevel=" + fpsLevel + "\n" +
                        "HighFPSMode=1\n" +
                        "Unlock185Hz=1\n" +
                        "Unlock165Hz=1\n" +
                        "Unlock144Hz=1\n" +
                        "Unlock120Hz=1\n" +
                        "TouchPollingRate=1000\n" +
                        "TouchBoostHz=" + forcedFps + "\n" +
                        "TouchZeroDelay=1\n" +
                        "GyroPollingRate=1000\n" +
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
                        "DroneViewHeight=4\n" +
                        "CameraFOV=180\n" +
                        "RecoilReduction=1.00\n" +
                        "WeaponKickScale=0.00\n" +
                        "ZeroInputLag=1\n";
            }
            if (ConfigFileHelper.writeContentAtomic(path, content)) {
                written++;
            }
        }
        Log.i(TAG, "Farlight 84 competitive " + forcedFps + "FPS + 1000% Aim/Tracking/Defense force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] touchKeys = {
            "TouchBoostHz=185",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "LowLatencyMode=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, touchKeys, "[TouchEngine]");
        }
        Log.i(TAG, "Farlight 84 fast zero-delay touch applied for " + packageName);
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
            "TargetLock=1",
            "TargetLockSensitivity=10000",
            "AimAssistRadius=5000",
            "CrosshairMagnetism=100.00",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00",
            "GyroSensitivity=1000"
        };
        for (String path : paths) {
            NativeConfigInjector.injectAimAssist(path);
            ConfigFileHelper.patchKeys(path, aimKeys, "[AimAssist]");
        }
        Log.i(TAG, "Farlight 84 10000 Aim Assist & Gyro 1000Hz applied for " + packageName);
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
            "+CVars=r.ScreenShake=0",
            "+CVars=r.WeaponKick=0",
            "+CVars=r.CameraShake=0",
            "+CVars=r.AimPunchMultiplier=0.00",
            "+CVars=r.FlinchMultiplier=0.00",
            "+CVars=r.ScopeShakeReduction=1.50",
            "+CVars=r.ScopeStability=5.00",
            "+CVars=r.ScopeRecoilMultiplier=0.00",
            "+CVars=r.SpreadScale=0.00",
            "+CVars=r.CrosshairSpread=0.00",
            "+CVars=r.BulletSpread=0.00",
            "RecoilControl=1",
            "ZeroRecoil=1",
            "NoRecoil=1",
            "RecoilScale=0.00",
            "RecoilReduction=1.00",
            "WeaponStability=500"
        };
        for (String path : paths) {
            NativeConfigInjector.injectNoRecoil(path);
            ConfigFileHelper.patchKeys(path, recoilKeys, "[WeaponStability]");
        }
        Log.i(TAG, "Farlight 84 Zero Recoil & Weapon Stability applied for " + packageName);
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageKeys = {
            "+CVars=r.DamageMultiplier=1000.00",
            "+CVars=r.BulletDamageScale=1000.00",
            "+CVars=r.PhysicalDamageBoost=1000.00",
            "+CVars=r.DamageBoost=1000.00",
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
            "DamageBoost=1000.00",
            "HeadshotDamageMultiplier=1000.00",
            "CriticalHitRate=100",
            "CriticalDamage=10000",
            "ArmorPenetration=10000",
            "MovementSpeedMultiplier=15.00",
            "SprintSpeedMultiplier=15.00",
            "SprintSensitivity=1000",
            "AttackSpeedMultiplier=25.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[UserCustom DeviceProfile]");
        }
        Log.i(TAG, "Farlight 84 1000% Damage Boost & Headshot Multiplier applied for " + packageName);
    }

    public static void applyFastCooldownConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] cdKeys = {
            "+CVars=r.CooldownReduction=0.99",
            "+CVars=r.SkillResponseZeroDelay=1",
            "+CVars=r.InstantCast=1",
            "+CVars=r.ReloadSpeedMultiplier=25.00",
            "+CVars=r.JetpackRechargeMultiplier=100.00",
            "SkillCoolDownReduceMode=1",
            "CooldownReductionBoost=0.99",
            "SkillCooldownMultiplier=0.01",
            "ReloadSpeedMultiplier=25.00",
            "JetpackCooldownReduction=0.99",
            "UnlimitedEnergy=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectFastCooldown(path);
            ConfigFileHelper.patchKeys(path, cdKeys, "[FastCooldown]");
        }
        Log.i(TAG, "Farlight 84 Fast Cooldown & Infinite Jetpack applied for " + packageName);
    }

    public static void applyShield1500Config(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] shieldKeys = {
            "+CVars=r.ArmorDamageReduction=0.9999",
            "+CVars=r.ShieldMultiplier=1500.00",
            "+CVars=r.ShieldEfficiency=1500.00",
            "+CVars=r.ShieldCapacityBoost=1500.00",
            "+CVars=r.DamageResistance=0.9999",
            "+CVars=r.VestDurabilityBoost=1500.00",
            "ShieldMultiplier=1500.00",
            "ShieldCapacity=1500.00",
            "ShieldEfficiency=1500.00",
            "ArmorBoost=50000",
            "DamageReduction=0.9999",
            "IncomingDamageReduction=0.9999"
        };
        for (String path : paths) {
            NativeConfigInjector.injectShield1500(path);
            ConfigFileHelper.patchKeys(path, shieldKeys, "[DefenseShield1500]");
        }
        Log.i(TAG, "Farlight 84 1500+ Shield Overdrive applied for " + packageName);
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
        Log.i(TAG, "Farlight 84 Drone View Ultra FOV 180 applied for " + packageName);
    }

    /**
     * Injects 1000% Shield Recharge Rate, Shield Efficiency, and Armor Damage Reduction for Farlight 84.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "+CVars=r.ArmorDamageReduction=0.9999",
            "+CVars=r.ShieldRechargeRate=1000.00",
            "+CVars=r.ShieldEfficiency=1500.00",
            "+CVars=r.ShieldCapacityBoost=1500.00",
            "+CVars=r.DamageResistance=0.9999",
            "+CVars=r.VestDurabilityBoost=1500.00",
            "+CVars=r.HelmetDamageReduction=0.9999",
            "+CVars=r.MaxHPMultiplier=100.00",
            "+CVars=r.HealthRegenDelay=0.00",
            "+CVars=r.HealthRegenBoost=1000.00",
            "+CVars=r.IncomingDamageReduction=0.9999",
            "+CVars=r.ExplosionResistance=0.9999",
            "+CVars=r.FallDamageReduction=1.00",
            "+CVars=r.HeavyDamageDampener=100.00",
            "+CVars=r.BurstDamageReduction=100.00",
            "ShieldEfficiency=1500.00",
            "ShieldCapacity=1500.00",
            "ShieldMultiplier=1500.00",
            "ShieldStrength=1500.00",
            "ArmorBoost=50000",
            "VestDurabilityBoost=1500.00",
            "DamageReductionRatio=0.9999",
            "DamageReduction=0.9999",
            "IncomingDamageReduction=0.9999",
            "PhysicalDefenseBoost=1000.00",
            "MagicDefenseBoost=1000.00",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=1000.00",
            "TenacityRatio=0.9999",
            "HeavyHitAbsorption=100.00",
            "BurstDamageReduction=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[DefenseConfig]");
        }
        Log.i(TAG, "Farlight 84 1000% Shield & Armor Defense applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Farlight 84.
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
        Log.i(TAG, "Farlight 84 15.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects 1000% Bullet Tracking, Magic Bullet, Hitbox Expansion, and Bullet Magnetism for Farlight 84.
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
            "CrosshairMagnetism=100.00",
            "ProjectileHoming=1",
            "HomingStrength=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectTrackingBullet(path);
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "Farlight 84 1000% Bullet Tracking & Hitbox Expansion applied for " + packageName);
    }

    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    private static boolean applyPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;
        String[] keys = {
            "FrameRateLimit=" + forcedFps + ".000000",
            "MaxFPS=" + forcedFps,
            "TargetFPS=" + forcedFps,
            "FPS=" + forcedFps,
            "MobileFPSLimit=" + forcedFps,
            "FPSLevel=" + fpsLevel,
            "+CVars=r.Solarland.MaxFPS=" + forcedFps,
            "+CVars=r.FrameRateLimit=" + forcedFps,
            "+CVars=r.MobileFPSLimit=" + forcedFps,
            "+CVars=r.Unlock120Hz=1",
            "+CVars=r.Unlock144Hz=1",
            "+CVars=r.Unlock165Hz=1",
            "+CVars=r.Unlock185Hz=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[SolarlandGraphics]");
    }
}
