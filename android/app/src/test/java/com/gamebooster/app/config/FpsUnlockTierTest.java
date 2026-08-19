package com.gamebooster.app.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class FpsUnlockTierTest {

    @Test
    public void testStandardTierResolutions() {
        assertEquals(90, FpsUnlockTier.resolveTargetFps(90));
        assertEquals(120, FpsUnlockTier.resolveTargetFps(120));
        assertEquals(144, FpsUnlockTier.resolveTargetFps(144));
        assertEquals(165, FpsUnlockTier.resolveTargetFps(165));
        assertEquals(185, FpsUnlockTier.resolveTargetFps(185));
    }

    @Test
    public void testBoundaryAndFallbackResolutions() {
        // Less than or equal to 0 defaults to 185
        assertEquals(185, FpsUnlockTier.resolveTargetFps(0));
        assertEquals(185, FpsUnlockTier.resolveTargetFps(-1));

        // Sub-90 positive values clamp to lowest tier (90)
        assertEquals(90, FpsUnlockTier.resolveTargetFps(60));

        // Intermediate values map to nearest tier at or below
        assertEquals(90, FpsUnlockTier.resolveTargetFps(95));
        assertEquals(120, FpsUnlockTier.resolveTargetFps(130));
        assertEquals(144, FpsUnlockTier.resolveTargetFps(150));
        assertEquals(165, FpsUnlockTier.resolveTargetFps(170));
        assertEquals(185, FpsUnlockTier.resolveTargetFps(200));
    }

    @Test
    public void testFromFpsLevels() {
        assertEquals(10, FpsUnlockTier.fromFps(185).level);
        assertEquals(9, FpsUnlockTier.fromFps(165).level);
        assertEquals(8, FpsUnlockTier.fromFps(144).level);
        assertEquals(7, FpsUnlockTier.fromFps(120).level);
        assertEquals(6, FpsUnlockTier.fromFps(90).level);
    }
}
