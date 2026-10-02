package com.gamebooster.app.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ShellSafety}.
 * Verifies strict shell injection protection for package names and filesystem paths.
 */
public class ShellSafetyTest {

    @Test
    public void testSafePackageName_validPackages() {
        assertTrue(ShellSafety.isSafePackageName("com.mobile.legends"));
        assertTrue(ShellSafety.isSafePackageName("com.tencent.ig"));
        assertTrue(ShellSafety.isSafePackageName("com.activision.callofduty.shooter"));
        assertTrue(ShellSafety.isSafePackageName("com.dts.freefiremax"));
        assertTrue(ShellSafety.isSafePackageName("a.b.c_123"));
    }

    @Test
    public void testSafePackageName_invalidPackages() {
        assertFalse("Null package", ShellSafety.isSafePackageName(null));
        assertFalse("Empty package", ShellSafety.isSafePackageName(""));
        assertFalse("Whitespace package", ShellSafety.isSafePackageName("   "));
        assertFalse("Command injection with semicolon", ShellSafety.isSafePackageName("com.game; rm -rf /"));
        assertFalse("Command injection with pipe", ShellSafety.isSafePackageName("com.game|reboot"));
        assertFalse("Command injection with backticks", ShellSafety.isSafePackageName("com.game`reboot`"));
        assertFalse("Command injection with subshell", ShellSafety.isSafePackageName("com.game$(reboot)"));
        assertFalse("Single quotes in package", ShellSafety.isSafePackageName("com.game'test"));
        assertFalse("Double quotes in package", ShellSafety.isSafePackageName("com.game\"test"));
        assertFalse("Leading dot", ShellSafety.isSafePackageName(".com.game"));
        assertFalse("Trailing dot", ShellSafety.isSafePackageName("com.game."));
        assertFalse("Only punctuation", ShellSafety.isSafePackageName("...."));
    }

    @Test
    public void testSafeShellPath_validPaths() {
        assertTrue(ShellSafety.isSafeShellPath("/storage/emulated/0/Android/data/com.tencent.ig/files/UserCustom.ini"));
        assertTrue(ShellSafety.isSafeShellPath("/data/data/com.mobile.legends/shared_prefs/com.mobile.legends.v2.playerprefs.xml"));
        assertTrue(ShellSafety.isSafeShellPath("/sdcard/Android/data/com.activision.callofduty.shooter/files/config.cfg"));
    }

    @Test
    public void testSafeShellPath_pathTraversalAndInjectionRejected() {
        assertFalse("Null path", ShellSafety.isSafeShellPath(null));
        assertFalse("Empty path", ShellSafety.isSafeShellPath(""));
        assertFalse("Path with double dots traversal", ShellSafety.isSafeShellPath("/sdcard/Android/data/../../etc/passwd"));
        assertFalse("Path with single dot segment", ShellSafety.isSafeShellPath("/sdcard/./Android/data"));
        assertFalse("Shell injection with semicolon", ShellSafety.isSafeShellPath("/sdcard/file.ini; reboot"));
        assertFalse("Shell injection with dollar sign", ShellSafety.isSafeShellPath("/sdcard/$HOME/test.ini"));
        assertFalse("Shell injection with space", ShellSafety.isSafeShellPath("/sdcard/my file.ini"));
        assertFalse("Shell injection with ampersand", ShellSafety.isSafeShellPath("/sdcard/test.ini&&whoami"));
    }

    @Test
    public void testEscapeSingleQuoted() {
        assertEquals("''", ShellSafety.escapeSingleQuoted(null));
        assertEquals("'normal_string'", ShellSafety.escapeSingleQuoted("normal_string"));
        assertEquals("'/path/to/file.ini'", ShellSafety.escapeSingleQuoted("/path/to/file.ini"));
        assertEquals("'it'\\''s dangerous'", ShellSafety.escapeSingleQuoted("it's dangerous"));
        assertEquals("'; rm -rf / ;'\\'''", ShellSafety.escapeSingleQuoted("; rm -rf / ;'"));
    }
}
