package com.gamespace.app.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import dev.rikka.shizuku.Shizuku;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ShizukuExecutor {

    public static boolean isShizukuAvailable() {
        try {
            return Shizuku.pingBinder();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasShizukuPermission() {
        if (!isShizukuAvailable()) return false;
        try {
            return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable t) {
            return false;
        }
    }

    public static String executeShizukuCommand(String command) {
        if (!hasShizukuPermission()) {
            return "ERROR: Shizuku Permission Denied";
        }
        Process process = null;
        BufferedReader reader = null;
        try {
            process = Shizuku.newProcess(new String[]{"sh", "-c", command}, null, null);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
            return output.toString().trim();
        } catch (Exception e) {
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
