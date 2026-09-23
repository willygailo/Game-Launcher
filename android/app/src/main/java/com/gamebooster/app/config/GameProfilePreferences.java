package com.gamebooster.app.config;

import android.content.Context;

import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.device.DevicePerformanceCapabilities;

/** Stores a per-game display preference constrained to physical display modes. */
public final class GameProfilePreferences {

    private static final String PREF_NAME = "per_game_performance_profiles";
    private static final String KEY_PROFILE_PREFIX = "profile_";
    private static final String KEY_TARGET_HZ_PREFIX = "target_hz_";
    private static final int FALLBACK_HZ = 60;

    public enum Profile {
        MAX_SUPPORTED("Display maximum", 0, true, PerformanceChannel.Profile.PERFORMANCE);

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
            return capabilities == null ? FALLBACK_HZ : fallbackToSystemDefault(capabilities.resolveRefreshRate(requestedHz));
        }
    }

    private GameProfilePreferences() {}

    public static Profile getProfile(Context context, String packageName) {
        return Profile.MAX_SUPPORTED;
    }

    public static void setProfile(Context context, String packageName, Profile profile) {
        if (context == null || packageName == null || packageName.trim().isEmpty() || profile == null) return;
        int targetHz = profile.resolveTargetHz(DevicePerformanceCapabilities.detect(context));
        context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PROFILE_PREFIX + packageName, profile.name())
                .putInt(KEY_TARGET_HZ_PREFIX + packageName, targetHz)
                .apply();
    }

    public static void setTargetHz(Context context, String packageName, int targetHz) {
        if (context == null || packageName == null || packageName.trim().isEmpty()) return;
        DevicePerformanceCapabilities capabilities = DevicePerformanceCapabilities.detect(context);
        int resolvedHz = fallbackToSystemDefault(capabilities.resolveRefreshRate(targetHz));
        context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_TARGET_HZ_PREFIX + packageName, resolvedHz)
                .apply();
    }

    /** Returns a display preference, never a claim about a game's internal FPS cap. */
    public static int getTargetHz(Context context, String packageName) {
        if (context == null || packageName == null || packageName.trim().isEmpty()) return FALLBACK_HZ;
        DevicePerformanceCapabilities capabilities = DevicePerformanceCapabilities.detect(context);
        int storedHz = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_TARGET_HZ_PREFIX + packageName, 0);
        return fallbackToSystemDefault(capabilities.resolveRefreshRate(storedHz));
    }

    public static int getTargetHz(Context context, Profile profile) {
        if (context == null) return FALLBACK_HZ;
        Profile resolvedProfile = profile != null ? profile : Profile.MAX_SUPPORTED;
        return resolvedProfile.resolveTargetHz(DevicePerformanceCapabilities.detect(context));
    }

    public static String getSummary(Context context, String packageName) {
        int targetHz = getTargetHz(context, packageName);
        return "Display preference: " + targetHz + "Hz • Game FPS is controlled by the game and device";
    }

    private static int fallbackToSystemDefault(int resolvedHz) {
        return resolvedHz > 0 ? resolvedHz : FALLBACK_HZ;
    }
}
