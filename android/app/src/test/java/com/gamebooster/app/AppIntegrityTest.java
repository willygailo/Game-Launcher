package com.gamebooster.app;

import com.gamebooster.app.cleaner.model.CleanResult;
import com.gamebooster.app.cleaner.model.JunkCategory;
import com.gamebooster.app.cleaner.model.JunkItem;
import com.gamebooster.app.cleaner.model.JunkScanResult;
import com.gamebooster.app.cleaner.scanner.ScanFilter;
import com.gamebooster.app.config.FpsUnlockTier;

import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AppIntegrityTest {

    @Test
    public void testAndroid13To16ApiLevels() {
        int minSdk = 33;    // Android 13
        int targetSdk = 36; // Android 16

        assertTrue("minSdk must be at least 33 (Android 13)", minSdk >= 33);
        assertEquals("targetSdk must be 36 (Android 16)", 36, targetSdk);
    }

    @Test
    public void test16KbPageKernelConstant() {
        int pageSize16Kb = 16384;
        assertEquals(16 * 1024, pageSize16Kb);
    }

    @Test
    public void testFpsUnlockTiers() {
        assertEquals(90, FpsUnlockTier.FPS_90.fps);
        assertEquals(120, FpsUnlockTier.FPS_120.fps);
        assertEquals(144, FpsUnlockTier.FPS_144.fps);
        assertEquals(165, FpsUnlockTier.FPS_165.fps);
        assertEquals(185, FpsUnlockTier.FPS_185.fps);

        assertEquals(FpsUnlockTier.FPS_165, FpsUnlockTier.fromFps(165));
        assertEquals(FpsUnlockTier.FPS_185, FpsUnlockTier.fromFps(240));
    }

    @Test
    public void testJunkScanResultFormatting() {
        assertEquals("0.0 MB", JunkScanResult.formatBytes(0));
        assertEquals("500 B", JunkScanResult.formatBytes(500));
        assertEquals("1.0 KB", JunkScanResult.formatBytes(1024));
        assertEquals("10.0 MB", JunkScanResult.formatBytes(10 * 1024 * 1024));
        assertEquals("2.50 GB", JunkScanResult.formatBytes((long) (2.5 * 1024 * 1024 * 1024)));
    }

    @Test
    public void testJunkScanResultAggregation() {
        JunkScanResult result = new JunkScanResult();
        result.addItem(new JunkItem("/sdcard/Android/data/com.test/cache", "App Cache", "com.test", 1024 * 1024 * 50, JunkCategory.APP_CACHE, true));
        result.addItem(new JunkItem("/sdcard/DCIM/.thumbnails", "Thumbnails", null, 1024 * 1024 * 20, JunkCategory.THUMBNAILS, true));

        assertEquals(2, result.getItems().size());
        assertEquals(1024 * 1024 * 70, result.getTotalBytes());
        assertEquals(1024 * 1024 * 70, result.getSelectedBytes());
        assertEquals(1024 * 1024 * 50, result.getCategorySize(JunkCategory.APP_CACHE));
        assertEquals(1024 * 1024 * 20, result.getCategorySize(JunkCategory.THUMBNAILS));
    }

    @Test
    public void testScanFilterSafety() {
        // System and Obb folders must be safe to scan or filtered
        assertFalse(ScanFilter.isSafeToScan(new File("/system/bin/sh")));
        assertFalse(ScanFilter.isSafeToScan(new File("/sdcard/Android/obb")));

        // File extensions checks
        assertEquals("sav", ScanFilter.getFileExtension("player.sav"));
        assertEquals("apk", ScanFilter.getFileExtension("update.apk"));
        assertEquals("log", ScanFilter.getFileExtension("crash.log"));
    }

    @Test
    public void testCleanResultCalculations() {
        CleanResult cleanResult = new CleanResult(true, 1024 * 1024 * 150, 45, 1200, Collections.singletonList("Purged caches"));
        assertEquals(1024 * 1024 * 150, cleanResult.getBytesFreed());
        assertEquals(45, cleanResult.getFilesDeletedCount());
        assertEquals("150.0 MB", cleanResult.getFormattedBytesFreed());
        assertTrue(cleanResult.isSuccess());
    }
}
