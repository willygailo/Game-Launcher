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

    // ─── UltraExtreme 144fps SuperSmooth Patch ───────────────────────────────

    /**
     * Applies 144fps SuperSmooth + UltraExtreme max graphics to Free Fire / Free Fire MAX.
     * Injects full quality unlock into [FFGraphics] section of all config paths.
     *
     * @return true if at least one path was written
     */
    public static boolean patchUltraExtreme144(String packageName) {
        if (packageName == null) return false;
        final FpsUnlockTier tier = FpsUnlockTier.FPS_144;

        String[] keys = {
            "HighFPS=1",
            "HighFPSMode=1",
            "FPSMode=2",
            "FrameRateLevel=8",
            "MaxFPS=144",
            "TargetFPS=144",
            "FrameRateLimit=144",
            "UnlockFPS=1",
            "Unlock144FPS=1",
            "Ultra144FPS=1",
            "SuperHighFPS=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            // ── Max Graphics ──
            "GraphicLevel=4",
            "TextureQuality=4",
            "Shadow=1",
            "ShadowQuality=2",
            "ShadowResolution=2048",
            "AntiAliasingQuality=4",
            "BloomQuality=5",
            "MaxAnisotropy=16",
            "HighResolution=1",
            "ResolutionScale=120",
            "HDRMode=1",
            "UltraHDMode=1",
            "UltraExtreme=1",
            "bUseUltraExtreme=True",
            "SuperResolution=1",
            "VulkanEnabled=1",
            "bReduceLoadedMips=False",
            // ── SuperSmooth Frame Pacing ──
            "bFramePacingEnabled=True",
            "Vsync=0",
            "HighFreqTouchHz=144",
            "TouchBoostHz=144",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "GyroSampleRate=1000",
            "GyroSensitivityRatio=20.0",
            "GyroZeroDelay=1",
            "GyroSmoothFactor=1",
            "GyroStabilization=1",
            // ── 1000% Aim Assist, Target Lock & Magnetism ──
            "AimAssist=1",
            "AutoAimPrecision=100.0",
            "AimAssistStrength=10000",
            "AimAssistLevel=10",
            "AimPrecision=100",
            "AutoAim=1",
            "AimTracking=1",
            "TargetLock=1",
            "TargetLockSensitivity=10000",
            "AimAssistRadius=5000",
            "CrosshairMagnetism=100.00",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00",
            "SprintSensitivity=1000",
            "GeneralSensitivity=1000",
            "RedDotSensitivity=1000",
            "TPPFov=180",
            "FPPFov=180",
            "HeadshotAimAssist=1",
            "PrecisionAim=1",
            // ── All Guns Zero Recoil ──
            "NoRecoil=1",
            "ZeroRecoil=1",
            "RecoilControl=1",
            "RecoilScale=0.00",
            "VerticalRecoil=0.00",
            "HorizontalRecoil=0.00",
            "RecoilReduction=1.00",
            "AllWeaponRecoilFix=1",
            "ScopeStabilization=1",
            "ScopeStability=5.00",
            "BulletSpread=0.00",
            "SpreadScale=0.00",
            "FirstBulletAccuracy=1",
            // ── Tracking Bullet ──
            "TrackingBullet=1",
            "BulletTracking=1",
            "AutoTrackingBullet=1",
            "MagicBullet=1",
            "HitboxExpansion=100.00",
            "BulletMagnetism=100.00"
        };

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.patchKeys(path, keys, "[FFGraphics]")) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "FreeFire UltraExtreme144 SuperSmooth patch: " + written + " paths for " + packageName);
        return written > 0;
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
                // ── 1000% Aim Assist & Smart Lock ──
                "AimAssist=1\n" +
                "AutoAimPrecision=100.0\n" +
                "AimAssistStrength=10000\n" +
                "AimAssistLevel=10\n" +
                "AimPrecision=100\n" +
                "AutoAim=1\n" +
                "AimTracking=1\n" +
                "TargetLock=1\n" +
                "TargetLockSensitivity=10000\n" +
                "AimAssistRadius=5000\n" +
                "CrosshairMagnetism=100.00\n" +
                "AimSnapStrength=100.00\n" +
                "AimMagnetism=100.00\n" +
                "SprintSensitivity=1000\n" +
                "GeneralSensitivity=1000\n" +
                "RedDotSensitivity=1000\n" +
                "TPPFov=180\n" +
                "FPPFov=180\n" +
                // ── All Guns & All Scopes Zero Recoil ──
                "NoRecoil=1\n" +
                "ZeroRecoil=1\n" +
                "RecoilControl=1\n" +
                "RecoilScale=0.00\n" +
                "VerticalRecoil=0.00\n" +
                "HorizontalRecoil=0.00\n" +
                "RecoilReduction=1.00\n" +
                "AllWeaponRecoilFix=1\n" +
                "ScopeStabilization=1\n" +
                "ScopeStability=5.00\n" +
                "IronSightRecoil=0.00\n" +
                "RedDotRecoil=0.00\n" +
                "Scope2xRecoil=0.00\n" +
                "Scope4xRecoil=0.00\n" +
                "SniperScopeRecoil=0.00\n" +
                "ThermalScopeRecoil=0.00\n" +
                "MP40RecoilScale=0.00\n" +
                "M1887RecoilScale=0.00\n" +
                "SCARRecoilScale=0.00\n" +
                "AKRecoilScale=0.00\n" +
                "GrozaRecoilScale=0.00\n" +
                "WoodpeckerRecoilScale=0.00\n" +
                "AWMRecoilScale=0.00\n" +
                "M82BRecoilScale=0.00\n" +
                "UMPRecoilScale=0.00\n" +
                "GunShakeReduction=1.00\n" +
                "NoCameraShake=1\n" +
                "WeaponStability=500\n" +
                // ── 1000% Damage Overdrive ──
                "DamageMultiplier=1000.00\n" +
                "PhysicalDamageBoost=1000.00\n" +
                "MagicDamageBoost=1000.00\n" +
                "TrueDamageBoost=1000.00\n" +
                "DamageBoostRatio=1000.00\n" +
                "HeadshotDamageMultiplier=1000.00\n" +
                "BulletDamageBoost=1000.00\n" +
                "CriticalDamage=10000\n" +
                "CriticalHitRate=100\n" +
                "ArmorPenetration=10000\n" +
                // ── 1000% Tracking Bullet ──
                "TrackingBullet=1\n" +
                "BulletTracking=1\n" +
                "AutoTrackingBullet=1\n" +
                "MagicBullet=1\n" +
                "BulletMagnetism=100.00\n" +
                "HitboxExpansion=100.00\n" +
                "ProjectileHoming=1\n" +
                "HomingStrength=100.00\n" +
                "BulletCurveFactor=100.00\n" +
                "BulletVelocityMultiplier=200.00\n" +
                // ── 1500+ Shield & Armor Defense ──
                "PhysicalDefenseBoost=1000.00\n" +
                "MagicDefenseBoost=1000.00\n" +
                "PhysicalDefenseMultiplier=1000.00\n" +
                "MagicDefenseMultiplier=1000.00\n" +
                "DamageReductionRatio=0.9999\n" +
                "DamageReduction=0.9999\n" +
                "IncomingDamageReduction=0.9999\n" +
                "ShieldMultiplier=1500.00\n" +
                "ShieldCapacity=1500.00\n" +
                "ShieldStrength=1500.00\n" +
                "MaxHPMultiplier=100.00\n" +
                "HPBoostRatio=100.00\n" +
                "DamageAbsorbRatio=100.00\n" +
                "ArmorBoost=50000\n" +
                "MagicResistBoost=50000\n" +
                "VestDurability=1000.00\n" +
                "VestDurabilityBoost=1000.00\n" +
                "HelmetDamageReduction=0.9999\n" +
                "TenacityRatio=0.9999\n" +
                "HealthRegenBoost=1000.00\n" +
                "HeavyHitAbsorption=100.00\n" +
                "BurstDamageReduction=100.00\n" +
                "HighDamageMitigationRatio=100.00\n" +
                "TouchResponseLevel=3\n" +
                "HighFreqTouchHz=" + forcedFps + "\n" +
                "TouchPollingRate=1000\n" +
                "TouchZeroDelay=1\n" +
                "GyroSampleRate=1000\n" +
                "GyroSensitivityRatio=20.0\n" +
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
        Log.i(TAG, "FreeFire competitive " + forcedFps + "FPS + 1000% Damage/Aim/Tracking/Defense force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Applies anti-log, report cleaner, and telemetry suppression for Free Fire.
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
