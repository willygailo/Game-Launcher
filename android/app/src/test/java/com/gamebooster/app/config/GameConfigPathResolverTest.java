package com.gamebooster.app.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class GameConfigPathResolverTest {

    // ─── known relative paths per game family ───────────────────────────────

    @Test
    public void mlbbPackage_resolvesDragon2017Paths() {
        List<String> rel = GameConfigPathResolver.getKnownRelativePathsForPackage("com.mobile.legends");
        assertTrue(rel.contains("files/dragon2017/assets/UI/Config/UserSystem.ini"));
        assertTrue(rel.contains("files/dragon2017/assets/UI/HighFPSConfig.ini"));
        assertTrue(rel.contains("shared_prefs/com.mobile.legends.v2.playerprefs.xml"));
    }

    @Test
    public void pubgFamilies_resolveUe4ShadowTrackerPaths() {
        List<String> rel = GameConfigPathResolver.getKnownRelativePathsForPackage("com.tencent.ig");
        assertTrue(rel.contains("files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini"));
        assertTrue(rel.contains("files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini"));
        assertTrue(rel.contains("files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav"));
    }

    @Test
    public void codFamily_resolvesJsonSettings() {
        List<String> rel = GameConfigPathResolver.getKnownRelativePathsForPackage("com.activision.callofduty.warzone");
        assertTrue(rel.contains("files/Config/UserSetting.json"));
        assertTrue(rel.contains("files/ControlsSettings.ini"));
    }

    @Test
    public void freeFireFamily_resolvesGraphicsIni() {
        assertTrue(GameConfigPathResolver.getKnownRelativePathsForPackage("com.dts.freefireth")
                .contains("files/FFGraphicsSettings.ini"));
    }

    @Test
    public void genshinFamily_resolvesConfigJson() {
        assertTrue(GameConfigPathResolver.getKnownRelativePathsForPackage("com.miHoYo.Yuanshen")
                .contains("files/Config/GameSettings.json"));
    }

    @Test
    public void wildRiftFamily_resolvesRiotPrefs() {
        List<String> rel = GameConfigPathResolver.getKnownRelativePathsForPackage("com.riotgames.league.wildrift");
        assertTrue(rel.contains("files/Saved/Config/Graphics.ini"));
        assertTrue(rel.contains("shared_prefs/RiotGames.xml"));
    }

    @Test
    public void hokFamily_resolvesDeviceHardwareIni() {
        assertTrue(GameConfigPathResolver.getKnownRelativePathsForPackage("com.tencent.sgame")
                .contains("files/DeviceHardware.ini"));
    }

    @Test
    public void unknownPackage_fallsBackToGenericProfiles() {
        List<String> rel = GameConfigPathResolver.getKnownRelativePathsForPackage("com.example.unknown.game");
        assertTrue(rel.contains("files/GameSettings.ini"));
        assertTrue(rel.contains("shared_prefs/com.example.unknown.game_preferences.xml"));
        assertEquals(4, rel.size());
    }

    @Test
    public void carxAndFarlightVariants_resolve() {
        assertTrue(GameConfigPathResolver.getKnownRelativePathsForPackage("com.glofta9hm.f2p")
                .contains("files/GraphicSettings.ini"));
        assertTrue(GameConfigPathResolver.getKnownRelativePathsForPackage("com.farlight84.solarland")
                .contains("files/UE4Game/Solarland/Solarland/Saved/Config/Android/GameUserSettings.ini"));
    }

    // ─── base path generation (data-dir / media variants / private spaces) ──

    @Test
    public void generateBasePaths_coversAllStorageLayouts() {
        List<String> roots = GameConfigPathResolver.generateBasePaths("com.mobile.legends");
        assertEquals(12, roots.size());
        assertTrue(roots.contains("/sdcard/Android/data/com.mobile.legends"));
        assertTrue(roots.contains("/storage/emulated/0/Android/data/com.mobile.legends"));
        assertTrue(roots.contains("/storage/emulated/10/Android/data/com.mobile.legends"));
        assertTrue(roots.contains("/storage/emulated/11/Android/data/com.mobile.legends"));
        assertTrue(roots.contains("/storage/emulated/999/Android/data/com.mobile.legends"));
        assertTrue(roots.contains("/data/data/com.mobile.legends"));
        assertTrue(roots.contains("/data/user/0/com.mobile.legends"));
        assertTrue(roots.contains("/data/user/10/com.mobile.legends"));
        assertTrue(roots.contains("/data/user/11/com.mobile.legends"));
        assertTrue(roots.contains("/data/user/999/com.mobile.legends"));
        assertTrue(roots.contains("/sdcard/Android/media/com.mobile.legends"));
        assertTrue(roots.contains("/storage/emulated/0/Android/media/com.mobile.legends"));
        for (String root : roots) {
            assertEquals(root, root.replace("//", "/"));
            assertTrue(root.endsWith("com.mobile.legends"));
        }
    }

    @Test
    public void resolveConfigPaths_blankPackage_returnsEmpty() {
        assertTrue(GameConfigPathResolver.resolveConfigPaths("", null).isEmpty());
        assertTrue(GameConfigPathResolver.resolveConfigPaths("   ", null).isEmpty());
        assertTrue(GameConfigPathResolver.resolveConfigPaths(null, java.util.Collections.emptyList()).isEmpty());
    }

    @Test
    public void resolveConfigPaths_generatesEveryRootVariant() {
        List<String> paths = GameConfigPathResolver.resolveConfigPaths(
                "com.example.genericgame", java.util.Collections.singletonList("files/GameSettings.ini"));
        // 12 roots incl. data/media/private-space variants
        assertEquals(12, paths.size());
        assertTrue(paths.contains("/data/data/com.example.genericgame/files/GameSettings.ini"));
        assertTrue(paths.contains("/data/user/999/com.example.genericgame/files/GameSettings.ini"));
        assertTrue(paths.contains("/storage/emulated/0/Android/media/com.example.genericgame/files/GameSettings.ini"));
    }

    @Test
    public void resolveConfigPaths_deduplicatesRepeatedRelatives() {
        List<String> rel = new java.util.ArrayList<>();
        rel.add("files/GameSettings.ini");
        rel.add("files/GameSettings.ini");
        assertEquals(12, GameConfigPathResolver.resolveConfigPaths("com.example.dup", rel).size());
    }

    @Test
    public void resolveConfigPaths_handlesLeadingSlashOnRelative() {
        List<String> paths = GameConfigPathResolver.resolveConfigPaths(
                "com.example.slash", java.util.Collections.singletonList("/files/x.ini"));
        assertEquals(12, paths.size());
        assertTrue(paths.contains("/data/data/com.example.slash/files/x.ini"));
    }

    // ─── cache behavior ──────────────────────────────────────────────────────

    @Test
    public void getPathsForGame_cachesAndClears() {
        GameConfigPathResolver.clearCache();
        List<String> first = GameConfigPathResolver.getPathsForGame("com.mobile.legends");
        assertFalse(first.isEmpty());
        assertEquals(first, GameConfigPathResolver.getPathsForGame("com.mobile.legends"));
        GameConfigPathResolver.clearCache();
        assertEquals(first, GameConfigPathResolver.getPathsForGame("com.mobile.legends"));
    }

    @Test
    public void getPathsForGame_lowercasesPackage() {
        GameConfigPathResolver.clearCache();
        List<String> upper = GameConfigPathResolver.getPathsForGame("COM.MOBILE.LEGENDS");
        assertTrue(upper.contains("/data/data/com.mobile.legends/files/dragon2017/assets/UI/Config/UserSystem.ini"));
    }
}