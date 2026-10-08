package com.gamebooster.app.config;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * GameModAutoSyncEngine — Automated File & Mod Replacement Engine (MT Manager Replacement).
 *
 * Automatically mirrors and synchronizes user custom mod files and directory structures
 * directly into target game data folders (/storage/emulated/0/Android/data/<pkg>/files/)
 * upon game launch, completely eliminating manual MT Manager file surgery.
 *
 * Supported Staging Directories (Scanned in order of priority):
 *  1. /storage/emulated/0/GameLauncher/mods/<pkg>/
 *  2. /sdcard/GameLauncher/mods/<pkg>/
 *  3. /storage/emulated/0/Download/GameLauncherMods/<pkg>/
 *  4. /sdcard/Download/GameLauncherMods/<pkg>/
 */
public final class GameModAutoSyncEngine {

    private static final String TAG = "GameModAutoSync";

    private GameModAutoSyncEngine() {}

    /**
     * Resolves potential staging directories for custom mods belonging to the target package.
     */
    public static List<String> getStagingDirectories(String pkg) {
        List<String> dirs = new ArrayList<>();
        if (pkg == null || pkg.trim().isEmpty()) return dirs;
        String cleanPkg = pkg.trim();

        dirs.add("/storage/emulated/0/GameLauncher/mods/" + cleanPkg);
        dirs.add("/sdcard/GameLauncher/mods/" + cleanPkg);
        dirs.add("/storage/emulated/0/Download/GameLauncherMods/" + cleanPkg);
        dirs.add("/sdcard/Download/GameLauncherMods/" + cleanPkg);
        dirs.add("/storage/emulated/0/Download/" + cleanPkg);
        dirs.add("/sdcard/Download/" + cleanPkg);
        return dirs;
    }

    /**
     * Resolves active game data target base paths (/Android/data/<pkg>/files/).
     */
    public static List<String> getTargetDestinations(String pkg) {
        List<String> targets = new ArrayList<>();
        if (pkg == null || pkg.trim().isEmpty()) return targets;
        String cleanPkg = pkg.trim();

        targets.add("/storage/emulated/0/Android/data/" + cleanPkg + "/files");
        targets.add("/sdcard/Android/data/" + cleanPkg + "/files");
        targets.add("/data/data/" + cleanPkg + "/files");

        // Android 15/16 Multi-user & Private Space profiles (User 10..14)
        for (int userId = 10; userId <= 14; userId++) {
            targets.add("/storage/emulated/" + userId + "/Android/data/" + cleanPkg + "/files");
        }
        return targets;
    }

    /**
     * Synchronizes custom user files from any active staging directory into the game's exact target directories.
     *
     * @param context Application context
     * @param pkg Target package name (e.g., "com.mobile.legends", "com.tencent.ig", "com.garena.game.codm")
     * @return Number of files synchronized successfully
     */
    public static int syncModsForPackage(Context context, String pkg) {
        if (pkg == null || pkg.trim().isEmpty()) return 0;
        String cleanPkg = pkg.trim();

        List<String> stagingDirs = getStagingDirectories(cleanPkg);
        String activeStagingDir = null;

        for (String candidate : stagingDirs) {
            if (ShizukuFileManager.fileExists(candidate) && ShizukuFileManager.isDirectory(candidate)) {
                activeStagingDir = candidate;
                break;
            }
        }

        if (activeStagingDir == null) {
            Log.d(TAG, "No custom mod staging directory found for " + cleanPkg + ". Built-in patchers will handle injection.");
            return 0;
        }

        List<String> targetDirs = getTargetDestinations(cleanPkg);
        int totalSynced = 0;

        for (String targetDir : targetDirs) {
            // Check if game destination root exists (or create parent if needed)
            String targetRoot = targetDir.endsWith("/files") ? targetDir.substring(0, targetDir.length() - 6) : targetDir;
            if (!ShizukuFileManager.fileExists(targetRoot)) {
                continue;
            }

            try {
                // Ensure target /files directory exists
                ShizukuFileManager.makeDirectory(targetDir);

                // Build fast recursive copy script with preservation and permission chmod
                StringBuilder sb = new StringBuilder();
                sb.append("SRC=\"").append(activeStagingDir).append("\"\n");
                sb.append("DST=\"").append(targetDir).append("\"\n");
                sb.append("[ -d \"$SRC\" ] || exit 0\n");
                sb.append("mkdir -p \"$DST\" 2>/dev/null\n");
                // Copy all contents recursively into target files directory
                sb.append("cp -rf \"$SRC/\"* \"$DST/\" 2>/dev/null\n");
                // Fix permissions: directories 777, files 666
                sb.append("find \"$DST\" -type d -exec chmod 777 {} + 2>/dev/null\n");
                sb.append("find \"$DST\" -type f -exec chmod 666 {} + 2>/dev/null\n");
                // Count copied files
                sb.append("find \"$SRC\" -type f 2>/dev/null | wc -l\n");

                String script = sb.toString();
                String result = ShizukuExecutor.hasShizukuPermission()
                        ? ShizukuExecutor.executeShizukuCommand(script)
                        : CommandExecutor.executeSystemCommand(script);

                int count = 0;
                if (result != null && !result.trim().isEmpty() && !result.startsWith("ERROR:")) {
                    try {
                        String lastLine = result.trim();
                        int lastNewline = lastLine.lastIndexOf('\n');
                        if (lastNewline >= 0) lastLine = lastLine.substring(lastNewline + 1).trim();
                        count = Integer.parseInt(lastLine);
                    } catch (Throwable ignored) {}
                }

                if (count > 0) {
                    totalSynced += count;
                    Log.i(TAG, "⚡ [MT-Manager Replacement] Auto-synced " + count + " custom files from " 
                            + activeStagingDir + " to " + targetDir);
                }
            } catch (Throwable t) {
                Log.w(TAG, "Error syncing mods for " + cleanPkg + " to " + targetDir, t);
            }
        }

        // Enforce SELinux and ownership bypass across target files
        try {
            List<String> verifyPaths = new ArrayList<>();
            verifyPaths.add(targetDirs.get(0));
            GameSecurityBypassEngine.enforceSelinuxAndOwnershipBypass(cleanPkg, verifyPaths);
        } catch (Throwable ignored) {}

        return totalSynced;
    }

    /**
     * Initializes default staging folder structure in /sdcard/GameLauncher/mods/
     * so user can conveniently drop files from file managers or PC via USB.
     */
    public static void initializeStagingDirectory() {
        String baseModsDir = "/storage/emulated/0/GameLauncher/mods";
        try {
            ShizukuFileManager.makeDirectory(baseModsDir);
            ShizukuFileManager.makeDirectory(baseModsDir + "/com.mobile.legends");
            ShizukuFileManager.makeDirectory(baseModsDir + "/com.tencent.ig");
            ShizukuFileManager.makeDirectory(baseModsDir + "/com.garena.game.codm");
            ShizukuFileManager.makeDirectory(baseModsDir + "/com.activision.callofduty.shooter");
            Log.d(TAG, "Initialized GameLauncher mod staging directories at " + baseModsDir);
        } catch (Throwable t) {
            Log.w(TAG, "Error initializing staging directories: " + t.getMessage());
        }
    }

    /**
     * Applies anti-redownload timestamps and checksum bypass locks so client game
     * does not overwrite patched configuration files.
     */
    public static boolean applyAntiRedownloadLocks(String pkg) {
        if (pkg == null) return false;
        String cleanPkg = pkg.trim();
        String script = "sh /data/data/com.gamebooster.app/assets/shell/install_game_mod_core.sh " + cleanPkg + " 2>/dev/null || "
                      + "touch -t 203001010000 /sdcard/Android/data/" + cleanPkg + "/files/res_skip_patch 2>/dev/null";
        try {
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(script);
            } else {
                CommandExecutor.executeSystemCommand(script);
            }
            return true;
        } catch (Throwable t) {
            Log.w(TAG, "applyAntiRedownloadLocks note: " + t.getMessage());
            return false;
        }
    }
}
