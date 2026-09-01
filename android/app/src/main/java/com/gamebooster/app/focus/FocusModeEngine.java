package com.gamebooster.app.focus;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

import com.gamebooster.app.config.ManualSettingsPreferences;
import com.gamebooster.app.config.NativeConfigInjector;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * FocusModeEngine — eSports Deep App Freezer & Background Suspension Engine.
 *
 * Implements privileged multi-layer Android package suspension (pm suspend / cmd package suspend),
 * appops background restriction, standby bucket restriction, process termination (am force-stop),
 * gaming DND / heads-up notification suppression, and network bandwidth prioritization to freeze
 * non-gaming background applications, dedicating 100% of device CPU, GPU, RAM, and network
 * bandwidth to the active game.
 */
public class FocusModeEngine {

    private static final String TAG = "FocusModeEngine";
    private static final String PREF_NAME = "focus_mode_prefs";
    private static final String KEY_FROZEN_PACKAGES = "frozen_packages_set";
    private static final String KEY_FOCUS_ACTIVE = "focus_mode_active";
    private static final String KEY_ORIGINAL_HEADS_UP = "original_heads_up_state";
    private static final String KEY_ORIGINAL_DND_FILTER = "original_dnd_filter";
    private static final String KEY_ORIGINAL_NET_RESTRICT = "original_net_restrict";

    public interface OnFreezeOperationListener {
        void onProgress(int current, int total, String appName);
        void onComplete(int totalProcessed, boolean isFrozen);
    }

    // System-critical packages that MUST NEVER be frozen
    private static final Set<String> SYSTEM_CRITICAL_PACKAGES = new HashSet<>(Arrays.asList(
            "android",
            "com.android.systemui",
            "com.android.phone",
            "com.android.server.telecom",
            "com.android.bluetooth",
            "com.android.nfc",
            "com.android.settings",
            "com.android.keyguard",
            "com.android.inputmethod.latin",
            "com.google.android.inputmethod.latin",
            "com.samsung.android.honeyboard",
            "com.sohu.inputmethod.sogou",
            "com.google.android.gms",
            "com.google.android.gsf",
            "com.google.android.gsf.login",
            "com.google.android.play.games",
            "com.google.android.webview",
            "com.android.webview",
            "com.android.vending",
            "com.gamebooster.app",
            "moe.shizuku.privileged.api",
            "com.topjohnwu.magisk",
            "io.github.a13e300.ksu",
            "me.bmax.apatch",
            "com.android.launcher",
            "com.android.launcher3",
            "com.google.android.apps.nexuslauncher",
            "com.sec.android.app.launcher",
            "com.miui.home",
            "com.oppo.launcher",
            "com.huawei.android.launcher",
            "com.transsion.hilauncher",
            "com.oneplus.launcher",
            "com.discord",
            "com.google.android.tts",
            "com.android.providers.media.module",
            "com.android.permissioncontroller",
            "com.google.android.permissioncontroller"
    ));

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isFocusModeActive(Context context) {
        if (context == null) return false;
        return getPrefs(context).getBoolean(KEY_FOCUS_ACTIVE, false);
    }

    public static int getFrozenCount(Context context) {
        if (context == null) return 0;
        Set<String> frozen = getPrefs(context).getStringSet(KEY_FROZEN_PACKAGES, null);
        return (frozen != null) ? frozen.size() : 0;
    }

    public static Set<String> getFrozenPackages(Context context) {
        if (context == null) return new HashSet<>();
        Set<String> set = getPrefs(context).getStringSet(KEY_FROZEN_PACKAGES, null);
        return set != null ? new HashSet<>(set) : new HashSet<>();
    }

    public static boolean isPackageFrozen(Context context, String packageName) {
        if (context == null || packageName == null) return false;
        return getFrozenPackages(context).contains(packageName);
    }

    /**
     * Retrieves all installed applications that can safely be suspended/frozen.
     */
    public static List<FocusAppModel> getFreezableApps(Context context) {
        List<FocusAppModel> result = new ArrayList<>();
        if (context == null) return result;

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        Set<String> userWhitelist = ManualSettingsPreferences.getFocusWhitelist(context);
        Set<String> currentlyFrozen = getFrozenPackages(context);

        for (ApplicationInfo appInfo : installedApps) {
            String pkg = appInfo.packageName;

            // Skip critical system packages
            if (SYSTEM_CRITICAL_PACKAGES.contains(pkg)) continue;
            if (pkg.equals(context.getPackageName())) continue;

            // Skip known game packages so games are not frozen
            if (GamePackageRegistry.isSupportedGame(pkg)) continue;

            // Allow if user-installed OR has a launcher activity (e.g. YouTube, Chrome, Social apps)
            boolean isSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            boolean isUpdatedSystem = (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
            boolean hasLaunchIntent = pm.getLaunchIntentForPackage(pkg) != null;

            if (isSystem && !isUpdatedSystem && !hasLaunchIntent) {
                continue;
            }

            try {
                String label = pm.getApplicationLabel(appInfo).toString();
                boolean isWhitelisted = userWhitelist.contains(pkg);
                boolean isFrozen = currentlyFrozen.contains(pkg);
                boolean selectedToFreeze = !isWhitelisted;
                result.add(new FocusAppModel(pkg, label, appInfo.loadIcon(pm), selectedToFreeze, isFrozen));
            } catch (Throwable ignored) {}
        }

        // Sort alphabetically by label
        result.sort((a, b) -> a.appLabel.compareToIgnoreCase(b.appLabel));
        return result;
    }

    /**
     * Freezes a specific set of target packages asynchronously with real-time progress callbacks.
     */
    public static void freezeSpecificAppsAsync(Context context, Set<String> packagesToFreeze, OnFreezeOperationListener listener) {
        if (context == null) {
            if (listener != null) listener.onComplete(0, true);
            return;
        }

        AppExecutors.getInstance().executeCommand(() -> {
            Set<String> validatedPackages = new HashSet<>();
            List<String> commands = new ArrayList<>();

            int total = packagesToFreeze != null ? packagesToFreeze.size() : 0;
            int current = 0;

            if (packagesToFreeze != null) {
                for (String pkg : packagesToFreeze) {
                    if (pkg == null || SYSTEM_CRITICAL_PACKAGES.contains(pkg) || pkg.equals(context.getPackageName())) {
                        continue;
                    }
                    if (!pkg.matches("^[a-zA-Z0-9_.]+$")) {
                        continue;
                    }

                    validatedPackages.add(pkg);
                    current++;
                    if (listener != null) {
                        final int c = current;
                        AppExecutors.getInstance().postToMainThread(() -> listener.onProgress(c, total, pkg));
                    }

                    // 1. Terminate running process
                    commands.add("am force-stop " + pkg);

                    // 2. Suspend package via Android PM & cmd
                    commands.add("pm suspend --user 0 " + pkg + " 2>/dev/null");
                    commands.add("cmd package suspend --user 0 " + pkg + " 2>/dev/null");

                    // 3. Deny background execution rights & restrict standby bucket
                    commands.add("cmd appops set " + pkg + " RUN_IN_BACKGROUND ignore 2>/dev/null");
                    commands.add("cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND ignore 2>/dev/null");
                    commands.add("am set-standby-bucket " + pkg + " restricted 2>/dev/null");
                    commands.add("am set-standby-bucket " + pkg + " 45 2>/dev/null");
                }
            }

            executeShellCommandsBatched(commands);

            // 4. Engage Gaming DND and Network QoS focus
            enableGamingDnd(context);
            enableNetworkFocus(context);

            // Persist frozen set
            getPrefs(context).edit()
                    .putBoolean(KEY_FOCUS_ACTIVE, !validatedPackages.isEmpty())
                    .putStringSet(KEY_FROZEN_PACKAGES, validatedPackages)
                    .apply();

            ManualSettingsPreferences.setFocusModeEnabled(context, !validatedPackages.isEmpty());

            final int count = validatedPackages.size();
            AppExecutors.getInstance().postToMainThread(() -> {
                if (listener != null) {
                    listener.onComplete(count, true);
                }
            });
        });
    }

    /**
     * Unsuspends and restores all frozen applications asynchronously.
     */
    public static void unfreezeAllAppsAsync(Context context, OnFreezeOperationListener listener) {
        if (context == null) {
            if (listener != null) listener.onComplete(0, false);
            return;
        }

        AppExecutors.getInstance().executeCommand(() -> {
            Set<String> packagesToUnsuspend = new HashSet<>(getFrozenPackages(context));
            List<FocusAppModel> candidates = getFreezableApps(context);
            for (FocusAppModel c : candidates) {
                if (c != null && c.packageName != null) {
                    packagesToUnsuspend.add(c.packageName);
                }
            }

            int total = packagesToUnsuspend.size();
            int current = 0;

            List<String> commands = new ArrayList<>();
            StringBuilder batchPkgList = new StringBuilder();

            for (String pkg : packagesToUnsuspend) {
                if (pkg == null || !pkg.matches("^[a-zA-Z0-9_.]+$")) continue;
                current++;
                if (listener != null) {
                    final int c = current;
                    AppExecutors.getInstance().postToMainThread(() -> listener.onProgress(c, total, pkg));
                }

                // Collect for fast batch unsuspend
                batchPkgList.append(pkg).append(" ");

                // Individual fallbacks & appops restore
                commands.add("pm unsuspend --user 0 " + pkg + " 2>/dev/null");
                commands.add("cmd appops set " + pkg + " RUN_IN_BACKGROUND allow 2>/dev/null");
                commands.add("cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND allow 2>/dev/null");
                commands.add("am set-standby-bucket " + pkg + " active 2>/dev/null");
                commands.add("am set-standby-bucket " + pkg + " 10 2>/dev/null");
            }

            if (batchPkgList.length() > 0) {
                commands.add(0, "cmd package unsuspend --user 0 " + batchPkgList.toString().trim() + " 2>/dev/null");
            }

            executeShellCommandsBatched(commands);

            // Restore Gaming DND and Network Focus
            restoreGamingDnd(context);
            restoreNetworkFocus(context);

            getPrefs(context).edit()
                    .putBoolean(KEY_FOCUS_ACTIVE, false)
                    .remove(KEY_FROZEN_PACKAGES)
                    .apply();

            ManualSettingsPreferences.setFocusModeEnabled(context, false);

            final int count = total;
            AppExecutors.getInstance().postToMainThread(() -> {
                if (listener != null) {
                    listener.onComplete(count, false);
                }
            });
        });
    }

    /**
     * Freezes and suspends all non-whitelisted background applications synchronously.
     */
    public static int enableFocusMode(Context context, String activeGamePackage) {
        if (context == null) return 0;

        List<FocusAppModel> candidates = getFreezableApps(context);
        Set<String> userWhitelist = ManualSettingsPreferences.getFocusWhitelist(context);
        Set<String> packagesToFreeze = new HashSet<>();
        List<String> commands = new ArrayList<>();

        for (FocusAppModel app : candidates) {
            String pkg = app.packageName;

            // Skip active game and any known game package
            if (activeGamePackage != null && pkg.equalsIgnoreCase(activeGamePackage)) continue;
            if (GamePackageRegistry.isSupportedGame(pkg) || GamePackageRegistry.getAllKnownGames().containsKey(pkg)) continue;

            // Skip user whitelist
            if (userWhitelist.contains(pkg)) continue;

            packagesToFreeze.add(pkg);

            // 1. Terminate running process to reclaim active RAM
            commands.add("am force-stop " + pkg);

            // 2. Suspend package via Android Package Manager & cmd
            commands.add("pm suspend --user 0 " + pkg + " 2>/dev/null");
            commands.add("cmd package suspend --user 0 " + pkg + " 2>/dev/null");

            // 3. Deny background execution rights & restrict standby bucket
            commands.add("cmd appops set " + pkg + " RUN_IN_BACKGROUND ignore 2>/dev/null");
            commands.add("cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND ignore 2>/dev/null");
            commands.add("am set-standby-bucket " + pkg + " restricted 2>/dev/null");
            commands.add("am set-standby-bucket " + pkg + " 45 2>/dev/null");
        }

        if (packagesToFreeze.isEmpty()) {
            Log.i(TAG, "No candidate applications found to freeze.");
        } else {
            Log.i(TAG, "Suspending and freezing " + packagesToFreeze.size() + " background apps for focus mode.");
            executeShellCommandsBatched(commands);
        }

        // 4. Engage Gaming DND & Network QoS Bandwidth Focus
        enableGamingDnd(context);
        enableNetworkFocus(context);

        // 5. Pin active game CPU core affinity & scheduling priority
        if (activeGamePackage != null) {
            pinActiveGameCpu(activeGamePackage);
        }

        getPrefs(context).edit()
                .putBoolean(KEY_FOCUS_ACTIVE, true)
                .putStringSet(KEY_FROZEN_PACKAGES, packagesToFreeze)
                .apply();

        ManualSettingsPreferences.setFocusModeEnabled(context, true);

        return packagesToFreeze.size();
    }

    /**
     * Unsuspends all currently frozen applications and restores baseline state synchronously.
     */
    public static int disableFocusMode(Context context) {
        if (context == null) return 0;

        Set<String> packagesToUnsuspend = new HashSet<>(getFrozenPackages(context));
        List<FocusAppModel> candidates = getFreezableApps(context);
        for (FocusAppModel c : candidates) {
            if (c != null && c.packageName != null) {
                packagesToUnsuspend.add(c.packageName);
            }
        }

        Log.i(TAG, "Unsuspending and restoring " + packagesToUnsuspend.size() + " applications from Focus Mode.");

        List<String> commands = new ArrayList<>();
        StringBuilder batchPkgList = new StringBuilder();

        for (String pkg : packagesToUnsuspend) {
            if (pkg == null || !pkg.matches("^[a-zA-Z0-9_.]+$")) continue;
            batchPkgList.append(pkg).append(" ");
            commands.add("pm unsuspend --user 0 " + pkg + " 2>/dev/null");
            commands.add("cmd appops set " + pkg + " RUN_IN_BACKGROUND allow 2>/dev/null");
            commands.add("cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND allow 2>/dev/null");
            commands.add("am set-standby-bucket " + pkg + " active 2>/dev/null");
            commands.add("am set-standby-bucket " + pkg + " 10 2>/dev/null");
        }

        if (batchPkgList.length() > 0) {
            commands.add(0, "cmd package unsuspend --user 0 " + batchPkgList.toString().trim() + " 2>/dev/null");
        }

        executeShellCommandsBatched(commands);

        // Restore Gaming DND and Network Focus
        restoreGamingDnd(context);
        restoreNetworkFocus(context);

        int count = packagesToUnsuspend.size();
        getPrefs(context).edit()
                .putBoolean(KEY_FOCUS_ACTIVE, false)
                .remove(KEY_FROZEN_PACKAGES)
                .apply();

        ManualSettingsPreferences.setFocusModeEnabled(context, false);
        return count;
    }

    // ─── Gaming Do-Not-Disturb & Notification Suppression ────────────────────

    public static void enableGamingDnd(Context context) {
        if (context == null) return;
        try {
            SharedPreferences prefs = getPrefs(context);
            // 1. Capture and suppress Heads-Up Banner Notifications
            try {
                int currentHeadsUp = Settings.Global.getInt(context.getContentResolver(), "heads_up_notifications_enabled", 1);
                prefs.edit().putInt(KEY_ORIGINAL_HEADS_UP, currentHeadsUp).apply();
            } catch (Throwable ignored) {}

            executeShellBatch("settings put global heads_up_notifications_enabled 0 2>/dev/null; "
                    + "cmd notification set_interruption_filter priority 2>/dev/null; "
                    + "cmd notification set_dnd_mode on 2>/dev/null");

            // 2. Framework NotificationManager API
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null && nm.isNotificationPolicyAccessGranted()) {
                int currentFilter = nm.getCurrentInterruptionFilter();
                prefs.edit().putInt(KEY_ORIGINAL_DND_FILTER, currentFilter).apply();
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
            }
        } catch (Throwable t) {
            Log.w(TAG, "enableGamingDnd failed: " + t.getMessage());
        }
    }

    public static void restoreGamingDnd(Context context) {
        if (context == null) return;
        try {
            SharedPreferences prefs = getPrefs(context);
            // 1. Restore Heads-Up Notifications
            int originalHeadsUp = prefs.getInt(KEY_ORIGINAL_HEADS_UP, 1);
            executeShellBatch("settings put global heads_up_notifications_enabled " + originalHeadsUp + " 2>/dev/null; "
                    + "cmd notification set_interruption_filter all 2>/dev/null; "
                    + "cmd notification set_dnd_mode off 2>/dev/null");

            // 2. Restore Notification Policy
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null && nm.isNotificationPolicyAccessGranted()) {
                int originalFilter = prefs.getInt(KEY_ORIGINAL_DND_FILTER, NotificationManager.INTERRUPTION_FILTER_ALL);
                nm.setInterruptionFilter(originalFilter);
            }
        } catch (Throwable t) {
            Log.w(TAG, "restoreGamingDnd failed: " + t.getMessage());
        }
    }

    // ─── Network QoS & Bandwidth Prioritization ──────────────────────────────

    public static void enableNetworkFocus(Context context) {
        if (context == null) return;
        try {
            executeShellBatch("cmd netpolicy set restrict-background true 2>/dev/null; "
                    + "cmd connectivity set-background-data false 2>/dev/null");
        } catch (Throwable t) {
            Log.w(TAG, "enableNetworkFocus failed: " + t.getMessage());
        }
    }

    public static void restoreNetworkFocus(Context context) {
        if (context == null) return;
        try {
            executeShellBatch("cmd netpolicy set restrict-background false 2>/dev/null; "
                    + "cmd connectivity set-background-data true 2>/dev/null");
        } catch (Throwable t) {
            Log.w(TAG, "restoreNetworkFocus failed: " + t.getMessage());
        }
    }

    // ─── Game CPU Core Pinning & Scheduling Priority ─────────────────────────

    public static void pinActiveGameCpu(String activeGamePackage) {
        if (activeGamePackage == null || activeGamePackage.trim().isEmpty()) return;
        try {
            String pidOut = ShizukuExecutor.hasShizukuPermission()
                    ? ShizukuExecutor.executeShizukuCommand("pidof " + activeGamePackage + " 2>/dev/null")
                    : com.gamebooster.app.engine.CommandExecutor.executeSystemCommand("pidof " + activeGamePackage + " 2>/dev/null");

            if (pidOut != null && !pidOut.trim().isEmpty()) {
                String[] pids = pidOut.trim().split("\\s+");
                for (String p : pids) {
                    try {
                        int pid = Integer.parseInt(p);
                        if (pid > 0) {
                            NativeConfigInjector.boostProcessResources(pid, 0);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "pinActiveGameCpu failed for " + activeGamePackage + ": " + t.getMessage());
        }
    }

    private static void executeShellCommandsBatched(List<String> commands) {
        if (commands == null || commands.isEmpty()) return;
        final int BATCH_SIZE = 25;
        for (int i = 0; i < commands.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, commands.size());
            List<String> batch = commands.subList(i, end);
            StringBuilder sb = new StringBuilder();
            for (String c : batch) {
                sb.append(c).append("; ");
            }
            executeShellBatch(sb.toString());
        }
    }

    private static void executeShellBatch(String script) {
        if (script == null || script.trim().isEmpty()) return;
        try {
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(script);
            } else {
                com.gamebooster.app.engine.CommandExecutor.executeSystemCommand(script);
            }
        } catch (Throwable t) {
            Log.w(TAG, "FocusMode batch execution error: " + t.getMessage());
        }
    }
}
