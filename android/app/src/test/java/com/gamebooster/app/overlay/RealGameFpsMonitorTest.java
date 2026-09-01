package com.gamebooster.app.overlay;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RealGameFpsMonitorTest {

    @Test
    public void testFpsStatsConstructors() {
        RealGameFpsMonitor.FpsStats stats = new RealGameFpsMonitor.FpsStats(120, 110, 100, 8.33, 0.25);
        assertEquals(120, stats.fps);
        assertEquals(110, stats.onePercentLow);
        assertEquals(100, stats.zeroPointOnePercentLow);
        assertEquals(8.33, stats.frameTimeMs, 0.01);
        assertEquals(0.25, stats.jitterMs, 0.01);

        RealGameFpsMonitor.FpsStats simpleStats = new RealGameFpsMonitor.FpsStats(60, 55);
        assertEquals(60, simpleStats.fps);
        assertEquals(55, simpleStats.onePercentLow);
        assertEquals(16.66, simpleStats.frameTimeMs, 0.1);
    }
}
