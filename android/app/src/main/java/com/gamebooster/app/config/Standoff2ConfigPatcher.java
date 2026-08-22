package com.gamebooster.app.config;

import android.util.Log;
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
                "    \"damage_multiplier\": 100.00,\n" +
                "    \"bullet_damage_boost\": 100.00,\n" +
                "    \"headshot_multiplier\": 100.00,\n" +
                "    \"critical_hit_rate\": 100,\n" +
                "    \"recoil_scale\": 0.00,\n" +
                "    \"weapon_kick_reduction\": 1.00,\n" +
                "    \"aim_assist\": 1,\n" +
                "    \"aim_assist_strength\": 1000,\n" +
                "    \"bullet_tracking\": 1,\n" +
                "    \"hitbox_expansion\": 50.00,\n" +
                "    \"damage_reduction\": 0.999,\n" +
                "    \"armor_boost\": 10000\n" +
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
                "ProjectileHoming=1\n" +
                "HomingStrength=100.00\n" +
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
                "CrosshairSpread=0.00\n";

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
        Log.i(TAG, "Standoff 2 competitive UltraExtreme " + forcedFps + "FPS + 1000% Aim/Tracking/Defense force-write: " + written + " paths");
        return written > 0;
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageKeys = {
            "DamageMultiplier=100.00",
            "PhysicalDamageBoost=100.00",
            "BulletDamageBoost=100.00",
            "DamageBoost=100.00",
            "DamageBoostRatio=100.00",
            "HeadshotMultiplier=100.00",
            "HeadshotDamageMultiplier=100.00",
            "CriticalDamage=1000",
            "CriticalDamageRate=100",
            "CriticalHitRate=100",
            "CriticalDamageMultiplier=10.00",
            "PenetrationBoost=1000",
            "ArmorPenetration=1000",
            "HighDamageRateMode=1",
            "AttackSpeedMultiplier=10.00",
            "AttackSpeedBoost=10.00",
            "ReloadSpeedMultiplier=10.00",
            "FireRateMultiplier=10.00",
            "MovementSpeedMultiplier=10.00",
            "SprintSpeedMultiplier=10.00",
            "SprintSensitivity=500",
            "AgilityMultiplier=10.00",
            "HitboxExpansion=10.00",
            "BulletVelocityMultiplier=50.00",
            "BulletVelocityScale=50.00",
            "BodyDamageMultiplier=10.00",
            "LimbDamageMultiplier=10.00",
            "ExplosiveDamageMultiplier=10.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[DamageScript]");
        }
        Log.i(TAG, "Standoff 2 1000% damage boost & headshot multiplier applied for " + packageName);
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
            "AimPrecision=10",
            "AimAssistStrength=1000",
            "AimAssistLevel=10",
            "TargetLock=1",
            "TargetLockSensitivity=1000",
            "AimAssistRadius=1000",
            "CrosshairMagnetism=100.00",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00",
            "AimSmooth=1",
            "AimTracking=1",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "SensitivityMultiplier=5.0",
            "Acceleration=0"
        };
        for (String path : paths) {
            NativeConfigInjector.injectAimAssist(path);
            ConfigFileHelper.patchKeys(path, aimKeys, "[AimAssist]");
        }
        Log.i(TAG, "Standoff2 1000% Aim Assist & Gyro 1000Hz applied for " + packageName);
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
            "WeaponKick=0",
            "GunKickReduction=1.00",
            "WeaponKickReduction=1.00",
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
            "ScopeShakeReduction=1.00",
            "ScopeRecoilMultiplier=0.00",
            "ScopeStability=5.00",
            "WeaponSway=0"
        };
        for (String path : paths) {
            NativeConfigInjector.injectNoRecoil(path);
            ConfigFileHelper.patchKeys(path, recoilKeys, "[WeaponStability]");
        }
        Log.i(TAG, "Standoff2 Zero Recoil & Weapon Stability applied for " + packageName);
    }

    /**
     * Injects 1000% Armor Efficiency, Vest Durability, Helmet Protection, and Damage Reduction into Standoff 2.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "ArmorEfficiency=100.00",
            "VestDurability=100.00",
            "VestDurabilityBoost=100.00",
            "DamageReduction=0.999",
            "DamageReductionRatio=0.999",
            "IncomingDamageReduction=0.999",
            "HelmetProtection=0.999",
            "HelmetDamageReduction=0.999",
            "ShieldCapacity=100.00",
            "ShieldMultiplier=100.00",
            "ShieldStrength=100.00",
            "MaxHPMultiplier=50.00",
            "HPBoostRatio=50.00",
            "DamageAbsorbRatio=50.00",
            "ArmorBoost=10000",
            "PhysicalDefenseBoost=100.00",
            "MagicDefenseBoost=100.00",
            "TenacityRatio=0.999",
            "ResilienceLevel=10",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=100.00",
            "FallDamageReduction=1.00",
            "ExplosionResistance=0.999",
            "HeavyHitAbsorption=10.00",
            "BurstDamageReduction=10.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[DefenseConfig]");
        }
        Log.i(TAG, "Standoff2 1000% Armor Defense & 100x Vest Durability applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Standoff 2.
     */
    public static void applySpeedBoostConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] speedKeys = {
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
        Log.i(TAG, "Standoff2 10.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects 1000% Bullet Tracking, Magic Bullet, Hitbox Expansion, and Crosshair Magnetism for Standoff 2.
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
            "HitboxExpansion=50.00",
            "TargetLockTracking=1",
            "BulletCurveFactor=50.00",
            "BulletVelocityMultiplier=100.00",
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
        Log.i(TAG, "Standoff2 1000% Bullet Tracking & Magic Bullet applied for " + packageName);
    }

    private static boolean applyStandardPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        String[] keys = {
            "target_framerate=" + forcedFps,
            "max_framerate=" + forcedFps,
            "framerate_cap=" + forcedFps,
            "TargetFPS=" + forcedFps,
            "MaxFPS=" + forcedFps,
            "FrameRateLimit=" + forcedFps,
            "fps_unlock=1",
            "fps_unlock_120=1",
            "fps_unlock_144=1",
            "fps_unlock_165=1",
            "fps_unlock_185=1",
            "high_fps_mode=1",
            "ultra_extreme=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[StandoffGraphics]");
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }
}
