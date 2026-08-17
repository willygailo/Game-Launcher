package com.gamebooster.app.chipset;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * MediaTek Dimensity & Helio Hardware & HyperEngine Tuning Subsystem.
 * Optimizes MediaTek PowerHAL, Mali GPU power policy, MiraVision frame rates,
 * and high-frequency CPU cluster affinity.
 */
public class MediaTekTuner {

    private static final String TAG = "MediaTekTuner";

    public static boolean applyMediaTekBoost(int targetFps) {
        Log.i(TAG, "⚡ Applying MediaTek Dimensity/Helio HyperEngine Optimization (" + targetFps + " FPS Target)");

        String fpsStr = String.valueOf(targetFps);
        String[] mtkCommands = new String[]{
                // 1. MediaTek HyperEngine & Turbo Tuning
                "setprop persist.vendor.game.turbo.enable 1",
                "setprop vendor.perf.profile 2",
                "setprop persist.vendor.powerhal.mode 3",
                "setprop persist.vendor.perf.fps_mode 1",

                // 2. ARM Mali / Immortalis GPU Turbo Engine
                "setprop vendor.mali.gpu.power_policy performance",
                "setprop debug.mali.force_high_performance 1",
                "setprop debug.hwui.renderer vulkan",
                "setprop debug.renderengine.backend vulkan",
                "setprop debug.sf.hw 1",

                // 3. MediaTek MiraVision Frame & Display Lock
                "setprop debug.mediatek.disp.hfr 1",
                "setprop persist.vendor.display.hfr.mode 1",
                "setprop ro.vendor.display.default_fps " + fpsStr,
                "setprop vendor.display.fps " + fpsStr,
                "setprop debug.sf.fps_limit " + fpsStr,

                // 4. MediaTek Low-Latency Touch Sampling
                "setprop persist.vendor.touch.sampling_rate 1000",
                "setprop vendor.touch.game_mode 1",
                "setprop view.touch_slop 0",
                "setprop sys.use_fifo 1",

                // 5. MediaTek Power Governor Boost
                "cmd power set-mode 0 1",
                "cmd power set-mode 2 1",
                "cmd power set-fixed-performance-mode-enabled true"
        };

        ShizukuExecutor.executeShizukuCommands(mtkCommands);
        return true;
    }
}
