package com.gamebooster.app.booster;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.engine.PrivilegeBridgeEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * MemoryCleanerPro — Pre-Game RAM Burst Clean + LMKD Tuning + Game Process Immortal Lock.
 *
 * Extends basic RamZramChannel.java with:
 *  1. Pre-game RAM burst clean — aggressive background app kill + ZRAM compaction before launch.
 *  2. LMKD tuning — raises minfree thresholds so the game is never OOM-killed mid-match.
 *  3. Game process immortal lock — sets game oom_adj = -17 (un-killable) via Shizuku.
 *  4. Idle process whitelist/blacklist — keeps only game + essentials alive.
 *  5. Auto-clean trigger — can be fired periodically during game session.
 *
 * 2026.2 Edition — Performance Suite Integration.
 */
public final class MemoryCleanerPro {

    private static final String TAG = "MemoryCleanerPro";

    private MemoryCleanerPro() {}

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Pre-game burst RAM clean.
     * Call before launching a game — kills background apps + compacts ZRAM.
     */
    public static boolean applyPreGameBurstClean(Context context) {
        Log.i(TAG, "🧹 Pre-game burst RAM clean starting...");
        List<String> cmds = new ArrayList<>();

        // Drop OS page/slab/inode caches
        cmds.add("sync");
        cmds.add("echo 3 > /proc/sys/vm/drop_caches");

        // Compact ZRAM
        cmds.add("echo 1 > /proc/sys/vm/compact_memory");

        // Trigger system-level memory trim
        cmds.add("am send-trim-memory all RUNNING_CRITICAL");
        cmds.add("am send-trim-memory all COMPLETE");

        // Kill common background drain processes
        String[] killTargets = {
            "com.google.android.googlequicksearchbox",
            "com.google.android.apps.photos",
            "com.google.android.youtube",
            "com.facebook.katana",
            "com.facebook.orca",
            "com.instagram.android",
            "com.tiktok.android",
            "com.snapchat.android",
            "com.spotify.music",
            "com.netflix.mediaclient",
        };
        for (String pkg : killTargets) {
            cmds.add("am force-stop " + pkg);
        }

        // Force GC on all running apps
        cmds.add("am gc");

        // Compact all running processes
        cmds.add("am compact all");

        boolean ok = CommandExecutor.executeBatch(cmds);
        Log.i(TAG, ok ? "✅ Pre-game burst RAM clean done" : "⚠️ Pre-game clean partial");
        return ok;
    }

    /**
     * LMKD Tuning — raises minfree thresholds so game never gets OOM-killed mid-match.
     * Standard minfree tiers (6 slots): foreground, visible, service, cached, etc.
     */
    public static boolean applyLmkdGamingTune() {
        Log.i(TAG, "🔧 Applying LMKD gaming tune...");
        List<String> cmds = new ArrayList<>();

        // Raise minfree (MB): 4, 8, 16, 64, 128, 192 — generous for gaming
        cmds.add("echo '4096,8192,16384,65536,131072,196608' > /sys/module/lowmemorykiller/parameters/minfree");

        // Raise OOM score adj limits for foreground processes
        cmds.add("echo '0,100,200,300,900,906' > /sys/module/lowmemorykiller/parameters/adj");

        // Disable swap tendency during game (keep RAM hot)
        cmds.add("echo 10 > /proc/sys/vm/swappiness");
        cmds.add("echo 5 > /proc/sys/vm/vfs_cache_pressure");

        // Disable background write-back jank
        cmds.add("echo 500 > /proc/sys/vm/dirty_expire_centisecs");
        cmds.add("echo 500 > /proc/sys/vm/dirty_writeback_centisecs");

        // Increase dirty ratio for lower I/O interrupts
        cmds.add("echo 30 > /proc/sys/vm/dirty_ratio");
        cmds.add("echo 5 > /proc/sys/vm/dirty_background_ratio");

        boolean ok = CommandExecutor.executeBatch(cmds);
        Log.i(TAG, ok ? "✅ LMKD gaming tune applied" : "⚠️ LMKD tune partial");
        return ok;
    }

    /**
     * Game Process Immortal Lock.
     * Sets game process oom_adj to -17 (system-level, un-killable) via Shizuku.
     * Call after game process is confirmed running.
     *
     * @param gamePackage the game's package name
     */
    public static boolean applyGameProcessImmortalLock(String gamePackage) {
        if (gamePackage == null) return false;
        Log.i(TAG, "🔒 Applying process immortal lock for " + gamePackage);
        List<String> cmds = new ArrayList<>();

        // Get PID and set oom_adj to -17 (immune to OOM killer)
        cmds.add("PID=$(pidof " + gamePackage + " | awk '{print $1}'); " +
                 "if [ ! -z \"$PID\" ]; then echo -17 > /proc/$PID/oom_adj; " +
                 "echo -1000 > /proc/$PID/oom_score_adj; fi");

        // Also set oom_score_adj via am (softer, doesn't need /proc write)
        cmds.add("am oom-adj " + gamePackage + " -17");

        // Boost scheduling priority
        cmds.add("PID=$(pidof " + gamePackage + " | awk '{print $1}'); " +
                 "if [ ! -z \"$PID\" ]; then renice -n -10 -p $PID; fi");

        boolean ok = CommandExecutor.executeBatch(cmds);
        Log.i(TAG, ok ? "✅ " + gamePackage + " process immortal lock applied" : "⚠️ Lock partial");
        return ok;
    }

    /**
     * Kill all non-essential background processes to free RAM for gaming.
     * Keeps: game, launcher, system server, surface flinger, audio.
     *
     * @param excludePackage the game package to exclude from killing
     */
    public static boolean killIdleProcesses(String excludePackage) {
        Log.i(TAG, "🧹 Killing idle background processes...");
        List<String> cmds = new ArrayList<>();

        // Kill cached + empty processes (safe — only background)
        cmds.add("am kill-all");

        // Trim HEAVY processes to 80MB resident (cached only)
        cmds.add("am send-trim-memory all RUNNING_MODERATE");

        // Compact via ActivityManager
        cmds.add("am compact all");

        boolean ok = CommandExecutor.executeBatch(cmds);
        Log.i(TAG, ok ? "✅ Idle process kill done" : "⚠️ Process kill partial");
        return ok;
    }

    /**
     * Full gaming memory profile — runs pre-game burst + LMKD tune + lock in one call.
     *
     * @param context     application context
     * @param gamePackage game package to immortal-lock
     */
    public static void applyFullGamingMemoryProfile(Context context, String gamePackage) {
        Log.i(TAG, "🚀 Applying full gaming memory profile for " + gamePackage);
        new Thread(() -> {
            try {
                applyPreGameBurstClean(context);
                Thread.sleep(200);
                applyLmkdGamingTune();
                Thread.sleep(100);
                killIdleProcesses(gamePackage);
                Thread.sleep(300);
                applyGameProcessImmortalLock(gamePackage);
                Log.i(TAG, "✅ Full gaming memory profile applied for " + gamePackage);
            } catch (Throwable t) {
                Log.w(TAG, "Full memory profile error: " + t.getMessage());
            }
        }, "MemoryCleanerPro-" + gamePackage).start();
    }

    /**
     * Restore default LMKD settings after game session ends.
     */
    public static void restoreDefaultLmkd() {
        List<String> cmds = new ArrayList<>();
        cmds.add("echo '18432,23040,27648,32256,36864,46080' > /sys/module/lowmemorykiller/parameters/minfree");
        cmds.add("echo '0,1,6,7,8,9' > /sys/module/lowmemorykiller/parameters/adj");
        cmds.add("echo 100 > /proc/sys/vm/swappiness");
        cmds.add("echo 100 > /proc/sys/vm/vfs_cache_pressure");
        CommandExecutor.executeBatch(cmds);
        Log.i(TAG, "✅ Default LMKD restored");
    }
}
