package com.gamespace.app.core;

import android.content.Context;
import android.provider.Settings;

import com.gamespace.app.core.CommandEngine.Command;
import com.gamespace.app.core.CommandEngine.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class PropertyResolver {

    public interface PropertyManager {
        Result set(String key, String value);
        Result get(String key);
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
    private final CommandEngine engine;

    private PropertyResolver(Context ctx, CommandEngine engine, List<PropertyManager> managers) {
        this.engine = engine;
        this.managers = managers;
        this.cache = new ConcurrentHashMap<>();
    }

    public static PropertyResolver configure(Context ctx, CommandEngine engine) {
        List<PropertyManager> mgrs = new ArrayList<>();
        mgrs.add(new SystemPropertiesManager(engine));
        mgrs.add(new GlobalSettingsManager(engine));
        mgrs.add(new SystemSettingsManager(engine));
        mgrs.add(new SecureSettingsManager(engine));
        mgrs.add(new KernelParameterManager(engine));
        mgrs.add(new ThermalManager(engine));
        mgrs.add(new DisplayManager(engine));
        mgrs.add(new GpuManager(engine));
        mgrs.add(new CpuManager(engine));
        mgrs.add(new MemoryManager(engine));
        mgrs.add(new NetworkManager(engine));
        mgrs.add(new FileSystemManager(engine));
        return new PropertyResolver(ctx, engine, mgrs);
    }

    public PropResult set(String key, String value) {
        String cacheKey = "set:" + key + "=" + value;
        PropResult cached = cache.get(cacheKey);
        if (cached != null && cached.success) return cached;

        for (PropertyManager m : managers) {
            if (m.supports(key)) {
                Result r = m.set(key, value);
                PropResult res = r.success ? PropResult.ok(value, m.getManagerName())
                    : PropResult.err(r.stderr.isEmpty() ? r.stdout : r.stderr, m.getManagerName());
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
                Result r = m.get(key);
                PropResult res = r.success ? PropResult.ok(r.stdout.trim(), m.getManagerName())
                    : PropResult.err(r.stderr.isEmpty() ? r.stdout : r.stderr, m.getManagerName());
                cache.put(cacheKey, res);
                return res;
            }
        }
        return PropResult.err("No manager supports key: " + key, "none");
    }

    public PropResult applyBatch(java.util.Map<String, String> pairs) {
        List<Command> cmds = new ArrayList<>();
        for (var e : pairs.entrySet()) cmds.add(Command.builder(e.getKey()).build());
        List<Result> results = engine.executeBatch(cmds);
        boolean allOk = true;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cmds.size(); i++) {
            String k = cmds.get(i).command;
            Result r = results.get(i);
            if (!r.success) allOk = false;
            sb.append(k).append("=").append(r.success ? "OK" : "FAIL: " + r.stderr).append("\n");
        }
        return allOk ? PropResult.ok(sb.toString(), "batch") : PropResult.err(sb.toString(), "batch");
    }

    public void clearCache() { cache.clear(); }

    // ======================== MANAGERS ========================

    private static abstract class BaseManager implements PropertyManager {
        protected final CommandEngine engine;
        BaseManager(CommandEngine e) { this.engine = e; }
        protected Result exec(String cmd) { return engine.execute(Command.builder(cmd).build()); }
        protected Result execRoot(String cmd) { return engine.execute(Command.builder(cmd).requiresRoot(true).build()); }
    }

    private static class SystemPropertiesManager extends BaseManager {
        SystemPropertiesManager(CommandEngine e) { super(e); }
        @Override public String getManagerName() { return "SystemProperties"; }
        @Override public boolean supports(String k) { return k.startsWith("sys.") || k.startsWith("debug.") || k.startsWith("persist.") || k.startsWith("ro.") || k.startsWith("vendor."); }
        @Override public Result set(String k, String v) { return exec("setprop " + k + " " + v); }
        @Override public Result get(String k) { return exec("getprop " + k); }
    }

    private static class GlobalSettingsManager extends BaseManager {
        GlobalSettingsManager(CommandEngine e) { super(e); }
        @Override public String getManagerName() { return "Settings.Global"; }
        @Override public boolean supports(String k) { return k.startsWith("global:"); }
        @Override public Result set(String k, String v) { return exec("settings put global " + k.substring(7) + " " + v); }
        @Override public Result get(String k) { return exec("settings get global " + k.substring(7)); }
    }

    private static class SystemSettingsManager extends BaseManager {
        SystemSettingsManager(CommandEngine e) { super(e); }
        @Override public String getManagerName() { return "Settings.System"; }
        @Override public boolean supports(String k) { return k.startsWith("system:"); }
        @Override public Result set(String k, String v) { return exec("settings put system " + k.substring(7) + " " + v); }
        @Override public Result get(String k) { return exec("settings get system " + k.substring(7)); }
    }

    private static class SecureSettingsManager extends BaseManager {
        SecureSettingsManager(CommandEngine e) { super(e); }
        @Override public String getManagerName() { return "Settings.Secure"; }
        @Override public boolean supports(String k) { return k.startsWith("secure:"); }
        @Override public Result set(String k, String v) { return exec("settings put secure " + k.substring(7) + " " + v); }
        @Override public Result get(String k) { return exec("settings get secure " + k.substring(7)); }
    }

    private static class KernelParameterManager extends BaseManager {
        KernelParameterManager(CommandEngine e) { super(e); }
        @Override public String getManagerName() { return "KernelParams"; }
        @Override public boolean supports(String k) { return k.startsWith("kernel:") || k.startsWith("/proc/") || k.startsWith("/sys/"); }
        @Override public Result set(String k, String v) {
            String path = k.startsWith("kernel:") ? k.substring(7) : k;
            return execRoot("echo " + v + " > " + path);
        }
        @Override public Result get(String k) {
            String path = k.startsWith("kernel:") ? k.substring(7) : k;
            return execRoot("cat " + path);
        }
    }

    private static class ThermalManager extends BaseManager {
        ThermalManager(CommandEngine e) { super(e); }
        @Override public String getManagerName() { return "Thermal"; }
        @Override public boolean supports(String k) {
            return k.equals("thermal:override") || k.equals("thermal:status") || k.equals("thermal:zones");
        }
        @Override public Result set(String k, String v) {
            return switch (k) {
                case "thermal:override" -> exec("cmd thermalservice override-status " + v);
                case "thermal:throttle" -> exec("cmd thermalservice set-throttle " + v);
                default -> exec("cmd thermal " + k.replace("thermal:", "") + " " + v);
            };
        }
        @Override public Result get(String k) {
            return switch (k) {
                case "thermal:status" -> exec("cmd thermalservice get-status");
                case "thermal:zones" -> exec("cat /sys/class/thermal/thermal_zone*/type 2>/dev/null; cat /sys/class/thermal/thermal_zone*/temp 2>/dev/null");
                default -> exec("cmd thermal " + k.replace("thermal:", ""));
            };
        }
    }

    private static class DisplayManager extends BaseManager {
        DisplayManager(CommandEngine e) { super(e); }
        @Override public String getManagerName() { return "Display"; }
        @Override public boolean supports(String k) {
            return k.startsWith("display:") ||
                k.contains("refresh_rate") || k.contains("peak_refresh") || k.contains("min_refresh") ||
                k.contains("user_refresh") || k.contains("density") || k.contains("mode_id");
        }
        @Override public Result set(String k, String v) {
            String key = k.startsWith("display:") ? k.substring(8) : k;
            if (key.contains("refresh") || key.contains("mode")) {
                return exec("settings put system " + key + " " + v);
            }
            return exec("setprop " + key + " " + v);
        }
        @Override public Result get(String k) {
            String key = k.startsWith("display:") ? k.substring(8) : k;
            if (key.contains("refresh") || key.contains("mode")) {
                return exec("settings get system " + key);
            }
            return exec("getprop " + key);
        }
    }

    private static class GpuManager extends BaseManager {
        GpuManager(CommandEngine e) { super(e); }
        @Override public String getManagerName() { return "GPU"; }
        @Override public boolean supports(String k) {
            return k.startsWith("gpu:") || k.contains("kgsl") || k.contains("adreno") || k.contains("gpu_") || k.contains("render");
        }
        @Override public Result set(String k, String v) {
            String key = k.startsWith("gpu:") ? k.substring(4) : k;
            if (key.startsWith("/sys/") || key.startsWith("/proc/")) {
                return execRoot("echo " + v + " > " + key);
            }
            return exec("setprop " + key + " " + v);
        }
        @Override public Result get(String k) {
            String key = k.startsWith("gpu:") ? k.substring(4) : k;
            if (key.startsWith("/sys/") || key.startsWith("/proc/")) {
                return execRoot("cat " + key);
            }
            return exec("getprop " + key);
        }
    }

    private static class CpuManager extends BaseManager {
        CpuManager(CommandEngine e) { super(e); }
        @Override public String getManagerName() { return "CPU"; }
        @Override public boolean supports(String k) {
            return k.startsWith("cpu:") || k.contains("scaling_governor") || k.contains("cpufreq") ||
                k.contains("core_ctl") || k.contains("sched_") || k.contains("cpuidle");
        }
        @Override public Result set(String k, String v) {
            String key = k.startsWith("cpu:") ? k.substring(4) : k;
            if (key.startsWith("/sys/")) return execRoot("echo " + v + " > " + key);
            if (key.equals("governor")) return execRoot("for f in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo " + v + " > $f; done");
            if (key.equals("max_freq")) return execRoot("for f in /sys/devices/system/cpu/cpu*/cpufreq/scaling_max_freq; do echo " + v + " > $f; done");
            if (key.equals("min_freq")) return execRoot("for f in /sys/devices/system/cpu/cpu*/cpufreq/scaling_min_freq; do echo " + v + " > $f; done");
            return exec("setprop " + key + " " + v);
        }
        @Override public Result get(String k) {
            String key = k.startsWith("cpu:") ? k.substring(4) : k;
            if (key.startsWith("/sys/")) return execRoot("cat " + key);
            if (key.equals("governor")) return execRoot("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
            if (key.equals("frequencies")) return execRoot("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
            if (key.equals("governors")) return execRoot("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors");
            return exec("getprop " + key);
        }
    }

    private static class MemoryManager extends BaseManager {
        MemoryManager(CommandEngine e) { super(e); }
        @Override public String getManagerName() { return "Memory"; }
        @Override public boolean supports(String k) {
            return k.startsWith("mem:") || k.contains("vm.") || k.contains("swappiness") ||
                k.contains("drop_caches") || k.contains("compact") || k.contains("oom_") || k.contains("minfree");
        }
        @Override public Result set(String k, String v) {
            String key = k.startsWith("mem:") ? k.substring(4) : k;
            if (key.startsWith("/proc/") || key.startsWith("/sys/")) return execRoot("echo " + v + " > " + key);
            return exec("setprop " + key + " " + v);
        }
        @Override public Result get(String k) {
            String key = k.startsWith("mem:") ? k.substring(4) : k;
            if (key.startsWith("/proc/") || key.startsWith("/sys/")) return execRoot("cat " + key);
            return exec("getprop " + key);
        }
    }

    private static class NetworkManager extends BaseManager {
        NetworkManager(CommandEngine e) { super(e); }
        @Override public String getManagerName() { return "Network"; }
        @Override public boolean supports(String k) {
            return k.startsWith("net:") || k.startsWith("tcp:") || k.startsWith("wifi:") || k.contains("net.") || k.contains("tcp_");
        }
        @Override public Result set(String k, String v) {
            String key = k.startsWith("net:") || k.startsWith("tcp:") || k.startsWith("wifi:") ? k.substring(4) : k;
            if (key.startsWith("/proc/") || key.startsWith("/sys/")) return execRoot("echo " + v + " > " + key);
            return exec("setprop " + key + " " + v);
        }
        @Override public Result get(String k) {
            String key = k.startsWith("net:") || k.startsWith("tcp:") || k.startsWith("wifi:") ? k.substring(4) : k;
            if (key.startsWith("/proc/") || key.startsWith("/sys/")) return execRoot("cat " + key);
            return exec("getprop " + key);
        }
    }

    private static class FileSystemManager extends BaseManager {
        FileSystemManager(CommandEngine e) { super(e); }
        @Override public String getManagerName() { return "FileSystem"; }
        @Override public boolean supports(String k) {
            return k.startsWith("fs:") || k.contains("scheduler") || k.contains("read_ahead") || k.contains("nr_requests") || k.contains("queue/");
        }
        @Override public Result set(String k, String v) {
            String key = k.startsWith("fs:") ? k.substring(3) : k;
            if (key.startsWith("/sys/")) return execRoot("echo " + v + " > " + key);
            return exec("setprop " + key + " " + v);
        }
        @Override public Result get(String k) {
            String key = k.startsWith("fs:") ? k.substring(3) : k;
            if (key.startsWith("/sys/")) return execRoot("cat " + key);
            return exec("getprop " + key);
        }
    }
}