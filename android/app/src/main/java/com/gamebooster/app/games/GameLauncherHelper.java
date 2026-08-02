package com.gamebooster.app.games;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.gamebooster.app.functions.PerformanceChannel;

import java.util.HashSet;
import java.util.Set;

public class GameLauncherHelper {

    private static final String PREF_NAME = "custom_game_library_prefs";
    private static final String KEY_CUSTOM_PACKAGES = "custom_game_packages";

    public static void launchGameWithAutoBoost(Context context, GameAppInfo game) {
        if (context == null || game == null) return;

        String pkgName = game.getPackageName();
        if (pkgName == null || pkgName.trim().isEmpty()) return;

        // 1. Offload background optimizations to AppExecutors so launch is instant
        com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
            try {
                com.gamebooster.app.shizuku.ShizukuExecutor.grantAppPermissionsViaShizuku(context);
                int targetFps = GameProfileAutoConfigurator.getTargetFpsHz(context);
                GameProfileAutoConfigurator.autoConfigGamePackage(context, pkgName, targetFps);
                PerformanceChannel.executeOneTapBoost(context);
                com.gamebooster.app.functions.NetworkOptimizer.flushDnsCache();
            } catch (Throwable ignored) {}
        });

        // 2. Perform 3-Tier Game Launch Fallback
        boolean launched = false;

        // Tier 1: Standard Intent Launch
        Intent launchIntent = game.getLaunchIntent();
        if (launchIntent != null) {
            try {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                context.startActivity(launchIntent);
                launched = true;
            } catch (Throwable ignored) {}
        }

        // Tier 2: PackageManager Re-query
        if (!launched) {
            try {
                Intent pmIntent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
                if (pmIntent != null) {
                    pmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    context.startActivity(pmIntent);
                    launched = true;
                }
            } catch (Throwable ignored) {}
        }

        // Tier 3: Shizuku ADB Direct Launch Fallback (monkey -p <pkg> 1)
        if (!launched) {
            try {
                String res = com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand("monkey -p " + pkgName + " -c android.intent.category.LAUNCHER 1");
                if (res != null && !res.startsWith("ERROR")) {
                    launched = true;
                }
            } catch (Throwable ignored) {}
        }

        if (launched) {
            Toast.makeText(context, "⚡ LAUNCHED: " + game.getLabel() + " Boosted!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Unable to launch " + game.getLabel(), Toast.LENGTH_SHORT).show();
        }
    }

    public static Set<String> getCustomPackages(Context context) {
        if (context == null) return new HashSet<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return new HashSet<>(prefs.getStringSet(KEY_CUSTOM_PACKAGES, new HashSet<>()));
    }

    public static void addCustomPackage(Context context, String packageName) {
        if (context == null || packageName == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_CUSTOM_PACKAGES, new HashSet<>()));
        set.add(packageName);
        prefs.edit().putStringSet(KEY_CUSTOM_PACKAGES, set).apply();
    }

    public static void removeCustomPackage(Context context, String packageName) {
        if (context == null || packageName == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_CUSTOM_PACKAGES, new HashSet<>()));
        set.remove(packageName);
        prefs.edit().putStringSet(KEY_CUSTOM_PACKAGES, set).apply();
    }
}
