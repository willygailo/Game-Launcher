package com.gamebooster.app;

import com.gamebooster.app.device.DeviceDetector;
import com.gamebooster.app.device.DeviceSpecModel;
import org.junit.Test;
import java.util.Map;
import static org.junit.Assert.*;

public class DeviceDetectorTest {

    @Test
    public void testDeviceDetectorSpecsMap() {
        Map<String, String> specs = DeviceDetector.getDeviceSpecs();
        assertNotNull(specs);
        assertTrue(specs.containsKey("model"));
        assertTrue(specs.containsKey("manufacturer"));
        assertTrue(specs.containsKey("android_version"));
        assertTrue(specs.containsKey("sdk_int"));
        assertTrue(specs.containsKey("chipset_vendor"));
        assertTrue(specs.containsKey("chipset_name"));
    }

    @Test
    public void testDeviceSpecModel() {
        DeviceSpecModel model = DeviceDetector.getDeviceSpecModel();
        assertNotNull(model);
        assertNotNull(model.getVendor());
        assertNotNull(model.getManufacturer());
        assertNotNull(model.getModel());
    }
}
