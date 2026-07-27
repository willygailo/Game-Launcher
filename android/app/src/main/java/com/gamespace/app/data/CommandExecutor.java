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
        return result == null || !result.startsWith("ERROR");
    }

    public static boolean setSystemSetting(String namespace, String key, String value) {
        String cmd = "settings put " + namespace + " " + key + " " + value;
        String result = executeSystemCommand(cmd);
        return result == null || !result.startsWith("ERROR");
    }
}
