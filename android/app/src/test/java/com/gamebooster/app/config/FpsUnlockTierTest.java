package com.gamebooster.app.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class FpsUnlockTierTest {

    @Test
    public void testFromFpsExactMatches() {
        assertEquals(FpsUnlockTier.FPS_90, FpsUnlockTier.fromFps(90));
        assertEquals(FpsUnlockTier.FPS_120, FpsUnlockTier.fromFps(120));
        assertEquals(FpsUnlockTier.FPS_144, FpsUnlockTier.fromFps(144));
        assertEquals(FpsUnlockTier.FPS_165, FpsUnlockTier.fromFps(165));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromFps(185));
    }

    @Test
    public void testFromFpsNearestResolution() {
        assertEquals(FpsUnlockTier.FPS_90, FpsUnlockTier.fromFps(60));
        assertEquals(FpsUnlockTier.FPS_90, FpsUnlockTier.fromFps(100));
        assertEquals(FpsUnlockTier.FPS_120, FpsUnlockTier.fromFps(130));
        assertEquals(FpsUnlockTier.FPS_144, FpsUnlockTier.fromFps(150));
        assertEquals(FpsUnlockTier.FPS_165, FpsUnlockTier.fromFps(170));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromFps(240));
    }

    @Test
    public void testResolveTargetFps() {
        assertEquals(185, FpsUnlockTier.resolveTargetFps(0));
        assertEquals(90, FpsUnlockTier.resolveTargetFps(60));
        assertEquals(120, FpsUnlockTier.resolveTargetFps(120));
        assertEquals(144, FpsUnlockTier.resolveTargetFps(144));
        assertEquals(165, FpsUnlockTier.resolveTargetFps(165));
        assertEquals(185, FpsUnlockTier.resolveTargetFps(185));
    }

    @Test
    public void testUnlockFlagsGeneration() {
        String flags90 = FpsUnlockTier.FPS_90.getUnlockFlags();
        assertTrue(flags90.isEmpty());

        String flags120 = FpsUnlockTier.FPS_120.getUnlockFlags();
        assertTrue(flags120.contains("Unlock120Hz=1"));
        assertFalse(flags120.contains("Unlock144Hz=1"));

        String flags185 = FpsUnlockTier.FPS_185.getUnlockFlags();
        assertTrue(flags185.contains("Unlock120Hz=1"));
        assertTrue(flags185.contains("Unlock144Hz=1"));
        assertTrue(flags185.contains("Unlock165Hz=1"));
        assertTrue(flags185.contains("Unlock185Hz=1"));
    }
}
