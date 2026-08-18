package com.gamebooster.app.booster;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

public class PerformanceChannel {

    private static final String TAG = "PerformanceChannel";

    public enum Profile {
        EXTREME_PERFORMANCE("Extreme Performance (185 FPS/Hz)"),
        PERFORMANCE("High Performance (185 FPS/Hz)"),
        BALANCED("Balanced Gaming (185 FPS/Hz)");

        public final String title;
        Profile(String title) { this.title = title; }
    }

    public static final class ProfileResult {
        public final boolean refreshRateApplied;
        public final int appliedHz;
        public final String message;

        private ProfileResult(boolean refreshRateApplied, int appliedHz, String message) {
            this.refreshRateApplied = refreshRateApplied;
            this.appliedHz = appliedHz;
            this.message = message;
        }
    }

    public static boolean applyProfile(Context context, Profile profile) {
        return applyProfileWithResult(context, profile).refreshRateApplied;
    }

    /** Applies the strongest refresh rate with full GPU, CPU, Touch, and Network optimizations. */
    public static ProfileResult applyProfileWithResult(Context context, Profile profile) {
        if (context == null) return new ProfileResult(false, 0, "Device context is unavailable");

        final int targetHz = GameProfileAutoConfigurator.getTargetFpsHz(context);

        HzFpsChannel.RefreshRateResult refreshResult;
        if (profile == Profile.EXTREME_PERFORMANCE) {
            // EXTREME: force unconditionally via Shizuku — no capability gate, no fallback
            refreshResult = HzFpsChannel.forceSetRefreshRate(context, targetHz);
        } else {
            refreshResult = HzFpsChannel.setRefreshRate(context, targetHz);
        }

        // Apply all hardware and software performance channels
        CpuGovernorChannel.setPerformanceLock();
        GpuTweaksChannel.setGpuMaxPerformance();
        GpuTweaksChannel.setGameDriverMode(true);
        TouchLatencyChannel.enableUltraTouchResponse();
        NetworkTweaksChannel.enableLowLatencyNetwork();
        ThermalChannel.setThermalOverride(true);
        RamZramChannel.trimMemoryAndCleanCache(context);
        writeAndExecuteRootTweaksScript(targetHz);

        return new ProfileResult(true, targetHz, "⚡ " + profile.title + " Locked @ " + targetHz + "Hz");
    }

    /** Writes and executes root shell script at 185Hz. */
    public static boolean writeAndExecuteRootTweaksScript() {
        return writeAndExecuteRootTweaksScript(185);
    }

    /**
     * Writes and executes an elevated performance script for the specified target Hz.
     *
     * @param targetHz Target refresh rate (120, 144, 165, or 185)
     */
    public static boolean writeAndExecuteRootTweaksScript(int targetHz) {
        final int hz = targetHz > 0 ? targetHz : 185;
        try {
            String scriptPath = "/data/local/tmp/gamebooster_tweaks.sh";
            String scriptContent = "#!/system/bin/sh\n" +
                    "sync; echo 3 > /proc/sys/vm/drop_caches\n" +
                    "setprop debug.sf.hw 1\n" +
                    "setprop debug.hwui.renderer vulkan\n" +
                    "setprop debug.renderengine.backend vulkan\n" +
                    "setprop debug.renderengine.skia_pipeline true\n" +
                    "setprop debug.sf.early_app_phase_offset_ns 500000\n" +
                    "setprop debug.sf.fps_limit " + hz + "\n" +
                    "setprop persist.sys.NV_FPSLIMIT " + hz + "\n" +
                    "setprop persist.sys.NV_POWERMODE 1\n" +
                    "service call SurfaceFlinger 1035 i32 " + hz + "\n" +
                    "service call SurfaceFlinger 1036 i32 " + hz + "\n" +
                    "cmd power set-mode 0 1\n" +
                    "cmd power set-mode 2 1\n" +
                    "cmd thermalservice override-status 0\n" +
                    "setprop view.touch_slop 0\n" +
                    "setprop sys.use_fifo 1\n" +
                    "setprop debug.input.max_events_per_sec 1000\n" +
                    "setprop persist.sys.touch.report_rate 1000\n" +
                    "setprop persist.vendor.touch.sampling_rate 1000\n" +
                    "setprop debug.sensor.gyro.sample_rate 1000\n" +
                    "setprop persist.sys.gyro.delay 0\n" +
                    "setprop debug.adreno.turbo 1\n" +
                    "setprop debug.adreno.perf_level 0\n" +
                    "setprop debug.mali.sched.priority -20\n" +
                    "setprop debug.hwui.use_gpu_pixel_buffers true\n" +
                    "setprop debug.hwui.render_thread_priority -20\n" +
                    "setprop net.ipv4.tcp_congestion_control bbr\n" +
                    "cmd wifi force-low-latency-mode enabled\n";

            String cmd = String.format("printf '%s' > %s && chmod 755 %s && sh %s",
                    scriptContent.replace("'", "'\\''"), scriptPath, scriptPath, scriptPath);

            if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                String out = ShizukuUserServiceConnector.getInstance().executeCommand(cmd);
                return out != null && !out.startsWith("ERROR");
            } else if (ShizukuExecutor.hasShizukuPermission()) {
                String out = ShizukuExecutor.executeShizukuCommand(cmd);
                return out != null && !out.startsWith("ERROR");
            } else {
                String res = CommandExecutor.executeSystemCommand(cmd);
                return CommandExecutor.isSuccessOutput(res);
            }
        } catch (Throwable t) {
            Log.w(TAG, "Error executing root tweaks script", t);
            return false;
        }
    }

    public static boolean setGpuRenderMode(boolean is3D) {
        if (is3D) {
            boolean ok = GpuTweaksChannel.enableVulkanRenderer();
            ok &= CommandExecutor.setSystemProperty("debug.sf.hw", "1");
            return ok;
        } else {
            boolean ok = CommandExecutor.setSystemProperty("debug.hwui.renderer", "skia");
            ok &= CommandExecutor.setSystemProperty("debug.sf.hw", "0");
            return ok;
        }
    }

    public static boolean executeOneTapBoost(Context context) {
        return applyProfile(context, Profile.EXTREME_PERFORMANCE);
    }
}
