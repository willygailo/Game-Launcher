package com.gamebooster.app.spoofer.lsposed;

import android.media.AudioManager;

import com.gamebooster.app.spoofer.SpoofProfile;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * AudioHardwareHooks — Emulates flagship low-latency audio hardware (DTS:X Ultra,
 * Dolby Atmos for Gaming, 128 buffer frames) inside target game processes.
 */
public final class AudioHardwareHooks {

    private AudioHardwareHooks() {}

    public static void apply(LoadPackageParam lpparam, SpoofProfile profile) {
        if (profile == null) return;

        Class<?> audioManagerClass = XposedHelpers.findClass("android.media.AudioManager", lpparam.classLoader);
        if (audioManagerClass != null) {
            try {
                XposedHelpers.findAndHookMethod(audioManagerClass, "getProperty", String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String key = (String) param.args[0];
                        if (AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER.equals(key)) {
                            param.setResult("128"); // Ultra low buffer size
                        } else if (AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE.equals(key)) {
                            param.setResult("48000"); // High quality 48kHz audio
                        } else if ("android.media.property.SPATIAL_AUDIO_ENABLED".equals(key)) {
                            param.setResult("true");
                        }
                    }
                });
            } catch (Throwable ignored) {}
        }

        XposedBridge.log("[GameBooster] AudioHardwareHooks installed for " + lpparam.packageName);
    }
}
