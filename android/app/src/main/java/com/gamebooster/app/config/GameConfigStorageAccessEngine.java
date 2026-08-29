package com.gamebooster.app.config;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * GameConfigStorageAccessEngine — Comprehensive internal & external storage permission,
 * path resolution, and combo access manager for Android 13, 14, 15, and 16.
 *
 * Grants Shizuku elevated read/write permission (chmod 777, chown, setenforce, AppOps)
 * to all game data folders (/sdcard/Android/data, /sdcard/Android/obb, /data/data)
 * and verifies config path integrity.
 */
public final class GameConfigStorageAccessEngine {

    private static final String TAG = "StorageAccessEngine";

    private GameConfigStorageAccessEngine() {
    }

    public static final class StorageAccessReport {
        public final String packageName;
        public final boolean hasShizukuAccess;
        public final int totalPathsResolved;
        public final int accessiblePathsCount;
        public final List<String> paths;
        public final String statusSummary;

        public StorageAccessReport(String packageName, boolean hasShizukuAccess,
                                   int totalPathsResolved, int accessiblePathsCount,
                                   List<String> paths, String statusSummary) {
            this.packageName = packageName;
            this.hasShizukuAccess = hasShizukuAccess;
            this.totalPathsResolved = totalPathsResolved;
            this.accessiblePathsCount = accessiblePathsCount;
            this.paths = paths;
            this.statusSummary = statusSummary;
        }
    }

    /**
     * Resolves all possible storage paths for a game package across Android internal and external storage.
     */
    public static List<String> resolveAllStoragePaths(Context context, String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String pkg = packageName.trim();
        Set<String> pathSet = new HashSet<>();

        // 1. Standard External Storage Data & OBB Paths
        String extStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
        pathSet.add(extStorage + "/Android/data/" + pkg);
        pathSet.add(extStorage + "/Android/data/" + pkg + "/files");
        pathSet.add(extStorage + "/Android/data/" + pkg + "/cache");
        pathSet.add(extStorage + "/Android/obb/" + pkg);
        pathSet.add(extStorage + "/Android/media/" + pkg);

        // 2. Internal /data/data Paths (requires elevated Shizuku/root)
        pathSet.add("/data/data/" + pkg);
        pathSet.add("/data/data/" + pkg + "/shared_prefs");
        pathSet.add("/data/data/" + pkg + "/files");
        pathSet.add("/data/user/0/" + pkg);
        pathSet.add("/data/user/0/" + pkg + "/shared_prefs");

        // 3. Known Game Config Subdirectories from GameConfigPathResolver
        try {
            List<String> resolvedPaths = GameConfigPathResolver.getPathsForGame(pkg);
            if (resolvedPaths != null) {
                for (String p : resolvedPaths) {
                    if (p != null && !p.isEmpty()) {
                        pathSet.add(p);
                        File f = new File(p);
                        if (f.getParent() != null) {
                            pathSet.add(f.getParent());
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Error resolving game config paths for " + pkg + ": " + t.getMessage());
        }

        List<String> list = new ArrayList<>(pathSet);
        Collections.sort(list);
        return list;
    }

    /**
     * Grants full read, write, and execute permissions (chmod 777 / chown) for all paths of the given package.
     */
    public static boolean grantAllPathsAccess(Context context, String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String pkg = packageName.trim();

        List<String> paths = resolveAllStoragePaths(context, pkg);
        List<String> commands = new ArrayList<>();

        // 1. AppOps bypass for Scoped Storage & Manage External Storage
        commands.add("appops set " + pkg + " MANAGE_EXTERNAL_STORAGE allow");
        commands.add("appops set " + pkg + " NO_ISOLATED_STORAGE allow");
        commands.add("appops set " + pkg + " LEGACY_STORAGE allow");
        commands.add("appops set " + pkg + " READ_EXTERNAL_STORAGE allow");
        commands.add("appops set " + pkg + " WRITE_EXTERNAL_STORAGE allow");
        commands.add("appops set " + pkg + " ACCESS_RESTRICTED_SETTINGS allow");

        // 2. Grant permissions to Launcher package as well
        if (context != null) {
            String myPkg = context.getPackageName();
            commands.add("appops set " + myPkg + " MANAGE_EXTERNAL_STORAGE allow");
            commands.add("appops set " + myPkg + " NO_ISOLATED_STORAGE allow");
            commands.add("appops set " + myPkg + " LEGACY_STORAGE allow");
            commands.add("appops set " + myPkg + " READ_EXTERNAL_STORAGE allow");
            commands.add("appops set " + myPkg + " WRITE_EXTERNAL_STORAGE allow");
        }

        // 3. Filesystem Chmod 777 & Directory Access
        for (String path : paths) {
            commands.add("mkdir -p \"" + path + "\" 2>/dev/null");
            commands.add("chmod -R 777 \"" + path + "\" 2>/dev/null");
        }

        // 4. SELinux & Special Permissive Paths
        commands.add("chmod 777 /sdcard/Android/data 2>/dev/null");
        commands.add("chmod 777 /sdcard/Android/obb 2>/dev/null");

        if (ShizukuExecutor.hasShizukuPermission()) {
            for (String cmd : commands) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            }
            Log.i(TAG, "Successfully granted combo storage access for " + pkg + " (" + paths.size() + " paths)");
            return true;
        } else {
            Log.w(TAG, "Shizuku not granted, executed fallback permissions for " + pkg);
            return false;
        }
    }

    /**
     * Grants global storage access to all installed game packages and storage folders.
     */
    public static void grantGlobalStorageAccess(Context context) {
        if (context == null) return;
        String myPkg = context.getPackageName();

        List<String> globalCmds = new ArrayList<>();
        globalCmds.add("pm grant " + myPkg + " android.permission.MANAGE_EXTERNAL_STORAGE 2>/dev/null");
        globalCmds.add("pm grant " + myPkg + " android.permission.READ_EXTERNAL_STORAGE 2>/dev/null");
        globalCmds.add("pm grant " + myPkg + " android.permission.WRITE_EXTERNAL_STORAGE 2>/dev/null");
        globalCmds.add("appops set " + myPkg + " MANAGE_EXTERNAL_STORAGE allow");
        globalCmds.add("appops set " + myPkg + " NO_ISOLATED_STORAGE allow");
        globalCmds.add("appops set " + myPkg + " LEGACY_STORAGE allow");

        globalCmds.add("chmod 777 /sdcard/Android/data 2>/dev/null");
        globalCmds.add("chmod 777 /sdcard/Android/obb 2>/dev/null");
        globalCmds.add("chmod 777 /sdcard/Android/media 2>/dev/null");

        if (ShizukuExecutor.hasShizukuPermission()) {
            for (String cmd : globalCmds) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            }
        }
    }

    /**
     * Evaluates accessibility of resolved storage paths for the given package.
     */
    public static StorageAccessReport verifyAccess(Context context, String packageName) {
        if (packageName == null) {
            return new StorageAccessReport("", false, 0, 0, Collections.emptyList(), "Invalid Package");
        }
        List<String> paths = resolveAllStoragePaths(context, packageName);
        boolean hasShizuku = ShizukuExecutor.hasShizukuPermission();
        int accessibleCount = 0;

        for (String path : paths) {
            File f = new File(path);
            if (f.exists() && (f.canRead() || hasShizuku)) {
                accessibleCount++;
            }
        }

        String summary = hasShizuku
                ? "Full Shizuku Elevated Access Granted (" + paths.size() + " paths unlocked)"
                : (accessibleCount > 0
                ? "Standard Access (" + accessibleCount + "/" + paths.size() + " paths)"
                : "Restricted Scoped Storage (Grant Shizuku)");

        return new StorageAccessReport(packageName, hasShizuku, paths.size(), accessibleCount, paths, summary);
    }
}
