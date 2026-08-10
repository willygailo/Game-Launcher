package com.gamebooster.app.bypasscharging;

import android.os.Build;
import android.util.Log;

public class BypassChargingManager {

    private static final String TAG = "BypassChargingManager";

    private static BypassChargingManager instance;
    private BypassChargingInterface currentStrategy;

    private BypassChargingManager() {
        initStrategy();
    }

    public static synchronized BypassChargingManager getInstance() {
        if (instance == null) {
            instance = new BypassChargingManager();
        }
        return instance;
    }

    /**
     * Inspects manufacturer and device properties to select the optimal strategy.
     */
    private void initStrategy() {
        String manufacturer = Build.MANUFACTURER != null ? Build.MANUFACTURER.toLowerCase() : "";
        String brand = Build.BRAND != null ? Build.BRAND.toLowerCase() : "";
        String model = Build.MODEL != null ? Build.MODEL.toLowerCase() : "";

        Log.d(TAG, "Detecting device for Bypass Charging strategy... Manufacturer=" + manufacturer + " Brand=" + brand + " Model=" + model + " SDK=" + Build.VERSION.SDK_INT);

        if (manufacturer.contains("samsung") || brand.contains("samsung")) {
            currentStrategy = new SamsungBypassStrategy();
        } else if (manufacturer.contains("xiaomi") || manufacturer.contains("poco") || manufacturer.contains("redmi") || manufacturer.contains("blackshark") ||
                   brand.contains("xiaomi") || brand.contains("poco") || brand.contains("redmi")) {
            currentStrategy = new XiaomiBypassStrategy();
        } else if (manufacturer.contains("asus") || brand.contains("asus") || model.contains("rog")) {
            currentStrategy = new AsusRogBypassStrategy();
        } else if (manufacturer.contains("infinix") || manufacturer.contains("tecno") || manufacturer.contains("transsion") || manufacturer.contains("itel") ||
                   brand.contains("infinix") || brand.contains("tecno")) {
            currentStrategy = new InfinixTecnoBypassStrategy();
        } else if (manufacturer.contains("sony") || brand.contains("sony")) {
            currentStrategy = new SonyBypassStrategy();
        } else if (manufacturer.contains("nubia") || manufacturer.contains("zte") || brand.contains("redmagic") || model.contains("redmagic")) {
            currentStrategy = new RedMagicBypassStrategy();
        } else if (manufacturer.contains("oneplus") || manufacturer.contains("oppo") || manufacturer.contains("realme") ||
                   brand.contains("oneplus") || brand.contains("oppo") || brand.contains("realme")) {
            currentStrategy = new OnePlusOppoBypassStrategy();
        } else {
            currentStrategy = new GenericSysfsBypassStrategy();
        }

        // If specific strategy isn't supported, fall back to GenericSysfsBypassStrategy
        if (!currentStrategy.isSupported()) {
            Log.d(TAG, "Primary strategy (" + currentStrategy.getStrategyName() + ") not supported by nodes/settings. Falling back to GenericSysfsBypassStrategy.");
            currentStrategy = new GenericSysfsBypassStrategy();
        }

        Log.d(TAG, "Selected Bypass Charging Strategy: " + currentStrategy.getStrategyName());
    }

    public BypassChargingInterface getCurrentStrategy() {
        return currentStrategy;
    }

    public String enableBypassCharging() {
        if (currentStrategy == null) initStrategy();
        return currentStrategy.enableBypassCharging();
    }

    public String disableBypassCharging() {
        if (currentStrategy == null) initStrategy();
        return currentStrategy.disableBypassCharging();
    }

    public String getBypassStatus() {
        if (currentStrategy == null) initStrategy();
        return currentStrategy.getBypassStatus();
    }
}
