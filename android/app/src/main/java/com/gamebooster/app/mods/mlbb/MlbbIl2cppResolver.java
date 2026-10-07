package com.gamebooster.app.mods.mlbb;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.engine.Il2cppDirectScanner;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * MlbbIl2cppResolver — Resolves target method RVAs, struct offsets, tokens, and pattern signatures
 * in libil2cpp.so. Loads deterministic offsets from assets/frida/mlbb_il2cpp_offsets.json
 * and bridges dynamic in-memory signature scanning via Il2cppDirectScanner as a fallback.
 */
public final class MlbbIl2cppResolver {

    private static final String TAG = "MlbbIl2cppResolver";
    private static final Map<String, Long> METHOD_CACHE = new HashMap<>();
    private static final Map<String, Long> FIELD_CACHE = new HashMap<>();
    private static final Map<String, Long> TOKEN_CACHE = new HashMap<>();
    private static final Map<String, byte[]> SIGNATURE_PATTERNS = new HashMap<>();
    private static final Map<String, String> SIGNATURE_MASKS = new HashMap<>();

    private MlbbIl2cppResolver() {}

    /**
     * Loads dumped offset table, fields, tokens, and signatures from assets/frida/mlbb_il2cpp_offsets.json.
     */
    public static synchronized void loadOffsets(Context ctx) {
        if (!METHOD_CACHE.isEmpty()) return;

        try (InputStream is = ctx.getAssets().open("frida/mlbb_il2cpp_offsets.json")) {
            byte[] buf = new byte[is.available()];
            int read = is.read(buf);
            if (read <= 0) {
                initDefaults();
                return;
            }
            String jsonStr = new String(buf, "UTF-8");
            JSONObject json = new JSONObject(jsonStr);

            // 1. Methods
            JSONObject methods = json.optJSONObject("methods");
            if (methods != null) {
                for (Iterator<String> it = methods.keys(); it.hasNext(); ) {
                    String key = it.next();
                    try {
                        long offset = Long.decode(methods.getString(key));
                        METHOD_CACHE.put(key, offset);
                    } catch (Exception ignored) {}
                }
            }

            // 2. Fields
            JSONObject fields = json.optJSONObject("fields");
            if (fields != null) {
                for (Iterator<String> it = fields.keys(); it.hasNext(); ) {
                    String key = it.next();
                    try {
                        long offset = Long.decode(fields.getString(key));
                        FIELD_CACHE.put(key, offset);
                    } catch (Exception ignored) {}
                }
            }

            // 3. Tokens
            JSONObject tokens = json.optJSONObject("tokens");
            if (tokens != null) {
                for (Iterator<String> it = tokens.keys(); it.hasNext(); ) {
                    String key = it.next();
                    try {
                        long token = Long.decode(tokens.getString(key));
                        TOKEN_CACHE.put(key, token);
                    } catch (Exception ignored) {}
                }
            }

            // 4. Signatures
            JSONObject signatures = json.optJSONObject("signatures");
            if (signatures != null) {
                for (Iterator<String> it = signatures.keys(); it.hasNext(); ) {
                    String key = it.next();
                    JSONObject sigObj = signatures.optJSONObject(key);
                    if (sigObj != null) {
                        String hexPat = sigObj.optString("pattern", "");
                        String mask = sigObj.optString("mask", "");
                        if (!hexPat.isEmpty() && !mask.isEmpty()) {
                            byte[] patternBytes = hexStringToBytes(hexPat);
                            if (patternBytes != null) {
                                SIGNATURE_PATTERNS.put(key, patternBytes);
                                SIGNATURE_MASKS.put(key, mask);
                            }
                        }
                    }
                }
            }

            Log.i(TAG, "Loaded " + METHOD_CACHE.size() + " methods, " + FIELD_CACHE.size()
                    + " fields, and " + SIGNATURE_PATTERNS.size() + " signatures.");
        } catch (Throwable t) {
            Log.w(TAG, "Offset manifest load warning (using defaults): " + t.getMessage());
            initDefaults();
        }
    }

    private static void initDefaults() {
        // Fallback default RVAs for libil2cpp.so (v2.2.16.x)
        METHOD_CACHE.put("HeroDamageCalc_CalculateDamage", 0x0182C4D0L);
        METHOD_CACHE.put("HeroAttackSpeed_GetMultiplier",   0x01831E20L);
        METHOD_CACHE.put("SkillManager_GetCooldownTime",   0x0184A100L);
        METHOD_CACHE.put("FogOfWarManager_IsVisible",       0x019056B0L);
        METHOD_CACHE.put("CameraManager_SetHeight",         0x019128A0L);

        FIELD_CACHE.put("Hero_CurrentHp", 0x48L);
        FIELD_CACHE.put("Hero_MaxHp", 0x4CL);
        FIELD_CACHE.put("Hero_AttackPower", 0x54L);
        FIELD_CACHE.put("Hero_AttackSpeed", 0x5CL);
        FIELD_CACHE.put("Hero_MovementSpeed", 0x68L);
    }

    public static long getOffset(String key) {
        Long val = METHOD_CACHE.get(key);
        return val != null ? val : 0L;
    }

    public static long getMethodOffset(String key) {
        return getOffset(key);
    }

    public static long getFieldOffset(String key) {
        Long val = FIELD_CACHE.get(key);
        return val != null ? val : 0L;
    }

    public static long getToken(String key) {
        Long val = TOKEN_CACHE.get(key);
        return val != null ? val : 0L;
    }

    /**
     * Attempts dynamic in-memory pattern scan via Il2cppDirectScanner if static RVA is unresolved.
     */
    public static long resolveDynamicOffset(String key) {
        long staticOffset = getOffset(key);
        if (staticOffset != 0L) {
            return staticOffset;
        }

        if (!Il2cppDirectScanner.isAvailable()) {
            return 0L;
        }

        byte[] pat = SIGNATURE_PATTERNS.get(key);
        String mask = SIGNATURE_MASKS.get(key);
        if (pat == null || mask == null) {
            return 0L;
        }

        long matchedAddr = Il2cppDirectScanner.scanPattern(pat, mask);
        if (matchedAddr != 0L) {
            long base = Il2cppDirectScanner.getIl2cppBase();
            long rva = matchedAddr - base;
            METHOD_CACHE.put(key, rva);
            Log.i(TAG, "Dynamically resolved RVA for " + key + " -> 0x" + Long.toHexString(rva));
            return rva;
        }

        return 0L;
    }

    private static byte[] hexStringToBytes(String s) {
        if (s == null || s.length() % 2 != 0) return null;
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}

