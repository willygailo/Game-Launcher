package com.gamebooster.app.config;

/**
 * FpsUnlockTier — Centralized FPS tier definitions for all game config patchers.
 *
 * Dedicated strictly to Extreme Gaming tiers: 120, 144, 165, and 185 FPS.
 * (No 60fps or 90fps).
 *
 * FPS Level mapping (PUBGM/UE4 standard):
 *   7  = 120 fps
 *   8  = 144 fps
 *   9  = 165 fps
 *   10 = 185 fps
 */
public enum FpsUnlockTier {

    FPS_120(120, 7,  "120fps"),
    FPS_144(144, 8,  "144fps"),
    FPS_165(165, 9,  "165fps"),
    FPS_185(185, 10, "185fps");

    /** The raw target FPS value (e.g. 144). */
    public final int fps;

    /** The internal device/frame-rate level integer (e.g. 8 for 144fps). */
    public final int level;

    /** Human-readable label for UI display (e.g. "144fps"). */
    public final String label;

    FpsUnlockTier(int fps, int level, String label) {
        this.fps   = fps;
        this.level = level;
        this.label = label;
    }

    // ─── Lookup Helpers ──────────────────────────────────────────────────────

    /**
     * Returns the FpsUnlockTier whose FPS value is closest to (but not exceeding)
     * the requested FPS. Clamped to [120, 185].
     *
     * Examples:
     *   fromFps(185) → FPS_185
     *   fromFps(165) → FPS_165
     *   fromFps(144) → FPS_144
     *   fromFps(120) → FPS_120
     *   fromFps(60)  → FPS_120 (minimum locked to 120)
     */
    public static FpsUnlockTier fromFps(int targetFps) {
        if (targetFps >= 185) return FPS_185;
        if (targetFps >= 165) return FPS_165;
        if (targetFps >= 144) return FPS_144;
        return FPS_120;
    }

    /**
     * Returns the FpsUnlockTier that matches a given device level integer.
     * Falls back to FPS_120 for unknown or lower levels.
     */
    public static FpsUnlockTier fromLevel(int level) {
        for (FpsUnlockTier tier : values()) {
            if (tier.level == level) return tier;
        }
        return FPS_120;
    }

    /**
     * Returns all available FPS tier values as an int array.
     * Useful for populating UI spinners/selectors.
     */
    public static int[] getAllFpsValues() {
        FpsUnlockTier[] tiers = values();
        int[] result = new int[tiers.length];
        for (int i = 0; i < tiers.length; i++) {
            result[i] = tiers[i].fps;
        }
        return result;
    }

    /**
     * Returns all available FPS tier labels as a String array.
     * Useful for populating UI spinners/selectors.
     */
    public static String[] getAllLabels() {
        FpsUnlockTier[] tiers = values();
        String[] result = new String[tiers.length];
        for (int i = 0; i < tiers.length; i++) {
            result[i] = tiers[i].label;
        }
        return result;
    }

    // ─── Unlock Flag Generators ──────────────────────────────────────────────

    /**
     * Returns INI-style unlock flags for this tier and all tiers below it.
     * Example for FPS_165: "Unlock120Hz=1\nUnlock144Hz=1\nUnlock165Hz=1\n"
     */
    public String getUnlockFlags() {
        StringBuilder sb = new StringBuilder();
        if (fps >= 120) sb.append("Unlock120Hz=1\n");
        if (fps >= 144) sb.append("Unlock144Hz=1\n");
        if (fps >= 165) sb.append("Unlock165Hz=1\n");
        if (fps >= 185) sb.append("Unlock185Hz=1\n");
        return sb.toString();
    }

    /**
     * Returns UE4 CVar-style unlock flags for this tier and all tiers below it.
     * Example for FPS_144:
     *   "+CVars=r.Unlock120Hz=1\n+CVars=r.Unlock144Hz=1\n"
     */
    public String getUE4UnlockCVars() {
        StringBuilder sb = new StringBuilder();
        if (fps >= 120) sb.append("+CVars=r.Unlock120Hz=1\n");
        if (fps >= 144) sb.append("+CVars=r.Unlock144Hz=1\n");
        if (fps >= 165) sb.append("+CVars=r.Unlock165Hz=1\n");
        if (fps >= 185) sb.append("+CVars=r.Unlock185Hz=1\n");
        return sb.toString();
    }

    /**
     * Returns JSON-style unlock flags for this tier and all tiers below it.
     * Example for FPS_165:
     *   "\"Unlock120Hz\": 1,\n\"Unlock144Hz\": 1,\n\"Unlock165Hz\": 1,\n"
     */
    public String getJsonUnlockFlags() {
        StringBuilder sb = new StringBuilder();
        if (fps >= 120) sb.append("  \"Unlock120Hz\": 1,\n");
        if (fps >= 144) sb.append("  \"Unlock144Hz\": 1,\n");
        if (fps >= 165) sb.append("  \"Unlock165Hz\": 1,\n");
        if (fps >= 185) sb.append("  \"Unlock185Hz\": 1,\n");
        return sb.toString();
    }

    /**
     * Returns XML PlayerPrefs-style unlock flags for this tier and all tiers below.
     */
    public String getXmlUnlockFlags() {
        StringBuilder sb = new StringBuilder();
        if (fps >= 120) sb.append("  <int name=\"Unlock120Hz\" value=\"1\" />\n");
        if (fps >= 144) sb.append("  <int name=\"Unlock144Hz\" value=\"1\" />\n");
        if (fps >= 165) sb.append("  <int name=\"Unlock165Hz\" value=\"1\" />\n");
        if (fps >= 185) sb.append("  <int name=\"Unlock185Hz\" value=\"1\" />\n");
        return sb.toString();
    }
}
