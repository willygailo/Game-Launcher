package com.gamebooster.app.cleaner.scanner;

import android.app.AppOpsManager;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * StorageStatsHelper — Real Android Storage & Cache Inspection and Allocation.
 *
 * Utilizes official Android Framework APIs (StorageStatsManager, StorageManager, StatFs)
 * to measure and manage 100% exact storage statistics across all installed apps.
 */
public final class StorageStatsHelper {

    private static final String TAG = "StorageStatsHelper";

    public static class AppStorageMetrics {
        public final String packageName;
        public final String appLabel;
        public final long cacheBytes;
        public final long appBytes;
        public final long dataBytes;

        public AppStorageMetrics(String packageName, String appLabel, long cacheBytes, long appBytes, long dataBytes) {
            this.packageName = packageName;
            this.appLabel = appLabel;
            this.cacheBytes = cacheBytes;
            this.appBytes = appBytes;
            this.dataBytes = dataBytes;
        }

        public long getTotalBytes() {
            return cacheBytes + appBytes + dataBytes;
        }
    }

    private StorageStatsHelper() {}

    /**
     * Checks whether PACKAGE_USAGE_STATS (Usage Access) is granted to this app.
     */
    public static boolean hasUsageStatsPermission(@Nullable Context context) {
        if (context == null) return false;
        try {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            if (appOps == null) return false;
            int mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.getPackageName()
            );
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (Throwable t) {
            Log.w(TAG, "hasUsageStatsPermission check error", t);
            return false;
        }
    }

    /**
     * Checks whether MANAGE_EXTERNAL_STORAGE (All Files Access) is granted.
     */
    public static boolean hasAllFilesAccess() {
        try {
            return Environment.isExternalStorageManager();
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Attempts to auto-grant PACKAGE_USAGE_STATS and MANAGE_EXTERNAL_STORAGE via Shizuku / ADB.
     */
    public static boolean autoGrantPrivilegedPermissions(@Nullable Context context) {
        if (context == null || !ShizukuExecutor.hasShizukuPermission()) return false;
        try {
            String pkg = context.getPackageName();
            StringBuilder sb = new StringBuilder();
            sb.append("pm grant ").append(pkg).append(" android.permission.PACKAGE_USAGE_STATS 2>/dev/null; ");
            sb.append("pm grant ").append(pkg).append(" android.permission.READ_EXTERNAL_STORAGE 2>/dev/null; ");
            sb.append("pm grant ").append(pkg).append(" android.permission.WRITE_EXTERNAL_STORAGE 2>/dev/null; ");
            sb.append("appops set ").append(pkg).append(" GET_USAGE_STATS allow 2>/dev/null; ");
            sb.append("appops set ").append(pkg).append(" android:get_usage_stats allow 2>/dev/null; ");
            sb.append("appops set ").append(pkg).append(" MANAGE_EXTERNAL_STORAGE allow 2>/dev/null; ");
            sb.append("appops set ").append(pkg).append(" android:manage_external_storage allow 2>/dev/null; ");
            sb.append("appops set ").append(pkg).append(" ACCESS_RESTRICTED_SETTINGS allow 2>/dev/null");
            ShizukuExecutor.executeShizukuCommand(sb.toString());
            Log.i(TAG, "Auto-granted storage & usage permissions via Shizuku");
            return true;
        } catch (Throwable t) {
            Log.w(TAG, "Failed auto-granting permissions via Shizuku", t);
            return false;
        }
    }

    /**
     * Creates an Intent to open the Usage Access Settings screen.
     */
    @NonNull
    public static Intent createUsageAccessSettingsIntent(@NonNull Context context) {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * Creates an Intent to open the All Files Access Settings screen.
     */
    @NonNull
    public static Intent createAllFilesAccessSettingsIntent(@NonNull Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * Queries the exact, real cache size of a single application using StorageStatsManager.
     */
    @Nullable
    public static AppStorageMetrics queryAppMetrics(@NonNull Context context, @NonNull ApplicationInfo appInfo) {

        try {
            StorageStatsManager statsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
            if (statsManager == null) return null;

            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            UUID storageUuid = StorageManager.UUID_DEFAULT;
            if (storageManager != null && appInfo.storageUuid != null) {
                storageUuid = appInfo.storageUuid;
            }

            UserHandle userHandle = Process.myUserHandle();
            StorageStats stats = statsManager.queryStatsForPackage(storageUuid, appInfo.packageName, userHandle);
            if (stats != null) {
                PackageManager pm = context.getPackageManager();
                CharSequence label = pm != null ? pm.getApplicationLabel(appInfo) : appInfo.packageName;
                return new AppStorageMetrics(
                        appInfo.packageName,
                        label != null ? label.toString() : appInfo.packageName,
                        stats.getCacheBytes(),
                        stats.getAppBytes(),
                        stats.getDataBytes()
                );
            }
        } catch (Throwable t) {
            // Permission not granted or app uninstalled during scan
            Log.d(TAG, "queryStatsForPackage failed for " + appInfo.packageName + ": " + t.getMessage());
        }
        return null;
    }

    @Nullable
    public static AppStorageMetrics queryAppMetrics(@NonNull Context context, @NonNull String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            if (pm == null) return null;
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            return queryAppMetrics(context, appInfo);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Queries exact real cache metrics for ALL installed applications and games.
     */
    @NonNull
    public static Map<String, AppStorageMetrics> queryAllInstalledAppMetrics(@NonNull Context context) {
        Map<String, AppStorageMetrics> results = new HashMap<>();

        if (!hasUsageStatsPermission(context)) {
            // Try auto-grant if Shizuku is active
            autoGrantPrivilegedPermissions(context);
            if (!hasUsageStatsPermission(context)) {
                Log.w(TAG, "Cannot query app metrics: PACKAGE_USAGE_STATS is not granted");
                return results;
            }
        }

        PackageManager pm = context.getPackageManager();
        if (pm == null) return results;

        List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo appInfo : installedApps) {
            if (appInfo == null || appInfo.packageName == null) continue;
            AppStorageMetrics metrics = queryAppMetrics(context, appInfo);
            if (metrics != null && metrics.cacheBytes > 0) {
                results.put(appInfo.packageName, metrics);
            }
        }

        return results;
    }

    /**
     * Queries the total allocatable bytes that the Android OS can reclaim
     * by clearing cache files across apps.
     */
    public static long getAllocatableBytes(@NonNull Context context) {
        try {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            if (sm != null) {
                return sm.getAllocatableBytes(StorageManager.UUID_DEFAULT);
            }
        } catch (Throwable t) {
            Log.w(TAG, "getAllocatableBytes exception", t);
        }
        return 0;
    }

    /**
     * Accurately calculates genuine reclaimable system cache by subtracting
     * the volume's raw free space from StorageManager's allocatable bytes.
     * Prevents reporting the device's free disk space as junk.
     */
    public static long getReclaimableCacheBytes(@NonNull Context context) {
        try {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            if (sm != null) {
                long allocatable = sm.getAllocatableBytes(StorageManager.UUID_DEFAULT);
                File dataDir = Environment.getDataDirectory();
                long freeBytes = (dataDir != null && dataDir.exists()) ? dataDir.getFreeSpace() : 0L;
                long reclaimable = Math.max(0L, allocatable - freeBytes);
                // Cap to 5GB maximum reasonable cache buffer to prevent any OS metric anomalies
                return Math.min(reclaimable, 5L * 1024 * 1024 * 1024);
            }
        } catch (Throwable t) {
            Log.w(TAG, "getReclaimableCacheBytes exception", t);
        }
        return 0L;
    }

    /**
     * Official Android OS system cache eviction: requests the system to reclaim
     * clearable cache from other applications to satisfy the specified byte quota.
     */
    public static boolean allocateBytes(@NonNull Context context, long bytesToReclaim) {
        if (bytesToReclaim <= 0) return true;
        try {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            if (sm != null) {
                long allocatable = sm.getAllocatableBytes(StorageManager.UUID_DEFAULT);
                long targetBytes = Math.min(bytesToReclaim, allocatable);
                if (targetBytes > 0) {
                    sm.allocateBytes(StorageManager.UUID_DEFAULT, targetBytes);
                    Log.i(TAG, "Successfully requested StorageManager.allocateBytes: " + targetBytes);
                    return true;
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "allocateBytes exception", t);
        }
        return false;
    }
}
