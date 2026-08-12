package com.gamebooster.app.feature.performance.thermal;

import android.os.Build;
import android.util.Log;

public class ThermalManager {

    private static final String TAG = "ThermalManager";

    private static ThermalManager instance;
    private ThermalInterface currentStrategy;

    private ThermalManager() {
        initStrategy();
    }

    public static synchronized ThermalManager getInstance() {
        if (instance == null) {
            instance = new ThermalManager();
        }
        return instance;
    }

    private void initStrategy() {
        String manufacturer = Build.MANUFACTURER != null ? Build.MANUFACTURER.toLowerCase() : "";
        String brand = Build.BRAND != null ? Build.BRAND.toLowerCase() : "";
        String model = Build.MODEL != null ? Build.MODEL.toLowerCase() : "";

        Log.d(TAG, "Detecting Thermal Strategy for Manufacturer=" + manufacturer + " Brand=" + brand + " Model=" + model);

        if (manufacturer.contains("samsung") || brand.contains("samsung")) {
            currentStrategy = new SamsungThermalStrategy();
        } else if (manufacturer.contains("xiaomi") || manufacturer.contains("poco") || manufacturer.contains("redmi") || brand.contains("xiaomi") || brand.contains("poco")) {
            currentStrategy = new XiaomiThermalStrategy();
        } else if (manufacturer.contains("asus") || brand.contains("asus") || model.contains("rog")) {
            currentStrategy = new AsusRogThermalStrategy();
        } else {
            currentStrategy = new GenericThermalStrategy();
        }

        Log.d(TAG, "Selected Thermal Strategy: " + currentStrategy.getStrategyName());
    }

    public ThermalInterface getCurrentStrategy() {
        return currentStrategy;
    }

    public String applyThermalOptimization() {
        if (currentStrategy == null) initStrategy();
        return currentStrategy.applyThermalOptimization();
    }

    public String resetThermalSettings() {
        if (currentStrategy == null) initStrategy();
        return currentStrategy.resetThermalSettings();
    }
}
