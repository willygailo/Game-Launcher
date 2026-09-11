package com.gamebooster.app.spoofer;

import android.util.Log;
import com.gamebooster.app.spoofer.brands.AppleProfiles;
import com.gamebooster.app.spoofer.brands.AsusRogProfiles;
import com.gamebooster.app.spoofer.brands.BlackSharkProfiles;
import com.gamebooster.app.spoofer.brands.LenovoLegionProfiles;
import com.gamebooster.app.spoofer.brands.NubiaProfiles;
import com.gamebooster.app.spoofer.brands.OnePlusProfiles;
import com.gamebooster.app.spoofer.brands.OppoProfiles;
import com.gamebooster.app.spoofer.brands.RealmeProfiles;
import com.gamebooster.app.spoofer.brands.SamsungProfiles;
import com.gamebooster.app.spoofer.brands.VivoProfiles;
import com.gamebooster.app.spoofer.brands.XiaomiProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SpoofProfileRegistry — Central aggregator for all per-brand device spoof profiles.
 *
 * Loads profiles from all 11 brand modules with strict zero-duplication guarantees.
 * Provides lookup methods by unique ID, brand name, or as a complete flat map.
 */
public class SpoofProfileRegistry {

    private static final String TAG = "SpoofProfileRegistry";

    /** Flat map: profile ID → SpoofProfile (Guaranteed strictly unique keys) */
    private static final Map<String, SpoofProfile> ALL_BY_ID = new LinkedHashMap<>();

    /** Brand map: brand label → list of SpoofProfile */
    private static final Map<String, List<SpoofProfile>> ALL_BY_BRAND = new LinkedHashMap<>();

    static {
        registerBrand(SamsungProfiles.getProfiles());
        registerBrand(RealmeProfiles.getProfiles());
        registerBrand(AsusRogProfiles.getProfiles());
        registerBrand(XiaomiProfiles.getProfiles());
        registerBrand(OnePlusProfiles.getProfiles());
        registerBrand(OppoProfiles.getProfiles());
        registerBrand(VivoProfiles.getProfiles());
        registerBrand(AppleProfiles.getProfiles());
        registerBrand(NubiaProfiles.getProfiles());
        registerBrand(BlackSharkProfiles.getProfiles());
        registerBrand(LenovoLegionProfiles.getProfiles());
    }

    private static void registerBrand(List<SpoofProfile> profiles) {
        if (profiles == null) return;
        for (SpoofProfile p : profiles) {
            if (p == null || p.id == null) continue;
            if (ALL_BY_ID.containsKey(p.id)) {
                Log.w(TAG, "Duplicate profile ID detected and ignored: " + p.id + " (" + p.displayName + ")");
                continue;
            }
            ALL_BY_ID.put(p.id, p);
            ALL_BY_BRAND
                .computeIfAbsent(p.brandLabel != null ? p.brandLabel : "Generic", k -> new ArrayList<>())
                .add(p);
        }
    }

    /**
     * Get a profile by its unique ID.
     * @return the SpoofProfile, or null if not found.
     */
    public static SpoofProfile getById(String id) {
        if (id == null) return null;
        return ALL_BY_ID.get(id.trim());
    }

    /**
     * Get all profiles for a given brand label (e.g. "ASUS ROG", "Samsung", "Realme").
     * Supports exact matches, case-insensitive matches, and substring/alias matching.
     * @return unmodifiable list, or empty list if brand not found.
     */
    public static List<SpoofProfile> getByBrand(String brandLabel) {
        if (brandLabel == null || brandLabel.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String trimmed = brandLabel.trim();

        // 1. Direct Map Lookup
        List<SpoofProfile> result = ALL_BY_BRAND.get(trimmed);
        if (result != null && !result.isEmpty()) {
            return Collections.unmodifiableList(result);
        }

        // 2. Case-Insensitive Key Matching
        for (Map.Entry<String, List<SpoofProfile>> entry : ALL_BY_BRAND.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(trimmed)) {
                return Collections.unmodifiableList(entry.getValue());
            }
        }

        // 3. Substring & Alias Matching (e.g. "rog" -> "ASUS ROG", "asus" -> "ASUS ROG")
        String query = trimmed.toLowerCase();
        for (Map.Entry<String, List<SpoofProfile>> entry : ALL_BY_BRAND.entrySet()) {
            String keyLower = entry.getKey().toLowerCase();
            if (keyLower.contains(query) || query.contains(keyLower) ||
                    (query.contains("rog") && keyLower.contains("asus")) ||
                    (query.contains("asus") && keyLower.contains("rog"))) {
                return Collections.unmodifiableList(entry.getValue());
            }
        }

        // 4. Per-profile fallback inspection
        List<SpoofProfile> matches = new ArrayList<>();
        for (SpoofProfile p : ALL_BY_ID.values()) {
            if ((p.brandLabel != null && p.brandLabel.toLowerCase().contains(query)) ||
                    (p.brand != null && p.brand.toLowerCase().contains(query)) ||
                    (p.displayName != null && p.displayName.toLowerCase().contains(query)) ||
                    (p.model != null && p.model.toLowerCase().contains(query))) {
                matches.add(p);
            }
        }

        return Collections.unmodifiableList(matches);
    }

    /**
     * Get all profiles as a flat ID-keyed map.
     */
    public static Map<String, SpoofProfile> getAllProfiles() {
        return Collections.unmodifiableMap(ALL_BY_ID);
    }

    /**
     * Get list of all registered brand names in insertion order.
     */
    public static List<String> getBrandNames() {
        return Collections.unmodifiableList(new ArrayList<>(ALL_BY_BRAND.keySet()));
    }

    /**
     * Get the full brand→profiles map.
     */
    public static Map<String, List<SpoofProfile>> getAllByBrand() {
        return Collections.unmodifiableMap(ALL_BY_BRAND);
    }

    /**
     * Get total number of registered profiles across all brands.
     */
    public static int getTotalCount() {
        return ALL_BY_ID.size();
    }

    /**
     * Get first profile in a brand (fallback when no specific ID is requested).
     */
    public static SpoofProfile getFirstByBrand(String brandLabel) {
        List<SpoofProfile> list = ALL_BY_BRAND.get(brandLabel);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    /**
     * Get default flagship profile (Samsung Galaxy S25 Ultra -> ROG 9 Pro -> REDMAGIC 10 Pro+).
     */
    public static SpoofProfile getDefaultProfile() {
        SpoofProfile p = getById("samsung_s25_ultra");
        if (p == null) p = getById("asus_rog9_pro");
        if (p == null) p = getById("redmagic_10_pro_plus");
        if (p == null && !ALL_BY_ID.isEmpty()) {
            return ALL_BY_ID.values().iterator().next();
        }
        return p;
    }
}
