package com.gamebooster.app.config;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.*;
import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.booster.HzFpsChannel;
import com.gamebooster.app.config.TweakPreferences;
import com.gamebooster.app.engine.CommandExecutor;

import java.util.ArrayList;
import java.util.List;

public class GameProfileAutoConfigurator {

    private static final String TAG = "GameAutoConfigurator";
    public static final String KEY_TARGET_HZ_FPS = "user_target_hz_fps";
    public static final int DEFAULT_TARGET_HZ = 60;

    public interface OnAutoConfigListener {
        void onAutoConfigCompleted(int gamesConfiguredCount, int targetFpsHz);
    }

    public static void setTargetFpsHz(Context context, int targetFpsHz) {
        if (context == null) return;
        int supportedTarget = DevicePerformanceCapabilities.detect(context).resolveRefreshRate(targetFpsHz);
        context.getApplicationContext()
                .getSharedPreferences("game_booster_tweak_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_TARGET_HZ_FPS, supportedTarget)
                .apply();
    }

    public static int getTargetFpsHz(Context context) {
        if (context == null) return DEFAULT_TARGET_HZ;
        int storedTarget = context.getApplicationContext()
                .getSharedPreferences("game_booster_tweak_prefs", Context.MODE_PRIVATE)
                .getInt(KEY_TARGET_HZ_FPS, DEFAULT_TARGET_HZ);
        return DevicePerformanceCapabilities.detect(context).resolveRefreshRate(storedTarget);
    }

    public static List<Integer> getSupportedDisplayRefreshRates(Context context) {
        if (context == null) return new ArrayList<>();
        return new ArrayList<>(DevicePerformanceCapabilities.detect(context).getSupportedRefreshRates());
    }

    public static boolean autoConfigGamePackage(Context context, String packageName, int targetFpsHz) {
        if (packageName == null || packageName.trim().isEmpty()) return false;

        int supportedTarget = DevicePerformanceCapabilities.detect(context).resolveRefreshRate(targetFpsHz);
        Log.d(TAG, "Configuring " + packageName + " for supported " + supportedTarget + " FPS / Hz...");

        // 1. Android Game Mode API Performance tuning
        CommandExecutor.executeSystemCommand("cmd game mode performance " + packageName);

        // 2. Per-app max refresh rate override
        CommandExecutor.executeSystemCommand("cmd window set-app-refresh-rate " + packageName + " " + supportedTarget);

        // 3. System-wide refresh rate peak/min lock
        HzFpsChannel.RefreshRateResult refreshResult = HzFpsChannel.setRefreshRate(context, supportedTarget);

        // 4. Auto-patch and create game configuration files inside app data storage folder for supported target FPS/Hz
        GameConfigPatcher.applyGameFpsPatch(packageName, supportedTarget);

        return refreshResult.success;
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
