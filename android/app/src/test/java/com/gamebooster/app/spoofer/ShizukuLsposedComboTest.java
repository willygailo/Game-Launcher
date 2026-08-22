package com.gamebooster.app.spoofer;

import com.gamebooster.app.shizuku.ShizukuConnectionManager;
import com.gamebooster.app.spoofer.lsposed.AntiDetectionHooks;
import com.gamebooster.app.spoofer.lsposed.IdentityHooks;
import com.gamebooster.app.spoofer.lsposed.LsposedDetector;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ShizukuLsposedComboTest {

    @Test
    public void testLsposedFrameworkTypes() {
        assertNotNull(LsposedDetector.FrameworkType.NONE);
        assertEquals("Standard Engine", LsposedDetector.FrameworkType.NONE.displayName);

        assertNotNull(LsposedDetector.FrameworkType.LSPOSED_ROOT);
        assertEquals("LSPosed Module (Root)", LsposedDetector.FrameworkType.LSPOSED_ROOT.displayName);
        assertEquals("#00F0FF", LsposedDetector.FrameworkType.LSPOSED_ROOT.colorHex);

        assertNotNull(LsposedDetector.FrameworkType.LSPATCH_NON_ROOT);
        assertEquals("LSPatch Module (Non-Root)", LsposedDetector.FrameworkType.LSPATCH_NON_ROOT.displayName);
        assertEquals("#00FF66", LsposedDetector.FrameworkType.LSPATCH_NON_ROOT.colorHex);
    }

    @Test
    public void testLsposedHeartbeatTracking() {
        String testGame = "com.dts.freefireth";
        LsposedDetector.recordGameHeartbeat(testGame);

        assertTrue(LsposedDetector.isAnyGameHookedActive());
        List<String> active = LsposedDetector.getActiveHookedGames();
        assertNotNull(active);
        assertTrue(active.contains(testGame));
    }

    @Test
    public void testShizukuConnectionStates() {
        ShizukuConnectionManager.State[] states = ShizukuConnectionManager.State.values();
        assertTrue(states.length >= 5);

        assertNotNull(ShizukuConnectionManager.State.IDLE);
        assertNotNull(ShizukuConnectionManager.State.BINDING);
        assertNotNull(ShizukuConnectionManager.State.READY);
        assertNotNull(ShizukuConnectionManager.State.DEAD);
        assertNotNull(ShizukuConnectionManager.State.RETRY);
    }

    @Test
    public void testSpoofProfilesForAllBrands() {
        List<String> brands = DeviceSpooferEngine.getBrandNames();
        assertNotNull(brands);
        assertFalse(brands.isEmpty());

        for (String brand : brands) {
            List<SpoofProfile> profiles = DeviceSpooferEngine.getProfilesByBrand(brand);
            assertNotNull(profiles);
            assertFalse(profiles.isEmpty());

            for (SpoofProfile p : profiles) {
                assertNotNull(p.id);
                assertNotNull(p.model);
                assertNotNull(p.brand);
                assertNotNull(p.socModel);
                assertTrue(p.maxRefreshRateHz >= 60);
                assertTrue(p.ramTotalMb >= 4096);

                // Check generated system properties
                Map<String, String> props = p.generateSystemProperties();
                assertNotNull(props);
                assertTrue(props.containsKey("ro.product.model"));
                assertTrue(props.containsKey("ro.product.brand"));
                assertEquals(p.model, props.get("ro.product.model"));
                assertEquals(p.brand, props.get("ro.product.brand"));

                // Check generated proc payloads
                String cpuInfo = p.generateCpuInfo();
                assertNotNull(cpuInfo);
                assertTrue(cpuInfo.contains("processor"));

                String memInfo = p.generateMemInfo();
                assertNotNull(memInfo);
                assertTrue(memInfo.contains("MemTotal:"));

                String procVer = p.generateProcVersion();
                assertNotNull(procVer);
                assertTrue(procVer.contains("Linux version"));
            }
        }
    }

    @Test
    public void testAntiDetectionHiddenFrameworksCoverage() {
        // Shizuku
        assertTrue(AntiDetectionHooks.isHiddenPackage("moe.shizuku.privileged.api"));
        assertTrue(AntiDetectionHooks.isHiddenPackage("moe.shizuku.manager"));
        assertTrue(AntiDetectionHooks.isHiddenPackage("rikka.shizuku"));

        // LSPosed & LSPatch
        assertTrue(AntiDetectionHooks.isHiddenPackage("org.lsposed.lspatch"));
        assertTrue(AntiDetectionHooks.isHiddenPackage("org.lsposed.lspatch.metamod"));
        assertTrue(AntiDetectionHooks.isHiddenPackage("org.lsposed.manager"));
        assertTrue(AntiDetectionHooks.isHiddenPackage("org.lsposed.lspd"));

        // Root Managers
        assertTrue(AntiDetectionHooks.isHiddenPackage("com.topjohnwu.magisk"));
        assertTrue(AntiDetectionHooks.isHiddenPackage("io.github.vvb2060.magisk"));
        assertTrue(AntiDetectionHooks.isHiddenPackage("io.github.a13e300.ksu"));
        assertTrue(AntiDetectionHooks.isHiddenPackage("me.bmax.apatch"));

        // Blocked paths
        assertTrue(AntiDetectionHooks.isBlockedPath("/system/bin/su"));
        assertTrue(AntiDetectionHooks.isBlockedPath("/data/adb/magisk"));
        assertTrue(AntiDetectionHooks.isBlockedPath("/data/adb/ksu"));
        assertTrue(AntiDetectionHooks.isBlockedPath("/data/adb/apatch"));
        assertTrue(AntiDetectionHooks.isBlockedPath("/data/adb/lspd"));
        assertTrue(AntiDetectionHooks.isBlockedPath("/data/local/tmp/shizuku"));
        assertTrue(AntiDetectionHooks.isBlockedPath("/dev/shizuku"));
    }

    @Test
    public void testDeterministicIdentityGeneration() {
        Map<String, SpoofProfile> all = SpoofProfileRegistry.getAllProfiles();
        for (SpoofProfile p : all.values()) {
            String imei1 = IdentityHooks.generateImei(p);
            String imei2 = IdentityHooks.generateImei(p);
            assertEquals("IMEI must be deterministic", imei1, imei2);
            assertEquals(15, imei1.length());

            String meid1 = IdentityHooks.generateMeid(p);
            String meid2 = IdentityHooks.generateMeid(p);
            assertEquals("MEID must be deterministic", meid1, meid2);
            assertEquals(14, meid1.length());

            String mac1 = IdentityHooks.generateMacAddress(p);
            String mac2 = IdentityHooks.generateMacAddress(p);
            assertEquals("MAC must be deterministic", mac1, mac2);
            assertTrue(mac1.startsWith("02:"));
        }
    }
}
