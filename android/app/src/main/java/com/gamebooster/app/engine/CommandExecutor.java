package com.gamebooster.app.engine;

import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

public class CommandExecutor {

    public static EngineMode getActiveEngineMode() {
        if (ShizukuExecutor.hasShizukuPermission()) {
            return EngineMode.SHIZUKU;
        } else if (ShellExecutor.isRootAvailable()) {
            return EngineMode.ROOT;
        } else {
            return EngineMode.SYSTEM_SETTINGS;
        }
    }

    public static String executeSystemCommand(String command) {
        EngineMode mode = getActiveEngineMode();

        switch (mode) {
            case SHIZUKU:
                return ShizukuUserServiceConnector.getInstance().executeCommand(command);
            case ROOT:
                ShellExecutor.CommandResult rootRes = ShellExecutor.executeRootCommand(command);
                if (!rootRes.isSuccess()) {
                    return "ERROR: " + (rootRes.stderr.isEmpty() ? "Root command failed with code " + rootRes.exitCode : rootRes.stderr);
                }
                return rootRes.stdout.isEmpty() ? "SUCCESS" : rootRes.stdout;
            case SYSTEM_SETTINGS:
            case READ_ONLY:
            default:
                // System-changing commands require the explicit Shizuku or root backend.
                // Executing them through a regular app shell is both misleading and unreliable.
                return "ERROR: No privileged backend is connected";
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
