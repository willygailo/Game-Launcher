package com.gamebooster.app.feature.performance.booster;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.gamebooster.app.platform.shell.CommandExecutor;
import com.gamebooster.app.platform.shell.ShellExecutor;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RamZramChannel performs complete RAM and ZRAM memory optimization,
 * process trimming, pagecache clearing, and ZRAM heap compaction.
 *
 * <p>Uses 100% legal Android Framework APIs (ActivityManager killBackgroundProcesses),
 * Shizuku ADB shell package cache trimming (pm trim-caches), and kernel drop_caches/compact
 * when root privileges are available.</p>
 */
public class RamZramChannel {

    private static final String TAG = "RamZramChannel";

    /** Protected packages that must never be terminated during memory cleanup. */
    private static final Set<String> WHITELISTED_PACKAGES = new HashSet<>(Arrays.asList(
            "com.gamebooster.app",
            "moe.shizuku.privileged.api",
            "com.android.systemui",
            "com.google.android.inputmethod.latin",
            "com.discord",
            "com.whatsapp",
            "org.telegram.messenger",
            "com.facebook.orca"
    ));

    public static class MemoryStats {
        public final long totalRamMb;
        public final long availRamMbBefore;
        public final long availRamMbAfter;
        public final long freedRamMb;

        public MemoryStats(long totalRamMb, long availRamMbBefore, long availRamMbAfter) {
            this.totalRamMb = totalRamMb;
            this.availRamMbBefore = availRamMbBefore;
            this.availRamMbAfter = availRamMbAfter;
            this.freedRamMb = Math.max(0, availRamMbAfter - availRamMbBefore);
        }
    }

    public static MemoryStats optimizeMemory(Context context) {
        return optimizeMemory(context, null);
    }

    /**
     * Executes a full legal RAM & cache purge while protecting the currently active game.
     *
     * @param context Application context.
     * @param activeGamePackage Current foreground game package (optional, will be protected).
     * @return MemoryStats containing before/after memory readings and freed MB.
     */
    public static MemoryStats optimizeMemory(Context context, String activeGamePackage) {
        long totalRam = 0;
        long availBefore = 0;
        long availAfter = 0;

        if (context != null) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                am.getMemoryInfo(mi);
                totalRam = mi.totalMem / (1024 * 1024);
                availBefore = mi.availMem / (1024 * 1024);
            }
        }

        Log.i(TAG, "Initiating memory optimization... Initial Free RAM: " + availBefore + "MB / " + totalRam + "MB");

        // 1. Android Framework Cache Trimming via Shizuku ADB (pm trim-caches 2000M)
        if (ShizukuExecutor.hasShizukuPermission()) {
            try {
                String trimRes = ShizukuExecutor.executeShizukuCommand("pm trim-caches 2000M");
                Log.d(TAG, "Shizuku pm trim-caches: " + trimRes);
            } catch (Throwable t) {
                Log.w(TAG, "pm trim-caches failed", t);
            }
        }

        // 2. Kill background processes for non-whitelisted third-party apps
        if (context != null) {
            try {
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                PackageManager pm = context.getPackageManager();
                if (am != null && pm != null) {
                    List<ApplicationInfo> apps = pm.getInstalledApplications(0);
                    for (ApplicationInfo app : apps) {
                        if (app == null || app.packageName == null) continue;
                        String pkg = app.packageName;

                        // Never terminate system apps, whitelisted apps, or the active game
                        if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;
                        if (WHITELISTED_PACKAGES.contains(pkg)) continue;
                        if (activeGamePackage != null && pkg.equalsIgnoreCase(activeGamePackage)) continue;
                        if (pkg.equals(context.getPackageName())) continue;

                        try {
                            am.killBackgroundProcesses(pkg);
                        } catch (Throwable ignored) {}
                    }
                }
            } catch (Throwable t) {
                Log.w(TAG, "Background process cleanup error", t);
            }
        }

        // 3. Kernel PageCache & ZRAM Compaction (if root privileges available)
        if (ShellExecutor.isRootAvailable()) {
            try {
                ShellExecutor.executeRootCommand("sync && echo 3 > /proc/sys/vm/drop_caches && echo 1 > /sys/block/zram0/compact");
                Log.d(TAG, "Executed root kernel drop_caches and zram compaction");
            } catch (Throwable ignored) {}
        }

        // 4. Measure post-optimization memory
        if (context != null) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                am.getMemoryInfo(mi);
                availAfter = mi.availMem / (1024 * 1024);
            }
        }

        MemoryStats stats = new MemoryStats(totalRam, availBefore, availAfter);
        Log.i(TAG, "RAM Optimization Complete. Freed: " + stats.freedRamMb + "MB (Now " + stats.availRamMbAfter + "MB Free)");
        return stats;
    }

    public static boolean trimMemoryAndCleanCache(Context context) {
        return trimMemoryAndCleanCache(context, null);
    }

    public static boolean trimMemoryAndCleanCache(Context context, String activeGamePackage) {
        MemoryStats stats = optimizeMemory(context, activeGamePackage);
        return stats.availRamMbAfter > 0;
    }
}
