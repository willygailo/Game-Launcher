package com.gamebooster.app.spoofer.lsposed;

import com.gamebooster.app.spoofer.SpoofProfile;
import com.gamebooster.app.spoofer.SpoofProfileRegistry;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * LsposedSpoofUnitTest — Unit tests validating LSPosed / LSPatch spoofing logic,
 * SystemProperties map generation, multi-partition fallbacks, and anti-detection filtering.
 */
public class LsposedSpoofUnitTest {

    private SpoofProfile rogProfile;
    private SpoofProfile samsungProfile;
    private SpoofProfile xiaomiProfile;
    private SpoofProfile redmagicProfile;

    @Before
    public void setUp() {
        rogProfile = SpoofProfileRegistry.getById("asus_rog9_pro");
        samsungProfile = SpoofProfileRegistry.getById("samsung_s26_ultra");
        xiaomiProfile = SpoofProfileRegistry.getById("xiaomi_15_ultra");
        redmagicProfile = SpoofProfileRegistry.getById("redmagic_10_pro_plus");
    }

    @Test
    public void testFlagshipProfilesExist() {
        assertNotNull("ROG Phone 9 Pro profile must exist", rogProfile);
        assertNotNull("Samsung S26 Ultra profile must exist", samsungProfile);
        assertNotNull("Xiaomi 15 Ultra profile must exist", xiaomiProfile);
        assertNotNull("REDMAGIC 10 Pro+ profile must exist", redmagicProfile);
        assertTrue("Max refresh rate should be >= 120Hz", rogProfile.maxRefreshRateHz >= 120);
        assertTrue("Samsung S26 Ultra should have 120Hz", samsungProfile.maxRefreshRateHz >= 120);
    }

    @Test
    public void testSystemPropertiesMapGeneration() {
        Map<String, String> props = SystemPropertiesHooks.buildPropertyMap(rogProfile);
        assertNotNull(props);

        assertEquals(rogProfile.model, props.get("ro.product.model"));
        assertEquals(rogProfile.brand, props.get("ro.product.brand"));
        assertEquals(rogProfile.manufacturer, props.get("ro.product.manufacturer"));
        assertEquals(rogProfile.socModel, props.get("ro.soc.model"));

        // Anti-Detection & Security verification
        assertEquals("0", props.get("ro.kernel.qemu"));
        assertEquals("0", props.get("ro.boot.qemu"));
        assertEquals("0", props.get("ro.debuggable"));
        assertEquals("1", props.get("ro.secure"));
        assertEquals("1", props.get("ro.boot.flash.locked"));
        assertEquals("green", props.get("ro.boot.verifiedbootstate"));
        assertEquals("locked", props.get("ro.boot.vbmeta.device_state"));
        assertEquals("0", props.get("ro.boot.warranty_bit"));
        assertEquals("0", props.get("ro.warranty_bit"));
    }

    @Test
    public void testSystemPropertiesLookupMultiPartition() {
        SystemPropertiesHooks.initPropertyMap(rogProfile);

        assertEquals(rogProfile.model, SystemPropertiesHooks.lookup("ro.product.model"));
        assertEquals(rogProfile.model, SystemPropertiesHooks.lookup("ro.vendor.product.model"));
        assertEquals(rogProfile.model, SystemPropertiesHooks.lookup("ro.odm.product.model"));
        assertEquals(rogProfile.model, SystemPropertiesHooks.lookup("ro.system.product.model"));
        assertEquals(rogProfile.brand, SystemPropertiesHooks.lookup("ro.product.brand"));
        assertEquals("green", SystemPropertiesHooks.lookup("ro.boot.verifiedbootstate"));
    }

    @Test
    public void testAntiDetectionHiddenPackages() {
        assertTrue(AntiDetectionHooks.isHiddenPackage("org.lsposed.manager"));
        assertTrue(AntiDetectionHooks.isHiddenPackage("org.lsposed.lspatch"));
        assertTrue(AntiDetectionHooks.isHiddenPackage("org.lsposed.lspd"));
        assertTrue(AntiDetectionHooks.isHiddenPackage("com.topjohnwu.magisk"));
        assertTrue(AntiDetectionHooks.isHiddenPackage("io.github.a13e300.ksu"));
        assertTrue(AntiDetectionHooks.isHiddenPackage("me.bmax.apatch"));
        assertTrue(AntiDetectionHooks.isHiddenPackage("moe.shizuku.privileged.api"));
        assertTrue(AntiDetectionHooks.isHiddenPackage("com.gamebooster.app"));

        // Case-insensitivity check
        assertTrue(AntiDetectionHooks.isHiddenPackage("ORG.LSPOSED.MANAGER"));
        assertTrue(AntiDetectionHooks.isHiddenPackage("COM.TOPJOHNWU.MAGISK"));

        // Legitimate apps must not be hidden
        assertFalse(AntiDetectionHooks.isHiddenPackage("com.mobile.legends"));
        assertFalse(AntiDetectionHooks.isHiddenPackage("com.tencent.ig"));
        assertFalse(AntiDetectionHooks.isHiddenPackage("com.activision.callofduty.shooter"));
    }

    @Test
    public void testAntiDetectionBlockedPaths() {
        assertTrue(AntiDetectionHooks.isBlockedPath("/system/bin/su"));
        assertTrue(AntiDetectionHooks.isBlockedPath("/system/xbin/su"));
        assertTrue(AntiDetectionHooks.isBlockedPath("/sbin/su"));
        assertTrue(AntiDetectionHooks.isBlockedPath("/data/adb/magisk"));
        assertTrue(AntiDetectionHooks.isBlockedPath("/data/adb/ksu"));
        assertTrue(AntiDetectionHooks.isBlockedPath("/data/adb/apatch"));
        assertTrue(AntiDetectionHooks.isBlockedPath("/data/adb/lspd"));
        assertTrue(AntiDetectionHooks.isBlockedPath("/system/framework/XposedBridge.jar"));
        assertTrue(AntiDetectionHooks.isBlockedPath("/SYSTEM/FRAMEWORK/XPOSEDBRIDGE.JAR"));
        assertTrue(AntiDetectionHooks.isBlockedPath("/system/app/Superuser.apk"));

        // Game asset paths must not be blocked
        assertFalse(AntiDetectionHooks.isBlockedPath("/sdcard/Android/data/com.mobile.legends/files"));
        assertFalse(AntiDetectionHooks.isBlockedPath("/data/data/com.tencent.ig/databases"));
    }

    @Test
    public void testSpoofModuleExcludedPackages() {
        assertTrue(SpoofModule.isExcludedPackage("com.gamebooster.app"));
        assertTrue(SpoofModule.isExcludedPackage("android"));
        assertTrue(SpoofModule.isExcludedPackage("com.android.systemui"));
        assertTrue(SpoofModule.isExcludedPackage("com.google.android.gms"));
        assertTrue(SpoofModule.isExcludedPackage("org.lsposed.manager"));

        // Games must not be excluded
        assertFalse(SpoofModule.isExcludedPackage("com.mobile.legends"));
        assertFalse(SpoofModule.isExcludedPackage("com.tencent.ig"));
        assertFalse(SpoofModule.isExcludedPackage("com.activision.callofduty.shooter"));
    }

    @Test
    public void testSerialGeneration() {
        String serial1 = BuildHooks.generateSerial(rogProfile);
        String serial2 = BuildHooks.generateSerial(rogProfile);
        assertNotNull(serial1);
        assertTrue(serial1.startsWith("GB"));
        assertEquals("Serial should be deterministic", serial1, serial2);
    }
}
