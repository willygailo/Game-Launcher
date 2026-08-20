package com.gamebooster.app.spoofer.lsposed;

import com.gamebooster.app.spoofer.SpoofProfile;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * SystemPropertiesHooks — intercepts android.os.SystemProperties and
 * vendor-specific property wrappers inside the target game process,
 * returning the spoofed profile values for every ro.* key (identity, build,
 * SoC, hardware, security flags, anti-detection).
 */
public final class SystemPropertiesHooks {

    private static volatile Map<String, String> propMap;

    private SystemPropertiesHooks() {}

    public static void apply(LoadPackageParam lpparam, SpoofProfile profile) {
        if (profile == null) return;
        initPropertyMap(profile);

        // 1. android.os.SystemProperties
        hookSystemPropertiesClass(lpparam.classLoader, "android.os.SystemProperties");

        // 2. Samsung SemSystemProperties (Knox / Game Optimizing Service)
        hookSystemPropertiesClass(lpparam.classLoader, "android.os.SemSystemProperties");

        XposedBridge.log("[GameBooster] SystemProperties hooks installed (" + propMap.size() + " props)");
    }

    public static void initPropertyMap(SpoofProfile profile) {
        if (profile != null) {
            propMap = buildPropertyMap(profile);
        }
    }

    private static void hookSystemPropertiesClass(ClassLoader classLoader, String className) {
        Class<?> sp = XposedHelpers.findClass(className, classLoader);
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
                    String def = (String) param.args[1];
                    String spoofed = lookup(key);
                    if (spoofed != null) {
                        param.setResult(spoofed);
                    } else if (def != null && isKeyHandled(key)) {
                        param.setResult(def);
                    }
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
                        try {
                            param.setResult(Integer.parseInt(spoofed.trim()));
                        } catch (NumberFormatException ignored2) {}
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
                        try {
                            param.setResult(Long.parseLong(spoofed.trim()));
                        } catch (NumberFormatException ignored2) {}
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
    }

    public static String lookup(String key) {
        if (key == null || propMap == null) return null;
        String v = propMap.get(key);
        if (v != null) return v;

        // Dynamic multi-partition fallback matching
        if (key.startsWith("ro.product.") || key.startsWith("ro.vendor.product.") ||
                key.startsWith("ro.odm.product.") || key.startsWith("ro.system.product.") ||
                key.startsWith("ro.system_ext.product.")) {
            if (key.endsWith(".model")) return propMap.get("ro.product.model");
            if (key.endsWith(".brand")) return propMap.get("ro.product.brand");
            if (key.endsWith(".manufacturer")) return propMap.get("ro.product.manufacturer");
            if (key.endsWith(".device")) return propMap.get("ro.product.device");
            if (key.endsWith(".name")) return propMap.get("ro.product.name");
            if (key.endsWith(".cpu.abilist")) return propMap.get("ro.product.cpu.abilist");
            if (key.endsWith(".cpu.abilist64")) return propMap.get("ro.product.cpu.abilist64");
            if (key.endsWith(".cpu.abi")) return "arm64-v8a";
        }

        if (key.startsWith("ro.build.fingerprint") || key.endsWith(".build.fingerprint")) {
            return propMap.get("ro.build.fingerprint");
        }

        if (key.startsWith("ro.soc.") || key.startsWith("ro.hardware") ||
                key.startsWith("ro.chipname") || key.startsWith("ro.board.")) {
            return propMap.get(key);
        }

        return null;
    }

    private static boolean isKeyHandled(String key) {
        if (key == null) return false;
        return key.startsWith("ro.product.") || key.startsWith("ro.build.") ||
                key.startsWith("ro.vendor.") || key.startsWith("ro.boot.") ||
                key.startsWith("ro.soc.") || key.startsWith("ro.hardware");
    }

    public static Map<String, String> buildPropertyMap(SpoofProfile profile) {
        Map<String, String> map = new HashMap<>(profile.generateSystemProperties());

        String serial = "GB" + String.format("%08X", profile.id.hashCode() & 0x7fffffff);
        String eglVendor = profile.glVendor.toLowerCase().contains("arm") ? "mali" : "adreno";

        // Security, Anti-Tamper & Anti-Emulator Flags (Always Enforced Clean)
        map.put("ro.kernel.qemu", "0");
        map.put("ro.boot.qemu", "0");
        map.put("ro.debuggable", "0");
        map.put("ro.secure", "1");
        map.put("ro.build.type", "user");
        map.put("ro.build.tags", "release-keys");
        map.put("ro.build.user", "android-build");
        map.put("ro.build.host", "android-build");
        map.put("ro.build.version.codename", "REL");
        map.put("ro.build.version.incremental", profile.displayId != null ? profile.displayId : "V1.0.0");
        map.put("ro.build.date", "Sat Jan 20 04:12:35 UTC 2025");
        map.put("ro.build.date.utc", "1737331955");
        map.put("ro.build.version.security_patch", profile.securityPatch != null ? profile.securityPatch : "2025-01-05");

        // CPU & ABI Multi-Partition
        map.put("ro.product.cpu.abi", "arm64-v8a");
        map.put("ro.product.cpu.abilist", "arm64-v8a,armeabi-v7a,armeabi");
        map.put("ro.product.cpu.abilist64", "arm64-v8a");
        map.put("ro.product.cpu.abilist32", "armeabi-v7a,armeabi");
        map.put("ro.product.first_api_level", String.valueOf(profile.sdkInt > 0 ? profile.sdkInt : 34));

        // Serials & Hardware
        map.put("ro.serialno", serial);
        map.put("ro.boot.serialno", serial);
        map.put("ro.hardware.egl", eglVendor);
        map.put("ro.hardware.vulkan", eglVendor);
        map.put("ro.opengles.version", "196610"); // OpenGL ES 3.2

        // Vendor & ODM Partitions
        map.put("ro.vendor.hardware", profile.hardware);
        map.put("ro.vendor.product.model", profile.model);
        map.put("ro.vendor.product.brand", profile.brand);
        map.put("ro.vendor.product.manufacturer", profile.manufacturer);
        map.put("ro.vendor.product.device", profile.device);
        map.put("ro.vendor.product.name", profile.productName);
        map.put("ro.vendor.build.fingerprint", profile.fingerprint);

        map.put("ro.odm.product.model", profile.model);
        map.put("ro.odm.product.brand", profile.brand);
        map.put("ro.odm.product.manufacturer", profile.manufacturer);
        map.put("ro.odm.product.device", profile.device);
        map.put("ro.odm.product.name", profile.productName);

        map.put("ro.system.product.model", profile.model);
        map.put("ro.system.product.brand", profile.brand);
        map.put("ro.system.product.manufacturer", profile.manufacturer);
        map.put("ro.system.product.device", profile.device);
        map.put("ro.system.product.name", profile.productName);

        map.put("ro.system_ext.product.model", profile.model);
        map.put("ro.system_ext.product.brand", profile.brand);

        // Boot state (Bypass bootloader unlock detection in soft anti-cheats)
        map.put("ro.boot.mode", "normal");
        map.put("ro.boot.warranty_bit", "0");
        map.put("ro.warranty_bit", "0");
        map.put("ro.boot.flash.locked", "1");
        map.put("ro.boot.verifiedbootstate", "green");
        map.put("ro.boot.vbmeta.device_state", "locked");
        map.put("ro.crypto.state", "encrypted");
        map.put("ro.crypto.type", "file");
        map.put("ro.build.selinux", "1");
        map.put("net.hostname", profile.model.replace(" ", "_"));

        return map;
    }
}