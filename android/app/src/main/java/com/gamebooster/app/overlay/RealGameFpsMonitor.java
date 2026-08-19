package com.gamebooster.app.overlay;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;
import android.view.Choreographer;

import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RealGameFpsMonitor — Real-time high-fidelity game FPS & frame-time latency telemetry.
 *
 * Architecture:
 * 1. Primary Privileged Tier (Shizuku UID 2000):
 *    Queries SurfaceFlinger hardware display frame queues (`dumpsys SurfaceFlinger --latency`)
 *    and parses actual hardware present timestamps to compute true in-game FPS, frame pacing,
 *    and 1% low frame drops.
 * 2. Secondary Unprivileged Fallback:
 *    Uses Choreographer VSYNC callback with window rate synchronization.
 */
public class RealGameFpsMonitor {

    private static final String TAG = "RealGameFpsMonitor";
    private static final long SAMPLE_INTERVAL_MS = 650;

    public interface FpsUpdateListener {
        void onFpsUpdated(int currentFps, int onePercentLowFps, boolean isRealGameSurface);
    }

    private static volatile RealGameFpsMonitor instance;

    private final Object lock = new Object();
    private boolean isRunning = false;
    private String targetPackage;
    private FpsUpdateListener listener;

    private HandlerThread monitorThread;
    private Handler monitorHandler;

    // VSYNC Choreographer fallback state
    private int fallbackFps = 60;
    private long lastFallbackCalcTimeNanos = 0;
    private int fallbackFrameCount = 0;

    private RealGameFpsMonitor() {}

    public static RealGameFpsMonitor getInstance() {
        if (instance == null) {
            synchronized (RealGameFpsMonitor.class) {
                if (instance == null) {
                    instance = new RealGameFpsMonitor();
                }
            }
        }
        return instance;
    }

    public void setTargetPackage(String packageName) {
        synchronized (lock) {
            this.targetPackage = packageName;
        }
    }

    public void start(Context context, FpsUpdateListener listener) {
        synchronized (lock) {
            if (isRunning) {
                this.listener = listener;
                return;
            }
            this.listener = listener;
            this.isRunning = true;

            monitorThread = new HandlerThread("GB_FpsTelemetry");
            monitorThread.start();
            monitorHandler = new Handler(monitorThread.getLooper());

            monitorHandler.post(sampleRunnable);
            Log.i(TAG, "RealGameFpsMonitor started.");
        }
    }

    public void stop() {
        synchronized (lock) {
            if (!isRunning) return;
            isRunning = false;
            if (monitorHandler != null) {
                monitorHandler.removeCallbacksAndMessages(null);
            }
            if (monitorThread != null) {
                monitorThread.quitSafely();
                monitorThread = null;
            }
            monitorHandler = null;
            listener = null;
            Log.i(TAG, "RealGameFpsMonitor stopped.");
        }
    }

    private final Runnable sampleRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;

            int computedFps = -1;
            int computed1PercentLow = -1;
            boolean isRealSurface = false;

            // Tier 1: Try SurfaceFlinger Latency query via Shizuku
            if (ShizukuExecutor.hasShizukuPermission()) {
                try {
                    String pkg;
                    synchronized (lock) {
                        pkg = targetPackage;
                    }
                    FpsStats stats = querySurfaceFlingerFps(pkg);
                    if (stats != null && stats.fps > 0) {
                        computedFps = stats.fps;
                        computed1PercentLow = stats.onePercentLow;
                        isRealSurface = true;
                    }
                } catch (Throwable t) {
                    Log.v(TAG, "SurfaceFlinger FPS sample warning: " + t.getMessage());
                }
            }

            // Tier 2: Fallback when SurfaceFlinger yields no frames or Shizuku is inactive
            if (computedFps <= 0) {
                computedFps = fallbackFps > 0 ? fallbackFps : 60;
                computed1PercentLow = Math.max(30, (int) (computedFps * 0.85f));
                isRealSurface = false;
            }

            final int finalFps = computedFps;
            final int finalLow = computed1PercentLow;
            final boolean finalIsReal = isRealSurface;

            AppExecutors.getInstance().postToMainThread(() -> {
                if (listener != null && isRunning) {
                    listener.onFpsUpdated(finalFps, finalLow, finalIsReal);
                }
            });

            if (monitorHandler != null && isRunning) {
                monitorHandler.postDelayed(this, SAMPLE_INTERVAL_MS);
            }
        }
    };

    /**
     * Updates Choreographer fallback frame count from the UI thread.
     */
    public void onChoreographerFrame(long frameTimeNanos) {
        if (!isRunning) return;
        fallbackFrameCount++;
        if (lastFallbackCalcTimeNanos == 0) {
            lastFallbackCalcTimeNanos = frameTimeNanos;
        } else {
            long elapsed = frameTimeNanos - lastFallbackCalcTimeNanos;
            if (elapsed >= 1_000_000_000L) {
                fallbackFps = (int) Math.round((fallbackFrameCount * 1_000_000_000.0) / elapsed);
                fallbackFrameCount = 0;
                lastFallbackCalcTimeNanos = frameTimeNanos;
            }
        }
    }

    private static class FpsStats {
        final int fps;
        final int onePercentLow;

        FpsStats(int fps, int onePercentLow) {
            this.fps = fps;
            this.onePercentLow = onePercentLow;
        }
    }

    /**
     * Queries SurfaceFlinger latency buffer to extract true hardware present timings.
     */
    private FpsStats querySurfaceFlingerFps(String targetPkg) {
        // Step 1: Detect active foreground surface layer name if package is unspecified
        String layerName = null;
        if (targetPkg != null && !targetPkg.trim().isEmpty()) {
            layerName = targetPkg.trim();
        } else {
            String focusDump = ShizukuExecutor.executeShizukuCommand("dumpsys window | grep -E 'mCurrentFocus|mFocusedApp'");
            if (focusDump != null && !focusDump.isEmpty()) {
                for (String line : focusDump.split("\n")) {
                    int slash = line.indexOf('/');
                    if (slash > 0) {
                        int space = line.lastIndexOf(' ', slash);
                        if (space >= 0 && slash > space) {
                            String p = line.substring(space + 1, slash).trim();
                            if (!p.isEmpty() && !p.contains(" ") && p.contains(".")) {
                                layerName = p;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (layerName == null || layerName.isEmpty()) {
            return null;
        }

        // Query SurfaceFlinger latency for the identified layer
        String latencyOutput = ShizukuExecutor.executeShizukuCommand("dumpsys SurfaceFlinger --latency \"" + layerName + "\"");
        if (latencyOutput == null || latencyOutput.isEmpty() || latencyOutput.startsWith("ERROR")) {
            return null;
        }

        String[] lines = latencyOutput.split("\n");
        if (lines.length < 5) {
            return null;
        }

        long refreshPeriodNanos;
        try {
            refreshPeriodNanos = Long.parseLong(lines[0].trim());
        } catch (Exception e) {
            refreshPeriodNanos = 16666666L; // Default 60Hz
        }

        if (refreshPeriodNanos <= 0) refreshPeriodNanos = 16666666L;

        List<Long> presentTimes = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            if (parts.length >= 2) {
                try {
                    long actualPresentTime = Long.parseLong(parts[1]);
                    // Ignore unpresented or sentinel values (0, Long.MAX_VALUE)
                    if (actualPresentTime > 0 && actualPresentTime < Long.MAX_VALUE - 1000) {
                        presentTimes.add(actualPresentTime);
                    }
                } catch (Exception ignored) {}
            }
        }

        if (presentTimes.size() < 4) {
            return null;
        }

        // Compute frame intervals
        List<Double> frameIntervalsMs = new ArrayList<>();
        int validFrames = 0;
        long lastTime = presentTimes.get(0);
        long nowNanos = System.nanoTime();
        long windowStartNanos = nowNanos - 1_200_000_000L; // Last 1.2s window

        for (int i = 1; i < presentTimes.size(); i++) {
            long pt = presentTimes.get(i);
            if (pt > lastTime) {
                long delta = pt - lastTime;
                double ms = delta / 1_000_000.0;
                // Plausible frame duration between 1ms (1000fps) and 250ms (4fps)
                if (ms >= 1.0 && ms <= 250.0) {
                    frameIntervalsMs.add(ms);
                    validFrames++;
                }
            }
            lastTime = pt;
        }

        if (frameIntervalsMs.isEmpty() || validFrames < 2) {
            return null;
        }

        // Calculate average FPS
        double totalIntervalMs = 0;
        for (double d : frameIntervalsMs) {
            totalIntervalMs += d;
        }
        double avgIntervalMs = totalIntervalMs / frameIntervalsMs.size();
        int avgFps = (int) Math.round(1000.0 / avgIntervalMs);

        // Calculate 1% low (99th percentile frame time)
        Collections.sort(frameIntervalsMs);
        int percentile99Index = (int) Math.floor(frameIntervalsMs.size() * 0.99);
        if (percentile99Index >= frameIntervalsMs.size()) percentile99Index = frameIntervalsMs.size() - 1;
        double worstFrameTimeMs = frameIntervalsMs.get(percentile99Index);
        int onePercentLowFps = worstFrameTimeMs > 0 ? (int) Math.round(1000.0 / worstFrameTimeMs) : avgFps;

        // Clamp to plausible bounds
        avgFps = Math.max(1, Math.min(avgFps, 240));
        onePercentLowFps = Math.max(1, Math.min(onePercentLowFps, avgFps));

        return new FpsStats(avgFps, onePercentLowFps);
    }
}
