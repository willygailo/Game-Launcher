package com.gamebooster.app.overlay;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GameSessionReportTest {

    @Test
    public void testGameSessionReportFieldsAndFormatting() {
        long start = 1000000L;
        long end = start + (15 * 60 * 1000L) + (30 * 1000L); // 15 mins 30 secs

        GameSessionReport report = new GameSessionReport(
                "com.mobile.legends",
                "Mobile Legends: Bang Bang",
                start,
                end,
                120,
                110,
                95,
                125,
                85,
                80,
                10.0f,
                38.5f,
                36.2f,
                98
        );

        assertEquals("com.mobile.legends", report.packageName);
        assertEquals("Mobile Legends: Bang Bang", report.gameTitle);
        assertEquals(start, report.startTimeMs);
        assertEquals(end, report.endTimeMs);
        assertEquals(120, report.averageFps);
        assertEquals(110, report.onePercentLowFps);
        assertEquals(95, report.minFps);
        assertEquals(125, report.maxFps);
        assertEquals(85, report.startBatteryLevel);
        assertEquals(80, report.endBatteryLevel);
        assertEquals(10.0f, report.batteryDrainRatePerHour, 0.01f);
        assertEquals(38.5f, report.peakTemperatureC, 0.01f);
        assertEquals(36.2f, report.averageTemperatureC, 0.01f);
        assertEquals(98, report.stabilityScorePercent);

        assertEquals(15, report.getPlaytimeMinutes());
        assertEquals(30, report.getPlaytimeSeconds());
        assertEquals("15m 30s", report.getFormattedPlaytime());
    }
}
