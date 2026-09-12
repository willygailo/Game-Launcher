package com.gamebooster.app.engine;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EngineTest {

    @Test
    public void testEngineModeEnum() {
        assertEquals("FULL ACCESS: ROOT (UID 0) SUPERUSER", EngineMode.ROOT.getDisplayName());
        assertEquals(0xFFFF0055, EngineMode.ROOT.getColorHex());

        assertEquals("DUAL ENGINE: ROOT + SHIZUKU ACTIVE", EngineMode.DUAL_ENGINE.getDisplayName());
        assertEquals(0xFF00FFCC, EngineMode.DUAL_ENGINE.getColorHex());

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

    @Test
    public void testPrivilegeBridgeEngineUnwrap() {
        assertEquals("setprop debug.test 1", PrivilegeBridgeEngine.unwrapSuCommand("su -c \"setprop debug.test 1\""));
        assertEquals("setprop debug.test 1", PrivilegeBridgeEngine.unwrapSuCommand("su -c 'setprop debug.test 1'"));
        assertEquals("cmd power set-mode 0 1", PrivilegeBridgeEngine.unwrapSuCommand("sudo cmd power set-mode 0 1"));
        assertEquals("sh", PrivilegeBridgeEngine.unwrapSuCommand("su"));
        assertEquals("sh", PrivilegeBridgeEngine.unwrapSuCommand("su -"));
        assertEquals("echo hello", PrivilegeBridgeEngine.unwrapSuCommand("echo hello"));
        assertNotNull(PrivilegeBridgeEngine.getPrivilegeTitle());
    }
}
