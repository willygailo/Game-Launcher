package com.gamebooster.app.focus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.gamebooster.app.config.ManualSettingsPreferences;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * FocusModeEngine — eSports Deep App Freezer & Background Suspension Engine.
 *
 * Implements Android package suspension (pm suspend) and process termination (am force-stop)
 * to freeze non-gaming background applications, dedicating 100% of device CPU, GPU,
 * RAM, and network bandwidth to the active game.
 */
public class FocusModeEngine {

    private static final String TAG = "FocusModeEngine";
    private static final String PREF_NAME = "focus_mode_prefs";
    private static final String KEY_FROZEN_PACKAGES = "frozen_packages_set";
    private static final String KEY_FOCUS_ACTIVE = "focus_mode_active";

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
            "com.oneplus.launcher"
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

            // Skip system pre-installed apps without updates unless they are user-facing
            boolean isSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            boolean isUpdatedSystem = (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
            if (isSystem && !isUpdatedSystem) continue;

            try {
                String label = pm.getApplicationLabel(appInfo).toString();
                boolean isWhitelisted = userWhitelist.contains(pkg);
                boolean isFrozen = currentlyFrozen.contains(pkg);
                // By default, if not explicitly whitelisted, it's a target to freeze
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

                    // 2. Suspend package via Android PM
                    commands.add("pm suspend --user 0 " + pkg);

                    // 3. Deny background execution rights
                    commands.add("cmd appops set " + pkg + " RUN_IN_BACKGROUND ignore 2>/dev/null");
                    commands.add("cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND ignore 2>/dev/null");
                    commands.add("am set-standby-bucket " + pkg + " restricted 2>/dev/null");
                }
            }

            if (!commands.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String c : commands) {
                    sb.append(c).append("; ");
                }
                executeShellBatch(sb.toString());
            }

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
            Set<String> frozen = getFrozenPackages(context);
            int total = frozen.size();
            int current = 0;

            List<String> commands = new ArrayList<>();
            for (String pkg : frozen) {
                if (pkg == null || !pkg.matches("^[a-zA-Z0-9_.]+$")) continue;
                current++;
                if (listener != null) {
                    final int c = current;
                    AppExecutors.getInstance().postToMainThread(() -> listener.onProgress(c, total, pkg));
                }

                commands.add("pm unsuspend --user 0 " + pkg);
                commands.add("cmd appops set " + pkg + " RUN_IN_BACKGROUND allow 2>/dev/null");
                commands.add("cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND allow 2>/dev/null");
                commands.add("am set-standby-bucket " + pkg + " active 2>/dev/null");
            }

            if (!commands.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String c : commands) {
                    sb.append(c).append("; ");
                }
                executeShellBatch(sb.toString());
            }

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

            // Skip active game
            if (activeGamePackage != null && pkg.equalsIgnoreCase(activeGamePackage)) continue;

            // Skip user whitelist
            if (userWhitelist.contains(pkg)) continue;

            packagesToFreeze.add(pkg);

            // 1. Terminate running process to reclaim active RAM
            commands.add("am force-stop " + pkg);

            // 2. Suspend package via Android Package Manager
            commands.add("pm suspend --user 0 " + pkg);

            // 3. Deny background execution rights
            commands.add("cmd appops set " + pkg + " RUN_IN_BACKGROUND ignore 2>/dev/null");
            commands.add("cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND ignore 2>/dev/null");
            commands.add("am set-standby-bucket " + pkg + " restricted 2>/dev/null");
        }

        if (packagesToFreeze.isEmpty()) {
            Log.i(TAG, "No candidate applications found to freeze.");
            getPrefs(context).edit().putBoolean(KEY_FOCUS_ACTIVE, true).putStringSet(KEY_FROZEN_PACKAGES, new HashSet<>()).apply();
            return 0;
        }

        Log.i(TAG, "Freezing " + packagesToFreeze.size() + " background apps for Focus Mode.");

        // Execute batch command
        StringBuilder sb = new StringBuilder();
        for (String c : commands) {
            sb.append(c).append("; ");
        }

        executeShellBatch(sb.toString());

        // Persist state
        getPrefs(context).edit()
                .putBoolean(KEY_FOCUS_ACTIVE, true)
                .putStringSet(KEY_FROZEN_PACKAGES, packagesToFreeze)
                .apply();

        return packagesToFreeze.size();
    }

    /**
     * Unsuspends and restores all previously frozen applications synchronously.
     */
    public static int disableFocusMode(Context context) {
        if (context == null) return 0;

        Set<String> frozen = getFrozenPackages(context);
        if (frozen.isEmpty()) {
            getPrefs(context).edit().putBoolean(KEY_FOCUS_ACTIVE, false).apply();
            return 0;
        }

        Log.i(TAG, "Unsuspending and restoring " + frozen.size() + " applications from Focus Mode.");

        List<String> commands = new ArrayList<>();
        for (String pkg : frozen) {
            // 1. Unsuspend package
            commands.add("pm unsuspend --user 0 " + pkg);

            // 2. Restore normal background permissions
            commands.add("cmd appops set " + pkg + " RUN_IN_BACKGROUND allow 2>/dev/null");
            commands.add("cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND allow 2>/dev/null");
            commands.add("am set-standby-bucket " + pkg + " active 2>/dev/null");
        }

        StringBuilder sb = new StringBuilder();
        for (String c : commands) {
            sb.append(c).append("; ");
        }

        executeShellBatch(sb.toString());

        int count = frozen.size();
        getPrefs(context).edit()
                .putBoolean(KEY_FOCUS_ACTIVE, false)
                .remove(KEY_FROZEN_PACKAGES)
                .apply();

        return count;
    }

    private static void executeShellBatch(String script) {
        try {
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuUserServiceConnector.getInstance().executeCommand(script);
            } else {
                CommandExecutor.executeSystemCommand(script);
            }
        } catch (Throwable t) {
            Log.w(TAG, "FocusMode batch execution fallback: " + t.getMessage());
            CommandExecutor.executeSystemCommand(script);
        }
    }
}
