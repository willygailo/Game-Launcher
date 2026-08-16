package com.gamebooster.app.games;
import com.gamebooster.app.config.*;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.gamespace.GameSpaceDndManager;
import com.gamebooster.app.gamespace.AutoGameMonitorService;

import java.util.HashSet;
import java.util.Set;

public class GameLauncherHelper {

    private static final String PREF_NAME = "custom_game_library_prefs";
    private static final String KEY_CUSTOM_PACKAGES = "custom_game_packages";

    public static void launchGameWithAutoBoost(Context context, GameAppInfo game) {
        if (context == null || game == null) return;

        String pkgName = game.getPackageName();
        if (pkgName == null || pkgName.trim().isEmpty()) return;

        GameProfilePreferences.Profile profile = GameProfilePreferences.getProfile(context, pkgName);
        GameSessionSettings.begin(context, pkgName);
        AutoGameMonitorService.start(context);

        // 1. Offload background optimizations to AppExecutors so launch is instant
        com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
            try {
                com.gamebooster.app.shizuku.ShizukuExecutor.grantAppPermissionsViaShizuku(context);
                com.gamebooster.app.spoofer.DeviceSpooferEngine.applySpoofing(context, pkgName);

                // Auto-apply saved per-game Competitive CFG Profile (FPS + Super Touch + Shizuku Hz)
                String gameKey = pkgName.contains("mobile.legends") || pkgName.contains("mobilelegends") ? CompetitiveCfgProfile.GAME_MLBB :
                                 pkgName.contains("pubg") || pkgName.contains("tencent.ig") || pkgName.contains("imobile") || pkgName.contains("vng.pubgmobile") ? CompetitiveCfgProfile.GAME_PUBGM :
                                 pkgName.contains("cod") || pkgName.contains("callofduty") ? CompetitiveCfgProfile.GAME_CODM :
                                 pkgName.contains("freefire") || pkgName.contains("dts.freefire") ? CompetitiveCfgProfile.GAME_FREEFIRE :
                                 pkgName.contains("genshin") || pkgName.contains("mihoyo") || pkgName.contains("cognosphere") || pkgName.contains("hoyoverse") || pkgName.contains("hkrpg") ? CompetitiveCfgProfile.GAME_GENSHIN :
                                 pkgName.contains("sgame") || pkgName.contains("levelinfinite") || pkgName.contains("arenaofvalor") || pkgName.contains("kgtw") || pkgName.contains("kgvn") ? CompetitiveCfgProfile.GAME_HOK :
                                 pkgName.contains("roblox") ? CompetitiveCfgProfile.GAME_ROBLOX : CompetitiveCfgProfile.GAME_ALL;
                CompetitiveCfgProfile cfgProf = CfgProfileManager.loadProfile(context, gameKey);
                CfgProfileManager.applyProfile(context, gameKey, cfgProf);

                int targetFps = 165;
                GameProfileAutoConfigurator.autoConfigGamePackage(context, pkgName, targetFps);
                com.gamebooster.app.booster.MaxHzForceChannel.forceApply(165);
                com.gamebooster.app.engine.RefreshRateOverrideEngine.applyRefreshRate(context, pkgName,
                        com.gamebooster.app.engine.RefreshRateOverrideEngine.RefreshRateMode.MODE_165HZ);
                PerformanceChannel.applyProfile(context, PerformanceChannel.Profile.EXTREME_PERFORMANCE);
                PerformanceChannel.writeAndExecuteRootTweaksScript(165);
                GameSpaceDndManager.setGamingDndMode(context, profile.enableDnd);
                com.gamebooster.app.booster.NetworkOptimizer.flushDnsCache();
            } catch (Throwable ignored) {}
        });

        // 2. Perform 3-Tier Game Launch Fallback
        boolean launched = false;

        // Tier 1: Standard Intent Launch
        Intent launchIntent = game.getLaunchIntent();
        if (launchIntent != null) {
            try {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                context.startActivity(launchIntent);
                launched = true;
            } catch (Throwable ignored) {}
        }

        // Tier 2: PackageManager Re-query
        if (!launched) {
            try {
                Intent pmIntent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
                if (pmIntent != null) {
                    pmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    context.startActivity(pmIntent);
                    launched = true;
                }
            } catch (Throwable ignored) {}
        }

        // Tier 3: Shizuku ADB Direct Launch Fallback (monkey -p <pkg> 1)
        if (!launched) {
            try {
                String res = com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand("monkey -p " + pkgName + " -c android.intent.category.LAUNCHER 1");
                if (res != null && !res.startsWith("ERROR")) {
                    launched = true;
                }
            } catch (Throwable ignored) {}
        }

        // Tier 4: Google Play Store / Web Store Launch Fallback
        if (!launched) {
            try {
                Intent marketIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=" + pkgName));
                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(marketIntent);
                launched = true;
            } catch (Throwable e) {
                try {
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + pkgName));
                    webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(webIntent);
                    launched = true;
                } catch (Throwable ignored) {}
            }
        }

        if (launched) {
            Toast.makeText(context, "⚡ LAUNCHED: " + game.getLabel() + " • "
                    + profile.label + " up to " + targetFpsForToast(context, pkgName) + "Hz", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Unable to launch " + game.getLabel(), Toast.LENGTH_SHORT).show();
        }
    }

    private static int targetFpsForToast(Context context, String packageName) {
        return GameProfilePreferences.getTargetHz(context, packageName);
    }

    public static Set<String> getCustomPackages(Context context) {
        if (context == null) return new HashSet<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return new HashSet<>(prefs.getStringSet(KEY_CUSTOM_PACKAGES, new HashSet<>()));
    }

    public static void addCustomPackage(Context context, String packageName) {
        if (context == null || packageName == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_CUSTOM_PACKAGES, new HashSet<>()));
        set.add(packageName);
        prefs.edit().putStringSet(KEY_CUSTOM_PACKAGES, set).apply();
    }

    public static void removeCustomPackage(Context context, String packageName) {
        if (context == null || packageName == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_CUSTOM_PACKAGES, new HashSet<>()));
        set.remove(packageName);
        prefs.edit().putStringSet(KEY_CUSTOM_PACKAGES, set).apply();
    }
}
