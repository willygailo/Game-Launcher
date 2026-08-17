package com.gamebooster.app.gamespace;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.io.File;

/**
 * Universal Game Cache & Memory Cleaner for Game Launcher PRO.
 * Performs deep memory compaction, shader cache purge, and temporary script buffer cleanup.
 */
public class GameCacheCleaner {

    private static final String TAG = "GameCacheCleaner";

    public static boolean performDeepGameCacheClean(Context context) {
        try {
            Log.i(TAG, "Starting Deep Game Cache & Shader Storage Cleaning...");

            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand("pm trim-caches 999999999999");
                ShizukuExecutor.executeShizukuCommand("sync && echo 3 > /proc/sys/vm/drop_caches 2>/dev/null");
                ShizukuExecutor.executeShizukuCommand("echo 1 > /proc/sys/vm/compact_memory 2>/dev/null");
                ShizukuExecutor.executeShizukuCommand("rm -f /data/local/tmp/gamebooster_*.sh");
            } else {
                CommandExecutor.executeSystemCommand("pm trim-caches 1000M");
                CommandExecutor.executeSystemCommand("rm -rf /data/local/tmp/gamebooster_*.sh");
            }

            if (context != null) {
                File cacheDir = context.getCacheDir();
                if (cacheDir != null && cacheDir.isDirectory()) {
                    deleteDirContents(cacheDir);
                }
                File extCache = context.getExternalCacheDir();
                if (extCache != null && extCache.isDirectory()) {
                    deleteDirContents(extCache);
                }
            }

            Log.i(TAG, "Deep Game Cache Cleaning Complete!");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Game Cache Clean Exception", e);
            return false;
        }
    }

    private static void deleteDirContents(File dir) {
        if (dir == null || !dir.isDirectory()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirContents(file);
                }
                file.delete();
            }
        }
    }
}
