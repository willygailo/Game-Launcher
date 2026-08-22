package com.gamebooster.app.spoofer.lsposed;

import com.gamebooster.app.spoofer.SpoofProfile;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * GlesHooks — spoofs GL_RENDERER, GL_VENDOR, GL_VERSION, and EGL strings
 * returned by GLES10/11/20/30/31/32 and EGL14 / EGL10 inside the target game process.
 *
 * Games use these strings to determine GPU capabilities, shader complexity,
 * and graphics tiers (e.g. Adreno 840 / Immortalis-G925 unlocking Extreme / Vulkan Ultra).
 */
public final class GlesHooks {

    private static final int GL_VENDOR = 0x1F00;
    private static final int GL_RENDERER = 0x1F01;
    private static final int GL_VERSION = 0x1F02;
    private static final int GL_EXTENSIONS = 0x1F03;

    private static final int EGL_VENDOR = 0x3053;
    private static final int EGL_VERSION = 0x3054;
    private static final int EGL_EXTENSIONS = 0x3055;

    private static volatile String renderer;
    private static volatile String vendor;
    private static volatile String version;

    private GlesHooks() {}

    public static void apply(LoadPackageParam lpparam, SpoofProfile profile) {
        if (profile == null) return;

        renderer = profile.glRenderer != null && !profile.glRenderer.isEmpty()
                ? profile.glRenderer
                : "Adreno (TM) 840";
        vendor = profile.glVendor != null && !profile.glVendor.isEmpty()
                ? profile.glVendor
                : "Qualcomm";
        version = profile.glVersion != null && !profile.glVersion.isEmpty()
                ? profile.glVersion
                : "OpenGL ES 3.2 V@0615.0 (GIT@8c90967, I8b0f807df9)";

        // 1. Hook Android OpenGL ES glGetString
        String[] glesClasses = {
            "android.opengl.GLES10",
            "android.opengl.GLES11",
            "android.opengl.GLES20",
            "android.opengl.GLES30",
            "android.opengl.GLES31",
            "android.opengl.GLES32"
        };
        for (String cls : glesClasses) {
            try {
                XposedHelpers.findAndHookMethod(cls, lpparam.classLoader, "glGetString",
                        int.class, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                int name = (Integer) param.args[0];
                                if (name == GL_RENDERER) param.setResult(renderer);
                                else if (name == GL_VENDOR) param.setResult(vendor);
                                else if (name == GL_VERSION) param.setResult(version);
                            }
                        });
            } catch (Throwable ignored) {}
        }

        // 2. Hook EGL14.eglQueryString
        try {
            Class<?> egl14Class = XposedHelpers.findClass("android.opengl.EGL14", lpparam.classLoader);
            if (egl14Class != null) {
                Class<?> eglDisplayClass = XposedHelpers.findClass("android.opengl.EGLDisplay", lpparam.classLoader);
                if (eglDisplayClass != null) {
                    XposedHelpers.findAndHookMethod(egl14Class, "eglQueryString",
                            eglDisplayClass, int.class, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) {
                                    int name = (Integer) param.args[1];
                                    if (name == EGL_VENDOR) param.setResult(vendor);
                                    else if (name == EGL_VERSION) param.setResult("1.5 Android meta-EGL (" + vendor + ")");
                                }
                            });
                }
            }
        } catch (Throwable ignored) {}

        // 3. Hook javax.microedition.khronos.egl.EGL10 (legacy / Unity fallback)
        try {
            Class<?> egl10Class = XposedHelpers.findClass("javax.microedition.khronos.egl.EGL10", lpparam.classLoader);
            Class<?> legacyDisplayClass = XposedHelpers.findClass("javax.microedition.khronos.egl.EGLDisplay", lpparam.classLoader);
            if (egl10Class != null && legacyDisplayClass != null) {
                XposedHelpers.findAndHookMethod(egl10Class, "eglQueryString",
                        legacyDisplayClass, int.class, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                int name = (Integer) param.args[1];
                                if (name == EGL_VENDOR) param.setResult(vendor);
                                else if (name == EGL_VERSION) param.setResult("1.4 (" + vendor + ")");
                            }
                        });
            }
        } catch (Throwable ignored) {}

        XposedBridge.log("[GameBooster] GlesHooks installed for " + lpparam.packageName
                + " -> " + renderer + " (" + vendor + ")");
    }
}