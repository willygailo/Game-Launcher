package com.gamebooster.app.config;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link GameConfigPathResolver}.
 * Verifies non-binary configuration file path validation and security blacklist filtering.
 */
public class GameConfigPathResolverTest {

    @Test
    public void testIsAcceptableConfigPath_validPaths() {
        assertTrue(GameConfigPathResolver.isAcceptableConfigPath(
                "/storage/emulated/0/Android/data/com.tencent.ig/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini"));
        assertTrue(GameConfigPathResolver.isAcceptableConfigPath(
                "/data/data/com.mobile.legends/shared_prefs/com.mobile.legends.v2.playerprefs.xml"));
        assertTrue(GameConfigPathResolver.isAcceptableConfigPath(
                "/storage/emulated/0/Android/data/com.activision.callofduty.shooter/files/configs/game_settings.json"));
        assertTrue(GameConfigPathResolver.isAcceptableConfigPath(
                "/sdcard/Android/data/com.mobile.legends/files/boot.config"));
        assertTrue(GameConfigPathResolver.isAcceptableConfigPath(
                "/sdcard/Android/data/com.riotgames.league.wildrift/files/SaveData/Local/Settings.cfg"));
    }

    @Test
    public void testIsAcceptableConfigPath_blacklistedDirectories() {
        assertFalse("Native library directory should be rejected",
                GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.mobile.legends/lib/libil2cpp.so"));
        assertFalse("Cache directory should be rejected",
                GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.mobile.legends/cache/temp_config.ini"));
        assertFalse("Code cache directory should be rejected",
                GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.mobile.legends/code_cache/dex.ini"));
        assertFalse("Crashlytics directory should be rejected",
                GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.game/crashlytics/crash.json"));
        assertFalse("Engine assets audio directory should be rejected",
                GameConfigPathResolver.isAcceptableConfigPath("/sdcard/Android/data/com.game/files/assets/audio/sound.ini"));
    }

    @Test
    public void testIsAcceptableConfigPath_blacklistedBinaryExtensions() {
        assertFalse("Shared library .so rejected",
                GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.game/files/test.so"));
        assertFalse("APK package rejected",
                GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.game/files/base.apk"));
        assertFalse("Unity asset bundle rejected",
                GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.game/files/scene.unity3d"));
        assertFalse("Raw byte stream rejected",
                GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.game/files/manifest.bytes"));
        assertFalse("OBB archive rejected",
                GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.game/files/main.obb"));
    }

    @Test
    public void testIsAcceptableConfigPath_manifestAndVerificationFiles() {
        assertFalse("MD5 file rejected",
                GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.game/files/files_md5.txt"));
        assertFalse("ResCheck file rejected",
                GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.game/files/rescheck.dat"));
        assertFalse("Checksum file rejected",
                GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.game/files/patch_checksum.ini"));
    }

    @Test
    public void testIsAcceptableConfigPath_xmlStrictRule() {
        // XML in assets must be rejected (game manifests, not configs)
        assertFalse("XML in assets must be rejected",
                GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.game/files/assets/version.xml"));

        // XML in shared_prefs or named playerprefs/settings is accepted
        assertTrue("XML in shared_prefs is accepted",
                GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.game/shared_prefs/app_prefs.xml"));
        assertTrue("Playerprefs XML is accepted",
                GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.game/files/com.game.playerprefs.xml"));
        assertTrue("Settings XML is accepted",
                GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.game/files/settings.xml"));

        // Generic arbitrary XML outside prefs is rejected
        assertFalse("Arbitrary non-preference XML rejected",
                GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.game/files/layout_data.xml"));
    }

    @Test
    public void testIsAcceptableConfigPath_nullAndEmpty() {
        assertFalse(GameConfigPathResolver.isAcceptableConfigPath(null));
        assertFalse(GameConfigPathResolver.isAcceptableConfigPath(""));
        assertFalse(GameConfigPathResolver.isAcceptableConfigPath("   "));
    }
}
