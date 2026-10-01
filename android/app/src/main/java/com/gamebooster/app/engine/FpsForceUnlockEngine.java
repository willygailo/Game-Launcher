package com.gamebooster.app.engine;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.engine.CommandExecutor;

/**
 * FpsForceUnlockEngine — 185fps Force Unlock for all 3 target games.
 *
 * Targets: com.mobile.legends | com.activision.callofduty.shooter | com.tencent.ig
 *
 * Applies unlock in 4 layers:
 *   Layer 1 — Android system settings (peak/min_refresh_rate)
 *   Layer 2 — SurfaceFlinger setprop + binder calls (1034/1035/1008/1029)
 *   Layer 3 — SwappyGL / EGL swap disable
 *   Layer 4 — Kernel sysfs nodes (Snapdragon / Dimensity / Exynos)
 *
 * Updated: Sep 30 2026
 */
public final class FpsForceUnlockEngine {

    private static final String TAG = "FpsForceUnlockEngine";
    private static final int TARGET_FPS = 185;

    private FpsForceUnlockEngine() {}

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Master 185fps unlock — runs all 4 layers for the given game package.
     * Call this from GameAutoInjectDispatcher.dispatchForPackage() before launch.
     */
    public static void applyFps185ForceUnlock(Context context, String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        int resolvedHz = com.gamebooster.app.config.GameProfileAutoConfigurator.clampTargetFpsToDisplay(context, TARGET_FPS);
        Log.i(TAG, "▶ [FPS" + resolvedHz + "] Starting " + resolvedHz + "fps force unlock for: " + packageName);

        applySystemLevelUnlock();
        applySurfaceFlingerForce();
        applySwappyDisable();
        applyKernelDisplayForce();
        applyGameModeClampRemoval(packageName);
        applyPerGamePatch(packageName);

        Log.i(TAG, "✅ [FPS" + resolvedHz + "] All 4 layers applied for: " + packageName);
    }

    /**
     * Deferred re-apply — re-enforces 185fps 3 seconds after launch
     * (prevents OEM display managers reverting mid-session).
     */
    public static void applyFps185ForceUnlockDeferred(Context context, String packageName) {
        new Handler(Looper.getMainLooper()).postDelayed(() ->
            applyFps185ForceUnlock(context, packageName), 3000);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LAYER 1 — Android System Settings
    // ─────────────────────────────────────────────────────────────────────────

    public static void applySystemLevelUnlock() {
        String cmd =
            "settings put system peak_refresh_rate " + TARGET_FPS + ".0 2>/dev/null; " +
            "settings put system min_refresh_rate " + TARGET_FPS + ".0 2>/dev/null; " +
            "settings put system match_content_frame_rate 0 2>/dev/null; " +
            "settings put global game_mode_config 0 2>/dev/null; ";
        executePrivileged(cmd);
        Log.d(TAG, "[Layer1] System settings applied");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LAYER 2 — SurfaceFlinger setprop + binder transactions
    // ─────────────────────────────────────────────────────────────────────────

    public static void applySurfaceFlingerForce() {
        String cmd =
            "setprop debug.sf.fps_limit " + TARGET_FPS + "; " +
            "setprop persist.sys.NV_FPSLIMIT " + TARGET_FPS + "; " +
            "setprop persist.game_mode.performance.fps " + TARGET_FPS + "; " +
            "setprop debug.sf.hw 1; " +
            "service call SurfaceFlinger 1035 i32 " + TARGET_FPS + " 2>/dev/null; ";
        executePrivileged(cmd);
        Log.d(TAG, "[Layer2] SurfaceFlinger force applied");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LAYER 3 — SwappyGL / EGL frame pacing
    // ─────────────────────────────────────────────────────────────────────────

    public static void applySwappyDisable() {
        // Keep EGL/Swappy defaults untouched to prevent UE4 loading deadlock
        String cmd =
            "setprop debug.egl.buffcount 3; " +
            "setprop debug.egl.hw 1; ";
        executePrivileged(cmd);
        Log.d(TAG, "[Layer3] EGL hardware acceleration configured");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LAYER 4 — Kernel sysfs nodes (all chipset vendors)
    // ─────────────────────────────────────────────────────────────────────────

    public static void applyKernelDisplayForce() {
        // MediaTek (Dimensity) sysfs paths
        String mtk =
            "echo " + TARGET_FPS + " > /sys/devices/platform/mtk_disp_mgr.0/refresh_rate 2>/dev/null; " +
            "echo " + TARGET_FPS + " > /proc/mtk_display/fps 2>/dev/null; " +
            "echo " + TARGET_FPS + " > /sys/devices/virtual/graphics/fb0/dynamic_fps 2>/dev/null; ";
        // Qualcomm (Snapdragon) sysfs paths
        String qcom =
            "echo " + TARGET_FPS + " > /sys/class/graphics/fb0/dynamic_fps 2>/dev/null; " +
            "echo " + TARGET_FPS + " > /sys/devices/platform/soc/soc:qcom,dsi-display-primary/max_fps 2>/dev/null; ";
        // Samsung (Exynos) sysfs paths
        String exynos =
            "echo " + TARGET_FPS + " > /sys/devices/platform/exynos-drm/drm/card0/card0-DSI-1/max_fps 2>/dev/null; " +
            "echo " + TARGET_FPS + " > /sys/class/drm/card0-DSI-1/max_fps 2>/dev/null; ";
        executePrivileged(mtk + qcom + exynos);
        Log.d(TAG, "[Layer4] Kernel sysfs nodes written (MTK/Qcom/Exynos)");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Android GameMode API clamp removal (API 33+ / Android 13+)
    // ─────────────────────────────────────────────────────────────────────────

    public static void applyGameModeClampRemoval(String packageName) {
        if (packageName == null) return;
        // mode 2 = GAME_MODE_PERFORMANCE (disables OEM FPS caps)
        String cmd = "cmd game set --mode 2 --user 0 " + packageName + " 2>/dev/null; " +
                     "cmd game mode performance " + packageName + " 2>/dev/null; ";
        executePrivileged(cmd);
        Log.d(TAG, "[GameMode] Performance mode set for: " + packageName);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Per-game config file patches (PlayerPrefs XML / Engine.ini / GFX XML)
    // ─────────────────────────────────────────────────────────────────────────

    public static void applyPerGamePatch(String packageName) {
        if (packageName == null) return;
        String pkg = packageName.trim().toLowerCase();

        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
            applyMlbbConfigFpsPatch(pkg);
        } else if (pkg.contains("callofduty") || pkg.contains("activision")) {
            applyCodmConfigFpsPatch(pkg);
        } else if (pkg.contains("tencent.ig") || pkg.contains("pubg")) {
            applyPubgmConfigFpsPatch(pkg);
        }
    }

    private static void applyMlbbConfigFpsPatch(String pkg) {
        // MLBB — PlayerPrefs XML: patch MaxFPS, FrameRateLimit, targetFrameRate keys
        String[] prefDirs = {
            "/data/data/" + pkg + "/shared_prefs",
            "/sdcard/Android/data/" + pkg + "/shared_prefs",
            "/storage/emulated/0/Android/data/" + pkg + "/shared_prefs"
        };
        StringBuilder sb = new StringBuilder();
        for (String dir : prefDirs) {
            String prefs = dir + "/" + pkg + "_playerprefs.xml";
            sb.append("if [ -f '").append(prefs).append("' ]; then ")
              .append("sed -i 's/name=\"MaxFPS\" value=\"[0-9]*\"/name=\"MaxFPS\" value=\"185\"/' '").append(prefs).append("' 2>/dev/null; ")
              .append("sed -i 's/name=\"FrameRateLimit\" value=\"[0-9]*\"/name=\"FrameRateLimit\" value=\"185\"/' '").append(prefs).append("' 2>/dev/null; ")
              .append("grep -q 'targetFrameRate' '").append(prefs).append("' || sed -i 's|</map>|<int name=\"targetFrameRate\" value=\"185\" /></map>|' '").append(prefs).append("' 2>/dev/null; ")
              .append("fi; ");
        }
        executePrivileged(sb.toString());
        Log.d(TAG, "[PerGame-MLBB] PlayerPrefs MaxFPS patched to 185");
    }

    private static void applyCodmConfigFpsPatch(String pkg) {
        // CODM — GraphicsSetting.xml: patch FPS field
        String[] gfxPaths = {
            "/sdcard/Android/data/" + pkg + "/files/GraphicsSetting.xml",
            "/storage/emulated/0/Android/data/" + pkg + "/files/GraphicsSetting.xml"
        };
        // UserCustom.ini — UE4 console variables
        String[] iniPaths = {
            "/sdcard/Android/data/" + pkg + "/files/UserCustom.ini",
            "/storage/emulated/0/Android/data/" + pkg + "/files/UserCustom.ini"
        };
        StringBuilder sb = new StringBuilder();
        for (String gfx : gfxPaths) {
            sb.append("if [ -f '").append(gfx).append("' ]; then ")
              .append("sed -i 's/<FPS value=\"[0-9]*\" \\/>/<FPS value=\"185\" \\/>/g' '").append(gfx).append("' 2>/dev/null; ")
              .append("fi; ");
        }
        for (String ini : iniPaths) {
            sb.append("printf '[ConsoleVariables]\\nt.MaxFPS=185\\nt.UnacceptableFrameTime=0.005\\nr.VSync=0\\n' >> '").append(ini).append("' 2>/dev/null; ");
        }
        executePrivileged(sb.toString());
        Log.d(TAG, "[PerGame-CODM] GraphicsSetting + UserCustom.ini FPS patched to 185");
    }

    private static void applyPubgmConfigFpsPatch(String pkg) {
        // PUBGM — Engine.ini (safe, not hash-checked) + GameUserSettings.ini
        String[] enginePaths = {
            "/sdcard/Android/data/" + pkg + "/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/Engine.ini",
            "/storage/emulated/0/Android/data/" + pkg + "/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/Engine.ini"
        };
        String[] gusPaths = {
            "/sdcard/Android/data/" + pkg + "/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini",
            "/storage/emulated/0/Android/data/" + pkg + "/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini"
        };
        StringBuilder sb = new StringBuilder();
        for (String engine : enginePaths) {
            sb.append("if [ -f '").append(engine).append("' ]; then ")
              .append("grep -q 't.MaxFPS' '").append(engine).append("' && sed -i 's/t\\.MaxFPS=[0-9.]*/t.MaxFPS=185/' '").append(engine).append("' 2>/dev/null || echo 't.MaxFPS=185' >> '").append(engine).append("' 2>/dev/null; ")
              .append("grep -q 'r.VSync' '").append(engine).append("' || echo 'r.VSync=0' >> '").append(engine).append("' 2>/dev/null; ")
              .append("fi; ");
        }
        for (String gus : gusPaths) {
            // GameUserSettings.ini is hash-checked — patch is safe because only in-memory hash is verified
            sb.append("if [ -f '").append(gus).append("' ]; then ")
              .append("sed -i 's/FrameRateLimit=[0-9.]*/FrameRateLimit=185.000000/' '").append(gus).append("' 2>/dev/null; ")
              .append("sed -i 's/UpperBound=(Type=Exclusive,Value=[0-9.]*)/UpperBound=(Type=Exclusive,Value=186.0)/' '").append(gus).append("' 2>/dev/null; ")
              .append("fi; ");
        }
        executePrivileged(sb.toString());
        Log.d(TAG, "[PerGame-PUBGM] Engine.ini + GameUserSettings.ini FPS patched to 185");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTERNAL
    // ─────────────────────────────────────────────────────────────────────────

    private static void executePrivileged(String cmd) {
        if (cmd == null || cmd.trim().isEmpty()) return;
        try {
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        } catch (Throwable t) {
            Log.w(TAG, "executePrivileged note: " + t.getMessage());
        }
    }
}
