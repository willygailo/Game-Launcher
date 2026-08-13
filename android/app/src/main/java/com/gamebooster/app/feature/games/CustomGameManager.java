package com.gamebooster.app.feature.games;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * CustomGameManager allows users to register custom non-registry apps/games
 * into the Game Space monitor loop.
 */
public class CustomGameManager {

    private static final String PREF_NAME = "custom_games_prefs";
    private static final String KEY_CUSTOM_PACKAGES = "custom_packages_set";

    public static boolean addCustomGame(Context context, String packageName) {
        if (context == null || packageName == null || packageName.trim().isEmpty()) return false;
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> customSet = new HashSet<>(sp.getStringSet(KEY_CUSTOM_PACKAGES, new HashSet<>()));
        boolean added = customSet.add(packageName.trim().toLowerCase());
        if (added) {
            sp.edit().putStringSet(KEY_CUSTOM_PACKAGES, customSet).apply();
        }
        return added;
    }

    public static boolean removeCustomGame(Context context, String packageName) {
        if (context == null || packageName == null) return false;
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> customSet = new HashSet<>(sp.getStringSet(KEY_CUSTOM_PACKAGES, new HashSet<>()));
        boolean removed = customSet.remove(packageName.trim().toLowerCase());
        if (removed) {
            sp.edit().putStringSet(KEY_CUSTOM_PACKAGES, customSet).apply();
        }
        return removed;
    }

    public static boolean isCustomGame(Context context, String packageName) {
        if (context == null || packageName == null) return false;
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> customSet = sp.getStringSet(KEY_CUSTOM_PACKAGES, new HashSet<>());
        return customSet.contains(packageName.trim().toLowerCase());
    }

    public static Set<String> getCustomGames(Context context) {
        if (context == null) return new HashSet<>();
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return new HashSet<>(sp.getStringSet(KEY_CUSTOM_PACKAGES, new HashSet<>()));
    }
}
