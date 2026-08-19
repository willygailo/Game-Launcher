package com.gamebooster.app.spoofer.lsposed;

import com.gamebooster.app.spoofer.SpoofProfile;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * GlesHooks — spoofs GL_RENDERER / GL_VENDOR / GL_VERSION returned by
 * GLES10/11/20/30/31/32.glGetString() inside the target game process.
 * Games use these strings to select the graphics tier and FPS unlocks.
 */
public final class GlesHooks {

    private static final int GL_VENDOR = 0x1F00;
    private static final int GL_RENDERER = 0x1F01;
    private static final int GL_VERSION = 0x1F02;
    private static final int GL_EXTENSIONS = 0x1F03;

    private static volatile String renderer;
    private static volatile String vendor;
    private static volatile String version;

    private GlesHooks() {}

    public static void apply(LoadPackageParam lpparam, SpoofProfile profile) {
        renderer = profile.glRenderer;
        vendor = profile.glVendor;
        version = profile.glVersion != null ? profile.glVersion : "OpenGL ES 3.2 V@0615.0";

        String[] classes = {
            "android.opengl.GLES10",
            "android.opengl.GLES11",
            "android.opengl.GLES20",
            "android.opengl.GLES30",
            "android.opengl.GLES31",
            "android.opengl.GLES32"
        };
        for (String cls : classes) {
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
    }
}