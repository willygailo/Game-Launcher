package com.gamebooster.app.root;

import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

public class CommandExecutor {

    public static EngineMode getActiveEngineMode() {
        if (ShizukuExecutor.hasShizukuPermission()) {
            return EngineMode.SHIZUKU;
        } else {
            return EngineMode.SYSTEM_SETTINGS;
        }
    }

    public static String executeSystemCommand(String command) {
        EngineMode mode = getActiveEngineMode();

        switch (mode) {
            case SHIZUKU:
                return ShizukuUserServiceConnector.getInstance().executeCommand(command);
            case SYSTEM_SETTINGS:
            case READ_ONLY:
            default:
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
    }

    public static boolean setSystemProperty(String key, String value) {
        String cmd = "setprop " + key + " " + value;
        String result = executeSystemCommand(cmd);
        return isSuccessOutput(result);
    }

    public static boolean setSystemSetting(String namespace, String key, String value) {
        String cmd = "settings put " + namespace + " " + key + " " + value;
        String result = executeSystemCommand(cmd);
        return isSuccessOutput(result);
    }

    public static boolean isSuccessOutput(String result) {
        if (result == null || result.trim().isEmpty()) {
            return false;
        }
        String lower = result.toLowerCase();
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
