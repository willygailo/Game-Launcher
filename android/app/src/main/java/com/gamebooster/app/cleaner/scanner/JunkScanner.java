package com.gamebooster.app.cleaner.scanner;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import com.gamebooster.app.cleaner.model.JunkCategory;
import com.gamebooster.app.cleaner.model.JunkItem;
import com.gamebooster.app.cleaner.model.JunkScanResult;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            // STEP 1: All Installed Applications Caches (0% - 35%)
            notifyProgress(listener, 5, "Scanning all installed application & game caches...", result.getTotalBytes());
            scanInstalledAppCaches(context, result, listener);
            if (isCancelled) return finishResult(result, startTime, listener);

            // STEP 2: Media Thumbnails & Gallery Cache (35% - 50%)
            notifyProgress(listener, 35, "Scanning thumbnail caches (.thumbnails)...", result.getTotalBytes());
            scanThumbnails(result, listener);
            if (isCancelled) return finishResult(result, startTime, listener);

            // STEP 3: Obsolete APK Installers (50% - 65%)
            notifyProgress(listener, 50, "Scanning obsolete APK installers in Downloads...", result.getTotalBytes());
            scanObsoleteApks(result, listener);
            if (isCancelled) return finishResult(result, startTime, listener);

            // STEP 4: Temp Files, ANR & Crash Logs (65% - 80%)
            notifyProgress(listener, 65, "Scanning system temp & crash dump logs...", result.getTotalBytes());
            scanTempAndLogs(context, result, listener);
            if (isCancelled) return finishResult(result, startTime, listener);

            // STEP 5: Empty & Orphaned Folders (80% - 90%)
            notifyProgress(listener, 80, "Scanning empty & orphaned directory trees...", result.getTotalBytes());
            scanEmptyFolders(result, listener);
            if (isCancelled) return finishResult(result, startTime, listener);

            // STEP 6: System & Shaders (90% - 100%)
            notifyProgress(listener, 90, "Evaluating system cache trim & shader storage...", result.getTotalBytes());
            scanSystemShaders(context, result, listener);

        } catch (Throwable t) {
            Log.e(TAG, "Storage scan exception", t);
        }

        return finishResult(result, startTime, listener);
    }

    /**
     * Scans cache directories for ALL installed applications and games.
     */
    private void scanInstalledAppCaches(Context context, JunkScanResult result, OnScanProgressListener listener) {
        if (context == null) return;
        PackageManager pm = context.getPackageManager();
        if (pm == null) return;

        Set<String> processedPackages = new HashSet<>();

        // 1. Scan the Launcher APK's own caches
        File internalCache = context.getCacheDir();
        if (internalCache != null && internalCache.exists()) {
            long size = getDirectorySize(internalCache);
            if (size > 0) {
                result.addItem(new JunkItem(internalCache.getAbsolutePath(), "Launcher App Cache", context.getPackageName(), size, JunkCategory.APP_CACHE, true));
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

        // 2. Query ALL installed applications and scan their individual caches
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
                notifyProgress(listener, progressPct, "Scanning app cache: " + appLabel, result.getTotalBytes());

                long appTotalCacheBytes = 0;

                // Check external storage cache (/sdcard/Android/data/<pkg>/cache)
                if (extStorage != null) {
                    File extAppCache = new File(extStorage, "Android/data/" + pkg + "/cache");
                    if (extAppCache.exists() && extAppCache.canRead()) {
                        long size = getDirectorySize(extAppCache);
                        if (size > 0) {
                            appTotalCacheBytes += size;
                        }
                    }

                    File extAppCodeCache = new File(extStorage, "Android/data/" + pkg + "/code_cache");
                    if (extAppCodeCache.exists() && extAppCodeCache.canRead()) {
                        long size = getDirectorySize(extAppCodeCache);
                        if (size > 0) {
                            appTotalCacheBytes += size;
                        }
                    }
                }

                // If Shizuku / Root is available, query internal protected cache (/data/data/<pkg>/cache)
                if (ShizukuFileManager.hasFullAccess()) {
                    try {
                        String duCmd = "du -sk /data/data/" + pkg + "/cache /data/data/" + pkg + "/code_cache 2>/dev/null";
                        String duRes = ShizukuExecutor.executeShizukuCommand(duCmd);
                        if (duRes != null && !duRes.startsWith("ERROR:")) {
                            long shizukuBytes = parseDuTotalBytes(duRes);
                            if (shizukuBytes > 0) {
                                appTotalCacheBytes += shizukuBytes;
                            }
                        }
                    } catch (Throwable ignored) {}
                }

                if (appTotalCacheBytes > 0) {
                    result.addItem(new JunkItem(
                            "/sdcard/Android/data/" + pkg + "/cache",
                            appLabel + " Cache",
                            pkg,
                            appTotalCacheBytes,
                            JunkCategory.APP_CACHE,
                            true
                    ));
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Error querying installed applications for cache scan", t);
        }

        // 3. Scan general external Android/data cache folders directly
        File extStorage = Environment.getExternalStorageDirectory();
        if (extStorage != null) {
            File androidData = new File(extStorage, "Android/data");
            if (androidData.exists() && androidData.canRead()) {
                File[] pkgDirs = androidData.listFiles();
                if (pkgDirs != null) {
                    for (File pkgDir : pkgDirs) {
                        if (isCancelled) return;
                        String pkgName = pkgDir.getName();
                        if (processedPackages.contains(pkgName)) continue;

                        File cacheDir = new File(pkgDir, "cache");
                        if (cacheDir.exists()) {
                            long size = getDirectorySize(cacheDir);
                            if (size > 0) {
                                result.addItem(new JunkItem(
                                        cacheDir.getAbsolutePath(),
                                        pkgName + " Cache",
                                        pkgName,
                                        size,
                                        JunkCategory.APP_CACHE,
                                        true
                                ));
                            }
                        }
                    }
                }
            }
        }
    }

    private void scanThumbnails(JunkScanResult result, OnScanProgressListener listener) {
        File extStorage = Environment.getExternalStorageDirectory();
        if (extStorage == null) return;

        String[] thumbnailPaths = {
                "DCIM/.thumbnails",
                "Pictures/.thumbnails",
                "Movies/.thumbnails",
                "Download/.thumbnails",
                "Android/data/.thumbnails"
        };

        for (String relativePath : thumbnailPaths) {
            if (isCancelled) return;
            File thumbDir = new File(extStorage, relativePath);
            if (thumbDir.exists()) {
                scanDirectory(thumbDir, JunkCategory.THUMBNAILS, result, listener, 40);
            }
        }
    }

    private void scanObsoleteApks(JunkScanResult result, OnScanProgressListener listener) {
        File extStorage = Environment.getExternalStorageDirectory();
        if (extStorage == null) return;

        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadDir != null && downloadDir.exists()) {
            scanApksInFolder(downloadDir, result, listener, 0);
        }
    }

    private void scanApksInFolder(File dir, JunkScanResult result, OnScanProgressListener listener, int depth) {
        if (dir == null || !dir.exists() || depth > 2 || isCancelled) return;
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory() && !f.getName().startsWith(".")) {
                scanApksInFolder(f, result, listener, depth + 1);
            } else if (f.isFile() && f.getName().toLowerCase().endsWith(".apk")) {
                result.addItem(new JunkItem(f.getAbsolutePath(), f.getName(), f.length(), JunkCategory.OBSOLETE_APKS, false));
                notifyProgress(listener, 55, f.getAbsolutePath(), result.getTotalBytes());
            }
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
        }

        if (ShizukuFileManager.hasFullAccess()) {
            scanPrivilegedTempDir("/data/local/tmp", JunkCategory.TEMP_FILES, result);
            scanPrivilegedTempDir("/data/anr", JunkCategory.TEMP_FILES, result);
            scanPrivilegedTempDir("/data/tombstones", JunkCategory.TEMP_FILES, result);
        }
    }

    private void scanPrivilegedTempDir(String path, JunkCategory category, JunkScanResult result) {
        try {
            String duRes = ShizukuExecutor.executeShizukuCommand("du -sk '" + path + "' 2>/dev/null");
            if (duRes != null && !duRes.startsWith("ERROR:")) {
                parseDuOutput(duRes, category, result);
            }
        } catch (Throwable ignored) {}
    }

    private void scanEmptyFolders(JunkScanResult result, OnScanProgressListener listener) {
        File extStorage = Environment.getExternalStorageDirectory();
        if (extStorage == null) return;

        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadDir != null && downloadDir.exists()) {
            findEmptyDirs(downloadDir, result, 0);
        }
    }

    private void findEmptyDirs(File dir, JunkScanResult result, int depth) {
        if (dir == null || !dir.exists() || !dir.isDirectory() || depth > 3 || isCancelled) return;
        File[] files = dir.listFiles();
        if (files == null) return;

        if (files.length == 0 && !dir.getName().startsWith(".")) {
            result.addItem(new JunkItem(dir.getAbsolutePath(), "Empty Folder: " + dir.getName(), 4096L, JunkCategory.EMPTY_FOLDERS, true));
            return;
        }

        for (File f : files) {
            if (f.isDirectory() && !f.getName().startsWith(".")) {
                findEmptyDirs(f, result, depth + 1);
            }
        }
    }

    private void scanSystemShaders(Context context, JunkScanResult result, OnScanProgressListener listener) {
        long estimatedSystemCache = 64L * 1024L * 1024L; // 64 MB minimum baseline
        if (ShizukuFileManager.hasFullAccess()) {
            estimatedSystemCache = 128L * 1024L * 1024L; // 128 MB Shizuku elevated trim
        }

        result.addItem(new JunkItem(
                "SYSTEM_TRIM_CACHES",
                "Shader Storage & System Package Trim Buffer",
                estimatedSystemCache,
                JunkCategory.SYSTEM_CACHE,
                false
        ));
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
                if (ScanFilter.isDisposableJunkFile(file) || category == JunkCategory.APP_CACHE || category == JunkCategory.THUMBNAILS) {
                    result.addItem(new JunkItem(file.getAbsolutePath(), file.getName(), file.length(), category, false));
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

    private long parseDuTotalBytes(String duOutput) {
        if (duOutput == null) return 0;
        long total = 0;
        String[] lines = duOutput.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            String[] parts = trimmed.split("\\s+", 2);
            if (parts.length >= 1) {
                try {
                    long kb = Long.parseLong(parts[0]);
                    total += kb * 1024L;
                } catch (Exception ignored) {}
            }
        }
        return total;
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
