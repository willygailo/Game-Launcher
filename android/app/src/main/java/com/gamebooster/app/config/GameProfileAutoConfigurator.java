package com.gamebooster.app.config;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.*;
import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.booster.HzFpsChannel;
import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.engine.CommandExecutor;

import java.util.ArrayList;
import java.util.List;

public class GameProfileAutoConfigurator {

    private static final String TAG = "GameAutoConfigurator";
    public static final String KEY_TARGET_HZ_FPS = "user_target_hz_fps";
    public static final int DEFAULT_TARGET_HZ = 185;

    public interface OnAutoConfigListener {
        void onAutoConfigCompleted(int gamesConfiguredCount, int targetFpsHz);
    }

    public static void setTargetFpsHz(Context context, int targetFpsHz) {
        if (context == null) return;
        int validHz = targetFpsHz > 0 ? targetFpsHz : DEFAULT_TARGET_HZ;
        context.getApplicationContext()
                .getSharedPreferences("game_booster_tweak_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_TARGET_HZ_FPS, validHz)
                .apply();
    }

    public static int getTargetFpsHz(Context context) {
        if (context == null) return DEFAULT_TARGET_HZ;
        return context.getApplicationContext()
                .getSharedPreferences("game_booster_tweak_prefs", Context.MODE_PRIVATE)
                .getInt(KEY_TARGET_HZ_FPS, DEFAULT_TARGET_HZ);
    }

    public static List<Integer> getSupportedDisplayRefreshRates(Context context) {
        if (context == null) return new ArrayList<>();
        return new ArrayList<>(DevicePerformanceCapabilities.detect(context).getSupportedRefreshRates());
    }

    /**
     * Auto-configures a game package and display for target FPS/Hz (120 / 144 / 165 / 185).
     * Uses Shizuku direct force channel to ensure zero-fallback execution.
     */
    public static boolean autoConfigGamePackage(Context context, String packageName, int targetFpsHz) {
        if (packageName == null || packageName.trim().isEmpty()) return false;

        final int forcedFpsHz = targetFpsHz > 0 ? targetFpsHz : DEFAULT_TARGET_HZ;
        Log.d(TAG, "Configuring " + packageName + " for target " + forcedFpsHz + " FPS / Hz...");

        // 1. Android Game Mode API Performance tuning
        CommandExecutor.executeSystemCommand("cmd game mode performance " + packageName);

        // 2. Per-app max refresh rate override
        CommandExecutor.executeSystemCommand("cmd window set-app-refresh-rate " + packageName + " " + forcedFpsHz);

        // 3. Android Game Mode FPS target
        CommandExecutor.executeSystemCommand("cmd game set --fps " + forcedFpsHz + " " + packageName);

        // 4. Force display refresh rate across all 6 layers (AOSP, SurfaceFlinger, setprop, OEM)
        if (ShizukuExecutor.hasShizukuPermission()) {
            MaxHzForceChannel.forceApply(forcedFpsHz);
        } else if (context != null) {
            HzFpsChannel.setRefreshRate(context, forcedFpsHz);
        }

        // 5. Auto-patch and create game configuration files for target FPS/Hz
        GameConfigPatcher.applyGameFpsPatch(packageName, forcedFpsHz);

        // 6. Apply Competitive CFG Profile
        String gameKey = packageName.contains("mobile.legends") || packageName.contains("mobilelegends") ? CompetitiveCfgProfile.GAME_MLBB :
                         packageName.contains("pubg") || packageName.contains("tencent.ig") || pkgContains(packageName, "imobile", "vng.pubgmobile") ? CompetitiveCfgProfile.GAME_PUBGM :
                         packageName.contains("cod") || packageName.contains("callofduty") ? CompetitiveCfgProfile.GAME_CODM :
                         packageName.contains("freefire") || packageName.contains("dts.freefire") ? CompetitiveCfgProfile.GAME_FREEFIRE :
                         packageName.contains("genshin") || packageName.contains("mihoyo") || packageName.contains("cognosphere") || packageName.contains("hoyoverse") || packageName.contains("hkrpg") ? CompetitiveCfgProfile.GAME_GENSHIN :
                         packageName.contains("sgame") || packageName.contains("levelinfinite") || packageName.contains("arenaofvalor") || packageName.contains("kgtw") || packageName.contains("kgvn") ? CompetitiveCfgProfile.GAME_HOK :
                         packageName.contains("roblox") ? CompetitiveCfgProfile.GAME_ROBLOX : CompetitiveCfgProfile.GAME_ALL;
        
        CompetitiveCfgProfile profile = new CompetitiveCfgProfile(gameKey, forcedFpsHz, true, true);
        if (context != null) {
            CfgProfileManager.applyProfile(context, gameKey, profile);
        }

        return true;
    }

    private static boolean pkgContains(String pkg, String... targets) {
        if (pkg == null) return false;
        for (String t : targets) {
            if (pkg.contains(t)) return true;
        }
        return false;
    }

    public static void autoConfigAllInstalledGamesAsync(Context context, OnAutoConfigListener listener) {
        autoConfigAllGamesAsync(context, getTargetFpsHz(context), listener);
    }

    /**
     * Applies target FPS and Hz (120 / 144 / 165) to all detected games and display.
     */
    public static void autoConfigAllGamesAsync(Context context, int targetFpsHz, OnAutoConfigListener listener) {
        if (context == null) return;

        setTargetFpsHz(context, targetFpsHz);

        AppExecutors.getInstance().executeCommand(() -> {
            // 1. Force global display refresh rate
            if (ShizukuExecutor.hasShizukuPermission()) {
                MaxHzForceChannel.forceApply(targetFpsHz);
            } else {
                HzFpsChannel.setRefreshRate(context, targetFpsHz);
            }

            // 2. Scan all games (Target + Installed)
            List<GameAppInfo> targetGames = HomeGameScanner.scanTargetGames(context);
            List<GameAppInfo> installedGames = GameManagerRepository.getInstalledGames(context);

            java.util.Set<String> processedPackages = new java.util.HashSet<>();
            int configuredCount = 0;

            for (GameAppInfo game : targetGames) {
                String pkg = game.getPackageName();
                if (!processedPackages.contains(pkg)) {
                    processedPackages.add(pkg);
                    if (autoConfigGamePackage(context, pkg, targetFpsHz)) {
                        configuredCount++;
                    }
                }
            }

            for (GameAppInfo game : installedGames) {
                String pkg = game.getPackageName();
                if (!processedPackages.contains(pkg)) {
                    processedPackages.add(pkg);
                    if (autoConfigGamePackage(context, pkg, targetFpsHz)) {
                        configuredCount++;
                    }
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
