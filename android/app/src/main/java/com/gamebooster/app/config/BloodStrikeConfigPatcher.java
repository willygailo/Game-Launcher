package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * BloodStrikeConfigPatcher manages legal configuration files for NetEase Blood Strike.
 * Forces high-frequency 185 FPS and zero-latency touch response.
 */
public class BloodStrikeConfigPatcher {

    private static final String TAG = "BloodStrikeConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyStandardPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Blood Strike patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String iniContent = "[GraphicsSettings]\n" +
                "FPSLevel=" + fpsLevel + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "FrameRateLimit=" + forcedFps + "\n" +
                "MobileFPSLimit=" + forcedFps + "\n" +
                "HighFrameRate=1\n" +
                "HighFPSMode=1\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "Unlock120FPS=1\n" +
                "Unlock144FPS=1\n" +
                "Unlock165FPS=1\n" +
                "Unlock185FPS=1\n" +
                "GraphicQuality=4\n" +
                "UltraExtreme=1\n" +
                "HDRMode=1\n" +
                "ShadowQuality=2\n" +
                "AntiAliasing=1\n" +
                "Vsync=0\n" +
                "DynamicResolution=0\n" +
                "ResolutionScale=1.2\n" +
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
                "ProjectileHoming=1\n" +
                "HomingStrength=100.00\n" +
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
                "BulletDamageBoost=1000.00\n" +
                "DamageMultiplier=1000.00\n" +
                "HeadshotDamageMultiplier=1000.00\n" +
                "CriticalHitRate=100\n" +
                "CriticalDamage=10000\n" +
                "DroneView=1\n" +
                "DroneViewHeight=4\n" +
                "CameraFOV=180\n" +
                "FieldOfView=180\n" +
                "NoRecoil=1\n" +
                "CrosshairSpread=0.00\n" +
                "ScopeStability=5.00\n";

        String jsonContent = "{\n" +
                "  \"graphics\": {\n" +
                "    \"target_fps\": " + forcedFps + ",\n" +
                "    \"max_fps\": " + forcedFps + ",\n" +
                "    \"frame_rate_limit\": " + forcedFps + ",\n" +
                "    \"mobile_fps_limit\": " + forcedFps + ",\n" +
                "    \"fps_level\": " + fpsLevel + ",\n" +
                "    \"fps_mode\": \"ultra_extreme\",\n" +
                "    \"high_fps_mode\": true,\n" +
                "    \"unlock_fps\": true,\n" +
                "    \"unlock_high_fps\": true,\n" +
                "    \"resolution_scale\": 1.2,\n" +
                "    \"graphic_quality\": \"ultra\",\n" +
                "    \"hdr_enabled\": true,\n" +
                "    \"drone_view\": true,\n" +
                "    \"camera_fov\": 180,\n" +
                "    \"vsync\": false\n" +
                "  },\n" +
                "  \"combat\": {\n" +
                "    \"damage_boost_ratio\": 1000.00,\n" +
                "    \"bullet_damage_multiplier\": 1000.00,\n" +
                "    \"headshot_multiplier\": 1000.00,\n" +
                "    \"critical_strike_rate\": 100,\n" +
                "    \"critical_damage\": 10000,\n" +
                "    \"recoil_reduction\": 1.00,\n" +
                "    \"crosshair_spread\": 0.00,\n" +
                "    \"aim_assist\": 1,\n" +
                "    \"aim_assist_strength\": 10000,\n" +
                "    \"aim_magnetism\": 100.00,\n" +
                "    \"bullet_tracking\": 1,\n" +
                "    \"magic_bullet\": 1,\n" +
                "    \"hitbox_expansion\": 100.00,\n" +
                "    \"damage_reduction\": 0.9999,\n" +
                "    \"shield_multiplier\": 1500.00,\n" +
                "    \"armor_boost\": 50000\n" +
                "  },\n" +
                "  \"input\": {\n" +
                "    \"touch_hz\": 1000,\n" +
                "    \"touch_latency_reduction\": true,\n" +
                "    \"gyro_sampling_hz\": 1000\n" +
                "  }\n" +
                "}\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            boolean ok;
            if (path.endsWith(".json")) {
                ok = ConfigFileHelper.writeContentAtomic(path, jsonContent);
            } else {
                ok = ConfigFileHelper.writeContentAtomic(path, iniContent);
            }
            if (ok) written++;
        }
        Log.i(TAG, "Blood Strike competitive UltraExtreme " + forcedFps + "FPS + 1000% Aim/Tracking/Defense force-write: " + written + " paths");
        return written > 0;
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageKeys = {
            "DamageMultiplier=1000.00",
            "PhysicalDamageBoost=1000.00",
            "MagicDamageBoost=1000.00",
            "TrueDamageBoost=1000.00",
            "BulletDamageBoost=1000.00",
            "DamageBoost=1000.00",
            "DamageBoostRatio=1000.00",
            "HeadshotMultiplier=1000.00",
            "HeadshotDamageMultiplier=1000.00",
            "CriticalDamage=10000",
            "CriticalHitRate=100",
            "CriticalDamageRate=100",
            "CriticalDamageMultiplier=50.00",
            "PenetrationBoost=10000",
            "ArmorPenetration=10000",
            "HighDamageRateMode=1",
            "AttackSpeedMultiplier=25.00",
            "AttackSpeedBoost=25.00",
            "ReloadSpeedMultiplier=25.00",
            "FireRateMultiplier=25.00",
            "MovementSpeedMultiplier=15.00",
            "SprintSpeedMultiplier=15.00",
            "SprintSensitivity=1000",
            "AgilityMultiplier=15.00",
            "HitboxExpansion=100.00",
            "BulletVelocityMultiplier=200.00",
            "BulletVelocityScale=200.00",
            "BodyDamageMultiplier=50.00",
            "LimbDamageMultiplier=50.00",
            "ExplosiveDamageMultiplier=50.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[DamageScript]");
        }
        Log.i(TAG, "Blood Strike 1000% damage boost & headshot multiplier applied for " + packageName);
    }

    public static void applyFastCooldownConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] cdKeys = {
            "SkillCoolDownReduceMode=1",
            "CooldownReductionBoost=0.99",
            "CooldownReduction=0.99",
            "SkillCooldownMultiplier=0.01",
            "SkillAnimationCancelZeroDelay=1",
            "SkillResponseZeroDelay=1",
            "SkillCastZeroDelay=1",
            "InstantSkillRelease=1",
            "ReloadSpeedMultiplier=25.00",
            "UnlimitedMana=1",
            "NoManaCost=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectFastCooldown(path);
            ConfigFileHelper.patchKeys(path, cdKeys, "[FastCooldown]");
        }
        Log.i(TAG, "Blood Strike Fast Cooldown 99% CDR applied for " + packageName);
    }

    public static void applyShield1500Config(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] shieldKeys = {
            "ShieldMultiplier=1500.00",
            "ShieldCapacity=1500.00",
            "ShieldStrength=1500.00",
            "ArmorEfficiency=1000.00",
            "ArmorDamageReduction=0.9999",
            "KineticArmorBoost=1500.00",
            "BodyArmorMultiplier=1500.00",
            "VestDurability=1500.00",
            "ArmorBoost=50000",
            "DamageReduction=0.9999",
            "IncomingDamageReduction=0.9999",
            "HealthRegenBoost=1000.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectShield1500(path);
            ConfigFileHelper.patchKeys(path, shieldKeys, "[DefenseShield1500]");
        }
        Log.i(TAG, "Blood Strike 1500+ Shield Overdrive applied for " + packageName);
    }

    public static void applyDroneViewConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] droneKeys = {
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
        Log.i(TAG, "Blood Strike Drone View Ultra FOV 180 applied for " + packageName);
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
            "AimAssistFOV=180",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "TouchSensitivity=1000",
            "AimTrackingRate=20.0"
        };
        for (String path : paths) {
            NativeConfigInjector.injectAimAssist(path);
            ConfigFileHelper.patchKeys(path, aimKeys, "[AimAssist]");
        }
        Log.i(TAG, "BloodStrike 10000 Aim Assist & Gyro 1000Hz applied for " + packageName);
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
            "RecoilReduction=1.00",
            "WeaponStability=500",
            "ScreenShake=0",
            "CameraShake=0",
            "NoCameraShake=1",
            "GunKick=0",
            "WeaponKick=0",
            "GunKickReduction=1.00",
            "WeaponKickReduction=1.00",
            "AllGunsRecoilReduction=1.00",
            "ScopeShakeReduction=1.00",
            "ScopeRecoilMultiplier=0.00",
            "ScopeStability=5.00",
            "BulletSpread=0.00",
            "CrosshairSpread=0.00",
            "SpreadScale=0.00",
            "SpreadReduction=1",
            "FirstBulletAccuracy=1",
            "AimPunchReduction=1",
            "FlinchReduction=1",
            "WeaponSway=0"
        };
        for (String path : paths) {
            NativeConfigInjector.injectNoRecoil(path);
            ConfigFileHelper.patchKeys(path, recoilKeys, "[WeaponStability]");
        }
        Log.i(TAG, "BloodStrike Zero Recoil & Weapon Stability applied for " + packageName);
    }

    /**
     * Injects 1000% Armor Efficiency, Kinetic Armor Boost, Helmet Protection, and Damage Resistance into Blood Strike.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "ArmorEfficiency=1000.00",
            "ArmorDamageReduction=0.9999",
            "KineticArmorBoost=1500.00",
            "BodyArmorMultiplier=1500.00",
            "HelmetDamageReduction=0.9999",
            "VestDurabilityBoost=1500.00",
            "VestDurability=1500.00",
            "ShieldCapacity=1500.00",
            "ShieldMultiplier=1500.00",
            "ShieldStrength=1500.00",
            "ShieldPointsMultiplier=1500.00",
            "PhysicalDefenseBoost=1000.00",
            "MagicDefenseBoost=1000.00",
            "ArmorBoost=50000",
            "MagicResistBoost=50000",
            "DamageReductionRatio=0.9999",
            "DamageReduction=0.9999",
            "IncomingDamageReduction=0.9999",
            "MaxHPMultiplier=100.00",
            "HPBoostRatio=100.00",
            "DamageAbsorbRatio=100.00",
            "TenacityRatio=0.9999",
            "ResilienceLevel=10",
            "ArmorLevel=10",
            "DamageResistance=0.9999",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=1000.00",
            "FallDamageReduction=1.00",
            "ExplosionResistance=0.9999",
            "HeadshotDamageReduction=0.9999",
            "HeavyHitAbsorption=100.00",
            "BurstDamageReduction=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[DefenseConfig]");
        }
        Log.i(TAG, "BloodStrike 1000% Armor Defense Boost & Damage Resistance applied for " + packageName);
    }

    /**
     * Injects 1000% Bullet Tracking, Magic Bullet, Hitbox Expansion, and Crosshair Magnetism for Blood Strike.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "TrackingBullet=1",
            "BulletTracking=1",
            "AutoTrackingBullet=1",
            "MagicBullet=1",
            "BulletMagnetism=100.00",
            "HitboxExpansion=100.00",
            "TargetLockTracking=1",
            "BulletCurveFactor=100.00",
            "BulletVelocityMultiplier=200.00",
            "CrosshairMagnetism=100.00",
            "FirstBulletAccuracy=1",
            "AutoAimTrack=1",
            "ProjectileHoming=1",
            "HomingStrength=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectTrackingBullet(path);
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "BloodStrike 1000% Bullet Tracking & Magic Bullet applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Blood Strike.
     */
    public static void applySpeedBoostConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] speedKeys = {
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
        Log.i(TAG, "BloodStrike 15.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    private static boolean applyStandardPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;
        String[] keys = {
            "MaxFPS=" + forcedFps,
            "TargetFPS=" + forcedFps,
            "FrameRateLimit=" + forcedFps,
            "MobileFPSLimit=" + forcedFps,
            "FPSLevel=" + fpsLevel,
            "GraphicQuality=4",
            "UltraExtreme=1",
            "HighFPSMode=1",
            "UnlockFPS=1",
            "Unlock120FPS=1",
            "Unlock144FPS=1",
            "Unlock165FPS=1",
            "Unlock185FPS=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[GraphicsSettings]");
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }
}
