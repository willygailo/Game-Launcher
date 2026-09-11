package com.gamebooster.app.config;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GameConfigStorageAccessEngineTest {

    @Test
    public void testIsFileLikePath() {
        assertTrue(GameConfigStorageAccessEngine.isFileLikePath("/sdcard/Android/data/com.pubg.imobile/files/UE4Game/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini"));
        assertTrue(GameConfigStorageAccessEngine.isFileLikePath("/data/data/com.mobile.legends/shared_prefs/com.mobile.legends.v2.playerprefs.xml"));
        assertTrue(GameConfigStorageAccessEngine.isFileLikePath("/sdcard/Android/data/com.tencent.ig/files/Active.sav"));
        assertTrue(GameConfigStorageAccessEngine.isFileLikePath("/sdcard/Android/data/com.activision.callofduty.shooter/files/config.json"));
        assertTrue(GameConfigStorageAccessEngine.isFileLikePath("/sdcard/Android/data/com.dts.freefireth/files/settings.cfg"));
        assertTrue(GameConfigStorageAccessEngine.isFileLikePath("/sdcard/Android/data/game/data.dat"));
        assertTrue(GameConfigStorageAccessEngine.isFileLikePath("/sdcard/Android/data/game/boot.config"));
        assertTrue(GameConfigStorageAccessEngine.isFileLikePath("/sdcard/Android/data/game/options.properties"));

        assertFalse(GameConfigStorageAccessEngine.isFileLikePath("/sdcard/Android/data/com.pubg.imobile/files"));
        assertFalse(GameConfigStorageAccessEngine.isFileLikePath("/data/data/com.tencent.ig"));
        assertFalse(GameConfigStorageAccessEngine.isFileLikePath(null));
    }

    @Test
    public void testStorageAccessModeEnum() {
        assertEquals("Elevated Shizuku ADB Access", GameConfigStorageAccessEngine.StorageAccessMode.SHIZUKU_ELEVATED.getDisplayName());
        assertEquals("Storage Access Framework (SAF) Tree", GameConfigStorageAccessEngine.StorageAccessMode.SAF_DOCUMENT_TREE.getDisplayName());
        assertEquals("App-Private Internal Storage", GameConfigStorageAccessEngine.StorageAccessMode.DIRECT_APP_PRIVATE.getDisplayName());
        assertEquals("Restricted Scoped Storage", GameConfigStorageAccessEngine.StorageAccessMode.RESTRICTED.getDisplayName());
    }

    @Test
    public void testResolveAllStoragePaths() {
        List<String> emptyPaths = GameConfigStorageAccessEngine.resolveAllStoragePaths(null, "");
        assertTrue(emptyPaths.isEmpty());

        List<String> paths = GameConfigStorageAccessEngine.resolveAllStoragePaths(null, "com.tencent.ig");
        assertNotNull(paths);
        assertFalse(paths.isEmpty());

        boolean hasDataData = false;
        boolean hasAndroidData = false;
        for (String p : paths) {
            if (p.contains("/data/data/com.tencent.ig") || p.contains("/data/user/0/com.tencent.ig")) {
                hasDataData = true;
            }
            if (p.contains("Android/data/com.tencent.ig")) {
                hasAndroidData = true;
            }
        }
        assertTrue(hasDataData);
        assertTrue(hasAndroidData);
    }

    @Test
    public void testVerifyAccessNullHandling() {
        GameConfigStorageAccessEngine.StorageAccessReport report = GameConfigStorageAccessEngine.verifyAccess(null, null);
        assertNotNull(report);
        assertEquals("", report.packageName);
        assertEquals(GameConfigStorageAccessEngine.StorageAccessMode.RESTRICTED, report.accessMode);
        assertEquals(0, report.totalPathsResolved);
    }
}
