package com.gamebooster.app.booster;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.config.CommonConfigTuningInjector;
import com.gamebooster.app.config.FpsUnlockTier;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.engine.VulkanRayTracingEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * NoLimitExtremeOverdriveEngine — Uncompromising No-Limit Hardware & 185 FPS Suite.
 *
 * Maximizes mobile hardware performance without artificial constraints:
 * 1. NO LIMIT FPS: Locks 165Hz – 185Hz display pipeline, eliminates SurfaceFlinger backpressure,
 *    nulls VSync phase delays, and latches unsignaled frames immediately.
 * 2. NO LIMIT CPU: Pins every CPU policy scaling_min_freq to scaling_max_freq with 'performance'
 *    governor; zeros /dev/cpu_dma_latency to abolish CPU C-state sleep between frames.
 * 3. NO LIMIT GPU: Max clocks and turbo burst for Qualcomm Adreno & ARM Mali/MediaTek chips.
 * 4. NO LIMIT THERMAL: Disables kernel thermal trip point throttling and locks PowerHAL fixed performance.
 * 5. ULTRA EXTREME GRAPHICS: Injects 185 FPS unlock keys, 120% render scale, AsyncCompute, and VRS.
 */
public final class NoLimitExtremeOverdriveEngine {

    private static final String TAG = "NoLimitOverdrive";
    private static volatile boolean sIsNoLimitActive = false;

    private NoLimitExtremeOverdriveEngine() {}

    public static boolean isNoLimitActive() {
        return sIsNoLimitActive;
    }

    /**
     * Engages the full No-Limit Hardware Overdrive suite for the active game.
     */
    public static void engageNoLimitOverdrive(Context context, String packageName, int targetFps) {
        final int finalFps = (targetFps >= 165) ? Math.min(185, targetFps) : 185;
        sIsNoLimitActive = true;

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                Log.i(TAG, "═══ ENGAGING NO-LIMIT EXTREME OVERDRIVE (" + finalFps + " FPS) ═══");

                // 1. Force Display Pipeline to 185Hz & SurfaceFlinger Uncapped
                forceSurfaceFlinger185(finalFps);

                // 2. Lock CPU Clocks to Max & Disable Idle Sleep
                lockCpuFrequenciesMax();

                // 3. Lock GPU Clocks & Turbo Burst
                lockGpuFrequenciesMax();

                // 4. Defeat All Thermal Throttling
                defeatAllThermalLimits();

                // 5. Inject Ultra Extreme Graphics & 185 FPS into Game Configs
                if (packageName != null && !packageName.trim().isEmpty()) {
                    injectUltraExtremeGraphics(context, packageName, finalFps);
                }

                // 6. 1000Hz Ultra Touch & Combat Engine
                CombatEngineChannel.enableCombatMode(context);

                // 7. Battery Bypass Charging Shield (if plugged into charger)
                if (BypassChargingController.isChargerConnected(context)) {
                    BypassChargingController.enableBypassCharging(context);
                }

                Log.i(TAG, "═══ NO-LIMIT EXTREME OVERDRIVE ACTIVE ═══");
            } catch (Throwable t) {
                Log.e(TAG, "Error during No-Limit Overdrive execution", t);
            }
        });
    }

    /**
     * Uncaps SurfaceFlinger display pipeline to 165Hz - 185Hz with zero buffer lag.
     */
    public static void forceSurfaceFlinger185(int hz) {
        // Apply standard MaxHz multi-layer forcing
        MaxHzForceChannel.forceApply(hz);

        List<String> sfCmds = new ArrayList<>();
        // Zero backpressure & latch immediately
        sfCmds.add("setprop debug.sf.disable_backpressure 1");
        sfCmds.add("setprop debug.sf.latch_unsignaled 1");
        sfCmds.add("setprop debug.sf.early_phase_offset_ns 0");
        sfCmds.add("setprop debug.sf.early_app_phase_offset_ns 0");
        sfCmds.add("setprop debug.sf.early_gl_phase_offset_ns 0");
        sfCmds.add("setprop debug.gr.swapinterval 0");
        sfCmds.add("setprop debug.egl.swapinterval 0");
        sfCmds.add("setprop debug.sf.fps_limit " + hz);
        sfCmds.add("setprop persist.sys.NV_FPSLIMIT " + hz);
        sfCmds.add("setprop persist.sys.game.fps " + hz);
        sfCmds.add("setprop persist.sys.game.rate " + hz);

        // Direct SurfaceFlinger binder overrides
        sfCmds.add("service call SurfaceFlinger 1035 i32 " + hz);
        sfCmds.add("service call SurfaceFlinger 1036 i32 " + hz);

        CommandExecutor.executeBatchCommands(sfCmds);
        Log.i(TAG, "SurfaceFlinger 185Hz display pipeline uncapped.");
    }

    /**
     * Locks CPU frequencies strictly to maximum hardware limits.
     * Prevents clock frequency drops even when scene load drops.
     */
    public static void lockCpuFrequenciesMax() {
        StringBuilder sb = new StringBuilder();

        // 1. Enforce performance governor and pin min_freq = max_freq
        sb.append("for p in /sys/devices/system/cpu/cpufreq/policy*; do ");
        sb.append("echo performance > \"$p/scaling_governor\" 2>/dev/null; ");
        sb.append("if [ -f \"$p/scaling_max_freq\" ]; then cat \"$p/scaling_max_freq\" > \"$p/scaling_min_freq\" 2>/dev/null; fi; ");
        sb.append("done; ");

        // 2. Prevent CPU deep C-state sleep (zero wake-up delay between frames)
        sb.append("echo 0 > /dev/cpu_dma_latency 2>/dev/null; ");

        // 3. Maximize task scheduling uclamp boost
        sb.append("echo 1024 > /dev/cpuset/top-app/uclamp.min 2>/dev/null; ");
        sb.append("echo 1024 > /dev/cpuset/top-app/uclamp.boosted 2>/dev/null; ");
        sb.append("echo 1024 > /dev/cpuset/foreground/uclamp.min 2>/dev/null; ");
        sb.append("echo 0-7 > /dev/cpuset/top-app/cpus 2>/dev/null; ");
        sb.append("echo 0-7 > /dev/cpuset/foreground/cpus 2>/dev/null; ");

        // 4. Disable energy aware scheduling (EAS downscaling)
        sb.append("echo 0 > /proc/sys/kernel/sched_energy_aware 2>/dev/null; ");
        sb.append("echo 0 > /sys/devices/system/cpu/eas/enable 2>/dev/null; ");

        CommandExecutor.executeSystemCommand(sb.toString());
        Log.i(TAG, "CPU cores locked to maximum burst frequencies.");
    }

    /**
     * Locks GPU frequencies to maximum clock speeds across Qualcomm & MediaTek chipsets.
     */
    public static void lockGpuFrequenciesMax() {
        List<String> gpuCmds = new ArrayList<>();

        // Qualcomm Adreno GPU Turbo Lock
        gpuCmds.add("echo 0 > /sys/class/kgsl/kgsl-3d0/thermal_pwrlevel 2>/dev/null");
        gpuCmds.add("echo 0 > /sys/class/kgsl/kgsl-3d0/throttling 2>/dev/null");
        gpuCmds.add("echo 1 > /sys/class/kgsl/kgsl-3d0/force_bus_on 2>/dev/null");
        gpuCmds.add("echo 1 > /sys/class/kgsl/kgsl-3d0/force_clk_on 2>/dev/null");
        gpuCmds.add("echo 1 > /sys/class/kgsl/kgsl-3d0/force_rail_on 2>/dev/null");
        gpuCmds.add("setprop debug.adreno.turbo 1");
        gpuCmds.add("setprop debug.adreno.perf_level 0");

        // ARM Mali / MediaTek MT6878 GPU Boost
        gpuCmds.add("setprop debug.mali.sched.priority -20");
        gpuCmds.add("setprop debug.mali.force_gpu_boost 1");
        gpuCmds.add("setprop vendor.gpu.power_mode 1");
        gpuCmds.add("setprop debug.gpu.performance 1");
        gpuCmds.add("echo always_on > /sys/class/misc/mali0/device/power_policy 2>/dev/null");
        gpuCmds.add("echo performance > /sys/devices/platform/13040000.mali/devfreq/13040000.mali/governor 2>/dev/null");

        CommandExecutor.executeBatchCommands(gpuCmds);
        Log.i(TAG, "GPU turbo clocks locked.");
    }

    /**
     * Completely disables thermal downclocking across kernel and framework layers.
     */
    public static void defeatAllThermalLimits() {
        // Universal Framework Thermal Bypass
        ThermalChannel.setThermalOverride(true);

        StringBuilder sb = new StringBuilder();
        // Disable Linux kernel thermal zones
        sb.append("for z in /sys/class/thermal/thermal_zone*; do echo disabled > \"$z/mode\" 2>/dev/null; done; ");
        // Elevate thermal trip points to 120C
        sb.append("for t in /sys/class/thermal/thermal_zone*/trip_point_*_temp; do echo 120000 > \"$t\" 2>/dev/null; done; ");

        CommandExecutor.executeSystemCommand(sb.toString());
        Log.i(TAG, "Thermal limits defeated.");
    }

    /**
     * Injects Ultra Extreme graphics config, 185 FPS unlock, and Vulkan optimizations.
     */
    public static void injectUltraExtremeGraphics(Context context, String packageName, int targetFps) {
        CommonConfigTuningInjector.applyUltraExtremeGraphics(packageName, targetFps);
        CommonConfigTuningInjector.applyHitRegistrationDpsBoost(packageName);
        CommonConfigTuningInjector.applyVulkanOptimization(packageName);
        VulkanRayTracingEngine.applyVulkanOptimizations(packageName);
        Log.i(TAG, "Ultra Extreme Graphics (185 FPS) injected for " + packageName);
    }
}
