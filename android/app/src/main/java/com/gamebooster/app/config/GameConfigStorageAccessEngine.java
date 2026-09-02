package com.gamebooster.app.config;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
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
 * Handles dual-engine storage access:
 * 1. Shizuku elevated shell (chmod 777, chown, setenforce, AppOps) for /data/data and /sdcard/Android/data.
 * 2. Storage Access Framework (SAF) Document Trees and app-private storage fallback.
 */
public final class GameConfigStorageAccessEngine {

    private static final String TAG = "StorageAccessEngine";

    public enum StorageAccessMode {
        SHIZUKU_ELEVATED("Elevated Shizuku ADB Access"),
        SAF_DOCUMENT_TREE("Storage Access Framework (SAF) Tree"),
        DIRECT_APP_PRIVATE("App-Private Internal Storage"),
        RESTRICTED("Restricted Scoped Storage");

        private final String displayName;

        StorageAccessMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private GameConfigStorageAccessEngine() {
    }

    public static final class StorageAccessReport {
        public final String packageName;
        public final boolean hasShizukuAccess;
        public final StorageAccessMode accessMode;
        public final int totalPathsResolved;
        public final int accessiblePathsCount;
        public final List<String> paths;
        public final String statusSummary;

        public StorageAccessReport(String packageName, boolean hasShizukuAccess,
                                   StorageAccessMode accessMode,
                                   int totalPathsResolved, int accessiblePathsCount,
                                   List<String> paths, String statusSummary) {
            this.packageName = packageName;
            this.hasShizukuAccess = hasShizukuAccess;
            this.accessMode = accessMode != null ? accessMode : StorageAccessMode.RESTRICTED;
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
        try {
            String extStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
            pathSet.add(extStorage + "/Android/data/" + pkg);
            pathSet.add(extStorage + "/Android/data/" + pkg + "/files");
            pathSet.add(extStorage + "/Android/data/" + pkg + "/cache");
            pathSet.add(extStorage + "/Android/obb/" + pkg);
            pathSet.add(extStorage + "/Android/media/" + pkg);
        } catch (Throwable ignored) {
            pathSet.add("/sdcard/Android/data/" + pkg);
            pathSet.add("/sdcard/Android/obb/" + pkg);
        }

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
     * Determines whether a given string is a configuration file path rather than a directory.
     */
    public static boolean isFileLikePath(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase();
        return lower.endsWith(".ini") || lower.endsWith(".xml") || lower.endsWith(".json")
                || lower.endsWith(".cfg") || lower.endsWith(".sav") || lower.endsWith(".dat")
                || lower.endsWith(".txt") || lower.endsWith(".conf") || lower.endsWith(".config")
                || lower.endsWith(".properties");
    }

    /**
     * Constructs a Storage Access Framework (SAF) Document Tree Uri representation for Android/data.
     */
    public static Uri buildSafDocumentTreeUri(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Android/data");
        }
        return DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Android/data/" + packageName.trim());
    }

    /**
     * Checks if standard file system access or SAF permissions are available.
     */
    public static boolean hasDirectStorageAccess(Context context, String path) {
        if (path == null || path.trim().isEmpty()) return false;
        try {
            File file = new File(path);
            return file.exists() && file.canRead();
        } catch (Throwable t) {
            return false;
        }
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

        // 3. Filesystem Chmod 777 & Directory Access (Never run mkdir -p on file paths)
        for (String path : paths) {
            if (isFileLikePath(path)) {
                File parent = new File(path).getParentFile();
                if (parent != null) {
                    commands.add("mkdir -p \"" + parent.getAbsolutePath() + "\" 2>/dev/null");
                }
                commands.add("chmod 666 \"" + path + "\" 2>/dev/null");
            } else {
                commands.add("mkdir -p \"" + path + "\" 2>/dev/null");
                commands.add("chmod -R 777 \"" + path + "\" 2>/dev/null");
            }
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
            return new StorageAccessReport("", false, StorageAccessMode.RESTRICTED, 0, 0, Collections.emptyList(), "Invalid Package");
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

        StorageAccessMode mode = hasShizuku
                ? StorageAccessMode.SHIZUKU_ELEVATED
                : (accessibleCount > 0 ? StorageAccessMode.DIRECT_APP_PRIVATE : StorageAccessMode.RESTRICTED);

        String summary = hasShizuku
                ? "Full Shizuku Elevated Access Granted (" + paths.size() + " paths unlocked)"
                : (accessibleCount > 0
                ? "Standard Access (" + accessibleCount + "/" + paths.size() + " paths)"
                : "Restricted Scoped Storage (Grant Shizuku)");

        return new StorageAccessReport(packageName, hasShizuku, mode, paths.size(), accessibleCount, paths, summary);
    }

    /**
     * Ensures a single target file path (or its parent directory) is accessible
     * for write operations before ConfigFileHelper attempts an atomic write.
     *
     * Execution order:
     *  1. mkdir -p parent directory
     *  2. chmod 777 parent directory
     *  3. chmod 666 target file (if it exists)
     *  4. appops set pkg MANAGE_EXTERNAL_STORAGE allow (for /sdcard paths)
     *  5. setenforce 0 (temporary SELinux permissive for /data/data paths, Shizuku only)
     *
     * @param context   application context (may be null, Shizuku path works without it)
     * @param filePath  absolute path to the target config file
     */
    public static void ensureAndGrantPathAccess(android.content.Context context, String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) return;
        if (!ShizukuExecutor.hasShizukuPermission()) return;

        String path = filePath.trim();
        java.io.File file   = new java.io.File(path);
        java.io.File parent = file.getParentFile();

        // 1. mkdir -p parent
        if (parent != null) {
            ShizukuExecutor.executeShizukuCommand("mkdir -p \"" + parent.getAbsolutePath() + "\" 2>/dev/null");
            // 2. chmod 777 parent dir
            ShizukuExecutor.executeShizukuCommand("chmod 777 \"" + parent.getAbsolutePath() + "\" 2>/dev/null");
        }

        // 3. chmod 666 file (write-accessible by any uid)
        ShizukuExecutor.executeShizukuCommand("chmod 666 \"" + path + "\" 2>/dev/null");

        // 4. appops for /sdcard/Android/data paths (scoped storage bypass)
        if (path.contains("/sdcard/") || path.contains("/storage/emulated/")) {
            String pkg = extractPackageFromPath(path);
            if (pkg != null) {
                ShizukuExecutor.executeShizukuCommand("appops set " + pkg + " MANAGE_EXTERNAL_STORAGE allow");
                ShizukuExecutor.executeShizukuCommand("appops set " + pkg + " NO_ISOLATED_STORAGE allow");
                ShizukuExecutor.executeShizukuCommand("appops set " + pkg + " LEGACY_STORAGE allow");
            }
        }

        // 5. Temporary SELinux permissive for /data/data or /data/user paths (Shizuku ADB shell)
        if (path.startsWith("/data/data/") || path.startsWith("/data/user/")) {
            ShizukuExecutor.executeShizukuCommand("setenforce 0 2>/dev/null");
        }
    }

    /**
     * Extracts the package name from a standard Android storage path.
     * e.g. "/sdcard/Android/data/com.tencent.ig/files/..." → "com.tencent.ig"
     */
    private static String extractPackageFromPath(String path) {
        if (path == null) return null;
        String[] segments = path.split("/");
        for (int i = 0; i < segments.length - 1; i++) {
            if ("data".equals(segments[i]) || "obb".equals(segments[i])) {
                String candidate = segments.length > i + 1 ? segments[i + 1] : null;
                if (candidate != null && candidate.contains(".")) return candidate;
            }
        }
        return null;
    }
}

