package com.gamebooster.app.config;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link FpsUnlockTier}.
 * Verifies tier clamping, level mappings, array exports, and unlock flag generation.
 */
public class FpsUnlockTierTest {

    @Test
    public void testFromFps_resolutionAndClamping() {
        assertEquals(FpsUnlockTier.FPS_120, FpsUnlockTier.fromFps(60));
        assertEquals(FpsUnlockTier.FPS_120, FpsUnlockTier.fromFps(120));
        assertEquals(FpsUnlockTier.FPS_144, FpsUnlockTier.fromFps(144));
        assertEquals(FpsUnlockTier.FPS_144, FpsUnlockTier.fromFps(150));
        assertEquals(FpsUnlockTier.FPS_165, FpsUnlockTier.fromFps(165));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromFps(185));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromFps(240));
    }

    @Test
    public void testFromLevel_mapping() {
        assertEquals(FpsUnlockTier.FPS_120, FpsUnlockTier.fromLevel(0));
        assertEquals(FpsUnlockTier.FPS_120, FpsUnlockTier.fromLevel(7));
        assertEquals(FpsUnlockTier.FPS_144, FpsUnlockTier.fromLevel(8));
        assertEquals(FpsUnlockTier.FPS_165, FpsUnlockTier.fromLevel(9));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromLevel(10));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromLevel(99));
    }

    @Test
    public void testResolveTargetFps() {
        assertEquals(120, FpsUnlockTier.resolveTargetFps(90));
        assertEquals(144, FpsUnlockTier.resolveTargetFps(144));
        assertEquals(165, FpsUnlockTier.resolveTargetFps(165));
        assertEquals(185, FpsUnlockTier.resolveTargetFps(185));
    }

    @Test
    public void testGetAllFpsValues() {
        int[] expected = new int[] {120, 144, 165, 185};
        assertArrayEquals(expected, FpsUnlockTier.getAllFpsValues());
    }

    @Test
    public void testGetAllLabels() {
        String[] expected = new String[] {"120fps", "144fps", "165fps", "185fps"};
        assertArrayEquals(expected, FpsUnlockTier.getAllLabels());
    }

    @Test
    public void testGetUnlockFlags() {
        String flags120 = FpsUnlockTier.FPS_120.getUnlockFlags();
        assertNotNull(flags120);
        assertTrue(flags120.contains("Unlock120Hz=1"));

        String flags185 = FpsUnlockTier.FPS_185.getUnlockFlags();
        assertNotNull(flags185);
        assertTrue(flags185.contains("Unlock120Hz=1"));
        assertTrue(flags185.contains("Unlock185Hz=1"));
    }
}
