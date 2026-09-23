package com.gamebooster.app.gamespace;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.config.GameSessionSettings;

/** Clears launcher-owned game-session state without changing OEM hardware policy. */
public final class GameStateReverter {

    private static final String TAG = "GameStateReverter";
    static final int DEFAULT_BASELINE_HZ = 60;

    private GameStateReverter() {}

    public static final class RevertReport {
        public final boolean sessionActive;
        public final boolean refreshRateRestored;
        public final boolean governorRestored;
        public final boolean thermalRestored;
        public final boolean networkRestored;
        public final boolean dndRestored;
        public final int restoredHz;
        public final String message;

        private RevertReport(boolean sessionActive, boolean refreshRateRestored,
                             boolean governorRestored, boolean thermalRestored,
                             boolean networkRestored, boolean dndRestored,
                             int restoredHz, String message) {
            this.sessionActive = sessionActive;
            this.refreshRateRestored = refreshRateRestored;
            this.governorRestored = governorRestored;
            this.thermalRestored = thermalRestored;
            this.networkRestored = networkRestored;
            this.dndRestored = dndRestored;
            this.restoredHz = restoredHz;
            this.message = message;
        }

        static RevertReport idle(int restoredHz, String message) {
            return new RevertReport(false, false, false, false, false, false, restoredHz, message);
        }

        static RevertReport active(int restoredHz, String message) {
            return new RevertReport(true, false, false, false, false, false, restoredHz, message);
        }
    }

    /** Pure decision logic used by callers and unit tests. */
    public static RevertReport evaluate(int previousHz, boolean previousDnd, boolean sessionActive) {
        if (!sessionActive) {
            return RevertReport.idle(0, "No active game session - nothing to revert");
        }
        int hz = previousHz > 0 ? previousHz : DEFAULT_BASELINE_HZ;
        return RevertReport.active(hz,
                "Game session cleared. Android and the device vendor retain their current performance policy.");
    }

    public static RevertReport revertToBaseline(Context context) {
        if (context == null) return RevertReport.idle(0, "No active game session - nothing to revert");
        boolean active = GameSessionSettings.hasActiveSession(context);
        RevertReport report = evaluate(GameSessionSettings.getStoredPreviousHz(context),
                GameSessionSettings.getStoredPreviousDnd(context), active);
        if (!active) return report;

        String activePackage = GameSessionSettings.getStoredActivePackage(context);
        GameSessionSettings.closeSession(context);
        try {
            if (activePackage != null) {
                GameSpaceAnalyticsManager.onSessionEnd(context, activePackage);
            }
        } catch (Throwable ignored) {
            // Session cleanup should remain best-effort.
        }
        Log.i(TAG, report.message);
        return report;
    }
}
