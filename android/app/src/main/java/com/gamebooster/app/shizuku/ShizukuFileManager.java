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

            if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                return ShizukuUserServiceConnector.getInstance().fileExists(path);
            }

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

            if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                if (ShizukuUserServiceConnector.getInstance().makeDirectories(dirPath)) {
                    return true;
                }
            }

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
     * Uses direct AIDL I/O or Base64 stream decoding to guarantee zero shell-escaping errors.
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

            // 1. Direct AIDL Native I/O (High Speed, zero subshell)
            if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                boolean written = ShizukuUserServiceConnector.getInstance().writeDirectFile(path, content, mode);
                if (written) {
                    Log.d(TAG, "writeFile via AIDL Native I/O SUCCESS: " + path);
                    return FileOpResult.ok(path, "Written directly via Shizuku AIDL");
                }
            }

            // 2. Base64 Shell Pipeline Fallback
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
     * Reads the exact raw bytes of a file at the given path via a Base64 round-trip
     * (byte-exact for binary config files like .sav / .dat, unlike {@link #readFile(String)}).
     */
    public static byte[] readFileBytes(String path) {
        if (path == null || path.trim().isEmpty()) return new byte[0];

        try {
            String cmd = "base64 -w0 '" + path + "' 2>/dev/null";
            String res;
            if (hasFullAccess()) {
                res = ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                res = CommandExecutor.executeSystemCommand(cmd);
            }
            if (res != null && !res.startsWith("ERROR:") && !res.trim().isEmpty()) {
                return Base64.decode(res.trim(), Base64.DEFAULT);
            }
        } catch (Throwable t) {
            Log.w(TAG, "readFileBytes exception for " + path, t);
        }
        return new byte[0];
    }

    /**
     * Edits an existing file in-place by searching for a pattern and replacing it (sed / regex).
     */
    public static FileOpResult editFile(String path, String searchPattern, String replacement) {
        if (path == null || path.trim().isEmpty()) {
            return FileOpResult.fail(path, "Invalid file path");
        }
        if (!fileExists(path)) {
            return FileOpResult.fail(path, "File does not exist: " + path);
        }

        try {
            // Escape single quotes for shell
            String safeSearch = searchPattern.replace("'", "'\\''");
            String safeReplace = replacement.replace("'", "'\\''");
            String sedCmd = "sed -i 's/" + safeSearch + "/" + safeReplace + "/g' '" + path + "' && chmod 666 '" + path + "'";

            if (hasFullAccess()) {
                String res = ShizukuExecutor.executeShizukuCommand(sedCmd);
                boolean ok = res != null && !res.toLowerCase().contains("error");
                return ok ? FileOpResult.ok(path, "File edited successfully via Shizuku sed")
                          : FileOpResult.fail(path, "Sed edit failed: " + res);
            } else {
                CommandExecutor.executeSystemCommand(sedCmd);
                return FileOpResult.ok(path, "File edited via shell sed");
            }
        } catch (Throwable t) {
            Log.e(TAG, "editFile exception for " + path, t);
            return FileOpResult.fail(path, "Exception: " + t.getMessage());
        }
    }

    /**
     * Edits an entire file with completely new content atomically.
     */
    public static FileOpResult editFileContent(String path, String newContent) {
        return writeFileAtomic(path, newContent, "666");
    }

    /**
     * Adds / creates a new file at path with initial content and sets permissions.
     */
    public static FileOpResult addFile(String path, String initialContent) {
        ensureParentDirectory(path);
        return writeFileAtomic(path, initialContent != null ? initialContent : "", "666");
    }

    /**
     * Adds / creates an empty file (touch) at the given path.
     */
    public static FileOpResult touchFile(String path) {
        if (path == null || path.trim().isEmpty()) {
            return FileOpResult.fail(path, "Invalid file path");
        }
        ensureParentDirectory(path);
        String cmd = "touch '" + path + "' && chmod 666 '" + path + "'";
        if (hasFullAccess()) {
            String res = ShizukuExecutor.executeShizukuCommand(cmd);
            return res != null && !res.toLowerCase().contains("error")
                    ? FileOpResult.ok(path, "File created (touched)")
                    : FileOpResult.fail(path, "Touch failed: " + res);
        } else {
            CommandExecutor.executeSystemCommand(cmd);
            return FileOpResult.ok(path, "File created (touched)");
        }
    }

    /**
     * Adds / creates a new directory (and all parent directories) at the given path.
     */
    public static FileOpResult addDirectory(String dirPath) {
        boolean ok = makeDirectory(dirPath);
        return ok ? FileOpResult.ok(dirPath, "Directory created: " + dirPath)
                  : FileOpResult.fail(dirPath, "Failed to create directory: " + dirPath);
    }

    /**
     * Uploads / imports a local file to a protected destination path (e.g. into /data/data/ or /sdcard/Android/data/).
     */
    public static FileOpResult uploadFile(String localSourcePath, String targetProtectedPath) {
        if (localSourcePath == null || targetProtectedPath == null) {
            return FileOpResult.fail(targetProtectedPath, "Source or destination path is null");
        }
        ensureParentDirectory(targetProtectedPath);
        boolean ok = copyFile(localSourcePath, targetProtectedPath);
        return ok ? FileOpResult.ok(targetProtectedPath, "Uploaded file from " + localSourcePath + " to " + targetProtectedPath)
                  : FileOpResult.fail(targetProtectedPath, "Failed to upload file to " + targetProtectedPath);
    }

    /**
     * Uploads raw bytes to a protected destination path via Base64 stream.
     */
    public static FileOpResult uploadBytes(String targetProtectedPath, byte[] data, String chmodMode) {
        if (targetProtectedPath == null || data == null) {
            return FileOpResult.fail(targetProtectedPath, "Path or data is null");
        }
        ensureParentDirectory(targetProtectedPath);
        String b64 = Base64.encodeToString(data, Base64.NO_WRAP);
        String mode = chmodMode != null ? chmodMode : "666";
        String cmd = "echo '" + b64 + "' | base64 -d > '" + targetProtectedPath + "' && chmod " + mode + " '" + targetProtectedPath + "'";

        if (hasFullAccess()) {
            String res = ShizukuExecutor.executeShizukuCommand(cmd);
            boolean ok = res != null && !res.toLowerCase().contains("error");
            return ok ? FileOpResult.ok(targetProtectedPath, "Uploaded " + data.length + " bytes to " + targetProtectedPath)
                      : FileOpResult.fail(targetProtectedPath, "Upload failed: " + res);
        } else {
            CommandExecutor.executeSystemCommand(cmd);
            return FileOpResult.ok(targetProtectedPath, "Uploaded bytes via shell");
        }
    }

    /**
     * Downloads / extracts a protected file to a local destination directory or cache.
     */
    public static FileOpResult downloadFile(String protectedSourcePath, String localDestPath) {
        if (protectedSourcePath == null || localDestPath == null) {
            return FileOpResult.fail(localDestPath, "Path is null");
        }
        ensureParentDirectory(localDestPath);
        boolean ok = copyFile(protectedSourcePath, localDestPath);
        return ok ? FileOpResult.ok(localDestPath, "Downloaded " + protectedSourcePath + " to " + localDestPath)
                  : FileOpResult.fail(localDestPath, "Failed to download " + protectedSourcePath);
    }

    /**
     * Appends text to the end of a file.
     */
    public static FileOpResult appendToFile(String path, String textToAppend) {
        if (path == null || textToAppend == null) {
            return FileOpResult.fail(path, "Invalid path or text");
        }
        ensureParentDirectory(path);
        String b64 = Base64.encodeToString(textToAppend.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        String cmd = "echo '" + b64 + "' | base64 -d >> '" + path + "' && chmod 666 '" + path + "'";

        if (hasFullAccess()) {
            String res = ShizukuExecutor.executeShizukuCommand(cmd);
            return res != null && !res.toLowerCase().contains("error")
                    ? FileOpResult.ok(path, "Appended text to " + path)
                    : FileOpResult.fail(path, "Append failed: " + res);
        } else {
            CommandExecutor.executeSystemCommand(cmd);
            return FileOpResult.ok(path, "Appended text via shell");
        }
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
     * Recursively deletes a file or directory (delete operation).
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
     * Deletes a specific file.
     */
    public static FileOpResult deleteFile(String path) {
        boolean ok = deletePath(path);
        return ok ? FileOpResult.ok(path, "File deleted: " + path)
                  : FileOpResult.fail(path, "Failed to delete file: " + path);
    }

    /**
     * Deletes an entire directory recursively.
     */
    public static FileOpResult deleteDirectory(String dirPath) {
        boolean ok = deletePath(dirPath);
        return ok ? FileOpResult.ok(dirPath, "Directory deleted: " + dirPath)
                  : FileOpResult.fail(dirPath, "Failed to delete directory: " + dirPath);
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
        ShizukuPermissionEnforcer.enforceAllPermissions(context);
        com.gamebooster.app.config.GameConfigStorageAccessEngine.grantGlobalStorageAccess(context);
    }

    /**
     * Grants full internal and external storage path combo access for a specific game package.
     */
    public static boolean grantFullStoragePathAccess(Context context, String packageName) {
        return com.gamebooster.app.config.GameConfigStorageAccessEngine.grantAllPathsAccess(context, packageName);
    }
}
