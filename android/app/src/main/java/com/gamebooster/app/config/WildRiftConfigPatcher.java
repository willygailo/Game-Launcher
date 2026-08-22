package com.gamebooster.app.config;

import android.util.Log;
import java.util.List;

/**
 * WildRiftConfigPatcher manages legal configuration files for League of Legends: Wild Rift (all regions).
 * Unlocks 90 FPS / 120 FPS / 144 FPS / 165 FPS / 185 FPS and 1000Hz touch input response.
 */
public class WildRiftConfigPatcher {

    private static final String TAG = "WildRiftConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyStandardPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Wild Rift patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String jsonContent = "{\n" +
                "  \"graphics\": {\n" +
                "    \"target_fps\": " + forcedFps + ",\n" +
                "    \"max_fps\": " + forcedFps + ",\n" +
                "    \"fps_level\": " + fpsLevel + ",\n" +
                "    \"fpsUnlock\": true,\n" +
                "    \"fps_unlock\": true,\n" +
                "    \"unlock_120\": true,\n" +
                "    \"unlock_144\": true,\n" +
                "    \"unlock_165\": true,\n" +
                "    \"unlock_185\": true,\n" +
                "    \"resolution\": 4,\n" +
                "    \"quality\": 4,\n" +
                "    \"character_quality\": 4,\n" +
                "    \"effects_quality\": 4,\n" +
                "    \"shadow_quality\": 4,\n" +
                "    \"ultra_extreme\": true,\n" +
                "    \"resolution_scale\": 1.2,\n" +
                "    \"post_processing\": true,\n" +
                "    \"vsync\": false\n" +
                "  },\n" +
                "  \"combat\": {\n" +
                "    \"physical_damage_boost\": 1.90,\n" +
                "    \"magic_damage_boost\": 1.90,\n" +
                "    \"true_damage_boost\": 1.90,\n" +
                "    \"critical_damage_rate\": 95,\n" +
                "    \"drone_view\": true,\n" +
                "    \"camera_fov\": 150,\n" +
                "    \"camera_distance\": 150\n" +
                "  },\n" +
                "  \"input\": {\n" +
                "    \"touch_polling_hz\": 1000,\n" +
                "    \"zero_latency_mode\": true\n" +
                "  }\n" +
                "}\n";

        String iniContent = "[WildRiftGraphics]\n" +
                "FPSLevel=" + fpsLevel + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "FPS=" + forcedFps + "\n" +
                "HighFPSMode=1\n" +
                "UnlockFPS=1\n" +
                "Unlock120=1\n" +
                "Unlock144=1\n" +
                "Unlock165=1\n" +
                "Unlock185=1\n" +
                "GraphicQuality=4\n" +
                "UltraExtreme=1\n" +
                "ResolutionScale=1.2\n" +
                "DroneView=1\n" +
                "CameraFOV=150\n" +
                "CameraDistance=150\n" +
                "PhysicalDamageBoost=1.90\n" +
                "MagicDamageBoost=1.90\n" +
                "TrueDamageBoost=1.90\n" +
                "DamageMultiplier=2.50\n" +
                "CriticalDamageRate=99\n" +
                "TouchPollingRate=1000\n" +
                "TouchSlop=1\n" +
                "TouchZeroDelay=1\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            boolean ok;
            if (path.endsWith(".json")) {
                ok = ConfigFileHelper.writeContentAtomic(path, jsonContent);
            } else {
                ok = ConfigFileHelper.writeContentAtomic(path, iniContent);
            }
            if (ok) written++;
        }
        Log.i(TAG, "Wild Rift competitive UltraExtreme " + forcedFps + "FPS force-write: " + written + " paths");
        return written > 0;
    }

    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageKeys = {
            "DamageMultiplier=5.00",
            "PhysicalDamageBoost=5.00",
            "MagicDamageBoost=5.00",
            "TrueDamageBoost=5.00",
            "DamageBoost=5.00",
            "DamageBoostRatio=5.00",
            "CritRate=100",
            "CritDamage=5.00",
            "CriticalDamageRate=100",
            "CriticalHitRate=100",
            "CriticalDamageMultiplier=5.00",
            "PenetrationBoost=100",
            "ArmorPenetration=100",
            "HeadshotMultiplier=5.00",
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
            "ExplosiveDamageMultiplier=3.50"
        };
        for (String path : paths) {
            NativeConfigInjector.injectHighDamage(path);
            ConfigFileHelper.patchKeys(path, damageKeys, "[DamageScript]");
        }
        Log.i(TAG, "Wild Rift 5.0x damage boost & critical multipliers applied for " + packageName);
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] touchKeys = {
            "TouchRate=1000",
            "TouchResponse=1",
            "TouchSlopReduction=1",
            "TouchZeroDelay=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, touchKeys, "[TouchEngine]");
        }
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "AimAssist=1",
            "SmartTargeting=1",
            "TargetLock=1",
            "SkillTargetAssist=1",
            "AutoSkillAim=1",
            "TouchSensitivity=150"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, aimKeys, "[AimAssist]");
        }
        Log.i(TAG, "WildRift Smart Target Assist applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "InputSmoothing=1",
            "SkillResponseZeroDelay=1",
            "TouchStabilization=1",
            "ZeroInputLag=1",
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
        Log.i(TAG, "WildRift Input Smoothing & Stabilization applied for " + packageName);
    }

    /**
     * Injects Armor Defense, Magic Resistance, Damage Reduction, and Shield Multiplier into Wild Rift.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "PhysicalArmor=5.00",
            "MagicResistance=5.00",
            "DamageReductionRatio=0.85",
            "DamageReduction=0.85",
            "IncomingDamageReduction=0.85",
            "ShieldMultiplier=5.00",
            "ShieldStrength=5.00",
            "ShieldEfficiency=5.00",
            "ShieldCapacity=5.00",
            "MaxHPMultiplier=3.00",
            "HPBoostRatio=3.00",
            "ArmorBoost=500",
            "PhysicalDefenseBoost=5.00",
            "MagicDefenseBoost=5.00",
            "DamageAbsorbRatio=3.00",
            "TenacityRatio=0.80",
            "ResilienceLevel=5",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=5.00",
            "FallDamageReduction=1.00",
            "ExplosionResistance=0.90"
        };
        for (String path : paths) {
            NativeConfigInjector.injectArmorDef(path);
            ConfigFileHelper.patchKeys(path, armorKeys, "[DefenseConfig]");
        }
        Log.i(TAG, "WildRift Armor Defense 85% Reduction & 5.0x Shield applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Wild Rift.
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
        Log.i(TAG, "WildRift 3.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects Skill Auto-Tracking, Target Lock, Smite Execution, and Skill Magnetism for Wild Rift.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "SkillTracking=1",
            "AutoTargetLock=1",
            "TargetLockTracking=1",
            "AutoSmiteExecution=1",
            "SkillMagnetism=1.50",
            "PredictPath=1",
            "HitboxExpansion=1.50",
            "TrackingBullet=1",
            "BulletTracking=1",
            "MagicBullet=1"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, trackingKeys, "[TrackingConfig]");
        }
        Log.i(TAG, "WildRift Skill Auto-Tracking & Smite Execution applied for " + packageName);
    }

    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    private static boolean applyStandardPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;
        String[] keys = {
            "target_fps=" + forcedFps,
            "max_fps=" + forcedFps,
            "fps_level=" + fpsLevel,
            "fpsUnlock=1",
            "unlock_120=1",
            "unlock_144=1",
            "unlock_165=1",
            "unlock_185=1",
            "resolution=4",
            "quality=4",
            "ultra_extreme=1",
            "TargetFPS=" + forcedFps,
            "MaxFPS=" + forcedFps,
            "FPSLevel=" + fpsLevel
        };
        return ConfigFileHelper.patchKeys(path, keys, "[WildRiftGraphics]");
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }
}
