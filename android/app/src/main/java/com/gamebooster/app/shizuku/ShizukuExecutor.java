package com.gamebooster.app.shizuku;

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
        BufferedReader stdoutReader = null;
        BufferedReader stderrReader = null;
        try {
            java.lang.reflect.Method newProcessMethod = Shizuku.class.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
            newProcessMethod.setAccessible(true);
            process = (Process) newProcessMethod.invoke(null, new String[]{"sh", "-c", command}, null, null);

            stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder stdout = new StringBuilder();
            String line;
            while ((line = stdoutReader.readLine()) != null) {
                stdout.append(line).append("\n");
            }

            stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder stderr = new StringBuilder();
            while ((line = stderrReader.readLine()) != null) {
                stderr.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            String stdoutStr = stdout.toString().trim();
            String stderrStr = stderr.toString().trim();
            Log.d(TAG, "executeShizukuCommand exitCode=" + exitCode + " stdout='" + stdoutStr + "' stderr='" + stderrStr + "'");

            if (exitCode == 0) {
                return stdoutStr.isEmpty() ? "SUCCESS" : stdoutStr;
            } else {
                return "ERROR: Shizuku command failed with exit code " + exitCode + (stderrStr.isEmpty() ? "" : ": " + stderrStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "executeShizukuCommand exception: " + e.getClass().getName() + " message=" + e.getMessage(), e);
            return "ERROR: " + (e.getMessage() != null ? e.getMessage() : "Shizuku execution failed");
        } finally {
            try {
                if (stdoutReader != null) stdoutReader.close();
                if (stderrReader != null) stderrReader.close();
                if (process != null) process.destroy();
            } catch (Exception ignored) {}
        }
    }

    public static void grantAppPermissionsViaShizuku(Context context) {
        if (context == null || !hasShizukuPermission()) return;
        String packageName = context.getPackageName();

        // Perform Shizuku ADB Grant Combo
        executeShizukuCommand("pm grant " + packageName + " android.permission.WRITE_SECURE_SETTINGS");
        executeShizukuCommand("pm grant " + packageName + " android.permission.WRITE_SETTINGS");
        executeShizukuCommand("pm grant " + packageName + " android.permission.PACKAGE_USAGE_STATS");
        executeShizukuCommand("pm grant " + packageName + " android.permission.MANAGE_EXTERNAL_STORAGE");
        executeShizukuCommand("pm grant " + packageName + " android.permission.READ_EXTERNAL_STORAGE");
        executeShizukuCommand("pm grant " + packageName + " android.permission.WRITE_EXTERNAL_STORAGE");
        executeShizukuCommand("pm grant " + packageName + " android.permission.ACCESS_NOTIFICATION_POLICY");
        executeShizukuCommand("pm grant " + packageName + " android.permission.DUMP");
        executeShizukuCommand("pm grant " + packageName + " android.permission.BATTERY_STATS");
        executeShizukuCommand("pm grant " + packageName + " android.permission.MANAGE_GAME_MODE");
        executeShizukuCommand("pm grant " + packageName + " android.permission.OVERRIDE_WIFI_CONFIG");
        executeShizukuCommand("pm grant " + packageName + " android.permission.CHANGE_COMPONENT_ENABLED_STATE");
        executeShizukuCommand("pm grant " + packageName + " android.permission.CHANGE_NETWORK_STATE");
        executeShizukuCommand("pm grant " + packageName + " android.permission.FORCE_STOP_PACKAGES");
        executeShizukuCommand("pm grant " + packageName + " android.permission.CLEAR_APP_CACHE");
        executeShizukuCommand("pm grant " + packageName + " android.permission.REAL_GET_TASKS");
        executeShizukuCommand("pm grant " + packageName + " android.permission.SET_PROCESS_LIMIT");
        executeShizukuCommand("pm grant " + packageName + " android.permission.MODIFY_PHONE_STATE");
        executeShizukuCommand("pm grant " + packageName + " android.permission.READ_PRIVILEGED_PHONE_STATE");
        executeShizukuCommand("pm grant " + packageName + " android.permission.HARDWARE_TEST");
        executeShizukuCommand("pm grant " + packageName + " android.permission.INTERNET");

        // AppOps Overrides for Unrestricted System Access
        executeShizukuCommand("cmd appops set " + packageName + " MANAGE_EXTERNAL_STORAGE allow");
        executeShizukuCommand("cmd appops set " + packageName + " SYSTEM_ALERT_WINDOW allow");
        executeShizukuCommand("cmd appops set " + packageName + " GET_USAGE_STATS allow");
        executeShizukuCommand("cmd appops set " + packageName + " WRITE_SETTINGS allow");
        executeShizukuCommand("cmd appops set " + packageName + " MANAGE_GAME_MODE allow");
        executeShizukuCommand("cmd appops set " + packageName + " RUN_IN_BACKGROUND allow");
        executeShizukuCommand("cmd appops set " + packageName + " RUN_ANY_IN_BACKGROUND allow");
        executeShizukuCommand("cmd appops set " + packageName + " AUTO_START allow");
        executeShizukuCommand("cmd appops set " + packageName + " TURN_SCREEN_ON allow");
        executeShizukuCommand("cmd appops set " + packageName + " PROJECT_MEDIA allow");
        executeShizukuCommand("cmd appops set " + packageName + " ACCESS_RESTRICTED_SETTINGS allow");
    }
}
