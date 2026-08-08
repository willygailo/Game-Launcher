package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import java.util.ArrayList;
import java.util.List;

/**
 * EsportsGameTouchPatcher provides zero touch delay injection for
 * Blood Strike, Farlight 84, Standoff 2, Wild Rift, Genshin Impact, and all other competitive games.
 */
public class EsportsGameTouchPatcher {

    private static final String TAG = "EsportsGameTouchPatcher";

    public static boolean applyGenericZeroTouchDelay(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String pkg = packageName.toLowerCase().trim();

        List<String> paths = new ArrayList<>();
        paths.add("/sdcard/Android/data/" + pkg + "/files/GameSettings.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/TouchSettings.ini");
        paths.add("/sdcard/Android/data/" + pkg + "/files/UserConfig.json");
        paths.add("/data/data/" + pkg + "/files/GameSettings.ini");
        paths.add("/data/data/" + pkg + "/files/TouchSettings.ini");

        String[] touchKeys = new String[]{
            "TouchBoostHz=165",
            "TouchResponseSpeed=3",
            "ZeroTouchDelay=1",
            "TouchFilterMode=0",
            "InputPollingRate=1000",
            "TouchSlopReduction=1"
        };

        int count = 0;
        for (String path : paths) {
            ensureDirectory(path);
            StringBuilder sb = new StringBuilder();
            for (String keyVal : touchKeys) {
                String k = keyVal.substring(0, keyVal.indexOf("="));
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(keyVal).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/^").append(k).append("=.*/").append(keyVal).append("/' ").append(path).append("; ");
            }
            String cmd = sb.toString();
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
            count++;
        }
        Log.i(TAG, "Applied generic zero touch delay for " + pkg + " across " + count + " paths");
        return true;
    }

    private static void ensureDirectory(String path) {
        if (path == null) return;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            String parentDir = path.substring(0, lastSlash);
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand("mkdir -p " + parentDir);
            } else {
                CommandExecutor.executeSystemCommand("mkdir -p " + parentDir);
            }
        }
    }
}
