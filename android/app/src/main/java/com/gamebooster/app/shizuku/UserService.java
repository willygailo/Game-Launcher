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
        String cmd = "for cpu in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo performance > \"$cpu\" 2>/dev/null; done; " +
                     "for gpu in /sys/class/kgsl/kgsl-3d0/devfreq/governor /sys/class/devfreq/*gpu*/governor; do echo performance > \"$gpu\" 2>/dev/null; done; " +
                     "setprop debug.adreno.turbo 1; " +
                     "setprop debug.mali.sched.priority -20; " +
                     "setprop debug.hwui.render_thread_priority -20";
        execCommand(cmd);
    }

    @Override
    public void restoreCpuGpuGovernors() {
        String cmd = "for cpu in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo schedutil > \"$cpu\" 2>/dev/null; done; " +
                     "for gpu in /sys/class/kgsl/kgsl-3d0/devfreq/governor /sys/class/devfreq/*gpu*/governor; do echo simple_ondemand > \"$gpu\" 2>/dev/null; done; " +
                     "setprop debug.adreno.turbo 0; " +
                     "setprop debug.mali.sched.priority 0; " +
                     "setprop debug.hwui.render_thread_priority 0; " +
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
        String cmd = "setprop debug.thermal.throttle.disable 1; " +
                     "setprop debug.performance.tuning 1; " +
                     "setprop debug.sf.disable_backpressure 1; " +
                     "setprop debug.sf.latch_unsignaled 1; " +
                     "setprop debug.hwui.renderer vulkan; " +
                     "for c in /sys/devices/system/cpu/cpufreq/policy*/scaling_governor; do echo performance > $c 2>/dev/null; done; " +
                     "for g in /sys/class/kgsl/kgsl-3d0/force_bus_on /sys/class/kgsl/kgsl-3d0/force_clk_on; do echo 1 > $g 2>/dev/null; done";
        execCommand(cmd);
    }
}
