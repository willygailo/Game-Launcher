package com.gamebooster.app.cleaner.scanner;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * ScanFilter — High-precision safety filter and junk classifier.
 *
 * Prevents accidental deletion of essential user documents, photos, media, game saves,
 * and databases while identifying genuine disposable caches, temporary files, diagnostic logs,
 * crash tombstones, thumbnails, and residual dumps across Android 13, 14, 15, and 16.
 */
public class ScanFilter {

    // Critical extensions that must NEVER be deleted
    private static final Set<String> PROTECTED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "sav", "save", "dat", "db", "sqlite", "sqlite3", "realm",
            "jpg", "jpeg", "png", "heic", "dng", "webp", "gif", "svg",
            "mp4", "mkv", "mov", "avi", "flv", "webm", "3gp",
            "mp3", "flac", "wav", "m4a", "ogg", "aac", "opus",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "csv",
            "obb", "key", "pem", "p12", "keystore", "jks", "crt", "cer", "json", "xml",
            "ini", "cfg", "conf", "lua", "pak", "bundle", "unity3d", "res", "properties", "yaml", "yml",
            "nomedia"
    ));

    // Safe extensions specifically recognized as disposable junk
    private static final Set<String> JUNK_EXTENSIONS = new HashSet<>(Arrays.asList(
            "tmp", "temp", "log", "bak", "old", "dmp", "dump", "crash",
            "thumb", "cache", "crdownload", "part", "download",
            "stackdump", "hprof", "tombstone", "anr", "trace", "logcat", "glog",
            "dex.tmp", "apk.tmp", "apk.part"
    ));

    // Protected user & system directories that should never be scanned for deletion
    private static final Set<String> PROTECTED_PATH_SEGMENTS = new HashSet<>(Arrays.asList(
            "/system", "/vendor", "/product", "/system_ext", "/apex",
            "/dcim/camera", "/pictures/screenshots", "/pictures/whatsapp",
            "/documents", "/music", "/podcasts", "/ringtones", "/alarms", "/notifications",
            "/android/obb"
    ));

    // Forbidden roots that can NEVER be deleted
    private static final Set<String> BLOCKED_ROOT_PATHS = new HashSet<>(Arrays.asList(
            "/", "/system", "/vendor", "/product", "/system_ext", "/apex",
            "/sdcard", "/sdcard/", "/storage", "/storage/",
            "/storage/emulated", "/storage/emulated/",
            "/storage/emulated/0", "/storage/emulated/0/",
            "/data", "/data/", "/data/data", "/data/data/",
            "/data/user", "/data/user/0", "/data/app",
            "/mnt", "/mnt/sdcard", "/mnt/runtime"
    ));

    public static boolean isBlockedRootPath(String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty()) return true;
        String normalized = rawPath.trim().toLowerCase(Locale.ROOT);
        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return BLOCKED_ROOT_PATHS.contains(normalized);
    }

    public static boolean isSafeToScan(File file) {
        if (file == null) return false;
        String path = file.getAbsolutePath().toLowerCase(Locale.ROOT);

        if (isBlockedRootPath(path)) return false;

        for (String protectedSegment : PROTECTED_PATH_SEGMENTS) {
            if (path.contains(protectedSegment)) {
                // If it's specifically .thumbnails or trash inside DCIM/Pictures, allow scanning only that folder
                if (path.contains("/.thumbnails") || path.contains("/.thumb") || path.contains("/.trash")) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    public static boolean isDisposableJunkFile(File file) {
        if (file == null || (file.exists() && file.isDirectory())) return false;
        return isDisposableJunkPath(file.getAbsolutePath());
    }

    public static boolean isDisposableJunkPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) return false;
        String path = filePath.toLowerCase(Locale.ROOT);
        if (isBlockedRootPath(path)) return false;

        int lastSlash = path.lastIndexOf('/');
        String name = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;

        // Strictly protect critical system paths
        if (path.startsWith("/system/") || path.startsWith("/vendor/") || path.startsWith("/apex/") || path.startsWith("/product/")) {
            return false;
        }

        // Never delete files inside user document/camera roots unless explicitly in .thumbnails
        if ((path.contains("/documents/") || path.contains("/dcim/camera/")) && !path.contains("/.thumbnails")) {
            return false;
        }

        // Do not touch .nomedia files anywhere
        if (name.equals(".nomedia")) {
            return false;
        }

        // Never delete protected user extensions unless explicitly in cache/temp/log directories
        String ext = getFileExtension(name);
        boolean inExplicitCacheFolder = path.contains("/cache/") || path.contains("/code_cache/") 
                || path.contains("/.thumbnails/") || path.contains("/app_webview/default/cache/")
                || path.contains("/app_webview/cache/") || path.contains("/gpu_cache/")
                || path.contains("/data/anr/") || path.contains("/data/tombstones/")
                || path.contains("/data/local/tmp/") || path.contains("/data/system/dropbox/")
                || path.contains("/saved/logs/") || path.contains("/saved/crashes/");

        if (PROTECTED_EXTENSIONS.contains(ext) && !inExplicitCacheFolder) {
            return false;
        }

        // Inside explicit cache directories
        if (inExplicitCacheFolder) {
            return true;
        }

        // Recognized junk extension
        if (JUNK_EXTENSIONS.contains(ext)) {
            return true;
        }

        // Stale APK in Downloads or temp
        if (name.endsWith(".apk") && (path.contains("/download") || path.contains("/tmp") || path.contains("/temp"))) {
            return true;
        }

        // Stale partial or download files
        if (name.endsWith(".crdownload") || name.endsWith(".part") || name.endsWith(".tmp") || name.endsWith(".download")) {
            return true;
        }

        // Known temporary crash/log file prefixes
        if (name.startsWith("logcat_") || name.startsWith("anr_") || name.startsWith("crash_") || name.startsWith("tombstone_") || name.startsWith("dump_")) {
            return true;
        }

        return false;
    }

    public static boolean isThumbnailFileOrDir(File file) {
        if (file == null) return false;
        String path = file.getAbsolutePath().toLowerCase(Locale.ROOT);
        return path.contains("/.thumbnails") || path.contains("/.thumb") || file.getName().startsWith(".thumb")
                || path.contains("/.trash") || path.contains("/trashbin");
    }

    public static boolean isObsoleteApkFile(File file) {
        if (file == null || (file.exists() && file.isDirectory())) return false;
        String name = file.getName().toLowerCase(Locale.ROOT);
        String path = file.getAbsolutePath().toLowerCase(Locale.ROOT);
        return name.endsWith(".apk") && (path.contains("/download") || path.contains("/temp") || path.contains("/tmp") || path.contains("/telegram documents"));
    }

    public static boolean isGameLogOrResidual(File file) {
        if (file == null || (file.exists() && file.isDirectory())) return false;
        String name = file.getName().toLowerCase(Locale.ROOT);
        String path = file.getAbsolutePath().toLowerCase(Locale.ROOT);

        boolean inGameOrDataPath = path.contains("/android/data/") || path.contains("/data/data/") || path.contains("/games/");
        if (!inGameOrDataPath) return false;

        String ext = getFileExtension(name);
        return ext.equals("log") || ext.equals("tmp") || ext.equals("dmp") || ext.equals("crash") || ext.equals("trace")
                || name.startsWith("logcat_") || name.startsWith("anr_") || name.startsWith("crash_")
                || path.contains("/saved/logs/") || path.contains("/saved/crashes/")
                || path.contains("/unity_logs/") || path.contains("/ff_log/")
                || path.contains("/assets/ui/logs/");
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
