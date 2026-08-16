package com.gamebooster.app.core;

import android.content.Context;
import com.gamebooster.app.engine.CommandExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class PropertyResolver {

    public interface PropertyManager {
        PropResult set(String key, String value);
        PropResult get(String key);
        boolean supports(String key);
        String getManagerName();
    }

    public static final class PropResult {
        public final boolean success;
        public final String value;
        public final String error;
        public final String source;

        PropResult(boolean success, String value, String error, String source) {
            this.success = success;
            this.value = value;
            this.error = error;
            this.source = source;
        }
        public static PropResult ok(String value, String source) { return new PropResult(true, value, null, source); }
        public static PropResult err(String error, String source) { return new PropResult(false, null, error, source); }
    }

    private final List<PropertyManager> managers;
    private final ConcurrentHashMap<String, PropResult> cache;

    private PropertyResolver(Context ctx, List<PropertyManager> managers) {
        this.managers = managers;
        this.cache = new ConcurrentHashMap<>();
    }

    public static PropertyResolver configure(Context ctx) {
        List<PropertyManager> mgrs = new ArrayList<>();
        mgrs.add(new SystemPropertiesManager());
        mgrs.add(new GlobalSettingsManager());
        mgrs.add(new SystemSettingsManager());
        mgrs.add(new SecureSettingsManager());
        mgrs.add(new KernelParameterManager());
        mgrs.add(new ThermalManager());
        mgrs.add(new DisplayManager());
        mgrs.add(new GpuManager());
        mgrs.add(new CpuManager());
        mgrs.add(new MemoryManager());
        mgrs.add(new NetworkManager());
        mgrs.add(new FileSystemManager());
        return new PropertyResolver(ctx, mgrs);
    }

    public PropResult set(String key, String value) {
        String cacheKey = "set:" + key + "=" + value;
        PropResult cached = cache.get(cacheKey);
        if (cached != null && cached.success) return cached;

        for (PropertyManager m : managers) {
            if (m.supports(key)) {
                PropResult res = m.set(key, value);
                if (res.success) cache.put(cacheKey, res);
                return res;
            }
        }
        return PropResult.err("No manager supports key: " + key, "none");
    }

    public PropResult get(String key) {
        String cacheKey = "get:" + key;
        PropResult cached = cache.get(cacheKey);
        if (cached != null) return cached;

        for (PropertyManager m : managers) {
            if (m.supports(key)) {
                PropResult res = m.get(key);
                cache.put(cacheKey, res);
                return res;
            }
        }
        return PropResult.err("No manager supports key: " + key, "none");
    }

    public void clearCache() { cache.clear(); }

    // ======================== MANAGERS ========================

    private static abstract class BaseManager implements PropertyManager {
        protected PropResult exec(String cmd) {
            String out = CommandExecutor.executeSystemCommand(cmd);
            boolean ok = CommandExecutor.isSuccessOutput(out);
            return ok ? PropResult.ok(out.trim(), getManagerName()) : PropResult.err(out, getManagerName());
        }
    }

    private static class SystemPropertiesManager extends BaseManager {
        @Override public String getManagerName() { return "SystemProperties"; }
        @Override public boolean supports(String k) { return k.startsWith("sys.") || k.startsWith("debug.") || k.startsWith("persist.") || k.startsWith("ro.") || k.startsWith("vendor."); }
        @Override public PropResult set(String k, String v) { return exec("setprop " + k + " " + v); }
        @Override public PropResult get(String k) { return exec("getprop " + k); }
    }

    private static class GlobalSettingsManager extends BaseManager {
        @Override public String getManagerName() { return "Settings.Global"; }
        @Override public boolean supports(String k) { return k.startsWith("global:"); }
        @Override public PropResult set(String k, String v) { return exec("settings put global " + k.substring(7) + " " + v); }
        @Override public PropResult get(String k) { return exec("settings get global " + k.substring(7)); }
    }

    private static class SystemSettingsManager extends BaseManager {
        @Override public String getManagerName() { return "Settings.System"; }
        @Override public boolean supports(String k) { return k.startsWith("system:"); }
        @Override public PropResult set(String k, String v) { return exec("settings put system " + k.substring(7) + " " + v); }
        @Override public PropResult get(String k) { return exec("settings get system " + k.substring(7)); }
    }

    private static class SecureSettingsManager extends BaseManager {
        @Override public String getManagerName() { return "Settings.Secure"; }
        @Override public boolean supports(String k) { return k.startsWith("secure:"); }
        @Override public PropResult set(String k, String v) { return exec("settings put secure " + k.substring(7) + " " + v); }
        @Override public PropResult get(String k) { return exec("settings get secure " + k.substring(7)); }
    }

    private static class KernelParameterManager extends BaseManager {
        @Override public String getManagerName() { return "KernelParams"; }
        @Override public boolean supports(String k) { return k.startsWith("kernel:") || k.startsWith("/proc/") || k.startsWith("/sys/"); }
        @Override public PropResult set(String k, String v) {
            String path = k.startsWith("kernel:") ? k.substring(7) : k;
            return exec("echo " + v + " > " + path);
        }
        @Override public PropResult get(String k) {
            String path = k.startsWith("kernel:") ? k.substring(7) : k;
            return exec("cat " + path);
        }
    }

    private static class ThermalManager extends BaseManager {
        @Override public String getManagerName() { return "Thermal"; }
        @Override public boolean supports(String k) {
            return k.equals("thermal:override") || k.equals("thermal:status") || k.equals("thermal:zones");
        }
        @Override public PropResult set(String k, String v) {
            switch (k) {
                case "thermal:override": return exec("cmd thermalservice override-status " + v);
                case "thermal:throttle": return exec("cmd thermalservice set-throttle " + v);
                default: return exec("cmd thermal " + k.replace("thermal:", "") + " " + v);
            }
        }
        @Override public PropResult get(String k) {
            switch (k) {
                case "thermal:status": return exec("cmd thermalservice get-status");
                case "thermal:zones": return exec("cat /sys/class/thermal/thermal_zone*/type 2>/dev/null; cat /sys/class/thermal/thermal_zone*/temp 2>/dev/null");
                default: return exec("cmd thermal " + k.replace("thermal:", ""));
            }
        }
    }

    private static class DisplayManager extends BaseManager {
        @Override public String getManagerName() { return "Display"; }
        @Override public boolean supports(String k) {
            return k.startsWith("display:") ||
                k.contains("refresh_rate") || k.contains("peak_refresh") || k.contains("min_refresh") ||
                k.contains("user_refresh") || k.contains("density") || k.contains("mode_id");
        }
        @Override public PropResult set(String k, String v) {
            String key = k.startsWith("display:") ? k.substring(8) : k;
            if (key.contains("refresh") || key.contains("mode")) {
                return exec("settings put system " + key + " " + v);
            }
            return exec("setprop " + key + " " + v);
        }
        @Override public PropResult get(String k) {
            String key = k.startsWith("display:") ? k.substring(8) : k;
            if (key.contains("refresh") || key.contains("mode")) {
                return exec("settings get system " + key);
            }
            return exec("getprop " + key);
        }
    }

    private static class GpuManager extends BaseManager {
        @Override public String getManagerName() { return "GPU"; }
        @Override public boolean supports(String k) {
            return k.startsWith("gpu:") || k.contains("kgsl") || k.contains("adreno") || k.contains("gpu_") || k.contains("render");
        }
        @Override public PropResult set(String k, String v) {
            String key = k.startsWith("gpu:") ? k.substring(4) : k;
            if (key.startsWith("/sys/") || key.startsWith("/proc/")) {
                return exec("echo " + v + " > " + key);
            }
            return exec("setprop " + key + " " + v);
        }
        @Override public PropResult get(String k) {
            String key = k.startsWith("gpu:") ? k.substring(4) : k;
            if (key.startsWith("/sys/") || key.startsWith("/proc/")) {
                return exec("cat " + key);
            }
            return exec("getprop " + key);
        }
    }

    private static class CpuManager extends BaseManager {
        @Override public String getManagerName() { return "CPU"; }
        @Override public boolean supports(String k) {
            return k.startsWith("cpu:") || k.contains("scaling_governor") || k.contains("cpufreq") ||
                k.contains("core_ctl") || k.contains("sched_") || k.contains("cpuidle");
        }
        @Override public PropResult set(String k, String v) {
            String key = k.startsWith("cpu:") ? k.substring(4) : k;
            if (key.startsWith("/sys/")) return exec("echo " + v + " > " + key);
            if (key.equals("governor")) return exec("for f in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo " + v + " > $f; done");
            if (key.equals("max_freq")) return exec("for f in /sys/devices/system/cpu/cpu*/cpufreq/scaling_max_freq; do echo " + v + " > $f; done");
            if (key.equals("min_freq")) return exec("for f in /sys/devices/system/cpu/cpu*/cpufreq/scaling_min_freq; do echo " + v + " > $f; done");
            return exec("setprop " + key + " " + v);
        }
        @Override public PropResult get(String k) {
            String key = k.startsWith("cpu:") ? k.substring(4) : k;
            if (key.startsWith("/sys/")) return exec("cat " + key);
            if (key.equals("governor")) return exec("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
            if (key.equals("frequencies")) return exec("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
            if (key.equals("governors")) return exec("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors");
            return exec("getprop " + key);
        }
    }

    private static class MemoryManager extends BaseManager {
        @Override public String getManagerName() { return "Memory"; }
        @Override public boolean supports(String k) {
            return k.startsWith("mem:") || k.contains("vm.") || k.contains("swappiness") ||
                k.contains("drop_caches") || k.contains("compact") || k.contains("oom_") || k.contains("minfree");
        }
        @Override public PropResult set(String k, String v) {
            String key = k.startsWith("mem:") ? k.substring(4) : k;
            if (key.startsWith("/proc/") || key.startsWith("/sys/")) return exec("echo " + v + " > " + key);
            return exec("setprop " + key + " " + v);
        }
        @Override public PropResult get(String k) {
            String key = k.startsWith("mem:") ? k.substring(4) : k;
            if (key.startsWith("/proc/") || key.startsWith("/sys/")) return exec("cat " + key);
            return exec("getprop " + key);
        }
    }

    private static class NetworkManager extends BaseManager {
        @Override public String getManagerName() { return "Network"; }
        @Override public boolean supports(String k) {
            return k.startsWith("net:") || k.startsWith("tcp:") || k.startsWith("wifi:") || k.contains("net.") || k.contains("tcp_");
        }
        @Override public PropResult set(String k, String v) {
            String key = k.startsWith("net:") || k.startsWith("tcp:") || k.startsWith("wifi:") ? k.substring(4) : k;
            if (key.startsWith("/proc/") || key.startsWith("/sys/")) return exec("echo " + v + " > " + key);
            return exec("setprop " + key + " " + v);
        }
        @Override public PropResult get(String k) {
            String key = k.startsWith("net:") || k.startsWith("tcp:") || k.startsWith("wifi:") ? k.substring(4) : k;
            if (key.startsWith("/proc/") || key.startsWith("/sys/")) return exec("cat " + key);
            return exec("getprop " + key);
        }
    }

    private static class FileSystemManager extends BaseManager {
        @Override public String getManagerName() { return "FileSystem"; }
        @Override public boolean supports(String k) {
            return k.startsWith("fs:") || k.contains("scheduler") || k.contains("read_ahead") || k.contains("nr_requests") || k.contains("queue/");
        }
        @Override public PropResult set(String k, String v) {
            String key = k.startsWith("fs:") ? k.substring(3) : k;
            if (key.startsWith("/sys/")) return exec("echo " + v + " > " + key);
            return exec("setprop " + key + " " + v);
        }
        @Override public PropResult get(String k) {
            String key = k.startsWith("fs:") ? k.substring(3) : k;
            if (key.startsWith("/sys/")) return exec("cat " + key);
            return exec("getprop " + key);
        }
    }
}