package com.gamebooster.app;

import com.gamebooster.app.config.DeltaForceConfigPatcher;
import com.gamebooster.app.config.FpsUnlockTier;
import com.gamebooster.app.config.NativeConfigInjector;
import com.gamebooster.app.config.WutheringWavesConfigPatcher;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class NativeConfigInjectorTest {

    @Test
    public void testDeltaForcePatcher() {
        List<String> paths = DeltaForceConfigPatcher.getConfigPaths("com.levelinfinite.deltaforce");
        assertNotNull(paths);
        assertFalse(paths.isEmpty());
        assertTrue(paths.get(0).contains("UE4Game/DeltaForce"));
    }

    @Test
    public void testWutheringWavesPatcher() {
        List<String> paths = WutheringWavesConfigPatcher.getConfigPaths("com.kurogame.wutheringwaves.global");
        assertNotNull(paths);
        assertFalse(paths.isEmpty());
        assertTrue(paths.get(0).contains("Saved/Config/Android"));
    }

    @Test
    public void testFpsUnlockTiers() {
        assertEquals(185, FpsUnlockTier.resolveTargetFps(185));
        assertEquals(165, FpsUnlockTier.resolveTargetFps(165));
        assertEquals(144, FpsUnlockTier.resolveTargetFps(144));
        assertEquals(120, FpsUnlockTier.resolveTargetFps(120));
        assertEquals(90, FpsUnlockTier.resolveTargetFps(90));
        assertEquals(90, FpsUnlockTier.resolveTargetFps(60));
    }

    @Test
    public void testExtractKey() {
        assertEquals("r.MaxFPS", NativeConfigInjector.extractKey("r.MaxFPS=185"));
        assertEquals("+CVars=r.PUBGMaxFPS", NativeConfigInjector.extractKey("+CVars=r.PUBGMaxFPS=185"));
    }
}
