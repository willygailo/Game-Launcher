package com.gamebooster.app.booster;

import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.games.GamePackageRegistry;

import java.io.File;
import java.io.FileFilter;
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
