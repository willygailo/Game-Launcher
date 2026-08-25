package com.gamebooster.app.booster;

import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.games.GamePackageRegistry;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.util.regex.Pattern;

public class CpuGovernorChannel {

    private static final String TAG = "CpuGovernorChannel";

    /**
     * Detects total available CPU cores on the device (8-core, 12-core, 16-core, etc.).
     */
    public static int detectCpuCoreCount() {
        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return Pattern.matches("cpu[0-9]+", pathname.getName());
                }
            });
            if (files != null && files.length > 0) {
                return files.length;
            }
        } catch (Throwable ignored) {}
        int cores = Runtime.getRuntime().availableProcessors();
        return cores > 0 ? cores : 8;
    }

    /**
     * Tunes Linux kernel cpuset and CPU topology scheduling based on detected core count (8, 12, 16+ cores).
     */
    public static void tuneMultiCoreTopology() {
        int coreCount = detectCpuCoreCount();
        int maxCoreIndex = Math.max(7, coreCount - 1);
        String allCores = "0-" + maxCoreIndex;

        // Determine efficiency cluster vs performance/prime cluster
        int bgMax = Math.min(3, maxCoreIndex / 2);
        String bgCores = "0-" + bgMax;

        StringBuilder sb = new StringBuilder();
        // Top-app & Foreground gets full access to all cores with priority on Big/Prime cores
        sb.append("echo ").append(allCores).append(" > /dev/cpuset/top-app/cpus 2>/dev/null; ");
        sb.append("echo ").append(allCores).append(" > /dev/cpuset/foreground/cpus 2>/dev/null; ");
        sb.append("echo ").append(bgCores).append(" > /dev/cpuset/background/cpus 2>/dev/null; ");
        sb.append("echo ").append(bgCores).append(" > /dev/cpuset/system-background/cpus 2>/dev/null; ");
        sb.append("echo ").append(allCores).append(" > /dev/cpuset/restricted/cpus 2>/dev/null; ");

        // Linux CFS scheduler & uclamp boost for real-time thread dispatching
        sb.append("for p in /sys/devices/system/cpu/cpufreq/policy*; do ");
        sb.append("echo performance > \"$p/scaling_governor\" 2>/dev/null; ");
        sb.append("if [ -f \"$p/scaling_max_freq\" ]; then cat \"$p/scaling_max_freq\" > \"$p/scaling_min_freq\" 2>/dev/null; fi; ");
        sb.append("done; ");

        sb.append("setprop sys.games.cpu_affinity 1; ");
        sb.append("setprop sys.use_fifo 1; ");
        sb.append("setprop sys.perf.sched_uclamp_min 1024; ");
        sb.append("setprop sys.perf.sched_uclamp_min_rt 1024; ");
        sb.append("setprop sys.perf.sched_min_granularity_ns 250000; ");
        sb.append("setprop sys.perf.sched_latency_ns 1000000; ");
        sb.append("setprop sys.perf.sched_wakeup_granularity_ns 500000; ");
        sb.append("setprop sys.perf.sched_boost 1; ");
        sb.append("cmd power set-fixed-performance-mode-enabled true; ");

        CommandExecutor.executeSystemCommand(sb.toString());
        Log.i(TAG, "Multi-core CPU topology tuned for " + coreCount + "-core processor (" + allCores + ").");
    }

    /**
     * Applies extended Linux kernel scheduler, VM, and EAS/SchedTune flags that are absent
     * from the base topology tuning. All writes use 2>/dev/null so they silently no-op on
     * kernels that don't expose the node.
     *
     * Covers:
     *  • CFS scheduler hints (child_runs_first, autogroup, migration_cost, nr_migrate)
     *  • Kernel sched debug features (NEXT_BUDDY)
     *  • VM pressure / dirty page / huge-page settings
     *  • EAS SchedTune (top-app boost=100, prefer_idle=0) for Qualcomm/MediaTek
     *  • cpu_dma_latency pin to 0 (prevents deep C-states between game frames)
     */
    public static void applyExtendedKernelFlags() {
        StringBuilder sb = new StringBuilder();

        // ── 1. CFS Scheduler hints ────────────────────────────────────────────
        // sched_child_runs_first: new spawned thread runs immediately → lower thread-spawn latency
        sb.append("echo 1 > /proc/sys/kernel/sched_child_runs_first 2>/dev/null; ");
        // sched_autogroup_enabled=0: disable process-group fairness throttling on game threads
        sb.append("echo 0 > /proc/sys/kernel/sched_autogroup_enabled 2>/dev/null; ");
        // sched_migration_cost_ns: keep game threads on Big/Prime cores longer before migrating
        sb.append("echo 50000 > /proc/sys/kernel/sched_migration_cost_ns 2>/dev/null; ");
        // sched_nr_migrate: allow more threads to migrate at once on 12/16-core devices
        sb.append("echo 128 > /proc/sys/kernel/sched_nr_migrate 2>/dev/null; ");
        // perf_event_paranoid=-1: allow userspace perf counters (needed by ADPF & game profilers)
        sb.append("echo -1 > /proc/sys/kernel/perf_event_paranoid 2>/dev/null; ");
        // CFS NEXT_BUDDY: woken thread is picked next — matches game render/audio thread patterns
        sb.append("echo NEXT_BUDDY > /sys/kernel/debug/sched_features 2>/dev/null; ");

        // ── 2. VM Memory Pressure Tuning ─────────────────────────────────────
        // swappiness=10: strongly prefer keeping game data in RAM over swapping
        sb.append("echo 10 > /proc/sys/vm/swappiness 2>/dev/null; ");
        // vfs_cache_pressure=50: keep game FS caches alive longer (default=100 reclaims aggressively)
        sb.append("echo 50 > /proc/sys/vm/vfs_cache_pressure 2>/dev/null; ");
        // dirty_ratio=5 / dirty_background_ratio=2: fast dirty-page writeback → less RAM pressure jank
        sb.append("echo 5 > /proc/sys/vm/dirty_ratio 2>/dev/null; ");
        sb.append("echo 2 > /proc/sys/vm/dirty_background_ratio 2>/dev/null; ");
        // page-cluster=0: read single swap pages → reduces swap-in stutter on ZRAM
        sb.append("echo 0 > /proc/sys/vm/page-cluster 2>/dev/null; ");
        // compaction_proactiveness=0: disable background memory compaction during gameplay
        sb.append("echo 0 > /proc/sys/vm/compaction_proactiveness 2>/dev/null; ");
        // watermark_boost_factor=0: prevents memory watermark boosts that trigger GC jank
        sb.append("echo 0 > /proc/sys/vm/watermark_boost_factor 2>/dev/null; ");
        // min_free_kbytes=65536: keep 64MB free pool to avoid allocation latency spikes
        sb.append("echo 65536 > /proc/sys/vm/min_free_kbytes 2>/dev/null; ");

        // ── 3. Transparent HugePages ─────────────────────────────────────────
        // Reduces TLB misses for the large game memory allocations (textures, geometry buffers)
        sb.append("echo always > /sys/kernel/mm/transparent_hugepage/enabled 2>/dev/null; ");
        sb.append("echo always > /sys/kernel/mm/transparent_hugepage/defrag 2>/dev/null; ");
        sb.append("echo 0 > /sys/kernel/mm/transparent_hugepage/khugepaged/scan_sleep_millisecs 2>/dev/null; ");

        // ── 4. EAS SchedTune (Qualcomm / MediaTek Energy Aware Scheduler) ────
        // boost=100 + prefer_idle=0: game task gets maximum energy budget, never idles
        sb.append("echo 100 > /dev/stune/top-app/schedtune.boost 2>/dev/null; ");
        sb.append("echo 0 > /dev/stune/top-app/schedtune.prefer_idle 2>/dev/null; ");
        sb.append("echo 100 > /dev/stune/foreground/schedtune.boost 2>/dev/null; ");
        sb.append("echo 0 > /dev/stune/foreground/schedtune.prefer_idle 2>/dev/null; ");
        sb.append("echo 0 > /dev/stune/background/schedtune.boost 2>/dev/null; ");
        // WALT/HMP per-cluster boost props (Qualcomm WALT scheduler)
        sb.append("setprop vendor.perf.cpu.boost.duration 2000; ");
        sb.append("setprop vendor.perf.cpu.boost.type 4; ");

        // ── 5. energyaware_latency / cpuidle latency governor ────────────────
        // Disable deep C-states between game frames via cpu_dma_latency fd-pin (best-effort)
        sb.append("echo 0 > /dev/cpu_dma_latency 2>/dev/null || true; ");

        CommandExecutor.executeSystemCommand(sb.toString());
        Log.i(TAG, "Extended kernel scheduler + VM + SchedTune flags applied.");
    }

    public static boolean setGovernor(String governor) {
        boolean isExtreme = "extreme".equalsIgnoreCase(governor) || "performance".equalsIgnoreCase(governor);
        if (isExtreme) {
            CommandExecutor.executeSystemCommand("cmd power set-mode 2 1");
            CommandExecutor.executeSystemCommand("cmd power set-mode 0 1");
            CommandExecutor.executeSystemCommand("cmd power set-fixed-performance-mode-enabled true");
            CommandExecutor.setSystemProperty("debug.hwui.render_thread_priority", "-20");
            CommandExecutor.setSystemProperty("sys.use_fifo", "1");
            CommandExecutor.setSystemProperty("sys.games.cpu_affinity", "1");

            // Tune multi-core CPU topology (8-core, 12-core, 16-core+)
            tuneMultiCoreTopology();

            // Apply extended kernel scheduler, VM, and EAS/SchedTune flags
            applyExtendedKernelFlags();

            // Apply per-game performance governor and CPU scheduler boost to all registered games
            for (String pkg : GamePackageRegistry.getAllKnownGames().keySet()) {
                try {
                    CommandExecutor.executeSystemCommand("cmd game mode performance " + pkg);
                    CommandExecutor.executeSystemCommand("cmd game set --fps 185 " + pkg);
                } catch (Throwable ignored) {}
            }
        } else {
            CommandExecutor.executeSystemCommand("cmd power set-fixed-performance-mode-enabled false");
            CommandExecutor.executeSystemCommand("cmd power set-mode 2 0");
            CommandExecutor.executeSystemCommand("cmd power set-mode 0 0");
            CommandExecutor.setSystemProperty("debug.hwui.render_thread_priority", "0");
            CommandExecutor.setSystemProperty("sys.use_fifo", "0");
            CommandExecutor.setSystemProperty("sys.games.cpu_affinity", "0");
            CommandExecutor.setSystemProperty("sys.perf.sched_uclamp_min", "0");
            CommandExecutor.setSystemProperty("sys.perf.sched_boost", "0");

            CommandExecutor.executeSystemCommand("for p in /sys/devices/system/cpu/cpufreq/policy*; do echo schedutil > \"$p/scaling_governor\" 2>/dev/null; done");

            for (String pkg : GamePackageRegistry.getAllKnownGames().keySet()) {
                try {
                    CommandExecutor.executeSystemCommand("cmd game mode standard " + pkg);
                    CommandExecutor.executeSystemCommand("cmd game reset " + pkg);
                } catch (Throwable ignored) {}
            }
        }
        return true;
    }

    public static boolean setPerformanceLock() {
        return setGovernor("extreme");
    }
}
