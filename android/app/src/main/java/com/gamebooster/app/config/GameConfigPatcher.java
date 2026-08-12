package com.gamebooster.app.config;

import android.content.Context;
import android.content.pm.PackageManager;

import com.gamebooster.app.device.DevicePerformanceCapabilities;

/**
 * Compatibility boundary for legacy game-file patch calls.
 *
 * <p>Modifying another app's graphics, FPS, HDR, input, or configuration files
 * is not a safe or supported way to improve online-game performance. It can
 * break updates and violate a game's rules, so this class intentionally does
 * not read, create, or write any third-party game file.</p>
 */
public final class GameConfigPatcher {
    private GameConfigPatcher() { }

    public static final class PatchResult {
        public final boolean success;
        public final String message;

        public PatchResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static PatchResult applyGameFpsPatch(String packageName, int targetFps) {
        return unavailable(packageName);
    }

    /**
     * Applies a launcher-owned profile for an installed game. This is the safe
     * replacement for the old file-injection API: it persists the user's choice
     * in this app and requests Android's supported display/Game Mode controls.
     */
    public static PatchResult applyGameFpsPatch(Context context, String packageName, int targetFps) {
        if (context == null || packageName == null || packageName.trim().isEmpty()) {
            return new PatchResult(false, "Invalid game or device context");
        }
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return new PatchResult(false, "The selected game is not installed");
        }

        int requested = Math.max(30, targetFps);
        GameProfilePreferences.Profile profile = requested <= 90
                ? GameProfilePreferences.Profile.BALANCED
                : requested <= 144
                ? GameProfilePreferences.Profile.COMPETITIVE
                : GameProfilePreferences.Profile.MAX_SUPPORTED;
        GameProfilePreferences.setProfile(context, packageName, profile);
        int applied = DevicePerformanceCapabilities.detect(context).resolveRefreshRate(requested);
        boolean requestedToAndroid = GameProfileAutoConfigurator.autoConfigGamePackage(
                context, packageName, applied);
        return new PatchResult(true, requestedToAndroid
                ? "Saved launcher profile and requested Android-supported " + applied + "Hz. Actual FPS/graphics remain game-controlled."
                : "Saved launcher profile. Android did not accept the display request; check permissions and device support.");
    }

    public static PatchResult applyCompetitivePatch(String packageName, int targetFps) {
        return unavailable(packageName);
    }

    public static PatchResult applyCompetitivePatch(Context context, String packageName, int targetFps) {
        return applyGameFpsPatch(context, packageName, targetFps);
    }

    private static PatchResult unavailable(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return new PatchResult(false, "Invalid package name");
        }
        return new PatchResult(false,
                "Game-file editing is disabled. Use the game's own graphics settings and this app's supported display/Game Mode request.");
    }
}
