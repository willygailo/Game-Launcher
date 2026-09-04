package com.gamebooster.app.spoofer;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.gamebooster.app.device.DeviceDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * SpoofValidator — Validates that a spoof profile has been correctly applied
 * by reading back live system properties and comparing to expected profile values.
 *
 * Returns a detailed SpoofValidationResult with per-layer pass/fail status.
 */
public class SpoofValidator {

    private static final String TAG = "SpoofValidator";

    // ─────────────────────────────────────────────────────────────────────────
    //  Validation Result
    // ─────────────────────────────────────────────────────────────────────────

    public static class SpoofValidationResult {
        public boolean modelMatch;
        public boolean brandMatch;
        public boolean manufacturerMatch;
        public boolean hardwareMatch;
        public boolean fingerprintMatch;
        public boolean socModelMatch;
        public boolean androidVersionMatch;

        public final List<String> mismatches = new ArrayList<>();

        public boolean isFullyValid() {
            return modelMatch && brandMatch && manufacturerMatch
                    && hardwareMatch && fingerprintMatch;
        }

        public int passCount() {
            int count = 0;
            if (modelMatch) count++;
            if (brandMatch) count++;
            if (manufacturerMatch) count++;
            if (hardwareMatch) count++;
            if (fingerprintMatch) count++;
            if (socModelMatch) count++;
            if (androidVersionMatch) count++;
            return count;
        }

        public int totalChecks() { return 7; }

        @Override
        public String toString() {
            return "SpoofValidation [" + passCount() + "/" + totalChecks() + " passed]"
                    + (mismatches.isEmpty() ? " ✅ All OK" : " ⚠️ Mismatches: " + mismatches);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Validate
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Validates that the live Android Build fields match the expected SpoofProfile.
     * Uses Java reflection to read the current (possibly spoofed) Build values.
     *
     * @param context  Android context (unused currently, reserved for future prop reads)
     * @param profile  The SpoofProfile that was applied
     * @return SpoofValidationResult with per-field pass/fail and mismatch list
     */
    public static SpoofValidationResult validate(Context context, SpoofProfile profile) {
        SpoofValidationResult result = new SpoofValidationResult();
        if (profile == null) {
            result.mismatches.add("Profile is null — nothing to validate");
            return result;
        }

        // Model
        result.modelMatch = check("MODEL", Build.MODEL, profile.model, result);
        // Brand
        result.brandMatch = check("BRAND", Build.BRAND, profile.brand, result);
        // Manufacturer
        result.manufacturerMatch = check("MANUFACTURER", Build.MANUFACTURER, profile.manufacturer, result);
        // Hardware
        result.hardwareMatch = check("HARDWARE", Build.HARDWARE, profile.hardware, result);
        // Fingerprint
        result.fingerprintMatch = check("FINGERPRINT", Build.FINGERPRINT, profile.fingerprint, result);
        // SoC Model
        result.socModelMatch = check("SOC_MODEL", Build.SOC_MODEL, profile.socModel, result);
        // Android Version
        result.androidVersionMatch = check("VERSION.RELEASE", Build.VERSION.RELEASE, profile.androidVersion, result);

        Log.d(TAG, result.toString());
        return result;
    }

    /**
     * Pre-apply sanity check: evaluates GPU and SoC family compatibility.
     */
    public static SpoofSanityChecker.SanityResult checkPreApplySanity(
            DeviceDetector.ChipsetVendor deviceChipset, SpoofProfile profile,
            GameSpoofSafetyRegistry.RiskTier riskTier) {
        return SpoofSanityChecker.checkForGame(deviceChipset, profile, riskTier);
    }

    /**
     * Quick check: returns true if spoofing appears active (model changed from device default).
     */
    public static boolean isSpoofActive(SpoofProfile profile) {
        if (profile == null) return false;
        return Build.MODEL.equals(profile.model) && Build.BRAND.equals(profile.brand);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static boolean check(String fieldName, String actual, String expected,
                                  SpoofValidationResult result) {
        if (expected == null) return true; // No expectation set
        boolean match = expected.equalsIgnoreCase(actual);
        if (!match) {
            String msg = fieldName + ": expected=\"" + expected + "\" actual=\"" + actual + "\"";
            result.mismatches.add(msg);
            Log.w(TAG, "MISMATCH — " + msg);
        }
        return match;
    }
}
