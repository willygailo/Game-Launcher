package com.gamebooster.app.feature.spoofer;

import org.junit.Test;
import static org.junit.Assert.*;

public class PackageIdentityMaskerTest {

    @Test
    public void testCanonicalPackageMasking() {
        String raw = "com.tencent.ig";
        String alias = PackageIdentityMasker.maskPackageName(raw);

        assertNotNull(alias);
        assertTrue(PackageIdentityMasker.isMaskedAlias(alias));
        assertEquals(raw, PackageIdentityMasker.unmaskAlias(alias));
    }

    @Test
    public void testDynamicPackageMasking() {
        String customPkg = "com.custom.game.title";
        String alias = PackageIdentityMasker.maskPackageName(customPkg);

        assertNotNull(alias);
        assertTrue(PackageIdentityMasker.isMaskedAlias(alias));
        assertEquals(customPkg, PackageIdentityMasker.unmaskAlias(alias));
    }

    @Test
    public void testNullPackageHandling() {
        String alias = PackageIdentityMasker.maskPackageName(null);
        assertNotNull(alias);
        assertTrue(PackageIdentityMasker.isMaskedAlias(alias));
    }
}
