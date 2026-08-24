package com.gamebooster.app;

import com.gamebooster.app.gamemanager.GameManagerStatus;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameManagerStatusTest {

    @Test
    public void testGameManagerStatusSingleton() {
        GameManagerStatus status = GameManagerStatus.getInstance();
        assertNotNull(status);

        status.setActiveSession("com.mobile.legends");
        assertTrue(status.hasActiveSession());
        assertEquals("com.mobile.legends", status.getActiveGamePackage());

        status.recordApply(18, "Applied all tweaks");
        assertTrue(status.getLastApplyTimestamp() > 0);
        assertEquals("Applied all tweaks", status.getLastApplySummary());

        status.setMaskedAppsCount(42);
        assertEquals(42, status.getMaskedAppsCount());

        status.setActiveSession(null);
        assertFalse(status.hasActiveSession());
        assertNull(status.getActiveGamePackage());
    }
}
