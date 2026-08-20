package com.gamebooster.app.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class FpsUnlockTierTest {

    @Test
    public void testResolveTargetFps() {
        assertEquals(90, FpsUnlockTier.resolveTargetFps(60));
        assertEquals(90, FpsUnlockTier.resolveTargetFps(90));
        assertEquals(120, FpsUnlockTier.resolveTargetFps(120));
        assertEquals(144, FpsUnlockTier.resolveTargetFps(144));
        assertEquals(165, FpsUnlockTier.resolveTargetFps(165));
        assertEquals(185, FpsUnlockTier.resolveTargetFps(185));
        assertEquals(185, FpsUnlockTier.resolveTargetFps(240));
    }

    @Test
    public void testFromFps() {
        assertEquals(FpsUnlockTier.FPS_120, FpsUnlockTier.fromFps(120));
        assertEquals(FpsUnlockTier.FPS_144, FpsUnlockTier.fromFps(144));
        assertEquals(FpsUnlockTier.FPS_165, FpsUnlockTier.fromFps(165));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromFps(185));
    }

    @Test
    public void testUnlockFlagsGenerators() {
        FpsUnlockTier tier185 = FpsUnlockTier.FPS_185;

        String iniFlags = tier185.getUnlockFlags();
        assertTrue(iniFlags.contains("Unlock120Hz=1"));
        assertTrue(iniFlags.contains("Unlock144Hz=1"));
        assertTrue(iniFlags.contains("Unlock165Hz=1"));
        assertTrue(iniFlags.contains("Unlock185Hz=1"));

        String ue4CVars = tier185.getUE4UnlockCVars();
        assertTrue(ue4CVars.contains("+CVars=r.Unlock120Hz=1"));
        assertTrue(ue4CVars.contains("+CVars=r.Unlock185Hz=1"));

        String jsonFlags = tier185.getJsonUnlockFlags();
        assertTrue(jsonFlags.contains("\"Unlock120Hz\": 1"));
        assertTrue(jsonFlags.contains("\"Unlock185Hz\": 1"));

        String xmlFlags = tier185.getXmlUnlockFlags();
        assertTrue(xmlFlags.contains("<int name=\"Unlock120Hz\" value=\"1\" />"));
        assertTrue(xmlFlags.contains("<int name=\"Unlock185Hz\" value=\"1\" />"));

        String ultraExtremeFlags = tier185.getUltraExtremeFlags();
        assertTrue(ultraExtremeFlags.contains("UltraExtreme=1"));
        assertTrue(ultraExtremeFlags.contains("bUseUltraExtreme=True"));
        assertTrue(ultraExtremeFlags.contains("GraphicsQuality=5"));
        assertTrue(ultraExtremeFlags.contains("FPS=185"));

        String ue4UltraExtreme = tier185.getUE4UltraExtremeCVars();
        assertTrue(ue4UltraExtreme.contains("+CVars=r.PUBGQualityLevel=4"));
        assertTrue(ue4UltraExtreme.contains("+CVars=r.MobileHDR=1"));
        assertTrue(ue4UltraExtreme.contains("+CVars=r.PUBGDeviceFPS=10"));
    }
}
