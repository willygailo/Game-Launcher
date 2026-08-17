package com.gamebooster.app.chipset;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * Unisoc Tiger (T606, T616, T618, T820) Hardware & Memory Subsystem.
 * Optimizes Mali / PowerVR GPU memory page locks, swap interval, and CPU governors.
 */
public class UnisocTuner {

    private static final String TAG = "UnisocTuner";

    public static boolean applyUnisocBoost(int targetFps) {
        Log.i(TAG, "⚡ Applying Unisoc Tiger Hardware Optimization (" + targetFps + " FPS Target)");

        String fpsStr = String.valueOf(targetFps);
        String[] unisocCommands = new String[]{
                // 1. GPU & Memory Page Optimization
                "setprop debug.hwui.renderer vulkan",
                "setprop debug.sf.hw 1",
                "setprop debug.gr.swapinterval 0",
                "setprop debug.egl.swapinterval 0",
                "setprop debug.sf.fps_limit " + fpsStr,

                // 2. Touch & Scheduler Boost
                "setprop view.touch_slop 0",
                "setprop persist.sys.touch.pressure.scale 0.0001",
                "setprop sys.use_fifo 1",

                // 3. Power Governor
                "cmd power set-mode 0 1",
                "cmd power set-mode 2 1"
        };

        ShizukuExecutor.executeShizukuCommands(unisocCommands);
        return true;
    }
}
