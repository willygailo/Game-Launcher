package com.gamebooster.app.functions;

import android.content.Context;

import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.root.CommandExecutor;
import com.gamebooster.app.root.EngineMode;

import java.util.ArrayList;
import java.util.List;

public class TweakManagerRepository {

    public interface OnBatchCompleteListener {
        void onBatchComplete(int appliedCount);
    }

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

        TWEAKS.add(new TweakItem(
                "tcp_latency_tuning",
                "TCP Low Latency Buffer Tuning",
                "Optimizes Wi-Fi and Cellular TCP buffer limits to lower multiplayer gaming ping",
                "setprop net.tcp.buffersize.wifi 524288,1048576,2097152,262144,524288,1048576; setprop net.tcp.buffersize.mobile 524288,1048576,2097152,262144,524288,1048576",
                "setprop net.tcp.buffersize.wifi default; setprop net.tcp.buffersize.mobile default",
                TweakCategory.CPU_GPU,
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

    public static void initializeStates(Context context) {
        if (context != null) {
            TweakPreferences.loadSavedStates(context, TWEAKS);
        }
    }

    public static boolean applyTweak(TweakItem tweak) {
        return applyTweak(null, tweak);
    }

    public static boolean applyTweak(Context context, TweakItem tweak) {
        EngineMode engineMode = CommandExecutor.getActiveEngineMode();
        if (tweak.isRequiresShizuku() && engineMode == EngineMode.READ_ONLY) {
            return false;
        }

        String res = CommandExecutor.executeSystemCommand(tweak.getApplyCommand());
        boolean success = CommandExecutor.isSuccessOutput(res);
        if (success) {
            tweak.setApplied(true);
            if (context != null) {
                TweakPreferences.saveTweakState(context, tweak.getId(), true);
            }
        }
        return success;
    }

    public static boolean revertTweak(TweakItem tweak) {
        return revertTweak(null, tweak);
    }

    public static boolean revertTweak(Context context, TweakItem tweak) {
        String res = CommandExecutor.executeSystemCommand(tweak.getRevertCommand());
        boolean success = CommandExecutor.isSuccessOutput(res);
        if (success) {
            tweak.setApplied(false);
            if (context != null) {
                TweakPreferences.saveTweakState(context, tweak.getId(), false);
            }
        }
        return success;
    }

    public static int applyAllSupportedTweaks() {
        return applyAllSupportedTweaks(null);
    }

    public static int applyAllSupportedTweaks(Context context) {
        int appliedCount = 0;
        EngineMode engineMode = CommandExecutor.getActiveEngineMode();

        for (TweakItem tweak : TWEAKS) {
            if (tweak.isRequiresShizuku() && engineMode == EngineMode.READ_ONLY) continue;

            if (applyTweak(context, tweak)) {
                appliedCount++;
            }
        }
        return appliedCount;
    }

    public static void applyAllSupportedTweaksAsync(Context context, OnBatchCompleteListener listener) {
        AppExecutors.getInstance().executeCommand(() -> {
            int appliedCount = applyAllSupportedTweaks(context);
            if (listener != null) {
                AppExecutors.getInstance().postToMainThread(() -> listener.onBatchComplete(appliedCount));
            }
        });
    }

    public static void restoreAppliedTweaksAsync(Context context) {
        if (context == null) return;
        AppExecutors.getInstance().executeCommand(() -> {
            EngineMode mode = CommandExecutor.getActiveEngineMode();
            if (mode == EngineMode.READ_ONLY) return;

            for (TweakItem tweak : TWEAKS) {
                boolean wasSavedApplied = TweakPreferences.isTweakApplied(context, tweak.getId());
                if (wasSavedApplied) {
                    if (tweak.isRequiresShizuku() && mode != EngineMode.SHIZUKU) continue;
                    applyTweak(context, tweak);
                }
            }
        });
    }
}

