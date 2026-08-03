package com.gamebooster.app.booster;

import android.app.ActivityManager;
import android.content.Context;
import com.gamebooster.app.engine.CommandExecutor;

public class RamZramChannel {

    public static boolean trimMemoryAndCleanCache(Context context) {
        boolean ok = true;
        // Kill cached background processes via Shizuku (not our own app)
        CommandExecutor.executeSystemCommand("am kill-all");
        CommandExecutor.executeSystemCommand("cmd activity trim-memory --mode COMPLETE");
        CommandExecutor.executeSystemCommand("cmd activity compact full");
        return ok;
    }
}
