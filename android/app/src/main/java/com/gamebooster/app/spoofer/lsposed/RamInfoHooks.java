package com.gamebooster.app.spoofer.lsposed;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * RamInfoHooks — spoofs ActivityManager.getMemoryInfo() results inside the
 * target game (totalMem/availMem/lowMemory). This is the API games like MLBB
 * and PUBG read to detect "device RAM" and gate graphics tiers.
 */
public final class RamInfoHooks {

    private RamInfoHooks() {}

    public static void apply(LoadPackageParam lpparam) {
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
                            com.gamebooster.app.spoofer.SpoofProfile profile =
                                    SpoofConfigBridge.resolveProfile(null);
                            if (profile == null) return;
                            try {
                                XposedHelpers.setLongField(info, "totalMem", profile.ramTotalMb * 1048576L);
                                XposedHelpers.setLongField(info, "availMem", profile.ramAvailableMb * 1048576L);
                                XposedHelpers.setLongField(info, "threshold", 0L);
                                XposedHelpers.setBooleanField(info, "lowMemory", false);
                            } catch (Throwable ignored) {}
                        }
                    });
        } catch (Throwable ignored) {}
    }
}