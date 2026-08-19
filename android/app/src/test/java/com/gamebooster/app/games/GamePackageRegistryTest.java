package com.gamebooster.app.games;

import org.junit.Test;
import static org.junit.Assert.*;

public class GamePackageRegistryTest {

    @Test
    public void testKnownGamePackages() {
        assertTrue(GamePackageRegistry.isKnownGame("com.tencent.ig"));
        assertTrue(GamePackageRegistry.isKnownGame("com.dts.freefireth"));
        assertTrue(GamePackageRegistry.isKnownGame("com.mobile.legends"));
        assertTrue(GamePackageRegistry.isKnownGame("com.activision.callofduty.shooter"));
        assertTrue(GamePackageRegistry.isKnownGame("com.miHoYo.GenshinImpact"));
        assertTrue(GamePackageRegistry.isKnownGame("com.riotgames.league.wildrift"));
        assertTrue(GamePackageRegistry.isKnownGame("com.roblox.client"));
        assertTrue(GamePackageRegistry.isKnownGame("com.axlebolt.standoff2"));
    }

    @Test
    public void testNonGamePackages() {
        assertFalse(GamePackageRegistry.isKnownGame("com.android.settings"));
        assertFalse(GamePackageRegistry.isKnownGame("com.google.android.youtube"));
        assertFalse(GamePackageRegistry.isKnownGame("com.whatsapp"));
    }

    @Test
    public void testGetGameSpec() {
        GamePackageRegistry.GameInfoSpec pubg = GamePackageRegistry.getSpec("com.tencent.ig");
        assertNotNull(pubg);
        assertEquals("PUBG Mobile (Global)", pubg.title);

        GamePackageRegistry.GameInfoSpec mlbb = GamePackageRegistry.getSpec("com.mobile.legends");
        assertNotNull(mlbb);
        assertEquals("Mobile Legends: Bang Bang", mlbb.title);
    }
}
