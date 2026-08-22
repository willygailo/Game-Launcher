package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * PubgConfigPatcher manages internal config files for PUBG Mobile, BGMI, and regional variants.
 *
 * Two patching modes:
 *  - patch()            → standard patch: in-memory key/CVar upserting
 *  - patchCompetitive() → competitive force-write: overwrites all paths atomically via ConfigFileHelper
 */
public class PubgConfigPatcher {

    private static final String TAG = "PubgConfigPatcher";

    // ─── Standard Patch ───────────────────────────────────────────────────────

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        patchActiveSavBinary(packageName, forcedFps);
        Log.i(TAG, "PUBGM patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL PUBGM/BGMI config paths unconditionally.
     * Includes full UE4 CVar injection for 120 / 144 / 165 / 185 FPS, frame rate limits, and content scale.
     *
     * @return true if at least one path was written
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final FpsUnlockTier tier = FpsUnlockTier.fromFps(forcedFps);
        final int pubgFpsLevel = tier.level;

        String content = "[UserCustom DeviceProfile]\n" +
                "+CVars=r.PUBGDeviceFPS=" + pubgFpsLevel + "\n" +
                "+CVars=r.PUBGMaxFPS=" + forcedFps + "\n" +
                "+CVars=r.PUBGFrameRateLimit=" + forcedFps + "\n" +
                "+CVars=r.MobileFPSLimit=" + forcedFps + "\n" +
                "+CVars=r.FrameRateLimit=" + forcedFps + "\n" +
                "+CVars=r.PUBGHDRMode=1\n" +
                "+CVars=r.MobileHDR=1\n" +
                "+CVars=r.PUBGQualityLevel=4\n" +
                "+CVars=r.PUBGSDKQualityLevel=4\n" +
                "+CVars=r.Tonemapper.Quality=4\n" +
                "+CVars=r.HDR.Display.OutputDevice=1\n" +
                "+CVars=r.MobileContentScaleFactor=1.0\n" +
                "+CVars=r.MobileTonemapperFilm=1\n" +
                "+CVars=r.PUBGTPPViewRange=100.00\n" +
                "+CVars=r.PUBGFPPViewRange=150.00\n" +
                "+CVars=r.SprintSensitivity=150\n" +
                "+CVars=r.Vsync=0\n" +
                "+CVars=r.Unlock120Hz=1\n" +
                "+CVars=r.Unlock144Hz=1\n" +
                "+CVars=r.Unlock165Hz=1\n" +
                "+CVars=r.Unlock185Hz=1\n" +
                "+CVars=r.SuppressLogs=1\n" +
                "+CVars=r.DisableDebugLog=1\n" +
                "+CVars=r.EnableCrashReporting=0\n" +
                "+CVars=r.Telemetry=0\n" +
                "+CVars=a.DisableAnalytics=1\n" +
                "+CVars=r.LogFilter=0\n" +
                "+CVars=r.TouchBoostHz=" + forcedFps + "\n" +
                "+CVars=r.MobileTouchBoostRate=" + forcedFps + "\n" +
                "+CVars=r.GyroSampleRate=1000\n" +
                "+CVars=r.GyroSensitivityRatio=2.5\n" +
                "+CVars=r.GyroZeroDelay=1\n" +
                "+CVars=r.GyroLatencyMode=0\n" +
                "+CVars=r.GyroSmoothFactor=1\n" +
                "+CVars=r.GyroStabilization=1\n" +
                "FrameRateLevel=" + pubgFpsLevel + "\n" +
                "FPS=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "UnlockFPS=1\n" +
                "Unlock120FPS=1\n" +
                "Unlock144FPS=1\n" +
                "Unlock165FPS=1\n" +
                "Unlock185FPS=1\n" +
                "Ultra144FPS=1\n" +
                "Ultra165FPS=1\n" +
                "Ultra185FPS=1\n" +
                "UltraExtreme=1\n" +
                "bUseUltraExtreme=True\n" +
                "GraphicsQuality=5\n" +
                "GraphicQuality=4\n" +
                "HDRMode=1\n" +
                "UltraHDMode=1\n" +
                "SuperResolution=1\n" +
                "bUseHDRMode=True\n" +
                "bUseHighQualityBloom=True\n" +
                "bUseAntiAliasing=True\n" +
                "bDisableAnalytics=True\n" +
                "bDisableBugReporting=True\n" +
                "SprintSensitivity=150\n" +
                "TPPFieldOfView=100\n" +
                "FPPFieldOfView=150\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (ConfigFileHelper.writeContentAtomic(path, content)) {
                written++;
            }
        }
        patchActiveSavBinary(packageName, forcedFps);
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "PUBGM competitive HDR " + forcedFps + "FPS force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Applies anti-log, log directory cleaning, and telemetry suppression for PUBGM/BGMI.
     */
    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    /**
     * Injects super-fast zero-delay touch CVar into PUBGM/BGMI config files.
     */
    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] touchCvars = {
            "+CVars=r.MobileTouchBoostRate=185",
            "+CVars=r.TouchSampleRate=1000",
            "+CVars=r.TouchResponseTime=0",
            "+CVars=r.ZeroTouchDelay=1",
            "+CVars=r.InputLatencyReduction=1",
            "+CVars=r.TouchDeadzone=0",
            "+CVars=r.TouchSlop=0"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, touchCvars, "[UserCustom DeviceProfile]");
        }
        Log.i(TAG, "PUBGM super-fast zero-delay touch applied for " + packageName);
    }

    /**
     * Injects Aim Assist 150%, FOV (TPP 100 / FPP 150), Sprint 150, Gyro 1000Hz Ultra Response (Super Smooth), and Aim Assist CVars into PUBGM/BGMI config files.
     */
    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] cvars = {
            "+CVars=r.AimAssist=1",
            "+CVars=r.AimAssist.Strength=2.0",
            "+CVars=r.AimAssist.Magnetism=1.5",
            "+CVars=r.AimAssist.SnapSpeed=2.0",
            "+CVars=r.AimAssistRadius=200",
            "+CVars=r.PUBGTPPViewRange=100.00",
            "+CVars=r.PUBGFPPViewRange=150.00",
            "+CVars=r.SprintSensitivity=150",
            "+CVars=r.GyroSampleRate=1000",
            "+CVars=r.GyroSensitivityRatio=2.5",
            "+CVars=r.GyroZeroDelay=1",
            "+CVars=r.GyroSmoothFactor=1",
            "+CVars=r.GyroStabilization=1",
            "+CVars=r.GyroLatencyMode=0",
            "AimAssist=1",
            "AimAssistLevel=5",
            "AimAssistStrength=150",
            "SprintSensitivity=150",
            "TPPFieldOfView=100",
            "FPPFieldOfView=150"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, cvars, "[UserCustom DeviceProfile]");
        }
        Log.i(TAG, "PUBGM Aim Assist 150%, FOV 150 & Gyro 1000Hz applied for " + packageName);
    }

    /**
     * Injects Zero Recoil & Weapon Stability CVars for ALL guns and ALL scopes into PUBGM/BGMI config files.
     */
    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilCvars = {
            "+CVars=r.WeaponRecoilScale=0.00",
            "+CVars=r.RecoilControl=1",
            "+CVars=r.VerticalRecoilMultiplier=0.00",
            "+CVars=r.HorizontalRecoilMultiplier=0.00",
            "+CVars=r.GunKickReduction=1",
            "+CVars=r.CameraShake=0",
            "+CVars=r.ScreenShake=0",
            "+CVars=r.WeaponSway=0",
            "+CVars=r.BulletSpread=0.00",
            "+CVars=r.CrosshairSpread=0.00",
            "+CVars=r.SpreadReduction=1",
            "+CVars=r.ScopeStability=2.00",
            "+CVars=r.FirstBulletAccuracy=1",
            "+CVars=r.AimPunchReduction=1",
            "+CVars=r.FlinchReduction=1",
            "+CVars=r.WeaponKick=0.00",
            "+CVars=r.ViewKick=0.00",
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
            "NoCameraShake=1"
        };
        for (String path : paths) {
            NativeConfigInjector.injectNoRecoil(path);
            ConfigFileHelper.patchKeys(path, recoilCvars, "[UserCustom DeviceProfile]");
        }
        Log.i(TAG, "PUBGM Zero Recoil & Weapon Stability applied for " + packageName);
    }

    /**
     * Injects Damage Multiplier & Critical Penetration CVars into PUBGM/BGMI config files.
     */
    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageCvars = {
            "+CVars=r.DamageMultiplier=5.00",
            "+CVars=r.BulletDamageScale=5.00",
            "+CVars=r.HeadshotMultiplier=5.00",
            "+CVars=r.WeaponDamageScale=5.00",
            "+CVars=r.CriticalHitRate=1.00",
            "+CVars=r.HitboxExpansion=2.50",
            "+CVars=r.BulletVelocityScale=5.00",
            "+CVars=r.PenetrationPower=5.00",
            "+CVars=r.BodyDamageMultiplier=3.50",
            "+CVars=r.LimbDamageMultiplier=3.00",
            "+CVars=r.ExplosiveDamageMultiplier=3.50",
            "+CVars=r.MovementSpeedMultiplier=3.00",
            "+CVars=r.SprintSpeedMultiplier=3.00",
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
            ConfigFileHelper.patchKeys(path, damageCvars, "[UserCustom DeviceProfile]");
        }
        Log.i(TAG, "PUBGM Damage Boost 500% & Bullet Penetration applied for " + packageName);
    }

    /**
     * Injects Armor Defense, Vest Durability, Helmet Protection, and Damage Reduction CVars into PUBGM/BGMI.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorCvars = {
            "+CVars=r.ArmorDamageReduction=0.85",
            "+CVars=r.VestDurabilityBoost=5.00",
            "+CVars=r.HelmetDamageReduction=0.90",
            "+CVars=r.IncomingDamageScale=0.15",
            "+CVars=r.ShieldEfficiency=5.00",
            "+CVars=r.HealthRegenBoost=5.00",
            "+CVars=r.DamageResistance=0.85",
            "+CVars=r.TenacityRatio=0.80",
            "+CVars=r.FallDamageReduction=1.00",
            "+CVars=r.ExplosionResistance=0.90",
            "+CVars=r.HeadshotDamageReduction=0.90",
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
            ConfigFileHelper.patchKeys(path, armorCvars, "[DefenseConfig]");
        }
        Log.i(TAG, "PUBGM Armor Defense 85% Reduction & 5.0x Vest applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for PUBGM/BGMI.
     */
    public static void applySpeedBoostConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] speedCvars = {
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
            ConfigFileHelper.patchKeys(path, speedCvars, "[SpeedEngine]");
        }
        Log.i(TAG, "PUBGM 3.0x Speed Boost & Sprint Agility applied for " + packageName);
    }

    /**
     * Injects Tracking Bullet, Bullet Magnetism, Magic Bullet, and Hitbox Expansion CVars into PUBGM/BGMI.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingCvars = {
            "+CVars=r.BulletTracking=1",
            "+CVars=r.MagicBullet=1",
            "+CVars=r.HitboxExpansion=1.50",
            "+CVars=r.BulletMagnetism=1.50",
            "+CVars=r.BulletVelocityScale=2.00",
            "+CVars=r.BulletCurveFactor=1.20",
            "+CVars=r.TargetLockTracking=1",
            "+CVars=r.FirstBulletAccuracy=1",
            "TrackingBullet=1",
            "BulletTracking=1",
            "MagicBullet=1",
            "HitboxExpansion=1.50",
            "BulletMagnetism=1.50",
            "BulletVelocityMultiplier=2.00"
        };
        for (String path : paths) {
            ConfigFileHelper.patchKeys(path, trackingCvars, "[TrackingConfig]");
        }
        Log.i(TAG, "PUBGM Tracking Bullet & Magic Bullet applied for " + packageName);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    /**
     * Patches Active.sav binary savegame file directly using byte manipulation.
     * Enforces FPSLevel, BattleFPS, and LobbyFPS to target levels (10=185fps, 9=165fps, 8=144fps, 7=120fps).
     */
    public static void patchActiveSavBinary(String pkg, int targetFps) {
        if (pkg == null) return;
        final int fpsLevel = FpsUnlockTier.fromFps(targetFps).level;
        String[] savPaths = {
            "/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav",
            "/data/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav"
        };
        for (String sav : savPaths) {
            try {
                if (!ShizukuFileManager.fileExists(sav)) continue;
                byte[] data = ShizukuFileManager.readFileBytes(sav);
                if (data != null && data.length > 0) {
                    boolean modified = patchBinarySavField(data, "FPSLevel", fpsLevel);
                    modified |= patchBinarySavField(data, "BattleFPS", fpsLevel);
                    modified |= patchBinarySavField(data, "LobbyFPS", fpsLevel);
                    if (modified) {
                        ShizukuFileManager.uploadBytes(sav, data, "666");
                    }
                }
            } catch (Throwable t) {
                Log.w(TAG, "patchActiveSavBinary error for " + sav + ": " + t.getMessage());
            }
        }
        Log.i(TAG, "PUBGM Active.sav binary enforced level " + fpsLevel + " (" + targetFps + " FPS) for " + pkg);
    }

    private static boolean patchBinarySavField(byte[] data, String fieldName, int value) {
        if (data == null || fieldName == null) return false;
        byte[] pattern = fieldName.getBytes(StandardCharsets.UTF_8);
        int idx = indexOfBytes(data, pattern);
        if (idx != -1) {
            // In Active.sav, the value byte usually appears 9-10 bytes after the field name ASCII bytes
            for (int offset = idx + pattern.length; offset < Math.min(data.length, idx + pattern.length + 16); offset++) {
                if (data[offset] >= 1 && data[offset] <= 10) {
                    data[offset] = (byte) value;
                    return true;
                }
            }
        }
        return false;
    }

    private static int indexOfBytes(byte[] source, byte[] target) {
        if (source == null || target == null || source.length < target.length) return -1;
        for (int i = 0; i <= source.length - target.length; i++) {
            boolean match = true;
            for (int j = 0; j < target.length; j++) {
                if (source[i + j] != target[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }

    private static boolean applyPatch(String path, int targetFps) {
        final FpsUnlockTier tier = FpsUnlockTier.fromFps(targetFps);
        final int pubgFpsLevel = tier.level;
        String[] cvars = {
            "+CVars=r.PUBGDeviceFPS=" + pubgFpsLevel,
            "+CVars=r.PUBGMaxFPS=" + targetFps,
            "+CVars=r.PUBGFrameRateLimit=" + targetFps,
            "+CVars=r.MobileFPSLimit=" + targetFps,
            "+CVars=r.FrameRateLimit=" + targetFps,
            "+CVars=r.PUBGHDRMode=1",
            "+CVars=r.MobileHDR=1",
            "+CVars=r.PUBGQualityLevel=4",
            "+CVars=r.PUBGSDKQualityLevel=4",
            "+CVars=r.Unlock120Hz=1",
            "+CVars=r.Unlock144Hz=1",
            "+CVars=r.Unlock165Hz=1",
            "+CVars=r.Unlock185Hz=1",
            "+CVars=r.Vsync=0",
            "FrameRateLevel=" + pubgFpsLevel,
            "FPS=" + targetFps,
            "TargetFPS=" + targetFps,
            "MaxFPS=" + targetFps,
            "bUseHDRMode=True",
            "bUseAntiAliasing=True"
        };
        return ConfigFileHelper.patchKeys(path, cvars, "[UserCustom DeviceProfile]");
    }
}
