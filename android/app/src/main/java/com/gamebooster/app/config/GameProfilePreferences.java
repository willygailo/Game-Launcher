package com.gamebooster.app.config;

import android.content.Context;

import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.booster.PerformanceChannel;

/** Persists a device-safe performance choice for every game package. */
public final class GameProfilePreferences {

    private static final String PREF_NAME = "per_game_performance_profiles";
    private static final String KEY_PROFILE_PREFIX = "profile_";

    public enum Profile {
        BALANCED("Balanced", 90, false, PerformanceChannel.Profile.BALANCED),
        COMPETITIVE("Competitive 144Hz", 144, true, PerformanceChannel.Profile.PERFORMANCE),
        MAX_SUPPORTED("Max 165Hz Extreme", 165, true, PerformanceChannel.Profile.EXTREME_PERFORMANCE);

        public final String label;
        private final int requestedHz;
        public final boolean enableDnd;
        public final PerformanceChannel.Profile performanceProfile;

        Profile(String label, int requestedHz, boolean enableDnd,
                PerformanceChannel.Profile performanceProfile) {
            this.label = label;
            this.requestedHz = requestedHz;
            this.enableDnd = enableDnd;
            this.performanceProfile = performanceProfile;
        }

        int resolveTargetHz(DevicePerformanceCapabilities capabilities) {
            return capabilities.resolveRefreshRate(requestedHz);
        }
    }

    private GameProfilePreferences() {}

    public static Profile getProfile(Context context, String packageName) {
        if (context == null || packageName == null) return Profile.COMPETITIVE;
        String stored = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_PROFILE_PREFIX + packageName, Profile.COMPETITIVE.name());
        try {
            return Profile.valueOf(stored);
        } catch (IllegalArgumentException ignored) {
            return Profile.COMPETITIVE;
        }
    }

    public static void setProfile(Context context, String packageName, Profile profile) {
        if (context == null || packageName == null || packageName.trim().isEmpty() || profile == null) return;
        context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PROFILE_PREFIX + packageName, profile.name())
                .apply();
    }

    public static int getTargetHz(Context context, String packageName) {
        return getTargetHz(context, getProfile(context, packageName));
    }

    public static int getTargetHz(Context context, Profile profile) {
        return profile.resolveTargetHz(DevicePerformanceCapabilities.detect(context));
    }

    public static String getSummary(Context context, String packageName) {
        Profile profile = getProfile(context, packageName);
        return "PROFILE: " + profile.label.toUpperCase() + " • UP TO "
                + getTargetHz(context, packageName) + "Hz";
    }
}
