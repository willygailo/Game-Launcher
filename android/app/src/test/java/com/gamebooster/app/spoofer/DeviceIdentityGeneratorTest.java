package com.gamebooster.app.spoofer;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DeviceIdentityGeneratorTest {

    @Test
    public void testUniqueIdentitiesAcrossAllRegisteredProfiles() {
        Map<String, SpoofProfile> allProfiles = SpoofProfileRegistry.getAllProfiles();
        assertNotNull(allProfiles);
        assertFalse(allProfiles.isEmpty());

        Set<String> profileIds = new HashSet<>();
        Set<String> androidIds = new HashSet<>();
        Set<String> serialNumbers = new HashSet<>();
        Set<String> wifiMacs = new HashSet<>();
        Set<String> btMacs = new HashSet<>();
        Set<String> oaids = new HashSet<>();
        Set<String> gsfIds = new HashSet<>();

        for (SpoofProfile profile : allProfiles.values()) {
            assertNotNull(profile);

            // Zero duplicate profile IDs
            assertTrue("Profile ID must be unique: " + profile.id, profileIds.add(profile.id));

            // 1. Android ID (16 lowercase hex characters)
            String androidId = profile.getAndroidId();
            assertNotNull(androidId);
            assertEquals(16, androidId.length());
            assertTrue("Android ID must be hex: " + androidId, androidId.matches("^[0-9a-f]{16}$"));
            assertTrue("Android IDs must be unique across different profiles", androidIds.add(androidId));

            // 2. Serial Number (non-empty, brand-accurate)
            String serial = profile.getSerialNumber();
            assertNotNull(serial);
            assertTrue(serial.length() >= 6);
            assertTrue("Serials must be unique across different profiles", serialNumbers.add(serial));

            // 3. Wi-Fi & BT MAC (XX:XX:XX:XX:XX:XX)
            String wifiMac = profile.getWifiMacAddress();
            String btMac = profile.getBluetoothMacAddress();
            assertNotNull(wifiMac);
            assertNotNull(btMac);
            assertEquals(17, wifiMac.length());
            assertEquals(17, btMac.length());
            assertTrue(wifiMac.matches("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$"));
            assertTrue(btMac.matches("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$"));
            assertFalse("Wi-Fi and BT MAC should not be identical", wifiMac.equals(btMac));
            assertTrue("Wi-Fi MACs must be unique", wifiMacs.add(wifiMac));
            assertTrue("BT MACs must be unique", btMacs.add(btMac));

            // 4. OAID (UUID 36-char)
            String oaid = profile.getOaid();
            assertNotNull(oaid);
            assertEquals(36, oaid.length());
            assertTrue("OAIDs must be unique", oaids.add(oaid));

            // 5. GSF ID (16 hex)
            String gsfId = profile.getGsfId();
            assertNotNull(gsfId);
            assertEquals(16, gsfId.length());
            assertTrue("GSF IDs must be unique", gsfIds.add(gsfId));

            // 6. Widevine DRM ID (64 hex)
            String widevine = profile.getWidevineDeviceId();
            assertNotNull(widevine);
            assertEquals(64, widevine.length());

            // 7. Advertising ID (UUID 36-char)
            String aaid = profile.getAdvertisingId();
            assertNotNull(aaid);
            assertEquals(36, aaid.length());

            // 8. System Properties validity
            Map<String, String> props = profile.generateSystemProperties();
            assertNotNull(props);
            assertFalse(props.isEmpty());
            assertTrue(props.containsKey("ro.product.model"));
            assertTrue(props.containsKey("ro.product.brand"));
            assertTrue(props.containsKey("ro.hardware.egl"));
            assertTrue(props.containsKey("debug.game.spoofed_android_id"));
        }
    }

    @Test
    public void testBrandSpecificOuiAndSerialFormatting() {
        SpoofProfile samsung = DeviceSpooferEngine.getProfileById("samsung_s25_ultra");
        SpoofProfile asus = DeviceSpooferEngine.getProfileById("asus_rog9_pro");
        SpoofProfile xiaomi = DeviceSpooferEngine.getProfileById("xiaomi_15_ultra");
        SpoofProfile nubia = DeviceSpooferEngine.getProfileById("redmagic_10_pro_plus");

        assertNotNull("samsung_s25_ultra must exist", samsung);
        assertTrue("Samsung serial should start with R58: " + samsung.getSerialNumber(),
                samsung.getSerialNumber().startsWith("R58"));
        assertTrue("Samsung Wi-Fi MAC should start with Samsung OUI: " + samsung.getWifiMacAddress(),
                samsung.getWifiMacAddress().startsWith("00:16:32"));

        assertNotNull("asus_rog9_pro must exist", asus);
        assertTrue("ASUS serial should match ROG pattern: " + asus.getSerialNumber(),
                asus.getSerialNumber().startsWith("N2AZB6"));
        assertTrue("ASUS Wi-Fi MAC should start with ASUS OUI: " + asus.getWifiMacAddress(),
                asus.getWifiMacAddress().startsWith("00:1A:92"));

        assertNotNull("xiaomi_15_ultra must exist", xiaomi);
        assertTrue("Xiaomi serial should start with 0x: " + xiaomi.getSerialNumber(),
                xiaomi.getSerialNumber().startsWith("0x"));
        assertTrue("Xiaomi Wi-Fi MAC should start with Xiaomi OUI: " + xiaomi.getWifiMacAddress(),
                xiaomi.getWifiMacAddress().startsWith("18:65:90"));

        assertNotNull("redmagic_10_pro_plus must exist", nubia);
        assertTrue("Nubia Wi-Fi MAC should start with ZTE/Nubia OUI: " + nubia.getWifiMacAddress(),
                nubia.getWifiMacAddress().startsWith("00:26:ED"));
    }

    @Test
    public void testAllRegisteredBrandsCountAndLookups() {
        List<String> brands = SpoofProfileRegistry.getBrandNames();
        assertNotNull(brands);
        assertEquals(11, brands.size());

        for (String brand : brands) {
            List<SpoofProfile> profiles = SpoofProfileRegistry.getByBrand(brand);
            assertNotNull(profiles);
            assertFalse(profiles.isEmpty());
            for (SpoofProfile p : profiles) {
                assertNotNull(p.id);
                assertNotNull(p.displayName);
                assertNotNull(p.model);
            }
        }
    }
}
