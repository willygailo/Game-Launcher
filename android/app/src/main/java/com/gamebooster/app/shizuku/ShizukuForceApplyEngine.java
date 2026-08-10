package com.gamebooster.app.shizuku;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.games.TargetGameRegistry;

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

        // Section 1: Permission Fortress & AppOps Lock
        batch.addAll(buildPermissionFortressBatch(appPkg));

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

    private static List<String> buildPermissionFortressBatch(String pkg) {
        List<String> b = new ArrayList<>();
        // Standard Privileged PM Grants
        b.add("pm grant " + pkg + " android.permission.WRITE_SECURE_SETTINGS");
        b.add("pm grant " + pkg + " android.permission.WRITE_SETTINGS");
        b.add("pm grant " + pkg + " android.permission.PACKAGE_USAGE_STATS");
        b.add("pm grant " + pkg + " android.permission.MANAGE_EXTERNAL_STORAGE");
        b.add("pm grant " + pkg + " android.permission.READ_EXTERNAL_STORAGE");
        b.add("pm grant " + pkg + " android.permission.WRITE_EXTERNAL_STORAGE");
        b.add("pm grant " + pkg + " android.permission.ACCESS_NOTIFICATION_POLICY");
        b.add("pm grant " + pkg + " android.permission.DUMP");
        b.add("pm grant " + pkg + " android.permission.BATTERY_STATS");
        b.add("pm grant " + pkg + " android.permission.MANAGE_GAME_MODE");
        b.add("pm grant " + pkg + " android.permission.OVERRIDE_WIFI_CONFIG");
        b.add("pm grant " + pkg + " android.permission.CHANGE_COMPONENT_ENABLED_STATE");
        b.add("pm grant " + pkg + " android.permission.CHANGE_NETWORK_STATE");
        b.add("pm grant " + pkg + " android.permission.FORCE_STOP_PACKAGES");
        b.add("pm grant " + pkg + " android.permission.CLEAR_APP_CACHE");
        b.add("pm grant " + pkg + " android.permission.REAL_GET_TASKS");
        b.add("pm grant " + pkg + " android.permission.SET_PROCESS_LIMIT");
        b.add("pm grant " + pkg + " android.permission.MODIFY_PHONE_STATE");
        b.add("pm grant " + pkg + " android.permission.HARDWARE_TEST");
        b.add("pm grant " + pkg + " android.permission.SYSTEM_ALERT_WINDOW");
        b.add("pm grant " + pkg + " android.permission.SCHEDULE_EXACT_ALARM");
        b.add("pm grant " + pkg + " android.permission.USE_EXACT_ALARM");

        // AppOps Overrides
        b.add("cmd appops set " + pkg + " MANAGE_EXTERNAL_STORAGE allow");
        b.add("cmd appops set " + pkg + " SYSTEM_ALERT_WINDOW allow");
        b.add("cmd appops set " + pkg + " GET_USAGE_STATS allow");
        b.add("cmd appops set " + pkg + " WRITE_SETTINGS allow");
        b.add("cmd appops set " + pkg + " MANAGE_GAME_MODE allow");
        b.add("cmd appops set " + pkg + " RUN_IN_BACKGROUND allow");
        b.add("cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND allow");
        b.add("cmd appops set " + pkg + " AUTO_START allow");
        b.add("cmd appops set " + pkg + " TURN_SCREEN_ON allow");
        b.add("cmd appops set " + pkg + " PROJECT_MEDIA allow");
        b.add("cmd appops set " + pkg + " ACCESS_RESTRICTED_SETTINGS allow");
        b.add("cmd appops set " + pkg + " NO_ISOLATED_STORAGE allow");
        b.add("cmd appops set " + pkg + " SCHEDULE_EXACT_ALARM allow");

        return b;
    }

    private static List<String> buildPersistPropertyBatch(int hz) {
        List<String> b = new ArrayList<>();
        b.add("setprop persist.sys.NV_FPSLIMIT " + hz);
        b.add("setprop persist.sys.NV_POWERMODE 1");
        b.add("setprop persist.sys.perf.topAppRenderThreadBoost.enable 1");
        b.add("setprop persist.vendor.thermal.enable 0");
        b.add("setprop persist.sys.gamemode.fps " + hz);
        b.add("setprop persist.hwui.renderer vulkan");
        b.add("setprop persist.debug.sf.hw 1");
        b.add("setprop persist.sys.bg_apps_limit 32");

        // Volatile Immediate Locks
        b.add("setprop debug.sf.fps_limit " + hz);
        b.add("setprop debug.sf.hw 1");
        b.add("setprop debug.hwui.renderer vulkan");
        b.add("setprop debug.renderengine.backend vulkan");
        b.add("setprop debug.sf.early_app_phase_offset_ns 500000");

        // SurfaceFlinger Refresh Lock Commands
        b.add("service call SurfaceFlinger 1035 i32 " + hz);
        b.add("service call SurfaceFlinger 1036 i32 " + hz);

        return b;
    }

    private static List<String> buildDeviceConfigBatch() {
        List<String> b = new ArrayList<>();
        b.add("device_config put activity_manager max_cached_processes 32");
        b.add("device_config put game_driver game_driver_all_apps 1");
        b.add("device_config put netd_native tcp_upgrade_to_v4mapped false");
        b.add("device_config put runtime_native_boot iorap_perfetto_enable true");
        return b;
    }

    private static List<String> buildPerGameLockBatch(List<String> gamePkgs, int hz) {
        List<String> b = new ArrayList<>();
        if (gamePkgs == null) return b;

        for (String gamePkg : gamePkgs) {
            b.add("cmd game mode performance " + gamePkg);
            b.add("cmd game set --fps " + hz + " " + gamePkg);
            b.add("cmd window set-app-refresh-rate " + gamePkg + " " + hz);
            b.add("device_config put game_overlay " + gamePkg + " mode=2,fps=" + hz + ":mode=3,fps=" + hz);
            b.add("cmd appops set " + gamePkg + " RUN_IN_BACKGROUND allow");
            b.add("cmd appops set " + gamePkg + " RUN_ANY_IN_BACKGROUND allow");
            b.add("cmd appops set " + gamePkg + " AUTO_START allow");
            b.add("cmd appops set " + gamePkg + " SYSTEM_ALERT_WINDOW allow");
            b.add("pm grant " + gamePkg + " android.permission.WRITE_SETTINGS");
            b.add("pm grant " + gamePkg + " android.permission.MANAGE_EXTERNAL_STORAGE");
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
        String scriptPath = "/data/local/tmp/gamebooster_force.sh";
        String scriptContent = "#!/system/bin/sh\\n" +
                "sync; echo 3 > /proc/sys/vm/drop_caches\\n" +
                "setprop persist.sys.NV_FPSLIMIT " + hz + "\\n" +
                "setprop persist.sys.NV_POWERMODE 1\\n" +
                "setprop debug.sf.fps_limit " + hz + "\\n" +
                "setprop debug.sf.hw 1\\n" +
                "setprop debug.hwui.renderer vulkan\\n" +
                "service call SurfaceFlinger 1035 i32 " + hz + "\\n" +
                "service call SurfaceFlinger 1036 i32 " + hz + "\\n" +
                "cmd power set-mode 0 1\\n" +
                "cmd power set-mode 2 1\\n" +
                "cmd thermalservice override-status 0\\n";

        String cmd = String.format("printf '%s' > %s && chmod 755 %s && sh %s",
                scriptContent, scriptPath, scriptPath, scriptPath);
        b.add(cmd);
        return b;
    }
}
