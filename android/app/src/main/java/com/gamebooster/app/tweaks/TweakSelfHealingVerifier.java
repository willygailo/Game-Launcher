package com.gamebooster.app.tweaks;

import android.os.Build;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.engine.PrivilegeBridgeEngine;

import java.io.File;

/**
 * TweakSelfHealingVerifier — 2026 Vendor-Adaptive Verification & Self-Healing Engine.
 *
 * Detects device chipset family (Snapdragon, MediaTek, Exynos, Tensor)
 * and dynamically rewrites/verifies sysfs nodes for GPU, CPU, and thermal governor controls.
 */
public final class TweakSelfHealingVerifier {

    private static final String TAG = "TweakSelfHealing";

    public enum ChipsetVendor {
        QUALCOMM,
        MEDIATEK,
        SAMSUNG_EXYNOS,
        GOOGLE_TENSOR,
        GENERIC
    }

    private static volatile ChipsetVendor sDetectedVendor = null;

    private TweakSelfHealingVerifier() {}

    /**
     * Identifies active device chipset architecture.
     */
    public static ChipsetVendor getChipsetVendor() {
        if (sDetectedVendor != null) return sDetectedVendor;

        String hardware = (Build.HARDWARE != null ? Build.HARDWARE.toLowerCase() : "");
        String board = (Build.BOARD != null ? Build.BOARD.toLowerCase() : "");
        String soc = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                soc = Build.SOC_MODEL != null ? Build.SOC_MODEL.toLowerCase() : "";
            } catch (Throwable ignored) {}
        }

        if (hardware.contains("qcom") || board.contains("qcom") || soc.contains("sm8") || soc.contains("snapdragon") || new File("/sys/class/kgsl/kgsl-3d0").exists()) {
            sDetectedVendor = ChipsetVendor.QUALCOMM;
        } else if (hardware.contains("mt") || board.contains("mt") || soc.contains("dimensity") || new File("/sys/module/mtk_fpsgo").exists() || new File("/sys/devices/platform/13040000.mali").exists()) {
            sDetectedVendor = ChipsetVendor.MEDIATEK;
        } else if (hardware.contains("exynos") || board.contains("universal") || new File("/sys/devices/platform/17000000.gpu").exists()) {
            sDetectedVendor = ChipsetVendor.SAMSUNG_EXYNOS;
        } else if (hardware.contains("tensor") || hardware.contains("gs101") || hardware.contains("gs201") || hardware.contains("zuma")) {
            sDetectedVendor = ChipsetVendor.GOOGLE_TENSOR;
        } else {
            sDetectedVendor = ChipsetVendor.GENERIC;
        }

        Log.i(TAG, "Hardware detected: vendor=" + sDetectedVendor + " [HW=" + hardware + ", BOARD=" + board + ", SOC=" + soc + "]");
        return sDetectedVendor;
    }

    /**
     * Adapts raw shell commands to match vendor-specific hardware nodes.
     */
    public static String adaptCommandForHardware(String command) {
        if (command == null || command.isEmpty()) return "";
        ChipsetVendor vendor = getChipsetVendor();

        String adapted = command;
        if (vendor == ChipsetVendor.QUALCOMM) {
            // Ensure KGSL parameters are enabled if touching GPU
            if (command.contains("adreno") || command.contains("gpu")) {
                adapted += "; echo 1 > /sys/class/kgsl/kgsl-3d0/force_bus_on 2>/dev/null; echo 1 > /sys/class/kgsl/kgsl-3d0/force_clk_on 2>/dev/null; echo 1 > /sys/class/kgsl/kgsl-3d0/force_rail_on 2>/dev/null";
            }
        } else if (vendor == ChipsetVendor.MEDIATEK) {
            // MediaTek Dimensity FPSGo and Mali pathing
            if (command.contains("adreno")) {
                adapted = adapted.replace("debug.adreno.turbo", "debug.mali.force_gpu_boost");
            }
            if (command.contains("mali") || command.contains("gpu")) {
                adapted += "; echo 1 > /proc/perfmgr/boost_ctrl/eas_ctrl/perfserv_ta_boost 2>/dev/null; echo 100 > /sys/module/mtk_fpsgo/parameters/fstb_soft_level 2>/dev/null";
            }
        } else if (vendor == ChipsetVendor.SAMSUNG_EXYNOS) {
            if (command.contains("gpu")) {
                adapted += "; setprop debug.exynos.performance.mode 1; echo performance > /sys/devices/platform/17000000.gpu/devfreq/17000000.gpu/governor 2>/dev/null";
            }
        }
        return adapted;
    }

    /**
     * Verifies if a system property or sysctl was applied successfully.
     */
    public static boolean verifyProperty(String key, String expectedValue) {
        if (key == null || expectedValue == null) return false;
        try {
            String out = PrivilegeBridgeEngine.executePrivileged("getprop " + key);
            if (out != null && out.trim().equals(expectedValue.trim())) {
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
