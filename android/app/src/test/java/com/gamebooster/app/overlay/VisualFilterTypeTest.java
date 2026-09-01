package com.gamebooster.app.overlay;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VisualFilterTypeTest {

    @Test
    public void testVisualFilterTypes() {
        assertEquals("Off (Default Display)", VisualFilterOverlayService.VisualFilterType.OFF.label);
        assertEquals("🎯 Sniper Shadow Boost (High Contrast)", VisualFilterOverlayService.VisualFilterType.SNIPER_SHADOW_BOOST.label);
        assertEquals("🌈 Vibrant Saturation (MOBA Enhanced)", VisualFilterOverlayService.VisualFilterType.VIBRANT_SATURATION.label);
        assertEquals("🌙 Night Eye Guard (Anti-Glare)", VisualFilterOverlayService.VisualFilterType.NIGHT_ANTI_GLARE.label);

        VisualFilterOverlayService.VisualFilterType[] types = VisualFilterOverlayService.VisualFilterType.values();
        assertNotNull(types);
        assertEquals(4, types.length);
    }
}
