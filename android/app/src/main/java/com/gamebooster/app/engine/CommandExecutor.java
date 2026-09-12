package com.gamebooster.app.engine;

import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

import java.util.Collections;
import java.util.List;

public class CommandExecutor {

    public static EngineMode getActiveEngineMode() {
        boolean hasRoot = ShellExecutor.isRootSuAvailable();
        boolean hasShizuku = PrivilegeBridgeEngine.isShizukuVirtualRootReady();
        if (hasRoot && hasShizuku) {
            return EngineMode.DUAL_ENGINE;
        } else if (hasRoot) {
            return EngineMode.ROOT;
        } else if (hasShizuku) {
            return EngineMode.SHIZUKU;
        } else {
            return EngineMode.SYSTEM_SETTINGS;
        }
    }

    public static String executeSystemCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return "";
        }
        return PrivilegeBridgeEngine.executePrivileged(command);
    }

    public static List<String> executeBatchCommands(List<String> commands) {
        if (commands == null || commands.isEmpty()) {
            return Collections.emptyList();
        }
        return PrivilegeBridgeEngine.executePrivilegedBatch(commands);
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

    private static final String[] SAFE_PREFIXES = {"settings ", "setprop ", "cmd ", "device_config "};
    private static final String UNSAFE_CHARS = ";|&`$()";

    private static boolean isSafeCommand(String command) {
        if (command == null || command.trim().isEmpty()) return false;
        String trimmed = command.trim();
        for (String prefix : SAFE_PREFIXES) {
            if (trimmed.startsWith(prefix)) return true;
        }
        for (int i = 0; i < UNSAFE_CHARS.length(); i++) {
            if (trimmed.indexOf(UNSAFE_CHARS.charAt(i)) >= 0) return false;
        }
        return true;
    }
}
