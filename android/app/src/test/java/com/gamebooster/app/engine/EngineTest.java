package com.gamebooster.app.engine;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EngineTest {

    @Test
    public void testEngineModeEnum() {
        assertEquals("FULL ACCESS: SHIZUKU API ACTIVE", EngineMode.SHIZUKU.getDisplayName());
        assertEquals(0xFF00FF66, EngineMode.SHIZUKU.getColorHex());

        assertEquals("SYSTEM SETTINGS ENGINE", EngineMode.SYSTEM_SETTINGS.getDisplayName());
        assertEquals(0xFF00F0FF, EngineMode.SYSTEM_SETTINGS.getColorHex());

        assertEquals("HARDWARE MONITOR MODE", EngineMode.READ_ONLY.getDisplayName());
        assertEquals(0xFFFFB800, EngineMode.READ_ONLY.getColorHex());
    }

    @Test
    public void testResolutionScalePresets() {
        assertEquals(ResolutionScalerEngine.ScalePreset.NATIVE_100, ResolutionScalerEngine.ScalePreset.fromScaleFactor(1.0f));
        assertEquals(ResolutionScalerEngine.ScalePreset.HIGH_900P, ResolutionScalerEngine.ScalePreset.fromScaleFactor(0.85f));
        assertEquals(ResolutionScalerEngine.ScalePreset.ESPORTS_720P, ResolutionScalerEngine.ScalePreset.fromScaleFactor(0.70f));
        assertEquals(ResolutionScalerEngine.ScalePreset.EXTREME_540P, ResolutionScalerEngine.ScalePreset.fromScaleFactor(0.50f));
    }
}
