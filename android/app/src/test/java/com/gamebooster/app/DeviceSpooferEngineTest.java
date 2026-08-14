package com.gamebooster.app;

import com.gamebooster.app.feature.spoofer.*;
import com.gamebooster.app.feature.spoofer.games.*;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class DeviceSpooferEngineTest {

    @Test
    public void testGetAllProfilesAndBrands() {
        Map<String, SpoofProfile> all = DeviceSpooferEngine.getAllProfiles();
        assertNotNull(all);
        assertTrue(all.size() >= 20);

        List<String> brands = DeviceSpooferEngine.getBrandNames();
        assertNotNull(brands);
        assertTrue(brands.contains("ASUS ROG"));
        assertTrue(brands.contains("Samsung"));
        assertTrue(brands.contains("Xiaomi"));
        assertTrue(brands.contains("Nubia / REDMAGIC"));
        assertTrue(brands.contains("Vivo"));
        assertTrue(brands.contains("Infinix"));
        assertTrue(brands.contains("Tecno"));

        SpoofProfile rog = DeviceSpooferEngine.getProfileById("asus_rog9_pro");
        assertNotNull(rog);
        assertEquals("asus_rog9_pro", rog.id);
        assertEquals(165, rog.targetRefreshRate);
        assertTrue(rog.displayName.contains("ROG Phone 9 Pro"));

        List<SpoofProfile> samsungProfiles = DeviceSpooferEngine.getProfilesByBrand("Samsung");
        assertNotNull(samsungProfiles);
        assertFalse(samsungProfiles.isEmpty());
    }

    @Test
    public void testAppDeviceProfileRepositoryResolutions() {
        SpoofProfile mlbb = AppDeviceProfileRepository.resolveProfileForGame(null, "com.mobile.legends");
        assertNotNull(mlbb);
        assertEquals("asus_rog9_pro", mlbb.id);
        assertEquals(165, mlbb.targetRefreshRate);

        SpoofProfile pubg = AppDeviceProfileRepository.resolveProfileForGame(null, "com.tencent.ig");
        assertNotNull(pubg);
        assertEquals("asus_rog9_pro", pubg.id);

        SpoofProfile codm = AppDeviceProfileRepository.resolveProfileForGame(null, "com.activision.callofduty.shooter");
        assertNotNull(codm);
        assertEquals("redmagic_9_pro", codm.id);

        SpoofProfile hok = AppDeviceProfileRepository.resolveProfileForGame(null, "com.levelinfinite.sgameGlobal");
        assertNotNull(hok);
        assertEquals("iqoo_13", hok.id);

        SpoofProfile ff = AppDeviceProfileRepository.resolveProfileForGame(null, "com.dts.freefireth");
        assertNotNull(ff);
        assertEquals("infinix_gt_20_pro", ff.id);

        SpoofProfile genshin = AppDeviceProfileRepository.resolveProfileForGame(null, "com.miHoYo.GenshinImpact");
        assertNotNull(genshin);
        assertEquals("apple_ipad_pro_m4", genshin.id);
    }

    @Test
    public void testPackageIdentityMasker() {
        assertEquals("pkg_alias_mlbb_global", PackageIdentityMasker.maskPackageName("com.mobile.legends"));
        assertEquals("com.mobile.legends", PackageIdentityMasker.unmaskAlias("pkg_alias_mlbb_global"));

        assertEquals("pkg_alias_pubgm_global", PackageIdentityMasker.maskPackageName("com.tencent.ig"));
        assertEquals("com.tencent.ig", PackageIdentityMasker.unmaskAlias("pkg_alias_pubgm_global"));

        String customAlias = PackageIdentityMasker.maskPackageName("com.custom.game.app");
        assertNotNull(customAlias);
        assertTrue(customAlias.startsWith("pkg_alias_"));
        assertTrue(PackageIdentityMasker.isMaskedAlias(customAlias));
    }

    @Test
    public void testLegalProfileBrandOptimizations() {
        SpoofProfile rog = DeviceSpooferEngine.getProfileById("asus_rog9_pro");
        assertTrue(DeviceSpooferEngine.applyProfileBrandOptimizations(rog, 165, "com.mobile.legends"));

        SpoofProfile redmagic = DeviceSpooferEngine.getProfileById("redmagic_9_pro");
        assertNotNull(redmagic);
        assertTrue(DeviceSpooferEngine.applyProfileBrandOptimizations(redmagic, 165, "com.tencent.ig"));

        SpoofProfile samsung = DeviceSpooferEngine.getProfileById("samsung_s24_ultra");
        if (samsung != null) {
            assertTrue(DeviceSpooferEngine.applyProfileBrandOptimizations(samsung, 120, "com.tencent.ig"));
        }

        SpoofProfile xiaomi = DeviceSpooferEngine.getProfileById("xiaomi_15_ultra");
        if (xiaomi != null) {
            assertTrue(DeviceSpooferEngine.applyProfileBrandOptimizations(xiaomi, 120, "com.tencent.dfm"));
        }

        SpoofProfile infinix = DeviceSpooferEngine.getProfileById("infinix_gt_20_pro");
        if (infinix != null) {
            assertTrue(DeviceSpooferEngine.applyProfileBrandOptimizations(infinix, 144, "com.dts.freefireth"));
        }
    }

    @Test
    public void testGameSpooferManagerStrategyResolution() {
        GameSpooferManager manager = GameSpooferManager.getInstance();
        assertNotNull(manager);

        GameSpooferInterface mlbbStrategy = manager.getStrategyForPackage("com.mobile.legends");
        assertTrue(mlbbStrategy instanceof MlbbSpooferStrategy);
        assertTrue(mlbbStrategy.getStrategyName().contains("MLBB"));

        GameSpooferInterface pubgStrategy = manager.getStrategyForPackage("com.tencent.ig");
        assertTrue(pubgStrategy instanceof PubgSpooferStrategy);

        GameSpooferInterface codmStrategy = manager.getStrategyForPackage("com.garena.game.codm");
        assertTrue(codmStrategy instanceof CodmSpooferStrategy);

        GameSpooferInterface genericStrategy = manager.getStrategyForPackage("com.unknown.game");
        assertTrue(genericStrategy instanceof GenericGameSpooferStrategy);
    }
}
