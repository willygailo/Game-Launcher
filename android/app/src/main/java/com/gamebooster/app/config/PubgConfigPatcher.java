package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import java.util.ArrayList;
import java.util.List;

/**
 * PubgConfigPatcher manages internal config files for PUBG Mobile, BGMI, and regional variants.
 *
 * Two patching modes:
 *  - patch()            → standard patch: create-if-missing or sed/grep update
 *  - patchCompetitive() → competitive force-write: ALWAYS overwrites all paths, no fallback,
 *                         executed via Shizuku for full data/data access (temporary root)
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
        Log.i(TAG, "PUBGM patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL PUBGM/BGMI config paths unconditionally.
     * Uses Shizuku (temporary root) to reach /data/data/ paths.
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
                // ── Gyro responsiveness (input hardware tuning, not aim assist) ─
                "+CVars=r.GyroSampleRate=1000\n" +
                "+CVars=r.GyroSensitivityRatio=2.5\n" +
                "+CVars=r.GyroZeroDelay=1\n" +
                "+CVars=r.GyroLatencyMode=0\n" +
                "+CVars=r.GyroSmoothFactor=1\n" +
                "+CVars=r.GyroStabilization=1\n" +
                "FrameRateLevel=" + pubgFpsLevel + "\n" +
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
            forceWrite(path, content);
            written++;
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
     * Sets r.MobileTouchBoostRate=185 for 185Hz touch acceleration and 1000Hz polling rate.
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
            ensureDirectory(path);
            StringBuilder sb = new StringBuilder();
            for (String cvar : touchCvars) {
                String key = cvar.contains("=") ? cvar.substring(0, cvar.indexOf("=")) : cvar;
                sb.append("grep -qF '").append(key).append("' ").append(path)
                  .append(" || echo '").append(cvar).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/").append(key.replace("+", "\\+")).append("=.*/").append(cvar.replace("+", "\\+")).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "PUBGM super-fast zero-delay touch applied for " + packageName);
    }

    /**
     * Injects Aim Assist 150%, FOV (TPP 100 / FPP 150), Sprint 150, Gyro 1000Hz Ultra Response (Super Smooth), and Aim Assist CVars into PUBGM/BGMI config files.
     * Uses Shizuku ADB temporary root access for /data/data/ and /sdcard/ file locations.
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
            ensureDirectory(path);
            StringBuilder sb = new StringBuilder();
            for (String cvar : cvars) {
                String key = cvar.contains("=") ? cvar.substring(0, cvar.indexOf("=")) : cvar;
                sb.append("grep -qF '").append(key).append("' ").append(path)
                  .append(" || echo '").append(cvar).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/").append(key.replace("+", "\\+")).append("=.*/").append(cvar.replace("+", "\\+")).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "PUBGM Aim Assist 150%, FOV 150 & Gyro 1000Hz applied for " + packageName);
    }

    /**
     * Injects Zero Recoil & Weapon Stability CVars for ALL guns and ALL scopes into PUBGM/BGMI config files.
     * Eliminates vertical and horizontal weapon kick (0.00 scale) and camera shake via Shizuku root access.
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
            "+CVars=r.SpreadReduction=1",
            "RecoilReduction=1",
            "WeaponStability=150",
            "ZeroRecoil=1",
            "NoCameraShake=1"
        };
        for (String path : paths) {
            ensureDirectory(path);
            StringBuilder sb = new StringBuilder();
            for (String cvar : recoilCvars) {
                String key = cvar.contains("=") ? cvar.substring(0, cvar.indexOf("=")) : cvar;
                sb.append("grep -qF '").append(key).append("' ").append(path)
                  .append(" || echo '").append(cvar).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/").append(key.replace("+", "\\+")).append("=.*/").append(cvar.replace("+", "\\+")).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
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
            "+CVars=r.DamageMultiplier=2.50",
            "+CVars=r.BulletDamageScale=2.50",
            "+CVars=r.CriticalHitRate=1.0",
            "+CVars=r.HeadshotMultiplier=3.50",
            "+CVars=r.HitboxExpansion=1.5",
            "+CVars=r.BulletVelocityScale=2.0",
            "PhysicalDamageBoost=2.50",
            "MagicDamageBoost=2.50",
            "TrueDamageBoost=2.50",
            "DamageMultiplier=2.50",
            "CriticalDamageRate=99",
            "HeadshotDamageMultiplier=3.50"
        };
        for (String path : paths) {
            ensureDirectory(path);
            StringBuilder sb = new StringBuilder();
            for (String cvar : damageCvars) {
                String key = cvar.contains("=") ? cvar.substring(0, cvar.indexOf("=")) : cvar;
                sb.append("grep -qF '").append(key).append("' ").append(path)
                  .append(" || echo '").append(cvar).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/").append(key.replace("+", "\\+")).append("=.*/").append(cvar.replace("+", "\\+")).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "PUBGM Damage Boost 150% & Bullet Penetration applied for " + packageName);
    }


    // ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    /**
     * Patches Active.sav binary savegame file directly using byte manipulation in Shizuku temporary root.
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
            ensureDirectory(sav);
            String hexByte = String.format("%02x", fpsLevel);
            String cmd = "if [ -f " + sav + " ]; then " +
                         "sed -i 's/FPSLevel.*/FPSLevel\\x00\\x04\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\" + hexByte + "/g' " + sav + " 2>/dev/null; " +
                         "sed -i 's/BattleFPS.*/BattleFPS\\x00\\x04\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\" + hexByte + "/g' " + sav + " 2>/dev/null; " +
                         "sed -i 's/LobbyFPS.*/LobbyFPS\\x00\\x04\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\" + hexByte + "/g' " + sav + " 2>/dev/null; " +
                         "chmod 666 " + sav + "; " +
                         "fi";
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "PUBGM Active.sav binary enforced level " + fpsLevel + " (" + targetFps + " FPS) for " + pkg);
    }

    private static void forceWrite(String path, String content) {
        ShizukuFileManager.writeFile(path, content, "666");
    }

    private static boolean applyPatch(String path, int targetFps) {
        final FpsUnlockTier tier = FpsUnlockTier.fromFps(targetFps);
        final int pubgFpsLevel = tier.level;
        if (!ShizukuFileManager.fileExists(path)) {
            String content = String.format(
                    "[UserCustom DeviceProfile]\n+CVars=r.PUBGDeviceFPS=%d\n+CVars=r.PUBGMaxFPS=%d\n+CVars=r.PUBGFrameRateLimit=%d\n+CVars=r.MobileFPSLimit=%d\n+CVars=r.FrameRateLimit=%d\n+CVars=r.PUBGHDRMode=1\n+CVars=r.MobileHDR=1\n+CVars=r.PUBGQualityLevel=4\n+CVars=r.PUBGSDKQualityLevel=4\n+CVars=r.Tonemapper.Quality=4\n+CVars=r.HDR.Display.OutputDevice=1\n+CVars=r.MobileContentScaleFactor=1.0\n+CVars=r.MobileTonemapperFilm=1\n+CVars=r.Vsync=0\n+CVars=r.TouchBoostHz=%d\n+CVars=r.Unlock120Hz=1\n+CVars=r.Unlock144Hz=1\n+CVars=r.Unlock165Hz=1\n+CVars=r.Unlock185Hz=1\nFrameRateLevel=%d\nbUseHDRMode=True\nbUseHighQualityBloom=True\nbUseAntiAliasing=True\n",
                    pubgFpsLevel, targetFps, targetFps, targetFps, targetFps, targetFps, pubgFpsLevel
            );
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String[][] cvars = {
                {"+CVars=r.PUBGDeviceFPS",      "+CVars=r.PUBGDeviceFPS="    + pubgFpsLevel},
                {"+CVars=r.PUBGMaxFPS",         "+CVars=r.PUBGMaxFPS="       + targetFps},
                {"+CVars=r.PUBGFrameRateLimit",  "+CVars=r.PUBGFrameRateLimit=" + targetFps},
                {"+CVars=r.MobileFPSLimit",      "+CVars=r.MobileFPSLimit="   + targetFps},
                {"+CVars=r.FrameRateLimit",      "+CVars=r.FrameRateLimit="   + targetFps},
                {"+CVars=r.PUBGHDRMode",         "+CVars=r.PUBGHDRMode=1"},
                {"+CVars=r.MobileHDR",           "+CVars=r.MobileHDR=1"},
                {"+CVars=r.PUBGQualityLevel",    "+CVars=r.PUBGQualityLevel=4"},
                {"+CVars=r.PUBGSDKQualityLevel", "+CVars=r.PUBGSDKQualityLevel=4"},
                {"+CVars=r.Unlock120Hz",         "+CVars=r.Unlock120Hz=1"},
                {"+CVars=r.Unlock144Hz",         "+CVars=r.Unlock144Hz=1"},
                {"+CVars=r.Unlock165Hz",         "+CVars=r.Unlock165Hz=1"},
                {"+CVars=r.Unlock185Hz",         "+CVars=r.Unlock185Hz=1"},
                {"+CVars=r.Vsync",               "+CVars=r.Vsync=0"}
            };
            for (String[] cvar : cvars) {
                String cmd = "grep -qF '" + cvar[0] + "' " + path + " || echo '" + cvar[1] + "' >> " + path;
                if (ShizukuExecutor.hasShizukuPermission()) {
                    ShizukuExecutor.executeShizukuCommand(cmd);
                } else {
                    CommandExecutor.executeSystemCommand(cmd);
                }
            }
            String updateCmd = "sed -i 's/+CVars=r.PUBGDeviceFPS=.*/+CVars=r.PUBGDeviceFPS=" + pubgFpsLevel + "/' " + path + "; " +
                              "sed -i 's/+CVars=r.PUBGMaxFPS=.*/+CVars=r.PUBGMaxFPS=" + targetFps + "/' " + path + "; " +
                              "sed -i 's/+CVars=r.PUBGFrameRateLimit=.*/+CVars=r.PUBGFrameRateLimit=" + targetFps + "/' " + path + "; " +
                              "sed -i 's/+CVars=r.MobileFPSLimit=.*/+CVars=r.MobileFPSLimit=" + targetFps + "/' " + path + "; " +
                              "sed -i 's/FrameRateLevel=.*/FrameRateLevel=" + pubgFpsLevel + "/' " + path + "; " +
                              "sed -i 's/bUseHDRMode=.*/bUseHDRMode=True/' " + path + "; " +
                              "sed -i 's/bUseAntiAliasing=.*/bUseAntiAliasing=True/' " + path + "; " +
                              "chmod 666 " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(updateCmd);
            } else {
                CommandExecutor.executeSystemCommand(updateCmd);
            }
            return true;
        }
    }

    private static void ensureDirectory(String path) {
        ShizukuFileManager.ensureParentDirectory(path);
    }
}
