package com.gamebooster.app.functions;

import android.app.ActivityManager;
import android.content.Context;
import com.gamebooster.app.root.CommandExecutor;

public class RamZramChannel {

    public static boolean trimMemoryAndCleanCache(Context context) {
        boolean ok = true;
        if (context != null) {
            try {
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    am.killBackgroundProcesses(context.getPackageName());
                }
            } catch (Exception ignored) {}
        }
        CommandExecutor.executeSystemCommand("am kill-all");
        CommandExecutor.executeSystemCommand("am trim-memory ALL");
        CommandExecutor.executeSystemCommand("cmd activity compact full");
        return ok;
    }
}
