package com.gamebooster.app.gamespace;

import org.junit.Test;
import static org.junit.Assert.*;

public class GameStateReverterTest {

    @Test
    public void testDefaultBaselineHzConstant() {
        assertEquals(60, GameStateReverter.DEFAULT_BASELINE_HZ);
    }

    @Test
    public void testRevertReportIdleState() {
        GameStateReverter.RevertReport report = GameStateReverter.RevertReport.idle(60, "No active session");
        assertNotNull(report);
        assertFalse(report.sessionActive);
        assertEquals(60, report.restoredHz);
        assertEquals("No active session", report.message);
    }

    @Test
    public void testRevertReportActiveState() {
        GameStateReverter.RevertReport report = GameStateReverter.RevertReport.active(120, "Session restored");
        assertNotNull(report);
        assertTrue(report.sessionActive);
        assertTrue(report.refreshRateRestored);
        assertTrue(report.governorRestored);
        assertEquals(120, report.restoredHz);
        assertEquals("Session restored", report.message);
    }
}
