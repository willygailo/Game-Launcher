package com.gamebooster.app.feature.gameprofiles.patcher;

import android.util.Log;
import com.gamebooster.app.platform.shell.CommandExecutor;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;
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
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, targetFps)) patched++;
        }
        Log.i(TAG, "MLBB patch: " + patched + " files for " + packageName + " @ " + targetFps + "fps");
        return patched > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL MLBB config paths unconditionally.
     * Uses Shizuku (temporary root) to reach /data/data/ paths.
     * No existence check — always writes the competitive config.
     *
     * @return true if at least one path was written
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        // MLBB FrameRateLevel: 9 = Ultra-High tier (120/144/165fps)
        int frameRateLevel = targetFps >= 90 ? 9 : (targetFps >= 60 ? 6 : 3);

        String content = "[Graphics]\n" +
                "HighFPSMode=1\n" +
                "FrameRateLevel=" + frameRateLevel + "\n" +
                "GraphicsQuality=4\n" +
                "HDMode=1\n" +
                "HDRMode=1\n" +
                "UltraHDMode=1\n" +
                "Shadow=1\n" +
                "FPS=" + targetFps + "\n" +
                "MaxFrameRate=" + targetFps + "\n" +
                "TargetFPS=" + targetFps + "\n" +
                "HighFrameRate=1\n" +
                "UnlockFPS=1\n" +
                "SuperHighFPS=1\n" +
                "Unlock165Hz=1\n";

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            forceWrite(path, content);
            written++;
        }
        Log.i(TAG, "MLBB competitive HDR 165FPS force-write: " + written + " paths @ " + targetFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Injects super-fast touch response keys into MLBB config files.
     * Optimized for 165Hz panels — sets HighFreqTouchHz=165 and max touch response level.
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
                "grep -qF 'HighFreqTouchHz' " + path + " || echo 'HighFreqTouchHz=165' >> " + path + "; " +
                "sed -i 's/^HighFreqTouchHz=.*/HighFreqTouchHz=165/' " + path;
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "MLBB super-fast touch applied for " + packageName);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/Config/UserSystem.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/HighFPSConfig.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/Com/MobileLegendsSettings.ini");
        paths.add("/data/data/" + pkg + "/files/dragon2017/assets/Com/MobileLegendsSettings.ini");
        paths.add("/data/data/" + pkg + "/files/dragon2017/assets/UI/Config/UserSystem.ini");
        return paths;
    }

    private static void forceWrite(String path, String content) {
        ensureDirectory(path);
        // Escape single quotes for shell safety
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
        int frameRateLevel = targetFps >= 90 ? 9 : (targetFps >= 60 ? 6 : 3);
        String checkRes = CommandExecutor.executeSystemCommand("test -f " + path + " && echo EXISTS");

        if (!checkRes.contains("EXISTS")) {
            String content = String.format(
                    "[Graphics]\\nHighFPSMode=1\\nFrameRateLevel=%d\\nGraphicsQuality=4\\nHDMode=1\\nShadow=1\\nFPS=%d\\nMaxFrameRate=%d\\nTargetFPS=%d\\nHighFrameRate=1\\n",
                    frameRateLevel, targetFps, targetFps, targetFps
            );
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
        } else {
            CommandExecutor.executeSystemCommand("sed -i 's/^HighFPSMode=.*/HighFPSMode=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^FrameRateLevel=.*/FrameRateLevel=" + frameRateLevel + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^GraphicsQuality=.*/GraphicsQuality=4/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^HDMode=.*/HDMode=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^Shadow=.*/Shadow=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^FPS=.*/FPS=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^MaxFrameRate=.*/MaxFrameRate=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^TargetFPS=.*/TargetFPS=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^HighFrameRate=.*/HighFrameRate=1/' " + path);
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
