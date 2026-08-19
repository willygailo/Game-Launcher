package com.gamebooster.app.spoofer.lsposed;

import com.gamebooster.app.spoofer.SpoofProfile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * ProcFileHooks — feeds the target game's Java-side /proc reads with the
 * profile's mock payloads: /proc/cpuinfo, /proc/meminfo, /proc/version.
 *
 * Games (and their Java-side telemetry libs) that parse these files now see the
 * spoofed SoC cores, frequencies, RAM totals and kernel version instead of the
 * real device values.
 */
public final class ProcFileHooks {

    private static volatile String cpuInfo;
    private static volatile String memInfo;
    private static volatile String procVersion;

    private ProcFileHooks() {}

    public static void apply(LoadPackageParam lpparam, SpoofProfile profile) {
        cpuInfo = profile.generateCpuInfo();
        memInfo = profile.generateMemInfo();
        procVersion = profile.generateProcVersion();

        // FileInputStream(String name)
        try {
            XposedHelpers.findAndHookMethod(FileInputStream.class, "<init>", String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            String path = (String) param.args[0];
                            byte[] mock = mockForPath(path);
                            if (mock != null) param.setResult(new ByteArrayInputStream(mock));
                        }
                    });
        } catch (Throwable ignored) {}

        // FileInputStream(File file)
        try {
            XposedHelpers.findAndHookMethod(FileInputStream.class, "<init>", File.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            File file = (File) param.args[0];
                            if (file == null) return;
                            byte[] mock = mockForPath(file.getAbsolutePath());
                            if (mock != null) param.setResult(new ByteArrayInputStream(mock));
                        }
                    });
        } catch (Throwable ignored) {}
    }

    private static byte[] mockForPath(String path) {
        if (path == null) return null;
        if (path.endsWith("cpuinfo")) return cpuInfo.getBytes();
        if (path.endsWith("meminfo")) return memInfo.getBytes();
        if (path.endsWith("/version")) return procVersion.getBytes();
        return null;
    }
}