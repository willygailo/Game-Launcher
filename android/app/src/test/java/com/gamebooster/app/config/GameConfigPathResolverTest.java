package com.gamebooster.app.config;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class GameConfigPathResolverTest {

    @Test
    public void testGenerateBasePaths_ContainsStandardRoots() {
        String pkg = "com.tencent.ig";
        List<String> roots = GameConfigPathResolver.generateBasePaths(pkg);

        assertNotNull(roots);
        assertTrue(roots.size() >= 5);

        boolean hasDataData = false;
        boolean hasSdcardData = false;
        for (String r : roots) {
            if (r.contains("/data/data/" + pkg)) hasDataData = true;
            if (r.contains("/sdcard/Android/data/" + pkg)) hasSdcardData = true;
        }

        assertTrue("Expected /data/data/" + pkg, hasDataData);
        assertTrue("Expected /sdcard/Android/data/" + pkg, hasSdcardData);
    }
}
