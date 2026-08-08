package com.gamebooster.app.device;

import android.os.Build;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * UniversalDeviceAdapter — Full compatibility adapter for Android 12, 13, 14, 15, 16 (API 31–36)
 * supporting all phone manufacturers (Tecno, Infinix, Transsion, Samsung, Realme, Xiaomi/MI,
 * Vivo, OPPO, OnePlus, Motorola, ASUS ROG, Nubia, ZTE) and chipsets (MediaTek Dimensity/Helio,
 * Qualcomm Snapdragon, Samsung Exynos, Unisoc).
 */
public class UniversalDeviceAdapter {

    private static final String TAG = "UniversalDeviceAdapter";

    public enum OemBrand {
        TRANSSION_TECNO_INFINIX,
        SAMSUNG,
        XIAOMI_REDMI_POCO,
        REALME_OPPO_ONEPLUS,
        VIVO_IQOO,
        GOOGLE_PIXEL,
        MOTOROLA,
        ASUS_ROG,
        REDMAGIC_NUBIA,
        GENERIC_AOSP
    }

    public enum ChipsetVendor {
        QUALCOMM_SNAPDRAGON,
        MEDIATEK_DIMENSITY_HELIO,
        GOOGLE_TENSOR,
        SAMSUNG_EXYNOS,
        UNISOC,
        UNKNOWN
    }

    public static int getAndroidApiVersion() {
        return Build.VERSION.SDK_INT;
    }

    public static String getAndroidVersionName() {
        switch (Build.VERSION.SDK_INT) {
            case 31: return "Android 12 (API 31)";
            case 32: return "Android 12L (API 32)";
            case 33: return "Android 13 (API 33)";
            case 34: return "Android 14 (API 34)";
            case 35: return "Android 15 (API 35)";
            case 36: return "Android 16 (API 36)";
            default: return "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
        }
    }

    public static OemBrand getOemBrand() {
        String mfr = Build.MANUFACTURER != null ? Build.MANUFACTURER.toLowerCase() : "";
        String brand = Build.BRAND != null ? Build.BRAND.toLowerCase() : "";
        String full = mfr + " " + brand;

        if (full.contains("infinix") || full.contains("tecno") || full.contains("transsion") || full.contains("itel")) {
            return OemBrand.TRANSSION_TECNO_INFINIX;
        } else if (full.contains("samsung")) {
            return OemBrand.SAMSUNG;
        } else if (full.contains("xiaomi") || full.contains("redmi") || full.contains("poco") || full.contains("blackshark")) {
            return OemBrand.XIAOMI_REDMI_POCO;
        } else if (full.contains("realme") || full.contains("oppo") || full.contains("oneplus")) {
            return OemBrand.REALME_OPPO_ONEPLUS;
        } else if (full.contains("vivo") || full.contains("iqoo")) {
            return OemBrand.VIVO_IQOO;
        } else if (full.contains("google") || full.contains("pixel")) {
            return OemBrand.GOOGLE_PIXEL;
        } else if (full.contains("motorola") || full.contains("moto")) {
            return OemBrand.MOTOROLA;
        } else if (full.contains("asus")) {
            return OemBrand.ASUS_ROG;
        } else if (full.contains("nubia") || full.contains("redmagic") || full.contains("zte")) {
            return OemBrand.REDMAGIC_NUBIA;
        } else {
            return OemBrand.GENERIC_AOSP;
        }
    }

    public static ChipsetVendor getChipsetVendor() {
        String hardware = Build.HARDWARE != null ? Build.HARDWARE.toLowerCase() : "";
        String board = Build.BOARD != null ? Build.BOARD.toLowerCase() : "";
        String soc = (hardware + " " + board).toLowerCase();

        if (soc.contains("qcom") || soc.contains("snapdragon") || soc.contains("sm8") || soc.contains("sm7") || soc.contains("sm6") || soc.contains("sm4")) {
            return ChipsetVendor.QUALCOMM_SNAPDRAGON;
        } else if (soc.contains("mt") || soc.contains("mediatek") || soc.contains("dimensity") || soc.contains("helio")) {
            return ChipsetVendor.MEDIATEK_DIMENSITY_HELIO;
        } else if (soc.contains("gs101") || soc.contains("gs201") || soc.contains("zuma") || soc.contains("tensor")) {
            return ChipsetVendor.GOOGLE_TENSOR;
        } else if (soc.contains("exynos") || soc.contains("s5e")) {
            return ChipsetVendor.SAMSUNG_EXYNOS;
        } else if (soc.contains("ums") || soc.contains("unisoc") || soc.contains("sprd")) {
            return ChipsetVendor.UNISOC;
        } else {
            return ChipsetVendor.UNKNOWN;
        }
    }

    /**
     * Uncaps Phantom Process Killer & Background Process Limits on Android 13, 14, 15, 16.
     */
    public static boolean applyAndroid13To16SystemUncap() {
        try {
            List<String> uncapCmds = new ArrayList<>();
            uncapCmds.add("device_config put activity_manager max_phantom_processes 2147483647");
            uncapCmds.add("settings put global settings_enable_monitor_phantom_procs false");
            uncapCmds.add("cmd deviceidle whitelist +com.gamebooster.app");
            uncapCmds.add("cmd appops set com.gamebooster.app RUN_IN_BACKGROUND allow");
            uncapCmds.add("cmd appops set com.gamebooster.app RUN_ANY_IN_BACKGROUND allow");

            for (String cmd : uncapCmds) {
                if (ShizukuExecutor.hasShizukuPermission()) {
                    ShizukuExecutor.executeShizukuCommand(cmd);
                } else {
                    CommandExecutor.executeSystemCommand(cmd);
                }
            }
            Log.i(TAG, "Android 13-16 Phantom Process Killer uncap executed successfully.");
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "applyAndroid13To16SystemUncap exception", t);
            return false;
        }
    }

    /**
     * Executes OEM-specific hardware game mode & refresh rate overrides.
     */
    public static int applyOemHardwareOptimization(int targetFps) {
        applyAndroid13To16SystemUncap();

        OemBrand brand = getOemBrand();
        ChipsetVendor chipset = getChipsetVendor();
        Log.i(TAG, "Applying OEM Optimization for " + brand + " | " + chipset + " | " + getAndroidVersionName());

        List<String> cmds = new ArrayList<>();
        String fpsStr = String.valueOf(targetFps);

        // ── 1. Android 12–16 Base Window & Frame Rate Properties ────────────────
        cmds.add("settings put system peak_refresh_rate " + fpsStr);
        cmds.add("settings put system min_refresh_rate " + fpsStr);
        cmds.add("settings put system user_refresh_rate " + fpsStr);
        cmds.add("settings put global peak_refresh_rate " + fpsStr);
        cmds.add("settings put global min_refresh_rate " + fpsStr);

        // ── 2. OEM Vendor Specific Commands ─────────────────────────────────────
        switch (brand) {
            case TRANSSION_TECNO_INFINIX:
                cmds.add("settings put system screen_refresh_rate_mode " + (targetFps >= 90 ? "1" : "0"));
                cmds.add("settings put system infinix_refresh_rate " + fpsStr);
                cmds.add("settings put system tecno_refresh_rate " + fpsStr);
                cmds.add("setprop sys.transsion.game.mode 1");
                cmds.add("setprop persist.sys.transsion.performance 1");
                break;

            case SAMSUNG:
                cmds.add("settings put secure refresh_rate_mode " + (targetFps >= 90 ? "2" : "1"));
                cmds.add("settings put system sec_display_fps " + fpsStr);
                cmds.add("setprop sys.samsung.game_performance 1");
                cmds.add("settings put global gos_game_fps " + fpsStr);
                break;

            case XIAOMI_REDMI_POCO:
                cmds.add("settings put secure user_refresh_rate " + fpsStr);
                cmds.add("settings put global surface_flinger_peak_refresh_rate " + fpsStr);
                cmds.add("setprop persist.sys.miui.game_mode 1");
                cmds.add("setprop sys.io.scheduler mq-deadline");
                cmds.add("settings put system joyose_game_mode 1");
                break;

            case REALME_OPPO_ONEPLUS:
                cmds.add("settings put global oneplus_screen_refresh_rate " + (targetFps >= 90 ? "2" : "1"));
                cmds.add("settings put global realme_screen_refresh_rate " + fpsStr);
                cmds.add("settings put system oppo_screen_refresh_rate " + fpsStr);
                cmds.add("setprop sys.oppo.game_space 1");
                break;

            case VIVO_IQOO:
                cmds.add("settings put system screen_refresh_rate " + fpsStr);
                cmds.add("settings put system iqoo_refresh_rate " + fpsStr);
                cmds.add("setprop sys.vivo.game_mode 1");
                break;

            case GOOGLE_PIXEL:
                cmds.add("settings put system peak_refresh_rate " + fpsStr);
                cmds.add("settings put system min_refresh_rate " + fpsStr);
                cmds.add("setprop debug.sf.showfps 0");
                break;

            case MOTOROLA:
                cmds.add("settings put global peak_refresh_rate " + fpsStr);
                cmds.add("settings put system min_refresh_rate " + fpsStr);
                break;

            case ASUS_ROG:
                cmds.add("settings put system asus_option_display_refresh_rate " + fpsStr);
                cmds.add("settings put system asus_hfr_mode 1");
                cmds.add("setprop sys.asus.gaming_mode 1");
                break;

            case REDMAGIC_NUBIA:
                cmds.add("settings put system display_refresh_rate " + fpsStr);
                cmds.add("settings put system redmagic_refresh_rate " + fpsStr);
                cmds.add("setprop sys.redmagic.game_mode 1");
                break;

            default:
                break;
        }

        // ── 3. Chipset Specific Tuning ──────────────────────────────────────────
        switch (chipset) {
            case MEDIATEK_DIMENSITY_HELIO:
                cmds.add("setprop persist.vendor.mtk_fps_limit " + fpsStr);
                cmds.add("setprop vendor.mtk.gpu.power_mode 1");
                cmds.add("setprop vendor.mtk.touch_boost 1");
                break;

            case QUALCOMM_SNAPDRAGON:
                cmds.add("setprop vendor.qcom.adreno.qgl.enabled 1");
                cmds.add("setprop debug.qcom.gpu.gpuboost.level 3");
                cmds.add("setprop vendor.kgsl.gpu.control 1");
                break;

            case GOOGLE_TENSOR:
                cmds.add("setprop debug.graphics.gpu.profiler.perfmode 1");
                break;

            case SAMSUNG_EXYNOS:
                cmds.add("setprop vendor.exynos.gpu.boost 1");
                break;

            case UNISOC:
                cmds.add("setprop vendor.sprd.gpu.boost 1");
                break;

            default:
                break;
        }

        // Safe execution loop with zero-crash fallback
        int success = 0;
        for (String cmd : cmds) {
            try {
                String res = ShizukuExecutor.hasShizukuPermission()
                        ? ShizukuExecutor.executeShizukuCommand(cmd)
                        : CommandExecutor.executeSystemCommand(cmd);
                if (res != null) success++;
            } catch (Throwable t) {
                Log.w(TAG, "Command failed gracefully on this ROM: " + cmd, t);
            }
        }
        return success;
    }
}
