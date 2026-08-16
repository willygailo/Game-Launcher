package com.gamebooster.app.shizuku;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import rikka.shizuku.Shizuku;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.util.Log;

public class ShizukuExecutor {

    private static final String TAG = "ShizukuDiag";
    public static final int REQUEST_CODE_SHIZUKU = 1001;

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
            return false;
        }
        try {
            int check = Shizuku.checkSelfPermission();
            return (check == PackageManager.PERMISSION_GRANTED);
        } catch (Throwable t) {
            Log.d(TAG, "hasShizukuPermission exception: " + t.getMessage());
            return false;
        }
    }

    public static void requestPermission(int requestCode) {
        if (isShizukuAvailable() && !hasShizukuPermission()) {
            try {
                Shizuku.requestPermission(requestCode);
            } catch (Throwable t) {
                Log.e(TAG, "requestPermission exception: " + t.getMessage());
            }
        }
    }

    public static void requestPermission() {
        requestPermission(REQUEST_CODE_SHIZUKU);
    }

    public static String executeShizukuCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return "SUCCESS";
        }

        // Tier 1: If Shizuku is granted, execute via Shizuku reflection
        if (hasShizukuPermission()) {
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

                if (exitCode == 0) {
                    return stdoutStr.isEmpty() ? "SUCCESS" : stdoutStr;
                } else if (!stderrStr.isEmpty()) {
                    return "ERROR: " + stderrStr;
                }
            } catch (Throwable e) {
                Log.w(TAG, "Shizuku newProcess fallback to rish/UserService: " + e.getMessage());
                try {
                    String rishOut = RishManager.executeRishCommand(null, command);
                    if (rishOut != null && !rishOut.startsWith("ERROR: rish binary not available")) {
                        return rishOut;
                    }
                } catch (Throwable ignored) {}

                try {
                    return ShizukuUserServiceConnector.getInstance().executeCommand(command);
                } catch (Throwable t) {
                    Log.e(TAG, "Shizuku UserService failed: " + t.getMessage());
                }
            } finally {
                try {
                    if (stdoutReader != null) stdoutReader.close();
                    if (stderrReader != null) stderrReader.close();
                    if (process != null) process.destroy();
                } catch (Throwable ignored) {}
            }
        }

        // Tier 2: Try rish directly if binder is in background
        try {
            String rishOut = RishManager.executeRishCommand(null, command);
            if (rishOut != null && !rishOut.startsWith("ERROR")) {
                return rishOut;
            }
        } catch (Throwable ignored) {}

        // Tier 3: Elevated Root / Direct Command fallback
        return com.gamebooster.app.engine.CommandExecutor.executeSystemCommand(command);
    }

    public static void grantAppPermissionsViaShizuku(Context context) {
        if (context == null) return;
        String packageName = context.getPackageName();

        // 1. Core Dangerous & Protected System Permissions
        String[] permissions = new String[]{
                "android.permission.WRITE_SECURE_SETTINGS",
                "android.permission.WRITE_SETTINGS",
                "android.permission.PACKAGE_USAGE_STATS",
                "android.permission.MANAGE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.ACCESS_NOTIFICATION_POLICY",
                "android.permission.POST_NOTIFICATIONS",
                "android.permission.SCHEDULE_EXACT_ALARM",
                "android.permission.USE_EXACT_ALARM",
                "android.permission.DUMP",
                "android.permission.BATTERY_STATS",
                "android.permission.MANAGE_GAME_MODE",
                "android.permission.OVERRIDE_WIFI_CONFIG",
                "android.permission.CHANGE_COMPONENT_ENABLED_STATE",
                "android.permission.CHANGE_NETWORK_STATE",
                "android.permission.FORCE_STOP_PACKAGES",
                "android.permission.CLEAR_APP_CACHE",
                "android.permission.REAL_GET_TASKS",
                "android.permission.SET_PROCESS_LIMIT",
                "android.permission.READ_MEDIA_IMAGES",
                "android.permission.READ_MEDIA_VIDEO",
                "android.permission.READ_MEDIA_AUDIO"
        };

        for (String perm : permissions) {
            executeShizukuCommand("pm grant " + packageName + " " + perm);
        }

        // 2. Complete AppOps Overrides for Unrestricted Access & Scoped Storage Bypass
        String[] appOps = new String[]{
                "MANAGE_EXTERNAL_STORAGE",
                "READ_EXTERNAL_STORAGE",
                "WRITE_EXTERNAL_STORAGE",
                "NO_ISOLATED_STORAGE",
                "LEGACY_STORAGE",
                "SYSTEM_ALERT_WINDOW",
                "GET_USAGE_STATS",
                "WRITE_SETTINGS",
                "MANAGE_GAME_MODE",
                "RUN_IN_BACKGROUND",
                "RUN_ANY_IN_BACKGROUND",
                "AUTO_START",
                "TURN_SCREEN_ON",
                "PROJECT_MEDIA",
                "ACCESS_RESTRICTED_SETTINGS"
        };

        for (String op : appOps) {
            executeShizukuCommand("cmd appops set " + packageName + " " + op + " allow");
        }

        // 3. Whitelist from Battery Optimization / Doze mode
        executeShizukuCommand("dumpsys deviceidle whitelist +" + packageName);
        executeShizukuCommand("cmd deviceidle whitelist +" + packageName);

        // 4. Grant Target Games Permission & AppOps Overrides
        for (String gamePkg : com.gamebooster.app.games.GamePackageRegistry.getAllKnownGames().keySet()) {
            executeShizukuCommand("cmd game mode performance " + gamePkg);
            executeShizukuCommand("cmd game set --fps 165 " + gamePkg);
            executeShizukuCommand("cmd window set-app-refresh-rate " + gamePkg + " 165");
            executeShizukuCommand("device_config put game_overlay " + gamePkg + " mode=2,fps=165:mode=3,fps=165");
            executeShizukuCommand("cmd appops set " + gamePkg + " RUN_IN_BACKGROUND allow");
            executeShizukuCommand("cmd appops set " + gamePkg + " RUN_ANY_IN_BACKGROUND allow");
            executeShizukuCommand("cmd appops set " + gamePkg + " AUTO_START allow");
            executeShizukuCommand("cmd appops set " + gamePkg + " SYSTEM_ALERT_WINDOW allow");
            executeShizukuCommand("cmd appops set " + gamePkg + " MANAGE_EXTERNAL_STORAGE allow");
            executeShizukuCommand("cmd appops set " + gamePkg + " NO_ISOLATED_STORAGE allow");
            executeShizukuCommand("pm grant " + gamePkg + " android.permission.WRITE_SETTINGS");
            executeShizukuCommand("pm grant " + gamePkg + " android.permission.MANAGE_EXTERNAL_STORAGE");
        }
    }

    public static String injectTouchTap(int x, int y) {
        return executeShizukuCommand("input tap " + x + " " + y);
    }

    public static String injectTouchSwipe(int startX, int startY, int endX, int endY, int durationMs) {
        return executeShizukuCommand("input swipe " + startX + " " + startY + " " + endX + " " + endY + " " + durationMs);
    }
}

