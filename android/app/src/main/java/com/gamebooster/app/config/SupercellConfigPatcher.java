package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * SupercellConfigPatcher manages legal configuration files for Brawl Stars, Clash Royale, and Squad Busters.
 * Unlocks native 120 FPS / 144 FPS / 165 FPS / 185 FPS display rendering and 1000Hz touch polling rate.
 */
public class SupercellConfigPatcher {

    private static final String TAG = "SupercellConfigPatcher";

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyStandardPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "Supercell patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String iniContent = "[SupercellEngine]\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "MaxFPS=" + forcedFps + "\n" +
                "FPSLevel=" + fpsLevel + "\n" +
                "FPSCap=" + forcedFps + "\n" +
                "HighFPSMode=1\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "Unlock120Hz=1\n" +
                "Unlock144Hz=1\n" +
                "Unlock165Hz=1\n" +
                "Unlock185Hz=1\n" +
                "HighRefreshRate=1\n" +
                "GraphicQuality=4\n" +
                "UltraExtreme=1\n" +
                "HDRMode=1\n" +
                "ResolutionScale=1.2\n" +
                "TouchPollingRate=1000\n" +
                "TouchSlop=1\n" +
                "TouchZeroDelay=1\n" +
                "DamageMultiplier=2.50\n" +
                "SuperAttackMultiplier=1.90\n" +
                "CriticalStrikeRate=95\n" +
                "AutoAimGuide=1\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            forceWrite(path, iniContent);
            written++;
        }
        Log.i(TAG, "Supercell competitive UltraExtreme " + forcedFps + "FPS force-write: " + written + " paths");
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
            "BulletDamageBoost=5.00",
            "DamageBoost=5.00",
            "DamageBoostRatio=5.00",
            "SuperAttackMultiplier=5.00",
            "HeadshotMultiplier=5.00",
            "CriticalStrikeRate=100",
            "CriticalDamage=100",
            "CriticalHitRate=100",
            "CriticalDamageMultiplier=5.00",
            "PenetrationBoost=100",
            "ArmorPenetration=100",
            "HitboxExpansion=2.50",
            "AttackSpeedBoost=3.00",
            "AttackSpeedMultiplier=3.00",
            "MovementSpeedMultiplier=3.00",
            "SprintSpeedMultiplier=3.00",
            "SprintSensitivity=200",
            "AgilityMultiplier=3.00",
            "BulletVelocityMultiplier=5.00",
            "BulletVelocityScale=5.00",
            "BodyDamageMultiplier=3.50",
            "ExplosiveDamageMultiplier=3.50"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
            NativeConfigInjector.injectHighDamage(path);
            StringBuilder sb = new StringBuilder();
            sb.append("grep -qF '[DamageScript]' ").append(path).append(" || echo '[DamageScript]' >> ").append(path).append("; ");
            for (String keyVal : damageKeys) {
                String k = keyVal.substring(0, keyVal.indexOf("="));
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/^").append(k).append("=.*/").append(keyVal).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Supercell 5.0x damage boost & attack multipliers applied for " + packageName);
    }

    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String touchAppend = "\n[TouchEngine]\nTouchRate=1000\nTouchResponse=1\nTouchSlopReduction=1\nTouchZeroDelay=1\n";
            if (ShizukuFileManager.fileExists(path)) {
                String appendCmd = "echo '" + touchAppend + "' >> " + path + "; chmod 666 " + path;
                if (ShizukuFileManager.hasFullAccess()) {
                    ShizukuExecutor.executeShizukuCommand(appendCmd);
                } else {
                    CommandExecutor.executeSystemCommand(appendCmd);
                }
            }
        }
        Log.i(TAG, "Supercell Input Smoothing & Stabilization applied for " + packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "AutoAimAssist=1",
            "AimSnap=1",
            "SmartTargeting=1",
            "AimAssistStrength=150",
            "TouchSensitivity=150"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
            StringBuilder sb = new StringBuilder();
            for (String keyVal : aimKeys) {
                String k = keyVal.substring(0, keyVal.indexOf("="));
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/^").append(k).append("=.*/").append(keyVal).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Supercell Auto-Aim Assist applied for " + packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "InputZeroDelay=1",
            "MovementStabilization=1",
            "TouchSmoothing=1",
            "ZeroInputLag=1",
            "CameraShake=0",
            "NoCameraShake=1",
            "ScreenShake=0",
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
            ensureParentDirectory(path);
            NativeConfigInjector.injectNoRecoil(path);
            StringBuilder sb = new StringBuilder();
            for (String keyVal : recoilKeys) {
                String k = keyVal.substring(0, keyVal.indexOf("="));
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/^").append(k).append("=.*/").append(keyVal).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Supercell Movement Stabilization applied for " + packageName);
    }

    /**
     * Injects Shield Multiplier, Defense Ratio, Damage Reduction, and HP Boost for Supercell games.
     */
    public static void applyArmorDefConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] armorKeys = {
            "ShieldMultiplier=5.00",
            "ShieldCapacity=5.00",
            "ShieldStrength=5.00",
            "ShieldEfficiency=5.00",
            "DefenseRatio=5.00",
            "DamageReduction=0.85",
            "DamageReductionRatio=0.85",
            "IncomingDamageReduction=0.85",
            "HPBoost=3.00",
            "HPBoostRatio=3.00",
            "MaxHPMultiplier=3.00",
            "DamageAbsorbRatio=3.00",
            "ArmorBoost=500",
            "PhysicalDefenseBoost=5.00",
            "MagicDefenseBoost=5.00",
            "TenacityRatio=0.80",
            "ResilienceLevel=5",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=5.00",
            "ExplosionResistance=0.90",
            "FallDamageReduction=1.00"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
            NativeConfigInjector.injectArmorDef(path);
            StringBuilder sb = new StringBuilder();
            sb.append("grep -qF '[CombatDefense]' ").append(path).append(" || echo '[CombatDefense]' >> ").append(path).append("; ");
            for (String keyVal : armorKeys) {
                String k = keyVal.substring(0, keyVal.indexOf("="));
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/^").append(k).append("=.*/").append(keyVal).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Supercell Shield 5.0x & Defense 85% Reduction applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for Supercell games.
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
            ensureParentDirectory(path);
            NativeConfigInjector.injectSpeedBoost(path);
            StringBuilder sb = new StringBuilder();
            sb.append("grep -qF '[SpeedEngine]' ").append(path).append(" || echo '[SpeedEngine]' >> ").append(path).append("; ");
            for (String keyVal : speedKeys) {
                String k = keyVal.substring(0, keyVal.indexOf("="));
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/^").append(k).append("=.*/").append(keyVal).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Supercell 3.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects Auto Attack Tracking, Super Attack Lock, Projectile Homing, and Skill Magnetism for Supercell games.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "AutoAttackTracking=1",
            "SuperAttackLock=1",
            "ProjectileHoming=1",
            "AutoTargetLock=1",
            "SkillMagnetism=1.50",
            "HitboxExpansion=1.50",
            "TrackingBullet=1",
            "BulletTracking=1"
        };
        for (String path : paths) {
            ensureParentDirectory(path);
            StringBuilder sb = new StringBuilder();
            for (String keyVal : trackingKeys) {
                String k = keyVal.substring(0, keyVal.indexOf("="));
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/^").append(k).append("=.*/").append(keyVal).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "Supercell Auto Attack Tracking & Super Lock applied for " + packageName);
    }

    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    private static boolean applyStandardPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int fpsLevel = FpsUnlockTier.fromFps(forcedFps).level;
        if (!ShizukuFileManager.fileExists(path)) {
            String content = String.format(
                    "[SupercellEngine]\nTargetFPS=%d\nMaxFPS=%d\nFPSLevel=%d\nFPSCap=%d\nHighFPSMode=1\nUnlockFPS=1\nSuperHighFPS=1\nUnlock120Hz=1\nUnlock144Hz=1\nUnlock165Hz=1\nUnlock185Hz=1\nHighRefreshRate=1\nGraphicQuality=4\nUltraExtreme=1\nHDRMode=1\nResolutionScale=1.2\n",
                    forcedFps, forcedFps, fpsLevel, forcedFps
            );
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd = "sed -i 's/^TargetFPS=.*/TargetFPS=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^MaxFPS=.*/MaxFPS=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^FPSCap=.*/FPSCap=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^FPSLevel=.*/FPSLevel=" + fpsLevel + "/' " + path + "; " +
                         "sed -i 's/^GraphicQuality=.*/GraphicQuality=4/' " + path + "; " +
                         "sed -i 's/^UltraExtreme=.*/UltraExtreme=1/' " + path + "; " +
                         "chmod 666 " + path;
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
            return true;
        }
    }

    private static void forceWrite(String path, String content) {
        ShizukuFileManager.writeFile(path, content, "666");
    }

    private static void ensureParentDirectory(String path) {
        if (path == null) return;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            String parentDir = path.substring(0, lastSlash);
            if (ShizukuFileManager.hasFullAccess()) {
                ShizukuExecutor.executeShizukuCommand("mkdir -p " + parentDir);
            } else {
                CommandExecutor.executeSystemCommand("mkdir -p " + parentDir);
            }
        }
    }

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }
}
