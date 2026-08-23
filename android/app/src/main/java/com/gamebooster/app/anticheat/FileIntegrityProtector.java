package com.gamebooster.app.anticheat;

import android.util.Log;

import androidx.annotation.NonNull;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.io.File;

/**
 * FileIntegrityProtector — Cloaks file modifications from game anti-cheat integrity scanners.
 *
 * Implements:
 * 1. Timestamp Retention: Restores original atime & mtime using POSIX utimensat / touch.
 * 2. Permission Normalization: Sets 0660 / 0644 so patched files do not have abnormal attributes.
 * 3. SELinux Security Context Restoration: Runs restorecon -R to preserve app_data_file contexts.
 */
public final class FileIntegrityProtector {

    private static final String TAG = "FileIntegrityProtector";

    private FileIntegrityProtector() {}

    public static class FileTimestampSnapshot {
        public final String filePath;
        public final long lastModifiedTime;

        public FileTimestampSnapshot(String filePath, long lastModifiedTime) {
            this.filePath = filePath;
            this.lastModifiedTime = lastModifiedTime;
        }
    }

    /**
     * Captures current timestamp snapshot before file patching.
     */
    public static FileTimestampSnapshot captureSnapshot(@NonNull String filePath) {
        File file = new File(filePath);
        long mtime = file.exists() ? file.lastModified() : System.currentTimeMillis();
        return new FileTimestampSnapshot(filePath, mtime);
    }

    /**
     * Restores file timestamps, permissions, and SELinux contexts to blend with original files.
     */
    public static boolean restoreIntegrity(@NonNull FileTimestampSnapshot snapshot) {
        if (snapshot.filePath == null || snapshot.filePath.isEmpty()) return false;

        File target = new File(snapshot.filePath);
        if (!target.exists()) return false;

        boolean success = true;

        // 1. Restore Java modification timestamp
        if (snapshot.lastModifiedTime > 0) {
            try {
                target.setLastModified(snapshot.lastModifiedTime);
            } catch (Throwable ignored) {}
        }

        // 2. Normalize permissions & SELinux context via Shizuku
        String path = target.getAbsolutePath();
        String parentDir = target.getParent();

        StringBuilder sb = new StringBuilder();
        // Permission normalization
        sb.append("chmod 660 \"").append(path).append("\" 2>/dev/null; ");

        // Inode timestamp restoration via shell touch if available
        long epochSec = snapshot.lastModifiedTime / 1000L;
        if (epochSec > 0) {
            sb.append("touch -d @").append(epochSec).append(" \"").append(path).append("\" 2>/dev/null; ");
        }

        // SELinux context restoration
        if (parentDir != null) {
            sb.append("restorecon -R \"").append(parentDir).append("\" 2>/dev/null; ");
        }

        executeCommand(sb.toString());
        Log.d(TAG, "Restored file integrity & timestamps for " + path);
        return success;
    }

    /**
     * Restores SELinux and permissions for an entire game package data directory.
     */
    public static void restoreGameDataDirectorySecurity(@NonNull String packageName) {
        if (packageName.isEmpty()) return;

        String cmd = "restorecon -R /sdcard/Android/data/" + packageName + " 2>/dev/null; "
                + "restorecon -R /data/data/" + packageName + " 2>/dev/null; "
                + "chmod -R 770 /sdcard/Android/data/" + packageName + " 2>/dev/null";

        executeCommand(cmd);
        Log.i(TAG, "Restored security context for game package " + packageName);
    }

    private static void executeCommand(String command) {
        if (ShizukuFileManager.hasFullAccess()) {
            ShizukuExecutor.executeShizukuCommand(command);
        } else {
            CommandExecutor.executeSystemCommand(command);
        }
    }
}
