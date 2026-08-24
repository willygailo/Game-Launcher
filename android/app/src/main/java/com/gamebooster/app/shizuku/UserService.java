package com.gamebooster.app.shizuku;

import android.os.Process;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class UserService extends IUserService.Stub {

    private static final String TAG = "UserService";

    public UserService() {
        Log.i(TAG, "UserService initialized under privileged UID=" + Process.myUid());
    }

    @Override
    public void destroy() {
        Log.i(TAG, "UserService destroyed.");
        System.exit(0);
    }

    @Override
    public int getUid() {
        return Process.myUid();
    }

    @Override
    public boolean isPrivilegedReady() {
        return true;
    }

    @Override
    public String execCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return "ERROR: Empty command";
        }
        java.lang.Process process = null;
        BufferedReader stdoutReader = null;
        BufferedReader stderrReader = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});

            stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder stdout = new StringBuilder();
            String line;
            while ((line = stdoutReader.readLine()) != null) {
                stdout.append(line).append("\n");
            }

            stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder stderr = new StringBuilder();
            while ((line = stderrReader.readLine()) != null) {
                stderr.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            String stdoutStr = stdout.toString().trim();
            String stderrStr = stderr.toString().trim();

            if (exitCode == 0) {
                return stdoutStr.isEmpty() ? "SUCCESS" : stdoutStr;
            } else {
                return "ERROR: Command failed with exit code " + exitCode + (stderrStr.isEmpty() ? "" : ": " + stderrStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "execCommand exception", e);
            return "ERROR: " + e.getMessage();
        } finally {
            try {
                if (stdoutReader != null) stdoutReader.close();
                if (stderrReader != null) stderrReader.close();
                if (process != null) process.destroy();
            } catch (Exception ignored) {}
        }
    }

    @Override
    public List<String> execBatchCommands(List<String> commands) {
        List<String> results = new ArrayList<>();
        if (commands == null || commands.isEmpty()) {
            return results;
        }
        for (String cmd : commands) {
            results.add(execCommand(cmd));
        }
        return results;
    }

    @Override
    public boolean writeDirectFile(String path, String content, String mode) {
        if (path == null || content == null) return false;
        try {
            File file = new File(path);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(file, false)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
                fos.flush();
            }

            if (mode != null && !mode.isEmpty()) {
                Runtime.getRuntime().exec(new String[]{"chmod", mode, path}).waitFor();
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "writeDirectFile failed for: " + path, e);
            // Elevated shell fallback
            String escaped = content.replace("'", "'\\''");
            String cmd = "mkdir -p \"" + new File(path).getParent() + "\" && printf '%s' '" + escaped + "' > \"" + path + "\"";
            if (mode != null && !mode.isEmpty()) {
                cmd += " && chmod " + mode + " \"" + path + "\"";
            }
            String res = execCommand(cmd);
            return res != null && !res.startsWith("ERROR");
        }
    }

    @Override
    public String readDirectFile(String path) {
        if (path == null) return null;
        try {
            File file = new File(path);
            if (!file.exists() || !file.canRead()) {
                return execCommand("cat \"" + path + "\" 2>/dev/null");
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "readDirectFile failed for: " + path, e);
            return execCommand("cat \"" + path + "\" 2>/dev/null");
        }
    }

    @Override
    public boolean deleteDirectFile(String path) {
        if (path == null) return false;
        try {
            File file = new File(path);
            if (file.exists()) {
                return file.delete();
            }
            return true;
        } catch (Exception e) {
            String res = execCommand("rm -rf \"" + path + "\"");
            return res != null && !res.startsWith("ERROR");
        }
    }

    @Override
    public boolean makeDirectories(String path) {
        if (path == null) return false;
        try {
            File file = new File(path);
            return file.mkdirs() || file.exists();
        } catch (Exception e) {
            String res = execCommand("mkdir -p \"" + path + "\" && chmod 777 \"" + path + "\"");
            return res != null && !res.startsWith("ERROR");
        }
    }

    @Override
    public boolean fileExists(String path) {
        if (path == null) return false;
        try {
            File file = new File(path);
            if (file.exists()) return true;
        } catch (Exception ignored) {}
        String res = execCommand("test -e \"" + path + "\" && echo 1 || echo 0");
        return "1".equals(res != null ? res.trim() : "");
    }

    @Override
    public long getAvailableMemoryBytes() {
        try {
            File meminfo = new File("/proc/meminfo");
            if (meminfo.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(meminfo))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("MemAvailable:")) {
                            String[] parts = line.split("\\s+");
                            if (parts.length >= 2) {
                                long kb = Long.parseLong(parts[1]);
                                return kb * 1024L;
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return -1L;
    }

    @Override
    public void forceDisplayRefreshRate(int hz) {
        if (hz <= 0) return;
        String cmd = "settings put system peak_refresh_rate " + hz + ".0; " +
                     "settings put system min_refresh_rate " + hz + ".0; " +
                     "settings put system user_refresh_rate " + hz + "; " +
                     "settings put global peak_refresh_rate " + hz + ".0; " +
                     "settings put global min_refresh_rate " + hz + ".0; " +
                     "settings put global oneplus_screen_refresh_rate " + hz + "; " +
                     "settings put system miui_refresh_rate " + hz + "; " +
                     "service call SurfaceFlinger 1035 i32 " + hz + "; " +
                     "service call SurfaceFlinger 1036 i32 " + hz;
        execCommand(cmd);
    }

    @Override
    public void trimCachesAndDropCaches() {
        String cmd = "pm trim-caches 2000M; " +
                     "sync; " +
                     "echo 3 > /proc/sys/vm/drop_caches; " +
                     "echo 1 > /proc/sys/vm/compact_memory";
        execCommand(cmd);
    }

    @Override
    public void setCpuGpuPerformanceGovernors() {
        String targetGamesCsv = com.gamebooster.app.booster.GpuTweaksChannel.getTargetGamesCsv();
        String cmd = "settings put global window_animation_scale 0.0; " +
                     "settings put global transition_animation_scale 0.0; " +
                     "settings put global animator_duration_scale 0.0; " +
                     "settings put system window_animation_scale 0.0; " +
                     "settings put system transition_animation_scale 0.0; " +
                     "settings put system animator_duration_scale 0.0; " +
                     "cmd activity update-configuration --anim-scale 0.0; " +
                     "settings put global game_driver_all_apps 0; " +
                     "settings put global updatable_driver_all_apps 0; " +
                     "settings put global game_driver_opt_in_apps " + targetGamesCsv + "; " +
                     "settings put global updatable_driver_production_opt_in_apps " + targetGamesCsv + "; " +
                     "setprop dalvik.vm.execution-mode int:jit; " +
                     "setprop dalvik.vm.usejit true; " +
                     "setprop dalvik.vm.usejitprofiles true; " +
                     "setprop dalvik.vm.heapgrowthlimit 512m; " +
                     "setprop dalvik.vm.heapsize 1024m; " +
                     "setprop dalvik.vm.heaptargetutilization 0.75; " +
                     "setprop dalvik.vm.jitthreshold 100; " +
                     "setprop dalvik.vm.dex2oat-filter speed; " +
                     "setprop pm.dexopt.boot speed-profile; " +
                     "setprop pm.dexopt.install speed; " +
                     "setprop pm.dexopt.bg-dexopt speed; " +
                     "echo 0-15 > /dev/cpuset/top-app/cpus 2>/dev/null; " +
                     "echo 0-15 > /dev/cpuset/foreground/cpus 2>/dev/null; " +
                     "echo 0-3 > /dev/cpuset/background/cpus 2>/dev/null; " +
                     "echo 0-3 > /dev/cpuset/system-background/cpus 2>/dev/null; " +
                     "echo 0-15 > /dev/cpuset/restricted/cpus 2>/dev/null; " +
                     "for p in /sys/devices/system/cpu/cpufreq/policy*; do " +
                     "echo performance > \"$p/scaling_governor\" 2>/dev/null; " +
                     "if [ -f \"$p/scaling_max_freq\" ]; then cat \"$p/scaling_max_freq\" > \"$p/scaling_min_freq\" 2>/dev/null; fi; " +
                     "done; " +
                     "setprop sys.games.cpu_affinity 1; " +
                     "setprop sys.use_fifo 1; " +
                     "setprop sys.perf.sched_uclamp_min 1024; " +
                     "setprop sys.perf.sched_uclamp_min_rt 1024; " +
                     "setprop sys.perf.sched_min_granularity_ns 250000; " +
                     "setprop sys.perf.sched_latency_ns 1000000; " +
                     "setprop sys.perf.sched_boost 1; " +
                     "cmd power set-fixed-performance-mode-enabled true; " +
                     "cmd power set-mode 0 1; " +
                     "cmd power set-mode 2 1; " +
                     "for gpu in /sys/class/kgsl/kgsl-3d0/devfreq/governor /sys/class/devfreq/*gpu*/governor; do echo performance > \"$gpu\" 2>/dev/null; done; " +
                     "echo 0 > /sys/class/kgsl/kgsl-3d0/min_pwrlevel 2>/dev/null; " +
                     "echo 1 > /sys/class/kgsl/kgsl-3d0/force_bus_on 2>/dev/null; " +
                     "echo 1 > /sys/class/kgsl/kgsl-3d0/force_clk_on 2>/dev/null; " +
                     "echo 1 > /sys/class/kgsl/kgsl-3d0/force_rail_on 2>/dev/null; " +
                     "echo 0 > /sys/class/misc/mali0/device/dvfs_enable 2>/dev/null; " +
                     "echo 1 > /sys/module/ged/parameters/gx_game_mode 2>/dev/null; " +
                     "echo 1 > /sys/module/ged/parameters/gx_boost_on 2>/dev/null; " +
                     "echo 1 > /sys/module/ged/parameters/gx_force_cpu_boost 2>/dev/null; " +
                     "echo 100 > /sys/module/ged/parameters/gx_top_app_pid_boost 2>/dev/null; " +
                     "echo 1 > /sys/devices/platform/17000000.gpu/power/control 2>/dev/null; " +
                     "setprop debug.adreno.turbo 1; " +
                     "setprop debug.adreno.perf_level 0; " +
                     "setprop debug.qualcomm.sns.hal 0; " +
                     "setprop vendor.perf.gestureFlingBoost 1; " +
                     "setprop persist.vendor.qti.games.gt.enable 1; " +
                     "setprop vendor.gpu.power_mode 1; " +
                     "setprop debug.mali.sched.priority -20; " +
                     "setprop debug.mali.force_gpu_boost 1; " +
                     "setprop debug.mali.realtime 1; " +
                     "setprop persist.vendor.ged.boost 1; " +
                     "setprop persist.vendor.dpt.enable 1; " +
                     "setprop vendor.ppt.boost 1; " +
                     "setprop debug.tensor.gpu.boost 1; " +
                     "setprop debug.exynos.performance.mode 1; " +
                     "setprop debug.xclipse.gpu.boost 1; " +
                     "setprop debug.hwui.renderer vulkan; " +
                     "setprop debug.renderengine.backend vulkan; " +
                     "setprop debug.renderengine.skia_pipeline true; " +
                     "setprop debug.hwui.use_gpu_pixel_buffers true; " +
                     "setprop debug.hwui.render_thread_priority -20; " +
                     "setprop debug.hwui.skip_empty_damage true; " +
                     "setprop debug.sf.hw 1; " +
                     "setprop debug.sf.latch_unsignaled 1; " +
                     "setprop debug.sf.disable_backpressure 1; " +
                     "setprop debug.sf.enable_gl_backpressure 0; " +
                     "setprop debug.sf.predict_hwc_composition_strategy 1; " +
                     "setprop debug.sf.enable_adpf_cpu_hint true; " +
                     "setprop debug.hwui.use_hint_manager true; " +
                     "setprop persist.sys.adpf.enable 1; " +
                     "setprop debug.adpf.hint.enabled 1; " +
                     "setprop debug.adpf.cpu.boost 1; " +
                     "setprop debug.adpf.gpu.boost 1; " +
                     "setprop debug.egl.force_msaa 1; " +
                     "setprop debug.egl.buffcount 3; " +
                     "setprop persist.sys.use_16bpp_alpha 1; " +
                     "setprop debug.egl.multithread 1; " +
                     "setprop debug.graphics.game_default_frame_rate.disabled 1; " +
                     "setprop ro.vendor.dfps.enable 0; " +
                     "echo '" + com.gamebooster.app.booster.WebViewBoosterChannel.getWebViewCommandLineFlags() + "' > /data/local/tmp/webview-command-line; " +
                     "chmod 644 /data/local/tmp/webview-command-line 2>/dev/null; " +
                     "echo '" + com.gamebooster.app.booster.WebViewBoosterChannel.getWebViewCommandLineFlags() + "' > /data/local/tmp/chrome-command-line; " +
                     "chmod 644 /data/local/tmp/chrome-command-line 2>/dev/null; " +
                     "echo '" + com.gamebooster.app.booster.WebViewBoosterChannel.getWebViewCommandLineFlags() + "' > /data/local/tmp/content-shell-command-line; " +
                     "chmod 644 /data/local/tmp/content-shell-command-line 2>/dev/null; " +
                     "settings put global webview_multiprocess 1; " +
                     "device_config put runtime_native_boot webview_surface_control true; " +
                     "setprop debug.chromium.flags \"--enable-gpu-rasterization --enable-zero-copy --enable-drdc --ignore-gpu-blocklist\"; " +
                     "setprop debug.v8.flags \"--opt --always-opt --turbo-fast-api-calls\"";
        execCommand(cmd);
    }

    @Override
    public void restoreCpuGpuGovernors() {
        String cmd = "for p in /sys/devices/system/cpu/cpufreq/policy*; do echo schedutil > \"$p/scaling_governor\" 2>/dev/null; done; " +
                     "for gpu in /sys/class/kgsl/kgsl-3d0/devfreq/governor /sys/class/devfreq/*gpu*/governor; do echo simple_ondemand > \"$gpu\" 2>/dev/null; done; " +
                     "echo 1 > /sys/class/misc/mali0/device/dvfs_enable 2>/dev/null; " +
                     "setprop sys.games.cpu_affinity 0; " +
                     "setprop sys.use_fifo 0; " +
                     "setprop sys.perf.sched_uclamp_min 0; " +
                     "setprop sys.perf.sched_boost 0; " +
                     "setprop debug.adreno.turbo 0; " +
                     "setprop debug.mali.sched.priority 0; " +
                     "setprop debug.mali.force_gpu_boost 0; " +
                     "setprop persist.vendor.ged.boost 0; " +
                     "setprop debug.hwui.render_thread_priority 0; " +
                     "cmd power set-fixed-performance-mode-enabled false; " +
                     "cmd power set-mode 2 0; " +
                     "cmd power set-mode 0 0";
        execCommand(cmd);
    }

    @Override
    public void optimize5GAndWifi() {
        String cmd = "setprop net.tcp.buffersize.5g 524288,1048576,8388608,262144,524288,4194304; " +
                     "setprop net.tcp.buffersize.6g 524288,1048576,8388608,262144,524288,4194304; " +
                     "setprop net.ipv4.tcp_congestion_control bbr; " +
                     "setprop net.tcp.delack.mode 1; " +
                     "cmd wifi force-low-latency-mode enabled; " +
                     "cmd wifi force-hi-perf-mode enabled; " +
                     "settings put global wifi_scan_always_enabled 0; " +
                     "settings put global mobile_data_always_on 1";
        execCommand(cmd);
    }

    @Override
    public boolean applyHardwareMask(String buildProps, String mockCpuInfo, String mockMemInfo) {
        try {
            if (buildProps != null && !buildProps.isEmpty()) {
                for (String line : buildProps.split("\n")) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        int eq = line.indexOf('=');
                        if (eq > 0) {
                            String key = line.substring(0, eq).trim();
                            String val = line.substring(eq + 1).trim();
                            execCommand("setprop " + key + " \"" + val + "\"");
                        }
                    }
                }
            }
            if (mockCpuInfo != null && !mockCpuInfo.isEmpty()) {
                writeDirectFile("/data/local/tmp/mock_cpuinfo", mockCpuInfo, "666");
            }
            if (mockMemInfo != null && !mockMemInfo.isEmpty()) {
                writeDirectFile("/data/local/tmp/mock_meminfo", mockMemInfo, "666");
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "applyHardwareMask failed", e);
            return false;
        }
    }

    @Override
    public boolean patchGameConfigFile(String targetPath, String content, String chmodMode) {
        if (targetPath == null || content == null) return false;
        String mode = (chmodMode != null && !chmodMode.isEmpty()) ? chmodMode : "666";
        return writeDirectFile(targetPath, content, mode);
    }

    @Override
    public void setGameModeApi(String packageName, int targetFps) {
        if (packageName == null || packageName.isEmpty()) return;
        final int fps = targetFps > 0 ? targetFps : 185;
        String cmd = "cmd game mode performance " + packageName + "; " +
                     "cmd game set --fps " + fps + " " + packageName + "; " +
                     "cmd window set-app-refresh-rate " + packageName + " " + fps;
        execCommand(cmd);
    }

    @Override
    public void enforceAppOpsAndPermissions(String packageName) {
        if (packageName == null || packageName.isEmpty()) return;
        String[] permissions = new String[]{
                "android.permission.MANAGE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.WRITE_SETTINGS",
                "android.permission.SYSTEM_ALERT_WINDOW"
        };
        for (String perm : permissions) {
            execCommand("pm grant " + packageName + " " + perm + " 2>/dev/null");
        }
        String[] appOps = new String[]{
                "MANAGE_EXTERNAL_STORAGE",
                "READ_EXTERNAL_STORAGE",
                "WRITE_EXTERNAL_STORAGE",
                "LEGACY_STORAGE",
                "NO_ISOLATED_STORAGE",
                "RUN_IN_BACKGROUND",
                "RUN_ANY_IN_BACKGROUND",
                "AUTO_START",
                "SYSTEM_ALERT_WINDOW"
        };
        for (String op : appOps) {
            execCommand("cmd appops set " + packageName + " " + op + " allow 2>/dev/null");
        }
    }

    @Override
    public void applyThermalAndKernelBoost() {
        String cmd = "cmd thermalservice override-status 0; " +
                     "cmd thermal override-status 0; " +
                     "setprop debug.thermal.throttle.disable 1; " +
                     "setprop debug.performance.tuning 1; " +
                     "setprop debug.sf.disable_backpressure 1; " +
                     "setprop debug.sf.latch_unsignaled 1; " +
                     "setprop debug.hwui.renderer vulkan; " +
                     "setprop debug.sensor.gyro.sample_rate 1000; " +
                     "setprop debug.sensor.gyro.smooth 1; " +
                     "setprop debug.sensor.gyro.stabilization 1; " +
                     "setprop persist.sys.gyro.filter 1; " +
                     "setprop persist.sys.gyro.delay 0; " +
                     "setprop debug.input.max_events_per_sec 1000; " +
                     "setprop persist.sys.touch.report_rate 1000; " +
                     "setprop persist.vendor.touch.sampling_rate 1000; " +
                     "setprop view.touch_slop 0; " +
                     "setprop sys.use_fifo 1; " +
                     "for c in /sys/devices/system/cpu/cpufreq/policy*/scaling_governor; do echo performance > $c 2>/dev/null; done; " +
                     "for g in /sys/class/kgsl/kgsl-3d0/force_bus_on /sys/class/kgsl/kgsl-3d0/force_clk_on; do echo 1 > $g 2>/dev/null; done";
        execCommand(cmd);
    }
}
