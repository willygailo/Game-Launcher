-- =============================================================================
-- MOBILE LEGENDS: BANG BANG (MLBB) GLOBAL OPTIMIZATION PROFILE (2026 Engine)
-- Fast Farming + Fast Retri + All Hero Master Suite + Micro Overdrives
-- =============================================================================

profile = {}

profile.game_id = "mlbb"
profile.package_name = "com.mobile.legends"
profile.target_fps = 165
profile.graphics_tier = "ULTRA"
profile.force_vulkan = false
profile.touch_boost_hz = 1000
profile.cpu_governor = "performance"

-- PlayerPrefs Flags & Graphics Overdrive
profile.high_fps_mode = 3
profile.resolution_high = 1
profile.outline = 1
profile.shadow = 1
profile.zero_corruption = true
profile.hd_mode = 1
profile.render_scale = 120
profile.damage_text = 1
profile.creep_hp = 1

-- ── 🌾 Fast Farming & Jungle Clear Overdrive ──────────────────────────────────
profile.fast_farming = true
profile.jungle_clear_speed_boost = 10.0
profile.creep_damage_multiplier = 10000
profile.jungle_monster_true_dmg = true
profile.minion_wave_instant_clear = true
profile.last_hit_assist = true
profile.gold_rate_boost = 3
profile.exp_rate_boost = 3
profile.creep_gold_multiplier = 3
profile.fast_level_up = true
profile.auto_smite_objective = 1
profile.smart_creep_targeting = 1
profile.jungle_path_zero_deadzone = true

-- ── ⚡ Fast Retribution & Objective Steal (Lord / Turtle / Buffs) ──────────────
profile.fast_retribution = true
profile.auto_retri_lord_turtle = true
profile.retri_hp_threshold_calc = true
profile.instant_smite = true
profile.smart_retribution_hp_threshold = 1
profile.target_lowest_hp_monster = 1
profile.retribution_damage_max = 10000
profile.auto_smite_lock = 1
profile.retribution_instant_cast = true
profile.objective_target_lock = 1
profile.retribution_steal_sync_rate = 1000
profile.retri_reaction_time_ms = 0
profile.flame_retri_instant = true
profile.ice_retri_instant = true
profile.bloody_retri_instant = true

-- ── 👑 All Hero God Suite (Universal Assassin, Fighter, Mage, Marksman, Tank, Support) ──
profile.all_hero_god_suite = true
profile.all_hero_damage_multiplier = 10000
profile.all_hero_defense_boost = 10000
profile.all_hero_crit_rate_boost = 100
profile.all_hero_true_damage = true
profile.all_hero_instant_cooldown = true
profile.all_hero_unlimited_energy = true
profile.all_hero_unlimited_mana = true
profile.hero_lock_priority = 0               -- Lowest HP hero lock
profile.skill_smart_aim = 1
profile.skill_auto_chain = 1
profile.zero_skill_delay = true
profile.zero_delay_skill_tap = 1
profile.fast_skill_cycle = 1
profile.skill_cast_delay_ms = 0
profile.fast_skill_release_speed = 10

-- ── ⚔️ Hero-Specific Micro Overdrives ─────────────────────────────────────────
-- Fanny: Zero cable delay, straight cable, full energy, wall reset
profile.fanny_cable_speed = 10
profile.fanny_zero_cable_delay = 1
profile.fanny_unlimited_energy = 1
profile.fanny_straight_cable = 1
profile.fanny_wall_hit_reset = 1

-- Ling: Instant sword collect, zero wall jump delay, 10x combo speed
profile.ling_combo_speed = 10
profile.ling_wall_jump_delay = 0
profile.ling_skill_chain_window = 1
profile.ling_sword_collect_zero_delay = 1

-- Gusion: 10-dagger insta recall, zero dagger delay
profile.gusion_dagger_speed = 10
profile.gusion_insta_recall = 1
profile.gusion_10_dagger_combo = 1

-- Chou: Freestyle flicker kick, zero kick delay, dash cancel
profile.chou_freestyle_flicker = 1
profile.chou_kick_zero_delay = 1
profile.chou_dash_cancel = 1

-- Hayabusa: Instant shadow swap, max ultimate burst
profile.haya_shadow_swap_instant = 1
profile.haya_ult_burst_max = 1

-- Beatrix: Instant gun swap, sniper aim lock, zero reload delay
profile.beatrix_gun_swap_instant = 1
profile.beatrix_sniper_aim_lock = 1

-- Lancelot & Franco & Saber & Zilong & Kagura & Alucard & YSS
profile.lancelot_dash_reset_instant = 1
profile.franco_hook_magnet_lock = 1
profile.saber_lock_ult_burst = 1
profile.zilong_slash_speed_overclock = 1
profile.kagura_umbrella_teleport_zero_lag = 1
profile.alucard_infinite_lifesteal_lock = 1
profile.yss_boat_dash_crit = 1

-- New Meta Heroes (Nolan, Joy, Arlott, Suyou)
profile.nolan_infinite_rift_energy = 1
profile.joy_perfect_beat_rhythm_lock = 1
profile.arlott_demon_gaze_auto_stab = 1
profile.suyou_stance_swap_zero_delay = 1

-- ── 🎯 MLBB Enemy Lock — Unity/MOBA Engine (2026.3) ────────────────────────────────────
-- MLBB is a TOP-DOWN MOBA — NO scopes, NO bullet drop, NO ballistic simulation.
-- Mechanics: HeroLock + SkillSmartAim + lowest-HP target priority.
-- Kill logic: 3-skill-hit combo burst (MLBB has no bullets — skills deal damage).
-- Config format: PlayerPrefs XML (com.mobile.legends.v2.playerprefs.xml)

-- ── Hero Target Lock (lowest HP enemy first) ────────────────────────────────────
profile.hero_lock                  = true
profile.hero_lock_enabled          = true
profile.hero_lock_target_priority  = 0    -- 0=lowest HP, 1=nearest
profile.auto_target_switch         = true
profile.target_priority            = 0
profile.smart_target_lock          = true

-- ── Skill Smart Aim — MLBB's native aim correction system ────────────────────
profile.skill_smart_aim            = 1
profile.skill_auto_chain           = 1
profile.zero_skill_delay           = true
profile.skill_cast_zero_delay      = true
profile.skill_aim_magnetism        = 1000
profile.skill_aim_snap_speed       = 10
profile.skill_aim_snap_threshold   = 0
profile.skill_predictive_aim       = true
profile.zero_delay_skill_tap       = 1
profile.fast_skill_cycle           = 1

-- ── Hero Hitbox & Hit Registration ───────────────────────────────────────────
profile.hero_hitbox_multiplier     = 3.0
profile.hero_hitbox_scale          = 3.0
profile.hero_hit_reg_sync_rate     = 1000
profile.hero_instant_hit_reg       = true
profile.hero_frame_sync_damage     = true

-- ── MLBB Kill Mechanic: 3-Skill-Hit Combo Burst ──────────────────────────────
-- MLBB kills via skill combos — NOT bullet counts (it's a MOBA)
profile.hero_skill_burst_kill      = 3    -- 3 skill hits = confirmed kill
profile.hero_kill_combo_count      = 3    -- land 3 hits to eliminate
profile.hero_skill_burst_enabled   = true
profile.skill_burst_damage_max     = 10000
profile.all_hero_damage_multiplier = 10000
profile.all_hero_true_damage       = true
profile.true_strike_mod            = 1
profile.crit_rate_boost            = 100
profile.crit_damage_multiplier     = 10.0
profile.penetration_boost          = true
profile.damage_reduction_bypass    = true

-- ── Aim Assist Keys (Unity PlayerPrefs XML) ───────────────────────────────────
profile.aim_lock                   = true
profile.aim_assist_lock_max        = true
profile.adaptive_aim_strength      = 1000
profile.aim_magnetism              = 1000
profile.aim_snap_speed             = 10
profile.aim_snap_threshold         = 0
profile.aim_smooth_factor          = 0
profile.aim_predict                = true
profile.silent_aimbot              = true
profile.ads_zero_delay             = true

-- ── Touch & Input Zero Lag (Unity touch layer) ────────────────────────────────
profile.touch_polling_rate         = 1000
profile.touch_zero_delay           = true
profile.zero_input_lag             = true

-- ── 💥 Damage Boost & Penetration ─────────────────────────────────────────────
profile.damage_boost = true
profile.damage_multiplier = 10000
profile.true_damage_boost = true
profile.crit_rate_boost = 100
profile.penetration_boost = true
profile.hit_reg_sync_rate = 1000

-- ── ⚡ Attack Speed & Skill Cooldown ─────────────────────────────────────────
profile.fast_attack_speed = true
profile.attack_speed_boost = 10000
profile.fire_rate_multiplier = 10.0
profile.fast_skill_cd = true
profile.skill_cd_ratio = 0.001
profile.ult_cd_reduction = 1.0
profile.zero_skill_cost = true
profile.max_ult_charge = true

-- ── 🏃 Movement, Touch & Drone View ──────────────────────────────────────────
profile.fast_run = true
profile.movement_speed_boost = true
profile.joystick_zero_deadzone = true
profile.zero_input_lag = true
profile.touch_zero_delay = true
profile.touch_polling_rate = 1000
profile.camera_height = 2
profile.fov_boost = 1.35
profile.minimap_enemy_priority = 1

-- ── 🗺️ Ranked & Classic Map Re-injection ──────────────────────────────────────
profile.ranked_combat_suite = true
profile.classic_combat_suite = true
profile.map_reinjection = true

-- ── 🌟 Season 42 "Starward Decade" — Sep 16 2026 ──────────────────────────
profile.season                    = 42
profile.season_name               = "Starward Decade"
profile.season_start              = "2026-09-16"

-- Masha rework: tearing_wounds passive override
profile.masha_wound_dmg           = 10000   -- wound proc damage max
profile.masha_wound_duration      = 0       -- DoT cleared instantly
profile.masha_heal_multiplier     = 0       -- heal proc zeroed
profile.masha_phalanx_timer       = 0       -- instant phalanx swap
profile.masha_stack_decay_rate    = 0       -- wound stacks never decay
profile.masha_tearing_proc        = "instant"

-- Sanctum Island Lord: redesigned Phase 2 + new HP threshold
profile.lord_hp_threshold         = 1       -- steal triggers at 1 HP
profile.turtle_hp_threshold       = 1
profile.lord_phase2_override      = 1       -- new Phase 2 logic bypass
profile.retri_steal_sync_rate     = 1000   -- steal packet rate 1000 Hz
profile.object_hp_floor           = 1
profile.smart_retri_timing        = "instant"

-- 7 Hero visual-refresh revamp boost (Bruno / Brody / Clint / Kadita / Badang / LuoYi / Paquito)
profile.bruno_rotation_speed          = 10   -- control responsiveness buff
profile.bruno_skill_track_accel       = 10
profile.kadita_undertow_duration      = 0    -- zero Undertow CC window
profile.badang_wall_lock_instant      = 1
profile.luoyi_reverse_instant         = 1
profile.paquito_heavy_handed_instant  = 1
profile.brody_star_mark_zero_delay    = 1
profile.clint_mastery_zero_cd         = 1
profile.emote_slot_zero_delay         = 1    -- 6-slot emote wheel fix

-- Season 42 ranked combat suite
profile.season42_master_suite     = true
profile.season42_masha_override   = true
profile.season42_lord_steal       = true
profile.season42_revamp_boost     = true

return profile
