package com.gamebooster.app.booster;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Android WebView Real-Time Performance & Hardware Acceleration Engine.
 *
 * <p>WebViews are heavily utilized across modern games (Roblox, HTML5/WebGL mini-games,
 * event webviews, Discord/Garena in-game overlays, login portals, and UI components).
 * This channel configures Chromium/WebView command-line flags, Vulkan Skia rendering,
 * GPU rasterization, multi-threaded CPU rasterization, zero-copy buffers, DrDc (Decoupled
 * Raster Dynamic Compositing), and V8 turbo JIT optimizations.
 */
public final class WebViewBoosterChannel {

    private static final String TAG = "WebViewBoosterChannel";

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
     * Returns the curated, maximum-performance command-line flag string for Android WebView.
     */
    public static String getWebViewCommandLineFlags() {
        int coreCount = CpuGovernorChannel.detectCpuCoreCount();
        int rasterThreads = Math.max(2, Math.min(8, coreCount / 2));

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
                "--enable-features=Vulkan,UseSkiaRenderer,DefaultAngleVulkan,CanvasOopRasterization,DrDc,GpuRasterization,VaapiVideoDecoder,WebAssemblySimd,WebAssemblyLazyCompilation,WebViewSurfaceControl,ThreadedScrollAnimator,ZeroCopyTabSwitch,EnableOopRasterizationHighPriorityStrategy,WebRtcHWDecoding,WebRtcHWEncoding,AcceleratedVideoEncoder,VulkanFromANGLE,AsyncImageDecoding,CanvasColorCache,ServiceWorkerBypassFetchHandler,WebAssemblyBaseline,WebAssemblyTiering,WebAssemblyTurbofan,WebGPU " +
                "--disable-features=UseChromeOSDirectVideoDecoder,LazyFrameLoading " +
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

    /**
     * Applies full WebView performance optimizations using elevated shell/Shizuku/Root and system properties.
     */
    public static boolean applyWebViewPerformanceBoost() {
        String flags = getWebViewCommandLineFlags();
        boolean success = true;

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
            sb.append("device_config put runtime_native_boot webview_skia_vulkan true; ");
            sb.append("device_config put runtime_native_boot webview_drdc true; ");
            sb.append("setprop debug.chromium.flags \"--enable-gpu-rasterization --enable-zero-copy --enable-drdc --ignore-gpu-blocklist --enable-oop-rasterization --enable-webgl2-compute-context\"; ");
            sb.append("setprop debug.hwui.use_gpu_pixel_buffers true; ");
            sb.append("setprop debug.hwui.renderer vulkan; ");
            sb.append("setprop debug.hwui.fps_limit 0; ");
            sb.append("setprop debug.v8.flags \"--opt --always-opt --turbo-fast-api-calls --turboshaft\"");

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
            // Root/Shizuku execution is the primary path
        }
    }
}
