package com.gamebooster.app.chipset;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * Qualcomm Snapdragon Hardware & Driver Tuning Subsystem.
 * Unlocks Adreno Turbo, KGSL GPU frequency governor locks, Vulkan pipeline,
 * and low-latency QTI touch sampling.
 */
public class QualcommTuner {

    private static final String TAG = "QualcommTuner";

    public static boolean applySnapdragonBoost(int targetFps) {
        Log.i(TAG, "⚡ Applying Qualcomm Snapdragon Optimization Engine (" + targetFps + " FPS Target)");

        String fpsStr = String.valueOf(targetFps);
        String[] qcomCommands = new String[]{
                // 1. Adreno GPU Turbo & Performance Governor
                "setprop debug.adreno.turbo 1",
                "setprop debug.adreno.perf_level 0",
                "setprop vendor.gpu.perf.level 0",
                "setprop debug.egl.hw 1",
                "setprop debug.egl.force_msaa 1",
                "setprop debug.hwui.renderer vulkan",
                "setprop debug.renderengine.backend vulkan",
                "setprop debug.hwui.use_gpu_pixel_buffers true",
                "setprop debug.renderengine.skia_pipeline true",

                // 2. Qualcomm KGSL & GPU Frequency Driver
                "setprop vendor.gpu.perf.mode 1",
                "setprop persist.vendor.qcom.subsys.thermal 0",
                "setprop vendor.display.enable_default_color_mode 1",

                // 3. QTI Touch & Motion Latency Accelerator
                "setprop vendor.perf.gesture.enable 1",
                "setprop persist.vendor.touch.sampling_rate 1000",
                "setprop debug.input.max_events_per_sec 1000",
                "setprop persist.sys.touch.pressure.scale 0.0001",
                "setprop view.touch_slop 0",

                // 4. SurfaceFlinger & Frame Buffer Timing
                "setprop debug.sf.fps_limit " + fpsStr,
                "setprop persist.sys.NV_FPSLIMIT " + fpsStr,
                "setprop persist.sys.NV_POWERMODE 1",
                "setprop debug.gr.swapinterval 0",
                "setprop debug.egl.swapinterval 0",
                "setprop debug.sf.disable_backpressure 1",
                "setprop debug.sf.early_phase_offset_ns 0",
                "setprop debug.sf.early_app_phase_offset_ns 0",

                // 5. Qualcomm Hexagon DSP & PowerHAL Boost
                "cmd power set-fixed-performance-mode-enabled true",
                "cmd power set-mode 0 1",
                "cmd power set-mode 2 1"
        };

        ShizukuExecutor.executeShizukuCommands(qcomCommands);
        return true;
    }
}
