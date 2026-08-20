package com.gamebooster.app.spoofer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.gamebooster.app.spoofer.lsposed.LspatchHelper;
import com.gamebooster.app.spoofer.lsposed.LsposedDetector;

import org.junit.Test;

public class LspatchHelperTest {

    @Test
    public void testConstants() {
        assertEquals("org.lsposed.lspatch", LspatchHelper.LSPATCH_PKG);
        assertEquals("org.lsposed.lspatch.metamod", LspatchHelper.LSPATCH_METAMOD_PKG);
        assertNotNull(LspatchHelper.LSPATCH_GITHUB_URL);
        assertTrue(LspatchHelper.LSPATCH_GITHUB_URL.startsWith("https://"));
    }

    @Test
    public void testFrameworkTypeEnum() {
        for (LsposedDetector.FrameworkType type : LsposedDetector.FrameworkType.values()) {
            assertNotNull(type.displayName);
            assertNotNull(type.colorHex);
            assertTrue(type.colorHex.startsWith("#"));
        }
    }

    @Test
    public void testHeartbeatTracking() {
        assertFalse(LsposedDetector.isAnyGameHookedActive());
        LsposedDetector.recordGameHeartbeat("com.dts.freefireth");
        assertTrue(LsposedDetector.isAnyGameHookedActive());
    }
}
