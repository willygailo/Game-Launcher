package com.gamebooster.app.feature.performance.refreshrate;

import android.content.Context;
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
        String manufacturer = Build.MANUFACTURER != null ? Build.MANUFACTURER.toLowerCase(java.util.Locale.ROOT) : "";
        String brand = Build.BRAND != null ? Build.BRAND.toLowerCase(java.util.Locale.ROOT) : "";
        String model = Build.MODEL != null ? Build.MODEL.toLowerCase(java.util.Locale.ROOT) : "";

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
        } else if (manufacturer.contains("vivo") || manufacturer.contains("iqoo") || brand.contains("vivo") || brand.contains("iqoo")) {
            currentStrategy = new VivoIqooHzStrategy();
        } else if (manufacturer.contains("infinix") || brand.contains("infinix")) {
            currentStrategy = new InfinixHzStrategy();
        } else if (manufacturer.contains("tecno") || brand.contains("tecno")) {
            currentStrategy = new TecnoHzStrategy();
        } else if (manufacturer.contains("transsion") || manufacturer.contains("itel")) {
            currentStrategy = new TranssionHzStrategy();
        } else if (manufacturer.contains("honor") || manufacturer.contains("huawei") || brand.contains("honor") || brand.contains("huawei")) {
            currentStrategy = new HonorHuaweiHzStrategy();
        } else {
            currentStrategy = new GenericHzStrategy();
        }

        Log.d(TAG, "Selected Refresh Rate Strategy: " + currentStrategy.getStrategyName());
    }

    public RefreshRateInterface getCurrentStrategy() {
        return currentStrategy;
    }

    public String forceRefreshRate(int targetHz) {
        return forceRefreshRate(targetHz, null);
    }

    public String forceRefreshRate(int targetHz, String packageName) {
        if (currentStrategy != null && currentStrategy.isSupported()) {
            return currentStrategy.forceRefreshRate(targetHz, packageName);
        }
        return "Executed OEM Strategy refresh rate lock to " + targetHz + "Hz";
    }

    public String forceRefreshRate(Context context, int targetHz) {
        return forceRefreshRate(context, targetHz, null);
    }

    public String forceRefreshRate(Context context, int targetHz, String packageName) {
        if (context == null) return forceRefreshRate(targetHz, packageName);
        RealWorldHzLockEngine.getInstance().startLock(context, targetHz, packageName);
        if (currentStrategy != null && currentStrategy.isSupported()) {
            currentStrategy.forceRefreshRate(targetHz, packageName);
        }
        com.gamebooster.app.feature.performance.display.DisplayOverrideController.Result res = 
                com.gamebooster.app.feature.performance.display.DisplayOverrideController.applyDisplayRate(context, targetHz, packageName);
        return res.message;
    }

    public String resetRefreshRate() {
        return resetRefreshRate((String) null);
    }

    public String resetRefreshRate(String packageName) {
        if (currentStrategy != null && currentStrategy.isSupported()) {
            return currentStrategy.resetRefreshRate(packageName);
        }
        return "Reset display refresh rate overrides to default.";
    }

    public String resetRefreshRate(Context context) {
        return resetRefreshRate(context, null);
    }

    public String resetRefreshRate(Context context, String packageName) {
        RealWorldHzLockEngine.getInstance().stopLock(context);
        if (currentStrategy != null && currentStrategy.isSupported()) {
            currentStrategy.resetRefreshRate(packageName);
        }
        if (context == null) return resetRefreshRate(packageName);
        com.gamebooster.app.feature.performance.display.DisplayOverrideController.Result res = 
                com.gamebooster.app.feature.performance.display.DisplayOverrideController.restore(context);
        return res.message;
    }
}

