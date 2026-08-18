package com.gamebooster.app.gamespace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.gamebooster.app.gamespace.GameStateReverter.RevertReport;

public class GameStateReverterTest {

    @Test
    public void evaluate_idleWhenNoSession() {
        RevertReport report = GameStateReverter.evaluate(185, true, false);
        assertFalse(report.sessionActive);
        assertFalse(report.refreshRateRestored);
        assertFalse(report.governorRestored);
        assertFalse(report.thermalRestored);
        assertFalse(report.networkRestored);
        assertFalse(report.dndRestored);
        assertEquals(0, report.restoredHz);
        assertTrue(report.message.contains("No active game session"));
    }

    @Test
    public void evaluate_restoresCapturedBaselineHz() {
        RevertReport report = GameStateReverter.evaluate(60, false, true);
        assertTrue(report.sessionActive);
        assertTrue(report.refreshRateRestored);
        assertTrue(report.governorRestored);
        assertTrue(report.thermalRestored);
        assertTrue(report.networkRestored);
        assertTrue(report.dndRestored);
        assertEquals(60, report.restoredHz);
        assertTrue(report.message.contains("display 60Hz"));
    }

    @Test
    public void evaluate_fallsBackToDefaultWhenPreviousHzUnknown() {
        RevertReport report = GameStateReverter.evaluate(0, false, true);
        assertEquals(GameStateReverter.DEFAULT_BASELINE_HZ, report.restoredHz);
        assertTrue(report.message.contains("display " + GameStateReverter.DEFAULT_BASELINE_HZ + "Hz"));
    }

    @Test
    public void evaluate_clampsInvalidHzToDefault() {
        RevertReport report = GameStateReverter.evaluate(-1, true, true);
        assertEquals(GameStateReverter.DEFAULT_BASELINE_HZ, report.restoredHz);
    }

    @Test
    public void evaluate_restoresDndWithCapturedState() {
        RevertReport report = GameStateReverter.evaluate(120, true, true);
        assertTrue(report.dndRestored);
        assertEquals(120, report.restoredHz);
    }
}