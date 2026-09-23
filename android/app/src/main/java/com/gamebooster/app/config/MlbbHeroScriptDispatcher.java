package com.gamebooster.app.config;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.gamebooster.app.config.MlbbHeroScriptRegistry.HeroEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * MlbbHeroScriptDispatcher — 2026 New Patch Method Engine.
 *
 * Dispatches per-hero modifier script commands at game launch.
 * Each hero script (assets/lua/mlbb_heroes/<id>_<name>_script.lua) defines:
 *   - s1/s2/ult_damage   — per-skill damage overrides
 *   - s1/s2/ult_cd       — per-skill cooldown overrides (seconds)
 *   - attack_range        — ranged/melee range extension
 *   - move_speed          — movement speed value
 *   - attack_speed        — attack animation speed multiplier
 *   - game_speed          — game tick rate multiplier (2026 NEW)
 *   - vision_range        — hero-specific sight radius
 *   - camera_lock         — pin drone view to player hero
 *   - camera_height       — drone elevation for camera lock
 *
 * Pipeline:
 *   GameAutoInjectDispatcher → MlbbHeroScriptDispatcher.dispatch()
 *     → loads script from AssetManager
 *     → parses modifier table (lightweight regex/split parser — no Lua VM needed)
 *     → injects via NativeConfigInjector.injectHeroScriptModifiers()
 *
 * If heroId == 0: dispatches ALL 20 meta hero scripts sequentially (global dispatch).
 * Session drift is applied via AntiBanStealthEngine.driftValue() to prevent fingerprinting.
 */
public final class MlbbHeroScriptDispatcher {

    private static final String TAG = "MlbbHeroScriptDispatcher";

    private MlbbHeroScriptDispatcher() {}

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Dispatches the per-hero script for a specific hero ID.
     * Pass heroId = 0 to dispatch ALL top-20 meta hero scripts (global dispatch).
     *
     * @param context   Application context (can be null; uses ConfigBackupManager fallback)
     * @param pkg       MLBB package name (e.g. "com.mobile.legends")
     * @param heroId    MLBB hero ID (0 = all meta heroes)
     */
    public static void dispatch(Context context, String pkg, int heroId) {
        if (pkg == null || (!pkg.contains("mobile.legends") && !pkg.contains("mobilelegends"))) {
            Log.w(TAG, "dispatch() called on non-MLBB package: " + pkg);
            return;
        }

        if (context == null) context = ConfigBackupManager.getAppContext();

        if (heroId == 0) {
            dispatchAllHeroes(context, pkg);
        } else {
            dispatchSingle(context, pkg, heroId);
        }
    }

    /**
     * Dispatches scripts and synthetic 10,000+ damage overdrives for ALL registered heroes
     * across ALL roles (Mage, Fighter, Marksman, Assassin, Tank, Support) — all 130+ heroes.
     */
    public static void dispatchAllHeroes(Context context, String pkg) {
        Log.i(TAG, "🎯 [HeroScript] Dispatching ALL 130+ hero scripts & role overdrives for " + pkg);
        HeroEntry[] allHeroes = MlbbHeroScriptRegistry.getAllHeroes();
        int dispatched = 0;
        for (HeroEntry hero : allHeroes) {
            if (hero == null) continue;
            boolean ok;
            if (hero.hasScript) {
                ok = dispatchSingle(context, pkg, hero.id);
            } else {
                ok = dispatchSyntheticHero(pkg, hero);
            }
            if (ok) dispatched++;
        }
        Log.i(TAG, "✅ [HeroScript] Successfully dispatched " + dispatched + "/" + allHeroes.length + " heroes (All Mage, Fighter, MM, Assassin, Tank, Support)");
    }

    /**
     * Dispatches scripts for all 20 Season 42 meta heroes (backward compatibility).
     */
    public static void dispatchAllMeta(Context context, String pkg) {
        dispatchAllHeroes(context, pkg);
    }

    /**
     * Synthesizes and injects full 10,000+ damage and zero cooldown modifiers for any hero in the roster,
     * tailored by role (Mage, Fighter, Marksman, Assassin, Tank, Support).
     */
    private static boolean dispatchSyntheticHero(String pkg, HeroEntry hero) {
        try {
            Map<String, String> mods = new HashMap<>(32);
            // Universal 10,000+ base damage floor and instant cooldowns
            mods.put("s1_damage", "10000");
            mods.put("s2_damage", "10000");
            mods.put("ult_damage", "10000");
            mods.put("basic_damage", "10000");
            mods.put("s1_cd", "0.001");
            mods.put("s2_cd", "0.001");
            mods.put("ult_cd", "0.001");
            mods.put("move_speed", "500");
            mods.put("attack_speed", "10.0");
            mods.put("game_speed", "1.0");

            // Role-specific mechanics
            String role = hero.role != null ? hero.role.toLowerCase(Locale.US) : "fighter";
            if (role.contains("mage")) {
                mods.put("vision_range", "35");
                mods.put("attack_range", "9999");
                mods.put("magic_power", "10000");
                mods.put("magic_pen", "10000");
            } else if (role.contains("marksman")) {
                mods.put("attack_range", "9999");
                mods.put("crit_rate", "100");
                mods.put("crit_damage", "10.0");
                mods.put("vision_range", "35");
            } else if (role.contains("assassin")) {
                mods.put("attack_range", "2500");
                mods.put("energy_regen", "10000");
                mods.put("true_damage", "10000");
                mods.put("vision_range", "30");
            } else if (role.contains("tank") || role.contains("support")) {
                mods.put("max_hp", "10000");
                mods.put("armor", "10000");
                mods.put("magic_defense", "10000");
                mods.put("vision_range", "35");
            } else {
                // Fighter / Default
                mods.put("attack_range", "3500");
                mods.put("true_damage", "10000");
                mods.put("armor_pen", "10000");
                mods.put("vision_range", "30");
            }

            applySessionDrift(mods);
            return injectModifiers(pkg, hero, mods);
        } catch (Throwable t) {
            Log.w(TAG, "dispatchSyntheticHero error for " + hero.name + ": " + t.getMessage());
            return false;
        }
    }

    // ── Internal ───────────────────────────────────────────────────────────────

    private static boolean dispatchSingle(Context context, String pkg, int heroId) {
        HeroEntry hero = MlbbHeroScriptRegistry.getById(heroId);
        if (hero == null) {
            Log.w(TAG, "Hero ID " + heroId + " not in registry");
            return false;
        }

        String assetPath = hero.assetPath();
        Map<String, String> modifiers = loadScript(context, assetPath);
        if (modifiers == null || modifiers.isEmpty()) {
            Log.w(TAG, "Failed to load script for " + hero.name + " at " + assetPath);
            return false;
        }

        // Apply session drift to numeric values to prevent fingerprinting
        applySessionDrift(modifiers);

        // Inject into game config via NativeConfigInjector
        boolean injected = injectModifiers(pkg, hero, modifiers);
        if (injected) {
            Log.i(TAG, "✅ [HeroScript] " + hero.name + " [ID:" + hero.id + " | " + hero.role + "] — "
                    + "S1_DMG=" + modifiers.getOrDefault("s1_damage", "n/a")
                    + " S2_DMG=" + modifiers.getOrDefault("s2_damage", "n/a")
                    + " ULT_DMG=" + modifiers.getOrDefault("ult_damage", "n/a")
                    + " CD=" + modifiers.getOrDefault("s1_cd", "n/a")
                    + " SPD=" + modifiers.getOrDefault("game_speed", "n/a")
                    + " VISION=" + modifiers.getOrDefault("vision_range", "n/a"));
        }
        return injected;
    }

    /**
     * Lightweight Lua table parser — extracts script.key = value assignments.
     * No Lua VM required: parses top-level key=value lines only.
     * Handles: numbers, booleans, quoted strings.
     */
    private static Map<String, String> loadScript(Context context, String assetPath) {
        if (context == null) return null;
        Map<String, String> result = new HashMap<>(32);
        try {
            AssetManager am = context.getAssets();
            InputStream is = am.open(assetPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip comments and blanks
                if (line.isEmpty() || line.startsWith("--") || line.startsWith("return")) continue;
                // Match: script.key = value  OR  profile.key = value
                if (!line.contains("=")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length < 2) continue;
                String lhs = parts[0].trim();
                String rhs = parts[1].trim();

                // Strip trailing Lua comment
                int commentIdx = rhs.indexOf("--");
                if (commentIdx > 0) rhs = rhs.substring(0, commentIdx).trim();
                // Strip trailing comma if any
                if (rhs.endsWith(",")) rhs = rhs.substring(0, rhs.length() - 1).trim();
                // Strip outer quotes for string values
                if ((rhs.startsWith("\"") && rhs.endsWith("\""))
                        || (rhs.startsWith("'") && rhs.endsWith("'"))) {
                    rhs = rhs.substring(1, rhs.length() - 1);
                }

                // Extract key: strip "script." or "profile." prefix
                String key = lhs;
                if (key.startsWith("script.")) key = key.substring(7);
                else if (key.startsWith("profile.")) key = key.substring(8);
                else continue; // ignore other assignments

                result.put(key.toLowerCase(Locale.US), rhs);
            }
            reader.close();
        } catch (IOException e) {
            Log.w(TAG, "loadScript IO error for " + assetPath + ": " + e.getMessage());
            return null;
        } catch (Throwable t) {
            Log.w(TAG, "loadScript error for " + assetPath + ": " + t.getMessage());
            return null;
        }
        return result;
    }

    /**
     * Applies session drift (±5%) to key numeric modifiers to prevent server-side fingerprinting.
     * Delegates actual drift factor to AntiBanStealthEngine.driftValue().
     */
    private static void applySessionDrift(Map<String, String> mods) {
        String[] driftKeys = {"s1_damage","s2_damage","ult_damage","basic_damage",
                              "move_speed","vision_range","attack_range"};
        for (String key : driftKeys) {
            String val = mods.get(key);
            if (val == null) continue;
            try {
                float f = Float.parseFloat(val);
                // Only drift values above believable player stats to stay stealth
                if (f > 1000f) {
                    float drifted = AntiBanStealthEngine.driftValue(f);
                    mods.put(key, String.valueOf((int) drifted));
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    /**
     * Injects parsed hero modifiers into the MLBB game config via NativeConfigInjector.
     * Applies all per-skill damage, cooldown, range, speed, vision, game_speed, camera_lock.
     */
    private static boolean injectModifiers(String pkg, HeroEntry hero, Map<String, String> mods) {
        try {
            // Build injection key-value map using MLBB PlayerPrefs XML naming convention
            Map<String, String> inject = new HashMap<>(48);

            // ── Per-skill damage modifiers ────────────────────────────────────
            putIfPresent(inject, mods, "s1_damage",    "Hero_" + hero.id + "_S1Damage");
            putIfPresent(inject, mods, "s2_damage",    "Hero_" + hero.id + "_S2Damage");
            putIfPresent(inject, mods, "ult_damage",   "Hero_" + hero.id + "_UltDamage");
            putIfPresent(inject, mods, "basic_damage", "Hero_" + hero.id + "_BasicDamage");

            // ── Per-skill cooldown overrides ──────────────────────────────────
            putIfPresent(inject, mods, "s1_cd",  "Hero_" + hero.id + "_S1Cd");
            putIfPresent(inject, mods, "s2_cd",  "Hero_" + hero.id + "_S2Cd");
            putIfPresent(inject, mods, "ult_cd", "Hero_" + hero.id + "_UltCd");

            // ── Attack range ──────────────────────────────────────────────────
            putIfPresent(inject, mods, "attack_range", "Hero_" + hero.id + "_AttackRange");

            // ── Speed overrides ───────────────────────────────────────────────
            putIfPresent(inject, mods, "move_speed",   "Hero_" + hero.id + "_MoveSpeed");
            putIfPresent(inject, mods, "attack_speed", "Hero_" + hero.id + "_AttackSpeed");
            putIfPresent(inject, mods, "anim_speed",   "Hero_" + hero.id + "_AnimSpeed");

            // ── Vision range ──────────────────────────────────────────────────
            putIfPresent(inject, mods, "vision_range", "Hero_" + hero.id + "_VisionRange");

            // ── 2026 NEW: Game speed multiplier ──────────────────────────────
            putIfPresent(inject, mods, "game_speed", "Hero_" + hero.id + "_GameSpeed");

            // ── Camera lock ───────────────────────────────────────────────────
            String cameraLock = mods.get("camera_lock");
            if ("true".equalsIgnoreCase(cameraLock) || "1".equals(cameraLock)) {
                inject.put("Hero_" + hero.id + "_CameraLock",   "1");
                putIfPresent(inject, mods, "camera_height", "Hero_" + hero.id + "_CameraHeight");
            }

            // Delegate to NativeConfigInjector batch write
            NativeConfigInjector.injectHeroScriptModifiers(pkg, inject);
            return true;
        } catch (Throwable t) {
            Log.w(TAG, "injectModifiers error for hero " + hero.name + ": " + t.getMessage());
            return false;
        }
    }

    private static void putIfPresent(Map<String, String> target, Map<String, String> source,
                                      String srcKey, String destKey) {
        String val = source.get(srcKey);
        if (val != null && !val.isEmpty()) target.put(destKey, val);
    }
}
