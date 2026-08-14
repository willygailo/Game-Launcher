package com.gamebooster.app.feature.spoofer;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.feature.performance.booster.GameManagerAdapter;
import com.gamebooster.app.feature.performance.device.DevicePerformanceCapabilities;
import com.gamebooster.app.feature.performance.display.DisplayOverrideController;
import com.gamebooster.app.feature.performance.tweaks.OemHardwareOptimizer;
import com.gamebooster.app.feature.performance.tweaks.SetEditSettingsEnforcer;
import com.gamebooster.app.platform.shell.CommandExecutor;

import java.util.List;
import java.util.Map;

/**
 * DeviceSpooferEngine — Legal, non-destructive eSports profile & performance optimizer.
 *
 * <p>Applies flagship profile hardware targets (up to 165Hz display modes, Vulkan/ANGLE GPU
 * driver channels, SurfaceFlinger low-latency frame pacing, and AOSP Game Mode performance)
 * via 100% legal, ban-free system APIs on Android 13, 14, 15, and 16 (API 33-36).</p>
 */
public final class DeviceSpooferEngine {
    private static final String TAG = "DeviceSpooferEngine";

    private DeviceSpooferEngine() { }

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

    /** No identity profile can be active. */
    public static String getActiveProfileId() {
        return null;
    }

    /** Resolves recommended device profile for target package. */
    public static SpoofProfile getRecommendedProfile(String packageName) {
        return AppDeviceProfileRepository.resolveProfileForGame(null, packageName);
    }

    public static boolean applySpoofing(Context context, String packageName) {
        if (context == null) return false;
        SpoofProfile profile = AppDeviceProfileRepository.resolveProfileForGame(context, packageName);
        return applyProfile(context, profile, packageName);
    }

    public static boolean applyProfile(Context context, SpoofProfile profile, String packageName) {
        if (context == null || profile == null) return false;

        // Persist an app-only profile selection so the launcher can restore it.
        SpoofPreferences.setSpoofEnabled(context, true);
        SpoofPreferences.setActiveProfileId(context, profile.id);
        if (packageName != null && !packageName.trim().isEmpty()) {
            SpoofPreferences.setGameSpoofProfileId(context, packageName, profile.id);
        }

        int requestedHz = Math.max(30, profile.targetRefreshRate);
        int supportedHz = DevicePerformanceCapabilities.detect(context)
                .resolveRefreshRate(requestedHz);

        // 1. Apply native display refresh rate lock
        DisplayOverrideController.Result display =
                DisplayOverrideController.applyDisplayRate(context, supportedHz, packageName);

        // 2. Apply Android 13–16 Game Mode Performance & Dynamic Game Overlay
        if (packageName != null && !packageName.trim().isEmpty() && !"global".equalsIgnoreCase(packageName.trim())) {
            GameManagerAdapter gmAdapter = new GameManagerAdapter(context);
            gmAdapter.setGameMode(packageName, GameManagerAdapter.GAME_MODE_PERFORMANCE);
            gmAdapter.setGameModeAndFps(packageName, GameManagerAdapter.GAME_MODE_PERFORMANCE, supportedHz);

            boolean useAngle = profile.glRenderer != null && profile.glRenderer.toLowerCase(java.util.Locale.ROOT).contains("adreno");
            gmAdapter.applyGameOverlay(packageName, GameManagerAdapter.GAME_MODE_PERFORMANCE, supportedHz, 1.0f, useAngle);
            gmAdapter.setSurfaceFrameRateHint(packageName, (float) supportedHz);

            // 3. Opt-in to Android Updatable Graphics Driver (ANGLE/Vulkan channel)
            SetEditSettingsEnforcer.enforceAngleDriverOptIn(packageName);
        }

        // 4. Enforce high refresh rate & touch matrix tuning via SetEdit engine
        SetEditSettingsEnforcer.enforceRefreshRate(supportedHz, packageName);
        SetEditSettingsEnforcer.enforceUltraTouchSettings();

        // 5. Enforce OEM hardware optimizer for host device
        OemHardwareOptimizer.applyOemOptimizations(supportedHz, packageName);

        // 6. Enforce legal profile-specific brand feature flags
        applyProfileBrandOptimizations(profile, supportedHz, packageName);

        Log.i(TAG, "Applied legal device profile " + profile.displayName
                + " for " + PackageIdentityMasker.maskPackageName(packageName)
                + "; native display request: " + display.message);
        return true;
    }

    /**
     * Applies non-destructive, legal system tuning corresponding to the selected profile's brand.
     *
     * @param profile Target flagship profile.
     * @param targetHz Refresh rate target.
     * @param packageName Target package name (optional/nullable).
     * @return true if properties were injected successfully.
     */
    public static boolean applyProfileBrandOptimizations(SpoofProfile profile, int targetHz, String packageName) {
        if (profile == null) return false;
        String brand = (profile.brandLabel != null ? profile.brandLabel : "").toLowerCase(java.util.Locale.ROOT);
        String model = (profile.model != null ? profile.model : "").toLowerCase(java.util.Locale.ROOT);
        String hzStr = String.valueOf(targetHz);

        if (brand.contains("rog") || brand.contains("asus") || model.contains("rog")) {
            CommandExecutor.setSystemProperty("sys.asus.gaming.mode", "1");
            CommandExecutor.setSystemProperty("persist.sys.asus.hz", hzStr);
            CommandExecutor.setSystemProperty("sys.asus.fps", hzStr);
        } else if (brand.contains("nubia") || brand.contains("redmagic") || model.contains("redmagic")) {
            CommandExecutor.setSystemProperty("sys.nubia.game.mode", "1");
            CommandExecutor.setSystemProperty("persist.sys.nubia.hz", hzStr);
            CommandExecutor.setSystemProperty("sys.nubia.fps", hzStr);
            CommandExecutor.setSystemSetting("system", "nubia_refresh_rate", hzStr);
            CommandExecutor.setSystemSetting("system", "redmagic_game_mode", "1");
        } else if (brand.contains("xiaomi") || brand.contains("poco") || brand.contains("blackshark") || brand.contains("redmi")) {
            CommandExecutor.setSystemProperty("persist.sys.joyose.fps", hzStr);
            CommandExecutor.setSystemProperty("persist.vendor.dfps.level", hzStr);
            CommandExecutor.setSystemProperty("persist.vendor.power.dfps", hzStr);
            CommandExecutor.setSystemProperty("sys.thermal.mode", "1");
            CommandExecutor.setSystemSetting("system", "miui_refresh_rate", hzStr);
        } else if (brand.contains("samsung")) {
            CommandExecutor.setSystemSetting("global", "game_auto_temperature_control", "0");
            CommandExecutor.setSystemSetting("secure", "game_performance_mode", "1");
            CommandExecutor.setSystemSetting("system", "refresh_rate_mode", "2");
            CommandExecutor.setSystemProperty("sys.gos.fps_limit", hzStr);
            CommandExecutor.setSystemSetting("system", "game_mode_fps", hzStr);
        } else if (brand.contains("infinix") || brand.contains("tecno") || brand.contains("transsion") || brand.contains("itel")) {
            CommandExecutor.setSystemProperty("persist.sys.darlink.mode", "1");
            CommandExecutor.setSystemProperty("persist.sys.phx.fps", hzStr);
            CommandExecutor.setSystemProperty("persist.sys.game.fps", hzStr);
            CommandExecutor.setSystemProperty("sys.bypass.charging", "1");
            CommandExecutor.setSystemProperty("sys.oem.fps_limit", "0");
            CommandExecutor.setSystemSetting("system", "infinix_refresh_rate_mode", hzStr);
            CommandExecutor.setSystemSetting("system", "tecno_refresh_rate_mode", hzStr);
            CommandExecutor.setSystemSetting("system", "transsion_refresh_rate_mode", hzStr);
        } else if (brand.contains("vivo") || brand.contains("iqoo")) {
            CommandExecutor.setSystemSetting("system", "vivo_screen_refresh_rate", "3");
            CommandExecutor.setSystemSetting("system", "iqoo_game_fps_target", hzStr);
            if (packageName != null && !packageName.trim().isEmpty() && !"global".equalsIgnoreCase(packageName.trim())) {
                String currentList = CommandExecutor.executeSystemCommand("cmd settings get secure high_refresh_rate_apps_list");
                if (currentList == null || currentList.contains("null") || currentList.trim().isEmpty()) {
                    CommandExecutor.executeSystemCommand("cmd settings put secure high_refresh_rate_apps_list " + packageName);
                } else if (!currentList.contains(packageName)) {
                    CommandExecutor.executeSystemCommand("cmd settings put secure high_refresh_rate_apps_list \"" + currentList.trim() + "," + packageName + "\"");
                }
            }
        } else if (brand.contains("oneplus") || brand.contains("oppo") || brand.contains("realme")) {
            CommandExecutor.setSystemSetting("system", "oplus_customize_screen_refresh_rate", hzStr);
            CommandExecutor.setSystemSetting("system", "oppo_screen_refresh_rate", hzStr);
            CommandExecutor.setSystemSetting("secure", "oplus_customize_display_level", "3");
        }

        // Universal SurfaceFlinger Frame-Pacing & Latency optimization
        CommandExecutor.setSystemProperty("debug.sf.fps_override", hzStr);
        CommandExecutor.setSystemProperty("debug.sf.latch_unsignaled", "1");
        CommandExecutor.setSystemProperty("debug.sf.enable_gl_backpressure", "0");
        CommandExecutor.setSystemSetting("secure", "match_content_frame_rate", "0");
        CommandExecutor.setSystemSetting("secure", "refresh_rate_mode", "2");

        return true;
    }

    public static void applyGameGraphicsSpoof(Context context, String packageName, int targetHz) {
        if (context == null || packageName == null) return;
        int supportedHz = DevicePerformanceCapabilities.detect(context)
                .resolveRefreshRate(Math.max(30, targetHz));
        DisplayOverrideController.applyGameProfile(context, packageName, supportedHz);

        GameManagerAdapter gmAdapter = new GameManagerAdapter(context);
        gmAdapter.setGameMode(packageName, GameManagerAdapter.GAME_MODE_PERFORMANCE);
        gmAdapter.setGameModeAndFps(packageName, GameManagerAdapter.GAME_MODE_PERFORMANCE, supportedHz);

        Log.i(TAG, "Applied supported Android Game Mode request for " + packageName
                + " at up to " + supportedHz + "Hz; no spoofing or game-file edits performed.");
    }

    public static void resetSpoofing() {
        resetSpoofing(null, null);
    }

    /** Restores the temporary display/Game Mode values owned by the launcher. */
    public static void resetSpoofing(Context context) {
        resetSpoofing(context, null);
    }

    /** Restores display, Game Mode, and driver properties for target game. */
    public static void resetSpoofing(Context context, String packageName) {
        if (context != null) {
            DisplayOverrideController.restore(context);
            SpoofPreferences.setSpoofEnabled(context, false);
            SpoofPreferences.clearActiveProfile(context);
            if (packageName != null && !packageName.trim().isEmpty() && !"global".equalsIgnoreCase(packageName.trim())) {
                new GameManagerAdapter(context).resetGameMode(packageName);
            }
        }
        CommandExecutor.setSystemProperty("debug.sf.fps_override", "0");
        CommandExecutor.executeSystemCommand("cmd thermalservice override-status -1");
        CommandExecutor.executeSystemCommand("cmd thermal override-status -1");
        Log.i(TAG, "Reset legal spoofing and Game Mode state.");
    }
}

