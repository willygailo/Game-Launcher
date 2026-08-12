package com.gamebooster.app.feature.gameprofiles.automation;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;

/** Applies launcher-owned per-game preferences through Android's supported APIs. */
public final class GameConfigAutoEngine {
    private static final String TAG = "GameConfigAutoEngine";

    private GameConfigAutoEngine() { }

    public static void autoApplyGameConfigAsync(Context context, String packageName) {
        if (context == null || packageName == null || packageName.trim().isEmpty()) return;
        AppExecutors.getInstance().executeCommand(() -> {
            int target = GameProfileAutoConfigurator.resolveGameHz(context, packageName);
            boolean applied = GameProfileAutoConfigurator.autoConfigGamePackage(context, packageName, target);
            Log.i(TAG, "Applied launcher-owned profile for " + packageName + ": " + applied);
        });
    }
}
