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
            return;
        }
        try {
            if (ShizukuManager.isShizukuInstalled(null) || Shizuku.pingBinder()) {
                Log.d(TAG, "Binding Shizuku UserService via AIDL...");
                isBinding = true;
                Shizuku.bindUserService(serviceArgs, serviceConnection);
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
