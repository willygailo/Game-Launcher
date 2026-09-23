package com.gamebooster.app.device;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class RefreshRatePolicyTest {

    @Test
    public void sanitizesRatesWithoutInventingModes() {
        assertEquals(Arrays.asList(60, 90, 120),
                RefreshRatePolicy.sanitizeReportedRates(Arrays.asList(120, 0, 90, 120, null, 60)));
    }

    @Test
    public void resolvesToHighestModeAtOrBelowRequest() {
        assertEquals(120, RefreshRatePolicy.resolveRate(Arrays.asList(60, 90, 120), 185));
        assertEquals(90, RefreshRatePolicy.resolveRate(Arrays.asList(60, 90, 120), 100));
        assertEquals(60, RefreshRatePolicy.resolveRate(Arrays.asList(60, 90, 120), 30));
    }

    @Test
    public void resolvesDefaultToHighestKnownModeAndUnknownToZero() {
        assertEquals(144, RefreshRatePolicy.resolveRate(Arrays.asList(60, 90, 144), 0));
        assertEquals(0, RefreshRatePolicy.resolveRate(Collections.emptyList(), 120));
    }

    @Test
    public void supportsOnlyReportedModes() {
        assertTrue(RefreshRatePolicy.supportsRate(Arrays.asList(60, 120), 120));
        assertFalse(RefreshRatePolicy.supportsRate(Arrays.asList(60, 120), 185));
    }
}
