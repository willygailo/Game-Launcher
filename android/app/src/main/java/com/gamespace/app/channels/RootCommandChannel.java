package com.gamespace.app.channels;

import com.gamespace.app.utils.ShellExecutor;

public class RootCommandChannel {

    public static boolean isAvailable() {
        return ShellExecutor.isRootAvailable();
    }

    public static ShellExecutor.CommandResult execute(String command) {
        return ShellExecutor.executeCommand(command, true);
    }

    public static boolean writeSysfs(String nodePath, String value) {
        if (!isAvailable()) return false;
        String cmd = "echo \"" + value + "\" > " + nodePath + " 2>/dev/null || true";
        ShellExecutor.CommandResult res = execute(cmd);
        return res.isSuccess();
    }

    public static boolean setProp(String key, String value) {
        if (!isAvailable()) return false;
        ShellExecutor.CommandResult res = execute("setprop " + key + " " + value);
        return res.isSuccess();
    }
}
