package com.gamebooster.app.spoofer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SpoofProfile — Comprehensive device identity profile for full-stack hardware spoofing.
 *
 * Covers all 5 detection vectors across Android apps & games:
 * 1. Model, Brand, Manufacturer, Device, Product
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
     * Primary full-spectrum constructor (with baseband).
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
        this.socManufacturer = socManufacturer != null ? socManufacturer : "Qualcomm";
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

    private static String inferSocManufacturer(String socModel, String brand) {
        if (socModel != null && socModel.toLowerCase().contains("dimensity")) return "MediaTek";
        if (socModel != null && (socModel.toLowerCase().contains("a18") || socModel.toLowerCase().contains("apple"))) return "Apple";
        if (brand != null && brand.equalsIgnoreCase("apple")) return "Apple";
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
        for (int i = 0; i < cpuCores; i++) {
            sb.append("processor\t: ").append(i).append("\n");
            sb.append("BogoMIPS\t: 38.40\n");
            sb.append("Features\t: ").append(cpuFeatures).append("\n");
            sb.append("CPU implementer\t: 0x51\n");
            sb.append("CPU architecture: 8\n");
            sb.append("CPU variant\t: 0x1\n");
            sb.append("CPU part\t: 0x805\n");
            sb.append("CPU revision\t: 1\n\n");
        }
        sb.append("Hardware\t: ").append(hardware != null ? hardware : "qcom").append("\n");
        sb.append("Processor\t: ").append(chipname != null ? chipname : socModel).append("\n");
        sb.append("SoC Model\t: ").append(socModel).append("\n");
        return sb.toString();
    }

    /**
     * Generates a fully formatted mock /proc/meminfo string.
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
               "Shmem:             45000 kB\n";
    }

    /**
     * Generates a full system properties map for all Android system namespaces.
     */
    /**
     * Generates a full system properties map covering all Android system, vendor, odm, and product namespaces.
     */
    public Map<String, String> generateSystemProperties() {
        Map<String, String> props = new LinkedHashMap<>();
        
        // Base / Main
        props.put("ro.product.model", model);
        props.put("ro.product.brand", brand);
        props.put("ro.product.name", productName);
        props.put("ro.product.device", device);
        props.put("ro.product.manufacturer", manufacturer);
        props.put("ro.product.board", board);
        props.put("ro.build.product", buildProduct);

        // System Partition Namespace
        props.put("ro.product.system.model", model);
        props.put("ro.product.system.brand", brand);
        props.put("ro.product.system.manufacturer", manufacturer);
        props.put("ro.product.system.name", productName);
        props.put("ro.product.system.device", device);

        // Vendor Partition Namespace
        props.put("ro.product.vendor.model", model);
        props.put("ro.product.vendor.brand", brand);
        props.put("ro.product.vendor.manufacturer", manufacturer);
        props.put("ro.product.vendor.name", productName);
        props.put("ro.product.vendor.device", device);

        // ODM Partition Namespace
        props.put("ro.product.odm.model", model);
        props.put("ro.product.odm.brand", brand);
        props.put("ro.product.odm.manufacturer", manufacturer);
        props.put("ro.product.odm.name", productName);
        props.put("ro.product.odm.device", device);

        // Product Partition Namespace
        props.put("ro.product.product.model", model);
        props.put("ro.product.product.brand", brand);
        props.put("ro.product.product.manufacturer", manufacturer);
        props.put("ro.product.product.name", productName);
        props.put("ro.product.product.device", device);

        // Hardware / SoC & Board
        props.put("ro.hardware", hardware);
        props.put("ro.board.platform", platform);
        props.put("ro.hardware.platform", platform);
        props.put("ro.soc.model", socModel);
        props.put("ro.soc.manufacturer", socManufacturer);
        props.put("ro.chipname", chipname);
        props.put("ro.hardware.chipname", chipname);

        // Baseband / Modem / Radio Identity
        props.put("gsm.version.baseband", baseband);
        props.put("gsm.version.baseband1", baseband);
        props.put("ro.baseband", board != null ? board : platform);
        props.put("ro.boot.baseband", board != null ? board : platform);
        props.put("gsm.network.type", "LTE,5G");

        // Build / Fingerprint / OS
        props.put("ro.build.fingerprint", fingerprint);
        props.put("ro.build.display.id", displayId);
        props.put("ro.build.version.release", androidVersion);
        props.put("ro.build.version.sdk", String.valueOf(sdkInt));
        props.put("ro.build.version.security_patch", securityPatch);
        props.put("ro.build.flavor", productName + "-user");
        props.put("ro.build.description", productName + "-user " + androidVersion + " " + displayId + " release-keys");

        // Graphics / EGL / Vulkan
        props.put("ro.hardware.egl", glVendor.toLowerCase().contains("arm") ? "mali" : "adreno");
        props.put("ro.opengles.version", "196610"); // OpenGL ES 3.2
        props.put("debug.hwui.renderer", "vulkan");
        props.put("debug.renderengine.backend", "vulkan");

        // RAM & Device Names
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
    //  In-Game Engine Profile Generators (For 40+ Top Titles)
    // ─────────────────────────────────────────────────────────────────────────

    public String generateUe4DeviceProfile(int targetFps) {
        return "[DeviceProfile]\n" +
                "DeviceName=" + model + "\n" +
                "DeviceBrand=" + brand + "\n" +
                "DeviceManufacturer=" + manufacturer + "\n" +
                "GPUFamily=" + glRenderer + "\n" +
                "SoCModel=" + socModel + "\n" +
                "RAMTotalMB=" + ramTotalMb + "\n" +
                "+CVars=r.PUBGDeviceFPS=9\n" +
                "+CVars=r.PUBGFrameRateLimit=" + targetFps + "\n" +
                "+CVars=r.MobileFPSLimit=" + targetFps + "\n" +
                "+CVars=r.FrameRateLimit=" + targetFps + "\n" +
                "+CVars=r.MobileTouchBoostRate=" + targetFps + "\n" +
                "+CVars=r.MobileHDR=1\n" +
                "+CVars=r.Vulkan.Enable=1\n" +
                "+CVars=r.ShadowQuality=4\n" +
                "+CVars=r.MaxAnisotropy=16\n" +
                "+CVars=r.Tonemapper.Quality=4\n" +
                "FrameRateLevel=9\n" +
                "Unlock185Hz=1\n" +
                "Unlock165Hz=1\n" +
                "Unlock120FPS=1\n" +
                "Unlock90FPS=1\n";
    }

    public String generateJsonHardwareProfile(int targetFps) {
        return "{\n" +
                "  \"DeviceModel\": \"" + model + "\",\n" +
                "  \"DeviceBrand\": \"" + brand + "\",\n" +
                "  \"Manufacturer\": \"" + manufacturer + "\",\n" +
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
                "ExtemeFrameRate=1\n" +
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

    @Override
    public String toString() {
        return displayName + " [" + model + " / " + brand + " / " + socModel + " / " + ramTotalMb + "MB]";
    }
}
