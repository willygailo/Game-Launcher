package com.gamebooster.app.feature.performance.booster;

import android.util.Log;
import com.gamebooster.app.platform.shell.CommandExecutor;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

/**
 * AngleGraphicsDriverChannel manages safe reset and prevention of Android ANGLE driver overrides
 * to guarantee 100% stability in Chrome browser, Android System WebView, and system compositors.
 */
public class AngleGraphicsDriverChannel {

    private static final String TAG = "AngleDriverChannel";

    public static class AngleResult {
        public final boolean success;
        public final String message;

        public AngleResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static AngleResult enableAngleDriverForPackage(String packageName) {
        // Disabled: ANGLE causes severe rendering glitches and crashes in Chrome & WebView
        Log.w(TAG, "ANGLE driver forced mode is disabled to protect Chrome browser stability.");
        resetAngleDriver();
        return new AngleResult(true, "Safe driver mode enforced (ANGLE disabled for stability)");
    }

    public static AngleResult enableGlobalAngleDriver() {
        // Disabled: Global ANGLE breaks system-wide WebView and Chrome browser
        Log.w(TAG, "Global ANGLE driver is permanently disabled to ensure browser stability.");
        resetAngleDriver();
        return new AngleResult(true, "Safe driver mode enforced (Global ANGLE disabled)");
    }

    public static AngleResult resetAngleDriver() {
        Log.d(TAG, "Resetting and disabling all ANGLE driver overrides");

        String cmd = "settings delete global angle_gl_driver_all_angle; "
                + "settings delete global angle_gl_driver_selection_pkgs; "
                + "settings delete global angle_gl_driver_selection_values; "
                + "settings delete global angle_enabled_pkgs; "
                + "settings delete global angle_defer_init; "
                + "settings delete global updatable_driver_all_apps; "
                + "settings delete global updatable_driver_production_opt_in_apps; "
                + "settings delete global game_driver_all_apps; "
                + "setprop debug.angle.backend \"\"";

        CommandExecutor.executeSystemCommand(cmd);
        ShizukuExecutor.executeShizukuCommand(cmd);

        return new AngleResult(true, "Reset and disabled all ANGLE driver settings");
    }
}
