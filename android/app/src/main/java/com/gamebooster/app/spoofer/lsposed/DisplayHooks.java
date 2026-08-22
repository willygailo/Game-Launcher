package com.gamebooster.app.spoofer.lsposed;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;

import com.gamebooster.app.spoofer.SpoofProfile;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * DisplayHooks — Unlocks High FPS (90Hz, 120Hz, 144Hz, 165Hz, 185Hz) and HDR graphics
 * inside target game processes by hooking Android Display & WindowManager APIs.
 *
 * Many modern games (MLBB, PUBG Mobile, CODM, Wild Rift, Free Fire, Standoff 2)
 * check Display.getRefreshRate() and Display.getSupportedModes() at startup.
 * If the physical device screen reports 60Hz/90Hz, the game locks high FPS options.
 *
 * This hook overrides:
 * 1. Display.getRefreshRate() -> returns target profile refresh rate (e.g. 185.0f / 165.0f / 120.0f).
 * 2. Display.getMode() & Display.getSupportedModes() -> injects high-refresh Display.Mode entries.
 * 3. Display.getHdrCapabilities() -> unlocks HDR/Dolby Vision tiers in games.
 */
public final class DisplayHooks {

    private static volatile float spoofedHz = 185.0f;
    private static volatile int targetWidth = 2448;
    private static volatile int targetHeight = 1080;

    private DisplayHooks() {}

    public static void apply(LoadPackageParam lpparam, SpoofProfile profile) {
        if (profile == null) return;

        spoofedHz = profile.maxRefreshRateHz > 0 ? (float) profile.maxRefreshRateHz : 185.0f;

        Class<?> displayClass = XposedHelpers.findClass("android.view.Display", lpparam.classLoader);
        if (displayClass == null) {
            displayClass = Display.class;
        }

        // 1. Display.getRefreshRate() -> Float target refresh rate
        try {
            XposedHelpers.findAndHookMethod(displayClass, "getRefreshRate", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(spoofedHz);
                }
            });
        } catch (Throwable ignored) {}

        // 2. Display.getMode() -> Current active mode at target Hz
        try {
            XposedHelpers.findAndHookMethod(displayClass, "getMode", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Display.Mode original = (Display.Mode) param.getResult();
                    if (original != null) {
                        Display.Mode mocked = createMockMode(original.getPhysicalWidth(), original.getPhysicalHeight(), spoofedHz);
                        if (mocked != null) {
                            param.setResult(mocked);
                        }
                    }
                }
            });
        } catch (Throwable ignored) {}

        // 3. Display.getSupportedModes() -> List of modes including 60, 90, 120, 144, 165, 185Hz
        try {
            XposedHelpers.findAndHookMethod(displayClass, "getSupportedModes", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Display.Mode[] originalModes = (Display.Mode[]) param.getResult();
                    int width = (originalModes != null && originalModes.length > 0) ? originalModes[0].getPhysicalWidth() : targetWidth;
                    int height = (originalModes != null && originalModes.length > 0) ? originalModes[0].getPhysicalHeight() : targetHeight;

                    // Strict High-Refresh Only: Eliminate 60Hz completely
                    float[] supportedRates = new float[]{90.0f, 120.0f, 144.0f, 165.0f, spoofedHz};
                    List<Display.Mode> newModes = new ArrayList<>();
                    int modeId = 1;
                    for (float rate : supportedRates) {
                        if (rate < 90.0f) continue; // Safety guard: never emit <90Hz
                        Display.Mode m = createMockModeWithId(modeId++, width, height, rate);
                        if (m != null) {
                            newModes.add(m);
                        }
                    }

                    if (!newModes.isEmpty()) {
                        param.setResult(newModes.toArray(new Display.Mode[0]));
                    }
                }
            });
        } catch (Throwable ignored) {}

        // 4. Display.getHdrCapabilities() -> Unlocks Ultra HD & HDR graphics settings
        try {
            Class<?> hdrClass = XposedHelpers.findClass("android.view.Display$HdrCapabilities", lpparam.classLoader);
            if (hdrClass != null) {
                XposedHelpers.findAndHookMethod(displayClass, "getHdrCapabilities", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            Constructor<?> ctor = hdrClass.getDeclaredConstructor(
                                    int[].class, float.class, float.class, float.class);
                            ctor.setAccessible(true);
                            // 1: DOLBY_VISION, 2: HDR10, 3: HLG, 4: HDR10_PLUS
                            int[] supportedHdr = new int[]{1, 2, 3, 4};
                            Object hdrObj = ctor.newInstance(supportedHdr, 1200.0f, 600.0f, 0.0001f);
                            param.setResult(hdrObj);
                        } catch (Throwable ignored) {}
                    }
                });
            }
        } catch (Throwable ignored) {}

        // 5. Display.getDeviceProductInfo() -> Android 11+ (API 30+)
        try {
            Class<?> prodInfoClass = XposedHelpers.findClass("android.hardware.display.DeviceProductInfo", lpparam.classLoader);
            if (prodInfoClass != null) {
                XposedHelpers.findAndHookMethod(displayClass, "getDeviceProductInfo", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            Constructor<?>[] ctors = prodInfoClass.getDeclaredConstructors();
                            for (Constructor<?> c : ctors) {
                                c.setAccessible(true);
                                Class<?>[] pTypes = c.getParameterTypes();
                                if (pTypes.length >= 2 && pTypes[0] == String.class && pTypes[1] == String.class) {
                                    Object[] args = new Object[pTypes.length];
                                    args[0] = profile.model;
                                    args[1] = profile.manufacturer;
                                    for (int i = 2; i < pTypes.length; i++) {
                                        if (pTypes[i] == String.class) args[i] = profile.productName;
                                        else if (pTypes[i] == int.class) args[i] = 2025;
                                        else args[i] = null;
                                    }
                                    param.setResult(c.newInstance(args));
                                    break;
                                }
                            }
                        } catch (Throwable ignored) {}
                    }
                });
            }
        } catch (Throwable ignored) {}

        XposedBridge.log("[GameBooster] DisplayHooks installed: " + spoofedHz + "Hz unlocked for " + lpparam.packageName);
    }

    public static float getSpoofedHz() {
        return spoofedHz;
    }

    public static Display.Mode createMockMode(int width, int height, float refreshRate) {
        return createMockModeWithId(1, width, height, refreshRate);
    }

    public static Display.Mode createMockModeWithId(int id, int width, int height, float refreshRate) {
        try {
            // Display.Mode(int modeId, int width, int height, float refreshRate)
            Constructor<Display.Mode> ctor = Display.Mode.class.getDeclaredConstructor(
                    int.class, int.class, int.class, float.class);
            ctor.setAccessible(true);
            return ctor.newInstance(id, width, height, refreshRate);
        } catch (Throwable t1) {
            try {
                // Older Android / Alternative Constructor signature: (int modeId, int width, int height, float refreshRate, float[] alternativeRefreshRates)
                Constructor<?>[] ctors = Display.Mode.class.getDeclaredConstructors();
                for (Constructor<?> c : ctors) {
                    c.setAccessible(true);
                    Class<?>[] params = c.getParameterTypes();
                    if (params.length == 4 && params[0] == int.class && params[1] == int.class && params[2] == int.class && params[3] == float.class) {
                        return (Display.Mode) c.newInstance(id, width, height, refreshRate);
                    }
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }
}
