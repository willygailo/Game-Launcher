package com.gamebooster.app;

import com.gamebooster.app.gamespace.GameStateReverter;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameStateReverterTest {

    @Test
    public void testReverterEvaluationWhenSessionActive() {
        GameStateReverter.RevertReport report = GameStateReverter.evaluate(120, true, true);
        assertNotNull(report);
        assertTrue(report.sessionActive);
        assertTrue(report.refreshRateRestored);
        assertEquals(120, report.restoredHz);
    }

    @Test
    public void testReverterEvaluationWhenIdle() {
        GameStateReverter.RevertReport report = GameStateReverter.evaluate(120, true, false);
        assertNotNull(report);
        assertFalse(report.sessionActive);
        assertFalse(report.refreshRateRestored);
        assertEquals(0, report.restoredHz);
    }

    @Test
    public void testReverterDefaultBaseline() {
        GameStateReverter.RevertReport report = GameStateReverter.evaluate(0, false, true);
        assertNotNull(report);
        assertTrue(report.sessionActive);
        assertEquals(120, report.restoredHz);
    }
}
