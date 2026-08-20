package com.gamebooster.app.spoofer;

import org.junit.Test;
import static org.junit.Assert.*;

public class SpoofProfileGeneratorsTest {

    @Test
    public void testGeneratorsIncludeModelAndHardware() {
        SpoofProfile profile = SpoofProfileRegistry.getById("samsung_s26_ultra");
        assertNotNull(profile);

        // 1. UE4
        String ue4 = profile.generateUe4DeviceProfile(185);
        assertTrue(ue4.contains(profile.model));
        assertTrue(ue4.contains(profile.brand));
        assertTrue(ue4.contains(profile.glRenderer));
        assertTrue(ue4.contains("Unlock185Hz=1"));

        // 2. JSON Hardware Profile (CODM, Blood Strike, Roblox)
        String json = profile.generateJsonHardwareProfile(185);
        assertTrue(json.contains(profile.model));
        assertTrue(json.contains(profile.glRenderer));
        assertTrue(json.contains(profile.socModel));
        assertTrue(json.contains(String.valueOf(profile.ramTotalMb)));

        // 3. MLBB
        String mlbb = profile.generateMlbbDeviceConfig(185);
        assertTrue(mlbb.contains(profile.model));
        assertTrue(mlbb.contains(profile.glRenderer));
        assertTrue(mlbb.contains(profile.socModel));

        // 4. Free Fire
        String ff = profile.generateFreeFireDeviceConfig(185);
        assertTrue(ff.contains(profile.model));
        assertTrue(ff.contains(profile.glRenderer));

        // 5. Genshin
        String genshin = profile.generateGenshinDeviceConfig(185);
        assertTrue(genshin.contains(profile.model));
        assertTrue(genshin.contains(profile.glRenderer));

        // 6. HOK
        String hok = profile.generateHokDeviceConfig(185);
        assertTrue(hok.contains(profile.model));
        assertTrue(hok.contains(profile.glRenderer));

        // 7. Standoff 2
        String so2 = profile.generateStandoff2DeviceConfig(185);
        assertTrue(so2.contains(profile.model));
        assertTrue(so2.contains(profile.glRenderer));

        // 8. CarX
        String carx = profile.generateCarXDeviceConfig(185);
        assertTrue(carx.contains(profile.model));
        assertTrue(carx.contains(profile.glRenderer));

        // 9. Supercell
        String supercell = profile.generateSupercellDeviceConfig(185);
        assertTrue(supercell.contains(profile.model));
        assertTrue(supercell.contains(profile.glRenderer));

        // 10. Generic
        String generic = profile.generateGenericHardwareConfig(185);
        assertTrue(generic.contains(profile.model));
        assertTrue(generic.contains(profile.glRenderer));
        assertTrue(generic.contains(profile.socModel));
    }
}
