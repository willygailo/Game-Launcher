package com.gamebooster.app.config;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FpsUnlockTierTest {

    // ─── fromFps: nearest supported tier at-or-below the request ───────────

    @Test
    public void fromFps_boundaryValues_mapExactly() {
        assertEquals(FpsUnlockTier.FPS_90, FpsUnlockTier.fromFps(90));
        assertEquals(FpsUnlockTier.FPS_120, FpsUnlockTier.fromFps(120));
        assertEquals(FpsUnlockTier.FPS_144, FpsUnlockTier.fromFps(144));
        assertEquals(FpsUnlockTier.FPS_165, FpsUnlockTier.fromFps(165));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromFps(185));
    }

    @Test
    public void fromFps_betweenTiers_roundsDown() {
        assertEquals(FpsUnlockTier.FPS_90, FpsUnlockTier.fromFps(91));
        assertEquals(FpsUnlockTier.FPS_90, FpsUnlockTier.fromFps(119));
        assertEquals(FpsUnlockTier.FPS_120, FpsUnlockTier.fromFps(121));
        assertEquals(FpsUnlockTier.FPS_120, FpsUnlockTier.fromFps(143));
        assertEquals(FpsUnlockTier.FPS_144, FpsUnlockTier.fromFps(145));
        assertEquals(FpsUnlockTier.FPS_165, FpsUnlockTier.fromFps(166));
    }

    @Test
    public void fromFps_aboveTopTier_clampsToTop() {
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromFps(999));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromFps(Integer.MAX_VALUE));
    }

    @Test
    public void fromFps_invalidValues_fallBackToTopTier() {
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromFps(0));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromFps(-1));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromFps(Integer.MIN_VALUE));
    }

    // ─── fromLevel ───────────────────────────────────────────────────────────

    @Test
    public void fromLevel_mapsLevelIntegers() {
        assertEquals(FpsUnlockTier.FPS_90, FpsUnlockTier.fromLevel(6));
        assertEquals(FpsUnlockTier.FPS_120, FpsUnlockTier.fromLevel(7));
        assertEquals(FpsUnlockTier.FPS_144, FpsUnlockTier.fromLevel(8));
        assertEquals(FpsUnlockTier.FPS_165, FpsUnlockTier.fromLevel(9));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromLevel(10));
    }

    @Test
    public void fromLevel_unknownLevels_fallBackToTopTier() {
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromLevel(0));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromLevel(-5));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromLevel(99));
    }

    // ─── resolveTargetFps / catalogs ─────────────────────────────────────────

    @Test
    public void resolveTargetFps_alignsToTier() {
        assertEquals(90, FpsUnlockTier.resolveTargetFps(90));
        assertEquals(120, FpsUnlockTier.resolveTargetFps(130));
        assertEquals(144, FpsUnlockTier.resolveTargetFps(160));
        assertEquals(185, FpsUnlockTier.resolveTargetFps(0));
    }

    @Test
    public void getAllFpsValues_orderedAscending() {
        assertArrayEquals(new int[]{90, 120, 144, 165, 185}, FpsUnlockTier.getAllFpsValues());
    }

    @Test
    public void getAllLabels_matchesFpsValues() {
        assertArrayEquals(new String[]{"90fps", "120fps", "144fps", "165fps", "185fps"},
                FpsUnlockTier.getAllLabels());
    }

    @Test
    public void tierFields_consistent() {
        assertEquals(6, FpsUnlockTier.FPS_90.level);
        assertEquals("90fps", FpsUnlockTier.FPS_90.label);
        assertEquals(90, FpsUnlockTier.FPS_90.fps);
        assertEquals(10, FpsUnlockTier.FPS_185.level);
    }

    // ─── unlock flag generators (monotonic, capped per tier) ────────────────

    @Test
    public void unlockFlags_areEmptyAtBaseline90() {
        assertEquals("", FpsUnlockTier.FPS_90.getUnlockFlags());
        assertEquals("", FpsUnlockTier.FPS_90.getUE4UnlockCVars());
        assertEquals("", FpsUnlockTier.FPS_90.getJsonUnlockFlags());
        assertEquals("", FpsUnlockTier.FPS_90.getXmlUnlockFlags());
    }

    @Test
    public void unlockFlags_includeOnlyTiersAtOrBelow() {
        assertEquals("Unlock120Hz=1\n", FpsUnlockTier.FPS_120.getUnlockFlags());
        assertEquals("Unlock120Hz=1\nUnlock144Hz=1\n", FpsUnlockTier.FPS_144.getUnlockFlags());
        assertEquals("Unlock120Hz=1\nUnlock144Hz=1\nUnlock165Hz=1\nUnlock185Hz=1\n",
                FpsUnlockTier.FPS_185.getUnlockFlags());
        assertFalse(FpsUnlockTier.FPS_165.getUnlockFlags().contains("Unlock185Hz"));
    }

    @Test
    public void unlockFlags_formatsMatchPerVariant() {
        assertEquals("+CVars=r.Unlock120Hz=1\n", FpsUnlockTier.FPS_120.getUE4UnlockCVars());
        assertEquals("  \"Unlock120Hz\": 1,\n", FpsUnlockTier.FPS_120.getJsonUnlockFlags());
        assertEquals("  <int name=\"Unlock120Hz\" value=\"1\" />\n",
                FpsUnlockTier.FPS_120.getXmlUnlockFlags());
    }

    @Test
    public void unlockFlags_noDuplicateLineForSameHz() {
        // 185 must not emit a 185Hz flag twice
        int count = 0;
        for (String line : FpsUnlockTier.FPS_185.getUnlockFlags().split("\n")) {
            if (line.contains("Unlock185Hz")) count++;
        }
        assertEquals(1, count);
        assertTrue(FpsUnlockTier.FPS_185.getUnlockFlags().endsWith("\n"));
    }
}