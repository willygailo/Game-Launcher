package com.gamebooster.app.spoofer;

import com.gamebooster.app.device.DeviceDetector;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * SpoofProfile — Comprehensive device identity profile for legal, full-stack hardware spoofing.
 *
 * Designed to safely bypass game developer FPS and graphics tier whitelists (unlocking 90Hz, 120Hz,
 * 144Hz, 165Hz, and 185Hz in titles like PUBG, MLBB, CODM, Wild Rift, and Genshin Impact) without
 * triggering anti-cheat integrity bans or telecom carrier violations.
 *
 * Covers 5 Core Detection & Whitelist Vectors:
 * 1. Model, Brand, Manufacturer, Device, Product across all Android partitions (system, vendor, odm, product)
 * 2. CPU / SoC: Cores, Architecture, Clock speed, Hardware strings, /proc/cpuinfo
 * 3. GPU / Graphics: GL Renderer, Vendor, Version, Vulkan driver, ANGLE
 * 4. RAM / Memory: Total MB, Available MB, /proc/meminfo
 * 5. OS Build / Fingerprint: Release, SDK_INT, Display ID, Security patch
 */
public class SpoofProfile {

    public final String id;
    public final String displayName;
    public final String brandLabel;

    // ── Core Identity ──
    public final String model;
    public final String brand;
    public final String manufacturer;
    public final String device;
    public final String productName;
    public final String buildProduct;

    // ── Hardware / SoC / CPU ──
    public final String hardware;
    public final String platform;
    public final String socModel;
    public final String board;
    public final String chipname;
    public final String socManufacturer;
    public final int cpuCores;
    public final int cpuMaxFreqKhz;
    public final String cpuArchitecture;
    public final String cpuFeatures;
    public final String baseband;

    // ── Build Identity & OS Version ──
    public final String fingerprint;
    public final String displayId;
    public final String androidVersion;
    public final int sdkInt;
    public final String securityPatch;

    // ── GPU / Graphics ──
    public final String glRenderer;
    public final String glVendor;
    public final String glVersion;
    public final String vulkanVersion;
    public final String vulkanDriverVersion;

    // ── RAM / Memory ──
    public final int ramTotalMb;
    public final int ramAvailableMb;

    // ── Display ──
    public final int maxRefreshRateHz;

    /**
     * Primary full-spectrum constructor (with explicit baseband).
     */
    public SpoofProfile(String id, String displayName, String brandLabel,
                        String model, String brand, String manufacturer,
                        String device, String productName, String buildProduct,
                        String hardware, String platform, String socModel,
                        String board, String chipname,
                        String socManufacturer, int cpuCores, int cpuMaxFreqKhz,
                        String cpuArchitecture, String cpuFeatures,
                        String baseband,
                        String fingerprint, String displayId,
                        String androidVersion, int sdkInt, String securityPatch,
                        String glRenderer, String glVendor, String glVersion,
                        String vulkanVersion, String vulkanDriverVersion,
                        int ramTotalMb, int ramAvailableMb,
                        int maxRefreshRateHz) {
        this.id = id;
        this.displayName = displayName;
        this.brandLabel = brandLabel;
        this.model = model;
        this.brand = brand;
        this.manufacturer = manufacturer;
        this.device = device;
        this.productName = productName;
        this.buildProduct = buildProduct;
        this.hardware = hardware;
        this.platform = platform;
        this.socModel = socModel;
        this.board = board;
        this.chipname = chipname;
        this.socManufacturer = socManufacturer != null ? socManufacturer : inferSocManufacturer(socModel, brand);
        this.cpuCores = cpuCores > 0 ? cpuCores : 8;
        this.cpuMaxFreqKhz = cpuMaxFreqKhz > 0 ? cpuMaxFreqKhz : 4320000;
        this.cpuArchitecture = cpuArchitecture != null ? cpuArchitecture : "ARM64-v9.2-A";
        this.cpuFeatures = cpuFeatures != null ? cpuFeatures : "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp";
        this.baseband = baseband != null ? baseband : inferBaseband(board, platform, model, displayId);
        this.fingerprint = fingerprint;
        this.displayId = displayId;
        this.androidVersion = androidVersion != null ? androidVersion : "15";
        this.sdkInt = sdkInt > 0 ? sdkInt : 35;
        this.securityPatch = securityPatch != null ? securityPatch : "2025-01-01";
        this.glRenderer = glRenderer;
        this.glVendor = glVendor != null ? glVendor : inferVendor(glRenderer);
        this.glVersion = glVersion != null ? glVersion : "OpenGL ES 3.2 V@0615.0 (GIT@56860db, Idd24e5256e) (Date:11/24/24)";
        this.vulkanVersion = vulkanVersion != null ? vulkanVersion : "1.3.280";
        this.vulkanDriverVersion = vulkanDriverVersion != null ? vulkanDriverVersion : "512.615.0";
        this.ramTotalMb = ramTotalMb > 0 ? ramTotalMb : 16384;
        this.ramAvailableMb = ramAvailableMb > 0 ? ramAvailableMb : (int) (this.ramTotalMb * 0.75);
        this.maxRefreshRateHz = maxRefreshRateHz > 0 ? maxRefreshRateHz : 185;
    }

    /**
     * Overloaded constructor without explicit baseband (auto-infers baseband).
     */
    public SpoofProfile(String id, String displayName, String brandLabel,
                        String model, String brand, String manufacturer,
                        String device, String productName, String buildProduct,
                        String hardware, String platform, String socModel,
                        String board, String chipname,
                        String socManufacturer, int cpuCores, int cpuMaxFreqKhz,
                        String cpuArchitecture, String cpuFeatures,
                        String fingerprint, String displayId,
                        String androidVersion, int sdkInt, String securityPatch,
                        String glRenderer, String glVendor, String glVersion,
                        String vulkanVersion, String vulkanDriverVersion,
                        int ramTotalMb, int ramAvailableMb,
                        int maxRefreshRateHz) {
        this(id, displayName, brandLabel,
             model, brand, manufacturer,
             device, productName, buildProduct,
             hardware, platform, socModel,
             board, chipname,
             socManufacturer, cpuCores, cpuMaxFreqKhz,
             cpuArchitecture, cpuFeatures,
             inferBaseband(board, platform, model, displayId),
             fingerprint, displayId,
             androidVersion, sdkInt, securityPatch,
             glRenderer, glVendor, glVersion,
             vulkanVersion, vulkanDriverVersion,
             ramTotalMb, ramAvailableMb,
             maxRefreshRateHz);
    }

    /**
     * Backward-compatible 17-parameter constructor.
     */
    public SpoofProfile(String id, String displayName, String brandLabel,
                        String model, String brand, String manufacturer,
                        String device, String productName, String buildProduct,
                        String hardware, String platform, String socModel,
                        String board, String chipname,
                        String fingerprint, String displayId,
                        String glRenderer) {
        this(id, displayName, brandLabel,
             model, brand, manufacturer,
             device, productName, buildProduct,
             hardware, platform, socModel,
             board, chipname,
             inferSocManufacturer(socModel, brand),
             8, 4320000, "ARM64-v9.2-A",
             "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
             inferBaseband(board, platform, model, displayId),
             fingerprint, displayId,
             "15", 35, "2025-01-01",
             glRenderer, inferVendor(glRenderer),
             "OpenGL ES 3.2 V@0615.0",
             "1.3.280", "512.615.0",
             16384, 12288,
             185);
    }

    private static String inferBaseband(String board, String platform, String model, String displayId) {
        if (displayId != null && displayId.contains(".")) {
            return displayId.substring(displayId.lastIndexOf('.') + 1);
        }
        String base = (platform != null && !platform.isEmpty()) ? platform : (board != null ? board : (model != null ? model : "generic"));
        return base.toUpperCase().replace(" ", "_") + "_MODEM_V1.0";
    }

    public DeviceDetector.ChipsetVendor getChipsetVendor() {
        String mfg = socManufacturer != null ? socManufacturer.toLowerCase(Locale.US) : "";
        String soc = socModel != null ? socModel.toLowerCase(Locale.US) : "";
        String b = brand != null ? brand.toLowerCase(Locale.US) : "";

        if (mfg.contains("mediatek") || soc.contains("dimensity") || soc.contains("mt")) {
            return DeviceDetector.ChipsetVendor.MEDIATEK;
        }
        if (mfg.contains("samsung") || soc.contains("exynos")) {
            return DeviceDetector.ChipsetVendor.EXYNOS;
        }
        if (mfg.contains("google") || soc.contains("tensor")) {
            return DeviceDetector.ChipsetVendor.TENSOR;
        }
        if (mfg.contains("unisoc")) {
            return DeviceDetector.ChipsetVendor.UNISOC;
        }
        if (mfg.contains("hisilicon") || soc.contains("kirin")) {
            return DeviceDetector.ChipsetVendor.KIRIN;
        }
        if (mfg.contains("apple") || soc.contains("apple") || b.contains("apple") || soc.matches("a1[0-9].*") || soc.matches("m[0-9].*")) {
            return DeviceDetector.ChipsetVendor.APPLE;
        }
        return DeviceDetector.ChipsetVendor.QUALCOMM;
    }

    private static String inferSocManufacturer(String socModel, String brand) {
        String soc = socModel != null ? socModel.toLowerCase(Locale.US) : "";
        String b = brand != null ? brand.toLowerCase(Locale.US) : "";
        if (soc.contains("dimensity") || soc.contains("mt")) return "MediaTek";
        if (soc.contains("exynos")) return "Samsung";
        if (soc.contains("tensor")) return "Google";
        if (soc.contains("unisoc")) return "Unisoc";
        if (soc.contains("kirin")) return "HiSilicon";
        if (soc.contains("a18") || soc.contains("a17") || soc.contains("apple") || soc.contains("m4") || b.contains("apple")) return "Apple";
        return "Qualcomm";
    }

    private static String inferVendor(String glRenderer) {
        if (glRenderer == null) return "Qualcomm";
        String lower = glRenderer.toLowerCase();
        if (lower.contains("mali") || lower.contains("immortalis")) return "ARM";
        if (lower.contains("apple")) return "Apple";
        if (lower.contains("powervr")) return "Imagination Technologies";
        return "Qualcomm";
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Mock /proc Payload Generators
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generates a fully formatted mock /proc/cpuinfo string.
     */
    public String generateCpuInfo() {
        StringBuilder sb = new StringBuilder();
        int primeCores = cpuCores >= 8 ? 2 : 1;
        for (int i = 0; i < cpuCores; i++) {
            sb.append("processor\t: ").append(i).append("\n");
            sb.append("BogoMIPS\t: ").append(i >= (cpuCores - primeCores) ? "48.00" : "38.40").append("\n");
            sb.append("Features\t: ").append(cpuFeatures).append("\n");
            sb.append("CPU implementer\t: 0x51\n");
            sb.append("CPU architecture: 8\n");
            sb.append("CPU variant\t: 0x1\n");
            sb.append("CPU part\t: ").append(i >= (cpuCores - primeCores) ? "0x806" : "0x805").append("\n");
            sb.append("CPU revision\t: 1\n\n");
        }
        sb.append("Hardware\t: ").append(hardware != null ? hardware : "qcom").append("\n");
        sb.append("Revision\t: 0001\n");
        sb.append("Serial\t\t: ").append(getSerialNumber()).append("\n");
        sb.append("Processor\t: ").append(chipname != null ? chipname : socModel).append("\n");
        sb.append("SoC Model\t: ").append(socModel).append("\n");
        sb.append("SoC Manufacturer: ").append(socManufacturer).append("\n");
        return sb.toString();
    }

    /**
     * Generates a fully formatted mock /proc/meminfo string with modern 64-bit kernel memory fields.
     */
    public String generateMemInfo() {
        long totalKb = (long) ramTotalMb * 1024L;
        long availKb = (long) ramAvailableMb * 1024L;
        long freeKb = (long) (availKb * 0.7);
        long cachedKb = (long) (availKb * 0.25);

        return "MemTotal:       " + totalKb + " kB\n" +
               "MemFree:        " + freeKb + " kB\n" +
               "MemAvailable:   " + availKb + " kB\n" +
               "Buffers:          350000 kB\n" +
               "Cached:          " + cachedKb + " kB\n" +
               "SwapCached:            0 kB\n" +
               "Active:          4200000 kB\n" +
               "Inactive:        2100000 kB\n" +
               "SwapTotal:       8388608 kB\n" +
               "SwapFree:        8388608 kB\n" +
               "Dirty:               120 kB\n" +
               "Writeback:             0 kB\n" +
               "AnonPages:       3100000 kB\n" +
               "Mapped:           850000 kB\n" +
               "Shmem:             45000 kB\n" +
               "KReclaimable:     500000 kB\n" +
               "Slab:             600000 kB\n" +
               "SReclaimable:     500000 kB\n" +
               "SUnreclaim:       100000 kB\n" +
               "KernelStack:       45000 kB\n" +
               "PageTables:        65000 kB\n" +
               "VmallocTotal:   34359738367 kB\n" +
               "VmallocUsed:      120000 kB\n" +
               "CmaTotal:        1048576 kB\n" +
               "CmaFree:          524288 kB\n";
    }

    /**
     * Generates a clean, deduplicated system properties map covering all Android partition namespaces.
     */
    public Map<String, String> generateSystemProperties() {
        Map<String, String> props = new LinkedHashMap<>();

        // Base / Main Identifiers
        props.put("ro.product.model", model);
        props.put("ro.product.brand", brand);
        props.put("ro.product.name", productName);
        props.put("ro.product.device", device);
        props.put("ro.product.manufacturer", manufacturer);
        props.put("ro.product.board", board);
        props.put("ro.build.product", buildProduct);

        // Partition Namespaces (System, Vendor, ODM, Product, System_Ext)
        String[] namespaces = {"system", "vendor", "odm", "product", "system_ext"};
        for (String ns : namespaces) {
            props.put("ro.product." + ns + ".model", model);
            props.put("ro.product." + ns + ".brand", brand);
            props.put("ro.product." + ns + ".manufacturer", manufacturer);
            props.put("ro.product." + ns + ".name", productName);
            props.put("ro.product." + ns + ".device", device);
            props.put("ro.product." + ns + ".cpu.abilist", "arm64-v8a,armeabi-v7a,armeabi");
        }

        // CPU ABI & Architecture
        props.put("ro.arch", "arm64");
        props.put("ro.product.cpu.abi", "arm64-v8a");
        props.put("ro.product.cpu.abilist", "arm64-v8a,armeabi-v7a,armeabi");
        props.put("ro.product.cpu.abilist64", "arm64-v8a");
        props.put("ro.product.cpu.abilist32", "armeabi-v7a,armeabi");

        // Hardware / SoC & Platform
        props.put("ro.hardware", hardware);
        props.put("ro.board.platform", platform);
        props.put("ro.hardware.platform", platform);
        props.put("ro.soc.model", socModel);
        props.put("ro.soc.manufacturer", socManufacturer);
        props.put("ro.soc.feature_level", "1");
        props.put("ro.chipname", chipname);
        props.put("ro.hardware.chipname", chipname);
        props.put("vendor.cpu.cores", String.valueOf(cpuCores));
        props.put("persist.sys.cpu.core_num", String.valueOf(cpuCores));
        props.put("persist.sys.cpu.brand", socModel);
        props.put("ro.vendor.cpu.max_freq", String.valueOf(cpuMaxFreqKhz));
        props.put("persist.sys.cpu.freq", String.valueOf(cpuMaxFreqKhz));
        props.put("ro.vendor.qti.soc_name", socModel);
        if (socModel != null && socModel.contains("SM8750")) {
            props.put("ro.vendor.qti.soc_id", "600");
        } else if (socModel != null && socModel.contains("SM8650")) {
            props.put("ro.vendor.qti.soc_id", "557");
        } else if (socModel != null && socModel.contains("SM8850")) {
            props.put("ro.vendor.qti.soc_id", "650");
        }

        // Baseband & Radio Identity
        props.put("gsm.version.baseband", baseband);
        props.put("gsm.version.baseband1", baseband);
        props.put("ro.baseband", board != null ? board : platform);
        props.put("ro.boot.baseband", board != null ? board : platform);
        props.put("gsm.network.type", "LTE,5G");

        // Build / Fingerprint / OS Versioning
        props.put("ro.build.fingerprint", fingerprint);
        props.put("ro.build.display.id", displayId);
        props.put("ro.build.version.release", androidVersion);
        props.put("ro.build.version.sdk", String.valueOf(sdkInt));
        props.put("ro.build.version.security_patch", securityPatch);
        props.put("ro.build.flavor", productName + "-user");
        props.put("ro.build.description", productName + "-user " + androidVersion + " " + displayId + " release-keys");
        props.put("ro.build.tags", "release-keys");
        props.put("ro.build.type", "user");
        props.put("ro.build.user", "builder");
        props.put("ro.build.host", "build-host");
        props.put("ro.debuggable", "0");
        props.put("ro.secure", "1");
        props.put("ro.vendor.build.fingerprint", fingerprint);
        props.put("ro.odm.build.fingerprint", fingerprint);
        props.put("ro.system.build.fingerprint", fingerprint);
        props.put("ro.system_ext.build.fingerprint", fingerprint);

        // Graphics / EGL / Vulkan
        String eglVendor = glVendor.toLowerCase().contains("arm") ? "mali" : "adreno";
        props.put("ro.hardware.egl", eglVendor);
        props.put("ro.hardware.vulkan", eglVendor);
        props.put("ro.opengles.version", "196610"); // OpenGL ES 3.2
        props.put("ro.vulkan.version", vulkanVersion != null ? vulkanVersion : "1.3.280");
        props.put("vendor.gpu.driver_version", vulkanDriverVersion != null ? vulkanDriverVersion : "512.700.0");
        props.put("ro.vendor.gpu.name", glRenderer);
        props.put("ro.vendor.gpu.model", glRenderer);
        props.put("ro.vendor.gpu.vendor", glVendor);
        props.put("vendor.gpu.available_frequencies", "1000000000,900000000,800000000,600000000,400000000");
        props.put("debug.adreno.version", glVersion != null ? glVersion : "OpenGL ES 3.2 V@0700.0");
        props.put("debug.egl.hw", "1");
        props.put("debug.hwui.renderer", "vulkan");
        props.put("debug.renderengine.backend", "vulkan");

        // RAM & Memory Masking
        props.put("ro.config.low_ram", "false");
        props.put("ro.config.avoid_gfx_accel", "false");
        props.put("ro.boot.ram_size", (ramTotalMb >= 1024 ? (ramTotalMb / 1024) + "GB" : ramTotalMb + "MB"));
        props.put("ro.ram.total", String.valueOf(ramTotalMb));
        props.put("ro.vendor.ram.total", String.valueOf(ramTotalMb));
        props.put("persist.sys.ram_size", String.valueOf(ramTotalMb));
        props.put("ro.sys.fw.bg_apps_limit", "64");
        props.put("ro.sys.fw.empty_app_percent", "50");
        props.put("ro.vendor.qti.am.free_page_min", "16384");
        props.put("dalvik.vm.heapgrowthlimit", "512m");
        props.put("dalvik.vm.heapsize", "1024m");
        props.put("dalvik.vm.heaptargetutilization", "0.75");
        props.put("dalvik.vm.heapminfree", "8m");
        props.put("dalvik.vm.heapmaxfree", "32m");

        // Legal Sandbox Virtual Identifiers (Strictly for Game Engine Whitelists & Privacy)
        props.put("ro.serialno", getSerialNumber());
        props.put("ro.boot.serialno", getSerialNumber());
        props.put("debug.game.spoofed_android_id", getAndroidId());
        props.put("debug.game.spoofed_oaid", getOaid());
        props.put("debug.game.spoofed_gsf_id", getGsfId());
        props.put("debug.game.spoofed_widevine", getWidevineDeviceId());
        props.put("debug.game.spoofed_wifi_mac", getWifiMacAddress());
        props.put("debug.game.spoofed_bt_mac", getBluetoothMacAddress());
        props.put("debug.game.spoofed_aaid", getAdvertisingId());

        // RAM & Device Hostnames
        props.put("debug.game.spoofed_ram", String.valueOf(ramTotalMb));
        props.put("debug.game.spoofed_ram_avail", String.valueOf(ramAvailableMb));
        props.put("persist.sys.device_name", displayName);
        props.put("net.hostname", model.replace(" ", "_"));
        props.put("bluetooth.device.default_name", displayName);

        return Collections.unmodifiableMap(props);
    }

    /**
     * Generates a mock /proc/version payload.
     */
    public String generateProcVersion() {
        return "Linux version 6.6.56-android15-11-g" + Long.toHexString(System.currentTimeMillis()).substring(0, 7) +
               " (android-build@google.com) (Android (11679469, based on r522817) clang version 18.0.1) #1 SMP PREEMPT " +
               "Mon Jan 20 04:12:35 UTC 2025\n";
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  In-Game Engine Profile Generators (For Top Titles)
    // ─────────────────────────────────────────────────────────────────────────

    public String[] generateUe4DeviceProfileKeys(int targetFps) {
        int clampedFps = Math.max(120, Math.min(185, targetFps));
        return new String[] {
            "DeviceName=" + model,
            "DeviceBrand=" + brand,
            "DeviceManufacturer=" + manufacturer,
            "DeviceID=" + getAndroidId(),
            "DeviceSerialNumber=" + getSerialNumber(),
            "GPUFamily=" + glRenderer,
            "SoCModel=" + socModel,
            "RAMTotalMB=" + ramTotalMb,
            "+CVars=r.PUBGDeviceFPS=10",
            "+CVars=r.PUBGDeviceFPSPolicy=1",
            "+CVars=r.PUBGTargetFPS=" + clampedFps,
            "+CVars=r.PUBGMaxFPS=" + clampedFps,
            "+CVars=r.MobileFPSLimit=" + clampedFps,
            "+CVars=r.FrameRateLimit=" + clampedFps,
            "+CVars=r.MobileTouchBoostRate=" + clampedFps,
            "+CVars=r.MobileHDR=1",
            "+CVars=r.Vulkan.Enable=1",
            "+CVars=r.ShadowQuality=4",
            "+CVars=r.MaxAnisotropy=16",
            "+CVars=r.Tonemapper.Quality=4",
            "+CVars=r.Streaming.PoolSize=4096",
            "+CVars=r.Android.DisableProgramBinaryCache=0",
            "FrameRateLevel=10",
            "Unlock185Hz=1",
            "Unlock165Hz=1",
            "Unlock144Hz=1",
            "Unlock120FPS=1"
        };
    }

    public String generateUe4DeviceProfile(int targetFps) {
        StringBuilder sb = new StringBuilder();
        sb.append("[DeviceProfile]\n");
        for (String key : generateUe4DeviceProfileKeys(targetFps)) {
            sb.append(key).append("\n");
        }
        return sb.toString();
    }

    public String generateJsonHardwareProfile(int targetFps) {
        return "{\n" +
                "  \"DeviceModel\": \"" + model + "\",\n" +
                "  \"DeviceBrand\": \"" + brand + "\",\n" +
                "  \"Manufacturer\": \"" + manufacturer + "\",\n" +
                "  \"AndroidID\": \"" + getAndroidId() + "\",\n" +
                "  \"SerialNumber\": \"" + getSerialNumber() + "\",\n" +
                "  \"OAID\": \"" + getOaid() + "\",\n" +
                "  \"MacAddress\": \"" + getWifiMacAddress() + "\",\n" +
                "  \"GPURenderer\": \"" + glRenderer + "\",\n" +
                "  \"GPUVendor\": \"" + glVendor + "\",\n" +
                "  \"SoCModel\": \"" + socModel + "\",\n" +
                "  \"SoCManufacturer\": \"" + socManufacturer + "\",\n" +
                "  \"CPUCores\": " + cpuCores + ",\n" +
                "  \"RAMTotalMB\": " + ramTotalMb + ",\n" +
                "  \"MaxFrameRate\": " + targetFps + ",\n" +
                "  \"FPSLimit\": " + targetFps + ",\n" +
                "  \"GraphicQuality\": 4,\n" +
                "  \"UnlockUltraHighFPS\": true,\n" +
                "  \"Unlock185Hz\": true,\n" +
                "  \"Unlock165Hz\": true,\n" +
                "  \"Unlock120Hz\": true,\n" +
                "  \"VulkanSupport\": true\n" +
                "}\n";
    }

    public String generateMlbbDeviceConfig(int targetFps) {
        return "[Hardware]\n" +
                "DeviceModel=" + model + "\n" +
                "DeviceBrand=" + brand + "\n" +
                "GPU=" + glRenderer + "\n" +
                "GPUVendor=" + glVendor + "\n" +
                "SoC=" + socModel + "\n" +
                "RAM=" + ramTotalMb + "\n" +
                "HighFPSMode=1\n" +
                "UltraFrameRate=1\n" +
                "SuperFrameRate=1\n" +
                "FrameRateLevel=10\n" +
                "TargetFPS=" + targetFps + "\n" +
                "ShadowQuality=3\n" +
                "HDMode=1\n" +
                "CreepOutline=1\n";
    }

    public String generateFreeFireDeviceConfig(int targetFps) {
        return "{\n" +
                "  \"device_model\": \"" + model + "\",\n" +
                "  \"device_brand\": \"" + brand + "\",\n" +
                "  \"gpu\": \"" + glRenderer + "\",\n" +
                "  \"soc\": \"" + socModel + "\",\n" +
                "  \"ram_mb\": " + ramTotalMb + ",\n" +
                "  \"high_fps\": 1,\n" +
                "  \"max_fps\": " + targetFps + ",\n" +
                "  \"shadow\": 1,\n" +
                "  \"high_res\": 1,\n" +
                "  \"vivid\": 1\n" +
                "}\n";
    }

    public String generateGenshinDeviceConfig(int targetFps) {
        return "{\n" +
                "  \"device_model\": \"" + model + "\",\n" +
                "  \"device_brand\": \"" + brand + "\",\n" +
                "  \"gpu_renderer\": \"" + glRenderer + "\",\n" +
                "  \"gpu_vendor\": \"" + glVendor + "\",\n" +
                "  \"soc_model\": \"" + socModel + "\",\n" +
                "  \"ram_total_mb\": " + ramTotalMb + ",\n" +
                "  \"vulkan_support\": true,\n" +
                "  \"max_refresh_rate\": " + targetFps + ",\n" +
                "  \"frame_rate_cap\": " + targetFps + ",\n" +
                "  \"quality_tier\": 5,\n" +
                "  \"render_resolution_scale\": 1.0\n" +
                "}\n";
    }

    public String generateHokDeviceConfig(int targetFps) {
        return "[DeviceConfig]\n" +
                "Model=" + model + "\n" +
                "Brand=" + brand + "\n" +
                "GPU=" + glRenderer + "\n" +
                "SoC=" + socModel + "\n" +
                "RAM=" + ramTotalMb + "\n" +
                "HighFrameRate=1\n" +
                "UltraFrameRate=1\n" +
                "ExtremeFrameRate=1\n" +
                "FPS=" + targetFps + "\n";
    }

    public String generateStandoff2DeviceConfig(int targetFps) {
        return "{\n" +
                "  \"device_model\": \"" + model + "\",\n" +
                "  \"device_brand\": \"" + brand + "\",\n" +
                "  \"gpu_renderer\": \"" + glRenderer + "\",\n" +
                "  \"gpu_vendor\": \"" + glVendor + "\",\n" +
                "  \"soc_model\": \"" + socModel + "\",\n" +
                "  \"ram_total_mb\": " + ramTotalMb + ",\n" +
                "  \"target_fps\": " + targetFps + ",\n" +
                "  \"max_fps\": " + targetFps + ",\n" +
                "  \"graphic_quality\": \"very_high\",\n" +
                "  \"shader_quality\": \"high\",\n" +
                "  \"texture_quality\": \"high\",\n" +
                "  \"anisotropic_filtering\": \"16x\",\n" +
                "  \"anti_aliasing\": \"8x\"\n" +
                "}\n";
    }

    public String generateCarXDeviceConfig(int targetFps) {
        return "[GraphicsSettings]\n" +
                "DeviceModel=" + model + "\n" +
                "DeviceBrand=" + brand + "\n" +
                "GPU=" + glRenderer + "\n" +
                "GPUVendor=" + glVendor + "\n" +
                "SoC=" + socModel + "\n" +
                "RAM=" + ramTotalMb + "\n" +
                "TargetFPS=" + targetFps + "\n" +
                "MaxFPS=" + targetFps + "\n" +
                "FPSLimit=" + targetFps + "\n" +
                "GraphicQuality=5\n" +
                "VulkanEnabled=1\n" +
                "MotionBlur=0\n" +
                "SmokeQuality=2\n";
    }

    public String generateSupercellDeviceConfig(int targetFps) {
        return "[DeviceProfile]\n" +
                "model=" + model + "\n" +
                "brand=" + brand + "\n" +
                "gpu=" + glRenderer + "\n" +
                "soc=" + socModel + "\n" +
                "ram=" + ramTotalMb + "\n" +
                "high_fps=1\n" +
                "fps_cap=" + targetFps + "\n" +
                "target_hz=" + targetFps + "\n";
    }

    public String generateGenericHardwareConfig(int targetFps) {
        return "[DeviceHardware]\n" +
                "DeviceModel=" + model + "\n" +
                "DeviceBrand=" + brand + "\n" +
                "Manufacturer=" + manufacturer + "\n" +
                "DeviceID=" + getAndroidId() + "\n" +
                "DeviceSerial=" + getSerialNumber() + "\n" +
                "AndroidID=" + getAndroidId() + "\n" +
                "OAID=" + getOaid() + "\n" +
                "GPURenderer=" + glRenderer + "\n" +
                "GPUVendor=" + glVendor + "\n" +
                "SoCModel=" + socModel + "\n" +
                "SoCManufacturer=" + socManufacturer + "\n" +
                "CPUCores=" + cpuCores + "\n" +
                "RAMTotalMB=" + ramTotalMb + "\n" +
                "MaxFrameRate=" + targetFps + "\n" +
                "HighFPSMode=1\n" +
                "VulkanSupport=1\n";
    }

    // ── Unique Device Identity Descriptors (7-Vector Hardware Fingerprint) ──

    public String getAndroidId() {
        return DeviceIdentityGenerator.generateAndroidId(this);
    }

    public String getSerialNumber() {
        return DeviceIdentityGenerator.generateSerialNumber(this);
    }

    public String getWifiMacAddress() {
        return DeviceIdentityGenerator.generateWifiMacAddress(this);
    }

    public String getBluetoothMacAddress() {
        return DeviceIdentityGenerator.generateBluetoothMacAddress(this);
    }

    public String getOaid() {
        return DeviceIdentityGenerator.generateOaid(this);
    }

    public String getGsfId() {
        return DeviceIdentityGenerator.generateGsfId(this);
    }

    public String getWidevineDeviceId() {
        return DeviceIdentityGenerator.generateWidevineDeviceId(this);
    }

    public String getAdvertisingId() {
        return DeviceIdentityGenerator.generateAdvertisingId(this);
    }

    public String getImei1() {
        return DeviceIdentityGenerator.generateImei(this, 1);
    }

    public String getImei2() {
        return DeviceIdentityGenerator.generateImei(this, 2);
    }

    @Override
    public String toString() {
        return displayName + " [" + model + " / " + brand + " / " + socModel + " / " + ramTotalMb + "MB]";
    }
}
