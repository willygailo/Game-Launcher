package com.gamebooster.app.mods;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * GameModProfile — per-game mod configuration model.
 * Holds all toggle states and multiplier values for one game.
 */
public class GameModProfile {

    public enum Game { MLBB, CODM }

    // ── MLBB fields ───────────────────────────────────────────────────────────
    public boolean mlbbDamageEnabled    = false;
    public float   mlbbDamageMult       = 3.0f;
    public boolean mlbbAttackSpeedEnabled = false;
    public float   mlbbAttackSpeedMult  = 2.0f;
    public boolean mlbbDefenseEnabled   = false;
    public float   mlbbDefenseMult      = 2.0f;
    public boolean mlbbNoCooldown       = false;
    public boolean mlbbCooldownReductionEnabled = false;
    public int     mlbbCooldownReductionPct     = 80; // 0% to 100%
    public boolean mlbbManaEnabled      = false;
    public boolean mlbbInfiniteMana     = true;
    public float   mlbbManaMult         = 5.0f;
    public boolean mlbbEnergyEnabled    = false;
    public boolean mlbbInfiniteEnergy   = true;
    public float   mlbbEnergyMult       = 5.0f;
    public boolean mlbbMapHack          = false;
    public boolean mlbbNoSpread         = false;
    public boolean mlbbDroneViewEnabled = true;
    public int     mlbbDroneTier        = 20;
    public boolean mlbbFpsUnlock        = false;
    public int     mlbbTargetFps        = 165;
    public boolean mlbbAntiBan         = true;

    // ── CODM fields ───────────────────────────────────────────────────────────
    public boolean codmAimbot          = false;
    public boolean codmAllScopeLock    = true;
    public boolean codmAutoHeadshot    = false;
    public boolean codmDamageEnabled   = false;
    public float   codmDamageMult      = 2.0f;
    public boolean codmSpeedEnabled    = false;
    public float   codmSpeedMult       = 1.5f;
    public boolean codmAttackEnabled   = false;
    public float   codmAttackMult      = 2.0f;
    public boolean codmDefenseEnabled  = false;
    public float   codmDefenseMult     = 0.8f;
    public boolean codmHealthEnabled   = false;
    public float   codmHealthMult      = 3.0f;
    public boolean codmCriticalEnabled = false;
    public float   codmCriticalMult    = 3.0f;
    public boolean codmNoRecoil        = false;
    public boolean codmNoSpread        = false;
    public boolean codmFpsUnlock       = false;
    public int     codmTargetFps       = 165;
    public boolean codmAntiBan         = true;


    private static final String PREFS_NAME = "game_mod_profile";

    public static GameModProfile load(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        GameModProfile p = new GameModProfile();

        p.mlbbDamageEnabled     = sp.getBoolean("mlbb_dmg_en", false);
        p.mlbbDamageMult        = sp.getFloat("mlbb_dmg_mult", 3.0f);
        p.mlbbAttackSpeedEnabled= sp.getBoolean("mlbb_aspd_en", false);
        p.mlbbAttackSpeedMult   = sp.getFloat("mlbb_aspd_mult", 2.0f);
        p.mlbbDefenseEnabled    = sp.getBoolean("mlbb_def_en", false);
        p.mlbbDefenseMult       = sp.getFloat("mlbb_def_mult", 2.0f);
        p.mlbbNoCooldown        = sp.getBoolean("mlbb_nocool", false);
        p.mlbbCooldownReductionEnabled = sp.getBoolean("mlbb_cdr_en", false);
        p.mlbbCooldownReductionPct     = sp.getInt("mlbb_cdr_pct", 80);
        p.mlbbManaEnabled       = sp.getBoolean("mlbb_mana_en", false);
        p.mlbbInfiniteMana      = sp.getBoolean("mlbb_inf_mana", true);
        p.mlbbManaMult          = sp.getFloat("mlbb_mana_mult", 5.0f);
        p.mlbbEnergyEnabled     = sp.getBoolean("mlbb_energy_en", false);
        p.mlbbInfiniteEnergy    = sp.getBoolean("mlbb_inf_energy", true);
        p.mlbbEnergyMult        = sp.getFloat("mlbb_energy_mult", 5.0f);
        p.mlbbMapHack           = sp.getBoolean("mlbb_map", false);
        p.mlbbNoSpread          = sp.getBoolean("mlbb_spread", false);
        p.mlbbDroneViewEnabled  = sp.getBoolean("mlbb_drone_en", true);
        p.mlbbDroneTier         = sp.getInt("mlbb_drone_tier", 20);
        p.mlbbFpsUnlock         = sp.getBoolean("mlbb_fps_en", false);
        p.mlbbTargetFps         = sp.getInt("mlbb_fps", 165);
        p.mlbbAntiBan           = sp.getBoolean("mlbb_antiban", true);

        p.codmAimbot            = sp.getBoolean("codm_aim", false);
        p.codmAllScopeLock      = sp.getBoolean("codm_allscope", true);
        p.codmAutoHeadshot      = sp.getBoolean("codm_headshot", false);
        p.codmDamageEnabled     = sp.getBoolean("codm_dmg_en", false);
        p.codmDamageMult        = sp.getFloat("codm_dmg_mult", 2.0f);
        p.codmSpeedEnabled      = sp.getBoolean("codm_speed_en", false);
        p.codmSpeedMult         = sp.getFloat("codm_speed_mult", 1.5f);
        p.codmAttackEnabled     = sp.getBoolean("codm_attack_en", false);
        p.codmAttackMult        = sp.getFloat("codm_attack_mult", 2.0f);
        p.codmDefenseEnabled    = sp.getBoolean("codm_def_en", false);
        p.codmDefenseMult       = sp.getFloat("codm_def_mult", 0.8f);
        p.codmHealthEnabled     = sp.getBoolean("codm_hp_en", false);
        p.codmHealthMult        = sp.getFloat("codm_hp_mult", 3.0f);
        p.codmCriticalEnabled   = sp.getBoolean("codm_crit_en", false);
        p.codmCriticalMult      = sp.getFloat("codm_crit_mult", 3.0f);
        p.codmNoRecoil          = sp.getBoolean("codm_recoil", false);
        p.codmNoSpread          = sp.getBoolean("codm_spread", false);
        p.codmFpsUnlock         = sp.getBoolean("codm_fps_en", false);
        p.codmTargetFps         = sp.getInt("codm_fps", 165);
        p.codmAntiBan           = sp.getBoolean("codm_antiban", true);

        return p;
    }

    public void save(Context ctx) {
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean("mlbb_dmg_en",       mlbbDamageEnabled)
            .putFloat("mlbb_dmg_mult",       mlbbDamageMult)
            .putBoolean("mlbb_aspd_en",      mlbbAttackSpeedEnabled)
            .putFloat("mlbb_aspd_mult",      mlbbAttackSpeedMult)
            .putBoolean("mlbb_def_en",       mlbbDefenseEnabled)
            .putFloat("mlbb_def_mult",       mlbbDefenseMult)
            .putBoolean("mlbb_nocool",       mlbbNoCooldown)
            .putBoolean("mlbb_cdr_en",       mlbbCooldownReductionEnabled)
            .putInt("mlbb_cdr_pct",          mlbbCooldownReductionPct)
            .putBoolean("mlbb_mana_en",      mlbbManaEnabled)
            .putBoolean("mlbb_inf_mana",     mlbbInfiniteMana)
            .putFloat("mlbb_mana_mult",      mlbbManaMult)
            .putBoolean("mlbb_energy_en",    mlbbEnergyEnabled)
            .putBoolean("mlbb_inf_energy",   mlbbInfiniteEnergy)
            .putFloat("mlbb_energy_mult",    mlbbEnergyMult)
            .putBoolean("mlbb_map",          mlbbMapHack)
            .putBoolean("mlbb_spread",       mlbbNoSpread)
            .putBoolean("mlbb_drone_en",     mlbbDroneViewEnabled)
            .putInt("mlbb_drone_tier",       mlbbDroneTier)
            .putBoolean("mlbb_fps_en",       mlbbFpsUnlock)
            .putInt("mlbb_fps",              mlbbTargetFps)
            .putBoolean("mlbb_antiban",      mlbbAntiBan)
            .putBoolean("codm_aim",          codmAimbot)
            .putBoolean("codm_allscope",     codmAllScopeLock)
            .putBoolean("codm_headshot",     codmAutoHeadshot)
            .putBoolean("codm_dmg_en",       codmDamageEnabled)
            .putFloat("codm_dmg_mult",       codmDamageMult)
            .putBoolean("codm_speed_en",     codmSpeedEnabled)
            .putFloat("codm_speed_mult",     codmSpeedMult)
            .putBoolean("codm_attack_en",    codmAttackEnabled)
            .putFloat("codm_attack_mult",    codmAttackMult)
            .putBoolean("codm_def_en",       codmDefenseEnabled)
            .putFloat("codm_def_mult",       codmDefenseMult)
            .putBoolean("codm_hp_en",        codmHealthEnabled)
            .putFloat("codm_hp_mult",        codmHealthMult)
            .putBoolean("codm_crit_en",      codmCriticalEnabled)
            .putFloat("codm_crit_mult",      codmCriticalMult)
            .putBoolean("codm_recoil",       codmNoRecoil)
            .putBoolean("codm_spread",       codmNoSpread)
            .putBoolean("codm_fps_en",       codmFpsUnlock)
            .putInt("codm_fps",              codmTargetFps)
            .putBoolean("codm_antiban",      codmAntiBan)
            .apply();
    }

}
