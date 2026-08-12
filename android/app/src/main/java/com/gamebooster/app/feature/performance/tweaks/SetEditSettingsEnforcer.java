package com.gamebooster.app.feature.performance.tweaks;

import com.gamebooster.app.platform.shell.CommandExecutor;

/**
 * SetEditSettingsEnforcer — Automated SetEdit-style system, secure, and global property injection engine.
 *
 * Enforces non-destructive system settings via Shizuku privileged shell (UID 2000)
 * to unlock 120/144/165Hz panel rates, ANGLE graphics drivers, and low-latency touch response.
 */
public class SetEditSettingsEnforcer {

    /**
     * Enforces the target display refresh rate across system & global namespaces.
     *
     * @param targetHz Refresh rate target (e.g. 120, 144, 165).
     * @return true if all refresh rate properties were successfully injected.
     */
    public static boolean enforceRefreshRate(int targetHz) {
        if (targetHz <= 0) {
            return false;
        }

        String hzStr = String.valueOf((float) targetHz);
        String intHzStr = String.valueOf(targetHz);

        boolean s1 = CommandExecutor.setSystemSetting("system", "peak_refresh_rate", hzStr);
        boolean s2 = CommandExecutor.setSystemSetting("system", "min_refresh_rate", hzStr);
        boolean s3 = CommandExecutor.setSystemSetting("system", "user_refresh_rate", intHzStr);
        boolean g1 = CommandExecutor.setSystemSetting("global", "user_refresh_rate", intHzStr);
        boolean g2 = CommandExecutor.setSystemSetting("global", "display_downscale_disable", "1");

        return s1 && s2 && s3 && g1 && g2;
    }

    /**
     * Configures Android Updatable Graphics Driver (ANGLE) channel for targeted game packages.
     *
     * @param commaSeparatedPackages Comma-separated list of target package names.
     * @return true if ANGLE production opt-in property was updated.
     */
    public static boolean enforceAngleDriverOptIn(String commaSeparatedPackages) {
        if (commaSeparatedPackages == null || commaSeparatedPackages.isEmpty()) {
            return false;
        }

        boolean disableAll = CommandExecutor.setSystemSetting("global", "updatable_driver_all_apps", "0");
        boolean setOptIn   = CommandExecutor.setSystemSetting("global", "updatable_driver_production_opt_in_apps", commaSeparatedPackages);

        return disableAll && setOptIn;
    }

    /**
     * Applies high-responsiveness touch sensitivity & latency tuning properties.
     *
     * @return true if touch properties were applied successfully.
     */
    public static boolean enforceUltraTouchSettings() {
        boolean t1 = CommandExecutor.setSystemSetting("system", "touch_prediction_latency", "0");
        boolean t2 = CommandExecutor.setSystemSetting("secure", "long_press_timeout", "250");
        boolean t3 = CommandExecutor.setSystemProperty("persist.sys.touch.rate", "240");

        return t1 && t2 && t3;
    }

    /**
     * Resets system and global refresh rate overrides back to stock dynamic defaults.
     *
     * @return true if default properties were restored.
     */
    public static boolean revertToDefaults() {
        boolean r1 = CommandExecutor.setSystemSetting("system", "peak_refresh_rate", "0.0");
        boolean r2 = CommandExecutor.setSystemSetting("system", "min_refresh_rate", "60.0");
        boolean r3 = CommandExecutor.setSystemSetting("global", "display_downscale_disable", "0");

        return r1 && r2 && r3;
    }
}
