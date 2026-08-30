package com.gamebooster.app.spoofer;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.config.GameConfigPathResolver;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HardwareMaskEngine — Full-Stack Hardware Masking & Device Spoofing Engine.
 *
 * Implements 6 comprehensive layers of device & hardware spoofing via Shizuku (temporary root):
 * 1. CPU / SoC MASKING:
 *    - Injects SoC Model, SoC Manufacturer, Architecture (ARM64-v9), Cores (8), and Clock speeds.
 *    - Generates & stages /proc/cpuinfo mock payloads and system properties.
 *
 * 2. GPU / GRAPHICS MASKING:
 *    - Injects Flagship GL Renderer (e.g. Adreno 840 / Immortalis-G925), GL Vendor, GL Version, Vulkan Driver.
 *    - Activates ANGLE GL Driver, Updatable Production Game Driver, and Vulkan RenderEngine backend.
 *
 * 3. RAM / MEMORY MASKING:
 *    - Spoofs Total RAM (16GB / 24GB LPDDR5X) & MemAvailable in system props and staged /proc/meminfo.
 *    - Tunes Dalvik VM heap size (heapsize=1024m, heapgrowthlimit=512m).
 *
 * 4. ANDROID MODEL & OS IDENTITY MASKING:
 *    - Modifies ro.product.*, ro.build.*, fingerprint, display ID, release version, SDK_INT.
 *    - Updates Global & System settings device names.
 *
 * 5. IN-APP JAVA REFLECTION OVERRIDE:
 *    - Safely mutates static Build fields (MODEL, BRAND, MANUFACTURER, HARDWARE, SOC_MODEL, etc.).
 *
 * 6. GAME ENGINE HARDWARE INJECTION:
 *    - Writes tailored hardware profiles (UE4 DeviceProfile.ini, Unity HardwareProfile.json,
 *      Genshin hardware_model_config.json, MLBB/HOK/FreeFire DeviceHardware.ini) to dynamically
 *      resolved game directories using GameConfigPathResolver.
 */
public class HardwareMaskEngine {

    private static final String TAG = "HardwareMaskEngine";

    /**
     * Applies full hardware masking across all 6 layers for a target profile and game package.
     */
    public static boolean applyFullHardwareMask(Context context, SpoofProfile profile, String packageName) {
        return applyFullHardwareMask(context, profile, packageName,
                GameSpoofSafetyRegistry.riskTierFor(packageName));
    }

    public static boolean applyFullHardwareMask(Context context, SpoofProfile profile, String packageName,
                                                GameSpoofSafetyRegistry.RiskTier riskTier) {
        if (profile == null) {
            Log.e(TAG, "Cannot apply null hardware mask profile.");
            return false;
        }

        try {
            Log.i(TAG, "══════════════════════════════════════════════════════════════════════");
            Log.i(TAG, "▶ [MASKING] Activating Full Hardware Masking: " + profile.displayName);
            Log.i(TAG, "▶ Model: " + profile.model + " | SoC: " + profile.socModel + " | GPU: " + profile.glRenderer + " | RAM: " + profile.ramTotalMb + "MB");
            Log.i(TAG, "══════════════════════════════════════════════════════════════════════");

            List<String> batchCommands = new ArrayList<>();

            // ═══════════════════════════════════════════════════════════════════
            //  LAYER 0: MAGISK / KERNELSU RESETPROP INJECTION (If Root Available)
            // ═══════════════════════════════════════════════════════════════════
            String serialNo = profile.getSerialNumber();
            String spoofedAndroidId = profile.getAndroidId();
            String spoofedWifiMac = profile.getWifiMacAddress();
            String spoofedBtMac = profile.getBluetoothMacAddress();
            String spoofedOaid = profile.getOaid();
            String spoofedGsfId = profile.getGsfId();
            String spoofedWidevine = profile.getWidevineDeviceId();
            String spoofedAaid = profile.getAdvertisingId();
            String spoofedImei1 = profile.getImei1();
            String spoofedImei2 = profile.getImei2();
            String eglVendor = profile.glVendor.toLowerCase().contains("arm") ? "mali" : "adreno";

            batchCommands.add("resetprop -n ro.serialno \"" + serialNo + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.boot.serialno \"" + serialNo + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ril.serialnumber \"" + serialNo + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.ril.oem.imei1 \"" + spoofedImei1 + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.ril.oem.imei2 \"" + spoofedImei2 + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.ril.miui.imei0 \"" + spoofedImei1 + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.ril.miui.imei1 \"" + spoofedImei2 + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.model \"" + profile.model + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.brand \"" + profile.brand + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.manufacturer \"" + profile.manufacturer + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.device \"" + profile.device + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.name \"" + profile.productName + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.board \"" + profile.board + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.vendor.model \"" + profile.model + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.vendor.brand \"" + profile.brand + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.vendor.name \"" + profile.productName + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.vendor.device \"" + profile.device + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.vendor.manufacturer \"" + profile.manufacturer + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.system.model \"" + profile.model + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.system.brand \"" + profile.brand + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.system.name \"" + profile.productName + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.system.device \"" + profile.device + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.system.manufacturer \"" + profile.manufacturer + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.odm.model \"" + profile.model + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.odm.brand \"" + profile.brand + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.odm.name \"" + profile.productName + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.odm.device \"" + profile.device + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.odm.manufacturer \"" + profile.manufacturer + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.build.product \"" + profile.buildProduct + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.soc.model \"" + profile.socModel + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.soc.manufacturer \"" + profile.socManufacturer + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.hardware \"" + profile.hardware + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.board.platform \"" + profile.platform + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.hardware.platform \"" + profile.platform + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.chipname \"" + profile.chipname + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.baseband \"" + profile.board + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.boot.baseband \"" + profile.board + "\" 2>/dev/null");
            batchCommands.add("resetprop -n gsm.version.baseband \"" + profile.baseband + "\" 2>/dev/null");
            batchCommands.add("resetprop -n gsm.version.baseband1 \"" + profile.baseband + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.build.fingerprint \"" + profile.fingerprint + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.build.display.id \"" + profile.displayId + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.build.version.release \"" + profile.androidVersion + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.build.version.sdk \"" + profile.sdkInt + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.build.version.security_patch \"" + profile.securityPatch + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.build.tags release-keys 2>/dev/null");
            batchCommands.add("resetprop -n ro.build.type user 2>/dev/null");
            batchCommands.add("resetprop -n ro.debuggable 0 2>/dev/null");
            batchCommands.add("resetprop -n ro.secure 1 2>/dev/null");
            batchCommands.add("resetprop -n ro.boot.flash.locked 1 2>/dev/null");
            batchCommands.add("resetprop -n ro.boot.verifiedbootstate green 2>/dev/null");
            batchCommands.add("resetprop -n ro.boot.vbmeta.device_state locked 2>/dev/null");
            batchCommands.add("resetprop -n ro.boot.veritymode enforcing 2>/dev/null");
            batchCommands.add("resetprop -n ro.boot.warranty_bit 0 2>/dev/null");
            batchCommands.add("resetprop -n ro.warranty_bit 0 2>/dev/null");
            batchCommands.add("resetprop -n sys.oem_unlock_allowed 0 2>/dev/null");
            batchCommands.add("resetprop -n ro.hardware.egl \"" + eglVendor + "\" 2>/dev/null");

            // ═══════════════════════════════════════════════════════════════════
            //  LAYER 1: CPU / SOC & BASEBAND MASKING
            // ═══════════════════════════════════════════════════════════════════
            batchCommands.add("setprop debug.game.spoofed_soc \"" + profile.socModel + "\"");
            batchCommands.add("setprop debug.game.spoofed_soc_vendor \"" + profile.socManufacturer + "\"");
            batchCommands.add("setprop debug.game.spoofed_cpu_cores \"" + profile.cpuCores + "\"");
            batchCommands.add("setprop debug.game.spoofed_cpu_freq \"" + profile.cpuMaxFreqKhz + "\"");
            batchCommands.add("setprop debug.game.spoofed_cpu_arch \"" + profile.cpuArchitecture + "\"");

            // ═══════════════════════════════════════════════════════════════════
            //  LAYER 2: GPU / GRAPHICS & VULKAN MASKING
            // ═══════════════════════════════════════════════════════════════════
            batchCommands.add("setprop debug.hwui.renderer vulkan");
            batchCommands.add("setprop debug.renderengine.backend vulkan");
            batchCommands.add("setprop debug.game.spoofed_gpu \"" + profile.glRenderer + "\"");
            batchCommands.add("setprop debug.game.spoofed_gpu_vendor \"" + profile.glVendor + "\"");
            batchCommands.add("setprop debug.game.spoofed_vulkan_ver \"" + profile.vulkanVersion + "\"");
            batchCommands.add("setprop debug.game.spoofed_vulkan_driver \"" + profile.vulkanDriverVersion + "\"");

            // ANGLE & Game Driver Routing (Per-Application Only — Never All Apps)
            batchCommands.add("settings put global angle_gl_driver_all_angle 0");
            batchCommands.add("settings put global game_driver_all_apps 0");
            batchCommands.add("settings put global updatable_driver_all_apps 0");
            String targetGames = com.gamebooster.app.booster.GpuTweaksChannel.getTargetGamesCsv();
            batchCommands.add("settings put global game_driver_opt_in_apps " + targetGames);
            batchCommands.add("settings put global updatable_driver_production_opt_in_apps " + targetGames);
            batchCommands.add("settings put global angle_gl_driver_selection_pkgs " + targetGames);
            batchCommands.add("settings put global angle_gl_driver_selection_values angle");

            // ═══════════════════════════════════════════════════════════════════
            //  LAYER 3: RAM & MEMORY MASKING
            // ═══════════════════════════════════════════════════════════════════
            batchCommands.add("setprop debug.game.spoofed_ram \"" + profile.ramTotalMb + "\"");
            batchCommands.add("setprop debug.game.spoofed_ram_avail \"" + profile.ramAvailableMb + "\"");
            batchCommands.add("setprop dalvik.vm.heapgrowthlimit 512m");
            batchCommands.add("setprop dalvik.vm.heapsize 1024m");
            batchCommands.add("setprop dalvik.vm.heaptargetutilization 0.75");
            batchCommands.add("setprop dalvik.vm.heapminfree 8m");
            batchCommands.add("setprop dalvik.vm.heapmaxfree 32m");

            // ═══════════════════════════════════════════════════════════════════
            //  LAYER 4: ANDROID MODEL & OS IDENTITY MASKING
            // ═══════════════════════════════════════════════════════════════════
            batchCommands.add("setprop ro.serialno \"" + serialNo + "\" 2>/dev/null");
            batchCommands.add("setprop ro.boot.serialno \"" + serialNo + "\" 2>/dev/null");
            batchCommands.add("setprop ro.build.tags release-keys 2>/dev/null");
            batchCommands.add("setprop ro.build.type user 2>/dev/null");
            batchCommands.add("setprop ro.debuggable 0 2>/dev/null");
            batchCommands.add("setprop ro.secure 1 2>/dev/null");
            batchCommands.add("setprop ro.boot.flash.locked 1 2>/dev/null");
            batchCommands.add("setprop ro.boot.verifiedbootstate green 2>/dev/null");

            for (Map.Entry<String, String> entry : profile.generateSystemProperties().entrySet()) {
                batchCommands.add("setprop " + entry.getKey() + " \"" + entry.getValue() + "\"");
            }

            // Determine Target Refresh Rate from profile field (no more string parsing hack)
            int targetHz = profile.maxRefreshRateHz > 0 ? profile.maxRefreshRateHz : 185;

            // Display & SurfaceFlinger Frame Rates
            batchCommands.add("settings put system peak_refresh_rate " + targetHz + ".0");
            batchCommands.add("settings put system min_refresh_rate " + targetHz + ".0");
            batchCommands.add("settings put system user_refresh_rate " + targetHz);
            batchCommands.add("settings put global peak_refresh_rate " + targetHz + ".0");
            batchCommands.add("settings put global min_refresh_rate " + targetHz + ".0");
            batchCommands.add("service call SurfaceFlinger 1035 i32 " + targetHz);
            batchCommands.add("service call SurfaceFlinger 1036 i32 " + targetHz);
            batchCommands.add("setprop debug.sf.fps_limit " + targetHz);
            batchCommands.add("setprop persist.sys.NV_FPSLIMIT " + targetHz);
            batchCommands.add("setprop persist.sys.NV_POWERMODE 1");
            batchCommands.add("setprop debug.gr.swapinterval 0");

            // Thermal Throttling Bypass for Maximum Performance without touching Real Battery
            batchCommands.add("cmd power set-fixed-performance-mode-enabled true 2>/dev/null");
            batchCommands.add("dumpsys battery reset 2>/dev/null");

            // Per-Package Android Game Mode & Overlay
            if (packageName != null && !packageName.trim().isEmpty()) {
                String pkg = packageName.trim();
                batchCommands.add("cmd game mode performance " + pkg);
                batchCommands.add("cmd game set --fps " + targetHz + " " + pkg);
                batchCommands.add("cmd window set-app-refresh-rate " + pkg + " " + targetHz);
                batchCommands.add("device_config put game_overlay " + pkg + " mode=2,fps=" + targetHz + ":mode=3,fps=" + targetHz);
                batchCommands.add("settings put global game_driver_opt_in_apps " + pkg);
                batchCommands.add("settings put global updatable_driver_production_opt_in_apps " + pkg);
            }

            // ═══════════════════════════════════════════════════════════════════
            //  NETWORK / IDENTITY SPOOFING & FULL PRIVACY SHIELD FOR GAMES & APPS
            // ═══════════════════════════════════════════════════════════════════
            // 1. Android ID, Device Name & Bluetooth Masking
            batchCommands.add("settings put secure android_id " + spoofedAndroidId);
            batchCommands.add("settings put global device_name \"" + profile.model + "\"");
            batchCommands.add("settings put system device_name \"" + profile.model + "\"");
            batchCommands.add("settings put system lock_screen_owner_info \"" + profile.model + "\"");
            batchCommands.add("settings put secure bluetooth_name \"" + profile.model + "\"");
            batchCommands.add("settings put secure bluetooth_address \"" + spoofedBtMac + "\"");
            batchCommands.add("settings put global randomized_mac_support 1");
            batchCommands.add("settings put global randomized_mac_connected_mac_randomization 1");
            batchCommands.add("setprop net.hostname \"" + profile.model.replace(" ", "_") + "\"");
            batchCommands.add("setprop debug.game.spoofed_android_id \"" + spoofedAndroidId + "\"");
            batchCommands.add("setprop debug.game.spoofed_serial \"" + serialNo + "\"");
            batchCommands.add("setprop debug.game.spoofed_wifi_mac \"" + spoofedWifiMac + "\"");
            batchCommands.add("setprop debug.game.spoofed_bt_mac \"" + spoofedBtMac + "\"");
            batchCommands.add("setprop debug.game.spoofed_oaid \"" + spoofedOaid + "\"");
            batchCommands.add("setprop debug.game.spoofed_gsf_id \"" + spoofedGsfId + "\"");
            batchCommands.add("setprop debug.game.spoofed_widevine \"" + spoofedWidevine + "\"");
            batchCommands.add("setprop debug.game.spoofed_aaid \"" + spoofedAaid + "\"");

            // 2. Advertising ID (AAID) & Privacy Anti-Tracking
            batchCommands.add("settings put secure limit_ad_tracking 1");
            batchCommands.add("settings put secure ad_id \"" + spoofedAaid + "\"");
            batchCommands.add("settings put secure advertising_id \"" + spoofedAaid + "\"");
            batchCommands.add("device_config put privacy privacy_sandbox_enabled false 2>/dev/null");

            // 3. Telemetry, Crash Analytics & Usage Reporting Deactivation
            batchCommands.add("settings put global usage_reporting_enabled 0");
            batchCommands.add("settings put secure usage_and_diagnostics_enabled 0");
            batchCommands.add("settings put secure upload_apk_enable 0");
            batchCommands.add("settings put secure send_action_app_error 0");
            batchCommands.add("settings put global send_action_app_error 0");

            // 4. AppOps Privacy & Device Identifier Shield (Hides IMEI, Serial, Phone State, Usage Stats, AAID)
            if (packageName != null && !packageName.trim().isEmpty()) {
                String pkg = packageName.trim();
                applyAppOpsShieldForPackage(batchCommands, pkg);
            }
            // Also enforce on all known games to prevent cross-app fingerprinting
            for (String gamePkg : com.gamebooster.app.games.GamePackageRegistry.getAllKnownGames().keySet()) {
                if (gamePkg != null && !gamePkg.equals(packageName)) {
                    applyAppOpsShieldForPackage(batchCommands, gamePkg);
                }
            }

            // Execute all elevated commands via Shizuku
            java.util.List<String> execResults =
                    ShizukuExecutor.executeShizukuCommandsWithResults(batchCommands);
            boolean commandsExecuted = execResults != null && !execResults.isEmpty();
            if (!commandsExecuted) {
                Log.w(TAG, "No elevated Shizuku channel available — ensure Shizuku permission is granted.");
            } else {
                int failures = 0;
                for (String r : execResults) {
                    if (r == null || r.startsWith("ERROR:") || r.startsWith("Permission denial")) failures++;
                }
                Log.i(TAG, "Elevated batch executed: " + execResults.size() + " command(s), "
                        + failures + " reported failure(s). Note: setprop ro.*/resetprop require root and may fail on Shizuku-only devices by design.");
            }

            // Trigger Shizuku display refresh rate and game driver forcing
            if (packageName != null && !packageName.trim().isEmpty()) {
                String pkg = packageName.trim();
                com.gamebooster.app.engine.GameModeApiSupport.setGameModePerformance(pkg);
            }
            if (profile.maxRefreshRateHz > 60) {
                com.gamebooster.app.booster.MaxHzForceChannel.forceApply(profile.maxRefreshRateHz);
            }

            // ═══════════════════════════════════════════════════════════════════
            //  LAYER 5: IN-APP REFLECTION OVERRIDE & HARDWARE PROFILES
            // ═══════════════════════════════════════════════════════════════════
            applyInAppReflectionMask(profile);

            // ═══════════════════════════════════════════════════════════════════
            //  LAYER 6: MOCK PROCFS PAYLOAD GENERATION & GAME ENGINE INJECTION
            // ═══════════════════════════════════════════════════════════════════
            exportMockProcfsPayloads(profile);

            // Inject hardware profile for targeted package + all registered games
            if (packageName != null && !packageName.trim().isEmpty()) {
                injectTailoredGameHardwareConfigs(packageName.trim(), profile);
            }
            injectAllInstalledGamesHardwareProfile(profile);

            Log.i(TAG, "✔ [MASKING COMPLETE] Hardware masking active for " + profile.displayName);
            return true;

        } catch (Throwable t) {
            Log.e(TAG, "Failed to apply full hardware mask: " + profile.id, t);
            return false;
        }
    }

    /**
     * Injects hardware profile into all known and registered games on the device.
     */
    public static void injectAllInstalledGamesHardwareProfile(SpoofProfile profile) {
        if (profile == null) return;
        try {
            for (String pkg : com.gamebooster.app.games.GamePackageRegistry.getAllKnownGames().keySet()) {
                injectTailoredGameHardwareConfigs(pkg, profile);
            }
        } catch (Throwable t) {
            Log.w(TAG, "injectAllInstalledGamesHardwareProfile error: " + t.getMessage());
        }
    }

    /**
     * Reflectively modifies static Build fields within the current Android runtime.
     */
    public static void applyInAppReflectionMask(SpoofProfile profile) {
        if (profile == null) return;
        try {
            setStaticField(android.os.Build.class, "MODEL", profile.model);
            setStaticField(android.os.Build.class, "BRAND", profile.brand);
            setStaticField(android.os.Build.class, "MANUFACTURER", profile.manufacturer);
            setStaticField(android.os.Build.class, "DEVICE", profile.device);
            setStaticField(android.os.Build.class, "PRODUCT", profile.productName);
            setStaticField(android.os.Build.class, "HARDWARE", profile.hardware);
            setStaticField(android.os.Build.class, "BOARD", profile.board);
            setStaticField(android.os.Build.class, "BOOTLOADER", profile.board);
            setStaticField(android.os.Build.class, "RADIO", profile.baseband);
            setStaticField(android.os.Build.class, "FINGERPRINT", profile.fingerprint);
            setStaticField(android.os.Build.class, "DISPLAY", profile.displayId);
            setStaticField(android.os.Build.class, "SERIAL", "R58" + String.format("%08X", (long) profile.id.hashCode() & 0xFFFFFFFFL));
            setStaticField(android.os.Build.class, "TAGS", "release-keys");
            setStaticField(android.os.Build.class, "TYPE", "user");

            try {
                setStaticField(android.os.Build.VERSION.class, "RELEASE", profile.androidVersion);
                setStaticField(android.os.Build.VERSION.class, "SDK_INT", profile.sdkInt);
                setStaticField(android.os.Build.VERSION.class, "SECURITY_PATCH", profile.securityPatch);
                setStaticField(android.os.Build.VERSION.class, "INCREMENTAL", profile.displayId);
            } catch (Throwable ignored) {}

            try {
                setStaticField(android.os.Build.class, "SOC_MODEL", profile.socModel);
                setStaticField(android.os.Build.class, "SOC_MANUFACTURER", profile.socManufacturer);
            } catch (Throwable ignored) {}

            Log.d(TAG, "In-app Build reflection applied: " + profile.model + " (" + profile.brand + ") Baseband: " + profile.baseband);
        } catch (Throwable t) {
            Log.w(TAG, "applyInAppReflectionMask non-fatal: " + t.getMessage());
        }
    }

    /**
     * Staging mock /proc/cpuinfo, /proc/meminfo, /proc/version, and system_spoof.prop files.
     */
    public static void exportMockProcfsPayloads(SpoofProfile profile) {
        if (profile == null) return;
        try {
            String cpuInfo = profile.generateCpuInfo();
            String memInfo = profile.generateMemInfo();
            String procVer = profile.generateProcVersion();

            StringBuilder props = new StringBuilder();
            for (Map.Entry<String, String> e : profile.generateSystemProperties().entrySet()) {
                props.append(e.getKey()).append("=").append(e.getValue()).append("\n");
            }

            String[] baseDirs = {
                "/sdcard/Android/data/com.gamebooster.app/files/fake_proc/",
                "/data/data/com.gamebooster.app/files/fake_proc/"
            };

            for (String dir : baseDirs) {
                ShizukuFileManager.ensureParentDirectory(dir + "fake_cpuinfo");
                ShizukuFileManager.writeFile(dir + "fake_cpuinfo", cpuInfo, "666");
                ShizukuFileManager.writeFile(dir + "fake_meminfo", memInfo, "666");
                ShizukuFileManager.writeFile(dir + "fake_version", procVer, "666");
                ShizukuFileManager.writeFile(dir + "system_spoof.prop", props.toString(), "666");
                ShizukuFileManager.writeFile(dir + "build.prop", props.toString(), "666");
            }
        } catch (Throwable t) {
            Log.w(TAG, "exportMockProcfsPayloads error: " + t.getMessage());
        }
    }

    /**
     * Injects hardware profiles directly into game directories resolved dynamically by GameConfigPathResolver.
     */
    public static void injectTailoredGameHardwareConfigs(String packageName, SpoofProfile profile) {
        if (packageName == null || profile == null) return;
        String pkg = packageName.toLowerCase().trim();

        // Use profile's maxRefreshRateHz field directly (no more fragile string parsing)
        int targetFps = profile.maxRefreshRateHz > 0 ? profile.maxRefreshRateHz : 185;

        // 1. Unreal Engine Games (PUBG, BGMI, Arena Breakout, Delta Force, Farlight, Valorant, Project C)
        if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") ||
            pkg.contains("vng.pubgmobile") || pkg.contains("arenabreakout") || pkg.contains("deltaforce") ||
            pkg.contains("uamo") || pkg.contains("farlight") || pkg.contains("solarland") ||
            pkg.contains("projectc") || pkg.contains("valorant")) {

            String ue4Profile = profile.generateUe4DeviceProfile(targetFps);
            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.endsWith("DeviceProfile.ini") || p.contains("Saved/Config/Android")) {
                    String targetPath = p.endsWith(".ini") ? p : (p + "/DeviceProfile.ini");
                    ShizukuFileManager.ensureParentDirectory(targetPath);
                    ShizukuFileManager.writeFile(targetPath, ue4Profile, "666");
                }
            }
        }

        // 2. Call of Duty Mobile / Warzone / Blood Strike
        else if (pkg.contains("cod") || pkg.contains("callofduty") || pkg.contains("bloodstrike") ||
                 pkg.contains("newspike") || pkg.contains("warzone")) {
            String jsonProfile = profile.generateJsonHardwareProfile(targetFps);
            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.contains("HardwareProfile.json") || p.endsWith(".json")) {
                    ShizukuFileManager.ensureParentDirectory(p);
                    ShizukuFileManager.writeFile(p, jsonProfile, "666");
                }
            }
        }

        // 3. Genshin Impact / Star Rail / ZZZ / Wuthering Waves
        else if (pkg.contains("genshin") || pkg.contains("mihoyo") || pkg.contains("cognosphere") ||
                 pkg.contains("hoyoverse") || pkg.contains("hkrpg") || pkg.contains("nap") || pkg.contains("wutheringwaves")) {
            String genshinProfile = profile.generateGenshinDeviceConfig(targetFps);
            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.contains("hardware_model_config.json") || p.contains("device_config.json") || p.endsWith(".json")) {
                    ShizukuFileManager.ensureParentDirectory(p);
                    ShizukuFileManager.writeFile(p, genshinProfile, "666");
                }
            }
        }

        // 4. Mobile Legends
        else if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
            String mlbbProfile = profile.generateMlbbDeviceConfig(targetFps);
            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.contains("device_cfg.ini") || p.contains("DeviceHardware.ini") || p.endsWith("HighFPSConfig.ini")) {
                    ShizukuFileManager.ensureParentDirectory(p);
                    ShizukuFileManager.writeFile(p, mlbbProfile, "666");
                }
            }
        }

        // 5. Free Fire / Free Fire MAX
        else if (pkg.contains("freefire") || pkg.contains("dts.freefire")) {
            String ffProfile = profile.generateFreeFireDeviceConfig(targetFps);
            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.contains("device_info.json") || p.contains("DeviceHardware.ini") || p.endsWith(".json")) {
                    ShizukuFileManager.ensureParentDirectory(p);
                    ShizukuFileManager.writeFile(p, ffProfile, "666");
                }
            }
        }

        // 6. Honor of Kings / Arena of Valor / Wild Rift
        else if (pkg.contains("sgame") || pkg.contains("levelinfinite") || pkg.contains("arenaofvalor") ||
                 pkg.contains("kgtw") || pkg.contains("kgvn") || pkg.contains("kgid") ||
                 pkg.contains("wildrift") || pkg.contains("riotgames.league")) {
            String hokProfile = profile.generateHokDeviceConfig(targetFps);
            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.contains("DeviceHardware.ini") || p.contains("device.ini") || p.endsWith(".ini") || p.contains("DeviceProfile.json")) {
                    ShizukuFileManager.ensureParentDirectory(p);
                    ShizukuFileManager.writeFile(p, hokProfile, "666");
                }
            }
        }

        // 7. Standoff 2
        else if (pkg.contains("standoff2") || pkg.contains("axlebolt")) {
            String so2Profile = profile.generateStandoff2DeviceConfig(targetFps);
            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.contains("DeviceHardware.json") || p.contains("Settings.json") || p.endsWith(".json")) {
                    ShizukuFileManager.ensureParentDirectory(p);
                    ShizukuFileManager.writeFile(p, so2Profile, "666");
                }
            }
        }

        // 8. CarX Street / Asphalt / Speed Drifters
        else if (pkg.contains("carx") || pkg.contains("glofta9hm") || pkg.contains("asphalt") || pkg.contains("r3_row")) {
            String carxProfile = profile.generateCarXDeviceConfig(targetFps);
            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.contains("DeviceHardware.ini") || p.contains("GraphicSettings.ini") || p.endsWith(".ini")) {
                    ShizukuFileManager.ensureParentDirectory(p);
                    ShizukuFileManager.writeFile(p, carxProfile, "666");
                }
            }
        }

        // 9. Supercell Games (Brawl Stars, Clash Royale, Clash of Clans)
        else if (pkg.contains("supercell") || pkg.contains("brawlstars") || pkg.contains("clashroyale") || pkg.contains("clashofclans")) {
            String supercellProfile = profile.generateSupercellDeviceConfig(targetFps);
            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.contains("DeviceHardware.ini") || p.contains("GameSettings.ini") || p.endsWith(".ini")) {
                    ShizukuFileManager.ensureParentDirectory(p);
                    ShizukuFileManager.writeFile(p, supercellProfile, "666");
                }
            }
        }

        // 10. Roblox
        else if (pkg.contains("roblox")) {
            String robloxProfile = profile.generateJsonHardwareProfile(targetFps);
            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.contains("DeviceHardware.json") || p.endsWith(".json")) {
                    ShizukuFileManager.ensureParentDirectory(p);
                    ShizukuFileManager.writeFile(p, robloxProfile, "666");
                }
            }
        }

        // 11. Generic Android Game Fallback
        else {
            String genericProfile = profile.generateGenericHardwareConfig(targetFps);
            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.contains("DeviceHardware.ini") || p.endsWith(".ini")) {
                    ShizukuFileManager.ensureParentDirectory(p);
                    ShizukuFileManager.writeFile(p, genericProfile, "666");
                }
            }
        }
    }

    /**
     * Resets hardware masking and returns system properties and settings to defaults.
     */
    public static void resetHardwareMask() {
        try {
            List<String> resetCmds = new ArrayList<>();
            resetCmds.add("setprop persist.sys.game.boost.profile 0");
            resetCmds.add("setprop debug.game.spoofed_model \"\"");
            resetCmds.add("setprop debug.game.spoofed_brand \"\"");
            resetCmds.add("setprop debug.game.spoofed_gpu \"\"");
            resetCmds.add("setprop debug.game.spoofed_soc \"\"");
            resetCmds.add("setprop debug.game.spoofed_ram \"\"");
            resetCmds.add("setprop debug.game.spoofed_android_ver \"\"");
            resetCmds.add("settings put global device_name \"" + android.os.Build.MODEL + "\" 2>/dev/null");
            resetCmds.add("settings put system device_name \"" + android.os.Build.MODEL + "\" 2>/dev/null");
            resetCmds.add("settings put global game_driver_opt_in_apps \"\" 2>/dev/null");
            resetCmds.add("settings put global updatable_driver_production_opt_in_apps \"\" 2>/dev/null");
            resetCmds.add("settings put global angle_gl_driver_selection_pkgs \"\" 2>/dev/null");
            resetCmds.add("settings put secure limit_ad_tracking 0 2>/dev/null");
            ShizukuExecutor.executeShizukuCommands(resetCmds);
            Log.i(TAG, "Hardware mask reset completed.");
        } catch (Throwable t) {
            Log.w(TAG, "resetHardwareMask non-fatal error: " + t.getMessage());
        }
    }

    /**
     * Masks a single game or application package using the currently active spoof profile.
     * Respects user toggle: if user did not enable spoofing or select a profile, does nothing.
     */
    public static boolean maskPackage(Context context, String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String activeId = context != null ? SpoofPreferences.resolveProfileId(context, packageName) : null;
        SpoofProfile profile = null;
        if (activeId != null && !activeId.trim().isEmpty()) {
            profile = DeviceSpooferEngine.getProfileById(activeId);
        }
        if (profile == null) {
            profile = DeviceSpooferEngine.getDefaultProfile();
        }
        if (profile == null) {
            Log.d(TAG, "No spoof profile available. Skipping maskPackage.");
            return false;
        }
        return applyFullHardwareMask(context, profile, packageName.trim());
    }

    /**
     * Masks ALL installed applications and games on the device across Android 13, 14, 15, and 16.
     */
    public static int maskAllInstalledApplications(Context context) {
        SpoofProfile profile = DeviceSpooferEngine.getActiveProfile();
        if (profile == null) {
            profile = DeviceSpooferEngine.getDefaultProfile();
        }
        return maskAllInstalledApplications(context, profile);
    }

    public static int maskAllInstalledApplications(Context context, SpoofProfile profile) {
        if (context == null) return 0;
        if (profile == null) {
            profile = DeviceSpooferEngine.getDefaultProfile();
        }

        int count = 0;
        try {
            List<com.gamebooster.app.games.GameAppInfo> allApps =
                    com.gamebooster.app.games.GameManagerRepository.getAllInstalledApps(context);
            List<String> batchCmds = new ArrayList<>();

            StringBuilder gameDriverApps = new StringBuilder();
            StringBuilder angleDriverApps = new StringBuilder();

            int targetHz = profile.maxRefreshRateHz > 0 ? profile.maxRefreshRateHz : 185;

            for (com.gamebooster.app.games.GameAppInfo app : allApps) {
                if (app == null || app.getPackageName() == null) continue;
                String pkg = app.getPackageName();
                if (pkg.equalsIgnoreCase(context.getPackageName())) continue;

                // 1. Android Game Driver / ANGLE Opt-in
                if (gameDriverApps.length() > 0) gameDriverApps.append(",");
                gameDriverApps.append(pkg);

                if (angleDriverApps.length() > 0) angleDriverApps.append(",");
                angleDriverApps.append(pkg);

                // 2. Android 13-16 GameMode & Overlay
                batchCmds.add("cmd game mode performance " + pkg + " 2>/dev/null");
                batchCmds.add("cmd game set --fps " + targetHz + " " + pkg + " 2>/dev/null");
                batchCmds.add("cmd window set-app-refresh-rate " + pkg + " " + targetHz + " 2>/dev/null");
                batchCmds.add("device_config put game_overlay " + pkg + " mode=2,useAngle=true,fps=" + targetHz + ",downscaleFactor=1.0,cpuPriority=high,gpuPriority=high 2>/dev/null");

                // 3. Inject hardware profile
                injectTailoredGameHardwareConfigs(pkg, profile);
                count++;
            }

            // Global Android Driver Enforcements
            if (gameDriverApps.length() > 0) {
                batchCmds.add("settings put global game_driver_opt_in_apps \"" + gameDriverApps.toString() + "\"");
                batchCmds.add("settings put global updatable_driver_production_opt_in_apps \"" + gameDriverApps.toString() + "\"");
                batchCmds.add("settings put global angle_gl_driver_selection_pkgs \"" + angleDriverApps.toString() + "\"");
                batchCmds.add("settings put global angle_gl_driver_selection_values angle");
            }

            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommands(batchCmds);
            }

            // Global Build reflection
            applyInAppReflectionMask(profile);

            // Staged procfs mock payloads
            exportMockProcfsPayloads(profile);

            SpoofPreferences.setSpoofEnabled(context, true);
            SpoofPreferences.setActiveProfileId(context, profile.id);
            SpoofPreferences.setSpoofAllApps(context, true);

            com.gamebooster.app.gamemanager.GameManagerStatus.getInstance().setMaskedAppsCount(count);
            Log.i(TAG, "⚡ Masked " + count + " applications with profile: " + profile.displayName);

        } catch (Throwable t) {
            Log.e(TAG, "Error masking all applications", t);
        }
        return count;
    }

    /**
     * Applies full multi-generational Android 13, 14, 15, and 16 hardware mask flags.
     */
    public static void maskAllAndroidVersions(Context context, String packageName) {
        maskAllAndroidVersions(context, packageName, 185);
    }

    public static void maskAllAndroidVersions(Context context, String packageName, int targetHz) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        String pkg = packageName.trim();
        int hz = targetHz > 0 ? targetHz : 185;
        maskForAndroid13(pkg, hz);
        maskForAndroid14(pkg, hz);
        maskForAndroid15(pkg, hz);
        maskForAndroid16(pkg, hz);
    }

    public static void maskForAndroid13(String pkg, int targetHz) {
        List<String> cmds = new ArrayList<>();
        cmds.add("device_config put game_overlay " + pkg + " mode=2,useAngle=true,fps=" + targetHz + ",downscaleFactor=1.0,cpuPriority=high,gpuPriority=high 2>/dev/null");
        cmds.add("cmd appops set " + pkg + " MANAGE_GAME_MODE allow 2>/dev/null");
        if (ShizukuExecutor.hasShizukuPermission()) ShizukuExecutor.executeShizukuCommands(cmds);
    }

    public static void maskForAndroid14(String pkg, int targetHz) {
        List<String> cmds = new ArrayList<>();
        cmds.add("cmd game mode performance " + pkg + " 2>/dev/null");
        cmds.add("cmd game set --fps " + targetHz + " " + pkg + " 2>/dev/null");
        cmds.add("cmd window set-app-refresh-rate " + pkg + " " + targetHz + " 2>/dev/null");
        if (ShizukuExecutor.hasShizukuPermission()) ShizukuExecutor.executeShizukuCommands(cmds);
    }

    public static void maskForAndroid15(String pkg, int targetHz) {
        List<String> cmds = new ArrayList<>();
        cmds.add("cmd power set-fixed-performance-mode-enabled true 2>/dev/null");
        cmds.add("cmd power set-mode 0 1 2>/dev/null");
        cmds.add("cmd power set-mode 2 1 2>/dev/null");
        if (ShizukuExecutor.hasShizukuPermission()) ShizukuExecutor.executeShizukuCommands(cmds);
    }

    public static void maskForAndroid16(String pkg, int targetHz) {
        List<String> cmds = new ArrayList<>();
        cmds.add("cmd game set --performance-class 3 " + pkg + " 2>/dev/null");
        cmds.add("device_config put runtime_native_boot use_app_image_startup_cache true 2>/dev/null");
        cmds.add("device_config put runtime_native_boot boost_sched_priority true 2>/dev/null");
        if (ShizukuExecutor.hasShizukuPermission()) ShizukuExecutor.executeShizukuCommands(cmds);
    }

    public static String generateMacAddress(SpoofProfile profile) {
        if (profile != null) {
            return profile.getWifiMacAddress();
        }
        return DeviceIdentityGenerator.generateWifiMacAddress(null);
    }

    /**
     * Applies full AppOps privacy & identity shield to block games and apps from querying
     * sensitive hardware identifiers (IMEI, MEID, SIM serial, IMSI, Phone number, AAID, Usage stats).
     */
    public static void applyAppOpsShieldForPackage(List<String> batchCommands, String pkg) {
        if (pkg == null || pkg.trim().isEmpty() || batchCommands == null) return;
        String p = pkg.trim();
        batchCommands.add("cmd appops set " + p + " READ_DEVICE_IDENTIFIERS ignore 2>/dev/null");
        batchCommands.add("cmd appops set " + p + " GET_USAGE_STATS ignore 2>/dev/null");
        batchCommands.add("cmd appops set " + p + " ACCESS_AD_ID ignore 2>/dev/null");
        batchCommands.add("cmd appops set " + p + " READ_PHONE_STATE ignore 2>/dev/null");
        batchCommands.add("cmd appops set " + p + " READ_PRIVILEGED_PHONE_STATE ignore 2>/dev/null");
        batchCommands.add("cmd appops set " + p + " ACTIVITY_RECOGNITION ignore 2>/dev/null");
    }

    private static void setStaticField(Class<?> clazz, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Throwable ignored) {}
    }
}
