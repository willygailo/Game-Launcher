package com.gamebooster.app.config;

/**
 * CompetitiveCfgProfile — Data model for a saved competitive configuration profile.
 *
 * Stores per-game settings for FPS target (144/165/185Hz), super-fast touch (185Hz/1000Hz polling),
 * Hz force-write, 1000% Damage script (all heroes/modes), Zero Recoil all guns/all scopes,
 * High Aim Assist, Tracking Bullet engine, Gyro Super Smooth tuning, and hardware masking.
 * Profiles are persisted to SharedPreferences via CfgProfileManager.
 *
 * FPS targets are aligned to the supported tier set (90/120/144/165/185).
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

    // ─── FPS Tier Constants ─────────────────────────────────────────────────
    public static final int FPS_185 = 185;
    public static final int FPS_165 = 165;
    public static final int FPS_144 = 144;
    public static final int FPS_120 = 120;
    public static final int FPS_90  =  90;

    // ─── Fields ──────────────────────────────────────────────────────────────
    private final String gameKey;
    private int targetFps;
    private boolean superFastTouchEnabled;
    private boolean forceWriteSystemHz;
    private boolean aimAssistEnabled;
    private boolean mlbbDamageScriptEnabled;
    private boolean recoilControlEnabled;
    private boolean trackingBulletEnabled;
    private boolean gyroTuningEnabled;
    private boolean touchNoDelayEnabled;
    private boolean armorDefEnabled;
    private boolean fastCooldownEnabled;
    private boolean shield1500Enabled;
    private boolean droneViewUltraEnabled;
    private boolean hardwareMaskEnabled;
    private boolean antiLogEnabled;
    private boolean focusFreezeEnabled = true;

    // ─── Constructors ────────────────────────────────────────────────────────
    public CompetitiveCfgProfile(String gameKey, int targetFps,
                                 boolean superFastTouchEnabled,
                                 boolean forceWriteSystemHz) {
        this(gameKey, targetFps, superFastTouchEnabled, forceWriteSystemHz, true, true, true, true, true, true, true, true, true, true, true, true);
    }

    public CompetitiveCfgProfile(String gameKey, int targetFps,
                                 boolean superFastTouchEnabled,
                                 boolean forceWriteSystemHz,
                                 boolean aimAssistEnabled,
                                 boolean mlbbDamageScriptEnabled,
                                 boolean recoilControlEnabled) {
        this(gameKey, targetFps, superFastTouchEnabled, forceWriteSystemHz, aimAssistEnabled, mlbbDamageScriptEnabled, recoilControlEnabled, true, true, true, true, true, true, true, true, true);
    }

    public CompetitiveCfgProfile(String gameKey, int targetFps,
                                 boolean superFastTouchEnabled,
                                 boolean forceWriteSystemHz,
                                 boolean aimAssistEnabled,
                                 boolean mlbbDamageScriptEnabled,
                                 boolean recoilControlEnabled,
                                 boolean gyroTuningEnabled,
                                 boolean touchNoDelayEnabled) {
        this(gameKey, targetFps, superFastTouchEnabled, forceWriteSystemHz, aimAssistEnabled, mlbbDamageScriptEnabled, recoilControlEnabled, true, gyroTuningEnabled, touchNoDelayEnabled, true, true, true, true, true, true);
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
        this(gameKey, targetFps, superFastTouchEnabled, forceWriteSystemHz, aimAssistEnabled, mlbbDamageScriptEnabled, recoilControlEnabled, true, gyroTuningEnabled, touchNoDelayEnabled, true, true, true, true, hardwareMaskEnabled, true);
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
        this(gameKey, targetFps, superFastTouchEnabled, forceWriteSystemHz, aimAssistEnabled, mlbbDamageScriptEnabled, recoilControlEnabled, true, gyroTuningEnabled, touchNoDelayEnabled, true, true, true, true, hardwareMaskEnabled, antiLogEnabled);
    }

    public CompetitiveCfgProfile(String gameKey, int targetFps,
                                 boolean superFastTouchEnabled,
                                 boolean forceWriteSystemHz,
                                 boolean aimAssistEnabled,
                                 boolean mlbbDamageScriptEnabled,
                                 boolean recoilControlEnabled,
                                 boolean gyroTuningEnabled,
                                 boolean touchNoDelayEnabled,
                                 boolean armorDefEnabled,
                                 boolean hardwareMaskEnabled,
                                 boolean antiLogEnabled) {
        this(gameKey, targetFps, superFastTouchEnabled, forceWriteSystemHz, aimAssistEnabled, mlbbDamageScriptEnabled, recoilControlEnabled, true, gyroTuningEnabled, touchNoDelayEnabled, armorDefEnabled, true, true, true, hardwareMaskEnabled, antiLogEnabled);
    }

    public CompetitiveCfgProfile(String gameKey, int targetFps,
                                 boolean superFastTouchEnabled,
                                 boolean forceWriteSystemHz,
                                 boolean aimAssistEnabled,
                                 boolean mlbbDamageScriptEnabled,
                                 boolean recoilControlEnabled,
                                 boolean trackingBulletEnabled,
                                 boolean gyroTuningEnabled,
                                 boolean touchNoDelayEnabled,
                                 boolean armorDefEnabled,
                                 boolean hardwareMaskEnabled,
                                 boolean antiLogEnabled) {
        this(gameKey, targetFps, superFastTouchEnabled, forceWriteSystemHz, aimAssistEnabled, mlbbDamageScriptEnabled, recoilControlEnabled, trackingBulletEnabled, gyroTuningEnabled, touchNoDelayEnabled, armorDefEnabled, true, true, true, hardwareMaskEnabled, antiLogEnabled);
    }

    public CompetitiveCfgProfile(String gameKey, int targetFps,
                                 boolean superFastTouchEnabled,
                                 boolean forceWriteSystemHz,
                                 boolean aimAssistEnabled,
                                 boolean mlbbDamageScriptEnabled,
                                 boolean recoilControlEnabled,
                                 boolean trackingBulletEnabled,
                                 boolean fastCooldownEnabled,
                                 boolean shield1500Enabled,
                                 boolean droneViewUltraEnabled,
                                 boolean armorDefEnabled,
                                 boolean hardwareMaskEnabled,
                                 boolean antiLogEnabled) {
        this(gameKey, targetFps, superFastTouchEnabled, forceWriteSystemHz, aimAssistEnabled, mlbbDamageScriptEnabled, recoilControlEnabled, trackingBulletEnabled, true, true, armorDefEnabled, fastCooldownEnabled, shield1500Enabled, droneViewUltraEnabled, hardwareMaskEnabled, antiLogEnabled);
    }

    public CompetitiveCfgProfile(String gameKey, int targetFps,
                                 boolean superFastTouchEnabled,
                                 boolean forceWriteSystemHz,
                                 boolean aimAssistEnabled,
                                 boolean mlbbDamageScriptEnabled,
                                 boolean recoilControlEnabled,
                                 boolean trackingBulletEnabled,
                                 boolean gyroTuningEnabled,
                                 boolean touchNoDelayEnabled,
                                 boolean armorDefEnabled,
                                 boolean fastCooldownEnabled,
                                 boolean shield1500Enabled,
                                 boolean droneViewUltraEnabled,
                                 boolean hardwareMaskEnabled,
                                 boolean antiLogEnabled) {
        this.gameKey                 = gameKey != null ? gameKey : GAME_ALL;
        this.targetFps               = FpsUnlockTier.resolveTargetFps(targetFps);
        this.superFastTouchEnabled   = superFastTouchEnabled;
        this.forceWriteSystemHz      = forceWriteSystemHz;
        this.aimAssistEnabled        = aimAssistEnabled;
        this.mlbbDamageScriptEnabled = mlbbDamageScriptEnabled;
        this.recoilControlEnabled    = recoilControlEnabled;
        this.trackingBulletEnabled   = trackingBulletEnabled;
        this.gyroTuningEnabled       = gyroTuningEnabled;
        this.touchNoDelayEnabled     = touchNoDelayEnabled;
        this.armorDefEnabled         = armorDefEnabled;
        this.fastCooldownEnabled     = fastCooldownEnabled;
        this.shield1500Enabled       = shield1500Enabled;
        this.droneViewUltraEnabled   = droneViewUltraEnabled;
        this.hardwareMaskEnabled     = hardwareMaskEnabled;
        this.antiLogEnabled          = antiLogEnabled;
    }

    /** Returns default competitive profile for the given game key (185fps extreme, all features enabled). */
    public static CompetitiveCfgProfile defaultCompetitive(String gameKey) {
        return new CompetitiveCfgProfile(gameKey, FPS_185, true, true, true, true, true, true, true, true, true, true, true, true, true, true);
    }

    // ─── UltraExtreme 144fps SuperSmooth Factory Methods ─────────────────────

    /**
     * Returns a 144fps SuperSmooth + UltraExtreme profile with ALL features enabled.
     * Use when the user selects the "144fps SuperSmooth UltraExtreme" preset.
     *
     * @param gameKey one of the GAME_* constants (e.g. {@link #GAME_PUBGM})
     */
    public static CompetitiveCfgProfile ultraExtreme144(String gameKey) {
        return new CompetitiveCfgProfile(
            gameKey,
            FPS_144,
            /* superFastTouchEnabled  */ true,
            /* forceWriteSystemHz     */ true,
            /* aimAssistEnabled       */ true,
            /* mlbbDamageScriptEnabled*/ true,
            /* recoilControlEnabled   */ true,
            /* trackingBulletEnabled  */ true,
            /* gyroTuningEnabled      */ true,
            /* touchNoDelayEnabled    */ true,
            /* armorDefEnabled        */ true,
            /* fastCooldownEnabled    */ true,
            /* shield1500Enabled      */ true,
            /* droneViewUltraEnabled  */ true,
            /* hardwareMaskEnabled    */ true,
            /* antiLogEnabled         */ true
        );
    }

    /**
     * Returns a 144fps profile focused only on max graphics + FPS unlock.
     * Combat-enhancing features (aim assist, damage, tracking bullet, armor) are disabled.
     * Intended for users who want visuals and framerate without gameplay modifications.
     *
     * @param gameKey one of the GAME_* constants
     */
    public static CompetitiveCfgProfile graphicsMaxOnly144(String gameKey) {
        return new CompetitiveCfgProfile(
            gameKey,
            FPS_144,
            /* superFastTouchEnabled  */ true,
            /* forceWriteSystemHz     */ true,
            /* aimAssistEnabled       */ false,
            /* mlbbDamageScriptEnabled*/ false,
            /* recoilControlEnabled   */ false,
            /* trackingBulletEnabled  */ false,
            /* gyroTuningEnabled      */ true,
            /* touchNoDelayEnabled    */ true,
            /* armorDefEnabled        */ false,
            /* fastCooldownEnabled    */ false,
            /* shield1500Enabled      */ false,
            /* droneViewUltraEnabled  */ false,
            /* hardwareMaskEnabled    */ false,
            /* antiLogEnabled         */ true
        );
    }

    /**
     * Returns a 144fps SuperSmooth profile optimised for gyro + touch response.
     * Max graphics enabled; combat mods disabled. Ideal for casual / ranked play
     * where only framerate and input latency improvements are wanted.
     *
     * @param gameKey one of the GAME_* constants
     */
    public static CompetitiveCfgProfile superSmooth144(String gameKey) {
        return new CompetitiveCfgProfile(
            gameKey,
            FPS_144,
            /* superFastTouchEnabled  */ true,
            /* forceWriteSystemHz     */ true,
            /* aimAssistEnabled       */ false,
            /* mlbbDamageScriptEnabled*/ false,
            /* recoilControlEnabled   */ false,
            /* trackingBulletEnabled  */ false,
            /* gyroTuningEnabled      */ true,
            /* touchNoDelayEnabled    */ true,
            /* armorDefEnabled        */ false,
            /* fastCooldownEnabled    */ false,
            /* shield1500Enabled      */ false,
            /* droneViewUltraEnabled  */ false,
            /* hardwareMaskEnabled    */ false,
            /* antiLogEnabled         */ true
        );
    }

    // ─── Getters / Setters ───────────────────────────────────────────────────
    public String getGameKey() { return gameKey; }

    /** Returns the profile's target FPS, aligned to a supported tier. */
    public int getTargetFps() { return targetFps; }

    /** Sets the target FPS, aligned to the nearest supported tier. */
    public void setTargetFps(int targetFps) {
        this.targetFps = FpsUnlockTier.resolveTargetFps(targetFps);
    }

    public boolean isSuperFastTouchEnabled() { return superFastTouchEnabled; }
    public void setSuperFastTouchEnabled(boolean enabled) { this.superFastTouchEnabled = enabled; }

    public boolean isForceWriteSystemHz() { return forceWriteSystemHz; }
    public void setForceWriteSystemHz(boolean enabled) { this.forceWriteSystemHz = enabled; }

    public boolean isAimAssistEnabled() { return aimAssistEnabled; }
    public void setAimAssistEnabled(boolean enabled) { this.aimAssistEnabled = enabled; }

    public boolean isMlbbDamageScriptEnabled() { return mlbbDamageScriptEnabled; }
    public void setMlbbDamageScriptEnabled(boolean enabled) { this.mlbbDamageScriptEnabled = enabled; }
    public boolean isDamageBoostEnabled() { return mlbbDamageScriptEnabled; }
    public void setDamageBoostEnabled(boolean enabled) { this.mlbbDamageScriptEnabled = enabled; }

    public boolean isRecoilControlEnabled() { return recoilControlEnabled; }
    public void setRecoilControlEnabled(boolean enabled) { this.recoilControlEnabled = enabled; }

    public boolean isTrackingBulletEnabled() { return trackingBulletEnabled; }
    public void setTrackingBulletEnabled(boolean enabled) { this.trackingBulletEnabled = enabled; }

    public boolean isGyroTuningEnabled() { return gyroTuningEnabled; }
    public void setGyroTuningEnabled(boolean enabled) { this.gyroTuningEnabled = enabled; }

    public boolean isTouchNoDelayEnabled() { return touchNoDelayEnabled; }
    public void setTouchNoDelayEnabled(boolean enabled) { this.touchNoDelayEnabled = enabled; }

    public boolean isArmorDefEnabled() { return armorDefEnabled; }
    public void setArmorDefEnabled(boolean enabled) { this.armorDefEnabled = enabled; }

    public boolean isFastCooldownEnabled() { return fastCooldownEnabled; }
    public void setFastCooldownEnabled(boolean enabled) { this.fastCooldownEnabled = enabled; }

    public boolean isShield1500Enabled() { return shield1500Enabled; }
    public void setShield1500Enabled(boolean enabled) { this.shield1500Enabled = enabled; }

    public boolean isDroneViewUltraEnabled() { return droneViewUltraEnabled; }
    public void setDroneViewUltraEnabled(boolean enabled) { this.droneViewUltraEnabled = enabled; }

    public boolean isHardwareMaskEnabled() { return hardwareMaskEnabled; }
    public void setHardwareMaskEnabled(boolean enabled) { this.hardwareMaskEnabled = enabled; }

    public boolean isAntiLogEnabled() { return antiLogEnabled; }
    public void setAntiLogEnabled(boolean enabled) { this.antiLogEnabled = enabled; }

    public boolean isFocusFreezeEnabled() { return focusFreezeEnabled; }
    public void setFocusFreezeEnabled(boolean enabled) { this.focusFreezeEnabled = enabled; }

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
                ", trackingBullet=" + trackingBulletEnabled +
                ", armorDef=" + armorDefEnabled +
                ", fastCooldown=" + fastCooldownEnabled +
                ", shield1500=" + shield1500Enabled +
                ", droneViewUltra=" + droneViewUltraEnabled +
                ", hardwareMask=" + hardwareMaskEnabled +
                ", antiLog=" + antiLogEnabled +
                '}';
    }
}
