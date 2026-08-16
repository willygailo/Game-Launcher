package com.gamebooster.app.device;

import com.gamebooster.app.device.DeviceDetector;

public class DeviceSpecModel {
    private final String manufacturer;
    private final String model;
    private final String board;
    private final String hardware;
    private final String androidVersion;
    private final int sdkInt;
    private final String chipsetName;
    private final DeviceDetector.ChipsetVendor vendor;

    public DeviceSpecModel(String manufacturer, String model, String board, String hardware,
                           String androidVersion, int sdkInt, String chipsetName,
                           DeviceDetector.ChipsetVendor vendor) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.board = board;
        this.hardware = hardware;
        this.androidVersion = androidVersion;
        this.sdkInt = sdkInt;
        this.chipsetName = chipsetName;
        this.vendor = vendor;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getBoard() {
        return board;
    }

    public String getHardware() {
        return hardware;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public int getSdkInt() {
        return sdkInt;
    }

    public String getChipsetName() {
        return chipsetName;
    }

    public DeviceDetector.ChipsetVendor getVendor() {
        return vendor;
    }

    public String getFormattedSummary() {
        return manufacturer + " " + model + " (" + chipsetName + ")";
    }
}
