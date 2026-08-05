package com.gamebooster.app.core.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gamebooster.app.core.profile.InputProfile;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * Manages device-level input, touch, and gyroscope property tuning.
 * Backs up pre-existing system state before applying changes, and provides
 * complete restoration on app exit, profile change, or uninstall.
 */
public class SettingsManager {

    private static final String TAG = "SettingsManager";
    private static final String PREF_NAME = "precision_aim_settings_backup";
    private static final String KEY_IS_TUNED = "is_device_tuned";

    private final SharedPreferences prefs;

    // Keys tuned by Precision Aim
    public static final String KEY_MAX_EVENTS_PER_SEC = "debug.input.max_events_per_sec";
    public static final String KEY_VIEW_TOUCH_SLOP = "view.touch_slop";
    public static final String KEY_TOUCH_SLOP_REDUCTION = "touch_slop_reduction";
    public static final String KEY_GYRO_RATE = "debug.sensor.gyro.rate";
    public static final String KEY_POINTER_SPEED = "pointer_speed";
    public static final String KEY_PRESSURE_SCALE = "persist.sys.touch.pressure.scale";

    public SettingsManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Reads current system property value via Shizuku.
     */
    public String getSystemProperty(String key) {
        try {
            String output = ShizukuExecutor.executeShizukuCommand("getprop " + key);
            if (output != null && !output.startsWith("ERROR")) {
                return output.trim();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting system property " + key, e);
        }
        return "";
    }

    /**
     * Reads current system setting value.
     */
    public String getSystemSetting(String table, String key) {
        try {
            String output = ShizukuExecutor.executeShizukuCommand("settings get " + table + " " + key);
            if (output != null && !output.startsWith("ERROR") && !output.equalsIgnoreCase("null")) {
                return output.trim();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading setting " + table + "/" + key, e);
        }
        return "";
    }

    /**
     * Backs up current initial settings if not already backed up.
     */
    public void backupOriginalValues() {
        if (prefs.getBoolean("has_backed_up", false)) {
            Log.d(TAG, "Original values already backed up.");
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("backup_" + KEY_MAX_EVENTS_PER_SEC, getSystemProperty(KEY_MAX_EVENTS_PER_SEC));
        editor.putString("backup_" + KEY_VIEW_TOUCH_SLOP, getSystemProperty(KEY_VIEW_TOUCH_SLOP));
        editor.putString("backup_" + KEY_TOUCH_SLOP_REDUCTION, getSystemProperty(KEY_TOUCH_SLOP_REDUCTION));
        editor.putString("backup_" + KEY_GYRO_RATE, getSystemProperty(KEY_GYRO_RATE));
        editor.putString("backup_" + KEY_POINTER_SPEED, getSystemSetting("system", KEY_POINTER_SPEED));
        editor.putString("backup_" + KEY_PRESSURE_SCALE, getSystemProperty(KEY_PRESSURE_SCALE));
        editor.putBoolean("has_backed_up", true);
        editor.apply();
        Log.i(TAG, "Original system properties successfully backed up.");
    }

    /**
     * Applies a given InputProfile to system properties via Shizuku.
     */
    public boolean applyProfile(InputProfile profile) {
        if (!ShizukuExecutor.hasShizukuPermission()) {
            Log.w(TAG, "Cannot apply profile: Shizuku permission missing.");
            return false;
        }

        backupOriginalValues();

        Log.i(TAG, "Applying tuning profile: " + profile.getProfileName());

        ShizukuExecutor.executeShizukuCommand("setprop " + KEY_MAX_EVENTS_PER_SEC + " " + profile.getMaxEventsPerSec());
        ShizukuExecutor.executeShizukuCommand("setprop " + KEY_VIEW_TOUCH_SLOP + " " + profile.getTouchSlop());
        ShizukuExecutor.executeShizukuCommand("setprop " + KEY_TOUCH_SLOP_REDUCTION + " " + profile.getTouchSlopReduction());
        ShizukuExecutor.executeShizukuCommand("setprop " + KEY_GYRO_RATE + " " + profile.getGyroRate());
        ShizukuExecutor.executeShizukuCommand("settings put system " + KEY_POINTER_SPEED + " " + profile.getPointerSpeed());
        ShizukuExecutor.executeShizukuCommand("setprop " + KEY_PRESSURE_SCALE + " " + profile.getPressureScale());

        prefs.edit().putBoolean(KEY_IS_TUNED, true).apply();
        return true;
    }

    /**
     * Restores all tuned system properties and settings back to original initial values.
     */
    public boolean restoreOriginalValues() {
        if (!ShizukuExecutor.hasShizukuPermission()) {
            Log.w(TAG, "Cannot restore values: Shizuku permission missing.");
            return false;
        }

        if (!prefs.getBoolean("has_backed_up", false)) {
            Log.d(TAG, "No backup found to restore.");
            return true;
        }

        Log.i(TAG, "Restoring original system input settings...");

        restoreProp(KEY_MAX_EVENTS_PER_SEC);
        restoreProp(KEY_VIEW_TOUCH_SLOP);
        restoreProp(KEY_TOUCH_SLOP_REDUCTION);
        restoreProp(KEY_GYRO_RATE);
        restoreSetting("system", KEY_POINTER_SPEED);
        restoreProp(KEY_PRESSURE_SCALE);

        prefs.edit().putBoolean(KEY_IS_TUNED, false).apply();
        Log.i(TAG, "All system settings reverted to original defaults.");
        return true;
    }

    private void restoreProp(String key) {
        String original = prefs.getString("backup_" + key, "");
        if (original != null) {
            ShizukuExecutor.executeShizukuCommand("setprop " + key + " \"" + original + "\"");
        }
    }

    private void restoreSetting(String table, String key) {
        String original = prefs.getString("backup_" + key, "");
        if (original != null && !original.isEmpty()) {
            ShizukuExecutor.executeShizukuCommand("settings put " + table + " " + key + " " + original);
        }
    }

    public boolean isDeviceTuned() {
        return prefs.getBoolean(KEY_IS_TUNED, false);
    }
}
