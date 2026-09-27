package com.gamebooster.app.booster;

import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.games.GamePackageRegistry;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CpuGovernorChannel {

    private static final String TAG = "CpuGovernorChannel";

    /**
     * Detects CPU cluster topology (big/LITTLE/prime cores) from sysfs.
     * Returns array of cluster info: [core_start, core_end, cluster_type]
     * cluster_type: 0=efficiency/LITTLE, 1=big, 2=prime/performance
     */
    public static int[][] detectCpuClusterTopology() {
        int coreCount = detectCpuCoreCount();
        List<int[]> clusters = new ArrayList<>();
        int[][] topology = new int[0][3];

        try {
            // Read related_cpus for each policy to identify clusters
            File policyDir = new File("/sys/devices/system/cpu/cpufreq/");
            File[] policies = policyDir.listFiles((dir, name) -> name.startsWith("policy"));
            if (policies != null && policies.length > 0) {
                for (File policy : policies) {
                    File relatedCpus = new File(policy, "related_cpus");
                    if (relatedCpus.exists()) {
                        String content = new String(java.nio.file.Files.readAllBytes(relatedCpus.toPath())).trim();
                        String[] parts = content.split("-");
                        if (parts.length == 2) {
                            int start = Integer.parseInt(parts[0]);
                            int end = Integer.parseInt(parts[1]);
                            // Determine cluster type by max frequency
                            File maxFreqFile = new File(policy, "cpuinfo_max_freq");
                            long maxFreq = 0;
                            if (maxFreqFile.exists()) {
                                maxFreq = Long.parseLong(new String(java.nio.file.Files.readAllBytes(maxFreqFile.toPath())).trim());
                            }
                            int clusterType = 0; // efficiency
                            if (maxFreq >= 2800000) clusterType = 2; // prime (2.8GHz+)
                            else if (maxFreq >= 2000000) clusterType = 1; // big (2.0GHz+)
                            clusters.add(new int[]{start, end, clusterType});
                        }
                    }
                }
                // Sort by start core
                clusters.sort((a, b) -> Integer.compare(a[0], b[0]));
                topology = clusters.toArray(new int[0][3]);
            }
        } catch (Throwable ignored) {
            // Fallback: simple split
            int mid = coreCount / 2;
            topology = new int[][]{
                {0, mid - 1, 0},           // efficiency
                {mid, coreCount - 1, 1}    // big
            };
        }
        return topology;
    }

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
     * Applies per-cluster governors: Prime/Performance → "performance", Big → "performance", LITTLE → "powersave"
     * Pins CPU frequencies (min = max), disables C-states, tunes uclamp for top-app.
     */
    public static void tuneMultiCoreTopology() {
        int coreCount = detectCpuCoreCount();
        int maxCoreIndex = Math.max(7, coreCount - 1);
        String allCores = "0-" + maxCoreIndex;
        int[][] topology = detectCpuClusterTopology();

        // Build per-cluster core lists
        StringBuilder primeCores = new StringBuilder();
        StringBuilder bigCores = new StringBuilder();
        StringBuilder littleCores = new StringBuilder();

        for (int[] cluster : topology) {
            int start = cluster[0];
            int end = cluster[1];
            int type = cluster[2];
            String range = start + "-" + end;
            if (type == 2) { // prime
                if (primeCores.length() > 0) primeCores.append(",");
                primeCores.append(range);
            } else if (type == 1) { // big
                if (bigCores.length() > 0) bigCores.append(",");
                bigCores.append(range);
            } else { // little/efficiency
                if (littleCores.length() > 0) littleCores.append(",");
                littleCores.append(range);
            }
        }

        String perfCores = (primeCores.length() > 0 ? primeCores.toString() : "") +
                (primeCores.length() > 0 && bigCores.length() > 0 ? "," : "") +
                (bigCores.length() > 0 ? bigCores.toString() : "");
        String bgCores = littleCores.length() > 0 ? littleCores.toString() : (bigCores.length() > 0 ? bigCores.toString() : "0-" + Math.min(3, maxCoreIndex / 2));

        StringBuilder sb = new StringBuilder();

        // ── Cpuset assignments ──────────────────────────────────────────────────
        // Top-app & Foreground gets ALL cores (but uclamp will prioritize perf cores)
        sb.append("echo ").append(allCores).append(" > /dev/cpuset/top-app/cpus 2>/dev/null; ");
        sb.append("echo ").append(allCores).append(" > /dev/cpuset/foreground/cpus 2>/dev/null; ");
        // Background restricted to efficiency cores
        sb.append("echo ").append(bgCores).append(" > /dev/cpuset/background/cpus 2>/dev/null; ");
        sb.append("echo ").append(bgCores).append(" > /dev/cpuset/system-background/cpus 2>/dev/null; ");
        sb.append("echo ").append(allCores).append(" > /dev/cpuset/restricted/cpus 2>/dev/null; ");

        // ── Per-Cluster Governors & Frequency Pinning ───────────────────────────
        // Prime/Performance cores → performance governor, min_freq = max_freq
        // Big cores → performance governor, min_freq = max_freq
        // LITTLE/Efficiency cores → powersave governor (isolate background)
        sb.append("for p in /sys/devices/system/cpu/cpufreq/policy*; do ");
        sb.append("  gov=\"performance\"; ");
        sb.append("  # Check if this policy covers only LITTLE cores ");
        sb.append("  related=$(cat \"$p/related_cpus\" 2>/dev/null || echo \"\"); ");
        sb.append("  if [ -n \"$related\" ]; then ");
        sb.append("    first_core=$(echo \"$related\" | cut -d'-' -f1); ");
        sb.append("    # Heuristic: core 0-3 typically LITTLE on 8-core, adjust for 12/16-core ");
        sb.append("    if [ \"$first_core\" -lt 4 ]; then gov=\"powersave\"; fi; ");
        sb.append("  fi; ");
        sb.append("  echo \"$gov\" > \"$p/scaling_governor\" 2>/dev/null; ");
        sb.append("  # Pin frequency: min = max ");
        sb.append("  if [ -f \"$p/scaling_max_freq\" ]; then cat \"$p/scaling_max_freq\" > \"$p/scaling_min_freq\" 2>/dev/null; fi; ");
        sb.append("  # Also pin cpuinfo_max_freq to scaling_min_freq ");
        sb.append("  if [ -f \"$p/cpuinfo_max_freq\" ]; then cat \"$p/cpuinfo_max_freq\" > \"$p/scaling_min_freq\" 2>/dev/null; fi; ");
        sb.append("  # Disable energy_perf_bias (0 = performance) ");
        sb.append("  echo 0 > \"$p/energy_perf_bias\" 2>/dev/null; ");
        sb.append("done; ");

        // ── Disable C-States / Idle States ──────────────────────────────────────
        // Prevent deep sleep between frames
        sb.append("for cpu in /sys/devices/system/cpu/cpu*/cpuidle/state*/disable; do ");
        sb.append("  echo 1 > \"$cpu\" 2>/dev/null; ");
        sb.append("done; ");
        sb.append("echo 1 > /sys/module/cpuidle/parameters/enable 2>/dev/null; "); // 1=disable cpuidle
        sb.append("echo 0 > /dev/cpu_dma_latency 2>/dev/null; ");

        // ── Linux CFS scheduler & uclamp boost for real-time thread dispatching ─
        sb.append("echo 0 > /proc/sys/kernel/sched_energy_aware 2>/dev/null; ");
        sb.append("echo 0 > /sys/devices/system/cpu/eas/enable 2>/dev/null; ");
        sb.append("echo 1024 > /dev/cpuset/top-app/uclamp.min 2>/dev/null; ");
        sb.append("echo 1024 > /dev/cpuset/top-app/uclamp.max 2>/dev/null; ");
        sb.append("echo 1024 > /dev/cpuset/top-app/uclamp.boosted 2>/dev/null; ");
        sb.append("echo 1024 > /dev/cpuset/foreground/uclamp.min 2>/dev/null; ");
        sb.append("echo 1024 > /dev/cpuset/foreground/uclamp.max 2>/dev/null; ");
        sb.append("echo 0 > /dev/cpuset/background/uclamp.max 2>/dev/null; ");
        sb.append("setprop sys.games.cpu_affinity 1; ");
        sb.append("setprop sys.perf.sched_uclamp_min 1024; ");
        sb.append("setprop sys.perf.sched_uclamp_max 1024; ");
        sb.append("setprop sys.perf.sched_uclamp_min_rt 1024; ");
        sb.append("setprop sys.perf.sched_min_granularity_ns 100000; ");  // 0.1ms - tighter
        sb.append("setprop sys.perf.sched_latency_ns 500000; ");         // 0.5ms - tighter
        sb.append("setprop sys.perf.sched_wakeup_granularity_ns 250000; "); // 0.25ms - tighter
        sb.append("setprop sys.perf.sched_boost 1; ");
        sb.append("cmd power set-fixed-performance-mode-enabled true; ");

        CommandExecutor.executeSystemCommand(sb.toString());
        Log.i(TAG, "Multi-core CPU topology tuned for " + coreCount + "-core processor (" + allCores + "). "
                + "Prime: " + (primeCores.length() > 0 ? primeCores : "none")
                + ", Big: " + (bigCores.length() > 0 ? bigCores : "none")
                + ", LITTLE: " + (littleCores.length() > 0 ? littleCores : "none"));
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
        // WALT/HMP per-cluster boost props (Qualcomm WALT scheduler)
        sb.append("setprop vendor.perf.cpu.boost.duration 2000; ");
        sb.append("setprop vendor.perf.cpu.boost.type 4; ");
        // MediaTek / Exynos / Tensor / Kirin / UNISOC CPU sched boosts
        sb.append("setprop persist.vendor.cpu.boost 1; ");
        sb.append("setprop sys.exynos.perf.mode 1; ");
        sb.append("setprop vendor.perf.gestureFlingBoost 1; ");
        sb.append("setprop persist.sys.cpufreq.boost 1; ");
        sb.append("setprop persist.sys.sprd.coreboost 1; ");

        // ── 5. energyaware_latency / cpuidle latency governor ────────────────
        // Disable deep C-states between game frames via cpu_dma_latency fd-pin (best-effort)
        sb.append("echo 0 > /dev/cpu_dma_latency 2>/dev/null || true; ");

        CommandExecutor.executeSystemCommand(sb.toString());
        Log.i(TAG, "Extended kernel scheduler + VM + SchedTune flags applied.");
    }

    /**
     * Applies I/O pipeline tuning, RT thread scheduling, and kernel perf unlock flags.
     *
     * Covers what applyExtendedKernelFlags() intentionally omits:
     *
     *  A. I/O Scheduler: 'deadline' (UFS/eMMC NAND) or 'mq-deadline' (NVMe-like UFS 3.1)
     *     replaces default 'cfq' → predictable read/write latency → no storage stall mid-match.
     *  B. io_is_busy=1: tells the CPU governor that I/O activity means the CPU should stay boosted
     *     → prevents CPU down-clocking during map asset streaming.
     *  C. sched_rt_runtime_us=-1: removes the 95% RT throttle cap — game RT threads can run
     *     more than 950ms/second without being starved (default Linux throttles RT at 950ms).
     *  D. kptr_restrict=0: exposes kernel symbol addresses to userspace → required by ADPF
     *     perf counter HAL and some Qualcomm profiler modules for DCVS feedback.
     *  E. IRQ kernel thread RT: elevates kworker / ksoftirqd that handle game NIC + GPU IRQs
     *     to SCHED_FIFO — eliminates the ~0.3ms IRQ service delay from CFS.
     *  F. vm.mmap_min_addr=0: allows zero-page mapping needed by some Unity3D JIT trampolines.
     */
    public static void applyIoPipelineAndRtFlags() {
        StringBuilder sb = new StringBuilder();

        // ── A. I/O Scheduler: deadline for all block devices ──────────────────
        sb.append("for q in /sys/block/*/queue/scheduler; do ")
          .append("echo deadline > \"$q\" 2>/dev/null || echo mq-deadline > \"$q\" 2>/dev/null; ")
          .append("done; ");

        // Tune deadline scheduler: reduce max latency for reads (game asset streaming)
        sb.append("for q in /sys/block/*/queue; do ")
          .append("echo 64 > \"$q/nr_requests\" 2>/dev/null; ")
          .append("echo 0 > \"$q/add_random\" 2>/dev/null; ")
          .append("echo 0 > \"$q/rotational\" 2>/dev/null; ")
          .append("echo 256 > \"$q/read_ahead_kb\" 2>/dev/null; ")
          .append("done; ");

        // ── B. io_is_busy: prevent CPU downclocking during map streaming ──────
        sb.append("for p in /sys/devices/system/cpu/cpufreq/policy*; do ")
          .append("echo 1 > \"$p/io_is_busy\" 2>/dev/null; ")
          .append("done; ");

        // ── C. Remove 95% RT runtime throttle cap ─────────────────────────────
        // Default: sched_rt_runtime_us=950000 (RT threads max 950ms/second)
        // Gaming threads (AudioFlinger, RenderThread) need > 950ms/second burst ability
        sb.append("sysctl -w kernel.sched_rt_runtime_us=-1 2>/dev/null; ");
        sb.append("sysctl -w kernel.sched_rt_period_us=1000000 2>/dev/null; ");

        // ── D. Kernel pointer exposure for perf counters (ADPF, Qualcomm DCVS) ─
        sb.append("sysctl -w kernel.kptr_restrict=0 2>/dev/null; ");
        sb.append("sysctl -w kernel.perf_event_max_sample_rate=100000 2>/dev/null; ");
        sb.append("sysctl -w kernel.perf_cpu_time_max_percent=25 2>/dev/null; ");

        // ── E. IRQ / kworker RT promotion ────────────────────────────────────
        // Raise kworker threads that handle GPU command completion + Wi-Fi DMA to FIFO 5
        sb.append("for pid in $(ps -T -eo pid,tid,comm | grep -E 'kworker|irq/' | awk '{print $2}' | head -20); do ")
          .append("chrt -f -p 5 $pid 2>/dev/null; ")
          .append("done; ");

        // ── F. vm.mmap_min_addr=0: Unity3D JIT trampoline compatibility ───────
        sb.append("sysctl -w vm.mmap_min_addr=0 2>/dev/null; ");

        // ── G. Kernel read-ahead for game asset bundles ───────────────────────
        sb.append("sysctl -w vm.dirty_writeback_centisecs=500 2>/dev/null; ");
        sb.append("sysctl -w vm.dirty_expire_centisecs=200 2>/dev/null; ");

        // ── H. Disable scheduler debug throttle (prevents CFS group throttling) ─
        sb.append("echo 0 > /proc/sys/kernel/sched_cfs_bandwidth_slice_us 2>/dev/null; ");
        sb.append("sysctl -w kernel.sched_latency_ns=2000000 2>/dev/null; ");
        sb.append("sysctl -w kernel.sched_min_granularity_ns=250000 2>/dev/null; ");
        sb.append("sysctl -w kernel.sched_wakeup_granularity_ns=500000 2>/dev/null; ");

        CommandExecutor.executeSystemCommand(sb.toString());
        Log.i(TAG, "I/O pipeline, RT throttle bypass, IRQ FIFO, and perf counter flags applied.");
    }

    public static boolean setGovernor(String governor) {
        boolean isExtreme = "extreme".equalsIgnoreCase(governor) || "performance".equalsIgnoreCase(governor);
        if (isExtreme) {
            CommandExecutor.executeSystemCommand("cmd power set-mode 2 1");
            CommandExecutor.executeSystemCommand("cmd power set-mode 0 1");
            CommandExecutor.executeSystemCommand("cmd power set-fixed-performance-mode-enabled true");
            CommandExecutor.setSystemProperty("debug.hwui.render_thread_priority", "-20");
            CommandExecutor.setSystemProperty("sys.games.cpu_affinity", "1");

            // Tune multi-core CPU topology (8-core, 12-core, 16-core+)
            tuneMultiCoreTopology();

            // Apply extended kernel scheduler, VM, and EAS/SchedTune flags
            applyExtendedKernelFlags();

            // Apply I/O scheduler, RT throttle bypass, IRQ FIFO, and perf unlock flags
            applyIoPipelineAndRtFlags();

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
            CommandExecutor.executeSystemCommand("echo 0 > /dev/cpuset/top-app/uclamp.min 2>/dev/null; echo 0 > /dev/cpuset/top-app/uclamp.boosted 2>/dev/null");

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
