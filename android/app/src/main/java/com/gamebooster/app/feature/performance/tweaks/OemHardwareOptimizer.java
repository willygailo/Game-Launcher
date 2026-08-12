package com.gamebooster.app.feature.performance.tweaks;

import android.os.Build;
import com.gamebooster.app.platform.shell.CommandExecutor;

/**
 * OemHardwareOptimizer — Vendor-specific hardware tuning & thermal throttling bypass engine.
 *
 * Applies tailored hardware profiles for Transsion (Infinix/Tecno), Samsung, Xiaomi/POCO,
 * Realme/OPPO, and Vivo/iQOO devices via Shizuku shell IPC.
 */
public class OemHardwareOptimizer {

    public enum OemVendor {
        INFINIX_TECNO,
        SAMSUNG,
        XIAOMI_POCO,
        REALME_OPPO,
        VIVO_IQOO,
        GENERIC
    }

    /**
     * Identifies device OEM vendor based on build metadata.
     */
    public static OemVendor detectVendor() {
        String manu = (Build.MANUFACTURER != null) ? Build.MANUFACTURER.toLowerCase() : "";
        String brand = (Build.BRAND != null) ? Build.BRAND.toLowerCase() : "";

        if (manu.contains("infinix") || manu.contains("tecno") || manu.contains("transsion") ||
            brand.contains("infinix") || brand.contains("tecno")) {
            return OemVendor.INFINIX_TECNO;
        } else if (manu.contains("samsung") || brand.contains("samsung")) {
            return OemVendor.SAMSUNG;
        } else if (manu.contains("xiaomi") || manu.contains("poco") || manu.contains("redmi") ||
                   brand.contains("xiaomi") || brand.contains("poco") || brand.contains("redmi")) {
            return OemVendor.XIAOMI_POCO;
        } else if (manu.contains("realme") || manu.contains("oppo") || manu.contains("oneplus") ||
                   brand.contains("realme") || brand.contains("oppo") || brand.contains("oneplus")) {
            return OemVendor.REALME_OPPO;
        } else if (manu.contains("vivo") || manu.contains("iqoo") ||
                   brand.contains("vivo") || brand.contains("iqoo")) {
            return OemVendor.VIVO_IQOO;
        }
        return OemVendor.GENERIC;
    }

    /**
     * Applies vendor-tailored performance properties.
     *
     * @param targetHz Desired refresh rate target (e.g., 120, 144, 165).
     * @return true if OEM optimizations were successfully applied.
     */
    public static boolean applyOemOptimizations(int targetHz) {
        OemVendor vendor = detectVendor();
        boolean success = true;

        switch (vendor) {
            case INFINIX_TECNO:
                // Enable bypass charging & Dar-Link high FPS governor hook
                boolean b1 = CommandExecutor.setSystemProperty("sys.bypass.charging", "1");
                boolean b2 = CommandExecutor.setSystemProperty("persist.sys.darlink.mode", "1");
                success = b1 && b2;
                break;

            case SAMSUNG:
                // Disable GOS auto temperature throttling limit
                boolean s1 = CommandExecutor.setSystemSetting("global", "game_auto_temperature_control", "0");
                boolean s2 = CommandExecutor.setSystemSetting("secure", "game_performance_mode", "1");
                success = s1 && s2;
                break;

            case XIAOMI_POCO:
                // Joyose high FPS target & thermal power override
                boolean x1 = CommandExecutor.setSystemProperty("persist.sys.power.fps", String.valueOf(targetHz));
                boolean x2 = CommandExecutor.setSystemProperty("persist.vendor.power.dfps", String.valueOf(targetHz));
                success = x1 && x2;
                break;

            case REALME_OPPO:
                // HyperBoost display refresh override
                success = CommandExecutor.setSystemSetting("system", "oplus_customize_screen_refresh_rate", String.valueOf(targetHz));
                break;

            case VIVO_IQOO:
                // Ultra Game Mode ultra-high Hz panel trigger
                success = CommandExecutor.setSystemSetting("system", "vivo_screen_refresh_rate", "3");
                break;

            case GENERIC:
            default:
                // Universal thermal sustained performance boost
                String cmd = "cmd power set-mode 0 1";
                String res = CommandExecutor.executeSystemCommand(cmd);
                success = CommandExecutor.isSuccessOutput(res);
                break;
        }

        return success;
    }
}
