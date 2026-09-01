package com.gamebooster.app.gamemanager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GameManagerStatusTest {

    @Test
    public void testGameManagerStatusSingleton() {
        GameManagerStatus status = GameManagerStatus.getInstance();
        assertNotNull(status);

        status.setActiveSession("com.mobile.legends");
        assertTrue(status.hasActiveSession());
        assertEquals("com.mobile.legends", status.getActiveGamePackage());

        status.recordApply(15, "15 tweaks applied");
        assertTrue(status.getLastApplyTimestamp() > 0);
        assertEquals("15 tweaks applied", status.getLastApplySummary());

        status.setGamesDetectedCount(5);
        assertEquals(5, status.getGamesDetectedCount());

        status.setMaskedAppsCount(3);
        assertEquals(3, status.getMaskedAppsCount());

        status.setActiveSession(null);
        assertFalse(status.hasActiveSession());
    }
}
