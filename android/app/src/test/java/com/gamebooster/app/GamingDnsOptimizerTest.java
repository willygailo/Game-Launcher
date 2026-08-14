package com.gamebooster.app;

import com.gamebooster.app.feature.performance.network.GamingDnsOptimizer;
import org.junit.Test;

import static org.junit.Assert.*;

public class GamingDnsOptimizerTest {

    @Test
    public void testDnsPresetsConstants() {
        assertEquals("1dot1dot1dot1.cloudflare-dns.com", GamingDnsOptimizer.CLOUDFLARE_DNS_HOSTNAME);
        assertEquals("dns.google", GamingDnsOptimizer.GOOGLE_DNS_HOSTNAME);
        assertEquals("dns.quad9.net", GamingDnsOptimizer.QUAD9_DNS_HOSTNAME);
        assertEquals("dns.adguard-dns.com", GamingDnsOptimizer.ADGUARD_DNS_HOSTNAME);

        GamingDnsOptimizer.DnsPreset[] presets = GamingDnsOptimizer.DnsPreset.values();
        assertEquals(4, presets.length);

        for (GamingDnsOptimizer.DnsPreset preset : presets) {
            assertNotNull(preset.label);
            assertNotNull(preset.hostname);
            assertFalse(preset.hostname.isEmpty());
        }
    }
}
