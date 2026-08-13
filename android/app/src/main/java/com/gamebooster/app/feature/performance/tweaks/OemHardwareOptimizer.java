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
        INFINIX,
        TECNO,
        TRANSSION,
        SAMSUNG,
        XIAOMI_POCO,
        REALME_OPPO,
        VIVO_IQOO,
        ASUS_ROG,
        REDMAGIC,
        HONOR_HUAWEI,
        GENERIC
    }

    /**
     * Identifies device OEM vendor based on build metadata.
     */
    public static OemVendor detectVendor() {
        String manu = (Build.MANUFACTURER != null) ? Build.MANUFACTURER.toLowerCase() : "";
        String brand = (Build.BRAND != null) ? Build.BRAND.toLowerCase() : "";
        String model = (Build.MODEL != null) ? Build.MODEL.toLowerCase() : "";

        if (manu.contains("infinix") || brand.contains("infinix")) {
            return OemVendor.INFINIX;
        } else if (manu.contains("tecno") || brand.contains("tecno")) {
            return OemVendor.TECNO;
        } else if (manu.contains("transsion") || manu.contains("itel")) {
            return OemVendor.TRANSSION;
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
        } else if (manu.contains("asus") || brand.contains("asus") || model.contains("rog")) {
            return OemVendor.ASUS_ROG;
        } else if (manu.contains("nubia") || manu.contains("zte") || brand.contains("redmagic") || model.contains("redmagic")) {
            return OemVendor.REDMAGIC;
        } else if (manu.contains("honor") || manu.contains("huawei") || brand.contains("honor") || brand.contains("huawei")) {
            return OemVendor.HONOR_HUAWEI;
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
            case INFINIX:
                // Infinix XOS: Enable bypass charging, Dar-Link 3.0/4.0 governor & force FPS lock
                boolean i1 = CommandExecutor.setSystemProperty("sys.bypass.charging", "1");
                boolean i2 = CommandExecutor.setSystemProperty("persist.sys.darlink.mode", "1");
                CommandExecutor.setSystemProperty("persist.sys.phx.fps", String.valueOf(targetHz));
                CommandExecutor.setSystemProperty("persist.sys.game.fps", String.valueOf(targetHz));
                CommandExecutor.setSystemProperty("sys.infinix.fps", String.valueOf(targetHz));
                CommandExecutor.setSystemProperty("sys.oem.fps_limit", "0");
                CommandExecutor.setSystemSetting("system", "infinix_refresh_rate_mode", String.valueOf(targetHz));
                CommandExecutor.setSystemSetting("system", "xos_display_refresh_rate", String.valueOf(targetHz));
                success = i1 && i2;
                break;

            case TECNO:
                // Tecno HiOS: Enable bypass charging, Panther Engine & force FPS lock
                boolean t1 = CommandExecutor.setSystemProperty("sys.bypass.charging", "1");
                boolean t2 = CommandExecutor.setSystemProperty("persist.sys.darlink.mode", "1");
                CommandExecutor.setSystemProperty("persist.sys.phx.fps", String.valueOf(targetHz));
                CommandExecutor.setSystemProperty("persist.sys.game.fps", String.valueOf(targetHz));
                CommandExecutor.setSystemProperty("sys.tecno.fps", String.valueOf(targetHz));
                CommandExecutor.setSystemProperty("sys.oem.fps_limit", "0");
                CommandExecutor.setSystemSetting("system", "tecno_refresh_rate_mode", String.valueOf(targetHz));
                CommandExecutor.setSystemSetting("system", "hios_display_refresh_rate", String.valueOf(targetHz));
                success = t1 && t2;
                break;

            case TRANSSION:
                // Transsion: Enable bypass charging & Dar-Link governor
                boolean b1 = CommandExecutor.setSystemProperty("sys.bypass.charging", "1");
                boolean b2 = CommandExecutor.setSystemProperty("persist.sys.darlink.mode", "1");
                CommandExecutor.setSystemProperty("persist.sys.phx.fps", String.valueOf(targetHz));
                CommandExecutor.setSystemProperty("persist.sys.game.fps", String.valueOf(targetHz));
                CommandExecutor.setSystemProperty("sys.oem.fps_limit", "0");
                CommandExecutor.setSystemSetting("system", "transsion_refresh_rate_mode", String.valueOf(targetHz));
                success = b1 && b2;
                break;

            case SAMSUNG:
                // Samsung One UI: Disable GOS auto temperature throttling limit & uncap FPS limits
                boolean s1 = CommandExecutor.setSystemSetting("global", "game_auto_temperature_control", "0");
                boolean s2 = CommandExecutor.setSystemSetting("secure", "game_performance_mode", "1");
                CommandExecutor.setSystemProperty("sys.gos.fps_limit", String.valueOf(targetHz));
                CommandExecutor.setSystemSetting("system", "game_mode_fps", String.valueOf(targetHz));
                success = s1 && s2;
                break;

            case XIAOMI_POCO:
                // Xiaomi HyperOS/MIUI: Joyose high FPS target & thermal power override
                boolean x1 = CommandExecutor.setSystemProperty("persist.sys.power.fps", String.valueOf(targetHz));
                boolean x2 = CommandExecutor.setSystemProperty("persist.vendor.power.dfps", String.valueOf(targetHz));
                CommandExecutor.setSystemProperty("persist.sys.joyose.fps", String.valueOf(targetHz));
                CommandExecutor.setSystemProperty("sys.thermal.mode", "1");
                success = x1 && x2;
                break;

            case REALME_OPPO:
                // ColorOS / Realme UI: HyperBoost display refresh override
                boolean r1 = CommandExecutor.setSystemSetting("system", "oplus_customize_screen_refresh_rate", String.valueOf(targetHz));
                CommandExecutor.setSystemSetting("system", "oppo_screen_refresh_rate", String.valueOf(targetHz));
                success = r1;
                break;

            case VIVO_IQOO:
                // OriginOS / FunTouchOS: Ultra Game Mode ultra-high Hz panel trigger
                boolean v1 = CommandExecutor.setSystemSetting("system", "vivo_screen_refresh_rate", "3");
                CommandExecutor.setSystemSetting("system", "iqoo_game_fps_target", String.valueOf(targetHz));
                success = v1;
                break;

            case ASUS_ROG:
                // ROG Phone X-Mode performance override
                boolean a1 = CommandExecutor.setSystemProperty("sys.asus.gaming.mode", "1");
                CommandExecutor.setSystemProperty("persist.sys.asus.hz", String.valueOf(targetHz));
                CommandExecutor.setSystemProperty("sys.asus.fps", String.valueOf(targetHz));
                success = a1;
                break;

            case REDMAGIC:
                // REDMAGIC Game Space 165Hz unlock
                boolean n1 = CommandExecutor.setSystemProperty("sys.nubia.game.mode", "1");
                CommandExecutor.setSystemProperty("persist.sys.nubia.hz", String.valueOf(targetHz));
                CommandExecutor.setSystemProperty("sys.nubia.fps", String.valueOf(targetHz));
                success = n1;
                break;

            case HONOR_HUAWEI:
                // Honor MagicOS / Huawei EMUI performance override
                boolean h1 = CommandExecutor.setSystemSetting("system", "honor_screen_refresh_rate", String.valueOf(targetHz));
                boolean h2 = CommandExecutor.setSystemSetting("system", "hw_display_refresh_rate", String.valueOf(targetHz));
                CommandExecutor.setSystemProperty("persist.sys.hw.fps", String.valueOf(targetHz));
                success = h1 || h2;
                break;

            case GENERIC:
            default:
                // Universal thermal sustained performance boost
                String cmd = "cmd power set-mode 0 1";
                String res = CommandExecutor.executeSystemCommand(cmd);
                success = CommandExecutor.isSuccessOutput(res);
                break;
        }

        // Universal SurfaceFlinger Non-Drop Properties (Eradicates 85-90 FPS dynamic cap)
        CommandExecutor.setSystemProperty("debug.sf.fps_override", String.valueOf(targetHz));
        CommandExecutor.setSystemProperty("debug.sf.latch_unsignaled", "1");
        CommandExecutor.setSystemProperty("debug.sf.enable_gl_backpressure", "0");

        return success;
    }
}
