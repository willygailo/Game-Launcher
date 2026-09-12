package com.gamebooster.app.games;

import com.gamebooster.app.gamespace.GameStateReverter;
import com.gamebooster.app.overlay.CrosshairPreset;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GamesSpaceOverlayTest {

    @Test
    public void testKnownGameRecognition() {
        // Test newly added competitive game packages
        assertTrue(GamePackageRegistry.isKnownGame("com.proximabeta.mf.uamo"));
        assertTrue(GamePackageRegistry.isKnownGame("com.levelinfinite.deltaforce"));
        assertTrue(GamePackageRegistry.isKnownGame("com.tencent.tmgp.dfm"));
        assertTrue(GamePackageRegistry.isKnownGame("com.proximabeta.deltaforce"));
        assertTrue(GamePackageRegistry.isKnownGame("com.kurogame.wutheringwaves.global"));
        assertTrue(GamePackageRegistry.isKnownGame("com.HoYoverse.Nap"));
        assertTrue(GamePackageRegistry.isKnownGame("com.activision.callofduty.warzone"));

        // Test non-games
        assertFalse(GamePackageRegistry.isKnownGame("com.android.settings"));
        assertFalse(GamePackageRegistry.isKnownGame("com.google.android.youtube"));
    }

    @Test
    public void testGameStateReverterEvaluation() {
        // Idle session
        GameStateReverter.RevertReport idleReport = GameStateReverter.evaluate(60, false, false);
        assertNotNull(idleReport);
        assertFalse(idleReport.sessionActive);
        assertEquals(0, idleReport.restoredHz);

        // Active session with 120Hz baseline
        GameStateReverter.RevertReport activeReport = GameStateReverter.evaluate(120, true, true);
        assertNotNull(activeReport);
        assertTrue(activeReport.sessionActive);
        assertTrue(activeReport.refreshRateRestored);
        assertEquals(120, activeReport.restoredHz);

        // Active session with 0Hz captured (fallback to default baseline)
        GameStateReverter.RevertReport defaultReport = GameStateReverter.evaluate(0, false, true);
        assertNotNull(defaultReport);
        assertTrue(defaultReport.sessionActive);
        assertEquals(60, defaultReport.restoredHz);
    }

    @Test
    public void testCrosshairPresets() {
        assertNotNull(CrosshairPreset.TACTICAL_CROSS);
        assertNotNull(CrosshairPreset.DOT);
        assertNotNull(CrosshairPreset.SCOPE_RING);
        assertNotNull(CrosshairPreset.SNIPER_CROSS);
        assertEquals("Tactical Cross", CrosshairPreset.TACTICAL_CROSS.getLabel());
    }
}
