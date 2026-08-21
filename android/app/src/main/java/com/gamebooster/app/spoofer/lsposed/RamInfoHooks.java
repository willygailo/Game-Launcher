package com.gamebooster.app.spoofer.lsposed;

import com.gamebooster.app.spoofer.SpoofProfile;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * RamInfoHooks — spoofs ActivityManager.getMemoryInfo() results inside the
 * target game (totalMem/availMem/lowMemory). This is the API games like MLBB,
 * PUBG Mobile, and Genshin Impact read to detect "device RAM" and gate graphics tiers.
 */
public final class RamInfoHooks {

    private static volatile long spoofedTotalBytes = 16384L * 1048576L;
    private static volatile long spoofedAvailBytes = 12288L * 1048576L;

    private RamInfoHooks() {}

    public static void apply(LoadPackageParam lpparam, SpoofProfile profile) {
        if (profile != null) {
            spoofedTotalBytes = (long) profile.ramTotalMb * 1048576L;
            spoofedAvailBytes = (long) profile.ramAvailableMb * 1048576L;
        }

        Class<?> amClass = XposedHelpers.findClass("android.app.ActivityManager", lpparam.classLoader);
        if (amClass == null) return;

        try {
            XposedHelpers.findAndHookMethod(amClass, "getMemoryInfo",
                    XposedHelpers.findClass("android.app.ActivityManager$MemoryInfo", lpparam.classLoader),
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Object info = param.args[0];
                            if (info == null) return;
                            try {
                                XposedHelpers.setLongField(info, "totalMem", spoofedTotalBytes);
                                XposedHelpers.setLongField(info, "availMem", spoofedAvailBytes);
                                XposedHelpers.setLongField(info, "threshold", 0L);
                                XposedHelpers.setBooleanField(info, "lowMemory", false);
                            } catch (Throwable ignored) {}
                        }
                    });
        } catch (Throwable ignored) {}

        XposedBridge.log("[GameBooster] RamInfoHooks applied: " + (spoofedTotalBytes / 1048576L) + "MB total RAM");
    }
}