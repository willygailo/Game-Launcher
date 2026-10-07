package com.gamebooster.app.mods.codm;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * CodmIl2cppResolver — Resolves target method RVAs, field offsets, and tokens
 * for Call of Duty Mobile (v1.6.57 Unity IL2CPP v23 monolithic engine in libunity.so).
 * Reads deterministic offset maps and tokens from assets/frida/codm_offsets.json.
 */
public final class CodmIl2cppResolver {

    private static final String TAG = "CodmIl2cppResolver";
    private static final Map<String, Long> OFFSET_CACHE = new HashMap<>();
    private static final Map<String, Long> TOKEN_CACHE = new HashMap<>();
    private static final Map<String, Long> FIELD_CACHE = new HashMap<>();

    private CodmIl2cppResolver() {}

    /**
     * Loads dumped offset table, fields, and tokens from assets/frida/codm_offsets.json.
     */
    public static synchronized void loadOffsets(Context ctx) {
        if (!OFFSET_CACHE.isEmpty()) return;

        try (InputStream is = ctx.getAssets().open("frida/codm_offsets.json")) {
            byte[] buf = new byte[is.available()];
            int read = is.read(buf);
            if (read <= 0) {
                initDefaults();
                return;
            }
            String jsonStr = new String(buf, "UTF-8");
            JSONObject json = new JSONObject(jsonStr);

            // 1. Target method RVAs / offsets
            JSONObject offsets = json.optJSONObject("offsets");
            if (offsets != null) {
                for (Iterator<String> it = offsets.keys(); it.hasNext(); ) {
                    String key = it.next();
                    try {
                        long val = Long.decode(offsets.getString(key));
                        OFFSET_CACHE.put(key, val);
                    } catch (Exception ignored) {}
                }
            }

            // 2. Metadata Tokens
            JSONObject tokens = json.optJSONObject("tokens");
            if (tokens != null) {
                for (Iterator<String> it = tokens.keys(); it.hasNext(); ) {
                    String key = it.next();
                    try {
                        long val = Long.decode(tokens.getString(key));
                        TOKEN_CACHE.put(key, val);
                    } catch (Exception ignored) {}
                }
            }

            // 3. Field Offsets
            JSONObject fields = json.optJSONObject("field_offsets");
            if (fields != null) {
                for (Iterator<String> it = fields.keys(); it.hasNext(); ) {
                    String key = it.next();
                    try {
                        long val = Long.decode(fields.getString(key));
                        FIELD_CACHE.put(key, val);
                    } catch (Exception ignored) {}
                }
            }

            Log.i(TAG, "Loaded " + OFFSET_CACHE.size() + " offsets, " +
                       TOKEN_CACHE.size() + " tokens, and " +
                       FIELD_CACHE.size() + " fields for CODM v1.6.57");
        } catch (Exception e) {
            Log.w(TAG, "Failed to load codm_offsets.json from assets: " + e.getMessage());
            initDefaults();
        }
    }

    private static void initDefaults() {
        // Fallback hardcoded values for v1.6.57
        OFFSET_CACHE.put("Camera_get_main", 0x00F82410L);
        OFFSET_CACHE.put("Transform_get_position", 0x00ED8920L);
        OFFSET_CACHE.put("Transform_set_position", 0x00ED8A30L);
        OFFSET_CACHE.put("WeaponController_RecoilModifier", 0x01E4A9C0L);
        OFFSET_CACHE.put("WeaponController_SpreadModifier", 0x01E4AD50L);
        OFFSET_CACHE.put("PlayerController_SpeedMultiplier", 0x01D382B0L);

        // Core Combat Tokens
        TOKEN_CACHE.put("RecoilScaleWeaponShake", 0x06012c6bL);
        TOKEN_CACHE.put("ShotSpread", 0x06014791L);
        TOKEN_CACHE.put("RandomShotSpread", 0x060148bfL);
        TOKEN_CACHE.put("CalcShotSpreadSize", 0x06014a9fL);
        TOKEN_CACHE.put("UseAimAssist", 0x0601480bL);
        TOKEN_CACHE.put("AimAssistDis", 0x06014815L);
        TOKEN_CACHE.put("RecoilUpBase", 0x06014851L);
        TOKEN_CACHE.put("RecoilUpMax", 0x06014853L);
        TOKEN_CACHE.put("RecoilLateralModifier", 0x06014855L);
        TOKEN_CACHE.put("EnableAimAssistanceForSniper", 0x0601d7c7L);
        TOKEN_CACHE.put("AimAssistanceSpeed", 0x0601d7cdL);
        TOKEN_CACHE.put("RecoilFactor", 0x060202b1L);
        TOKEN_CACHE.put("IsOpenAimAssist", 0x060203a5L);

        // Field Struct Offsets
        FIELD_CACHE.put("GAS_MainFireFunnelTask_RecoilUpBase", 0x28L);
        FIELD_CACHE.put("GAS_MainFireFunnelTask_RecoilUpModifier", 0x2CL);
        FIELD_CACHE.put("GAS_MeleeAttackTask_RecoilUpMax", 0x30L);
        FIELD_CACHE.put("GAS_MeleeAttackTask_RecoilLateralBase", 0x34L);
        FIELD_CACHE.put("GAS_MeleeAttackTask_RecoilLateralModifier", 0x38L);
        FIELD_CACHE.put("GAS_MeleeAttackTask_Damage", 0x54L);
    }

    public static long getOffset(String key) {
        Long val = OFFSET_CACHE.get(key);
        return val != null ? val : 0L;
    }

    public static long getToken(String key) {
        Long val = TOKEN_CACHE.get(key);
        return val != null ? val : 0L;
    }

    public static long getFieldOffset(String key) {
        Long val = FIELD_CACHE.get(key);
        return val != null ? val : 0L;
    }
}
