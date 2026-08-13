package com.gamebooster.app.feature.bypass_charging;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

public class BypassChargingManager {

    private static final String TAG = "BypassChargingManager";
    private static final String PREF_NAME = "bypass_charging_prefs";
    private static final String KEY_ENABLED = "bypass_charging_enabled";

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

        if (manufacturer.contains("asus") || brand.contains("asus") || model.contains("rog")) {
            currentStrategy = new AsusRogBypassStrategy();
        } else if (manufacturer.contains("nubia") || brand.contains("redmagic") || model.contains("redmagic")) {
            currentStrategy = new RedMagicBypassStrategy();
        } else if (manufacturer.contains("xiaomi") || manufacturer.contains("poco") || manufacturer.contains("redmi") || brand.contains("xiaomi")) {
            currentStrategy = new XiaomiBypassStrategy();
        } else if (manufacturer.contains("samsung") || brand.contains("samsung")) {
            currentStrategy = new SamsungBypassStrategy();
        } else if (manufacturer.contains("oneplus") || manufacturer.contains("oppo") || manufacturer.contains("realme") || brand.contains("oneplus")) {
            currentStrategy = new OnePlusOppoBypassStrategy();
        } else if (manufacturer.contains("infinix") || manufacturer.contains("tecno") || manufacturer.contains("transsion") || brand.contains("infinix")) {
            currentStrategy = new InfinixTecnoBypassStrategy();
        } else if (manufacturer.contains("sony") || brand.contains("sony")) {
            currentStrategy = new SonyBypassStrategy();
        } else {
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

    public String enableBypassCharging(Context context) {
        String result = enableBypassCharging();
        if (context != null && isSuccessful(result)) setBypassEnabled(context, true);
        return result;
    }

    public String disableBypassCharging(Context context) {
        String result = disableBypassCharging();
        if (context != null && isSuccessful(result)) setBypassEnabled(context, false);
        return result;
    }

    public boolean isBypassEnabled(Context context) {
        if (context == null) return false;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ENABLED, false);
    }

    public void setBypassEnabled(Context context, boolean enabled) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public String toggleBypassCharging(Context context) {
        boolean nextState = !isBypassEnabled(context);
        if (nextState) {
            return enableBypassCharging(context);
        } else {
            return disableBypassCharging(context);
        }
    }

    public void restoreSavedState(Context context) {
        if (context == null) return;
        if (isBypassEnabled(context)) {
            enableBypassCharging(context);
        }
    }

    public String getBypassStatus() {
        if (currentStrategy == null) initStrategy();
        return currentStrategy.getBypassStatus();
    }

    private static boolean isSuccessful(String result) {
        if (result == null) return false;
        String normalized = result.trim().toLowerCase();
        return !normalized.startsWith("error") && !normalized.contains("not verified");
    }
}
