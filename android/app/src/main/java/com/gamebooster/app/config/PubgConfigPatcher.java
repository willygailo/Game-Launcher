package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
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
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, targetFps)) patched++;
        }
        Log.i(TAG, "PUBGM patch: " + patched + " files for " + packageName + " @ " + targetFps + "fps");
        return patched > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL PUBGM/BGMI config paths unconditionally.
     * Uses Shizuku (temporary root) to reach /data/data/ paths.
     * Includes full UE4 CVar injection for FPS, frame rate limit, and content scale.
     *
     * @return true if at least one path was written
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        // PUBGM FPS level: 9=165fps, 7=120fps, 6=90fps, 5=60fps
        int pubgFpsLevel = targetFps >= 165 ? 9 : (targetFps >= 120 ? 7 : (targetFps >= 90 ? 6 : 5));

        String content = String.format(
                "[UserCustom DeviceProfile]\n" +
                "+CVars=r.PUBGDeviceFPS=%d\n" +
                "+CVars=r.PUBGFrameRateLimit=%d\n" +
                "+CVars=r.MobileFPSLimit=%d\n" +
                "+CVars=r.FrameRateLimit=%d\n" +
                "+CVars=r.PUBGMaxFPS=%d\n" +
                "+CVars=r.PUBGHDRMode=1\n" +
                "+CVars=r.MobileHDR=1\n" +
                "+CVars=r.PUBGExtremeHDR=1\n" +
                "+CVars=r.PUBGUltraExtreme=1\n" +
                "+CVars=r.PUBGEnableUltraHDR=1\n" +
                "+CVars=r.PUBGEnableUltraExtremeFPS=1\n" +
                "+CVars=r.PUBGQualityLevel=4\n" +
                "+CVars=r.PUBGSDKQualityLevel=4\n" +
                "+CVars=r.Tonemapper.Quality=4\n" +
                "+CVars=r.HDR.Display.OutputDevice=1\n" +
                "+CVars=r.MobileContentScaleFactor=1.0\n" +
                "+CVars=r.MobileTonemapperFilm=1\n" +
                "+CVars=r.MobileTouchBoostRate=165\n" +
                "FrameRateLevel=%d\n" +
                "bUseHDRMode=True\n" +
                "bUseHighQualityBloom=True\n" +
                "bUseAntiAliasing=True\n",
                pubgFpsLevel, targetFps, targetFps, targetFps, targetFps, pubgFpsLevel
        );

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            forceWrite(path, content);
            written++;
        }

        // Apply Active.sav binary patch for 165FPS/120FPS unlock
        patchActiveSav(packageName, pubgFpsLevel);

        Log.i(TAG, "PUBGM competitive UltraExtreme HDR 165FPS force-write: " + written + " paths @ " + targetFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Patches binary Active.sav files for PUBGM/BGMI to override internal FPS cap headers.
     */
    public static void patchActiveSav(String packageName, int pubgFpsLevel) {
        if (packageName == null) return;
        List<String> savPaths = new ArrayList<>();
        savPaths.add("/sdcard/Android/data/" + packageName + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav");
        savPaths.add("/data/data/" + packageName + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav");

        // Format byte as hex string (e.g., 09 for 165FPS, 07 for 120FPS)
        String hexByte = String.format("%02x", pubgFpsLevel);
        for (String sav : savPaths) {
            String cmd = "test -f " + sav + " && printf '\\x" + hexByte + "' | dd of=" + sav + " bs=1 seek=44 count=1 conv=notrunc 2>/dev/null";
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "PUBGM Active.sav binary FPS patch applied (Level " + pubgFpsLevel + ") for " + packageName);
    }

    /**
     * Injects super-fast touch CVar into PUBGM/BGMI config files.
     * Sets r.MobileTouchBoostRate=165 for 165Hz touch acceleration.
     */
    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String cvar = "+CVars=r.MobileTouchBoostRate=165";
            String cmd =
                "grep -qF 'r.MobileTouchBoostRate' " + path + " || echo '" + cvar + "' >> " + path + "; " +
                "sed -i 's/+CVars=r.MobileTouchBoostRate=.*/+CVars=r.MobileTouchBoostRate=165/' " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "PUBGM super-fast touch applied for " + packageName);
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
        ensureDirectory(path);
        String escaped = content.replace("'", "'\\''");
        String writeCmd = "printf '" + escaped + "' > " + path;
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommand(writeCmd);
        } else {
            CommandExecutor.executeSystemCommand(writeCmd);
        }
    }

    private static boolean applyPatch(String path, int targetFps) {
        ensureDirectory(path);
        int pubgFpsLevel = targetFps >= 165 ? 9 : (targetFps >= 120 ? 7 : (targetFps >= 90 ? 6 : 5));
        String checkRes = CommandExecutor.executeSystemCommand("test -f " + path + " && echo EXISTS");

        if (!checkRes.contains("EXISTS")) {
            String content = String.format(
                    "[UserCustom DeviceProfile]\\n+CVars=r.PUBGDeviceFPS=%d\\n+CVars=r.PUBGFrameRateLimit=%d\\n+CVars=r.MobileFPSLimit=%d\\nFrameRateLevel=%d\\n",
                    pubgFpsLevel, targetFps, targetFps, pubgFpsLevel
            );
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
        } else {
            String[][] cvars = {
                {"+CVars=r.PUBGDeviceFPS",      "+CVars=r.PUBGDeviceFPS="    + pubgFpsLevel},
                {"+CVars=r.PUBGFrameRateLimit",  "+CVars=r.PUBGFrameRateLimit=" + targetFps},
                {"+CVars=r.MobileFPSLimit",      "+CVars=r.MobileFPSLimit="   + targetFps}
            };
            for (String[] cvar : cvars) {
                CommandExecutor.executeSystemCommand(
                    "grep -qF '" + cvar[0] + "' " + path + " || echo '" + cvar[1] + "' >> " + path);
            }
            CommandExecutor.executeSystemCommand("sed -i 's/+CVars=r.PUBGDeviceFPS=.*/+CVars=r.PUBGDeviceFPS=" + pubgFpsLevel + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/+CVars=r.PUBGFrameRateLimit=.*/+CVars=r.PUBGFrameRateLimit=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/+CVars=r.MobileFPSLimit=.*/+CVars=r.MobileFPSLimit=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/FrameRateLevel=.*/FrameRateLevel=" + pubgFpsLevel + "/' " + path);
        }
        return true;
    }

    private static void ensureDirectory(String path) {
        if (path == null) return;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            String parentDir = path.substring(0, lastSlash);
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand("mkdir -p " + parentDir);
            } else {
                CommandExecutor.executeSystemCommand("mkdir -p " + parentDir);
            }
        }
    }
}
