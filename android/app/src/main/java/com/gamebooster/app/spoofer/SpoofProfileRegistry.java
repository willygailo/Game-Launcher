package com.gamebooster.app.spoofer;

import com.gamebooster.app.spoofer.brands.AppleProfiles;
import com.gamebooster.app.spoofer.brands.AsusRogProfiles;
import com.gamebooster.app.spoofer.brands.BlackSharkProfiles;
import com.gamebooster.app.spoofer.brands.NubiaProfiles;
import com.gamebooster.app.spoofer.brands.OnePlusProfiles;
import com.gamebooster.app.spoofer.brands.OppoProfiles;
import com.gamebooster.app.spoofer.brands.RealmeProfiles;
import com.gamebooster.app.spoofer.brands.SamsungProfiles;
import com.gamebooster.app.spoofer.brands.VivoProfiles;
import com.gamebooster.app.spoofer.brands.XiaomiProfiles;

import com.gamebooster.app.spoofer.brands.InfinixProfiles;
import com.gamebooster.app.spoofer.brands.TecnoProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SpoofProfileRegistry — Central aggregator for all per-brand device spoof profiles.
 *
 * Loads profiles from all brand classes and provides lookup methods by ID,
 * brand name, or as a complete flat map.
 */
public class SpoofProfileRegistry {

    /** Flat map: profile ID → SpoofProfile */
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
        registerBrand(InfinixProfiles.getProfiles());
        registerBrand(TecnoProfiles.getProfiles());
        registerBrand(AppleProfiles.getProfiles());
        registerBrand(NubiaProfiles.getProfiles());
        registerBrand(BlackSharkProfiles.getProfiles());
    }

    private static void registerBrand(List<SpoofProfile> profiles) {
        for (SpoofProfile p : profiles) {
            ALL_BY_ID.put(p.id, p);
            ALL_BY_BRAND
                .computeIfAbsent(p.brandLabel, k -> new ArrayList<>())
                .add(p);
        }
    }

    /**
     * Get a profile by its unique ID.
     * @return the SpoofProfile, or null if not found.
     */
    public static SpoofProfile getById(String id) {
        return ALL_BY_ID.get(id);
    }

    /**
     * Get all profiles for a given brand label (e.g. "Samsung", "Realme").
     * @return unmodifiable list, or empty list if brand not found.
     */
    public static List<SpoofProfile> getByBrand(String brandLabel) {
        List<SpoofProfile> result = ALL_BY_BRAND.get(brandLabel);
        return result != null ? Collections.unmodifiableList(result) : Collections.emptyList();
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
     * Validates that all registered brand profiles meet mandatory non-null and valid data contracts.
     * @return true if all registered profiles are valid, false otherwise.
     */
    public static boolean validateRegistryIntegrity() {
        if (ALL_BY_ID.isEmpty()) return false;
        for (SpoofProfile p : ALL_BY_ID.values()) {
            if (p.id == null || p.id.trim().isEmpty() ||
                p.displayName == null || p.displayName.trim().isEmpty() ||
                p.brandLabel == null || p.brandLabel.trim().isEmpty() ||
                p.model == null || p.model.trim().isEmpty() ||
                p.brand == null || p.manufacturer == null ||
                p.hardware == null || p.glRenderer == null) {
                return false;
            }
        }
        return true;
    }
}

