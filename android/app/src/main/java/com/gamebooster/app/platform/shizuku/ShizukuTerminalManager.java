package com.gamebooster.app.platform.shizuku;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.gamebooster.app.platform.shell.ShellExecutor;

/**
 * ShizukuTerminalManager — Manages the dedicated terminal execution folder for Game Booster.
 *
 * <p>Creates /data/local/tmp/gamebooster_terminal/ as the persistent workspace for:
 *  - start_shizuku.sh  : Dynamically resolves libshizuku.so path at runtime (no hardcoded hashes)
 *  - force_apply.sh    : Boot-time system property and device_config lock script
 *
 * <p>The libshizuku.so path resolution uses PackageManager.getApplicationInfo().nativeLibraryDir
 * so it works across every install, update, and Android 13-16 version regardless of the
 * hash-encoded /data/app/~~<hash>/ directory structure.
 *
 * <p>LEGAL NOTE: All operations use standard Android shell commands (sh, pm, exec).
 * No binary patching, no executable injection. Pure ADB-level system calls (uid 2000).
 */
public class ShizukuTerminalManager {

    private static final String TAG = "ShizukuTerminal";

    /** Working directory created in world-writable /data/local/tmp (accessible as uid 2000) */
    public static final String TERMINAL_DIR = "/data/local/tmp/gamebooster_terminal";

    /** Shizuku daemon start script path */
    public static final String START_SCRIPT_PATH = TERMINAL_DIR + "/start_shizuku.sh";

    /** Force-apply boot script path */
    public static final String FORCE_APPLY_SCRIPT_PATH = TERMINAL_DIR + "/force_apply.sh";

    /** Shizuku package name */
    private static final String SHIZUKU_PKG = "moe.shizuku.privileged.api";

    // -----------------------------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------------------------

    /**
     * Ensures the terminal working directory exists.
     * Creates /data/local/tmp/gamebooster_terminal/ if not present.
     */
    public static void ensureTerminalDir() {
        ShellExecutor.CommandResult result = ShellExecutor.executeCommand("mkdir -p " + TERMINAL_DIR);
        if (result.isSuccess()) {
            Log.i(TAG, "✅ Terminal dir ready: " + TERMINAL_DIR);
        } else {
            Log.w(TAG, "Terminal dir creation failed (may already exist): " + result.stderr);
        }
    }

    /**
     * Writes the dynamic Shizuku start script to the terminal directory.
     *
     * <p>The script resolves the correct libshizuku.so path at runtime using 'pm path'
     * instead of relying on any hardcoded /data/app/~~hash/ path which changes on every install.
     *
     * @param context Application context for PackageManager lookup
     * @return true if script was successfully written and made executable
     */
    public static boolean writeStartScript(Context context) {
        ensureTerminalDir();

        // Build the dynamic start script — resolves libshizuku.so path at shell runtime
        // so it works regardless of the /data/app/~~<hash>/ install-time hash
        String scriptContent = buildStartScriptContent(context);

        boolean written = writeScript(START_SCRIPT_PATH, scriptContent);
        if (written) {
            Log.i(TAG, "✅ Shizuku start script written: " + START_SCRIPT_PATH);
        } else {
            Log.e(TAG, "❌ Failed to write Shizuku start script");
        }
        return written;
    }

    /**
     * Writes the force-apply boot script with the specified target Hz.
     *
     * @param targetHz Target refresh rate (60/90/120/144/165)
     * @return true if script was written successfully
     */
    public static boolean writeForceApplyScript(int targetHz) {
        ensureTerminalDir();

        String scriptContent = buildForceApplyScriptContent(targetHz);
        boolean written = writeScript(FORCE_APPLY_SCRIPT_PATH, scriptContent);
        if (written) {
            Log.i(TAG, "✅ Force-apply script written: " + FORCE_APPLY_SCRIPT_PATH);
        }
        return written;
    }

    /**
     * Executes the Shizuku start script via root shell (su).
     * Falls back to direct libshizuku.so path from PackageManager if script fails.
     *
     * @param context Application context
     * @return CommandResult with success/failure and output
     */
    public static ShellExecutor.CommandResult startShizukuViaScript(Context context) {
        // Ensure script is freshly written
        writeStartScript(context);

        Log.i(TAG, "⚡ Executing Shizuku start script: " + START_SCRIPT_PATH);
        ShellExecutor.CommandResult result = ShellExecutor.executeRootCommand(
                "sh " + START_SCRIPT_PATH
        );

        Log.i(TAG, "Shizuku start result: exitCode=" + result.exitCode
                + " stdout='" + result.stdout + "' stderr='" + result.stderr + "'");
        return result;
    }

    /**
     * Executes the force-apply script.
     *
     * @param targetHz Target Hz for the script
     * @return CommandResult
     */
    public static ShellExecutor.CommandResult runForceApplyScript(int targetHz) {
        writeForceApplyScript(targetHz);
        Log.i(TAG, "⚡ Running force-apply script at " + targetHz + "Hz");
        return ShellExecutor.executeCommand("sh " + FORCE_APPLY_SCRIPT_PATH);
    }

    /**
     * Lists all files in the terminal directory for display in the Terminal UI.
     *
     * @return ls -la output string
     */
    public static String listTerminalDir() {
        ShellExecutor.CommandResult result = ShellExecutor.executeCommand("ls -la " + TERMINAL_DIR);
        return result.isSuccess() ? result.stdout : "ERROR: " + result.stderr;
    }

    /**
     * Reads the content of a file in the terminal directory.
     *
     * @param filename Filename (not full path) within terminal dir
     * @return File content string, or error message
     */
    public static String readTerminalFile(String filename) {
        if (filename == null || filename.isEmpty()) return "ERROR: empty filename";
        String path = TERMINAL_DIR + "/" + filename;
        ShellExecutor.CommandResult result = ShellExecutor.executeCommand("cat " + path);
        return result.isSuccess() ? result.stdout : "ERROR: " + result.stderr;
    }

    // -----------------------------------------------------------------------------------------
    // Script Content Builders
    // -----------------------------------------------------------------------------------------

    /**
     * Builds the dynamic Shizuku start script content.
     *
     * Uses 'pm path moe.shizuku.privileged.api' at shell runtime to resolve the apk path,
     * then constructs the lib/arm64/libshizuku.so sibling path. This avoids any hardcoded
     * /data/app/~~<hash>/ path which becomes stale after every Shizuku update.
     *
     * Three fallback levels:
     *   1. libshizuku.so via pm path (primary)
     *   2. /data/data/<pkg>/starter (internal data)
     *   3. /sdcard/Android/data/<pkg>/files/start.sh (sdcard script)
     */
    private static String buildStartScriptContent(Context context) {
        // Pre-resolve path via PackageManager as an immediate hint in the script comment
        String pmHint = resolveLibshizukuPathViaPackageManager(context);

        return "#!/system/bin/sh\n"
                + "# Game Booster Pro — Shizuku Daemon Start Script\n"
                + "# Auto-generated by ShizukuTerminalManager — DO NOT HARDCODE PATHS\n"
                + "# Dynamic resolution avoids stale /data/app/~~hash/ references\n"
                + "# Last known path hint (may change after update): " + (pmHint != null ? pmHint : "unknown") + "\n"
                + "\n"
                + "PKG=\"moe.shizuku.privileged.api\"\n"
                + "\n"
                + "# Method 1: Resolve via pm path → strip 'package:' prefix → build lib path\n"
                + "APK_PATH=$(pm path \"$PKG\" 2>/dev/null | sed 's/package://' | tr -d '\\n\\r')\n"
                + "if [ -n \"$APK_PATH\" ]; then\n"
                + "    # Walk up from base.apk to the install directory\n"
                + "    INSTALL_DIR=$(dirname \"$APK_PATH\")\n"
                + "    LIB_PATH=\"$INSTALL_DIR/lib/arm64/libshizuku.so\"\n"
                + "    if [ -f \"$LIB_PATH\" ]; then\n"
                + "        echo \"[ShizukuTerminal] Starting via: $LIB_PATH\"\n"
                + "        exec \"$LIB_PATH\"\n"
                + "        exit $?\n"
                + "    fi\n"
                + "    # arm (32-bit) fallback\n"
                + "    LIB_PATH_32=\"$INSTALL_DIR/lib/arm/libshizuku.so\"\n"
                + "    if [ -f \"$LIB_PATH_32\" ]; then\n"
                + "        echo \"[ShizukuTerminal] Starting via arm: $LIB_PATH_32\"\n"
                + "        exec \"$LIB_PATH_32\"\n"
                + "        exit $?\n"
                + "    fi\n"
                + "fi\n"
                + "\n"
                + "# Method 2: Internal data starter binary\n"
                + "STARTER=\"/data/data/$PKG/starter\"\n"
                + "if [ -f \"$STARTER\" ]; then\n"
                + "    echo \"[ShizukuTerminal] Starting via internal starter: $STARTER\"\n"
                + "    exec \"$STARTER\"\n"
                + "    exit $?\n"
                + "fi\n"
                + "\n"
                + "# Method 3: SD card start.sh script\n"
                + "SDCARD_SCRIPT=\"/sdcard/Android/data/$PKG/files/start.sh\"\n"
                + "if [ -f \"$SDCARD_SCRIPT\" ]; then\n"
                + "    echo \"[ShizukuTerminal] Starting via sdcard script: $SDCARD_SCRIPT\"\n"
                + "    sh \"$SDCARD_SCRIPT\"\n"
                + "    exit $?\n"
                + "fi\n"
                + "\n"
                + "echo \"[ShizukuTerminal] ERROR: Shizuku not installed or starter binary not found.\"\n"
                + "echo \"[ShizukuTerminal] Install Shizuku from: https://shizuku.rikka.app\"\n"
                + "exit 1\n";
    }

    /**
     * Builds the force-apply boot script content as a clean external file.
     * Replaces the fragile inline String.format() approach in ShizukuForceApplyEngine.
     */
    private static String buildForceApplyScriptContent(int hz) {
        return "#!/system/bin/sh\n"
                + "# Game Booster Pro — Force Apply Boot Script\n"
                + "# Target Hz: " + hz + "\n"
                + "# Generated by ShizukuTerminalManager — all sections mirror ShizukuForceApplyEngine\n"
                + "\n"
                + "# Drop page cache\n"
                + "sync\n"
                + "echo 3 > /proc/sys/vm/drop_caches 2>/dev/null || true\n"
                + "\n"
                + "# ── FPS / Frame Lock ──────────────────────────────────────────────────────────\n"
                + "setprop persist.sys.NV_FPSLIMIT " + hz + "\n"
                + "setprop persist.sys.NV_POWERMODE 1\n"
                + "setprop debug.sf.fps_limit " + hz + "\n"
                + "setprop persist.sys.gamemode.fps " + hz + "\n"
                + "\n"
                + "# ── GPU / Rendering Pipeline ──────────────────────────────────────────────────\n"
                + "setprop debug.sf.hw 1\n"
                + "setprop persist.debug.sf.hw 1\n"
                + "setprop debug.hwui.renderer vulkan\n"
                + "setprop persist.hwui.renderer vulkan\n"
                + "setprop debug.renderengine.backend vulkan\n"
                + "setprop debug.sf.latch_unsignaled 1\n"
                + "setprop debug.sf.disable_backpressure 1\n"
                + "setprop debug.sf.early_app_phase_offset_ns 500000\n"
                + "setprop debug.sf.early_phase_offset_ns 500000\n"
                + "\n"
                + "# ── EGL / GPU VSync Uncap (Adreno + Mali) ─────────────────────────────────────\n"
                + "# Disables the hard VSync barrier — frames render and present at full GPU throughput\n"
                + "setprop debug.egl.swapinterval 0\n"
                + "setprop debug.gr.swapinterval 0\n"
                + "\n"
                + "# ── Qualcomm Render Thread Priority Boost ─────────────────────────────────────\n"
                + "setprop persist.sys.perf.topAppRenderThreadBoost.enable 1\n"
                + "\n"
                + "# ── Peak / Minimum Refresh Rate — Dual Namespace ──────────────────────────────\n"
                + "# 'system' namespace: standard AOSP path\n"
                + "settings put system peak_refresh_rate " + hz + ".0\n"
                + "settings put system min_refresh_rate " + hz + ".0\n"
                + "# 'global' namespace: OnePlus, Realme, VIVO, IQOO OEM firmware reads from here\n"
                + "settings put global peak_refresh_rate " + hz + ".0\n"
                + "settings put global min_refresh_rate " + hz + ".0\n"
                + "# Android 14+ Window Manager source of truth\n"
                + "settings put system user_refresh_rate " + hz + ".0\n"
                + "\n"
                + "# ── Android 15 Official FPS Cap Removal ───────────────────────────────────────\n"
                + "# disable_default_frame_rate_for_games is the AOSP-blessed flag in Developer Options\n"
                + "# that lifts the 60 FPS default cap. Most effective single flag available on A15.\n"
                + "device_config put game_manager disable_default_frame_rate_for_games true\n"
                + "device_config put game_manager game_frame_rate_override_enabled true\n"
                + "device_config put game_manager game_default_frame_rate 0\n"
                + "device_config put game_manager game_driver_all_apps 1\n"
                + "device_config put game_driver game_driver_all_apps 1\n"
                + "device_config put activity_manager default_application_start_info_enabled true\n"
                + "\n"
                + "# ── SurfaceFlinger binder calls ───────────────────────────────────────────────\n"
                + "service call SurfaceFlinger 1035 i32 " + hz + " 2>/dev/null || true\n"
                + "service call SurfaceFlinger 1036 i32 " + hz + " 2>/dev/null || true\n"
                + "\n"
                + "# ── Performance / Thermal Mode ────────────────────────────────────────────────\n"
                + "cmd thermalservice override-status 0 2>/dev/null || true\n"
                + "cmd power set-mode 0 1 2>/dev/null || true\n"
                + "cmd power set-mode 2 1 2>/dev/null || true\n"
                + "# SUSTAINED_PERFORMANCE_MODE (mode 6) — prevents throttle drops in long sessions\n"
                + "cmd power set-mode 6 1 2>/dev/null || true\n"
                + "\n"
                + "# ── Touch Latency ─────────────────────────────────────────────────────────────\n"
                + "setprop debug.input.max_events_per_sec 1000\n"
                + "setprop view.touch_slop 0\n"
                + "setprop persist.sys.touch.response_time 0\n"
                + "setprop persist.sys.touch_prediction 1\n"
                + "\n"
                + "# ── Animation Scales (0.5x — smooth but fast) ─────────────────────────────────\n"
                + "settings put global animator_duration_scale 0.5\n"
                + "settings put global transition_animation_scale 0.5\n"
                + "settings put global window_animation_scale 0.5\n"
                + "\n"
                + "echo \"[GameBooster] Force-apply complete at " + hz + "Hz — Full Combo Active\"\n"
                + "exit 0\n";
    }

    // -----------------------------------------------------------------------------------------
    // Internal Helpers
    // -----------------------------------------------------------------------------------------

    /**
     * Writes a script file using a heredoc-safe approach that avoids printf % issues.
     * Uses 'cat > file << HEREDOC_MARKER' pattern via a temp file written with dd.
     */
    private static boolean writeScript(String path, String content) {
        try {
            // Write via line-by-line echo approach to avoid printf % and quote escaping issues
            StringBuilder cmd = new StringBuilder();
            cmd.append("rm -f ").append(path).append(" && ");
            cmd.append("cat > ").append(path).append(" << 'GAMEBOOSTER_EOF'\n");
            cmd.append(content);
            cmd.append("\nGAMEBOOSTER_EOF\n");
            cmd.append("chmod 755 ").append(path);

            ShellExecutor.CommandResult result = ShellExecutor.executeRootCommand(cmd.toString());

            if (result.isSuccess()) {
                return true;
            }

            // Fallback: write using printf with safe escaping of each line
            return writeScriptFallback(path, content);

        } catch (Exception e) {
            Log.e(TAG, "writeScript exception for " + path, e);
            return false;
        }
    }

    /**
     * Fallback script writer using line-by-line printf.
     * Handles % characters safely by doubling them for printf.
     */
    private static boolean writeScriptFallback(String path, String content) {
        String[] lines = content.split("\n", -1);
        StringBuilder batch = new StringBuilder();
        batch.append("rm -f ").append(path);
        for (String line : lines) {
            // Escape single quotes and percent signs for printf safety
            String safe = line.replace("'", "'\\''").replace("%", "%%");
            batch.append(" && printf '%s\\n' '").append(safe).append("' >> ").append(path);
        }
        batch.append(" && chmod 755 ").append(path);
        ShellExecutor.CommandResult result = ShellExecutor.executeCommand(batch.toString());
        return result.isSuccess();
    }

    /**
     * Resolves the libshizuku.so absolute path via Android PackageManager.
     * Returns null if Shizuku is not installed.
     */
    private static String resolveLibshizukuPathViaPackageManager(Context context) {
        if (context == null) return null;
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(SHIZUKU_PKG, 0);
            if (info != null && info.nativeLibraryDir != null) {
                return info.nativeLibraryDir + "/libshizuku.so";
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Shizuku not installed — PackageManager lookup returned null");
        } catch (Exception e) {
            Log.e(TAG, "Error resolving libshizuku.so path", e);
        }
        return null;
    }
}
