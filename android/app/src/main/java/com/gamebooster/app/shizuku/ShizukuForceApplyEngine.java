package com.gamebooster.app.shizuku;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.engine.PermissionBatchBuilder;
import com.gamebooster.app.engine.SetPropValidator;
import com.gamebooster.app.games.TargetGameRegistry;
import com.gamebooster.app.shizuku.ShizukuTerminalManager;

import java.util.ArrayList;
import java.util.List;

/**
 * ShizukuForceApplyEngine — Persistent, forceful lock engine via Shizuku ADB (uid 2000).
 * Applies all system properties, permissions, AppOps, device_config, and per-game performance modes
 * as a single batch without fallbacks, soft reverts, or UI cancels.
 */
public class ShizukuForceApplyEngine {

    private static final String TAG = "ShizukuForceEngine";

    public static class ForceApplyResult {
        public final boolean success;
        public final int totalCommands;
        public final String outputLog;

        public ForceApplyResult(boolean success, int totalCommands, String outputLog) {
            this.success = success;
            this.totalCommands = totalCommands;
            this.outputLog = outputLog;
        }
    }

    public static ForceApplyResult forceApplyAll(Context context, int targetHz) {
        if (context == null) {
            return new ForceApplyResult(false, 0, "ERROR: Context is null");
        }

        if (!ShizukuExecutor.hasShizukuPermission()) {
            return new ForceApplyResult(false, 0, "ERROR: Shizuku permission not granted");
        }

        Log.i(TAG, "⚡ Launching FULL FORCE-APPLY Engine for Target " + targetHz + " Hz...");

        List<String> batch = new ArrayList<>();
        String appPkg = context.getPackageName();

        // Section 1: Permission Fortress & AppOps Lock (via PermissionBatchBuilder — no duplicates)
        batch.addAll(PermissionBatchBuilder.buildGrantBatch(appPkg));


        // Section 2: System Persist Properties Lock (Never revert on reboot)
        batch.addAll(buildPersistPropertyBatch(targetHz));

        // Section 3: device_config Namespace Lock
        batch.addAll(buildDeviceConfigBatch());

        // Section 4: Per-Game Total Lock (All Target Games)
        batch.addAll(buildPerGameLockBatch(TargetGameRegistry.getAllPackages(), targetHz));

        // Section 5: OEM Battery Saver & Background Optimization Bypass
        batch.addAll(buildOemBypassBatch(appPkg));

        // Section 6: Thermal Override Lock
        batch.addAll(buildThermalLockBatch());

        // Section 7: Persistent Shell Script Boot Execution
        batch.addAll(buildBootScriptBatch(targetHz));

        Log.i(TAG, "Executing total of " + batch.size() + " forceful Shizuku commands...");
        String resultStr = ShizukuExecutor.executeShizukuBatchCommands(batch);

        boolean success = resultStr != null && !resultStr.startsWith("ERROR");
        if (success) {
            ForceApplyPreferences.setForceApplied(context, true, targetHz);
            Log.i(TAG, "✅ ALL " + batch.size() + " SHIZUKU FORCE-APPLY COMMANDS LOCKED SUCCESSFULLY!");
        } else {
            Log.e(TAG, "❌ Force Apply Batch Execution reported issues: " + resultStr);
        }

        return new ForceApplyResult(success, batch.size(), resultStr);
    }

    // Removed buildPermissionFortressBatch() — replaced by PermissionBatchBuilder.buildGrantBatch()
    // This eliminates the duplicate grant list that existed in both ShizukuExecutor and here.


    private static List<String> buildPersistPropertyBatch(int hz) {
        List<String> b = new ArrayList<>();

        // Detect OEM for safe thermal override (do NOT disable vendor thermal on Samsung/MediaTek)
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        boolean isSamsung   = manufacturer.contains("samsung");
        boolean isMediaTek  = Build.HARDWARE.toLowerCase().contains("mt")  // MediaTek SOC
                           || Build.HARDWARE.toLowerCase().contains("mediatek");

        // FPS / Frame Rate Lock
        addIfSupported(b, "setprop persist.sys.NV_FPSLIMIT " + hz);
        addIfSupported(b, "setprop persist.sys.NV_POWERMODE 1");
        addIfSupported(b, "setprop persist.sys.gamemode.fps " + hz);
        addIfSupported(b, "setprop persist.sys.perf.topAppRenderThreadBoost.enable 1");
        addIfSupported(b, "setprop persist.sys.bg_apps_limit 32");

        // GPU / Rendering pipeline
        addIfSupported(b, "setprop persist.hwui.renderer vulkan");
        addIfSupported(b, "setprop persist.debug.sf.hw 1");

        // Volatile immediate locks
        addIfSupported(b, "setprop debug.sf.fps_limit " + hz);
        addIfSupported(b, "setprop debug.sf.hw 1");
        addIfSupported(b, "setprop debug.hwui.renderer vulkan");
        addIfSupported(b, "setprop debug.renderengine.backend vulkan");
        addIfSupported(b, "setprop debug.sf.early_app_phase_offset_ns 500000");
        addIfSupported(b, "setprop debug.sf.early_phase_offset_ns 500000");
        addIfSupported(b, "setprop debug.sf.latch_unsignaled 1");
        addIfSupported(b, "setprop debug.sf.disable_backpressure 1");

        // Touch latency
        addIfSupported(b, "setprop debug.input.max_events_per_sec 1000");
        addIfSupported(b, "setprop view.touch_slop 0");
        addIfSupported(b, "setprop persist.sys.touch.response_time 0");
        addIfSupported(b, "setprop persist.sys.touch_prediction 1");
        addIfSupported(b, "setprop persist.sys.touch.sensitivity 10");
        addIfSupported(b, "setprop persist.vendor.qti.input.touch_boost 1");

        // Thermal — OEM-guarded: skip on Samsung and MediaTek to prevent thermal throttle crash
        if (!isSamsung && !isMediaTek) {
            addIfSupported(b, "setprop persist.vendor.thermal.enable 0");
        } else {
            Log.d(TAG, "OEM thermal guard: skipping persist.vendor.thermal.enable 0 on "
                    + Build.MANUFACTURER);
        }

        // Settings namespace additions (Android 13-16 compatible)
        b.add("settings put system pointer_speed 7");
        b.add("settings put global low_power 0");
        b.add("settings put global animator_duration_scale 0.5");
        b.add("settings put global transition_animation_scale 0.5");
        b.add("settings put global window_animation_scale 0.5");
        b.add("settings put secure game_dashboard_shown 1");
        // ANGLE driver per-game enforcement
        b.add("settings put global angle_gl_driver_all_angle 1");

        // SurfaceFlinger binder calls
        b.add("service call SurfaceFlinger 1035 i32 " + hz);
        b.add("service call SurfaceFlinger 1036 i32 " + hz);

        return b;
    }

    /** Adds a setprop command only if it's supported on the current Android API level. */
    private static void addIfSupported(List<String> batch, String setpropCmd) {
        String filtered = SetPropValidator.filterCommand(setpropCmd);
        if (filtered != null) {
            batch.add(filtered);
        }
    }

    private static List<String> buildDeviceConfigBatch() {
        List<String> b = new ArrayList<>();
        b.add("device_config put activity_manager max_cached_processes 32");
        // Android 12-13: game_driver namespace
        b.add("device_config put game_driver game_driver_all_apps 1");
        // Android 14+ uses game_manager namespace (dual-write for cross-version compat)
        b.add("device_config put game_manager game_driver_all_apps 1");
        b.add("device_config put game_manager game_default_frame_rate 0");
        b.add("device_config put activity_manager max_phantom_processes 2147483647");
        b.add("device_config put netd_native tcp_upgrade_to_v4mapped false");
        b.add("device_config put runtime_native_boot iorap_perfetto_enable true");
        b.add("device_config put connectivity tcp_use_default_deltas false");
        return b;
    }

    private static List<String> buildPerGameLockBatch(List<String> gamePkgs, int hz) {
        List<String> b = new ArrayList<>();
        if (gamePkgs == null) return b;

        for (String gamePkg : gamePkgs) {
            // Delegate to PermissionBatchBuilder for per-game batch
            b.addAll(PermissionBatchBuilder.buildPerGameBatch(gamePkg, hz));
        }

        return b;
    }

    private static List<String> buildOemBypassBatch(String appPkg) {
        List<String> b = new ArrayList<>();
        b.add("dumpsys deviceidle whitelist +" + appPkg);
        b.add("settings put global app_power_save_mode_delay_ms 0");
        b.add("settings put global aggressive_battery_saver_enabled 0");
        b.add("settings put global wifi_sleep_policy 2");
        b.add("settings put global mobile_data_always_on 1");
        return b;
    }

    private static List<String> buildThermalLockBatch() {
        List<String> b = new ArrayList<>();
        b.add("cmd thermalservice override-status 0");
        b.add("cmd power set-mode 0 1");
        b.add("cmd power set-mode 2 1");
        return b;
    }

    private static List<String> buildBootScriptBatch(int hz) {
        List<String> b = new ArrayList<>();
        // Delegate to ShizukuTerminalManager which handles clean multi-line script writing
        // This replaces the fragile String.format('%s') + inline \n approach that broke on
        // some shell interpreters and with % characters in content
        try {
            ShizukuTerminalManager.writeForceApplyScript(hz);
            b.add("sh " + ShizukuTerminalManager.FORCE_APPLY_SCRIPT_PATH);
        } catch (Exception e) {
            Log.w(TAG, "Failed to write force-apply script via terminal manager, using inline fallback", e);
            // Inline fallback (minimal, avoids format string issues)
            b.add("setprop persist.sys.NV_FPSLIMIT " + hz
                    + " && setprop debug.sf.fps_limit " + hz
                    + " && setprop debug.sf.hw 1");
        }
        return b;
    }
}
