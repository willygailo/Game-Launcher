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

        try {
            // 1. Shizuku ADB Permission Combo grant
            com.gamebooster.app.shizuku.ShizukuExecutor.grantAppPermissionsViaShizuku(context);

            // 2. Target FPS / Hz refresh rate lock & Game Mode API
            int targetFps = GameProfileAutoConfigurator.getTargetFpsHz(context);
            GameProfileAutoConfigurator.autoConfigGamePackage(context, game.getPackageName(), targetFps);

            // 3. One-tap system tweaks & GPU Vulkan renderer
            PerformanceChannel.executeOneTapBoost(context);
            com.gamebooster.app.functions.NetworkOptimizer.flushDnsCache();

            Toast.makeText(context, "⚡ SHIZUKU COMBO BOOSTED: Launching " + game.getLabel() + "...", Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {}

        // Launch game intent
        Intent launchIntent = game.getLaunchIntent();
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
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
