package com.gamebooster.app.shizuku;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.gamebooster.app.BuildConfig;

import rikka.shizuku.Shizuku;

public class ShizukuUserServiceConnector {

    private static final String TAG = "ShizukuUserService";
    private static final ShizukuUserServiceConnector INSTANCE = new ShizukuUserServiceConnector();

    private IUserService userServiceInstance = null;
    private boolean isBinding = false;
    private int mRetryCount = 0;
    private static final int MAX_RETRY_COUNT = 5;
    private static final long BASE_RETRY_DELAY_MS = 1000L; // 1s base, doubles each retry

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "IUserService connected successfully under privileged shell UID.");
            userServiceInstance = IUserService.Stub.asInterface(service);
            isBinding = false;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "IUserService disconnected / unbound.");
            userServiceInstance = null;
            isBinding = false;
            // Exponential backoff reconnect — waits 1s, 2s, 4s, 8s, 16s before giving up
            if (mRetryCount < MAX_RETRY_COUNT) {
                long delayMs = BASE_RETRY_DELAY_MS * (1L << mRetryCount);
                mRetryCount++;
                Log.i(TAG, "Scheduling UserService reconnect attempt " + mRetryCount
                        + " in " + delayMs + "ms...");
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                        ShizukuUserServiceConnector.this::bindService, delayMs
                );
            } else {
                Log.w(TAG, "UserService max retry count reached — giving up reconnect.");
            }
        }
    };

    private final Shizuku.UserServiceArgs serviceArgs = new Shizuku.UserServiceArgs(
            new ComponentName(BuildConfig.APPLICATION_ID, UserService.class.getName()))
            .daemon(false)
            .processNameSuffix("service")
            .debuggable(BuildConfig.DEBUG)
            .version(1);

    public static ShizukuUserServiceConnector getInstance() {
        return INSTANCE;
    }

    public synchronized void bindService() {
        if (userServiceInstance != null || isBinding) {
            Log.d(TAG, "bindService: already bound or binding — skipping.");
            return;
        }
        try {
            if (Shizuku.pingBinder()) {
                Log.d(TAG, "Binding Shizuku UserService via AIDL...");
                isBinding = true;
                mRetryCount = 0; // Reset retry counter on fresh bind
                Shizuku.bindUserService(serviceArgs, serviceConnection);
            } else {
                Log.w(TAG, "bindService: Shizuku binder not alive — skipping bind.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to bind Shizuku UserService", e);
            isBinding = false;
        }
    }

    public synchronized void unbindService() {
        if (userServiceInstance != null) {
            try {
                Shizuku.unbindUserService(serviceArgs, serviceConnection, true);
                Log.d(TAG, "Shizuku UserService unbound.");
            } catch (Exception e) {
                Log.e(TAG, "Error unbinding Shizuku UserService", e);
            } finally {
                userServiceInstance = null;
                isBinding = false;
            }
        }
    }

    /** Returns true if the UserService AIDL binder is currently connected and responsive. */
    public boolean isServiceAlive() {
        if (userServiceInstance == null) return false;
        try {
            // Lightweight liveness check — IUserService.getUid() is a no-op call
            userServiceInstance.getUid();
            return true;
        } catch (Exception e) {
            Log.w(TAG, "isServiceAlive: binder ping failed — service is dead: " + e.getMessage());
            userServiceInstance = null;
            return false;
        }
    }

    public boolean isConnected() {
        return userServiceInstance != null;
    }

    public String executeCommandDirectly(String command) {
        if (userServiceInstance == null) {
            return "ERROR: UserService not bound";
        }
        try {
            return userServiceInstance.execCommand(command);
        } catch (Exception e) {
            Log.e(TAG, "RemoteException calling IUserService.execCommand", e);
            userServiceInstance = null; // Reset dead binder reference
            return "ERROR: Shizuku AIDL service call failed: " + e.getMessage();
        }
    }

    public String executeCommand(String command) {
        if (userServiceInstance == null) {
            bindService();
            // Brief polling wait for binding to establish before falling back
            int retries = 3;
            while (userServiceInstance == null && retries > 0) {
                try {
                    Thread.sleep(40);
                } catch (InterruptedException ignored) {}
                retries--;
            }
        }

        if (userServiceInstance != null) {
            return executeCommandDirectly(command);
        }

        Log.w(TAG, "UserService not bound yet — executing system command.");
        return com.gamebooster.app.engine.ShellExecutor.executeCommand(command).stdout;
    }
}
