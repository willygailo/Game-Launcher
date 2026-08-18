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

    public static void launchGameWithAutoBoost(Context context, GameAppInfo game) {
        if (context == null || game == null) return;

        String pkgName = game.getPackageName();
        if (pkgName == null || pkgName.trim().isEmpty()) return;

        // Strict Shizuku Check: if Shizuku is not active, prompt the user
        if (!ShizukuManager.isShizukuRunningAndGranted()) {
            ShizukuManager.showShizukuPermissionDialog(context, "Game Auto-Boost (" + game.getLabel() + ")");
            return;
        }

        GameProfilePreferences.Profile profile = GameProfilePreferences.getProfile(context, pkgName);
        GameSessionSettings.begin(context, pkgName);
        AutoGameMonitorService.start(context);

        // 1. Offload privileged optimizations to AppExecutors so launch is instant
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                ShizukuUserServiceConnector.getInstance().bindService();
                ShizukuPermissionEnforcer.enforceAllPermissions(context);
                ShizukuUserServiceConnector.getInstance().enforceAppOpsAndPermissions(pkgName);

                // Apply device spoofing / hardware mask
                DeviceSpooferEngine.applySpoofing(context, pkgName);

                // Auto-apply saved per-game Competitive CFG Profile (FPS + Super Touch + Shizuku Hz)
                String pkgLower = pkgName.toLowerCase();
                String gameKey = pkgLower.contains("mobile.legends") || pkgLower.contains("mobilelegends") ? CompetitiveCfgProfile.GAME_MLBB :
                                 pkgLower.contains("pubg") || pkgLower.contains("tencent.ig") || pkgLower.contains("imobile") || pkgLower.contains("vng.pubgmobile") ? CompetitiveCfgProfile.GAME_PUBGM :
                                 pkgLower.contains("cod") || pkgLower.contains("callofduty") || pkgLower.contains("warzone") ? CompetitiveCfgProfile.GAME_CODM :
                                 pkgLower.contains("freefire") || pkgLower.contains("dts.freefire") ? CompetitiveCfgProfile.GAME_FREEFIRE :
                                 pkgLower.contains("genshin") || pkgLower.contains("mihoyo") || pkgLower.contains("cognosphere") || pkgLower.contains("hoyoverse") || pkgLower.contains("hkrpg") || pkgLower.contains("nap") ? CompetitiveCfgProfile.GAME_GENSHIN :
                                 pkgLower.contains("wildrift") || pkgLower.contains("riotgames.league") ? CompetitiveCfgProfile.GAME_WILDRIFT :
                                 pkgLower.contains("sgame") || pkgLower.contains("levelinfinite") || pkgLower.contains("arenaofvalor") || pkgLower.contains("kgtw") || pkgLower.contains("kgvn") ? CompetitiveCfgProfile.GAME_HOK :
                                 pkgLower.contains("bloodstrike") || pkgLower.contains("newspike") ? CompetitiveCfgProfile.GAME_BLOODSTRIKE :
                                 pkgLower.contains("standoff2") || pkgLower.contains("axlebolt") ? CompetitiveCfgProfile.GAME_STANDOFF2 :
                                 pkgLower.contains("carx") || pkgLower.contains("glofta9hm") || pkgLower.contains("asphalt") || pkgLower.contains("r3_row") ? CompetitiveCfgProfile.GAME_CARX :
                                 pkgLower.contains("uamo") || pkgLower.contains("arenabreakout") || pkgLower.contains("deltaforce") ? CompetitiveCfgProfile.GAME_ARENABREAKOUT :
                                 pkgLower.contains("supercell") || pkgLower.contains("brawlstars") || pkgLower.contains("clashroyale") || pkgLower.contains("clashofclans") ? CompetitiveCfgProfile.GAME_SUPERCELL :
                                 pkgLower.contains("roblox") ? CompetitiveCfgProfile.GAME_ROBLOX :
                                 pkgLower.contains("projectc") || pkgLower.contains("valorant") ? CompetitiveCfgProfile.GAME_VALORANT :
                                 pkgLower.contains("farlight") || pkgLower.contains("solarland") ? CompetitiveCfgProfile.GAME_FARLIGHT : CompetitiveCfgProfile.GAME_ALL;
                
                CompetitiveCfgProfile cfgProf = CfgProfileManager.loadProfile(context, gameKey);
                CfgProfileManager.applyProfile(context, gameKey, cfgProf);

                int targetFps = 185; // hard-locked to 185 FPS
                com.gamebooster.app.engine.MasterOptimizationEnforcer.enforceGameLaunchOptimizations(context, pkgName, targetFps);
                GameSpaceDndManager.setGamingDndMode(context, profile.enableDnd);
            } catch (Throwable ignored) {}
        });

        // 2. Perform 3-Tier Game Launch
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
                String res = ShizukuExecutor.executeShizukuCommand("monkey -p " + pkgName + " -c android.intent.category.LAUNCHER 1");
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
                    + profile.label + " up to " + targetFpsForToast(context, pkgName) + "Hz [SHIZUKU PRIVILEGED]", Toast.LENGTH_SHORT).show();
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
