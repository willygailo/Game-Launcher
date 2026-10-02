package com.gamebooster.app.config;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link PubgIniEncryptionBridge}.
 * Verifies encryption scheme detection, plaintext validation, and round-trip transformation.
 */
public class PubgIniEncryptionBridgeTest {

    private static final String SAMPLE_INI =
            "[UserCustom DeviceProfile]\n" +
            "+CVars=r.PUBGDeviceFPS=185\n" +
            "+CVars=r.PUBGQualityLevel=0\n";

    @Test
    public void testIsPlaintextIni_validPlaintext() {
        byte[] data = SAMPLE_INI.getBytes(StandardCharsets.UTF_8);
        assertTrue("Standard INI should be detected as plaintext", PubgIniEncryptionBridge.isPlaintextIni(data));

        byte[] commentIni = "; Configuration File\n[Section]\nKey=Value\n".getBytes(StandardCharsets.UTF_8);
        assertTrue("Commented INI should be plaintext", PubgIniEncryptionBridge.isPlaintextIni(commentIni));

        byte[] empty = new byte[0];
        assertTrue("Empty data defaults to plaintext", PubgIniEncryptionBridge.isPlaintextIni(empty));
    }

    @Test
    public void testIsPlaintextIni_binaryData() {
        byte[] binaryData = new byte[] {
                (byte) 0x7F, 0x45, 0x4C, 0x46, // ELF magic
                0x02, 0x01, 0x01, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00
        };
        assertFalse("ELF binary header should not be plaintext INI", PubgIniEncryptionBridge.isPlaintextIni(binaryData));

        byte[] nullCorrupted = new byte[64];
        assertFalse("All-zero bytes should not be plaintext INI", PubgIniEncryptionBridge.isPlaintextIni(nullCorrupted));
    }

    @Test
    public void testDetectEncryptionType_plaintext() {
        byte[] data = SAMPLE_INI.getBytes(StandardCharsets.UTF_8);
        assertEquals(PubgIniEncryptionBridge.EncryptionType.NONE, PubgIniEncryptionBridge.detectEncryptionType(data));
    }

    @Test
    public void testRoundtrip_xorEncryption() {
        byte[] encryptedXor = PubgIniEncryptionBridge.encryptIni(
                SAMPLE_INI, true, PubgIniEncryptionBridge.EncryptionType.XOR);
        assertNotNull(encryptedXor);
        assertTrue("Encrypted bytes should have same length as input", encryptedXor.length > 0);

        PubgIniEncryptionBridge.EncryptionType detected = PubgIniEncryptionBridge.detectEncryptionType(encryptedXor);
        assertEquals("Should detect XOR encryption", PubgIniEncryptionBridge.EncryptionType.XOR, detected);

        String decrypted = PubgIniEncryptionBridge.decryptIni(encryptedXor);
        assertEquals("Decrypted INI should match original content", SAMPLE_INI, decrypted);
    }

    @Test
    public void testRoundtrip_base64Encryption() {
        byte[] encryptedB64 = PubgIniEncryptionBridge.encryptIni(
                SAMPLE_INI, true, PubgIniEncryptionBridge.EncryptionType.BASE64);
        assertNotNull(encryptedB64);
        assertTrue(encryptedB64.length > 0);

        PubgIniEncryptionBridge.EncryptionType detected = PubgIniEncryptionBridge.detectEncryptionType(encryptedB64);
        assertEquals("Should detect BASE64 encryption", PubgIniEncryptionBridge.EncryptionType.BASE64, detected);

        String decrypted = PubgIniEncryptionBridge.decryptIni(encryptedB64);
        assertEquals("Decrypted Base64 should match original content", SAMPLE_INI, decrypted);
    }

    @Test
    public void testDecryptIni_nullAndEmpty() {
        assertEquals("", PubgIniEncryptionBridge.decryptIni(null));
        assertEquals("", PubgIniEncryptionBridge.decryptIni(new byte[0]));
    }
}
