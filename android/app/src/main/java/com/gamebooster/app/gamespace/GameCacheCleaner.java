package com.gamebooster.app.gamespace;
import com.gamebooster.app.config.*;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;

public class GameCacheCleaner {

    private static final String TAG = "GameCacheCleaner";

    public static boolean performDeepGameCacheClean(Context context) {
        try {
            Log.i(TAG, "Starting Deep Game Cache & Shader Storage Cleaning...");

            String[] gamePackages = new String[]{
                "com.mobile.legends",
                "com.mobile.legends.vng",
                "com.activision.callofduty.shooter",
                "com.garena.game.codm",
                "com.tencent.ig",
                "com.pubg.imobile",
                "com.vng.pubgmobile",
                "com.dts.freefireth",
                "com.dts.freefiremax",
                "com.riotgames.league.wildrift",
                "com.cognosphere.GenshinImpact"
            };

            for (String pkg : gamePackages) {
                String cmd = "rm -rf /data/data/" + pkg + "/cache/* /data/data/" + pkg + "/code_cache/* " +
                        "/sdcard/Android/data/" + pkg + "/cache/* " +
                        "find /data/data/" + pkg + "/ -name '*.shader' -delete; " +
                        "find /data/data/" + pkg + "/ -name '*.spv' -delete";
                if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
                    com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand(cmd);
                } else {
                    CommandExecutor.executeSystemCommand(cmd);
                }
            }

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

            Log.i(TAG, "Deep Game Cache & Shader Purge Complete!");
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
