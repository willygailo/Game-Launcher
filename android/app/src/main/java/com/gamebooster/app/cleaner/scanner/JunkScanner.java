package com.gamebooster.app.cleaner.scanner;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import com.gamebooster.app.cleaner.model.JunkCategory;
import com.gamebooster.app.cleaner.model.JunkItem;
import com.gamebooster.app.cleaner.model.JunkScanResult;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JunkScanner — Real, Deep Storage & Cache Scanner for Android 13, 14, 15, and 16.
 *
 * Grounded in official Android Framework APIs (StorageStatsManager, StorageManager,
 * PackageManager) combined with deep Shizuku/Root storage inspection.
 * Scans real app caches, uninstalled app residuals (CorpseFinder), social media buffers,
 * game dumps, hidden thumbnails, OEM gallery trashes, and obsolete APKs.
 */
public class JunkScanner {

    private static final String TAG = "JunkScanner";

    public interface OnScanProgressListener {
        void onScanProgress(int percent, String currentPath, long bytesFoundSoFar);
        void onScanComplete(JunkScanResult result);
    }

    private volatile boolean isCancelled = false;

    public void cancelScan() {
        this.isCancelled = true;
    }

    public JunkScanResult scanStorage(Context context, OnScanProgressListener listener) {
        isCancelled = false;
        long startTime = System.currentTimeMillis();
        JunkScanResult result = new JunkScanResult();

        try {
            // STEP 1: All Installed Application Caches (0% - 30%)
            notifyProgress(listener, 5, "Scanning application & game caches...", result.getTotalBytes());
            scanInstalledAppCaches(context, result, listener);
            if (isCancelled) return finishResult(result, startTime, listener);

            // STEP 2: Leftover / Orphaned Folders from Uninstalled Apps (CorpseFinder) (30% - 45%)
            notifyProgress(listener, 30, "Scanning orphaned folders from uninstalled apps...", result.getTotalBytes());
            scanUninstalledResiduals(context, result, listener);
            if (isCancelled) return finishResult(result, startTime, listener);

            // STEP 3: Messaging & Social Media Caches (45% - 60%)
            notifyProgress(listener, 45, "Scanning WhatsApp, Telegram & social media caches...", result.getTotalBytes());
            scanSocialMediaCaches(context, result, listener);
            if (isCancelled) return finishResult(result, startTime, listener);

            // STEP 4: Media Thumbnails & Gallery Trash (60% - 70%)
            notifyProgress(listener, 60, "Scanning thumbnail caches & gallery trash (.thumbnails)...", result.getTotalBytes());
            scanThumbnails(result, listener);
            if (isCancelled) return finishResult(result, startTime, listener);

            // STEP 5: Obsolete APK Installers & Partial Downloads (70% - 80%)
            notifyProgress(listener, 70, "Inspecting obsolete APKs & stale downloads...", result.getTotalBytes());
            scanObsoleteApks(context, result, listener);
            if (isCancelled) return finishResult(result, startTime, listener);

            // STEP 6: Temp Files, Crash Logs & Game Shader Dumps (80% - 90%)
            notifyProgress(listener, 80, "Scanning temp logs, crash dumps & game residuals...", result.getTotalBytes());
            scanTempAndLogs(context, result, listener);
            if (isCancelled) return finishResult(result, startTime, listener);

            // STEP 7: Empty Directories (90% - 95%)
            notifyProgress(listener, 90, "Scanning empty directory trees...", result.getTotalBytes());
            scanEmptyFolders(result, listener);
            if (isCancelled) return finishResult(result, startTime, listener);

            // STEP 8: OS System Reclaimable Cache Buffer (95% - 100%)
            notifyProgress(listener, 95, "Querying OS system reclaimable cache quota...", result.getTotalBytes());
            scanSystemAllocatable(context, result, listener);

        } catch (Throwable t) {
            Log.e(TAG, "Storage scan exception", t);
        }

        return finishResult(result, startTime, listener);
    }

    /**
     * Scans real cache sizes for ALL installed applications and games.
     */
    private void scanInstalledAppCaches(Context context, JunkScanResult result, OnScanProgressListener listener) {
        if (context == null) return;
        PackageManager pm = context.getPackageManager();
        if (pm == null) return;

        Set<String> processedPackages = new HashSet<>();

        // 1. Scan the Launcher APK's own caches directly
        File internalCache = context.getCacheDir();
        if (internalCache != null && internalCache.exists()) {
            long size = getDirectorySize(internalCache);
            if (size > 0) {
                result.addItem(new JunkItem(internalCache.getAbsolutePath(), "Launcher Internal Cache", context.getPackageName(), size, JunkCategory.APP_CACHE, true));
            }
        }

        File codeCache = context.getCodeCacheDir();
        if (codeCache != null && codeCache.exists()) {
            long size = getDirectorySize(codeCache);
            if (size > 0) {
                result.addItem(new JunkItem(codeCache.getAbsolutePath(), "Launcher Code Cache", context.getPackageName(), size, JunkCategory.APP_CACHE, true));
            }
        }

        File[] extCaches = context.getExternalCacheDirs();
        if (extCaches != null) {
            for (File extCache : extCaches) {
                if (extCache != null && extCache.exists()) {
                    long size = getDirectorySize(extCache);
                    if (size > 0) {
                        result.addItem(new JunkItem(extCache.getAbsolutePath(), "Launcher External Cache", context.getPackageName(), size, JunkCategory.APP_CACHE, true));
                    }
                }
            }
        }
        processedPackages.add(context.getPackageName());

        // 2. Query ALL installed applications via StorageStatsManager (100% Real OS Metrics)
        boolean hasUsageStats = StorageStatsHelper.hasUsageStatsPermission(context);
        if (!hasUsageStats && ShizukuFileManager.hasFullAccess()) {
            StorageStatsHelper.autoGrantPrivilegedPermissions(context);
            hasUsageStats = StorageStatsHelper.hasUsageStatsPermission(context);
        }

        try {
            List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            int totalApps = installedApps.size();
            File extStorage = Environment.getExternalStorageDirectory();

            for (int i = 0; i < totalApps; i++) {
                if (isCancelled) return;
                ApplicationInfo appInfo = installedApps.get(i);
                if (appInfo == null || appInfo.packageName == null || processedPackages.contains(appInfo.packageName)) continue;

                String pkg = appInfo.packageName;
                processedPackages.add(pkg);

                CharSequence labelSeq = pm.getApplicationLabel(appInfo);
                String appLabel = labelSeq != null ? labelSeq.toString() : pkg;

                int progressPct = 5 + (int) (((i + 1) / (float) totalApps) * 25);
                notifyProgress(listener, progressPct, "Scanning: " + appLabel, result.getTotalBytes());

                long appCacheBytes = 0;

                // Query exact cache bytes from Android OS StorageStatsService
                if (hasUsageStats) {
                    StorageStatsHelper.AppStorageMetrics metrics = StorageStatsHelper.queryAppMetrics(context, appInfo);
                    if (metrics != null && metrics.cacheBytes > 0) {
                        appCacheBytes = metrics.cacheBytes;
                    }
                }

                // If StorageStats was not available or reported 0, check accessible external cache folder
                if (appCacheBytes == 0 && extStorage != null) {
                    File extAppCache = new File(extStorage, "Android/data/" + pkg + "/cache");
                    if (extAppCache.exists() && extAppCache.canRead()) {
                        long size = getDirectorySize(extAppCache);
                        if (size > 0) {
                            appCacheBytes += size;
                        }
                    }
                }

                if (appCacheBytes > 0) {
                    result.addItem(new JunkItem(
                            "/sdcard/Android/data/" + pkg + "/cache",
                            appLabel + " Cache",
                            pkg,
                            appCacheBytes,
                            JunkCategory.APP_CACHE,
                            true
                    ));
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Error querying installed applications for cache scan", t);
        }

        // 3. If Shizuku / Root is available, inspect deep internal caches (/data/data/*/cache)
        if (ShizukuFileManager.hasFullAccess()) {
            try {
                String duCmd = "du -sk /sdcard/Android/data/*/cache /data/data/*/cache /data/data/*/code_cache /data/data/*/app_webview/Default/Cache 2>/dev/null";
                String duRes = ShizukuExecutor.executeShizukuCommand(duCmd);
                if (duRes != null && !duRes.startsWith("ERROR:")) {
                    parseDuAppCaches(duRes, pm, processedPackages, result);
                }
            } catch (Throwable ignored) {}
        }
    }

    /**
     * Identifies residual data and media folders left behind by uninstalled applications (CorpseFinder).
     */
    private void scanUninstalledResiduals(Context context, JunkScanResult result, OnScanProgressListener listener) {
        if (context == null) return;
        PackageManager pm = context.getPackageManager();
        if (pm == null) return;

        File extStorage = Environment.getExternalStorageDirectory();
        if (extStorage == null) return;

        String[] targetRoots = new String[]{
                "Android/data",
                "Android/media",
                "Android/obb"
        };

        for (String rootRel : targetRoots) {
            File rootDir = new File(extStorage, rootRel);
            if (!rootDir.exists() || !rootDir.isDirectory()) continue;

            File[] subDirs = rootDir.listFiles();
            if (subDirs == null) continue;

            for (File subDir : subDirs) {
                if (isCancelled) return;
                if (!subDir.isDirectory()) continue;

                String dirName = subDir.getName();
                // A valid Android package contains at least one dot (e.g. com.example.app)
                if (dirName.contains(".") && !dirName.startsWith(".")) {
                    try {
                        pm.getPackageInfo(dirName, 0);
                        // App is installed, not a residual
                    } catch (PackageManager.NameNotFoundException e) {
                        // App is NOT installed! This is an orphaned leftover folder
                        long size = getDirectorySize(subDir);
                        if (size > 0) {
                            result.addItem(new JunkItem(
                                    subDir.getAbsolutePath(),
                                    "Uninstalled App Residual: " + dirName,
                                    dirName,
                                    size,
                                    JunkCategory.RESIDUAL_UNINSTALLED,
                                    true
                            ));
                            notifyProgress(listener, 40, "Residual: " + dirName, result.getTotalBytes());
                        }
                    } catch (Throwable ignored) {}
                }
            }
        }

        // Also scan well-known legacy junk root folders from uninstalled tools
        String[] legacyRootJunks = new String[]{
                "tencent", "baidu", "UCNews", "UCDownloads", "shareit", "zapya",
                "kugou", "viber", "alibaba", "duomi", ".turing_debug", "msc", "backups/.trash"
        };
        for (String legacyName : legacyRootJunks) {
            File legacyDir = new File(extStorage, legacyName);
            if (legacyDir.exists() && legacyDir.isDirectory()) {
                long size = getDirectorySize(legacyDir);
                if (size > 0 && !containsPath(result, legacyDir.getAbsolutePath())) {
                    result.addItem(new JunkItem(
                            legacyDir.getAbsolutePath(),
                            "Legacy App Leftover: " + legacyName,
                            legacyName,
                            size,
                            JunkCategory.RESIDUAL_UNINSTALLED,
                            true
                    ));
                }
            }
        }
    }

    /**
     * Scans social media, messaging, and web browser temporary buffers.
     */
    private void scanSocialMediaCaches(Context context, JunkScanResult result, OnScanProgressListener listener) {
        File extStorage = Environment.getExternalStorageDirectory();
        if (extStorage == null) return;

        String[] socialPaths = new String[]{
                // Telegram
                "Telegram/Telegram Images/.nomedia",
                "Telegram/Telegram Video/.nomedia",
                "Android/data/org.telegram.messenger/cache",
                "Android/data/org.thunderdog.challegram/cache",
                // WhatsApp
                "Android/media/com.whatsapp/WhatsApp/Media/.Trash",
                "WhatsApp/Media/.Trash",
                "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes/.trash",
                "Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Trash",
                // TikTok & ByteDance / CapCut
                "Android/data/com.zhiliaoapp.musically/cache",
                "Android/data/com.ss.android.ugc.trill/cache",
                "Android/data/com.lemon.lvoverseas/cache", // CapCut
                // Discord & Reddit
                "Android/data/com.discord/cache",
                "Android/data/com.reddit.frontpage/cache",
                // Facebook & Instagram & Messenger
                "Android/data/com.instagram.android/cache",
                "Android/data/com.facebook.katana/cache",
                "Android/data/com.facebook.orca/cache",
                "Android/data/com.twitter.android/cache",
                // Browsers
                "Android/data/com.android.chrome/cache",
                "Android/data/com.brave.browser/cache",
                "Android/data/com.microsoft.emmx/cache",
                "Android/data/org.mozilla.firefox/cache",
                "Android/data/com.opera.browser/cache",
                "Android/data/com.sec.android.app.sbrowser/cache"
        };

        for (String rel : socialPaths) {
            if (isCancelled) return;
            File f = new File(extStorage, rel);
            if (f.exists()) {
                long size = f.isDirectory() ? getDirectorySize(f) : f.length();
                if (size > 0 && !containsPath(result, f.getAbsolutePath())) {
                    result.addItem(new JunkItem(
                            f.getAbsolutePath(),
                            "Social Cache: " + f.getName(),
                            size,
                            JunkCategory.SOCIAL_CACHE,
                            true
                    ));
                    notifyProgress(listener, 55, f.getAbsolutePath(), result.getTotalBytes());
                }
            }
        }
    }

    private void scanThumbnails(JunkScanResult result, OnScanProgressListener listener) {
        File extStorage = Environment.getExternalStorageDirectory();
        if (extStorage == null) return;

        String[] thumbnailPaths = {
                "DCIM/.thumbnails",
                "DCIM/.thumb",
                "Pictures/.thumbnails",
                "Pictures/.thumb",
                "Movies/.thumbnails",
                "Download/.thumbnails",
                "Pictures/.trash",
                "DCIM/.trash",
                "Android/data/com.miui.gallery/files/trashBin",
                "Android/data/com.sec.android.gallery3d/cache",
                "Android/data/com.coloros.gallery3d/cache",
                "Android/data/com.oplus.gallery/cache",
                "Android/data/com.google.android.apps.photos/cache"
        };

        for (String relativePath : thumbnailPaths) {
            if (isCancelled) return;
            File thumbDir = new File(extStorage, relativePath);
            if (thumbDir.exists()) {
                scanDirectory(thumbDir, JunkCategory.THUMBNAILS, result, listener, 65);
            }
        }
    }

    private void scanObsoleteApks(Context context, JunkScanResult result, OnScanProgressListener listener) {
        File extStorage = Environment.getExternalStorageDirectory();
        if (extStorage == null || context == null) return;

        PackageManager pm = context.getPackageManager();
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadDir != null && downloadDir.exists()) {
            scanApksInFolder(context, pm, downloadDir, result, listener, 0);
        }

        File customDownload = new File(extStorage, "Download");
        if (customDownload.exists() && !customDownload.equals(downloadDir)) {
            scanApksInFolder(context, pm, customDownload, result, listener, 0);
        }

        File telegramDocs = new File(extStorage, "Telegram/Telegram Documents");
        if (telegramDocs.exists()) {
            scanApksInFolder(context, pm, telegramDocs, result, listener, 0);
        }
    }

    private void scanApksInFolder(Context context, PackageManager pm, File dir, JunkScanResult result, OnScanProgressListener listener, int depth) {
        if (dir == null || !dir.exists() || depth > 2 || isCancelled) return;
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (isCancelled) return;
            if (f.isDirectory() && !f.getName().startsWith(".")) {
                scanApksInFolder(context, pm, f, result, listener, depth + 1);
            } else if (f.isFile() && f.getName().toLowerCase().endsWith(".apk")) {
                inspectAndAddApk(pm, f, result, listener);
            }
        }
    }

    private void inspectAndAddApk(PackageManager pm, File apkFile, JunkScanResult result, OnScanProgressListener listener) {
        try {
            PackageInfo archiveInfo = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), 0);
            if (archiveInfo != null) {
                String apkPkg = archiveInfo.packageName;
                long apkVersion = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                        ? archiveInfo.getLongVersionCode()
                        : archiveInfo.versionCode;

                String displayName = apkFile.getName();

                try {
                    PackageInfo installedInfo = pm.getPackageInfo(apkPkg, 0);
                    if (installedInfo != null) {
                        long installedVersion = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                                ? installedInfo.getLongVersionCode()
                                : installedInfo.versionCode;

                        if (installedVersion >= apkVersion) {
                            displayName = "Obsolete: " + apkFile.getName() + " (Installed: v" + installedInfo.versionName + ")";
                        } else {
                            displayName = "Update APK: " + apkFile.getName() + " (v" + archiveInfo.versionName + ")";
                        }
                    }
                } catch (PackageManager.NameNotFoundException ignored) {
                    displayName = "Installer: " + apkFile.getName() + " (v" + archiveInfo.versionName + ")";
                }

                result.addItem(new JunkItem(apkFile.getAbsolutePath(), displayName, apkPkg, apkFile.length(), JunkCategory.OBSOLETE_APKS, false));
                notifyProgress(listener, 75, apkFile.getAbsolutePath(), result.getTotalBytes());
            } else {
                result.addItem(new JunkItem(apkFile.getAbsolutePath(), "Corrupted APK: " + apkFile.getName(), apkFile.length(), JunkCategory.OBSOLETE_APKS, false));
            }
        } catch (Throwable t) {
            result.addItem(new JunkItem(apkFile.getAbsolutePath(), apkFile.getName(), apkFile.length(), JunkCategory.OBSOLETE_APKS, false));
        }
    }

    private void scanTempAndLogs(Context context, JunkScanResult result, OnScanProgressListener listener) {
        if (context != null) {
            File appDir = context.getFilesDir();
            if (appDir != null) {
                File[] files = appDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (ScanFilter.isDisposableJunkFile(f)) {
                            result.addItem(new JunkItem(f.getAbsolutePath(), f.getName(), f.length(), JunkCategory.TEMP_FILES, false));
                        }
                    }
                }
            }
        }

        File extStorage = Environment.getExternalStorageDirectory();
        if (extStorage != null) {
            File[] rootFiles = extStorage.listFiles();
            if (rootFiles != null) {
                for (File f : rootFiles) {
                    if (isCancelled) return;
                    if (f.isFile() && ScanFilter.isDisposableJunkFile(f)) {
                        result.addItem(new JunkItem(f.getAbsolutePath(), f.getName(), f.length(), JunkCategory.TEMP_FILES, false));
                    }
                }
            }

            // Game Crash & Log Folders
            String[] gameLogPaths = {
                    "Android/data/com.dts.freefireth/files/FF_Log",
                    "Android/data/com.dts.freefiremax/files/FF_Log",
                    "Android/data/com.tencent.ig/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Logs",
                    "Android/data/com.tencent.ig/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Crashes",
                    "Android/data/com.levelinfinite.deltaforce/files/UE4Game/DeltaForce/DeltaForce/Saved/Logs",
                    "Android/data/com.mobile.legends/files/dragon2017/assets/UI/Logs"
            };
            for (String gl : gameLogPaths) {
                File gDir = new File(extStorage, gl);
                if (gDir.exists()) {
                    scanDirectory(gDir, JunkCategory.GAME_RESIDUALS, result, listener, 85);
                }
            }
        }

        // Privileged temp batch scan
        if (ShizukuFileManager.hasFullAccess()) {
            try {
                String duRes = ShizukuExecutor.executeShizukuCommand("du -sk /data/local/tmp /data/anr /data/tombstones /data/system/dropbox 2>/dev/null");
                if (duRes != null && !duRes.startsWith("ERROR:")) {
                    parseDuOutput(duRes, JunkCategory.TEMP_FILES, result);
                }
            } catch (Throwable ignored) {}
        }
    }

    private void scanEmptyFolders(JunkScanResult result, OnScanProgressListener listener) {
        File extStorage = Environment.getExternalStorageDirectory();
        if (extStorage == null) return;

        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadDir != null && downloadDir.exists()) {
            findEmptyDirs(downloadDir, result, 0);
        }

        File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (documentsDir != null && documentsDir.exists()) {
            findEmptyDirs(documentsDir, result, 0);
        }
    }

    private void findEmptyDirs(File dir, JunkScanResult result, int depth) {
        if (dir == null || !dir.exists() || !dir.isDirectory() || depth > 3 || isCancelled) return;
        File[] files = dir.listFiles();
        if (files == null) return;

        if (files.length == 0 && !dir.getName().startsWith(".")) {
            result.addItem(new JunkItem(dir.getAbsolutePath(), "Empty Directory: " + dir.getName(), 0L, JunkCategory.EMPTY_FOLDERS, true));
            return;
        }

        for (File f : files) {
            if (f.isDirectory() && !f.getName().startsWith(".")) {
                findEmptyDirs(f, result, depth + 1);
            }
        }
    }

    private void scanSystemAllocatable(Context context, JunkScanResult result, OnScanProgressListener listener) {
        if (context == null) return;
        long allocatableBytes = StorageStatsHelper.getAllocatableBytes(context);
        if (allocatableBytes > 0) {
            result.addItem(new JunkItem(
                    "SYSTEM_ALLOCATABLE_TRIM",
                    "Android OS System Reclaimable Cache Buffer",
                    allocatableBytes,
                    JunkCategory.SYSTEM_CACHE,
                    true
            ));
        }
    }

    private void scanDirectory(File dir, JunkCategory category, JunkScanResult result, OnScanProgressListener listener, int progressPct) {
        if (dir == null || !dir.exists() || isCancelled) return;
        if (!ScanFilter.isSafeToScan(dir)) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (isCancelled) return;
            if (file.isDirectory()) {
                scanDirectory(file, category, result, listener, progressPct);
            } else if (file.isFile()) {
                if (ScanFilter.isDisposableJunkFile(file) || category == JunkCategory.APP_CACHE || category == JunkCategory.THUMBNAILS || category == JunkCategory.GAME_RESIDUALS) {
                    result.addItem(new JunkItem(file.getAbsolutePath(), file.getName(), file.length(), category, true));
                    notifyProgress(listener, progressPct, file.getAbsolutePath(), result.getTotalBytes());
                }
            }
        }
    }

    private long getDirectorySize(File dir) {
        if (dir == null || !dir.exists()) return 0;
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    size += getDirectorySize(f);
                } else {
                    size += f.length();
                }
            }
        }
        return size;
    }

    private void parseDuAppCaches(String duOutput, PackageManager pm, Set<String> processed, JunkScanResult result) {
        if (duOutput == null || duOutput.isEmpty()) return;
        String[] lines = duOutput.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            String[] parts = trimmed.split("\\s+", 2);
            if (parts.length == 2) {
                try {
                    long kb = Long.parseLong(parts[0]);
                    long bytes = kb * 1024L;
                    String path = parts[1];
                    if (bytes <= 0) continue;

                    String pkg = extractPackageFromPath(path);
                    if (pkg != null && !processed.contains(pkg)) {
                        processed.add(pkg);
                        String label = pkg;
                        try {
                            ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                            CharSequence cs = pm.getApplicationLabel(ai);
                            if (cs != null) label = cs.toString();
                        } catch (Throwable ignored) {}

                        result.addItem(new JunkItem(path, label + " Cache", pkg, bytes, JunkCategory.APP_CACHE, true));
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private String extractPackageFromPath(String path) {
        if (path == null) return null;
        if (path.contains("/Android/data/")) {
            int start = path.indexOf("/Android/data/") + "/Android/data/".length();
            int end = path.indexOf("/", start);
            return end > start ? path.substring(start, end) : path.substring(start);
        }
        if (path.contains("/data/data/")) {
            int start = path.indexOf("/data/data/") + "/data/data/".length();
            int end = path.indexOf("/", start);
            return end > start ? path.substring(start, end) : path.substring(start);
        }
        return null;
    }

    private void parseDuOutput(String duOutput, JunkCategory category, JunkScanResult result) {
        if (duOutput == null) return;
        String[] lines = duOutput.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            String[] parts = trimmed.split("\\s+", 2);
            if (parts.length == 2) {
                try {
                    long kb = Long.parseLong(parts[0]);
                    long bytes = kb * 1024L;
                    String path = parts[1];
                    if (bytes > 0 && !containsPath(result, path)) {
                        result.addItem(new JunkItem(path, new File(path).getName(), bytes, category, true));
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private boolean containsPath(JunkScanResult result, String path) {
        for (JunkItem item : result.getItems()) {
            if (item.getPath().equals(path)) return true;
        }
        return false;
    }

    private void notifyProgress(OnScanProgressListener listener, int percent, String path, long bytesFound) {
        if (listener != null) {
            listener.onScanProgress(percent, path, bytesFound);
        }
    }

    private JunkScanResult finishResult(JunkScanResult result, long startTime, OnScanProgressListener listener) {
        result.setScanDurationMs(System.currentTimeMillis() - startTime);
        if (listener != null) {
            listener.onScanComplete(result);
        }
        return result;
    }
}
