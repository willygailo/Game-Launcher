package com.gamebooster.app.mods;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.gamebooster.app.config.CodmConfigPatcher;
import com.gamebooster.app.config.MlbbConfigPatcher;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.spoofer.HardwareMaskEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * GameModEngine — Core engine for MLBB/CODM mod operations.
 *
 * Responsibilities:
 *  1. Apply SystemProperties for each enabled mod (read by Frida hooks)
 *  2. Start frida-server on the device (via shell)
 *  3. Inject the correct hook script into the target game process
 *  4. Stop frida-server when game session ends
 *  5. APK pull/push workflow helpers
 *  6. 4-Layer real-game 120/144/165 FPS unlock enforcement
 */
public final class GameModEngine {

    private static final String TAG = "GameModEngine";

    public static final String PKG_MLBB = "com.mobile.legends";
    public static final String PKG_CODM = "com.garena.game.codm";

    private static final String FRIDA_SERVER_DEVICE_PATH = "/data/local/tmp/frida-server";
    private static final String FRIDA_HOOK_DEVICE_DIR    = "/data/local/tmp/gb_hooks/";

    private GameModEngine() {}

    // ─── Frida Server ─────────────────────────────────────────────────────────

    /** Start frida-server in background on device (requires root/Shizuku). */
    public static void startFridaServer(Context ctx) {
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                exec("killall frida-server 2>/dev/null; sleep 0.2");
                exec(FRIDA_SERVER_DEVICE_PATH + " &");
                Thread.sleep(800); // let server bind
                Log.i(TAG, "✅ frida-server started");
            } catch (Throwable t) {
                Log.e(TAG, "frida-server start failed: " + t.getMessage());
            }
        });
    }

    /** Stop frida-server. */
    public static void stopFridaServer() {
        AppExecutors.getInstance().executeCommand(() -> exec("killall frida-server 2>/dev/null"));
    }

    // ─── 4-Layer Real-Game Display & FPS Enforcement ─────────────────────────

    /**
     * Enforces hardware display panel refresh rate & SurfaceFlinger caps
     * to ensure the OS never drops to 60Hz/90Hz during game sessions.
     */
    public static void enforceDisplayRefreshRate(int targetHz) {
        if (targetHz <= 60) return;
        try {
            String cmd = String.format(
                "settings put system peak_refresh_rate %d.0; " +
                "settings put system min_refresh_rate %d.0; " +
                "settings put system user_refresh_rate %d; " +
                "settings put global peak_refresh_rate %d.0; " +
                "settings put global min_refresh_rate %d.0; " +
                "service call SurfaceFlinger 1035 i32 %d 2>/dev/null; " +
                "cmd window set-app-refresh-rate global %d 2>/dev/null; " +
                "cmd game set --fps %d global 2>/dev/null; " +
                "setprop debug.graphics.game_default_frame_rate.disabled 1; " +
                "setprop ro.vendor.dfps.enable 0; " +
                "setprop persist.vendor.display.vrr.disable 1; " +
                "setprop debug.sf.fps_limit %d; " +
                "setprop persist.sys.game.fps %d",
                targetHz, targetHz, targetHz, targetHz, targetHz,
                targetHz, targetHz, targetHz, targetHz, targetHz
            );
            exec(cmd);
            Log.i(TAG, "⚡ Enforced display refresh rate: " + targetHz + "Hz");
        } catch (Throwable t) {
            Log.w(TAG, "Display refresh rate enforcement warning: " + t.getMessage());
        }
    }

    // ─── Mod Apply ────────────────────────────────────────────────────────────

    /**
     * Apply mod profile for a game:
     *  1. Write all config values as SystemProperties (read by Frida JS)
     *  2. Execute display refresh rate overclock and static storage patches
     *  3. Push hook script to device
     *  4. Inject into running game process via frida-inject
     */
    public static void applyMods(Context ctx, String packageName, GameModProfile profile) {
        if (PKG_MLBB.equals(packageName)) {
            com.gamebooster.app.mods.mlbb.MlbbModManager.applyProfile(ctx, profile);
        } else if (PKG_CODM.equals(packageName)) {
            com.gamebooster.app.mods.codm.CodmModManager.applyProfile(ctx, profile);
        }
    }

    // ─── APK Pull / Push ──────────────────────────────────────────────────────

    /**
     * Pull base.apk from the device for a given package into local mods directory.
     * @param packageName  e.g. "com.mobile.legends"
     * @param outputPath   local path to save pulled APK
     * @param callback     called on completion with success flag + message
     */
    public static void pullBaseApk(String packageName, String outputPath, PullCallback callback) {
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                // Get apk path from device
                String pathResult = execOutput("adb shell pm path " + packageName + " | grep base.apk | cut -d: -f2 | tr -d '\\r\\n'");
                if (pathResult == null || pathResult.isEmpty()) {
                    if (callback != null) callback.onResult(false, "Could not find base.apk for " + packageName);
                    return;
                }
                String apkPath = pathResult.trim();
                String pullCmd = "adb pull \"" + apkPath + "\" \"" + outputPath + "\"";
                exec(pullCmd);
                if (callback != null) callback.onResult(true, "Pulled: " + outputPath);
                Log.i(TAG, "✅ Pulled " + packageName + " → " + outputPath);
            } catch (Throwable t) {
                if (callback != null) callback.onResult(false, t.getMessage());
            }
        });
    }

    /** Reinstall a patched APK onto the device. */
    public static void pushAndInstallApk(String apkPath, PullCallback callback) {
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                exec("adb install -r -d \"" + apkPath + "\"");
                if (callback != null) callback.onResult(true, "Installed: " + apkPath);
            } catch (Throwable t) {
                if (callback != null) callback.onResult(false, t.getMessage());
            }
        });
    }

    public interface PullCallback {
        void onResult(boolean success, String message);
    }

    // ─── Internal Helpers ─────────────────────────────────────────────────────

    private static void setprop(String key, String value) {
        exec("setprop " + key + " " + value);
    }

    private static void injectFrida(String packageName, String scriptPath) {
        // frida-inject: attach to running process and run script
        // Requires frida-server running on device
        exec("frida -U -f " + packageName + " -l " + scriptPath + " --no-pause &");
        Log.i(TAG, "[Frida] Injected into " + packageName + " with " + scriptPath);
    }

    private static void pushScriptToDevice(String content, String devicePath) {
        if (content == null || content.isEmpty()) return;
        exec("mkdir -p " + FRIDA_HOOK_DEVICE_DIR);
        // Write via echo (simple approach for JS files)
        // For larger files use adb push from a temp local file
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

    private static String execOutput(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            p.waitFor();
            byte[] out = p.getInputStream().readAllBytes();
            return new String(out).trim();
        } catch (Exception e) {
            return "";
        }
    }
}
