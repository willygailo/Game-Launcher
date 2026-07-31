package com.gamebooster.app.functions;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TweakPreferences {

    private static final String PREF_NAME = "game_booster_tweak_prefs";
    private static final String KEY_PREFIX_TWEAK = "tweak_applied_";

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveTweakState(Context context, String tweakId, boolean applied) {
        if (context == null || tweakId == null) return;
        getPrefs(context).edit().putBoolean(KEY_PREFIX_TWEAK + tweakId, applied).apply();
    }

    public static boolean isTweakApplied(Context context, String tweakId) {
        if (context == null || tweakId == null) return false;
        return getPrefs(context).getBoolean(KEY_PREFIX_TWEAK + tweakId, false);
    }

    public static Set<String> getAppliedTweakIds(Context context) {
        Set<String> appliedIds = new HashSet<>();
        if (context == null) return appliedIds;
        SharedPreferences prefs = getPrefs(context);
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(KEY_PREFIX_TWEAK) && Boolean.TRUE.equals(prefs.getAll().get(key))) {
                appliedIds.add(key.substring(KEY_PREFIX_TWEAK.length()));
            }
        }
        return appliedIds;
    }

    public static void loadSavedStates(Context context, List<TweakItem> tweaks) {
        if (context == null || tweaks == null) return;
        SharedPreferences prefs = getPrefs(context);
        for (TweakItem tweak : tweaks) {
            boolean applied = prefs.getBoolean(KEY_PREFIX_TWEAK + tweak.getId(), false);
            tweak.setApplied(applied);
        }
    }
}
