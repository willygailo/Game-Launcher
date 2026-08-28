package com.gamebooster.app.apk;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ApkInstallerEngine — High-performance APK discovery, silent Shizuku installation,
 * and APK backup/extraction engine.
 */
public class ApkInstallerEngine {

    private static final String TAG = "ApkInstallerEngine";

    public interface InstallCallback {
        void onResult(boolean success, String message);
    }

    public interface ExtractCallback {
        void onResult(boolean success, String outputPath);
    }

    /**
     * Recursively scans common device storage locations for .apk files.
     */
    public static List<ApkItem> scanStorageApks(Context context) {
        List<ApkItem> results = new ArrayList<>();
        if (context == null) return results;

        PackageManager pm = context.getPackageManager();
        Set<String> scannedPaths = new HashSet<>();

        List<File> searchDirs = new ArrayList<>();
        searchDirs.add(Environment.getExternalStorageDirectory());
        searchDirs.add(new File("/sdcard/Download"));
        searchDirs.add(new File("/sdcard/Downloads"));
        searchDirs.add(new File("/sdcard/Telegram/Telegram Documents"));
        searchDirs.add(new File("/sdcard/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents"));
        searchDirs.add(new File("/sdcard/GameBooster_APKs"));

        File ext = context.getExternalFilesDir(null);
        if (ext != null && ext.getParentFile() != null) {
            searchDirs.add(ext.getParentFile());
        }

        List<File> apkFiles = new ArrayList<>();
        for (File dir : searchDirs) {
            if (dir != null && dir.exists() && dir.canRead()) {
                collectApkFiles(dir, apkFiles, scannedPaths, 0, 3);
            }
        }

        for (File f : apkFiles) {
            try {
                String path = f.getAbsolutePath();
                PackageInfo pi = pm.getPackageArchiveInfo(path, 0);
                if (pi == null) continue;

                ApplicationInfo appInfo = pi.applicationInfo;
                if (appInfo != null) {
                    appInfo.sourceDir = path;
                    appInfo.publicSourceDir = path;
                }

                CharSequence label = appInfo != null ? pm.getApplicationLabel(appInfo) : pi.packageName;
                String appName = (label != null && !label.toString().isEmpty()) ? label.toString() : f.getName().replace(".apk", "");
                String pkg = pi.packageName;
                String verName = pi.versionName != null ? pi.versionName : "1.0";
                int verCode = (int) androidx.core.content.pm.PackageInfoCompat.getLongVersionCode(pi);
                long size = f.length();

                Drawable icon = null;
                try {
                    if (appInfo != null) {
                        icon = pm.getApplicationIcon(appInfo);
                    }
                } catch (Throwable ignored) {}

                boolean isInstalled = false;
                String installedVer = null;
                try {
                    PackageInfo installedPi = pm.getPackageInfo(pkg, 0);
                    if (installedPi != null) {
                        isInstalled = true;
                        installedVer = installedPi.versionName;
                    }
                } catch (Throwable ignored) {}

                results.add(new ApkItem(
                        appName,
                        pkg,
                        verName,
                        verCode,
                        path,
                        size,
                        icon,
                        isInstalled,
                        installedVer
                ));
            } catch (Throwable t) {
                Log.w(TAG, "Error parsing APK file: " + f.getAbsolutePath(), t);
            }
        }

        Collections.sort(results, (a, b) -> a.getAppName().compareToIgnoreCase(b.getAppName()));
        return results;
    }

    private static void collectApkFiles(File dir, List<File> result, Set<String> scanned, int depth, int maxDepth) {
        if (dir == null || !dir.exists() || depth > maxDepth) return;
        String absPath = dir.getAbsolutePath();
        if (scanned.contains(absPath)) return;
        scanned.add(absPath);

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                String name = f.getName();
                if (!name.startsWith(".") && !name.equalsIgnoreCase("Android/data")) {
                    collectApkFiles(f, result, scanned, depth + 1, maxDepth);
                }
            } else if (f.isFile() && f.getName().toLowerCase().endsWith(".apk")) {
                result.add(f);
            }
        }
    }

    /**
     * Installs an APK file via elevated Shizuku shell (silent) or falls back to system package installer.
     */
    public static void installApk(Context context, String apkPath, InstallCallback callback) {
        if (context == null || apkPath == null || apkPath.trim().isEmpty()) {
            if (callback != null) callback.onResult(false, "Invalid APK file path");
            return;
        }

        File apkFile = new File(apkPath);
        if (!apkFile.exists()) {
            if (callback != null) callback.onResult(false, "APK file does not exist: " + apkPath);
            return;
        }

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                if (ShizukuExecutor.hasShizukuPermission()) {
                    // Try elevated Shizuku installation
                    String cmd = "pm install -r -d \"" + apkPath + "\"";
                    String res = ShizukuExecutor.executeShizukuCommand(cmd);

                    if (res != null && (res.contains("Success") || res.contains("success"))) {
                        AppExecutors.getInstance().postToMainThread(() -> {
                            if (callback != null) callback.onResult(true, "Installed successfully via Shizuku!");
                        });
                        return;
                    } else if (res != null && !res.startsWith("ERROR")) {
                        // Might be session install or warning
                        AppExecutors.getInstance().postToMainThread(() -> {
                            if (callback != null) callback.onResult(true, "Installation output: " + res);
                        });
                        return;
                    }
                }

                // Fallback to standard Android Package Installer Intent
                AppExecutors.getInstance().postToMainThread(() -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri apkUri;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            apkUri = FileProvider.getUriForFile(context,
                                    context.getPackageName() + ".fileprovider", apkFile);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } else {
                            apkUri = Uri.fromFile(apkFile);
                        }
                        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);

                        if (callback != null) callback.onResult(true, "Opened system package installer");
                    } catch (Throwable t) {
                        Log.e(TAG, "Standard install fallback failed", t);
                        if (callback != null) callback.onResult(false, "Install failed: " + t.getMessage());
                    }
                });

            } catch (Throwable t) {
                Log.e(TAG, "Failed to install APK: " + apkPath, t);
                AppExecutors.getInstance().postToMainThread(() -> {
                    if (callback != null) callback.onResult(false, "Error: " + t.getMessage());
                });
            }
        });
    }

    /**
     * Extracts and backs up the base APK of an installed package to /sdcard/Download/GameBooster_APKs/.
     */
    public static void extractApk(Context context, String packageName, ExtractCallback callback) {
        if (context == null || packageName == null || packageName.trim().isEmpty()) {
            if (callback != null) callback.onResult(false, null);
            return;
        }

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                PackageManager pm = context.getPackageManager();
                ApplicationInfo appInfo = pm.getApplicationInfo(packageName.trim(), 0);
                String sourceApk = appInfo.sourceDir;

                if (sourceApk == null || !new File(sourceApk).exists()) {
                    if (callback != null) {
                        AppExecutors.getInstance().postToMainThread(() -> callback.onResult(false, "Cannot find source APK"));
                    }
                    return;
                }

                File outDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "GameBooster_APKs");
                if (!outDir.exists()) outDir.mkdirs();

                PackageInfo pi = pm.getPackageInfo(packageName.trim(), 0);
                String ver = pi != null && pi.versionName != null ? pi.versionName : "1.0";
                File targetFile = new File(outDir, packageName + "_v" + ver + ".apk");

                try (InputStream in = new FileInputStream(sourceApk);
                     OutputStream out = new FileOutputStream(targetFile)) {
                    byte[] buf = new byte[65536];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.flush();
                }

                AppExecutors.getInstance().postToMainThread(() -> {
                    if (callback != null) callback.onResult(true, targetFile.getAbsolutePath());
                });

            } catch (Throwable t) {
                Log.e(TAG, "Failed to extract APK: " + packageName, t);
                AppExecutors.getInstance().postToMainThread(() -> {
                    if (callback != null) callback.onResult(false, t.getMessage());
                });
            }
        });
    }

    /**
     * Deletes an APK file from storage.
     */
    public static boolean deleteApkFile(String apkPath) {
        if (apkPath == null) return false;
        try {
            File f = new File(apkPath);
            if (f.exists()) {
                return f.delete();
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
