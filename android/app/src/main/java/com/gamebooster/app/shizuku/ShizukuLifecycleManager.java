package com.gamebooster.app.shizuku;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gamebooster.app.booster.BackgroundLimitImmunityEngine;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.device.NetworkStateObserver;

import java.util.concurrent.atomic.AtomicBoolean;

import rikka.shizuku.Shizuku;

/**
 * ShizukuLifecycleManager — Core architecture layer for strict Shizuku official API management.
 *
 * Requirements addressed:
 *  1. Shizuku Detection: Immediately detect installation and running binder.
 *  2. Full Permission Handling: Official Shizuku.requestPermission, no false reporting.
 *  3. Reliable Connection: Official Binder Received and Binder Dead listeners with automatic recovery.
 *  4. Offline Operation: Kernel Binder IPC (/dev/binder) functions 100% offline.
 *  5. Reactive State: Observable LiveData<ShizukuStatus> powering the real-time UI dashboard.
 */
public class ShizukuLifecycleManager implements NetworkStateObserver.Listener, ShizukuManager.ShizukuStateListener {

    private static final String TAG = "ShizukuLifecycleMgr";

    private static volatile ShizukuLifecycleManager sInstance;

    public static ShizukuLifecycleManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ShizukuLifecycleManager.class) {
                if (sInstance == null) {
                    sInstance = new ShizukuLifecycleManager(context);
                }
            }
        }
        return sInstance;
    }

    private final Context appContext;
    private final NetworkStateObserver networkObserver;
    private final MutableLiveData<ShizukuStatus> statusLiveData = new MutableLiveData<>();
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

    private volatile ShizukuStatus currentStatus = new ShizukuStatus();

    private ShizukuLifecycleManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.networkObserver = NetworkStateObserver.getInstance(appContext);
    }

    public LiveData<ShizukuStatus> getStatusLiveData() {
        return statusLiveData;
    }

    public void init() {
        if (!isInitialized.compareAndSet(false, true)) {
            refreshStatus();
            return;
        }

        // Register core Shizuku binder lifecycle listeners via ShizukuManager
        ShizukuManager.registerBinderListeners();
        ShizukuManager.addStateListener(this);

        // Register network listener for online/offline indicator only
        networkObserver.addListener(this);

        // Perform immediate cold-start evaluation
        refreshStatus();
    }

    /**
     * Call in Activity.onResume() or Fragment onHiddenChanged to re-verify status automatically
     * after returning to the APK from games or background state.
     * Proactively forces an immediate binder re-check, re-binds the AIDL UserService,
     * enforces background limit immunity, and performs a multi-pulse recovery post-gaming.
     */
    public void onResumeCheck() {
        // 1. Proactively force a connection check and reset backoff counters
        ShizukuConnectionManager.getInstance().forceReconnectCheck();

        // 2. Re-enforce background immunity so OS never suspends Shizuku or Game Booster
        BackgroundLimitImmunityEngine.enforceImmunity(appContext);

        // 3. Re-bind AIDL UserService if binder is alive
        try {
            if (Shizuku.pingBinder() && !ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                ShizukuUserServiceConnector.getInstance().bindService();
            }
        } catch (Throwable ignored) {}

        // Pass 1: Instant evaluation (0ms)
        refreshStatus();

        // Pass 2: Quick recovery pass (250ms) as IPC stabilizes
        AppExecutors.getInstance().postDelayed(() -> {
            try {
                if (Shizuku.pingBinder()) {
                    if (!ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                        ShizukuUserServiceConnector.getInstance().bindService();
                    }
                    ShizukuConnectionManager.getInstance().whitelistServicesFromDoze();
                } else {
                    BackgroundLimitImmunityEngine.tryAutoResurrectShizukuDaemon();
                    ShizukuConnectionManager.getInstance().forceReconnectCheck();
                }
            } catch (Throwable ignored) {}
            refreshStatus();
        }, 250);

        // Pass 3: Deep recovery pass (750ms) for high-load games that heavily froze background daemons
        AppExecutors.getInstance().postDelayed(() -> {
            try {
                if (Shizuku.pingBinder()) {
                    if (!ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                        ShizukuUserServiceConnector.getInstance().bindService();
                    }
                }
            } catch (Throwable ignored) {}
            refreshStatus();
        }, 750);

        // Pass 4: Final stabilization pass (1500ms) ensuring UI reflects active state
        AppExecutors.getInstance().postDelayed(this::refreshStatus, 1500);
    }

    /**
     * Reads real-time status from the system and updates LiveData.
     */
    public ShizukuStatus refreshStatus() {
        boolean installed = ShizukuManager.isShizukuInstalled(appContext);
        boolean binderAlive = false;
        try {
            binderAlive = Shizuku.pingBinder();
        } catch (Throwable ignored) {}

        boolean permissionGranted = false;
        if (binderAlive) {
            try {
                permissionGranted = (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED);
            } catch (Throwable ignored) {}
        }

        if (binderAlive && permissionGranted) {
            try {
                if (!ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                    ShizukuUserServiceConnector.getInstance().bindService();
                }
            } catch (Throwable ignored) {}
        }

        ShizukuStatus.ConnectionState connState;
        if (binderAlive && permissionGranted) {
            connState = ShizukuStatus.ConnectionState.ACTIVE;
        } else if (binderAlive) {
            connState = ShizukuStatus.ConnectionState.CONNECTED;
        } else {
            ShizukuConnectionManager.State connMgrState = ShizukuConnectionManager.getInstance().getState();
            if (connMgrState == ShizukuConnectionManager.State.BINDING) {
                connState = ShizukuStatus.ConnectionState.CONNECTING;
            } else if (connMgrState == ShizukuConnectionManager.State.DEAD) {
                connState = ShizukuStatus.ConnectionState.DEAD;
            } else if (connMgrState == ShizukuConnectionManager.State.RETRY) {
                connState = ShizukuStatus.ConnectionState.CONNECTING;
            } else {
                connState = ShizukuStatus.ConnectionState.DISCONNECTED;
            }
        }

        boolean online = networkObserver.isOnline();
        ShizukuStatus.AppStatus appState = ShizukuStatus.AppStatus.RUNNING;

        ShizukuStatus newStatus = new ShizukuStatus(
                installed,
                binderAlive,
                permissionGranted,
                connState,
                online,
                appState
        );

        currentStatus = newStatus;
        AppExecutors.getInstance().postToMainThread(() -> statusLiveData.setValue(newStatus));

        return newStatus;
    }

    /**
     * Requests permission via the official Shizuku permission API.
     */
    public void requestPermission() {
        try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    Shizuku.requestPermission(ShizukuManager.REQUEST_CODE_SHIZUKU);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to request Shizuku permission", e);
        }
    }

    /**
     * Opens Shizuku manager app or installation screen.
     */
    public void openShizukuApp(Context context) {
        ShizukuManager.openOrInstallShizukuManager(context);
    }

    /**
     * Shows standard required Shizuku dialog when not running.
     */
    public void showRequiredDialog(Context context, String featureName) {
        ShizukuManager.showShizukuPermissionDialog(context, featureName != null ? featureName : "Game Booster Pro");
    }

    public void showRequiredDialog(Context context) {
        showRequiredDialog(context, "Game Booster Pro");
    }

    @Override
    public void onBinderStateChanged(boolean alive) {
        Log.d(TAG, "Shizuku binder state changed event received: alive=" + alive);
        refreshStatus();
    }

    @Override
    public void onNetworkStateChanged(boolean isOnline) {
        refreshStatus();
    }

    public ShizukuStatus getCurrentStatus() {
        return currentStatus;
    }
}
