package com.gamebooster.app.spoofer.lsposed;

import com.gamebooster.app.spoofer.SpoofProfile;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * SystemPropertiesHooks — intercepts android.os.SystemProperties.get/getInt/
 * getBoolean inside the target game, returning the spoofed profile values for
 * every ro.* key (identity, build, SoC, hardware, security flags).
 *
 * This is the hook that defeats apps reading properties directly via reflection
 * or via getprop after boot.
 */
public final class SystemPropertiesHooks {

    private static volatile Map<String, String> propMap;

    private SystemPropertiesHooks() {}

    public static void apply(LoadPackageParam lpparam, SpoofProfile profile) {
        if (propMap == null) {
            propMap = buildPropertyMap(profile);
        }

        Class<?> sp = XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader);
        if (sp == null) return;

        try {
            XposedHelpers.findAndHookMethod(sp, "get", String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    String key = (String) param.args[0];
                    String spoofed = lookup(key);
                    if (spoofed != null) param.setResult(spoofed);
                }
            });
        } catch (Throwable ignored) {}

        try {
            XposedHelpers.findAndHookMethod(sp, "get", String.class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    String key = (String) param.args[0];
                    String spoofed = lookup(key);
                    if (spoofed != null) param.setResult(spoofed);
                }
            });
        } catch (Throwable ignored) {}

        try {
            XposedHelpers.findAndHookMethod(sp, "getInt", String.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    String key = (String) param.args[0];
                    String spoofed = lookup(key);
                    if (spoofed != null) {
                        try { param.setResult(Integer.parseInt(spoofed.trim())); } catch (NumberFormatException ignored2) {}
                    }
                }
            });
        } catch (Throwable ignored) {}

        try {
            XposedHelpers.findAndHookMethod(sp, "getLong", String.class, long.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    String key = (String) param.args[0];
                    String spoofed = lookup(key);
                    if (spoofed != null) {
                        try { param.setResult(Long.parseLong(spoofed.trim())); } catch (NumberFormatException ignored2) {}
                    }
                }
            });
        } catch (Throwable ignored) {}

        try {
            XposedHelpers.findAndHookMethod(sp, "getBoolean", String.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    String key = (String) param.args[0];
                    String spoofed = lookup(key);
                    if (spoofed != null) {
                        param.setResult("1".equals(spoofed) || "true".equalsIgnoreCase(spoofed));
                    }
                }
            });
        } catch (Throwable ignored) {}

        XposedBridge.log("[GameBooster] SystemProperties hooks installed (" + propMap.size() + " props)");
    }

    private static String lookup(String key) {
        if (key == null || propMap == null) return null;
        String v = propMap.get(key);
        if (v != null) return v;
        // Security flags any key prefix should answer
        if (key.startsWith("ro.product.") || key.startsWith("ro.build.") ||
                key.startsWith("ro.soc.") || key.startsWith("ro.hardware") ||
                key.startsWith("ro.chipname") || key.startsWith("ro.board.") ||
                key.startsWith("ro.boot.")) {
            return propMap.get(key);
        }
        return null;
    }

    private static Map<String, String> buildPropertyMap(SpoofProfile profile) {
        Map<String, String> map = new HashMap<>(profile.generateSystemProperties());

        // Security & anti-emulator flags — always forced
        map.put("ro.kernel.qemu", "0");
        map.put("ro.boot.qemu", "0");
        map.put("ro.debuggable", "0");
        map.put("ro.secure", "1");
        map.put("ro.build.type", "user");
        map.put("ro.build.tags", "release-keys");
        map.put("ro.build.version.codename", "REL");
        map.put("ro.build.version.incremental", profile.displayId);
        map.put("ro.product.cpu.abilist", "arm64-v8a,armeabi-v7a,armeabi");
        map.put("ro.product.cpu.abilist64", "arm64-v8a");
        map.put("ro.product.cpu.abilist32", "armeabi-v7a,armeabi");
        map.put("ro.product.first_api_level", String.valueOf(profile.sdkInt));
        map.put("ro.serialno", "GB" + String.format("%08X", profile.id.hashCode() & 0x7fffffff));
        map.put("ro.boot.serialno", "GB" + String.format("%08X", profile.id.hashCode() & 0x7fffffff));
        map.put("ro.build.version.security_patch", profile.securityPatch);
        map.put("ro.build.date", "Sat Jan 20 04:12:35 UTC 2025");
        map.put("ro.build.date.utc", "1737331955");
        map.put("ro.opengles.version", "196610");
        map.put("ro.hardware.egl", profile.glVendor.toLowerCase().contains("arm") ? "mali" : "adreno");
        map.put("ro.vendor.hardware", profile.hardware);
        map.put("ro.vendor.product.model", profile.model);
        map.put("ro.vendor.product.brand", profile.brand);
        map.put("ro.vendor.product.manufacturer", profile.manufacturer);
        map.put("ro.odm.product.model", profile.model);
        map.put("ro.odm.product.brand", profile.brand);
        map.put("ro.odm.product.manufacturer", profile.manufacturer);

        return map;
    }
}