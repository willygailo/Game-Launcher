package com.gamebooster.app.gamespace;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;

public class GameCacheCleaner {

    private static final String TAG = "GameCacheCleaner";

    public static boolean performDeepGameCacheClean(Context context) {
        try {
            Log.i(TAG, "Starting Deep Game Cache & Shader Storage Cleaning...");

            // Execute package cache trim & system cache purge
            CommandExecutor.executeSystemCommand("pm trim-caches 1000M");
            CommandExecutor.executeSystemCommand("rm -rf /data/local/tmp/*");
            CommandExecutor.executeSystemCommand("sync && echo 3 > /proc/sys/vm/drop_caches");
            CommandExecutor.executeSystemCommand("cmd package bg-dexopt-job");

            if (context != null) {
                java.io.File cacheDir = context.getCacheDir();
                if (cacheDir != null && cacheDir.isDirectory()) {
                    deleteDirContents(cacheDir);
                }
            }

            Log.i(TAG, "Deep Game Cache Cleaning Complete!");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Game Cache Clean Exception", e);
            return false;
        }
    }

    private static void deleteDirContents(java.io.File dir) {
        if (dir == null || !dir.isDirectory()) return;
        java.io.File[] files = dir.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                if (file.isDirectory()) {
                    deleteDirContents(file);
                }
                file.delete();
            }
        }
    }
}
