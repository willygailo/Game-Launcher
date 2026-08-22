package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * HokConfigPatcher manages internal config files for Honor of Kings (HOK) and Arena of Valor (AoV).
 * Unlocks 120/144/165/185 FPS modes, HDR ultra frame rates, and high-frequency touch response.
 */
public class HokConfigPatcher {

    private static final String TAG = "HokConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "HOK patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;

        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String content = "[Graphics]\n" +
                "HighFPSMode=1\n" +
                "FrameRateLevel=" + frameRateLevel + "\n" +
                "FPS=" + forcedFps + "\n" +
                "MaxFrameRate=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "GraphicsQuality=4\n" +
                "HDMode=1\n" +
                "HDRMode=1\n" +
                "UltraFrameRate=1\n" +
                "VulkanEnabled=1\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "Unlock120Hz=1\n" +
                "Unlock144Hz=1\n" +
                "Unlock165Hz=1\n" +
                "Unlock185Hz=1\n" +
                "DroneView=1\n" +
                "DroneViewHeight=3\n" +
                "CameraHeight=3\n" +
                "CameraDistance=150\n" +
                "CameraFOV=150\n" +
                "FieldOfView=150\n" +
                "PhysicalDamageMultiplier=2.50\n" +
                "MagicDamageMultiplier=2.50\n" +
                "CriticalRateBoost=95\n" +
                "HighFreqTouchHz=" + forcedFps + "\n" +
                "TouchPollingRate=1000\n" +
                "TouchZeroDelay=1\n" +
                "TouchResponseLevel=3\n" +
                "GyroSampleRate=1000\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.writeContentAtomic(path, content)) {
                written++;
            }
        }
        Log.i(TAG, "HOK competitive " + forcedFps + "FPS + Drone View force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] touchKeys = {
            "HighFreqTouchHz=185",
            "TouchResponseLevel=3",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, touchKeys, "[TouchEngine]");
        }
        Log.i(TAG, "HOK super-fast zero-delay touch applied for " + packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "AimAssist=1",
            "SmartTargeting=1",
            "TargetLock=1",
            "SkillTargetAssist=1",
            "AutoAim=1",
            "TouchSensitivity=150"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, aimKeys, "[AimAssist]");
        }
        Log.i(TAG, "HOK Smart Target Assist applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "InputSmoothing=1",
            "TouchStabilization=1",
            "ZeroInputDelay=1",
            "SkillResponseFast=1",
            "CameraShake=0",
            "ScreenShake=0",
            "NoCameraShake=1",
            "AimPunchReduction=1",
            "FlinchReduction=1",
            "ScopeShakeReduction=1.50",
            "ScopeStability=1.50",
            "WeaponSway=0",
            "RecoilControl=1",
            "ZeroRecoil=1",
            "NoRecoil=1",
            "SpreadScale=0.00",
            "CrosshairSpread=0.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectNoRecoil(path);
            ConfigFileHelper.patchKeys(path, recoilKeys, "[WeaponStability]");
        }
        Log.i(TAG, "HOK Input Smoothing & Stabilization applied for " + packageName);
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageKeys = {
            "PhysicalDamageBoost=5.00",
            "MagicDamageBoost=5.00",
            "TrueDamageBoost=5.00",
            "DamageMultiplier=5.00",
            "DamageBoost=5.00",
            "DamageBoostRatio=5.00",
            "CritRate=100",
            "CritDamage=5.00",
            "CriticalDamage=100",
            "CriticalDamageRate=100",
            "CriticalHitRate=100",
            "CriticalDamageMultiplier=5.00",
            "HeadshotMultiplier=5.00",
            "HeadshotDamageMultiplier=5.00",
            "AttackSpeedBoost=3.00",
            "AttackSpeedMultiplier=3.00",
            "MovementSpeedMultiplier=3.00",
            "SprintSpeedMultiplier=3.00",
            "SprintSensitivity=200",
            "AgilityMultiplier=3.00",
            "HitboxExpansion=2.50",
            "BulletVelocityMultiplier=5.00",
            "BulletVelocityScale=5.00",
            "BodyDamageMultiplier=3.50",
            "ExplosiveDamageMultiplier=3.50",
            "FOV=150"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[DamageScript]");
        }
        Log.i(TAG, "HOK 5.0x Damage Boost & FOV applied for " + packageName);
    }

    /**
     * Injects Physical Armor, Magic Resistance, Damage Reduction, and Shield Boost for Honor of Kings / Arena of Valor.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "PhysicalArmor=5.00",
            "MagicResistance=5.00",
            "DamageReduction=0.85",
            "DamageReductionRatio=0.85",
            "IncomingDamageReduction=0.85",
            "ShieldBoost=5.00",
            "ShieldStrength=5.00",
            "ShieldEfficiency=5.00",
            "ShieldCapacity=5.00",
            "ShieldMultiplier=5.00",
            "MaxHPBoost=3.00",
            "MaxHPMultiplier=3.00",
            "HPBoostRatio=3.00",
            "Tenacity=0.80",
            "TenacityRatio=0.80",
            "ResilienceLevel=5",
            "ArmorBoost=500",
            "PhysicalDefenseBoost=5.00",
            "MagicDefenseBoost=5.00",
            "DamageAbsorbRatio=3.00",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=5.00",
            "ExplosionResistance=0.90",
            "FallDamageReduction=1.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[DefenseConfig]");
        }
        Log.i(TAG, "HOK Armor Defense 85% Reduction & 5.0x Shield applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Honor of Kings.
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
        Log.i(TAG, "HOK 3.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects Skill Auto-Tracking, Target Lock, and Skill Magnetism for Honor of Kings.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "SkillTracking=1",
            "AutoTargetLock=1",
            "TargetLockTracking=1",
            "PredictPath=1",
            "SkillMagnetism=1.50",
            "HeroPriorityLock=1",
            "LowestHPTargetLock=1",
            "HitboxExpansion=1.50",
            "TrackingBullet=1",
            "BulletTracking=1",
            "MagicBullet=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "HOK Skill Auto-Tracking & Target Lock applied for " + packageName);
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
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;
        String[] keys = {
            "HighFPSMode=1",
            "FrameRateLevel=" + frameRateLevel,
            "FPS=" + forcedFps,
            "MaxFrameRate=" + forcedFps,
            "TargetFPS=" + forcedFps,
            "GraphicsQuality=4",
            "HDMode=1",
            "HDRMode=1",
            "UltraFrameRate=1",
            "VulkanEnabled=1",
            "UnlockFPS=1",
            "SuperHighFPS=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "Shadow=1",
            "ResolutionScale=1.2",
            "HighFreqTouchHz=" + forcedFps
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Graphics]");
    }
}
