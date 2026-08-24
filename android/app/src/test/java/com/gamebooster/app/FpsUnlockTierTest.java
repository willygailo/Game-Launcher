package com.gamebooster.app;

import com.gamebooster.app.config.FpsUnlockTier;
import org.junit.Test;
import static org.junit.Assert.*;

public class FpsUnlockTierTest {

    @Test
    public void testFpsTierResolution() {
        assertEquals(FpsUnlockTier.FPS_90, FpsUnlockTier.fromFps(90));
        assertEquals(FpsUnlockTier.FPS_120, FpsUnlockTier.fromFps(120));
        assertEquals(FpsUnlockTier.FPS_144, FpsUnlockTier.fromFps(144));
        assertEquals(FpsUnlockTier.FPS_165, FpsUnlockTier.fromFps(165));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromFps(185));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromFps(0));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromFps(-1));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromFps(240));
    }

    @Test
    public void testFpsValuesArray() {
        int[] all = FpsUnlockTier.getAllFpsValues();
        assertNotNull(all);
        assertEquals(5, all.length);
        assertEquals(90, all[0]);
        assertEquals(185, all[4]);
    }

    @Test
    public void testUnlockFlagsGeneration() {
        String flags185 = FpsUnlockTier.FPS_185.getUnlockFlags();
        assertNotNull(flags185);
        assertTrue(flags185.contains("Unlock185Hz=1"));
        assertTrue(flags185.contains("Unlock120Hz=1"));

        String ue4Flags = FpsUnlockTier.FPS_185.getUE4UltraExtremeCVars();
        assertNotNull(ue4Flags);
        assertTrue(ue4Flags.contains("+CVars=r.PUBGMaxFPS=185"));
        assertTrue(ue4Flags.contains("+CVars=r.MobileHDR=1"));
    }
}
