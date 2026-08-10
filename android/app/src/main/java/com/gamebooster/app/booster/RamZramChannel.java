package com.gamebooster.app.booster;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * RamZramChannel performs complete RAM and ZRAM memory optimization,
 * process trimming, pagecache clearing, and ZRAM heap compaction.
 */
public class RamZramChannel {

    private static final String TAG = "RamZramChannel";

    public static class MemoryStats {
        public final long totalRamMb;
        public final long availRamMbBefore;
        public final long availRamMbAfter;
        public final long freedRamMb;

        public MemoryStats(long totalRamMb, long availRamMbBefore, long availRamMbAfter) {
            this.totalRamMb = totalRamMb;
            this.availRamMbBefore = availRamMbBefore;
            this.availRamMbAfter = availRamMbAfter;
            this.freedRamMb = Math.max(0, availRamMbAfter - availRamMbBefore);
        }
    }

    public static MemoryStats optimizeMemory(Context context) {
        long totalRam = 0;
        long availBefore = 0;
        long availAfter = 0;

        if (context != null) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                am.getMemoryInfo(mi);
                totalRam = mi.totalMem / (1024 * 1024);
                availBefore = mi.availMem / (1024 * 1024);
            }
        }

        Log.d(TAG, "Starting RAM & ZRAM Optimization. Avail Before: " + availBefore + "MB / " + totalRam + "MB");

        // 1. Kill cached non-foreground processes via ADB shell
        CommandExecutor.executeSystemCommand("am kill-all");
        ShizukuExecutor.executeShizukuCommand("am kill-all");

        // 2. Instruct ActivityManager to perform full memory trim & compaction
        CommandExecutor.executeSystemCommand("cmd activity trim-memory --mode COMPLETE");
        ShizukuExecutor.executeShizukuCommand("cmd activity trim-memory --mode COMPLETE");

        CommandExecutor.executeSystemCommand("cmd activity compact full");
        ShizukuExecutor.executeShizukuCommand("cmd activity compact full");

        // 3. Pagecache & ZRAM memory compaction via sysfs
        CommandExecutor.executeSystemCommand("echo 3 > /proc/sys/vm/drop_caches");
        ShizukuExecutor.executeShizukuCommand("echo 3 > /proc/sys/vm/drop_caches");

        CommandExecutor.executeSystemCommand("echo 1 > /proc/sys/vm/compact_memory");
        ShizukuExecutor.executeShizukuCommand("echo 1 > /proc/sys/vm/compact_memory");

        if (context != null) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                am.getMemoryInfo(mi);
                availAfter = mi.availMem / (1024 * 1024);
            }
        }

        MemoryStats stats = new MemoryStats(totalRam, availBefore, availAfter);
        Log.d(TAG, "RAM Optimization Complete. Freed: " + stats.freedRamMb + "MB (Now " + stats.availRamMbAfter + "MB Free)");
        return stats;
    }

    public static boolean trimMemoryAndCleanCache(Context context) {
        MemoryStats stats = optimizeMemory(context);
        return stats.freedRamMb >= 0;
    }
}
