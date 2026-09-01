package com.gamebooster.app.engine;

import android.os.PowerManager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NativeFrameworkBridgeTest {

    @Test
    public void testThermalStatusNames() {
        assertEquals("NORMAL", NativeFrameworkBridge.getThermalStatusName(PowerManager.THERMAL_STATUS_NONE));
        assertEquals("LIGHT", NativeFrameworkBridge.getThermalStatusName(PowerManager.THERMAL_STATUS_LIGHT));
        assertEquals("MODERATE", NativeFrameworkBridge.getThermalStatusName(PowerManager.THERMAL_STATUS_MODERATE));
        assertEquals("SEVERE", NativeFrameworkBridge.getThermalStatusName(PowerManager.THERMAL_STATUS_SEVERE));
        assertEquals("CRITICAL", NativeFrameworkBridge.getThermalStatusName(PowerManager.THERMAL_STATUS_CRITICAL));
        assertEquals("EMERGENCY", NativeFrameworkBridge.getThermalStatusName(PowerManager.THERMAL_STATUS_EMERGENCY));
        assertEquals("SHUTDOWN", NativeFrameworkBridge.getThermalStatusName(PowerManager.THERMAL_STATUS_SHUTDOWN));
        assertEquals("UNKNOWN (999)", NativeFrameworkBridge.getThermalStatusName(999));
    }

    @Test
    public void testNullContextGuardrails() {
        assertFalse(NativeFrameworkBridge.setGameModePerformance(null, "com.pubg.imobile"));
        assertFalse(NativeFrameworkBridge.setGameModePerformance(null, null));
        assertEquals(-1, NativeFrameworkBridge.getGameMode(null, "com.pubg.imobile"));
        assertEquals(60.0f, NativeFrameworkBridge.getHighestSupportedRefreshRate(null), 0.001f);
        assertNull(NativeFrameworkBridge.findHighestRefreshRateMode(null));
        assertTrue(NativeFrameworkBridge.isIgnoringBatteryOptimizations(null));
        assertNull(NativeFrameworkBridge.createIgnoreBatteryOptimizationIntent(null));
    }

    @Test
    public void testLockStateInspectors() {
        // Without active acquisition, locks should report false
        assertFalse(NativeFrameworkBridge.isWifiLockHeld());
        assertFalse(NativeFrameworkBridge.isSustainedPerformanceLockHeld());
    }

    @Test
    public void testAdpfEngineSessionLifecycle() {
        AdpfPerformanceEngine engine = AdpfPerformanceEngine.getInstance();
        assertFalse(engine.isSessionActive());
        assertFalse(engine.isSupported(null));
        assertFalse(engine.startSession(null, 120));
        assertTrue(Float.isNaN(AdpfPerformanceEngine.getThermalHeadroom(null, 5)));
    }
}
