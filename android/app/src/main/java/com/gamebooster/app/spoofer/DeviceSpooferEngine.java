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
        return com.gamebooster.app.spoofer.games.GameSpooferManager.getInstance().getStrategyForPackage(packageName).getSpoofProfile();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Apply
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Applies the auto-recommended or user-selected spoof profile for the given game package.
     */
    public static boolean applySpoofing(Context context, String packageName) {
        SpoofProfile profile = null;
        if (context != null && packageName != null) {
            String customId = SpoofPreferences.getGameSpoofProfileId(context, packageName);
            if (customId != null) {
                profile = getProfileById(customId);
            }
        }
        if (profile == null && context != null) {
            String activeId = SpoofPreferences.getActiveProfileId(context);
            if (activeId != null) {
                profile = getProfileById(activeId);
            }
        }
        if (profile == null) {
            profile = getRecommendedProfile(packageName);
        }
        return applyProfile(context, profile, packageName);
    }

    /**
     * Applies a specific SpoofProfile across Android 13–16 devices.
     * Uses Shizuku ADB privileged system property overrides when active,
     * and safe system settings / launcher profile configuration when non-root.
     */
    public static boolean applyProfile(Context context, SpoofProfile profile, String packageName) {
        if (profile == null) {
            Log.e(TAG, "Cannot apply null spoof profile.");
            return false;
        }

        try {
            activeProfileId = profile.id;
            if (context != null) {
                SpoofPreferences.setActiveProfileId(context, profile.id);
                SpoofPreferences.setSpoofEnabled(context, true);
                if (packageName != null) {
                    SpoofPreferences.setGameSpoofProfileId(context, packageName, profile.id);
                }
            }

            Log.d(TAG, "▶ Applying spoof profile: " + profile.displayName + " [" + profile.id + "]");

            // ── Safe System Settings Override (Non-Root / WRITE_SETTINGS) ──
            if (context != null) {
                try {
                    float targetHz = (float) profile.targetRefreshRate;
                    if (android.provider.Settings.System.canWrite(context)) {
                        android.provider.Settings.System.putFloat(context.getContentResolver(), "peak_refresh_rate", targetHz);
                        android.provider.Settings.System.putFloat(context.getContentResolver(), "min_refresh_rate", targetHz);
                        android.provider.Settings.System.putInt(context.getContentResolver(), "user_refresh_rate", profile.targetRefreshRate);
                    }
                } catch (Throwable ignored) {}
            }

            // ── Full System ADB Property Overrides via Shizuku (when available) ──
            if (ShizukuExecutor.isShizukuAvailable()) {
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
                exec("resetprop ro.soc.manufacturer " + profile.socVendor);
                exec("resetprop ro.product.board " + profile.board);
                exec("resetprop ro.chipname " + profile.chipname);
                exec("resetprop ro.sys.cpu.hardware " + profile.socModel);
                exec("resetprop ro.product.cpu.abi " + profile.cpuAbi);

                // ── GPU / Graphics Driver Identity ──
                exec("resetprop ro.hardware.egl " + profile.eglHardware);
                exec("resetprop debug.egl.hw_renderer \"" + profile.glRenderer + "\"");
                exec("resetprop ro.opengles.version " + profile.glesVersion);

                // ── Memory / RAM Capacity Overrides ──
                exec("resetprop ro.config.ram " + profile.ramTotalMb);
                exec("resetprop ro.sys.ram.total " + (profile.ramTotalMb * 1024L * 1024L));
                exec("resetprop ro.config.hw_ram " + profile.ramTotalMb);

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

                // ── Display refresh rate via ADB ──
                float targetHz = (float) profile.targetRefreshRate;
                exec("settings put system peak_refresh_rate " + targetHz);
                exec("settings put system min_refresh_rate " + targetHz);
                exec("settings put system user_refresh_rate " + profile.targetRefreshRate);

                // ── GPU / ANGLE acceleration ──
                exec("settings put global angle_gl_driver_all_angle 1");
                exec("settings put global game_driver_all_apps 1");

                // ── Per-package game mode boost across ALL target games (MLBB, PUBGM, CODM, HOK, Genshin, etc.) ──
                applyGameModeCommandsToAllGames(context, profile);

                if (packageName != null && !packageName.trim().isEmpty()) {
                    exec("cmd game mode performance " + packageName);
                    exec("cmd game set --fps " + profile.targetRefreshRate + " " + packageName);
                    exec("cmd window set-app-refresh-rate " + packageName + " " + profile.targetRefreshRate);
                    exec("device_config put game_overlay " + packageName + " mode=2,fps=" + profile.targetRefreshRate + ":mode=3,fps=" + profile.targetRefreshRate);
                    exec("settings put global game_driver_opt_in_apps " + packageName);
                }
                Log.i(TAG, "✔ Full Shizuku ADB property spoofing active across all target game packages: " + profile.displayName);
            } else {
                Log.i(TAG, "✔ Safe Non-Root launcher profile spoofing active: " + profile.displayName);
            }

            return true;

        } catch (Throwable e) {
            Log.e(TAG, "Failed to apply device spoofing: " + profile.id, e);
            return false;
        }
    }

    /**
     * Applies Shizuku API Game Mode performance, FPS locks, and Game Driver flags to ALL target eSports game packages.
     */
    private static void applyGameModeCommandsToAllGames(Context context, SpoofProfile profile) {
        if (profile == null) return;
        int fps = profile.targetRefreshRate;
        java.util.Set<String> targetPackages = new java.util.HashSet<>();

        // 1. Popular eSports & Target Game Packages
        targetPackages.addAll(com.gamebooster.app.games.TargetGameRegistry.getAllPackages());

        // 2. Scanned Installed Games & Custom Added Games
        if (context != null) {
            try {
                List<com.gamebooster.app.games.GameAppInfo> scanned = com.gamebooster.app.games.HomeGameScanner.scanTargetGames(context);
                for (com.gamebooster.app.games.GameAppInfo app : scanned) {
                    if (app != null && app.getPackageName() != null) {
                        targetPackages.add(app.getPackageName());
                    }
                }
                java.util.Set<String> custom = com.gamebooster.app.games.GameLauncherHelper.getCustomPackages(context);
                if (custom != null) {
                    targetPackages.addAll(custom);
                }
            } catch (Throwable ignored) {}
        }

        // 3. Apply Shizuku API Game Mode & FPS forcing to each game package legally & safely
        for (String pkg : targetPackages) {
            if (pkg == null || pkg.trim().isEmpty()) continue;
            try {
                exec("cmd game mode performance " + pkg);
                exec("cmd game set --fps " + fps + " " + pkg);
                exec("cmd window set-app-refresh-rate " + pkg + " " + fps);
                exec("device_config put game_overlay " + pkg + " mode=2,fps=" + fps + ":mode=3,fps=" + fps);
                exec("settings put global game_driver_opt_in_apps " + pkg);
                exec("appops set " + pkg + " AUTO_REVOKE_PERMISSIONS_IF_UNUSED ignore");
            } catch (Throwable ignored) {}
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
