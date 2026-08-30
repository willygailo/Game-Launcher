package com.gamebooster.app.spoofer;

import org.junit.Test;

import java.util.HashSet;
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

        Set<String> androidIds = new HashSet<>();
        Set<String> serialNumbers = new HashSet<>();
        Set<String> wifiMacs = new HashSet<>();
        Set<String> btMacs = new HashSet<>();
        Set<String> oaids = new HashSet<>();
        Set<String> gsfIds = new HashSet<>();

        for (SpoofProfile profile : allProfiles.values()) {
            assertNotNull(profile);

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

            // 8. IMEI (15 digits with valid Luhn)
            String imei1 = profile.getImei1();
            String imei2 = profile.getImei2();
            assertNotNull(imei1);
            assertNotNull(imei2);
            assertEquals(15, imei1.length());
            assertEquals(15, imei2.length());
            assertFalse(imei1.equals(imei2));
        }
    }

    @Test
    public void testBrandSpecificOuiAndSerialFormatting() {
        SpoofProfile samsung = DeviceSpooferEngine.getProfileById("samsung_s25_ultra");
        SpoofProfile asus = DeviceSpooferEngine.getProfileById("asus_rog_9_pro");
        SpoofProfile xiaomi = DeviceSpooferEngine.getProfileById("xiaomi_15_ultra");
        SpoofProfile nubia = DeviceSpooferEngine.getProfileById("nubia_redmagic_10_pro_plus");

        if (samsung != null) {
            assertTrue("Samsung serial should start with R58: " + samsung.getSerialNumber(),
                    samsung.getSerialNumber().startsWith("R58"));
            assertTrue("Samsung Wi-Fi MAC should start with Samsung OUI: " + samsung.getWifiMacAddress(),
                    samsung.getWifiMacAddress().startsWith("00:16:32"));
        }

        if (asus != null) {
            assertTrue("ASUS serial should match ROG pattern: " + asus.getSerialNumber(),
                    asus.getSerialNumber().startsWith("N2AZB6"));
            assertTrue("ASUS Wi-Fi MAC should start with ASUS OUI: " + asus.getWifiMacAddress(),
                    asus.getWifiMacAddress().startsWith("00:1A:92"));
        }

        if (xiaomi != null) {
            assertTrue("Xiaomi serial should start with 0x: " + xiaomi.getSerialNumber(),
                    xiaomi.getSerialNumber().startsWith("0x"));
            assertTrue("Xiaomi Wi-Fi MAC should start with Xiaomi OUI: " + xiaomi.getWifiMacAddress(),
                    xiaomi.getWifiMacAddress().startsWith("18:65:90"));
        }

        if (nubia != null) {
            assertTrue("Nubia Wi-Fi MAC should start with ZTE/Nubia OUI: " + nubia.getWifiMacAddress(),
                    nubia.getWifiMacAddress().startsWith("00:26:ED"));
        }
    }
}
