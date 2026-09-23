package com.gamebooster.app.booster;

import android.content.Context;

import com.gamebooster.app.config.GameProfileAutoConfigurator;

/**
 * Applies only a capability-checked display preference. CPU/GPU governors,
 * thermal policy, and another app's renderer remain owned by Android and the
 * device vendor.
 */
public final class PerformanceChannel {

    private PerformanceChannel() {}

    public enum Profile {
        EXTREME_PERFORMANCE("Display maximum"),
        PERFORMANCE("High refresh"),
        BALANCED("Balanced");

        public final String title;

        Profile(String title) {
            this.title = title;
        }
    }

    public static final class ProfileResult {
        public final boolean refreshRateApplied;
        public final int appliedHz;
        public final String message;

        private ProfileResult(boolean refreshRateApplied, int appliedHz, String message) {
            this.refreshRateApplied = refreshRateApplied;
            this.appliedHz = appliedHz;
            this.message = message;
        }
    }

    public static boolean applyProfile(Context context, Profile profile) {
        return applyProfileWithResult(context, profile).refreshRateApplied;
    }

    public static ProfileResult applyProfileWithResult(Context context, Profile profile) {
        if (context == null) return new ProfileResult(false, 0, "Device context is unavailable");
        int targetHz = GameProfileAutoConfigurator.getTargetFpsHz(context);
        HzFpsChannel.RefreshRateResult result = HzFpsChannel.setRefreshRate(context, targetHz);
        String label = profile == null ? Profile.BALANCED.title : profile.title;
        return new ProfileResult(result.success, result.appliedHz, label + ": " + result.message);
    }

    /** Deprecated: performance shell scripts are not executed by the launcher. */
    public static boolean writeAndExecutePerformanceTweaksScript() {
        return false;
    }

    /** Deprecated: performance shell scripts are not executed by the launcher. */
    public static boolean writeAndExecuteRootTweaksScript() {
        return false;
    }

    /** Deprecated: performance shell scripts are not executed by the launcher. */
    public static boolean writeAndExecuteRootTweaksScript(int targetHz) {
        return false;
    }

    /** Deprecated: performance shell scripts are not executed by the launcher. */
    public static boolean writeAndExecutePerformanceTweaksScript(int targetHz) {
        return false;
    }

    /** The launcher does not change a device-wide GPU rendering backend. */
    public static boolean setGpuRenderMode(boolean is3D) {
        return false;
    }

    public static boolean executeOneTapBoost(Context context) {
        return applyProfile(context, Profile.PERFORMANCE);
    }
}
