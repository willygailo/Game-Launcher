package com.gamebooster.app.shizuku;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * ShizukuFileManager — Privileged File System Engine.
 *
 * Provides full read, write, copy, delete, list, and chmod control over
 * protected game directories (/data/data/<pkg>/, /sdcard/Android/data/<pkg>/, /sdcard/Android/obb/<pkg>/)
 * and system paths on Android 13, 14, 15, and 16 using official Shizuku API (ADB elevated shell).
 */
public final class ShizukuFileManager {

    private static final String TAG = "ShizukuFileManager";

    public static class FileOpResult {
        public final boolean success;
        public final String path;
        public final String message;

        public FileOpResult(boolean success, String path, String message) {
            this.success = success;
            this.path = path;
            this.message = message;
        }

        public static FileOpResult ok(String path, String msg) {
            return new FileOpResult(true, path, msg);
        }

        public static FileOpResult fail(String path, String msg) {
            return new FileOpResult(false, path, msg);
        }
    }

    /**
     * Checks whether Shizuku is currently active with full privileged file system permissions.
     */
    public static boolean hasFullAccess() {
        return ShizukuExecutor.hasShizukuPermission();
    }

    /**
     * Checks if a file or directory exists at the given path.
     */
    public static boolean fileExists(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        try {
            File localFile = new File(path);
            if (localFile.exists()) return true;

            if (hasFullAccess()) {
                String res = ShizukuExecutor.executeShizukuCommand("test -e '" + path + "' && echo EXISTS");
                return res != null && res.contains("EXISTS");
            } else {
                String res = CommandExecutor.executeSystemCommand("test -e '" + path + "' && echo EXISTS");
                return res != null && res.contains("EXISTS");
            }
        } catch (Throwable t) {
            Log.w(TAG, "fileExists exception for " + path, t);
            return false;
        }
    }

    /**
     * Checks if a path exists and is a directory.
     */
    public static boolean isDirectory(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        try {
            File localFile = new File(path);
            if (localFile.isDirectory()) return true;

            if (hasFullAccess()) {
                String res = ShizukuExecutor.executeShizukuCommand("test -d '" + path + "' && echo IS_DIR");
                return res != null && res.contains("IS_DIR");
            } else {
                String res = CommandExecutor.executeSystemCommand("test -d '" + path + "' && echo IS_DIR");
                return res != null && res.contains("IS_DIR");
            }
        } catch (Throwable t) {
            Log.w(TAG, "isDirectory exception for " + path, t);
            return false;
        }
    }

    /**
     * Creates all required parent directories for a path (mkdir -p).
     */
    public static boolean ensureParentDirectory(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash <= 0) return true;

        String parentDir = path.substring(0, lastSlash);
        return makeDirectory(parentDir);
    }

    /**
     * Creates a directory (and any necessary parent directories).
     */
    public static boolean makeDirectory(String dirPath) {
        if (dirPath == null || dirPath.trim().isEmpty()) return false;
        try {
            File dir = new File(dirPath);
            if (dir.exists() && dir.isDirectory()) return true;

            String cmd = "mkdir -p '" + dirPath + "' && chmod 777 '" + dirPath + "'";
            if (hasFullAccess()) {
                String res = ShizukuExecutor.executeShizukuCommand(cmd);
                return res != null && !res.toLowerCase().contains("error");
            } else {
                dir.mkdirs();
                CommandExecutor.executeSystemCommand(cmd);
                return dir.exists();
            }
        } catch (Throwable t) {
            Log.w(TAG, "makeDirectory exception for " + dirPath, t);
            return false;
        }
    }

    /**
     * Writes content to a file at the given path.
     * Uses Base64 stream decoding to guarantee zero shell-escaping errors and safe character handling.
     * Automatically sets permissions (default 666 / 777).
     */
    public static FileOpResult writeFile(String path, String content, String chmodMode) {
        if (path == null || path.trim().isEmpty()) {
            return FileOpResult.fail(path, "Invalid file path");
        }
        if (content == null) content = "";

        try {
            ensureParentDirectory(path);

            String mode = (chmodMode != null && !chmodMode.isEmpty()) ? chmodMode : "666";
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            String b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);

            String writeCmd = "echo '" + b64 + "' | base64 -d > '" + path + "' && chmod " + mode + " '" + path + "'";

            if (hasFullAccess()) {
                String res = ShizukuExecutor.executeShizukuCommand(writeCmd);
                boolean ok = res != null && !res.toLowerCase().contains("error");
                if (ok) {
                    Log.d(TAG, "writeFile via Shizuku SUCCESS: " + path + " (" + bytes.length + " bytes, mode=" + mode + ")");
                    return FileOpResult.ok(path, "Written " + bytes.length + " bytes via Shizuku");
                } else {
                    Log.w(TAG, "writeFile via Shizuku FAILED: " + path + " -> " + res);
                    return FileOpResult.fail(path, "Shizuku write failed: " + res);
                }
            } else {
                CommandExecutor.executeSystemCommand(writeCmd);
                Log.d(TAG, "writeFile via CommandExecutor: " + path);
                return FileOpResult.ok(path, "Written via standard shell");
            }
        } catch (Throwable t) {
            Log.e(TAG, "writeFile exception for " + path, t);
            return FileOpResult.fail(path, "Exception: " + t.getMessage());
        }
    }

    /**
     * Overload with default mode 666.
     */
    public static FileOpResult writeFile(String path, String content) {
        return writeFile(path, content, "666");
    }

    /**
     * Writes content to a file atomically via a temporary file and atomic rename (mv -f)
     * to eliminate file truncation or partial writes during active game startup.
     */
    public static FileOpResult writeFileAtomic(String path, String content, String chmodMode) {
        if (path == null || path.trim().isEmpty()) {
            return FileOpResult.fail(path, "Invalid file path");
        }
        String tmpPath = path + ".tmp." + System.currentTimeMillis();
        FileOpResult tmpRes = writeFile(tmpPath, content, chmodMode);
        if (!tmpRes.success) {
            return tmpRes;
        }

        String moveCmd = "mv -f '" + tmpPath + "' '" + path + "' && chmod " + (chmodMode != null ? chmodMode : "666") + " '" + path + "'";
        if (hasFullAccess()) {
            String res = ShizukuExecutor.executeShizukuCommand(moveCmd);
            boolean ok = res != null && !res.toLowerCase().contains("error");
            return ok ? FileOpResult.ok(path, "Written atomically via Shizuku") : FileOpResult.fail(path, "Atomic rename failed: " + res);
        } else {
            CommandExecutor.executeSystemCommand(moveCmd);
            return FileOpResult.ok(path, "Written atomically via shell");
        }
    }

    public static FileOpResult writeFileAtomic(String path, String content) {
        return writeFileAtomic(path, content, "666");
    }

    /**
     * Recursively copies a directory from srcDir to destDir with full permissions.
     */
    public static boolean copyDirectory(String srcDir, String destDir) {
        if (srcDir == null || destDir == null) return false;
        try {
            ensureParentDirectory(destDir);
            String cmd = "cp -rf '" + srcDir + "/.' '" + destDir + "/' && chmod -R 777 '" + destDir + "'";
            if (hasFullAccess()) {
                String res = ShizukuExecutor.executeShizukuCommand(cmd);
                return res != null && !res.toLowerCase().contains("error");
            } else {
                String res = CommandExecutor.executeSystemCommand(cmd);
                return res != null && !res.toLowerCase().contains("error");
            }
        } catch (Throwable t) {
            Log.w(TAG, "copyDirectory exception: " + srcDir + " -> " + destDir, t);
            return false;
        }
    }

    /**
     * Reads the entire contents of a file at the given path.
     */
    public static String readFile(String path) {
        if (path == null || path.trim().isEmpty()) return "";

        try {
            if (hasFullAccess()) {
                String res = ShizukuExecutor.executeShizukuCommand("cat '" + path + "'");
                if (res != null && !res.startsWith("ERROR:")) {
                    return res;
                }
            } else {
                String res = CommandExecutor.executeSystemCommand("cat '" + path + "'");
                if (res != null && !res.startsWith("ERROR:")) {
                    return res;
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "readFile exception for " + path, t);
        }
        return "";
    }

    /**
     * Copies a file from src to dest with permission preservation.
     */
    public static boolean copyFile(String src, String dest) {
        if (src == null || dest == null) return false;
        try {
            ensureParentDirectory(dest);
            String cmd = "cp -f '" + src + "' '" + dest + "' && chmod 666 '" + dest + "'";
            if (hasFullAccess()) {
                String res = ShizukuExecutor.executeShizukuCommand(cmd);
                return res != null && !res.toLowerCase().contains("error");
            } else {
                String res = CommandExecutor.executeSystemCommand(cmd);
                return res != null && !res.toLowerCase().contains("error");
            }
        } catch (Throwable t) {
            Log.w(TAG, "copyFile exception: " + src + " -> " + dest, t);
            return false;
        }
    }

    /**
     * Recursively deletes a file or directory.
     */
    public static boolean deletePath(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        try {
            String cmd = "rm -rf '" + path + "'";
            if (hasFullAccess()) {
                String res = ShizukuExecutor.executeShizukuCommand(cmd);
                return res != null && !res.toLowerCase().contains("error");
            } else {
                String res = CommandExecutor.executeSystemCommand(cmd);
                return res != null && !res.toLowerCase().contains("error");
            }
        } catch (Throwable t) {
            Log.w(TAG, "deletePath exception for " + path, t);
            return false;
        }
    }

    /**
     * Applies file permissions (chmod).
     */
    public static boolean setPermissions(String path, String mode) {
        if (path == null || mode == null) return false;
        try {
            String cmd = "chmod " + mode + " '" + path + "'";
            if (hasFullAccess()) {
                String res = ShizukuExecutor.executeShizukuCommand(cmd);
                return res != null && !res.toLowerCase().contains("error");
            } else {
                String res = CommandExecutor.executeSystemCommand(cmd);
                return res != null && !res.toLowerCase().contains("error");
            }
        } catch (Throwable t) {
            Log.w(TAG, "setPermissions exception for " + path, t);
            return false;
        }
    }

    /**
     * Lists directory entries at path.
     */
    public static List<String> listDirectory(String dirPath) {
        List<String> list = new ArrayList<>();
        if (dirPath == null || dirPath.trim().isEmpty()) return list;

        try {
            String cmd = "ls -1 '" + dirPath + "'";
            String res;
            if (hasFullAccess()) {
                res = ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                res = CommandExecutor.executeSystemCommand(cmd);
            }

            if (res != null && !res.startsWith("ERROR:")) {
                String[] lines = res.split("\n");
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        list.add(trimmed);
                    }
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "listDirectory exception for " + dirPath, t);
        }
        return list;
    }

    /**
     * Grants all storage, data, and system file permissions to the launcher and all games.
     */
    public static void grantAllStoragePermissions(Context context) {
        if (context == null || !hasFullAccess()) return;

        String myPkg = context.getPackageName();
        ShizukuExecutor.executeShizukuCommand("pm grant " + myPkg + " android.permission.MANAGE_EXTERNAL_STORAGE");
        ShizukuExecutor.executeShizukuCommand("pm grant " + myPkg + " android.permission.READ_EXTERNAL_STORAGE");
        ShizukuExecutor.executeShizukuCommand("pm grant " + myPkg + " android.permission.WRITE_EXTERNAL_STORAGE");
        ShizukuExecutor.executeShizukuCommand("cmd appops set " + myPkg + " MANAGE_EXTERNAL_STORAGE allow");
        ShizukuExecutor.executeShizukuCommand("cmd appops set " + myPkg + " READ_EXTERNAL_STORAGE allow");
        ShizukuExecutor.executeShizukuCommand("cmd appops set " + myPkg + " WRITE_EXTERNAL_STORAGE allow");
        ShizukuExecutor.executeShizukuCommand("cmd appops set " + myPkg + " ACCESS_RESTRICTED_SETTINGS allow");

        // Unlock permissions for Android 13-16 Media Permissions
        ShizukuExecutor.executeShizukuCommand("pm grant " + myPkg + " android.permission.READ_MEDIA_IMAGES");
        ShizukuExecutor.executeShizukuCommand("pm grant " + myPkg + " android.permission.READ_MEDIA_VIDEO");
        ShizukuExecutor.executeShizukuCommand("pm grant " + myPkg + " android.permission.READ_MEDIA_AUDIO");
    }
}
