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
 * <p>Prevents LTPO / VRR displays from dropping down to 60Hz/90Hz/120Hz during touch-idle frames,
 * cutscenes, or thermal throttling events by periodically re-enforcing peak/min refresh rates,
 * SurfaceFlinger lock parameters, and GPU composition pipeline while a game is active in foreground.</p>
 */
public class RealWorldHzLockEngine {

    private static final String TAG = "RealWorldHzLockEngine";
    private static final long LOCK_PULSE_INTERVAL_MS = 1500; // 1.5-second ultra-responsive pulse interval

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

        Log.i(TAG, "Starting Ultra-Stable Real-World Hz Lock Engine @ " + targetHz + "Hz for " + (packageName != null ? packageName : "global"));

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

                // 1. Enforce peak, min & user refresh rates across System, Global & Secure namespaces
                CommandExecutor.setSystemSetting("system", "peak_refresh_rate", hzFloatStr);
                CommandExecutor.setSystemSetting("system", "min_refresh_rate", hzFloatStr);
                CommandExecutor.setSystemSetting("system", "user_refresh_rate", hzStr);
                CommandExecutor.setSystemSetting("system", "default_refresh_rate", hzFloatStr);

                CommandExecutor.setSystemSetting("global", "peak_refresh_rate", hzFloatStr);
                CommandExecutor.setSystemSetting("global", "min_refresh_rate", hzFloatStr);
                CommandExecutor.setSystemSetting("global", "user_refresh_rate", hzStr);
                CommandExecutor.setSystemSetting("global", "default_refresh_rate", hzFloatStr);
                CommandExecutor.setSystemSetting("global", "sf_max_fps", hzStr);
                CommandExecutor.setSystemSetting("global", "mode_fps_override", hzStr);
                CommandExecutor.setSystemSetting("global", "fps_limit", "0");
                CommandExecutor.setSystemSetting("global", "display_downscale_disable", "1");

                CommandExecutor.setSystemSetting("secure", "peak_refresh_rate", hzFloatStr);
                CommandExecutor.setSystemSetting("secure", "min_refresh_rate", hzFloatStr);
                CommandExecutor.setSystemSetting("secure", "user_refresh_rate", hzStr);
                CommandExecutor.setSystemSetting("secure", "refresh_rate_mode", "2");
                CommandExecutor.setSystemSetting("secure", "match_content_frame_rate", "0");

                // 2. Direct SurfaceFlinger Mode Binder Calls
                CommandExecutor.executeSystemCommand("service call SurfaceFlinger 1035 i32 " + hz);
                CommandExecutor.executeSystemCommand("service call SurfaceFlinger 1036 i32 " + hz);
                CommandExecutor.executeSystemCommand("service call SurfaceFlinger 1037 i32 " + hz);

                // 3. GPU Hardware Composition & Frame Uncapping
                CommandExecutor.setSystemProperty("debug.composition.type", "gpu");
                CommandExecutor.setSystemProperty("persist.sys.composition.type", "gpu");
                CommandExecutor.setSystemProperty("debug.egl.hw", "1");
                CommandExecutor.setSystemProperty("debug.sf.hw", "1");
                CommandExecutor.setSystemProperty("vendor.gpu.boost", "1");
                CommandExecutor.setSystemProperty("debug.sf.fps_override", hzStr);
                CommandExecutor.setSystemProperty("debug.sf.latch_unsignaled", "1");
                CommandExecutor.setSystemProperty("debug.sf.enable_gl_backpressure", "0");
                CommandExecutor.setSystemProperty("debug.sf.disable_backpressure", "1");

                // 4. PowerHAL & Thermal Override to prevent thermal throttling Hz drop
                CommandExecutor.executeSystemCommand("cmd power set-mode 0 1");
                CommandExecutor.executeSystemCommand("cmd power set-mode 2 1");
                CommandExecutor.executeSystemCommand("cmd thermalservice override-status 0");
                CommandExecutor.executeSystemCommand("cmd thermal override-status 0");

                // 5. Execute SetEdit settings enforcer & OEM hardware locks with package context
                SetEditSettingsEnforcer.enforceRefreshRate(hz, lockedPackage);
                OemHardwareOptimizer.applyOemOptimizations(hz, lockedPackage);

                // 6. Window Manager & Game Mode App Refresh Rate Lock
                if (lockedPackage != null && !lockedPackage.trim().isEmpty() && !"global".equalsIgnoreCase(lockedPackage.trim())) {
                    CommandExecutor.executeSystemCommand("cmd window set-app-refresh-rate " + lockedPackage + " " + hz);
                    CommandExecutor.executeSystemCommand("cmd game set --mode 2 --fps " + hz + " " + lockedPackage);
                    CommandExecutor.setSystemSetting("secure", "high_refresh_rate_apps_list", lockedPackage);
                }

                Log.d(TAG, "Ultra-Stable Hz Lock Pulse executed: locked at " + hz + "Hz for " + (lockedPackage != null ? lockedPackage : "global"));
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
