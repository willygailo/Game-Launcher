package com.gamebooster.app.games;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.config.CfgProfileManager;
import com.gamebooster.app.config.CompetitiveCfgProfile;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.config.GameProfilePreferences;
import com.gamebooster.app.config.GameSessionSettings;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.gamespace.AutoGameMonitorService;
import com.gamebooster.app.gamespace.GameSpaceDndManager;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuManager;
import com.gamebooster.app.shizuku.ShizukuPermissionEnforcer;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;

import java.util.HashSet;
import java.util.Set;

public class GameLauncherHelper {

    private static final String PREF_NAME = "custom_game_library_prefs";
    private static final String KEY_CUSTOM_PACKAGES = "custom_game_packages";
    private static final String KEY_EXCLUDED_PACKAGES = "excluded_game_packages";

    public static void autoLaunchGame(Context context, GameAppInfo game) {
        if (context == null || game == null) return;
        com.gamebooster.app.gamemanager.GameManagerLauncher.launchGame(context, game);
    }

    public static void launchGameWithAutoBoost(Context context, GameAppInfo game) {
        autoLaunchGame(context, game);
    }

    private static int targetFpsForToast(Context context, String packageName) {
        return GameProfilePreferences.getTargetHz(context, packageName);
    }

    public static Set<String> getCustomPackages(Context context) {
        if (context == null) return new HashSet<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return new HashSet<>(prefs.getStringSet(KEY_CUSTOM_PACKAGES, new HashSet<>()));
    }

    public static void addCustomPackage(Context context, String packageName) {
        if (context == null || packageName == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> customSet = new HashSet<>(prefs.getStringSet(KEY_CUSTOM_PACKAGES, new HashSet<>()));
        customSet.add(packageName);
        Set<String> excludedSet = new HashSet<>(prefs.getStringSet(KEY_EXCLUDED_PACKAGES, new HashSet<>()));
        excludedSet.remove(packageName); // Un-exclude if it was previously excluded
        prefs.edit()
                .putStringSet(KEY_CUSTOM_PACKAGES, customSet)
                .putStringSet(KEY_EXCLUDED_PACKAGES, excludedSet)
                .apply();
    }

    public static void removeCustomPackage(Context context, String packageName) {
        if (context == null || packageName == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_CUSTOM_PACKAGES, new HashSet<>()));
        set.remove(packageName);
        prefs.edit().putStringSet(KEY_CUSTOM_PACKAGES, set).apply();
    }

    public static Set<String> getExcludedPackages(Context context) {
        if (context == null) return new HashSet<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return new HashSet<>(prefs.getStringSet(KEY_EXCLUDED_PACKAGES, new HashSet<>()));
    }

    public static void excludePackage(Context context, String packageName) {
        if (context == null || packageName == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_EXCLUDED_PACKAGES, new HashSet<>()));
        set.add(packageName);
        prefs.edit().putStringSet(KEY_EXCLUDED_PACKAGES, set).apply();
    }

    public static void unexcludePackage(Context context, String packageName) {
        if (context == null || packageName == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_EXCLUDED_PACKAGES, new HashSet<>()));
        set.remove(packageName);
        prefs.edit().putStringSet(KEY_EXCLUDED_PACKAGES, set).apply();
    }

    public static void removeGameFromHome(Context context, String packageName) {
        if (context == null || packageName == null) return;
        removeCustomPackage(context, packageName);
        excludePackage(context, packageName);
    }

    public static int clearAllCustomPackages(Context context) {
        if (context == null) return 0;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> customSet = prefs.getStringSet(KEY_CUSTOM_PACKAGES, new HashSet<>());
        int count = customSet.size();
        prefs.edit().remove(KEY_CUSTOM_PACKAGES).apply();
        return count;
    }

    public static void resetAllExcludedPackages(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_EXCLUDED_PACKAGES).apply();
    }
}
