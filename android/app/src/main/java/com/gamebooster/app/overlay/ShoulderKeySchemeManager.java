package com.gamebooster.app.overlay;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

/**
 * ShoulderKeySchemeManager — Manages L1/R1 trigger coordinates and schemes.
 * Supports per-game coordinate profiles and scheme sharing.
 */
public final class ShoulderKeySchemeManager {

    private static final String PREF_NAME = "game_shoulder_key_schemes";

    public static class TriggerPoint {
        public int x;
        public int y;
        public String label;

        public TriggerPoint(int x, int y, String label) {
            this.x = x;
            this.y = y;
            this.label = label;
        }
    }

    public static class Scheme {
        public TriggerPoint l1;
        public TriggerPoint r1;

        public Scheme(TriggerPoint l1, TriggerPoint r1) {
            this.l1 = l1;
            this.r1 = r1;
        }
    }

    private ShoulderKeySchemeManager() {}

    public static Scheme getSchemeForPackage(Context context, String packageName) {
        if (context == null) return getDefaultScheme();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String raw = prefs.getString("pkg_" + packageName, null);
        if (raw == null) {
            return getDefaultScheme();
        }

        try {
            JSONObject obj = new JSONObject(raw);
            JSONObject l1Obj = obj.getJSONObject("l1");
            JSONObject r1Obj = obj.getJSONObject("r1");
            return new Scheme(
                    new TriggerPoint(l1Obj.getInt("x"), l1Obj.getInt("y"), l1Obj.optString("label", "L1")),
                    new TriggerPoint(r1Obj.getInt("x"), r1Obj.getInt("y"), r1Obj.optString("label", "R1"))
            );
        } catch (Exception e) {
            return getDefaultScheme();
        }
    }

    public static void saveSchemeForPackage(Context context, String packageName, Scheme scheme) {
        if (context == null || scheme == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        try {
            JSONObject obj = new JSONObject();
            JSONObject l1Obj = new JSONObject();
            l1Obj.put("x", scheme.l1.x);
            l1Obj.put("y", scheme.l1.y);
            l1Obj.put("label", scheme.l1.label);

            JSONObject r1Obj = new JSONObject();
            r1Obj.put("x", scheme.r1.x);
            r1Obj.put("y", scheme.r1.y);
            r1Obj.put("label", scheme.r1.label);

            obj.put("l1", l1Obj);
            obj.put("r1", r1Obj);

            prefs.edit().putString("pkg_" + packageName, obj.toString()).apply();
        } catch (Exception ignored) {}
    }

    public static Scheme getDefaultScheme() {
        // Default position: L1 top-left, R1 top-right
        return new Scheme(
                new TriggerPoint(250, 450, "L1 SCOPE"),
                new TriggerPoint(1800, 450, "R1 FIRE")
        );
    }
}
