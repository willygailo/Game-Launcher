package com.gamebooster.app.booster;
import com.gamebooster.app.config.*;

import android.content.Context;

import com.gamebooster.app.device.DevicePerformanceCapabilities;

public class PerformanceChannel {

    public enum Profile {
        EXTREME_PERFORMANCE("Extreme Performance Mode"),
        PERFORMANCE("High Performance Mode"),
        BALANCED("Balanced Game Performance");

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

    /** Applies the strongest refresh rate that is physically supported by this device. */
    public static ProfileResult applyProfileWithResult(Context context, Profile profile) {
        if (context == null) return new ProfileResult(false, 0, "Device context is unavailable");

        DevicePerformanceCapabilities caps = DevicePerformanceCapabilities.detect(context);
        int targetHz;
        switch (profile) {
            case EXTREME_PERFORMANCE:
                targetHz = 165;
                break;

            case PERFORMANCE:
                targetHz = caps.resolveRefreshRate(144);
                break;

            case BALANCED:
                targetHz = caps.resolveRefreshRate(90);
                break;

            default:
                return new ProfileResult(false, 0, "Unknown performance profile");
        }

        HzFpsChannel.RefreshRateResult refreshResult = HzFpsChannel.setRefreshRate(context, targetHz);
        if (!refreshResult.success) {
            return new ProfileResult(false, targetHz, refreshResult.message);
        }

        // Thermal protection remains enabled. It prevents heat-related frame drops in longer sessions.
        if (profile == Profile.EXTREME_PERFORMANCE || profile == Profile.PERFORMANCE) {
            CpuGovernorChannel.setPerformanceLock();
            GpuTweaksChannel.enableVulkanRenderer();
            TouchLatencyChannel.enableUltraTouchResponse();
            NetworkTweaksChannel.enableLowLatencyNetwork();
            RamZramChannel.trimMemoryAndCleanCache(context);
            writeAndExecuteRootTweaksScript();
        } else {
            CpuGovernorChannel.setGovernor("schedutil");
            TouchLatencyChannel.enableUltraTouchResponse();
        }
        return new ProfileResult(true, targetHz, refreshResult.message + " • Thermal protection stays active");
    }

    public static boolean writeAndExecuteRootTweaksScript() {
        try {
            String scriptPath = "/data/local/tmp/gamebooster_tweaks.sh";
            String scriptContent = "#!/system/bin/sh\\n" +
                    "sync; echo 3 > /proc/sys/vm/drop_caches\\n" +
                    "setprop debug.sf.hw 1\\n" +
                    "setprop debug.hwui.renderer vulkan\\n" +
                    "setprop debug.renderengine.backend vulkan\\n" +
                    "setprop debug.sf.early_app_phase_offset_ns 500000\\n" +
                    "setprop debug.sf.fps_limit 165\\n" +
                    "setprop persist.sys.NV_FPSLIMIT 165\\n" +
                    "setprop persist.sys.NV_POWERMODE 1\\n" +
                    "service call SurfaceFlinger 1035 i32 165\\n" +
                    "cmd power set-mode 0 1\\n" +
                    "cmd power set-mode 2 1\\n" +
                    "cmd thermalservice override-status 0\\n";

            String cmd = String.format("printf '%s' > %s && chmod 755 %s && sh %s",
                    scriptContent, scriptPath, scriptPath, scriptPath);
            String res = com.gamebooster.app.engine.CommandExecutor.executeSystemCommand(cmd);
            return com.gamebooster.app.engine.CommandExecutor.isSuccessOutput(res);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean setGpuRenderMode(boolean is3D) {
        if (is3D) {
            boolean ok = GpuTweaksChannel.enableVulkanRenderer();
            ok &= com.gamebooster.app.engine.CommandExecutor.setSystemProperty("debug.sf.hw", "1");
            return ok;
        } else {
            boolean ok = com.gamebooster.app.engine.CommandExecutor.setSystemProperty("debug.hwui.renderer", "skia");
            ok &= com.gamebooster.app.engine.CommandExecutor.setSystemProperty("debug.sf.hw", "0");
            return ok;
        }
    }

    public static boolean executeOneTapBoost(Context context) {
        return applyProfile(context, Profile.PERFORMANCE);
    }
}
