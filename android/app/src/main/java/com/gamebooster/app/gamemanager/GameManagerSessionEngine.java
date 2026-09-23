package com.gamebooster.app.gamemanager;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.booster.HzFpsChannel;
import com.gamebooster.app.config.GameProfilePreferences;
import com.gamebooster.app.config.GameSessionSettings;
import com.gamebooster.app.gamespace.GameStateReverter;

/**
 * Tracks a game-launcher session and applies only user-authorized display
 * preferences. CPU/GPU policy, thermal control, game data, identity, and input
 * remain managed by Android, the OEM, and the launched game.
 */
public final class GameManagerSessionEngine {

    private static final String TAG = "GameManagerSession";

    private GameManagerSessionEngine() {}

    /** Must be called off the main thread. */
    public static void beginSession(Context context, String packageName) {
        if (context == null || packageName == null || packageName.trim().isEmpty()) return;
        Context appContext = context.getApplicationContext();
        String packageId = packageName.trim();

        GameSessionSettings.begin(appContext, packageId);
        GameManagerStatus.getInstance().setActiveSession(packageId);

        int targetHz = GameProfilePreferences.getTargetHz(appContext, packageId);
        HzFpsChannel.RefreshRateResult displayResult = HzFpsChannel.setRefreshRate(appContext, targetHz);
        String summary = "Game session started for " + packageId + ". " + displayResult.message
                + ". Game FPS is controlled by the game and device.";
        GameManagerStatus.getInstance().recordApply(displayResult.success ? 1 : 0, summary);
        Log.i(TAG, summary);
    }

    /** Clears launcher session state and leaves OEM performance policy untouched. */
    public static void endSession(Context context, String packageName) {
        if (context == null) return;
        GameStateReverter.revertToBaseline(context.getApplicationContext());
        GameManagerStatus.getInstance().setActiveSession(null);
        Log.i(TAG, "Game session ended for " + packageName);
    }

    public static boolean isSessionActive(Context context) {
        return context != null && (GameSessionSettings.hasActiveSession(context)
                || GameManagerStatus.getInstance().hasActiveSession());
    }
}
