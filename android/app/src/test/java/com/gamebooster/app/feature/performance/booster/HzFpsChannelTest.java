package com.gamebooster.app.feature.performance.booster;

import org.junit.Test;
import static org.junit.Assert.*;

public class HzFpsChannelTest {

    @Test
    public void testForce165HzEngineWithNullContext() {
        HzFpsChannel.RefreshRateResult res = HzFpsChannel.force165HzEngine(null, "com.tencent.ig");
        assertNotNull(res);
        assertFalse(res.success);
        assertEquals(165, res.requestedHz);
    }
}
