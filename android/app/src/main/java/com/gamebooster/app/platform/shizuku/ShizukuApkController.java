package com.gamebooster.app.platform.shizuku;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * ShizukuApkController delivers complete, elevated APK & package management
 * via Shizuku ADB shell privileges (uid 2000 / shell).
 *
 * Capabilities:
 *  - Silent APK Installation (pm install -r -d -g)
 *  - Silent Uninstallation (pm uninstall)
 *  - Freeze / Disable App (pm disable-user --user 0)
 *  - Unfreeze / Enable App (pm enable)
 *  - Clear App Data & Cache (pm clear)
 *  - Force Stop / Kill Application (am force-stop / kill -9)
 *  - AppOps & Elevated System Permissions Granter
 *  - App Launch with Boost Flags
 */
public class ShizukuApkController {

    private static final String TAG = "ShizukuApkController";

    public static class AppDetail {
        public final String packageName;
        public final String appName;
        public final boolean isSystem;
        public final boolean isEnabled;
        public final String apkPath;

        public AppDetail(String packageName, String appName, boolean isSystem, boolean isEnabled, String apkPath) {
            this.packageName = packageName;
            this.appName = appName;
            this.isSystem = isSystem;
            this.isEnabled = isEnabled;
            this.apkPath = apkPath;
        }
    }

    /**
     * Silently installs an APK file located at the specified local path.
     * Flags:
     *  -r : Reinstall existing app, keeping its data
     *  -d : Allow version code downgrade
     *  -g : Automatically grant all runtime permissions on install
     */
    public static boolean installApkSilently(String apkFilePath) {
        if (apkFilePath == null || apkFilePath.trim().isEmpty()) return false;
        String cmd = "pm install -r -d -g \"" + apkFilePath.trim() + "\"";
        String res = ShizukuExecutor.executeShizukuCommand(cmd);
        Log.d(TAG, "installApkSilently result: " + res);
        return res != null && res.toLowerCase().contains("success");
    }

    /**
     * Silently uninstalls an application by package name.
     * @param keepDataAndCache If true, passes -k to retain app data/cache directories.
     */
    public static boolean uninstallApkSilently(String packageName, boolean keepDataAndCache) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String flag = keepDataAndCache ? "-k " : "";
        String cmd = "pm uninstall " + flag + packageName.trim();
        String res = ShizukuExecutor.executeShizukuCommand(cmd);
        Log.d(TAG, "uninstallApkSilently result: " + res);
        return res != null && res.toLowerCase().contains("success");
    }

    /**
     * Freezes (disables) an application for user 0 without uninstalling it.
     */
    public static boolean freezeApp(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String cmd = "pm disable-user --user 0 " + packageName.trim();
        String res = ShizukuExecutor.executeShizukuCommand(cmd);
        Log.d(TAG, "freezeApp result: " + res);
        return res != null && (res.contains("new state: disabled") || res.contains("disabled-user") || res.contains("disabled"));
    }

    /**
     * Unfreezes (enables) a previously frozen application.
     */
    public static boolean unfreezeApp(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String cmd = "pm enable " + packageName.trim();
        String res = ShizukuExecutor.executeShizukuCommand(cmd);
        Log.d(TAG, "unfreezeApp result: " + res);
        return res != null && (res.contains("new state: enabled") || res.contains("enabled"));
    }

    /**
     * Clears all user data, databases, and cache for the given package.
     */
    public static boolean clearAppData(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String cmd = "pm clear " + packageName.trim();
        String res = ShizukuExecutor.executeShizukuCommand(cmd);
        Log.d(TAG, "clearAppData result: " + res);
        return res != null && res.toLowerCase().contains("success");
    }

    /**
     * Force stops an application and terminates all of its active processes.
     */
    public static boolean forceStopApp(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String cmd = "am force-stop " + packageName.trim();
        String res = ShizukuExecutor.executeShizukuCommand(cmd);
        Log.d(TAG, "forceStopApp result: " + res);
        return res != null && !res.toLowerCase().contains("error");
    }

    /**
     * Grants a specific runtime Android permission to a package.
     */
    public static boolean grantPermission(String packageName, String permission) {
        if (packageName == null || permission == null) return false;
        String cmd = "pm grant " + packageName.trim() + " " + permission.trim();
        String res = ShizukuExecutor.executeShizukuCommand(cmd);
        return res != null && !res.toLowerCase().contains("error");
    }

    /**
     * Sets AppOps mode for a given package (e.g. SYSTEM_ALERT_WINDOW, MANAGE_EXTERNAL_STORAGE).
     */
    public static boolean setAppOp(String packageName, String opName, String mode) {
        if (packageName == null || opName == null || mode == null) return false;
        String cmd = "appops set " + packageName.trim() + " " + opName.trim() + " " + mode.trim();
        String res = ShizukuExecutor.executeShizukuCommand(cmd);
        return res != null && !res.toLowerCase().contains("error");
    }

    /**
     * Launches an app directly via Intent invocation through ADB shell.
     */
    public static boolean launchApp(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String cmd = "monkey -p " + packageName.trim() + " -c android.intent.category.LAUNCHER 1";
        String res = ShizukuExecutor.executeShizukuCommand(cmd);
        return res != null && (res.contains("Events injected") || res.contains("monkey"));
    }

    /**
     * Queries detailed list of installed applications.
     */
    public static List<AppDetail> getInstalledApps(Context context, boolean includeSystem) {
        List<AppDetail> list = new ArrayList<>();
        if (context == null) return list;
        try {
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_META_DATA);
            for (PackageInfo pInfo : packages) {
                boolean isSys = (pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                if (!includeSystem && isSys) {
                    continue;
                }
                String appName = pInfo.applicationInfo.loadLabel(pm).toString();
                boolean isEnabled = pInfo.applicationInfo.enabled;
                String apkPath = pInfo.applicationInfo.sourceDir;
                list.add(new AppDetail(pInfo.packageName, appName, isSys, isEnabled, apkPath));
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error listing installed apps: " + t.getMessage());
        }
        return list;
    }
}
