package com.gamebooster.app.config;

/**
 * FpsUnlockTier — Centralized FPS tier definitions for all game config patchers.
 *
 * Hard-locked to 185 FPS only. All lookup helpers unconditionally return FPS_185.
 *
 * FPS Level mapping (PUBGM/UE4 standard):
 *   10 = 185 fps (only supported tier)
 */
public enum FpsUnlockTier {

    FPS_185(185, 10, "185fps");

    /** The raw target FPS value (185). */
    public final int fps;

    /** The internal device/frame-rate level integer (10 for 185fps). */
    public final int level;

    /** Human-readable label for UI display ("185fps"). */
    public final String label;

    FpsUnlockTier(int fps, int level, String label) {
        this.fps   = fps;
        this.level = level;
        this.label = label;
    }

    // ─── Lookup Helpers ──────────────────────────────────────────────────────

    /**
     * Always returns FPS_185 regardless of input.
     * All tiers are locked to 185 FPS.
     */
    public static FpsUnlockTier fromFps(int targetFps) {
        return FPS_185;
    }

    /**
     * Always returns FPS_185 regardless of level input.
     */
    public static FpsUnlockTier fromLevel(int level) {
        return FPS_185;
    }

    /**
     * Returns all available FPS tier values as an int array.
     * Always returns {185}.
     */
    public static int[] getAllFpsValues() {
        return new int[]{185};
    }

    /**
     * Returns all available FPS tier labels as a String array.
     * Always returns {"185fps"}.
     */
    public static String[] getAllLabels() {
        return new String[]{"185fps"};
    }

    // ─── Unlock Flag Generators ──────────────────────────────────────────────

    /**
     * Returns INI-style unlock flags for all Hz tiers.
     * All 4 capability flags are always emitted to enable the full 185Hz pipeline.
     */
    public String getUnlockFlags() {
        return "Unlock120Hz=1\n" +
               "Unlock144Hz=1\n" +
               "Unlock165Hz=1\n" +
               "Unlock185Hz=1\n";
    }

    /**
     * Returns UE4 CVar-style unlock flags for all Hz tiers.
     * All 4 capability CVars are always emitted.
     */
    public String getUE4UnlockCVars() {
        return "+CVars=r.Unlock120Hz=1\n" +
               "+CVars=r.Unlock144Hz=1\n" +
               "+CVars=r.Unlock165Hz=1\n" +
               "+CVars=r.Unlock185Hz=1\n";
    }

    /**
     * Returns JSON-style unlock flags for all Hz tiers.
     * All 4 capability flags are always emitted.
     */
    public String getJsonUnlockFlags() {
        return "  \"Unlock120Hz\": 1,\n" +
               "  \"Unlock144Hz\": 1,\n" +
               "  \"Unlock165Hz\": 1,\n" +
               "  \"Unlock185Hz\": 1,\n";
    }

    /**
     * Returns XML PlayerPrefs-style unlock flags for all Hz tiers.
     * All 4 capability flags are always emitted.
     */
    public String getXmlUnlockFlags() {
        return "  <int name=\"Unlock120Hz\" value=\"1\" />\n" +
               "  <int name=\"Unlock144Hz\" value=\"1\" />\n" +
               "  <int name=\"Unlock165Hz\" value=\"1\" />\n" +
               "  <int name=\"Unlock185Hz\" value=\"1\" />\n";
    }
}
