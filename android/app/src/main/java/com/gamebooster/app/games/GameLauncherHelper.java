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
        com.gamebooster.app.overlay.RealGameFpsMonitor.getInstance().setTargetPackage(pkgName);

        // 1. Offload privileged optimizations to AppExecutors so launch is instant
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                ShizukuUserServiceConnector.getInstance().bindService();
                ShizukuPermissionEnforcer.enforceAllPermissions(context);
                ShizukuUserServiceConnector.getInstance().enforceAppOpsAndPermissions(pkgName);

                // Apply device spoofing / hardware mask
                DeviceSpooferEngine.applySpoofing(context, pkgName);

                // Auto-apply saved per-game Competitive CFG Profile (FPS + Super Touch + Shizuku Hz)
                String gameKey = CfgProfileManager.resolveGameKey(pkgName);
                
                CompetitiveCfgProfile cfgProf = CfgProfileManager.loadProfile(context, gameKey);
                CfgProfileManager.applyProfile(context, gameKey, cfgProf);

                int targetFps = com.gamebooster.app.config.GameProfileAutoConfigurator.getTargetFpsHz(context);
                com.gamebooster.app.engine.MasterOptimizationEnforcer.enforceGameLaunchOptimizations(
                        context, pkgName, targetFps, report -> {
                    // Phase 1.2: surface per-step failures/skips instead of a silent Log.w
                    if (report != null && !report.fullyApplied()) {
                        com.gamebooster.app.ui.dialogs.CyberActionDialog.showDetailed(
                                context, "MASTER OPTIMIZATION REPORT", false, 4500,
                                report.toDialogLines());
                    }
                });
                GameSpaceDndManager.setGamingDndMode(context, profile.enableDnd);
            } catch (Throwable ignored) {}
        });

        // 2. Perform 3-Tier Game Launch
        boolean launched = false;

        // Tier 1: Standard Intent Launch with explicit component
        Intent launchIntent = game.getLaunchIntent();
        if (launchIntent == null) {
            launchIntent = HomeGameScanner.resolveLaunchIntent(context.getPackageManager(), pkgName);
        }
        if (launchIntent != null) {
            try {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(launchIntent);
                launched = true;
            } catch (Throwable ignored) {}
        }

        // Tier 2: PackageManager Re-query
        if (!launched) {
            try {
                Intent pmIntent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
                if (pmIntent != null) {
                    pmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                            | Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(pmIntent);
                    launched = true;
                }
            } catch (Throwable ignored) {}
        }

        // Tier 3: Privileged Shizuku / Rish / Root Launch Fallback
        if (!launched) {
            try {
                String cmd = "am start --activity-brought-to-front -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -p " + pkgName + " 2>/dev/null || monkey -p " + pkgName + " -c android.intent.category.LAUNCHER 1";
                if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                    ShizukuUserServiceConnector.getInstance().executeCommand(cmd);
                    launched = true;
                } else if (ShizukuExecutor.hasShizukuPermission()) {
                    ShizukuExecutor.executeShizukuCommand(cmd);
                    launched = true;
                } else if (com.gamebooster.app.engine.ShellExecutor.isRootSuAvailable()) {
                    com.gamebooster.app.engine.ShellExecutor.executeCommand(cmd, true);
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
