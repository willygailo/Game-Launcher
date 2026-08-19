package com.gamebooster.app.gamespace;

import org.junit.Test;
import static org.junit.Assert.*;

public class GameStateReverterTest {

    @Test
    public void testEvaluate_NoActiveSession_ReturnsIdle() {
        GameStateReverter.RevertReport report = GameStateReverter.evaluate(120, false, false);
        assertNotNull(report);
        assertFalse(report.sessionActive);
        assertFalse(report.refreshRateRestored);
        assertFalse(report.governorRestored);
    }

    @Test
    public void testEvaluate_ActiveSession_RestoresBaselineHz() {
        GameStateReverter.RevertReport report = GameStateReverter.evaluate(90, true, true);
        assertNotNull(report);
        assertTrue(report.sessionActive);
        assertTrue(report.refreshRateRestored);
        assertTrue(report.governorRestored);
        assertEquals(90, report.restoredHz);
    }

    @Test
    public void testEvaluate_ActiveSession_DefaultHzFallback() {
        GameStateReverter.RevertReport report = GameStateReverter.evaluate(0, false, true);
        assertNotNull(report);
        assertTrue(report.sessionActive);
        assertEquals(60, report.restoredHz);
    }
}
