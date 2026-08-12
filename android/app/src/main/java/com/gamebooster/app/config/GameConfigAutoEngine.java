package com.gamebooster.app.config;

import android.content.Context;
import android.util.Log;

/** Retained for source compatibility; third-party game config injection is disabled. */
public final class GameConfigAutoEngine {
    private static final String TAG = "GameConfigAutoEngine";

    private GameConfigAutoEngine() { }

    public static void autoApplyGameConfigAsync(Context context, String packageName) {
        Log.i(TAG, "Skipped unsupported game-file configuration for " + packageName);
    }
}
