package com.gamebooster.app.engine;

import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

import java.util.Collections;
import java.util.List;

public class CommandExecutor {

    public static EngineMode getActiveEngineMode() {
        if (ShizukuExecutor.hasShizukuPermission()) {
            return EngineMode.SHIZUKU;
        } else {
            return EngineMode.SYSTEM_SETTINGS;
        }
    }

    public static String executeSystemCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return "";
        }

        // 1. Try Shizuku direct AIDL Connector first
        if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
            String res = ShizukuUserServiceConnector.getInstance().executeCommand(command);
            if (res != null) return res;
        }

        // 2. Try Shizuku reflection fallback
        if (ShizukuExecutor.hasShizukuPermission()) {
            String res = ShizukuExecutor.executeShizukuCommand(command);
            if (res != null) return res;
        }

        // 3. Fallback to Local Shell Execution
        ShellExecutor.CommandResult shellRes = ShellExecutor.executeCommand(command, false);
        if (!shellRes.isSuccess()) {
            return "ERROR: " + (shellRes.stderr.isEmpty() ? "Command failed with code " + shellRes.exitCode : shellRes.stderr);
        }
        if (!shellRes.stderr.isEmpty()) {
            return "ERROR: " + shellRes.stderr;
        }
        if (shellRes.stdout.isEmpty()) {
            return "SUCCESS";
        }
        return shellRes.stdout;
    }

    public static List<String> executeBatchCommands(List<String> commands) {
        if (commands == null || commands.isEmpty()) {
            return Collections.emptyList();
        }

        if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
            return ShizukuUserServiceConnector.getInstance().execBatchCommands(commands);
        } else if (ShizukuExecutor.hasShizukuPermission()) {
            String combined = String.join("; ", commands);
            ShizukuExecutor.executeShizukuCommands(combined);
            return Collections.singletonList("SUCCESS");
        }

        return Collections.emptyList();
    }

    public static boolean setSystemProperty(String key, String value) {
        String cmd = "setprop " + key + " " + value;
        String result = executeSystemCommand(cmd);
        return isSuccessOutput(result);
    }

    public static String getSystemProperty(String key) {
        String cmd = "getprop " + key;
        return executeSystemCommand(cmd);
    }

    public static boolean setSystemSetting(String namespace, String key, String value) {
        String cmd = "settings put " + namespace + " " + key + " " + value;
        String result = executeSystemCommand(cmd);
        return isSuccessOutput(result);
    }

    public static String getSystemSetting(String namespace, String key) {
        String cmd = "settings get " + namespace + " " + key;
        return executeSystemCommand(cmd);
    }

    public static boolean isSuccessOutput(String result) {
        if (result == null) {
            return false;
        }
        String trimmed = result.trim();
        if (trimmed.isEmpty() || trimmed.equalsIgnoreCase("SUCCESS")) {
            return true;
        }
        String lower = trimmed.toLowerCase();
        if (lower.startsWith("error") ||
            lower.contains("permission denial") ||
            lower.contains("securityexception") ||
            lower.contains("permission denied") ||
            lower.contains("operation not permitted") ||
            lower.contains("failed") ||
            lower.contains("not found")) {
            return false;
        }
        return true;
    }
}
