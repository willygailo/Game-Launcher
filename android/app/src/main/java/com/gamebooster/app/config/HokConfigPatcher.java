package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileBridge;
import java.util.ArrayList;
import java.util.List;

/**
 * HokConfigPatcher manages internal config files for Honor of Kings (HOK Global & CN versions).
 * Packages: com.levelinfinite.sgameGlobal, com.tencent.tmgp.sgame
 *
 * HOK hardware maximum is 120 FPS (FrameRateLevel=4, FPSLevel=5) — never falls back below that.
 * patchCompetitive() force-overwrites ALL paths unconditionally via Shizuku.
 */
public class HokConfigPatcher {

    private static final String TAG = "HokConfigPatcher";

    /** HOK hardware max FPS — never force higher than the game engine supports. */
    private static final int HOK_MAX_FPS = 120;

    // ─── Standard Patch ───────────────────────────────────────────────────────

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;

        // Always minimum 120 — never 60/90
        int fps = Math.max(Math.min(targetFps, HOK_MAX_FPS), 120);
        int frameRateLevel = 4; // 4 = Ultra High (120Hz)
        int fpsLevel       = 5; // 5 = 120 FPS in HOK internal enum

        List<String> paths = getConfigPaths(packageName);

        String[] keys = new String[] {
            "HighFrameRate",
            "FrameRate",
            "FrameRateLevel",
            "FPSLevel",
            "GraphicsLevel",
            "ResolutionRate",
            "HDMode",
            "ShadowQuality",
            "ParticleQuality",
            "TargetFPS",
            "Unlock120Hz"
        };

        String[] values = new String[] {
            "1",
            String.valueOf(fps),
            String.valueOf(frameRateLevel),
            String.valueOf(fpsLevel),
            "5",   // Ultra HD Graphics
            "4",   // Extreme Resolution
            "1",
            "4",
            "4",
            String.valueOf(fps),
            "1"
        };

        int count = 0;
        for (String path : paths) {
            if (ShizukuFileBridge.updateIniKeys(path, keys, values, "[SystemConfig]")) {
                count++;
            }
        }
        Log.i(TAG, "HOK patch applied for " + packageName + ": " + count + " paths @ " + fps + " FPS (120Hz Max)");
        return count > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL HOK config paths unconditionally.
     * Always clamps to HOK_MAX_FPS (120). Uses Shizuku for /data/data/ access.
     * Never falls back to 60 or 90 Hz — minimum 120.
     *
     * @return true if at least one path was written
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;

        // HOK engine max is 120 — cap but never fall below 120
        int fps            = Math.max(Math.min(targetFps, HOK_MAX_FPS), 120);
        int frameRateLevel = 4; // Ultra High = 120Hz
        int fpsLevel       = 5; // 120 FPS enum level

        String content = "[SystemConfig]\n" +
                "HighFrameRate=1\n" +
                "FrameRate=" + fps + "\n" +
                "FrameRateLevel=" + frameRateLevel + "\n" +
                "FPSLevel=" + fpsLevel + "\n" +
                "TargetFPS=" + fps + "\n" +
                "MaxFPS=" + fps + "\n" +
                "Unlock120Hz=1\n" +
                "HighFPSMode=1\n" +
                "GraphicsLevel=5\n" +
                "ResolutionRate=4\n" +
                "HDMode=1\n" +
                "HDRMode=1\n" +
                "ShadowQuality=4\n" +
                "ParticleQuality=4\n" +
                "AntiAliasing=1\n" +
                "SuperResolution=1\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            forceWrite(path, content);
            written++;
        }
        Log.i(TAG, "HOK competitive 120FPS force-write: " + written + " paths @ " + fps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Injects super-fast touch response keys into HOK config files.
     * HOK max touch rate is 120Hz — sets HighFreqTouchHz=120 and max touch response level.
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
                "grep -qF 'HighFreqTouchHz' " + path + " || echo 'HighFreqTouchHz=120' >> " + path + "; " +
                "sed -i 's/^HighFreqTouchHz=.*/HighFreqTouchHz=120/' " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "HOK super-fast touch applied for " + packageName);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/Resources/SystemConfig.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/Resources/GameConfig.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/SystemConfig.ini");
        paths.add("/data/data/" + pkg + "/files/Resources/SystemConfig.ini");
        paths.add("/data/data/" + pkg + "/files/SystemConfig.ini");
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
