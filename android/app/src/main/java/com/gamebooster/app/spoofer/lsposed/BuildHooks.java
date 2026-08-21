package com.gamebooster.app.spoofer.lsposed;

import com.gamebooster.app.spoofer.SpoofProfile;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * BuildHooks — overrides android.os.Build and android.os.Build.VERSION static fields
 * and methods inside the target game process before game code reads them.
 */
public final class BuildHooks {

    private BuildHooks() {}

    public static void apply(LoadPackageParam lpparam, SpoofProfile profile) {
        if (profile == null) return;

        Class<?> build = XposedHelpers.findClass("android.os.Build", lpparam.classLoader);
        if (build == null) return;

        String serial = generateSerial(profile);

        // Core Identity Fields
        setStaticFieldSilent(build, "MODEL", profile.model);
        setStaticFieldSilent(build, "BRAND", profile.brand);
        setStaticFieldSilent(build, "MANUFACTURER", profile.manufacturer);
        setStaticFieldSilent(build, "DEVICE", profile.device);
        setStaticFieldSilent(build, "PRODUCT", profile.productName);
        setStaticFieldSilent(build, "BOARD", profile.board);
        setStaticFieldSilent(build, "HARDWARE", profile.hardware);
        setStaticFieldSilent(build, "FINGERPRINT", profile.fingerprint);
        setStaticFieldSilent(build, "DISPLAY", profile.displayId);
        setStaticFieldSilent(build, "ID", profile.displayId);
        setStaticFieldSilent(build, "BOOTLOADER", profile.board);
        setStaticFieldSilent(build, "RADIO", "unknown");
        setStaticFieldSilent(build, "TAGS", "release-keys");
        setStaticFieldSilent(build, "TYPE", "user");
        setStaticFieldSilent(build, "USER", "android-build");
        setStaticFieldSilent(build, "HOST", "android-build");
        setStaticFieldSilent(build, "SERIAL", serial);

        try {
            XposedHelpers.setStaticLongField(build, "TIME", 1737331955000L);
        } catch (Throwable ignored) {}

        // SoC Metadata (Android 12+)
        if (profile.socModel != null && !profile.socModel.isEmpty()) {
            setStaticFieldSilent(build, "SOC_MODEL", profile.socModel);
        }
        if (profile.socManufacturer != null && !profile.socManufacturer.isEmpty()) {
            setStaticFieldSilent(build, "SOC_MANUFACTURER", profile.socManufacturer);
        }

        // Architecture ABI Lists
        try {
            XposedHelpers.setStaticObjectField(build, "SUPPORTED_ABIS", new String[]{"arm64-v8a", "armeabi-v7a", "armeabi"});
            XposedHelpers.setStaticObjectField(build, "SUPPORTED_64_BIT_ABIS", new String[]{"arm64-v8a"});
            XposedHelpers.setStaticObjectField(build, "SUPPORTED_32_BIT_ABIS", new String[]{"armeabi-v7a", "armeabi"});
        } catch (Throwable ignored) {}

        // Build.VERSION Subclass
        Class<?> version = XposedHelpers.findClass("android.os.Build$VERSION", lpparam.classLoader);
        if (version != null) {
            setStaticFieldSilent(version, "RELEASE", profile.androidVersion != null ? profile.androidVersion : "15");
            try {
                XposedHelpers.setStaticIntField(version, "SDK_INT", profile.sdkInt > 0 ? profile.sdkInt : 35);
            } catch (Throwable ignored) {}
            setStaticFieldSilent(version, "SECURITY_PATCH", profile.securityPatch != null ? profile.securityPatch : "2025-01-05");
            setStaticFieldSilent(version, "CODENAME", "REL");
            setStaticFieldSilent(version, "INCREMENTAL", profile.displayId != null ? profile.displayId : "V1.0.0");
            setStaticFieldSilent(version, "BASE_OS", "");
            try {
                XposedHelpers.setStaticIntField(version, "PREVIEW_SDK_INT", 0);
                XposedHelpers.setStaticIntField(version, "MEDIA_PERFORMANCE_CLASS", 35);
            } catch (Throwable ignored) {}
        }

        // Method hooks: Build.getSerial() & Build.getRadioVersion()
        try {
            XposedHelpers.findAndHookMethod(build, "getSerial",
                    XC_MethodReplacement.returnConstant(serial));
        } catch (Throwable ignored) {}

        try {
            XposedHelpers.findAndHookMethod(build, "getRadioVersion",
                    XC_MethodReplacement.returnConstant("MPSS.DE.1.0-00001"));
        } catch (Throwable ignored) {}

        // Android 10+ Build.getFingerprintedPartitions()
        try {
            XposedHelpers.findAndHookMethod(build, "getFingerprintedPartitions", new de.robv.android.xposed.XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        Class<?> partitionClass = XposedHelpers.findClass("android.os.Build$Partition", lpparam.classLoader);
                        if (partitionClass != null) {
                            java.lang.reflect.Constructor<?> ctor = partitionClass.getDeclaredConstructor(String.class, String.class, long.class);
                            ctor.setAccessible(true);
                            java.util.List<Object> list = new java.util.ArrayList<>();
                            list.add(ctor.newInstance("system", profile.fingerprint, 1737331955000L));
                            list.add(ctor.newInstance("vendor", profile.fingerprint, 1737331955000L));
                            list.add(ctor.newInstance("odm", profile.fingerprint, 1737331955000L));
                            list.add(ctor.newInstance("product", profile.fingerprint, 1737331955000L));
                            list.add(ctor.newInstance("system_ext", profile.fingerprint, 1737331955000L));
                            param.setResult(list);
                        }
                    } catch (Throwable ignored) {}
                }
            });
        } catch (Throwable ignored) {}

        XposedBridge.log("[GameBooster] BuildHooks applied: " + profile.model + " [" + profile.brand + "]");
    }

    private static void setStaticFieldSilent(Class<?> clazz, String fieldName, Object value) {
        try {
            XposedHelpers.setStaticObjectField(clazz, fieldName, value);
        } catch (Throwable ignored) {}
    }

    /** Deterministic pseudo-serial derived from the profile id. */
    public static String generateSerial(SpoofProfile profile) {
        int h = profile.id.hashCode() & 0x7fffffff;
        return "GB" + String.format("%08X", h);
    }
}