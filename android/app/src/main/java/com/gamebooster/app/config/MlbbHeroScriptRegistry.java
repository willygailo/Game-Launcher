package com.gamebooster.app.config;

import android.util.Log;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;

/**
 * MlbbHeroScriptRegistry — Static O(1) hero script lookup registry.
 *
 * Maps MLBB hero IDs and names → per-hero Lua script asset paths under
 * assets/lua/mlbb_heroes/<heroId>_<heroName>_script.lua
 *
 * Top 20 Season 42 "Starward Decade" meta heroes have active script files.
 * All other hero IDs return the generic fallback script path.
 *
 * 2026 New Patch Method: dispatches individual hero modifier commands covering
 * cooldowns, damage, attack range, speed, vision, game speed, camera lock.
 */
public final class MlbbHeroScriptRegistry {

    private static final String TAG             = "MlbbHeroScriptRegistry";
    private static final String HERO_SCRIPTS_DIR = "lua/mlbb_heroes";
    private static final String FALLBACK_SCRIPT  = "lua/mlbb_boost.lua";

    /** Hero entry: id, name, script filename, role */
    public static final class HeroEntry {
        public final int    id;
        public final String name;
        public final String scriptFile;
        public final String role;
        public final boolean hasScript;

        HeroEntry(int id, String name, String scriptFile, String role, boolean hasScript) {
            this.id         = id;
            this.name       = name;
            this.scriptFile = scriptFile;
            this.role       = role;
            this.hasScript  = hasScript;
        }

        /** Full asset path for AssetManager.open() */
        public String assetPath() {
            return hasScript ? (HERO_SCRIPTS_DIR + "/" + scriptFile) : FALLBACK_SCRIPT;
        }
    }

    // Map for O(1) int-keyed hero ID lookup
    private static final Map<Integer, HeroEntry> sById   = new HashMap<>(130);
    // HashMap for O(1) hero name (lowercase) lookup
    private static final Map<String, HeroEntry>  sByName = new HashMap<>(130);

    static {
        // ── Season 42 Top-20 Meta Heroes (active scripts) ─────────────────────
        register(new HeroEntry(103, "Alucard",   "103_alucard_script.lua",   "Fighter",   true));
        register(new HeroEntry(107, "Chou",      "107_chou_script.lua",      "Fighter",   true));
        register(new HeroEntry(108, "Franco",    "108_franco_script.lua",    "Tank",      true));
        register(new HeroEntry(111, "Hayabusa",  "111_hayabusa_script.lua",  "Assassin",  true));
        register(new HeroEntry(114, "Fanny",     "114_fanny_script.lua",     "Assassin",  true));
        register(new HeroEntry(120, "Kagura",    "120_kagura_script.lua",    "Mage",      true));
        register(new HeroEntry(128, "Gusion",    "128_gusion_script.lua",    "Assassin",  true));
        register(new HeroEntry(130, "Lancelot",  "130_lancelot_script.lua",  "Assassin",  true));
        register(new HeroEntry(142, "Roger",     "142_roger_script.lua",     "Fighter",   true));
        register(new HeroEntry(154, "Ling",      "154_ling_script.lua",      "Assassin",  true));
        register(new HeroEntry(155, "Masha",     "155_masha_script.lua",     "Fighter",   true));
        register(new HeroEntry(160, "Brody",     "160_brody_script.lua",     "Marksman",  true));
        register(new HeroEntry(167, "Paquito",   "167_paquito_script.lua",   "Fighter",   true));
        register(new HeroEntry(175, "Beatrix",   "175_beatrix_script.lua",   "Marksman",  true));
        register(new HeroEntry(193, "Joy",       "193_joy_script.lua",       "Assassin",  true));
        register(new HeroEntry(196, "Nolan",     "196_nolan_script.lua",     "Assassin",  true));
        register(new HeroEntry(198, "Arlott",    "198_arlott_script.lua",    "Fighter",   true));
        register(new HeroEntry(199, "Zilong",    "199_zilong_script.lua",    "Fighter",   true));
        register(new HeroEntry(204, "Suyou",     "204_suyou_script.lua",     "Fighter",   true));
        register(new HeroEntry(205, "Zhuxin",    "205_zhuxin_script.lua",    "Support",   true));

        // ── Remaining heroes (fallback to global mlbb_boost.lua) ──────────────
        register(new HeroEntry(100, "Miya",      null, "Marksman", false));
        register(new HeroEntry(101, "Balmond",   null, "Fighter",  false));
        register(new HeroEntry(102, "Saber",     null, "Assassin", false));
        register(new HeroEntry(104, "Alice",     null, "Mage",     false));
        register(new HeroEntry(105, "Tigreal",   null, "Tank",     false));
        register(new HeroEntry(106, "Layla",     null, "Marksman", false));
        register(new HeroEntry(109, "Nana",      null, "Mage",     false));
        register(new HeroEntry(110, "Eudora",    null, "Mage",     false));
        register(new HeroEntry(112, "Bane",      null, "Fighter",  false));
        register(new HeroEntry(113, "Akai",      null, "Tank",     false));
        register(new HeroEntry(115, "Bruno",     null, "Marksman", false));
        register(new HeroEntry(116, "Clint",     null, "Marksman", false));
        register(new HeroEntry(117, "Rafaela",   null, "Support",  false));
        register(new HeroEntry(118, "Moskov",    null, "Marksman", false));
        register(new HeroEntry(119, "Johnson",   null, "Tank",     false));
        register(new HeroEntry(121, "Cyclops",   null, "Mage",     false));
        register(new HeroEntry(122, "Freya",     null, "Fighter",  false));
        register(new HeroEntry(123, "Gord",      null, "Mage",     false));
        register(new HeroEntry(124, "Natalia",   null, "Assassin", false));
        register(new HeroEntry(125, "Hilda",     null, "Tank",     false));
        register(new HeroEntry(126, "Aurora",    null, "Mage",     false));
        register(new HeroEntry(127, "Estes",     null, "Support",  false));
        register(new HeroEntry(129, "LapuLapu",  null, "Fighter",  false));
        register(new HeroEntry(131, "Vexana",    null, "Mage",     false));
        register(new HeroEntry(132, "Odette",    null, "Mage",     false));
        register(new HeroEntry(133, "Gatotkaca", null, "Tank",     false));
        register(new HeroEntry(134, "Harley",    null, "Mage",     false));
        register(new HeroEntry(135, "Minotaur",  null, "Tank",     false));
        register(new HeroEntry(136, "Lolita",    null, "Tank",     false));
        register(new HeroEntry(137, "Belerick",  null, "Tank",     false));
        register(new HeroEntry(138, "Irithel",   null, "Marksman", false));
        register(new HeroEntry(139, "Grock",     null, "Tank",     false));
        register(new HeroEntry(140, "Diggie",    null, "Support",  false));
        register(new HeroEntry(141, "Lylia",     null, "Mage",     false));
        register(new HeroEntry(143, "Jawhead",   null, "Fighter",  false));
        register(new HeroEntry(144, "Angela",    null, "Support",  false));
        register(new HeroEntry(146, "Vale",      null, "Mage",     false));
        register(new HeroEntry(147, "Leomord",   null, "Fighter",  false));
        register(new HeroEntry(148, "Lunox",     null, "Mage",     false));
        register(new HeroEntry(149, "Hanabi",    null, "Marksman", false));
        register(new HeroEntry(150, "Change",    null, "Mage",     false));
        register(new HeroEntry(151, "Uranus",    null, "Tank",     false));
        register(new HeroEntry(152, "Martis",    null, "Fighter",  false));
        register(new HeroEntry(153, "Hanzo",     null, "Assassin", false));
        register(new HeroEntry(156, "Kimmy",     null, "Marksman", false));
        register(new HeroEntry(157, "Thamuz",    null, "Fighter",  false));
        register(new HeroEntry(158, "Harith",    null, "Mage",     false));
        register(new HeroEntry(159, "Claude",    null, "Marksman", false));
        register(new HeroEntry(161, "Esmeralda", null, "Tank",     false));
        register(new HeroEntry(162, "Terizla",   null, "Fighter",  false));
        register(new HeroEntry(163, "XBorg",     null, "Fighter",  false));
        register(new HeroEntry(164, "Dyrroth",   null, "Fighter",  false));
        register(new HeroEntry(166, "Baxia",     null, "Tank",     false));
        register(new HeroEntry(168, "Wanwan",    null, "Marksman", false));
        register(new HeroEntry(169, "Silvanna",  null, "Fighter",  false));
        register(new HeroEntry(170, "Cecilion",  null, "Mage",     false));
        register(new HeroEntry(171, "Carmilla",  null, "Support",  false));
        register(new HeroEntry(172, "Atlas",     null, "Tank",     false));
        register(new HeroEntry(173, "Popol",     null, "Marksman", false));
        register(new HeroEntry(174, "YuZhong",   null, "Fighter",  false));
        register(new HeroEntry(176, "Khaleed",   null, "Fighter",  false));
        register(new HeroEntry(177, "Barats",    null, "Tank",     false));
        register(new HeroEntry(178, "Yve",       null, "Mage",     false));
        register(new HeroEntry(179, "Mathilda",  null, "Support",  false));
        register(new HeroEntry(180, "Phoveus",   null, "Fighter",  false));
        register(new HeroEntry(181, "Aulus",     null, "Fighter",  false));
        register(new HeroEntry(182, "Floryn",    null, "Support",  false));
        register(new HeroEntry(183, "Natan",     null, "Marksman", false));
        register(new HeroEntry(184, "Aamon",     null, "Assassin", false));
        register(new HeroEntry(185, "Valentina", null, "Mage",     false));
        register(new HeroEntry(186, "Edith",     null, "Tank",     false));
        register(new HeroEntry(187, "Yin",       null, "Fighter",  false));
        register(new HeroEntry(188, "Julian",    null, "Fighter",  false));
        register(new HeroEntry(189, "Xavier",    null, "Mage",     false));
        register(new HeroEntry(190, "Melissa",   null, "Marksman", false));
        register(new HeroEntry(191, "Fredrinn",  null, "Tank",     false));
        register(new HeroEntry(192, "Ixia",      null, "Marksman", false));
        register(new HeroEntry(194, "Novaria",   null, "Mage",     false));
        register(new HeroEntry(195, "Udal",      null, "Tank",     false));
        register(new HeroEntry(197, "Chip",      null, "Support",  false));
        register(new HeroEntry(200, "Lukas",     null, "Fighter",  false));
        register(new HeroEntry(201, "Cici",      null, "Fighter",  false));
        register(new HeroEntry(202, "Kalea",     null, "Tank",     false));
        register(new HeroEntry(203, "Taara",     null, "Fighter",  false));
    }

    private static void register(HeroEntry e) {
        sById.put(e.id, e);
        sByName.put(e.name.toLowerCase(), e);
    }

    private MlbbHeroScriptRegistry() {}

    /** Lookup by hero ID. Returns null if unknown. */
    public static HeroEntry getById(int heroId) { return sById.get(heroId); }

    /** Lookup by hero name (case-insensitive). Returns null if unknown. */
    public static HeroEntry getByName(String heroName) {
        if (heroName == null) return null;
        return sByName.get(heroName.trim().toLowerCase());
    }

    /**
     * Returns the asset path for the hero script.
     * @param heroId 0 = All Heroes global dispatch → returns global fallback
     */
    public static String resolveScriptPath(int heroId) {
        if (heroId <= 0) return FALLBACK_SCRIPT;
        HeroEntry e = sById.get(heroId);
        if (e == null) {
            Log.w(TAG, "Hero ID " + heroId + " not registered — using global fallback");
            return FALLBACK_SCRIPT;
        }
        return e.assetPath();
    }

    /** Returns all top-20 Season 42 meta hero entries with active scripts. */
    public static HeroEntry[] getMetaHeroes() {
        int[] metaIds = {103,107,108,111,114,120,128,130,142,154,
                         155,160,167,175,193,196,198,199,204,205};
        HeroEntry[] result = new HeroEntry[metaIds.length];
        for (int i = 0; i < metaIds.length; i++) result[i] = sById.get(metaIds[i]);
        return result;
    }

    public static String getFallbackScript()  { return FALLBACK_SCRIPT; }
    public static String getHeroScriptsDir()  { return HERO_SCRIPTS_DIR; }

    /** Returns count of registered heroes */
    public static int totalRegistered() { return sByName.size(); }
}
