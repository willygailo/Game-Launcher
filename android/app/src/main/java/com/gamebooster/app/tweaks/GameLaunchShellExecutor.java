package com.gamebooster.app.tweaks;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.config.GameProfilePreferences;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * GameLaunchShellExecutor — Auto Shell Apply on Game Launch.
 *
 * Every time a game is launched this class:
 *  1. Fires a pre-built base performance script (CPU governor + GPU turbo + thermal bypass)
 *  2. Batch-applies all enabled TweakManagerRepository tweaks (1 Shizuku process spawn)
 *  3. Runs a per-game asset shell script from assets/shell/<packagename>.sh if it exists
 *  4. Enforces the hardware-adaptive Hz target for this game session
 *  5. Clears VM caches for maximum RAM headroom before the first frame
 *
 * Called from GameManagerLauncher.postLaunchPipeline — runs 100% in background,
 * game startActivity() is NEVER blocked.
 */
public final class GameLaunchShellExecutor {

    private static final String TAG = "GameLaunchShell";
    private static final String SHELL_ASSET_DIR = "shell";

    private GameLaunchShellExecutor() {}

    // ─────────────────────────────────────────────────────────────────────────
    // PRIMARY ENTRY POINT — Called by GameManagerLauncher
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Auto-applies the full shell pipeline for the given game package.
     * Runs asynchronously — never blocks the calling thread.
     *
     * @param ctx         Application context
     * @param packageName Target game package name
     * @param targetHz    Per-game Hz from GameProfilePreferences (0 = auto-detect)
     * @param onDone      Optional callback fired on the calling thread when complete
     */
    public static void applyForGameLaunch(Context ctx, String packageName, int targetHz, Runnable onDone) {
        if (ctx == null || packageName == null || packageName.trim().isEmpty()) return;
        final Context appCtx = ctx.getApplicationContext();
        final String pkg = packageName.trim().toLowerCase();

        // Resolve target Hz — hardware adaptive clamp
        final int resolvedHz = (targetHz > 0)
                ? GameProfileAutoConfigurator.clampTargetFpsToDisplay(appCtx, targetHz)
                : GameProfileAutoConfigurator.clampTargetFpsToDisplay(
                        appCtx, GameProfilePreferences.getTargetHz(appCtx, pkg));

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                long start = System.currentTimeMillis();
                Log.i(TAG, "🔥 [GameLaunchShell] Starting auto shell pipeline for " + pkg + " @ " + resolvedHz + "Hz");

                // PHASE 1 — Base performance burst (always fires)
                applyBasePerformanceScript(appCtx, resolvedHz);

                // PHASE 2 — Batch all enabled tweaks from TweakManagerRepository
                applyEnabledTweaksBatch(appCtx, resolvedHz);

                // PHASE 3 — Per-game asset shell script
                applyPerGameAssetScript(appCtx, pkg, resolvedHz);

                // PHASE 4 — Drop VM caches for max RAM headroom
                exec("echo 3 > /proc/sys/vm/drop_caches 2>/dev/null");

                long ms = System.currentTimeMillis() - start;
                Log.i(TAG, "✅ [GameLaunchShell] Pipeline complete for " + pkg + " in " + ms + "ms");

            } catch (Throwable t) {
                Log.e(TAG, "GameLaunchShell pipeline error for " + pkg + ": " + t.getMessage(), t);
            } finally {
                if (onDone != null) {
                    try { onDone.run(); } catch (Throwable ignored) {}
                }
            }
        });
    }

    /**
     * Watchdog re-apply — called at T+10s and T+30s by LobbyInjectionEngine.
     * Re-asserts Hz + GPU + thermal in case OEM daemons reverted settings.
     */
    public static void reApplyWatchdog(Context ctx, String packageName, int targetHz) {
        if (ctx == null || packageName == null) return;
        final Context appCtx = ctx.getApplicationContext();
        final String pkg = packageName.trim().toLowerCase();
        final int resolvedHz = GameProfileAutoConfigurator.clampTargetFpsToDisplay(appCtx, targetHz);

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                Log.i(TAG, "⚙️ [Watchdog] Re-asserting shell tweaks for " + pkg + " @ " + resolvedHz + "Hz");
                applyBasePerformanceScript(appCtx, resolvedHz);
                applyEnabledTweaksBatch(appCtx, resolvedHz);
                Log.i(TAG, "✅ [Watchdog] Re-apply complete for " + pkg);
            } catch (Throwable t) {
                Log.w(TAG, "Watchdog re-apply warning: " + t.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PHASE 1 — BASE PERFORMANCE SCRIPT
    // ─────────────────────────────────────────────────────────────────────────

    private static void applyBasePerformanceScript(Context ctx, int hz) {
        // Try asset script first
        String assetScript = readAssetScript(ctx, SHELL_ASSET_DIR + "/base_perf.sh");
        if (assetScript != null && !assetScript.isEmpty()) {
            String resolved = assetScript.replace("{TARGET_HZ}", String.valueOf(hz));
            exec(resolved);
            Log.i(TAG, "[Phase1] base_perf.sh applied from assets @ " + hz + "Hz");
            return;
        }

        // Fallback — inline base commands if asset missing
        StringBuilder sb = new StringBuilder();

        // CPU — performance governor on all policies
        sb.append("for p in /sys/devices/system/cpu/cpufreq/policy*; do")
          .append(" echo performance > \"$p/scaling_governor\" 2>/dev/null;")
          .append(" done; ");

        // CPU cpuset — all cores to top-app
        sb.append("echo 0-7 > /dev/cpuset/top-app/cpus 2>/dev/null; ");
        sb.append("echo 1000 > /dev/cpuset/top-app/uclamp.min 2>/dev/null; ");

        // GPU — Adreno turbo
        sb.append("setprop debug.adreno.turbo 1; ");
        sb.append("setprop debug.adreno.perf_level 0; ");
        sb.append("setprop vendor.perf.gestureFlingBoost 1; ");

        // GPU — Mali boost
        sb.append("setprop debug.mali.sched.priority -20; ");
        sb.append("setprop debug.mali.force_gpu_boost 1; ");

        // GPU power mode max
        sb.append("setprop vendor.gpu.power_mode 1; ");
        sb.append("setprop debug.gpu.performance 1; ");

        // Thermal bypass — disable zones & stop daemon
        sb.append("for z in /sys/class/thermal/thermal_zone*; do")
          .append(" echo disabled > \"$z/mode\" 2>/dev/null;")
          .append(" done; ");
        sb.append("stop thermal-engine 2>/dev/null; ");
        sb.append("stop thermald 2>/dev/null; ");
        sb.append("setprop persist.sys.thermal.ignore 1; ");

        // Hz enforcement — hardware adaptive
        sb.append("settings put system peak_refresh_rate ").append(hz).append(".0; ");
        sb.append("settings put system min_refresh_rate ").append(hz).append(".0; ");
        sb.append("settings put system match_content_frame_rate 0; ");
        sb.append("setprop debug.sf.fps_limit ").append(hz).append("; ");
        sb.append("setprop persist.sys.NV_FPSLIMIT ").append(hz).append("; ");
        sb.append("service call SurfaceFlinger 1035 i32 ").append(hz).append(" 2>/dev/null; ");

        // SurfaceFlinger HW composition
        sb.append("setprop debug.sf.hw 1; ");
        sb.append("setprop debug.sf.disable_hwc_vds 1; ");

        // Touch precision
        sb.append("setprop view.touch_slop 0; ");
        sb.append("setprop persist.sys.touch.report_rate 1000; ");
        sb.append("setprop persist.vendor.touch.sampling_rate 1000; ");

        // Network BBR
        sb.append("sysctl -w net.ipv4.tcp_congestion_control=bbr 2>/dev/null; ");
        sb.append("sysctl -w net.ipv4.tcp_quickack=1 2>/dev/null; ");

        exec(sb.toString());
        Log.i(TAG, "[Phase1] Inline base perf applied @ " + hz + "Hz");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PHASE 2 — BATCH ALL ENABLED TWEAKS
    // ─────────────────────────────────────────────────────────────────────────

    private static void applyEnabledTweaksBatch(Context ctx, int hz) {
        List<TweakItem> enabledTweaks = TweakManagerRepository.getAllEnabledTweaks(ctx);
        if (enabledTweaks == null || enabledTweaks.isEmpty()) {
            Log.i(TAG, "[Phase2] No enabled tweaks to batch-apply");
            return;
        }

        // Build one massive batched command — single Shizuku process spawn
        StringBuilder batch = new StringBuilder();
        for (TweakItem item : enabledTweaks) {
            String cmd = item.getApplyCommand();
            if (cmd == null || cmd.trim().isEmpty()) continue;
            // Dynamic Hz replacement
            cmd = cmd.replace("{TARGET_HZ}", String.valueOf(hz));
            batch.append(cmd);
            if (!cmd.endsWith(";") && !cmd.endsWith("\n")) {
                batch.append("; ");
            } else {
                batch.append(" ");
            }
        }

        if (batch.length() > 0) {
            exec(batch.toString());
            Log.i(TAG, "[Phase2] Batch applied " + enabledTweaks.size() + " enabled tweaks");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PHASE 3 — PER-GAME ASSET SHELL SCRIPT
    // ─────────────────────────────────────────────────────────────────────────

    private static void applyPerGameAssetScript(Context ctx, String pkg, int hz) {
        // Try exact package match first: e.g. shell/com.mobile.legends.sh
        String exactScript = readAssetScript(ctx, SHELL_ASSET_DIR + "/" + pkg + ".sh");
        if (exactScript != null && !exactScript.isEmpty()) {
            exec(exactScript.replace("{TARGET_HZ}", String.valueOf(hz)));
            Log.i(TAG, "[Phase3] Per-game script applied: " + pkg + ".sh");
            return;
        }

        // Try prefix match for regional packages (e.g. com.mobile.legends → mlbb family)
        String familyScript = resolveGameFamilyScript(ctx, pkg, hz);
        if (familyScript != null) {
            exec(familyScript.replace("{TARGET_HZ}", String.valueOf(hz)));
            Log.i(TAG, "[Phase3] Game family script applied for " + pkg);
        }
    }

    private static String resolveGameFamilyScript(Context ctx, String pkg, int hz) {
        String[][] families = {
            {"com.mobile.legends", "mobilelegends", "com.vng.mlbbvn"},
            {"com.tencent.ig", "com.pubg", "com.vng.pubgmobile", "com.krafton.bgmi"},
            {"com.activision.callofduty", "com.garena.game.codm", "com.vng.codmvn"},
            {"com.dts.freefire", "com.dts.freefiremax"},
            {"com.levelinfinite.sgame", "com.tencent.tmgp.sgame"},
            {"com.riotgames.league.wildrift"},
        };
        String[] familyScripts = {
            "com.mobile.legends.sh",
            "com.tencent.ig.sh",
            "com.activision.callofduty.shooter.sh",
            "com.dts.freefireth.sh",
            "com.levelinfinite.sgameGlobal.sh",
            "com.riotgames.league.wildrift.sh",
        };

        for (int i = 0; i < families.length; i++) {
            for (String prefix : families[i]) {
                if (pkg.contains(prefix.toLowerCase()) || pkg.startsWith(prefix.toLowerCase())) {
                    String script = readAssetScript(ctx, SHELL_ASSET_DIR + "/" + familyScripts[i]);
                    if (script != null) return script;
                }
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SHELL EXECUTION — Tiered: Shizuku → su → unprivileged
    // ─────────────────────────────────────────────────────────────────────────

    private static void exec(String command) {
        if (command == null || command.trim().isEmpty()) return;
        try {
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(command);
                return;
            }
        } catch (Throwable ignored) {}

        try {
            if (com.gamebooster.app.engine.ShellExecutor.isRootSuAvailable()) {
                com.gamebooster.app.engine.ShellExecutor.executeSuCommand(command);
                return;
            }
        } catch (Throwable ignored) {}

        // Best-effort unprivileged (works for non-privileged settings only)
        try {
            com.gamebooster.app.engine.ShellExecutor.executeCommand(command, false);
        } catch (Throwable ignored) {}
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ASSET READER
    // ─────────────────────────────────────────────────────────────────────────

    private static String readAssetScript(Context ctx, String assetPath) {
        if (ctx == null || assetPath == null) return null;
        try {
            AssetManager am = ctx.getAssets();
            InputStream is = am.open(assetPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith("#") && !line.trim().isEmpty()) {
                    sb.append(line).append("; ");
                }
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            // File simply doesn't exist — not an error
            return null;
        } catch (Throwable t) {
            Log.w(TAG, "Asset read error for " + assetPath + ": " + t.getMessage());
            return null;
        }
    }
}
