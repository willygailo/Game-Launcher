package com.gamebooster.app.mods.mlbb;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * MlbbIl2cppResolver — Resolves target method RVAs and struct offsets in libil2cpp.so.
 * Loads deterministic offsets dumped from APK disassembly or falls back to known signature bases.
 */
public final class MlbbIl2cppResolver {

    private static final String TAG = "MlbbIl2cppResolver";
    private static final Map<String, Long> OFFSET_CACHE = new HashMap<>();

    private MlbbIl2cppResolver() {}

    /**
     * Loads dumped offset table from assets/frida/mlbb_il2cpp_offsets.json.
     */
    public static synchronized void loadOffsets(Context ctx) {
        if (!OFFSET_CACHE.isEmpty()) return;

        try (InputStream is = ctx.getAssets().open("frida/mlbb_il2cpp_offsets.json")) {
            byte[] buf = new byte[is.available()];
            is.read(buf);
            String jsonStr = new String(buf, "UTF-8");
            JSONObject json = new JSONObject(jsonStr);

            JSONObject methods = json.optJSONObject("methods");
            if (methods != null) {
                for (java.util.Iterator<String> it = methods.keys(); it.hasNext(); ) {
                    String key = it.next();
                    String hexVal = methods.getString(key);
                    long offset = Long.decode(hexVal);
                    OFFSET_CACHE.put(key, offset);
                }
            }
            Log.i(TAG, "Loaded " + OFFSET_CACHE.size() + " IL2CPP target offsets.");
        } catch (Throwable t) {
            Log.w(TAG, "Offset manifest load warning (using defaults): " + t.getMessage());
            initDefaults();
        }
    }

    private static void initDefaults() {
        // Fallback default RVAs for libil2cpp.so (v2.2.16.x)
        OFFSET_CACHE.put("HeroDamageCalc_CalculateDamage", 0x0182C4D0L);
        OFFSET_CACHE.put("HeroAttackSpeed_GetMultiplier",   0x01831E20L);
        OFFSET_CACHE.put("SkillManager_GetCooldownTime",   0x0184A100L);
        OFFSET_CACHE.put("FogOfWarManager_IsVisible",       0x019056B0L);
        OFFSET_CACHE.put("CameraManager_SetHeight",         0x019128A0L);
    }

    public static long getOffset(String key) {
        Long val = OFFSET_CACHE.get(key);
        return val != null ? val : 0L;
    }
}
