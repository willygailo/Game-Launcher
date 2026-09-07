package com.gamebooster.app.shizuku;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import rikka.shizuku.Shizuku;

/**
 * ShizukuKeepAliveWatchdog — Active Immunity & Heartbeat Daemon.
 *
 * Prevents Shizuku from automatically turning off or disconnecting during
 * multi-hour gaming sessions by:
 * 1. OOM Shielding: Sets oom_score_adj = -1000 on Shizuku daemon and server processes
 *    (SYSTEM_ADJ immunity from Android Low Memory Killer).
 * 2. Standby & Battery Immunity: Whitelists Shizuku from Doze/deviceidle, forces
 *    standby bucket to ACTIVE, and enables background/auto-start AppOps.
 * 3. Phantom Process Killer Bypass: Raises max_phantom_processes to 2 Billion and
 *    disables Android 12–16 phantom monitoring and cached apps freezer.
 * 4. Wireless Debugging Keep-Alive: Locks Wi-Fi sleep policy and disables ADB timeouts.
 * 5. Persistent Heartbeat: Periodically validates binder liveness and triggers
 *    immediate reconnection/re-binding if dropped.
 */
public class ShizukuKeepAliveWatchdog {

    private static final String TAG = "ShizukuWatchdog";
    private static final ShizukuKeepAliveWatchdog INSTANCE = new ShizukuKeepAliveWatchdog();

    public static final String SHIZUKU_PKG = "moe.shizuku.privileged.api";
    private static final long HEARTBEAT_INTERVAL_MS = 3_000L; // Fast 3-second health check
    private static final long FAST_CHECK_INTERVAL_MS = 1_500L;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isShieldingActive = new AtomicBoolean(false);

    private HandlerThread watchdogThread;
    private Handler watchdogHandler;
    private Runnable heartbeatRunnable;

    private volatile int userServicePid = -1;
    private volatile long lastFullShieldTimeMs = 0L;
    private static final long FULL_SHIELD_COOLDOWN_MS = 30_000L; // Full shield refreshed every 30s

    private android.net.wifi.WifiManager.WifiLock wifiLock;
    private android.os.PowerManager.WakeLock wakeLock;

    private ShizukuKeepAliveWatchdog() {}

    public static ShizukuKeepAliveWatchdog getInstance() {
        return INSTANCE;
    }

    public boolean isWatchdogActive() {
        return isRunning.get();
    }

    public void onUserServiceConnected(int pid) {
        this.userServicePid = pid;
        if (pid > 0) {
            AppExecutors.getInstance().executeCommand(() -> {
                try {
                    String cmd = "echo -1000 > /proc/" + pid + "/oom_score_adj 2>/dev/null || echo 0 > /proc/" + pid + "/oom_score_adj 2>/dev/null; " +
                            "renice -n -20 -p " + pid + " 2>/dev/null";
                    ShizukuExecutor.executeShizukuCommand(cmd);
                    Log.i(TAG, "Applied immediate LMK immunity to UserService PID=" + pid);
                } catch (Throwable ignored) {}
            });
        }
    }

    /**
     * Starts the keep-alive watchdog thread and periodic heartbeat.
     * Guaranteed safe to call repeatedly (idempotent).
     */
    public synchronized void startWatchdog(Context context) {
        acquireLocks(context);

        if (isRunning.compareAndSet(false, true)) {
            Log.i(TAG, "Starting Shizuku Keep-Alive Watchdog daemon...");

            watchdogThread = new HandlerThread("ShizukuWatchdogThread", Process.THREAD_PRIORITY_BACKGROUND);
            watchdogThread.start();
            watchdogHandler = new Handler(watchdogThread.getLooper());

            heartbeatRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!isRunning.get()) return;
                    performHeartbeatCheck();
                    if (watchdogHandler != null && isRunning.get()) {
                        watchdogHandler.postDelayed(this, HEARTBEAT_INTERVAL_MS);
                    }
                }
            };

            // Immediate initial run
            watchdogHandler.post(heartbeatRunnable);

            // If Shizuku is already ready, enforce immunity right away
            if (ShizukuManager.isShizukuRunningAndGranted()) {
                applyImmunityShieldAsync();
            }
        }
    }

    private synchronized void acquireLocks(Context context) {
        if (context == null) return;
        Context appCtx = context.getApplicationContext();
        try {
            if (wifiLock == null) {
                android.net.wifi.WifiManager wm = (android.net.wifi.WifiManager) appCtx.getSystemService(Context.WIFI_SERVICE);
                if (wm != null) {
                    wifiLock = wm.createWifiLock(android.net.wifi.WifiManager.WIFI_MODE_FULL_LOW_LATENCY, "GameBooster:ShizukuWifiLock");
                    wifiLock.setReferenceCounted(false);
                }
            }
            if (wifiLock != null && !wifiLock.isHeld()) {
                wifiLock.acquire();
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to acquire WifiLock in Watchdog: " + t.getMessage());
        }

        try {
            if (wakeLock == null) {
                android.os.PowerManager pm = (android.os.PowerManager) appCtx.getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    wakeLock = pm.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "GameBooster:ShizukuWakeLock");
                    wakeLock.setReferenceCounted(false);
                }
            }
            if (wakeLock != null && !wakeLock.isHeld()) {
                wakeLock.acquire(12 * 60 * 60 * 1000L); // 12-hour ceiling
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to acquire WakeLock in Watchdog: " + t.getMessage());
        }
    }

    /**
     * Stops the keep-alive watchdog daemon.
     */
    public synchronized void stopWatchdog() {
        if (isRunning.compareAndSet(true, false)) {
            Log.i(TAG, "Stopping Shizuku Keep-Alive Watchdog daemon.");
            if (watchdogHandler != null && heartbeatRunnable != null) {
                watchdogHandler.removeCallbacks(heartbeatRunnable);
            }
            if (watchdogThread != null) {
                watchdogThread.quitSafely();
                watchdogThread = null;
            }
            watchdogHandler = null;
            heartbeatRunnable = null;

            if (wifiLock != null && wifiLock.isHeld()) {
                try { wifiLock.release(); } catch (Throwable ignored) {}
            }
            if (wakeLock != null && wakeLock.isHeld()) {
                try { wakeLock.release(); } catch (Throwable ignored) {}
            }
        }
    }

    /**
     * Hook called immediately when Shizuku connection state becomes READY.
     */
    public void onShizukuConnected() {
        Log.i(TAG, "onShizukuConnected: Triggering full immunity shield and acquiring locks");
        android.content.Context ctx = com.gamebooster.app.GameBoosterApp.getInstance();
        if (ctx != null) {
            acquireLocks(ctx);
        }
        applyImmunityShieldAsync();
    }

    /**
     * Asynchronously executes full immunity shield commands via worker executor.
     */
    public void applyImmunityShieldAsync() {
        AppExecutors.getInstance().executeCommand(this::applyImmunityShieldInternal);
    }

    /**
     * Full system-level immunity enforcement.
     */
    public void applyImmunityShieldInternal() {
        if (!ShizukuExecutor.hasShizukuPermission()) {
            Log.d(TAG, "Cannot apply immunity shield: Shizuku not permitted or running");
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastFullShieldTimeMs < FULL_SHIELD_COOLDOWN_MS) {
            // Re-apply only OOM score without repeating heavy settings
            refreshOomScoreOnly();
            return;
        }
        lastFullShieldTimeMs = now;

        try {
            Log.i(TAG, "Applying complete anti-kill immunity shield for Shizuku & Game Booster...");
            List<String> shieldCmds = buildImmunityCommands();
            ShizukuExecutor.executeShizukuCommands(shieldCmds);
            isShieldingActive.set(true);
            Log.i(TAG, "Shizuku immunity shield applied successfully! (OOM -1000, PPK bypass, Doze whitelist, Wi-Fi lock)");
        } catch (Throwable t) {
            Log.w(TAG, "Failed to apply full immunity shield: " + t.getMessage());
        }
    }

    /**
     * Compiles all shell commands needed to shield Shizuku from Android system kills.
     */
    public List<String> buildImmunityCommands() {
        List<String> cmds = new ArrayList<>();

        // ─── 1. OOM Score Adjustment (-1000 = SYSTEM_ADJ, completely immune to LMK) ───
        // Target Shizuku main package process
        cmds.add("for p in $(pidof " + SHIZUKU_PKG + " 2>/dev/null); do " +
                "echo -1000 > /proc/$p/oom_score_adj 2>/dev/null || echo 0 > /proc/$p/oom_score_adj 2>/dev/null; " +
                "renice -n -20 -p $p 2>/dev/null; done");

        // Target Shizuku standalone server / starter daemons spawned by ADB
        cmds.add("for p in $(pgrep -f 'shizuku_server' 2>/dev/null); do " +
                "echo -1000 > /proc/$p/oom_score_adj 2>/dev/null || echo 0 > /proc/$p/oom_score_adj 2>/dev/null; " +
                "renice -n -20 -p $p 2>/dev/null; done");
        cmds.add("for p in $(pgrep -f 'shizuku_starter' 2>/dev/null); do " +
                "echo -1000 > /proc/$p/oom_score_adj 2>/dev/null || echo 0 > /proc/$p/oom_score_adj 2>/dev/null; " +
                "renice -n -20 -p $p 2>/dev/null; done");
        cmds.add("for p in $(pgrep -f 'moe.shizuku' 2>/dev/null); do " +
                "echo -1000 > /proc/$p/oom_score_adj 2>/dev/null || echo 0 > /proc/$p/oom_score_adj 2>/dev/null; " +
                "renice -n -20 -p $p 2>/dev/null; done");

        // Target privileged secondary UserService daemon process (:service)
        cmds.add("for p in $(pgrep -f 'com.gamebooster.app:service' 2>/dev/null); do " +
                "echo -1000 > /proc/$p/oom_score_adj 2>/dev/null || echo 0 > /proc/$p/oom_score_adj 2>/dev/null; " +
                "renice -n -20 -p $p 2>/dev/null; done");

        int uPid = userServicePid;
        if (uPid > 0) {
            cmds.add("echo -1000 > /proc/" + uPid + "/oom_score_adj 2>/dev/null || echo 0 > /proc/" + uPid + "/oom_score_adj 2>/dev/null; " +
                    "renice -n -20 -p " + uPid + " 2>/dev/null");
        }

        // Protect our own Game Booster launcher process from LMK
        try {
            int myPid = Process.myPid();
            cmds.add("echo -1000 > /proc/" + myPid + "/oom_score_adj 2>/dev/null || echo 0 > /proc/" + myPid + "/oom_score_adj 2>/dev/null");
            cmds.add("renice -n -20 -p " + myPid + " 2>/dev/null");
        } catch (Throwable ignored) {}

        // ─── 2. Battery Optimization, Doze Mode & Standby Bucket Immunity ───
        // Whitelist both Shizuku and Game Booster from Doze mode (deviceidle)
        cmds.add("dumpsys deviceidle whitelist +" + SHIZUKU_PKG + " 2>/dev/null; cmd deviceidle whitelist +" + SHIZUKU_PKG + " 2>/dev/null");
        cmds.add("dumpsys deviceidle whitelist +com.gamebooster.app 2>/dev/null; cmd deviceidle whitelist +com.gamebooster.app 2>/dev/null");

        // Pin both App Standby Buckets to ACTIVE (Bucket 10) so Android never treats them as RARE/RESTRICTED
        cmds.add("am set-standby-bucket " + SHIZUKU_PKG + " active 2>/dev/null; cmd activity set-standby-bucket " + SHIZUKU_PKG + " active 2>/dev/null");
        cmds.add("am set-standby-bucket com.gamebooster.app active 2>/dev/null; cmd activity set-standby-bucket com.gamebooster.app active 2>/dev/null");

        // Grant continuous background execution & auto-start AppOps (bypasses Xiaomi/HyperOS, Samsung, ColorOS)
        cmds.add("cmd appops set " + SHIZUKU_PKG + " RUN_IN_BACKGROUND allow 2>/dev/null");
        cmds.add("cmd appops set " + SHIZUKU_PKG + " RUN_ANY_IN_BACKGROUND allow 2>/dev/null");
        cmds.add("cmd appops set " + SHIZUKU_PKG + " START_FOREGROUND allow 2>/dev/null");
        cmds.add("cmd appops set " + SHIZUKU_PKG + " AUTO_START allow 2>/dev/null");
        cmds.add("cmd appops set " + SHIZUKU_PKG + " SYSTEM_ALERT_WINDOW allow 2>/dev/null");

        cmds.add("cmd appops set com.gamebooster.app RUN_IN_BACKGROUND allow 2>/dev/null");
        cmds.add("cmd appops set com.gamebooster.app RUN_ANY_IN_BACKGROUND allow 2>/dev/null");
        cmds.add("cmd appops set com.gamebooster.app START_FOREGROUND allow 2>/dev/null");
        cmds.add("cmd appops set com.gamebooster.app AUTO_START allow 2>/dev/null");
        cmds.add("cmd appops set com.gamebooster.app WAKE_LOCK allow 2>/dev/null");

        // Whitelist from network policy background restrictions
        cmds.add("cmd netpolicy add restrict-background-whitelist " + SHIZUKU_PKG + " 2>/dev/null");
        cmds.add("cmd netpolicy add restrict-background-whitelist com.gamebooster.app 2>/dev/null");
        cmds.add("cmd netpolicy add restrict-background-whitelist com.android.shell 2>/dev/null");

        // ─── 3. Android 12–16 Phantom Process Killer & Cached Apps Freezer Bypass ───
        cmds.add("device_config set_sync_disabled_for_tests persistent 2>/dev/null");
        cmds.add("device_config put activity_manager max_phantom_processes 2147483647 2>/dev/null");
        cmds.add("settings put global settings_enable_monitor_phantom_procs false 2>/dev/null");
        cmds.add("setprop persist.sys.fflag.override.settings_enable_monitor_phantom_procs false 2>/dev/null");
        cmds.add("cmd device_config put activity_manager freeze_debounce_timeout 86400000 2>/dev/null");
        cmds.add("settings put global cached_apps_freezer disabled 2>/dev/null");
        cmds.add("cmd power set-mode 0 1 2>/dev/null");

        // ─── 4. Wireless Debugging & Wi-Fi Power Saving Keep-Alive ───
        cmds.add("settings put global adb_wifi_enabled 1 2>/dev/null; " +
                "settings put global wifi_sleep_policy 2 2>/dev/null; " +
                "cmd settings put global adb_allowed_connection_time 0 2>/dev/null; " +
                "settings put global adb_authorization_timeout 0 2>/dev/null; " +
                "setprop persist.adb.wifi 1 2>/dev/null; " +
                "setprop persist.adb.nonblocking_ffs 0 2>/dev/null; " +
                "setprop persist.sys.usb.config adb 2>/dev/null; " +
                "settings put global wifi_wakeup_available 1 2>/dev/null; " +
                "settings put global wifi_wakeup_enabled 1 2>/dev/null");

        // ─── 5. Auto-Resurrect Fallback (Root & Storage Starter) ───
        cmds.add("if ! pgrep -f 'shizuku_server' >/dev/null 2>&1; then " +
                "if [ -x /data/adb/shizuku/shizuku_starter ]; then /data/adb/shizuku/shizuku_starter & fi; " +
                "if [ -f /sdcard/Android/data/moe.shizuku.privileged.api/starter ]; then sh /sdcard/Android/data/moe.shizuku.privileged.api/starter & fi; " +
                "if [ -f /storage/emulated/0/Android/data/moe.shizuku.privileged.api/starter ]; then sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/starter & fi; " +
                "fi 2>/dev/null");

        return cmds;
    }

    /**
     * Fast, low-overhead OOM score refresh targeting Shizuku PIDs and UserService daemon.
     */
    private void refreshOomScoreOnly() {
        try {
            int uPid = userServicePid;
            int myPid = Process.myPid();
            String extra = (uPid > 0) ? ("echo -1000 > /proc/" + uPid + "/oom_score_adj 2>/dev/null || echo 0 > /proc/" + uPid + "/oom_score_adj 2>/dev/null; renice -n -20 -p " + uPid + " 2>/dev/null; ") : "";
            String myPidCmd = (myPid > 0) ? ("echo -1000 > /proc/" + myPid + "/oom_score_adj 2>/dev/null || echo 0 > /proc/" + myPid + "/oom_score_adj 2>/dev/null; renice -n -20 -p " + myPid + " 2>/dev/null; ") : "";
            String oomCmd = "for p in $(pidof " + SHIZUKU_PKG + " 2>/dev/null); do echo -1000 > /proc/$p/oom_score_adj 2>/dev/null || echo 0 > /proc/$p/oom_score_adj 2>/dev/null; renice -n -20 -p $p 2>/dev/null; done; " +
                    "for p in $(pgrep -f 'shizuku_server' 2>/dev/null); do echo -1000 > /proc/$p/oom_score_adj 2>/dev/null || echo 0 > /proc/$p/oom_score_adj 2>/dev/null; renice -n -20 -p $p 2>/dev/null; done; " +
                    "for p in $(pgrep -f 'shizuku_starter' 2>/dev/null); do echo -1000 > /proc/$p/oom_score_adj 2>/dev/null || echo 0 > /proc/$p/oom_score_adj 2>/dev/null; renice -n -20 -p $p 2>/dev/null; done; " +
                    "for p in $(pgrep -f 'moe.shizuku' 2>/dev/null); do echo -1000 > /proc/$p/oom_score_adj 2>/dev/null || echo 0 > /proc/$p/oom_score_adj 2>/dev/null; renice -n -20 -p $p 2>/dev/null; done; " +
                    "for p in $(pgrep -f 'com.gamebooster.app:service' 2>/dev/null); do echo -1000 > /proc/$p/oom_score_adj 2>/dev/null || echo 0 > /proc/$p/oom_score_adj 2>/dev/null; renice -n -20 -p $p 2>/dev/null; done; " +
                    extra + myPidCmd;
            ShizukuExecutor.executeShizukuCommand(oomCmd);
        } catch (Throwable ignored) {}
    }

    /**
     * Fast heartbeat execution running every 3s.
     * Verifies binder liveness, re-establishes dropped connections, and keeps UserService bound.
     */
    private void performHeartbeatCheck() {
        try {
            boolean binderAlive = false;
            try {
                binderAlive = Shizuku.pingBinder();
            } catch (Throwable t) {
                binderAlive = false;
            }

            if (!binderAlive) {
                // Active multi-stage recovery check before declaring dead: re-fetch from provider
                android.content.Context ctx = com.gamebooster.app.GameBoosterApp.getInstance();
                for (int i = 0; i < 3 && !binderAlive; i++) {
                    if (ctx != null) {
                        ShizukuManager.activelyFetchAndAttachBinder(ctx);
                        binderAlive = Shizuku.pingBinder();
                    }
                    if (!binderAlive) {
                        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                    }
                }
            }

            if (binderAlive) {
                // Shizuku core is alive!
                // 1. Ensure state is READY
                if (ShizukuConnectionManager.getInstance().getState() != ShizukuConnectionManager.State.READY) {
                    ShizukuConnectionManager.getInstance().onBinderReceived();
                }

                // 2. Ensure AIDL UserService is bound and responsive
                if (!ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                    Log.d(TAG, "Heartbeat: AIDL UserService dropped — auto-re-binding...");
                    ShizukuUserServiceConnector.getInstance().bindService();
                }

                // 3. Keep-alive OOM refresh
                refreshOomScoreOnly();
            } else {
                // Binder is genuinely not responding after active recovery
                Log.w(TAG, "Heartbeat: Shizuku binder not responding — attempting active auto-resurrection");
                try {
                    if (com.gamebooster.app.engine.ShellExecutor.isRootSuAvailable()) {
                        com.gamebooster.app.engine.ShellExecutor.executeCommand(
                                "if [ -x /data/adb/shizuku/shizuku_starter ]; then /data/adb/shizuku/shizuku_starter & fi; " +
                                "if [ -f /sdcard/Android/data/moe.shizuku.privileged.api/starter ]; then sh /sdcard/Android/data/moe.shizuku.privileged.api/starter & fi");
                    }
                } catch (Throwable ignored) {}
                ShizukuConnectionManager.getInstance().onBinderDead();
            }
        } catch (Throwable t) {
            Log.e(TAG, "Heartbeat check error: " + t.getMessage());
        }
    }
}
