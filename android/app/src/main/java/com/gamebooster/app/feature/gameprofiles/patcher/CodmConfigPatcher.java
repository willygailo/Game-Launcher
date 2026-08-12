package com.gamebooster.app.feature.gameprofiles.patcher;

import android.util.Log;
import com.gamebooster.app.platform.shell.CommandExecutor;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;
import java.util.ArrayList;
import java.util.List;

/**
 * CodmConfigPatcher manages internal config files for Call of Duty Mobile (all versions/regions).
 *
 * Two patching modes:
 *  - patch()            → standard patch: create-if-missing or sed-update
 *  - patchCompetitive() → competitive force-write: ALWAYS overwrites all paths, no fallback,
 *                         executed via Shizuku for full data/data access (temporary root)
 */
public class CodmConfigPatcher {

    private static final String TAG = "CodmConfigPatcher";

    // ─── Standard Patch ───────────────────────────────────────────────────────

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, targetFps)) patched++;
        }
        Log.i(TAG, "CODM patch: " + patched + " files for " + packageName + " @ " + targetFps + "fps");
        return patched > 0;
    }

    // ─── Competitive Force-Write (Shizuku, No Fallback) ──────────────────────

    /**
     * Force-overwrites ALL CODM config paths unconditionally.
     * Writes proper JSON for UserSetting.json and INI for GraphicsSettings.ini.
     * Uses Shizuku (temporary root) to reach /data/data/ paths.
     *
     * @return true if at least one path was written
     */
    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;

        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            String content;
            if (path.endsWith(".json")) {
                content = "{\n" +
                        "  \"MaxFrameRate\": " + targetFps + ",\n" +
                        "  \"GraphicQuality\": 4,\n" +
                        "  \"FPSLimit\": " + targetFps + ",\n" +
                        "  \"HDRMode\": 1,\n" +
                        "  \"HDRColorMode\": 2,\n" +
                        "  \"Unlock165Hz\": 1,\n" +
                        "  \"TouchBoostHz\": 165,\n" +
                        "  \"SuperResolution\": 1,\n" +
                        "  \"FieldOfView\": 90,\n" +
                        "  \"AntiAliasing\": 1,\n" +
                        "  \"ShadowQuality\": 2\n" +
                        "}\n";
            } else if (path.endsWith(".xml")) {
                // PlayerPrefs XML format
                content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n" +
                        "<map>\n" +
                        "  <int name=\"MaxFrameRate\" value=\"" + targetFps + "\" />\n" +
                        "  <int name=\"FPSLimit\" value=\"" + targetFps + "\" />\n" +
                        "  <int name=\"GraphicQuality\" value=\"4\" />\n" +
                        "  <int name=\"HDRMode\" value=\"1\" />\n" +
                        "  <int name=\"Unlock165Hz\" value=\"1\" />\n" +
                        "</map>\n";
            } else {
                // INI format
                content = "[Graphics]\n" +
                        "MaxFrameRate=" + targetFps + "\n" +
                        "FPSLimit=" + targetFps + "\n" +
                        "GraphicQuality=4\n" +
                        "HDRMode=1\n" +
                        "HDRColorMode=2\n" +
                        "Unlock165Hz=1\n" +
                        "SuperResolution=1\n" +
                        "TouchBoostHz=165\n" +
                        "AntiAliasing=1\n";
            }
            forceWrite(path, content);
            written++;
        }
        Log.i(TAG, "CODM competitive HDR 165FPS force-write: " + written + " paths @ " + targetFps + "fps for " + packageName);
        return written > 0;
    }

    /**
     * Injects super-fast touch settings into CODM config files.
     * Sets TouchBoostHz=165 in both JSON and INI formats.
     */
    public static void applySuperFastTouch(String packageName) {
        if (packageName == null) return;
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            String cmd;
            if (path.endsWith(".json")) {
                cmd = "grep -qF 'TouchBoostHz' " + path +
                      " || sed -i 's/}$/,\\n  \"TouchBoostHz\": 165\\n}/' " + path + "; " +
                      "sed -i 's/\"TouchBoostHz\":.*/\"TouchBoostHz\": 165,/' " + path;
            } else {
                cmd = "grep -qF 'TouchBoostHz' " + path + " || echo 'TouchBoostHz=165' >> " + path + "; " +
                      "sed -i 's/^TouchBoostHz=.*/TouchBoostHz=165/' " + path;
            }
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        }
        Log.i(TAG, "CODM super-fast touch applied for " + packageName);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/Config/UserSetting.json");
        paths.add("/sdcard/Android/data/" + pkg + "/files/" + pkg + ".v2.playerprefs.xml");
        paths.add("/sdcard/Android/data/" + pkg + "/files/GraphicsSettings.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/ControlsSettings.ini");
        paths.add("/data/data/" + pkg + "/files/GraphicsSettings.ini");
        paths.add("/data/data/" + pkg + "/files/ControlsSettings.ini");
        paths.add("/data/data/" + pkg + "/files/Config/UserSetting.json");
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
        String checkRes = CommandExecutor.executeSystemCommand("test -f " + path + " && echo EXISTS");

        if (!checkRes.contains("EXISTS")) {
            String content = String.format(
                    "{\\n  \"MaxFrameRate\": %d,\\n  \"GraphicQuality\": 4,\\n  \"FPSLimit\": %d,\\n  \"SuperResolution\": 1,\\n  \"FieldOfView\": 90\\n}\\n",
                    targetFps, targetFps
            );
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
        } else {
            CommandExecutor.executeSystemCommand("sed -i 's/\"MaxFrameRate\":.*/\"MaxFrameRate\": " + targetFps + ",/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/\"FPSLimit\":.*/\"FPSLimit\": " + targetFps + ",/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/\"GraphicQuality\":.*/\"GraphicQuality\": 4,/' " + path);
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
