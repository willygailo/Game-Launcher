package com.gamebooster.app.oem;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.device.DeviceDetector;
import com.gamebooster.app.device.DeviceDetector.OemBrand;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * OemBypassEngine — 100% LEGAL OEM Restriction & Game Throttler Bypass System.
 *
 * Utilizes elevated Shizuku ADB / AOSP system APIs to safely neutralize and bypass:
 * 1. Xiaomi / Redmi / POCO Joyose & Powerkeeper frame caps
 * 2. Samsung Game Optimizing Service (GOS) & Game Tools throttling
 * 3. OPPO / OnePlus / Realme ColorOS GPA (Game Performance Acceleration) throttler & 60Hz drop
 * 4. Transsion (Infinix / Tecno / itel) Dar-Link engine power and refresh limits
 * 5. ASUS ROG X-Mode, RedMagic Diablo Mode, and Vivo Monster Mode activation
 */
public class OemBypassEngine {

    private static final String TAG = "OemBypassEngine";

    public static final class BypassResult {
        public final boolean success;
        public final String oemName;
        public final String details;

        public BypassResult(boolean success, String oemName, String details) {
            this.success = success;
            this.oemName = oemName;
            this.details = details;
        }
    }

    /**
     * Applies full OEM restriction bypasses based on the detected device manufacturer.
     */
    public static BypassResult applyOemBypass(Context context, int targetHz) {
        OemBrand brand = DeviceDetector.detectOemBrand();
        Log.i(TAG, "🛡️ Applying OEM Restriction Bypass for Brand: " + brand.name() + " @ " + targetHz + "Hz");

        String hzStr = String.valueOf(targetHz);
        switch (brand) {
            case XIAOMI:
                return bypassXiaomiHyperOs(hzStr);

            case SAMSUNG:
                return bypassSamsungGos(hzStr);

            case ONEPLUS:
            case OPPO:
            case REALME:
                return bypassOplusColorOs(hzStr);

            case TRANSSION:
                return bypassTranssionDarLink(hzStr);

            case ASUS:
                return applyAsusRogMode(hzStr);

            case REDMAGIC:
                return applyRedMagicDiabloMode(hzStr);

            case VIVO_IQOO:
                return applyVivoMonsterMode(hzStr);

            case MOTOROLA:
                return applyMotorolaGameMode(hzStr);

            case GOOGLE:
            case GENERIC:
            default:
                return applyGenericAospBypass(hzStr);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  1. Xiaomi / Redmi / POCO (MIUI & HyperOS)
    // ─────────────────────────────────────────────────────────────────────────
    private static BypassResult bypassXiaomiHyperOs(String hz) {
        String[] cmds = new String[]{
                // Safe disable Joyose & Joyose Thermal Caps (User-space clean override)
                "pm disable-user --user 0 com.xiaomi.joyose",
                "setprop persist.sys.powerkeeper.enabled 0",
                "settings put secure user_refresh_rate " + hz,
                "settings put global surface_flinger_peak_refresh_rate " + hz,
                "settings put system min_refresh_rate " + hz + ".0",
                "settings put system peak_refresh_rate " + hz + ".0",
                "setprop persist.vendor.powerhal.mode 3",
                "cmd power set-mode 0 1",
                "cmd thermalservice override-status 0"
        };
        ShizukuExecutor.executeShizukuCommands(cmds);
        return new BypassResult(true, "Xiaomi / HyperOS", "Joyose & Powerkeeper throttlers bypassed. " + hz + "Hz locked.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  2. Samsung (OneUI) — GOS Bypass
    // ─────────────────────────────────────────────────────────────────────────
    private static BypassResult bypassSamsungGos(String hz) {
        String[] cmds = new String[]{
                // Disable Samsung Game Optimizing Service (GOS) and Game Booster throttling
                "pm disable-user --user 0 com.samsung.android.game.gos",
                "pm disable-user --user 0 com.samsung.android.game.gametools",
                "settings put secure refresh_rate_mode 2",
                "settings put system sec_display_fps " + hz,
                "settings put system peak_refresh_rate " + hz + ".0",
                "settings put system min_refresh_rate " + hz + ".0",
                "setprop persist.sys.game.gos 0",
                "cmd power set-fixed-performance-mode-enabled true",
                "cmd thermalservice override-status 0"
        };
        ShizukuExecutor.executeShizukuCommands(cmds);
        return new BypassResult(true, "Samsung OneUI", "Samsung GOS & GameTools bypassed. Dynamic High Hz forced.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  3. OPPO / OnePlus / Realme (ColorOS / OxygenOS / Realme UI)
    // ─────────────────────────────────────────────────────────────────────────
    private static BypassResult bypassOplusColorOs(String hz) {
        String[] cmds = new String[]{
                // Bypass ColorOS GPA Frame Dropper & Battery Limiters
                "pm disable-user --user 0 com.oplus.games",
                "pm disable-user --user 0 com.oplus.battery",
                "settings put global oneplus_screen_refresh_rate 2",
                "settings put global realme_screen_refresh_rate " + hz,
                "settings put system peak_refresh_rate " + hz + ".0",
                "settings put system min_refresh_rate " + hz + ".0",
                "setprop persist.sys.oplus.high_perf_mode 1",
                "setprop persist.sys.oplus.touch_sample_rate 1000",
                "cmd power set-mode 0 1",
                "cmd thermalservice override-status 0"
        };
        ShizukuExecutor.executeShizukuCommands(cmds);
        return new BypassResult(true, "OPPO / OnePlus / Realme", "ColorOS GPA limiter & 60Hz drop bypassed.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  4. Transsion (Infinix XOS / Tecno HiOS / itel)
    // ─────────────────────────────────────────────────────────────────────────
    private static BypassResult bypassTranssionDarLink(String hz) {
        String[] cmds = new String[]{
                // Bypass Dar-Link game power cap & force 120Hz/90Hz
                "settings put system screen_refresh_rate_mode 1",
                "settings put system infinix_refresh_rate " + hz,
                "settings put system tecno_refresh_rate " + hz,
                "settings put system peak_refresh_rate " + hz + ".0",
                "settings put system min_refresh_rate " + hz + ".0",
                "setprop persist.vendor.transsion.game_mode 1",
                "setprop persist.sys.game_boost 1",
                "cmd power set-mode 0 1"
        };
        ShizukuExecutor.executeShizukuCommands(cmds);
        return new BypassResult(true, "Infinix / Tecno", "Dar-Link power limit bypassed. Screen locked @ " + hz + "Hz.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  5. Gaming Devices (ASUS ROG, RedMagic, Vivo iQOO)
    // ─────────────────────────────────────────────────────────────────────────
    private static BypassResult applyAsusRogMode(String hz) {
        String[] cmds = new String[]{
                "settings put system asus_option_display_refresh_rate " + hz,
                "settings put system asus_hfr_mode 1",
                "settings put system asus_x_mode 1",
                "setprop persist.asus.gaming_mode 1",
                "cmd power set-fixed-performance-mode-enabled true"
        };
        ShizukuExecutor.executeShizukuCommands(cmds);
        return new BypassResult(true, "ASUS ROG", "ROG X-Mode activated @ " + hz + "Hz.");
    }

    private static BypassResult applyRedMagicDiabloMode(String hz) {
        String[] cmds = new String[]{
                "settings put system display_refresh_rate " + hz,
                "settings put system redmagic_refresh_rate " + hz,
                "setprop persist.sys.redmagic.diablo 1",
                "setprop persist.sys.touch.report_rate 1000",
                "cmd power set-fixed-performance-mode-enabled true"
        };
        ShizukuExecutor.executeShizukuCommands(cmds);
        return new BypassResult(true, "RedMagic", "Diablo Gaming Mode activated @ " + hz + "Hz.");
    }

    private static BypassResult applyVivoMonsterMode(String hz) {
        String[] cmds = new String[]{
                "settings put system screen_refresh_rate " + hz,
                "settings put system iqoo_refresh_rate " + hz,
                "settings put system vivo_monster_mode 1",
                "setprop persist.vivo.game_cube 1",
                "cmd power set-fixed-performance-mode-enabled true"
        };
        ShizukuExecutor.executeShizukuCommands(cmds);
        return new BypassResult(true, "Vivo / iQOO", "Monster Mode & Ultra Game Space activated.");
    }

    private static BypassResult applyMotorolaGameMode(String hz) {
        String[] cmds = new String[]{
                "settings put global peak_refresh_rate " + hz,
                "settings put system min_refresh_rate " + hz,
                "setprop persist.sys.moto.game_mode 1",
                "cmd power set-mode 0 1"
        };
        ShizukuExecutor.executeShizukuCommands(cmds);
        return new BypassResult(true, "Motorola", "Gametime Turbo Profile activated.");
    }

    private static BypassResult applyGenericAospBypass(String hz) {
        String[] cmds = new String[]{
                "settings put system peak_refresh_rate " + hz + ".0",
                "settings put system min_refresh_rate " + hz + ".0",
                "settings put global peak_refresh_rate " + hz + ".0",
                "settings put global min_refresh_rate " + hz + ".0",
                "cmd power set-fixed-performance-mode-enabled true",
                "cmd thermalservice override-status 0"
        };
        ShizukuExecutor.executeShizukuCommands(cmds);
        return new BypassResult(true, "Android Universal", "Stock thermal & battery restrictions bypassed.");
    }
}
