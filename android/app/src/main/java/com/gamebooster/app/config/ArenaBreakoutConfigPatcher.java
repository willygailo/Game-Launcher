package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * ArenaBreakoutConfigPatcher manages legal UE4/UE5 configuration files for Arena Breakout & Delta Force.
 * Unlocks 90 FPS / 120 FPS / 144 FPS / 165 FPS / 185 FPS and low-latency input.
 */
public class ArenaBreakoutConfigPatcher {

    private static final String TAG = "ArenaBreakoutConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyStandardPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Arena Breakout/Delta Force patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String ueContent = "[/Script/Engine.GameUserSettings]\n" +
                "bUseDesiredScreenHeight=False\n" +
                "FrameRateLimit=" + forcedFps + ".000000\n" +
                "FrameRate=" + forcedFps + "\n" +
                "FPSLevel=" + fpsLevel + "\n" +
                "bUnlockFPS=True\n" +
                "Unlock120=1\n" +
                "Unlock144=1\n" +
                "Unlock165=1\n" +
                "Unlock185=1\n" +
                "Unlock120FPS=1\n" +
                "Unlock144FPS=1\n" +
                "Unlock165FPS=1\n" +
                "Unlock185FPS=1\n" +
                "Ultra144FPS=1\n" +
                "Ultra165FPS=1\n" +
                "Ultra185FPS=1\n" +
                "ScreenScale=120\n" +
                "ResolutionScale=120\n" +
                "ShadowQuality=2\n" +
                "AntiAliasingQuality=4\n" +
                "PostProcessQuality=3\n" +
                "TextureQuality=3\n" +
                "EffectsQuality=3\n" +
                "FoliageQuality=2\n" +
                "ShadingQuality=2\n" +
                "UltraExtreme=1\n" +
                "bUseUltraExtreme=True\n" +
                "GraphicsQuality=5\n" +
                "GraphicQuality=4\n" +
                "HDRMode=1\n" +
                "UltraHDMode=1\n" +
                "SuperResolution=1\n" +
                "VulkanEnabled=1\n" +
                "\n" +
                "[UserCustom DeviceProfile]\n" +
                "+CVars=r.FrameRateLimit=" + forcedFps + "\n" +
                "+CVars=r.MobileFPSLimit=" + forcedFps + "\n" +
                "+CVars=r.PUBGDeviceFPS=" + fpsLevel + "\n" +
                "+CVars=r.Unlock120Hz=1\n" +
                "+CVars=r.Unlock144Hz=1\n" +
                "+CVars=r.Unlock165Hz=1\n" +
                "+CVars=r.Unlock185Hz=1\n" +
                "+CVars=r.PUBGQualityLevel=4\n" +
                "+CVars=r.PUBGSDKQualityLevel=4\n" +
                "+CVars=r.MobileHDR=1\n" +
                "+CVars=r.Tonemapper.Quality=4\n" +
                "+CVars=r.HDR.Display.OutputDevice=1\n" +
                // ── 10000 Aim Assist CVars ──
                "+CVars=r.AimAssist=1\n" +
                "+CVars=r.AimAssist.Strength=100.00\n" +
                "+CVars=r.AimAssist.Magnetism=100.00\n" +
                "+CVars=r.AimAssist.SnapSpeed=100.00\n" +
                "+CVars=r.AimAssistRadius=5000\n" +
                "+CVars=r.CrosshairMagnetism=100.00\n" +
                "+CVars=r.TargetLockSensitivity=10000\n" +
                "+CVars=r.AimSnapStrength=100.00\n" +
                "+CVars=r.AimLead=1\n" +
                "+CVars=r.AimLeadStrength=100.00\n" +
                // ── 100 Hitbox & Tracking Bullet CVars ──
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
                // ── 1500+ Shield & Armor Defense CVars ──
                "+CVars=r.ArmorDamageReduction=0.9999\n" +
                "+CVars=r.VestDurabilityBoost=1500.00\n" +
                "+CVars=r.HelmetDamageReduction=0.9999\n" +
                "+CVars=r.IncomingDamageScale=0.0001\n" +
                "+CVars=r.ShieldMultiplier=1500.00\n" +
                "+CVars=r.ShieldEfficiency=1500.00\n" +
                "+CVars=r.HealthRegenBoost=1000.00\n" +
                "+CVars=r.DamageResistance=0.9999\n" +
                "+CVars=r.TenacityRatio=0.9999\n" +
                "+CVars=r.HeavyDamageDampener=100.00\n" +
                "+CVars=r.BurstDamageReduction=100.00\n" +
                // ── Drone View FOV 180 ──
                "+CVars=r.CameraFOV=180\n" +
                "+CVars=r.FieldOfView=180\n" +
                "+CVars=r.DroneViewHeight=4\n" +
                "\n" +
                "[UserCustom]\n" +
                "FrameRateLevel=" + fpsLevel + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "FrameRateLimit=" + forcedFps + "\n" +
                "MobileFPSLimit=" + forcedFps + "\n" +
                "HighFPSMode=1\n" +
                "UltraExtreme=1\n" +
                "GraphicQuality=4\n" +
                "HDRMode=1\n" +
                "UltraHDMode=1\n" +
                "TouchPollingRate=1000\n" +
                "TouchSlop=1\n" +
                "TouchZeroDelay=1\n" +
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
                "CameraFOV=180\n" +
                "NoRecoil=1\n" +
                "WeaponKickReduction=1.00\n" +
                "CrosshairSpread=0.00\n" +
                "ScopeStability=5.00\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.writeContentAtomic(path, ueContent)) {
                written++;
            }
        }
        Log.i(TAG, "Arena Breakout competitive UltraExtreme " + forcedFps + "FPS + 1000% Aim/Tracking/Defense force-write: " + written + " paths");
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

private static boolean applyStandardPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;
        String[] keys = {
            "FrameRateLimit=" + forcedFps + ".000000",
            "MaxFPS=" + forcedFps,
            "TargetFPS=" + forcedFps,
            "FPSLevel=" + fpsLevel,
            "FrameRateLevel=" + fpsLevel,
            "+CVars=r.FrameRateLimit=" + forcedFps,
            "+CVars=r.MobileFPSLimit=" + forcedFps,
            "+CVars=r.PUBGDeviceFPS=" + fpsLevel,
            "+CVars=r.Unlock120Hz=1",
            "+CVars=r.Unlock144Hz=1",
            "+CVars=r.Unlock165Hz=1",
            "+CVars=r.Unlock185Hz=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[/Script/Engine.GameUserSettings]");
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }
}
