package com.gamebooster.app.gamespace;

import android.content.Context;

/**
 * Universal Game Cache & Memory Cleaner for Game Launcher PRO.
 * Delegates to JankAndCacheCleanerEngine for full legal jank and cache elimination.
 */
public class GameCacheCleaner {

    public static boolean performDeepGameCacheClean(Context context) {
        JankAndCacheCleanerEngine.cleanJankAndCacheAsync(context, null);
        return true;
    }
}
