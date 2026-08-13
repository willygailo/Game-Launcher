package com.gamebooster.app.feature.performance.refreshrate;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.feature.performance.tweaks.OemHardwareOptimizer;
import com.gamebooster.app.feature.performance.tweaks.SetEditSettingsEnforcer;
import com.gamebooster.app.platform.shell.CommandExecutor;

/**
 * RealWorldHzLockEngine — Continuous display refresh rate & frame pacing lock daemon.
 *
 * <p>Prevents LTPO / VRR displays from dropping down to 60Hz/90Hz during touch-idle frames,
 * cutscenes, or thermal throttling events by periodically re-enforcing peak/min refresh rates
 * and SurfaceFlinger lock parameters while a game is active in foreground.</p>
 */
public class RealWorldHzLockEngine {

    private static final String TAG = "RealWorldHzLockEngine";
    private static final long LOCK_PULSE_INTERVAL_MS = 5000; // 5-second pulse interval

    private static RealWorldHzLockEngine instance;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isLockActive = false;
    private int lockedHz = 0;
    private String lockedPackage = null;
    private Runnable lockRunnable;

    private RealWorldHzLockEngine() {}

    public static synchronized RealWorldHzLockEngine getInstance() {
        if (instance == null) {
            instance = new RealWorldHzLockEngine();
        }
        return instance;
    }

    /**
     * Starts continuous real-world refresh rate lock for the target package.
     *
     * @param context Application context.
     * @param targetHz Native panel refresh rate target (e.g., 90, 120, 144, 165).
     * @param packageName Target game package name.
     */
    public synchronized void startLock(Context context, int targetHz, String packageName) {
        if (targetHz <= 0) return;

        this.lockedHz = targetHz;
        this.lockedPackage = packageName;
        this.isLockActive = true;

        Log.i(TAG, "Starting Real-World Hz Lock Engine @ " + targetHz + "Hz for " + (packageName != null ? packageName : "global"));

        // Execute initial immediate lock pulse
        performLockPulse();

        // Setup background pulse loop
        if (lockRunnable != null) {
            mainHandler.removeCallbacks(lockRunnable);
        }

        lockRunnable = new Runnable() {
            @Override
            public void run() {
                if (isLockActive && lockedHz > 0) {
                    performLockPulse();
                    mainHandler.postDelayed(this, LOCK_PULSE_INTERVAL_MS);
                }
            }
        };
        mainHandler.postDelayed(lockRunnable, LOCK_PULSE_INTERVAL_MS);
    }

    /**
     * Executes single high-frequency Hz lock pulse across system properties,
     * SurfaceFlinger IPC, and vendor daemons.
     */
    private void performLockPulse() {
        if (!isLockActive || lockedHz <= 0) return;

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                int hz = lockedHz;
                String hzStr = String.valueOf(hz);
                String hzFloatStr = String.format(java.util.Locale.US, "%.1f", (float) hz);

                // Enforce peak and min refresh rates synchronously to defeat LTPO idle drop
                CommandExecutor.setSystemSetting("system", "peak_refresh_rate", hzFloatStr);
                CommandExecutor.setSystemSetting("system", "min_refresh_rate", hzFloatStr);
                CommandExecutor.setSystemSetting("system", "user_refresh_rate", hzStr);

                CommandExecutor.setSystemSetting("global", "peak_refresh_rate", hzFloatStr);
                CommandExecutor.setSystemSetting("global", "min_refresh_rate", hzFloatStr);
                CommandExecutor.setSystemSetting("global", "user_refresh_rate", hzStr);

                // Disable VRR/LTPO dynamic downscaling
                CommandExecutor.setSystemSetting("secure", "refresh_rate_mode", "2");
                CommandExecutor.setSystemSetting("secure", "match_content_frame_rate", "0");

                // Execute SetEdit settings enforcer & OEM hardware locks
                SetEditSettingsEnforcer.enforceRefreshRate(hz);
                OemHardwareOptimizer.applyOemOptimizations(hz);

                // Re-apply window manager app refresh rate lock
                CommandExecutor.executeSystemCommand("cmd window set-app-refresh-rate global " + hz);
                if (lockedPackage != null && !lockedPackage.isEmpty()) {
                    CommandExecutor.executeSystemCommand("cmd window set-game-refresh-rate " + lockedPackage + " " + hz);
                }

                // SurfaceFlinger FPS override to override display driver 60 FPS cap
                CommandExecutor.setSystemProperty("debug.sf.fps_override", hzStr);

                // Thermal status override to prevent thermal Hz drop
                CommandExecutor.executeSystemCommand("cmd thermalservice override-status 0");
                CommandExecutor.executeSystemCommand("cmd thermal override-status 0");

                Log.d(TAG, "Hz Lock Pulse executed: locked at " + hz + "Hz");
            } catch (Throwable t) {
                Log.e(TAG, "Error executing Hz Lock Pulse", t);
            }
        });
    }

    /**
     * Stops continuous real-world refresh rate locking and restores default behavior.
     */
    public synchronized void stopLock(Context context) {
        if (!isLockActive) return;

        Log.i(TAG, "Stopping Real-World Hz Lock Engine...");
        isLockActive = false;
        lockedHz = 0;
        lockedPackage = null;

        if (lockRunnable != null) {
            mainHandler.removeCallbacks(lockRunnable);
            lockRunnable = null;
        }

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                // Clear FPS override
                CommandExecutor.setSystemProperty("debug.sf.fps_override", "0");
                CommandExecutor.executeSystemCommand("cmd thermalservice override-status -1");
                CommandExecutor.executeSystemCommand("cmd thermal override-status -1");

                if (context != null) {
                    com.gamebooster.app.feature.performance.display.DisplayOverrideController.restore(context);
                }
            } catch (Throwable t) {
                Log.e(TAG, "Error stopping Hz Lock Engine", t);
            }
        });
    }

    public boolean isLockActive() {
        return isLockActive;
    }

    public int getLockedHz() {
        return lockedHz;
    }

    public String getLockedPackage() {
        return lockedPackage;
    }
}
