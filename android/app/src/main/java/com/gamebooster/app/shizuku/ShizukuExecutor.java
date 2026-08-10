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

    public static String executeShizukuBatchCommands(java.util.List<String> commands) {
        if (commands == null || commands.isEmpty()) return "SUCCESS";
        StringBuilder sb = new StringBuilder();
        for (String cmd : commands) {
            if (cmd != null && !cmd.trim().isEmpty()) {
                sb.append(cmd.trim()).append("; ");
            }
        }
        if (sb.length() == 0) return "SUCCESS";
        return executeShizukuCommand(sb.toString());
    }

    public static void grantAppPermissionsViaShizuku(Context context) {
        if (context == null || !hasShizukuPermission()) return;
        String packageName = context.getPackageName();
        java.util.List<String> batch = new java.util.ArrayList<>();

        // Perform Shizuku ADB Grant Combo
        batch.add("pm grant " + packageName + " android.permission.WRITE_SECURE_SETTINGS");
        batch.add("pm grant " + packageName + " android.permission.WRITE_SETTINGS");
        batch.add("pm grant " + packageName + " android.permission.PACKAGE_USAGE_STATS");
        batch.add("pm grant " + packageName + " android.permission.MANAGE_EXTERNAL_STORAGE");
        batch.add("pm grant " + packageName + " android.permission.READ_EXTERNAL_STORAGE");
        batch.add("pm grant " + packageName + " android.permission.WRITE_EXTERNAL_STORAGE");
        batch.add("pm grant " + packageName + " android.permission.ACCESS_NOTIFICATION_POLICY");
        batch.add("pm grant " + packageName + " android.permission.DUMP");
        batch.add("pm grant " + packageName + " android.permission.BATTERY_STATS");
        batch.add("pm grant " + packageName + " android.permission.MANAGE_GAME_MODE");
        batch.add("pm grant " + packageName + " android.permission.OVERRIDE_WIFI_CONFIG");
        batch.add("pm grant " + packageName + " android.permission.CHANGE_COMPONENT_ENABLED_STATE");
        batch.add("pm grant " + packageName + " android.permission.CHANGE_NETWORK_STATE");
        batch.add("pm grant " + packageName + " android.permission.FORCE_STOP_PACKAGES");
        batch.add("pm grant " + packageName + " android.permission.CLEAR_APP_CACHE");
        batch.add("pm grant " + packageName + " android.permission.REAL_GET_TASKS");
        batch.add("pm grant " + packageName + " android.permission.SET_PROCESS_LIMIT");
        batch.add("pm grant " + packageName + " android.permission.MODIFY_PHONE_STATE");
        batch.add("pm grant " + packageName + " android.permission.READ_PRIVILEGED_PHONE_STATE");
        batch.add("pm grant " + packageName + " android.permission.HARDWARE_TEST");
        batch.add("pm grant " + packageName + " android.permission.INTERNET");
        batch.add("pm grant " + packageName + " android.permission.SYSTEM_ALERT_WINDOW");

        // AppOps Overrides for Unrestricted System Access
        batch.add("cmd appops set " + packageName + " MANAGE_EXTERNAL_STORAGE allow");
        batch.add("cmd appops set " + packageName + " SYSTEM_ALERT_WINDOW allow");
        batch.add("cmd appops set " + packageName + " GET_USAGE_STATS allow");
        batch.add("cmd appops set " + packageName + " WRITE_SETTINGS allow");
        batch.add("cmd appops set " + packageName + " MANAGE_GAME_MODE allow");
        batch.add("cmd appops set " + packageName + " RUN_IN_BACKGROUND allow");
        batch.add("cmd appops set " + packageName + " RUN_ANY_IN_BACKGROUND allow");
        batch.add("cmd appops set " + packageName + " AUTO_START allow");
        batch.add("cmd appops set " + packageName + " TURN_SCREEN_ON allow");
        batch.add("cmd appops set " + packageName + " PROJECT_MEDIA allow");
        batch.add("cmd appops set " + packageName + " ACCESS_RESTRICTED_SETTINGS allow");
        batch.add("cmd appops set " + packageName + " NO_ISOLATED_STORAGE allow");

        // Force Target Games Permission & AppOps Overrides
        String[] targetGames = new String[] {
                "com.mobile.legends", "com.mobilelegends.win",
                "com.tencent.ig", "com.pubg.krmobile", "com.vng.pubgmobile", "com.pubg.imobile", "com.pubg.newstate",
                "com.activision.callofduty.shooter", "com.garena.game.codm",
                "com.dts.freefireth", "com.dts.freefiremax",
                "com.levelinfinite.sgameGlobal", "com.tencent.tmgp.sgame", "com.garena.game.kgtw",
                "com.riotgames.league.wildrift", "com.miHoYo.GenshinImpact", "com.HoYoverse.hkrpg",
                "com.HoYoverse.Nap", "com.kurogame.wutheringwaves.global", "com.ofg.bloodstrike"
        };

        for (String gamePkg : targetGames) {
            batch.add("cmd game mode performance " + gamePkg);
            batch.add("cmd game set --fps 165 " + gamePkg);
            batch.add("cmd window set-app-refresh-rate " + gamePkg + " 165");
            batch.add("device_config put game_overlay " + gamePkg + " mode=2,fps=165:mode=3,fps=165");
            batch.add("cmd appops set " + gamePkg + " RUN_IN_BACKGROUND allow");
            batch.add("cmd appops set " + gamePkg + " RUN_ANY_IN_BACKGROUND allow");
            batch.add("cmd appops set " + gamePkg + " AUTO_START allow");
            batch.add("cmd appops set " + gamePkg + " SYSTEM_ALERT_WINDOW allow");
            batch.add("pm grant " + gamePkg + " android.permission.WRITE_SETTINGS");
            batch.add("pm grant " + gamePkg + " android.permission.MANAGE_EXTERNAL_STORAGE");
        }

        executeShizukuBatchCommands(batch);
    }
}

