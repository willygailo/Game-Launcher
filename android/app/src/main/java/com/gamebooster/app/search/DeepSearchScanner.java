package com.gamebooster.app.search;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DeepSearchScanner implements multi-platform deep scanning for Android 11 to 16.
 * Bypasses Package Visibility restrictions using Shizuku ADB commands, multi-user queries
 * (pm list users & pm list packages --user <id>), platform store discovery, and storage directory inspection.
 */
public class DeepSearchScanner {

    private static final String TAG = "DeepSearchScanner";

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

        if (ShizukuExecutor.isShizukuAvailable()) {
            // 1. Multi-User Discovery via Shizuku ADB (pm list users)
            List<String> userIds = getSystemUserIds();
            for (String userId : userIds) {
                String cmd = "pm list packages --user " + userId + " -3 -u -a";
                parsePackagesFromCmd(cmd, discoveredPackages);
            }

            // 2. Fallback global query if multi-user produced no results
            if (discoveredPackages.isEmpty()) {
                parsePackagesFromCmd("pm list packages -3 -u -a", discoveredPackages);
            }

            // 3. Storage Directory Deep Inspection (/sdcard/Android/data & obb & /data/data)
            scanStorageDirectory("/sdcard/Android/data/", discoveredPackages);
            scanStorageDirectory("/sdcard/Android/obb/", discoveredPackages);
            scanStorageDirectory("/data/data/", discoveredPackages);
        }

        // 4. Standard PackageManager Query Fallback for known games
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

        Log.i(TAG, "Deep Search completed. Discovered " + discoveredPackages.size() + " game/platform packages across multi-user spaces.");
        return discoveredPackages;
    }

    private static List<String> getSystemUserIds() {
        List<String> userIds = new ArrayList<>();
        userIds.add("0"); // Default owner user ID
        try {
            String usersRes = ShizukuExecutor.executeShizukuCommand("pm list users");
            if (usersRes != null && !usersRes.startsWith("ERROR")) {
                Pattern pattern = Pattern.compile("UserInfo\\{(\\d+):");
                Matcher matcher = pattern.matcher(usersRes);
                while (matcher.find()) {
                    String id = matcher.group(1);
                    if (id != null && !userIds.contains(id)) {
                        userIds.add(id);
                    }
                }
            }
        } catch (Throwable e) {
            Log.w(TAG, "Error listing system user IDs: " + e.getMessage());
        }
        return userIds;
    }

    private static void parsePackagesFromCmd(String command, Set<String> targetSet) {
        try {
            String cmdRes = ShizukuExecutor.executeShizukuCommand(command);
            if (cmdRes != null && !cmdRes.startsWith("ERROR")) {
                String[] lines = cmdRes.split("\n");
                for (String line : lines) {
                    String pkg = line.trim().replace("package:", "").trim();
                    if (!pkg.isEmpty()) {
                        if (GamePackageRegistry.isKnownGame(pkg) || PLATFORM_STORES.contains(pkg.toLowerCase())) {
                            targetSet.add(pkg);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            Log.w(TAG, "Error running package query: " + command + " - " + e.getMessage());
        }
    }

    private static void scanStorageDirectory(String path, Set<String> targetSet) {
        try {
            String dataRes = ShizukuExecutor.executeShizukuCommand("ls -1 " + path + " 2>/dev/null");
            if (dataRes != null && !dataRes.startsWith("ERROR")) {
                String[] folders = dataRes.split("\n");
                for (String folder : folders) {
                    String pkg = folder.trim();
                    if (GamePackageRegistry.isKnownGame(pkg)) {
                        targetSet.add(pkg);
                    }
                }
            }
        } catch (Throwable ignored) {}
    }
}
