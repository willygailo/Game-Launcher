package com.gamebooster.app.feature.spoofer;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.feature.performance.device.DevicePerformanceCapabilities;
import com.gamebooster.app.feature.performance.display.DisplayOverrideController;

import java.util.List;
import java.util.Map;

/**
 * Safe compatibility boundary for the old device-profile feature.
 *
 * <p>Changing build fingerprints, model identifiers, GPU details, or other
 * device identity to influence an online game's feature gates is disabled. A
 * selected profile is persisted as a launcher-owned label and its refresh-rate
 * preference is resolved against the real panel modes reported by Android.</p>
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
        if (context == null) return false;
        SpoofProfile profile = null;
        String customId = packageName == null ? null
                : SpoofPreferences.getGameSpoofProfileId(context, packageName);
        if (customId != null) profile = getProfileById(customId);
        if (profile == null) {
            String activeId = SpoofPreferences.getActiveProfileId(context);
            if (activeId != null) profile = getProfileById(activeId);
        }
        return profile != null && applyProfile(context, profile, packageName);
    }

    public static boolean applyProfile(Context context, SpoofProfile profile, String packageName) {
        if (context == null || profile == null) return false;

        // Persist an app-only profile selection so the launcher can restore it.
        SpoofPreferences.setSpoofEnabled(context, true);
        SpoofPreferences.setActiveProfileId(context, profile.id);
        if (packageName != null && !packageName.trim().isEmpty()) {
            SpoofPreferences.setGameSpoofProfileId(context, packageName, profile.id);
        }

        int requestedHz = Math.max(30, profile.targetRefreshRate);
        int supportedHz = DevicePerformanceCapabilities.detect(context)
                .resolveRefreshRate(requestedHz);
        DisplayOverrideController.Result display =
                DisplayOverrideController.applyDisplayRate(context, supportedHz, packageName);
        Log.i(TAG, "Saved app-only device profile " + profile.displayName
                + "; native display request: " + display.message);
        // The local profile is applied even when Android requires Shizuku or
        // WRITE_SETTINGS for the optional display request.
        return true;
    }

    public static void applyGameGraphicsSpoof(Context context, String packageName, int targetHz) {
        if (context == null || packageName == null) return;
        int supportedHz = DevicePerformanceCapabilities.detect(context)
                .resolveRefreshRate(Math.max(30, targetHz));
        DisplayOverrideController.applyGameProfile(context, packageName, supportedHz);
        Log.i(TAG, "Applied supported Android Game Mode request for " + packageName
                + " at up to " + supportedHz + "Hz; no spoofing or game-file edits performed.");
    }

    public static void resetSpoofing() {
        Log.i(TAG, "No device identity override is active.");
    }

    /** Restores the temporary display/Game Mode values owned by the launcher. */
    public static void resetSpoofing(Context context) {
        if (context != null) {
            DisplayOverrideController.restore(context);
            SpoofPreferences.setSpoofEnabled(context, false);
            SpoofPreferences.clearActiveProfile(context);
        }
        resetSpoofing();
    }
}
