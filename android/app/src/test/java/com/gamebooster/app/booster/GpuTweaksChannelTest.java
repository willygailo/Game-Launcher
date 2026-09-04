package com.gamebooster.app.booster;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GpuTweaksChannelTest {

    @Test
    public void testIsGameDriverEligibleForMlbb() {
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.mobile.legends"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.mobilelegends.mi"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.vng.mlbbvn"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.mobile.legends.vng"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.mobilelegends.na"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.mobilelegends.hw"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.mobile.legends.moonton"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.mobile.legends.kr"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.mobile.legends.jp"));
    }

    @Test
    public void testIsGameDriverEligibleForCodm() {
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.activision.callofduty.shooter"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.garena.game.codm"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.tencent.tmgp.kr.codm"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.vng.codmvn"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.tencent.tmgp.cod"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.activision.callofduty.warzone"));
    }

    @Test
    public void testIsGameDriverEligibleForPubgm() {
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.tencent.ig"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.pubg.imobile"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.vng.pubgmobile"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.pubg.krmobile"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.rekoo.pubgm"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.tencent.tmgp.pubgmhd"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.tencent.iglite"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.pubg.newstate"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.tencent.tmgp.pubgm"));
    }

    @Test
    public void testIsGameDriverNotEligibleForOtherGames() {
        assertFalse(GpuTweaksChannel.isGameDriverEligible("com.miHoYo.GenshinImpact"));
        assertFalse(GpuTweaksChannel.isGameDriverEligible("com.cognosphere.GenshinImpact"));
        assertFalse(GpuTweaksChannel.isGameDriverEligible("com.dts.freefireth"));
        assertFalse(GpuTweaksChannel.isGameDriverEligible("com.dts.freefiremax"));
        assertFalse(GpuTweaksChannel.isGameDriverEligible("com.roblox.client"));
        assertFalse(GpuTweaksChannel.isGameDriverEligible("com.riotgames.league.wildrift"));
        assertFalse(GpuTweaksChannel.isGameDriverEligible("com.h20.carxstreet"));
        assertFalse(GpuTweaksChannel.isGameDriverEligible("com.supercell.brawlstars"));
        assertFalse(GpuTweaksChannel.isGameDriverEligible(""));
        assertFalse(GpuTweaksChannel.isGameDriverEligible("   "));
        assertFalse(GpuTweaksChannel.isGameDriverEligible(null));
    }

    @Test
    public void testGetTargetGamesCsvOnlyContainsMlbbCodmPubgm() {
        String csv = GpuTweaksChannel.getTargetGamesCsv();
        assertNotNull(csv);
        assertFalse(csv.isEmpty());

        String[] pkgs = csv.split(",");
        assertTrue("Eligible package count should be greater than 15", pkgs.length >= 15);

        for (String p : pkgs) {
            String trimmed = p.trim();
            assertFalse(trimmed.isEmpty());
            assertTrue("Package " + trimmed + " must be eligible for Game Driver",
                    GpuTweaksChannel.isGameDriverEligible(trimmed));
        }

        // Verify non-eligible games are strictly excluded from the CSV
        assertFalse(csv.contains("com.miHoYo.GenshinImpact"));
        assertFalse(csv.contains("com.dts.freefireth"));
        assertFalse(csv.contains("com.roblox.client"));
        assertFalse(csv.contains("com.riotgames.league.wildrift"));
    }

    @Test
    public void testGraphicsDriverTypeEnum() {
        GpuTweaksChannel.GraphicsDriverType[] types = GpuTweaksChannel.GraphicsDriverType.values();
        assertEquals("GraphicsDriverType should have exactly 2 options (DEFAULT, GAME_DRIVER)", 2, types.length);
        assertEquals(GpuTweaksChannel.GraphicsDriverType.DEFAULT, types[0]);
        assertEquals(GpuTweaksChannel.GraphicsDriverType.GAME_DRIVER, types[1]);
    }
}
