package com.gamebooster.app.cleaner.scanner;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ScanFilter {

    // Critical extensions that must NEVER be deleted
    private static final Set<String> PROTECTED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "sav", "save", "dat", "db", "sqlite", "sqlite3",
            "jpg", "jpeg", "png", "heic", "dng", "webp",
            "mp4", "mkv", "mov", "avi", "flv", "webm",
            "mp3", "flac", "wav", "m4a", "ogg",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt",
            "obb", "key", "pem", "p12", "keystore", "jks"
    ));

    // Safe extensions specifically recognized as disposable junk
    private static final Set<String> JUNK_EXTENSIONS = new HashSet<>(Arrays.asList(
            "tmp", "temp", "log", "bak", "old", "dmp", "dump", "crash",
            "thumb", "cache", "crdownload", "part"
    ));

    // Protected directories that should never be traversed or deleted
    private static final Set<String> PROTECTED_PATH_SEGMENTS = new HashSet<>(Arrays.asList(
            "/system", "/vendor", "/product", "/system_ext", "/apex",
            "/dcim/camera", "/pictures/screenshots", "/documents", "/music",
            "/android/obb"
    ));

    public static boolean isSafeToScan(File file) {
        if (file == null) return false;
        String path = file.getAbsolutePath().toLowerCase(Locale.ROOT);

        for (String protectedSegment : PROTECTED_PATH_SEGMENTS) {
            if (path.contains(protectedSegment)) {
                // If it's specifically .thumbnails inside DCIM/Pictures, allow scanning only that folder
                if (path.contains("/.thumbnails")) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    public static boolean isDisposableJunkFile(File file) {
        if (file == null || !file.exists() || file.isDirectory()) return false;
        String name = file.getName().toLowerCase(Locale.ROOT);
        String path = file.getAbsolutePath().toLowerCase(Locale.ROOT);

        // Check if explicitly protected
        String ext = getFileExtension(name);
        if (PROTECTED_EXTENSIONS.contains(ext)) {
            return false;
        }

        // Check if inside standard cache folders
        if (path.contains("/cache/") || path.contains("/code_cache/") || path.contains("/.thumbnails/")) {
            return true;
        }

        // Check junk extensions
        if (JUNK_EXTENSIONS.contains(ext)) {
            return true;
        }

        // Stale APK in Downloads or temp
        if (name.endsWith(".apk") && (path.contains("/download") || path.contains("/tmp") || path.contains("/temp"))) {
            return true;
        }

        return false;
    }

    public static boolean isThumbnailFileOrDir(File file) {
        if (file == null) return false;
        String path = file.getAbsolutePath().toLowerCase(Locale.ROOT);
        return path.contains("/.thumbnails") || path.contains("/.thumb") || file.getName().startsWith(".thumb");
    }

    public static boolean isObsoleteApkFile(File file) {
        if (file == null || !file.isFile()) return false;
        String name = file.getName().toLowerCase(Locale.ROOT);
        String path = file.getAbsolutePath().toLowerCase(Locale.ROOT);
        return name.endsWith(".apk") && (path.contains("/download") || path.contains("/temp") || path.contains("/tmp"));
    }

    public static boolean isGameLogOrResidual(File file) {
        if (file == null || !file.isFile()) return false;
        String name = file.getName().toLowerCase(Locale.ROOT);
        String path = file.getAbsolutePath().toLowerCase(Locale.ROOT);

        boolean inGameOrDataPath = path.contains("/android/data/") || path.contains("/data/data/") || path.contains("/games/");
        if (!inGameOrDataPath) return false;

        String ext = getFileExtension(name);
        return ext.equals("log") || ext.equals("tmp") || ext.equals("dmp") || ext.equals("crash")
                || name.startsWith("logcat_") || name.startsWith("anr_") || name.startsWith("crash_");
    }

    public static String getFileExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        }
        return "";
    }
}
