package com.gamebooster.app.feature.spoofer;

import org.junit.Test;
import static org.junit.Assert.*;

public class AppDeviceProfileRepositoryTest {

    @Test
    public void testDefaultGameProfileResolution() {
        SpoofProfile profile = AppDeviceProfileRepository.resolveProfileForGame(null, "com.tencent.ig");
        assertNotNull(profile);
        assertEquals("asus_rog8_pro", profile.id);
        assertEquals(165, profile.targetRefreshRate);
    }

    @Test
    public void testUnknownGameFallback() {
        SpoofProfile profile = AppDeviceProfileRepository.resolveProfileForGame(null, "com.unknown.game");
        assertNotNull(profile);
        assertNotNull(profile.id);
    }
}
