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

        // Do not select a strategy from brand strings alone. The old implementation guessed
        // vendor settings and then fell back to generic sysfs writes, which could disable
        // charging or alter the power path on an unrelated model. Add an OEM strategy only after
        // it has been verified against the exact model and firmware, including restore checks.
        currentStrategy = new UnsupportedBypassStrategy();

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
