package com.gamebooster.app.spoofer.lsposed;

import com.gamebooster.app.spoofer.SpoofProfile;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * RuntimeMemoryHooks — spoofs java.lang.Runtime memory + CPU core reports
 * inside the target game: totalMemory/maxMemory/freeMemory mirror the profile
 * RAM, availableProcessors() mirrors the profile CPU core count.
 */
public final class RuntimeMemoryHooks {

    private static volatile long totalBytes;
    private static volatile long availBytes;
    private static volatile int cores;

    private RuntimeMemoryHooks() {}

    public static void apply(SpoofProfile profile) {
        totalBytes = profile.ramTotalMb * 1048576L;
        availBytes = profile.ramAvailableMb * 1048576L;
        cores = profile.cpuCores > 0 ? profile.cpuCores : 8;

        try {
            XposedHelpers.findAndHookMethod(Runtime.class, "totalMemory", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(totalBytes);
                }
            });
        } catch (Throwable ignored) {}

        try {
            XposedHelpers.findAndHookMethod(Runtime.class, "maxMemory", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(totalBytes);
                }
            });
        } catch (Throwable ignored) {}

        try {
            XposedHelpers.findAndHookMethod(Runtime.class, "freeMemory", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(availBytes);
                }
            });
        } catch (Throwable ignored) {}

        try {
            XposedHelpers.findAndHookMethod(Runtime.class, "availableProcessors", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(cores);
                }
            });
        } catch (Throwable ignored) {}
    }
}