package com.gamespace.app.tweaks;

import com.gamespace.app.core.EngineMode;
import com.gamespace.app.data.CommandExecutor;

import java.util.ArrayList;
import java.util.List;

public class TweakManagerRepository {

    private static final List<TweakItem> TWEAKS = new ArrayList<>();

    static {
        // CPU & GPU Tweaks
        TWEAKS.add(new TweakItem(
                "gpu_hw_composition",
                "GPU Hardware Acceleration",
                "Forces GPU composition for all windows to reduce CPU overhead",
                "setprop debug.composition.type gpu",
                "setprop debug.composition.type cputext",
                TweakCategory.CPU_GPU,
                false, true
        ));

        TWEAKS.add(new TweakItem(
                "hw_overlays",
                "Disable HW Overlays",
                "Always use GPU for screen compositing",
                "setprop debug.sf.hw 1",
                "setprop debug.sf.hw 0",
                TweakCategory.CPU_GPU,
                false, true
        ));

        TWEAKS.add(new TweakItem(
                "fifo_audio_render",
                "FIFO Realtime Rendering",
                "Forces realtime FIFO scheduling queue for smooth rendering",
                "setprop sys.use_fifo 1",
                "setprop sys.use_fifo 0",
                TweakCategory.CPU_GPU,
                false, true
        ));

        // Touch & Display Tweaks
        TWEAKS.add(new TweakItem(
                "touch_sample_rate",
                "300Hz Touch Response",
                "Boosts touch event rate for instantaneous touch input",
                "setprop windowsmgr.max_events_per_sec 300",
                "setprop windowsmgr.max_events_per_sec 60",
                TweakCategory.TOUCH_DISPLAY,
                false, true
        ));

        TWEAKS.add(new TweakItem(
                "touch_slop_sensitivity",
                "Ultra-Sensitive Touch Threshold",
                "Reduces touch slop sensitivity for instant swipe detection",
                "setprop view.touch_slop 2",
                "setprop view.touch_slop 8",
                TweakCategory.TOUCH_DISPLAY,
                false, true
        ));

        TWEAKS.add(new TweakItem(
                "scrolling_cache_boost",
                "Smooth Scroll Cache",
                "Disables scrolling cache compression to save CPU cycles",
                "setprop persist.sys.scrollingcache 3",
                "setprop persist.sys.scrollingcache 1",
                TweakCategory.TOUCH_DISPLAY,
                false, true
        ));

        // Rooted Device Kernel Tweaks
        TWEAKS.add(new TweakItem(
                "root_cpu_performance",
                "CPU Performance Governor [ROOT]",
                "Locks CPU cores to maximum frequency governor",
                "for f in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo performance > $f; done",
                "for f in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo schedutil > $f; done",
                TweakCategory.ROOT_KERNEL,
                true, false
        ));

        TWEAKS.add(new TweakItem(
                "root_gpu_power_level",
                "Adreno GPU Max Power [ROOT]",
                "Locks GPU frequency to max performance tier",
                "echo 0 > /sys/class/kgsl/kgsl-3d0/default_pwrlevel 2>/dev/null || true",
                "echo 1 > /sys/class/kgsl/kgsl-3d0/default_pwrlevel 2>/dev/null || true",
                TweakCategory.ROOT_KERNEL,
                true, false
        ));

        TWEAKS.add(new TweakItem(
                "root_io_scheduler",
                "No-Op Disk I/O Scheduler [ROOT]",
                "Switch internal storage scheduler to lowest latency queue",
                "for f in /sys/block/sd*/queue/scheduler /sys/block/mmcblk*/queue/scheduler; do echo noop > $f 2>/dev/null || true; done",
                "for f in /sys/block/sd*/queue/scheduler /sys/block/mmcblk*/queue/scheduler; do echo mq-deadline > $f 2>/dev/null || true; done",
                TweakCategory.ROOT_KERNEL,
                true, false
        ));

        // Shizuku / ADB System Tweaks
        TWEAKS.add(new TweakItem(
                "shizuku_fast_anim",
                "0.5x UI Speed Animations",
                "Speeds up system window transition times",
                "settings put global window_animation_scale 0.5; settings put global transition_animation_scale 0.5; settings put global animator_duration_scale 0.5",
                "settings put global window_animation_scale 1.0; settings put global transition_animation_scale 1.0; settings put global animator_duration_scale 1.0",
                TweakCategory.SHIZUKU_SYSTEM,
                false, true
        ));

        TWEAKS.add(new TweakItem(
                "shizuku_disable_thermal_throttle",
                "Thermal Override Bypass",
                "Overrides thermal throttling status via System Server",
                "cmd thermal override-status 0",
                "cmd thermal override-status -1",
                TweakCategory.SHIZUKU_SYSTEM,
                false, true
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
        if (tweak.isRequiresRoot() && engineMode != EngineMode.ROOT) {
            return false;
        }
        if (tweak.isRequiresShizuku() && engineMode == EngineMode.READ_ONLY) {
            return false;
        }

        String res = CommandExecutor.executeSystemCommand(tweak.getApplyCommand());
        boolean success = (res == null || !res.startsWith("ERROR"));
        if (success) {
            tweak.setApplied(true);
        }
        return success;
    }

    public static boolean revertTweak(TweakItem tweak) {
        String res = CommandExecutor.executeSystemCommand(tweak.getRevertCommand());
        boolean success = (res == null || !res.startsWith("ERROR"));
        if (success) {
            tweak.setApplied(false);
        }
        return success;
    }

    public static int applyAllSupportedTweaks() {
        int appliedCount = 0;
        EngineMode engineMode = CommandExecutor.getActiveEngineMode();

        for (TweakItem tweak : TWEAKS) {
            if (tweak.isRequiresRoot() && engineMode != EngineMode.ROOT) continue;
            if (tweak.isRequiresShizuku() && engineMode == EngineMode.READ_ONLY) continue;

            if (applyTweak(tweak)) {
                appliedCount++;
            }
        }
        return appliedCount;
    }
}
