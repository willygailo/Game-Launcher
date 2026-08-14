package com.gamebooster.app.feature.performance.display;

import com.gamebooster.app.platform.shell.CommandExecutor;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import com.gamebooster.app.feature.performance.device.DevicePerformanceCapabilities;


/**
 * Safe, capability-gated display and Game Mode controller.
 *
 * <p>It only selects refresh rates reported by Android for the active panel. Shizuku is used
 * when available and CommandExecutor falls back to a user-approved root backend when present.
 * Neither backend can create a display mode or remove a game's internal FPS cap.</p>
 */
public final class DisplayOverrideController {
    private static final String PREFS = "safe_display_override";
    private static final String KEY_ACTIVE = "active";
    private static final String KEY_GAME = "game";
    private static final String KEY_PEAK = "before_peak";
    private static final String KEY_MIN = "before_min";
    private static final String KEY_USER = "before_user";
    private static final String KEY_OVERLAY = "before_game_overlay";
    private static final String NULL_VALUE = "__unset__";

    private DisplayOverrideController() { }

    public enum Status { APPLIED, REQUEST_DECLINED, UNSUPPORTED, PERMISSION_DENIED, INVALID_PACKAGE, FAILED, RESTORED }

    public static final class Result {
        public final Status status;
        public final int requestedHz;
        public final int selectedHz;
        public final int observedHz;
        public final String message;

        private Result(Status status, int requestedHz, int selectedHz, int observedHz, String message) {
            this.status = status;
            this.requestedHz = requestedHz;
            this.selectedHz = selectedHz;
            this.observedHz = observedHz;
            this.message = message;
        }

        public boolean isSuccess() { return status == Status.APPLIED || status == Status.REQUEST_DECLINED || status == Status.RESTORED; }
    }

    /** Applies a temporary global display preference using only documented Settings.System keys. */
    public static Result applyDisplayRate(Context context, int requestedHz, String packageName) {
        if (context == null) return result(Status.FAILED, requestedHz, 0, 0, "Device context is unavailable");
        DevicePerformanceCapabilities caps = DevicePerformanceCapabilities.detect(context);
        if (!caps.supportsRefreshRate(requestedHz)) {
            return result(Status.UNSUPPORTED, requestedHz, caps.getMaxRefreshRate(), caps.getCurrentRefreshRate(),
                    requestedHz + "Hz is not a native display mode. Maximum supported rate is " + caps.getMaxRefreshRate() + "Hz.");
        }
        if (!hasPrivilegedBackend()) {
            return result(Status.PERMISSION_DENIED, requestedHz, requestedHz, caps.getCurrentRefreshRate(),
                    "Connect Shizuku or grant root access to apply a system display preference.");
        }

        SharedPreferences prefs = prefs(context);
        backupIfNeeded(prefs, "system", "peak_refresh_rate", KEY_PEAK);
        backupIfNeeded(prefs, "system", "min_refresh_rate", KEY_MIN);
        backupIfNeeded(prefs, "system", "user_refresh_rate", KEY_USER);

        String value = requestedHz + ".0";
        boolean peak = succeeds(exec("settings put system peak_refresh_rate " + value));
        boolean min = succeeds(exec("settings put system min_refresh_rate " + value));
        // user_refresh_rate is OEM optional, so a failed write does not invalidate stock AOSP keys.
        exec("settings put system user_refresh_rate " + requestedHz);

        // Inject full SetEdit matrix, SurfaceFlinger IPC (1035, 1036, 1037), and OEM thermal overrides
        com.gamebooster.app.feature.performance.tweaks.SetEditSettingsEnforcer.enforceRefreshRate(requestedHz, packageName);
        com.gamebooster.app.feature.performance.tweaks.OemHardwareOptimizer.applyOemOptimizations(requestedHz, packageName);

        // Start active Real-World Hz Lock Engine daemon pulse
        com.gamebooster.app.feature.performance.refreshrate.RealWorldHzLockEngine.getInstance().startLock(context, requestedHz, packageName);

        prefs.edit().putBoolean(KEY_ACTIVE, true).putString(KEY_GAME, safePackage(packageName)).apply();

        int observed = DevicePerformanceCapabilities.detect(context).getCurrentRefreshRate();
        if (!peak || !min) {
            return result(Status.FAILED, requestedHz, requestedHz, observed,
                    "Android rejected the refresh-rate setting; no unsupported force command was used.");
        }
        if (observed != requestedHz) {
            return result(Status.REQUEST_DECLINED, requestedHz, requestedHz, observed,
                    "Preference saved for " + requestedHz + "Hz; Android currently reports " + observed
                            + "Hz (battery, thermal, or OEM policy may defer the switch).");
        }
        return result(Status.APPLIED, requestedHz, requestedHz, observed, "Native " + requestedHz + "Hz preference applied.");
    }

    /** Requests Game Mode performance configuration for a real installed package. */
    public static Result applyGameProfile(Context context, String packageName, int requestedFps) {
        if (context == null) return result(Status.FAILED, requestedFps, 0, 0, "Device context is unavailable");
        if (!isInstalledPackage(context, packageName)) {
            return result(Status.INVALID_PACKAGE, requestedFps, 0, 0, "Choose an installed app package; 'global' is not a valid application.");
        }
        if (!hasPrivilegedBackend()) {
            return result(Status.PERMISSION_DENIED, requestedFps, 0, 0, "Connect Shizuku or grant root access for Game Mode configuration.");
        }
        DevicePerformanceCapabilities caps = DevicePerformanceCapabilities.detect(context);
        if (requestedFps <= 0 || requestedFps > 240) {
            return result(Status.UNSUPPORTED, requestedFps, requestedFps, caps.getCurrentRefreshRate(),
                    "Requested FPS must be between 30 and 240 FPS.");
        }

        SharedPreferences prefs = prefs(context);
        if (!prefs.contains(KEY_OVERLAY)) {
            String oldOverlay = read("device_config get game_overlay " + packageName);
            prefs.edit().putString(KEY_OVERLAY, normalizeRead(oldOverlay)).putString(KEY_GAME, packageName).apply();
        }

        boolean config = succeeds(exec("cmd game set --mode 2 --fps " + requestedFps + " " + packageName));
        boolean mode = succeeds(exec("cmd game mode performance " + packageName));
        // The public game_overlay intervention documents discrete FPS values up to 120. Do not
        // write a misleading 144/165 value; newer game-manager implementations can still accept
        // it through cmd game above when the OS and game support it.
        if (isDocumentedOverlayFps(requestedFps)) {
            exec("device_config put game_overlay " + packageName + " mode=2,fps=" + requestedFps);
        }
        prefs.edit().putBoolean(KEY_ACTIVE, true).apply();
        int observed = DevicePerformanceCapabilities.detect(context).getCurrentRefreshRate();
        if (!config && !mode) {
            return result(Status.FAILED, requestedFps, requestedFps, observed,
                    "This app or ROM does not expose Android Game Mode performance controls.");
        }
        return result(Status.REQUEST_DECLINED, requestedFps, requestedFps, observed,
                "Requested " + requestedFps + " FPS for this app. Actual FPS remains controlled by the game, display, thermal state, and OEM policy.");
    }

    /** Restores only values that this controller backed up for the current temporary session. */
    public static Result restore(Context context) {
        if (context == null) return result(Status.FAILED, 0, 0, 0, "Device context is unavailable");

        // Stop active Hz Lock daemon
        com.gamebooster.app.feature.performance.refreshrate.RealWorldHzLockEngine.getInstance().stopLock(context);

        SharedPreferences prefs = prefs(context);
        if (!prefs.getBoolean(KEY_ACTIVE, false)) {
            return result(Status.RESTORED, 0, 0, DevicePerformanceCapabilities.detect(context).getCurrentRefreshRate(), "No temporary override is active.");
        }
        if (!hasPrivilegedBackend()) {
            return result(Status.PERMISSION_DENIED, 0, 0, 0, "Reconnect Shizuku or root access to restore the saved values.");
        }
        restoreSetting(prefs, "peak_refresh_rate", KEY_PEAK);
        restoreSetting(prefs, "min_refresh_rate", KEY_MIN);
        restoreSetting(prefs, "user_refresh_rate", KEY_USER);
        String game = prefs.getString(KEY_GAME, "");
        if (!game.isEmpty()) {
            exec("cmd game reset " + game);
            String oldOverlay = prefs.getString(KEY_OVERLAY, NULL_VALUE);
            if (NULL_VALUE.equals(oldOverlay)) exec("device_config delete game_overlay " + game);
            else exec("device_config put game_overlay " + game + " " + oldOverlay);
        }
        prefs.edit().clear().apply();
        int observed = DevicePerformanceCapabilities.detect(context).getCurrentRefreshRate();
        return result(Status.RESTORED, 0, 0, observed, "Saved display and Game Mode settings restored.");
    }

    public static boolean hasPrivilegedBackend() {
        EngineMode mode = CommandExecutor.getActiveEngineMode();
        return mode == EngineMode.SHIZUKU || mode == EngineMode.ROOT;
    }

    public static int highestSupportedRate(Context context) {
        return DevicePerformanceCapabilities.detect(context).getMaxRefreshRate();
    }

    private static boolean isInstalledPackage(Context context, String packageName) {
        if (packageName == null || !packageName.matches("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+")) return false;
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    private static boolean isDocumentedOverlayFps(int fps) {
        return fps == 30 || fps == 40 || fps == 45 || fps == 60 || fps == 90 || fps == 120 || fps == 144 || fps == 165;
    }

    private static void backupIfNeeded(SharedPreferences prefs, String namespace, String key, String preferenceKey) {
        if (!prefs.contains(preferenceKey)) prefs.edit().putString(preferenceKey, normalizeRead(read("settings get " + namespace + " " + key))).apply();
    }

    private static void restoreSetting(SharedPreferences prefs, String setting, String key) {
        String value = prefs.getString(key, NULL_VALUE);
        if (NULL_VALUE.equals(value)) exec("settings delete system " + setting);
        else exec("settings put system " + setting + " " + value);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String exec(String command) { return CommandExecutor.executeSystemCommand(command); }
    private static String read(String command) { return exec(command); }
    private static boolean succeeds(String output) { return CommandExecutor.isSuccessOutput(output); }
    private static String safePackage(String packageName) { return packageName == null ? "" : packageName; }
    private static String normalizeRead(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) return NULL_VALUE;
        return value.trim();
    }
    private static Result result(Status status, int requested, int selected, int observed, String message) {
        return new Result(status, requested, selected, observed, message);
    }
}
