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

        // Phase 1.1: wait briefly for the AIDL user service when permission is
        // granted but the service isn't connected yet (no cost in degraded mode, never block main thread)
        if (hasShizukuPermission() && !ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
            if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
                ShizukuConnectionManager.getInstance().ensureReady(150);
            }
        }

        // Tier 1: If Shizuku is granted, try fast direct AIDL UserService first
        if (hasShizukuPermission()) {
            if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                String aidlRes = ShizukuUserServiceConnector.getInstance().executeCommandDirect(command);
                if (aidlRes != null) {
                    return aidlRes;
                }
            }

            Process process = null;
            try {
                java.lang.reflect.Method newProcessMethod = Shizuku.class.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
                newProcessMethod.setAccessible(true);
                process = (Process) newProcessMethod.invoke(null, new String[]{"sh", "-c", command}, null, null);

                if (process != null) {
                    return readProcessOutput(process, 4000L);
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
                    // Direct AIDL only — never call executeCommand() here (mutual recursion)
                    String directRes = ShizukuUserServiceConnector.getInstance().executeCommandDirect(command);
                    if (directRes != null) {
                        return directRes;
                    }
                } catch (Throwable t) {
                    Log.e(TAG, "Shizuku UserService failed: " + t.getMessage());
                }
            } finally {
                if (process != null) {
                    try {
                        process.destroy();
                    } catch (Throwable ignored) {}
                }
            }
        }

        // Tier 2: Try rish directly if binder is in background
        try {
            String rishOut = RishManager.executeRishCommand(null, command);
            if (rishOut != null && !rishOut.startsWith("ERROR")) {
                return rishOut;
            }
        } catch (Throwable ignored) {}

        // Tier 3: Local Shell unprivileged fallback
        com.gamebooster.app.engine.ShellExecutor.CommandResult shellRes = com.gamebooster.app.engine.ShellExecutor.executeCommand(command, false);
        if (shellRes.isSuccess()) {
            return shellRes.stdout.isEmpty() ? "SUCCESS" : shellRes.stdout;
        }
        return "ERROR: " + (shellRes.stderr.isEmpty() ? "Command failed with code " + shellRes.exitCode : shellRes.stderr);
    }

    public static void grantAppPermissionsViaShizuku(Context context) {
        if (context == null) return;
        ShizukuPermissionEnforcer.enforceAllPermissions(context);
    }

    /**
     * Executes multiple shell commands sequentially in a single batch.
     */
    public static void executeShizukuCommands(String... commands) {
        if (commands == null || commands.length == 0) return;
        StringBuilder sb = new StringBuilder();
        for (String cmd : commands) {
            if (cmd != null && !cmd.trim().isEmpty()) {
                sb.append(cmd.trim()).append("; ");
            }
        }
        if (sb.length() > 0) {
            executeShizukuCommand(sb.toString());
        }
    }

    /**
     * Executes a list of shell commands in a single batch.
     */
    public static void executeShizukuCommands(java.util.List<String> commands) {
        if (commands == null || commands.isEmpty()) return;
        if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
            ShizukuUserServiceConnector.getInstance().executeBatchCommands(commands);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String cmd : commands) {
            if (cmd != null && !cmd.trim().isEmpty()) {
                sb.append(cmd.trim()).append("; ");
            }
        }
        if (sb.length() > 0) {
            executeShizukuCommand(sb.toString());
        }
    }

    /**
     * Executes a list of shell commands and returns the per-command results.
     *
     * Unlike the void {@link #executeShizukuCommands(List)} fire-and-forget
     * variant, this lets callers distinguish "commands really ran" from
     * "no elevated channel existed". Returns one entry per non-empty command;
     * an empty list means no command was executed at all (no Shizuku, no rish,
     * no shell fallback), and each entry is the raw stdout/SUCCESS/ERROR string.
     */
    public static java.util.List<String> executeShizukuCommandsWithResults(java.util.List<String> commands) {
        java.util.List<String> results = new java.util.ArrayList<>();
        if (commands == null || commands.isEmpty()) return results;

        if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
            return ShizukuUserServiceConnector.getInstance().executeBatchCommands(commands);
        }

        StringBuilder sb = new StringBuilder();
        for (String cmd : commands) {
            if (cmd != null && !cmd.trim().isEmpty()) {
                sb.append(cmd.trim()).append("; ");
            }
        }
        if (sb.length() > 0) {
            String res = executeShizukuCommand(sb.toString());
            results.add(res != null ? res : "ERROR: no output");
        }
        return results;
    }

    /**
     * Executes an entire multiline shell script via base64 pipeline.
     */
    public static String executeShizukuScript(String scriptContent) {
        if (scriptContent == null || scriptContent.trim().isEmpty()) return "SUCCESS";
        String b64 = android.util.Base64.encodeToString(scriptContent.getBytes(java.nio.charset.StandardCharsets.UTF_8), android.util.Base64.NO_WRAP);
        String cmd = "echo '" + b64 + "' | base64 -d | sh";
        return executeShizukuCommand(cmd);
    }

    public static String injectTouchTap(int x, int y) {
        return executeShizukuCommand("input tap " + x + " " + y);
    }

    public static String injectTouchSwipe(int startX, int startY, int endX, int endY, int durationMs) {
        return executeShizukuCommand("input swipe " + startX + " " + startY + " " + endX + " " + endY + " " + durationMs);
    }

    public static String readProcessOutput(Process process, long timeoutMs) {
        if (process == null) return "ERROR: Process is null";
        final StringBuilder stdout = new StringBuilder();
        final StringBuilder stderr = new StringBuilder();

        Thread outThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdout.append(line).append("\n");
                }
            } catch (Throwable ignored) {}
        }, "Shizuku-StdoutReader");

        Thread errThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stderr.append(line).append("\n");
                }
            } catch (Throwable ignored) {}
        }, "Shizuku-StderrReader");

        outThread.setDaemon(true);
        errThread.setDaemon(true);
        outThread.start();
        errThread.start();

        long deadline = System.currentTimeMillis() + timeoutMs;
        boolean completed = false;
        while (System.currentTimeMillis() < deadline) {
            try {
                process.exitValue();
                completed = true;
                break;
            } catch (IllegalThreadStateException e) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException ignored) {}
            }
        }

        if (!completed) {
            try {
                process.destroy();
            } catch (Throwable ignored) {}
            return "ERROR: Command timed out after " + timeoutMs + "ms";
        }

        try {
            outThread.join(250);
            errThread.join(250);
        } catch (InterruptedException ignored) {}

        String stdoutStr = stdout.toString().trim();
        String stderrStr = stderr.toString().trim();
        int exitCode = process.exitValue();

        if (exitCode == 0) {
            return stdoutStr.isEmpty() ? "SUCCESS" : stdoutStr;
        } else if (!stderrStr.isEmpty()) {
            return "ERROR: " + stderrStr;
        } else {
            return stdoutStr.isEmpty() ? "ERROR: Exit code " + exitCode : stdoutStr;
        }
    }
}

