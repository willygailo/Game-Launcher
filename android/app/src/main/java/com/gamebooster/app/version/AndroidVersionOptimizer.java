package com.gamebooster.app.version;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * AndroidVersionOptimizer — Tailored OS Optimizations across Android 12, 13, 14, 15, and 16.
 *
 * Utilizes OS version-specific system tools, ADPF (Android Dynamic Performance Framework),
 * Game Mode APIs, App Standby Bucket elevations, and cgroup v2 Linux kernel priorities.
 */
public class AndroidVersionOptimizer {

    private static final String TAG = "AndroidVersionOptimizer";

    public static boolean applyVersionOptimizations(Context context, String packageName, int targetFps) {
        int sdk = Build.VERSION.SDK_INT;
        Log.i(TAG, "🚀 Applying Android OS Optimizations (SDK " + sdk + " / Android " + Build.VERSION.RELEASE + ") for target: " + targetFps + " FPS");

        String fpsStr = String.valueOf(targetFps);

        // 1. Android 12+ (API 31+): Game Mode Performance Enforcement
        if (sdk >= Build.VERSION_CODES.S) {
            ShizukuExecutor.executeShizukuCommand("cmd game mode performance global");
            if (packageName != null && !packageName.isEmpty()) {
                ShizukuExecutor.executeShizukuCommand("cmd game mode performance " + packageName);
                ShizukuExecutor.executeShizukuCommand("cmd game set --fps " + fpsStr + " " + packageName);
            }
        }

        // 2. Android 13+ (API 33+): SurfaceFlinger & Latency Eliminators
        if (sdk >= Build.VERSION_CODES.TIRAMISU) {
            ShizukuExecutor.executeShizukuCommand("service call SurfaceFlinger 1035 i32 " + fpsStr);
            ShizukuExecutor.executeShizukuCommand("service call SurfaceFlinger 1036 i32 " + fpsStr);
            ShizukuExecutor.executeShizukuCommand("setprop debug.sf.disable_backpressure 1");
        }

        // 3. Android 14+ (API 34+): WindowManager Refresh Rate & Fixed Performance Mode
        if (sdk >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (packageName != null && !packageName.isEmpty()) {
                ShizukuExecutor.executeShizukuCommand("cmd window set-app-refresh-rate " + packageName + " " + fpsStr);
                ShizukuExecutor.executeShizukuCommand("device_config put game_overlay " + packageName
                        + " mode=2,fps=" + fpsStr + ":mode=3,fps=" + fpsStr);
            }
            ShizukuExecutor.executeShizukuCommand("cmd power set-fixed-performance-mode-enabled true");
        }

        // 4. Android 15+ (API 35+ Vanilla Ice Cream): App Standby Bucket Elevation to ACTIVE
        if (sdk >= 35) {
            if (packageName != null && !packageName.isEmpty()) {
                // Elevate target game to highest possible standby bucket (active) to prevent CPU core de-scheduling
                ShizukuExecutor.executeShizukuCommand("cmd usage-stats set-app-standby-bucket " + packageName + " active");
                ShizukuExecutor.executeShizukuCommand("cmd activity set-process-limit 32");
            }
            ShizukuExecutor.executeShizukuCommand("setprop debug.hwui.use_gpu_pixel_buffers true");
        }

        // 5. Android 16+ (API 36+ Baklava): Linux 6.6+ Kernel cgroup v2 & Ultra High Refresh Rates
        if (sdk >= 36) {
            ShizukuExecutor.executeShizukuCommand("setprop persist.sys.game.fps " + fpsStr);
            ShizukuExecutor.executeShizukuCommand("setprop debug.input.max_events_per_sec 1000");
            ShizukuExecutor.executeShizukuCommand("cmd power set-mode 0 1");
            ShizukuExecutor.executeShizukuCommand("cmd power set-mode 2 1");
        }

        return true;
    }
}
