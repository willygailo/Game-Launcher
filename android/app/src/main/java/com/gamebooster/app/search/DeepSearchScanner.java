package com.gamebooster.app.search;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.HashSet;
import java.util.Set;

/**
 * DeepSearchScanner implements multi-platform deep scanning for Android 11 to 16.
 * It bypasses Package Visibility restrictions using Shizuku ADB commands, multi-user queries,
 * third-party store discovery (TapTap, Garena, Galaxy Store, APKPure), and storage directory inspection.
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

        // 1. Shizuku ADB Multi-User & Unrestricted Package Query (pm list packages -3 -u -a)
        if (ShizukuExecutor.isShizukuAvailable()) {
            try {
                String cmdRes = ShizukuExecutor.executeShizukuCommand("pm list packages -3 -u -a");
                if (cmdRes != null && !cmdRes.startsWith("ERROR")) {
                    String[] lines = cmdRes.split("\n");
                    for (String line : lines) {
                        String pkg = line.trim().replace("package:", "").trim();
                        if (!pkg.isEmpty()) {
                            if (GamePackageRegistry.isKnownGame(pkg) || PLATFORM_STORES.contains(pkg.toLowerCase())) {
                                discoveredPackages.add(pkg);
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                Log.e(TAG, "Shizuku package query error", e);
            }

            // 2. Storage Directory Deep Inspection (/sdcard/Android/data & /sdcard/Android/obb)
            try {
                String dataRes = ShizukuExecutor.executeShizukuCommand("ls -1 /sdcard/Android/data/ 2>/dev/null");
                if (dataRes != null && !dataRes.startsWith("ERROR")) {
                    String[] folders = dataRes.split("\n");
                    for (String folder : folders) {
                        String pkg = folder.trim();
                        if (GamePackageRegistry.isKnownGame(pkg)) {
                            discoveredPackages.add(pkg);
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }

        // 3. Standard PackageManager Query Fallback
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

        Log.i(TAG, "Deep Search completed. Discovered " + discoveredPackages.size() + " game/platform packages.");
        return discoveredPackages;
    }
}
