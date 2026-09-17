package com.gamebooster.app.shizuku;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.gamebooster.app.booster.BackgroundLimitImmunityEngine;
import com.gamebooster.app.core.AppExecutors;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import rikka.shizuku.Shizuku;

/**
 * ShizukuAutoConnectEngine — 2026 Zero-Touch Auto-Connect Engine.
 *
 * Eliminates cold-start Binder IPC race conditions when opening the APK.
 * Features:
 * 1. Early Boot Auto-Resurrection: Auto-starts Shizuku daemon via root su if binder is dead.
 * 2. Multi-Pulse Scanner: Polls at 0ms, 50ms, 150ms, 350ms, 700ms, 1500ms, 3000ms until binder arrives.
 * 3. Foreground Permission Dispatch: Auto-pops Shizuku permission request on active Activity.
 * 4. Zero-Touch AIDL Binding: Immediately connects IUserService & enforces background immunity upon grant.
 */
public final class ShizukuAutoConnectEngine {

    private static final String TAG = "ShizukuAutoConnect";

    private static final long[] PULSE_DELAYS_MS = {0L, 50L, 150L, 350L, 700L, 1500L, 3000L};

    private static final AtomicBoolean sPulseInProgress = new AtomicBoolean(false);
    private static volatile WeakReference<Activity> sForegroundActivityRef = new WeakReference<>(null);

    private ShizukuAutoConnectEngine() {}

    /**
     * Sets the active foreground Activity for safe dialog and permission dispatching.
     */
    public static void setForegroundActivity(Activity activity) {
        if (activity != null) {
            sForegroundActivityRef = new WeakReference<>(activity);
        }
    }

    /**
     * Clears foreground activity reference when destroyed.
     */
    public static void clearForegroundActivity(Activity activity) {
        Activity current = sForegroundActivityRef.get();
        if (current == activity) {
            sForegroundActivityRef.clear();
        }
    }

    /**
     * Early initialization called in GameBoosterApp.onCreate().
     * Checks if daemon needs root resurrection and kicks off initial binder pulse.
     */
    public static void startEarlyAutoConnect(Context context) {
        if (context == null) return;
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                boolean pingAlive = Shizuku.pingBinder();
                if (!pingAlive) {
                    // Attempt daemon resurrection if root is available
                    BackgroundLimitImmunityEngine.tryAutoResurrectShizukuDaemon();
                }
            } catch (Throwable t) {
                Log.w(TAG, "Early auto-resurrect check warning: " + t.getMessage());
            }

            // Launch first multi-pulse scan
            triggerMultiPulseScan(context.getApplicationContext());
        });
    }

    /**
     * Invoked when MainActivity opens (onCreate & onResume).
     * Binds foreground activity context and fires immediate multi-pulse scan.
     */
    public static void pulseConnect(Activity activity) {
        if (activity == null) return;
        setForegroundActivity(activity);

        Context appContext = activity.getApplicationContext();
        AppExecutors.getInstance().executeCommand(() -> {
            // Immediate check
            if (evaluateAndConnect(appContext)) {
                return;
            }

            // If not connected, kick off multi-pulse scanner
            triggerMultiPulseScan(appContext);
        });
    }

    /**
     * Executes non-blocking staggered pulses across [0ms, 50ms, 150ms, 350ms, 700ms, 1500ms, 3000ms].
     * Stops the moment Shizuku is connected and authorized.
     */
    public static void triggerMultiPulseScan(Context context) {
        if (!sPulseInProgress.compareAndSet(false, true)) {
            Log.d(TAG, "Pulse scan already in progress, skipping duplicate schedule.");
            return;
        }

        for (int i = 0; i < PULSE_DELAYS_MS.length; i++) {
            long delay = PULSE_DELAYS_MS[i];
            boolean isLast = (i == PULSE_DELAYS_MS.length - 1);

            AppExecutors.getInstance().postDelayed(() -> {
                try {
                    boolean success = evaluateAndConnect(context);
                    if (success || isLast) {
                        sPulseInProgress.set(false);
                        if (success) {
                            Log.i(TAG, "Auto-connected to Shizuku API successfully!");
                        }
                    }
                } catch (Throwable t) {
                    if (isLast) sPulseInProgress.set(false);
                }
            }, delay);
        }
    }

    /**
     * Evaluates binder status, requests permission if ungranted, and executes post-connect pipeline.
     * Returns true if Shizuku is fully running and granted.
     */
    public static boolean evaluateAndConnect(Context context) {
        try {
            boolean pingAlive = Shizuku.pingBinder();
            if (!pingAlive) {
                // If dead on rooted device, try resurrection once
                BackgroundLimitImmunityEngine.tryAutoResurrectShizukuDaemon();
                return false;
            }

            // Binder is alive! Check permission
            int permStatus = PackageManager.PERMISSION_DENIED;
            try {
                permStatus = Shizuku.checkSelfPermission();
            } catch (Throwable ignored) {}

            if (permStatus == PackageManager.PERMISSION_GRANTED) {
                // Fully granted: execute post-connection sequence
                onAutoConnectSuccess(context);
                return true;
            } else {
                // Binder is alive but permission not granted: auto-request permission immediately
                requestPermissionAuto();
                return false;
            }
        } catch (Throwable t) {
            Log.w(TAG, "evaluateAndConnect error: " + t.getMessage());
            return false;
        }
    }

    /**
     * Dispatches Shizuku.requestPermission() safely on the Main Thread using foreground Activity if available.
     */
    public static void requestPermissionAuto() {
        AppExecutors.getInstance().postToMainThread(() -> {
            try {
                if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    Activity act = sForegroundActivityRef.get();
                    if (act != null && !act.isFinishing() && !act.isDestroyed()) {
                        Log.i(TAG, "Auto-requesting Shizuku permission on foreground activity: " + act.getClass().getSimpleName());
                        Shizuku.requestPermission(ShizukuManager.REQUEST_CODE_SHIZUKU);
                    } else {
                        Log.i(TAG, "Auto-requesting Shizuku permission globally");
                        Shizuku.requestPermission(ShizukuManager.REQUEST_CODE_SHIZUKU);
                    }
                }
            } catch (Throwable t) {
                Log.w(TAG, "Auto-permission request error: " + t.getMessage());
            }
        });
    }

    /**
     * Pipeline executed the millisecond Shizuku is connected and authorized.
     */
    private static void onAutoConnectSuccess(Context context) {
        if (context != null) {
            com.gamebooster.app.config.ShizukuPreferences.setShizukuEverGranted(context, true);
            com.gamebooster.app.config.ShizukuPreferences.recordOnlineTimestamp(context);
        }

        // 1. Notify ShizukuConnectionManager
        ShizukuConnectionManager.getInstance().onBinderReceived();

        // 2. Bind AIDL UserService
        if (!ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
            ShizukuUserServiceConnector.getInstance().bindService();
        }

        // 3. Enforce background immunity shield & privileged permissions
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                if (context != null) {
                    ShizukuPermissionEnforcer.enforceAllPermissions(context, false);
                    ShizukuFileManager.grantAllStoragePermissions(context);
                    BackgroundLimitImmunityEngine.enforceImmunity(context);
                }
                ShizukuManager.triggerThrottledPostConnectionSync();
            } catch (Throwable t) {
                Log.w(TAG, "Post-connect enforcement warning: " + t.getMessage());
            }
        });
    }
}
