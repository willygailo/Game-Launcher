package com.gamespace.app.channels;

import android.content.Context;
import com.gamespace.app.data.CommandExecutor;

public class RamZramChannel {

    public static boolean trimMemoryAndCleanCache(Context context) {
        boolean ok = true;
        // Trim background tasks via Shizuku/ADB or Root
        CommandExecutor.executeSystemCommand("am kill-all");
        CommandExecutor.executeSystemCommand("am trim-memory ALL");

        if (RootCommandChannel.isAvailable()) {
            RootCommandChannel.writeSysfs("/proc/sys/vm/drop_caches", "3");
            RootCommandChannel.writeSysfs("/proc/sys/vm/swappiness", "10");
        }
        return ok;
    }
}
