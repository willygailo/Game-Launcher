package com.gamespace.app.channels;

import com.gamespace.app.utils.ShizukuExecutor;

public class ShizukuChannel {

    public static boolean isAvailable() {
        return ShizukuExecutor.hasShizukuPermission();
    }

    public static String execute(String command) {
        if (!isAvailable()) {
            return "ERROR: Shizuku not available or permission denied";
        }
        return ShizukuExecutor.executeShizukuCommand(command);
    }

    public static boolean putSetting(String namespace, String key, String value) {
        String res = execute("settings put " + namespace + " " + key + " " + value);
        return res != null && !res.toLowerCase().contains("error") && !res.toLowerCase().contains("denied");
    }
}
