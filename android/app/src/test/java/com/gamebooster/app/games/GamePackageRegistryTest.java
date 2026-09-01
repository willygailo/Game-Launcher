package com.gamebooster.app.games;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GamePackageRegistryTest {

    @Test
    public void testKnownGameRecognition() {
        assertTrue(GamePackageRegistry.isKnownGame("com.mobile.legends"));
        assertTrue(GamePackageRegistry.isKnownGame("com.tencent.ig"));
        assertTrue(GamePackageRegistry.isKnownGame("com.activision.callofduty.shooter"));
        assertTrue(GamePackageRegistry.isKnownGame("com.dts.freefireth"));
        assertTrue(GamePackageRegistry.isKnownGame("com.miHoYo.GenshinImpact"));
        assertTrue(GamePackageRegistry.isKnownGame("com.roblox.client"));
    }

    @Test
    public void testGetSpec() {
        GamePackageRegistry.GameInfoSpec mlbbSpec = GamePackageRegistry.getSpec("com.mobile.legends");
        assertNotNull(mlbbSpec);
        assertEquals("MOBA", mlbbSpec.category);
        assertEquals(185, mlbbSpec.maxSupportedFps);

        GamePackageRegistry.GameInfoSpec codmSpec = GamePackageRegistry.getSpec("com.activision.callofduty.shooter");
        assertNotNull(codmSpec);
        assertEquals("FPS", codmSpec.category);
        assertEquals(185, codmSpec.maxSupportedFps);
    }

    @Test
    public void testGetAllKnownGames() {
        Map<String, GamePackageRegistry.GameInfoSpec> all = GamePackageRegistry.getAllKnownGames();
        assertNotNull(all);
        assertTrue(all.size() >= 30);
    }
}
