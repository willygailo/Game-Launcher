package com.gamebooster.app.spoofer;

/**
 * SpoofProfile — Complete device identity profile for hardware spoofing.
 *
 * Each profile contains real-world device properties sourced from actual
 * `getprop` output on physical devices. When applied via Shizuku (temporary
 * full root), ALL Android property namespaces are overwritten to fully hide
 * the real device identity from game anti-cheat and device-detection systems.
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
    public final String socVendor;
    public final String board;
    public final String chipname;
    public final String cpuAbi;

    // ── Build Identity ──
    public final String fingerprint;
    public final String displayId;

    // ── GPU / Graphics Driver ──
    public final String glRenderer;
    public final String eglHardware;
    public final String glesVersion;

    // ── Memory / RAM ──
    public final int ramTotalMb;

    public SpoofProfile(String id, String displayName, String brandLabel,
                         String model, String brand, String manufacturer,
                         String device, String productName, String buildProduct,
                         String hardware, String platform, String socModel,
                         String board, String chipname,
                         String fingerprint, String displayId,
                         String glRenderer) {
        this(id, displayName, brandLabel, model, brand, manufacturer, device, productName,
                buildProduct, hardware, platform, socModel, "Qualcomm", board, chipname,
                "arm64-v8a", fingerprint, displayId, glRenderer, "adreno", "196610", 16384);
    }

    public SpoofProfile(String id, String displayName, String brandLabel,
                         String model, String brand, String manufacturer,
                         String device, String productName, String buildProduct,
                         String hardware, String platform, String socModel,
                         String socVendor, String board, String chipname,
                         String cpuAbi, String fingerprint, String displayId,
                         String glRenderer, String eglHardware, String glesVersion,
                         int ramTotalMb) {
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
        this.socVendor = socVendor != null ? socVendor : "Qualcomm";
        this.board = board;
        this.chipname = chipname;
        this.cpuAbi = cpuAbi != null ? cpuAbi : "arm64-v8a";
        this.fingerprint = fingerprint;
        this.displayId = displayId;
        this.glRenderer = glRenderer;
        this.eglHardware = eglHardware != null ? eglHardware : "adreno";
        this.glesVersion = glesVersion != null ? glesVersion : "196610";
        this.ramTotalMb = ramTotalMb > 0 ? ramTotalMb : 16384;
    }

    @Override
    public String toString() {
        return displayName + " [" + model + " / " + brand + " / " + socModel + " / " + (ramTotalMb / 1024) + "GB RAM]";
    }
}
