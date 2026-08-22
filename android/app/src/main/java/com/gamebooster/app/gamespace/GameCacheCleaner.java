package com.gamebooster.app.gamespace;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;

import java.io.File;

public class GameCacheCleaner {

    private static final String TAG = "GameCacheCleaner";

    public static boolean performDeepGameCacheClean(Context context) {
        try {
            Log.i(TAG, "Starting Deep Game Cache & Shader Storage Cleaning...");

            // Execute package cache trim & system cache purge
            CommandExecutor.executeSystemCommand("pm trim-caches 4096M");
            CommandExecutor.executeSystemCommand("rm -rf /data/local/tmp/* 2>/dev/null");
            CommandExecutor.executeSystemCommand("rm -rf /data/anr/* 2>/dev/null");
            CommandExecutor.executeSystemCommand("rm -rf /data/tombstones/* 2>/dev/null");
            CommandExecutor.executeSystemCommand("sync && echo 3 > /proc/sys/vm/drop_caches");
            CommandExecutor.executeSystemCommand("cmd package bg-dexopt-job");

            if (context != null) {
                File cacheDir = context.getCacheDir();
                if (cacheDir != null && cacheDir.isDirectory()) {
                    deleteDirContents(cacheDir);
                }

                File codeCacheDir = context.getCodeCacheDir();
                if (codeCacheDir != null && codeCacheDir.isDirectory()) {
                    deleteDirContents(codeCacheDir);
                }

                File[] extCaches = context.getExternalCacheDirs();
                if (extCaches != null) {
                    for (File extCache : extCaches) {
                        if (extCache != null && extCache.isDirectory()) {
                            deleteDirContents(extCache);
                        }
                    }
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
