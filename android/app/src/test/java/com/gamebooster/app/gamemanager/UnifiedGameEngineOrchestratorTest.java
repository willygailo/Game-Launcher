package com.gamebooster.app.gamemanager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UnifiedGameEngineOrchestratorTest {

    @Test
    public void testSessionPresetsConfiguration() {
        UnifiedGameEngineOrchestrator.SessionPreset esports = UnifiedGameEngineOrchestrator.SessionPreset.ESPORTS_TOURNAMENT;
        assertNotNull(esports);
        assertEquals(185, esports.defaultHz);
        assertTrue(esports.extremeGovernor);
        assertTrue(esports.zeroLatencyVsync);

        UnifiedGameEngineOrchestrator.SessionPreset ultra = UnifiedGameEngineOrchestrator.SessionPreset.ULTRA_FRAMERATE;
        assertNotNull(ultra);
        assertEquals(165, ultra.defaultHz);
        assertTrue(ultra.extremeGovernor);
        assertFalse(ultra.zeroLatencyVsync);

        UnifiedGameEngineOrchestrator.SessionPreset balanced = UnifiedGameEngineOrchestrator.SessionPreset.BALANCED_PERFORMANCE;
        assertNotNull(balanced);
        assertEquals(120, balanced.defaultHz);
        assertFalse(balanced.extremeGovernor);
        assertFalse(balanced.zeroLatencyVsync);
    }

    @Test
    public void testInitialState() {
        assertFalse(UnifiedGameEngineOrchestrator.isSessionActive());
    }
}
