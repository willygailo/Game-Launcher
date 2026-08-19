package com.gamebooster.app.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class ShellSafetyTest {

    @Test
    public void testIsSafePackageName_ValidPackages() {
        assertTrue(ShellSafety.isSafePackageName("com.tencent.ig"));
        assertTrue(ShellSafety.isSafePackageName("com.mobile.legends"));
        assertTrue(ShellSafety.isSafePackageName("com.activision.callofduty.shooter"));
        assertTrue(ShellSafety.isSafePackageName("com.dts.freefireth"));
        assertTrue(ShellSafety.isSafePackageName("com.miHoYo.GenshinImpact"));
        assertTrue(ShellSafety.isSafePackageName("com.supercell.brawlstars"));
    }

    @Test
    public void testIsSafePackageName_InjectionAttempts() {
        assertFalse(ShellSafety.isSafePackageName("com.tencent.ig; rm -rf /"));
        assertFalse(ShellSafety.isSafePackageName("com.test && whoami"));
        assertFalse(ShellSafety.isSafePackageName("com.pkg | cat /etc/passwd"));
        assertFalse(ShellSafety.isSafePackageName("com.pkg'"));
        assertFalse(ShellSafety.isSafePackageName("com.pkg\""));
        assertFalse(ShellSafety.isSafePackageName("com.pkg\nreboot"));
        assertFalse(ShellSafety.isSafePackageName("com.pkg$PATH"));
        assertFalse(ShellSafety.isSafePackageName("com.pkg`id`"));
    }

    @Test
    public void testIsSafePackageName_NullAndEmpty() {
        assertFalse(ShellSafety.isSafePackageName(null));
        assertFalse(ShellSafety.isSafePackageName(""));
        assertFalse(ShellSafety.isSafePackageName("   "));
    }

    @Test
    public void testIsSafeShellPath() {
        assertTrue(ShellSafety.isSafeShellPath("/data/data/com.tencent.ig/files/Active.sav"));
        assertTrue(ShellSafety.isSafeShellPath("/sdcard/Android/data/com.mobile.legends/files"));
        assertFalse(ShellSafety.isSafeShellPath("/data/../etc/passwd"));
        assertFalse(ShellSafety.isSafeShellPath("/data/data/pkg; rm -rf /"));
    }

    @Test
    public void testEscapeSingleQuoted() {
        assertEquals("''", ShellSafety.escapeSingleQuoted(""));
        assertEquals("'hello'", ShellSafety.escapeSingleQuoted("hello"));
        assertEquals("'hello'\\''world'", ShellSafety.escapeSingleQuoted("hello'world"));
    }
}
