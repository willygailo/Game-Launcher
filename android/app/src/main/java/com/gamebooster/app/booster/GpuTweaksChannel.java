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

    public static boolean disableVulkanRenderer() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemProperty("debug.hwui.renderer", "skia");
        ok &= CommandExecutor.setSystemProperty("debug.renderengine.backend", "gles");
        ok &= CommandExecutor.setSystemProperty("debug.sf.hw", "0");
        ok &= CommandExecutor.setSystemProperty("debug.sf.latch_unsignaled", "0");
        return ok;
    }

    public static boolean enableForceMsaa() {
        return CommandExecutor.setSystemProperty("debug.egl.force_msaa", "1");
    }

    public static boolean disableForceMsaa() {
        return CommandExecutor.setSystemProperty("debug.egl.force_msaa", "0");
    }

    public static boolean setGpuMaxPerformance() {
        boolean ok = enableVulkanRenderer();
        ok &= enableForceMsaa();
        return ok;
    }

    public static boolean revertGpuMaxPerformance() {
        boolean ok = disableVulkanRenderer();
        ok &= disableForceMsaa();
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
            CommandExecutor.executeSystemCommand("settings put global game_driver_all_apps 0");
            CommandExecutor.executeSystemCommand("settings put global updatable_driver_all_apps 0");
            String targetPkgs = "com.mobile.legends,com.mobilelegends.win,com.tencent.ig,com.pubg.krmobile,com.vng.pubgmobile,com.pubg.imobile,com.activision.callofduty.shooter,com.garena.game.codm";
            CommandExecutor.executeSystemCommand("settings put global game_driver_opt_in_apps " + targetPkgs);
            String res = CommandExecutor.executeSystemCommand("settings put global updatable_driver_production_opt_in_apps " + targetPkgs);
            return CommandExecutor.isSuccessOutput(res);
        } else {
            CommandExecutor.executeSystemCommand("settings put global game_driver_all_apps 0");
            CommandExecutor.executeSystemCommand("settings put global updatable_driver_all_apps 0");
            CommandExecutor.executeSystemCommand("settings put global game_driver_opt_in_apps \"\"");
            String res = CommandExecutor.executeSystemCommand("settings put global updatable_driver_production_opt_in_apps \"\"");
            return CommandExecutor.isSuccessOutput(res);
        }
    }
}
