package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * FarlightConfigPatcher manages internal UE4 Solarland config files for
 * Farlight 84 (all global and regional package releases).
 *
 * Configures 120 / 144 / 165 / 185 FPS unlock, 1000Hz touch & gyro polling,
 * zero input lag, recoil control, and performance rendering pipeline.
 */
public class FarlightConfigPatcher {

    private static final String TAG = "FarlightConfigPatcher";

    // ─── Standard Patch ───────────────────────────────────────────────────────

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Farlight 84 patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── UltraExtreme 144fps SuperSmooth Patch ───────────────────────────────

    /**
     * Applies 144fps SuperSmooth + UltraExtreme max graphics to Farlight 84.
     *
     * @return true if at least one path was written
     */
    public static boolean patchUltraExtreme144(String packageName) {
        if (packageName == null) return false;

        String[] keys = {
            "MaxFPS=144",
            "TargetFPS=144",
            "FrameRateLimit=144",
            "FrameRateLevel=8",
            "UnlockFPS=1",
            "Unlock144FPS=1",
            "Ultra144FPS=1",
            "Unlock120Hz=1", "Unlock144Hz=1", "Unlock165Hz=1", "Unlock185Hz=1",
            "HighFPSMode=1", "SuperHighFPS=1",
            "ShadingQuality=4", "TextureQuality=4", "ShadowQuality=2",
            "AntiAliasingQuality=4", "BloomQuality=5", "MaxAnisotropy=16",
            "HDRMode=1", "ResolutionScale=120",
            "UltraExtreme=1", "bUseUltraExtreme=True",
            "bFramePacingEnabled=True", "Vsync=0",
            "TouchBoostHz=144", "TouchPollingRate=1000",
            "GyroSampleRate=1000", "GyroZeroDelay=1",
            // Zero Recoil & Aim Assist
            "ZeroRecoil=1", "NoRecoil=1", "RecoilControl=1", "RecoilScale=0.00",
            "VerticalRecoil=0.00", "HorizontalRecoil=0.00", "RecoilReduction=1.00",
            "WeaponStability=1000", "BulletSpread=0.00",
            "AimAssist=1", "AimAssistStrength=10000", "AimAssistLevel=10", "AimPrecision=100",
            "CrosshairMagnetism=100.00", "AimSnapStrength=100.00", "AimMagnetism=100.00",
            "TrackingBullet=1", "BulletTracking=1", "BulletMagnetism=100.00", "HitboxExpansion=100.00"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[Graphics]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "Farlight UltraExtreme144 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL Farlight 84 config paths unconditionally.
     *
     * @return true if at least one path was written
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            String content;
            if (path.endsWith(".json")) {
                content = "{\n" +
                        "  \"FrameRateLimit\": " + forcedFps + ",\n" +
                        "  \"MaxFPS\": " + forcedFps + ",\n" +
                        "  \"TargetFPS\": " + forcedFps + ",\n" +
                        "  \"FPS\": " + forcedFps + ",\n" +
                        "  \"MobileFPSLimit\": " + forcedFps + ",\n" +
                        "  \"FPSLevel\": " + fpsLevel + ",\n" +
                        "  \"GraphicQuality\": 3,\n" +
                        "  \"HighFPSMode\": 1,\n" +
                        "  \"Unlock185Hz\": 1,\n" +
                        "  \"Unlock165Hz\": 1,\n" +
                        "  \"Unlock144Hz\": 1,\n" +
                        "  \"Unlock120Hz\": 1,\n" +
                        "  \"TouchPollingRate\": 1000,\n" +
                        "  \"TouchBoostHz\": " + forcedFps + ",\n" +
                        "  \"TouchZeroDelay\": 1,\n" +
                        "  \"GyroPollingRate\": 1000,\n" +
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
                        "  \"VestDurability\": 1500.00,\n" +
                        "  \"HelmetDamageReduction\": 0.9999,\n" +
                        "  \"TenacityRatio\": 0.9999,\n" +
                        "  \"DamageMultiplier\": 1000.00,\n" +
                        "  \"DamageBoostRatio\": 1000.00,\n" +
                        "  \"BulletDamageBoost\": 1000.00,\n" +
                        "  \"HeadshotDamageMultiplier\": 1000.00,\n" +
                        "  \"CriticalHitRate\": 100,\n" +
                        "  \"CriticalDamage\": 10000,\n" +
                        "  \"DroneView\": 1,\n" +
                        "  \"DroneViewHeight\": 4,\n" +
                        "  \"CameraFOV\": 180,\n" +
                        "  \"RecoilReduction\": 1.00,\n" +
                        "  \"LowLatencyMode\": 1,\n" +
                        "  \"AntiAliasing\": 1\n" +
                        "}\n";
            } else {
                // UE4 INI format (Solarland / GameUserSettings.ini / UserCustom.ini)
                content = "[/Script/Engine.GameUserSettings]\n" +
                        "bUseVSync=False\n" +
                        "FrameRateLimit=" + forcedFps + ".000000\n" +
                        "ResolutionSizeX=2400\n" +
                        "ResolutionSizeY=1080\n" +
                        "WindowMode=0\n" +
                        "[ScalabilityGroups]\n" +
                        "sg.ResolutionQuality=100.000000\n" +
                        "sg.ViewDistanceQuality=3\n" +
                        "sg.AntiAliasingQuality=1\n" +
                        "sg.ShadowQuality=0\n" +
                        "sg.PostProcessQuality=1\n" +
                        "sg.TextureQuality=3\n" +
                        "sg.EffectsQuality=1\n" +
                        "[UserCustom DeviceProfile]\n" +
                        "+CVars=r.Solarland.MaxFPS=" + forcedFps + "\n" +
                        "+CVars=r.FrameRateLimit=" + forcedFps + "\n" +
                        "+CVars=r.MobileFPSLimit=" + forcedFps + "\n" +
                        "+CVars=r.Unlock120Hz=1\n" +
                        "+CVars=r.Unlock144Hz=1\n" +
                        "+CVars=r.Unlock165Hz=1\n" +
                        "+CVars=r.Unlock185Hz=1\n" +
                        "+CVars=r.AimAssist=1\n" +
                        "+CVars=r.AimAssist.Strength=100.00\n" +
                        "+CVars=r.AimAssist.Magnetism=100.00\n" +
                        "+CVars=r.AimAssist.SnapSpeed=100.00\n" +
                        "+CVars=r.AimAssistRadius=5000\n" +
                        "+CVars=r.CrosshairMagnetism=100.00\n" +
                        "+CVars=r.TargetLockSensitivity=10000\n" +
                        "+CVars=r.AimSnapStrength=100.00\n" +
                        "+CVars=r.BulletTracking=1\n" +
                        "+CVars=r.MagicBullet=1\n" +
                        "+CVars=r.HitboxExpansion=100.00\n" +
                        "+CVars=r.BulletMagnetism=100.00\n" +
                        "+CVars=r.BulletVelocityScale=200.00\n" +
                        "+CVars=r.BulletCurveFactor=100.00\n" +
                        "+CVars=r.TargetLockTracking=1\n" +
                        "+CVars=r.FirstBulletAccuracy=1\n" +
                        "+CVars=r.ProjectileHoming=1\n" +
                        "+CVars=r.HomingStrength=100.00\n" +
                        "+CVars=r.ArmorDamageReduction=0.9999\n" +
                        "+CVars=r.ShieldEfficiency=1500.00\n" +
                        "+CVars=r.ShieldMultiplier=1500.00\n" +
                        "+CVars=r.ShieldRechargeRate=1000.00\n" +
                        "+CVars=r.ShieldCapacityBoost=1500.00\n" +
                        "+CVars=r.DamageResistance=0.9999\n" +
                        "+CVars=r.VestDurabilityBoost=1500.00\n" +
                        "+CVars=r.HelmetDamageReduction=0.9999\n" +
                        "+CVars=r.MaxHPMultiplier=100.00\n" +
                        "+CVars=r.IncomingDamageScale=0.0001\n" +
                        "+CVars=r.HeavyDamageDampener=100.00\n" +
                        "+CVars=r.BurstDamageReduction=100.00\n" +
                        "+CVars=r.CameraFOV=180\n" +
                        "+CVars=r.FieldOfView=180\n" +
                        "+CVars=r.DroneViewHeight=4\n" +
                        "[SolarlandGraphics]\n" +
                        "FrameRateLimit=" + forcedFps + "\n" +
                        "MaxFPS=" + forcedFps + "\n" +
                        "TargetFPS=" + forcedFps + "\n" +
                        "FPS=" + forcedFps + "\n" +
                        "MobileFPSLimit=" + forcedFps + "\n" +
                        "FPSLevel=" + fpsLevel + "\n" +
                        "HighFPSMode=1\n" +
                        "Unlock185Hz=1\n" +
                        "Unlock165Hz=1\n" +
                        "Unlock144Hz=1\n" +
                        "Unlock120Hz=1\n" +
                        "TouchPollingRate=1000\n" +
                        "TouchBoostHz=" + forcedFps + "\n" +
                        "TouchZeroDelay=1\n" +
                        "GyroPollingRate=1000\n" +
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
                        "DamageMultiplier=1000.00\n" +
                        "BulletDamageBoost=1000.00\n" +
                        "HeadshotDamageMultiplier=1000.00\n" +
                        "CriticalHitRate=100\n" +
                        "CriticalDamage=10000\n" +
                        "DroneView=1\n" +
                        "DroneViewHeight=4\n" +
                        "CameraFOV=180\n" +
                        "RecoilReduction=1.00\n" +
                        "WeaponKickScale=0.00\n" +
                        "ZeroInputLag=1\n";
            }
            if (ConfigFileHelper.writeContentAtomic(path, content)) {
                written++;
            }
        }
        Log.i(TAG, "Farlight 84 competitive " + forcedFps + "FPS + 1000% Aim/Tracking/Defense force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

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

private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    private static boolean applyPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;
        String[] keys = {
            "FrameRateLimit=" + forcedFps + ".000000",
            "MaxFPS=" + forcedFps,
            "TargetFPS=" + forcedFps,
            "FPS=" + forcedFps,
            "MobileFPSLimit=" + forcedFps,
            "FPSLevel=" + fpsLevel,
            "+CVars=r.Solarland.MaxFPS=" + forcedFps,
            "+CVars=r.FrameRateLimit=" + forcedFps,
            "+CVars=r.MobileFPSLimit=" + forcedFps,
            "+CVars=r.Unlock120Hz=1",
            "+CVars=r.Unlock144Hz=1",
            "+CVars=r.Unlock165Hz=1",
            "+CVars=r.Unlock185Hz=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[SolarlandGraphics]");
    }
}
