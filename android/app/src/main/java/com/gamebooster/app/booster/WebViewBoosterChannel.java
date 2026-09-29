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
     * Uses a scoring system: GPU score + CPU score + RAM tier + SDK level.
     */
    public static DeviceTier detectDeviceTier(Context context) {
        long ramMb = getTotalRamMb(context);
        int sdk = Build.VERSION.SDK_INT;
        String hardware = (Build.HARDWARE != null ? Build.HARDWARE : "").toLowerCase();
        String board = (Build.BOARD != null ? Build.BOARD : "").toLowerCase();
        String soc = "";
        String device = (Build.DEVICE != null ? Build.DEVICE : "").toLowerCase();
        String model = (Build.MODEL != null ? Build.MODEL : "").toLowerCase();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                soc = (Build.SOC_MODEL != null ? Build.SOC_MODEL : "").toLowerCase();
            } catch (Throwable ignored) {}
        }

        // GPU Score estimation (0-100)
        int gpuScore = estimateGpuScore(hardware, soc, board, device, model);
        // CPU Score estimation (0-100) - rough Geekbench 6 single-core proxy
        int cpuScore = estimateCpuScore(hardware, soc, board, device, model);

        // Tier thresholds
        // FLAGSHIP: GPU >= 85 && CPU >= 1200 && RAM >= 8GB && SDK >= 33 (Android 13+)
        // MID_RANGE: GPU >= 55 && CPU >= 800 && RAM >= 4GB && SDK >= 30 (Android 11+)
        // BUDGET: everything else

        boolean isLowRam = (ramMb > 0 && ramMb < 4096) || sdk < 30;
        if (isLowRam) {
            return DeviceTier.BUDGET_SAFE;
        }

        boolean isHighRam = ramMb >= 8192; // 8GB+
        boolean isMidRam = ramMb >= 4096;  // 4GB+

        if (isHighRam && gpuScore >= 85 && cpuScore >= 1200 && sdk >= 33) {
            return DeviceTier.FLAGSHIP;
        }

        if (isMidRam && gpuScore >= 55 && cpuScore >= 800 && sdk >= 30) {
            return DeviceTier.MID_RANGE;
        }

        return DeviceTier.BUDGET_SAFE;
    }

    /**
     * Estimates GPU score (0-100) based on SoC identifiers.
     * Adreno 750/740=100, Adreno 730/720=90, Adreno 660/650=80, Adreno 640=75
     * Mali G720/G715=95, Mali G710=85, Mali G78=75, Mali G77=70, Mali G68=65, Mali G57=50
     * Xclipse 950=90, Xclipse 940=85
     * Maleoon 910=80
     * Tiger T820=55
     */
    private static int estimateGpuScore(String hardware, String soc, String board, String device, String model) {
        String combined = hardware + " " + soc + " " + board + " " + device + " " + model;

        // Adreno (Snapdragon)
        if (combined.contains("sm8650") || combined.contains("sm8550") || combined.contains("sm8475") || combined.contains("sm8450")) return 100; // 8 Gen 3/2/1
        if (combined.contains("sm8425") || combined.contains("sm8350") || combined.contains("sm8250")) return 95;   // 8+ Gen 1, 888+
        if (combined.contains("sm7550") || combined.contains("sm7450") || combined.contains("sm7325")) return 85;   // 7+ Gen 3/2/1
        if (combined.contains("sm7225") || combined.contains("sm7150") || combined.contains("sm7125")) return 75;   // 7 Gen 1, 778G
        if (combined.contains("sm6375") || combined.contains("sm6225") || combined.contains("sm6150")) return 65;   // 6 Gen 1, 695
        if (combined.contains("sm8150") || combined.contains("sdm855") || combined.contains("sdm845")) return 80;   // 855/845

        // Mali (MediaTek Dimensity / Exynos / Tensor / Kirin / UNISOC)
        if (combined.contains("dimensity 9300") || combined.contains("dimensity 9200")) return 95;  // G720
        if (combined.contains("dimensity 9000") || combined.contains("dimensity 8300") || combined.contains("dimensity 8200")) return 88; // G715/G710
        if (combined.contains("dimensity 8100") || combined.contains("dimensity 8050") || combined.contains("dimensity 8000")) return 82; // G710/G78
        if (combined.contains("dimensity 1200") || combined.contains("dimensity 1100") || combined.contains("dimensity 1000")) return 75; // G77
        if (combined.contains("dimensity 900") || combined.contains("dimensity 800") || combined.contains("dimensity 700")) return 65; // G68/G57
        if (combined.contains("dimensity 6000") || combined.contains("dimensity 7000")) return 60;

        // Exynos
        if (combined.contains("exynos 2400") || combined.contains("exynos 2300")) return 90;  // Xclipse 950/940
        if (combined.contains("exynos 2200") || combined.contains("exynos 2100")) return 85;  // Xclipse 920/910
        if (combined.contains("exynos 1380") || combined.contains("exynos 1280")) return 70;  // Mali G68

        // Google Tensor
        if (combined.contains("tensor g3") || combined.contains("tensor g4")) return 88;
        if (combined.contains("tensor g2") || combined.contains("gs201")) return 82;
        if (combined.contains("tensor") || combined.contains("gs101")) return 75;

        // Kirin / HiSilicon
        if (combined.contains("kirin 9010") || combined.contains("kirin 9000s") || combined.contains("kirin 9000")) return 80; // Maleoon 910
        if (combined.contains("kirin 990") || combined.contains("kirin 980")) return 70;

        // UNISOC
        if (combined.contains("t820") || combined.contains("t760") || combined.contains("t770")) return 55;
        if (combined.contains("t619") || combined.contains("t618") || combined.contains("t616") || combined.contains("t612")) return 45;

        // Generic hardware fallback
        if (hardware.contains("qcom") || hardware.contains("qualcomm")) return 70;
        if (hardware.contains("mtk") || hardware.contains("mediatek")) return 60;
        if (hardware.contains("exynos") || hardware.contains("s5e")) return 65;
        if (hardware.contains("kirin") || hardware.contains("hi36") || hardware.contains("hi11")) return 60;
        if (hardware.contains("sprd") || hardware.contains("unisoc")) return 40;

        return 50; // Unknown = mid
    }

    /**
     * Estimates CPU score (rough Geekbench 6 single-core) based on SoC identifiers.
     */
    private static int estimateCpuScore(String hardware, String soc, String board, String device, String model) {
        String combined = hardware + " " + soc + " " + board + " " + device + " " + model;

        // Snapdragon 8 Gen 3/2/1
        if (combined.contains("sm8650") || combined.contains("sm8550") || combined.contains("sm8475") || combined.contains("sm8450")) return 2200;
        if (combined.contains("sm8425") || combined.contains("sm8350")) return 1800;
        if (combined.contains("sm8250") || combined.contains("sdm855")) return 1400;

        // Snapdragon 7 series
        if (combined.contains("sm7550") || combined.contains("sm7450")) return 1500;
        if (combined.contains("sm7325") || combined.contains("sm7225")) return 1200;
        if (combined.contains("sm7150") || combined.contains("sm7125")) return 1000;

        // Snapdragon 6 series
        if (combined.contains("sm6375") || combined.contains("sm6225")) return 900;

        // Dimensity 9000 series
        if (combined.contains("dimensity 9300") || combined.contains("dimensity 9200")) return 2000;
        if (combined.contains("dimensity 9000") || combined.contains("dimensity 8300") || combined.contains("dimensity 8200")) return 1600;
        if (combined.contains("dimensity 8100") || combined.contains("dimensity 8050") || combined.contains("dimensity 8000")) return 1300;
        if (combined.contains("dimensity 1200") || combined.contains("dimensity 1100") || combined.contains("dimensity 1000")) return 1100;
        if (combined.contains("dimensity 900") || combined.contains("dimensity 800") || combined.contains("dimensity 700")) return 900;

        // Exynos
        if (combined.contains("exynos 2400") || combined.contains("exynos 2300")) return 1900;
        if (combined.contains("exynos 2200") || combined.contains("exynos 2100")) return 1400;
        if (combined.contains("exynos 1380") || combined.contains("exynos 1280")) return 1000;

        // Google Tensor
        if (combined.contains("tensor g3") || combined.contains("tensor g4")) return 1600;
        if (combined.contains("tensor g2") || combined.contains("gs201")) return 1300;
        if (combined.contains("tensor") || combined.contains("gs101")) return 1100;

        // Kirin
        if (combined.contains("kirin 9010") || combined.contains("kirin 9000s") || combined.contains("kirin 9000")) return 1300;
        if (combined.contains("kirin 990") || combined.contains("kirin 980")) return 1000;

        // UNISOC
        if (combined.contains("t820") || combined.contains("t760") || combined.contains("t770")) return 800;
        if (combined.contains("t619") || combined.contains("t618") || combined.contains("t616") || combined.contains("t612")) return 650;

        // Generic
        if (hardware.contains("qcom") || hardware.contains("qualcomm")) return 1100;
        if (hardware.contains("mtk") || hardware.contains("mediatek")) return 900;
        if (hardware.contains("exynos") || hardware.contains("s5e")) return 1000;
        if (hardware.contains("kirin") || hardware.contains("hi36") || hardware.contains("hi11")) return 900;
        if (hardware.contains("sprd") || hardware.contains("unisoc")) return 600;

        return 800; // Unknown = mid
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
                        "--enable-skia-graphite " +
                        "--enable-low-latency-webgl " +
                        "--enable-webgl2-compute-context " +
                        "--enable-unsafe-webgpu " +
                        "--enable-webgpu " +
                        "--enable-webassembly-threads " +
                        "--enable-webassembly-simd " +
                        "--enable-gpu-async-worker-context " +
                        "--disable-backgrounding-occluded-windows " +
                        "--disable-renderer-backgrounding " +
                        "--canvas-2d-layers " +
                        "--enable-surface-synchronization " +
                        "--enable-quic " +
                        "--enable-tcp-fastopen " +
                        "--enable-fast-unload " +
                        "--enable-features=Vulkan,UseSkiaRenderer,SkiaGraphite,Canvas2dOOPR,CanvasOopRasterization,DrDc,GpuRasterization,VaapiVideoDecoder,WebAssemblySimd,WebAssemblyThreads,WebAssemblyLazyCompilation,WebViewSurfaceControl,ThreadedScrollAnimator,ZeroCopyTabSwitch,EnableOopRasterizationHighPriorityStrategy,WebRtcHWDecoding,WebRtcHWEncoding,AcceleratedVideoEncoder,AsyncImageDecoding,CanvasColorCache,ServiceWorkerBypassFetchHandler,WebAssemblyBaseline,WebAssemblyTiering,WebAssemblyTurbofan,WebAssemblyFastApi,WebGPU,BackForwardCache,Prerender2,UseGpuSchedulerDfs,HighPriorityGpuTaskScheduling,RawDraw,DelegatedCompositing,CanvasColorCache,WebCodecs " +
                        "--disable-features=UseChromeOSDirectVideoDecoder,LazyFrameLoading,DefaultAngleVulkan,VulkanFromANGLE,CalculateNativeWinOcclusion,MediaEngagementBypassAutoplayPolicies " +
                        "--num-raster-threads=" + rasterThreads + " " +
                        "--enable-drdc " +
                        "--enable-threaded-compositing " +
                        "--enable-webgl-developer-extensions " +
                        "--enable-webgl-draft-extensions " +
                        "--enable-webassembly-simd " +
                        "--enable-webassembly-threads " +
                        "--enable-webassembly-tiering " +
                        "--disable-frame-rate-limit " +
                        "--disable-gpu-vsync " +
                        "--max-gum-fps=480 " +
                        "--js-flags=\"--max-semi-space-size=256 --max-old-space-size=4096 --opt --always-opt --turbo-fast-api-calls --turboshaft --wasm-opt --wasm-tier-up --expose-wasm --wasm-simd --harmony-simd --predictable-gc-schedule --jitless=false\"";
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
                        "--disable-backgrounding-occluded-windows " +
                        "--disable-renderer-backgrounding " +
                        "--enable-surface-synchronization " +
                        "--enable-quic " +
                        "--enable-tcp-fastopen " +
                        "--enable-fast-unload " +
                        "--enable-features=CanvasOopRasterization,Canvas2dOOPR,DrDc,GpuRasterization,VaapiVideoDecoder,WebAssemblySimd,WebAssemblyThreads,WebViewSurfaceControl,ThreadedScrollAnimator,ZeroCopyTabSwitch,WebRtcHWDecoding,WebRtcHWEncoding,AsyncImageDecoding,BackForwardCache,Prerender2,HighPriorityGpuTaskScheduling,RawDraw,WebCodecs " +
                        "--disable-features=UseChromeOSDirectVideoDecoder,LazyFrameLoading,DefaultAngleVulkan,VulkanFromANGLE,WebGPU,CalculateNativeWinOcclusion " +
                        "--num-raster-threads=" + rasterThreads + " " +
                        "--enable-drdc " +
                        "--enable-threaded-compositing " +
                        "--enable-webassembly-simd " +
                        "--enable-webassembly-threads " +
                        "--enable-webassembly-tiering " +
                        "--disable-frame-rate-limit " +
                        "--max-gum-fps=165 " +
                        "--js-flags=\"--max-semi-space-size=128 --max-old-space-size=2048 --opt --turbo-fast-api-calls --turboshaft --wasm-opt --wasm-simd\"";
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
                        "--enable-features=CanvasOopRasterization,GpuRasterization,ZeroCopyTabSwitch " +
                        "--disable-features=Vulkan,UseSkiaRenderer,WebGPU,DrDc,DefaultAngleVulkan,VulkanFromANGLE,CalculateNativeWinOcclusion " +
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
            sb.append("device_config put runtime_native_boot webview_raw_draw true; ");
            sb.append("device_config put runtime_native_boot webview_back_forward_cache true; ");

            if (tier == DeviceTier.FLAGSHIP) {
                sb.append("device_config put runtime_native_boot webview_skia_vulkan true; ");
                sb.append("device_config put runtime_native_boot webview_drdc true; ");
                sb.append("device_config put runtime_native_boot webview_graphite true; ");
                sb.append("device_config put runtime_native_boot webview_webgpu true; ");
                sb.append("device_config put runtime_native_boot webview_webassembly_threads true; ");
                sb.append("setprop debug.chromium.flags \"--enable-gpu-rasterization --enable-zero-copy --enable-drdc --ignore-gpu-blocklist --enable-oop-rasterization --enable-webgl2-compute-context --enable-skia-graphite --enable-raw-draw --enable-webgpu --enable-webassembly-threads --enable-webassembly-simd\"; ");
                sb.append("setprop debug.hwui.use_gpu_pixel_buffers true; ");
                sb.append("setprop debug.hwui.renderer vulkan; ");
                sb.append("setprop debug.hwui.render_thread_priority -20; ");
                sb.append("setprop debug.hwui.fps_limit 0; ");
                sb.append("setprop debug.chromium.prerender 1; ");
                sb.append("setprop debug.v8.flags \"--opt --always-opt --turbo-fast-api-calls --turboshaft --wasm-opt --wasm-simd --wasm-threads --predictable-gc-schedule --jitless=false\"; ");
            } else if (tier == DeviceTier.MID_RANGE) {
                sb.append("device_config put runtime_native_boot webview_drdc true; ");
                sb.append("device_config put runtime_native_boot webview_webassembly_threads true; ");
                sb.append("setprop debug.chromium.flags \"--enable-gpu-rasterization --enable-zero-copy --enable-drdc --ignore-gpu-blocklist --enable-oop-rasterization --enable-raw-draw --enable-webassembly-threads --enable-webassembly-simd\"; ");
                sb.append("setprop debug.hwui.use_gpu_pixel_buffers true; ");
                sb.append("setprop debug.hwui.render_thread_priority -20; ");
                sb.append("setprop debug.hwui.fps_limit 0; ");
                sb.append("setprop debug.v8.flags \"--opt --turbo-fast-api-calls --turboshaft --wasm-opt --wasm-simd\"; ");
            } else {
                // Budget: Safe properties to avoid OpenGL/Vulkan conflicts
                sb.append("setprop debug.chromium.flags \"--enable-gpu-rasterization --enable-zero-copy --ignore-gpu-blocklist --enable-oop-rasterization\"; ");
                sb.append("setprop debug.hwui.render_thread_priority -16; ");
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

    /**
     * §9.1 read-back verification: reads the command-line flag file and the
     * debug.chromium.flags prop back and checks the tier's marker flag.
     */
    public static VerifyResult verifyFlags(Context context) {
        DeviceTier tier = detectDeviceTier(context);
        String required = requiredMarkerFlag(tier);
        String forbidden = tier == DeviceTier.FLAGSHIP ? null : "--enable-webgpu";
        String content = readBackFlags();
        String problem = evaluateFlags(content, required, forbidden);
        if (problem != null) {
            if (content == null || content.trim().isEmpty()) {
                return VerifyResult.unavailable("webview_flags",
                        "flag file and debug.chromium.flags both unreadable");
            }
            return VerifyResult.fail("webview_flags", problem);
        }
        return VerifyResult.pass("webview_flags", tier.name() + " marker flag present");
    }

    /** Marker flag each tier must have present in the read-back content. */
    static String requiredMarkerFlag(DeviceTier tier) {
        switch (tier) {
            case FLAGSHIP:
                return "--enable-webgpu";
            case MID_RANGE:
                return "--enable-drdc";
            case BUDGET_SAFE:
            default:
                return "--enable-gpu-rasterization";
        }
    }

    /**
     * Returns null when read-back flags are correct for the tier,
     * otherwise a human-readable failure reason.
     */
    static String evaluateFlags(String content, String required, String forbidden) {
        if (content == null || content.trim().isEmpty()) return "empty flag content";
        if (required != null && !content.contains(required)) return "missing " + required;
        if (forbidden != null && content.contains(forbidden)) {
            return "tier must not enable " + forbidden;
        }
        return null;
    }

    private static String readBackFlags() {
        StringBuilder sb = new StringBuilder();
        try {
            String out = CommandExecutor.executeSystemCommand(
                    "cat /data/local/tmp/webview-command-line /data/local/tmp/chrome-command-line"
                            + " /data/local/tmp/android-webview-command-line 2>/dev/null");
            if (out != null) sb.append(out);
        } catch (Throwable ignored) {
        }
        try {
            String prop = CommandExecutor.getSystemProperty("debug.chromium.flags");
            if (prop != null) sb.append(' ').append(prop);
        } catch (Throwable ignored) {
        }
        return sb.toString();
    }
}
