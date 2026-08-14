package com.gamebooster.app.feature.gameprofiles.automation;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.feature.performance.booster.MaxHzForceChannel;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.feature.performance.device.DisplayCapabilitiesDetector;
import com.gamebooster.app.feature.performance.device.DisplayRefreshRatePreferences;
import com.gamebooster.app.platform.shell.CommandExecutor;
import com.gamebooster.app.feature.games.*;
import com.gamebooster.app.feature.gameprofiles.preferences.GameProfilePreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * GameProfileAutoConfigurator — fires per-game and batch auto-configuration.
 *
 * <p>Hz selection policy: use the user's persisted selection first, then per-game
 * preference from GamePackageRegistry, then fall back to the hardware panel's max rate.
 * No hardcoded Hz floor is applied — the display hardware decides what is valid.
 */
public class GameProfileAutoConfigurator {

    private static final String TAG = "GameAutoConfigurator";
    public static final String KEY_TARGET_HZ_FPS = "user_target_hz_fps";

    /**
     * Default target Hz when no preference is stored and no panel info is available.
     * This is a soft default only — the actual value is always read from the panel.
     */
    public static final int DEFAULT_TARGET_HZ = 165;

    public interface OnAutoConfigListener {
        void onAutoConfigCompleted(int gamesConfiguredCount, int targetFpsHz);
    }

    /**
     * Stores the global target Hz (user selection — no forced clamp).
     */
    public static void setTargetFpsHz(Context context, int targetFpsHz) {
        if (context == null) return;
        context.getApplicationContext()
                .getSharedPreferences("game_booster_tweak_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_TARGET_HZ_FPS, targetFpsHz)
                .apply();
        // Also keep DisplayRefreshRatePreferences in sync
        DisplayRefreshRatePreferences.saveSelectedHz(context, targetFpsHz);
    }

    /**
     * Returns the global target Hz as stored by the user, or the panel's max rate if not set.
     */
    public static int getTargetFpsHz(Context context) {
        if (context == null) return DEFAULT_TARGET_HZ;
        int stored = context.getApplicationContext()
                .getSharedPreferences("game_booster_tweak_prefs", Context.MODE_PRIVATE)
                .getInt(KEY_TARGET_HZ_FPS, 0);
        if (stored > 0) return stored;
        // Fall back to the display's actual max rate
        try {
            DisplayCapabilitiesDetector.DisplayCaps caps =
                    DisplayCapabilitiesDetector.detect(context);
            if (caps != null && caps.maxRefreshRate > 0) return caps.maxRefreshRate;
        } catch (Throwable ignored) {}
        return DEFAULT_TARGET_HZ;
    }

    /**
     * Returns all refresh rates supported by the physical display panel.
     * List is populated dynamically from {@link DisplayCapabilitiesDetector} —
     * no hardcoded values.
     */
    public static List<Integer> getSupportedDisplayRefreshRates(Context context) {
        if (context == null) return new ArrayList<>();
        try {
            DisplayCapabilitiesDetector.DisplayCaps caps =
                    DisplayCapabilitiesDetector.detect(context);
            if (caps != null && !caps.getRecommendedRates().isEmpty()) {
                return new ArrayList<>(caps.getRecommendedRates());
            }
        } catch (Throwable ignored) {}
        return new ArrayList<>();
    }

    /**
     * Resolves the target Hz for a specific game package:
     *  1. Per-game saved preference
     *  2. GamePackageRegistry.maxSupportedFps
     *  3. Global user preference
     *  4. Panel max rate
     */
    public static int resolveGameHz(Context context, String packageName) {
        // Per-game preference
        int perGame = GameProfilePreferences.getTargetHz(context, packageName);
        if (perGame > 0) return perGame;

        // Registry max
        GamePackageRegistry.GameInfoSpec spec = GamePackageRegistry.getSpec(packageName);
        if (spec != null && spec.maxSupportedFps > 0) return spec.maxSupportedFps;

        // Global preference / panel max
        return getTargetFpsHz(context);
    }

    /**
     * Configures a single game with supported Android controls only:
     * a native display-rate preference and an advisory Android Game Mode request.
     * These requests cannot bypass a game's internal FPS, HDR, or graphics limits.
     */
    public static boolean autoConfigGamePackage(Context context, String packageName, int targetFpsHz) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        if (context == null) return false;
        targetFpsHz = com.gamebooster.app.feature.performance.device.DevicePerformanceCapabilities.detect(context)
                .resolveRefreshRate(targetFpsHz);

        Log.d(TAG, "autoConfigGamePackage: " + packageName + " @ native " + targetFpsHz + "Hz");

        // 1. Request a native display rate and a valid per-package Game Mode profile.
        MaxHzForceChannel.ForceResult forceResult =
                MaxHzForceChannel.forceApply(context, targetFpsHz, packageName);

        // Game Mode is advisory: the game, panel and OEM policy still determine actual FPS.
        com.gamebooster.app.feature.performance.display.DisplayOverrideController.applyGameProfile(context, packageName, targetFpsHz);

        // Request system-level Performance mode via Android 12+ GameManager framework bridge
        try {
            com.gamebooster.app.feature.performance.booster.GameManagerAdapter gma =
                    new com.gamebooster.app.feature.performance.booster.GameManagerAdapter(context);
            gma.setGameMode(packageName, com.gamebooster.app.feature.performance.booster.GameManagerAdapter.GAME_MODE_PERFORMANCE);
        } catch (Throwable ignored) {}

        // Execute package-specific Shizuku file config patchers for target com. packages (PUBGM, CODM, MLBB, HOK, Genshin, Roblox, etc.)
        applyTargetGameFilePatcher(packageName, targetFpsHz);

        return forceResult.success;
    }

    private static void applyTargetGameFilePatcher(String packageName, int targetFpsHz) {
        if (packageName == null) return;
        String lower = packageName.toLowerCase();

        try {
            if (lower.contains("mobile.legends") || lower.contains("mobilelegends")) {
                com.gamebooster.app.feature.gameprofiles.patcher.MlbbConfigPatcher.patchCompetitive(packageName, targetFpsHz);
                com.gamebooster.app.feature.gameprofiles.patcher.MlbbConfigPatcher.applySuperFastTouch(packageName);
            } else if (lower.contains("tencent.ig") || lower.contains("pubg") || lower.contains("imobile") || lower.contains("vng.pubgmobile")) {
                com.gamebooster.app.feature.gameprofiles.patcher.PubgMobileConfigPatcher.patchPubgMobileConfig(targetFpsHz);
            } else if (lower.contains("codm") || lower.contains("callofduty")) {
                com.gamebooster.app.feature.gameprofiles.patcher.CodmConfigPatcher.patch(packageName, targetFpsHz);
            } else if (lower.contains("sgame") || lower.contains("honorofkings") || lower.contains("arenaofvalor")) {
                com.gamebooster.app.feature.gameprofiles.patcher.HokConfigPatcher.patch(packageName, targetFpsHz);
            } else if (lower.contains("genshin") || lower.contains("mihoyo") || lower.contains("cognosphere")) {
                com.gamebooster.app.feature.gameprofiles.patcher.GenshinConfigPatcher.patch(packageName, targetFpsHz);
            } else if (lower.contains("roblox")) {
                com.gamebooster.app.feature.gameprofiles.patcher.RobloxConfigPatcher.patch(packageName, targetFpsHz);
            } else if (lower.contains("freefire")) {
                com.gamebooster.app.feature.gameprofiles.patcher.FreeFireConfigPatcher.patch(packageName, targetFpsHz);
            } else if (lower.contains("bloodstrike")) {
                com.gamebooster.app.feature.gameprofiles.patcher.BloodStrikeConfigPatcher.patch(packageName, targetFpsHz);
            } else if (lower.contains("wildrift")) {
                com.gamebooster.app.feature.gameprofiles.patcher.WildRiftConfigPatcher.patch(packageName, targetFpsHz);
            } else if (lower.contains("deltaforce") || lower.contains("dfm")) {
                com.gamebooster.app.feature.gameprofiles.patcher.DeltaForceConfigPatcher.patch(packageName, targetFpsHz);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error applying Shizuku file config patch for " + packageName, t);
        }
    }

    /**
     * Asynchronously configures ALL installed games with their max supported Hz.
     * Each game is resolved individually from GamePackageRegistry.
     */
    public static void autoConfigAllInstalledGamesAsync(Context context, OnAutoConfigListener listener) {
        if (context == null) return;

        AppExecutors.getInstance().executeCommand(() -> {
            List<GameAppInfo> games = GameManagerRepository.getInstalledGames(context);
            int configuredCount = 0;

            for (GameAppInfo game : games) {
                String pkg = game.getPackageName();
                int gameHz = resolveGameHz(context, pkg);

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
