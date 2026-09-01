package com.gamebooster.app.overlay;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CrosshairPresetTest {

    @Test
    public void testCrosshairPresets() {
        assertEquals("Dot", CrosshairPreset.DOT.getLabel());
        assertEquals("Tactical Cross", CrosshairPreset.TACTICAL_CROSS.getLabel());
        assertEquals("Scope Ring", CrosshairPreset.SCOPE_RING.getLabel());
        assertEquals("Sniper Cross", CrosshairPreset.SNIPER_CROSS.getLabel());

        CrosshairPreset[] presets = CrosshairPreset.values();
        assertNotNull(presets);
        assertEquals(4, presets.length);
    }
}
