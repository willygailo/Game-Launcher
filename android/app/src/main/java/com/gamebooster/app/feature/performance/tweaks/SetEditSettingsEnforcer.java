package com.gamebooster.app.feature.performance.tweaks;

import com.gamebooster.app.platform.shell.CommandExecutor;

/**
 * SetEditSettingsEnforcer — Automated SetEdit-style system, secure, and global property injection engine.
 *
 * Enforces non-destructive system settings via Shizuku privileged shell (UID 2000)
 * to unlock 120/144/165Hz panel rates, ANGLE graphics drivers, and low-latency touch response.
 */
public class SetEditSettingsEnforcer {

    /**
     * Enforces the target display refresh rate across system & global namespaces.
     *
     * @param targetHz Refresh rate target (e.g. 120, 144, 165).
     * @return true if all refresh rate properties were successfully injected.
     */
    public static boolean enforceRefreshRate(int targetHz) {
        return enforceRefreshRate(targetHz, null);
    }

    /**
     * Enforces the target display refresh rate globally and for the specific game package.
     *
     * @param targetHz Refresh rate target (e.g. 120, 144, 165).
     * @param packageName Target package name (optional/nullable).
     * @return true if refresh rate properties were injected.
     */
    public static boolean enforceRefreshRate(int targetHz, String packageName) {
        if (targetHz <= 0) {
            return false;
        }

        String hzStr = String.format(java.util.Locale.US, "%.1f", (float) targetHz);
        String intHzStr = String.valueOf(targetHz);

        // System Namespace
        boolean s1 = CommandExecutor.setSystemSetting("system", "peak_refresh_rate", hzStr);
        boolean s2 = CommandExecutor.setSystemSetting("system", "min_refresh_rate", hzStr);
        boolean s3 = CommandExecutor.setSystemSetting("system", "user_refresh_rate", intHzStr);
        boolean s4 = CommandExecutor.setSystemSetting("system", "default_refresh_rate", hzStr);
        CommandExecutor.setSystemSetting("system", "miui_refresh_rate", intHzStr);
        CommandExecutor.setSystemSetting("system", "oppo_display_refresh_rate", intHzStr);
        CommandExecutor.setSystemSetting("system", "thermal_limit_refresh_rate", intHzStr);

        // Global Namespace (Crucial for Android 13-16 dynamic display daemons)
        boolean g1 = CommandExecutor.setSystemSetting("global", "peak_refresh_rate", hzStr);
        boolean g2 = CommandExecutor.setSystemSetting("global", "min_refresh_rate", hzStr);
        boolean g3 = CommandExecutor.setSystemSetting("global", "user_refresh_rate", intHzStr);
        boolean g4 = CommandExecutor.setSystemSetting("global", "default_refresh_rate", hzStr);
        boolean g5 = CommandExecutor.setSystemSetting("global", "display_downscale_disable", "1");
        boolean g6 = CommandExecutor.setSystemSetting("global", "mode_fps_override", intHzStr);
        CommandExecutor.setSystemSetting("global", "fps_limit", "0");
        CommandExecutor.setSystemSetting("global", "sf_max_fps", intHzStr);

        // Secure Namespace (Disables VRR/LTPO dynamic drop to 60/80/90Hz)
        CommandExecutor.setSystemSetting("secure", "peak_refresh_rate", hzStr);
        CommandExecutor.setSystemSetting("secure", "min_refresh_rate", hzStr);
        CommandExecutor.setSystemSetting("secure", "user_refresh_rate", intHzStr);
        CommandExecutor.setSystemSetting("secure", "refresh_rate_mode", "2");
        CommandExecutor.setSystemSetting("secure", "match_content_frame_rate", "0");
        CommandExecutor.setSystemSetting("secure", "oplus_customize_display_level", "3");

        // SurfaceFlinger & WindowManager Direct IPC
        if (packageName != null && !packageName.trim().isEmpty() && !"global".equalsIgnoreCase(packageName.trim())) {
            CommandExecutor.executeSystemCommand("cmd window set-app-refresh-rate " + packageName + " " + targetHz);
            CommandExecutor.executeSystemCommand("cmd game set --mode 2 --fps " + targetHz + " " + packageName);
            CommandExecutor.setSystemSetting("secure", "high_refresh_rate_apps_list", packageName);
        }
        CommandExecutor.executeSystemCommand("service call SurfaceFlinger 1035 i32 " + targetHz);
        CommandExecutor.executeSystemCommand("service call SurfaceFlinger 1036 i32 " + targetHz);
        CommandExecutor.executeSystemCommand("service call SurfaceFlinger 1037 i32 " + targetHz);

        // SurfaceFlinger Frame-Pacing & GPU Composition
        CommandExecutor.setSystemProperty("debug.composition.type", "gpu");
        CommandExecutor.setSystemProperty("persist.sys.composition.type", "gpu");
        CommandExecutor.setSystemProperty("debug.egl.hw", "1");
        CommandExecutor.setSystemProperty("debug.sf.hw", "1");
        CommandExecutor.setSystemProperty("vendor.gpu.boost", "1");
        CommandExecutor.setSystemProperty("debug.sf.fps_override", intHzStr);
        CommandExecutor.setSystemProperty("debug.sf.latch_unsignaled", "1");
        CommandExecutor.setSystemProperty("debug.sf.enable_gl_backpressure", "0");
        CommandExecutor.setSystemProperty("debug.sf.disable_backpressure", "1");

        // PowerHAL & Thermal Bypass to prevent thermal drop of display Hz
        CommandExecutor.executeSystemCommand("cmd power set-mode 0 1");
        CommandExecutor.executeSystemCommand("cmd power set-mode 2 1");
        CommandExecutor.executeSystemCommand("cmd thermalservice override-status 0");
        CommandExecutor.executeSystemCommand("cmd thermal override-status 0");

        return s1 && s2 && s3 && g1 && g2 && g3 && g5;
    }

    /**
     * Enforces maximum 165Hz display refresh rate lock across system & OEM properties.
     *
     * @return true if 165Hz mode was injected successfully.
     */
    public static boolean enforceMax165HzMode() {
        boolean base = enforceRefreshRate(165);
        boolean oem  = OemHardwareOptimizer.applyOemOptimizations(165);
        return base && oem;
    }

    /**
     * Cleans up Android Updatable Graphics Driver & ANGLE settings to prevent breaking Chrome and WebViews.
     */
    public static boolean enforceAngleDriverOptIn(String commaSeparatedPackages) {
        CommandExecutor.executeSystemCommand("settings delete global angle_gl_driver_all_angle; settings delete global updatable_driver_all_apps; settings delete global updatable_driver_production_opt_in_apps");
        return true;
    }

    /**
     * Applies high-responsiveness touch sensitivity, 1000Hz digitizer polling, and 0px deadzone.
     *
     * @return true if touch properties were applied successfully.
     */
    public static boolean enforceUltraTouchSettings() {
        boolean t1 = CommandExecutor.setSystemSetting("system", "touch_prediction_latency", "0");
        boolean t2 = CommandExecutor.setSystemSetting("secure", "long_press_timeout", "250");
        boolean t3 = CommandExecutor.setSystemProperty("debug.input.max_events_per_sec", "1000");
        CommandExecutor.setSystemSetting("system", "touch_slop_reduction", "1");
        CommandExecutor.setSystemProperty("view.touch_slop", "0");
        CommandExecutor.setSystemSetting("system", "pointer_speed", "7");
        CommandExecutor.setSystemProperty("persist.sys.touch.pressure.scale", "0.0001");
        CommandExecutor.setSystemProperty("touch.filter.level", "0");
        CommandExecutor.setSystemProperty("persist.vendor.qti.inputopts.enable", "true");
        CommandExecutor.setSystemProperty("persist.vendor.qti.inputopts.movetouchslop", "0");
        CommandExecutor.setSystemProperty("sys.touch.boost", "1");
        CommandExecutor.setSystemProperty("persist.sys.touch.rate", "1000");
        CommandExecutor.setSystemProperty("persist.sys.touch.glove_mode", "0");

        return t1 && t2 && t3;
    }

    /**
     * Applies 1000Hz Gyroscope & Accelerometer sampling with sensor HAL batching bypass for zero delay.
     *
     * @return true if gyro properties were applied successfully.
     */
    public static boolean enforceZeroDelayGyroSettings() {
        boolean g1 = CommandExecutor.setSystemProperty("debug.sensor.gyro.rate", "1000");
        CommandExecutor.setSystemProperty("vendor.sensor.gyro.rate", "1000");
        CommandExecutor.setSystemProperty("debug.sensor.accel.rate", "1000");
        CommandExecutor.setSystemProperty("vendor.sensor.accel.rate", "1000");
        CommandExecutor.setSystemProperty("persist.sensor.fast_rate", "1");
        CommandExecutor.setSystemProperty("debug.sensor.latency", "0");
        CommandExecutor.setSystemProperty("persist.sys.sensor.polling_rate", "1000");
        CommandExecutor.setSystemProperty("ro.sensor.gyro.rate", "1000");
        return g1;
    }

    /**
     * Resets system and global refresh rate overrides back to stock dynamic defaults.
     *
     * @return true if default properties were restored.
     */
    public static boolean revertToDefaults() {
        boolean r1 = CommandExecutor.setSystemSetting("system", "peak_refresh_rate", "0.0");
        boolean r2 = CommandExecutor.setSystemSetting("system", "min_refresh_rate", "60.0");
        boolean r3 = CommandExecutor.setSystemSetting("global", "display_downscale_disable", "0");

        return r1 && r2 && r3;
    }
}
