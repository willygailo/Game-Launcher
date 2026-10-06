package com.gamebooster.app.booster;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.engine.PrivilegeBridgeEngine;

/**
 * SafetyGuard — runtime thermal watchdog for boost sessions.
 *
 * IMPROVEMENT_PLAN.md §8.3 / §13: while aggressive tweaks are armed, poll
 * thermal zones every 2s. At >=50C back the GPU floor off 10%; at >=55C
 * restore the pre-boost snapshot and disarm. Also carries the §8.2 device
 * compatibility blocklist (severity WARN logs + notifies, BLOCK refuses to arm).
 */
@android.annotation.SuppressLint("ObsoleteSdkInt")
public final class SafetyGuard {

    private static final String TAG = "SafetyGuard";

    public static final int STAGE_OK = 0;
    public static final int STAGE_WARN = 1;
    public static final int STAGE_CRITICAL = 2;

    static final int WARN_MILLIC = 50_000;
    static final int CRITICAL_MILLIC = 55_000;
    static final long POLL_INTERVAL_MS = 2_000L;

    private static final Object LOCK = new Object();

    public interface Listener {
        void onStage(int stage, int milliCelsius, String action);
    }

    public static final class DeviceBlock {
        public final String reason;
        public final boolean blocked;

        DeviceBlock(String reason, boolean blocked) {
            this.reason = reason;
            this.blocked = blocked;
        }
    }

    private static volatile boolean armed = false;
    private static volatile Thread monitor = null;
    private static volatile Listener listener = null;
    private static Context appContext = null;

    private SafetyGuard() {}

    public static void setListener(Listener l) {
        listener = l;
    }

    static int evaluateStage(int milliCelsius) {
        if (milliCelsius >= CRITICAL_MILLIC) return STAGE_CRITICAL;
        if (milliCelsius >= WARN_MILLIC) return STAGE_WARN;
        return STAGE_OK;
    }

    static int parseMaxMilliC(String shellOutput) {
        if (shellOutput == null) return -1;
        int max = -1;
        for (String line : shellOutput.split("\n")) {
            String t = line.trim();
            if (t.isEmpty()) continue;
            try {
                int v = Integer.parseInt(t);
                if (v > max) max = v;
            } catch (NumberFormatException ignored) {
            }
        }
        return max;
    }

    /**
     * IMPROVEMENT_PLAN.md §8.2 known-problematic device table.
     * WARN: arm anyway but notify. BLOCK: refuse to arm aggressive boost.
     */
    static DeviceBlock findBlock(String manufacturer, String model) {
        if (manufacturer == null || model == null) return null;
        String mfr = manufacturer.toLowerCase();
        String mdl = model.toLowerCase();

        if (mfr.equals("google") && (mdl.startsWith("pixel 7") || mdl.startsWith("pixel 8"))) {
            return new DeviceBlock(
                    "Known WebView Vulkan/Graphite black-screen risk on Pixel 7/8 — use Budget WebView tier",
                    false);
        }
        if (mfr.equals("samsung") && (mdl.startsWith("sm-s901b") || mdl.startsWith("sm-s906b")
                || mdl.startsWith("sm-s908b"))) {
            return new DeviceBlock(
                    "Known VRR-disable display flicker on Exynos S22 variants — refresh overrides disabled",
                    false);
        }
        if (mfr.equals("xiaomi") || mfr.equals("redmi") || mfr.equals("poco")) {
            return new DeviceBlock(
                    "Xiaomi/HyperOS: refresh-rate pair validated strictly before restore (bootloop guard)",
                    false);
        }
        return null;
    }

    public static boolean isArmed() {
        return armed;
    }

    private static volatile android.os.PowerManager.OnThermalStatusChangedListener thermalStatusListener = null;

    static int readMaxMilliCDirect() {
        int max = -1;
        try {
            java.io.File thermalDir = new java.io.File("/sys/class/thermal");
            if (thermalDir.exists() && thermalDir.isDirectory()) {
                java.io.File[] files = thermalDir.listFiles((dir, name) -> name.startsWith("thermal_zone"));
                if (files != null) {
                    for (java.io.File zone : files) {
                        java.io.File tempFile = new java.io.File(zone, "temp");
                        if (tempFile.exists() && tempFile.canRead()) {
                            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(tempFile))) {
                                String line = br.readLine();
                                if (line != null) {
                                    int v = Integer.parseInt(line.trim());
                                    if (v > max) max = v;
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
        return max;
    }

    public static boolean ensureArmed(Context context) {
        synchronized (LOCK) {
            if (armed) return true;

            DeviceBlock block = findBlock(Build.MANUFACTURER, Build.MODEL);
            if (block != null && block.blocked) {
                Log.w(TAG, "Refusing to arm on " + Build.MANUFACTURER + " " + Build.MODEL
                        + ": " + block.reason);
                return false;
            }
            if (block != null) {
                notifyListener(STAGE_OK, 0, "Compatibility warning: " + block.reason);
            }

            if (context == null) return false;
            if (!PrivilegeBridgeEngine.isPrivilegedActive()) {
                Log.w(TAG, "Refusing to arm: no active Root/Shizuku privilege");
                return false;
            }

            appContext = context.getApplicationContext();
            SnapshotSystem.Snapshot snapshot = SnapshotSystem.capture("pre-boost");
            if (snapshot.entries.isEmpty()) {
                Log.w(TAG, "Refusing to arm: snapshot captured no entries");
                return false;
            }
            if (!SnapshotSystem.save(appContext, snapshot)) {
                Log.w(TAG, "Refusing to arm: snapshot could not be persisted");
                return false;
            }

            // Register Android Framework native thermal status callback (API 29+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && thermalStatusListener == null) {
                try {
                    android.os.PowerManager pm = (android.os.PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    if (pm != null) {
                        thermalStatusListener = status -> {
                            if (!armed) return;
                            if (status >= android.os.PowerManager.THERMAL_STATUS_CRITICAL) {
                                Log.w(TAG, "Native ThermalStatus CRITICAL/EMERGENCY -> Disarming boost to protect hardware");
                                stop(true);
                                notifyListener(STAGE_CRITICAL, 55000, "Native PowerManager status CRITICAL: boost disabled, snapshot restored");
                            } else if (status >= android.os.PowerManager.THERMAL_STATUS_SEVERE) {
                                reduceGpuFloorPercent(90);
                                Log.w(TAG, "Native ThermalStatus SEVERE -> GPU floor reduced 10%");
                                notifyListener(STAGE_WARN, 50000, "Native PowerManager status SEVERE: GPU floor reduced 10%");
                            }
                        };
                        pm.addThermalStatusListener(thermalStatusListener);
                    }
                } catch (Throwable ignored) {}
            }

            armed = true;
            Thread t = new Thread(() -> monitorLoop(), "safety-guard");
            t.setDaemon(true);
            monitor = t;
            t.start();
            Log.i(TAG, "Armed: " + snapshot.entries.size() + " state entries snapshotted, polling every "
                    + POLL_INTERVAL_MS + "ms");
            return true;
        }
    }

    public static void stop(boolean restoreSnapshot) {
        Thread t;
        synchronized (LOCK) {
            armed = false;
            t = monitor;
            monitor = null;
            if (t != null) t.interrupt();
        }
        if (thermalStatusListener != null && appContext != null) {
            try {
                android.os.PowerManager pm = (android.os.PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    pm.removeThermalStatusListener(thermalStatusListener);
                }
            } catch (Throwable ignored) {}
            thermalStatusListener = null;
        }
        if (restoreSnapshot && appContext != null) {
            SnapshotSystem.restoreLatest(appContext);
            notifyListener(STAGE_OK, 0, "Pre-boost snapshot restored");
        }
        Log.i(TAG, "Disarmed (restore=" + restoreSnapshot + ")");
    }

    private static void monitorLoop() {
        int lastStage = STAGE_OK;
        while (armed) {
            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                return;
            }
            if (!armed) return;

            try {
                // Tier 1: Direct sysfs file reading (0ms, 0 process forks, 0 context-switch lag)
                int milli = readMaxMilliCDirect();
                // Tier 2: Shell fallback only if direct sysfs read was unreadable
                if (milli < 0) {
                    String out = CommandExecutor.executeSystemCommand(
                            "cat /sys/class/thermal/thermal_zone*/temp 2>/dev/null");
                    milli = parseMaxMilliC(out);
                }
                if (milli < 0) continue;

                int stage = evaluateStage(milli);
                if (stage == STAGE_CRITICAL) {
                    Log.w(TAG, "CRITICAL thermal stage at " + (milli / 1000.0)
                            + "C — restoring snapshot and disarming");
                    stop(true);
                    notifyListener(STAGE_CRITICAL, milli,
                            "Temperature " + (milli / 1000.0) + "C >= 55C: boost disabled, system restored");
                    return;
                }
                if (stage == STAGE_WARN && lastStage != STAGE_WARN) {
                    reduceGpuFloorPercent(90);
                    Log.w(TAG, "WARN thermal stage at " + (milli / 1000.0) + "C — GPU floor reduced 10%");
                    notifyListener(STAGE_WARN, milli,
                            "Temperature " + (milli / 1000.0) + "C >= 50C: GPU floor reduced 10%");
                }
                lastStage = stage;
            } catch (Throwable t) {
                Log.w(TAG, "monitor iteration failed: " + t.getMessage());
            }
        }
    }

    private static void reduceGpuFloorPercent(int percentOfMax) {
        CommandExecutor.executeSystemCommand(
                "for d in /sys/class/devfreq/*gpu* /sys/class/devfreq/*kgsl* /sys/class/devfreq/*mali*; do "
                        + "max=$(cat \"$d/max_freq\" 2>/dev/null); "
                        + "if [ -n \"$max\" ]; then echo $((max * " + percentOfMax + " / 100)) > \"$d/min_freq\" 2>/dev/null; fi; "
                        + "done");
    }

    private static void notifyListener(int stage, int milliCelsius, String action) {
        Listener l = listener;
        if (l != null) {
            try {
                l.onStage(stage, milliCelsius, action);
            } catch (Throwable t) {
                Log.w(TAG, "listener threw: " + t.getMessage());
            }
        } else {
            Log.i(TAG, "[" + stage + "] " + action);
        }
    }
}
