package com.gamebooster.app.games;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class TargetGameRegistryTest {

    @Test
    public void testGetAllPackages_NotEmpty() {
        List<String> packages = TargetGameRegistry.getAllPackages();
        assertNotNull(packages);
        assertFalse(packages.isEmpty());
        assertTrue(packages.size() >= 30);
    }

    @Test
    public void testIsTargetPackage() {
        assertTrue(TargetGameRegistry.isTargetPackage("com.mobile.legends"));
        assertTrue(TargetGameRegistry.isTargetPackage("com.tencent.ig"));
        assertTrue(TargetGameRegistry.isTargetPackage("com.activision.callofduty.shooter"));
        assertFalse(TargetGameRegistry.isTargetPackage("com.unrelated.random.app"));
    }
}
