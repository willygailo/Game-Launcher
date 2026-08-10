package com.gamebooster.app.shizuku;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;

/**
 * ShizukuFileBridge manages file operations in restricted locations (/sdcard/Android/data/ and /data/data/)
 * using Shizuku ADB shell privileges (uid 2000).
 *
 * Provides:
 *  - Automatic directory creation
 *  - Safety backup (.bak) generation before patching
 *  - Safe atomic file writes
 *  - Read-only locking (chmod 444 / chmod 644) to prevent games from resetting config files on boot
 */
public class ShizukuFileBridge {

    private static final String TAG = "ShizukuFileBridge";

    public static void ensureParentDir(String filePath) {
        if (filePath == null) return;
        int lastSlash = filePath.lastIndexOf('/');
        if (lastSlash > 0) {
            String parentDir = filePath.substring(0, lastSlash);
            String cmd = "mkdir -p " + parentDir;
            execute(cmd);
        }
    }

    public static void createBackup(String filePath) {
        if (filePath == null) return;
        String backupPath = filePath + ".bak";
        String cmd = "test -f " + filePath + " && test ! -f " + backupPath + " && cp " + filePath + " " + backupPath;
        execute(cmd);
    }

    public static boolean writeContent(String filePath, String content, boolean makeReadOnly) {
        if (filePath == null || content == null) return false;

        ensureParentDir(filePath);
        createBackup(filePath);

        // Unlock file permissions first in case it was previously locked
        execute("chmod 666 " + filePath);

        String escaped = content.replace("'", "'\\''");
        String writeCmd = "printf '" + escaped + "' > " + filePath;
        String res = execute(writeCmd);

        if (makeReadOnly) {
            // Read-only lock to prevent game client overwriting on startup
            execute("chmod 444 " + filePath);
        } else {
            execute("chmod 644 " + filePath);
        }

        Log.d(TAG, "writeContent to " + filePath + " readOnly=" + makeReadOnly + " -> " + res);
        return true;
    }

    public static boolean updateIniKeys(String filePath, String[] keys, String[] values, String sectionHeader) {
        if (filePath == null || keys == null || values == null || keys.length != values.length) return false;

        ensureParentDir(filePath);
        createBackup(filePath);

        execute("chmod 666 " + filePath);

        String checkCmd = "test -f " + filePath + " && echo EXISTS";
        String checkRes = execute(checkCmd);

        if (!checkRes.contains("EXISTS")) {
            StringBuilder sb = new StringBuilder();
            if (sectionHeader != null && !sectionHeader.isEmpty()) {
                sb.append(sectionHeader).append("\n");
            }
            for (int i = 0; i < keys.length; i++) {
                sb.append(keys[i]).append("=").append(values[i]).append("\n");
            }
            writeContent(filePath, sb.toString(), false);
        } else {
            if (sectionHeader != null && !sectionHeader.isEmpty()) {
                execute("grep -qF '" + sectionHeader + "' " + filePath + " || echo '" + sectionHeader + "' >> " + filePath);
            }
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                String v = values[i];
                String cmd = "grep -qF '" + k + "=' " + filePath +
                        " && sed -i 's/^" + k + "=.*/" + k + "=" + v + "/' " + filePath +
                        " || echo '" + k + "=" + v + "' >> " + filePath;
                execute(cmd);
            }
        }
        execute("chmod 644 " + filePath);
        return true;
    }

    public static boolean recursiveChmod(String dirPath, String mode) {
        if (dirPath == null || mode == null) return false;
        String cmd = "chmod -R " + mode + " " + dirPath;
        String res = execute(cmd);
        Log.d(TAG, "recursiveChmod " + dirPath + " mode=" + mode + " -> " + res);
        return true;
    }

    public static boolean forceRemove(String targetPath) {
        if (targetPath == null) return false;
        String cmd = "rm -rf " + targetPath;
        String res = execute(cmd);
        Log.d(TAG, "forceRemove " + targetPath + " -> " + res);
        return true;
    }

    public static boolean copyDirectory(String sourceDir, String destDir) {
        if (sourceDir == null || destDir == null) return false;
        String cmd = "mkdir -p " + destDir + " && cp -r " + sourceDir + "/* " + destDir + "/";
        String res = execute(cmd);
        Log.d(TAG, "copyDirectory from " + sourceDir + " to " + destDir + " -> " + res);
        return true;
    }

    private static String execute(String command) {
        if (ShizukuExecutor.hasShizukuPermission()) {
            return ShizukuExecutor.executeShizukuCommand(command);
        } else {
            return CommandExecutor.executeSystemCommand(command);
        }
    }
}
