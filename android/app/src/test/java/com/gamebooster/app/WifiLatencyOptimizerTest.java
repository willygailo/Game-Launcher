package com.gamebooster.app;

import com.gamebooster.app.feature.performance.network.WifiLatencyOptimizer;
import org.junit.Test;

import static org.junit.Assert.*;

public class WifiLatencyOptimizerTest {

    @Test
    public void testAcquireWithNullContext() {
        boolean acquired = WifiLatencyOptimizer.acquireLowLatencyLock(null);
        assertFalse(acquired);
    }

    @Test
    public void testReleaseWhenNotHeld() {
        // Should handle safely without throwing
        boolean released = WifiLatencyOptimizer.releaseLowLatencyLock();
        assertFalse(released);
        assertFalse(WifiLatencyOptimizer.isLockHeld());
    }
}
