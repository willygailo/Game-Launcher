package com.gamebooster.app.shizuku;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.List;

/**
 * GameStorageEngine — Direct access, config injector, and backup engine
 * for protected Android directories (/sdcard/Android/data/, /sdcard/Android/obb/, /data/data/<pkg>/).
 */
public class GameStorageEngine {

    private static final String TAG = "GameStorageEngine";

    public static final String SDCARD_ANDROID_DATA = "/sdcard/Android/data";
    public static final String SDCARD_ANDROID_OBB = "/sdcard/Android/obb";
    public static final String DATA_APP_DIR = "/data/data";
    public static final String TEMP_SWAP_DIR = "/data/local/tmp";

    /**
     * Gets the full path to a game's /Android/data directory.
     */
    public static String getGameDataDirectory(String packageName) {
        return SDCARD_ANDROID_DATA + "/" + packageName;
    }

    /**
     * Gets the full path to a game's /Android/obb directory.
     */
    public static String getGameObbDirectory(String packageName) {
        return SDCARD_ANDROID_OBB + "/" + packageName;
    }

    /**
     * Gets the full path to a game's private /data/data directory.
     */
    public static String getGamePrivateDirectory(String packageName) {
        return DATA_APP_DIR + "/" + packageName;
    }

    /**
     * Checks if a game has data files stored on disk.
     */
    public static boolean hasGameData(String packageName) {
        String dataPath = getGameDataDirectory(packageName);
        return ShizukuFileManager.fileExists(dataPath);
    }

    /**
     * Creates a safe backup (.bak) of a configuration file before modifying it.
     */
    public static boolean backupFile(String targetFilePath) {
        if (!ShizukuFileManager.fileExists(targetFilePath)) {
            Log.w(TAG, "Cannot backup non-existent file: " + targetFilePath);
            return false;
        }

        String backupPath = targetFilePath + ".bak";
        if (ShizukuFileManager.fileExists(backupPath)) {
            Log.d(TAG, "Backup already exists: " + backupPath);
            return true;
        }

        return ShizukuFileManager.copyFile(targetFilePath, backupPath);
    }

    /**
     * Restores a configuration file from its .bak backup.
     */
    public static boolean restoreFromBackup(String targetFilePath) {
        String backupPath = targetFilePath + ".bak";
        if (!ShizukuFileManager.fileExists(backupPath)) {
            Log.w(TAG, "No backup file found at: " + backupPath);
            return false;
        }

        boolean success = ShizukuFileManager.copyFile(backupPath, targetFilePath);
        if (success) {
            fixFilePermissions(targetFilePath);
        }
        return success;
    }

    /**
     * Writes or replaces a game configuration file atomically and fixes permissions.
     */
    public static boolean injectConfig(String targetFilePath, String configContent, boolean autoBackup) {
        if (targetFilePath == null || configContent == null) return false;

        if (autoBackup && ShizukuFileManager.fileExists(targetFilePath)) {
            backupFile(targetFilePath);
        }

        ShizukuFileManager.FileOpResult res = ShizukuFileManager.writeFileAtomic(targetFilePath, configContent, "777");
        if (res.success) {
            fixFilePermissions(targetFilePath);
            Log.i(TAG, "Config successfully injected: " + targetFilePath);
            return true;
        } else {
            Log.e(TAG, "Failed to inject config: " + res.message);
            return false;
        }
    }

    /**
     * Edits an existing game configuration file in-place using search & replace.
     */
    public static boolean editGameConfig(String targetFilePath, String search, String replace, boolean autoBackup) {
        if (targetFilePath == null || search == null || replace == null) return false;
        if (autoBackup && ShizukuFileManager.fileExists(targetFilePath)) {
            backupFile(targetFilePath);
        }
        ShizukuFileManager.FileOpResult res = ShizukuFileManager.editFile(targetFilePath, search, replace);
        if (res.success) {
            fixFilePermissions(targetFilePath);
            Log.i(TAG, "Config successfully edited: " + targetFilePath);
            return true;
        }
        return false;
    }

    /**
     * Adds / creates a new game configuration file.
     */
    public static boolean addGameConfigFile(String targetFilePath, String initialContent) {
        ShizukuFileManager.FileOpResult res = ShizukuFileManager.addFile(targetFilePath, initialContent);
        if (res.success) {
            fixFilePermissions(targetFilePath);
            Log.i(TAG, "New config file added: " + targetFilePath);
            return true;
        }
        return false;
    }

    /**
     * Deletes a game configuration file or directory.
     */
    public static boolean deleteGameConfigFile(String targetFilePath) {
        ShizukuFileManager.FileOpResult res = ShizukuFileManager.deleteFile(targetFilePath);
        if (res.success) {
            Log.i(TAG, "Config file deleted: " + targetFilePath);
            return true;
        }
        return false;
    }

    /**
     * Uploads / imports a local configuration file into a protected game folder.
     */
    public static boolean uploadGameConfigFile(String localFilePath, String targetGameConfigPath) {
        ShizukuFileManager.FileOpResult res = ShizukuFileManager.uploadFile(localFilePath, targetGameConfigPath);
        if (res.success) {
            fixFilePermissions(targetGameConfigPath);
            Log.i(TAG, "Uploaded config file to: " + targetGameConfigPath);
            return true;
        }
        return false;
    }

    /**
     * Extracts / downloads a protected game config file to local app storage or cache.
     */
    public static boolean extractGameConfigFile(String targetGameConfigPath, String localDestPath) {
        ShizukuFileManager.FileOpResult res = ShizukuFileManager.downloadFile(targetGameConfigPath, localDestPath);
        return res.success;
    }

    /**
     * Cleans temporary game files, cached shaders, and junk logs to free up memory and storage.
     */
    public static boolean cleanGameTempFiles(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String cacheDir = getGameDataDirectory(packageName) + "/cache";
        String privateCacheDir = getGamePrivateDirectory(packageName) + "/cache";
        String codeCache = getGamePrivateDirectory(packageName) + "/code_cache";

        ShizukuFileManager.deletePath(cacheDir);
        ShizukuFileManager.deletePath(privateCacheDir);
        ShizukuFileManager.deletePath(codeCache);
        Log.i(TAG, "Cleaned temp caches for " + packageName);
        return true;
    }

    /**
     * Reads a protected game configuration or log file.
     */
    public static String readConfig(String targetFilePath) {
        return ShizukuFileManager.readFile(targetFilePath);
    }

    /**
     * Ensures game processes have full read/write permissions on modified files.
     */
    public static void fixFilePermissions(String filePath) {
        if (filePath == null) return;
        ShizukuFileManager.setPermissions(filePath, "777");
        ShizukuExecutor.executeShizukuCommand("chmod 777 '" + filePath + "' 2>/dev/null");
    }

    /**
     * Fixes recursive permissions on an entire game data folder (/Android/data/<pkg>).
     */
    public static void fixGameDirectoryPermissions(String packageName) {
        if (packageName == null) return;
        String dataDir = getGameDataDirectory(packageName);
        String obbDir = getGameObbDirectory(packageName);

        ShizukuExecutor.executeShizukuCommand("chmod -R 777 '" + dataDir + "' 2>/dev/null");
        ShizukuExecutor.executeShizukuCommand("chmod -R 777 '" + obbDir + "' 2>/dev/null");
    }

    /**
     * Lists files inside a protected game directory.
     */
    public static List<String> listGameFiles(String targetDirPath) {
        return ShizukuFileManager.listDirectory(targetDirPath);
    }
}
