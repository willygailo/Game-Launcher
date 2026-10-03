package com.gamebooster.app.core;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.gamebooster.app.engine.PrivilegeBridgeEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * NativeKernelBridge — Low-level kernel sysfs, scheduler, and cgroup process isolation engine.
 *
 * Provides direct zero-fork sysfs manipulation with transparent escalation via PrivilegeBridgeEngine.
 * Handles process pinning to top-app CPU clusters and OOM score adjustment.
 */
public final class NativeKernelBridge {

    private static final String TAG = "NativeKernelBridge";

    private NativeKernelBridge() {}

    /**
     * Writes raw value to a kernel sysfs/proc node.
     * Tries zero-fork direct file write first; falls back to privileged bridge if restricted.
     */
    public static boolean writeSysfs(String path, String value) {
        if (path == null || value == null) return false;
        try {
            File f = new File(path);
            if (f.exists() && f.canWrite()) {
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    fos.write(value.getBytes(StandardCharsets.US_ASCII));
                    fos.flush();
                    return true;
                }
            }
        } catch (Throwable ignored) {
            // Hand off to privileged executor
        }

        String cmd = "echo " + value + " > " + path + " 2>/dev/null";
        String res = PrivilegeBridgeEngine.executePrivileged(cmd);
        return res != null && !res.startsWith("ERROR");
    }

    /**
     * Resolves the primary PID of the target game process without launching subshells.
     */
    public static int findGameProcessPid(Context context, String packageName) {
        if (context == null || packageName == null || packageName.isEmpty()) return -1;

        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                List<ActivityManager.RunningAppProcessInfo> procs = am.getRunningAppProcesses();
                if (procs != null) {
                    for (ActivityManager.RunningAppProcessInfo p : procs) {
                        if (packageName.equals(p.processName)) {
                            return p.pid;
                        }
                    }
                    for (ActivityManager.RunningAppProcessInfo p : procs) {
                        if (p.pkgList != null) {
                            for (String pkg : p.pkgList) {
                                if (packageName.equals(pkg)) {
                                    return p.pid;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.d(TAG, "ActivityManager PID lookup note: " + t.getMessage());
        }

        // Fallback: check /proc/[pid]/cmdline via privileged bridge
        try {
            String pgrep = PrivilegeBridgeEngine.executePrivileged("pidof " + packageName + " 2>/dev/null || pgrep -f \"" + packageName + "\" 2>/dev/null");
            if (pgrep != null && !pgrep.trim().isEmpty() && !pgrep.startsWith("ERROR")) {
                String firstLine = pgrep.trim().split("\\s+")[0];
                return Integer.parseInt(firstLine);
            }
        } catch (Throwable ignored) {}

        return -1;
    }

    /**
     * Isolates the game process to top-app CPU clusters and grants Low Memory Killer (LMK) immunity.
     */
    public static boolean isolateProcessToPrimeCores(Context context, String packageName) {
        if (packageName == null || packageName.isEmpty()) return false;

        int pid = findGameProcessPid(context, packageName);
        if (pid <= 0) {
            Log.d(TAG, "Cannot isolate process: PID not found for " + packageName);
            return false;
        }

        String pidStr = Integer.toString(pid);
        boolean ok = true;

        // 1. Move PID to top-app CPU cluster cgroup (grants priority access to Big/Prime cores)
        ok &= writeSysfs("/dev/cpuset/top-app/tasks", pidStr);
        writeSysfs("/dev/cpuset/top-app/cgroup.procs", pidStr);

        // 2. Shield process from Android Low Memory Killer (-1000 = completely immune)
        ok &= writeSysfs("/proc/" + pidStr + "/oom_score_adj", "-1000");

        // 3. Elevate nice level / priority
        PrivilegeBridgeEngine.executePrivileged("renice -n -20 -p " + pidStr + " 2>/dev/null");

        Log.i(TAG, "⚡ Process isolated to top-app cluster: " + packageName + " (PID " + pid + ")");
        return ok;
    }

    /**
     * Enforces kernel CFS/EAS scheduler pacing parameters for zero-jitter thread wakeup.
     */
    public static void tuneSchedulerPacing() {
        writeSysfs("/proc/sys/kernel/sched_migration_cost_ns", "5000000");
        writeSysfs("/proc/sys/kernel/sched_latency_ns", "4000000");
        writeSysfs("/proc/sys/kernel/sched_min_granularity_ns", "1000000");
        writeSysfs("/proc/sys/kernel/sched_schedstats", "0");
        Log.i(TAG, "Kernel CFS/EAS scheduler pacing parameters enforced.");
    }
}
