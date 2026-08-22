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
                        "  \"AimAssistStrength\": 150,\n" +
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
                        "AimAssistStrength=150\n" +
                        "RecoilReduction=1.50\n" +
                        "WeaponKickScale=0.00\n" +
                        "ZeroInputLag=1\n";
            }
            if (ConfigFileHelper.writeContentAtomic(path, content)) {
                written++;
            }
        }
        Log.i(TAG, "Farlight 84 competitive " + forcedFps + "FPS force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] touchKeys = {
            "TouchBoostHz=185",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "LowLatencyMode=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, touchKeys, "[TouchEngine]");
        }
        Log.i(TAG, "Farlight 84 fast zero-delay touch applied for " + packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "+CVars=r.AimAssist=1",
            "+CVars=r.AimAssistStrength=2.0",
            "+CVars=r.AimAssistRadius=200",
            "+CVars=r.GyroSampleRate=1000",
            "+CVars=r.GyroZeroDelay=1",
            "AimAssist=1",
            "AimAssistStrength=150",
            "GyroSensitivity=150"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, aimKeys, "[AimAssist]");
        }
        Log.i(TAG, "Farlight 84 Aim Assist & Gyro 1000Hz applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "+CVars=r.WeaponRecoilScale=0.00",
            "+CVars=r.VerticalRecoilMultiplier=0.00",
            "+CVars=r.HorizontalRecoilMultiplier=0.00",
            "+CVars=r.VerticalRecoilScale=0.00",
            "+CVars=r.HorizontalRecoilScale=0.00",
            "+CVars=r.WeaponKickScale=0.00",
            "+CVars=r.GunKickScale=0.00",
            "+CVars=r.ScreenShake=0",
            "+CVars=r.WeaponKick=0",
            "+CVars=r.CameraShake=0",
            "+CVars=r.AimPunchMultiplier=0.00",
            "+CVars=r.FlinchMultiplier=0.00",
            "+CVars=r.ScopeShakeReduction=1.50",
            "+CVars=r.ScopeStability=1.50",
            "+CVars=r.ScopeRecoilMultiplier=0.00",
            "+CVars=r.SpreadScale=0.00",
            "+CVars=r.CrosshairSpread=0.00",
            "+CVars=r.BulletSpread=0.00",
            "RecoilControl=1",
            "ZeroRecoil=1",
            "NoRecoil=1",
            "RecoilScale=0.00",
            "RecoilReduction=1.50",
            "WeaponStability=150"
        };
        for (String path : paths) {
            NativeConfigInjector.injectNoRecoil(path);
            ConfigFileHelper.patchKeys(path, recoilKeys, "[WeaponStability]");
        }
        Log.i(TAG, "Farlight 84 Zero Recoil & Weapon Stability applied for " + packageName);
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageKeys = {
            "+CVars=r.DamageMultiplier=5.00",
            "+CVars=r.BulletDamageScale=5.00",
            "+CVars=r.PhysicalDamageBoost=5.00",
            "+CVars=r.DamageBoost=5.00",
            "+CVars=r.HeadshotMultiplier=5.00",
            "+CVars=r.HeadshotDamageMultiplier=5.00",
            "+CVars=r.CriticalDamage=100",
            "+CVars=r.CriticalHitRate=100",
            "+CVars=r.CriticalDamageMultiplier=5.00",
            "+CVars=r.PenetrationBoost=100",
            "+CVars=r.ArmorPenetration=100",
            "+CVars=r.BulletVelocityMultiplier=5.00",
            "+CVars=r.HitboxExpansion=2.50",
            "+CVars=r.BodyDamageMultiplier=3.50",
            "+CVars=r.LimbDamageMultiplier=3.00",
            "+CVars=r.ExplosiveDamageMultiplier=3.50",
            "+CVars=r.MovementSpeedMultiplier=3.00",
            "+CVars=r.SprintSpeedMultiplier=3.00",
            "DamageMultiplier=5.00",
            "DamageBoost=5.00",
            "HeadshotDamageMultiplier=5.00",
            "CriticalHitRate=100",
            "CriticalDamage=100",
            "ArmorPenetration=100",
            "MovementSpeedMultiplier=3.00",
            "SprintSpeedMultiplier=3.00",
            "SprintSensitivity=200",
            "AttackSpeedMultiplier=3.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[UserCustom DeviceProfile]");
        }
        Log.i(TAG, "Farlight 84 5.0x Damage Boost & Headshot Multiplier applied for " + packageName);
    }

    /**
     * Injects Shield Recharge Rate, Shield Efficiency, and Armor Damage Reduction for Farlight 84.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "+CVars=r.ArmorDamageReduction=0.85",
            "+CVars=r.ShieldRechargeRate=5.00",
            "+CVars=r.ShieldEfficiency=5.00",
            "+CVars=r.ShieldCapacityBoost=5.00",
            "+CVars=r.DamageResistance=0.85",
            "+CVars=r.VestDurabilityBoost=5.00",
            "+CVars=r.HelmetDamageReduction=0.90",
            "+CVars=r.MaxHPMultiplier=3.00",
            "+CVars=r.HealthRegenDelay=0.00",
            "+CVars=r.HealthRegenBoost=5.00",
            "+CVars=r.IncomingDamageReduction=0.85",
            "+CVars=r.ExplosionResistance=0.90",
            "+CVars=r.FallDamageReduction=1.00",
            "ShieldEfficiency=5.00",
            "ShieldCapacity=5.00",
            "ShieldMultiplier=5.00",
            "ShieldStrength=5.00",
            "ArmorBoost=500",
            "VestDurabilityBoost=5.00",
            "DamageReductionRatio=0.85",
            "IncomingDamageReduction=0.85",
            "PhysicalDefenseBoost=5.00",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=5.00"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[DefenseConfig]");
        }
        Log.i(TAG, "Farlight 84 Shield 5.0x & Armor Defense 85% applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Farlight 84.
     */
    public static void applySpeedBoostConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] speedKeys = {
            "+CVars=r.MovementSpeedMultiplier=3.00",
            "+CVars=r.SprintSpeedMultiplier=3.00",
            "+CVars=r.AttackSpeedMultiplier=3.00",
            "+CVars=r.BulletVelocityScale=5.00",
            "+CVars=r.ZeroInputLag=1",
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
        Log.i(TAG, "Farlight 84 3.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects Bullet Tracking, Magic Bullet, Hitbox Expansion, and Bullet Magnetism for Farlight 84.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "+CVars=r.BulletTracking=1",
            "+CVars=r.MagicBullet=1",
            "+CVars=r.HitboxExpansion=1.50",
            "+CVars=r.BulletMagnetism=1.50",
            "+CVars=r.BulletVelocityScale=2.00",
            "+CVars=r.TargetLockTracking=1",
            "+CVars=r.FirstBulletAccuracy=1",
            "TrackingBullet=1",
            "BulletTracking=1",
            "MagicBullet=1",
            "HitboxExpansion=1.50",
            "BulletMagnetism=1.50",
            "BulletVelocityMultiplier=2.00",
            "CrosshairMagnetism=1.50"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "Farlight 84 Bullet Tracking & Hitbox Expansion applied for " + packageName);
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
