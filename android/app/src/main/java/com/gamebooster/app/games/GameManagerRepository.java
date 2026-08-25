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
        List<GameAppInfo> gamesList = new ArrayList<>();
        if (context == null) return gamesList;

        PackageManager pm = context.getPackageManager();
        Set<String> addedPackages = new HashSet<>();
        Set<String> customPkgs = GameLauncherHelper.getCustomPackages(context);

        // 1. Primary Scanner: Query launcher intent activities (Android 11-15 <queries> matched)
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        for (ResolveInfo ri : resolveInfos) {
            if (ri == null || ri.activityInfo == null) continue;
            String pkgName = ri.activityInfo.packageName;
            if (pkgName == null || addedPackages.contains(pkgName)) continue;

            String label = ri.loadLabel(pm).toString();
            Drawable icon = ri.loadIcon(pm);

            ApplicationInfo appInfo = ri.activityInfo.applicationInfo;
            if (isPackageGame(context, pkgName, label, appInfo, customPkgs)) {
                Intent launchIntent = pm.getLaunchIntentForPackage(pkgName);
                if (launchIntent == null) {
                    launchIntent = new Intent(Intent.ACTION_MAIN);
                    launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    launchIntent.setClassName(pkgName, ri.activityInfo.name);
                }
                gamesList.add(new GameAppInfo(label, pkgName, icon, launchIntent));
                addedPackages.add(pkgName);
            }
        }

        // 2. Secondary Scanner: Query installed applications
        try {
            List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo app : apps) {
                if (app == null || app.packageName == null || addedPackages.contains(app.packageName)) continue;

                CharSequence labelSeq = pm.getApplicationLabel(app);
                String label = labelSeq != null ? labelSeq.toString() : app.packageName;

                if (isPackageGame(context, app.packageName, label, app, customPkgs)) {
                    Intent launchIntent = pm.getLaunchIntentForPackage(app.packageName);
                    Drawable icon = pm.getApplicationIcon(app);
                    gamesList.add(new GameAppInfo(label, app.packageName, icon, launchIntent));
                    addedPackages.add(app.packageName);
                }
            }
        } catch (Throwable ignored) {}

        // 3. Tertiary Scanner & Deep Search Engine (Multi-User & Multi-Platform)
        if (ShizukuExecutor.isShizukuAvailable()) {
            try {
                Set<String> deepDiscovered = com.gamebooster.app.search.DeepSearchScanner.performDeepSearch(context);
                for (String pkgName : deepDiscovered) {
                    if (pkgName == null || pkgName.isEmpty() || addedPackages.contains(pkgName)) continue;
                    if (pkgName.equalsIgnoreCase(context.getPackageName())) continue;

                    try {
                        ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);
                        String label = pm.getApplicationLabel(appInfo).toString();
                        Intent launchIntent = pm.getLaunchIntentForPackage(pkgName);
                        Drawable icon = pm.getApplicationIcon(appInfo);
                        gamesList.add(new GameAppInfo(label, pkgName, icon, launchIntent));
                        addedPackages.add(pkgName);
                    } catch (Throwable e) {
                        GamePackageRegistry.GameInfoSpec spec = GamePackageRegistry.getSpec(pkgName);
                        String label = spec != null ? spec.title : pkgName;
                        Drawable defaultAppIcon = context.getApplicationInfo().loadIcon(pm);
                        gamesList.add(new GameAppInfo(label, pkgName, defaultAppIcon, null));
                        addedPackages.add(pkgName);
                    }
                }
            } catch (Throwable e) {
                Log.w(TAG, "Deep search scanner error: " + e.getMessage());
            }
        }

        // 4. Custom Packages Explicit Enforcement
        for (String customPkg : customPkgs) {
            if (!addedPackages.contains(customPkg)) {
                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(customPkg, 0);
                    String label = pm.getApplicationLabel(appInfo).toString();
                    Drawable icon = pm.getApplicationIcon(appInfo);
                    Intent launchIntent = pm.getLaunchIntentForPackage(customPkg);
                    gamesList.add(new GameAppInfo(label, customPkg, icon, launchIntent));
                    addedPackages.add(customPkg);
                } catch (Throwable e) {
                    GamePackageRegistry.GameInfoSpec spec = GamePackageRegistry.getSpec(customPkg);
                    String label = spec != null ? spec.title : customPkg;
                    Drawable defaultIcon = context.getApplicationInfo().loadIcon(pm);
                    gamesList.add(new GameAppInfo(label, customPkg, defaultIcon, null));
                    addedPackages.add(customPkg);
                }
            }
        }

        // 5. Always Guarantee Essential Online Games (MLBB, CODM, PUBG, Wild Rift, Free Fire, HOK, Genshin, Roblox)
        Map<String, GamePackageRegistry.GameInfoSpec> knownMap = GamePackageRegistry.getAllKnownGames();
        Drawable defaultIcon = context.getApplicationInfo().loadIcon(pm);

        for (Map.Entry<String, GamePackageRegistry.GameInfoSpec> entry : knownMap.entrySet()) {
            String pkg = entry.getKey();
            GamePackageRegistry.GameInfoSpec spec = entry.getValue();
            if (!addedPackages.contains(pkg)) {
                // Check if package is physically installed on device
                Intent launchIntent = pm.getLaunchIntentForPackage(pkg);
                if (launchIntent != null) {
                    try {
                        ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);
                        String label = pm.getApplicationLabel(appInfo).toString();
                        Drawable icon = pm.getApplicationIcon(appInfo);
                        gamesList.add(new GameAppInfo(label, pkg, icon, launchIntent));
                        addedPackages.add(pkg);
                    } catch (Throwable ignored) {}
                }
            }
        }

        return gamesList;
    }

    public static List<GameAppInfo> getAllInstalledApps(Context context) {
        List<GameAppInfo> appsList = new ArrayList<>();
        if (context == null) return appsList;

        PackageManager pm = context.getPackageManager();
        Set<String> addedPackages = new HashSet<>();

        // Method 1: Launcher activities
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        for (ResolveInfo ri : resolveInfos) {
            if (ri == null || ri.activityInfo == null) continue;
            String pkgName = ri.activityInfo.packageName;
            if (pkgName == null || addedPackages.contains(pkgName)) continue;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) continue;

            String label = ri.loadLabel(pm).toString();
            Drawable icon = ri.loadIcon(pm);
            Intent launchIntent = pm.getLaunchIntentForPackage(pkgName);

            appsList.add(new GameAppInfo(label, pkgName, icon, launchIntent));
            addedPackages.add(pkgName);
        }

        // Method 2: Installed applications scan
        try {
            List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo app : apps) {
                if (app == null || app.packageName == null || addedPackages.contains(app.packageName)) continue;
                if (app.packageName.equalsIgnoreCase(context.getPackageName())) continue;

                CharSequence labelSeq = pm.getApplicationLabel(app);
                String label = labelSeq != null ? labelSeq.toString() : app.packageName;
                Drawable icon = pm.getApplicationIcon(app);
                Intent launchIntent = pm.getLaunchIntentForPackage(app.packageName);

                appsList.add(new GameAppInfo(label, app.packageName, icon, launchIntent));
                addedPackages.add(app.packageName);
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
                            ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);
                            String label = pm.getApplicationLabel(appInfo).toString();
                            Drawable icon = pm.getApplicationIcon(appInfo);
                            Intent launchIntent = pm.getLaunchIntentForPackage(pkgName);
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

    private static boolean isPackageGame(Context context, String pkgName, String label, ApplicationInfo appInfo, Set<String> customPkgs) {
        if (pkgName == null) return false;
        String pkgLower = pkgName.toLowerCase();
        String labelLower = label != null ? label.toLowerCase() : "";

        // Filter out our own booster app
        if (context != null && pkgName.equalsIgnoreCase(context.getPackageName())) {
            return false;
        }

        // Check registry or custom list
        if (customPkgs.contains(pkgName) || GamePackageRegistry.isKnownGame(pkgName)) {
            return true;
        }

        // Check Android system game flags/categories
        if (appInfo != null) {
            if (appInfo.category == ApplicationInfo.CATEGORY_GAME || (appInfo.flags & ApplicationInfo.FLAG_IS_GAME) != 0) {
                return true;
            }
        }

        // Package name keyword check
        if (pkgLower.contains("mobilelegends") || pkgLower.contains("mobile.legends") ||
            pkgLower.contains("pubg") || pkgLower.contains("cod") || pkgLower.contains("callofduty") ||
            pkgLower.contains("freefire") || pkgLower.contains("genshin") || pkgLower.contains("roblox") ||
            pkgLower.contains("wildrift") || pkgLower.contains("league") || pkgLower.contains("sgameglobal") ||
            pkgLower.contains("honorofkings") || pkgLower.contains("supercell") || pkgLower.contains("brawlstars") ||
            pkgLower.contains("clashroyale") || pkgLower.contains("clashofclans") || pkgLower.contains("ea.gp") ||
            pkgLower.contains("garena") || pkgLower.contains("tencent") || pkgLower.contains("netease") ||
            pkgLower.contains("hoyoverse") || pkgLower.contains("mihoyo") || pkgLower.contains("bloodstrike") ||
            pkgLower.contains("farlight") || pkgLower.contains("game")) {
            return true;
        }

        // App Label keyword check
        if (labelLower.contains("mobile legends") || labelLower.contains("mlbb") ||
            labelLower.contains("call of duty") || labelLower.contains("pubg") ||
            labelLower.contains("free fire") || labelLower.contains("wild rift") ||
            labelLower.contains("genshin") || labelLower.contains("roblox") ||
            labelLower.contains("honor of kings") || labelLower.contains("arena of valor") ||
            labelLower.contains("blood strike") || labelLower.contains("farlight") ||
            labelLower.contains("brawl stars") || labelLower.contains("clash") ||
            labelLower.contains("efootball") || labelLower.contains("asphalt")) {
            return true;
        }

        return false;
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
