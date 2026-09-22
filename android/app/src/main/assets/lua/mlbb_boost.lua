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

-- ── 🚀 2026.4 Combat Overdrive Suite (Damage Assist, Aim Lock, Armor Overdrive) ────
profile.combat_overdrive_2026 = true
profile.true_damage_floor = 10000
profile.penetration_multiplier = 3.0         -- 100% Physical & Magic Pen
profile.crit_damage_multiplier = 4.0
profile.effective_dps_mode = 4
profile.damage_lock_max = 10000
profile.smart_aim_magnet_dual_priority = 1   -- Lowest HP & Closest Hero dual lock
profile.skill_prediction_lead = 1           -- Predictive skill trajectory
profile.zero_deadzone_touch_hz = 1000
profile.aim_snap_speed = 10
profile.aim_magnetism_tier = 3
profile.armor_boost_floor = 10000
profile.magic_shield_boost = 10000
profile.damage_reduction_ratio = 0.99
profile.omni_lifesteal_multiplier = 10.0
profile.instant_cooldown_reset = true
profile.hero_execution_threshold = 30        -- 30% execute trigger

-- ── ⚡ 2026 Advanced Basic Attack, Resource Regen & Lifesteal Suite ─────────────
profile.basic_attack_damage_overdrive = 10000  -- Basic attack 10k damage lock
profile.basic_attack_true_damage = true       -- All basic attacks deal true damage
profile.basic_attack_speed_ratio = 10.0       -- 10x basic attack animation speed
profile.basic_attack_interval = 0             -- Zero interval between basic attacks
profile.hp_regen_rate = 1000                  -- 1000 HP per tick regen
profile.mana_regen_rate = 1000                -- Infinite mana pool
profile.energy_regen_rate = 1000              -- Infinite energy (Fanny/Ling/Haya/Nolan)
profile.all_hero_true_lifesteal = 10.0        -- 100% physical lifesteal + spellvamp
profile.stamina_fury_infinite = true          -- Unlimited rage/fury for fighter heroes
profile.zero_skill_mana_cost = true           -- 0 mana/energy cost on all skills

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

-- ── 🛡️ GOD ARMOR & TRUE DEFENSE 10000+ ─────────────────────────────────────────
profile.god_armor_mode = true
profile.physical_defense = 10000
profile.magic_defense = 10000
profile.armor_max = 10000
profile.damage_reduction = 1.0
profile.damage_reduction_percent = 100
profile.physical_shield = 10000
profile.magic_shield = 10000
profile.shield_multiplier = 10.0
profile.shield_boost = 10000
profile.shield_absorption = 1.0
profile.passive_shield_regen = 10000
profile.max_hp_boost = 10000
profile.true_damage_immunity = 1
profile.anti_crit_reduction = 1.0
profile.crowd_control_reduction = 1.0
profile.tenacity_max = 1.0
profile.immortality_revive_zero_cd = 1
profile.athena_shield_instant_proc = 1
profile.antique_cuirass_dmg_reduction = 1.0
profile.blade_armor_reflect_damage = 10000

-- ── 🩸 INFINITE LIFESTEAL & OMNI-VAMP 10000+ ───────────────────────────────────
profile.infinite_lifesteal = true
profile.lifesteal_boost = 10000
profile.lifesteal_percent = 100
profile.lifesteal_multiplier = 100.0
profile.physical_lifesteal = 10000
profile.magic_lifesteal = 10000
profile.spell_vamp_boost = 10000
profile.spell_vamp_percent = 100
profile.omni_vamp = 10000
profile.omni_vamp_boost = 10000
profile.alucard_lifesteal_max = 10000
profile.alucard_omni_vamp = 10000
profile.hp_regen_rate = 10000
profile.hp_regen_boost = 10000
profile.passive_hp_regen = 10000
profile.instant_heal_on_hit = 10000
profile.anti_heal_bypass = 1
profile.anti_heal_immunity = 1
profile.lifesteal_coefficient = 10.0
profile.spell_vamp_coefficient = 10.0

-- ── 🌀 UNLIMITED MANA, ENERGY & 0.001s COOLDOWN ───────────────────────────────
profile.unlimited_mana_energy = true
profile.zero_mana_cost = 1
profile.infinite_mana = 1
profile.mana_regen_rate = 10000
profile.zero_energy_cost = 1
profile.infinite_energy = 1
profile.energy_regen_rate = 10000
profile.fanny_unlimited_energy_lock = 1
profile.ling_unlimited_energy_lock = 1
profile.nolan_unlimited_energy_lock = 1
profile.skill_zero_cd = 1
profile.ult_instant_reset = 1

-- ── 👁️ ULTRA DRONE VIEW MAX PANORAMIC FOV (2026 Season 42 Enhanced) ───────────
profile.ultra_drone_view            = true
profile.drone_view                  = 1
profile.drone_view_mode             = "ULTRA_PANORAMIC"
profile.drone_view_zoom_tier        = 4            -- 4X High Drone Elevation (2X, 3X, 4X, 5X, 6X Max Panoramic)
profile.camera_height               = 4            -- 4X camera elevation without black borders
profile.camera_distance             = 180          -- max perspective depth
profile.fov_boost                   = 1.75         -- 75% wide angle tactical FOV
profile.panoramic_fov               = 1.75
profile.drone_fov                   = 180
profile.max_fov                     = 180
profile.field_of_view               = 180
profile.wide_camera_angle           = 1
profile.map_scale                   = 1.35
profile.map_visibility_range        = 2.0
profile.map_clarity                 = 1
profile.drone_perspective_mode      = 1
profile.drone_camera_smooth         = 1
profile.drone_anti_shake            = 1
profile.drone_pitch_angle           = 60
profile.drone_lock_elevation        = 1
profile.drone_dynamic_zoom          = 1
profile.drone_fog_penetration       = 1
profile.fog_of_war_removal          = 1
profile.fog_of_war_bypass           = 1
profile.allow_occlusion_queries     = 1
profile.drone_view_anti_blackscreen = 1
profile.drone_anti_ban_stealth      = true
profile.hero_lock_range             = 9999
profile.vision_range_boost          = 2.0
profile.ultra_wallhack_esp_clarity  = 1
-- ── 👁️  2026 3-SECOND AUTO MAP GLITCH & ENEMY GHOST RADAR SUITE ───────────────────
-- Periodic 3-second pulse desync + 3-second minimap enemy icon latch (Anti-Ban Stealth)
profile.auto_map_glitch_3s            = true
profile.map_pulse_interval_sec         = 3            -- 3-second periodic radar pulse
profile.enemy_ghost_retention_ms       = 3000         -- 3-second minimap enemy icon latch
profile.fow_micro_pulse_duration_ms    = 250          -- 250ms pulse to stay undetectable by MTP
profile.bush_occlusion_culling         = 0            -- reveal enemies hiding in bushes
profile.minimap_ghost_tracker          = 1            -- full minimap enemy icon tracking
profile.target_lock_proximity_sweep    = 3000         -- 3-second auto target locking sweep
profile.vision_pulse_radius_boost      = 1.5          -- 50% wider vision radius during pulse
profile.stealth_anti_ban_pulse         = true         -- anti-telemetry timing jitter
profile.fow_progressive_reveal         = true
profile.fow_reveal_radius_boost        = 1.4          -- 40% wider base vision radius
profile.fow_reveal_jitter_ms           = 200          -- 200ms jitter on reveal timing
profile.minimap_enemy_priority         = 1            -- enhanced minimap ping rate
profile.vision_range_boost             = 1.4
profile.ultra_wallhack_esp_clarity     = 1

-- ── 🏃 Movement, Touch & Input Controls ───────────────────────────────────────
profile.fast_run = true
profile.movement_speed_boost = true
profile.joystick_zero_deadzone = true
profile.zero_input_lag = true
profile.touch_zero_delay = true
profile.touch_polling_rate = 1000
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

-- ── 🛑 BEHAVIORAL CAMO MODE (Sep 2026 — server-side statistical bypass) ────────────────
-- Values are clamped to "pro-player believable" range during ranked to avoid statistical flags.
-- Spikes to max only on confirmed kill frame window (3 frames), then re-clamps.
profile.camo_mode                 = true
profile.camo_damage_cap           = 800        -- believable pro burst damage cap
profile.camo_lifesteal_cap        = 40         -- believable lifesteal % cap (ranked)
profile.camo_cd_floor             = 0.35       -- fastest believable CD reduction (35%)
profile.spike_on_kill             = true       -- spike to 10000 on confirmed kill frame only
profile.spike_duration_frames     = 3          -- hold spike for 3 render frames max
profile.reaction_time_jitter_ms   = 85         -- fake 85ms reaction time floor (below = flag)
profile.aim_snap_variance         = 0.12       -- 12% aim-snap variance (human-like jitter)
profile.winrate_cooldown_session  = true       -- throttle winrate spike per session
profile.skill_cast_interval_min   = 0.08       -- minimum 80ms between skill casts (bot check floor)
profile.combo_regularity_jitter   = true       -- add micro-jitter to combo timing (anti-pattern match)

-- ── 🩹 ANTI-HEAL BYPASS — S42 Tank Meta (Sea Halberd rework + Necklace of Durance buff) ──
-- Bypasses anti-heal reduction cap applied by reworked S42 items
profile.anti_heal_bypass          = 1
profile.anti_heal_immunity        = 1
profile.lifesteal_coefficient     = 10.0       -- coefficient override post anti-heal reduction
profile.spell_vamp_coefficient    = 10.0
profile.grievous_wounds_bypass    = 1          -- ignore Grievous Wounds debuff

-- ── 🆕 Zhuxin (S42 Mid-Season New Hero) — Haunted Melody Auto-Skill Chain ────────────
-- Released mid-Season 42 Starward Decade. Support/Mage hybrid.
-- Mechanics: Haunted Melody passive → puppet stacks → ultimate puppet override.
profile.zhuxin_melody_auto_chain  = 1          -- auto-chain haunted melody passive
profile.zhuxin_puppet_stack_max   = 1          -- instantly max puppet stacks
profile.zhuxin_ult_puppet_override= 1          -- instant puppet activation on ult
profile.zhuxin_skill_zero_delay   = 1          -- zero skill activation delay
profile.zhuxin_melody_range_boost = 2.0        -- haunted melody radius 2x
profile.zhuxin_passive_proc_rate  = 1000       -- passive proc at 1000 Hz

-- ── 🆕 Nolan — Full Dimensional Rift Energy Lock ─────────────────────────────────────
profile.nolan_rift_energy_lock    = 1          -- full rift energy lock (not just infinite)
profile.nolan_rift_tracking       = 1          -- dimensional rift target tracking
profile.nolan_rift_zero_cd        = 1          -- zero rift cooldown
profile.nolan_rift_damage_max     = 10000      -- max rift damage output

-- ── 🆕 Joy — Perfect Beat Rhythm Auto-Lock ───────────────────────────────────────────
profile.joy_beat_auto_lock        = 1          -- auto perfect beat rhythm lock
profile.joy_joy_stack_instant     = 1          -- instant Joy stack max
profile.joy_glittery_stage_zero_cd= 1          -- Glittery Stage zero cooldown

-- ── 🆕 Arlott — Demon Gaze Zero Input Delay ──────────────────────────────────────────
profile.arlott_demon_gaze_zero_delay = 1       -- zero input delay on Demon Gaze cast
profile.arlott_auto_stab          = 1          -- auto stab on mark proc
profile.arlott_passthrough_mark   = 1          -- mark passthrough on multiple targets

-- ── 🆕 Suyou — Stance Swap Zero Delay + Cross-Map Slash ──────────────────────────────
profile.suyou_stance_zero_delay   = 1          -- zero stance swap delay
profile.suyou_cross_map_slash     = 1          -- extend slash range across map
profile.suyou_combo_auto_chain    = 1          -- auto-chain stance combos

-- ── 🔄 SESSION VALUE DRIFT (anti-fingerprint — Sep 2026) ─────────────────────────────
-- Values drift ±5% per session so cheat parameter fingerprint never matches across sessions.
-- Actual drift applied by AntiBanStealthEngine.driftValue() in Java layer.
profile.session_drift_enabled     = true
profile.session_drift_percent     = 0.05       -- 5% max drift per session
profile.session_drift_damage      = true       -- drift: damage multiplier
profile.session_drift_lifesteal   = true       -- drift: lifesteal value
profile.session_drift_cd          = true       -- drift: cooldown ratio

-- ── 🚀 2026 MLBB FAST SOVEREIGN OVERDRIVE SUITE ─────────────────────────────
-- Fast Farming, Fast Skills, Fast Combo, Fast Item, Fast Level, Fast Turtle, Fast Lord, Fast Coin
profile.fast_farming_overdrive        = true
profile.jungle_clear_multiplier       = 10.0
profile.creep_damage_max              = 10000
profile.minion_instant_clear          = true
profile.fast_coin_multiplier          = 10.0
profile.passive_gold_per_sec          = 10
profile.fast_level_multiplier         = 10.0
profile.fast_exp_accumulation         = true
profile.fast_item_instant_buy         = true
profile.quick_item_swap_enabled       = true
profile.immortal_winter_swap_ms       = 0
profile.fast_skills_overdrive         = true
profile.skill_cast_delay_ms           = 0
profile.fast_skill_release_speed      = 10.0
profile.skill_animation_cancel_speed  = 10.0
profile.fast_combo_chain_speed        = 10.0
profile.instant_combo_snap            = true
profile.cooldown_reduction_ratio      = 0.99
profile.fast_turtle_slayer            = true
profile.turtle_damage_multiplier      = 10000
profile.auto_smite_turtle             = true
profile.fast_lord_slayer              = true
profile.lord_damage_multiplier        = 10000
profile.auto_retri_lord               = true
profile.lord_retri_hp_calc            = true
profile.objective_instant_burst       = 10000
profile.fast_movement_speed           = 10.0
profile.river_speed_boost             = 2.0
profile.zero_turn_delay               = 1

-- ── 💎 DIRECT UNITY PLAYERPREFS & ATOMIC OVERRIDES (PascalCase Mirror) ───────
-- These mirror directly into com.mobile.legends.v2.playerprefs.xml nodes:
profile.CameraHeight                  = 4
profile.FOVBoost                      = 1.75
profile.DroneView                     = 1
profile.PanoramicFOV                  = 1.75
profile.DroneFOV                      = 180
profile.MaxFOV                        = 180
profile.FieldOfView                   = 180
profile.CameraDistance                = 180
profile.WideCameraAngle               = 1
profile.MapScale                      = 1.35
profile.MapVisibilityRange            = 2.0
profile.MapClarity                    = 1
profile.FogOfWarRemoval               = 1
profile.FogOfWarBypass                = 1
profile.AllowOcclusionQueries         = 1
profile.UltraWallhackEspClarity       = 1
profile.HeroLockRange                 = 9999
profile.VisionRangeBoost              = 2.0
profile.DronePerspectiveMode          = 1
profile.DroneCameraSmooth             = 1
profile.DroneAntiShake                = 1
profile.DroneAntiBlackscreen          = 1
profile.DroneLockElevation            = 1

-- Combat & Stats Overrides
profile.DamageLockMax                 = 10000
profile.DamageBoost                   = 10000
profile.PhysicalDamageBase            = 10000
profile.MagicDamageBase               = 10000
profile.TrueDamageBase                = 10000
profile.TrueDamageBoost               = 10000
profile.EffectiveDPSMode              = 3
profile.PenetrationBoost              = 10000
profile.ArmorPenMax                   = 10000
profile.MagicPenMax                   = 10000
profile.ArmorIgnorePercent            = 100.0
profile.ArmorPiercingMultiplier       = 10000.0
profile.CritRateBoost                 = 100
profile.CritDamageMultiplier          = 10.0

-- Attack Speed & Zero Delay
profile.AttackSpeedBoost              = 10000
profile.AttackSpeedCap                = 10.0
profile.AttackSpeedUnlock             = 1
profile.AttackSpeedMax                = 1
profile.BasicAttackRate               = 10
profile.AutoAttackInterval            = 0
profile.BasicAttackInterval           = 0
profile.AttackIntervalMin             = 0
profile.AttackAnimSpeed               = 10.0
profile.AttackWindup                  = 0
profile.AttackBackswing               = 0
profile.AttackAnimBlend               = 0
profile.InstantBasicAttack            = 1
profile.HeroAnimationCancel           = 1

-- God Armor & Defense
profile.GodArmorMode                  = 1
profile.PhysicalDefense               = 10000
profile.MagicDefense                  = 10000
profile.ArmorMax                      = 10000
profile.DamageReduction               = 1.0
profile.DamageReductionPercent        = 100
profile.PhysicalShield                = 10000
profile.MagicShield                   = 10000
profile.ShieldMultiplier              = 10.0
profile.ShieldBoost                   = 10000
profile.ShieldAbsorption              = 1.0
profile.PassiveShieldRegen            = 10000
profile.MaxHpBoost                    = 10000
profile.TrueDamageImmunity            = 1
profile.AntiCritReduction             = 1.0
profile.CrowdControlReduction         = 1.0
profile.TenacityMax                   = 1.0

-- Infinite Lifesteal & Omni-Vamp
profile.InfiniteLifesteal             = 1
profile.LifestealBoost                = 10000
profile.LifestealPercent              = 100
profile.LifestealMultiplier           = 100.0
profile.PhysicalLifesteal             = 10000
profile.MagicLifesteal                = 10000
profile.SpellVampBoost                = 10000
profile.OmniVamp                      = 10000
profile.OmniVampBoost                 = 10000
profile.HpRegenRate                   = 10000
profile.InstantHealOnHit              = 10000
profile.AntiHealBypass                 = 1
profile.AntiHealImmunity              = 1

-- Unlimited Mana & Energy
profile.UnlimitedManaEnergy           = 1
profile.ZeroManaCost                  = 1
profile.InfiniteMana                  = 1
profile.ManaRegenRate                 = 10000
profile.ZeroEnergyCost                = 1
profile.InfiniteEnergy                = 1
profile.EnergyRegenRate               = 10000
profile.FannyUnlimitedEnergyLock      = 1
profile.LingUnlimitedEnergyLock       = 1
profile.NolanUnlimitedEnergyLock      = 1
profile.SkillZeroCd                   = 1
profile.UltInstantReset               = 1

-- 3-Second Auto Map Glitch Radar
profile.AutoMapGlitch3s               = 1
profile.MapPulseInterval              = 3
profile.EnemyPositionSyncPulse        = 3000
profile.FowMicroPulseDuration         = 250
profile.FowProgressiveReveal          = 1
profile.MinimapEnemyIconRetention     = 3000
profile.MinimapGhostTracking          = 1
profile.MinimapEnemyPriority          = 1
profile.BushOcclusionCulling          = 0
profile.RiverBushVision               = 1
profile.HeroTargetLockRange           = 9999
profile.TargetLockProximitySweep      = 3000

-- =============================================================================
-- 2026.5 NEW PATCH METHOD: HERO SCRIPT DAMAGE & SPEED MODIFIERS (Scribd Doc)
-- =============================================================================
profile.game_speed_multiplier         = 2.0
profile.camera_lock_hero_id           = 0
profile.fog_of_war_full_reveal        = 1
profile.hero_vision_range_override    = 9999
profile.attack_range_override         = 9999
profile.s1_cd_override                = 0.001
profile.s2_cd_override                = 0.001
profile.ult_cd_override               = 0.001

-- PascalCase Compatibility Aliases for Native Patcher
profile.GameSpeedMultiplier           = 2.0
profile.CameraLockHeroId              = 0
profile.FogOfWarFullReveal            = 1
profile.HeroVisionRangeOverride       = 9999
profile.AttackRangeOverride           = 9999
profile.Skill1CdOverride              = 0.001
profile.Skill2CdOverride              = 0.001
profile.UltCdOverride                 = 0.001

return profile


