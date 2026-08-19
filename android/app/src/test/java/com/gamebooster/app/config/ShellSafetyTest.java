package com.gamebooster.app.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class ShellSafetyTest {

    @Test
    public void testSafePaths() {
        String safePath1 = "/sdcard/Android/data/com.tencent.ig/files/UE4Game/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini";
        String safePath2 = "/storage/emulated/0/Android/data/com.dts.freefireth/files/FFGraphics.ini";
        assertFalse(containsShellInjection(safePath1));
        assertFalse(containsShellInjection(safePath2));
    }

    @Test
    public void testRejectDangerousTokens() {
        String[] attacks = {
            "/sdcard/Android/data/com.tencent.ig; rm -rf /",
            "/sdcard/Android/data/com.tencent.ig && reboot",
            "/sdcard/Android/data/`id`",
            "/sdcard/Android/data/$(whoami)",
            "/sdcard/Android/data/test|cat",
            "/sdcard/Android/data/test\nreboot"
        };
        for (String attack : attacks) {
            assertTrue("Should detect attack token in: " + attack, containsShellInjection(attack));
        }
    }

    private static boolean containsShellInjection(String path) {
        if (path == null) return false;
        return path.contains(";") || path.contains("&&") || path.contains("||") ||
               path.contains("`") || path.contains("$") || path.contains("|") ||
               path.contains("\n") || path.contains("\r");
    }
}
