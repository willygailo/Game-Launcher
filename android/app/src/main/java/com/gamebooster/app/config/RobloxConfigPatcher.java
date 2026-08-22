package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * RobloxConfigPatcher manages ClientAppSettings.json FastFlags and local graphics settings
 * for Roblox on Android.
 * Unlocks 120/144/165/185 FPS frame rate limits and enables high performance rendering.
 */
public class RobloxConfigPatcher {

    private static final String TAG = "RobloxConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Roblox patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);

        String clientAppSettings = "{\n" +
                "  \"DFIntTaskSchedulerTargetFps\": " + forcedFps + ",\n" +
                "  \"FIntTargetFPS\": " + forcedFps + ",\n" +
                "  \"FIntDesiredMaxFrameRate\": " + forcedFps + ",\n" +
                "  \"FFlagEnableHighFPS\": \"True\",\n" +
                "  \"FFlagUnlockFPS\": \"True\",\n" +
                "  \"FFlagTaskSchedulerLimitTargetFps\": \"False\",\n" +
                "  \"FFlagDebugGraphicsDisableDirect3D11\": \"False\",\n" +
                "  \"FFlagDebugGraphicsPreferVulkan\": \"True\",\n" +
                "  \"FFlagFixGraphicsQuality\": \"True\",\n" +
                "  \"DFFlagDisableDPIScale\": \"True\",\n" +
                "  \"FFlagCommitToFastPhysics\": \"True\",\n" +
                "  \"FFlagEnableVulkan\": \"True\",\n" +
                "  \"FIntCameraMaxZoomDistance\": 500,\n" +
                "  \"FFlagDroneViewUnlocked\": \"True\",\n" +
                "  \"FIntFieldOfView\": 150,\n" +
                "  \"FFlagFastTouchResponse\": \"True\",\n" +
                "  \"FIntTouchPollingRate\": 1000,\n" +
                "  \"FFlagZeroTouchDelay\": \"True\",\n" +
                "  \"FFlagReduceInputLatency\": \"True\",\n" +
                "  \"FFlagTouchSlopReduction\": \"True\",\n" +
                "  \"FFlagGyroFastAim\": \"True\",\n" +
                "  \"FIntGyroPollingRate\": 1000,\n" +
                "  \"FFlagDisableCameraShake\": \"True\",\n" +
                "  \"FFlagWeaponRecoilReduction\": \"True\",\n" +
                "  \"FFlagAimAssist\": \"True\",\n" +
                "  \"FIntAimAssistStrength\": 1000,\n" +
                "  \"FIntAimAssistLevel\": 10,\n" +
                "  \"FIntAimPrecision\": 10,\n" +
                "  \"FIntTargetLockSensitivity\": 1000,\n" +
                "  \"FIntCrosshairMagnetism\": 100,\n" +
                "  \"FFlagBulletTracking\": \"True\",\n" +
                "  \"FFlagAutoTrackingBullet\": \"True\",\n" +
                "  \"FFlagMagicBullet\": \"True\",\n" +
                "  \"FIntHitboxExpansion\": 50,\n" +
                "  \"FIntBulletMagnetism\": 100,\n" +
                "  \"FFlagProjectileHoming\": \"True\",\n" +
                "  \"FIntHomingStrength\": 100,\n" +
                "  \"FIntDefenseMultiplier\": 100,\n" +
                "  \"FIntDamageReduction\": 999,\n" +
                "  \"FIntShieldMultiplier\": 100,\n" +
                "  \"FIntArmorBoost\": 10000,\n" +
                "  \"FFlagDamageBoostMode\": \"True\",\n" +
                "  \"FIntDamageMultiplier\": 100\n" +
                "}\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.writeContentAtomic(path, clientAppSettings)) {
                written++;
            }
        }
        Log.i(TAG, "Roblox competitive " + forcedFps + "FPS FastFlag + 1000% Aim/Tracking/Defense force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] touchKeys = {
            "FFlagFastTouchResponse=True",
            "FIntTouchPollingRate=1000",
            "FFlagZeroTouchDelay=True",
            "FFlagTouchSlopReduction=True",
            "FFlagReduceInputLatency=True"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, touchKeys, "[TouchEngine]");
        }
        Log.i(TAG, "Roblox fast zero-delay touch applied for " + packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "FFlagAimAssist=True",
            "FIntAimAssistStrength=1000",
            "FIntAimAssistLevel=10",
            "FIntAimPrecision=10",
            "FIntTargetLockSensitivity=1000",
            "FIntCrosshairMagnetism=100",
            "AimAssist=1",
            "AimPrecision=10",
            "AimAssistStrength=1000",
            "AimAssistLevel=10",
            "TargetLock=1",
            "TargetLockSensitivity=1000",
            "CrosshairMagnetism=100.00",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00",
            "CameraSensitivity=500",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "TouchSensitivity=500"
        };
        for (String path : paths) {
            NativeConfigInjector.injectAimAssist(path);
            ConfigFileHelper.patchKeys(path, aimKeys, "[AimAssist]");
        }
        Log.i(TAG, "Roblox 1000% Aim Assist & Gyro 1000Hz applied for " + packageName);
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
            "CameraShake=0",
            "NoCameraShake=1",
            "WeaponStability=500",
            "InputSmoothing=1",
            "ScopeShakeReduction=1.00",
            "ScopeStability=5.00",
            "ScopeRecoilMultiplier=0.00",
            "AimPunchReduction=1",
            "FlinchReduction=1",
            "GunKick=0",
            "WeaponKickReduction=1.00",
            "SpreadScale=0.00",
            "BulletSpread=0.00",
            "CrosshairSpread=0.00",
            "WeaponSway=0"
        };
        for (String path : paths) {
            NativeConfigInjector.injectNoRecoil(path);
            ConfigFileHelper.patchKeys(path, recoilKeys, "[WeaponStability]");
        }
        Log.i(TAG, "Roblox Zero Recoil & Camera Shake Elimination applied for " + packageName);
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageKeys = {
            "DamageMultiplier=100.00",
            "PhysicalDamageBoost=100.00",
            "MagicDamageBoost=100.00",
            "TrueDamageBoost=100.00",
            "BulletDamageBoost=100.00",
            "DamageBoost=100.00",
            "DamageBoostRatio=100.00",
            "HeadshotMultiplier=100.00",
            "HeadshotDamageMultiplier=100.00",
            "CriticalDamage=1000",
            "CriticalHitRate=100",
            "CriticalDamageRate=100",
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
            "ExplosiveDamageMultiplier=10.00",
            "FOV=150"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[DamageScript]");
        }
        Log.i(TAG, "Roblox 1000% Damage Boost & FOV applied for " + packageName);
    }

    /**
     * Injects 1000% Defense Multiplier, Damage Reduction, Shield Multiplier, and HP Boost for Roblox.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "FFlagDefenseBoost=True",
            "FIntDefenseMultiplier=100",
            "FIntDamageReduction=999",
            "FIntShieldMultiplier=100",
            "DefenseMultiplier=100.00",
            "DamageReductionRatio=0.999",
            "DamageReduction=0.999",
            "IncomingDamageReduction=0.999",
            "ShieldMultiplier=100.00",
            "ShieldCapacity=100.00",
            "ShieldStrength=100.00",
            "ShieldEfficiency=100.00",
            "MaxHPMultiplier=50.00",
            "HPBoostRatio=50.00",
            "ArmorBoost=10000",
            "PhysicalDefenseBoost=100.00",
            "MagicDefenseBoost=100.00",
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
        Log.i(TAG, "Roblox 1000% Armor Defense & 100x Shield applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Roblox.
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
        Log.i(TAG, "Roblox 10.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects 1000% Bullet Tracking, Magic Bullet, Hitbox Expansion, and Target Lock for Roblox.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "FFlagBulletTracking=True",
            "FFlagHitboxExpansion=True",
            "FFlagMagicBullet=True",
            "FFlagTargetLockTracking=True",
            "FFlagProjectileHoming=True",
            "FIntHitboxExpansion=50",
            "FIntBulletMagnetism=100",
            "FIntHomingStrength=100",
            "BulletTracking=1",
            "TrackingBullet=1",
            "AutoTrackingBullet=1",
            "MagicBullet=1",
            "HitboxExpansion=50.00",
            "BulletMagnetism=100.00",
            "BulletCurveFactor=50.00",
            "BulletVelocityMultiplier=100.00",
            "CrosshairMagnetism=100.00",
            "TargetLockTracking=1",
            "ProjectileHoming=1",
            "HomingStrength=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectTrackingBullet(path);
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "Roblox 1000% Bullet Tracking & Hitbox Expansion applied for " + packageName);
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
            "DFIntTaskSchedulerTargetFps=" + forcedFps,
            "FIntTargetFPS=" + forcedFps,
            "FIntDesiredMaxFrameRate=" + forcedFps,
            "FFlagEnableHighFPS=True",
            "FFlagUnlockFPS=True",
            "FFlagDebugGraphicsPreferVulkan=True",
            "FFlagFixGraphicsQuality=True",
            "DFFlagDisableDPIScale=True",
            "FFlagCommitToFastPhysics=True",
            "FFlagEnableVulkan=True"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Roblox]");
    }
}
