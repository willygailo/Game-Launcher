package com.gamebooster.app.booster;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CombatEngineChannelTest {

    @Test
    public void testCombatModeEnableAndRestore() {
        // Enable combat mode
        boolean enabled = CombatEngineChannel.enableCombatMode(null);
        assertTrue("Combat mode should enable successfully", enabled);
        assertTrue("isCombatModeActive should be true", CombatEngineChannel.isCombatModeActive());

        // Restore default mode
        boolean restored = CombatEngineChannel.restoreDefaultMode(null);
        assertTrue("Default mode should restore successfully", restored);
        assertFalse("isCombatModeActive should be false after restore", CombatEngineChannel.isCombatModeActive());
    }
}
