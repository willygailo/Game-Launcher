package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * CodmConfigPatcher manages internal config files for Call of Duty Mobile (all versions/regions).
 *
 * Two patching modes:
 *  - patch()            → standard patch: in-memory key/JSON/XML upserting
 *  - patchCompetitive() → competitive force-write: overwrites all paths atomically via ConfigFileHelper
 */
public class CodmConfigPatcher {

    private static final String TAG = "CodmConfigPatcher";

    // ─── Standard Patch ───────────────────────────────────────────────────────

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "CODM patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL CODM config paths unconditionally.
     * Writes proper JSON for UserSetting.json, XML for PlayerPrefs, and INI for GraphicsSettings.ini.
     *
     * @return true if at least one path was written
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            String content;
            if (path.endsWith(".json")) {
                content = "{\n" +
                        "  \"MaxFrameRate\": " + forcedFps + ",\n" +
                        "  \"TargetFPS\": " + forcedFps + ",\n" +
                        "  \"GraphicQuality\": 4,\n" +
                        "  \"FPSLimit\": " + forcedFps + ",\n" +
                        "  \"FrameRateLimit\": " + forcedFps + ",\n" +
                        "  \"MobileFPSLimit\": " + forcedFps + ",\n" +
                        "  \"HDRMode\": 1,\n" +
                        "  \"HDRColorMode\": 2,\n" +
                        "  \"Unlock120Hz\": 1,\n" +
                        "  \"Unlock144Hz\": 1,\n" +
                        "  \"Unlock165Hz\": 1,\n" +
                        "  \"Unlock185Hz\": 1,\n" +
                        "  \"TouchBoostHz\": " + forcedFps + ",\n" +
                        "  \"TouchPollingRate\": 1000,\n" +
                        "  \"TouchZeroDelay\": 1,\n" +
                        "  \"GyroSampleRate\": 1000,\n" +
                        "  \"SuperResolution\": 1,\n" +
                        "  \"FieldOfView\": 150,\n" +
                        "  \"FPP_FOV\": 150,\n" +
                        "  \"TPP_FOV\": 100,\n" +
                        "  \"SprintSensitivity\": 500,\n" +
                        "  \"AlwaysSprint\": 1,\n" +
                        "  \"AimAssist\": 1,\n" +
                        "  \"AimAssistStrength\": 1000,\n" +
                        "  \"AimAssistLevel\": 10,\n" +
                        "  \"AimPrecision\": 10,\n" +
                        "  \"TargetLockSensitivity\": 1000,\n" +
                        "  \"CrosshairMagnetism\": 100.00,\n" +
                        "  \"AimSnapStrength\": 100.00,\n" +
                        "  \"AimMagnetism\": 100.00,\n" +
                        "  \"TrackingBullet\": 1,\n" +
                        "  \"BulletTracking\": 1,\n" +
                        "  \"AutoTrackingBullet\": 1,\n" +
                        "  \"MagicBullet\": 1,\n" +
                        "  \"HitboxExpansion\": 50.00,\n" +
                        "  \"BulletMagnetism\": 100.00,\n" +
                        "  \"ProjectileHoming\": 1,\n" +
                        "  \"HomingStrength\": 100.00,\n" +
                        "  \"BulletCurveFactor\": 50.00,\n" +
                        "  \"BulletVelocityMultiplier\": 100.00,\n" +
                        "  \"PhysicalDefenseBoost\": 100.00,\n" +
                        "  \"MagicDefenseBoost\": 100.00,\n" +
                        "  \"DamageReductionRatio\": 0.999,\n" +
                        "  \"DamageReduction\": 0.999,\n" +
                        "  \"IncomingDamageReduction\": 0.999,\n" +
                        "  \"ShieldMultiplier\": 100.00,\n" +
                        "  \"ShieldCapacity\": 100.00,\n" +
                        "  \"ArmorBoost\": 10000,\n" +
                        "  \"VestDurability\": 100.00,\n" +
                        "  \"HelmetDamageReduction\": 0.999,\n" +
                        "  \"TenacityRatio\": 0.999,\n" +
                        "  \"RecoilScale\": 0.00,\n" +
                        "  \"WeaponKickReduction\": 1.00,\n" +
                        "  \"AllGunsRecoilReduction\": 1.00,\n" +
                        "  \"ScopeShakeReduction\": 1.00,\n" +
                        "  \"ScopeRecoilMultiplier\": 0.00,\n" +
                        "  \"ScopeStability\": 5.00,\n" +
                        "  \"VerticalRecoilScale\": 0.00,\n" +
                        "  \"HorizontalRecoilScale\": 0.00,\n" +
                        "  \"BulletSpread\": 0.00,\n" +
                        "  \"DamageMultiplier\": 100.00,\n" +
                        "  \"DamageBoostRatio\": 100.00,\n" +
                        "  \"BulletDamageBoost\": 100.00,\n" +
                        "  \"HeadshotDamageMultiplier\": 100.00,\n" +
                        "  \"CriticalHitRate\": 100,\n" +
                        "  \"CriticalDamage\": 1000,\n" +
                        "  \"ArmorPenetration\": 1000,\n" +
                        "  \"GyroSampleRate\": 1000,\n" +
                        "  \"GyroSensitivityRatio\": 10.0,\n" +
                        "  \"GyroZeroDelay\": 1,\n" +
                        "  \"GyroSmoothFactor\": 1,\n" +
                        "  \"GyroStabilization\": 1,\n" +
                        "  \"GyroLatencyMode\": 0,\n" +
                        "  \"AntiAliasing\": 1,\n" +
                        "  \"ShadowQuality\": 2\n" +
                        "}\n";
            } else if (path.endsWith(".xml")) {
                content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n" +
                        "<map>\n" +
                        "  <int name=\"MaxFrameRate\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"TargetFPS\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"FPSLimit\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"FrameRateLimit\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"MobileFPSLimit\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"GraphicQuality\" value=\"4\" />\n" +
                        "  <int name=\"HDRMode\" value=\"1\" />\n" +
                        "  <int name=\"Unlock120Hz\" value=\"1\" />\n" +
                        "  <int name=\"Unlock144Hz\" value=\"1\" />\n" +
                        "  <int name=\"Unlock165Hz\" value=\"1\" />\n" +
                        "  <int name=\"Unlock185Hz\" value=\"1\" />\n" +
                        "  <int name=\"TouchBoostHz\" value=\"" + forcedFps + "\" />\n" +
                        "  <int name=\"TouchPollingRate\" value=\"1000\" />\n" +
                        "  <int name=\"GyroSampleRate\" value=\"1000\" />\n" +
                        "  <int name=\"FieldOfView\" value=\"150\" />\n" +
                        "  <int name=\"FPP_FOV\" value=\"150\" />\n" +
                        "  <int name=\"TPP_FOV\" value=\"100\" />\n" +
                        "  <int name=\"SprintSensitivity\" value=\"500\" />\n" +
                        "  <int name=\"AimAssist\" value=\"1\" />\n" +
                        "  <int name=\"AimAssistStrength\" value=\"1000\" />\n" +
                        "  <int name=\"AimAssistLevel\" value=\"10\" />\n" +
                        "  <int name=\"AimPrecision\" value=\"10\" />\n" +
                        "  <int name=\"TargetLockSensitivity\" value=\"1000\" />\n" +
                        "  <float name=\"CrosshairMagnetism\" value=\"100.00\" />\n" +
                        "  <float name=\"AimSnapStrength\" value=\"100.00\" />\n" +
                        "  <float name=\"AimMagnetism\" value=\"100.00\" />\n" +
                        "  <int name=\"TrackingBullet\" value=\"1\" />\n" +
                        "  <int name=\"BulletTracking\" value=\"1\" />\n" +
                        "  <int name=\"AutoTrackingBullet\" value=\"1\" />\n" +
                        "  <int name=\"MagicBullet\" value=\"1\" />\n" +
                        "  <float name=\"HitboxExpansion\" value=\"50.00\" />\n" +
                        "  <float name=\"BulletMagnetism\" value=\"100.00\" />\n" +
                        "  <float name=\"BulletVelocityMultiplier\" value=\"100.00\" />\n" +
                        "  <float name=\"PhysicalDefenseBoost\" value=\"100.00\" />\n" +
                        "  <float name=\"MagicDefenseBoost\" value=\"100.00\" />\n" +
                        "  <float name=\"DamageReductionRatio\" value=\"0.999\" />\n" +
                        "  <float name=\"ShieldMultiplier\" value=\"100.00\" />\n" +
                        "  <int name=\"ArmorBoost\" value=\"10000\" />\n" +
                        "  <float name=\"VestDurability\" value=\"100.00\" />\n" +
                        "  <float name=\"HelmetDamageReduction\" value=\"0.999\" />\n" +
                        "  <float name=\"RecoilScale\" value=\"0.00\" />\n" +
                        "  <float name=\"WeaponKickReduction\" value=\"1.00\" />\n" +
                        "  <float name=\"AllGunsRecoilReduction\" value=\"1.00\" />\n" +
                        "  <float name=\"ScopeShakeReduction\" value=\"1.00\" />\n" +
                        "  <float name=\"ScopeRecoilMultiplier\" value=\"0.00\" />\n" +
                        "  <float name=\"VerticalRecoilScale\" value=\"0.00\" />\n" +
                        "  <float name=\"HorizontalRecoilScale\" value=\"0.00\" />\n" +
                        "  <float name=\"DamageMultiplier\" value=\"100.00\" />\n" +
                        "  <float name=\"DamageBoostRatio\" value=\"100.00\" />\n" +
                        "  <float name=\"BulletDamageBoost\" value=\"100.00\" />\n" +
                        "  <float name=\"HeadshotDamageMultiplier\" value=\"100.00\" />\n" +
                        "  <int name=\"CriticalHitRate\" value=\"100\" />\n" +
                        "  <int name=\"CriticalDamage\" value=\"1000\" />\n" +
                        "  <int name=\"ArmorPenetration\" value=\"1000\" />\n" +
                        "  <int name=\"GyroSampleRate\" value=\"1000\" />\n" +
                        "  <float name=\"GyroSensitivityRatio\" value=\"10.0\" />\n" +
                        "  <int name=\"GyroZeroDelay\" value=\"1\" />\n" +
                        "  <int name=\"GyroSmoothFactor\" value=\"1\" />\n" +
                        "  <int name=\"GyroStabilization\" value=\"1\" />\n" +
                        "  <int name=\"GyroLatencyMode\" value=\"0\" />\n" +
                        "</map>\n";
            } else {
                content = "[Graphics]\n" +
                        "MaxFrameRate=" + forcedFps + "\n" +
                        "TargetFPS=" + forcedFps + "\n" +
                        "FPSLimit=" + forcedFps + "\n" +
                        "FrameRateLimit=" + forcedFps + "\n" +
                        "MobileFPSLimit=" + forcedFps + "\n" +
                        "GraphicQuality=4\n" +
                        "HDRMode=1\n" +
                        "HDRColorMode=2\n" +
                        "Unlock120Hz=1\n" +
                        "Unlock144Hz=1\n" +
                        "Unlock165Hz=1\n" +
                        "Unlock185Hz=1\n" +
                        "SuperResolution=1\n" +
                        "TouchBoostHz=" + forcedFps + "\n" +
                        "TouchPollingRate=1000\n" +
                        "TouchZeroDelay=1\n" +
                        "GyroSampleRate=1000\n" +
                        "FieldOfView=150\n" +
                        "FPP_FOV=150\n" +
                        "TPP_FOV=100\n" +
                        "SprintSensitivity=500\n" +
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
                        "ProjectileHoming=1\n" +
                        "HomingStrength=100.00\n" +
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
                        "RecoilScale=0.00\n" +
                        "WeaponKickReduction=1.00\n" +
                        "AllGunsRecoilReduction=1.00\n" +
                        "ScopeShakeReduction=1.00\n" +
                        "ScopeRecoilMultiplier=0.00\n" +
                        "ScopeStability=5.00\n" +
                        "VerticalRecoilScale=0.00\n" +
                        "HorizontalRecoilScale=0.00\n" +
                        "BulletSpread=0.00\n" +
                        "DamageMultiplier=100.00\n" +
                        "DamageBoostRatio=100.00\n" +
                        "BulletDamageBoost=100.00\n" +
                        "HeadshotDamageMultiplier=100.00\n" +
                        "CriticalHitRate=100\n" +
                        "CriticalDamage=1000\n" +
                        "ArmorPenetration=1000\n" +
                        "GyroSampleRate=1000\n" +
                        "GyroSensitivityRatio=10.0\n" +
                        "GyroZeroDelay=1\n" +
                        "GyroSmoothFactor=1\n" +
                        "GyroStabilization=1\n" +
                        "GyroLatencyMode=0\n" +
                        "AntiAliasing=1\n";
            }
            if (ConfigFileHelper.writeContentAtomic(path, content)) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "CODM competitive HDR " + forcedFps + "FPS + 1000% Aim/Tracking/Defense force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Applies anti-log, log directory cleaning, and telemetry suppression for CODM.
     */
    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    /**
     * Injects super-fast zero-delay touch response keys into CODM config files.
     */
    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] touchKeys = {
            "TouchBoostHz=185",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "TouchLatencyReduction=1",
            "ZeroInputLag=1",
            "TouchSensitivity=500"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, touchKeys, "[TouchEngine]");
        }
        Log.i(TAG, "CODM super-fast zero-delay touch applied for " + packageName);
    }

    /**
     * Injects FOV 150, Sprint 500, Aim Assist 1000%, and Gyro 1000Hz Ultra Response into CODM config files.
     */
    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "AimAssist=1",
            "AimAssistLevel=10",
            "AimAssistStrength=1000",
            "AimPrecision=10",
            "SprintSensitivity=500",
            "AlwaysSprint=1",
            "FieldOfView=150",
            "FPP_FOV=150",
            "TPP_FOV=100",
            "TargetLock=1",
            "TargetLockSensitivity=1000",
            "AimAssistRadius=1000",
            "CrosshairMagnetism=100.00",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00",
            "GyroSampleRate=1000",
            "GyroSensitivityRatio=10.0",
            "GyroZeroDelay=1",
            "GyroSmoothFactor=1",
            "GyroStabilization=1",
            "GyroLatencyMode=0"
        };
        for (String path : paths) {
            NativeConfigInjector.injectAimAssist(path);
            ConfigFileHelper.patchKeys(path, aimKeys, "[AimAssist]");
        }
        Log.i(TAG, "CODM FOV 150, 1000% Aim Assist & Gyro 1000Hz applied for " + packageName);
    }

    /**
     * Injects Zero Recoil & Weapon Stability for ALL guns and ALL scopes into CODM config files.
     */
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
            "GunKick=0",
            "WeaponKickReduction=1.00",
            "AllGunsRecoilReduction=1.00",
            "ScopeShakeReduction=1.00",
            "ScopeRecoilMultiplier=0.00",
            "ScopeStability=5.00",
            "BulletSpread=0.00",
            "CrosshairSpread=0.00",
            "SpreadScale=0.00",
            "BulletSpreadReduction=1",
            "FirstBulletAccuracy=1",
            "NoCameraShake=1",
            "AimPunchReduction=1",
            "FlinchReduction=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectNoRecoil(path);
            ConfigFileHelper.patchKeys(path, recoilKeys, "[WeaponStability]");
        }
        Log.i(TAG, "CODM Zero Recoil & Weapon Stability applied for " + packageName);
    }

    /**
     * Injects 1000% Damage Multiplier & Critical Penetration keys into CODM config files.
     */
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
            "CriticalHitRate=100",
            "CriticalDamage=1000",
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
            "SkillDamageMultiplier=100.00",
            "DamageAssetOverride=1",
            "AutoDamageExecutionMode=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[DamageScript]");
        }
        Log.i(TAG, "CODM Damage Boost 1000% & Bullet Penetration applied for " + packageName);
    }

    /**
     * Injects 1000% Armor Defense, Vest Durability, Helmet Protection, and Damage Reduction keys into CODM.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "PhysicalDefenseBoost=100.00",
            "MagicDefenseBoost=100.00",
            "PhysicalDefenseMultiplier=100.00",
            "MagicDefenseMultiplier=100.00",
            "DamageReductionRatio=0.999",
            "DamageReduction=0.999",
            "IncomingDamageReduction=0.999",
            "ShieldMultiplier=100.00",
            "ShieldCapacity=100.00",
            "ShieldStrength=100.00",
            "MaxHPMultiplier=50.00",
            "HPBoostRatio=50.00",
            "DamageAbsorbRatio=50.00",
            "ArmorBoost=10000",
            "MagicResistBoost=10000",
            "VestDurability=100.00",
            "VestDurabilityBoost=100.00",
            "HelmetDamageReduction=0.999",
            "TenacityRatio=0.999",
            "ResilienceLevel=10",
            "ArmorLevel=10",
            "DamageResistance=0.999",
            "ShieldEfficiency=100.00",
            "ShieldPointsMultiplier=100.00",
            "ArmorPlateEfficiency=100.00",
            "KineticArmorBoost=100.00",
            "FlakJacketRatio=0.999",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=100.00",
            "FallDamageReduction=1.00",
            "ExplosionResistance=0.999",
            "HeadshotDamageReduction=0.999",
            "HighDamageMitigationRatio=10.00",
            "HeavyHitAbsorption=10.00",
            "BurstDamageReduction=10.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[DefenseConfig]");
        }
        Log.i(TAG, "CODM 1000% Armor Defense & 100x Vest applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for CODM.
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
        Log.i(TAG, "CODM 10.0x Speed Boost & Sprint Agility applied for " + packageName);
    }

    /**
     * Injects 1000% Tracking Bullet, Bullet Magnetism, Magic Bullet, and Hitbox Expansion keys into CODM.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "TrackingBullet=1",
            "BulletTracking=1",
            "AutoTrackingBullet=1",
            "MagicBullet=1",
            "HitboxExpansion=50.00",
            "BulletMagnetism=100.00",
            "BulletCurveFactor=50.00",
            "BulletVelocityMultiplier=100.00",
            "TargetLockTracking=1",
            "FirstBulletAccuracy=1",
            "ProjectileHoming=1",
            "HomingStrength=100.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectTrackingBullet(path);
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "CODM 1000% Tracking Bullet & Magic Bullet applied for " + packageName);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    private static boolean applyPatch(String path, int targetFps) {
        String[] keys = {
            "MaxFrameRate=" + targetFps,
            "TargetFPS=" + targetFps,
            "FPSLimit=" + targetFps,
            "FrameRateLimit=" + targetFps,
            "MobileFPSLimit=" + targetFps,
            "GraphicQuality=4",
            "HDRMode=1",
            "HDRColorMode=2",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "SuperResolution=1",
            "TouchBoostHz=" + targetFps
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Graphics]");
    }
}
