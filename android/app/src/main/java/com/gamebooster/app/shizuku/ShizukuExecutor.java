package com.gamebooster.app.shizuku;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.List;

import rikka.shizuku.Shizuku;

public class ShizukuExecutor {

    private static final String TAG = "ShizukuDiag";

    public static class GrantResult {
        public final boolean success;
        public final int totalCommands;
        public final int executedCommands;

        public GrantResult(boolean success, int totalCommands, int executedCommands) {
            this.success = success;
            this.totalCommands = totalCommands;
            this.executedCommands = executedCommands;
        }
    }

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
        if (command == null || command.trim().isEmpty()) {
            return "ERROR: Empty command";
        }
        String cleanCmd = command.trim();
        if (cleanCmd.startsWith("adb shell ")) {
            cleanCmd = cleanCmd.substring("adb shell ".length()).trim();
        } else if (cleanCmd.equals("adb shell")) {
            return "SUCCESS";
        }

        if (!hasShizukuPermission()) {
            // A command requested through this class must never degrade into the app's normal
            // process. Doing so makes a Shizuku-labelled action look privileged even though it
            // was executed with ordinary application permissions.
            return "ERROR: Shizuku is unavailable or permission was not granted";
        }

        // Execute only through the UserService AIDL path. Shizuku's legacy
        // newProcess API is deprecated and reflective access made failures
        // difficult to distinguish from a working privileged backend.
        try {
            ShizukuUserServiceConnector connector = ShizukuUserServiceConnector.getInstance();
            return connector.executeCommand(cleanCmd);
        } catch (Throwable t) {
            Log.e(TAG, "Shizuku UserService execution failed", t);
            return "ERROR: Shizuku UserService execution failed: "
                    + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage());
        }
    }

    public static String executeShizukuBatchCommands(List<String> commands) {
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

    public static GrantResult grantAppPermissionsViaShizuku(Context context) {
        if (context == null || !hasShizukuPermission()) {
            return new GrantResult(false, 0, 0);
        }

        // Shizuku's user approval is the authorization boundary. Do not turn it into a
        // blanket pm-grant/appops batch for this launcher or other game packages.
        ShizukuUserServiceConnector connector = ShizukuUserServiceConnector.getInstance();
        connector.bindService();
        for (int retry = 0; retry < 25 && !connector.isServiceAlive(); retry++) {
            try {
                Thread.sleep(40L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new GrantResult(false, 1, 0);
            }
        }
        return new GrantResult(connector.isServiceAlive(), 1, connector.isServiceAlive() ? 1 : 0);
    }
}
