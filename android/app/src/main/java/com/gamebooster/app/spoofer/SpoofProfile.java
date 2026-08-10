package com.gamebooster.app.spoofer;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * SpoofProfile — Complete device identity profile for hardware spoofing.
 *
 * Sourced from real-world `getprop` output on physical devices.
 * Overwrites ALL Android property namespaces to hide real device identity from
 * game anti-cheat & device detection systems.
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
    public final int androidSdkVersion;

    // ── GPU / Graphics Driver ──
    public final String glRenderer;
    public final String eglHardware;
    public final String glesVersion;

    // ── Memory & Display Capabilities ──
    public final int ramTotalMb;
    public final int targetRefreshRate;

    public SpoofProfile(String id, String displayName, String brandLabel,
                         String model, String brand, String manufacturer,
                         String device, String productName, String buildProduct,
                         String hardware, String platform, String socModel,
                         String board, String chipname,
                         String fingerprint, String displayId,
                         String glRenderer) {
        this(id, displayName, brandLabel, model, brand, manufacturer, device, productName,
                buildProduct, hardware, platform, socModel, "Qualcomm", board, chipname,
                "arm64-v8a", fingerprint, displayId, 34, glRenderer, "adreno", "196610", 16384, 165);
    }

    public SpoofProfile(String id, String displayName, String brandLabel,
                         String model, String brand, String manufacturer,
                         String device, String productName, String buildProduct,
                         String hardware, String platform, String socModel,
                         String socVendor, String board, String chipname,
                         String cpuAbi, String fingerprint, String displayId,
                         String glRenderer, String eglHardware, String glesVersion,
                         int ramTotalMb) {
        this(id, displayName, brandLabel, model, brand, manufacturer, device, productName,
                buildProduct, hardware, platform, socModel, socVendor, board, chipname,
                cpuAbi, fingerprint, displayId, 34, glRenderer, eglHardware, glesVersion, ramTotalMb, 165);
    }

    public SpoofProfile(String id, String displayName, String brandLabel,
                         String model, String brand, String manufacturer,
                         String device, String productName, String buildProduct,
                         String hardware, String platform, String socModel,
                         String socVendor, String board, String chipname,
                         String cpuAbi, String fingerprint, String displayId,
                         int androidSdkVersion, String glRenderer, String eglHardware,
                         String glesVersion, int ramTotalMb, int targetRefreshRate) {
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
        this.androidSdkVersion = androidSdkVersion > 0 ? androidSdkVersion : 34;
        this.glRenderer = glRenderer;
        this.eglHardware = eglHardware != null ? eglHardware : "adreno";
        this.glesVersion = glesVersion != null ? glesVersion : "196610";
        this.ramTotalMb = ramTotalMb > 0 ? ramTotalMb : 16384;
        this.targetRefreshRate = targetRefreshRate > 0 ? targetRefreshRate : 165;
    }

    public JSONObject toJsonObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("displayName", displayName);
            obj.put("brandLabel", brandLabel);
            obj.put("model", model);
            obj.put("brand", brand);
            obj.put("manufacturer", manufacturer);
            obj.put("device", device);
            obj.put("socModel", socModel);
            obj.put("glRenderer", glRenderer);
            obj.put("ramTotalMb", ramTotalMb);
            obj.put("targetRefreshRate", targetRefreshRate);
            obj.put("androidSdkVersion", androidSdkVersion);
        } catch (JSONException ignored) {}
        return obj;
    }

    @Override
    public String toString() {
        return displayName + " [" + model + " / " + brand + " / " + socModel + " / " + (ramTotalMb / 1024) + "GB RAM / " + targetRefreshRate + "Hz]";
    }
}
