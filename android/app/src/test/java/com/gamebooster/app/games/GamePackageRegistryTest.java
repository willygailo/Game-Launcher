package com.gamebooster.app.games;

import org.junit.Test;
import java.util.Map;
import static org.junit.Assert.*;

public class GamePackageRegistryTest {

    @Test
    public void testIsKnownGame_MajorEsportsTitles() {
        assertTrue(GamePackageRegistry.isKnownGame("com.tencent.ig"));
        assertTrue(GamePackageRegistry.isKnownGame("com.pubg.imobile"));
        assertTrue(GamePackageRegistry.isKnownGame("com.mobile.legends"));
        assertTrue(GamePackageRegistry.isKnownGame("com.activision.callofduty.shooter"));
        assertTrue(GamePackageRegistry.isKnownGame("com.dts.freefireth"));
        assertTrue(GamePackageRegistry.isKnownGame("com.riotgames.league.wildrift"));
        assertTrue(GamePackageRegistry.isKnownGame("com.miHoYo.GenshinImpact"));
        assertTrue(GamePackageRegistry.isKnownGame("com.roblox.client"));
        assertTrue(GamePackageRegistry.isKnownGame("com.axlebolt.standoff2"));
        assertTrue(GamePackageRegistry.isKnownGame("com.supercell.brawlstars"));
    }

    @Test
    public void testIsKnownGame_HeuristicMatching() {
        // Regional / modded variants
        assertTrue(GamePackageRegistry.isKnownGame("com.vng.pubgmobile"));
        assertTrue(GamePackageRegistry.isKnownGame("com.garena.game.codm"));
        assertTrue(GamePackageRegistry.isKnownGame("com.kurogame.wutheringwaves.global"));
        assertTrue(GamePackageRegistry.isKnownGame("com.miracle.farlight84"));
    }

    @Test
    public void testIsKnownGame_NonGames() {
        assertFalse(GamePackageRegistry.isKnownGame("com.whatsapp"));
        assertFalse(GamePackageRegistry.isKnownGame("com.google.android.youtube"));
        assertFalse(GamePackageRegistry.isKnownGame("org.telegram.messenger"));
        assertFalse(GamePackageRegistry.isKnownGame(null));
        assertFalse(GamePackageRegistry.isKnownGame(""));
    }

    @Test
    public void testGetSpec_ValidGame() {
        GamePackageRegistry.GameInfoSpec spec = GamePackageRegistry.getSpec("com.tencent.ig");
        assertNotNull(spec);
        assertEquals("PUBG Mobile (Global)", spec.title);
        assertEquals("Battle Royale", spec.category);
        assertEquals(185, spec.maxSupportedFps);
    }

    @Test
    public void testGetAllKnownGames_NotEmpty() {
        Map<String, GamePackageRegistry.GameInfoSpec> all = GamePackageRegistry.getAllKnownGames();
        assertNotNull(all);
        assertTrue(all.size() >= 30);
    }
}
