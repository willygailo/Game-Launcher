package com.gamebooster.app.feature.performance.booster;

import android.util.Log;
import com.gamebooster.app.platform.shell.CommandExecutor;

/** GPU hardware composition, graphics driver, and rendering channel. */
public final class GpuTweaksChannel {
    private static final String TAG = "GpuTweaksChannel";

    private GpuTweaksChannel() { }

    public static boolean enableVulkanRenderer() {
        Log.i(TAG, "Enabling SkiaVulkan hardware rendering...");
        return CommandExecutor.setSystemProperty("debug.hwui.renderer", "skiavk");
    }

    public static boolean enableForceMsaa() {
        return CommandExecutor.setSystemProperty("debug.egl.force_msaa", "true");
    }

    public static boolean setGpuMaxPerformance() {
        Log.i(TAG, "Enforcing maximum GPU hardware composition...");
        boolean g1 = CommandExecutor.setSystemProperty("debug.composition.type", "gpu");
        boolean g2 = CommandExecutor.setSystemProperty("persist.sys.composition.type", "gpu");
        boolean g3 = CommandExecutor.setSystemProperty("debug.egl.hw", "1");
        boolean g4 = CommandExecutor.setSystemProperty("vendor.gpu.boost", "1");
        return g1 || g2 || g3 || g4;
    }

    public static boolean setAngleMode(boolean enabled) {
        AngleGraphicsDriverChannel.resetAngleDriver();
        return true;
    }

    public static boolean setGameDriverMode(boolean enabled) {
        return CommandExecutor.setSystemSetting("global", "game_driver_all_apps", enabled ? "1" : "0");
    }

    public static boolean applyEnhancedGraphics(boolean lock) {
        setGpuMaxPerformance();
        enableVulkanRenderer();
        return true;
    }

    public static boolean unlockEnhancedGraphics() {
        return true;
    }
}
