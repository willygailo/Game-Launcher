package com.gamebooster.app.config;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class GameConfigPathResolverTest {

    @Test
    public void testResolvePubgPaths() {
        List<String> roots = GameConfigPathResolver.generateBasePaths("com.tencent.ig");
        assertNotNull(roots);
        assertFalse(roots.isEmpty());
        for (String path : roots) {
            assertTrue(path.contains("com.tencent.ig"));
        }
    }

    @Test
    public void testKnownRelativePaths() {
        List<String> paths = GameConfigPathResolver.getKnownRelativePathsForPackage("com.mobile.legends");
        assertNotNull(paths);
        assertTrue(paths.size() >= 2);
    }
}
