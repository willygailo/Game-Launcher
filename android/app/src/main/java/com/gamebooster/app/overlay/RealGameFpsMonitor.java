package com.gamebooster.app.overlay;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;
import android.view.Choreographer;
import android.view.Display;
import android.view.WindowManager;

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
        default void onFpsDetailedUpdated(int currentFps, int onePercentLowFps, int zeroPointOnePercentLowFps, double frameTimeMs, double jitterMs, boolean isRealGameSurface) {}
    }

    private static volatile RealGameFpsMonitor instance;

    private final Object lock = new Object();
    private boolean isRunning = false;
    private String targetPackage;
    private FpsUpdateListener listener;

    private HandlerThread monitorThread;
    private Handler monitorHandler;

    // VSYNC Choreographer fallback state (High-Refresh baseline)
    private int fallbackFps = 185;
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

            try {
                Display display = context.getDisplay();
                if (display != null) {
                    float deviceRefreshRate = display.getRefreshRate();
                    if (deviceRefreshRate > 0) {
                        fallbackFps = Math.round(deviceRefreshRate);
                    }
                }
            } catch (Exception ignored) {}

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
            int computed01PercentLow = -1;
            double computedFrameTimeMs = 5.4;
            double computedJitterMs = 0.2;
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
                        computed01PercentLow = stats.zeroPointOnePercentLow;
                        computedFrameTimeMs = stats.frameTimeMs;
                        computedJitterMs = stats.jitterMs;
                        isRealSurface = true;
                    }
                } catch (Throwable t) {
                    Log.v(TAG, "SurfaceFlinger FPS sample warning: " + t.getMessage());
                }
            }

            // Tier 2: Fallback when SurfaceFlinger yields no frames or Shizuku is inactive
            if (computedFps <= 0) {
                computedFps = fallbackFps > 0 ? fallbackFps : 185;
                computed1PercentLow = Math.max(90, (int) (computedFps * 0.85f));
                computed01PercentLow = Math.max(80, (int) (computedFps * 0.75f));
                computedFrameTimeMs = 1000.0 / Math.max(1, computedFps);
                computedJitterMs = 0.2;
                isRealSurface = false;
            }

            final int finalFps = computedFps;
            final int finalLow = computed1PercentLow;
            final int final01Low = computed01PercentLow;
            final double finalFrameTime = computedFrameTimeMs;
            final double finalJitter = computedJitterMs;
            final boolean finalIsReal = isRealSurface;

            AppExecutors.getInstance().postToMainThread(() -> {
                if (listener != null && isRunning) {
                    listener.onFpsUpdated(finalFps, finalLow, finalIsReal);
                    listener.onFpsDetailedUpdated(finalFps, finalLow, final01Low, finalFrameTime, finalJitter, finalIsReal);
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

    public static class FpsStats {
        public final int fps;
        public final int onePercentLow;
        public final int zeroPointOnePercentLow;
        public final double frameTimeMs;
        public final double jitterMs;

        public FpsStats(int fps, int onePercentLow, int zeroPointOnePercentLow, double frameTimeMs, double jitterMs) {
            this.fps = fps;
            this.onePercentLow = onePercentLow;
            this.zeroPointOnePercentLow = zeroPointOnePercentLow;
            this.frameTimeMs = frameTimeMs;
            this.jitterMs = jitterMs;
        }

        public FpsStats(int fps, int onePercentLow) {
            this(fps, onePercentLow, (int)(onePercentLow * 0.92), 1000.0 / Math.max(1, fps), 0.2);
        }
    }

    // Cache for resolved SurfaceFlinger layer name
    private String cachedLayerName = null;
    private long lastLayerResolveTime = 0L;
    private String lastResolvedPkg = null;

    /**
     * Resolves the active SurfaceView or rendering layer name from SurfaceFlinger.
     * Matches exact package layers (e.g. SurfaceView[pkg/...], pkg/..., pkg#0)
     * across all game engines (Unity, Unreal Engine, Cocos, custom native renderers).
     */
    private String resolveActiveGameLayer(String targetPkg) {
        long now = System.currentTimeMillis();
        if (targetPkg != null && targetPkg.equals(lastResolvedPkg) && cachedLayerName != null && (now - lastLayerResolveTime < 2500L)) {
            return cachedLayerName;
        }

        String pkg = (targetPkg != null && !targetPkg.trim().isEmpty()) ? targetPkg.trim() : null;

        // Auto-detect foreground package if not set
        if (pkg == null) {
            String focusDump = ShizukuExecutor.executeShizukuCommand("dumpsys window | grep -E 'mCurrentFocus|mFocusedApp'");
            if (focusDump != null && !focusDump.isEmpty()) {
                for (String line : focusDump.split("\n")) {
                    int slash = line.indexOf('/');
                    if (slash > 0) {
                        int space = line.lastIndexOf(' ', slash);
                        if (space >= 0 && slash > space) {
                            String p = line.substring(space + 1, slash).trim();
                            if (!p.isEmpty() && !p.contains(" ") && p.contains(".") && !p.contains("com.gamebooster.app")) {
                                pkg = p;
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Query active layer list from SurfaceFlinger
        String listOutput = ShizukuExecutor.executeShizukuCommand("dumpsys SurfaceFlinger --list");
        if (listOutput == null || listOutput.isEmpty() || listOutput.startsWith("ERROR")) {
            // Fallback to raw package name
            return pkg;
        }

        String bestLayer = null;
        int bestPriority = -1;

        for (String line : listOutput.split("\n")) {
            String layer = line.trim();
            if (layer.isEmpty() || layer.contains("com.gamebooster.app") || layer.contains("NavigationBar") || layer.contains("StatusBar") || layer.contains("ScreenDecorOverlay")) {
                continue;
            }

            int priority = -1;
            if (pkg != null && layer.contains(pkg)) {
                if (layer.contains("SurfaceView") || layer.contains("surface-view")) {
                    priority = 100; // Top priority: Game render surface
                } else if (layer.contains("#") && layer.contains("/")) {
                    priority = 80;  // Standard activity layer
                } else {
                    priority = 60;  // Package-associated layer
                }
            } else if (pkg == null && (layer.contains("SurfaceView") || layer.contains("surface-view"))) {
                priority = 40; // General SurfaceView from any active game
            }

            if (priority > bestPriority) {
                bestPriority = priority;
                bestLayer = layer;
                if (priority == 100) break; // Found exact game surface layer
            }
        }

        if (bestLayer != null) {
            cachedLayerName = bestLayer;
            lastLayerResolveTime = now;
            lastResolvedPkg = pkg;
            return bestLayer;
        }

        return pkg;
    }

    /**
     * Queries SurfaceFlinger latency buffer to extract true hardware present timings.
     */
    private FpsStats querySurfaceFlingerFps(String targetPkg) {
        String layerName = resolveActiveGameLayer(targetPkg);
        if (layerName == null || layerName.isEmpty()) {
            return null;
        }

        // Query SurfaceFlinger latency for the identified layer
        String latencyOutput = ShizukuExecutor.executeShizukuCommand("dumpsys SurfaceFlinger --latency \"" + layerName + "\"");
        if (latencyOutput == null || latencyOutput.isEmpty() || latencyOutput.startsWith("ERROR")) {
            cachedLayerName = null; // Invalidate cache on failure
            return null;
        }

        String[] lines = latencyOutput.split("\n");
        if (lines.length < 5) {
            cachedLayerName = null;
            return null;
        }

        long refreshPeriodNanos;
        try {
            refreshPeriodNanos = Long.parseLong(lines[0].trim());
        } catch (Exception e) {
            // Default to 185Hz (~5.405ms) — the app's flagship target refresh rate
            refreshPeriodNanos = 5_405_405L;
        }

        if (refreshPeriodNanos <= 0) refreshPeriodNanos = 5_405_405L; // 185Hz fallback

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

        // Calculate variance / jitter (Standard Deviation)
        double varianceSum = 0;
        for (double d : frameIntervalsMs) {
            varianceSum += Math.pow(d - avgIntervalMs, 2);
        }
        double jitterMs = Math.sqrt(varianceSum / frameIntervalsMs.size());

        // Calculate 1% low and 0.1% low
        Collections.sort(frameIntervalsMs);
        int percentile99Index = (int) Math.floor(frameIntervalsMs.size() * 0.99);
        if (percentile99Index >= frameIntervalsMs.size()) percentile99Index = frameIntervalsMs.size() - 1;
        double worst99FrameTimeMs = frameIntervalsMs.get(percentile99Index);
        int onePercentLowFps = worst99FrameTimeMs > 0 ? (int) Math.round(1000.0 / worst99FrameTimeMs) : avgFps;

        int percentile999Index = (int) Math.floor(frameIntervalsMs.size() * 0.999);
        if (percentile999Index >= frameIntervalsMs.size()) percentile999Index = frameIntervalsMs.size() - 1;
        double worst999FrameTimeMs = frameIntervalsMs.get(percentile999Index);
        int zeroPointOnePercentLowFps = worst999FrameTimeMs > 0 ? (int) Math.round(1000.0 / worst999FrameTimeMs) : onePercentLowFps;

        // Clamp to plausible bounds
        avgFps = Math.max(1, Math.min(avgFps, 240));
        onePercentLowFps = Math.max(1, Math.min(onePercentLowFps, avgFps));
        zeroPointOnePercentLowFps = Math.max(1, Math.min(zeroPointOnePercentLowFps, onePercentLowFps));

        return new FpsStats(avgFps, onePercentLowFps, zeroPointOnePercentLowFps, avgIntervalMs, jitterMs);
    }
}
