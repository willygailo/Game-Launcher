package com.gamebooster.app.booster;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * HzFpsChannel routes display refresh rate requests and per-game FPS forcing.
 * Consolidates refresh rate forcing directly through MaxHzForceChannel for zero duplication.
 */
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
                    "Android did not allow the " + appliedHz + "Hz setting. Connect Shizuku or grant Write Secure Settings.");
        }
    }

    /**
     * Forces the display to {@code requestedHz} via Shizuku/API with NO capability check and NO fallback.
     */
    public static RefreshRateResult forceSetRefreshRate(Context context, int requestedHz) {
        if (context == null) return RefreshRateResult.failed(requestedHz, 0);
        MaxHzForceChannel.ForceResult r = MaxHzForceChannel.forceApply(requestedHz);
        return r.success
                ? RefreshRateResult.success(requestedHz, r.appliedHz)
                : RefreshRateResult.failed(requestedHz, requestedHz);
    }

    /**
     * Applies display refresh rate for the given requested Hz, using MaxHzForceChannel for execution.
     */
    public static RefreshRateResult setRefreshRate(Context context, int requestedHz) {
        if (context == null) return RefreshRateResult.failed(requestedHz, 0);

        DevicePerformanceCapabilities capabilities = DevicePerformanceCapabilities.detect(context);
        if (!capabilities.supportsRefreshRate(requestedHz)) {
            return RefreshRateResult.unsupported(requestedHz, capabilities.getMaxRefreshRate());
        }

        MaxHzForceChannel.ForceResult result = MaxHzForceChannel.forceApply(requestedHz);
        return result.success 
                ? RefreshRateResult.success(requestedHz, requestedHz)
                : RefreshRateResult.failed(requestedHz, requestedHz);
    }

    /**
     * Forces internal game frame rate cap and window manager refresh rate for a target package.
     */
    public static boolean forceGameFps(Context context, String packageName, int targetFps) {
        if (packageName == null || packageName.isEmpty()) return false;
        
        boolean ok = true;
        // 1. Android Game Mode API per-app FPS set
        String cmdFps = "cmd game set --fps " + targetFps + " " + packageName;
        String resFps = ShizukuExecutor.hasShizukuPermission() 
                ? ShizukuExecutor.executeShizukuCommand(cmdFps)
                : CommandExecutor.executeSystemCommand(cmdFps);
        ok &= CommandExecutor.isSuccessOutput(resFps);

        // 2. Android Window Manager per-app refresh rate override
        String windowCmd = "cmd window set-app-refresh-rate " + packageName + " " + targetFps;
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommand(windowCmd);
            ShizukuExecutor.executeShizukuCommand("cmd game mode performance " + packageName);
        } else {
            CommandExecutor.executeSystemCommand(windowCmd);
            CommandExecutor.executeSystemCommand("cmd game mode performance " + packageName);
        }

        return ok;
    }
}
