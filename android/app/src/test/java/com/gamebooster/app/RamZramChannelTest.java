package com.gamebooster.app;

import com.gamebooster.app.feature.performance.booster.RamZramChannel;
import org.junit.Test;

import static org.junit.Assert.*;

public class RamZramChannelTest {

    @Test
    public void testMemoryStatsCalculation() {
        RamZramChannel.MemoryStats stats = new RamZramChannel.MemoryStats(8192, 2048, 3500);
        assertEquals(8192, stats.totalRamMb);
        assertEquals(2048, stats.availRamMbBefore);
        assertEquals(3500, stats.availRamMbAfter);
        assertEquals(1452, stats.freedRamMb);
    }

    @Test
    public void testMemoryStatsZeroOrNegativeFreed() {
        RamZramChannel.MemoryStats stats = new RamZramChannel.MemoryStats(4096, 1500, 1200);
        assertEquals(0, stats.freedRamMb);
    }

    @Test
    public void testOptimizeMemoryWithNullContext() {
        RamZramChannel.MemoryStats stats = RamZramChannel.optimizeMemory(null, "com.mobile.legends");
        assertNotNull(stats);
        assertEquals(0, stats.totalRamMb);
        assertEquals(0, stats.availRamMbBefore);
        assertEquals(0, stats.availRamMbAfter);
        assertEquals(0, stats.freedRamMb);
    }
}
