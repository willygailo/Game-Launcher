package com.gamebooster.app.feature.games.search;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.gamebooster.app.feature.games.GamePackageRegistry;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DeepSearchScanner implements multi-platform deep scanning for Android 11 to 16.
 * Bypasses Package Visibility restrictions using Shizuku ADB commands, multi-user queries,
 * Android OS CATEGORY_GAME classification, storage directory inspection (/sdcard/Android/obb & /sdcard/Android/data),
 * and third-party store discovery.
 */
public class DeepSearchScanner {

    private static final String TAG = "DeepSearchScanner";

    // Known Platform Store Packages
    public static final Set<String> PLATFORM_STORES = new HashSet<>();

    static {
        PLATFORM_STORES.add("com.taptap.global");
        PLATFORM_STORES.add("com.taptap");
        PLATFORM_STORES.add("com.garena.appstore");
        PLATFORM_STORES.add("com.sec.android.app.samsungapps");
        PLATFORM_STORES.add("com.apkpure.aether");
        PLATFORM_STORES.add("com.qooapp.qoohelper");
        PLATFORM_STORES.add("com.amazon.venezia");
    }

    public static Set<String> performDeepSearch(Context context) {
        Set<String> discoveredPackages = new HashSet<>();
        if (context == null) return discoveredPackages;

        PackageManager pm = context.getPackageManager();

        // 1. Android OS Native Package Category Scan (API 26+ CATEGORY_GAME & FLAG_IS_GAME)
        try {
            List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo app : installedApps) {
                if (app == null || app.packageName == null) continue;
                String pkg = app.packageName.toLowerCase();

                boolean isGameCategory = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    isGameCategory = (app.category == ApplicationInfo.CATEGORY_GAME);
                }
                boolean isGameFlag = (app.flags & ApplicationInfo.FLAG_IS_GAME) != 0;

                if (isGameCategory || isGameFlag || GamePackageRegistry.isKnownGame(pkg) || PLATFORM_STORES.contains(pkg)) {
                    discoveredPackages.add(app.packageName);
                }
            }
        } catch (Throwable e) {
            Log.w(TAG, "PackageManager installed applications query fallback: " + e.getMessage());
        }

        // 2. Shizuku ADB Multi-User & Privileged Package Query (pm list packages -3 -u -a)
        if (ShizukuExecutor.isShizukuAvailable()) {
            try {
                String cmdRes = ShizukuExecutor.executeShizukuCommand("pm list packages -3 -u -a");
                if (cmdRes != null && !cmdRes.startsWith("ERROR")) {
                    String[] lines = cmdRes.split("\n");
                    for (String line : lines) {
                        String pkg = line.trim().replace("package:", "").trim();
                        if (!pkg.isEmpty()) {
                            if (GamePackageRegistry.isKnownGame(pkg) || PLATFORM_STORES.contains(pkg.toLowerCase()) || matchesGamePattern(pkg)) {
                                discoveredPackages.add(pkg);
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                Log.e(TAG, "Shizuku package query error", e);
            }

            // 3. Storage Directory Deep Inspection (/sdcard/Android/data & /sdcard/Android/obb)
            try {
                String[] inspectDirs = {"/sdcard/Android/data/", "/sdcard/Android/obb/"};
                for (String dirPath : inspectDirs) {
                    String dirRes = ShizukuExecutor.executeShizukuCommand("ls -1 " + dirPath + " 2>/dev/null");
                    if (dirRes != null && !dirRes.startsWith("ERROR")) {
                        String[] folders = dirRes.split("\n");
                        for (String folder : folders) {
                            String pkg = folder.trim();
                            if (!pkg.isEmpty() && (GamePackageRegistry.isKnownGame(pkg) || matchesGamePattern(pkg))) {
                                discoveredPackages.add(pkg);
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }

        // 4. Registry Fallback Query
        try {
            for (String knownPkg : GamePackageRegistry.getAllKnownGames().keySet()) {
                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(knownPkg, 0);
                    if (appInfo != null) {
                        discoveredPackages.add(knownPkg);
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}

        Log.i(TAG, "Deep PAL Search completed. Discovered " + discoveredPackages.size() + " game/platform packages.");
        return discoveredPackages;
    }

    private static boolean matchesGamePattern(String pkg) {
        if (pkg == null) return false;
        String lower = pkg.toLowerCase();
        return lower.contains(".game") || lower.contains(".moba") || lower.contains(".fps")
                || lower.contains(".rpg") || lower.contains(".racing") || lower.contains(".shooter")
                || lower.contains(".battle") || lower.contains(".pubg") || lower.contains(".cod")
                || lower.contains(".legends");
    }
}
