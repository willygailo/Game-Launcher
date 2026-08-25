package com.gamebooster.app.cleaner;

import com.gamebooster.app.cleaner.model.JunkCategory;
import com.gamebooster.app.cleaner.scanner.ScanFilter;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScanFilterTest {

    @Test
    public void testProtectedExtensionsAreNeverDisposableOutsideCache() {
        // User Photos and Media
        assertFalse(ScanFilter.isDisposableJunkPath("/sdcard/DCIM/Camera/IMG_20260825_123456.jpg"));
        assertFalse(ScanFilter.isDisposableJunkPath("/sdcard/Pictures/Family/photo.png"));
        assertFalse(ScanFilter.isDisposableJunkPath("/sdcard/Movies/Vacation.mp4"));
        assertFalse(ScanFilter.isDisposableJunkPath("/sdcard/Music/Song.mp3"));

        // User Documents
        assertFalse(ScanFilter.isDisposableJunkPath("/sdcard/Documents/Work/Report.pdf"));
        assertFalse(ScanFilter.isDisposableJunkPath("/sdcard/Documents/Notes.docx"));
        assertFalse(ScanFilter.isDisposableJunkPath("/sdcard/Documents/Spreadsheet.xlsx"));

        // Game Save Databases and Assets
        assertFalse(ScanFilter.isDisposableJunkPath("/sdcard/Android/data/com.mobile.legends/files/save.sav"));
        assertFalse(ScanFilter.isDisposableJunkPath("/sdcard/Android/data/com.tencent.ig/files/database.sqlite"));
        assertFalse(ScanFilter.isDisposableJunkPath("/sdcard/Android/obb/com.dts.freefireth/main.12345.com.dts.freefireth.obb"));
    }

    @Test
    public void testProtectedDirectoriesAreSafe() {
        assertFalse(ScanFilter.isSafeToScan(new File("/system/bin/app_process")));
        assertFalse(ScanFilter.isSafeToScan(new File("/vendor/lib64/libvulkan.so")));
        assertFalse(ScanFilter.isSafeToScan(new File("/sdcard/DCIM/Camera")));
        assertFalse(ScanFilter.isSafeToScan(new File("/sdcard/Documents/Tax")));

        // Thumbnail and trash folders within pictures should be safe to scan
        assertTrue(ScanFilter.isSafeToScan(new File("/sdcard/DCIM/.thumbnails")));
        assertTrue(ScanFilter.isSafeToScan(new File("/sdcard/Pictures/.trash")));
    }

    @Test
    public void testGenuineDisposableJunkIsRecognized() {
        // Temporary and log files
        assertTrue(ScanFilter.isDisposableJunkPath("/sdcard/Android/data/com.example/cache/http.tmp"));
        assertTrue(ScanFilter.isDisposableJunkPath("/sdcard/Android/data/com.tencent.ig/files/Saved/Logs/logcat_2026.log"));
        assertTrue(ScanFilter.isDisposableJunkPath("/data/local/tmp/dump_crash.dmp"));
        assertTrue(ScanFilter.isDisposableJunkPath("/data/anr/anr_2026.txt"));

        // App WebView and GPU Caches
        assertTrue(ScanFilter.isDisposableJunkPath("/sdcard/Android/data/com.example/cache/data_0"));
        assertTrue(ScanFilter.isDisposableJunkPath("/data/data/com.example/app_webview/Default/Cache/f_0001"));
    }

    @Test
    public void testCategorySafeDefaults() {
        // 100% Safe Categories are default selected
        assertTrue(JunkCategory.APP_CACHE.isDefaultSelected());
        assertTrue(JunkCategory.SYSTEM_CACHE.isDefaultSelected());
        assertTrue(JunkCategory.TEMP_FILES.isDefaultSelected());
        assertTrue(JunkCategory.THUMBNAILS.isDefaultSelected());

        // Potentially user-dependent items require conscious opt-in (default false)
        assertFalse(JunkCategory.OBSOLETE_APKS.isDefaultSelected());
        assertFalse(JunkCategory.EMPTY_FOLDERS.isDefaultSelected());
        assertFalse(JunkCategory.RESIDUAL_UNINSTALLED.isDefaultSelected());
    }
}
