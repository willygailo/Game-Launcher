package com.gamebooster.app.overlay;

import java.io.Serializable;

/**
 * GameSessionReport — Complete match performance and telemetry benchmark model.
 */
public class GameSessionReport implements Serializable {

    public final String packageName;
    public final String gameTitle;
    public final long startTimeMs;
    public final long endTimeMs;

    public final int averageFps;
    public final int onePercentLowFps;
    public final int minFps;
    public final int maxFps;

    public final int startBatteryLevel;
    public final int endBatteryLevel;
    public final float batteryDrainRatePerHour;

    public final float peakTemperatureC;
    public final float averageTemperatureC;
    public final int stabilityScorePercent;

    public GameSessionReport(String packageName, String gameTitle, long startTimeMs, long endTimeMs,
                             int averageFps, int onePercentLowFps, int minFps, int maxFps,
                             int startBatteryLevel, int endBatteryLevel, float batteryDrainRatePerHour,
                             float peakTemperatureC, float averageTemperatureC, int stabilityScorePercent) {
        this.packageName = packageName;
        this.gameTitle = gameTitle;
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
        this.averageFps = averageFps;
        this.onePercentLowFps = onePercentLowFps;
        this.minFps = minFps;
        this.maxFps = maxFps;
        this.startBatteryLevel = startBatteryLevel;
        this.endBatteryLevel = endBatteryLevel;
        this.batteryDrainRatePerHour = batteryDrainRatePerHour;
        this.peakTemperatureC = peakTemperatureC;
        this.averageTemperatureC = averageTemperatureC;
        this.stabilityScorePercent = stabilityScorePercent;
    }

    public long getPlaytimeMinutes() {
        long durationMs = Math.max(0, endTimeMs - startTimeMs);
        return durationMs / 60000;
    }

    public long getPlaytimeSeconds() {
        long durationMs = Math.max(0, endTimeMs - startTimeMs);
        return (durationMs % 60000) / 1000;
    }

    public String getFormattedPlaytime() {
        long min = getPlaytimeMinutes();
        long sec = getPlaytimeSeconds();
        return min + "m " + sec + "s";
    }
}
