package com.gamebooster.app.spoofer;

import org.junit.Test;
import java.util.Map;
import static org.junit.Assert.*;

public class SpoofProfileRegistryTest {

    @Test
    public void testRegistryNotEmpty() {
        Map<String, SpoofProfile> profiles = SpoofProfileRegistry.getAllProfiles();
        assertNotNull(profiles);
        assertFalse(profiles.isEmpty());
        assertTrue(SpoofProfileRegistry.getTotalCount() >= 10);
    }

    @Test
    public void testFlagshipBrandsExist() {
        assertFalse(SpoofProfileRegistry.getByBrand("ASUS ROG").isEmpty());
        assertFalse(SpoofProfileRegistry.getByBrand("Samsung").isEmpty());
        assertFalse(SpoofProfileRegistry.getByBrand("Xiaomi").isEmpty());
        assertFalse(SpoofProfileRegistry.getByBrand("OnePlus").isEmpty());
        assertFalse(SpoofProfileRegistry.getByBrand("Apple").isEmpty());
    }

    @Test
    public void testProfileIntegrity() {
        for (SpoofProfile p : SpoofProfileRegistry.getAllProfiles().values()) {
            assertNotNull(p.id);
            assertNotNull(p.displayName);
            assertNotNull(p.brand);
            assertNotNull(p.model);
            assertNotNull(p.socModel);
            assertNotNull(p.glRenderer);
            assertTrue(p.ramTotalMb > 0);
            assertTrue(p.sdkInt >= 24);
        }
    }
}
