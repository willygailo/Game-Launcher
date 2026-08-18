package com.gamebooster.app.shizuku;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import rikka.shizuku.Shizuku;

public class ShizukuManager {

    private static final String TAG = "ShizukuManager";
    public static final String SHIZUKU_PACKAGE_NAME = "moe.shizuku.privileged.api";

    public interface ShizukuStateListener {
        void onBinderStateChanged(boolean alive);
    }

    private static final List<ShizukuStateListener> STATE_LISTENERS = new CopyOnWriteArrayList<>();

    public static void addStateListener(ShizukuStateListener listener) {
        if (listener != null && !STATE_LISTENERS.contains(listener)) {
            STATE_LISTENERS.add(listener);
        }
    }

    public static void removeStateListener(ShizukuStateListener listener) {
        if (listener != null) {
            STATE_LISTENERS.remove(listener);
        }
    }

    private static void notifyStateChanged(boolean alive) {
        for (ShizukuStateListener l : STATE_LISTENERS) {
            try {
                l.onBinderStateChanged(alive);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying state listener", e);
            }
        }
    }

    public static final int REQUEST_CODE_SHIZUKU = 1001;

    private static final Shizuku.OnRequestPermissionResultListener PERMISSION_RESULT_LISTENER = (requestCode, grantResult) -> {
        if (requestCode == REQUEST_CODE_SHIZUKU) {
            boolean granted = (grantResult == PackageManager.PERMISSION_GRANTED);
            Log.i(TAG, "Shizuku permission result: " + (granted ? "GRANTED" : "DENIED"));
            if (granted) {
                try {
                    ShizukuUserServiceConnector.getInstance().bindService();
                } catch (Throwable ignored) {}
                // Phase 1.1: drive the connection state machine to READY
                ShizukuConnectionManager.getInstance().onBinderReceived();
            }
            notifyStateChanged(granted);
        }
    };

    private static final Shizuku.OnBinderReceivedListener RECEIVED_LISTENER = () -> {
        Log.i(TAG, "Shizuku binder connected cleanly.");
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(REQUEST_CODE_SHIZUKU);
            } else {
                ShizukuUserServiceConnector.getInstance().bindService();
            }
        } catch (Exception ignored) {}
        // Phase 1.1: converge the connection state machine
        ShizukuConnectionManager.getInstance().onBinderReceived();
        notifyStateChanged(true);
    };

    private static final Shizuku.OnBinderDeadListener DEAD_LISTENER = () -> {
        Log.w(TAG, "Shizuku binder died / service disconnected.");
        ShizukuConnectionManager.getInstance().onBinderDead();
        notifyStateChanged(false);
    };

    public static void registerBinderListeners() {
        try {
            Shizuku.addBinderReceivedListenerSticky(RECEIVED_LISTENER);
            Shizuku.addBinderDeadListener(DEAD_LISTENER);
            Shizuku.addRequestPermissionResultListener(PERMISSION_RESULT_LISTENER);
            Log.d(TAG, "Shizuku binder listeners registered successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to register Shizuku binder listeners", e);
        }
    }

    public static void unregisterBinderListeners() {
        try {
            Shizuku.removeBinderReceivedListener(RECEIVED_LISTENER);
            Shizuku.removeBinderDeadListener(DEAD_LISTENER);
            Shizuku.removeRequestPermissionResultListener(PERMISSION_RESULT_LISTENER);
            Log.d(TAG, "Shizuku binder listeners unregistered.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to unregister Shizuku binder listeners", e);
        }
    }

    /**
     * Checks if Shizuku is currently running, binder is alive, and permission is granted.
     * This is the master gatekeeper for all APK features.
     */
    public static boolean isShizukuRunningAndGranted() {
        try {
            if (!Shizuku.pingBinder()) return false;
            return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Checks if Shizuku is fully operational, including the privileged AIDL UserService.
     */
    public static boolean isFullyOperational() {
        return isShizukuRunningAndGranted() && (ShizukuUserServiceConnector.getInstance().isServiceConnected() || Shizuku.pingBinder());
    }

    /**
     * Strict requirement check: returns true if Shizuku is available, or shows prompt dialog and returns false.
     */
    public static boolean requireShizuku(Context context, String featureTitle) {
        if (isShizukuRunningAndGranted()) {
            return true;
        }
        if (context != null) {
            showShizukuPermissionDialog(context, featureTitle);
        }
        return false;
    }

    public static void requestShizukuPermission() {
        try {
            if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(REQUEST_CODE_SHIZUKU);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error requesting Shizuku permission", e);
        }
    }

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
        String message = "'" + featureTitle + "' requires active Shizuku ADB access for privileged system control.\n\n" +
                (installed ? "Please start and authorize GAME BOOSTER in Shizuku." : "Shizuku Manager is not installed on this device. Please install and start Shizuku.");

        new AlertDialog.Builder(context)
                .setTitle("⚡ SHIZUKU ADB PRIVILEGE REQUIRED")
                .setMessage(message)
                .setPositiveButton(actionBtnText, (dialog, which) -> openOrInstallShizukuManager(context))
                .setNegativeButton("CANCEL", null)
                .show();
    }
}
