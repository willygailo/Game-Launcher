package com.gamebooster.app.booster;

import android.content.Context;

/** Public, capability-checked display preference channel. */
public final class HzFpsChannel {

    private HzFpsChannel() {}

    public static final class RefreshRateResult {
        public final boolean success;
        public final int requestedHz;
        public final int appliedHz;
        public final String message;

        private RefreshRateResult(boolean success, int requestedHz, int appliedHz, String message) {
            this.success = success;
            this.requestedHz = requestedHz;
            this.appliedHz = appliedHz;
            this.message = message;
        }

        public static RefreshRateResult success(int requestedHz, int appliedHz) {
            String note = requestedHz == appliedHz ? "Applied " + appliedHz + "Hz"
                    : "Applied supported " + appliedHz + "Hz instead of requested " + requestedHz + "Hz";
            return new RefreshRateResult(true, requestedHz, appliedHz, note);
        }

        public static RefreshRateResult unsupported(int requestedHz, int maxHz) {
            return new RefreshRateResult(false, requestedHz, 0,
                    requestedHz + "Hz is not supported on this device (max " + maxHz + "Hz)");
        }

        public static RefreshRateResult failed(int requestedHz, int appliedHz) {
            return new RefreshRateResult(false, requestedHz, appliedHz,
                    "Android did not allow the " + appliedHz + "Hz setting. Allow Modify system settings or select it in Android Display settings.");
        }
    }

    /**
     * Backwards-compatible entry point. A request is always constrained to a
     * physical mode reported by Android; it does not alter a game's FPS cap.
     */
    public static RefreshRateResult forceSetRefreshRate(Context context, int requestedHz) {
        return setRefreshRate(context, requestedHz);
    }

    /** Applies only a physical display mode exposed by Android. */
    public static RefreshRateResult setRefreshRate(Context context, int requestedHz) {
        if (context == null) return RefreshRateResult.failed(requestedHz, 0);

        int targetHz = com.gamebooster.app.device.HardwareDisplayController
                .resolveSupportedRefreshRate(context, requestedHz);
        if (targetHz <= 0) return RefreshRateResult.failed(requestedHz, 0);
        if (requestedHz > 0 && targetHz != requestedHz) {
            return RefreshRateResult.unsupported(requestedHz,
                    Math.round(com.gamebooster.app.device.HardwareDisplayController.getMaxHardwareRefreshRate(context)));
        }

        boolean applied = com.gamebooster.app.device.HardwareDisplayController
                .forceUnlockSystemRefreshRate(context, targetHz);
        return applied ? RefreshRateResult.success(requestedHz, targetHz)
                : RefreshRateResult.failed(requestedHz, targetHz);
    }

    /** Third-party apps control their own frame pacing and FPS limits. */
    public static boolean forceGameFps(Context context, String packageName, int targetFps) {
        return false;
    }
}
