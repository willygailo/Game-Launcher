package com.gamebooster.app.cleaner;

import com.gamebooster.app.cleaner.model.CleanResult;
import com.gamebooster.app.cleaner.model.JunkCategory;
import com.gamebooster.app.cleaner.model.JunkItem;
import com.gamebooster.app.cleaner.model.JunkScanResult;
import com.gamebooster.app.cleaner.scanner.ScanFilter;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CleanerUnitTest {

    @Test
    public void testScanFilterJunkExtensions() {
        assertTrue(ScanFilter.isDisposableJunkPath("/sdcard/Download/temp_installer.tmp"));
        assertTrue(ScanFilter.isDisposableJunkPath("/sdcard/crash_dump.dmp"));
        assertTrue(ScanFilter.isDisposableJunkPath("/sdcard/app.log"));
        assertTrue(ScanFilter.isDisposableJunkPath("/sdcard/Download/bigfile.crdownload"));
        assertTrue(ScanFilter.isDisposableJunkPath("/sdcard/Download/partial.part"));
        assertTrue(ScanFilter.isDisposableJunkPath("/sdcard/Android/data/com.game/cache/image.thumb"));
    }

    @Test
    public void testScanFilterProtectedExtensions() {
        assertFalse(ScanFilter.isDisposableJunkPath("/sdcard/Documents/my_thesis.docx"));
        assertFalse(ScanFilter.isDisposableJunkPath("/sdcard/DCIM/Camera/photo.jpg"));
        assertFalse(ScanFilter.isDisposableJunkPath("/sdcard/Music/song.mp3"));
        assertFalse(ScanFilter.isDisposableJunkPath("/sdcard/Movies/video.mp4"));
        assertFalse(ScanFilter.isDisposableJunkPath("/sdcard/games/savegame.sav"));
        assertFalse(ScanFilter.isDisposableJunkPath("/sdcard/data/game.db"));
    }

    @Test
    public void testScanFilterThumbnailsAndLogs() {
        assertTrue(ScanFilter.isThumbnailFileOrDir(new File("/sdcard/DCIM/.thumbnails")));
        assertTrue(ScanFilter.isThumbnailFileOrDir(new File("/sdcard/Pictures/.trash")));
        assertTrue(ScanFilter.isGameLogOrResidual(new File("/sdcard/Android/data/com.tencent.ig/files/UE4Game/Saved/Logs/UE4.log")));
        assertTrue(ScanFilter.isGameLogOrResidual(new File("/sdcard/Android/data/com.dts.freefireth/files/FF_Log/trace.log")));
    }

    @Test
    public void testJunkScanResultAggregation() {
        JunkScanResult result = new JunkScanResult();
        result.addItem(new JunkItem("/sdcard/test.tmp", "Test Temp", 1024L * 1024L * 50L, JunkCategory.TEMP_FILES, true));
        result.addItem(new JunkItem("/sdcard/app_cache", "App Cache", "com.example.app", 1024L * 1024L * 100L, JunkCategory.APP_CACHE, true));

        assertEquals(1024L * 1024L * 150L, result.getTotalBytes());
        assertEquals(1024L * 1024L * 150L, result.getSelectedBytes());
        assertEquals(2, result.getItems().size());
        assertEquals(1024L * 1024L * 50L, result.getCategorySize(JunkCategory.TEMP_FILES));
        assertEquals(1024L * 1024L * 100L, result.getCategorySize(JunkCategory.APP_CACHE));

        // Deselect one item
        result.getItems().get(0).setSelected(false);
        assertEquals(1024L * 1024L * 100L, result.getSelectedBytes());
    }

    @Test
    public void testCleanResultFormatting() {
        List<String> logs = new ArrayList<>();
        logs.add("Initial Available Storage: 10.0 GB");
        logs.add("Clean Completed in 120ms.");

        CleanResult result = new CleanResult(true, 1024L * 1024L * 500L, 42, 120L, logs);
        assertTrue(result.isSuccess());
        assertEquals(1024L * 1024L * 500L, result.getBytesFreed());
        assertEquals(42, result.getFilesDeletedCount());
        assertEquals(120L, result.getDurationMs());
        assertEquals(2, result.getLogs().size());
    }

    @Test
    public void testFormatBytes() {
        assertEquals("0.0 MB", JunkScanResult.formatBytes(0));
        assertEquals("500 B", JunkScanResult.formatBytes(500));
        assertEquals("1.0 KB", JunkScanResult.formatBytes(1024));
        assertEquals("1.0 MB", JunkScanResult.formatBytes(1024 * 1024));
        assertEquals("1.50 GB", JunkScanResult.formatBytes((long) (1.5 * 1024 * 1024 * 1024)));
    }
}
