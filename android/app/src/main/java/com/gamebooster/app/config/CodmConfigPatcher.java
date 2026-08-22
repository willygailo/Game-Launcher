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
                        "  \"SprintSensitivity\": 150,\n" +
                        "  \"AlwaysSprint\": 1,\n" +
                        "  \"AimAssist\": 1,\n" +
                        "  \"AimAssistStrength\": 150,\n" +
                        "  \"AimAssistLevel\": 5,\n" +
                        "  \"TargetLockSensitivity\": 150,\n" +
                        "  \"RecoilScale\": 0.00,\n" +
                        "  \"WeaponKickReduction\": 1.50,\n" +
                        "  \"AllGunsRecoilReduction\": 1.50,\n" +
                        "  \"ScopeShakeReduction\": 1.50,\n" +
                        "  \"ScopeRecoilMultiplier\": 0.00,\n" +
                        "  \"ScopeStability\": 1.50,\n" +
                        "  \"VerticalRecoilScale\": 0.00,\n" +
                        "  \"HorizontalRecoilScale\": 0.00,\n" +
                        "  \"BulletSpread\": 0.00,\n" +
                        "  \"DamageBoostRatio\": 2.50,\n" +
                        "  \"BulletDamageBoost\": 2.50,\n" +
                        "  \"HeadshotDamageMultiplier\": 3.50,\n" +
                        "  \"CriticalHitRate\": 99,\n" +
                        "  \"GyroSampleRate\": 1000,\n" +
                        "  \"GyroSensitivityRatio\": 2.5,\n" +
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
                        "  <int name=\"SprintSensitivity\" value=\"150\" />\n" +
                        "  <int name=\"AimAssist\" value=\"1\" />\n" +
                        "  <int name=\"AimAssistStrength\" value=\"150\" />\n" +
                        "  <int name=\"AimAssistLevel\" value=\"5\" />\n" +
                        "  <int name=\"TargetLockSensitivity\" value=\"150\" />\n" +
                        "  <float name=\"RecoilScale\" value=\"0.00\" />\n" +
                        "  <float name=\"WeaponKickReduction\" value=\"1.50\" />\n" +
                        "  <float name=\"AllGunsRecoilReduction\" value=\"1.50\" />\n" +
                        "  <float name=\"ScopeShakeReduction\" value=\"1.50\" />\n" +
                        "  <float name=\"ScopeRecoilMultiplier\" value=\"0.00\" />\n" +
                        "  <float name=\"VerticalRecoilScale\" value=\"0.00\" />\n" +
                        "  <float name=\"HorizontalRecoilScale\" value=\"0.00\" />\n" +
                        "  <float name=\"DamageBoostRatio\" value=\"2.50\" />\n" +
                        "  <float name=\"BulletDamageBoost\" value=\"2.50\" />\n" +
                        "  <float name=\"HeadshotDamageMultiplier\" value=\"3.50\" />\n" +
                        "  <int name=\"CriticalHitRate\" value=\"99\" />\n" +
                        "  <int name=\"GyroSampleRate\" value=\"1000\" />\n" +
                        "  <float name=\"GyroSensitivityRatio\" value=\"2.5\" />\n" +
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
                        "SprintSensitivity=150\n" +
                        "AimAssist=1\n" +
                        "AimAssistStrength=150\n" +
                        "AimAssistLevel=5\n" +
                        "TargetLockSensitivity=150\n" +
                        "RecoilScale=0.00\n" +
                        "WeaponKickReduction=1.50\n" +
                        "AllGunsRecoilReduction=1.50\n" +
                        "ScopeShakeReduction=1.50\n" +
                        "ScopeRecoilMultiplier=0.00\n" +
                        "ScopeStability=1.50\n" +
                        "VerticalRecoilScale=0.00\n" +
                        "HorizontalRecoilScale=0.00\n" +
                        "BulletSpread=0.00\n" +
                        "DamageBoostRatio=2.50\n" +
                        "BulletDamageBoost=2.50\n" +
                        "HeadshotDamageMultiplier=3.50\n" +
                        "CriticalHitRate=99\n" +
                        "GyroSampleRate=1000\n" +
                        "GyroSensitivityRatio=2.5\n" +
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
        Log.i(TAG, "CODM competitive HDR " + forcedFps + "FPS force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
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
            "TouchSensitivity=150"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, touchKeys, "[TouchEngine]");
        }
        Log.i(TAG, "CODM super-fast zero-delay touch applied for " + packageName);
    }

    /**
     * Injects FOV 150, Sprint 150, Aim Assist 150%, and Gyro 1000Hz Ultra Response into CODM config files.
     */
    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "AimAssist=1",
            "AimAssistLevel=5",
            "AimAssistStrength=150",
            "SprintSensitivity=150",
            "AlwaysSprint=1",
            "FieldOfView=150",
            "FPP_FOV=150",
            "TPP_FOV=100",
            "TargetLockSensitivity=150",
            "GyroSampleRate=1000",
            "GyroSensitivityRatio=2.5",
            "GyroZeroDelay=1",
            "GyroSmoothFactor=1",
            "GyroStabilization=1",
            "GyroLatencyMode=0"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, aimKeys, "[AimAssist]");
        }
        Log.i(TAG, "CODM FOV 150, Aim Assist 150% & Gyro 1000Hz applied for " + packageName);
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
            "RecoilReduction=1.50",
            "WeaponStability=150",
            "ScreenShake=0",
            "GunKick=0",
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
     * Injects Damage Multiplier & Critical Penetration keys into CODM config files.
     */
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
            "CriticalHitRate=100",
            "CriticalDamage=100",
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
            "SkillDamageMultiplier=5.00",
            "DamageAssetOverride=1",
            "AutoDamageExecutionMode=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[DamageScript]");
        }
        Log.i(TAG, "CODM Damage Boost 500% & Bullet Penetration applied for " + packageName);
    }

    /**
     * Injects Armor Defense, Vest Durability, Helmet Protection, and Damage Reduction keys into CODM.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "PhysicalDefenseBoost=5.00",
            "MagicDefenseBoost=5.00",
            "DamageReductionRatio=0.85",
            "DamageReduction=0.85",
            "IncomingDamageReduction=0.85",
            "ShieldMultiplier=5.00",
            "ShieldCapacity=5.00",
            "ShieldStrength=5.00",
            "MaxHPMultiplier=3.00",
            "HPBoostRatio=3.00",
            "DamageAbsorbRatio=3.00",
            "ArmorBoost=500",
            "MagicResistBoost=500",
            "VestDurability=5.00",
            "VestDurabilityBoost=5.00",
            "HelmetDamageReduction=0.90",
            "TenacityRatio=0.80",
            "ResilienceLevel=5",
            "ArmorLevel=6",
            "DamageResistance=0.85",
            "ShieldEfficiency=5.00",
            "ShieldPointsMultiplier=5.00",
            "ArmorPlateEfficiency=5.00",
            "KineticArmorBoost=5.00",
            "FlakJacketRatio=0.90",
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
        Log.i(TAG, "CODM Armor Defense 85% Reduction & 5.0x Vest applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for CODM.
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
        Log.i(TAG, "CODM 3.0x Speed Boost & Sprint Agility applied for " + packageName);
    }

    /**
     * Injects Tracking Bullet, Bullet Magnetism, Magic Bullet, and Hitbox Expansion keys into CODM.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "TrackingBullet=1",
            "BulletTracking=1",
            "MagicBullet=1",
            "HitboxExpansion=1.50",
            "BulletMagnetism=1.50",
            "BulletVelocityMultiplier=2.00",
            "TargetLockTracking=1",
            "FirstBulletAccuracy=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "CODM Tracking Bullet & Magic Bullet applied for " + packageName);
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
