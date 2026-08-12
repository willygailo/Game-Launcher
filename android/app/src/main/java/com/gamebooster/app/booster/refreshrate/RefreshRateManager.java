package com.gamebooster.app.booster.refreshrate;

import android.os.Build;
import android.util.Log;

public class RefreshRateManager {

    private static final String TAG = "RefreshRateManager";

    private static RefreshRateManager instance;
    private RefreshRateInterface currentStrategy;

    private RefreshRateManager() {
        initStrategy();
    }

    public static synchronized RefreshRateManager getInstance() {
        if (instance == null) {
            instance = new RefreshRateManager();
        }
        return instance;
    }

    private void initStrategy() {
        String manufacturer = Build.MANUFACTURER != null ? Build.MANUFACTURER.toLowerCase() : "";
        String brand = Build.BRAND != null ? Build.BRAND.toLowerCase() : "";
        String model = Build.MODEL != null ? Build.MODEL.toLowerCase() : "";

        Log.d(TAG, "Detecting Refresh Rate Strategy for Manufacturer=" + manufacturer + " Brand=" + brand + " Model=" + model);

        if (manufacturer.contains("samsung") || brand.contains("samsung")) {
            currentStrategy = new SamsungHzStrategy();
        } else if (manufacturer.contains("xiaomi") || manufacturer.contains("poco") || manufacturer.contains("redmi") || brand.contains("xiaomi") || brand.contains("poco")) {
            currentStrategy = new XiaomiHzStrategy();
        } else if (manufacturer.contains("asus") || brand.contains("asus") || model.contains("rog")) {
            currentStrategy = new AsusRogHzStrategy();
        } else if (manufacturer.contains("oneplus") || manufacturer.contains("oppo") || manufacturer.contains("realme") || brand.contains("oneplus") || brand.contains("oppo")) {
            currentStrategy = new OnePlusOppoHzStrategy();
        } else if (manufacturer.contains("nubia") || manufacturer.contains("zte") || brand.contains("redmagic") || model.contains("redmagic")) {
            currentStrategy = new RedMagicHzStrategy();
        } else {
            currentStrategy = new GenericHzStrategy();
        }

        Log.d(TAG, "Selected Refresh Rate Strategy: " + currentStrategy.getStrategyName());
    }

    public RefreshRateInterface getCurrentStrategy() {
        return currentStrategy;
    }

    public String forceRefreshRate(int targetHz) {
        return "ERROR: A Context is required to verify native display modes. Use DisplayOverrideController.";
    }

    public String resetRefreshRate() {
        return "ERROR: A Context is required to restore saved display settings. Use DisplayOverrideController.";
    }
}
