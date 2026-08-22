package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * FreeFireConfigPatcher manages internal config files for Garena Free Fire and Free Fire MAX.
 * Unlocks 120/144/165/185 FPS, high-frequency touch, and max graphic presets.
 */
public class FreeFireConfigPatcher {

    private static final String TAG = "FreeFireConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "FreeFire patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String content = "[FFGraphics]\n" +
                "HighFPS=1\n" +
                "HighFPSMode=1\n" +
                "FPSMode=2\n" +
                "FrameRateLevel=" + frameRateLevel + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "GraphicLevel=3\n" +
                "Shadow=1\n" +
                "HighResolution=1\n" +
                "VulkanEnabled=1\n" +
                "Unlock120Hz=1\n" +
                "Unlock144Hz=1\n" +
                "Unlock165Hz=1\n" +
                "Unlock185Hz=1\n" +
                "AimAssist=1\n" +
                "AutoAimPrecision=1.5\n" +
                "AimAssistStrength=150\n" +
                "AimAssistLevel=5\n" +
                "SprintSensitivity=150\n" +
                "GeneralSensitivity=150\n" +
                "RedDotSensitivity=150\n" +
                "TPPFov=100\n" +
                "FPPFov=150\n" +
                "NoRecoil=1\n" +
                "RecoilReduction=1.50\n" +
                "AllWeaponRecoilFix=1\n" +
                "ScopeStabilization=1\n" +
                "Scope2xRecoil=0.00\n" +
                "Scope4xRecoil=0.00\n" +
                "SniperScopeRecoil=0.00\n" +
                "GunShakeReduction=1.50\n" +
                "DamageBoostRatio=2.50\n" +
                "HeadshotDamageMultiplier=3.50\n" +
                "BulletDamageBoost=2.50\n" +
                "CriticalHitRate=99\n" +
                "TouchResponseLevel=3\n" +
                "HighFreqTouchHz=" + forcedFps + "\n" +
                "TouchPollingRate=1000\n" +
                "TouchZeroDelay=1\n" +
                "GyroSampleRate=1000\n" +
                "GyroSensitivityRatio=2.5\n" +
                "GyroZeroDelay=1\n" +
                "GyroSmoothFactor=1\n" +
                "GyroStabilization=1\n" +
                "GyroLatencyMode=0\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.writeContentAtomic(path, content)) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "FreeFire competitive " + forcedFps + "FPS force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Applies anti-log, report cleaner, and telemetry suppression for Free Fire.
     */
    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] touchKeys = {
            "TouchResponseLevel=3",
            "HighFreqTouchHz=185",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "TouchSlopReduction=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, touchKeys, "[TouchEngine]");
        }
        Log.i(TAG, "FreeFire super-fast zero-delay touch applied for " + packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "AimAssist=1",
            "AimPrecision=3",
            "AimAssistStrength=150",
            "AutoAim=1",
            "AimAssistRadius=200",
            "ScopeAimAssist=1",
            "RedDotAimAssist=1",
            "GyroSampleRate=1000",
            "GyroSensitivity=150",
            "GyroStabilization=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, aimKeys, "[AimAssist]");
        }
        Log.i(TAG, "FreeFire Aim Assist 150% & Precision applied for " + packageName);
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
            "ScreenShake=0",
            "CameraShake=0",
            "NoCameraShake=1",
            "GunKick=0",
            "GunKickReduction=1.50",
            "WeaponKickReduction=1.50",
            "AllGunsRecoilReduction=1.50",
            "ScopeShakeReduction=1.50",
            "ScopeRecoilMultiplier=0.00",
            "ScopeStability=1.50",
            "BulletSpread=0.00",
            "CrosshairSpread=0.00",
            "SpreadScale=0.00",
            "BulletSpreadReduction=1",
            "FirstBulletAccuracy=1",
            "AimPunchReduction=1",
            "FlinchReduction=1",
            "WeaponSway=0"
        };
        for (String path : paths) {
            NativeConfigInjector.injectNoRecoil(path);
            ConfigFileHelper.patchKeys(path, recoilKeys, "[WeaponStability]");
        }
        Log.i(TAG, "FreeFire Zero Recoil & Weapon Stability applied for " + packageName);
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
        Log.i(TAG, "FreeFire Damage Boost 500% & Headshot Multiplier applied for " + packageName);
    }

    /**
     * Injects Armor Defense, Vest Durability, Helmet Protection, and Shield Capacity for Free Fire.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "VestDurability=5.00",
            "VestDurabilityBoost=5.00",
            "HelmetDamageReduction=0.90",
            "ArmorDamageAbsorb=0.90",
            "ShieldCapacity=5.00",
            "ShieldMultiplier=5.00",
            "ShieldStrength=5.00",
            "ArmorBoostRatio=5.00",
            "HPBoostRatio=3.00",
            "MaxHPMultiplier=3.00",
            "DamageAbsorbRatio=3.00",
            "DamageReductionRatio=0.85",
            "DamageReduction=0.85",
            "IncomingDamageReduction=0.85",
            "PhysicalDefenseBoost=5.00",
            "MagicDefenseBoost=5.00",
            "ArmorBoost=500",
            "MagicResistBoost=500",
            "TenacityRatio=0.80",
            "ResilienceLevel=5",
            "ArmorLevel=6",
            "DamageResistance=0.85",
            "ShieldEfficiency=5.00",
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
        Log.i(TAG, "FreeFire Armor Defense 85% Reduction & 5.0x Vest Durability applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Free Fire.
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
        Log.i(TAG, "FreeFire 3.0x Speed Boost & Sprint Agility applied for " + packageName);
    }

    /**
     * Injects Auto Aim Track, Bullet Tracking, Headshot Tracking, and Hitbox Expansion for Free Fire.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "AutoAimTrack=1",
            "BulletTracking=1",
            "HeadshotTracking=1",
            "HitboxExpansion=1.50",
            "MagicBullet=1",
            "BulletMagnetism=1.50",
            "AutoTrackingBullet=1",
            "TargetLockTracking=1",
            "TrackingBullet=1",
            "CrosshairMagnetism=1.50",
            "BulletVelocityMultiplier=2.00",
            "FirstBulletAccuracy=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "FreeFire Auto Aim Track & Bullet Tracking applied for " + packageName);
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    private static boolean applyPatch(String path, int targetFps) {
        int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;
        String[] keys = {
            "HighFPS=1",
            "HighFPSMode=1",
            "FPSMode=2",
            "FrameRateLevel=" + frameRateLevel,
            "MaxFPS=" + forcedFps,
            "TargetFPS=" + forcedFps,
            "UnlockFPS=1",
            "SuperHighFPS=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "GraphicLevel=3",
            "Shadow=1",
            "HighResolution=1",
            "VulkanEnabled=1",
            "HighFreqTouchHz=" + forcedFps
        };
        return ConfigFileHelper.patchKeys(path, keys, "[FFGraphics]");
    }
}
