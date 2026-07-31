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
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        java.util.Set<String> customPkgs = GameLauncherHelper.getCustomPackages(context);

        for (ApplicationInfo app : apps) {
            Intent launchIntent = pm.getLaunchIntentForPackage(app.packageName);
            if (launchIntent == null) continue;

            boolean isGame = customPkgs.contains(app.packageName);
            if (!isGame && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                if (app.category == ApplicationInfo.CATEGORY_GAME) {
                    isGame = true;
                }
            }

            if (!isGame && (app.flags & ApplicationInfo.FLAG_IS_GAME) != 0) {
                isGame = true;
            }

            String pkgNameLower = app.packageName.toLowerCase();
            if (!isGame && (pkgNameLower.contains("game") || pkgNameLower.contains("pubg") ||
                pkgNameLower.contains("mobilelegends") || pkgNameLower.contains("freefire") ||
                pkgNameLower.contains("genshin") || pkgNameLower.contains("roblox") ||
                pkgNameLower.contains("cod") || pkgNameLower.contains("minecraft") ||
                pkgNameLower.contains("apex") || pkgNameLower.contains("ea.gp"))) {
                isGame = true;
            }

            if (isGame) {
                CharSequence label = pm.getApplicationLabel(app);
                Drawable icon = pm.getApplicationIcon(app);
                gamesList.add(new GameAppInfo(label.toString(), app.packageName, icon, launchIntent));
            }
        }
        return gamesList;
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
