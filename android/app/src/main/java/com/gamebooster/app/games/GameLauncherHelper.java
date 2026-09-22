package com.gamebooster.app.games;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.config.CfgProfileManager;
import com.gamebooster.app.config.CompetitiveCfgProfile;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.config.GameProfilePreferences;
import com.gamebooster.app.config.GameSessionSettings;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.gamespace.AutoGameMonitorService;
import com.gamebooster.app.gamespace.GameSpaceDndManager;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuManager;
import com.gamebooster.app.shizuku.ShizukuPermissionEnforcer;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;

import java.util.HashSet;
import java.util.Set;

public class GameLauncherHelper {

    private static final String PREF_NAME = "custom_game_library_prefs";
    private static final String KEY_CUSTOM_PACKAGES = "custom_game_packages";
    private static final String KEY_EXCLUDED_PACKAGES = "excluded_game_packages";

    private static final String[][] GAME_FAMILY_PACKAGES = {
        // Mobile Legends: Bang Bang (Global, Xiaomi Mi Store, VNG, Huawei, NA, KR, JP, Moonton)
        {
            "com.mobile.legends", "com.mobilelegends.mi", "com.vng.mlbbvn",
            "com.mobile.legends.vng", "com.mobilelegends.hw", "com.mobilelegends.na",
            "com.mobile.legends.kr", "com.mobile.legends.jp", "com.mobile.legends.moonton"
        },
        // PUBG Mobile (Global, India BGMI, KR, VN, Lite, New State, CN)
        {
            "com.tencent.ig", "com.pubg.imobile", "com.pubg.krmobile",
            "com.vng.pubgmobile", "com.tencent.iglite", "com.pubg.newstate",
            "com.krafton.bgmi", "com.tencent.tmgp.pubgm"
        },
        // Call of Duty: Mobile (Global, Garena, VN, KR, CN)
        {
            "com.activision.callofduty.shooter", "com.garena.game.codm",
            "com.vng.codmvn", "com.tencent.tmgp.kr.codm", "com.tencent.tmgp.cod"
        },
        // Free Fire & Free Fire MAX
        {
            "com.dts.freefireth", "com.dts.freefiremax"
        },
        // Honor of Kings & Arena of Valor
        {
            "com.levelinfinite.sgameGlobal", "com.levelinfinite.sgameGlobal.gpkg",
            "com.tencent.tmgp.sgame", "com.garena.game.kgtw", "com.garena.game.kgvn", "com.garena.game.kgid"
        },
        // Wild Rift
        {
            "com.riotgames.league.wildrift", "com.riotgames.league.wildrifttw", "com.riotgames.league.wildriftvn"
        }
    };

    /**
     * Finds if an alternate regional or OEM store package is installed for the requested game.
     */
    public static String resolveInstalledFamilyPackage(Context context, String pkg) {
        if (context == null || pkg == null) return pkg;
        android.content.pm.PackageManager pm = context.getPackageManager();
        if (pm == null) return pkg;
        if (HomeGameScanner.isPackageInstalled(pm, pkg)) return pkg;

        for (String[] family : GAME_FAMILY_PACKAGES) {
            boolean isPart = false;
            for (String p : family) {
                if (p.equalsIgnoreCase(pkg)) {
                    isPart = true;
                    break;
                }
            }
            if (isPart) {
                for (String p : family) {
                    if (HomeGameScanner.isPackageInstalled(pm, p)) {
                        return p;
                    }
                }
            }
        }
        return pkg;
    }

    public static void autoLaunchGame(Context context, GameAppInfo game) {
        if (context == null || game == null) return;
        android.content.pm.PackageManager pm = context.getPackageManager();
        String pkg = game.getPackageName();
        if (pkg != null && pm != null) {
            String resolvedPkg = resolveInstalledFamilyPackage(context, pkg);
            if (!resolvedPkg.equals(pkg)) {
                Intent resolvedIntent = HomeGameScanner.resolveLaunchIntent(pm, resolvedPkg);
                game = new GameAppInfo(
                        game.getLabel(),
                        resolvedPkg,
                        game.getIcon(),
                        resolvedIntent,
                        game.getGameType(),
                        game.getCardBgRes(),
                        game.getBadgeColor()
                );
                pkg = resolvedPkg;
            }

            if (!HomeGameScanner.isPackageInstalled(pm, pkg)) {
                String title = game.getLabel() != null ? game.getLabel() : pkg;
                Toast.makeText(context, "❌ " + title + " is not installed on this device.", Toast.LENGTH_SHORT).show();
                try {
                    Intent storeIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=" + pkg));
                    storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(storeIntent);
                } catch (Throwable ignored) {}
                return;
            }
        }
        // Per-game immediate inject: fire before startActivity so configs land on disk
        // before the game process spawns. GameAutoInjectDispatcher routes per-package:
        //   MLBB     → MlbbConfigPatcher + MlbbDroneViewPatcher suite
        //   PUBGM    → PubgConfigPatcher full suite
        //   CODM     → CodmConfigPatcher full suite
        //   FreeFire → FreeFire config suite
        //   Others   → CommonConfigTuningInjector defaults
        final Context injectCtx = context.getApplicationContext();
        final String injectPkg = pkg;
        com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
            try {
                com.gamebooster.app.config.GameAutoInjectDispatcher.dispatchForPackage(injectCtx, injectPkg, false);
            } catch (Throwable ignored) {}
        });

        com.gamebooster.app.gamemanager.GameManagerLauncher.launchGame(context, game);
    }


    public static void launchGameWithAutoBoost(Context context, GameAppInfo game) {
        autoLaunchGame(context, game);
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
        Set<String> customSet = new HashSet<>(prefs.getStringSet(KEY_CUSTOM_PACKAGES, new HashSet<>()));
        customSet.add(packageName);
        Set<String> excludedSet = new HashSet<>(prefs.getStringSet(KEY_EXCLUDED_PACKAGES, new HashSet<>()));
        excludedSet.remove(packageName); // Un-exclude if it was previously excluded
        prefs.edit()
                .putStringSet(KEY_CUSTOM_PACKAGES, customSet)
                .putStringSet(KEY_EXCLUDED_PACKAGES, excludedSet)
                .apply();
    }

    public static void removeCustomPackage(Context context, String packageName) {
        if (context == null || packageName == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_CUSTOM_PACKAGES, new HashSet<>()));
        set.remove(packageName);
        prefs.edit().putStringSet(KEY_CUSTOM_PACKAGES, set).apply();
    }

    public static Set<String> getExcludedPackages(Context context) {
        if (context == null) return new HashSet<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return new HashSet<>(prefs.getStringSet(KEY_EXCLUDED_PACKAGES, new HashSet<>()));
    }

    public static void excludePackage(Context context, String packageName) {
        if (context == null || packageName == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_EXCLUDED_PACKAGES, new HashSet<>()));
        set.add(packageName);
        prefs.edit().putStringSet(KEY_EXCLUDED_PACKAGES, set).apply();
    }

    public static void unexcludePackage(Context context, String packageName) {
        if (context == null || packageName == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_EXCLUDED_PACKAGES, new HashSet<>()));
        set.remove(packageName);
        prefs.edit().putStringSet(KEY_EXCLUDED_PACKAGES, set).apply();
    }

    public static void removeGameFromHome(Context context, String packageName) {
        if (context == null || packageName == null) return;
        removeCustomPackage(context, packageName);
        excludePackage(context, packageName);
    }

    public static int clearAllCustomPackages(Context context) {
        if (context == null) return 0;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> customSet = prefs.getStringSet(KEY_CUSTOM_PACKAGES, new HashSet<>());
        int count = customSet.size();
        prefs.edit().remove(KEY_CUSTOM_PACKAGES).apply();
        return count;
    }

    public static void resetAllExcludedPackages(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_EXCLUDED_PACKAGES).apply();
    }
}
