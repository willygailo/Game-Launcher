package com.gamebooster.app;

import com.gamebooster.app.spoofer.SpoofProfile;
import com.gamebooster.app.spoofer.SpoofProfileRegistry;
import org.junit.Test;
import java.util.Map;
import static org.junit.Assert.*;

public class SpoofProfileRegistryTest {

    @Test
    public void testRegistryContainsProfiles() {
        Map<String, SpoofProfile> all = SpoofProfileRegistry.getAllProfiles();
        assertNotNull(all);
        assertFalse(all.isEmpty());

        SpoofProfile s26 = SpoofProfileRegistry.getById("samsung_s26_ultra");
        assertNotNull(s26);
        assertEquals("samsung", s26.brand.toLowerCase());
        assertEquals(36, s26.sdkInt);
        assertEquals(185, s26.maxRefreshRateHz);

        SpoofProfile rog9 = SpoofProfileRegistry.getById("asus_rog9_pro_edition");
        assertNotNull(rog9);
        assertEquals(185, rog9.maxRefreshRateHz);

        SpoofProfile bs5 = SpoofProfileRegistry.getById("blackshark_5_pro");
        assertNotNull(bs5);
        assertEquals(144, bs5.maxRefreshRateHz);
    }
}
