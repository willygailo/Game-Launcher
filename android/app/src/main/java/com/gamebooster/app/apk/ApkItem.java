package com.gamebooster.app.apk;

import android.graphics.drawable.Drawable;

/**
 * Model representing a standalone APK file located on device storage.
 */
public class ApkItem {

    private final String appName;
    private final String packageName;
    private final String versionName;
    private final int versionCode;
    private final String filePath;
    private final long fileSizeBytes;
    private final Drawable icon;
    private final boolean isInstalled;
    private final String installedVersionName;

    public ApkItem(String appName, String packageName, String versionName, int versionCode,
                   String filePath, long fileSizeBytes, Drawable icon, boolean isInstalled, String installedVersionName) {
        this.appName = appName;
        this.packageName = packageName;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.filePath = filePath;
        this.fileSizeBytes = fileSizeBytes;
        this.icon = icon;
        this.isInstalled = isInstalled;
        this.installedVersionName = installedVersionName;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public Drawable getIcon() {
        return icon;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public String getInstalledVersionName() {
        return installedVersionName;
    }
}
