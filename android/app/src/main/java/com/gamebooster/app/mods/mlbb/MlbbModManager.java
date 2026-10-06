package com.gamebooster.app.mods.mlbb;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.config.MlbbConfigPatcher;
import com.gamebooster.app.config.MlbbHeroScriptDispatcher;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.mods.GameModEngine;
import com.gamebooster.app.mods.GameModProfile;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.spoofer.HardwareMaskEngine;

import java.io.IOException;
import java.io.InputStream;

/**
 * MlbbModManager — Dedicated orchestrator for Mobile Legends: Bang Bang mod operations.
 * Coordinates 4-layer injection, Hero scripting, and IL2CPP memory resolution.
 */
public final class MlbbModManager {

    private static final String TAG = "MlbbModManager";
    public static final String PKG_MLBB = "com.mobile.legends";
    private static final String FRIDA_HOOK_DEVICE_DIR = "/data/local/tmp/gb_hooks/";

    private MlbbModManager() {}

    /**
     * Applies the complete MLBB mod configuration suite based on GameModProfile.
     */
    public static void applyProfile(Context ctx, GameModProfile profile) {
        if (profile == null) return;

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                Log.i(TAG, "⚡ Initializing MLBB Mod Pipeline...");

                // Layer 1: Display refresh rate & surface flinger unlock
                if (profile.mlbbFpsUnlock) {
                    GameModEngine.enforceDisplayRefreshRate(profile.mlbbTargetFps);
                    try {
                        MlbbConfigPatcher.patch(PKG_MLBB, profile.mlbbTargetFps);
                        HardwareMaskEngine.maskAllAndroidVersions(ctx, PKG_MLBB, profile.mlbbTargetFps);
                    } catch (Throwable t) {
                        Log.w(TAG, "MLBB static config patch note: " + t.getMessage());
                    }
                }

                // Layer 2: Asset deployment and Hero scripts
                if (profile.mlbbDamageEnabled || profile.mlbbAttackSpeedEnabled ||
                    profile.mlbbNoCooldown || profile.mlbbMapHack) {
                    try {
                        MlbbConfigPatcher.deployMlbbAssets(ctx, PKG_MLBB);
                        MlbbConfigPatcher.applyBattleConfigOverdrive(PKG_MLBB);
                        MlbbConfigPatcher.applyMlbbMasterSuite(PKG_MLBB);
                        MlbbHeroScriptDispatcher.dispatchAllHeroes(ctx, PKG_MLBB);
                    } catch (Throwable t) {
                        Log.w(TAG, "MLBB master suite error: " + t.getMessage());
                    }
                }

                // Layer 3: SystemProperties for runtime bridge
                setprop("gamebooster.mlbb.dmg",     profile.mlbbDamageEnabled ? String.valueOf(profile.mlbbDamageMult) : "1.0");
                setprop("gamebooster.mlbb.aspd",    profile.mlbbAttackSpeedEnabled ? String.valueOf(profile.mlbbAttackSpeedMult) : "1.0");
                setprop("gamebooster.mlbb.nocool",  profile.mlbbNoCooldown ? "1" : "0");
                setprop("gamebooster.mlbb.map",     profile.mlbbMapHack ? "1" : "0");
                setprop("gamebooster.mlbb.fps",     String.valueOf(profile.mlbbFpsUnlock ? profile.mlbbTargetFps : 60));
                setprop("gamebooster.mlbb.antiban", profile.mlbbAntiBan ? "1" : "0");

                // Layer 4: Frida dynamic hooks injection
                boolean anyModActive = profile.mlbbDamageEnabled || profile.mlbbAttackSpeedEnabled
                        || profile.mlbbNoCooldown || profile.mlbbMapHack || profile.mlbbFpsUnlock || profile.mlbbAntiBan;

                if (anyModActive) {
                    String hookScript = readAsset(ctx, "frida/mlbb_hooks.js");
                    if (!hookScript.isEmpty()) {
                        pushScriptToDevice(hookScript, FRIDA_HOOK_DEVICE_DIR + "mlbb_hooks.js");
                        injectFrida(PKG_MLBB, FRIDA_HOOK_DEVICE_DIR + "mlbb_hooks.js");
                    }
                }

                Log.i(TAG, "✅ MLBB mod pipeline fully deployed.");
            } catch (Throwable t) {
                Log.e(TAG, "MLBB mod pipeline error: " + t.getMessage(), t);
            }
        });
    }

    private static void setprop(String key, String value) {
        exec("setprop " + key + " " + value);
    }

    private static void injectFrida(String packageName, String scriptPath) {
        exec("frida -U -f " + packageName + " -l " + scriptPath + " --no-pause &");
        Log.i(TAG, "[Frida] Injected into " + packageName + " with " + scriptPath);
    }

    private static void pushScriptToDevice(String content, String devicePath) {
        if (content == null || content.isEmpty()) return;
        exec("mkdir -p " + FRIDA_HOOK_DEVICE_DIR);
        exec("echo '" + content.replace("'", "'\\''") + "' > " + devicePath);
    }

    private static String readAsset(Context ctx, String assetPath) {
        try (InputStream is = ctx.getAssets().open(assetPath)) {
            byte[] buf = new byte[is.available()];
            is.read(buf);
            return new String(buf, "UTF-8");
        } catch (IOException e) {
            Log.e(TAG, "Asset read failed: " + assetPath + " — " + e.getMessage());
            return "";
        }
    }

    private static void exec(String cmd) {
        try {
            ShizukuExecutor.executeShizukuCommand(cmd);
        } catch (Throwable ignored) {
            try {
                Runtime.getRuntime().exec(new String[]{"su", "-c", cmd}).waitFor();
            } catch (Exception e) {
                Log.w(TAG, "exec: " + cmd + " → " + e.getMessage());
            }
        }
    }
}
