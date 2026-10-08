package com.gamebooster.app.mods.codm;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.config.CodmConfigPatcher;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.mods.GameModEngine;
import com.gamebooster.app.mods.GameModProfile;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.spoofer.HardwareMaskEngine;

import java.io.IOException;
import java.io.InputStream;

/**
 * CodmModManager — Dedicated orchestrator for Call of Duty: Mobile mod operations.
 * Coordinates aimbot/recoil suites, weapon tracking, and ACE security cloaking.
 */
public final class CodmModManager {

    private static final String TAG = "CodmModManager";
    public static final String PKG_CODM = "com.garena.game.codm";
    private static final String FRIDA_HOOK_DEVICE_DIR = "/data/local/tmp/gb_hooks/";

    private CodmModManager() {}

    /**
     * Applies the complete CODM mod configuration suite based on GameModProfile.
     */
    public static void applyProfile(Context ctx, GameModProfile profile) {
        applyProfile(ctx, PKG_CODM, profile);
    }

    public static void applyProfile(Context ctx, String packageName, GameModProfile profile) {
        if (profile == null) return;
        final String pkg = (packageName != null && !packageName.trim().isEmpty()) ? packageName.trim() : PKG_CODM;

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                Log.i(TAG, "⚡ Initializing CODM Mod Pipeline for " + pkg + "...");
                CodmIl2cppResolver.loadOffsets(ctx);

                // Layer 1: Display refresh rate & surface flinger unlock
                if (profile.codmFpsUnlock) {
                    GameModEngine.enforceDisplayRefreshRate(profile.codmTargetFps);
                    try {
                        CodmConfigPatcher.patch(pkg, profile.codmTargetFps);
                        HardwareMaskEngine.maskAllAndroidVersions(ctx, pkg, profile.codmTargetFps);
                    } catch (Throwable t) {
                        Log.w(TAG, "CODM static config patch note: " + t.getMessage());
                    }
                }

                // Layer 2: In-storage weapon and combat suites
                if (profile.codmAimbot || profile.codmDamageEnabled ||
                    profile.codmSpeedEnabled || profile.codmNoRecoil || profile.codmAllScopeLock) {
                    try {
                        CodmConfigPatcher.applyCodmMasterSuite(pkg);
                        CodmConfigPatcher.applyNoRecoilNoSpread(pkg);
                        CodmConfigPatcher.applyTrackingBulletConfig(pkg);
                        CodmConfigPatcher.applyAimHeadLockConfig(pkg);
                        CodmConfigPatcher.applyUltraDamageOverdriveConfig(pkg);
                        if (profile.codmAllScopeLock) {
                            CodmConfigPatcher.applyEnemyLockMaxAllScope(pkg);
                        }
                        if (profile.codmAutoHeadshot) {
                            CodmConfigPatcher.applyAutoHeadshotBulletKill(pkg);
                        }

                        // Apply 2026 8-Feature Master Combat Matrix
                        float dmg = profile.codmDamageEnabled ? profile.codmDamageMult : 1.0f;
                        float spd = profile.codmSpeedEnabled ? profile.codmSpeedMult : 1.0f;
                        CodmConfigPatcher.applyMasterCombatMatrix(pkg, dmg, 120.0f, spd, 2.0f, true);
                        com.gamebooster.app.config.GameModAutoSyncEngine.applyAntiRedownloadLocks(pkg);
                    } catch (Throwable t) {
                        Log.w(TAG, "CODM master suite error: " + t.getMessage());
                    }
                }

                // Layer 3: Anticheat Cloak & ACE suppression
                if (profile.codmAntiBan) {
                    CodmSecurityCloak.applyCloak(ctx);
                }

                // Layer 4: SystemProperties for Frida / Native bridge
                setprop("gamebooster.codm.aim",     profile.codmAimbot ? "1" : "0");
                setprop("gamebooster.codm.allscope",profile.codmAllScopeLock ? "1" : "0");
                setprop("gamebooster.codm.dmg",     profile.codmDamageEnabled ? String.valueOf(profile.codmDamageMult) : "1.0");
                setprop("gamebooster.codm.speed",   profile.codmSpeedEnabled ? String.valueOf(profile.codmSpeedMult) : "1.0");
                setprop("gamebooster.codm.recoil",  profile.codmNoRecoil ? "1" : "0");
                setprop("gamebooster.codm.fps",     String.valueOf(profile.codmFpsUnlock ? profile.codmTargetFps : 60));
                setprop("gamebooster.codm.antiban", profile.codmAntiBan ? "1" : "0");

                // Layer 5: Frida dynamic hooks injection
                boolean anyModActive = profile.codmAimbot || profile.codmDamageEnabled ||
                    profile.codmSpeedEnabled || profile.codmNoRecoil || profile.codmFpsUnlock || profile.codmAntiBan;

                if (anyModActive) {
                    String hookScript = readAsset(ctx, "frida/codm_hooks.js");
                    if (!hookScript.isEmpty()) {
                        pushScriptToDevice(hookScript, FRIDA_HOOK_DEVICE_DIR + "codm_hooks.js");
                        injectFrida(pkg, FRIDA_HOOK_DEVICE_DIR + "codm_hooks.js");
                    }
                }

                Log.i(TAG, "✅ CODM mod pipeline fully deployed for " + pkg);
            } catch (Throwable t) {
                Log.e(TAG, "CODM mod pipeline error: " + t.getMessage(), t);
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
