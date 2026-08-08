package com.gamebooster.app.config;

/**
 * CompetitiveCfgProfile — Data model for a saved competitive configuration profile.
 *
 * Stores per-game settings for FPS target, super-fast touch, and Hz force-write.
 * Each profile is identified by a gameKey (MLBB / PUBGM / CODM / ALL).
 * Profiles are persisted to SharedPreferences via CfgProfileManager.
 */
public class CompetitiveCfgProfile {

    // ─── Game Key Constants ─────────────────────────────────────────────────
    public static final String GAME_MLBB  = "MLBB";
    public static final String GAME_PUBGM = "PUBGM";
    public static final String GAME_CODM  = "CODM";
    public static final String GAME_ALL   = "ALL";

    // ─── FPS Tier Constants ──────────────────────────────────────────────────
    public static final int FPS_60  = 60;
    public static final int FPS_90  = 90;
    public static final int FPS_120 = 120;
    public static final int FPS_144 = 144;
    public static final int FPS_165 = 165;

    // ─── Fields ──────────────────────────────────────────────────────────────
    private final String gameKey;
    private int targetFps;
    private boolean superFastTouchEnabled;
    private boolean forceWriteSystemHz;
    private boolean aimAssistEnabled;
    private boolean mlbbDamageScriptEnabled;
    private boolean recoilControlEnabled;

    // ─── Constructors ────────────────────────────────────────────────────────
    public CompetitiveCfgProfile(String gameKey, int targetFps,
                                 boolean superFastTouchEnabled,
                                 boolean forceWriteSystemHz) {
        this(gameKey, targetFps, superFastTouchEnabled, forceWriteSystemHz, true, true, true);
    }

    public CompetitiveCfgProfile(String gameKey, int targetFps,
                                 boolean superFastTouchEnabled,
                                 boolean forceWriteSystemHz,
                                 boolean aimAssistEnabled,
                                 boolean mlbbDamageScriptEnabled,
                                 boolean recoilControlEnabled) {
        this.gameKey               = gameKey;
        this.targetFps             = targetFps;
        this.superFastTouchEnabled = superFastTouchEnabled;
        this.forceWriteSystemHz    = forceWriteSystemHz;
        this.aimAssistEnabled      = aimAssistEnabled;
        this.mlbbDamageScriptEnabled = mlbbDamageScriptEnabled;
        this.recoilControlEnabled  = recoilControlEnabled;
    }

    /** Returns default competitive profile for the given game key (165fps, all enabled). */
    public static CompetitiveCfgProfile defaultCompetitive(String gameKey) {
        return new CompetitiveCfgProfile(gameKey, FPS_165, true, true, true, true, true);
    }

    // ─── Getters / Setters ───────────────────────────────────────────────────
    public String getGameKey() { return gameKey; }

    public int getTargetFps() { return targetFps; }
    public void setTargetFps(int targetFps) { this.targetFps = targetFps; }

    public boolean isSuperFastTouchEnabled() { return superFastTouchEnabled; }
    public void setSuperFastTouchEnabled(boolean enabled) { this.superFastTouchEnabled = enabled; }

    public boolean isForceWriteSystemHz() { return forceWriteSystemHz; }
    public void setForceWriteSystemHz(boolean enabled) { this.forceWriteSystemHz = enabled; }

    public boolean isAimAssistEnabled() { return aimAssistEnabled; }
    public void setAimAssistEnabled(boolean enabled) { this.aimAssistEnabled = enabled; }

    public boolean isMlbbDamageScriptEnabled() { return mlbbDamageScriptEnabled; }
    public void setMlbbDamageScriptEnabled(boolean enabled) { this.mlbbDamageScriptEnabled = enabled; }

    public boolean isRecoilControlEnabled() { return recoilControlEnabled; }
    public void setRecoilControlEnabled(boolean enabled) { this.recoilControlEnabled = enabled; }

    // ─── SharedPrefs Key Builder ─────────────────────────────────────────────
    /** Returns the SharedPreferences key prefix for this profile. */
    public String getPrefsKey() {
        return "cfg_profile_" + gameKey.toLowerCase();
    }

    @Override
    public String toString() {
        return "CompetitiveCfgProfile{" +
                "game='" + gameKey + '\'' +
                ", fps=" + targetFps +
                ", superTouch=" + superFastTouchEnabled +
                ", forceHz=" + forceWriteSystemHz +
                ", aimAssist=" + aimAssistEnabled +
                ", damageScript=" + mlbbDamageScriptEnabled +
                '}';
    }
}
