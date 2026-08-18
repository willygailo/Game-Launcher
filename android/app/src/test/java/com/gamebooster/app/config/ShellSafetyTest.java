package com.gamebooster.app.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * ShellSafety + GameConfigPatcher injection surface — package names and paths
 * that reach `sed` / `sh -c` must be validated (no `;` / `'` / `$` injection).
 */
public class ShellSafetyTest {

    // ─── package name validation ─────────────────────────────────────────────

    @Test
    public void validPackageNames_pass() {
        assertTrue(ShellSafety.isSafePackageName("com.mobile.legends"));
        assertTrue(ShellSafety.isSafePackageName("com.tencent.ig"));
        assertTrue(ShellSafety.isSafePackageName("vng.pubgmobile"));
        assertTrue(ShellSafety.isSafePackageName("com.example_game_v2.0"));
        assertTrue(ShellSafety.isSafePackageName("a"));
    }

    @Test
    public void shellMetacharacters_rejected() {
        assertFalse(ShellSafety.isSafePackageName("com.x;rm -rf /"));
        assertFalse(ShellSafety.isSafePackageName("com.x' OR 1=1 --"));
        assertFalse(ShellSafety.isSafePackageName("com.x\"id\""));
        assertFalse(ShellSafety.isSafePackageName("com.x$HOME"));
        assertFalse(ShellSafety.isSafePackageName("com.x`id`"));
        assertFalse(ShellSafety.isSafePackageName("com.x|whoami"));
        assertFalse(ShellSafety.isSafePackageName("com.x&&echo pwned"));
        assertFalse(ShellSafety.isSafePackageName("com.x<file"));
        assertFalse(ShellSafety.isSafePackageName("/com/x"));
        assertFalse(ShellSafety.isSafePackageName("com x"));
    }

    @Test
    public void degenerateInputs_rejected() {
        assertFalse(ShellSafety.isSafePackageName(null));
        assertFalse(ShellSafety.isSafePackageName(""));
        assertFalse(ShellSafety.isSafePackageName("   "));
        assertFalse(ShellSafety.isSafePackageName("."));
        assertFalse(ShellSafety.isSafePackageName("..."));
    }

    @Test
    public void overlongPackageName_rejected() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) sb.append('a');
        assertFalse(ShellSafety.isSafePackageName(sb.toString()));
    }

    // ─── shell path validation (sed operands) ───────────────────────────────

    @Test
    public void validAbsolutePaths_pass() {
        assertTrue(ShellSafety.isSafeShellPath("/data/data/com.x/files/UserSystem.ini"));
        assertTrue(ShellSafety.isSafeShellPath("/storage/emulated/0/Android/data/com.x/files/a-1_2.ini"));
    }

    @Test
    public void hostilePaths_rejected() {
        assertFalse(ShellSafety.isSafeShellPath("/data/data/com.x;rm -rf /"));
        assertFalse(ShellSafety.isSafeShellPath("/data/data/com.x'$(id)'"));
        assertFalse(ShellSafety.isSafeShellPath("/tmp/a b.ini"));
        assertFalse(ShellSafety.isSafeShellPath("/tmp/a\tb.ini"));
        assertFalse(ShellSafety.isSafeShellPath("$(touch /tmp/x)"));
        assertFalse(ShellSafety.isSafeShellPath("/tmp/../../etc/passwd"));
        assertFalse(ShellSafety.isSafeShellPath(""));
        assertFalse(ShellSafety.isSafeShellPath(null));
    }

    // ─── single-quote escaping ──────────────────────────────────────────────

    @Test
    public void escapeSingleQuoted_wrapsPlainToken() {
        assertEquals("'/data/data/com.x/files/a.ini'",
                ShellSafety.escapeSingleQuoted("/data/data/com.x/files/a.ini"));
    }

    @Test
    public void escapeSingleQuoted_neutralizesEmbeddedQuotes() {
        assertEquals("'a'\\''b'", ShellSafety.escapeSingleQuoted("a'b"));
        String escaped = ShellSafety.escapeSingleQuoted("'; rm -rf /; '");
        // The whole input must remain ONE single-quoted shell token:
        // ' ( [^'] | '\'' )* '  — no unquoted content, no command stitching.
        assertTrue("escaped output is not a single quoted token: " + escaped,
                escaped.matches("^'([^']|'\\\\'')*'$"));
        assertTrue(escaped.startsWith("'"));
        assertTrue(escaped.endsWith("'"));
    }

    @Test
    public void escapeSingleQuoted_nullAndEmpty() {
        assertEquals("''", ShellSafety.escapeSingleQuoted(null));
        assertEquals("''", ShellSafety.escapeSingleQuoted(""));
    }

    // ─── GameConfigPatcher entry rejection (no Android/Shizuku touched) ─────

    @Test
    public void applyGameFpsPatch_rejectsHostilePackageNames() {
        GameConfigPatcher.PatchResult r = GameConfigPatcher.applyGameFpsPatch("com.mobile.legends;rm -rf /", 120);
        assertFalse(r.success);
        assertTrue(r.message.contains("Unsafe"));
    }

    @Test
    public void applyGameFpsPatch_rejectsQuoteInjection() {
        GameConfigPatcher.PatchResult r = GameConfigPatcher.applyGameFpsPatch("com.x' OR '1'='1", 120);
        assertFalse(r.success);
        assertTrue(r.message.contains("Unsafe"));
    }

    @Test
    public void applyGameFpsPatch_rejectsNullAndBlank() {
        assertFalse(GameConfigPatcher.applyGameFpsPatch(null, 120).success);
        assertFalse(GameConfigPatcher.applyGameFpsPatch("", 120).success);
        assertFalse(GameConfigPatcher.applyGameFpsPatch("   ", 120).success);
    }
}