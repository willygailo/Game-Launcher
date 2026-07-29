package com.gamespace.app.channels;

import com.gamespace.app.data.CommandExecutor;

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
        if (RootCommandChannel.isAvailable()) {
            RootCommandChannel.writeSysfs("/sys/class/kgsl/kgsl-3d0/default_pwrlevel", "0");
            RootCommandChannel.writeSysfs("/sys/class/kgsl/kgsl-3d0/devfreq/governor", "performance");
            RootCommandChannel.writeSysfs("/sys/class/misc/mali0/device/dvfs_governor", "performance");
        }
        return ok;
    }
}
