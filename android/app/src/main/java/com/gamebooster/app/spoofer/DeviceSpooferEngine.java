package com.gamebooster.app.spoofer;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.List;
import java.util.Map;

/**
 * DeviceSpooferEngine — Full hardware identity spoofing for Android 11–16.
 *
 * Overrides ALL Android system property namespaces (product, vendor, system, odm,
 * product.product, system_ext) via Shizuku ADB Binder IPC (temporary full root).
 * Also spoofs hardware chipname, board, fingerprint, display ID, and GL renderer hint.
 *
 * Profiles are loaded from {@link SpoofProfileRegistry} which aggregates per-brand
 * files (Samsung, Realme, ASUS ROG, Xiaomi, OnePlus, OPPO, Vivo, Apple, Nubia, Black Shark).
 *
 * STRICT SHIZUKU-ONLY: No OS fallback. Returns false immediately if Shizuku is unavailable.
 */
public class DeviceSpooferEngine {

    private static final String TAG = "DeviceSpooferEngine";

    /** Currently active spoof profile ID, null if no spoof is applied. */
    private static String activeProfileId = null;

    // ─────────────────────────────────────────────────────────────────────────
    //  Profile access (delegates to registry)
    // ─────────────────────────────────────────────────────────────────────────

    public static Map<String, SpoofProfile> getAllProfiles() {
        return SpoofProfileRegistry.getAllProfiles();
    }

    public static Map<String, List<SpoofProfile>> getAllByBrand() {
        return SpoofProfileRegistry.getAllByBrand();
    }

    public static List<String> getBrandNames() {
        return SpoofProfileRegistry.getBrandNames();
    }

    public static SpoofProfile getProfileById(String id) {
        return SpoofProfileRegistry.getById(id);
    }

    public static List<SpoofProfile> getProfilesByBrand(String brand) {
        return SpoofProfileRegistry.getByBrand(brand);
    }

    public static String getActiveProfileId() {
        return activeProfileId;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Smart profile recommendation per game package
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the recommended spoof profile for a given game package name.
     * Picks the best-fit device to unlock the highest FPS/graphics tier.
     */
    public static SpoofProfile getRecommendedProfile(String packageName) {
        if (packageName == null) return SpoofProfileRegistry.getById("asus_rog8_pro");
        String pkg = packageName.toLowerCase();

        // MLBB / Wild Rift / Honor of Kings → ROG Phone 8 Pro (165 Hz extreme)
        if (pkg.contains("mobile.legends") || pkg.contains("wildrift") || pkg.contains("sgame")) {
            return SpoofProfileRegistry.getById("asus_rog8_pro");
        }
        // CODM / Blood Strike → Black Shark 5 Pro (max CODM graphics)
        if (pkg.contains("callofduty") || pkg.contains("codm") || pkg.contains("bloodstrike")) {
            return SpoofProfileRegistry.getById("black_shark_5_pro");
        }
        // PUBGM / BGMI / FreeFire → REDMAGIC 9 Pro (165 Hz extreme PUBG)
        if (pkg.contains("tencent.ig") || pkg.contains("pubg") || pkg.contains("imobile") || pkg.contains("freefire")) {
            return SpoofProfileRegistry.getById("redmagic_9_pro");
        }
        // Genshin / Honkai → Xiaomi 14 Ultra (ultra graphics)
        if (pkg.contains("genshin") || pkg.contains("hkrpg") || pkg.contains("honkai")) {
            return SpoofProfileRegistry.getById("xiaomi_14_ultra");
        }
        // Default → ROG Phone 8 Pro
        return SpoofProfileRegistry.getById("asus_rog8_pro");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Apply
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Applies the auto-recommended spoof profile for the given game package.
     */
    public static boolean applySpoofing(Context context, String packageName) {
        SpoofProfile profile = getRecommendedProfile(packageName);
        return applyProfile(context, profile, packageName);
    }

    /**
     * Applies a specific SpoofProfile — overrides ALL Android property namespaces
     * using resetprop + setprop via Shizuku (temporary full root).
     *
     * Covers:
     *   - 6 model/brand/manufacturer/device/name namespaces
     *   - Hardware: hw, chipname, board, platform, soc
     *   - Build: product, display ID, fingerprint
     *   - Per-package game mode boost
     */
    public static boolean applyProfile(Context context, SpoofProfile profile, String packageName) {
        if (profile == null) {
            Log.e(TAG, "Cannot apply null spoof profile.");
            return false;
        }
        if (!ShizukuExecutor.isShizukuAvailable()) {
            Log.e(TAG, "Shizuku ADB unavailable. STRICT MODE: hardware spoofing cannot proceed.");
            return false;
        }

        try {
            Log.d(TAG, "▶ Applying spoof profile: " + profile.displayName + " [" + profile.id + "]");

            // ── Model — 6 namespaces ──
            exec("resetprop ro.product.model " + profile.model);
            exec("resetprop ro.product.vendor.model " + profile.model);
            exec("resetprop ro.product.system.model " + profile.model);
            exec("resetprop ro.product.odm.model " + profile.model);
            exec("resetprop ro.product.product.model " + profile.model);
            exec("resetprop ro.product.system_ext.model " + profile.model);

            // ── Brand — 6 namespaces ──
            exec("resetprop ro.product.brand " + profile.brand);
            exec("resetprop ro.product.vendor.brand " + profile.brand);
            exec("resetprop ro.product.system.brand " + profile.brand);
            exec("resetprop ro.product.odm.brand " + profile.brand);
            exec("resetprop ro.product.product.brand " + profile.brand);
            exec("resetprop ro.product.system_ext.brand " + profile.brand);

            // ── Manufacturer — 6 namespaces ──
            exec("resetprop ro.product.manufacturer " + profile.manufacturer);
            exec("resetprop ro.product.vendor.manufacturer " + profile.manufacturer);
            exec("resetprop ro.product.system.manufacturer " + profile.manufacturer);
            exec("resetprop ro.product.odm.manufacturer " + profile.manufacturer);
            exec("resetprop ro.product.product.manufacturer " + profile.manufacturer);
            exec("resetprop ro.product.system_ext.manufacturer " + profile.manufacturer);

            // ── Device — 6 namespaces ──
            exec("resetprop ro.product.device " + profile.device);
            exec("resetprop ro.product.vendor.device " + profile.device);
            exec("resetprop ro.product.system.device " + profile.device);
            exec("resetprop ro.product.odm.device " + profile.device);
            exec("resetprop ro.product.product.device " + profile.device);
            exec("resetprop ro.product.system_ext.device " + profile.device);

            // ── Product Name — 6 namespaces ──
            exec("resetprop ro.product.name " + profile.productName);
            exec("resetprop ro.product.vendor.name " + profile.productName);
            exec("resetprop ro.product.system.name " + profile.productName);
            exec("resetprop ro.product.odm.name " + profile.productName);
            exec("resetprop ro.product.product.name " + profile.productName);
            exec("resetprop ro.product.system_ext.name " + profile.productName);

            // ── Hardware / SoC Identity ──
            exec("resetprop ro.hardware " + profile.hardware);
            exec("resetprop ro.hardware.chipname " + profile.chipname);
            exec("resetprop ro.board.platform " + profile.platform);
            exec("resetprop ro.soc.model " + profile.socModel);
            exec("resetprop ro.product.board " + profile.board);

            // ── Build Identity ──
            exec("resetprop ro.build.product " + profile.buildProduct);
            exec("resetprop ro.build.display.id " + profile.displayId);
            exec("resetprop ro.build.fingerprint " + profile.fingerprint);
            exec("resetprop ro.vendor.build.fingerprint " + profile.fingerprint);
            exec("resetprop ro.system.build.fingerprint " + profile.fingerprint);

            // ── Marketing Name ──
            exec("resetprop ro.config.marketing_name \"" + profile.displayName + "\"");

            // ── Persist game boost profile ID ──
            exec("setprop persist.sys.game.boost.profile " + profile.id);

            // ── Display refresh rate ──
            exec("settings put system peak_refresh_rate 165.0");
            exec("settings put system min_refresh_rate 165.0");
            exec("settings put system user_refresh_rate 165");

            // ── GPU / ANGLE acceleration ──
            exec("settings put global angle_gl_driver_all_angle 1");
            exec("settings put global game_driver_all_apps 1");

            // ── Per-package game mode boost ──
            if (packageName != null && !packageName.trim().isEmpty()) {
                exec("cmd game mode performance " + packageName);
                exec("cmd game set --fps 165 " + packageName);
                exec("cmd window set-app-refresh-rate " + packageName + " 165");
                exec("device_config put game_overlay " + packageName + " mode=2,fps=165:mode=3,fps=165");
                exec("settings put global game_driver_opt_in_apps " + packageName);
            }

            activeProfileId = profile.id;
            Log.i(TAG, "✔ Full spoofing active: " + profile.model + " / " + profile.brand + " / " + profile.socModel);
            return true;

        } catch (Throwable e) {
            Log.e(TAG, "Failed to apply full device spoofing: " + profile.id, e);
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Reset
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resets the active game boost profile persist key.
     * Full property restoration requires device reboot (resetprop changes are volatile on reboot).
     */
    public static void resetSpoofing() {
        if (!ShizukuExecutor.isShizukuAvailable() || activeProfileId == null) return;
        try {
            exec("setprop persist.sys.game.boost.profile 0");
            Log.i(TAG, "Device spoofing reset. Properties will restore on next reboot.");
            activeProfileId = null;
        } catch (Throwable ignored) {}
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Internal helper
    // ─────────────────────────────────────────────────────────────────────────

    private static void exec(String command) {
        ShizukuExecutor.executeShizukuCommand(command);
    }
}
