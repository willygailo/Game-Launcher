package com.gamespace.app.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import rikka.shizuku.Shizuku;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.util.Log;

public class ShizukuExecutor {

    private static final String TAG = "ShizukuDiag";

    public static boolean isShizukuAvailable() {
        try {
            boolean ping = Shizuku.pingBinder();
            Log.d(TAG, "isShizukuAvailable pingBinder=" + ping);
            return ping;
        } catch (Throwable t) {
            Log.d(TAG, "isShizukuAvailable exception: " + t.getMessage());
            return false;
        }
    }

    public static boolean hasShizukuPermission() {
        if (!isShizukuAvailable()) {
            Log.d(TAG, "hasShizukuPermission: Shizuku NOT available");
            return false;
        }
        try {
            int check = Shizuku.checkSelfPermission();
            boolean granted = (check == PackageManager.PERMISSION_GRANTED);
            Log.d(TAG, "hasShizukuPermission: checkSelfPermission=" + check + " granted=" + granted);
            return granted;
        } catch (Throwable t) {
            Log.d(TAG, "hasShizukuPermission exception: " + t.getMessage());
            return false;
        }
    }

    public static String executeShizukuCommand(String command) {
        Log.d(TAG, "executeShizukuCommand input: " + command);
        if (!hasShizukuPermission()) {
            Log.d(TAG, "executeShizukuCommand: FAILED - Permission Denied");
            return "ERROR: Shizuku Permission Denied";
        }
        Process process = null;
        BufferedReader reader = null;
        try {
            java.lang.reflect.Method newProcessMethod = Shizuku.class.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
            newProcessMethod.setAccessible(true);
            process = (Process) newProcessMethod.invoke(null, new String[]{"sh", "-c", command}, null, null);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            String result = output.toString().trim();
            Log.d(TAG, "executeShizukuCommand output: exitCode=" + exitCode + " result='" + result + "'");
            return result;
        } catch (Exception e) {
            Log.e(TAG, "executeShizukuCommand exception: " + e.getClass().getName() + " message=" + e.getMessage(), e);
            return "ERROR: " + (e.getMessage() != null ? e.getMessage() : "Shizuku execution failed");
        } finally {
            try {
                if (reader != null) reader.close();
                if (process != null) process.destroy();
            } catch (Exception ignored) {}
        }
    }

    public static void grantAppPermissionsViaShizuku(Context context) {
        if (context == null || !hasShizukuPermission()) return;
        String packageName = context.getPackageName();

        executeShizukuCommand("pm grant " + packageName + " android.permission.WRITE_SECURE_SETTINGS");
        executeShizukuCommand("pm grant " + packageName + " android.permission.WRITE_SETTINGS");
        executeShizukuCommand("pm grant " + packageName + " android.permission.PACKAGE_USAGE_STATS");
    }
}
