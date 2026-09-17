package com.gamebooster.app.overlay;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FloatingOverlayHudModeTest {

    @Test
    public void testHudModes() {
        FloatingOverlayService.HudMode[] modes = FloatingOverlayService.HudMode.values();
        assertNotNull(modes);
        assertEquals(5, modes.length);

        assertEquals(FloatingOverlayService.HudMode.PILL, FloatingOverlayService.HudMode.valueOf("PILL"));
        assertEquals(FloatingOverlayService.HudMode.MICRO_FPS, FloatingOverlayService.HudMode.valueOf("MICRO_FPS"));
        assertEquals(FloatingOverlayService.HudMode.EXPANDED_DOCK, FloatingOverlayService.HudMode.valueOf("EXPANDED_DOCK"));
        assertEquals(FloatingOverlayService.HudMode.HIDDEN_EDGE, FloatingOverlayService.HudMode.valueOf("HIDDEN_EDGE"));
        assertEquals(FloatingOverlayService.HudMode.FULL_INVISIBLE, FloatingOverlayService.HudMode.valueOf("FULL_INVISIBLE"));
    }
}
