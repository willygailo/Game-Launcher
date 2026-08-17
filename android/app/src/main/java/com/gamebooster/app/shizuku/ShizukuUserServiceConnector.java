package com.gamebooster.app.shizuku;

import android.content.ComponentName;
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
            .version(2);

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

    public boolean isServiceConnected() {
        return userServiceInstance != null;
    }

    public String executeCommand(String command) {
        ensureConnected();
        if (userServiceInstance == null) {
            return ShizukuExecutor.executeShizukuCommand(command);
        }
        try {
            return userServiceInstance.execCommand(command);
        } catch (Exception e) {
            Log.e(TAG, "RemoteException calling IUserService.execCommand", e);
            userServiceInstance = null;
            return ShizukuExecutor.executeShizukuCommand(command);
        }
    }

    public boolean writeFile(String path, String content, String mode) {
        ensureConnected();
        if (userServiceInstance == null) {
            ShizukuFileManager.FileOpResult res = ShizukuFileManager.writeFile(path, content, mode);
            return res != null && res.success;
        }
        try {
            return userServiceInstance.writeFile(path, content, mode);
        } catch (Exception e) {
            Log.e(TAG, "RemoteException calling IUserService.writeFile", e);
            userServiceInstance = null;
            ShizukuFileManager.FileOpResult res = ShizukuFileManager.writeFile(path, content, mode);
            return res != null && res.success;
        }
    }

    public String readFile(String path) {
        ensureConnected();
        if (userServiceInstance == null) {
            return ShizukuFileManager.readFile(path);
        }
        try {
            return userServiceInstance.readFile(path);
        } catch (Exception e) {
            Log.e(TAG, "RemoteException calling IUserService.readFile", e);
            userServiceInstance = null;
            return ShizukuFileManager.readFile(path);
        }
    }

    public boolean deletePath(String path) {
        ensureConnected();
        if (userServiceInstance == null) {
            return ShizukuFileManager.deletePath(path);
        }
        try {
            return userServiceInstance.deletePath(path);
        } catch (Exception e) {
            Log.e(TAG, "RemoteException calling IUserService.deletePath", e);
            userServiceInstance = null;
            return ShizukuFileManager.deletePath(path);
        }
    }

    private void ensureConnected() {
        if (userServiceInstance == null && !isBinding) {
            bindService();
            int retries = 2;
            while (userServiceInstance == null && retries > 0) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {}
                retries--;
            }
        }
    }
}
