package com.gamebooster.app.functions;

import com.gamebooster.app.root.EngineMode;

import com.gamebooster.app.root.EngineMode;
import com.gamebooster.app.root.CommandExecutor;

import java.util.ArrayList;
import java.util.List;

public class TweakManagerRepository {

    private static final List<TweakItem> TWEAKS = new ArrayList<>();

    static {
        // CPU & GPU Tweaks
        TWEAKS.add(new TweakItem(
                "gpu_hw_composition",
                "Vulkan HWUI Renderer",
                "Forces Vulkan hardware graphics pipeline to boost rendering throughput",
                "setprop debug.hwui.renderer vulkan",
                "setprop debug.hwui.renderer skia",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "hw_overlays",
                "Force SurfaceFlinger HW Composition",
                "Forces GPU hardware composition to eliminate CPU rendering overhead",
                "setprop debug.sf.hw 1",
                "setprop debug.sf.hw 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "fifo_audio_render",
                "FIFO Realtime Scheduling Queue",
                "Forces realtime FIFO scheduling queue for ultra-low audio & touch latency",
                "setprop sys.use_fifo 1",
                "setprop sys.use_fifo 0",
                TweakCategory.CPU_GPU,
                true
        ));

        // Touch & Display Tweaks
        TWEAKS.add(new TweakItem(
                "touch_sample_rate",
                "Touch Slop Sensitivity Boost",
                "Reduces swipe activation threshold for instant gesture response",
                "settings put system touch_slop_reduction 1; setprop view.touch_slop 2",
                "settings put system touch_slop_reduction 0; setprop view.touch_slop 8",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "scrolling_cache_boost",
                "Zero Scroll Cache Latency",
                "Disables scrolling cache compression to save CPU render cycles",
                "setprop persist.sys.scrollingcache 3",
                "setprop persist.sys.scrollingcache 1",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        // Shizuku / ADB System Tweaks
        TWEAKS.add(new TweakItem(
                "shizuku_fast_anim",
                "0.5x UI Speed Animations",
                "Reduces system window transition duration",
                "settings put global window_animation_scale 0.5; settings put global transition_animation_scale 0.5; settings put global animator_duration_scale 0.5",
                "settings put global window_animation_scale 1.0; settings put global transition_animation_scale 1.0; settings put global animator_duration_scale 1.0",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "shizuku_disable_thermal_throttle",
                "Thermal Override Bypass",
                "Overrides thermal throttling caps via ThermalService",
                "cmd thermalservice override-status 0 || cmd thermal override-status 0",
                "cmd thermalservice override-status -1 || cmd thermal override-status -1",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));
    }

    public static List<TweakItem> getAllTweaks() {
        return TWEAKS;
    }

    public static List<TweakItem> getTweaksByCategory(TweakCategory category) {
        if (category == TweakCategory.ALL) return TWEAKS;

        List<TweakItem> filtered = new ArrayList<>();
        for (TweakItem tweak : TWEAKS) {
            if (tweak.getCategory() == category) {
                filtered.add(tweak);
            }
        }
        return filtered;
    }

    public static boolean applyTweak(TweakItem tweak) {
        EngineMode engineMode = CommandExecutor.getActiveEngineMode();
        if (tweak.isRequiresShizuku() && engineMode == EngineMode.READ_ONLY) {
            return false;
        }

        String res = CommandExecutor.executeSystemCommand(tweak.getApplyCommand());
        boolean success = CommandExecutor.isSuccessOutput(res);
        if (success) {
            tweak.setApplied(true);
        }
        return success;
    }

    public static boolean revertTweak(TweakItem tweak) {
        String res = CommandExecutor.executeSystemCommand(tweak.getRevertCommand());
        boolean success = CommandExecutor.isSuccessOutput(res);
        if (success) {
            tweak.setApplied(false);
        }
        return success;
    }

    public static int applyAllSupportedTweaks() {
        int appliedCount = 0;
        EngineMode engineMode = CommandExecutor.getActiveEngineMode();

        for (TweakItem tweak : TWEAKS) {
            if (tweak.isRequiresShizuku() && engineMode == EngineMode.READ_ONLY) continue;

            if (applyTweak(tweak)) {
                appliedCount++;
            }
        }
        return appliedCount;
    }
}
