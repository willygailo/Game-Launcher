package com.gamebooster.app.config;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class GameConfigPathResolverTest {

    @Test
    public void testKnownRelativePathsForMlbb() {
        List<String> paths = GameConfigPathResolver.getKnownRelativePathsForPackage("com.mobile.legends");
        assertNotNull(paths);
        assertFalse(paths.isEmpty());
        assertTrue(paths.contains("files/dragon2017/assets/UI/Config/UserSystem.ini"));
        assertTrue(paths.contains("files/dragon2017/assets/UI/Config/DamageSystem.ini"));
    }

    @Test
    public void testKnownRelativePathsForPubgm() {
        List<String> paths = GameConfigPathResolver.getKnownRelativePathsForPackage("com.tencent.ig");
        assertNotNull(paths);
        assertFalse(paths.isEmpty());
        assertTrue(paths.contains("files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini"));
    }

    @Test
    public void testKnownRelativePathsForCodm() {
        List<String> paths = GameConfigPathResolver.getKnownRelativePathsForPackage("com.activision.callofduty.shooter");
        assertNotNull(paths);
        assertFalse(paths.isEmpty());
        assertTrue(paths.contains("files/Config/UserSetting.json"));
    }

    @Test
    public void testNullOrEmptyPackage() {
        List<String> paths = GameConfigPathResolver.resolveConfigPaths(null, null);
        assertNotNull(paths);
        assertTrue(paths.isEmpty());

        paths = GameConfigPathResolver.resolveConfigPaths("", null);
        assertNotNull(paths);
        assertTrue(paths.isEmpty());
    }
}
