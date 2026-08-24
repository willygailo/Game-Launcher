package com.gamebooster.app;

import com.gamebooster.app.config.GameConfigPathResolver;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class GameConfigPathResolverTest {

    @Test
    public void testBasePathsGeneration() {
        List<String> basePaths = GameConfigPathResolver.generateBasePaths("com.mobile.legends");
        assertNotNull(basePaths);
        assertFalse(basePaths.isEmpty());

        boolean hasData = false;
        boolean hasDataData = false;
        for (String p : basePaths) {
            if (p.contains("/sdcard/Android/data/com.mobile.legends")) hasData = true;
            if (p.contains("/data/data/com.mobile.legends")) hasDataData = true;
        }
        assertTrue("Must include /sdcard/Android/data", hasData);
        assertTrue("Must include /data/data", hasDataData);
    }
}
