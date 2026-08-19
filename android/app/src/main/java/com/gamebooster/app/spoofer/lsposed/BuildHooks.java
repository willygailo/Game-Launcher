package com.gamebooster.app.spoofer.lsposed;

import com.gamebooster.app.spoofer.SpoofProfile;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * BuildHooks — overrides android.os.Build static fields INSIDE the target game
 * process before any game code reads them, plus Build.getSerial().
 *
 * The launcher-side reflection (HardwareMaskEngine.applyInAppReflectionMask)
 * only mutated the launcher's own process — these hooks mutate the game's.
 */
public final class BuildHooks {

    private BuildHooks() {}

    public static void apply(LoadPackageParam lpparam, SpoofProfile profile) {
        Class<?> build = XposedHelpers.findClass("android.os.Build", lpparam.classLoader);
        if (build == null) return;

        XposedHelpers.setStaticObjectField(build, "MODEL", profile.model);
        XposedHelpers.setStaticObjectField(build, "BRAND", profile.brand);
        XposedHelpers.setStaticObjectField(build, "MANUFACTURER", profile.manufacturer);
        XposedHelpers.setStaticObjectField(build, "DEVICE", profile.device);
        XposedHelpers.setStaticObjectField(build, "PRODUCT", profile.productName);
        XposedHelpers.setStaticObjectField(build, "BOARD", profile.board);
        XposedHelpers.setStaticObjectField(build, "HARDWARE", profile.hardware);
        XposedHelpers.setStaticObjectField(build, "FINGERPRINT", profile.fingerprint);
        XposedHelpers.setStaticObjectField(build, "DISPLAY", profile.displayId);
        XposedHelpers.setStaticObjectField(build, "HOST", "android-build");

        try {
            XposedHelpers.setStaticObjectField(build, "SOC_MODEL", profile.socModel);
            XposedHelpers.setStaticObjectField(build, "SOC_MANUFACTURER", profile.socManufacturer);
        } catch (Throwable ignored) {}

        try {
            XposedHelpers.setStaticObjectField(build, "SUPPORTED_ABIS", new String[]{"arm64-v8a"});
            XposedHelpers.setStaticObjectField(build, "SUPPORTED_64_BIT_ABIS", new String[]{"arm64-v8a"});
        } catch (Throwable ignored) {}

        Class<?> version = XposedHelpers.findClass("android.os.Build$VERSION", lpparam.classLoader);
        if (version != null) {
            try {
                XposedHelpers.setStaticObjectField(version, "RELEASE", profile.androidVersion);
                XposedHelpers.setStaticIntField(version, "SDK_INT", profile.sdkInt);
                XposedHelpers.setStaticObjectField(version, "SECURITY_PATCH", profile.securityPatch);
                XposedHelpers.setStaticObjectField(version, "CODENAME", "REL");
                XposedHelpers.setStaticObjectField(version, "INCREMENTAL", profile.displayId);
            } catch (Throwable ignored) {}
        }

        // Build.getSerial() — deterministic fake serial
        try {
            XposedHelpers.findAndHookMethod(build, "getSerial",
                    XC_MethodReplacement.returnConstant(generateSerial(profile)));
        } catch (Throwable ignored) {}
    }

    /** Deterministic pseudo-serial derived from the profile id. */
    private static String generateSerial(SpoofProfile profile) {
        int h = profile.id.hashCode() & 0x7fffffff;
        return "GB" + String.format("%08X", h);
    }
}