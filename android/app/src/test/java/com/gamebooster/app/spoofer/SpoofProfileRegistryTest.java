package com.gamebooster.app.spoofer;

import org.junit.Test;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;

public class SpoofProfileRegistryTest {

    @Test
    public void testProfileRegistry_NonEmpty() {
        int total = SpoofProfileRegistry.getTotalCount();
        assertTrue("Expected at least 10 profiles, got " + total, total >= 10);
    }

    @Test
    public void testProfileRegistry_BrandNames() {
        List<String> brands = SpoofProfileRegistry.getBrandNames();
        assertNotNull(brands);
        assertTrue(brands.contains("Samsung"));
        assertTrue(brands.contains("ASUS ROG"));
        assertTrue(brands.contains("Xiaomi"));
        assertTrue(brands.contains("OnePlus"));
        assertTrue(brands.contains("Apple"));
    }

    @Test
    public void testProfileIntegrity_AllProfilesValid() {
        Map<String, SpoofProfile> all = SpoofProfileRegistry.getAllProfiles();
        for (Map.Entry<String, SpoofProfile> entry : all.entrySet()) {
            SpoofProfile p = entry.getValue();
            assertNotNull("Profile should not be null for id " + entry.getKey(), p);
            assertNotNull("Profile id missing", p.id);
            assertFalse("Profile id empty", p.id.trim().isEmpty());
            assertNotNull("Display name missing for " + p.id, p.displayName);
            assertNotNull("Model missing for " + p.id, p.model);
            assertNotNull("Brand missing for " + p.id, p.brand);
            assertNotNull("Manufacturer missing for " + p.id, p.manufacturer);
            assertNotNull("Fingerprint missing for " + p.id, p.fingerprint);
            assertTrue("SDK version should be >= 28 for " + p.id, p.sdkInt >= 28);
        }
    }

    @Test
    public void testGetById_KnownProfiles() {
        SpoofProfile rog = SpoofProfileRegistry.getById("asus_rog9_pro");
        assertNotNull(rog);
        assertEquals("ASUS", rog.manufacturer.toUpperCase());

        SpoofProfile s26 = SpoofProfileRegistry.getById("samsung_s26_ultra");
        assertNotNull(s26);
        assertEquals("samsung", s26.brand.toLowerCase());
    }
}
