package com.gamespace.app.data;

import com.gamespace.app.core.EngineMode;
import com.gamespace.app.utils.ShellExecutor;
import com.gamespace.app.utils.ShizukuExecutor;

public class CommandExecutor {

    public static EngineMode getActiveEngineMode() {
        if (ShellExecutor.isRootAvailable()) {
            return EngineMode.ROOT;
        } else if (ShizukuExecutor.hasShizukuPermission()) {
            return EngineMode.SHIZUKU;
        } else {
            return EngineMode.READ_ONLY;
        }
    }

    public static String executeSystemCommand(String command) {
        EngineMode mode = getActiveEngineMode();

        switch (mode) {
            case SHIZUKU:
                return ShizukuExecutor.executeShizukuCommand(command);
            case ROOT:
                ShellExecutor.CommandResult rootRes = ShellExecutor.executeCommand(command, true);
                return rootRes.stdout.isEmpty() ? rootRes.stderr : rootRes.stdout;
            case READ_ONLY:
            default:
                ShellExecutor.CommandResult shellRes = ShellExecutor.executeCommand(command, false);
                return shellRes.stdout.isEmpty() ? shellRes.stderr : shellRes.stdout;
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
        if (result == null) return true;
        String lower = result.toLowerCase();
        if (lower.isEmpty() || lower.equals("ok") || lower.equals("0")) return true;
        if (lower.contains("error") || lower.contains("permission denial") ||
            lower.contains("securityexception") || lower.contains("permission denied") ||
            lower.contains("operation not permitted") || lower.contains("failed") ||
            lower.contains("not found")) {
            return false;
        }
        return true;
    }
}
