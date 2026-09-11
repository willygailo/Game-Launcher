package com.gamebooster.app.booster;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WebViewBoosterChannelTest {

    @Test
    public void testWebViewCommandLineFlagsPurgesAngleVulkan() {
        String flags = WebViewBoosterChannel.getWebViewCommandLineFlags();
        assertNotNull(flags);
        assertFalse(flags.isEmpty());

        // Extract enable-features and disable-features sections
        int enableIndex = flags.indexOf("--enable-features=");
        assertTrue("Flags must include --enable-features", enableIndex >= 0);
        int enableEnd = flags.indexOf(" ", enableIndex);
        String enablePart = enableEnd > 0 ? flags.substring(enableIndex, enableEnd) : flags.substring(enableIndex);

        int disableIndex = flags.indexOf("--disable-features=");
        assertTrue("Flags must include --disable-features", disableIndex >= 0);
        int disableEnd = flags.indexOf(" ", disableIndex);
        String disablePart = disableEnd > 0 ? flags.substring(disableIndex, disableEnd) : flags.substring(disableIndex);

        // Assert that ANGLE Vulkan features are NOT enabled
        assertFalse("DefaultAngleVulkan must NOT be in --enable-features", enablePart.contains("DefaultAngleVulkan"));
        assertFalse("VulkanFromANGLE must NOT be in --enable-features", enablePart.contains("VulkanFromANGLE"));

        // Assert that ANGLE Vulkan features are explicitly DISABLED
        assertTrue("DefaultAngleVulkan must be in --disable-features", disablePart.contains("DefaultAngleVulkan"));
        assertTrue("VulkanFromANGLE must be in --disable-features", disablePart.contains("VulkanFromANGLE"));
    }
}
