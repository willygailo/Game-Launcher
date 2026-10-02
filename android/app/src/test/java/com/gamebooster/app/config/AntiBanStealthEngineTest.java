package com.gamebooster.app.config;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link AntiBanStealthEngine}.
 * Verifies rate limiter enforcement, state reset, and signature randomization noise injection.
 */
public class AntiBanStealthEngineTest {

    private static final String TEST_PKG = "com.test.game";

    @Before
    public void setUp() {
        AntiBanStealthEngine.resetRateLimit(TEST_PKG);
    }

    @Test
    public void testRateLimiter_allowsFirstInjectionThenBlocks() {
        assertTrue("First injection should be allowed",
                AntiBanStealthEngine.isInjectionAllowed(TEST_PKG));

        AntiBanStealthEngine.recordInjection(TEST_PKG);

        assertFalse("Subsequent immediate injection should be rate-limited",
                AntiBanStealthEngine.isInjectionAllowed(TEST_PKG));
    }

    @Test
    public void testRateLimiter_resetAllowsImmediateReinject() {
        AntiBanStealthEngine.recordInjection(TEST_PKG);
        assertFalse(AntiBanStealthEngine.isInjectionAllowed(TEST_PKG));

        AntiBanStealthEngine.resetRateLimit(TEST_PKG);
        assertTrue("Reset should immediately clear rate limit",
                AntiBanStealthEngine.isInjectionAllowed(TEST_PKG));
    }

    @Test
    public void testRateLimiter_nullPackageSafety() {
        assertFalse("Null package must not be allowed",
                AntiBanStealthEngine.isInjectionAllowed(null));
        // Should not throw
        AntiBanStealthEngine.recordInjection(null);
        AntiBanStealthEngine.resetRateLimit(null);
    }

    @Test
    public void testRandomizeSignature_iniFormat() {
        String baseContent = "[Settings]\nKey=Value\n";
        String randomized = AntiBanStealthEngine.randomizeSignature(baseContent, "ini");

        assertNotNull(randomized);
        assertTrue("Original content must be preserved", randomized.startsWith(baseContent));
        assertTrue("Must include stealth salt comment", randomized.contains("; GB_STEALTH_SALT="));
        assertTrue("Must include noise keys", randomized.contains("; GraphicsProfileCacheVersion="));
    }

    @Test
    public void testRandomizeSignature_jsonFormat() {
        String baseContent = "{\"key\": \"value\"}";
        String randomized = AntiBanStealthEngine.randomizeSignature(baseContent, "json");

        assertNotNull(randomized);
        assertTrue(randomized.startsWith(baseContent));
        assertTrue("Must use // comment syntax for JSON", randomized.contains("// GB_STEALTH_SALT="));
    }

    @Test
    public void testRandomizeSignature_xmlFormat() {
        String baseContent = "<map><string name=\"test\">val</string></map>";
        String randomized = AntiBanStealthEngine.randomizeSignature(baseContent, "xml");

        assertNotNull(randomized);
        assertTrue(randomized.startsWith(baseContent));
        assertTrue("Must use XML comment syntax", randomized.contains("<!-- GB_STEALTH_SALT="));
        assertTrue(randomized.contains("-->"));
    }
}
