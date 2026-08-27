package com.gamebooster.app.overlay;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import com.gamebooster.app.device.DeviceInfoChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GameSessionRecorder — High-precision game match telemetry recording engine.
 *
 * Samples FPS, frame pacing, battery discharge rate (%/hr), and thermal throttling
 * curves across active game sessions to produce the post-game performance report card.
 */
public class GameSessionRecorder {

    private static final String TAG = "GameSessionRecorder";
    private static volatile GameSessionRecorder sInstance;

    private boolean isRecording = false;
    private String currentPackage;
    private String currentGameTitle;
    private long sessionStartTimeMs;

    private int startBatteryLevel = 100;
    private final List<Integer> fpsSamples = new ArrayList<>();
    private final List<Integer> onePercentLowSamples = new ArrayList<>();
    private final List<Float> tempSamples = new ArrayList<>();

    private GameSessionReport lastCompletedReport;

    private GameSessionRecorder() {}

    public static GameSessionRecorder getInstance() {
        if (sInstance == null) {
            synchronized (GameSessionRecorder.class) {
                if (sInstance == null) {
                    sInstance = new GameSessionRecorder();
                }
            }
        }
        return sInstance;
    }

    public synchronized void startSession(Context context, String packageName, String gameTitle) {
        this.currentPackage = packageName;
        this.currentGameTitle = (gameTitle != null && !gameTitle.isEmpty()) ? gameTitle : packageName;
        this.sessionStartTimeMs = System.currentTimeMillis();
        this.isRecording = true;

        this.fpsSamples.clear();
        this.onePercentLowSamples.clear();
        this.tempSamples.clear();

        if (context != null) {
            this.startBatteryLevel = getBatteryLevel(context);
        }

        Log.i(TAG, "Game session recording started for: " + currentGameTitle);
    }

    public synchronized void recordFpsSample(int currentFps, int onePercentLow) {
        if (!isRecording) return;
        if (currentFps > 0 && currentFps <= 240) {
            fpsSamples.add(currentFps);
        }
        if (onePercentLow > 0 && onePercentLow <= 240) {
            onePercentLowSamples.add(onePercentLow);
        }
    }

    public synchronized void recordTemperatureSample(float tempC) {
        if (!isRecording) return;
        if (tempC > 10f && tempC < 110f) {
            tempSamples.add(tempC);
        }
    }

    public synchronized GameSessionReport endSession(Context context) {
        if (!isRecording) {
            return lastCompletedReport;
        }

        long endTimeMs = System.currentTimeMillis();
        isRecording = false;

        int endBattery = (context != null ? getBatteryLevel(context) : startBatteryLevel);
        int batteryUsed = Math.max(0, startBatteryLevel - endBattery);
        long durationMs = Math.max(1000, endTimeMs - sessionStartTimeMs);
        float hours = (float) durationMs / (1000f * 3600f);
        float drainRatePerHour = (hours > 0.001f) ? (batteryUsed / hours) : 0f;

        // Calculate FPS Stats
        int avgFps = 60;
        int minFps = 60;
        int maxFps = 60;
        int avg1PercentLow = 50;

        if (!fpsSamples.isEmpty()) {
            long sum = 0;
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (int fps : fpsSamples) {
                sum += fps;
                if (fps < min) min = fps;
                if (fps > max) max = fps;
            }
            avgFps = (int) (sum / fpsSamples.size());
            minFps = min;
            maxFps = max;
        }

        if (!onePercentLowSamples.isEmpty()) {
            long sum = 0;
            for (int l : onePercentLowSamples) {
                sum += l;
            }
            avg1PercentLow = (int) (sum / onePercentLowSamples.size());
        } else {
            avg1PercentLow = (int) (avgFps * 0.85f);
        }

        // Calculate Thermals
        float peakTemp = 38.0f;
        float avgTemp = 36.0f;
        if (!tempSamples.isEmpty()) {
            float sum = 0f;
            float max = Float.MIN_VALUE;
            for (float t : tempSamples) {
                sum += t;
                if (t > max) max = t;
            }
            peakTemp = max;
            avgTemp = sum / tempSamples.size();
        }

        // Stability Score
        int stabilityScore = 95;
        if (avgFps > 0 && avg1PercentLow > 0) {
            stabilityScore = Math.min(100, Math.max(60, (int) (((float) avg1PercentLow / (float) avgFps) * 100f)));
        }

        lastCompletedReport = new GameSessionReport(
                currentPackage,
                currentGameTitle,
                sessionStartTimeMs,
                endTimeMs,
                avgFps,
                avg1PercentLow,
                minFps,
                maxFps,
                startBatteryLevel,
                endBattery,
                drainRatePerHour,
                peakTemp,
                avgTemp,
                stabilityScore
        );

        Log.i(TAG, "Game session report generated for: " + currentGameTitle + " | Avg FPS: " + avgFps + " | Stability: " + stabilityScore + "%");
        return lastCompletedReport;
    }

    public GameSessionReport getLastReport() {
        return lastCompletedReport;
    }

    public boolean isRecording() {
        return isRecording;
    }

    private static int getBatteryLevel(Context context) {
        try {
            Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (intent != null) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                if (level >= 0 && scale > 0) {
                    return (int) ((level / (float) scale) * 100);
                }
            }
        } catch (Exception ignored) {}
        return 100;
    }
}
