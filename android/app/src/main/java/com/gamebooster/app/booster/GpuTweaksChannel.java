package com.gamebooster.app.booster;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.LinkedHashSet;
import java.util.Set;

public class GpuTweaksChannel {

    /**
     * Dedicated packages eligible for Game Driver opt-in.
     * Exclusively includes:
     * - Mobile Legends: Bang Bang (MLBB) & Regional / Store Editions
     * - Call of Duty: Mobile (CODM) & Regional Editions & Warzone Mobile
     * - PUBG Mobile (PUBGM) & Regional Editions (BGMI, VNG, KR/JP, Peacekeeper Elite)
     */
    public static final String GAME_DRIVER_PACKAGES_MLBB_CODM_PUBGM =
            // Mobile Legends: Bang Bang (MLBB) & Regional Variants
            "com.mobile.legends,com.mobilelegends.mi,com.vng.mlbbvn,com.mobile.legends.vng,com.mobilelegends.na,com.mobilelegends.hw,com.mobile.legends.moonton,com.mobile.legends.kr,com.mobile.legends.jp," +
            // Call of Duty: Mobile (CODM) & Regional Variants & Warzone Mobile
            "com.activision.callofduty.shooter,com.garena.game.codm,com.tencent.tmgp.kr.codm,com.vng.codmvn,com.tencent.tmgp.cod,com.activision.callofduty.warzone," +
            // PUBG Mobile & Regional Variants & BGMI
            "com.tencent.ig,com.pubg.imobile,com.vng.pubgmobile,com.pubg.krmobile,com.rekoo.pubgm,com.tencent.tmgp.pubgmhd,com.tencent.iglite,com.pubg.newstate,com.tencent.tmgp.pubgm";

    public static final String TARGET_GAMES_PACKAGES = GAME_DRIVER_PACKAGES_MLBB_CODM_PUBGM;

    /**
     * Returns true ONLY if the package is a known MLBB, CODM, or PUBGM release.
     */
    public static boolean isGameDriverEligible(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String target = packageName.trim().toLowerCase();
        for (String p : GAME_DRIVER_PACKAGES_MLBB_CODM_PUBGM.split(",")) {
            if (target.equals(p.trim().toLowerCase())) return true;
        }
        return false;
    }

    /**
     * Compiles the CSV of target game packages strictly for Game Driver opt-in (MLBB, CODM, PUBGM only).
     */
    public static String getTargetGamesCsv() {
        Set<String> set = new LinkedHashSet<>();
        for (String p : GAME_DRIVER_PACKAGES_MLBB_CODM_PUBGM.split(",")) {
            String clean = p.trim();
            if (!clean.isEmpty()) set.add(clean);
        }
        return String.join(",", set);
    }

    public static boolean enableVulkanRenderer() {
        CommandExecutor.setSystemProperty("debug.hwui.renderer", "vulkan");
        CommandExecutor.setSystemProperty("debug.renderengine.backend", "vulkan");
        CommandExecutor.setSystemProperty("debug.renderengine.skia_pipeline", "true");
        CommandExecutor.setSystemProperty("debug.hwui.use_gpu_pixel_buffers", "true");
        CommandExecutor.setSystemProperty("debug.hwui.render_thread_priority", "-20");
        CommandExecutor.setSystemProperty("debug.sf.hw", "1");

        // Apply Vulkan Game Overlay to all registered games on Android 13+
        for (String pkg : GamePackageRegistry.getAllKnownGames().keySet()) {
            try {
                CommandExecutor.executeSystemCommand("device_config put game_overlay " + pkg + " mode=2,useAngle=false,fps=185:mode=3,useAngle=false,fps=185");
            } catch (Throwable ignored) {}
        }
        return true;
    }

    public static boolean enableAdrenoTurbo() {
        CommandExecutor.setSystemProperty("debug.adreno.turbo", "1");
        CommandExecutor.setSystemProperty("debug.adreno.perf_level", "0");
        CommandExecutor.setSystemProperty("debug.qualcomm.sns.hal", "0");
        CommandExecutor.setSystemProperty("vendor.perf.gestureFlingBoost", "1");
        CommandExecutor.setSystemProperty("persist.vendor.qti.games.gt.enable", "1");
        CommandExecutor.setSystemProperty("vendor.gpu.power_mode", "1");

        // Sysfs GPU devfreq clock and power rail locks for Adreno (Snapdragon)
        CommandExecutor.executeSystemCommand(
                "echo performance > /sys/class/kgsl/kgsl-3d0/devfreq/governor 2>/dev/null; " +
                "echo 0 > /sys/class/kgsl/kgsl-3d0/min_pwrlevel 2>/dev/null; " +
                "echo 1 > /sys/class/kgsl/kgsl-3d0/force_bus_on 2>/dev/null; " +
                "echo 1 > /sys/class/kgsl/kgsl-3d0/force_clk_on 2>/dev/null; " +
                "echo 1 > /sys/class/kgsl/kgsl-3d0/force_rail_on 2>/dev/null"
        );
        return true;
    }

    public static boolean enableMediaTekGedBoost() {
        CommandExecutor.setSystemProperty("debug.mali.sched.priority", "-20");
        CommandExecutor.setSystemProperty("debug.mali.force_gpu_boost", "1");
        CommandExecutor.setSystemProperty("debug.mali.realtime", "1");
        CommandExecutor.setSystemProperty("persist.vendor.ged.boost", "1");
        CommandExecutor.setSystemProperty("persist.vendor.dpt.enable", "1");
        CommandExecutor.setSystemProperty("vendor.ppt.boost", "1");

        // MediaTek GPU Engine Driver (GED) kernel game mode & PID boost
        CommandExecutor.executeSystemCommand(
                "echo 0 > /sys/class/misc/mali0/device/dvfs_enable 2>/dev/null; " +
                "echo 1 > /sys/module/ged/parameters/gx_game_mode 2>/dev/null; " +
                "echo 1 > /sys/module/ged/parameters/gx_boost_on 2>/dev/null; " +
                "echo 1 > /sys/module/ged/parameters/gx_force_cpu_boost 2>/dev/null; " +
                "echo 100 > /sys/module/ged/parameters/gx_top_app_pid_boost 2>/dev/null; " +
                "for g in /sys/class/devfreq/*gpu*/governor; do echo performance > \"$g\" 2>/dev/null; done"
        );
        return true;
    }

    public static boolean enableTensorBoost() {
        CommandExecutor.setSystemProperty("debug.tensor.gpu.boost", "1");
        return true;
    }

    public static boolean enableExynosXclipseBoost() {
        CommandExecutor.setSystemProperty("debug.exynos.performance.mode", "1");
        CommandExecutor.setSystemProperty("debug.xclipse.gpu.boost", "1");
        CommandExecutor.executeSystemCommand("echo 1 > /sys/devices/platform/17000000.gpu/power/control 2>/dev/null");
        return true;
    }

    public static boolean enableForceMsaa() {
        CommandExecutor.setSystemProperty("debug.egl.force_msaa", "1");
        return true;
    }

    /**
     * Extended Adreno (Snapdragon) GPU flags missing from enableAdrenoTurbo().
     *
     * Adds: max_pwrlevel=0 (pin GPU to highest P-state), thermal_pwrlevel=0
     * (prevent thermal governor from reducing P-state), devfreq min_freq=max_freq clock lock,
     * cframe_hint (Adreno content-frame workload prediction), preemption enable.
     */
    public static boolean applyExtendedAdrenoFlags() {
        StringBuilder sb = new StringBuilder();

        // Pin GPU to highest performance P-state from both ends (min and max)
        sb.append("echo 0 > /sys/class/kgsl/kgsl-3d0/max_pwrlevel 2>/dev/null; ");
        sb.append("echo 0 > /sys/class/kgsl/kgsl-3d0/thermal_pwrlevel 2>/dev/null; ");

        // Lock GPU devfreq clock floor == ceiling == max frequency
        sb.append("MAX_GPU_FREQ=$(cat /sys/class/kgsl/kgsl-3d0/devfreq/max_freq 2>/dev/null); ");
        sb.append("[ -n \"$MAX_GPU_FREQ\" ] && echo $MAX_GPU_FREQ > /sys/class/kgsl/kgsl-3d0/devfreq/min_freq 2>/dev/null; ");

        // Adreno content-frame hint: predicts next-frame GPU workload for lower latency
        sb.append("echo 1 > /sys/class/kgsl/kgsl-3d0/cframe_hint 2>/dev/null; ");

        // GPU preemption: allows command-buffer preemption — reduces multi-task stall
        sb.append("echo 1 > /sys/class/kgsl/kgsl-3d0/preemption 2>/dev/null; ");

        // Adreno: disable DCVS (Dynamic Clock and Voltage Scaling) during gameplay
        sb.append("echo 0 > /sys/class/kgsl/kgsl-3d0/dispatch_queue_length 2>/dev/null; ");
        sb.append("echo 1 > /sys/class/kgsl/kgsl-3d0/perfcounter 2>/dev/null; ");

        // QTI GPU sustained performance HAL hint via Shizuku
        com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommands(
            "setprop vendor.qti.hardware.gpu.perf 1 2>/dev/null",
            "setprop vendor.display.comp_mask 0 2>/dev/null",
            "setprop debug.adreno.cframe_hint 1 2>/dev/null",
            "setprop debug.adreno.preemption 1 2>/dev/null"
        );

        CommandExecutor.executeSystemCommand(sb.toString());
        return true;
    }

    /**
     * Extended MediaTek GED (GPU Engine Driver) flags missing from enableMediaTekGedBoost().
     *
     * Adds: fps_cap_margin, max_cpu_loading (disables CPU-load-based GPU freq reduction),
     * GED KPI monitoring, dvfs_margin_mode=0 (remove conservative DVFS headroom),
     * mali gralloc shared memory, MTK GPU power limit bypass.
     */
    public static boolean applyExtendedMediaTekFlags() {
        StringBuilder sb = new StringBuilder();

        // GED FPS cap margin — controls FPS headroom above target; 0 = no extra cap
        sb.append("echo 0 > /sys/module/ged/parameters/gx_fps_cap_margin 2>/dev/null; ");

        // Disable CPU-load-based GPU frequency reduction (keeps GPU at max regardless of CPU load)
        sb.append("echo 100 > /sys/module/ged/parameters/gx_max_cpu_loading 2>/dev/null; ");

        // Enable GED KPI monitoring — allows GED to make per-frame GPU boost decisions
        sb.append("echo 1 > /sys/module/ged/parameters/gx_is_GED_KPI_enabled 2>/dev/null; ");

        // dvfs_margin_mode=0: remove conservative DVFS frequency headroom
        sb.append("echo 0 > /sys/module/ged/parameters/gx_dvfs_margin_mode 2>/dev/null; ");

        // Mali shared-memory gralloc: reduces buffer copy overhead between CPU and GPU
        CommandExecutor.setSystemProperty("debug.mali.gralloc.shared", "1");

        // Disable MTK GPU power cap — allows GPU to reach TDP ceiling
        CommandExecutor.setSystemProperty("persist.vendor.mtkgpu.use_power_limit", "0");

        // MTK PPM (Policy and Power Manager) game scenario
        sb.append("echo 1 > /proc/ppm/policy/ut_ppm_game_cfg 2>/dev/null; ");

        CommandExecutor.executeSystemCommand(sb.toString());
        return true;
    }

    /**
     * Extended Google Tensor GPU flags missing from enableTensorBoost().
     *
     * Adds: Mali devfreq governor = performance, min_freq = max_freq clock lock,
     * SF phase offset duration mode (Tensor-tuned), PerfHAL sustained mode.
     */
    public static boolean applyExtendedTensorFlags() {
        StringBuilder sb = new StringBuilder();

        // Pin Tensor Mali GPU devfreq governor to performance
        sb.append("for f in /sys/class/devfreq/18000000.mali/governor; do echo performance > \"$f\" 2>/dev/null; done; ");
        // Also covers alternate Tensor GPU path (Pixel 8/9 series)
        sb.append("for f in /sys/class/devfreq/*mali*/governor; do echo performance > \"$f\" 2>/dev/null; done; ");

        // Lock Tensor Mali GPU clock floor = ceiling = max frequency
        sb.append("MAX_TENSOR_GPU=$(cat /sys/class/devfreq/18000000.mali/max_freq 2>/dev/null); ");
        sb.append("[ -n \"$MAX_TENSOR_GPU\" ] && echo $MAX_TENSOR_GPU > /sys/class/devfreq/18000000.mali/min_freq 2>/dev/null; ");

        // SurfaceFlinger phase offset mode tuned for Tensor's display pipeline
        CommandExecutor.setSystemProperty("debug.sf.use_phase_offsets_as_durations", "1");
        CommandExecutor.setSystemProperty("debug.sf.late.sf.duration", "10500000");
        CommandExecutor.setSystemProperty("debug.sf.late.app.duration", "20500000");

        // Google PerfHAL sustained performance enable
        CommandExecutor.setSystemProperty("vendor.perf.perfhal.enable", "1");

        CommandExecutor.executeSystemCommand(sb.toString());
        return true;
    }

    /**
     * Extended Samsung Exynos GPU flags (AMD RDNA Xclipse 950/940/920/530 & Mali G78/G77/G68).
     */
    public static boolean applyExtendedExynosFlags() {
        StringBuilder sb = new StringBuilder();
        // Xclipse & Exynos Mali GPU devfreq performance governor
        sb.append("for f in /sys/devices/platform/17000000.gpu/devfreq/*/governor; do echo performance > \"$f\" 2>/dev/null; done; ");
        sb.append("for f in /sys/class/devfreq/*gpu*/governor; do echo performance > \"$f\" 2>/dev/null; done; ");

        // Exynos power control lock
        sb.append("echo on > /sys/devices/platform/17000000.gpu/power/control 2>/dev/null; ");

        CommandExecutor.setSystemProperty("debug.xclipse.driver.mode", "1");
        CommandExecutor.setSystemProperty("debug.exynos.gos.disable", "1");

        CommandExecutor.executeSystemCommand(sb.toString());
        return true;
    }

    /**
     * HiSilicon Kirin GPU Boost (Maleoon 910 & Mali-G78/G76 on Kirin 9010/9000S/9000/990/980).
     */
    public static boolean enableKirinBoost() {
        CommandExecutor.setSystemProperty("debug.kirin.gpu.boost", "1");
        CommandExecutor.setSystemProperty("persist.sys.huawei.perf_mode", "1");
        CommandExecutor.setSystemProperty("persist.sys.hisilicon.game_mode", "1");
        CommandExecutor.setSystemProperty("persist.sys.performance", "1");
        return true;
    }

    /**
     * Extended HiSilicon Kirin GPU flags.
     */
    public static boolean applyExtendedKirinFlags() {
        StringBuilder sb = new StringBuilder();
        // Kirin Maleoon & Mali devfreq governor
        sb.append("for f in /sys/class/devfreq/gpufreq/governor; do echo performance > \"$f\" 2>/dev/null; done; ");
        sb.append("for f in /sys/class/devfreq/*mali*/governor; do echo performance > \"$f\" 2>/dev/null; done; ");
        sb.append("echo on > /sys/devices/platform/e82c0000.mali/power/control 2>/dev/null; ");

        CommandExecutor.executeSystemCommand(sb.toString());
        return true;
    }

    /**
     * UNISOC GPU Boost (Tiger T820/T760/T619/T618/T616/T612/T610/T606 Mali-G57/G52).
     */
    public static boolean enableUnisocBoost() {
        CommandExecutor.setSystemProperty("debug.unisoc.gpu.boost", "1");
        CommandExecutor.setSystemProperty("persist.sys.sprd.perf_mode", "1");
        CommandExecutor.setSystemProperty("persist.sys.sprd.game_mode", "1");
        CommandExecutor.setSystemProperty("debug.sprd.fps.boost", "1");
        CommandExecutor.setSystemProperty("persist.sys.sprd.highperf", "1");
        return true;
    }

    /**
     * Extended UNISOC GPU & DevFreq scene boost flags.
     */
    public static boolean applyExtendedUnisocFlags() {
        StringBuilder sb = new StringBuilder();
        // UNISOC Spreadtrum scene-frequency boost
        sb.append("echo 1 > /sys/class/devfreq/scene-frequency/sprd_governor/scene_boost 2>/dev/null; ");
        // UNISOC Mali devfreq governor lock
        sb.append("for f in /sys/class/devfreq/sprd-mali/governor; do echo performance > \"$f\" 2>/dev/null; done; ");
        sb.append("for f in /sys/class/devfreq/*mali*/governor; do echo performance > \"$f\" 2>/dev/null; done; ");
        sb.append("echo on > /sys/devices/platform/sprd-mali/power/control 2>/dev/null; ");

        CommandExecutor.executeSystemCommand(sb.toString());
        return true;
    }

    public enum GraphicsDriverType {
        DEFAULT("Default System Driver"),
        GAME_DRIVER("Vulkan Game Driver (MLBB / CODM / PUBGM Only)");

        public final String label;
        GraphicsDriverType(String label) {
            this.label = label;
        }
    }

    public static boolean setGpuMaxPerformance() {
        enableVulkanRenderer();
        enableAdrenoTurbo();
        applyExtendedAdrenoFlags();
        enableMediaTekGedBoost();
        applyExtendedMediaTekFlags();
        enableTensorBoost();
        applyExtendedTensorFlags();
        enableExynosXclipseBoost();
        applyExtendedExynosFlags();
        enableKirinBoost();
        applyExtendedKirinFlags();
        enableUnisocBoost();
        applyExtendedUnisocFlags();
        enableForceMsaa();
        return true;
    }

    /**
     * Applies graphics driver for a single target game package without affecting global applications.
     * Game Driver is strictly applied if the package is MLBB, CODM, or PUBGM; otherwise defaults to system driver.
     */
    public static boolean setTargetGameDriver(String packageName, GraphicsDriverType driverType) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String pkg = packageName.trim();

        // Always purge global ANGLE switches to prevent game crashes & latency
        purgeAngleDriver();
        CommandExecutor.executeSystemCommand("settings put global game_driver_all_apps 0 2>/dev/null");
        CommandExecutor.executeSystemCommand("settings put global updatable_driver_all_apps 0 2>/dev/null");

        if (driverType == GraphicsDriverType.GAME_DRIVER && isGameDriverEligible(pkg)) {
            CommandExecutor.executeSystemCommand("settings put global game_driver_opt_in_apps " + pkg);
            CommandExecutor.executeSystemCommand("settings put global game_driver_prerelease_opt_in_apps " + pkg);
            CommandExecutor.executeSystemCommand("settings put global updatable_driver_production_opt_in_apps " + pkg);
            return true;
        } else {
            // Revert back to default system driver for non-eligible or default-selected
            CommandExecutor.executeSystemCommand("settings put global game_driver_opt_in_apps \"\"");
            CommandExecutor.executeSystemCommand("settings put global updatable_driver_production_opt_in_apps \"\"");
            return true;
        }
    }

    /**
     * Purges and neutralizes ANGLE across the Android system globally.
     */
    public static boolean purgeAngleDriver() {
        CommandExecutor.setSystemProperty("debug.angle.backend", "0");
        CommandExecutor.executeSystemCommand("settings delete global angle_gl_driver_selection_pkgs 2>/dev/null");
        CommandExecutor.executeSystemCommand("settings delete global angle_gl_driver_selection_values 2>/dev/null");
        CommandExecutor.executeSystemCommand("settings delete global angle_enabled_pkgs 2>/dev/null");
        CommandExecutor.executeSystemCommand("settings delete global angle_defer_init 2>/dev/null");
        CommandExecutor.executeSystemCommand("settings put global angle_gl_driver_all_angle 0 2>/dev/null");
        return true;
    }

    public static boolean setAngleMode(boolean enabled) {
        // Enforce total purge of ANGLE driver regardless of legacy flag
        return purgeAngleDriver();
    }

    public static boolean setGameDriverMode(boolean enabled) {
        String targetCsv = getTargetGamesCsv();
        // Never allow global game driver to prevent crashing system apps
        CommandExecutor.executeSystemCommand("settings put global game_driver_all_apps 0");
        CommandExecutor.executeSystemCommand("settings put global updatable_driver_all_apps 0");

        if (enabled) {
            CommandExecutor.executeSystemCommand("settings put global game_driver_opt_in_apps " + targetCsv);
            CommandExecutor.executeSystemCommand("settings put global game_driver_prerelease_opt_in_apps " + targetCsv);
            String res = CommandExecutor.executeSystemCommand("settings put global updatable_driver_production_opt_in_apps " + targetCsv);
            return CommandExecutor.isSuccessOutput(res);
        } else {
            CommandExecutor.executeSystemCommand("settings put global game_driver_opt_in_apps \"\"");
            CommandExecutor.executeSystemCommand("settings put global game_driver_prerelease_opt_in_apps \"\"");
            String res = CommandExecutor.executeSystemCommand("settings put global updatable_driver_production_opt_in_apps \"\"");
            return CommandExecutor.isSuccessOutput(res);
        }
    }
}
