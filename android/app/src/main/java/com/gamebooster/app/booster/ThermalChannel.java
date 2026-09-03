package com.gamebooster.app.booster;

import android.os.Build;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

import java.util.ArrayList;
import java.util.List;

/**
 * ThermalChannel — Universal Temperature Limit & Thermal Throttling Bypass Engine.
 *
 * Implements comprehensive multi-layer thermal mitigation bypass for Android 13 (API 33),
 * Android 14 (API 34), Android 15 (API 35), and Android 16 (API 36 Baklava).
 *
 * Supported Layers:
 * 1. Android Framework ThermalService & PowerHAL (API 33-36)
 * 2. Qualcomm Snapdragon Adreno GPU & kgsl thermal levels
 * 3. MediaTek Dimensity / Helio thermal daemon suppression
 * 4. Google Tensor & Samsung Exynos thermal limits
 * 5. Linux Kernel /sys/class/thermal trip points elevation & zone disable
 */
public class ThermalChannel {

    private static final String TAG = "ThermalChannel";

    public static boolean setThermalOverride(boolean bypass) {
        String status = bypass ? "0" : "-1";
        String disableProp = bypass ? "1" : "0";
        String enableProp = bypass ? "0" : "1";
        boolean ok = false;

        Log.i(TAG, "Executing Universal Thermal Bypass: bypass=" + bypass + " (Android " + Build.VERSION.RELEASE + ", SDK " + Build.VERSION.SDK_INT + ")");

        List<String> batchCommands = new ArrayList<>();

        // ═══════════════════════════════════════════════════════════════════
        // LAYER 1: ANDROID FRAMEWORK THERMALSERVICE & POWERHAL (API 33-36)
        // ═══════════════════════════════════════════════════════════════════
        // Force ThermalService status to 0 (THERMAL_STATUS_NONE = Normal)
        batchCommands.add("cmd thermalservice override-status " + status);
        batchCommands.add("cmd thermal override-status " + status);

        if (bypass) {
            // Android 13-16 PowerHAL Fixed Performance Mode (Sustained Clocks)
            batchCommands.add("cmd power set-fixed-performance-mode-enabled true 2>/dev/null");
        } else {
            batchCommands.add("cmd power set-fixed-performance-mode-enabled false 2>/dev/null");
            batchCommands.add("dumpsys thermalservice reset 2>/dev/null");
        }

        // ═══════════════════════════════════════════════════════════════════
        // LAYER 2: QUALCOMM / SNAPDRAGON THERMAL MITIGATION PROPS & KGSL GPU
        // ═══════════════════════════════════════════════════════════════════
        batchCommands.add("setprop debug.thermal.throttle.disable " + disableProp);
        batchCommands.add("setprop debug.thermal.suppress_throttle " + disableProp);
        batchCommands.add("setprop vendor.thermal.mode " + (bypass ? "performance" : "normal"));
        batchCommands.add("setprop vendor.thermal.engine.mode " + (bypass ? "performance" : "normal"));
        batchCommands.add("setprop persist.sys.thermal.mitigation " + enableProp);
        batchCommands.add("setprop sys.thermal.mode " + (bypass ? "1" : "0"));
        batchCommands.add("setprop sys.thermal.perf_mode " + (bypass ? "1" : "0"));

        if (bypass) {
            // Lock Adreno GPU out of thermal down-stepping
            batchCommands.add("echo 0 > /sys/class/kgsl/kgsl-3d0/thermal_pwrlevel 2>/dev/null");
            batchCommands.add("echo 0 > /sys/class/kgsl/kgsl-3d0/throttling 2>/dev/null");
            batchCommands.add("echo 1 > /sys/class/kgsl/kgsl-3d0/force_bus_on 2>/dev/null");
            batchCommands.add("echo 1 > /sys/class/kgsl/kgsl-3d0/force_clk_on 2>/dev/null");
            batchCommands.add("echo 1 > /sys/class/kgsl/kgsl-3d0/force_rail_on 2>/dev/null");
        }

        // ═══════════════════════════════════════════════════════════════════
        // LAYER 3: MEDIATEK / DIMENSITY THERMAL SUPPRESSION
        // ═══════════════════════════════════════════════════════════════════
        batchCommands.add("setprop persist.vendor.thermal.mode " + (bypass ? "performance" : "normal"));
        batchCommands.add("setprop vendor.thermal.enable " + enableProp);
        batchCommands.add("setprop persist.sys.thermal.disabled " + disableProp);

        // ═══════════════════════════════════════════════════════════════════
        // LAYER 4: GOOGLE TENSOR & SAMSUNG EXYNOS THERMAL PROPS
        // ═══════════════════════════════════════════════════════════════════
        batchCommands.add("setprop sys.game.thermal.mode " + (bypass ? "max" : "normal"));
        batchCommands.add("setprop debug.cpurend.throttle " + enableProp);
        batchCommands.add("setprop debug.power.thermal.limit " + enableProp);

        // ═══════════════════════════════════════════════════════════════════
        // LAYER 5: LINUX KERNEL SYSFS THERMAL ZONES & TRIP POINTS
        // ═══════════════════════════════════════════════════════════════════
        if (bypass) {
            batchCommands.add("for z in /sys/class/thermal/thermal_zone*/mode; do echo disabled > $z 2>/dev/null; done");
            batchCommands.add("for z in /sys/devices/virtual/thermal/thermal_zone*/mode; do echo disabled > $z 2>/dev/null; done");
            batchCommands.add("for t in /sys/class/thermal/thermal_zone*/trip_point_*_temp; do echo 105000 > $t 2>/dev/null; done");
        } else {
            batchCommands.add("for z in /sys/class/thermal/thermal_zone*/mode; do echo enabled > $z 2>/dev/null; done");
            batchCommands.add("for z in /sys/devices/virtual/thermal/thermal_zone*/mode; do echo enabled > $z 2>/dev/null; done");
        }

        // Execute batch through Shizuku / Privileged AIDL service or System Shell
        StringBuilder joined = new StringBuilder();
        for (String cmd : batchCommands) {
            joined.append(cmd).append("; ");
        }

        try {
            if (ShizukuExecutor.hasShizukuPermission()) {
                String out = ShizukuExecutor.executeShizukuCommand(joined.toString());
                if (out != null) ok = true;
            }
        } catch (Throwable t) {
            Log.w(TAG, "Shizuku thermal execution failed: " + t.getMessage());
        }

        // Always ensure system properties and core framework commands execute locally / ADB
        String res1 = CommandExecutor.executeSystemCommand("cmd thermalservice override-status " + status);
        if (CommandExecutor.isSuccessOutput(res1)) ok = true;

        CommandExecutor.executeSystemCommand("cmd thermal override-status " + status);
        CommandExecutor.setSystemProperty("debug.thermal.throttle.disable", disableProp);
        CommandExecutor.setSystemProperty("vendor.thermal.mode", bypass ? "performance" : "normal");
        CommandExecutor.setSystemProperty("debug.thermal.suppress_throttle", disableProp);
        CommandExecutor.setSystemProperty("persist.sys.thermal.mitigation", enableProp);

        return ok;
    }
}
