package com.gamebooster.app.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class ShellSafetyTest {

    @Test
    public void testSafePackageNames() {
        assertTrue(isValidPackageName("com.mobile.legends"));
        assertTrue(isValidPackageName("com.tencent.ig"));
        assertTrue(isValidPackageName("com.activision.callofduty.shooter"));
        assertTrue(isValidPackageName("com.dts.freefireth"));
    }

    @Test
    public void testRejectMaliciousShellTokens() {
        assertFalse(isValidPackageName("com.game; rm -rf /"));
        assertFalse(isValidPackageName("com.game && echo pwned"));
        assertFalse(isValidPackageName("com.game | sh"));
        assertFalse(isValidPackageName("com.game`id`"));
        assertFalse(isValidPackageName("com.game$(reboot)"));
        assertFalse(isValidPackageName("com.game' || '1'='1"));
        assertFalse(isValidPackageName("com.game\nreboot"));
    }

    private boolean isValidPackageName(String pkg) {
        if (pkg == null || pkg.trim().isEmpty()) return false;
        return pkg.matches("^[a-zA-Z0-9._-]+$");
    }
}
