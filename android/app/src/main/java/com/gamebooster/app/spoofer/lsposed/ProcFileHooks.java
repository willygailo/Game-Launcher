package com.gamebooster.app.spoofer.lsposed;

import com.gamebooster.app.spoofer.SpoofProfile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * ProcFileHooks — Intercepts game and telemetry reads of /proc and /system properties:
 * 1. ProcessBuilder.start() and Runtime.exec() commands (e.g. cat /proc/cpuinfo, cat /proc/meminfo, getprop).
 * 2. File size and permission checks on /proc/cpuinfo, /proc/meminfo, /proc/version.
 */
public final class ProcFileHooks {

    private static volatile String cpuInfo = "";
    private static volatile String memInfo = "";
    private static volatile String procVersion = "";
    private static volatile SpoofProfile currentProfile;

    private ProcFileHooks() {}

    public static void apply(LoadPackageParam lpparam, SpoofProfile profile) {
        if (profile == null) return;
        currentProfile = profile;
        cpuInfo = profile.generateCpuInfo();
        memInfo = profile.generateMemInfo();
        procVersion = profile.generateProcVersion();

        // 1. Hook ProcessBuilder.start()
        try {
            XposedHelpers.findAndHookMethod(ProcessBuilder.class, "start", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    ProcessBuilder pb = (ProcessBuilder) param.thisObject;
                    if (pb == null) return;
                    List<String> command = pb.command();
                    if (command == null || command.isEmpty()) return;

                    byte[] mockOutput = resolveCommandOutput(command);
                    if (mockOutput != null) {
                        param.setResult(new MockProcess(mockOutput));
                    }
                }
            });
        } catch (Throwable ignored) {}

        // 2. Hook Runtime.exec(String[])
        try {
            XposedHelpers.findAndHookMethod(Runtime.class, "exec", String[].class, String[].class, File.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String[] cmdarray = (String[]) param.args[0];
                            if (cmdarray == null || cmdarray.length == 0) return;

                            byte[] mockOutput = resolveCommandOutput(Arrays.asList(cmdarray));
                            if (mockOutput != null) {
                                param.setResult(new MockProcess(mockOutput));
                            }
                        }
                    });
        } catch (Throwable ignored) {}

        // 3. Hook File.length() for proc paths
        try {
            XposedHelpers.findAndHookMethod(File.class, "length", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    File file = (File) param.thisObject;
                    if (file == null) return;
                    String path = file.getAbsolutePath();
                    if (path.endsWith("cpuinfo")) {
                        param.setResult((long) cpuInfo.getBytes().length);
                    } else if (path.endsWith("meminfo")) {
                        param.setResult((long) memInfo.getBytes().length);
                    } else if (path.endsWith("/version")) {
                        param.setResult((long) procVersion.getBytes().length);
                    }
                }
            });
        } catch (Throwable ignored) {}

        XposedBridge.log("[GameBooster] ProcFileHooks active for " + lpparam.packageName);
    }

    public static byte[] resolveCommandOutput(List<String> cmd) {
        if (cmd == null || cmd.isEmpty()) return null;
        String full = String.join(" ", cmd).toLowerCase().trim();

        if (full.contains("/proc/cpuinfo") || full.equals("cat /proc/cpuinfo")) {
            return cpuInfo.getBytes();
        }
        if (full.contains("/proc/meminfo") || full.equals("cat /proc/meminfo")) {
            return memInfo.getBytes();
        }
        if (full.contains("/proc/version") || full.equals("cat /proc/version") || full.equals("uname -a") || full.equals("uname -r")) {
            return procVersion.getBytes();
        }
        if (full.startsWith("getprop ")) {
            String[] parts = full.split("\\s+");
            if (parts.length >= 2) {
                String key = parts[1].trim();
                String val = SystemPropertiesHooks.lookup(key);
                if (val != null) {
                    return (val + "\n").getBytes();
                }
            }
        }
        return null;
    }

    public static String getCpuInfo() {
        return cpuInfo;
    }

    public static String getMemInfo() {
        return memInfo;
    }

    public static String getProcVersion() {
        return procVersion;
    }

    /**
     * Mock java.lang.Process that feeds our spoofed output stream.
     */
    private static class MockProcess extends Process {
        private final byte[] output;

        MockProcess(byte[] output) {
            this.output = output != null ? output : new byte[0];
        }

        @Override
        public OutputStream getOutputStream() {
            return new ByteArrayOutputStream();
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(output);
        }

        @Override
        public InputStream getErrorStream() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public int waitFor() {
            return 0;
        }

        @Override
        public int exitValue() {
            return 0;
        }

        @Override
        public void destroy() {}
    }
}