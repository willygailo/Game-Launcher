package com.gamebooster.app.config;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.games.*;

import java.util.ArrayList;
import java.util.List;

/**
 * GameProfileAutoConfigurator — fires per-game and batch auto-configuration.
 *
 * Hz selection policy (NO FALLBACK to 60 or 90):
 *   1. Use per-game saved preference if set AND >= 120
 *   2. Else use GamePackageRegistry.maxSupportedFps (MLBB=165, HOK=120, etc.)
 *   3. Else use global user preference IF >= 120
 *   4. Hard floor: 120Hz minimum — never 60, never 90
 */
public class GameProfileAutoConfigurator {

    private static final String TAG = "GameAutoConfigurator";
    public static final String KEY_TARGET_HZ_FPS = "user_target_hz_fps";

    /** Hard minimum Hz — never fall back below this. */
    public static final int MIN_FORCED_HZ = 120;
    /** Default when no preference set and registry has no entry. */
    public static final int DEFAULT_TARGET_HZ = 120;

    public interface OnAutoConfigListener {
        void onAutoConfigCompleted(int gamesConfiguredCount, int targetFpsHz);
    }

    /**
     * Stores the global target Hz. Clamps to minimum 120.
     */
    public static void setTargetFpsHz(Context context, int targetFpsHz) {
        if (context == null) return;
        int clamped = Math.max(targetFpsHz, MIN_FORCED_HZ);
        context.getApplicationContext()
                .getSharedPreferences("game_booster_tweak_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_TARGET_HZ_FPS, clamped)
                .apply();
    }

    /**
     * Returns the global target Hz. Always >= 120.
     */
    public static int getTargetFpsHz(Context context) {
        if (context == null) return DEFAULT_TARGET_HZ;
        int stored = context.getApplicationContext()
                .getSharedPreferences("game_booster_tweak_prefs", Context.MODE_PRIVATE)
                .getInt(KEY_TARGET_HZ_FPS, DEFAULT_TARGET_HZ);
        return Math.max(stored, MIN_FORCED_HZ);
    }

    public static List<Integer> getSupportedDisplayRefreshRates(Context context) {
        if (context == null) return new ArrayList<>();
        // Return only high-refresh options — 120, 144, 165
        List<Integer> rates = new ArrayList<>();
        rates.add(120);
        rates.add(144);
        rates.add(165);
        return rates;
    }

    /**
     * Resolves the target Hz for a specific game package:
     *  1. Per-game saved preference (if >= 120)
     *  2. GamePackageRegistry.maxSupportedFps
     *  3. Global preference
     *  4. Hard floor: 120
     */
    public static int resolveGameHz(Context context, String packageName) {
        // Per-game preference
        int perGame = GameProfilePreferences.getTargetHz(context, packageName);
        if (perGame >= MIN_FORCED_HZ) return perGame;

        // Registry max
        GamePackageRegistry.GameInfoSpec spec = GamePackageRegistry.getSpec(packageName);
        if (spec != null && spec.maxSupportedFps >= MIN_FORCED_HZ) return spec.maxSupportedFps;

        // Global preference (already clamped >= 120)
        return getTargetFpsHz(context);
    }

    /**
     * Configures a single game package with max performance:
     *  1. MaxHzForceChannel.forceApply() — 6-layer Shizuku Hz force, NO capability gate
     *  2. GameConfigPatcher.applyCompetitivePatch() — force-writes game config files
     *  3. Android Game Mode performance per-app
     *  4. Per-app window refresh rate override
     *
     * NEVER falls back below 120Hz.
     */
    public static boolean autoConfigGamePackage(Context context, String packageName, int targetFpsHz) {
        if (packageName == null || packageName.trim().isEmpty()) return false;

        // Hard minimum — never 60 or 90
        int hz = Math.max(targetFpsHz, MIN_FORCED_HZ);
        Log.d(TAG, "autoConfigGamePackage: " + packageName + " @ " + hz + "Hz (no fallback)");

        // 1. Force Hz via MaxHzForceChannel — bypasses DevicePerformanceCapabilities gate
        MaxHzForceChannel.ForceResult forceResult =
                MaxHzForceChannel.forceApply(context, hz, packageName);

        // 2. Per-app Game Mode + window refresh rate override
        CommandExecutor.executeSystemCommand("cmd game mode performance " + packageName);
        CommandExecutor.executeSystemCommand("cmd window set-app-refresh-rate " + packageName + " " + hz);

        // 3. Competitive force-write game config files
        GameConfigPatcher.applyCompetitivePatch(packageName, hz);

        return forceResult.success;
    }

    /**
     * Asynchronously configures ALL installed games with their max supported Hz.
     * Each game is resolved individually from GamePackageRegistry — no global 60Hz default.
     */
    public static void autoConfigAllInstalledGamesAsync(Context context, OnAutoConfigListener listener) {
        if (context == null) return;

        AppExecutors.getInstance().executeCommand(() -> {
            List<GameAppInfo> games = GameManagerRepository.getInstalledGames(context);
            int configuredCount = 0;

            for (GameAppInfo game : games) {
                String pkg = game.getPackageName();

                // Resolve Hz — always >= 120
                int gameHz = resolveGameHz(context, pkg);

                // Apply competitive CFG profile
                String gameKey = pkg.contains("mobile.legends") || pkg.contains("mobilelegends") ? CompetitiveCfgProfile.GAME_MLBB :
                                 pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile") ? CompetitiveCfgProfile.GAME_PUBGM :
                                 pkg.contains("cod") || pkg.contains("callofduty") ? CompetitiveCfgProfile.GAME_CODM : CompetitiveCfgProfile.GAME_ALL;
                CompetitiveCfgProfile cfgProf = CfgProfileManager.loadProfile(context, gameKey);
                CfgProfileManager.applyProfile(context, gameKey, cfgProf);

                if (autoConfigGamePackage(context, pkg, gameHz)) {
                    configuredCount++;
                }
            }

            final int finalCount = configuredCount;
            final int finalHz = getTargetFpsHz(context);
            if (listener != null) {
                AppExecutors.getInstance().postToMainThread(() ->
                        listener.onAutoConfigCompleted(finalCount, finalHz));
            }
        });
    }
}

