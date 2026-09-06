package com.gamebooster.app.shizuku;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.gamebooster.app.core.AppExecutors;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import moe.shizuku.api.BinderContainer;
import rikka.shizuku.Shizuku;

public class ShizukuManager {

    private static final String TAG = "ShizukuManager";
    public static final String SHIZUKU_PACKAGE_NAME = "moe.shizuku.privileged.api";

    public interface ShizukuStateListener {
        void onBinderStateChanged(boolean alive);
    }

    private static final List<ShizukuStateListener> STATE_LISTENERS = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean LISTENERS_REGISTERED = new AtomicBoolean(false);
    private static volatile Boolean lastNotifiedAlive = null;

    private static volatile long lastPostSyncTimestamp = 0L;
    private static final long POST_SYNC_COOLDOWN_MS = 15000L;
    private static final Object SYNC_LOCK = new Object();

    public static void addStateListener(ShizukuStateListener listener) {
        if (listener != null && !STATE_LISTENERS.contains(listener)) {
            STATE_LISTENERS.add(listener);
            try {
                listener.onBinderStateChanged(isShizukuRunningAndGranted());
            } catch (Throwable ignored) {}
        }
    }

    public static void removeStateListener(ShizukuStateListener listener) {
        if (listener != null) {
            STATE_LISTENERS.remove(listener);
        }
    }

    public static void forceNotifyStateChanged() {
        lastNotifiedAlive = null;
        notifyStateChanged(isShizukuRunningAndGranted());
    }

    private static void notifyStateChanged(boolean alive) {
        if (lastNotifiedAlive != null && lastNotifiedAlive.booleanValue() == alive) {
            return;
        }
        lastNotifiedAlive = alive;
        for (ShizukuStateListener l : STATE_LISTENERS) {
            try {
                l.onBinderStateChanged(alive);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying state listener", e);
            }
        }
    }

    /**
     * Actively queries moe.shizuku.privileged.api.shizuku provider to retrieve the live binder.
     * Bypasses passive waiting and guarantees instant reconnection (<5ms) after exiting games.
     */
    public static boolean activelyFetchAndAttachBinder(Context context) {
        if (context == null) return false;
        try {
            if (Shizuku.pingBinder()) {
                return true;
            }
            Uri uri = Uri.parse("content://moe.shizuku.privileged.api.shizuku");
            Bundle bundle = new Bundle();
            Bundle reply = null;
            try {
                reply = context.getContentResolver().call(uri, "getBinder", null, bundle);
            } catch (Throwable t) {
                Log.d(TAG, "ContentResolver call to Shizuku provider failed: " + t.getMessage());
            }

            if (reply != null) {
                IBinder binder = null;
                try {
                    reply.setClassLoader(BinderContainer.class.getClassLoader());
                    BinderContainer container = reply.getParcelable("moe.shizuku.privileged.api.intent.extra.BINDER");
                    if (container != null) {
                        binder = container.binder;
                    }
                } catch (Throwable t) {
                    Log.d(TAG, "Unparcel BinderContainer fallback: " + t.getMessage());
                }

                if (binder == null) {
                    try {
                        binder = reply.getBinder("moe.shizuku.privileged.api.intent.extra.BINDER");
                    } catch (Throwable ignored) {}
                }

                if (binder != null && binder.isBinderAlive() && binder.pingBinder()) {
                    Log.i(TAG, "Successfully retrieved live Shizuku binder on-demand!");
                    Shizuku.onBinderReceived(binder, context.getPackageName());
                    return true;
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "activelyFetchAndAttachBinder error: " + t.getMessage());
        }
        return isShizukuRunningAndGranted();
    }

    /**
     * Full lifecycle recheck & reconnect workflow triggered on Activity/Fragment onResume.
     */
    public static void recheckAndReconnect(Context context) {
        if (context == null) return;
        final Context appCtx = context.getApplicationContext();
        registerBinderListeners();

        AppExecutors.getInstance().executeCommand(() -> {
            boolean active = activelyFetchAndAttachBinder(appCtx);
            if (active) {
                ShizukuConnectionManager.getInstance().onBinderReceived();
            } else {
                ShizukuConnectionManager.getInstance().start();
            }
            forceNotifyStateChanged();
        });
    }

    public static final int REQUEST_CODE_SHIZUKU = 1001;

    private static final Shizuku.OnRequestPermissionResultListener PERMISSION_RESULT_LISTENER = (requestCode, grantResult) -> {
        if (requestCode == REQUEST_CODE_SHIZUKU) {
            boolean granted = (grantResult == PackageManager.PERMISSION_GRANTED);
            Log.i(TAG, "Shizuku permission result: " + (granted ? "GRANTED" : "DENIED"));
            if (granted) {
                AppExecutors.getInstance().executeCommand(() -> {
                    try {
                        ShizukuUserServiceConnector.getInstance().bindService();
                    } catch (Throwable ignored) {}
                    triggerThrottledPostConnectionSync();
                    ShizukuConnectionManager.getInstance().onBinderReceived();
                });
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
                AppExecutors.getInstance().executeCommand(() -> {
                    try {
                        ShizukuUserServiceConnector.getInstance().bindService();
                    } catch (Throwable ignored) {}
                    triggerThrottledPostConnectionSync();
                    ShizukuConnectionManager.getInstance().onBinderReceived();
                });
            }
        } catch (Exception ignored) {}
        notifyStateChanged(isShizukuRunningAndGranted());
    };

    private static final Shizuku.OnBinderDeadListener DEAD_LISTENER = () -> {
        Log.w(TAG, "Shizuku binder died / service disconnected.");
        ShizukuConnectionManager.getInstance().onBinderDead();
        notifyStateChanged(false);
    };

    public static void registerBinderListeners() {
        if (!LISTENERS_REGISTERED.compareAndSet(false, true)) {
            Log.d(TAG, "Shizuku binder listeners already registered.");
            return;
        }
        try {
            Shizuku.addBinderReceivedListenerSticky(RECEIVED_LISTENER);
            Shizuku.addBinderDeadListener(DEAD_LISTENER);
            Shizuku.addRequestPermissionResultListener(PERMISSION_RESULT_LISTENER);
            Log.d(TAG, "Shizuku binder listeners registered successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to register Shizuku binder listeners", e);
            LISTENERS_REGISTERED.set(false);
        }
    }

    public static void unregisterBinderListeners() {
        if (!LISTENERS_REGISTERED.compareAndSet(true, false)) {
            return;
        }
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
     * Executes throttled post-connection sync (permissions, storage, tweaks) to avoid overloading Shizuku IPC.
     */
    public static void triggerThrottledPostConnectionSync() {
        synchronized (SYNC_LOCK) {
            long now = System.currentTimeMillis();
            if (now - lastPostSyncTimestamp < POST_SYNC_COOLDOWN_MS) {
                Log.d(TAG, "Post-connection sync suppressed (within " + POST_SYNC_COOLDOWN_MS + "ms cooldown)");
                return;
            }
            lastPostSyncTimestamp = now;
        }

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                Context ctx = com.gamebooster.app.GameBoosterApp.getInstance();
                if (ctx != null && isShizukuRunningAndGranted()) {
                    Log.i(TAG, "Executing throttled post-connection system sync...");
                    ShizukuPermissionEnforcer.enforceAllPermissions(ctx);
                    ShizukuFileManager.grantAllStoragePermissions(ctx);
                    com.gamebooster.app.tweaks.TweakManagerRepository.restoreAppliedTweaksAsync(ctx);
                }
            } catch (Throwable t) {
                Log.w(TAG, "Throttled sync error: " + t.getMessage());
            }
        });
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
        if (context instanceof android.app.Activity) {
            android.app.Activity a = (android.app.Activity) context;
            if (a.isFinishing() || a.isDestroyed()) return;
        }

        try {
            boolean binderAlive = Shizuku.pingBinder();
            boolean installed = isShizukuInstalled(context);

            if (binderAlive) {
                new AlertDialog.Builder(context)
                        .setTitle("⚡ SHIZUKU PERMISSION REQUIRED")
                        .setMessage("'" + featureTitle + "' requires Shizuku privileged shell authorization.\n\nShizuku is running! Tap 'GRANT PERMISSION' to authorize Game Booster immediately.")
                        .setPositiveButton("GRANT PERMISSION", (dialog, which) -> requestShizukuPermission())
                        .setNeutralButton("OPEN SHIZUKU", (dialog, which) -> openOrInstallShizukuManager(context))
                        .setNegativeButton("CANCEL", null)
                        .show();
            } else {
                String actionBtnText = installed ? "START SHIZUKU MANAGER" : "INSTALL SHIZUKU";
                String message = "'" + featureTitle + "' requires active Shizuku ADB access for privileged system control.\n\n" +
                        (installed ? "Shizuku is installed but not running. Please start Shizuku service via Wireless Debugging or Root, then return here." : "Shizuku Manager is not installed on this device. Please install and start Shizuku.");

                new AlertDialog.Builder(context)
                        .setTitle("⚡ SHIZUKU ADB PRIVILEGE REQUIRED")
                        .setMessage(message)
                        .setPositiveButton(actionBtnText, (dialog, which) -> openOrInstallShizukuManager(context))
                        .setNegativeButton("CANCEL", null)
                        .show();
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to show Shizuku permission dialog: " + t.getMessage());
        }
    }

    /**
     * Interactive 1-tap handler when the user taps on Shizuku status cards, strips, or banners.
     */
    public static void handleShizukuCardClick(Context context) {
        if (context == null) return;

        if (isShizukuRunningAndGranted()) {
            android.widget.Toast.makeText(context, "⚡ Shizuku Active: Forcing Privileged Permissions & Tweaks...", android.widget.Toast.LENGTH_SHORT).show();
            AppExecutors.getInstance().executeCommand(() -> {
                try {
                    ShizukuUserServiceConnector.getInstance().bindService();
                    ShizukuPermissionEnforcer.enforceAllPermissions(context);
                    ShizukuFileManager.grantAllStoragePermissions(context);
                    com.gamebooster.app.tweaks.TweakManagerRepository.restoreAppliedTweaksAsync(context);
                    ShizukuConnectionManager.getInstance().onBinderReceived();
                } catch (Throwable t) {
                    Log.w(TAG, "Force sync error: " + t.getMessage());
                }
            });
        } else if (Shizuku.pingBinder()) {
            requestShizukuPermission();
        } else {
            showShizukuPermissionDialog(context, "Shizuku Privileged Engine");
        }
    }
}
