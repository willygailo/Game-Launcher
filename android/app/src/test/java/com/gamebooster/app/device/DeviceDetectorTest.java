package com.gamebooster.app.device;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DeviceDetectorTest {

    @Test
    public void testGetDeviceSpecModel() {
        DeviceSpecModel model = DeviceDetector.getDeviceSpecModel();
        assertNotNull(model);
        assertNotNull(model.getManufacturer());
        assertNotNull(model.getModel());
        assertNotNull(model.getVendor());
        assertNotNull(model.getChipsetName());
    }

    @Test
    public void testGetDeviceSpecsMap() {
        Map<String, String> specs = DeviceDetector.getDeviceSpecs();
        assertNotNull(specs);
        assertTrue(specs.containsKey("model"));
        assertTrue(specs.containsKey("manufacturer"));
        assertTrue(specs.containsKey("chipset_vendor"));
        assertTrue(specs.containsKey("chipset_name"));
    }
}
