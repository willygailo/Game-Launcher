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
                "BulletDamageBoost=2.50\n" +
                "DamageMultiplier=2.50\n" +
                "HeadshotDamageMultiplier=3.50\n" +
                "CriticalHitRate=99\n" +
                "NoRecoil=1\n" +
                "CrosshairSpread=0.00\n" +
                "ScopeStability=1.50\n";

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
                "    \"vsync\": false\n" +
                "  },\n" +
                "  \"combat\": {\n" +
                "    \"damage_boost_ratio\": 1.90,\n" +
                "    \"bullet_damage_multiplier\": 1.90,\n" +
                "    \"headshot_multiplier\": 2.90,\n" +
                "    \"critical_strike_rate\": 95,\n" +
                "    \"recoil_reduction\": 1.00,\n" +
                "    \"crosshair_spread\": 0.00\n" +
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
        Log.i(TAG, "Blood Strike competitive UltraExtreme " + forcedFps + "FPS force-write: " + written + " paths");
        return written > 0;
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageKeys = {
            "DamageMultiplier=5.00",
            "PhysicalDamageBoost=5.00",
            "MagicDamageBoost=5.00",
            "TrueDamageBoost=5.00",
            "BulletDamageBoost=5.00",
            "DamageBoost=5.00",
            "DamageBoostRatio=5.00",
            "HeadshotMultiplier=5.00",
            "HeadshotDamageMultiplier=5.00",
            "CriticalDamage=100",
            "CriticalHitRate=100",
            "CriticalDamageRate=100",
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
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[DamageScript]");
        }
        Log.i(TAG, "Blood Strike 5.0x damage boost & headshot multiplier applied for " + packageName);
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
            "AimPrecision=3",
            "AimAssistStrength=150",
            "AimMagnetism=2.5",
            "AimAssistFOV=150",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "TouchSensitivity=150",
            "AimTrackingRate=2.5"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, aimKeys, "[AimAssist]");
        }
        Log.i(TAG, "BloodStrike Aim Assist & Gyro 1000Hz applied for " + packageName);
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
            "ScreenShake=0",
            "CameraShake=0",
            "NoCameraShake=1",
            "GunKick=0",
            "WeaponKick=0",
            "GunKickReduction=2.00",
            "WeaponKickReduction=2.00",
            "AllGunsRecoilReduction=2.00",
            "ScopeShakeReduction=2.00",
            "ScopeRecoilMultiplier=0.00",
            "ScopeStability=2.50",
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
     * Injects Armor Efficiency, Kinetic Armor Boost, Helmet Protection, and Damage Resistance into Blood Strike.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "ArmorEfficiency=5.00",
            "ArmorDamageReduction=0.85",
            "KineticArmorBoost=5.00",
            "BodyArmorMultiplier=5.00",
            "HelmetDamageReduction=0.90",
            "VestDurabilityBoost=5.00",
            "VestDurability=5.00",
            "ShieldCapacity=5.00",
            "ShieldMultiplier=5.00",
            "ShieldStrength=5.00",
            "ShieldPointsMultiplier=5.00",
            "PhysicalDefenseBoost=5.00",
            "ArmorBoost=500",
            "DamageReductionRatio=0.85",
            "DamageReduction=0.85",
            "IncomingDamageReduction=0.85",
            "MaxHPMultiplier=3.00",
            "HPBoostRatio=3.00",
            "DamageAbsorbRatio=3.00",
            "TenacityRatio=0.80",
            "ResilienceLevel=5",
            "ArmorLevel=6",
            "DamageResistance=0.85",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=5.00",
            "FallDamageReduction=1.00",
            "ExplosionResistance=0.90",
            "HeadshotDamageReduction=0.90"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[DefenseConfig]");
        }
        Log.i(TAG, "BloodStrike Armor Defense Boost & Damage Resistance applied for " + packageName);
    }

    /**
     * Injects Bullet Tracking, Magic Bullet, Hitbox Expansion, and Crosshair Magnetism for Blood Strike.
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
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "BloodStrike Bullet Tracking & Magic Bullet applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Blood Strike.
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
            NativeConfigInjector.injectSpeedBoost(path);
            ConfigFileHelper.patchKeys(path, speedKeys, "[SpeedEngine]");
        }
        Log.i(TAG, "BloodStrike 3.0x Speed Boost & Movement Agility applied for " + packageName);
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
