package com.gamebooster.app.spoofer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SpoofProfileRegistry + SpoofProfile data integrity: unique IDs per brand,
 * non-empty identity fields, and RAM/GPU consistency across generated payloads
 * (/proc/meminfo ↔ ramTotalMb, system properties ↔ profile fields).
 */
public class SpoofProfileRegistryTest {

    @Test
    public void registry_hasProfilesAndBrands() {
        assertTrue(SpoofProfileRegistry.getTotalCount() > 0);
        assertTrue(SpoofProfileRegistry.getBrandNames().size() > 0);
        assertEquals(SpoofProfileRegistry.getTotalCount(), SpoofProfileRegistry.getAllProfiles().size());
    }

    @Test
    public void registry_profileIdsAreUnique() {
        Set<String> ids = new HashSet<>();
        for (SpoofProfile p : SpoofProfileRegistry.getAllProfiles().values()) {
            assertTrue("duplicate id: " + p.id, ids.add(p.id));
        }
        assertEquals(SpoofProfileRegistry.getTotalCount(), ids.size());
    }

    @Test
    public void registry_everyProfileHasCompleteIdentity() {
        for (SpoofProfile p : SpoofProfileRegistry.getAllProfiles().values()) {
            assertNotNull("id", p.id);
            assertFalse("displayName empty for " + p.id, p.displayName == null || p.displayName.isEmpty());
            assertFalse("brandLabel empty for " + p.id, p.brandLabel == null || p.brandLabel.isEmpty());
            assertFalse("model empty for " + p.id, p.model == null || p.model.isEmpty());
            assertFalse("brand empty for " + p.id, p.brand == null || p.brand.isEmpty());
            assertFalse("socModel empty for " + p.id, p.socModel == null || p.socModel.isEmpty());
            assertFalse("glRenderer empty for " + p.id, p.glRenderer == null || p.glRenderer.isEmpty());
            assertFalse("vulkanVersion empty for " + p.id, p.vulkanVersion == null || p.vulkanVersion.isEmpty());
        }
    }

    @Test
    public void registry_ramConsistency_availableNeverExceedsTotal() {
        for (SpoofProfile p : SpoofProfileRegistry.getAllProfiles().values()) {
            assertTrue("ramTotalMb <= 0 for " + p.id, p.ramTotalMb > 0);
            assertTrue("ramAvailableMb <= 0 for " + p.id, p.ramAvailableMb > 0);
            assertTrue("ramAvailableMb > ramTotalMb for " + p.id, p.ramAvailableMb <= p.ramTotalMb);
        }
    }

    @Test
    public void registry_hardwareFieldsWithinSaneRanges() {
        for (SpoofProfile p : SpoofProfileRegistry.getAllProfiles().values()) {
            assertTrue("bad sdkInt for " + p.id, p.sdkInt >= 1 && p.sdkInt <= 36);
            assertTrue("bad cpuCores for " + p.id, p.cpuCores > 0 && p.cpuCores <= 32);
            assertTrue("bad cpuMaxFreqKhz for " + p.id, p.cpuMaxFreqKhz > 0);
            assertTrue("bad maxRefreshRateHz for " + p.id, p.maxRefreshRateHz >= 60 && p.maxRefreshRateHz <= 240);
        }
    }

    @Test
    public void registry_brandBundlesContainOnlyTheirBrand() {
        for (String brand : SpoofProfileRegistry.getBrandNames()) {
            List<SpoofProfile> profiles = SpoofProfileRegistry.getByBrand(brand);
            assertFalse("brand '" + brand + "' has no profiles", profiles.isEmpty());
            for (SpoofProfile p : profiles) {
                assertEquals(brand, p.brandLabel);
            }
        }
    }

    @Test
    public void registry_lookups() {
        SpoofProfile first = SpoofProfileRegistry.getAllProfiles().values().iterator().next();
        assertEquals(first, SpoofProfileRegistry.getById(first.id));
        assertNull(SpoofProfileRegistry.getById("definitely-not-a-profile-id"));
        assertNull(SpoofProfileRegistry.getById(null));
        assertTrue(SpoofProfileRegistry.getByBrand("NoSuchBrand").isEmpty());
        assertNotNull(SpoofProfileRegistry.getFirstByBrand(SpoofProfileRegistry.getBrandNames().get(0)));
        assertNull(SpoofProfileRegistry.getFirstByBrand("NoSuchBrand"));
    }

    // ─── meminfo / system-property payloads must match the profile fields ───

    @Test
    public void memInfo_agreesWithRamFields() {
        for (SpoofProfile p : SpoofProfileRegistry.getAllProfiles().values()) {
            String mem = p.generateMemInfo();
            long totalKb = (long) p.ramTotalMb * 1024L;
            long availKb = (long) p.ramAvailableMb * 1024L;
            assertTrue(mem.contains("MemTotal:       " + totalKb + " kB"));
            assertTrue(mem.contains("MemAvailable:   " + availKb + " kB"));
            assertFalse(mem.contains("MemFree:        " + totalKb + " kB"));
        }
    }

    @Test
    public void systemProperties_matchProfileFields() {
        for (SpoofProfile p : SpoofProfileRegistry.getAllProfiles().values()) {
            Map<String, String> props = p.generateSystemProperties();
            assertEquals(p.model, props.get("ro.product.model"));
            assertEquals(p.brand, props.get("ro.product.brand"));
            assertEquals(p.manufacturer, props.get("ro.product.manufacturer"));
            assertEquals(p.socModel, props.get("ro.soc.model"));
            assertEquals(p.socManufacturer, props.get("ro.soc.manufacturer"));
            assertEquals(p.ramTotalMb + "", props.get("debug.game.spoofed_ram"));
            assertEquals(p.ramAvailableMb + "", props.get("debug.game.spoofed_ram_avail"));
            // GPU namespace consistency: ARM vendor ⇒ mali EGL, else adreno
            String expectedEgl = p.glVendor.toLowerCase().contains("arm") ? "mali" : "adreno";
            assertEquals(expectedEgl, props.get("ro.hardware.egl"));
        }
    }

    @Test
    public void generateCpuInfo_containsCoreCountAndHardware() {
        SpoofProfile p = SpoofProfileRegistry.getAllProfiles().values().iterator().next();
        String cpu = p.generateCpuInfo();
        int processorLines = cpu.split("processor\\s*:").length - 1;
        assertEquals(p.cpuCores, processorLines);
        assertTrue(cpu.contains("Hardware\t: " + p.hardware));
        assertTrue(cpu.contains("SoC Model\t: " + p.socModel));
    }

    @Test
    public void enginePayloads_embedFpsAndIdentity() {
        SpoofProfile p = SpoofProfileRegistry.getAllProfiles().values().iterator().next();
        String ue4 = p.generateUe4DeviceProfile(144);
        assertTrue(ue4.contains("r.MobileFPSLimit=144"));
        assertTrue(ue4.contains("RAMTotalMB=" + p.ramTotalMb));
        assertTrue(ue4.contains("DeviceName=" + p.model));
        String json = p.generateJsonHardwareProfile(120);
        assertTrue(json.contains("\"MaxFrameRate\": 120"));
        assertTrue(json.contains("\"GPURenderer\": \"" + p.glRenderer + "\""));
    }

    // ─── SpoofProfile defaulting (backward-compatible 17-param ctor) ────────

    @Test
    public void legacyConstructor_appliesSafeDefaults() {
        SpoofProfile p = new SpoofProfile("tst", "Test", "TestBrand",
                "TestModel", "TestBrand", "TestMfr", "testdev", "testprod", "testprod",
                "qcom", "sm8550", "snapdragon", "taro", "taro",
                "test/fingerprint", "T1", "OpenGL ES 3.2 Mali");
        assertEquals("Qualcomm", p.socManufacturer);
        assertEquals(8, p.cpuCores);
        assertEquals(16384, p.ramTotalMb);
        assertEquals(12288, p.ramAvailableMb);
        assertEquals(35, p.sdkInt);
        assertEquals(185, p.maxRefreshRateHz);
        assertTrue(p.ramAvailableMb <= p.ramTotalMb);
    }

    @Test
    public void vendorInference_matchesRenderer() {
        // Exercise inferVendor paths through the 17-param constructor
        SpoofProfile mali = new SpoofProfile("m", "M", "B", "model", "brand", "mfr", "d", "p", "p",
                "h", "pl", "soc", "b", "c", "f", "d", "Mali-G715 Immortalis");
        assertEquals("ARM", mali.glVendor);
        SpoofProfile immortalis = new SpoofProfile("i", "I", "B", "model", "brand", "mfr", "d", "p", "p",
                "h", "pl", "soc", "b", "c", "f", "d", "Immortalis-G925");
        assertEquals("ARM", immortalis.glVendor);
        SpoofProfile adreno = new SpoofProfile("a", "A", "B", "model", "brand", "mfr", "d", "p", "p",
                "h", "pl", "soc", "b", "c", "f", "d", "Adreno (TM) 740");
        assertEquals("Qualcomm", adreno.glVendor);
        SpoofProfile powervr = new SpoofProfile("p", "P", "B", "model", "brand", "mfr", "d", "p", "p",
                "h", "pl", "soc", "b", "c", "f", "d", "PowerVR B-Series");
        assertEquals("Imagination Technologies", powervr.glVendor);
        SpoofProfile appleGpu = new SpoofProfile("ap", "AP", "B", "model", "brand", "mfr", "d", "p", "p",
                "h", "pl", "soc", "b", "c", "f", "d", "Apple GPU");
        assertEquals("Apple", appleGpu.glVendor);
        SpoofProfile unknownGpu = new SpoofProfile("u", "U", "B", "model", "brand", "mfr", "d", "p", "p",
                "h", "pl", "soc", "b", "c", "f", "d", null);
        assertEquals("Qualcomm", unknownGpu.glVendor);
        SpoofProfile noGpu = new SpoofProfile("n", "N", "B", "model", "brand", "mfr", "d", "p", "p",
                "h", "pl", "soc", "b", "c", "f", "d", "");
        assertEquals("Qualcomm", noGpu.glVendor);
    }

    @Test
    public void socManufacturerInference_branches() {
        // MediaTek dimensity
        SpoofProfile dim = new SpoofProfile("d1", "D1", "B", "model", "brand", "mfr", "d", "p", "p",
                "h", "pl", "dimensity 9300", "b", "c", "f", "d", "Mali-G720");
        assertEquals("MediaTek", dim.socManufacturer);
        // Apple silicon
        SpoofProfile apl = new SpoofProfile("d2", "D2", "B", "model", "brand", "mfr", "d", "p", "p",
                "h", "pl", "A18 Pro", "b", "c", "f", "d", "Apple GPU");
        assertEquals("Apple", apl.socManufacturer);
        // Brand-only Apple signal
        SpoofProfile apl2 = new SpoofProfile("d3", "D3", "B", "model", "Apple", "mfr", "d", "p", "p",
                "h", "pl", "soc", "b", "c", "f", "d", "Apple GPU");
        assertEquals("Apple", apl2.socManufacturer);
        // Fallback stays Qualcomm (no dimensity/apple signals)
        SpoofProfile q = new SpoofProfile("d4", "D4", "B", "model", "brand", "mfr", "d", "p", "p",
                "h", "pl", "snapdragon 8 gen 3", "b", "c", "f", "d", "Adreno 750");
        assertEquals("Qualcomm", q.socManufacturer);
    }
}