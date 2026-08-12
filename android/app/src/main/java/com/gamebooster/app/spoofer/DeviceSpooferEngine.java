package com.gamebooster.app.spoofer;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.Map;

/**
 * Legacy compatibility boundary for device-profile features.
 *
 * <p>Changing build fingerprints, model identifiers, GPU details, or other
 * device identity to influence an online game's feature gates is disabled. It
 * is unreliable, can impair system integrity, and may violate game rules. The
 * launcher instead uses the real display capabilities reported by Android.</p>
 */
public final class DeviceSpooferEngine {
    private static final String TAG = "DeviceSpooferEngine";

    private DeviceSpooferEngine() { }

    public static Map<String, SpoofProfile> getAllProfiles() {
        return SpoofProfileRegistry.getAllProfiles();
    }

    public static Map<String, List<SpoofProfile>> getAllByBrand() {
        return SpoofProfileRegistry.getAllByBrand();
    }

    public static List<String> getBrandNames() {
        return SpoofProfileRegistry.getBrandNames();
    }

    public static SpoofProfile getProfileById(String id) {
        return SpoofProfileRegistry.getById(id);
    }

    public static List<SpoofProfile> getProfilesByBrand(String brand) {
        return SpoofProfileRegistry.getByBrand(brand);
    }

    /** No identity profile can be active. */
    public static String getActiveProfileId() {
        return null;
    }

    /** Kept only so old UI code can list legacy entries; it is never applied. */
    public static SpoofProfile getRecommendedProfile(String packageName) {
        return null;
    }

    public static boolean applySpoofing(Context context, String packageName) {
        Log.w(TAG, "Device identity changes are disabled for online-game safety.");
        return false;
    }

    public static boolean applyProfile(Context context, SpoofProfile profile, String packageName) {
        Log.w(TAG, "Device identity changes are disabled for online-game safety.");
        return false;
    }

    public static void applyGameGraphicsSpoof(Context context, String packageName, int targetHz) {
        Log.w(TAG, "Game graphics spoofing is disabled; use supported game settings instead.");
    }

    public static void resetSpoofing() {
        Log.i(TAG, "No device identity override is active.");
    }
}
