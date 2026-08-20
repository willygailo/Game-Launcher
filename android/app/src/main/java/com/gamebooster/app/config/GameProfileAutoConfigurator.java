package com.gamebooster.app.config;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.*;
import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.device.DisplayCapabilitiesDetector;
import com.gamebooster.app.booster.HzFpsChannel;
import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;

import java.util.ArrayList;
import java.util.List;

public class GameProfileAutoConfigurator {

    private static final String TAG = "GameAutoConfigurator";
    public static final String KEY_TARGET_HZ_FPS = "user_target_hz_fps";
    public static final int DEFAULT_TARGET_HZ = FpsUnlockTier.FPS_185.fps;

    public interface OnAutoConfigListener {
        void onAutoConfigCompleted(int gamesConfiguredCount, int targetFpsHz);
    }

    /**
     * Clamps a target FPS/Hz to the display's real capability.
     * When display detection fails (fallback max of 60), the target is honored
     * as-is instead of being clamped down on unknown hardware.
     */
    public static int clampTargetFpsToDisplay(Context context, int targetFpsHz) {
        if (context == null || targetFpsHz <= 0) return targetFpsHz;
        int maxRate = DisplayCapabilitiesDetector.detect(context).maxRefreshRate;
        if (maxRate > 60 && targetFpsHz > maxRate) {
            return maxRate;
        }
        return targetFpsHz;
    }

    /**
     * Stores the user-selected target FPS/Hz, clamped to display capability.
     *
     * @return the clamped value that was persisted
     */
    public static int setTargetFpsHz(Context context, int targetFpsHz) {
        if (context == null) return targetFpsHz;
        int clamped = clampTargetFpsToDisplay(context, targetFpsHz);
        context.getApplicationContext()
                .getSharedPreferences("game_booster_tweak_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_TARGET_HZ_FPS, clamped)
                .apply();
        return clamped;
    }

    public static int getTargetFpsHz(Context context) {
        if (context == null) return DEFAULT_TARGET_HZ;
        int stored = context.getApplicationContext()
                .getSharedPreferences("game_booster_tweak_prefs", Context.MODE_PRIVATE)
                .getInt(KEY_TARGET_HZ_FPS, DEFAULT_TARGET_HZ);
        return clampTargetFpsToDisplay(context, stored);
    }

    public static List<Integer> getSupportedDisplayRefreshRates(Context context) {
        if (context == null) return new ArrayList<>();
        return new ArrayList<>(DevicePerformanceCapabilities.detect(context).getSupportedRefreshRates());
    }

    /**
     * Auto-configures a game package and display for the requested target FPS/Hz,
     * clamped to the display's real refresh capability.
     * Uses Shizuku direct force channel to ensure zero-fallback execution.
     */
    public static boolean autoConfigGamePackage(Context context, String packageName, int targetFpsHz) {
        if (packageName == null || packageName.trim().isEmpty()) return false;

        final int forcedFpsHz = clampTargetFpsToDisplay(context,
                FpsUnlockTier.resolveTargetFps(targetFpsHz));
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
        GameConfigPatcher.applyGameFpsPatch(context, packageName, forcedFpsHz);

        // 6. Apply Competitive CFG Profile
        String gameKey = packageName.contains("mobile.legends") || packageName.contains("mobilelegends") ? CompetitiveCfgProfile.GAME_MLBB :
                         packageName.contains("pubg") || packageName.contains("tencent.ig") || pkgContains(packageName, "imobile", "vng.pubgmobile") ? CompetitiveCfgProfile.GAME_PUBGM :
                         packageName.contains("cod") || packageName.contains("callofduty") || packageName.contains("warzone") ? CompetitiveCfgProfile.GAME_CODM :
                         packageName.contains("freefire") || packageName.contains("dts.freefire") ? CompetitiveCfgProfile.GAME_FREEFIRE :
                         packageName.contains("genshin") || packageName.contains("mihoyo") || packageName.contains("cognosphere") || packageName.contains("hoyoverse") || packageName.contains("hkrpg") || packageName.contains("nap") ? CompetitiveCfgProfile.GAME_GENSHIN :
                         packageName.contains("wildrift") || packageName.contains("riotgames.league") ? CompetitiveCfgProfile.GAME_WILDRIFT :
                         packageName.contains("sgame") || packageName.contains("levelinfinite") || packageName.contains("arenaofvalor") || packageName.contains("kgtw") || packageName.contains("kgvn") ? CompetitiveCfgProfile.GAME_HOK :
                         packageName.contains("bloodstrike") || packageName.contains("newspike") ? CompetitiveCfgProfile.GAME_BLOODSTRIKE :
                         packageName.contains("standoff2") || packageName.contains("axlebolt") ? CompetitiveCfgProfile.GAME_STANDOFF2 :
                         packageName.contains("carx") || packageName.contains("glofta9hm") || packageName.contains("asphalt") || packageName.contains("r3_row") ? CompetitiveCfgProfile.GAME_CARX :
                         packageName.contains("uamo") || packageName.contains("arenabreakout") || packageName.contains("deltaforce") ? CompetitiveCfgProfile.GAME_ARENABREAKOUT :
                         packageName.contains("supercell") || packageName.contains("brawlstars") || packageName.contains("clashroyale") || packageName.contains("clashofclans") ? CompetitiveCfgProfile.GAME_SUPERCELL :
                         packageName.contains("roblox") ? CompetitiveCfgProfile.GAME_ROBLOX :
                         packageName.contains("projectc") || packageName.contains("valorant") ? CompetitiveCfgProfile.GAME_VALORANT :
                         packageName.contains("farlight") || packageName.contains("solarland") ? CompetitiveCfgProfile.GAME_FARLIGHT : CompetitiveCfgProfile.GAME_ALL;
        
        CompetitiveCfgProfile profile = new CompetitiveCfgProfile(gameKey, forcedFpsHz, true, true, true, true, true, true, true, true, true, true);
        if (context != null) {
            CfgProfileManager.applyProfile(context, gameKey, profile);
            DeviceSpooferEngine.applySpoofing(context, packageName);
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
     * Applies the requested target FPS and Hz to all detected games and display.
     */
    public static void autoConfigAllGamesAsync(Context context, int targetFpsHz, OnAutoConfigListener listener) {
        if (context == null) return;

        final int resolvedHz = setTargetFpsHz(context, targetFpsHz);

        AppExecutors.getInstance().executeCommand(() -> {
            // 1. Force global display refresh rate
            if (ShizukuExecutor.hasShizukuPermission()) {
                MaxHzForceChannel.forceApply(resolvedHz);
            } else {
                HzFpsChannel.setRefreshRate(context, resolvedHz);
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
                    if (autoConfigGamePackage(context, pkg, resolvedHz)) {
                        configuredCount++;
                    }
                }
            }

            for (GameAppInfo game : installedGames) {
                String pkg = game.getPackageName();
                if (!processedPackages.contains(pkg)) {
                    processedPackages.add(pkg);
                    if (autoConfigGamePackage(context, pkg, resolvedHz)) {
                        configuredCount++;
                    }
                }
            }

            final int finalCount = configuredCount;
            if (listener != null) {
                AppExecutors.getInstance().postToMainThread(() ->
                        listener.onAutoConfigCompleted(finalCount, resolvedHz));
            }
        });
    }
}
