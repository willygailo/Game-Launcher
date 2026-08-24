package com.gamebooster.app;

import com.gamebooster.app.cleaner.model.CleanResult;
import com.gamebooster.app.cleaner.model.JunkCategory;
import com.gamebooster.app.cleaner.model.JunkItem;
import com.gamebooster.app.cleaner.model.JunkScanResult;
import com.gamebooster.app.cleaner.scanner.ScanFilter;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class JunkCleanerTest {

    @Test
    public void testJunkCategories() {
        assertNotNull(JunkCategory.APP_CACHE);
        assertNotNull(JunkCategory.SYSTEM_CACHE);
        assertNotNull(JunkCategory.RESIDUAL_UNINSTALLED);
        assertNotNull(JunkCategory.SOCIAL_CACHE);
        assertNotNull(JunkCategory.THUMBNAILS);
        assertNotNull(JunkCategory.OBSOLETE_APKS);
        assertNotNull(JunkCategory.GAME_RESIDUALS);

        for (JunkCategory cat : JunkCategory.values()) {
            assertNotNull(cat.getTitle());
            assertNotNull(cat.getDescription());
            assertNotNull(cat.getIconEmoji());
            assertTrue(cat.getColorResId() != 0);
        }
    }

    @Test
    public void testJunkScanResultAggregation() {
        JunkScanResult scanResult = new JunkScanResult();

        scanResult.addItem(new JunkItem("/sdcard/Android/data/com.test.app/cache", "App Cache", "com.test.app", 1024 * 1024 * 10, JunkCategory.APP_CACHE, true));
        scanResult.addItem(new JunkItem("/sdcard/DCIM/.thumbnails/thumb1.jpg", "Thumbnail", 1024 * 500, JunkCategory.THUMBNAILS, true));
        scanResult.addItem(new JunkItem("/sdcard/Android/data/com.old.game", "Residual Game Folder", "com.old.game", 1024 * 1024 * 50, JunkCategory.RESIDUAL_UNINSTALLED, true));

        assertEquals(3, scanResult.getItems().size());
        assertTrue(scanResult.getTotalBytes() > 0);
        assertEquals(scanResult.getTotalBytes(), scanResult.getSelectedBytes());

        // Toggle selection
        scanResult.setCategorySelected(JunkCategory.THUMBNAILS, false);
        assertFalse(scanResult.isCategorySelected(JunkCategory.THUMBNAILS));
        assertTrue(scanResult.getSelectedBytes() < scanResult.getTotalBytes());
    }

    @Test
    public void testScanFilterSafety() {
        // Protected file extensions
        File saveFile = new File("/sdcard/Game/save.sav");
        assertFalse("Save files must never be marked as disposable junk", ScanFilter.isDisposableJunkFile(saveFile));

        File photo = new File("/sdcard/DCIM/Camera/IMG_2026.jpg");
        assertFalse("User photos must never be disposable junk", ScanFilter.isDisposableJunkFile(photo));

        File doc = new File("/sdcard/Documents/contract.pdf");
        assertFalse("User documents must never be disposable junk", ScanFilter.isDisposableJunkFile(doc));

        // Junk file extensions
        File tempFile = new File("/sdcard/temp/crash_dump.dmp");
        assertTrue("Crash dump should be disposable junk", ScanFilter.isDisposableJunkFile(tempFile));

        File logFile = new File("/sdcard/Download/app.log");
        assertTrue("Log file should be disposable junk", ScanFilter.isDisposableJunkFile(logFile));
    }

    @Test
    public void testCleanResultModel() {
        List<String> logs = new ArrayList<>();
        logs.add("Storage Delta Freed: 150 MB");
        logs.add("Clean completed in 450ms");

        CleanResult result = new CleanResult(true, 1024 * 1024 * 150, 42, 450, logs);
        assertTrue(result.isSuccess());
        assertEquals(1024 * 1024 * 150, result.getBytesFreed());
        assertEquals(42, result.getFilesDeletedCount());
        assertEquals(450, result.getDurationMs());
        assertNotNull(result.getFormattedBytesFreed());
    }
}
