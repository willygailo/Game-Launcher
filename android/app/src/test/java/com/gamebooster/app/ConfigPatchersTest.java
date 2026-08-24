package com.gamebooster.app;

import com.gamebooster.app.config.DeltaForceConfigPatcher;
import com.gamebooster.app.config.WutheringWavesConfigPatcher;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class ConfigPatchersTest {

    @Test
    public void testDeltaForceConfigPaths() {
        List<String> paths = DeltaForceConfigPatcher.getConfigPaths("com.levelinfinite.deltaforce");
        assertNotNull(paths);
        assertFalse(paths.isEmpty());

        boolean hasIni = false;
        for (String p : paths) {
            if (p.contains("UserCustom.ini") || p.contains("Engine.ini")) hasIni = true;
        }
        assertTrue("Delta Force must target UE5 INI configs", hasIni);
    }

    @Test
    public void testWutheringWavesConfigPaths() {
        List<String> paths = WutheringWavesConfigPatcher.getConfigPaths("com.kurogame.wutheringwaves.global");
        assertNotNull(paths);
        assertFalse(paths.isEmpty());

        boolean hasEngine = false;
        for (String p : paths) {
            if (p.contains("Engine.ini")) hasEngine = true;
        }
        assertTrue("Wuthering Waves must target Engine.ini", hasEngine);
    }
}
