package com.gamebooster.app.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class GameConfigPatchVerifierTest {

    @Test
    public void testVerifyFpsInIniContent() {
        String ini = "[UserCustom]\nFPS=185\nMaxFPS=185\n";
        assertTrue(GameConfigPatchVerifier.verifyFpsInContent(ini, 185));
        assertFalse(GameConfigPatchVerifier.verifyFpsInContent(ini, 120));
    }

    @Test
    public void testVerifyFpsInCVarContent() {
        String cvar = "+CVars=r.PUBGMaxFPS=165\n+CVars=r.PUBGDeviceFPS=9\n";
        assertTrue(GameConfigPatchVerifier.verifyFpsInContent(cvar, 165));
        assertFalse(GameConfigPatchVerifier.verifyFpsInContent(cvar, 185));
    }

    @Test
    public void testVerifyFpsInJsonContent() {
        String json = "{\n  \"fps_limit\": 144,\n  \"graphic\": 3\n}";
        assertTrue(GameConfigPatchVerifier.verifyFpsInContent(json, 144));
        assertFalse(GameConfigPatchVerifier.verifyFpsInContent(json, 60));
    }

    @Test
    public void testVerifyUltraExtremeInContent() {
        String ini = "[Graphics]\nUltraExtreme=1\nGraphicsQuality=5\n";
        assertTrue(GameConfigPatchVerifier.verifyUltraExtremeInContent(ini));

        String cvar = "+CVars=r.PUBGQualityLevel=4\n+CVars=r.MobileHDR=1\n";
        assertTrue(GameConfigPatchVerifier.verifyUltraExtremeInContent(cvar));

        String json = "{\n  \"GraphicQuality\": 4\n}";
        assertTrue(GameConfigPatchVerifier.verifyUltraExtremeInContent(json));

        String basic = "FPS=60\n";
        assertFalse(GameConfigPatchVerifier.verifyUltraExtremeInContent(basic));
    }

    @Test
    public void testVerifyPatchInContent() {
        String content = "FPS=185\nUltraExtreme=1\n";
        assertTrue(GameConfigPatchVerifier.verifyPatchInContent(content, 185));
    }
}
