package com.gamebooster.app.core;

import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.engine.AdpfPerformanceEngine;
import com.gamebooster.app.engine.AotCompilerEngine;
import com.gamebooster.app.engine.ArtCompilerEngine;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SubsystemImprovementsTest {

    @Test
    public void testAppExecutorsScheduledIO() throws InterruptedException {
        AppExecutors executors = AppExecutors.getInstance();
        assertNotNull(executors.getCommandIO());
        assertNotNull(executors.getScanIO());
        assertNotNull(executors.getScheduledIO());

        CountDownLatch latch = new CountDownLatch(1);
        executors.schedule(latch::countDown, 50, TimeUnit.MILLISECONDS);
        assertTrue(latch.await(500, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testArtCompilerEngineFilters() {
        assertEquals("speed", ArtCompilerEngine.CompileFilter.SPEED.filterName);
        assertEquals("speed-profile", ArtCompilerEngine.CompileFilter.SPEED_PROFILE.filterName);
        assertEquals("quicken", ArtCompilerEngine.CompileFilter.QUICKEN.filterName);

        // Verify AotCompilerEngine delegation mappings
        assertEquals(ArtCompilerEngine.CompileFilter.SPEED, AotCompilerEngine.CompileMode.SPEED.toArtFilter());
        assertEquals(ArtCompilerEngine.CompileFilter.SPEED_PROFILE, AotCompilerEngine.CompileMode.SPEED_PROFILE.toArtFilter());
    }

    @Test
    public void testDevicePerformanceCapabilitiesDefaults() {
        float headroom = DevicePerformanceCapabilities.getThermalHeadroom(null, 0);
        assertEquals(1.0f, headroom, 0.01f);

        boolean throttling = DevicePerformanceCapabilities.isThermalThrottling(null);
        assertFalse(throttling);
    }

    @Test
    public void testAdpfEngineThreadDiscovery() {
        int[] tids = AdpfPerformanceEngine.discoverGameThreads(1);
        assertNotNull(tids);
        assertTrue(tids.length >= 1);
        assertEquals(1, tids[0]);
    }
}
