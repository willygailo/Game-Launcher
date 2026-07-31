package com.gamebooster.app.games;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.functions.HzFpsChannel;
import com.gamebooster.app.functions.TweakPreferences;
import com.gamebooster.app.root.CommandExecutor;

import java.util.List;

public class GameProfileAutoConfigurator {

    private static final String TAG = "GameAutoConfigurator";
    public static final String KEY_TARGET_HZ_FPS = "user_target_hz_fps";
    public static final int DEFAULT_TARGET_HZ = 120;

    public interface OnAutoConfigListener {
        void onAutoConfigCompleted(int gamesConfiguredCount, int targetFpsHz);
    }

    public static void setTargetFpsHz(Context context, int targetFpsHz) {
        if (context == null) return;
        context.getApplicationContext()
                .getSharedPreferences("game_booster_tweak_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_TARGET_HZ_FPS, targetFpsHz)
                .apply();
    }

    public static int getTargetFpsHz(Context context) {
        if (context == null) return DEFAULT_TARGET_HZ;
        return context.getApplicationContext()
                .getSharedPreferences("game_booster_tweak_prefs", Context.MODE_PRIVATE)
                .getInt(KEY_TARGET_HZ_FPS, DEFAULT_TARGET_HZ);
    }

    public static boolean autoConfigGamePackage(Context context, String packageName, int targetFpsHz) {
        if (packageName == null || packageName.trim().isEmpty()) return false;

        Log.d(TAG, "Configuring " + packageName + " for " + targetFpsHz + " FPS / Hz...");

        // 1. Android Game Mode API Performance tuning
        CommandExecutor.executeSystemCommand("cmd game mode performance " + packageName);

        // 2. Per-app max refresh rate override
        CommandExecutor.executeSystemCommand("cmd window set-app-refresh-rate " + packageName + " " + targetFpsHz);

        // 3. System-wide refresh rate peak/min lock
        HzFpsChannel.setRefreshRate((float) targetFpsHz);

        // 4. SurfaceFlinger high FPS offset
        CommandExecutor.executeSystemCommand("setprop debug.sf.high_fps_early_phase_offset_ns 1");
        CommandExecutor.executeSystemCommand("setprop debug.hwui.renderer vulkan");

        // 5. In-game config file sed/ini patch
        GameConfigPatcher.applyGameFpsPatch(packageName, targetFpsHz);

        return true;
    }

    public static void autoConfigAllInstalledGamesAsync(Context context, OnAutoConfigListener listener) {
        if (context == null) return;

        final int targetFpsHz = getTargetFpsHz(context);

        AppExecutors.getInstance().executeCommand(() -> {
            List<GameAppInfo> games = GameManagerRepository.getInstalledGames(context);
            int configuredCount = 0;

            for (GameAppInfo game : games) {
                if (autoConfigGamePackage(context, game.getPackageName(), targetFpsHz)) {
                    configuredCount++;
                }
            }

            final int finalCount = configuredCount;
            if (listener != null) {
                AppExecutors.getInstance().postToMainThread(() ->
                        listener.onAutoConfigCompleted(finalCount, targetFpsHz));
            }
        });
    }
}
