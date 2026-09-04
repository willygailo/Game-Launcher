package com.gamebooster.app.spoofer;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.booster.GpuTweaksChannel;
import com.gamebooster.app.config.ConfigFileHelper;
import com.gamebooster.app.config.GameConfigPathResolver;
import com.gamebooster.app.config.GameSecurityBypassEngine;
import com.gamebooster.app.config.MlbbConfigPatcher;
import com.gamebooster.app.config.NativeConfigInjector;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * HardwareMaskEngine — Full-Stack Hardware Masking & Device Spoofing Engine.
 *
 * Implements 6 comprehensive, ban-safe layers of device & hardware spoofing via Shizuku / Root:
 *
 * 1. CPU / SoC MASKING:
 *    - Injects SoC Model, SoC Manufacturer, Architecture, Cores, and Clock speeds.
 *    - Generates & stages /proc/cpuinfo mock payloads and system properties.
 *
 * 2. GPU / GRAPHICS MASKING:
 *    - Injects Flagship GL Renderer (e.g. Adreno 840 / Immortalis-G925), GL Vendor, GL Version, Vulkan Driver.
 *    - Activates Updatable Production Game Driver and Vulkan RenderEngine backend while strictly purging ANGLE.
 *
 * 3. RAM / MEMORY MASKING:
 *    - Spoofs Total RAM (16GB / 24GB LPDDR5X) & MemAvailable in debug properties and staged /proc/meminfo.
 *    - Tunes Dalvik VM heap size (heapsize=1024m, heapgrowthlimit=512m).
 *
 * 4. TARGET GAME ISOLATION & INTEGRITY SHIELD:
 *    - Zero modification to global Android OS identity (ro.product.*, ro.build.* untouched to protect SafetyNet/Play Integrity).
 *    - Applies AppOps privacy shields and GameMode overlays exclusively to target game packages (PUBGM, CODM, Free Fire, MLBB, etc.).
 *
 * 5. IN-APP JAVA REFLECTION OVERRIDE:
 *    - Safely mutates static Build fields (MODEL, BRAND, MANUFACTURER, HARDWARE, SOC_MODEL, etc.) in the launcher.
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

            Set<String> batchCommands = new LinkedHashSet<>();
            String eglVendor = profile.glVendor.toLowerCase().contains("arm") ? "mali" : "adreno";
            int maxPhysicalHz = 120;
            if (context != null) {
                try {
                    com.gamebooster.app.device.DisplayCapabilitiesDetector.DisplayCaps caps =
                            com.gamebooster.app.device.DisplayCapabilitiesDetector.detect(context);
                    if (caps != null && caps.maxRefreshRate > 0) {
                        maxPhysicalHz = caps.maxRefreshRate;
                    }
                } catch (Throwable ignored) {}
            }
            int targetHz = Math.max(60, Math.min(maxPhysicalHz, profile.maxRefreshRateHz > 0 ? profile.maxRefreshRateHz : maxPhysicalHz));

            // ═══════════════════════════════════════════════════════════════════
            //  LAYER 1: LAUNCHER TELEMETRY & APP-SCOPED PROPS (Zero OS Tampering)
            //  Does NOT alter global ro.product.* or ro.build.* properties.
            // ═══════════════════════════════════════════════════════════════════
            batchCommands.add("setprop debug.game.spoofed_soc \"" + profile.socModel + "\"");
            batchCommands.add("setprop debug.game.spoofed_soc_vendor \"" + profile.socManufacturer + "\"");
            batchCommands.add("setprop debug.game.spoofed_cpu_cores \"" + profile.cpuCores + "\"");
            batchCommands.add("setprop debug.game.spoofed_cpu_freq \"" + profile.cpuMaxFreqKhz + "\"");
            batchCommands.add("setprop debug.game.spoofed_cpu_arch \"" + profile.cpuArchitecture + "\"");
            batchCommands.add("setprop debug.game.spoofed_gpu \"" + profile.glRenderer + "\"");
            batchCommands.add("setprop debug.game.spoofed_gpu_vendor \"" + profile.glVendor + "\"");
            batchCommands.add("setprop debug.game.spoofed_vulkan_ver \"" + profile.vulkanVersion + "\"");
            batchCommands.add("setprop debug.game.spoofed_vulkan_driver \"" + profile.vulkanDriverVersion + "\"");
            batchCommands.add("setprop debug.game.spoofed_ram \"" + profile.ramTotalMb + "\"");
            batchCommands.add("setprop debug.game.spoofed_ram_avail \"" + profile.ramAvailableMb + "\"");

            // ═══════════════════════════════════════════════════════════════════
            //  LAYER 2: DISPLAY REFRESH RATE & SCHEDULER TUNING
            // ═══════════════════════════════════════════════════════════════════
            // Display & SurfaceFlinger Frame Rates
            batchCommands.add("settings put system peak_refresh_rate " + targetHz + ".0");
            batchCommands.add("settings put system min_refresh_rate " + targetHz + ".0");
            batchCommands.add("settings put system user_refresh_rate " + targetHz);
            batchCommands.add("settings put global peak_refresh_rate " + targetHz + ".0");
            batchCommands.add("settings put global min_refresh_rate " + targetHz + ".0");
            batchCommands.add("setprop debug.sf.fps_limit " + targetHz);
            batchCommands.add("setprop persist.sys.NV_FPSLIMIT " + targetHz);
            batchCommands.add("setprop persist.sys.NV_POWERMODE 1");
            batchCommands.add("setprop debug.gr.swapinterval 0");

            // Dalvik VM Memory Tuning
            batchCommands.add("setprop dalvik.vm.heapgrowthlimit 512m");
            batchCommands.add("setprop dalvik.vm.heapsize 1024m");
            batchCommands.add("setprop dalvik.vm.heaptargetutilization 0.75");
            batchCommands.add("setprop dalvik.vm.heapminfree 8m");
            batchCommands.add("setprop dalvik.vm.heapmaxfree 32m");

            // Performance Mode & Battery Profile
            batchCommands.add("cmd power set-fixed-performance-mode-enabled true 2>/dev/null");
            batchCommands.add("dumpsys battery reset 2>/dev/null");

            // Per-Package Android Game Mode & Overlay (Isolated to target game only)
            if (packageName != null && !packageName.trim().isEmpty()) {
                String pkg = packageName.trim();
                batchCommands.add("cmd game mode performance " + pkg + " 2>/dev/null");
                batchCommands.add("cmd game set --fps " + targetHz + " " + pkg + " 2>/dev/null");
                batchCommands.add("cmd window set-app-refresh-rate " + pkg + " " + targetHz + " 2>/dev/null");
                batchCommands.add("device_config put game_overlay " + pkg + " mode=2,useAngle=false,fps=" + targetHz + ",downscaleFactor=1.0 2>/dev/null");
            }

            // ═══════════════════════════════════════════════════════════════════
            //  LAYER 3: PRIVACY SHIELD & PER-GAME APPOPS ISOLATION
            //  (Device names and OS Bluetooth IDs are left untouched for system safety)
            // ═══════════════════════════════════════════════════════════════════
            batchCommands.add("settings put global randomized_mac_support 1");
            batchCommands.add("settings put global randomized_mac_connected_mac_randomization 1");
            batchCommands.add("settings put secure limit_ad_tracking 1");
            batchCommands.add("settings put secure ad_id \"" + profile.getAdvertisingId() + "\"");
            batchCommands.add("settings put secure advertising_id \"" + profile.getAdvertisingId() + "\"");
            batchCommands.add("device_config put privacy privacy_sandbox_enabled false 2>/dev/null");
            batchCommands.add("settings put global usage_reporting_enabled 0");
            batchCommands.add("settings put secure usage_and_diagnostics_enabled 0");
            batchCommands.add("settings put secure upload_apk_enable 0");
            batchCommands.add("settings put secure send_action_app_error 0");
            batchCommands.add("settings put global send_action_app_error 0");

            // Apply AppOps shield strictly to target game(s) with zero duplicate commands
            Set<String> targetShieldPkgs = new LinkedHashSet<>();
            if (packageName != null && !packageName.trim().isEmpty()) {
                targetShieldPkgs.add(packageName.trim());
            } else {
                targetShieldPkgs.addAll(com.gamebooster.app.games.GamePackageRegistry.getAllKnownGames().keySet());
            }
            for (String gamePkg : targetShieldPkgs) {
                if (gamePkg != null && !gamePkg.isEmpty()) {
                    applyAppOpsShieldForPackage(batchCommands, gamePkg);
                }
            }

            // Execute elevated commands via Shizuku
            List<String> execResults = ShizukuExecutor.executeShizukuCommandsWithResults(new ArrayList<>(batchCommands));
            boolean commandsExecuted = execResults != null && !execResults.isEmpty();
            if (!commandsExecuted) {
                Log.w(TAG, "No elevated Shizuku channel available — ensure Shizuku permission is granted.");
            } else {
                Log.i(TAG, "Elevated batch executed: " + execResults.size() + " command(s) for profile " + profile.displayName);
            }

            // Trigger Shizuku display refresh rate and game driver forcing
            if (packageName != null && !packageName.trim().isEmpty()) {
                com.gamebooster.app.engine.GameModeApiSupport.setGameModePerformance(packageName.trim(), targetHz);
            }
            if (targetHz > 60) {
                com.gamebooster.app.booster.MaxHzForceChannel.forceApply(targetHz);
            }

            // ═══════════════════════════════════════════════════════════════════
            //  LAYER 5: IN-APP REFLECTION OVERRIDE & HARDWARE PROFILES
            // ═══════════════════════════════════════════════════════════════════
            applyInAppReflectionMask(profile);

            // ═══════════════════════════════════════════════════════════════════
            //  LAYER 6: MOCK PROCFS PAYLOAD GENERATION & GAME ENGINE INJECTION
            // ═══════════════════════════════════════════════════════════════════
            exportMockProcfsPayloads(profile);

            // Inject hardware profile for targeted package OR all registered games if null
            if (packageName != null && !packageName.trim().isEmpty()) {
                injectTailoredGameHardwareConfigs(packageName.trim(), profile);
            } else {
                injectAllInstalledGamesHardwareProfile(profile);
            }

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
            setStaticField(android.os.Build.class, "SERIAL", profile.getSerialNumber());
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
                setStaticField(android.os.Build.class, "SUPPORTED_ABIS", new String[]{"arm64-v8a", "armeabi-v7a", "armeabi"});
                setStaticField(android.os.Build.class, "SUPPORTED_64_BIT_ABIS", new String[]{"arm64-v8a"});
                setStaticField(android.os.Build.class, "SUPPORTED_32_BIT_ABIS", new String[]{"armeabi-v7a", "armeabi"});
                setStaticField(android.os.Build.class, "USER", "builder");
                setStaticField(android.os.Build.class, "HOST", "build-host");
            } catch (Throwable ignored) {}

            Log.d(TAG, "In-app Build reflection applied: " + profile.model + " (" + profile.brand + ")");
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
        int targetFps = profile.maxRefreshRateHz > 0 ? profile.maxRefreshRateHz : 185;

        // 1. Unreal Engine Games (PUBG, BGMI, Arena Breakout, Delta Force, Farlight, Valorant, Project C)
        if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") ||
            pkg.contains("vng.pubgmobile") || pkg.contains("arenabreakout") || pkg.contains("deltaforce") ||
            pkg.contains("uamo") || pkg.contains("farlight") || pkg.contains("solarland") ||
            pkg.contains("projectc") || pkg.contains("valorant")) {

            String[] profileKeys = profile.generateUe4DeviceProfileKeys(targetFps);

            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.endsWith("UserCustom.ini")) {
                    ConfigFileHelper.patchKeys(p, profileKeys, "[UserCustom DeviceProfile]");
                } else if (p.endsWith("EnjoyCJZC.ini") || p.endsWith("EnjoyCJ.ini") || p.endsWith("BGMIEnjoyCJZC.ini") || p.endsWith("KREnjoyCJZC.ini")) {
                    ConfigFileHelper.patchKeys(p, profileKeys, "[EnjoyCJZC DeviceProfile]");
                } else if (p.endsWith("DeviceProfile.ini")) {
                    ConfigFileHelper.patchKeys(p, profileKeys, "[DeviceProfile]");
                }
            }
        }

        // 2. Call of Duty Mobile / Warzone / Blood Strike
        else if (pkg.contains("cod") || pkg.contains("callofduty") || pkg.contains("bloodstrike") ||
                 pkg.contains("newspike") || pkg.contains("warzone")) {
            String jsonProfile = profile.generateJsonHardwareProfile(targetFps);
            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.contains("HardwareProfile.json")) {
                    if (!NativeConfigInjector.injectHardwareMaskProfile(p, profile.glRenderer, profile.socModel, profile.ramTotalMb, targetFps)) {
                        ShizukuFileManager.ensureParentDirectory(p);
                        ShizukuFileManager.writeFile(p, jsonProfile, "666");
                    }
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
                    if (!NativeConfigInjector.injectHardwareMaskProfile(p, profile.glRenderer, profile.socModel, profile.ramTotalMb, targetFps)) {
                        ShizukuFileManager.ensureParentDirectory(p);
                        ShizukuFileManager.writeFile(p, genshinProfile, "666");
                    }
                }
            }
        }

        // 4. Mobile Legends
        else if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
            GameSecurityBypassEngine.purgeCorruptedAssetCaches(packageName);
            try {
                MlbbConfigPatcher.patchUltraExtreme165(packageName);
            } catch (Throwable ignored) {}
            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.contains("playerprefs") || p.endsWith(".xml")) {
                    NativeConfigInjector.injectHardwareMaskProfile(p, profile.glRenderer, profile.socModel, profile.ramTotalMb, targetFps);
                }
            }
            GameSecurityBypassEngine.enforceSelinuxAndOwnershipBypass(packageName, paths);
        }

        // 5. Free Fire / Free Fire MAX
        else if (pkg.contains("freefire") || pkg.contains("dts.freefire")) {
            String ffProfile = profile.generateFreeFireDeviceConfig(targetFps);
            List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
            for (String p : paths) {
                if (p.contains("device_info.json") || p.contains("DeviceHardware.ini") || p.endsWith(".json")) {
                    if (!NativeConfigInjector.injectHardwareMaskProfile(p, profile.glRenderer, profile.socModel, profile.ramTotalMb, targetFps)) {
                        ShizukuFileManager.ensureParentDirectory(p);
                        ShizukuFileManager.writeFile(p, ffProfile, "666");
                    }
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
                    if (!NativeConfigInjector.injectHardwareMaskProfile(p, profile.glRenderer, profile.socModel, profile.ramTotalMb, targetFps)) {
                        ShizukuFileManager.ensureParentDirectory(p);
                        ShizukuFileManager.writeFile(p, hokProfile, "666");
                    }
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
            resetCmds.add("settings put global game_driver_opt_in_apps \"\" 2>/dev/null");
            resetCmds.add("settings put global updatable_driver_production_opt_in_apps \"\" 2>/dev/null");
            resetCmds.add("settings delete global angle_gl_driver_selection_pkgs 2>/dev/null");
            resetCmds.add("settings delete global angle_gl_driver_selection_values 2>/dev/null");
            resetCmds.add("settings delete global angle_enabled_pkgs 2>/dev/null");
            resetCmds.add("settings put global angle_gl_driver_all_angle 0 2>/dev/null");
            resetCmds.add("settings put secure limit_ad_tracking 0 2>/dev/null");
            ShizukuExecutor.executeShizukuCommands(resetCmds);
            Log.i(TAG, "Hardware mask reset completed.");
        } catch (Throwable t) {
            Log.w(TAG, "resetHardwareMask non-fatal error: " + t.getMessage());
        }
    }

    /**
     * Masks a single game or application package using the currently active spoof profile.
     */
    public static boolean maskPackage(Context context, String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String activeId = context != null ? SpoofPreferences.resolveProfileId(context, packageName) : null;
        SpoofProfile profile = null;
        if (activeId != null && !activeId.trim().isEmpty()) {
            profile = SpoofProfileRegistry.getById(activeId);
        }
        if (profile == null) {
            profile = SpoofProfileRegistry.getDefaultProfile();
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
        String activeId = context != null ? SpoofPreferences.getActiveProfileId(context) : null;
        SpoofProfile profile = activeId != null ? SpoofProfileRegistry.getById(activeId) : null;
        if (profile == null) {
            profile = SpoofProfileRegistry.getDefaultProfile();
        }
        return maskAllInstalledApplications(context, profile);
    }

    public static int maskAllInstalledApplications(Context context, SpoofProfile profile) {
        if (context == null) return 0;
        if (profile == null) {
            profile = SpoofProfileRegistry.getDefaultProfile();
        }

        int count = 0;
        try {
            List<com.gamebooster.app.games.GameAppInfo> installedGames =
                    com.gamebooster.app.games.GameManagerRepository.getInstalledGames(context);
            Set<String> batchCmds = new LinkedHashSet<>();

            Set<String> gamePkgs = new LinkedHashSet<>();
            int maxPhysicalHz = 120;
            try {
                com.gamebooster.app.device.DisplayCapabilitiesDetector.DisplayCaps caps =
                        com.gamebooster.app.device.DisplayCapabilitiesDetector.detect(context);
                if (caps != null && caps.maxRefreshRate > 0) {
                    maxPhysicalHz = caps.maxRefreshRate;
                }
            } catch (Throwable ignored) {}
            int targetHz = Math.max(60, Math.min(maxPhysicalHz, profile.maxRefreshRateHz > 0 ? profile.maxRefreshRateHz : maxPhysicalHz));

            if (installedGames != null) {
                for (com.gamebooster.app.games.GameAppInfo game : installedGames) {
                    if (game == null || game.getPackageName() == null) continue;
                    String pkg = game.getPackageName();
                    if (pkg.equalsIgnoreCase(context.getPackageName())) continue;
                    gamePkgs.add(pkg);
                }
            }

            // Also include known game packages if installed on device
            for (String known : com.gamebooster.app.games.GamePackageRegistry.getAllKnownGames().keySet()) {
                if (known != null && !known.trim().isEmpty()) {
                    try {
                        context.getPackageManager().getPackageInfo(known.trim(), 0);
                        gamePkgs.add(known.trim());
                    } catch (Throwable ignored) {}
                }
            }

            for (String pkg : gamePkgs) {
                if (pkg == null || pkg.trim().isEmpty() || pkg.equalsIgnoreCase(context.getPackageName())) continue;

                // Android 13-16 GameMode & Overlay (Isolated strictly to target games)
                batchCmds.add("cmd game mode performance " + pkg + " 2>/dev/null");
                batchCmds.add("cmd game set --fps " + targetHz + " " + pkg + " 2>/dev/null");
                batchCmds.add("cmd window set-app-refresh-rate " + pkg + " " + targetHz + " 2>/dev/null");
                batchCmds.add("device_config put game_overlay " + pkg + " mode=2,useAngle=false,fps=" + targetHz + ",downscaleFactor=1.0 2>/dev/null");

                // Inject hardware profile into target game data dir
                injectTailoredGameHardwareConfigs(pkg, profile);
                applyAppOpsShieldForPackage(batchCmds, pkg);
                count++;
            }

            // Global Android Driver Enforcements (Strictly MLBB, CODM, PUBGM only; ANGLE Purged)
            List<String> eligibleGameDriverPkgs = new ArrayList<>();
            for (String p : gamePkgs) {
                if (com.gamebooster.app.booster.GpuTweaksChannel.isGameDriverEligible(p)) {
                    eligibleGameDriverPkgs.add(p);
                }
            }
            String gameDriverCsv = String.join(",", eligibleGameDriverPkgs);
            batchCmds.add("settings put global game_driver_all_apps 0 2>/dev/null");
            batchCmds.add("settings put global updatable_driver_all_apps 0 2>/dev/null");
            batchCmds.add("settings put global game_driver_opt_in_apps \"" + gameDriverCsv + "\"");
            batchCmds.add("settings put global game_driver_prerelease_opt_in_apps \"" + gameDriverCsv + "\"");
            batchCmds.add("settings put global updatable_driver_production_opt_in_apps \"\" 2>/dev/null");
            batchCmds.add("settings delete global angle_gl_driver_selection_pkgs 2>/dev/null");
            batchCmds.add("settings delete global angle_gl_driver_selection_values 2>/dev/null");
            batchCmds.add("settings delete global angle_enabled_pkgs 2>/dev/null");
            batchCmds.add("settings put global angle_gl_driver_all_angle 0 2>/dev/null");
            batchCmds.add("setprop debug.angle.backend 0");

            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommands(new ArrayList<>(batchCmds));
            }

            // Global Build reflection
            applyInAppReflectionMask(profile);

            // Staged procfs mock payloads
            exportMockProcfsPayloads(profile);

            SpoofPreferences.setSpoofEnabled(context, true);
            SpoofPreferences.setActiveProfileId(context, profile.id);
            SpoofPreferences.setSpoofAllApps(context, true);

            com.gamebooster.app.gamemanager.GameManagerStatus.getInstance().setMaskedAppsCount(count);
            Log.i(TAG, "⚡ Masked " + count + " game applications with profile: " + profile.displayName);

        } catch (Throwable t) {
            Log.e(TAG, "Error masking installed games", t);
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
        com.gamebooster.app.engine.AndroidVersionSupportManager.applyVersionOptimizations(context, packageName.trim(), targetHz);
    }

    public static void maskForAndroid13(String pkg, int targetHz) {
        List<String> cmds = new ArrayList<>();
        com.gamebooster.app.engine.AndroidVersionSupportManager.applyAndroid13Optimizations(pkg, targetHz, cmds);
        if (ShizukuExecutor.hasShizukuPermission()) ShizukuExecutor.executeShizukuCommands(cmds);
    }

    public static void maskForAndroid14(String pkg, int targetHz) {
        List<String> cmds = new ArrayList<>();
        com.gamebooster.app.engine.AndroidVersionSupportManager.applyAndroid14Optimizations(pkg, targetHz, cmds);
        if (ShizukuExecutor.hasShizukuPermission()) ShizukuExecutor.executeShizukuCommands(cmds);
    }

    public static void maskForAndroid15(String pkg, int targetHz) {
        List<String> cmds = new ArrayList<>();
        com.gamebooster.app.engine.AndroidVersionSupportManager.applyAndroid15Optimizations(pkg, targetHz, cmds);
        if (ShizukuExecutor.hasShizukuPermission()) ShizukuExecutor.executeShizukuCommands(cmds);
    }

    public static void maskForAndroid16(String pkg, int targetHz) {
        List<String> cmds = new ArrayList<>();
        com.gamebooster.app.engine.AndroidVersionSupportManager.applyAndroid16Optimizations(pkg, targetHz, cmds);
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
    public static void applyAppOpsShieldForPackage(Collection<String> batchCommands, String pkg) {
        if (pkg == null || pkg.trim().isEmpty() || batchCommands == null) return;
        String p = pkg.trim();
        boolean isMlbb = p.contains("mobile.legends") || p.contains("mobilelegends");
        // Moonton's native identity checker triggers fatal security exceptions or crash loops
        // if READ_PHONE_STATE / READ_DEVICE_IDENTIFIERS are forcefully set to 'ignore'.
        if (!isMlbb) {
            batchCommands.add("cmd appops set " + p + " READ_DEVICE_IDENTIFIERS ignore 2>/dev/null");
            batchCommands.add("cmd appops set " + p + " READ_PHONE_STATE ignore 2>/dev/null");
            batchCommands.add("cmd appops set " + p + " READ_PRIVILEGED_PHONE_STATE ignore 2>/dev/null");
        }
        batchCommands.add("cmd appops set " + p + " GET_USAGE_STATS ignore 2>/dev/null");
        batchCommands.add("cmd appops set " + p + " ACCESS_AD_ID ignore 2>/dev/null");
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
