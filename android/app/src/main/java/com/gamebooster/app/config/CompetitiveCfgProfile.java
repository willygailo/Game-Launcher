package com.gamebooster.app.config;

/**
 * CompetitiveCfgProfile — Data model for a saved competitive configuration profile.
 *
 * Stores per-game settings for FPS target, super-fast touch, Hz force-write,
 * aim assist, damage script, recoil control, gyro tuning, and hardware masking.
 * Profiles are persisted to SharedPreferences via CfgProfileManager.
 */
public class CompetitiveCfgProfile {

    // ─── Game Key Constants ─────────────────────────────────────────────────
    public static final String GAME_MLBB          = "MLBB";
    public static final String GAME_PUBGM         = "PUBGM";
    public static final String GAME_CODM          = "CODM";
    public static final String GAME_FREEFIRE      = "FREEFIRE";
    public static final String GAME_GENSHIN       = "GENSHIN";
    public static final String GAME_HOK           = "HOK";
    public static final String GAME_ROBLOX        = "ROBLOX";
    public static final String GAME_VALORANT      = "VALORANT";
    public static final String GAME_FARLIGHT      = "FARLIGHT";
    public static final String GAME_BLOODSTRIKE   = "BLOODSTRIKE";
    public static final String GAME_STANDOFF2     = "STANDOFF2";
    public static final String GAME_WILDRIFT      = "WILDRIFT";
    public static final String GAME_CARX          = "CARX";
    public static final String GAME_ARENABREAKOUT = "ARENABREAKOUT";
    public static final String GAME_SUPERCELL     = "SUPERCELL";
    public static final String GAME_ALL           = "ALL";

    // ─── FPS Tier Constants (120, 144, 165, 185 only) ──────────────────────
    public static final int FPS_120 = 120;
    public static final int FPS_144 = 144;
    public static final int FPS_165 = 165;
    public static final int FPS_185 = 185;

    // ─── Fields ──────────────────────────────────────────────────────────────
    private final String gameKey;
    private int targetFps;
    private boolean superFastTouchEnabled;
    private boolean forceWriteSystemHz;
    private boolean aimAssistEnabled;
    private boolean mlbbDamageScriptEnabled;
    private boolean recoilControlEnabled;
    private boolean gyroTuningEnabled;
    private boolean touchNoDelayEnabled;
    private boolean hardwareMaskEnabled;
    private boolean antiLogEnabled;

    // ─── Constructors ────────────────────────────────────────────────────────
    public CompetitiveCfgProfile(String gameKey, int targetFps,
                                 boolean superFastTouchEnabled,
                                 boolean forceWriteSystemHz) {
        this(gameKey, targetFps, superFastTouchEnabled, forceWriteSystemHz, true, true, true, true, true, true, true);
    }

    public CompetitiveCfgProfile(String gameKey, int targetFps,
                                 boolean superFastTouchEnabled,
                                 boolean forceWriteSystemHz,
                                 boolean aimAssistEnabled,
                                 boolean mlbbDamageScriptEnabled,
                                 boolean recoilControlEnabled) {
        this(gameKey, targetFps, superFastTouchEnabled, forceWriteSystemHz, aimAssistEnabled, mlbbDamageScriptEnabled, recoilControlEnabled, true, true, true, true);
    }

    public CompetitiveCfgProfile(String gameKey, int targetFps,
                                 boolean superFastTouchEnabled,
                                 boolean forceWriteSystemHz,
                                 boolean aimAssistEnabled,
                                 boolean mlbbDamageScriptEnabled,
                                 boolean recoilControlEnabled,
                                 boolean gyroTuningEnabled,
                                 boolean touchNoDelayEnabled) {
        this(gameKey, targetFps, superFastTouchEnabled, forceWriteSystemHz, aimAssistEnabled, mlbbDamageScriptEnabled, recoilControlEnabled, gyroTuningEnabled, touchNoDelayEnabled, true, true);
    }

    public CompetitiveCfgProfile(String gameKey, int targetFps,
                                 boolean superFastTouchEnabled,
                                 boolean forceWriteSystemHz,
                                 boolean aimAssistEnabled,
                                 boolean mlbbDamageScriptEnabled,
                                 boolean recoilControlEnabled,
                                 boolean gyroTuningEnabled,
                                 boolean touchNoDelayEnabled,
                                 boolean hardwareMaskEnabled) {
        this(gameKey, targetFps, superFastTouchEnabled, forceWriteSystemHz, aimAssistEnabled, mlbbDamageScriptEnabled, recoilControlEnabled, gyroTuningEnabled, touchNoDelayEnabled, hardwareMaskEnabled, true);
    }

    public CompetitiveCfgProfile(String gameKey, int targetFps,
                                 boolean superFastTouchEnabled,
                                 boolean forceWriteSystemHz,
                                 boolean aimAssistEnabled,
                                 boolean mlbbDamageScriptEnabled,
                                 boolean recoilControlEnabled,
                                 boolean gyroTuningEnabled,
                                 boolean touchNoDelayEnabled,
                                 boolean hardwareMaskEnabled,
                                 boolean antiLogEnabled) {
        this.gameKey                 = gameKey != null ? gameKey : GAME_ALL;
        this.targetFps               = targetFps > 0 ? targetFps : FPS_185;
        this.superFastTouchEnabled   = superFastTouchEnabled;
        this.forceWriteSystemHz      = forceWriteSystemHz;
        this.aimAssistEnabled        = aimAssistEnabled;
        this.mlbbDamageScriptEnabled = mlbbDamageScriptEnabled;
        this.recoilControlEnabled    = recoilControlEnabled;
        this.gyroTuningEnabled       = gyroTuningEnabled;
        this.touchNoDelayEnabled     = touchNoDelayEnabled;
        this.hardwareMaskEnabled     = hardwareMaskEnabled;
        this.antiLogEnabled          = antiLogEnabled;
    }

    /** Returns default competitive profile for the given game key (185fps extreme, all features enabled). */
    public static CompetitiveCfgProfile defaultCompetitive(String gameKey) {
        return new CompetitiveCfgProfile(gameKey, FPS_185, true, true, true, true, true, true, true, true, true);
    }

    // ─── Getters / Setters ───────────────────────────────────────────────────
    public String getGameKey() { return gameKey; }

    public int getTargetFps() { return targetFps > 0 ? targetFps : FPS_185; }
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

    public boolean isGyroTuningEnabled() { return gyroTuningEnabled; }
    public void setGyroTuningEnabled(boolean enabled) { this.gyroTuningEnabled = enabled; }

    public boolean isTouchNoDelayEnabled() { return touchNoDelayEnabled; }
    public void setTouchNoDelayEnabled(boolean enabled) { this.touchNoDelayEnabled = enabled; }

    public boolean isHardwareMaskEnabled() { return hardwareMaskEnabled; }
    public void setHardwareMaskEnabled(boolean enabled) { this.hardwareMaskEnabled = enabled; }

    public boolean isAntiLogEnabled() { return antiLogEnabled; }
    public void setAntiLogEnabled(boolean enabled) { this.antiLogEnabled = enabled; }

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
                ", recoilControl=" + recoilControlEnabled +
                ", hardwareMask=" + hardwareMaskEnabled +
                ", antiLog=" + antiLogEnabled +
                '}';
    }
}
