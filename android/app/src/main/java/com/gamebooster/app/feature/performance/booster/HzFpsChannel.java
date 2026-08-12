package com.gamebooster.app.feature.performance.booster;
import android.content.Context;
import com.gamebooster.app.feature.performance.display.DisplayOverrideController;

public class HzFpsChannel {

    private static final String TAG = "HzFpsChannel";

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
                    "Android did not allow the " + appliedHz + "Hz setting. Connect Shizuku or allow Modify system settings.");
        }
    }

    /** Kept for callers; unsupported rates are rejected rather than force-written. */
    public static RefreshRateResult forceSetRefreshRate(Context context, int requestedHz) {
        return setRefreshRate(context, requestedHz);
    }

    /**
     * Applies only a refresh rate exposed by Android for the current display.
     * This controls display refresh rate, not an individual game's internal FPS cap.
     */
    public static RefreshRateResult setRefreshRate(Context context, int requestedHz) {
        DisplayOverrideController.Result result = DisplayOverrideController.applyDisplayRate(context, requestedHz, null);
        return result.isSuccess() ? RefreshRateResult.success(requestedHz, result.selectedHz)
                : RefreshRateResult.failed(requestedHz, result.selectedHz);
    }

    public static boolean forceGameFps(Context context, String packageName, int targetFps) {
        return DisplayOverrideController.applyGameProfile(context, packageName, targetFps).isSuccess();
    }
}
