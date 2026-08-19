package com.gamebooster.app.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class FpsUnlockTierTest {

    @Test
    public void testResolveTargetFps_StandardTiers() {
        assertEquals(90, FpsUnlockTier.resolveTargetFps(90));
        assertEquals(120, FpsUnlockTier.resolveTargetFps(120));
        assertEquals(144, FpsUnlockTier.resolveTargetFps(144));
        assertEquals(165, FpsUnlockTier.resolveTargetFps(165));
        assertEquals(185, FpsUnlockTier.resolveTargetFps(185));
    }

    @Test
    public void testResolveTargetFps_BoundaryValues() {
        // Zero or negative should fallback to 185 (or default tier)
        assertTrue(FpsUnlockTier.resolveTargetFps(0) >= 90);
        assertTrue(FpsUnlockTier.resolveTargetFps(-10) >= 90);

        // Very high FPS should resolve cleanly to a supported tier (185)
        assertEquals(185, FpsUnlockTier.resolveTargetFps(240));
        assertEquals(185, FpsUnlockTier.resolveTargetFps(999));
    }

    @Test
    public void testFromFps_TierMapping() {
        FpsUnlockTier tier90 = FpsUnlockTier.fromFps(90);
        assertNotNull(tier90);
        assertEquals(90, tier90.fps);

        FpsUnlockTier tier120 = FpsUnlockTier.fromFps(120);
        assertNotNull(tier120);
        assertEquals(120, tier120.fps);

        FpsUnlockTier tier185 = FpsUnlockTier.fromFps(185);
        assertNotNull(tier185);
        assertEquals(185, tier185.fps);
    }
}
