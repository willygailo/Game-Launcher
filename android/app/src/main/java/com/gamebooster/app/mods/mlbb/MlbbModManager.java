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
        applyProfile(ctx, PKG_MLBB, profile);
    }

    public static void applyProfile(Context ctx, String packageName, GameModProfile profile) {
        if (profile == null) return;
        final String pkg = (packageName != null && !packageName.trim().isEmpty()) ? packageName.trim() : PKG_MLBB;

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                Log.i(TAG, "⚡ Initializing MLBB Mod Pipeline for " + pkg + "...");

                // Pre-load IL2CPP offsets and dynamic signatures from assets
                try {
                    MlbbIl2cppResolver.loadOffsets(ctx);
                } catch (Throwable t) {
                    Log.w(TAG, "IL2CPP offset load warning: " + t.getMessage());
                }

                // Purge any legacy corrupted files/AssetBundles causing black screen
                try {
                    MlbbConfigPatcher.cleanLegacyLoadingLocks(pkg);
                } catch (Throwable ignored) {}


                // Layer 1: Display refresh rate & surface flinger unlock
                if (profile.mlbbFpsUnlock) {
                    GameModEngine.enforceDisplayRefreshRate(profile.mlbbTargetFps);
                    try {
                        MlbbConfigPatcher.patch(pkg, profile.mlbbTargetFps);
                        HardwareMaskEngine.maskAllAndroidVersions(ctx, pkg, profile.mlbbTargetFps);
                    } catch (Throwable t) {
                        Log.w(TAG, "MLBB static config patch note: " + t.getMessage());
                    }
                }

                // Layer 2: Asset deployment, Drone View, and Hero scripts
                if (profile.mlbbDamageEnabled || profile.mlbbAttackSpeedEnabled ||
                    profile.mlbbNoCooldown || profile.mlbbMapHack || profile.mlbbDroneViewEnabled) {
                    try {
                        MlbbConfigPatcher.deployMlbbAssets(ctx, pkg);
                        MlbbConfigPatcher.applyBattleConfigOverdrive(pkg);
                        MlbbConfigPatcher.applyRankedCombatFullSuite(pkg);
                        MlbbConfigPatcher.applyClassicCombatFullSuite(pkg);
                        MlbbConfigPatcher.applyMlbbRankedMastery(pkg);
                        MlbbConfigPatcher.applyMlbbMasterSuite(pkg);
                        MlbbHeroScriptDispatcher.dispatchAllHeroes(ctx, pkg);

                        if (profile.mlbbDroneViewEnabled) {
                            com.gamebooster.app.config.MlbbDroneViewPatcher.applyDroneViewAtomic(ctx, pkg, profile.mlbbDroneTier);
                        }

                        // Apply 2026 8-Feature Master Combat Matrix
                        float dmg = profile.mlbbDamageEnabled ? profile.mlbbDamageMult : 1.0f;
                        float fov = profile.mlbbDroneViewEnabled ? 180.0f : 0.0f;
                        float aspd = profile.mlbbAttackSpeedEnabled ? profile.mlbbAttackSpeedMult : 1.0f;
                        MlbbConfigPatcher.applyMasterCombatMatrix(pkg, dmg, fov, 1.45f, aspd, profile.mlbbMapHack);
                        com.gamebooster.app.config.GameModAutoSyncEngine.applyAntiRedownloadLocks(pkg);
                    } catch (Throwable t) {
                        Log.w(TAG, "MLBB master suite error: " + t.getMessage());
                    }
                }

                // Layer 3: SystemProperties for runtime bridge
                setprop("gamebooster.mlbb.dmg",     profile.mlbbDamageEnabled ? String.valueOf(profile.mlbbDamageMult) : "1.0");
                setprop("gamebooster.mlbb.aspd",    profile.mlbbAttackSpeedEnabled ? String.valueOf(profile.mlbbAttackSpeedMult) : "1.0");
                setprop("gamebooster.mlbb.nocool",  profile.mlbbNoCooldown ? "1" : "0");
                setprop("gamebooster.mlbb.map",     profile.mlbbMapHack ? "1" : "0");
                setprop("gamebooster.mlbb.drone",   profile.mlbbDroneViewEnabled ? String.valueOf(profile.mlbbDroneTier) : "0");
                setprop("gamebooster.mlbb.fps",     String.valueOf(profile.mlbbFpsUnlock ? profile.mlbbTargetFps : 60));
                setprop("gamebooster.mlbb.antiban", profile.mlbbAntiBan ? "1" : "0");

                // Layer 4: Frida dynamic hooks injection
                boolean anyModActive = profile.mlbbDamageEnabled || profile.mlbbAttackSpeedEnabled
                        || profile.mlbbNoCooldown || profile.mlbbMapHack || profile.mlbbFpsUnlock || profile.mlbbAntiBan;

                if (anyModActive) {
                    String hookScript = readAsset(ctx, "frida/mlbb_hooks.js");
                    if (!hookScript.isEmpty()) {
                        pushScriptToDevice(hookScript, FRIDA_HOOK_DEVICE_DIR + "mlbb_hooks.js");
                        injectFrida(pkg, FRIDA_HOOK_DEVICE_DIR + "mlbb_hooks.js");
                    }
                }

                Log.i(TAG, "✅ MLBB mod pipeline fully deployed for " + pkg);
            } catch (Throwable t) {
                Log.e(TAG, "MLBB mod pipeline error: " + t.getMessage(), t);
            }
        });
    }

    private static void setprop(String key, String value) {
        exec("setprop " + key + " " + value);
    }

    private static void injectFrida(String packageName, String scriptPath) {
        try {
            String whichFrida = ShizukuExecutor.executeShizukuCommand("which frida 2>/dev/null || which frida-inject 2>/dev/null");
            if (whichFrida != null && !whichFrida.trim().isEmpty() && !whichFrida.startsWith("ERROR:")) {
                String bin = whichFrida.trim().split("\n")[0].trim();
                if (bin.contains("frida-inject")) {
                    exec(bin + " -n " + packageName + " -s " + scriptPath + " &");
                } else {
                    exec(bin + " -U -f " + packageName + " -l " + scriptPath + " --no-pause &");
                }
                Log.i(TAG, "[Frida] Injected into " + packageName + " with " + scriptPath);
            } else {
                Log.i(TAG, "ℹ️ Frida CLI not installed on device; native C++ & config layers actively maintaining mods.");
            }
        } catch (Throwable t) {
            Log.w(TAG, "injectFrida note: " + t.getMessage());
        }
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
