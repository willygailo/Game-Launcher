package com.gamebooster.app.shizuku;

import android.content.Context;

import com.gamebooster.app.engine.DisplayOverrideController;

/**
 * Legacy entry point retained for the settings screen and boot receiver.
 * It now performs one verified native display-rate request instead of writing persistent
 * properties, private SurfaceFlinger transactions, thermal controls, or global game entries.
 */
public final class ShizukuForceApplyEngine {
    private ShizukuForceApplyEngine() { }

    public static final class ForceApplyResult {
        public final boolean success;
        public final int totalCommands;
        public final String outputLog;

        public ForceApplyResult(boolean success, int totalCommands, String outputLog) {
            this.success = success;
            this.totalCommands = totalCommands;
            this.outputLog = outputLog;
        }
    }

    public static ForceApplyResult forceApplyAll(Context context, int targetHz) {
        DisplayOverrideController.Result result =
                DisplayOverrideController.applyDisplayRate(context, targetHz, null);
        if (result.isSuccess() && context != null) {
            ForceApplyPreferences.setForceApplied(context, true, result.selectedHz);
        }
        return new ForceApplyResult(result.isSuccess(), 1, result.message);
    }
}
