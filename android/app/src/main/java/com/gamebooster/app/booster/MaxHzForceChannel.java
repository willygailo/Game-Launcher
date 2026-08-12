package com.gamebooster.app.booster;

import android.content.Context;

import com.gamebooster.app.engine.DisplayOverrideController;

/**
 * Compatibility facade for the former force-Hz API.
 *
 * <p>It deliberately no longer attempts unsupported panel modes, private SurfaceFlinger
 * transaction IDs, thermal bypasses, or unverified system properties. Existing callers keep
 * their API while receiving a capability-gated, reversible override.</p>
 */
public final class MaxHzForceChannel {
    public static final String PREFS_HZ_STATE = "hz_state";
    public static final String PREFS_KEY_HZ = "current_hz";
    public static final String PREFS_KEY_GAME = "current_game_pkg";
    public static final String PREFS_KEY_LOCKED = "hz_locked";

    private MaxHzForceChannel() { }

    public static final class ForceResult {
        public final boolean success;
        public final int appliedHz;
        public final int successCount;
        public final int failCount;
        public final int totalCommands;
        public final String message;

        private ForceResult(boolean success, int appliedHz, String message) {
            this.success = success;
            this.appliedHz = appliedHz;
            this.successCount = success ? 1 : 0;
            this.failCount = success ? 0 : 1;
            this.totalCommands = 1;
            this.message = message;
        }
    }

    public static ForceResult forceApply(Context context, int targetHz, String gamePkg) {
        DisplayOverrideController.Result result = DisplayOverrideController.applyDisplayRate(context, targetHz, gamePkg);
        if (context != null && result.isSuccess()) {
            context.getApplicationContext().getSharedPreferences(PREFS_HZ_STATE, Context.MODE_PRIVATE).edit()
                    .putInt(PREFS_KEY_HZ, result.selectedHz)
                    .putString(PREFS_KEY_GAME, gamePkg == null ? "" : gamePkg)
                    .apply();
        }
        return new ForceResult(result.isSuccess(), result.selectedHz, result.message);
    }

    /** Deprecated because a context is required to verify native display modes. */
    public static ForceResult forceApply(int targetHz) {
        return new ForceResult(false, 0, "A device context is required to verify supported display modes.");
    }

    public static ForceResult lockHz(Context context, int targetHz, String gamePkg) {
        ForceResult result = forceApply(context, targetHz, gamePkg);
        if (context != null && result.success) {
            context.getApplicationContext().getSharedPreferences(PREFS_HZ_STATE, Context.MODE_PRIVATE).edit()
                    .putBoolean(PREFS_KEY_LOCKED, true).apply();
        }
        return result;
    }

    public static boolean unlockHz(Context context) {
        DisplayOverrideController.Result result = DisplayOverrideController.restore(context);
        if (context != null && result.isSuccess()) {
            context.getApplicationContext().getSharedPreferences(PREFS_HZ_STATE, Context.MODE_PRIVATE).edit()
                    .putBoolean(PREFS_KEY_LOCKED, false).remove(PREFS_KEY_HZ).remove(PREFS_KEY_GAME).apply();
        }
        return result.isSuccess();
    }

    public static boolean isHzLocked(Context context) {
        return context != null && context.getApplicationContext().getSharedPreferences(PREFS_HZ_STATE, Context.MODE_PRIVATE)
                .getBoolean(PREFS_KEY_LOCKED, false);
    }

    public static int getLockedHz(Context context) {
        return context == null ? 0 : context.getApplicationContext().getSharedPreferences(PREFS_HZ_STATE, Context.MODE_PRIVATE)
                .getInt(PREFS_KEY_HZ, 0);
    }
}
