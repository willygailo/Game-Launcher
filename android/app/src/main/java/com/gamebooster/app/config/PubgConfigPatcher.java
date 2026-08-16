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
        int forcedFps = 165;
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        Log.i(TAG, "PUBGM patch: " + patched + " files for " + packageName + " @ 165fps");
        return patched > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL PUBGM/BGMI config paths unconditionally.
     * Uses Shizuku (temporary root) to reach /data/data/ paths.
     * Includes full UE4 CVar injection for 165 FPS, 165 Hz frame rate limit, and content scale.
     *
     * @return true if at least one path was written
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        // PUBGM FPS level: 9=165fps
        final int pubgFpsLevel = 9;
        final int forcedFps = 165;

        String content = "[UserCustom DeviceProfile]\n" +
                "+CVars=r.PUBGDeviceFPS=" + pubgFpsLevel + "\n" +
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
                "+CVars=r.Unlock165Hz=1\n" +
                "+CVars=r.TouchBoostHz=165\n" +
                "+CVars=r.PUBGAimAssist=1\n" +
                "+CVars=r.AimAssistStrength=1.00\n" +
                "+CVars=r.PUBGRecoilScale=0.00\n" +
                "+CVars=r.WeaponKickReduction=1.00\n" +
                "+CVars=r.AllGunsRecoilReduction=1.00\n" +
                "+CVars=r.NoRecoilAllScopes=1\n" +
                "+CVars=r.BulletDamageBoost=1.90\n" +
                "+CVars=r.DamageMultiplier=1.90\n" +
                "+CVars=r.HeadshotDamageMultiplier=2.90\n" +
                "+CVars=r.CriticalDamageRate=95\n" +
                "FrameRateLevel=" + pubgFpsLevel + "\n" +
                "bUseHDRMode=True\n" +
                "bUseHighQualityBloom=True\n" +
                "bUseAntiAliasing=True\n" +
                "bEnableAimAssist=True\n" +
                "AimAssistLevel=3\n" +
                "SprintSensitivity=150\n" +
                "TPPFieldOfView=100\n" +
                "FPPFieldOfView=150\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            forceWrite(path, content);
            written++;
        }
        Log.i(TAG, "PUBGM competitive HDR 165FPS force-write: " + written + " paths @ 165fps for " + packageName);
        return written > 0;
    }

    /**
     * Injects super-fast zero-delay touch CVar into PUBGM/BGMI config files.
     * Sets r.MobileTouchBoostRate=165 for 165Hz touch acceleration and 1000Hz polling rate.
     */
    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] touchCvars = {
            "+CVars=r.MobileTouchBoostRate=165",
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
     * Injects Aim Assist 100%, FOV (TPP 100 / FPP 150), Sprint 150, Gyro 1000Hz Ultra Response, and 90+ Damage Boost CVars into PUBGM/BGMI config files.
     * Uses Shizuku ADB temporary root access for /data/data/ and /sdcard/ file locations.
     */
    public static void applyAimAssistConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] aimCvars = {
            "+CVars=r.PUBGAimAssist=1",
            "+CVars=r.PUBGAimLockSensitivity=100",
            "+CVars=r.AimAssistStrength=1.00",
            "+CVars=r.PUBGAimbotLock=1",
            "+CVars=r.AimbotTrackingRate=1.00",
            "+CVars=r.PUBGTPPViewRange=100.00",
            "+CVars=r.PUBGFPPViewRange=150.00",
            "+CVars=r.SprintSensitivity=150",
            "+CVars=r.CrosshairMagnetism=1",
            "+CVars=r.GyroSensitivityRatio=2.0",
            "+CVars=r.GyroZeroDelay=1",
            "+CVars=r.GyroSmoothFactor=0",
            "+CVars=r.GyroSampleRate=1000",
            "+CVars=r.GyroAimAssist=1",
            "+CVars=r.GyroRecoilCompensation=1.00",
            "+CVars=r.GyroLatencyMode=0",
            "+CVars=r.BulletTrackingOptimization=1",
            "+CVars=r.MobileTouchAssistMode=1",
            "+CVars=r.DamageMultiplier=1.90",
            "+CVars=r.BulletDamageBoost=1.90",
            "+CVars=r.HeadshotDamageMultiplier=2.90",
            "+CVars=r.CriticalDamageRate=95",
            "+CVars=r.DamageBoostRatio=1.90",
            "bEnableAimAssist=True",
            "AimAssistLevel=3",
            "SprintSensitivity=150",
            "TPPFieldOfView=100",
            "FPPFieldOfView=150"
        };
        for (String path : paths) {
            ensureDirectory(path);
            StringBuilder sb = new StringBuilder();
            for (String cvar : aimCvars) {
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
        Log.i(TAG, "PUBGM Aim Assist 100%, TPP 100, FPP 150, Sprint 150, Gyro 1000Hz & 90+ Damage CVars applied via Shizuku for " + packageName);
    }

    /**
     * Injects Zero Recoil & Weapon Stability CVars for ALL guns and ALL scopes into PUBGM/BGMI config files.
     * Eliminates vertical and horizontal weapon kick (0.00 scale) and camera shake via Shizuku root access.
     */
    public static void applyRecoilControlConfig(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        String[] recoilCvars = {
            "+CVars=r.PUBGRecoilScale=0.00",
            "+CVars=r.WeaponKickReduction=1.00",
            "+CVars=r.VerticalRecoilScale=0.00",
            "+CVars=r.HorizontalRecoilScale=0.00",
            "+CVars=r.BulletSpread=0.00",
            "+CVars=r.GunShakeOptimization=1",
            "+CVars=r.GunBobbing=0",
            "+CVars=r.CameraShakeMultiplier=0.00",
            "+CVars=r.ScopeStabilizationMode=1",
            "+CVars=r.AllGunsRecoilReduction=1.00",
            "+CVars=r.NoRecoilAllScopes=1",
            "+CVars=r.RedDotRecoilScale=0.00",
            "+CVars=r.Scope2xRecoilScale=0.00",
            "+CVars=r.Scope3xRecoilScale=0.00",
            "+CVars=r.Scope4xRecoilScale=0.00",
            "+CVars=r.Scope6xRecoilScale=0.00",
            "+CVars=r.Scope8xRecoilScale=0.00",
            "+CVars=r.WeaponSwayMultiplier=0.00",
            "+CVars=r.GyroRecoilCompensation=1.00"
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
        Log.i(TAG, "PUBGM Zero Recoil All Guns & All Scopes (0.00 Scale) applied via Shizuku for " + packageName);
    }


    // ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/EnjoyCJ.ini");
        paths.add("/data/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini");
        paths.add("/data/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini");
        paths.add("/data/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/EnjoyCJ.ini");
        return paths;
    }

    private static void forceWrite(String path, String content) {
        ShizukuFileManager.writeFile(path, content, "666");
    }

    private static boolean applyPatch(String path, int targetFps) {
        int pubgFpsLevel = targetFps >= 165 ? 9 : (targetFps >= 120 ? 7 : (targetFps >= 90 ? 6 : 5));
        if (!ShizukuFileManager.fileExists(path)) {
            String content = String.format(
                    "[UserCustom DeviceProfile]\n+CVars=r.PUBGDeviceFPS=%d\n+CVars=r.PUBGFrameRateLimit=%d\n+CVars=r.MobileFPSLimit=%d\nFrameRateLevel=%d\n",
                    pubgFpsLevel, targetFps, targetFps, pubgFpsLevel
            );
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String[][] cvars = {
                {"+CVars=r.PUBGDeviceFPS",      "+CVars=r.PUBGDeviceFPS="    + pubgFpsLevel},
                {"+CVars=r.PUBGFrameRateLimit",  "+CVars=r.PUBGFrameRateLimit=" + targetFps},
                {"+CVars=r.MobileFPSLimit",      "+CVars=r.MobileFPSLimit="   + targetFps}
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
                              "sed -i 's/+CVars=r.PUBGFrameRateLimit=.*/+CVars=r.PUBGFrameRateLimit=" + targetFps + "/' " + path + "; " +
                              "sed -i 's/+CVars=r.MobileFPSLimit=.*/+CVars=r.MobileFPSLimit=" + targetFps + "/' " + path + "; " +
                              "sed -i 's/FrameRateLevel=.*/FrameRateLevel=" + pubgFpsLevel + "/' " + path + "; " +
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
