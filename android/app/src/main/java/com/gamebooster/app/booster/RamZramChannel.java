package com.gamebooster.app.booster;
import com.gamebooster.app.config.*;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;

public class RamZramChannel {

    private static final String TAG = "RamZramChannel";

    public static boolean trimMemoryAndCleanCache(Context context) {
        boolean isGameActive = false;
        String activeGame = null;
        if (context != null) {
            try {
                activeGame = com.gamebooster.app.games.ForegroundGameDetector.detectActiveGame(context);
                isGameActive = (activeGame != null && !activeGame.isEmpty());
            } catch (Throwable ignored) {}
        }

        if (isGameActive) {
            return trimMemoryInGame(context, activeGame);
        } else {
            return executePreLaunchClean(context);
        }
    }

    /**
     * Full pre-launch clean: safe to execute BEFORE game starts to maximize free RAM.
     */
    public static boolean executePreLaunchClean(Context context) {
        CommandExecutor.executeSystemCommand("cmd activity trim-memory --mode COMPLETE 2>/dev/null; "
                + "cmd activity compact full 2>/dev/null; "
                + "sync; echo 3 > /proc/sys/vm/drop_caches 2>/dev/null || true; "
                + "echo 10 > /proc/sys/vm/swappiness 2>/dev/null");
        Log.i(TAG, "Full pre-launch memory clean & pagecache drop executed.");
        return true;
    }

    /**
     * In-game memory optimization: compacts background processes and sets OOM priority
     * WITHOUT purging pagecache (preserves active game textures and shaders in RAM).
     */
    public static boolean trimMemoryInGame(Context context, String activeGamePkg) {
        StringBuilder sb = new StringBuilder();
        sb.append("cmd activity trim-memory --mode RUNNING_CRITICAL 2>/dev/null; ");
        sb.append("cmd activity compact background 2>/dev/null; ");
        sb.append("echo 10 > /proc/sys/vm/swappiness 2>/dev/null; ");
        if (activeGamePkg != null && !activeGamePkg.isEmpty()) {
            sb.append("pgrep -f \"").append(activeGamePkg).append("\" | head -1 | while read p; do [ -n \"$p\" ] && echo -1000 > /proc/$p/oom_score_adj 2>/dev/null; done; ");
        }
        CommandExecutor.executeSystemCommand(sb.toString());
        Log.i(TAG, "In-game RAM compaction applied (pagecache preserved for " + activeGamePkg + ").");
        return true;
    }

    /**
     * Tunes Linux block I/O scheduler and VM flags for gaming workloads.
     *
     * Game asset streaming (textures, audio, map chunks) benefits greatly from:
     *  • mq-deadline I/O scheduler (deterministic latency, no fairness overhead)
     *  • Large read_ahead_kb (2048KB) for sequential asset stream burst reads
     *  • Larger nr_requests queue (256) for GPU texture burst uploads
     *  • rotational=0 hint (mark internal UFS/eMMC storage as SSD)
     *
     * Also applies VM flags that complement CpuGovernorChannel.applyExtendedKernelFlags()
     * but are specifically memory/IO oriented and safe to call independently.
     *
     * All writes redirect stderr to /dev/null — silently no-ops on unsupported kernels.
     */
    public static void applyIOAndMemoryFlags() {
        StringBuilder sb = new StringBuilder();

        // ── 1. Block device I/O Scheduler ────────────────────────────────────
        // mq-deadline: deterministic latency, no fairness throttling between game and system I/O
        // Falls back gracefully on kernels using 'none' (NVMe) or older 'deadline'
        sb.append("for q in /sys/block/*/queue/scheduler; do " +
                "echo mq-deadline > \"$q\" 2>/dev/null || echo deadline > \"$q\" 2>/dev/null || true; " +
                "done; ");

        // read_ahead_kb=2048: large sequential read buffer for game level/map/texture streaming
        sb.append("for q in /sys/block/*/queue/read_ahead_kb; do echo 2048 > \"$q\" 2>/dev/null; done; ");

        // nr_requests=256: larger I/O queue depth for burst texture uploads to GPU
        sb.append("for q in /sys/block/*/queue/nr_requests; do echo 256 > \"$q\" 2>/dev/null; done; ");

        // rotational=0: mark internal UFS/eMMC/NVMe storage as SSD (scheduler hint)
        sb.append("for q in /sys/block/*/queue/rotational; do echo 0 > \"$q\" 2>/dev/null; done; ");

        // add_random=0: disable entropy collection from I/O (not needed for games, reduces overhead)
        sb.append("for q in /sys/block/*/queue/add_random; do echo 0 > \"$q\" 2>/dev/null; done; ");

        // rq_affinity=2: I/O completions processed on the CPU that submitted them (cache-friendly)
        sb.append("for q in /sys/block/*/queue/rq_affinity; do echo 2 > \"$q\" 2>/dev/null; done; ");

        // ── 2. ZRAM / Swap tuning ────────────────────────────────────────────
        // Use lz4 for ZRAM compression if available — faster decompress than lzo at same ratio
        sb.append("echo lz4 > /sys/block/zram0/comp_algorithm 2>/dev/null || true; ");

        // writeback_limit_enable=0: disable ZRAM writeback rate limiting during gameplay
        sb.append("echo 0 > /sys/block/zram0/writeback_limit_enable 2>/dev/null; ");

        // ── 3. Additional VM flags (gaming-specific, complement CpuGovernorChannel) ──
        // oom_kill_allocating_task=0: don't kill allocating task on OOM (prefer killing background)
        sb.append("echo 0 > /proc/sys/vm/oom_kill_allocating_task 2>/dev/null; ");

        // overcommit_memory=1: always allow memory allocation (prevents false OOM during loading)
        sb.append("echo 1 > /proc/sys/vm/overcommit_memory 2>/dev/null; ");

        // zone_reclaim_mode=0: disable NUMA zone reclaim — reduces jank from cross-zone memory ops
        sb.append("echo 0 > /proc/sys/vm/zone_reclaim_mode 2>/dev/null; ");

        CommandExecutor.executeSystemCommand(sb.toString());
        Log.i(TAG, "I/O scheduler + memory flags applied for gaming.");
    }
}
