package com.gamebooster.app.spoofer;

import org.junit.Test;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;

public class SpoofProfileRegistryTest {

    @Test
    public void testRegistryHasProfiles() {
        Map<String, SpoofProfile> profiles = SpoofProfileRegistry.getAllProfiles();
        assertNotNull(profiles);
        assertFalse(profiles.isEmpty());
        assertTrue(profiles.size() >= 5);
    }

    @Test
    public void testProfileById() {
        SpoofProfile rog = SpoofProfileRegistry.getById("asus_rog8_pro");
        assertNotNull(rog);
        assertEquals("asus", rog.manufacturer);
        assertEquals("Adreno (TM) 750", rog.glRenderer);

        SpoofProfile s26 = SpoofProfileRegistry.getById("samsung_s26_ultra");
        assertNotNull(s26);
        assertEquals("samsung", s26.manufacturer);
    }

    @Test
    public void testProfileFieldsComplete() {
        for (SpoofProfile profile : SpoofProfileRegistry.getAllProfiles().values()) {
            assertNotNull("ID should not be null", profile.id);
            assertNotNull("Model should not be null", profile.model);
            assertNotNull("Manufacturer should not be null", profile.manufacturer);
            assertNotNull("GL Renderer should not be null", profile.glRenderer);
            assertNotNull("GL Vendor should not be null", profile.glVendor);
        }
    }
}
