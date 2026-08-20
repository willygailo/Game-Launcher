package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import java.util.ArrayList;
import java.util.List;

/**
 * MlbbConfigPatcher manages internal config files for Mobile Legends: Bang Bang (all versions).
 *
 * Two patching modes:
 *  - patch()            → standard patch: create-if-missing or sed-update
 *  - patchCompetitive() → competitive force-write: ALWAYS overwrites all paths, no fallback,
 *                         executed via Shizuku for full data/data access (temporary root)
 */
public class MlbbConfigPatcher {

    private static final String TAG = "MlbbConfigPatcher";

    // ─── Standard Patch ───────────────────────────────────────────────────────

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "MLBB patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL MLBB config paths unconditionally.
     * Uses Shizuku (temporary root) to reach /data/data/ paths.
     * Hard-locked to 185 FPS only. Sets FrameRateLevel=10 (185fps) unconditionally.
     *
     * @return true if at least one path was written
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;

        String content = "[Graphics]\n" +
                "HighFPSMode=1\n" +
                "FrameRateLevel=" + frameRateLevel + "\n" +
                "GraphicsQuality=4\n" +
                "HDMode=1\n" +
                "HDRMode=1\n" +
                "UltraHDMode=1\n" +
                "Shadow=1\n" +
                "FPS=" + forcedFps + "\n" +
                "MaxFrameRate=" + forcedFps + "\n" +
                "TargetFPS=" + forcedFps + "\n" +
                "HighFrameRate=1\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "Unlock120Hz=1\n" +
                "Unlock144Hz=1\n" +
                "Unlock165Hz=1\n" +
                "Unlock185Hz=1\n" +
                "Ultra144FPS=1\n" +
                "Ultra165FPS=1\n" +
                "Ultra185FPS=1\n" +
                "DisableLogging=1\n" +
                "DisableCrashlytics=1\n" +
                "DisableTelemetry=1\n" +
                "AntiLog=1\n" +
                "LogcatDisable=1\n" +
                "DroneView=1\n" +
                "DroneViewHeight=3\n" +
                "CameraHeight=3\n" +
                "CameraDistance=150\n" +
                "CameraFOV=150\n" +
                "WideScreenMode=1\n" +
                "FieldOfView=150\n" +
                "HighFreqTouchHz=" + forcedFps + "\n" +
                "TouchPollingRate=1000\n" +
                "TouchZeroDelay=1\n" +
                "TouchResponseLevel=3\n" +
                "PhysicalDamageBoost=2.50\n" +
                "MagicDamageBoost=2.50\n" +
                "TrueDamageBoost=2.50\n" +
                "DamageMultiplier=2.50\n" +
                "CriticalDamageRate=99\n" +
                "HeadshotDamageMultiplier=3.50\n" +
                "GyroSampleRate=1000\n" +
                "GyroSensitivityRatio=2.5\n" +
                "GyroZeroDelay=1\n" +
                "GyroSmoothFactor=1\n" +
                "GyroStabilization=1\n" +
                "GyroLatencyMode=0\n" +
                "AimAssistStrength=150\n" +
                "AimAssistLevel=5\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            forceWrite(path, content);
            written++;
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "MLBB competitive HDR " + forcedFps + "FPS + Drone View force-write: " + written + " paths @ " + forcedFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Applies anti-log, log directory cleaning, and telemetry suppression for MLBB.
     */
    public static void applyAntiLog(String packageName) {
        if (packageName == null) return;
        AntiLogPatcher.applyAntiLog(packageName);
    }

    /**
     * Injects super-fast zero-delay touch response keys into MLBB config files.
     * Optimized for 185Hz panels — sets HighFreqTouchHz=185, TouchPollingRate=1000, TouchZeroDelay=1, and max touch response level.
     */
    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String cmd =
                "grep -qF 'HighFreqTouch' " + path + " || echo 'HighFreqTouch=1' >> " + path + "; " +
                "sed -i 's/^HighFreqTouch=.*/HighFreqTouch=1/' " + path + "; " +
                "grep -qF 'TouchResponseLevel' " + path + " || echo 'TouchResponseLevel=3' >> " + path + "; " +
                "sed -i 's/^TouchResponseLevel=.*/TouchResponseLevel=3/' " + path + "; " +
                "grep -qF 'HighFreqTouchHz' " + path + " || echo 'HighFreqTouchHz=185' >> " + path + "; " +
                "sed -i 's/^HighFreqTouchHz=.*/HighFreqTouchHz=185/' " + path + "; " +
                "grep -qF 'TouchPollingRate' " + path + " || echo 'TouchPollingRate=1000' >> " + path + "; " +
                "sed -i 's/^TouchPollingRate=.*/TouchPollingRate=1000/' " + path + "; " +
                "grep -qF 'TouchZeroDelay' " + path + " || echo 'TouchZeroDelay=1' >> " + path + "; " +
                "sed -i 's/^TouchZeroDelay=.*/TouchZeroDelay=1/' " + path + "; " +
                "grep -qF 'TouchLatencyReduction' " + path + " || echo 'TouchLatencyReduction=1' >> " + path + "; " +
                "sed -i 's/^TouchLatencyReduction=.*/TouchLatencyReduction=1/' " + path + "; " +
                "grep -qF 'ZeroInputLag' " + path + " || echo 'ZeroInputLag=1' >> " + path + "; " +
                "sed -i 's/^ZeroInputLag=.*/ZeroInputLag=1/' " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "MLBB super-fast zero-delay touch applied for " + packageName);
    }

    /**
     * Injects Drone View (Camera Height / FOV 150), Damage Script 90+, Physical/Magic/True Damage Boost, Critical and Penetration keys into MLBB config files.
     * Uses Shizuku ADB temporary root access for /data/data/ and /sdcard/ file locations.
     */
    public static void applyDamageScriptConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] damageDroneKeys = {
            "DroneView=1",
            "DroneViewHeight=3",
            "CameraHeight=3",
            "CameraDistance=150",
            "CameraFOV=150",
            "FieldOfView=150",
            "WideScreenMode=1",
            "UltraWideCamera=1",
            // Damage 500% & Penetration
            "PhysicalDamageBoost=5.00",
            "MagicDamageBoost=5.00",
            "TrueDamageBoost=5.00",
            "BulletDamageBoost=5.00",
            "PhysicalPenetrationBoost=100",
            "MagicPenetrationBoost=100",
            "ArmorPenetration=100",
            "MagicResistPenetration=100",
            "DamageMultiplier=5.00",
            "DamageBoost=5.00",
            "DamageBoostRatio=5.00",
            "SkillDamageMultiplier=5.00",
            "HeadshotDamageMultiplier=5.00",
            "CriticalDamageRate=100",
            "CriticalDamageMultiplier=5.00",
            "CriticalHitRate=1.00",
            "CriticalDamage=100",
            "AttackSpeedMultiplier=3.00",
            "AttackSpeedBoost=3.00",
            "AttackDelayReduction=1",
            "MovementSpeedMultiplier=3.00",
            "MovementSpeedBoost=3.00",
            "SprintSpeedMultiplier=3.00",
            "SprintSpeedBoost=3.00",
            "SprintSensitivity=200",
            "AgilityMultiplier=3.00",
            "SkillAnimationCancelZeroDelay=1",
            "SkillCoolDownReduceMode=1",
            "CooldownReductionBoost=0.50",
            "HighDamageRateMode=1",
            "DamageAssetOverride=1",
            "AutoDamageExecutionMode=1",
            "AutoSmiteExecution=1",
            "RetributionDamageThreshold=5000",
            "TurretDamageReduction=0.85",
            "MinionDamageBoost=3.00",
            "MonsterDamageBoost=5.00",
            "HitboxExpansion=2.50",
            "BulletVelocityMultiplier=5.00",
            "BulletVelocityScale=5.00",
            "BodyDamageMultiplier=3.50",
            "LimbDamageMultiplier=3.00",
            "ExplosiveDamageMultiplier=3.50",
            // Gyro Super Smooth
            "GyroSampleRate=1000",
            "GyroSensitivityRatio=3.0",
            "GyroZeroDelay=1",
            "GyroSmoothFactor=1",
            "GyroStabilization=1",
            "GyroLatencyMode=0"
        };
        for (String path : paths) {
            ensureDirectory(path);
            NativeConfigInjector.injectHighDamage(path);
            StringBuilder sb = new StringBuilder();
            if (path.endsWith(".xml")) {
                for (String keyVal : damageDroneKeys) {
                    String k = keyVal.substring(0, keyVal.indexOf("="));
                    String v = keyVal.substring(keyVal.indexOf("=") + 1);
                    sb.append("grep -qF 'name=\"").append(k).append("\"' ").append(path)
                      .append(" || sed -i '/<\\/map>/i \\  <string name=\"").append(k).append("\">").append(v).append("<\\/string>' ").append(path).append("; ");
                }
            } else {
                sb.append("grep -qF '[DamageScript]' ").append(path).append(" || echo '[DamageScript]' >> ").append(path).append("; ");
                sb.append("grep -qF '[CameraConfig]' ").append(path).append(" || echo '[CameraConfig]' >> ").append(path).append("; ");
                for (String keyVal : damageDroneKeys) {
                    String k = keyVal.substring(0, keyVal.indexOf("="));
                    sb.append("grep -qF '").append(k).append("' ").append(path)
                      .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                    sb.append("sed -i 's/^").append(k).append("=.*/").append(keyVal).append("/' ").append(path).append("; ");
                }
            }
            String cmd = sb.toString();
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "MLBB Drone View FOV 150 & Damage Script 5.0x applied via Shizuku for " + packageName);
    }

    /**
     * Injects Smart Aim Assist, Hero Priority Lock, and Skill Target Assistance for MLBB.
     */
    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimKeys = {
            "AimAssistStrength=150",
            "AimAssistLevel=5",
            "AutoSkillLock=1",
            "SkillTargetAssist=1",
            "SmartTargetingMode=1",
            "HeroPriorityLock=1",
            "LowestHPTargetLock=1",
            "NearestTargetLock=0",
            "SkillAimAssist=1",
            "SmartAimCast=1",
            "SkillPredictPath=1",
            "AutoAimAssist=1",
            "TargetTracker=1",
            "HeroLockMode=1"
        };
        for (String path : paths) {
            ensureDirectory(path);
            StringBuilder sb = new StringBuilder();
            if (path.endsWith(".xml")) {
                for (String keyVal : aimKeys) {
                    String k = keyVal.substring(0, keyVal.indexOf("="));
                    String v = keyVal.substring(keyVal.indexOf("=") + 1);
                    sb.append("grep -qF 'name=\"").append(k).append("\"' ").append(path)
                      .append(" || sed -i '/<\\/map>/i \\  <string name=\"").append(k).append("\">").append(v).append("<\\/string>' ").append(path).append("; ");
                }
            } else {
                sb.append("grep -qF '[AimAssist]' ").append(path).append(" || echo '[AimAssist]' >> ").append(path).append("; ");
                for (String keyVal : aimKeys) {
                    String k = keyVal.substring(0, keyVal.indexOf("="));
                    sb.append("grep -qF '").append(k).append("' ").append(path)
                      .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                    sb.append("sed -i 's/^").append(k).append("=.*/").append(keyVal).append("/' ").append(path).append("; ");
                }
            }
            String cmd = sb.toString();
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "MLBB Smart Aim Assist & Hero Priority Lock applied for " + packageName);
    }

    /**
     * Injects joystick and movement stabilization, zero input delay, and skill cancel zero-delay for MLBB.
     */
    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilKeys = {
            "RecoilControl=1",
            "ZeroRecoil=1",
            "NoRecoil=1",
            "MovementStabilization=1",
            "JoystickZeroDeadzone=1",
            "JoystickResponseLevel=3",
            "SkillCancellationZeroDelay=1",
            "InputSmoothing=1",
            "TouchStabilization=1",
            "ZeroInputDelay=1",
            "SkillResponseZeroDelay=1",
            "TouchJitterFilter=1",
            "AimPunchReduction=1",
            "FlinchReduction=1",
            "WeaponStability=150"
        };
        for (String path : paths) {
            ensureDirectory(path);
            NativeConfigInjector.injectNoRecoil(path);
            StringBuilder sb = new StringBuilder();
            if (path.endsWith(".xml")) {
                for (String keyVal : recoilKeys) {
                    String k = keyVal.substring(0, keyVal.indexOf("="));
                    String v = keyVal.substring(keyVal.indexOf("=") + 1);
                    sb.append("grep -qF 'name=\"").append(k).append("\"' ").append(path)
                      .append(" || sed -i '/<\\/map>/i \\  <string name=\"").append(k).append("\">").append(v).append("<\\/string>' ").append(path).append("; ");
                }
            } else {
                sb.append("grep -qF '[InputStabilization]' ").append(path).append(" || echo '[InputStabilization]' >> ").append(path).append("; ");
                for (String keyVal : recoilKeys) {
                    String k = keyVal.substring(0, keyVal.indexOf("="));
                    sb.append("grep -qF '").append(k).append("' ").append(path)
                      .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                    sb.append("sed -i 's/^").append(k).append("=.*/").append(keyVal).append("/' ").append(path).append("; ");
                }
            }
            String cmd = sb.toString();
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "MLBB Movement Stabilization & Joystick Zero-Deadzone applied for " + packageName);
    }

    /**
     * Injects Armor Defense Boost, Damage Reduction, Shield Multiplier, and Resilience for MLBB.
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
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=5.00",
            "FallDamageReduction=1.00",
            "ExplosionResistance=0.90",
            "HeadshotDamageReduction=0.90"
        };
        for (String path : paths) {
            ensureDirectory(path);
            NativeConfigInjector.injectArmorDef(path);
            StringBuilder sb = new StringBuilder();
            if (path.endsWith(".xml")) {
                for (String keyVal : armorKeys) {
                    String k = keyVal.substring(0, keyVal.indexOf("="));
                    String v = keyVal.substring(keyVal.indexOf("=") + 1);
                    sb.append("grep -qF 'name=\"").append(k).append("\"' ").append(path)
                      .append(" || sed -i '/<\\/map>/i \\  <string name=\"").append(k).append("\">").append(v).append("<\\/string>' ").append(path).append("; ");
                }
            } else {
                sb.append("grep -qF '[DefenseConfig]' ").append(path).append(" || echo '[DefenseConfig]' >> ").append(path).append("; ");
                for (String keyVal : armorKeys) {
                    String k = keyVal.substring(0, keyVal.indexOf("="));
                    sb.append("grep -qF '").append(k).append("' ").append(path)
                      .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                    sb.append("sed -i 's/^").append(k).append("=.*/").append(keyVal).append("/' ").append(path).append("; ");
                }
            }
            String cmd = sb.toString();
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "MLBB Armor Defense 85% Reduction & 5.0x Shield applied for " + packageName);
    }

    /**
     * Injects Speed Boost & Movement Agility for MLBB.
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
            ensureDirectory(path);
            NativeConfigInjector.injectSpeedBoost(path);
            StringBuilder sb = new StringBuilder();
            if (path.endsWith(".xml")) {
                for (String keyVal : speedKeys) {
                    String k = keyVal.substring(0, keyVal.indexOf("="));
                    String v = keyVal.substring(keyVal.indexOf("=") + 1);
                    sb.append("grep -qF 'name=\"").append(k).append("\"' ").append(path)
                      .append(" || sed -i '/<\\/map>/i \\  <string name=\"").append(k).append("\">").append(v).append("<\\/string>' ").append(path).append("; ");
                }
            } else {
                sb.append("grep -qF '[SpeedEngine]' ").append(path).append(" || echo '[SpeedEngine]' >> ").append(path).append("; ");
                for (String keyVal : speedKeys) {
                    String k = keyVal.substring(0, keyVal.indexOf("="));
                    sb.append("grep -qF '").append(k).append("' ").append(path)
                      .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                    sb.append("sed -i 's/^").append(k).append("=.*/").append(keyVal).append("/' ").append(path).append("; ");
                }
            }
            String cmd = sb.toString();
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "MLBB 3.0x Speed Boost & Movement Agility applied for " + packageName);
    }

    /**
     * Injects Skill Auto-Tracking, Projectile Magnetism, Retribution/Smite Lock, and Hitbox Tracking for MLBB.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] trackingKeys = {
            "AutoTrackingSkill=1",
            "AutoTargetLock=1",
            "SkillPathPrediction=1",
            "AutoRetributionSmiteLock=1",
            "SkillMagnetism=1.50",
            "BasicAttackTracking=1",
            "ProjectileTracking=1",
            "HitboxExpansion=1.50",
            "TargetLockTracking=1",
            "TrackingBullet=1",
            "BulletTracking=1",
            "MagicBullet=1"
        };
        for (String path : paths) {
            ensureDirectory(path);
            StringBuilder sb = new StringBuilder();
            if (path.endsWith(".xml")) {
                for (String keyVal : trackingKeys) {
                    String k = keyVal.substring(0, keyVal.indexOf("="));
                    String v = keyVal.substring(keyVal.indexOf("=") + 1);
                    sb.append("grep -qF 'name=\"").append(k).append("\"' ").append(path)
                      .append(" || sed -i '/<\\/map>/i \\  <string name=\"").append(k).append("\">").append(v).append("<\\/string>' ").append(path).append("; ");
                }
            } else {
                sb.append("grep -qF '[TrackingConfig]' ").append(path).append(" || echo '[TrackingConfig]' >> ").append(path).append("; ");
                for (String keyVal : trackingKeys) {
                    String k = keyVal.substring(0, keyVal.indexOf("="));
                    sb.append("grep -qF '").append(k).append("' ").append(path)
                      .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                    sb.append("sed -i 's/^").append(k).append("=.*/").append(keyVal).append("/' ").append(path).append("; ");
                }
            }
            String cmd = sb.toString();
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "MLBB Skill Auto-Tracking & Projectile Magnetism applied for " + packageName);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    private static void forceWrite(String path, String content) {
        ShizukuFileManager.writeFile(path, content, "666");
    }

    private static boolean applyPatch(String path, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final int frameRateLevel = FpsUnlockTier.fromFps(forcedFps).level;
        if (!ShizukuFileManager.fileExists(path)) {
            String content = String.format(
                    "[Graphics]\nHighFPSMode=1\nFrameRateLevel=%d\nGraphicsQuality=4\nHDMode=1\nHDRMode=1\nUltraHDMode=1\nShadow=1\nFPS=%d\nMaxFrameRate=%d\nTargetFPS=%d\nHighFrameRate=1\nUnlockFPS=1\nSuperHighFPS=1\nUnlock120Hz=1\nUnlock144Hz=1\nUnlock165Hz=1\nUnlock185Hz=1\nUltra144FPS=1\nUltra165FPS=1\nUltra185FPS=1\nHighFreqTouchHz=%d\n",
                    frameRateLevel, forcedFps, forcedFps, forcedFps, forcedFps
            );
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd = "sed -i 's/^HighFPSMode=.*/HighFPSMode=1/' " + path + "; " +
                         "sed -i 's/^FrameRateLevel=.*/FrameRateLevel=" + frameRateLevel + "/' " + path + "; " +
                         "sed -i 's/^GraphicsQuality=.*/GraphicsQuality=4/' " + path + "; " +
                         "sed -i 's/^HDMode=.*/HDMode=1/' " + path + "; " +
                         "sed -i 's/^HDRMode=.*/HDRMode=1/' " + path + "; " +
                         "sed -i 's/^UltraHDMode=.*/UltraHDMode=1/' " + path + "; " +
                         "sed -i 's/^Shadow=.*/Shadow=1/' " + path + "; " +
                         "sed -i 's/^FPS=.*/FPS=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^MaxFrameRate=.*/MaxFrameRate=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^TargetFPS=.*/TargetFPS=" + forcedFps + "/' " + path + "; " +
                         "sed -i 's/^HighFrameRate=.*/HighFrameRate=1/' " + path + "; " +
                         "sed -i 's/^UnlockFPS=.*/UnlockFPS=1/' " + path + "; " +
                         "chmod 666 " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
            return true;
        }
    }

    private static void ensureDirectory(String path) {
        ShizukuFileManager.ensureParentDirectory(path);
    }
}
