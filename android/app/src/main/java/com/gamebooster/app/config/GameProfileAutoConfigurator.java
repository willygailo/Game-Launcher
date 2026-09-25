package com.gamebooster.app.config;

import android.content.Context;

import com.gamebooster.app.booster.HzFpsChannel;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.GameManagerRepository;
import com.gamebooster.app.games.HomeGameScanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Configures launcher-owned display preferences. Per-game game files, identity
 * changes, and system shell mutations are intentionally outside this flow.
 */
public final class GameProfileAutoConfigurator {

    public static final String KEY_TARGET_HZ_FPS = "user_target_hz_fps";
    public static final int DEFAULT_TARGET_HZ = 60;
    public static final int MIN_ENFORCED_HZ    = 60;
    public static final int MAX_ENFORCED_HZ    = 185;

    private GameProfileAutoConfigurator() {}

    public interface OnAutoConfigListener {
        void onAutoConfigCompleted(int gamesConfiguredCount, int targetFpsHz);
    }

    /**
     * Resolves to the best high-refresh target matching hardware capability.
     * Supported standard tiers: 60, 90, 120, 144, 165, 185 FPS / Hz.
     */
    public static int clampTargetFpsToDisplay(Context context, int targetFpsHz) {
        int deviceMax = 0;
        List<Integer> supported = null;
        if (context != null) {
            DevicePerformanceCapabilities caps = DevicePerformanceCapabilities.detect(context);
            deviceMax = caps.getMaxRefreshRate();
            supported = caps.getSupportedRefreshRates();
        }
        if (deviceMax <= 0) {
            deviceMax = 60;
        }

        // If target was not specified (<=0), use hardware maximum
        if (targetFpsHz <= 0) {
            return deviceMax;
        }

        // Match exact or nearest supported gaming tier
        if (targetFpsHz >= 185 && deviceMax >= 185) return 185;
        if (targetFpsHz >= 165 && deviceMax >= 165) return 165;
        if (targetFpsHz >= 144 && deviceMax >= 144) return 144;
        if (targetFpsHz >= 120 && deviceMax >= 120) return 120;
        if (targetFpsHz >= 90  && deviceMax >= 90)  return 90;
        if (targetFpsHz >= 60) return Math.min(targetFpsHz, deviceMax);

        return Math.min(targetFpsHz, deviceMax);
    }

    public static int setTargetFpsHz(Context context, int targetFpsHz) {
        if (context == null) return targetFpsHz;
        int resolved = clampTargetFpsToDisplay(context, targetFpsHz);
        context.getApplicationContext().getSharedPreferences("game_booster_tweak_prefs", Context.MODE_PRIVATE)
                .edit().putInt(KEY_TARGET_HZ_FPS, resolved).apply();
        return resolved;
    }

    public static int getTargetFpsHz(Context context) {
        if (context == null) return MIN_ENFORCED_HZ;
        int stored = context.getApplicationContext().getSharedPreferences("game_booster_tweak_prefs", Context.MODE_PRIVATE)
                .getInt(KEY_TARGET_HZ_FPS, 0);
        return clampTargetFpsToDisplay(context, stored);
    }

    public static List<Integer> getSupportedDisplayRefreshRates(Context context) {
        if (context == null) return new ArrayList<>();
        return new ArrayList<>(DevicePerformanceCapabilities.detect(context).getSupportedRefreshRates());
    }

    /** Persists a capability-checked display preference for a game. */
    public static boolean autoConfigGamePackage(Context context, String packageName, int targetFpsHz) {
        if (context == null || packageName == null || packageName.trim().isEmpty()) return false;
        int resolvedHz = clampTargetFpsToDisplay(context, targetFpsHz);
        GameProfilePreferences.setTargetHz(context, packageName.trim(), resolvedHz);
        return true;
    }

    public static void autoConfigAllInstalledGamesAsync(Context context, OnAutoConfigListener listener) {
        autoConfigAllGamesAsync(context, getTargetFpsHz(context), listener);
    }

    /**
     * Applies a user-requested system display preference if Android authorizes it,
     * then stores the same physical-mode preference for visible game launchers.
     */
    public static void autoConfigAllGamesAsync(Context context, int targetFpsHz, OnAutoConfigListener listener) {
        if (context == null) return;
        final Context appContext = context.getApplicationContext();
        final int resolvedHz = setTargetFpsHz(appContext, targetFpsHz);

        AppExecutors.getInstance().executeCommand(() -> {
            HzFpsChannel.setRefreshRate(appContext, resolvedHz);

            List<GameAppInfo> targetGames = HomeGameScanner.scanTargetGames(appContext);
            List<GameAppInfo> installedGames = GameManagerRepository.getInstalledGames(appContext);
            Set<String> processedPackages = new HashSet<>();
            int configuredCount = saveProfiles(appContext, targetGames, processedPackages, resolvedHz);
            configuredCount += saveProfiles(appContext, installedGames, processedPackages, resolvedHz);

            if (listener != null) {
                final int finalCount = configuredCount;
                AppExecutors.getInstance().postToMainThread(() ->
                        listener.onAutoConfigCompleted(finalCount, resolvedHz));
            }
        });
    }

    private static int saveProfiles(Context context, List<GameAppInfo> games,
                                    Set<String> processedPackages, int targetHz) {
        if (games == null) return 0;
        int count = 0;
        for (GameAppInfo game : games) {
            if (game == null || game.getPackageName() == null) continue;
            String packageName = game.getPackageName();
            if (processedPackages.add(packageName) && autoConfigGamePackage(context, packageName, targetHz)) {
                count++;
            }
        }
        return count;
    }
}
