package com.gamebooster.app.booster;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.LinkedHashSet;
import java.util.Set;

public class GpuTweaksChannel {

    public static final String TARGET_GAMES_PACKAGES =
            "com.mobile.legends,com.mobilelegends.mi,com.vng.mlbbvn,com.mobilelegends.na,com.mobilelegends.hw,com.mobile.legends.moonton,com.mobile.legends.kr,com.mobile.legends.jp," +
            "com.tencent.ig,com.pubg.imobile,com.vng.pubgmobile,com.pubg.krmobile,com.rekoo.pubgm,com.tencent.tmgp.pubgmhd,com.tencent.iglite,com.pubg.newstate,com.tencent.tmgp.pubgm," +
            "com.activision.callofduty.shooter,com.garena.game.codm,com.tencent.tmgp.kr.codm,com.vng.codmvn,com.tencent.tmgp.cod,com.activision.callofduty.warzone," +
            "com.dts.freefireth,com.dts.freefiremax," +
            "com.miHoYo.GenshinImpact,com.cognosphere.GenshinImpact,com.HoYoverse.hkrpgoversea,com.HoYoverse.nap,com.miHoYo.bh3oversea,com.kurogame.wutheringwaves.global," +
            "com.levelinfinite.sgameGlobal,com.levelinfinite.sgameGlobal.gpkg,com.tencent.tmgp.sgame,com.garena.game.kgtw,com.garena.game.kgvn,com.garena.game.kgid,com.riotgames.league.wildrift," +
            "com.roblox.client,com.riotgames.valorant.mobile,com.riotgames.valorantmobile,com.tencent.tmgp.projectc,com.farlightgames.farlight84.android,com.miracle.farlight84," +
            "com.netease.bloodstrike,com.netease.newspike,com.axlebolt.standoff2," +
            "com.h20.carxstreet,com.gameloft.anmp.android.glofta9hm,com.ea.games.r3_row,com.garena.game.fdtw," +
            "com.proximabeta.mf.uamo,com.levelinfinite.deltaforce," +
            "com.supercell.brawlstars,com.supercell.clashroyale,com.supercell.clashofclans,com.supercell.squad";

    /**
     * Dynamically compiles the complete CSV of target game packages from GamePackageRegistry.
     */
    public static String getTargetGamesCsv() {
        Set<String> set = new LinkedHashSet<>();
        set.addAll(GamePackageRegistry.getAllKnownGames().keySet());
        for (String p : TARGET_GAMES_PACKAGES.split(",")) {
            String clean = p.trim();
            if (!clean.isEmpty()) set.add(clean);
        }
        return String.join(",", set);
    }

    public static boolean enableVulkanRenderer() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemProperty("debug.hwui.renderer", "vulkan");
        ok &= CommandExecutor.setSystemProperty("debug.renderengine.backend", "vulkan");
        ok &= CommandExecutor.setSystemProperty("debug.renderengine.skia_pipeline", "true");
        ok &= CommandExecutor.setSystemProperty("debug.hwui.use_gpu_pixel_buffers", "true");
        ok &= CommandExecutor.setSystemProperty("debug.hwui.render_thread_priority", "-20");
        ok &= CommandExecutor.setSystemProperty("debug.sf.hw", "1");

        // Apply Vulkan Game Overlay to all registered games on Android 13+
        for (String pkg : GamePackageRegistry.getAllKnownGames().keySet()) {
            try {
                CommandExecutor.executeSystemCommand("device_config put game_overlay " + pkg + " mode=2,fps=185:mode=3,fps=185");
            } catch (Throwable ignored) {}
        }
        return ok;
    }

    public static boolean enableAdrenoTurbo() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemProperty("debug.adreno.turbo", "1");
        ok &= CommandExecutor.setSystemProperty("debug.adreno.perf_level", "0");
        ok &= CommandExecutor.setSystemProperty("debug.qualcomm.sns.hal", "0");
        ok &= CommandExecutor.setSystemProperty("vendor.perf.gestureFlingBoost", "1");
        ok &= CommandExecutor.setSystemProperty("persist.vendor.qti.games.gt.enable", "1");
        ok &= CommandExecutor.setSystemProperty("vendor.gpu.power_mode", "1");

        // Sysfs GPU devfreq clock and power rail locks for Adreno (Snapdragon)
        CommandExecutor.executeSystemCommand(
                "echo performance > /sys/class/kgsl/kgsl-3d0/devfreq/governor 2>/dev/null; " +
                "echo 0 > /sys/class/kgsl/kgsl-3d0/min_pwrlevel 2>/dev/null; " +
                "echo 1 > /sys/class/kgsl/kgsl-3d0/force_bus_on 2>/dev/null; " +
                "echo 1 > /sys/class/kgsl/kgsl-3d0/force_clk_on 2>/dev/null; " +
                "echo 1 > /sys/class/kgsl/kgsl-3d0/force_rail_on 2>/dev/null"
        );
        return ok;
    }

    public static boolean enableMediaTekGedBoost() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemProperty("debug.mali.sched.priority", "-20");
        ok &= CommandExecutor.setSystemProperty("debug.mali.force_gpu_boost", "1");
        ok &= CommandExecutor.setSystemProperty("debug.mali.realtime", "1");
        ok &= CommandExecutor.setSystemProperty("persist.vendor.ged.boost", "1");
        ok &= CommandExecutor.setSystemProperty("persist.vendor.dpt.enable", "1");
        ok &= CommandExecutor.setSystemProperty("vendor.ppt.boost", "1");

        // MediaTek GPU Engine Driver (GED) kernel game mode & PID boost
        CommandExecutor.executeSystemCommand(
                "echo 0 > /sys/class/misc/mali0/device/dvfs_enable 2>/dev/null; " +
                "echo 1 > /sys/module/ged/parameters/gx_game_mode 2>/dev/null; " +
                "echo 1 > /sys/module/ged/parameters/gx_boost_on 2>/dev/null; " +
                "echo 1 > /sys/module/ged/parameters/gx_force_cpu_boost 2>/dev/null; " +
                "echo 100 > /sys/module/ged/parameters/gx_top_app_pid_boost 2>/dev/null; " +
                "for g in /sys/class/devfreq/*gpu*/governor; do echo performance > \"$g\" 2>/dev/null; done"
        );
        return ok;
    }

    public static boolean enableTensorBoost() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemProperty("debug.tensor.gpu.boost", "1");
        return ok;
    }

    public static boolean enableExynosXclipseBoost() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemProperty("debug.exynos.performance.mode", "1");
        ok &= CommandExecutor.setSystemProperty("debug.xclipse.gpu.boost", "1");
        CommandExecutor.executeSystemCommand("echo 1 > /sys/devices/platform/17000000.gpu/power/control 2>/dev/null");
        return ok;
    }

    public static boolean enableForceMsaa() {
        return CommandExecutor.setSystemProperty("debug.egl.force_msaa", "1");
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

    public enum GraphicsDriverType {
        DEFAULT("Default System Driver"),
        GAME_DRIVER("Vulkan Game Driver (Hardware Accelerated)"),
        ANGLE_VULKAN("ANGLE (OpenGL over Vulkan)");

        public final String label;
        GraphicsDriverType(String label) {
            this.label = label;
        }
    }

    public static boolean setGpuMaxPerformance() {
        boolean ok = enableVulkanRenderer();
        ok &= enableAdrenoTurbo();
        ok &= applyExtendedAdrenoFlags();
        ok &= enableMediaTekGedBoost();
        ok &= applyExtendedMediaTekFlags();
        ok &= enableTensorBoost();
        ok &= applyExtendedTensorFlags();
        ok &= enableExynosXclipseBoost();
        ok &= enableForceMsaa();
        return ok;
    }

    /**
     * Applies graphics driver for a single target game package without affecting global applications.
     */
    public static boolean setTargetGameDriver(String packageName, GraphicsDriverType driverType) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String pkg = packageName.trim();

        // Always delete/clear global ANGLE switches to prevent game crashes
        CommandExecutor.executeSystemCommand("settings delete global angle_gl_driver_selection_pkgs 2>/dev/null");
        CommandExecutor.executeSystemCommand("settings delete global angle_gl_driver_selection_values 2>/dev/null");
        CommandExecutor.executeSystemCommand("settings delete global angle_enabled_pkgs 2>/dev/null");
        CommandExecutor.executeSystemCommand("settings put global angle_gl_driver_all_angle 0 2>/dev/null");
        CommandExecutor.executeSystemCommand("settings put global game_driver_all_apps 0 2>/dev/null");
        CommandExecutor.executeSystemCommand("settings put global updatable_driver_all_apps 0 2>/dev/null");

        if (driverType == GraphicsDriverType.GAME_DRIVER) {
            CommandExecutor.executeSystemCommand("settings put global game_driver_opt_in_apps " + pkg);
            CommandExecutor.executeSystemCommand("settings put global game_driver_prerelease_opt_in_apps " + pkg);
            CommandExecutor.executeSystemCommand("settings put global updatable_driver_production_opt_in_apps " + pkg);
            return true;
        } else {
            // Revert back to default
            CommandExecutor.executeSystemCommand("settings put global game_driver_opt_in_apps \"\"");
            CommandExecutor.executeSystemCommand("settings put global updatable_driver_production_opt_in_apps \"\"");
            return true;
        }
    }

    public static boolean setAngleMode(boolean enabled) {
        // Purge unstable ANGLE layer
        CommandExecutor.setSystemProperty("debug.angle.backend", "0");
        CommandExecutor.executeSystemCommand("settings delete global angle_gl_driver_selection_pkgs 2>/dev/null");
        CommandExecutor.executeSystemCommand("settings delete global angle_gl_driver_selection_values 2>/dev/null");
        CommandExecutor.executeSystemCommand("settings delete global angle_enabled_pkgs 2>/dev/null");
        CommandExecutor.executeSystemCommand("settings put global angle_gl_driver_all_angle 0 2>/dev/null");
        return true;
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
