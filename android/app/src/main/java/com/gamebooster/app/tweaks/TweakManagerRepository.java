package com.gamebooster.app.tweaks;

import android.content.Context;
import com.gamebooster.app.config.ManualSettingsPreferences;
import com.gamebooster.app.config.TweakPreferences;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.engine.EngineMode;

import java.util.ArrayList;
import java.util.List;

public class TweakManagerRepository {

    public interface OnBatchCompleteListener {
        void onBatchComplete(int appliedCount);
    }

    private static final List<TweakItem> TWEAKS = new ArrayList<>();

    static {
        TWEAKS.addAll(CpuGpuTweaksProvider.getTweaks());
        TWEAKS.addAll(TouchDisplayTweaksProvider.getTweaks());
        TWEAKS.addAll(NetworkAudioTweaksProvider.getTweaks());
        TWEAKS.addAll(SystemKernelTweaksProvider.getTweaks());

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
                "powerhal_sustained_perf",
                "PowerHAL Sustained Performance & Power Boost",
                "Forces PowerHAL sustained performance & extreme boost modes to prevent CPU clock drops",
                "cmd power set-mode 0 1; cmd power set-mode 2 1",
                "cmd power set-mode 0 0; cmd power set-mode 2 0",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "thermalservice_override",
                "Thermal Throttling Bypass Override",
                "Overrides thermal service status to cool (0) to eliminate thermal clock throttling",
                "cmd thermalservice override-status 0",
                "cmd thermalservice override-status -1",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_vsync",
                "Disable VSync Lock",
                "Removes VSync frame cap to unlock maximum GPU frame output",
                "setprop debug.egl.swapinterval 0",
                "setprop debug.egl.swapinterval 1",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "gpu_overdraw_debug_off",
                "Disable GPU Overdraw Debug",
                "Turns off GPU overdraw visualization layer to reclaim rendering cycles",
                "setprop debug.hwui.overdraw false",
                "setprop debug.hwui.overdraw show",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_blur_effects",
                "Disable Window Blur Effects",
                "Disables UI background blur effects on Android 12+ to save GPU rendering cycles",
                "settings put global window_blurs_enabled 0",
                "settings put global window_blurs_enabled 1",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "force_dark_mode",
                "Force System Dark UI Mode",
                "Enforces system dark theme to reduce OLED display power draw and GPU overdraw",
                "cmd uimode night yes",
                "cmd uimode night no",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_hw_overlay_compositor",
                "Disable HW Overlay Compositor",
                "Forces SurfaceFlinger GPU composition for consistent frame pacing",
                "setprop debug.sf.disable_hwc_overlay 1",
                "setprop debug.sf.disable_hwc_overlay 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "gpu_buffer_count",
                "Triple Buffer Rendering",
                "Configures EGL 3-buffer pipeline to eliminate micro-stutter from swap waits",
                "setprop debug.egl.buffcount 3",
                "setprop debug.egl.buffcount 2",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_notification_alerts",
                "Gaming DND Notification Silence",
                "Enables Zen Mode DND to suppress popups and notification interruptions during gaming",
                "settings put global zen_mode 2",
                "settings put global zen_mode 0",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_haptic_feedback",
                "Disable Vibration Haptics",
                "Disables touch vibration motors to prevent tactile latency and save battery",
                "settings put system haptic_feedback_enabled 0",
                "settings put system haptic_feedback_enabled 1",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "max_background_limit",
                "Limit Background Apps",
                "Restricts cached background processes to 2, releasing 200-400MB RAM for games",
                "settings put global background_process_limit 2",
                "settings put global background_process_limit -1",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "dns_google_fast",
                "Google DNS Fast Resolve",
                "Routes DNS queries through Google 8.8.8.8 for fastest domain resolution",
                "setprop net.dns1 8.8.8.8; setprop net.dns2 8.8.4.4",
                "setprop net.dns1 \"\"; setprop net.dns2 \"\"",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "private_dns_cloudflare",
                "Cloudflare Private DNS 1.1.1.1",
                "Enables TLS-encrypted Cloudflare Private DNS for ultra-low gaming lookup ping",
                "settings put global private_dns_mode hostname; settings put global private_dns_specifier one.one.one.one",
                "settings put global private_dns_mode off",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_captive_portal",
                "Disable Captive Portal Check",
                "Disables HTTP ping checks to prevent network latency spikes during game connections",
                "settings put global captive_portal_mode 0",
                "settings put global captive_portal_mode 1",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_mobile_data_always",
                "Disable Always-On Mobile Data",
                "Stops background mobile data drain when connected to Wi-Fi to save battery",
                "settings put global mobile_data_always_on 0",
                "settings put global mobile_data_always_on 1",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "super_fast_touch_165",
                "Super Fast Touch 165Hz Competitive Mode",
                "Ultra-low slop (0), max digitizer input rate 1000/s, min pressure scale — tuned for 165Hz competitive play",
                "setprop view.touch_slop 0; setprop persist.sys.touch.pressure.scale 0.0001; setprop debug.input.max_events_per_sec 1000; setprop sys.use_fifo 1; settings put system touch_slop_reduction 1",
                "setprop view.touch_slop 8; setprop persist.sys.touch.pressure.scale 1.0; setprop debug.input.max_events_per_sec 150; setprop sys.use_fifo 0; settings put system touch_slop_reduction 0",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "ram_turbo_mode",
                "RAM Turbo — Clear Hidden App Reserve",
                "Clears Android hidden app RAM reserves and lowers min memory floor to free maximum RAM for games",
                "settings put global min_hidden_apps 0; settings put global hidden_app_minmem_kb 0; settings put global background_process_limit 2",
                "settings put global min_hidden_apps 5; settings put global hidden_app_minmem_kb 512; settings put global background_process_limit -1",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "io_scheduler_gaming",
                "Gaming I/O Scheduler & Low Swappiness",
                "Sets I/O scheduler to deadline for deterministic latency and lowers swappiness to 20 to keep game data in RAM",
                "setprop sys.io.scheduler deadline; setprop vm.swappiness 20; setprop vm.dirty_ratio 10; setprop vm.dirty_background_ratio 5",
                "setprop sys.io.scheduler cfq; setprop vm.swappiness 60; setprop vm.dirty_ratio 20; setprop vm.dirty_background_ratio 10",
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
        if (tweak == null) return false;

        EngineMode engineMode = CommandExecutor.getActiveEngineMode();
        if (tweak.isRequiresShizuku() && engineMode == EngineMode.READ_ONLY) {
            return false;
        }

        String res;
        if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
            res = com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand(tweak.getApplyCommand());
        } else {
            res = CommandExecutor.executeSystemCommand(tweak.getApplyCommand());
        }

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
        if (tweak == null) return false;

        String res;
        if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
            res = com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand(tweak.getRevertCommand());
        } else {
            res = CommandExecutor.executeSystemCommand(tweak.getRevertCommand());
        }

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

            // 1. Re-apply all enabled Shizuku ADB system tweaks
            for (TweakItem tweak : TWEAKS) {
                boolean wasSavedApplied = TweakPreferences.isTweakApplied(context, tweak.getId());
                if (wasSavedApplied) {
                    if (tweak.isRequiresShizuku() && mode != EngineMode.SHIZUKU) continue;
                    applyTweak(context, tweak);
                }
            }

            // 2. Re-apply manual hardware engine settings permanently
            if (ManualSettingsPreferences.isAngleModeEnabled(context)) {
                com.gamebooster.app.booster.GpuTweaksChannel.setAngleMode(true);
            }
            if (ManualSettingsPreferences.isGameDriverEnabled(context)) {
                com.gamebooster.app.booster.GpuTweaksChannel.setGameDriverMode(true);
            }
            boolean isVulkan = "vulkan".equalsIgnoreCase(ManualSettingsPreferences.getGpuMode(context));
            com.gamebooster.app.booster.PerformanceChannel.setGpuRenderMode(isVulkan);

            boolean isPerfCpu = "performance".equalsIgnoreCase(ManualSettingsPreferences.getCpuMode(context));
            com.gamebooster.app.booster.CpuGovernorChannel.setGovernor(isPerfCpu ? "extreme" : "schedutil");

            com.gamebooster.app.booster.NetworkOptimizer.flushDnsCache();
        });
    }
}
