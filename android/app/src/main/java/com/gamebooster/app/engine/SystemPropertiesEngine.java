package com.gamebooster.app.engine;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * SystemPropertiesEngine — Centralized AOSP API helper to modify
 * System, Global, Secure settings, DeviceConfig, and setprop variables
 * using Shizuku ADB / Root privileges.
 */
public class SystemPropertiesEngine {

    private static final String TAG = "SystemPropertiesEngine";

    public static boolean putSystemSetting(String key, String value) {
        if (key == null || value == null) return false;
        String cmd = "settings put system " + key + " " + value;
        return execute(cmd);
    }

    public static boolean putGlobalSetting(String key, String value) {
        if (key == null || value == null) return false;
        String cmd = "settings put global " + key + " " + value;
        return execute(cmd);
    }

    public static boolean putSecureSetting(String key, String value) {
        if (key == null || value == null) return false;
        String cmd = "settings put secure " + key + " " + value;
        return execute(cmd);
    }

    public static boolean putDeviceConfig(String namespace, String key, String value) {
        if (namespace == null || key == null || value == null) return false;
        String cmd = "device_config put " + namespace + " " + key + " " + value;
        return execute(cmd);
    }

    public static boolean setSystemProperty(String key, String value) {
        if (key == null || value == null) return false;
        String cmd = "setprop " + key + " " + value;
        return execute(cmd);
    }

    public static boolean setGameMode(String packageName, String mode) {
        if (packageName == null || mode == null) return false;
        String cmd = "cmd game mode " + mode + " " + packageName;
        return execute(cmd);
    }

    public static boolean setAppFpsHz(String packageName, int targetHz) {
        if (packageName == null) return false;
        String cmd = String.format("cmd game set --fps %d %s && cmd window set-app-refresh-rate %s %d && device_config put game_overlay %s mode=2,fps=%d:mode=3,fps=%d",
                targetHz, packageName, packageName, targetHz, packageName, targetHz, targetHz);
        return execute(cmd);
    }

    private static boolean execute(String command) {
        try {
            String res;
            if (ShizukuExecutor.hasShizukuPermission()) {
                res = ShizukuExecutor.executeShizukuCommand(command);
            } else {
                res = CommandExecutor.executeSystemCommand(command);
            }
            boolean success = CommandExecutor.isSuccessOutput(res);
            Log.d(TAG, "Executing: [" + command + "] -> " + (success ? "OK" : "FAILED (" + res + ")"));
            return success;
        } catch (Throwable t) {
            Log.e(TAG, "Error executing command: " + command, t);
            return false;
        }
    }
}
