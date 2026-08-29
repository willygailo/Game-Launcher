package com.gamebooster.app.games;
import com.gamebooster.app.config.*;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameManagerRepository {

    private static final String TAG = "GameManagerRepository";

    public static List<GameAppInfo> getInstalledGames(Context context) {
        return HomeGameScanner.scanTargetGames(context);
    }

    public static List<GameAppInfo> getAllInstalledApps(Context context) {
        List<GameAppInfo> appsList = new ArrayList<>();
        if (context == null) return appsList;

        PackageManager pm = context.getPackageManager();
        if (pm == null) return appsList;

        Set<String> addedPackages = new HashSet<>();

        // Method 1: Launcher activities (Android 13-16)
        try {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> resolveInfos = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                try {
                    resolveInfos = pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL));
                } catch (Throwable ignored) {}
            }
            if (resolveInfos == null || resolveInfos.isEmpty()) {
                resolveInfos = pm.queryIntentActivities(mainIntent, 0);
            }

            if (resolveInfos != null) {
                for (ResolveInfo ri : resolveInfos) {
                    if (ri == null || ri.activityInfo == null) continue;
                    String pkgName = ri.activityInfo.packageName;
                    if (pkgName == null || addedPackages.contains(pkgName)) continue;
                    if (pkgName.equalsIgnoreCase(context.getPackageName())) continue;

                    String label = ri.loadLabel(pm).toString();
                    Drawable icon = ri.loadIcon(pm);
                    Intent launchIntent = HomeGameScanner.resolveLaunchIntent(pm, pkgName);

                    appsList.add(new GameAppInfo(label, pkgName, icon, launchIntent));
                    addedPackages.add(pkgName);
                }
            }
        } catch (Throwable ignored) {}

        // Method 2: Installed applications scan (Android 13-16)
        try {
            List<ApplicationInfo> apps = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                try {
                    apps = pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0));
                } catch (Throwable ignored) {}
            }
            if (apps == null) {
                apps = pm.getInstalledApplications(0);
            }

            if (apps != null) {
                for (ApplicationInfo app : apps) {
                    if (app == null || app.packageName == null || addedPackages.contains(app.packageName)) continue;
                    if (app.packageName.equalsIgnoreCase(context.getPackageName())) continue;

                    CharSequence labelSeq = pm.getApplicationLabel(app);
                    String label = labelSeq != null ? labelSeq.toString() : app.packageName;
                    Drawable icon = null;
                    try {
                        icon = pm.getApplicationIcon(app);
                    } catch (Throwable ignored) {}
                    Intent launchIntent = HomeGameScanner.resolveLaunchIntent(pm, app.packageName);

                    appsList.add(new GameAppInfo(label, app.packageName, icon, launchIntent));
                    addedPackages.add(app.packageName);
                }
            }
        } catch (Throwable ignored) {}

        // Method 3: Shizuku ADB package list
        if (ShizukuExecutor.isShizukuAvailable()) {
            try {
                String shizukuRes = ShizukuExecutor.executeShizukuCommand("pm list packages -3");
                if (shizukuRes != null && !shizukuRes.startsWith("ERROR")) {
                    String[] lines = shizukuRes.split("\n");
                    for (String line : lines) {
                        String pkgName = line.trim().replace("package:", "").trim();
                        if (pkgName.isEmpty() || addedPackages.contains(pkgName)) continue;
                        if (pkgName.equalsIgnoreCase(context.getPackageName())) continue;

                        try {
                            ApplicationInfo appInfo = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                appInfo = pm.getApplicationInfo(pkgName, PackageManager.ApplicationInfoFlags.of(0));
                            } else {
                                appInfo = pm.getApplicationInfo(pkgName, 0);
                            }

                            String label = (appInfo != null) ? pm.getApplicationLabel(appInfo).toString() : pkgName;
                            Drawable icon = (appInfo != null) ? pm.getApplicationIcon(appInfo) : null;
                            Intent launchIntent = HomeGameScanner.resolveLaunchIntent(pm, pkgName);
                            appsList.add(new GameAppInfo(label, pkgName, icon, launchIntent));
                            addedPackages.add(pkgName);
                        } catch (Throwable ignored) {}
                    }
                }
            } catch (Throwable ignored) {}
        }

        // Sort alphabetically by app label
        Collections.sort(appsList, Comparator.comparing(a -> a.getLabel().toLowerCase()));
        return appsList;
    }

    public static String boostRamAndOptimize(Context context) {
        if (context == null) return "Boost Failed";

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            ActivityManager.MemoryInfo memBefore = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(memBefore);
            long beforeMB = memBefore.availMem / (1024 * 1024);

            CommandExecutor.executeSystemCommand("sync; echo 3 > /proc/sys/vm/drop_caches");

            ActivityManager.MemoryInfo memAfter = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(memAfter);
            long afterMB = memAfter.availMem / (1024 * 1024);
            long freedMB = Math.max(0, afterMB - beforeMB);

            return "Memory Boosted! Freed " + freedMB + " MB RAM (" + afterMB + " MB Available)";
        }

        return "Memory Optimization Completed!";
    }
}
