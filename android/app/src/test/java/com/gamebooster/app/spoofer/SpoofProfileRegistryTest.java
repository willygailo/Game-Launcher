package com.gamebooster.app.spoofer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import java.util.List;
import java.util.Map;

public class SpoofProfileRegistryTest {

    @Test
    public void testAllProfilesIntegrity() {
        Map<String, SpoofProfile> allProfiles = SpoofProfileRegistry.getAllProfiles();
        assertNotNull(allProfiles);
        assertFalse(allProfiles.isEmpty());
        assertTrue("Expected at least 30 gaming profiles", allProfiles.size() >= 30);

        for (Map.Entry<String, SpoofProfile> entry : allProfiles.entrySet()) {
            String id = entry.getKey();
            SpoofProfile p = entry.getValue();

            assertNotNull("ID should match entry key", p.id);
            assertNotNull("DisplayName must not be null for " + id, p.displayName);
            assertNotNull("BrandLabel must not be null for " + id, p.brandLabel);
            assertNotNull("Model must not be null for " + id, p.model);
            assertNotNull("Brand must not be null for " + id, p.brand);
            assertNotNull("Manufacturer must not be null for " + id, p.manufacturer);
            assertNotNull("SocModel must not be null for " + id, p.socModel);
            assertNotNull("GlRenderer must not be null for " + id, p.glRenderer);
            assertNotNull("Fingerprint must not be null for " + id, p.fingerprint);

            assertTrue("RAM must be > 0 for " + id, p.ramTotalMb > 0);
            assertTrue("Refresh rate must be between 60 and 240 for " + id, p.maxRefreshRateHz >= 60 && p.maxRefreshRateHz <= 240);
            assertTrue("CPU Cores must be at least 4 for " + id, p.cpuCores >= 4);
        }
    }

    @Test
    public void testBrandRegistration() {
        List<String> brands = SpoofProfileRegistry.getBrandNames();
        assertNotNull(brands);
        assertTrue(brands.contains("Samsung"));
        assertTrue(brands.contains("ASUS ROG"));
        assertTrue(brands.contains("Vivo"));
        assertTrue(brands.contains("Realme"));
        assertTrue(brands.contains("Xiaomi"));
        assertTrue(brands.contains("OnePlus"));
        assertTrue(brands.contains("OPPO"));
        assertTrue(brands.contains("Nubia"));
        assertTrue(brands.contains("Black Shark"));
        assertTrue(brands.contains("Lenovo Legion"));
        assertTrue(brands.contains("Apple"));
    }
}
