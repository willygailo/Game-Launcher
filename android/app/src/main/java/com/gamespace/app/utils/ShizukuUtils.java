package com.gamespace.app.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;

public class ShizukuUtils {

    public static final String SHIZUKU_PACKAGE_NAME = "moe.shizuku.privileged.api";

    public static boolean isShizukuInstalled(Context context) {
        if (context == null) return false;
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(SHIZUKU_PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void openOrInstallShizukuManager(Context context) {
        if (context == null) return;
        PackageManager pm = context.getPackageManager();
        if (isShizukuInstalled(context)) {
            Intent launchIntent = pm.getLaunchIntentForPackage(SHIZUKU_PACKAGE_NAME);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
                return;
            }
        }
        try {
            Intent storeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + SHIZUKU_PACKAGE_NAME));
            storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(storeIntent);
        } catch (Exception e) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app"));
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(webIntent);
        }
    }

    public static void showShizukuPermissionDialog(Context context, String featureTitle) {
        if (context == null) return;

        boolean installed = isShizukuInstalled(context);
        String actionBtnText = installed ? "OPEN SHIZUKU MANAGER" : "INSTALL SHIZUKU";
        String message = "'" + featureTitle + "' requires 1-Tap Shizuku ADB access for system-level acceleration.\n\n" +
                (installed ? "Please authorize GAME SPACE inside the Shizuku app." : "Shizuku Manager is not installed on this device.");

        new AlertDialog.Builder(context)
                .setTitle("⚡ SHIZUKU ADB PERMISSION REQUIRED")
                .setMessage(message)
                .setPositiveButton(actionBtnText, (dialog, which) -> openOrInstallShizukuManager(context))
                .setNegativeButton("CANCEL", null)
                .show();
    }
}
