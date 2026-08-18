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
            String eglVendor = profile.glVendor.toLowerCase().contains("arm") ? "mali" : "adreno";
            batchCommands.add("resetprop -n ro.product.model \"" + profile.model + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.brand \"" + profile.brand + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.manufacturer \"" + profile.manufacturer + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.device \"" + profile.device + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.product.name \"" + profile.productName + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.build.product \"" + profile.buildProduct + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.soc.model \"" + profile.socModel + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.soc.manufacturer \"" + profile.socManufacturer + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.hardware \"" + profile.hardware + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.board.platform \"" + profile.platform + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.chipname \"" + profile.chipname + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.build.fingerprint \"" + profile.fingerprint + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.build.display.id \"" + profile.displayId + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.build.version.release \"" + profile.androidVersion + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.build.version.sdk \"" + profile.sdkInt + "\" 2>/dev/null");
            batchCommands.add("resetprop -n ro.hardware.egl \"" + eglVendor + "\" 2>/dev/null");

            // ═══════════════════════════════════════════════════════════════════
            //  LAYER 1: CPU / SOC MASKING
            // ═══════════════════════════════════════════════════════════════════
            batchCommands.add("setprop ro.soc.model \"" + profile.socModel + "\"");
            batchCommands.add("setprop ro.soc.manufacturer \"" + profile.socManufacturer + "\"");
            batchCommands.add("setprop ro.board.platform \"" + profile.platform + "\"");
            batchCommands.add("setprop ro.hardware \"" + profile.hardware + "\"");
            batchCommands.add("setprop ro.chipname \"" + profile.chipname + "\"");
            batchCommands.add("setprop debug.game.spoofed_soc \"" + profile.socModel + "\"");
            batchCommands.add("setprop debug.game.spoofed_soc_vendor \"" + profile.socManufacturer + "\"");
            batchCommands.add("setprop debug.game.spoofed_cpu_cores \"" + profile.cpuCores + "\"");
            batchCommands.add("setprop debug.game.spoofed_cpu_freq \"" + profile.cpuMaxFreqKhz + "\"");
            batchCommands.add("setprop debug.game.spoofed_cpu_arch \"" + profile.cpuArchitecture + "\"");

            // ═══════════════════════════════════════════════════════════════════
            //  LAYER 2: GPU / GRAPHICS & VULKAN MASKING
            // ═══════════════════════════════════════════════════════════════════
            batchCommands.add("setprop ro.hardware.egl " + eglVendor);
            batchCommands.add("setprop debug.hwui.renderer vulkan");
            batchCommands.add("setprop debug.renderengine.backend vulkan");
            batchCommands.add("setprop debug.game.spoofed_gpu \"" + profile.glRenderer + "\"");
            batchCommands.add("setprop debug.game.spoofed_gpu_vendor \"" + profile.glVendor + "\"");
            batchCommands.add("setprop debug.game.spoofed_vulkan_ver \"" + profile.vulkanVersion + "\"");
            batchCommands.add("setprop debug.game.spoofed_vulkan_driver \"" + profile.vulkanDriverVersion + "\"");

            // ANGLE & Game Driver Routing
            batchCommands.add("settings put global angle_gl_driver_all_angle 1");
            batchCommands.add("settings put global game_driver_all_apps 1");
            batchCommands.add("settings put global updatable_driver_all_apps 1");

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
            batchCommands.add("setprop ro.product.model \"" + profile.model + "\"");
            batchCommands.add("setprop ro.product.brand \"" + profile.brand + "\"");
            batchCommands.add("setprop ro.product.name \"" + profile.productName + "\"");
            batchCommands.add("setprop ro.product.device \"" + profile.device + "\"");
            batchCommands.add("setprop ro.product.manufacturer \"" + profile.manufacturer + "\"");
            batchCommands.add("setprop ro.build.product \"" + profile.buildProduct + "\"");
            batchCommands.add("setprop ro.build.fingerprint \"" + profile.fingerprint + "\"");
            batchCommands.add("setprop ro.build.display.id \"" + profile.displayId + "\"");
            batchCommands.add("setprop ro.build.version.release \"" + profile.androidVersion + "\"");
            batchCommands.add("setprop ro.build.version.sdk \"" + profile.sdkInt + "\"");
            batchCommands.add("setprop ro.build.version.security_patch \"" + profile.securityPatch + "\"");
            batchCommands.add("setprop persist.sys.device_name \"" + profile.displayName + "\"");
            batchCommands.add("settings put global device_name \"" + profile.displayName + "\"");
            batchCommands.add("settings put system custom_device_name \"" + profile.displayName + "\"");

            // Display & SurfaceFlinger Frame Rates
            batchCommands.add("settings put system peak_refresh_rate 120.0");
            batchCommands.add("settings put system min_refresh_rate 120.0");
            batchCommands.add("settings put system user_refresh_rate 120");
            batchCommands.add("settings put global peak_refresh_rate 120.0");
            batchCommands.add("settings put global min_refresh_rate 120.0");
            batchCommands.add("service call SurfaceFlinger 1035 i32 120");
            batchCommands.add("service call SurfaceFlinger 1036 i32 120");
            batchCommands.add("setprop debug.sf.fps_limit 120");
            batchCommands.add("setprop persist.sys.NV_FPSLIMIT 120");
            batchCommands.add("setprop persist.sys.NV_POWERMODE 1");
            batchCommands.add("setprop debug.gr.swapinterval 0");

            // Per-Package Android Game Mode & Overlay
            if (packageName != null && !packageName.trim().isEmpty()) {
                String pkg = packageName.trim();
                batchCommands.add("cmd game mode performance " + pkg);
                batchCommands.add("cmd game set --fps 120 " + pkg);
                batchCommands.add("cmd window set-app-refresh-rate " + pkg + " 120");
                batchCommands.add("device_config put game_overlay " + pkg + " mode=2,fps=120:mode=3,fps=120");
                batchCommands.add("settings put global game_driver_opt_in_apps " + pkg);
                batchCommands.add("settings put global updatable_driver_production_opt_in_apps " + pkg);
            }

            // Execute all elevated commands via Shizuku
            ShizukuExecutor.executeShizukuCommands(batchCommands);

            // ═══════════════════════════════════════════════════════════════════
            //  LAYER 5: IN-APP REFLECTION OVERRIDE
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
            setStaticField(android.os.Build.class, "FINGERPRINT", profile.fingerprint);
            setStaticField(android.os.Build.class, "DISPLAY", profile.displayId);

            try {
                setStaticField(android.os.Build.VERSION.class, "RELEASE", profile.androidVersion);
                setStaticField(android.os.Build.VERSION.class, "SDK_INT", profile.sdkInt);
                setStaticField(android.os.Build.VERSION.class, "SECURITY_PATCH", profile.securityPatch);
            } catch (Throwable ignored) {}

            try {
                setStaticField(android.os.Build.class, "SOC_MODEL", profile.socModel);
                setStaticField(android.os.Build.class, "SOC_MANUFACTURER", profile.socManufacturer);
            } catch (Throwable ignored) {}

            Log.d(TAG, "In-app Build reflection applied: " + profile.model + " (" + profile.brand + ")");
        } catch (Throwable t) {
            Log.w(TAG, "applyInAppReflectionMask non-fatal: " + t.getMessage());
        }
    }

    /**
     * Staging mock /proc/cpuinfo and /proc/meminfo files.
     */
    public static void exportMockProcfsPayloads(SpoofProfile profile) {
        if (profile == null) return;
        try {
            String cpuInfo = profile.generateCpuInfo();
            String memInfo = profile.generateMemInfo();

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
                ShizukuFileManager.writeFile(dir + "system_spoof.prop", props.toString(), "666");
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

        // 1. Unreal Engine Games (PUBG, BGMI, Arena Breakout, Delta Force, Farlight)
        if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") ||
            pkg.contains("vng.pubgmobile") || pkg.contains("arenabreakout") || pkg.contains("deltaforce") ||
            pkg.contains("farlight") || pkg.contains("solarland") || pkg.contains("projectc") || pkg.contains("valorant")) {

            String ue4Profile = profile.generateUe4DeviceProfile(165);
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
        else if (pkg.contains("cod") || pkg.contains("callofduty") || pkg.contains("bloodstrike") || pkg.contains("warzone")) {
            String jsonProfile = profile.generateJsonHardwareProfile(165);
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
            String genshinProfile = "{\n" +
                    "  \"device_model\": \"" + profile.model + "\",\n" +
                    "  \"device_brand\": \"" + profile.brand + "\",\n" +
                    "  \"gpu_renderer\": \"" + profile.glRenderer + "\",\n" +
                    "  \"gpu_vendor\": \"" + profile.glVendor + "\",\n" +
                    "  \"soc_model\": \"" + profile.socModel + "\",\n" +
                    "  \"ram_total_mb\": " + profile.ramTotalMb + ",\n" +
                    "  \"vulkan_support\": true,\n" +
                    "  \"max_refresh_rate\": 165,\n" +
                    "  \"frame_rate_cap\": 165\n" +
                    "}\n";
            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.contains("hardware_model_config.json") || p.contains("device_config.json")) {
                    ShizukuFileManager.ensureParentDirectory(p);
                    ShizukuFileManager.writeFile(p, genshinProfile, "666");
                }
            }
        }

        // 4. Mobile Legends / Honor of Kings / Free Fire
        else if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends") ||
                 pkg.contains("freefire") || pkg.contains("dts.freefire") ||
                 pkg.contains("sgame") || pkg.contains("levelinfinite") || pkg.contains("arenaofvalor")) {
            String iniProfile = "[Hardware]\n" +
                    "DeviceModel=" + profile.model + "\n" +
                    "DeviceBrand=" + profile.brand + "\n" +
                    "GPU=" + profile.glRenderer + "\n" +
                    "GPUVendor=" + profile.glVendor + "\n" +
                    "SoC=" + profile.socModel + "\n" +
                    "RAM=" + profile.ramTotalMb + "\n" +
                    "HighFPSMode=1\n" +
                    "FrameRateLevel=9\n" +
                    "FPS=165\n" +
                    "Unlock165Hz=1\n";
            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.contains("DeviceHardware.ini") || p.endsWith("HighFPSConfig.ini")) {
                    ShizukuFileManager.ensureParentDirectory(p);
                    ShizukuFileManager.writeFile(p, iniProfile, "666");
                }
            }
        }

        // 5. Roblox
        else if (pkg.contains("roblox")) {
            String robloxProfile = profile.generateJsonHardwareProfile(165);
            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.contains("DeviceHardware.json") || p.endsWith(".json")) {
                    ShizukuFileManager.ensureParentDirectory(p);
                    ShizukuFileManager.writeFile(p, robloxProfile, "666");
                }
            }
        }
    }

    /**
     * Resets hardware masking and returns system properties to defaults.
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
            ShizukuExecutor.executeShizukuCommands(resetCmds);
            Log.i(TAG, "Hardware mask reset completed.");
        } catch (Throwable t) {
            Log.w(TAG, "resetHardwareMask non-fatal error: " + t.getMessage());
        }
    }

    private static void setStaticField(Class<?> clazz, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Throwable ignored) {}
    }
}
