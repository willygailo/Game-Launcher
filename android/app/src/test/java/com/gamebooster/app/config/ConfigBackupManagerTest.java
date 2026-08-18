package com.gamebooster.app.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class ConfigBackupManagerTest {

    // ─── SHA-256 hashing (backup/restore round-trip integrity) ──────────────

    @Test
    public void sha256Hex_knownVector() {
        // SHA-256("hello") — deterministic reference
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
                ConfigBackupManager.sha256Hex("hello"));
    }

    @Test
    public void sha256Hex_deterministicForSameBytes() {
        byte[] data = "original game config bytes".getBytes(StandardCharsets.UTF_8);
        assertEquals(ConfigBackupManager.sha256Hex(data), ConfigBackupManager.sha256Hex(data));
    }

    @Test
    public void sha256Hex_differsForDifferentContent() {
        String a = ConfigBackupManager.sha256Hex("config A");
        String b = ConfigBackupManager.sha256Hex("config B");
        assertFalse(a.equals(b));
    }

    @Test
    public void sha256Hex_stringAndBytes_agree() {
        String text = "shared_prefs payload";
        assertEquals(ConfigBackupManager.sha256Hex(text),
                ConfigBackupManager.sha256Hex(text.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void sha256Hex_nullString_returnsEmpty() {
        assertEquals("", ConfigBackupManager.sha256Hex((String) null));
    }

    @Test
    public void sha256Hex_produces64LowerCaseHexChars() {
        String hash = ConfigBackupManager.sha256Hex("x");
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"));
    }

    // ─── sanitize (record keys / backup dir names — no shell/path injection) ─

    @Test
    public void sanitize_null_becomesUnknown() {
        assertEquals("unknown", ConfigBackupManager.sanitize(null));
    }

    @Test
    public void sanitize_whitelistedCharsUnchanged() {
        assertEquals("com.mobile.legends", ConfigBackupManager.sanitize("com.mobile.legends"));
        assertEquals("a-b_c.d", ConfigBackupManager.sanitize("a-b_c.d"));
    }

    @Test
    public void sanitize_stripsShellAndPathMetacharacters() {
        assertEquals("a_b_c_d", ConfigBackupManager.sanitize("a;b'c\nd"));
        assertEquals("a_b", ConfigBackupManager.sanitize("a b"));
        assertEquals("_", ConfigBackupManager.sanitize(";"));
        assertEquals("__", ConfigBackupManager.sanitize("'\""));
    }

    @Test
    public void sanitize_neverEmitsShellHazards() {
        for (String hostile : new String[]{
                "com.x;rm -rf /", "a'b", "a\"b", "a$b", "a`b", "a|b", "a&b",
                "../evil", "/tmp/x", "a\\\\b"}) {
            String clean = ConfigBackupManager.sanitize(hostile);
            assertFalse("sanitize left shell hazard from '" + hostile + "' → '" + clean + "'",
                    clean.matches(".*[;'\"$`|&\\\\/\\s].*"));
        }
    }

    @Test
    public void sanitize_collapsesNonWhitelistedCharsToUnderscore() {
        // Per-char replacement: ';' and ' ' both map to '_' (collision by design)
        assertEquals("a_b", ConfigBackupManager.sanitize("a;b"));
        assertEquals("a_b", ConfigBackupManager.sanitize("a b"));
        assertEquals(ConfigBackupManager.sanitize("a;b"), ConfigBackupManager.sanitize("a b"));
        // Whitelisted '-' survives unchanged and stays distinct
        assertEquals("a-b", ConfigBackupManager.sanitize("a-b"));
        assertFalse(ConfigBackupManager.sanitize("a-b").equals(ConfigBackupManager.sanitize("a;b")));
    }
}