package com.gamebooster.app.games;

import org.junit.Test;
import static org.junit.Assert.*;

public class GamePackageRegistryTest {

    @Test
    public void testKnownGameLookups() {
        assertTrue(GamePackageRegistry.isKnownGame("com.mobile.legends"));
        assertTrue(GamePackageRegistry.isKnownGame("com.tencent.ig"));
        assertTrue(GamePackageRegistry.isKnownGame("com.pubg.imobile"));
        assertTrue(GamePackageRegistry.isKnownGame("com.activision.callofduty.shooter"));
        assertTrue(GamePackageRegistry.isKnownGame("com.dts.freefireth"));
        assertTrue(GamePackageRegistry.isKnownGame("com.dts.freefiremax"));
        assertTrue(GamePackageRegistry.isKnownGame("com.riotgames.league.wildrift"));
        assertTrue(GamePackageRegistry.isKnownGame("com.levelinfinite.sgameGlobal"));
        assertTrue(GamePackageRegistry.isKnownGame("com.miHoYo.GenshinImpact"));
        assertTrue(GamePackageRegistry.isKnownGame("com.roblox.client"));
        assertTrue(GamePackageRegistry.isKnownGame("com.axlebolt.standoff2"));
    }

    @Test
    public void testHeuristicDetection() {
        assertTrue(GamePackageRegistry.isKnownGame("com.vng.pubgmobile.custom"));
        assertTrue(GamePackageRegistry.isKnownGame("com.garena.game.codm.vn"));
        assertTrue(GamePackageRegistry.isKnownGame("com.mobile.legends.mod"));
        assertTrue(GamePackageRegistry.isKnownGame("com.netease.bloodstrike"));
        assertFalse(GamePackageRegistry.isKnownGame("com.android.calculator2"));
        assertFalse(GamePackageRegistry.isKnownGame("com.google.android.youtube"));
    }

    @Test
    public void testGameInfoSpecRetrieval() {
        GamePackageRegistry.GameInfoSpec spec = GamePackageRegistry.getSpec("com.mobile.legends");
        assertNotNull(spec);
        assertEquals("Mobile Legends: Bang Bang", spec.title);
        assertEquals("MOBA", spec.category);
        assertEquals(185, spec.maxSupportedFps);
    }
}
