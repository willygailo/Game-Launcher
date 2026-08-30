package com.gamebooster.app.spoofer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.UUID;

/**
 * DeviceIdentityGenerator — Generates brand-authentic, deterministic, and unique hardware
 * identifiers for every SpoofProfile to prevent device cross-fingerprinting and account bans.
 *
 * Covers 7 Detection Vectors:
 * 1. Android ID (64-bit lowercase 16-hex characters with full entropy)
 * 2. Hardware Build & Boot Serial Numbers (Pattern-matched to Samsung, ASUS ROG, Xiaomi, Nubia, Vivo, OnePlus, Pixel)
 * 3. Genuine IEEE OUI MAC Addresses (Wi-Fi & Bluetooth MAC with vendor-allocated prefixes)
 * 4. OAID (Open Anonymous Device Identifier / MSA UUID)
 * 5. GSF ID (Google Services Framework 16-hex identifier)
 * 6. Widevine DRM Hardware Device ID (32-byte hash)
 * 7. Google Advertising ID (AAID / GAID UUID)
 */
public final class DeviceIdentityGenerator {

    private DeviceIdentityGenerator() {}

    /**
     * Generates a realistic 16-hex character Settings.Secure.ANDROID_ID.
     * Full 64-bit entropy derived from SHA-256 hash of profile ID and model.
     */
    public static String generateAndroidId(SpoofProfile profile) {
        String seed = (profile != null ? profile.id + ":" + profile.model + ":" + profile.brand : "generic_default");
        byte[] hash = sha256(seed + ":android_id_v2");
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 8; i++) {
            sb.append(String.format("%02x", hash[i]));
        }
        return sb.toString().toLowerCase(Locale.US);
    }

    /**
     * Generates a brand-accurate serial number matching real OEM factory formatting.
     */
    public static String generateSerialNumber(SpoofProfile profile) {
        if (profile == null) return "R58M0000000";
        String brand = profile.brand != null ? profile.brand.toLowerCase(Locale.US) : "";
        String model = profile.model != null ? profile.model.toUpperCase(Locale.US) : "";
        byte[] hash = sha256(profile.id + ":serial_v2");

        if (brand.contains("samsung")) {
            // Samsung format: e.g. R58M123456T or RFCN80ABCDE (11 alphanumeric)
            String part1 = String.format("%04X", ((hash[0] & 0xFF) << 8) | (hash[1] & 0xFF));
            String part2 = String.format("%04X", ((hash[2] & 0xFF) << 8) | (hash[3] & 0xFF));
            char suffix = (char) ('A' + (Math.abs(hash[4]) % 26));
            return "R58M" + part1.substring(0, 3) + part2.substring(0, 3) + suffix;
        } else if (brand.contains("asus")) {
            // ASUS ROG format: e.g. M1AZB6001234 or N2AZB6005678 (12 alphanumeric)
            String hex = String.format("%06X", ((hash[0] & 0xFF) << 16) | ((hash[1] & 0xFF) << 8) | (hash[2] & 0xFF));
            return "N2AZB6" + hex;
        } else if (brand.contains("xiaomi") || brand.contains("blackshark") || brand.contains("poco") || brand.contains("redmi")) {
            // Xiaomi format: e.g. 0x82a49b2f or 82a49b2f1c (8 to 10 hex)
            String hex = String.format("%08x", ((hash[0] & 0xFF) << 24) | ((hash[1] & 0xFF) << 16) | ((hash[2] & 0xFF) << 8) | (hash[3] & 0xFF));
            return "0x" + hex;
        } else if (brand.contains("nubia") || brand.contains("zte") || model.contains("REDMAGIC")) {
            // Nubia RedMagic format: e.g. NX769J88123456
            String modelPrefix = profile.device != null && !profile.device.isEmpty() ? profile.device.toUpperCase(Locale.US) : "NX769J";
            String hex = String.format("%06X", ((hash[0] & 0xFF) << 16) | ((hash[1] & 0xFF) << 8) | (hash[2] & 0xFF));
            return modelPrefix + hex;
        } else if (brand.contains("vivo") || brand.contains("iqoo")) {
            // Vivo / iQOO format: e.g. V2302A829301
            String prefix = profile.device != null && !profile.device.isEmpty() ? profile.device.toUpperCase(Locale.US) : "V2302A";
            String hex = String.format("%06X", ((hash[0] & 0xFF) << 16) | ((hash[1] & 0xFF) << 8) | (hash[2] & 0xFF));
            return prefix + hex;
        } else if (brand.contains("google")) {
            // Pixel format: e.g. 1A231FDEE00123 (14 alphanumeric)
            String hex = String.format("%08X%04X",
                    ((hash[0] & 0xFF) << 24) | ((hash[1] & 0xFF) << 16) | ((hash[2] & 0xFF) << 8) | (hash[3] & 0xFF),
                    ((hash[4] & 0xFF) << 8) | (hash[5] & 0xFF));
            return "1A" + hex;
        } else if (brand.contains("oneplus") || brand.contains("oppo") || brand.contains("realme")) {
            // OnePlus / OPPO / Realme format: e.g. f0a1b2c3d4e5 (12 hex chars)
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", hash[i]));
            }
            return sb.toString();
        } else if (brand.contains("lenovo")) {
            // Lenovo Legion format: e.g. HA123456
            String hex = String.format("%06X", ((hash[0] & 0xFF) << 16) | ((hash[1] & 0xFF) << 8) | (hash[2] & 0xFF));
            return "HA" + hex;
        } else {
            // Standard Android OEM fallback
            String hex = String.format("%08X", ((hash[0] & 0xFF) << 24) | ((hash[1] & 0xFF) << 16) | ((hash[2] & 0xFF) << 8) | (hash[3] & 0xFF));
            return "SN" + hex;
        }
    }

    /**
     * Generates a genuine IEEE OUI Wi-Fi MAC Address matching the device brand.
     */
    public static String generateWifiMacAddress(SpoofProfile profile) {
        String oui = getBrandOui(profile, false);
        byte[] hash = sha256((profile != null ? profile.id : "default") + ":wifi_mac_v2");
        return String.format("%s:%02X:%02X:%02X", oui, hash[0] & 0xFF, hash[1] & 0xFF, hash[2] & 0xFF);
    }

    /**
     * Generates a paired genuine IEEE OUI Bluetooth MAC Address matching the device brand.
     */
    public static String generateBluetoothMacAddress(SpoofProfile profile) {
        String oui = getBrandOui(profile, true);
        byte[] hash = sha256((profile != null ? profile.id : "default") + ":bt_mac_v2");
        return String.format("%s:%02X:%02X:%02X", oui, hash[0] & 0xFF, hash[1] & 0xFF, hash[2] & 0xFF);
    }

    /**
     * Generates a deterministic 36-character OAID (Open Anonymous Device Identifier) UUID.
     */
    public static String generateOaid(SpoofProfile profile) {
        byte[] hash = sha256((profile != null ? profile.id : "default") + ":oaid_v2");
        return UUID.nameUUIDFromBytes(hash).toString().toLowerCase(Locale.US);
    }

    /**
     * Generates a deterministic 16-hex character GSF (Google Services Framework) ID.
     */
    public static String generateGsfId(SpoofProfile profile) {
        byte[] hash = sha256((profile != null ? profile.id : "default") + ":gsf_id_v2");
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 8; i++) {
            sb.append(String.format("%02x", hash[i]));
        }
        return sb.toString().toLowerCase(Locale.US);
    }

    /**
     * Generates a deterministic 64-hex character Widevine DRM Device Unique ID.
     */
    public static String generateWidevineDeviceId(SpoofProfile profile) {
        byte[] hash = sha256((profile != null ? profile.id : "default") + ":widevine_drm_v2");
        StringBuilder sb = new StringBuilder(64);
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString().toLowerCase(Locale.US);
    }

    /**
     * Generates a deterministic 36-character Google Advertising ID (AAID/GAID).
     */
    public static String generateAdvertisingId(SpoofProfile profile) {
        byte[] hash = sha256((profile != null ? profile.id : "default") + ":aaid_v2");
        return UUID.nameUUIDFromBytes(hash).toString().toLowerCase(Locale.US);
    }

    /**
     * Generates a TAC-compliant mock IMEI identifier for dual-SIM telephony spoofing.
     */
    public static String generateImei(SpoofProfile profile, int slot) {
        String brand = profile != null && profile.brand != null ? profile.brand.toLowerCase(Locale.US) : "";
        String tac = "86940205"; // Default 3GPP TAC prefix
        if (brand.contains("samsung")) tac = "35874111";
        else if (brand.contains("asus")) tac = "35928310";
        else if (brand.contains("xiaomi")) tac = "86782404";
        else if (brand.contains("nubia")) tac = "86339106";
        else if (brand.contains("vivo")) tac = "86221505";
        else if (brand.contains("google")) tac = "35412810";
        else if (brand.contains("oneplus")) tac = "86910305";

        byte[] hash = sha256((profile != null ? profile.id : "default") + ":imei_slot_" + slot);
        long serial6 = Math.abs(((long) (hash[0] & 0xFF) << 16) | ((long) (hash[1] & 0xFF) << 8) | (long) (hash[2] & 0xFF)) % 1000000L;
        String body = tac + String.format("%06d", serial6);
        int checkDigit = computeLuhnCheckDigit(body);
        return body + checkDigit;
    }

    private static String getBrandOui(SpoofProfile profile, boolean isBluetooth) {
        if (profile == null || profile.brand == null) return "00:16:32";
        String b = profile.brand.toLowerCase(Locale.US);

        if (b.contains("samsung")) {
            return isBluetooth ? "50:01:D9" : "00:16:32";
        } else if (b.contains("asus")) {
            return isBluetooth ? "AC:9E:17" : "00:1A:92";
        } else if (b.contains("xiaomi") || b.contains("blackshark") || b.contains("poco") || b.contains("redmi")) {
            return isBluetooth ? "78:11:DC" : "18:65:90";
        } else if (b.contains("nubia") || b.contains("zte")) {
            return isBluetooth ? "34:DE:1A" : "00:26:ED";
        } else if (b.contains("vivo") || b.contains("iqoo")) {
            return isBluetooth ? "58:20:59" : "20:82:C0";
        } else if (b.contains("oneplus")) {
            return isBluetooth ? "A0:C5:89" : "94:65:2D";
        } else if (b.contains("oppo") || b.contains("realme")) {
            return isBluetooth ? "4C:58:35" : "10:2A:B3";
        } else if (b.contains("google")) {
            return isBluetooth ? "D8:3C:69" : "3C:5A:B4";
        } else if (b.contains("sony")) {
            return isBluetooth ? "40:40:A7" : "00:01:4A";
        } else if (b.contains("lenovo")) {
            return isBluetooth ? "60:D8:19" : "00:59:07";
        } else {
            return isBluetooth ? "8C:77:12" : "28:18:78";
        }
    }

    private static int computeLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n = (n % 10) + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum * 9) % 10;
    }

    private static byte[] sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            return input.getBytes(StandardCharsets.UTF_8);
        }
    }
}
