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
                        "  \"FieldOfView\": 180,\n" +
                        "  \"FPP_FOV\": 180,\n" +
                        "  \"TPP_FOV\": 180,\n" +
                        "  \"SprintSensitivity\": 1000,\n" +
                        "  \"AlwaysSprint\": 1,\n" +
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
                        "  \"VestDurability\": 1000.00,\n" +
                        "  \"HelmetDamageReduction\": 0.9999,\n" +
                        "  \"TenacityRatio\": 0.9999,\n" +
                        "  \"RecoilScale\": 0.00,\n" +
                        "  \"WeaponKickReduction\": 1.00,\n" +
                        "  \"AllGunsRecoilReduction\": 1.00,\n" +
                        "  \"ScopeShakeReduction\": 1.00,\n" +
                        "  \"ScopeRecoilMultiplier\": 0.00,\n" +
                        "  \"ScopeStability\": 5.00,\n" +
                        "  \"VerticalRecoilScale\": 0.00,\n" +
                        "  \"HorizontalRecoilScale\": 0.00,\n" +
                        "  \"BulletSpread\": 0.00,\n" +
                        "  \"DamageMultiplier\": 1000.00,\n" +
                        "  \"DamageBoostRatio\": 1000.00,\n" +
                        "  \"BulletDamageBoost\": 1000.00,\n" +
                        "  \"HeadshotDamageMultiplier\": 1000.00,\n" +
                        "  \"CriticalHitRate\": 100,\n" +
                        "  \"CriticalDamage\": 10000,\n" +
                        "  \"ArmorPenetration\": 10000,\n" +
                        "  \"GyroSampleRate\": 1000,\n" +
                        "  \"GyroSensitivityRatio\": 20.0,\n" +
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
                        "  <int name=\"FieldOfView\" value=\"180\" />\n" +
                        "  <int name=\"FPP_FOV\" value=\"180\" />\n" +
                        "  <int name=\"TPP_FOV\" value=\"180\" />\n" +
                        "  <int name=\"SprintSensitivity\" value=\"1000\" />\n" +
                        "  <int name=\"AimAssist\" value=\"1\" />\n" +
                        "  <int name=\"AimAssistStrength\" value=\"10000\" />\n" +
                        "  <int name=\"AimAssistLevel\" value=\"10\" />\n" +
                        "  <int name=\"AimPrecision\" value=\"100\" />\n" +
                        "  <int name=\"TargetLockSensitivity\" value=\"10000\" />\n" +
                        "  <float name=\"CrosshairMagnetism\" value=\"100.00\" />\n" +
                        "  <float name=\"AimSnapStrength\" value=\"100.00\" />\n" +
                        "  <float name=\"AimMagnetism\" value=\"100.00\" />\n" +
                        "  <int name=\"TrackingBullet\" value=\"1\" />\n" +
                        "  <int name=\"BulletTracking\" value=\"1\" />\n" +
                        "  <int name=\"AutoTrackingBullet\" value=\"1\" />\n" +
                        "  <int name=\"MagicBullet\" value=\"1\" />\n" +
                        "  <float name=\"HitboxExpansion\" value=\"100.00\" />\n" +
                        "  <float name=\"BulletMagnetism\" value=\"100.00\" />\n" +
                        "  <float name=\"BulletVelocityMultiplier\" value=\"200.00\" />\n" +
                        "  <float name=\"PhysicalDefenseBoost\" value=\"1000.00\" />\n" +
                        "  <float name=\"MagicDefenseBoost\" value=\"1000.00\" />\n" +
                        "  <float name=\"DamageReductionRatio\" value=\"0.9999\" />\n" +
                        "  <float name=\"ShieldMultiplier\" value=\"1500.00\" />\n" +
                        "  <int name=\"ArmorBoost\" value=\"50000\" />\n" +
                        "  <float name=\"VestDurability\" value=\"1000.00\" />\n" +
                        "  <float name=\"HelmetDamageReduction\" value=\"0.9999\" />\n" +
                        "  <float name=\"RecoilScale\" value=\"0.00\" />\n" +
                        "  <float name=\"WeaponKickReduction\" value=\"1.00\" />\n" +
                        "  <float name=\"AllGunsRecoilReduction\" value=\"1.00\" />\n" +
                        "  <float name=\"ScopeShakeReduction\" value=\"1.00\" />\n" +
                        "  <float name=\"ScopeRecoilMultiplier\" value=\"0.00\" />\n" +
                        "  <float name=\"VerticalRecoilScale\" value=\"0.00\" />\n" +
                        "  <float name=\"HorizontalRecoilScale\" value=\"0.00\" />\n" +
                        "  <float name=\"DamageMultiplier\" value=\"1000.00\" />\n" +
                        "  <float name=\"DamageBoostRatio\" value=\"1000.00\" />\n" +
                        "  <float name=\"BulletDamageBoost\" value=\"1000.00\" />\n" +
                        "  <float name=\"HeadshotDamageMultiplier\" value=\"1000.00\" />\n" +
                        "  <int name=\"CriticalHitRate\" value=\"100\" />\n" +
                        "  <int name=\"CriticalDamage\" value=\"10000\" />\n" +
                        "  <int name=\"ArmorPenetration\" value=\"10000\" />\n" +
                        "  <int name=\"GyroSampleRate\" value=\"1000\" />\n" +
                        "  <float name=\"GyroSensitivityRatio\" value=\"20.0\" />\n" +
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
                        "FieldOfView=180\n" +
                        "FPP_FOV=180\n" +
                        "TPP_FOV=180\n" +
                        "SprintSensitivity=1000\n" +
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
                        "ProjectileHoming=1\n" +
                        "HomingStrength=100.00\n" +
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
                        "VestDurability=1000.00\n" +
                        "HelmetDamageReduction=0.9999\n" +
                        "TenacityRatio=0.9999\n" +
                        "RecoilScale=0.00\n" +
                        "WeaponKickReduction=1.00\n" +
                        "AllGunsRecoilReduction=1.00\n" +
                        "ScopeShakeReduction=1.00\n" +
                        "ScopeRecoilMultiplier=0.00\n" +
                        "ScopeStability=5.00\n" +
                        "VerticalRecoilScale=0.00\n" +
                        "HorizontalRecoilScale=0.00\n" +
                        "BulletSpread=0.00\n" +
                        "DamageMultiplier=1000.00\n" +
                        "DamageBoostRatio=1000.00\n" +
                        "BulletDamageBoost=1000.00\n" +
                        "HeadshotDamageMultiplier=1000.00\n" +
                        "CriticalHitRate=100\n" +
                        "CriticalDamage=10000\n" +
                        "ArmorPenetration=10000\n" +
                        "GyroSampleRate=1000\n" +
                        "GyroSensitivityRatio=20.0\n" +
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

    // ─── Delegated Common Tuning Injectors ───────────────────────────────────

    public static void applySuperFastTouch(String packageName) {
        CommonConfigTuningInjector.applySuperFastTouch(packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        CommonConfigTuningInjector.applyAimAssistConfig(packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        CommonConfigTuningInjector.applyRecoilControlConfig(packageName);
    }

    public static void applyDamageScriptConfig(String packageName) {
        CommonConfigTuningInjector.applyDamageScriptConfig(packageName);
    }

    public static void applyFastCooldownConfig(String packageName) {
        CommonConfigTuningInjector.applyFastCooldownConfig(packageName);
    }

    public static void applyShield1500Config(String packageName) {
        CommonConfigTuningInjector.applyShield1500Config(packageName);
    }

    public static void applyDroneViewUltraConfig(String packageName) {
        CommonConfigTuningInjector.applyDroneViewUltraConfig(packageName);
    }

    public static void applyDroneViewConfig(String packageName) {
        CommonConfigTuningInjector.applyDroneViewConfig(packageName);
    }

    public static void applyArmorDefConfig(String packageName) {
        CommonConfigTuningInjector.applyArmorDefConfig(packageName);
    }

    public static void applySpeedBoostConfig(String packageName) {
        CommonConfigTuningInjector.applySpeedBoostConfig(packageName);
    }

    public static void applyTrackingBulletConfig(String packageName) {
        CommonConfigTuningInjector.applyTrackingBulletConfig(packageName);
    }

    public static void applyAntiLog(String packageName) {
        CommonConfigTuningInjector.applyAntiLog(packageName);
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
