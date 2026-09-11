package com.gamebooster.app.booster;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Android WebView Real-Time Performance & Hardware Acceleration Engine.
 *
 * <p>WebViews are heavily utilized across modern games (Roblox, HTML5/WebGL mini-games,
 * event webviews, Discord/Garena in-game overlays, login portals, and UI components).
 * This channel configures device-adaptive Chromium/WebView command-line flags, Vulkan Skia rendering,
 * GPU rasterization, multi-threaded CPU rasterization, zero-copy buffers, DrDc (Decoupled
 * Raster Dynamic Compositing), and V8 turbo JIT optimizations without crashing low-end devices.
 */
public final class WebViewBoosterChannel {

    private static final String TAG = "WebViewBoosterChannel";

    public enum DeviceTier {
        FLAGSHIP("Flagship Overdrive (Vulkan + WebGPU + 185Hz)"),
        MID_RANGE("Mid-Range High Performance (Vulkan/GL + DrDc + 120Hz)"),
        BUDGET_SAFE("Budget & Low-RAM Stability (Safe OpenGL ES + 60Hz)");

        public final String description;

        DeviceTier(String description) {
            this.description = description;
        }
    }

    /**
     * Standard Chromium/WebView command-line flag files read on startup by System WebView.
     */
    public static final String[] WEBVIEW_FLAG_FILES = {
            "/data/local/tmp/webview-command-line",
            "/data/local/tmp/chrome-command-line",
            "/data/local/tmp/content-shell-command-line",
            "/data/local/tmp/android-webview-command-line"
    };

    private WebViewBoosterChannel() {
    }

    /**
     * Detects total system RAM in Megabytes.
     */
    public static long getTotalRamMb(Context context) {
        if (context != null) {
            try {
                ActivityManager actMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                if (actMgr != null) {
                    ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
                    actMgr.getMemoryInfo(memInfo);
                    return memInfo.totalMem / (1024 * 1024);
                }
            } catch (Throwable ignored) {}
        }

        // Fallback: parse /proc/meminfo
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("MemTotal:")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        return Long.parseLong(parts[1]) / 1024;
                    }
                }
            }
        } catch (Throwable ignored) {}

        return 4096; // Safe default 4GB
    }

    /**
     * Detects device tier based on RAM, SoC hardware, and Android SDK.
     */
    public static DeviceTier detectDeviceTier(Context context) {
        long ramMb = getTotalRamMb(context);
        int sdk = Build.VERSION.SDK_INT;
        String hardware = (Build.HARDWARE != null ? Build.HARDWARE : "").toLowerCase();
        String board = (Build.BOARD != null ? Build.BOARD : "").toLowerCase();
        String soc = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                soc = (Build.SOC_MODEL != null ? Build.SOC_MODEL : "").toLowerCase();
            } catch (Throwable ignored) {}
        }

        boolean isLowRam = (ramMb > 0 && ramMb < 3800) || sdk < 29;
        if (isLowRam) {
            return DeviceTier.BUDGET_SAFE;
        }

        // Flagship tier: RAM >= 7.5GB and Android 11+ with modern Qualcomm Snapdragon or Dimensity 9xxx or Tensor
        boolean isHighRam = ramMb >= 7200;
        boolean isHighEndSoc = hardware.contains("qcom") || hardware.contains("qualcomm")
                || soc.contains("sm8") || soc.contains("sd8") || soc.contains("dimensity 9")
                || soc.contains("tensor") || board.contains("taro") || board.contains("kalama")
                || board.contains("pineapple") || board.contains("cliffs");

        if (isHighRam && isHighEndSoc && sdk >= 31) {
            return DeviceTier.FLAGSHIP;
        }

        // Mid-Range tier: 4GB - 7GB RAM or modern mid-tier SoC
        return DeviceTier.MID_RANGE;
    }

    /**
     * Returns curated command-line flag string customized for the detected or specified device tier.
     */
    public static String getWebViewCommandLineFlags(Context context) {
        DeviceTier tier = detectDeviceTier(context);
        return getWebViewCommandLineFlagsForTier(tier);
    }

    /**
     * Backward-compatible overload.
     */
    public static String getWebViewCommandLineFlags() {
        return getWebViewCommandLineFlagsForTier(DeviceTier.MID_RANGE);
    }

    /**
     * Generates curated Chromium/WebView flags tuned for the specified DeviceTier.
     */
    public static String getWebViewCommandLineFlagsForTier(DeviceTier tier) {
        int coreCount = CpuGovernorChannel.detectCpuCoreCount();

        switch (tier) {
            case FLAGSHIP: {
                int rasterThreads = Math.max(4, Math.min(8, coreCount / 2));
                return "_ " +
                        "--ignore-gpu-blocklist " +
                        "--enable-gpu-rasterization " +
                        "--enable-zero-copy " +
                        "--enable-native-gpu-memory-buffers " +
                        "--enable-accelerated-2d-canvas " +
                        "--enable-accelerated-video-decode " +
                        "--enable-accelerated-mjpeg-decode " +
                        "--enable-oop-rasterization " +
                        "--enable-raw-draw " +
                        "--enable-low-latency-webgl " +
                        "--enable-webgl2-compute-context " +
                        "--enable-unsafe-webgpu " +
                        "--enable-gpu-async-worker-context " +
                        "--canvas-2d-layers " +
                        "--enable-surface-synchronization " +
                        "--enable-quic " +
                        "--enable-tcp-fastopen " +
                        "--enable-fast-unload " +
                        "--enable-features=Vulkan,UseSkiaRenderer,CanvasOopRasterization,DrDc,GpuRasterization,VaapiVideoDecoder,WebAssemblySimd,WebAssemblyLazyCompilation,WebViewSurfaceControl,ThreadedScrollAnimator,ZeroCopyTabSwitch,EnableOopRasterizationHighPriorityStrategy,WebRtcHWDecoding,WebRtcHWEncoding,AcceleratedVideoEncoder,AsyncImageDecoding,CanvasColorCache,ServiceWorkerBypassFetchHandler,WebAssemblyBaseline,WebAssemblyTiering,WebAssemblyTurbofan,WebGPU " +
                        "--disable-features=UseChromeOSDirectVideoDecoder,LazyFrameLoading,DefaultAngleVulkan,VulkanFromANGLE " +
                        "--num-raster-threads=" + rasterThreads + " " +
                        "--enable-drdc " +
                        "--enable-threaded-compositing " +
                        "--enable-webgl-developer-extensions " +
                        "--enable-webgl-draft-extensions " +
                        "--enable-webassembly-simd " +
                        "--enable-webassembly-tiering " +
                        "--disable-frame-rate-limit " +
                        "--disable-gpu-vsync " +
                        "--max-gum-fps=185 " +
                        "--js-flags=\"--max-semi-space-size=128 --max-old-space-size=2048 --opt --always-opt --turbo-fast-api-calls --turboshaft --wasm-opt --wasm-tier-up --expose-wasm --wasm-simd --harmony-simd\"";
            }

            case MID_RANGE: {
                int rasterThreads = Math.max(2, Math.min(4, coreCount / 2));
                return "_ " +
                        "--ignore-gpu-blocklist " +
                        "--enable-gpu-rasterization " +
                        "--enable-zero-copy " +
                        "--enable-native-gpu-memory-buffers " +
                        "--enable-accelerated-2d-canvas " +
                        "--enable-accelerated-video-decode " +
                        "--enable-oop-rasterization " +
                        "--enable-low-latency-webgl " +
                        "--enable-gpu-async-worker-context " +
                        "--enable-surface-synchronization " +
                        "--enable-quic " +
                        "--enable-tcp-fastopen " +
                        "--enable-fast-unload " +
                        "--enable-features=CanvasOopRasterization,DrDc,GpuRasterization,VaapiVideoDecoder,WebAssemblySimd,WebViewSurfaceControl,ThreadedScrollAnimator,ZeroCopyTabSwitch,WebRtcHWDecoding,WebRtcHWEncoding,AsyncImageDecoding " +
                        "--disable-features=UseChromeOSDirectVideoDecoder,LazyFrameLoading,WebGPU " +
                        "--num-raster-threads=" + rasterThreads + " " +
                        "--enable-drdc " +
                        "--enable-threaded-compositing " +
                        "--enable-webassembly-simd " +
                        "--disable-frame-rate-limit " +
                        "--max-gum-fps=120 " +
                        "--js-flags=\"--max-semi-space-size=64 --max-old-space-size=1024 --opt --turbo-fast-api-calls --turboshaft --wasm-opt\"";
            }

            case BUDGET_SAFE:
            default: {
                // Stable OpenGL ES acceleration, zero Vulkan/WebGPU experimental features to eliminate black screen
                return "_ " +
                        "--ignore-gpu-blocklist " +
                        "--enable-gpu-rasterization " +
                        "--enable-zero-copy " +
                        "--enable-accelerated-2d-canvas " +
                        "--enable-accelerated-video-decode " +
                        "--enable-oop-rasterization " +
                        "--enable-threaded-compositing " +
                        "--num-raster-threads=2 " +
                        "--enable-features=CanvasOopRasterization,GpuRasterization " +
                        "--disable-features=Vulkan,UseSkiaRenderer,WebGPU,DrDc,DefaultAngleVulkan " +
                        "--disable-frame-rate-limit " +
                        "--max-gum-fps=60 " +
                        "--js-flags=\"--max-semi-space-size=32 --max-old-space-size=512 --opt\"";
            }
        }
    }

    /**
     * Applies full WebView performance optimizations using elevated Shizuku and system properties.
     */
    public static boolean applyWebViewPerformanceBoost() {
        return applyWebViewPerformanceBoost(null);
    }

    /**
     * Applies full WebView performance optimizations customized for the calling context and detected device tier.
     */
    public static boolean applyWebViewPerformanceBoost(Context context) {
        DeviceTier tier = detectDeviceTier(context);
        String flags = getWebViewCommandLineFlagsForTier(tier);
        boolean success = true;

        Log.i(TAG, "⚡ Applying Adaptive WebView Performance Boost [" + tier.name() + "] (" + tier.description + ")");

        try {
            // 1. Build shell commands to write flag files with correct read permissions (644)
            StringBuilder sb = new StringBuilder();
            for (String path : WEBVIEW_FLAG_FILES) {
                sb.append("echo '").append(flags).append("' > ").append(path).append("; ");
                sb.append("chmod 644 ").append(path).append(" 2>/dev/null; ");
            }

            // 2. Add System Properties & DeviceConfig optimizations for WebView
            sb.append("settings put global webview_multiprocess 1; ");
            sb.append("device_config put runtime_native_boot webview_surface_control true; ");
            sb.append("device_config put runtime_native_boot webview_zero_copy true; ");
            sb.append("device_config put runtime_native_boot webview_gpu_raster true; ");

            if (tier == DeviceTier.FLAGSHIP) {
                sb.append("device_config put runtime_native_boot webview_skia_vulkan true; ");
                sb.append("device_config put runtime_native_boot webview_drdc true; ");
                sb.append("setprop debug.chromium.flags \"--enable-gpu-rasterization --enable-zero-copy --enable-drdc --ignore-gpu-blocklist --enable-oop-rasterization --enable-webgl2-compute-context\"; ");
                sb.append("setprop debug.hwui.use_gpu_pixel_buffers true; ");
                sb.append("setprop debug.hwui.renderer vulkan; ");
                sb.append("setprop debug.hwui.fps_limit 0; ");
                sb.append("setprop debug.v8.flags \"--opt --always-opt --turbo-fast-api-calls --turboshaft\"; ");
            } else if (tier == DeviceTier.MID_RANGE) {
                sb.append("device_config put runtime_native_boot webview_drdc true; ");
                sb.append("setprop debug.chromium.flags \"--enable-gpu-rasterization --enable-zero-copy --enable-drdc --ignore-gpu-blocklist --enable-oop-rasterization\"; ");
                sb.append("setprop debug.hwui.use_gpu_pixel_buffers true; ");
                sb.append("setprop debug.hwui.fps_limit 0; ");
                sb.append("setprop debug.v8.flags \"--opt --turbo-fast-api-calls --turboshaft\"; ");
            } else {
                // Budget: Safe properties to avoid OpenGL/Vulkan conflicts
                sb.append("setprop debug.chromium.flags \"--enable-gpu-rasterization --enable-zero-copy --ignore-gpu-blocklist --enable-oop-rasterization\"; ");
                sb.append("setprop debug.v8.flags \"--opt\"; ");
            }

            String fullCmd = sb.toString();

            // Execute via Shizuku UserService if bound
            if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                String result = ShizukuUserServiceConnector.getInstance().executeCommand(fullCmd);
                Log.d(TAG, "Applied WebView flags via Shizuku UserService: " + result);
            } else if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(fullCmd);
                Log.d(TAG, "Applied WebView flags via ShizukuExecutor");
            } else {
                CommandExecutor.executeSystemCommand(fullCmd);
                Log.d(TAG, "Applied WebView flags via CommandExecutor fallback");
            }

            // 3. Fallback: Also attempt writing locally to cache/data directory if readable
            writeLocalWebViewFlagsFallback(flags);

        } catch (Throwable t) {
            Log.e(TAG, "Failed to apply WebView performance flags: " + t.getMessage(), t);
            success = false;
        }

        return success;
    }

    /**
     * Clears WebView performance flags from command-line files to restore default behavior.
     */
    public static void restoreWebViewDefaults() {
        try {
            StringBuilder sb = new StringBuilder();
            for (String path : WEBVIEW_FLAG_FILES) {
                sb.append("rm -f ").append(path).append(" 2>/dev/null; ");
            }
            sb.append("setprop debug.chromium.flags \"\"; ");
            sb.append("setprop debug.v8.flags \"\"");

            String clearCmd = sb.toString();
            if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                ShizukuUserServiceConnector.getInstance().executeCommand(clearCmd);
            } else if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(clearCmd);
            } else {
                CommandExecutor.executeSystemCommand(clearCmd);
            }
            Log.i(TAG, "Restored WebView default configuration.");
        } catch (Throwable t) {
            Log.e(TAG, "Error restoring WebView defaults: " + t.getMessage(), t);
        }
    }

    /**
     * Attempts writing locally to app files directory for internal WebView inspection.
     */
    @android.annotation.SuppressLint("SetWorldReadable")
    private static void writeLocalWebViewFlagsFallback(String flags) {
        try {
            File tmpDir = new File("/data/local/tmp");
            if (tmpDir.exists() && tmpDir.canWrite()) {
                File targetFile = new File(tmpDir, "webview-command-line");
                try (FileOutputStream fos = new FileOutputStream(targetFile);
                     OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                    writer.write(flags);
                    writer.flush();
                }
                targetFile.setReadable(true, false);
            }
        } catch (Throwable ignored) {
            // Shizuku execution is the primary path
        }
    }
}
