package com.gamebooster.app.booster;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for NetworkOptimizer 2026 gaming DNS, Philippine carrier profiles,
 * and Wi-Fi 5G/6G/7G configurations.
 */
public class NetworkOptimizerTest {

    @Test
    public void testDnsModesAndAddresses() {
        assertEquals("1.1.1.1", NetworkOptimizer.DnsMode.CLOUDFLARE_1_1_1_1.primary);
        assertEquals("1.0.0.1", NetworkOptimizer.DnsMode.CLOUDFLARE_1_1_1_1.secondary);
        assertEquals("one.one.one.one", NetworkOptimizer.DnsMode.CLOUDFLARE_1_1_1_1.privateDnsHost);

        assertEquals("8.8.8.8", NetworkOptimizer.DnsMode.GOOGLE_8_8_8_8.primary);
        assertEquals("8.8.4.4", NetworkOptimizer.DnsMode.GOOGLE_8_8_8_8.secondary);
        assertEquals("dns.google", NetworkOptimizer.DnsMode.GOOGLE_8_8_8_8.privateDnsHost);

        assertEquals("94.140.14.14", NetworkOptimizer.DnsMode.ADGUARD_GAMING.primary);
        assertEquals("94.140.15.15", NetworkOptimizer.DnsMode.ADGUARD_GAMING.secondary);
        assertEquals("dns.adguard-dns.com", NetworkOptimizer.DnsMode.ADGUARD_GAMING.privateDnsHost);

        assertEquals("9.9.9.9", NetworkOptimizer.DnsMode.QUAD9_GAMING.primary);
        assertEquals("149.112.112.112", NetworkOptimizer.DnsMode.QUAD9_GAMING.secondary);
        assertEquals("dns.quad9.net", NetworkOptimizer.DnsMode.QUAD9_GAMING.privateDnsHost);
    }

    @Test
    public void testPhilippineCarrierProfiles() {
        // TNT / Smart profile validation
        NetworkOptimizer.PhCarrier tnt = NetworkOptimizer.PhCarrier.TNT_SMART;
        assertEquals("TNT / Smart 5G Ultra Gaming", tnt.title);
        assertEquals("smartdata", tnt.defaultApn);
        assertEquals(1460, tnt.mtu);
        assertEquals("1.1.1.1", tnt.dnsPrimary);
        assertEquals("one.one.one.one", tnt.privateDnsHost);

        // TM / Globe profile validation
        NetworkOptimizer.PhCarrier tm = NetworkOptimizer.PhCarrier.TM_GLOBE;
        assertEquals("TM / Globe 5G Turbo Fast", tm.title);
        assertEquals("real.globe.com.ph", tm.defaultApn);
        assertEquals(1440, tm.mtu);
        assertEquals("8.8.8.8", tm.dnsPrimary);
        assertEquals("dns.google", tm.privateDnsHost);

        // DITO profile validation
        NetworkOptimizer.PhCarrier dito = NetworkOptimizer.PhCarrier.DITO;
        assertEquals("DITO 5G Fast Route", dito.title);
        assertEquals("dito.ph", dito.defaultApn);
        assertEquals(1460, dito.mtu);
    }

    @Test
    public void testTelemetrySnapshot() {
        NetworkOptimizer.NetworkTelemetry telemetry = NetworkOptimizer.getLiveTelemetry(null);
        assertNotNull(telemetry);
        assertNotNull(telemetry.activeInterface);
        assertNotNull(telemetry.activeDns);
    }

    @Test
    public void testDisableSavers() {
        // Ensure the saver disabling routines execute gracefully without exceptions
        org.junit.Assert.assertTrue(NetworkOptimizer.disableDataSaver());
        org.junit.Assert.assertTrue(NetworkOptimizer.disableBatterySaver());
        org.junit.Assert.assertTrue(NetworkOptimizer.disableWifiPowerSaver());
        org.junit.Assert.assertTrue(NetworkOptimizer.disableAllSaversForSuperFastInternet(null));
    }
}
