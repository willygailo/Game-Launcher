package com.gamebooster.app.tweaks;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.gamebooster.app.engine.PrivilegeBridgeEngine;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TweakSelfHealingVerifier — 2026 Vendor-Adaptive Verification & Autonomous Self-Healing Engine.
 *
 * Detects modern chipset families (Snapdragon 8 Gen 3/4 Oryon, MediaTek Dimensity 9300/9400,
 * Samsung Exynos 2400 Xclipse, Google Tensor G3/G4) and dynamically rewrites/verifies sysfs nodes.
 * Continuously monitors active tweaks and heals any parameters reverted by OEM thermal daemons.
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
    private static ScheduledExecutorService sWatchdogExecutor = null;
    private static volatile boolean sWatchdogRunning = false;

    private TweakSelfHealingVerifier() {}

    /**
     * Identifies active device chipset architecture with 2026 deep SoC detection.
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

        // Qualcomm: SM8450/SM8550/SM8650/SM8750 (Snapdragon 8 Gen 1-4, Oryon), kalama, pineapple, sun
        if (hardware.contains("qcom") || board.contains("qcom") || board.contains("pineapple") || board.contains("sun")
                || soc.contains("sm8") || soc.contains("snapdragon") || new File("/sys/class/kgsl/kgsl-3d0").exists()) {
            sDetectedVendor = ChipsetVendor.QUALCOMM;
        // MediaTek: Dimensity 9000/9200/9300/9400 (MT6983/MT6985/MT6989/MT6991)
        } else if (hardware.contains("mt") || board.contains("mt") || soc.contains("dimensity") || soc.contains("mt69")
                || new File("/sys/module/mtk_fpsgo").exists() || new File("/sys/devices/platform/13040000.mali").exists()) {
            sDetectedVendor = ChipsetVendor.MEDIATEK;
        // Exynos: 2200/2400 (Xclipse 920/940 RDNA3 GPU)
        } else if (hardware.contains("exynos") || board.contains("universal") || soc.contains("s5e99")
                || new File("/sys/devices/platform/17000000.gpu").exists()) {
            sDetectedVendor = ChipsetVendor.SAMSUNG_EXYNOS;
        // Google Tensor: G1-G4 (gs101, gs201, zuma, zumapro)
        } else if (hardware.contains("tensor") || hardware.contains("gs101") || hardware.contains("gs201")
                || hardware.contains("zuma") || board.contains("zuma")) {
            sDetectedVendor = ChipsetVendor.GOOGLE_TENSOR;
        } else {
            sDetectedVendor = ChipsetVendor.GENERIC;
        }

        Log.i(TAG, "Hardware detected: vendor=" + sDetectedVendor + " [HW=" + hardware + ", BOARD=" + board + ", SOC=" + soc + "]");
        return sDetectedVendor;
    }

    /**
     * Resolves the hardware display max refresh rate from context or system properties.
     */
    public static int getDeviceMaxRefreshRate(Context context) {
        if (context != null) {
            try {
                float maxRate = com.gamebooster.app.device.HardwareDisplayController.getMaxHardwareRefreshRate(context);
                if (maxRate > 0) {
                    return com.gamebooster.app.config.GameProfileAutoConfigurator.clampTargetFpsToDisplay(context, Math.round(maxRate));
                }
            } catch (Throwable ignored) {}
        }
        return 60;
    }

    /**
     * Adapts raw shell commands to match vendor-specific hardware nodes (2026 edition).
     */
    public static String adaptCommandForHardware(String command) {
        return adaptCommandForHardware(command, null);
    }

    /**
     * Adapts raw shell commands to match vendor-specific hardware nodes and dynamic panel capabilities.
     */
    public static String adaptCommandForHardware(String command, Context context) {
        if (command == null || command.isEmpty()) return "";
        ChipsetVendor vendor = getChipsetVendor();

        int targetHz = getDeviceMaxRefreshRate(context);
        if (targetHz <= 0) targetHz = 60;
        String hzStr = String.valueOf(targetHz);
        String hzFloat = targetHz + ".0";

        String adapted = command;

        // Dynamic placeholder substitution
        if (adapted.contains("{TARGET_HZ}")) {
            adapted = adapted.replace("{TARGET_HZ}", hzStr);
        }

        // Hardware panel adaptive clamping: clamp 185Hz / 120Hz if device panel doesn't support them
        if (targetHz < 185) {
            adapted = adapted.replace("SurfaceFlinger 1035 i32 185", "SurfaceFlinger 1035 i32 " + hzStr);
            adapted = adapted.replace("SurfaceFlinger 1036 i32 185", "SurfaceFlinger 1036 i32 " + hzStr);
            adapted = adapted.replace("debug.sf.fps_limit 185", "debug.sf.fps_limit " + hzStr);
            adapted = adapted.replace("persist.sys.NV_FPSLIMIT 185", "persist.sys.NV_FPSLIMIT " + hzStr);
            adapted = adapted.replace("persist.sys.game.fps 185", "persist.sys.game.fps " + hzStr);
            adapted = adapted.replace("persist.sys.game.rate 185", "persist.sys.game.rate " + hzStr);
            adapted = adapted.replace("persist.sys.fps 185", "persist.sys.fps " + hzStr);
            adapted = adapted.replace("peak_refresh_rate 185.0", "peak_refresh_rate " + hzFloat);
            adapted = adapted.replace("min_refresh_rate 185.0", "min_refresh_rate " + hzFloat);
            adapted = adapted.replace("user_refresh_rate 185", "user_refresh_rate " + hzStr);
            adapted = adapted.replace("cmd game set --fps 185 global", "cmd game set --fps " + hzStr + " global");
            adapted = adapted.replace("cmd window set-app-refresh-rate global 185", "cmd window set-app-refresh-rate global " + hzStr);
            adapted = adapted.replace("fps=185:mode=3,fps=185", "fps=" + hzStr + ":mode=3,fps=" + hzStr);
        }

        if (targetHz < 120) {
            adapted = adapted.replace("SurfaceFlinger 1035 i32 120", "SurfaceFlinger 1035 i32 " + hzStr);
            adapted = adapted.replace("debug.sf.fps_limit 120", "debug.sf.fps_limit " + hzStr);
            adapted = adapted.replace("persist.sys.NV_FPSLIMIT 120", "persist.sys.NV_FPSLIMIT " + hzStr);
            adapted = adapted.replace("persist.sys.game.fps 120", "persist.sys.game.fps " + hzStr);
            adapted = adapted.replace("peak_refresh_rate 120.0", "peak_refresh_rate " + hzFloat);
            adapted = adapted.replace("min_refresh_rate 120.0", "min_refresh_rate " + hzFloat);
            adapted = adapted.replace("cmd window set-app-refresh-rate global 120", "cmd window set-app-refresh-rate global " + hzStr);
        }

        if (vendor == ChipsetVendor.QUALCOMM) {
            // Adreno 7xx/8xx KGSL rail, bus, and clock locking
            if (command.contains("adreno") || command.contains("gpu")) {
                adapted += "; echo 1 > /sys/class/kgsl/kgsl-3d0/force_bus_on 2>/dev/null; echo 1 > /sys/class/kgsl/kgsl-3d0/force_clk_on 2>/dev/null; echo 1 > /sys/class/kgsl/kgsl-3d0/force_rail_on 2>/dev/null; echo 1000000 > /sys/class/kgsl/kgsl-3d0/idle_timer 2>/dev/null";
            }
            if (command.contains("scaling_governor") || command.contains("cpufreq")) {
                adapted += "; echo 1 > /sys/devices/system/cpu/cpufreq/policy0/schedutil/iowait_boost_enable 2>/dev/null; echo 1024 > /dev/cpuset/top-app/uclamp.min 2>/dev/null";
            }
        } else if (vendor == ChipsetVendor.MEDIATEK) {
            // Dimensity 9300/9400 All-Big-Core FPSGo & EAS controls
            if (command.contains("adreno")) {
                adapted = adapted.replace("debug.adreno.turbo", "debug.mali.force_gpu_boost");
            }
            if (command.contains("mali") || command.contains("gpu")) {
                adapted += "; echo 1 > /proc/perfmgr/boost_ctrl/eas_ctrl/perfserv_ta_boost 2>/dev/null; echo 100 > /sys/module/mtk_fpsgo/parameters/fstb_soft_level 2>/dev/null";
            }
            if (command.contains("scaling_governor")) {
                adapted += "; echo 1 > /proc/perfmgr/boost_ctrl/dram_ctrl/ddr 2>/dev/null";
            }
        } else if (vendor == ChipsetVendor.SAMSUNG_EXYNOS) {
            if (command.contains("gpu")) {
                adapted += "; setprop debug.exynos.performance.mode 1; echo performance > /sys/devices/platform/17000000.gpu/devfreq/17000000.gpu/governor 2>/dev/null";
            }
        } else if (vendor == ChipsetVendor.GOOGLE_TENSOR) {
            if (command.contains("scaling_governor")) {
                adapted += "; echo performance > /sys/devices/system/cpu/cpu7/cpufreq/scaling_governor 2>/dev/null";
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

    /**
     * Verifies an applied tweak and autonomously heals/re-applies if rolled back.
     */
    public static boolean verifyAndHeal(TweakItem item) {
        return verifyAndHeal(item, null);
    }

    public static boolean verifyAndHeal(TweakItem item, Context context) {
        if (item == null || !item.isApplied()) return false;
        try {
            String cmd = item.getApplyCommand();
            if (cmd == null || cmd.isEmpty()) return true;

            // Thermal watchdog check: verify thermal zones status
            if ("thermalservice_override".equals(item.getId()) || cmd.contains("thermal")) {
                String thermalMode = PrivilegeBridgeEngine.executePrivileged("cat /sys/class/thermal/thermal_zone0/mode 2>/dev/null");
                if (thermalMode != null && thermalMode.trim().equalsIgnoreCase("enabled")) {
                    Log.w(TAG, "⚡ [Self-Healing] Thermal throttling re-enabled by OEM daemon, re-defeating...");
                    String adapted = adaptCommandForHardware(cmd, context);
                    PrivilegeBridgeEngine.executePrivileged(adapted);
                    return true;
                }
            }

            // Extract the first property test if applicable
            if (cmd.contains("setprop ")) {
                int idx = cmd.indexOf("setprop ");
                String sub = cmd.substring(idx + 8).trim();
                String[] parts = sub.split("\\s+");
                if (parts.length >= 2) {
                    String propKey = parts[0];
                    String expectedVal = parts[1].replace(";", "").trim();
                    if (!verifyProperty(propKey, expectedVal)) {
                        Log.w(TAG, "⚡ [Self-Healing] Revert detected on " + item.getId() + " (" + propKey + "), re-applying...");
                        String adapted = adaptCommandForHardware(item.getApplyCommand(), context);
                        PrivilegeBridgeEngine.executePrivileged(adapted);
                        return true;
                    }
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "verifyAndHeal error for " + item.getId() + ": " + t.getMessage());
        }
        return false;
    }

    /**
     * Starts the background Self-Healing Watchdog during gaming sessions.
     */
    public static synchronized void startSelfHealingWatchdog(Context context, List<TweakItem> activeTweaks) {
        if (sWatchdogRunning) return;
        sWatchdogRunning = true;
        sWatchdogExecutor = Executors.newSingleThreadScheduledExecutor();
        sWatchdogExecutor.scheduleWithFixedDelay(() -> {
            try {
                if (activeTweaks == null || activeTweaks.isEmpty()) return;
                int healedCount = 0;
                for (TweakItem item : activeTweaks) {
                    if (item.isApplied() && verifyAndHeal(item, context)) {
                        healedCount++;
                    }
                }
                if (healedCount > 0) {
                    Log.i(TAG, "⚡ [Self-Healing Watchdog] Autonomously restored " + healedCount + " reverted tweaks.");
                }
            } catch (Throwable ignored) {}
        }, 15, 30, TimeUnit.SECONDS);
        Log.i(TAG, "🚀 [Self-Healing Watchdog] Engine started (30s interval).");
    }

    /**
     * Stops the background Self-Healing Watchdog.
     */
    public static synchronized void stopSelfHealingWatchdog() {
        sWatchdogRunning = false;
        if (sWatchdogExecutor != null) {
            try {
                sWatchdogExecutor.shutdownNow();
            } catch (Throwable ignored) {}
            sWatchdogExecutor = null;
        }
        Log.i(TAG, "🛑 [Self-Healing Watchdog] Engine stopped.");
    }
}

