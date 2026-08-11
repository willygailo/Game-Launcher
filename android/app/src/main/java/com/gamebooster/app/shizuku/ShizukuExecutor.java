package com.gamebooster.app.shizuku;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.engine.PermissionBatchBuilder;
import com.gamebooster.app.games.TargetGameRegistry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
            Log.d(TAG, "Shizuku permission not active — executing command via local process fallback: " + cleanCmd);
            com.gamebooster.app.engine.ShellExecutor.CommandResult res =
                    com.gamebooster.app.engine.ShellExecutor.executeCommand(cleanCmd);
            if (res.isSuccess()) {
                return res.stdout.isEmpty() ? "SUCCESS" : res.stdout;
            } else {
                return "ERROR: Shizuku permission not active & local execution failed (code " + res.exitCode + "): " + res.stderr;
            }
        }

        // 1. Primary AIDL IPC path via Shizuku UserService
        try {
            ShizukuUserServiceConnector connector = ShizukuUserServiceConnector.getInstance();
            connector.bindService();
            String aidlResult = connector.executeCommandDirectly(cleanCmd);
            if (aidlResult != null && !aidlResult.startsWith("ERROR: UserService not bound")) {
                return aidlResult;
            }
        } catch (Throwable t) {
            Log.w(TAG, "Shizuku UserService AIDL call pending, trying fallback: " + t.getMessage());
        }

        // 2. Fallback reflection path
        Process process = null;
        BufferedReader stdoutReader = null;
        BufferedReader stderrReader = null;
        try {
            Method newProcessMethod = Shizuku.class.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
            newProcessMethod.setAccessible(true);
            process = (Process) newProcessMethod.invoke(null, new String[]{"sh", "-c", cleanCmd}, null, null);

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

        String packageName = context.getPackageName();

        // Delegate to PermissionBatchBuilder — single source of truth (no duplicates)
        List<String> batch = new ArrayList<>(PermissionBatchBuilder.buildGrantBatch(packageName));

        // Per-game Game Mode + FPS lock for all target games
        int maxHz = 165;
        try {
            DevicePerformanceCapabilities caps = DevicePerformanceCapabilities.detect(context);
            if (caps != null && caps.getMaxRefreshRate() > 0) {
                maxHz = caps.getMaxRefreshRate();
            }
        } catch (Throwable ignored) {}

        List<String> targetGames = TargetGameRegistry.getAllPackages();
        for (String gamePkg : targetGames) {
            batch.addAll(PermissionBatchBuilder.buildPerGameBatch(gamePkg, maxHz));
        }

        String res = executeShizukuBatchCommands(batch);
        boolean success = res != null && !res.startsWith("ERROR");
        return new GrantResult(success, batch.size(), success ? batch.size() : 0);
    }
}
