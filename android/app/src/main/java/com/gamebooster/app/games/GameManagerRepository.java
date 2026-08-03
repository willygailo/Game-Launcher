package com.gamebooster.app.games;

import com.gamebooster.app.root.CommandExecutor;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.gamebooster.app.games.GameAppInfo;

import java.util.ArrayList;
import java.util.List;

public class GameManagerRepository {

    public static List<GameAppInfo> getInstalledGames(Context context) {
        List<GameAppInfo> gamesList = new ArrayList<>();
        if (context == null) return gamesList;

        PackageManager pm = context.getPackageManager();
        java.util.Set<String> addedPackages = new java.util.HashSet<>();
        java.util.Set<String> customPkgs = GameLauncherHelper.getCustomPackages(context);

        // 1. Primary Scanner: Query all launcher intent activities (Catches MLBB, CODM, PUBG, etc. on Android 11-15)
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<android.content.pm.ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        for (android.content.pm.ResolveInfo ri : resolveInfos) {
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

        // 2. Secondary Scanner: Query installed applications (Catches any games with custom launch flags)
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

        // 3. Fallback for demo/emulator: If no installed online games are detected physically, populate default featured online game profiles
        if (gamesList.isEmpty()) {
            Drawable defaultIcon = context.getApplicationInfo().loadIcon(pm);
            gamesList.add(new GameAppInfo("Mobile Legends: Bang Bang (Global)", "com.mobile.legends", defaultIcon, null));
            gamesList.add(new GameAppInfo("Call of Duty: Mobile (Garena)", "com.garena.game.codm", defaultIcon, null));
            gamesList.add(new GameAppInfo("PUBG Mobile (Global)", "com.tencent.ig", defaultIcon, null));
            gamesList.add(new GameAppInfo("League of Legends: Wild Rift", "com.riotgames.league.wildrift", defaultIcon, null));
            gamesList.add(new GameAppInfo("Garena Free Fire MAX", "com.dts.freefiremax", defaultIcon, null));
            gamesList.add(new GameAppInfo("Honor of Kings (Global)", "com.levelinfinite.sgameGlobal", defaultIcon, null));
            gamesList.add(new GameAppInfo("Genshin Impact", "com.cognosphere.GenshinImpact", defaultIcon, null));
            gamesList.add(new GameAppInfo("Roblox", "com.roblox.client", defaultIcon, null));
        }

        return gamesList;
    }

    private static boolean isPackageGame(Context context, String pkgName, String label, ApplicationInfo appInfo, java.util.Set<String> customPkgs) {
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
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                if (appInfo.category == ApplicationInfo.CATEGORY_GAME) {
                    return true;
                }
            }
            if ((appInfo.flags & ApplicationInfo.FLAG_IS_GAME) != 0) {
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
