package com.gamebooster.app.booster;
import com.gamebooster.app.config.*;

import com.gamebooster.app.engine.CommandExecutor;

public class GpuTweaksChannel {

    public static boolean enableVulkanRenderer() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemProperty("debug.hwui.renderer", "vulkan");
        ok &= CommandExecutor.setSystemProperty("debug.sf.hw", "1");
        ok &= CommandExecutor.setSystemProperty("debug.sf.latch_unsignaled", "1");
        return ok;
    }

    public static boolean enableForceMsaa() {
        return CommandExecutor.setSystemProperty("debug.egl.force_msaa", "1");
    }

    public static boolean setGpuMaxPerformance() {
        boolean ok = enableVulkanRenderer();
        ok &= enableForceMsaa();
        return ok;
    }

    public static boolean setAngleMode(boolean enabled) {
        if (enabled) {
            CommandExecutor.executeSystemCommand("settings put global angle_gl_driver_all_angle 1");
            CommandExecutor.setSystemProperty("debug.angle.backend", "2");
            String res = CommandExecutor.executeSystemCommand("settings put global angle_enabled_pkgs 1");
            return CommandExecutor.isSuccessOutput(res);
        } else {
            CommandExecutor.executeSystemCommand("settings put global angle_gl_driver_all_angle 0");
            CommandExecutor.setSystemProperty("debug.angle.backend", "0");
            String res = CommandExecutor.executeSystemCommand("settings put global angle_enabled_pkgs 0");
            return CommandExecutor.isSuccessOutput(res);
        }
    }

    public static boolean setGameDriverMode(boolean enabled) {
        if (enabled) {
            CommandExecutor.executeSystemCommand("settings put global game_driver_all_apps 1");
            String res = CommandExecutor.executeSystemCommand("settings put global updatable_driver_all_apps 1");
            return CommandExecutor.isSuccessOutput(res);
        } else {
            CommandExecutor.executeSystemCommand("settings put global game_driver_all_apps 0");
            String res = CommandExecutor.executeSystemCommand("settings put global updatable_driver_all_apps 0");
            return CommandExecutor.isSuccessOutput(res);
        }
    }

    /**
     * Enhances graphics performance and locks/unlocks ultra rendering modes.
     */
    public static boolean applyEnhancedGraphics(boolean lock) {
        boolean ok = enableVulkanRenderer();
        ok &= enableForceMsaa();
        CommandExecutor.setSystemProperty("debug.egl.swapinterval", "0");
        CommandExecutor.setSystemProperty("debug.gr.swapinterval", "0");
        CommandExecutor.setSystemProperty("debug.sf.disable_backpressure", "1");
        CommandExecutor.setSystemProperty("debug.sf.early_app_phase_offset_ns", "500000");
        CommandExecutor.setSystemProperty("debug.sf.early_sf_phase_offset_ns", "500000");
        CommandExecutor.setSystemProperty("persist.sys.sf.native_mode", "1");
        CommandExecutor.setSystemProperty("vendor.display.enable_default_color_mode", "1");
        
        com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand("setprop debug.hwui.renderer vulkan");
        com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand("setprop debug.renderengine.backend vulkan");
        com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand("setprop debug.egl.force_msaa 1");
        com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand("setprop debug.sf.hw 1");
        
        if (lock) {
            setAngleMode(true);
            setGameDriverMode(true);
        }
        return ok;
    }

    /**
     * Unlocks enhanced graphics settings, restoring default renderer behavior.
     */
    public static boolean unlockEnhancedGraphics() {
        CommandExecutor.setSystemProperty("debug.egl.force_msaa", "0");
        CommandExecutor.setSystemProperty("debug.hwui.renderer", "opengl");
        com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand("setprop debug.egl.force_msaa 0");
        com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand("setprop debug.hwui.renderer opengl");
        setAngleMode(false);
        setGameDriverMode(false);
        return true;
    }
}
