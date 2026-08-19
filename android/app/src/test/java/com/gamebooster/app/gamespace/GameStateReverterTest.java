package com.gamebooster.app.gamespace;

import org.junit.Test;
import static org.junit.Assert.*;

public class GameStateReverterTest {

    @Test
    public void testBaselineStateModel() {
        GameStateReverter.RevertReport idleReport = GameStateReverter.evaluate(60, false, false);
        assertNotNull(idleReport);
        assertFalse(idleReport.sessionActive);
        assertEquals(0, idleReport.restoredHz);

        GameStateReverter.RevertReport activeReport = GameStateReverter.evaluate(120, true, true);
        assertNotNull(activeReport);
        assertTrue(activeReport.sessionActive);
        assertEquals(120, activeReport.restoredHz);
        assertTrue(activeReport.refreshRateRestored);
        assertTrue(activeReport.dndRestored);
    }
}
