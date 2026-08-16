package com.gamebooster.app.booster;

import android.os.Build;
import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * MaxHzForceChannel — Dedicated Shizuku-direct engine for forcing 120Hz / 144Hz / 165Hz / 185Hz.
 *
 * Unlike HzFpsChannel.setRefreshRate() which gates on Display.getSupportedModes(),
 * this class fires ALL known commands unconditionally via ShizukuExecutor —
 * bypassing CommandExecutor / UserServiceConnector for zero-delay, zero-fallback execution.
 *
 * Command Layers:
 *   Layer 1 — AOSP standard: system + global namespace settings
 *   Layer 2 — Android Game Mode API + cmd window
 *   Layer 3 — device_config game_overlay global policy
 *   Layer 4 — SurfaceFlinger direct binder (1035 + 1036)
 *   Layer 5 — setprop runtime overrides (NV_FPSLIMIT, fps_limit, swapinterval)
 *   Layer 6 — OEM/vendor-specific refresh rate keys (auto-detected per manufacturer: ASUS ROG 185Hz, RedMagic, Xiaomi, Samsung, etc.)
 */
public final class MaxHzForceChannel {

    private static final String TAG = "MaxHzForceChannel";

    // ── Result ────────────────────────────────────────────────────────────────────────

    public static final class ForceResult {
        public final boolean success;
        public final int appliedHz;
        public final int successCount;
        public final int failCount;
        public final int totalCommands;
        public final String message;

        private ForceResult(boolean success, int appliedHz,
                            int successCount, int failCount, int total, String message) {
            this.success = success;
            this.appliedHz = appliedHz;
            this.successCount = successCount;
            this.failCount = failCount;
            this.totalCommands = total;
            this.message = message;
        }

        /** Shizuku not available or permission denied. */
        public static ForceResult noShizuku(int hz) {
            return new ForceResult(false, hz, 0, 0, 0,
                    "Shizuku not available or permission not granted. "
                            + "Connect Shizuku to force " + hz + "Hz.");
        }

        /** Normal result after firing all commands. */
        public static ForceResult complete(int hz, int success, int fail, int total) {
            String status = fail == 0 ? " ✅" : " (" + fail + " failed)";
            String msg = "Force " + hz + "Hz — " + success + "/" + total
                    + " Shizuku commands OK" + status;
            return new ForceResult(success > 0, hz, success, fail, total, msg);
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────────────

    /**
     * Forces the display to {@code targetHz} via Shizuku — NO capability check, NO fallback.
     * Uses ShizukuExecutor directly to avoid UserService binding delays.
     *
     * @param targetHz Target refresh rate: 120, 144, 165, or 185
     * @return ForceResult with per-layer success tracking
     */
    public static ForceResult forceApply(int targetHz) {
        if (!ShizukuExecutor.hasShizukuPermission()) {
            Log.w(TAG, "forceApply(" + targetHz + "Hz): Shizuku not available, aborting.");
            return ForceResult.noShizuku(targetHz);
        }

        String hz  = String.valueOf(targetHz);
        String hzF = targetHz + ".0";
        int ok = 0, total = 0;

        Log.i(TAG, "══ MaxHzForceChannel.forceApply START (" + targetHz + "Hz) ══");

        // ── Layer 1: AOSP Standard Settings & Dynamic Refresh Defeat ─────────────────
        ok += run("settings put system peak_refresh_rate "  + hzF); total++;
        ok += run("settings put system min_refresh_rate "   + hzF); total++;
        ok += run("settings put system user_refresh_rate "  + hz);  total++;
        ok += run("settings put global peak_refresh_rate "  + hzF); total++;
        ok += run("settings put global min_refresh_rate "   + hzF); total++;
        ok += run("settings put system match_content_frame_rate 0"); total++;
        ok += run("settings put secure match_content_frame_rate_preference 0"); total++;

        // ── Layer 2: Android Game Mode API + Window Manager ───────────────────────────
        ok += run("cmd game mode performance global");                               total++;
        ok += run("cmd game set --fps " + targetHz + " global");                     total++;
        ok += run("cmd window set-app-refresh-rate global " + targetHz);             total++;

        // ── Layer 3: device_config game_overlay (per-global refresh rate policy) ─────
        ok += run("device_config put game_overlay global "
                + "mode=2,fps=" + targetHz + ":mode=3,fps=" + targetHz);             total++;

        // ── Layer 4: SurfaceFlinger Direct Binder ────────────────────────────────────
        ok += run("service call SurfaceFlinger 1035 i32 " + targetHz);               total++;
        ok += run("service call SurfaceFlinger 1036 i32 " + targetHz);               total++;

        // ── Layer 5: setprop Runtime Overrides & Latency Eliminators ────────────────
        ok += run("setprop debug.sf.fps_limit "                + hz);                total++;
        ok += run("setprop persist.sys.NV_FPSLIMIT "           + hz);                total++;
        ok += run("setprop persist.sys.NV_POWERMODE 1");                             total++;
        ok += run("setprop debug.gr.swapinterval 0");                                total++;
        ok += run("setprop debug.egl.swapinterval 0");                               total++;
        ok += run("setprop debug.sf.disable_backpressure 1");                        total++;
        ok += run("setprop debug.sf.early_phase_offset_ns 0");                       total++;
        ok += run("setprop debug.sf.early_app_phase_offset_ns 0");                   total++;
        ok += run("setprop persist.sys.game.fps "              + hz);                total++;
        ok += run("setprop persist.vendor.power.dfps.level "   + hz);                total++;
        ok += run("setprop ro.vendor.display.default_fps "     + hz);                total++;
        ok += run("setprop vendor.display.fps "                + hz);                total++;
        ok += run("setprop debug.hwui.fps_divisor 1");                               total++;

        // ── Layer 6: OEM / Vendor-Specific Keys (auto-detected) ──────────────────────
        String mfr    = Build.MANUFACTURER != null ? Build.MANUFACTURER.toLowerCase() : "";
        String brand  = Build.BRAND        != null ? Build.BRAND.toLowerCase()        : "";
        String id     = mfr + " " + brand;

        if (id.contains("xiaomi") || id.contains("redmi") || id.contains("poco")) {
            // Xiaomi / Redmi / POCO (MIUI / HyperOS)
            ok += run("settings put secure user_refresh_rate " + hz);                total++;
            ok += run("settings put global surface_flinger_peak_refresh_rate " + hz); total++;

        } else if (id.contains("samsung")) {
            // Samsung (OneUI) — mode 2 = Dynamic / High, mode 1 = Standard 60Hz
            ok += run("settings put secure refresh_rate_mode " + (targetHz >= 90 ? "2" : "1")); total++;
            ok += run("settings put system sec_display_fps " + hz);                  total++;

        } else if (id.contains("oneplus") || id.contains("oppo") || id.contains("realme")) {
            // OnePlus / Realme / Oppo (OxygenOS / ColorOS)
            ok += run("settings put global oneplus_screen_refresh_rate "
                    + (targetHz >= 90 ? "2" : "1"));                                total++;
            ok += run("settings put global realme_screen_refresh_rate " + hz);       total++;

        } else if (id.contains("asus")) {
            // ASUS ROG Phone / ZenFone
            ok += run("settings put system asus_option_display_refresh_rate " + hz); total++;
            ok += run("settings put system asus_hfr_mode 1");                        total++;

        } else if (id.contains("vivo") || id.contains("iqoo")) {
            // vivo / iQOO (Funtouch OS / Origin OS)
            ok += run("settings put system screen_refresh_rate " + hz);              total++;
            ok += run("settings put system iqoo_refresh_rate " + hz);                total++;

        } else if (id.contains("nubia") || id.contains("redmagic") || id.contains("zte")) {
            // RedMagic / Nubia / ZTE gaming devices
            ok += run("settings put system display_refresh_rate " + hz);             total++;
            ok += run("settings put system redmagic_refresh_rate " + hz);            total++;

        } else if (id.contains("infinix") || id.contains("tecno") || id.contains("itel")
                || id.contains("transsion")) {
            // Transsion Holdings (Infinix XOS / Tecno HiOS / itel)
            ok += run("settings put system screen_refresh_rate_mode "
                    + (targetHz >= 90 ? "1" : "0"));                                total++;
            ok += run("settings put system infinix_refresh_rate " + hz);             total++;

        } else if (id.contains("motorola") || id.contains("moto")) {
            // Motorola (MyUX)
            ok += run("settings put global peak_refresh_rate " + hz);               total++;
            ok += run("settings put system min_refresh_rate " + hz);                 total++;

        } else {
            // Generic / Google Pixel — reinforce AOSP settings
            ok += run("settings put system peak_refresh_rate " + hz);               total++;
            ok += run("settings put system user_refresh_rate " + hz);               total++;
        }

        int fail = total - ok;
        Log.i(TAG, "══ MaxHzForceChannel.forceApply DONE: "
                + ok + "/" + total + " OK, " + targetHz + "Hz ══");
        return ForceResult.complete(targetHz, ok, fail, total);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────────────

    /**
     * Fires a single Shizuku command directly via ShizukuExecutor (reflection path).
     * Returns 1 on success, 0 on failure/exception.
     */
    private static int run(String command) {
        try {
            String result = ShizukuExecutor.executeShizukuCommand(command);
            boolean success = result != null
                    && !result.trim().toLowerCase().startsWith("error")
                    && !result.trim().toLowerCase().contains("permission denial")
                    && !result.trim().toLowerCase().contains("not found");
            Log.d(TAG, (success ? "  ✓ " : "  ✗ ") + command
                    + (success ? "" : "  →  " + (result != null ? result.trim() : "null")));
            return success ? 1 : 0;
        } catch (Throwable t) {
            Log.w(TAG, "  ! EXC " + command + " — " + t.getMessage());
            return 0;
        }
    }
}
