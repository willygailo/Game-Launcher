package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * GenshinConfigPatcher manages internal config and hardware profile JSON files
 * for Genshin Impact, Honkai: Star Rail, and Zenless Zone Zero.
 * Unlocks 120/144/165/185 FPS, unlocks Vulkan backend, and sets max rendering quality.
 */
public class GenshinConfigPatcher {

    private static final String TAG = "GenshinConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Genshin patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);

        String jsonContent = "{\n" +
                "  \"fps\": " + forcedFps + ",\n" +
                "  \"max_fps\": " + forcedFps + ",\n" +
                "  \"target_frame_rate\": " + forcedFps + ",\n" +
                "  \"targetFrameRateForOthers\": " + forcedFps + ",\n" +
                "  \"fpsUnlock\": true,\n" +
                "  \"fps_unlock_120\": true,\n" +
                "  \"fps_unlock_144\": true,\n" +
                "  \"fps_unlock_165\": true,\n" +
                "  \"fps_unlock_185\": true,\n" +
                "  \"graphics_quality\": 5,\n" +
                "  \"render_resolution\": 1.2,\n" +
                "  \"shadow_quality\": 4,\n" +
                "  \"visual_effects\": 4,\n" +
                "  \"sfx_quality\": 4,\n" +
                "  \"environment_detail\": 4,\n" +
                "  \"motion_blur\": 0,\n" +
                "  \"bloom\": 1,\n" +
                "  \"crowd_density\": 2,\n" +
                "  \"subsurface_scattering\": 1,\n" +
                "  \"co_op_teammate_effects\": 1,\n" +
                "  \"vulkan_enabled\": true,\n" +
                "  \"unlock_120hz\": true,\n" +
                "  \"unlock_144hz\": true,\n" +
                "  \"unlock_165hz\": true,\n" +
                "  \"unlock_185hz\": true,\n" +
                "  \"camera_distance\": 6.0,\n" +
                "  \"camera_fov\": 150,\n" +
                "  \"drone_view\": true,\n" +
                "  \"field_of_view\": 150,\n" +
                "  \"touch_polling_rate\": 1000,\n" +
                "  \"zero_touch_delay\": true,\n" +
                "  \"touch_response_ms\": 0,\n" +
                "  \"input_latency_reduction\": true,\n" +
                "  \"gyro_sample_rate\": 1000,\n" +
                "  \"aim_assist\": 1,\n" +
                "  \"aim_assist_strength\": 1000,\n" +
                "  \"aim_assist_level\": 10,\n" +
                "  \"aim_precision\": 10,\n" +
                "  \"target_lock_sensitivity\": 1000,\n" +
                "  \"crosshair_magnetism\": 100.00,\n" +
                "  \"aim_snap_strength\": 100.00,\n" +
                "  \"aim_magnetism\": 100.00,\n" +
                "  \"bow_auto_tracking\": 1,\n" +
                "  \"homing_arrows\": 1,\n" +
                "  \"projectile_homing\": 1,\n" +
                "  \"homing_strength\": 100.00,\n" +
                "  \"hitbox_expansion\": 50.00,\n" +
                "  \"bullet_magnetism\": 100.00,\n" +
                "  \"bullet_tracking\": 1,\n" +
                "  \"auto_tracking_bullet\": 1,\n" +
                "  \"magic_bullet\": 1,\n" +
                "  \"defense_multiplier\": 100.00,\n" +
                "  \"shield_strength\": 100.00,\n" +
                "  \"shield_capacity\": 100.00,\n" +
                "  \"shield_multiplier\": 100.00,\n" +
                "  \"damage_reduction_ratio\": 0.999,\n" +
                "  \"damage_reduction\": 0.999,\n" +
                "  \"incoming_damage_reduction\": 0.999,\n" +
                "  \"elemental_resistance_boost\": 100.00,\n" +
                "  \"armor_boost\": 10000,\n" +
                "  \"tenacity_ratio\": 0.999,\n" +
                "  \"damage_multiplier\": 100.00,\n" +
                "  \"attack_speed_multiplier\": 10.0,\n" +
                "  \"crit_rate_boost\": 1.00,\n" +
                "  \"recoil_compensation\": 1.0,\n" +
                "  \"camera_shake\": 0.0\n" +
                "}\n";

        String hardwareConfig = "{\n" +
                "  \"device_model\": \"SM-S948B\",\n" +
                "  \"gpu_renderer\": \"Adreno (TM) 840\",\n" +
                "  \"vulkan_support\": true,\n" +
                "  \"max_refresh_rate\": " + forcedFps + ",\n" +
                "  \"frame_rate_cap\": " + forcedFps + "\n" +
                "}\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            boolean ok;
            if (path.contains("hardware_model")) {
                ok = ConfigFileHelper.writeContentAtomic(path, hardwareConfig);
            } else {
                ok = ConfigFileHelper.writeContentAtomic(path, jsonContent);
            }
            if (ok) written++;
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "Genshin competitive " + forcedFps + "FPS + 1000% Aim/Tracking/Defense force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] touchKeys = {
            "touch_polling_rate=1000",
            "zero_touch_delay=1",
            "touch_response_ms=0",
            "input_latency_reduction=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, touchKeys, "[TouchEngine]");
        }
        Log.i(TAG, "Genshin zero-delay touch acceleration applied for " + packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "AimAssist=1",
            "AimPrecision=10",
            "AimAssistStrength=1000",
            "AimAssistLevel=10",
            "BowAimAssist=1",
            "AutoTargeting=1",
            "TargetLock=1",
            "TargetLockSensitivity=1000",
            "CrosshairMagnetism=100.00",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "CameraFOV=120"
        };
        for (String path : paths) {
            NativeConfigInjector.injectAimAssist(path);
            ConfigFileHelper.patchKeys(path, aimKeys, "[AimAssist]");
        }
        Log.i(TAG, "Genshin 1000% Bow Aim Assist & Gyro 1000Hz applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "BowSwayReduction=1",
            "BowSwayScale=0.00",
            "CameraShake=0",
            "ScreenShake=0",
            "NoCameraShake=1",
            "InputSmoothing=1",
            "ZeroCameraLag=1",
            "SkillCameraShake=0",
            "AimPunchReduction=1",
            "FlinchReduction=1",
            "ScopeShakeReduction=1.00",
            "ScopeStability=5.00",
            "WeaponSway=0",
            "CrosshairSpread=0.00",
            "SpreadScale=0.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectNoRecoil(path);
            ConfigFileHelper.patchKeys(path, recoilKeys, "[WeaponStability]");
        }
        Log.i(TAG, "Genshin Camera Stabilization & Sway Reduction applied for " + packageName);
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageKeys = {
            "ElementalDamageBoost=100.00",
            "PhysicalDamageBoost=100.00",
            "MagicDamageBoost=100.00",
            "TrueDamageBoost=100.00",
            "DamageMultiplier=100.00",
            "DamageBoost=100.00",
            "DamageBoostRatio=100.00",
            "CritRate=100",
            "CritDamage=10.00",
            "CriticalDamage=1000",
            "CriticalHitRate=100",
            "CriticalDamageRate=100",
            "CriticalDamageMultiplier=10.00",
            "HeadshotMultiplier=100.00",
            "HeadshotDamageMultiplier=100.00",
            "AttackSpeedBoost=10.00",
            "AttackSpeedMultiplier=10.00",
            "MovementSpeedMultiplier=10.00",
            "SprintSpeedMultiplier=10.00",
            "SprintSensitivity=500",
            "AgilityMultiplier=10.00",
            "HitboxExpansion=10.00",
            "BulletVelocityMultiplier=50.00",
            "BulletVelocityScale=50.00",
            "BodyDamageMultiplier=10.00",
            "ExplosiveDamageMultiplier=10.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[DamageScript]");
        }
        Log.i(TAG, "Genshin 1000% Elemental & Physical Damage Boost applied for " + packageName);
    }

    /**
     * Injects 1000% Defense Multiplier, Shield Strength, Elemental Resistance, and Poise Resistance into Genshin/Star Rail/ZZZ.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "DefenseMultiplier=100.00",
            "ShieldStrength=100.00",
            "ShieldEfficiency=100.00",
            "ShieldCapacity=100.00",
            "ShieldMultiplier=100.00",
            "DamageReductionRatio=0.999",
            "DamageReduction=0.999",
            "IncomingDamageReduction=0.999",
            "ElementalResistanceBoost=100.00",
            "HPMultiplier=50.00",
            "MaxHPMultiplier=50.00",
            "HPBoostRatio=50.00",
            "PoiseResistance=100.00",
            "ArmorBoost=10000",
            "DamageAbsorbRatio=50.00",
            "TenacityRatio=0.999",
            "ResilienceLevel=10",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=100.00",
            "ExplosionResistance=0.999",
            "FallDamageReduction=1.00",
            "HeavyHitAbsorption=10.00",
            "BurstDamageReduction=10.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[DefenseConfig]");
        }
        Log.i(TAG, "Genshin 1000% Defense Multiplier & Shield Boost applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Genshin / Star Rail / ZZZ.
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
        Log.i(TAG, "Genshin 10.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects 1000% Bow Auto-Tracking, Homing Projectiles, and Target Lock for Genshin/Hoyoverse.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "BowAutoTracking=1",
            "HomingArrows=1",
            "TargetLockTracking=1",
            "AutoTargetLock=1",
            "SkillProjectileTracking=1",
            "HitboxExpansion=50.00",
            "BulletMagnetism=100.00",
            "TrackingBullet=1",
            "BulletTracking=1",
            "AutoTrackingBullet=1",
            "MagicBullet=1",
            "ProjectileHoming=1",
            "HomingStrength=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectTrackingBullet(path);
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "Genshin 1000% Bow Auto-Tracking & Homing Projectiles applied for " + packageName);
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
        String[] keys = {
            "fps=" + forcedFps,
            "max_fps=" + forcedFps,
            "target_frame_rate=" + forcedFps,
            "targetFrameRateForOthers=" + forcedFps,
            "fpsUnlock=1",
            "fps_unlock_120=1",
            "fps_unlock_144=1",
            "fps_unlock_165=1",
            "fps_unlock_185=1",
            "vulkan_enabled=1",
            "unlock_120hz=1",
            "unlock_144hz=1",
            "unlock_165hz=1",
            "unlock_185hz=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Graphics]");
    }
}
