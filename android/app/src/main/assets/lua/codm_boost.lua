-- =============================================================================
-- CALL OF DUTY MOBILE GLOBAL OPTIMIZATION PROFILE (2026 Native Engine)
-- =============================================================================

profile = {}

profile.game_id = "codm"
profile.package_name = "com.activision.callofduty.shooter"
profile.target_fps = 120
profile.graphics_tier = "VERY_HIGH"
profile.force_vulkan = true
profile.touch_boost_hz = 1000
profile.cpu_governor = "performance"

-- Engine & Shader Keys
profile.codm_max_fps = 120
profile.codm_gfx_quality = 4
profile.vulkan_prewarm = true
profile.anti_telemetry = true

-- ── Combat Enhancement Suite 2026 ─────────────────────────────────────────────
-- Adaptive No Recoil — per weapon category
profile.adaptive_no_recoil = true
profile.ar_recoil_scale = 0.0               -- AR: full zero
profile.smg_recoil_scale = 0.0              -- SMG: full zero
profile.sniper_recoil_scale = 0.0           -- Sniper: zero sway
profile.lmg_recoil_scale = 0.0             -- LMG: full zero
profile.shotgun_recoil_scale = 0.0
profile.dmr_recoil_scale = 0.0
profile.pistol_recoil_scale = 0.0
profile.spread_decay_rate = 15              -- fast spray recovery
profile.bullet_spread_scale = 0
profile.weapon_sway = 0
profile.gyro_stabilization = 1
profile.gyro_sample_rate = 1000

-- ── 🚀 2026.4 Combat Overdrive Suite (Damage Assist, Aim Lock, Armor Overdrive) ────
profile.combat_overdrive_2026       = true
profile.kinetic_armor_overdrive     = 10000       -- 10,000 Kinetic defense floor
profile.damage_reduction_ratio      = 0.95        -- 95% incoming damage absorption
profile.flak_jacket_explosion_lock  = 1           -- Zero explosive damage
profile.laser_beam_zero_spread      = true        -- Zero weapon spread on all optics
profile.ads_zero_delay_instant      = true        -- 0ms ADS transition
profile.head_magnetism_max          = 1000        -- Max head magnetism
profile.hit_reg_packet_sync_1000hz  = true        -- 1000Hz hit packet sync
profile.damage_floor_max            = 10000       -- Max damage floor
profile.aim_snap_speed_instant      = 10          -- Maximum snap acquisition

-- ── 🔭 Multi-Range Scope Lock 100m-450m & Fast Reload / Sprint (All Guns) ──────
profile.scope_lock_all_scopes_all_guns = 1
profile.scope_100m_lock             = 1           -- 100m Iron, Red Dot, Holo lock
profile.scope_200m_lock             = 1           -- 200m Tactical & 3x ACOG lock
profile.scope_300m_lock             = 1           -- 300m 4.4x Scope lock
profile.scope_400m_lock             = 1           -- 400m 6x Scope lock
profile.scope_450m_lock             = 1           -- 450m 8x+ Sniper Scope lock
profile.bullet_tracking_lock        = 1           -- Bullet tracking magnetism
profile.bullet_tracking_hitbox      = 3.0         -- 3x hitbox registration
profile.fast_reload_chambering      = 1           -- 0ms instant reload
profile.fast_sprint_turbo           = 1           -- Instant max sprint speed
profile.slide_cancel_fast           = 1           -- Instant slide cancel responsiveness
profile.fast_weapon_swap_instant    = 1           -- Zero delay weapon swap

-- ── 🎯 Enemy Lock MAX — Dame Aim Assist + Multi-Range + Auto Headshot (2026.3) ──────────
-- Enemy lock: targets lowest HP enemy first, head-bone hard-lock
profile.enemy_lock_max              = true
profile.target_priority             = 0           -- lowest HP first
profile.target_lock_range           = 450         -- outer fence
profile.lock_on_enemy_max           = true
profile.auto_target_switch          = true

-- Head bone hard-lock (bone index 0 = head)
profile.head_bone_aim_priority      = 1
profile.bone_index                  = 0
profile.head_magnetism              = 1000
profile.head_snap_enabled           = true
profile.head_snap_speed             = 10

-- Aim snap & magnetism — silent aimbot mode
profile.aim_lock                    = true
profile.aim_assist_lock_max         = true
profile.aim_magnetism               = 1000
profile.aim_magnetism_level         = 10
profile.aim_snap_speed              = 10
profile.aim_snap_threshold          = 0
profile.scope_headshot              = true         -- all scope tiers snap to head
profile.head_bone_priority          = 1
profile.ads_zero_delay              = true
profile.predictive_aim              = true
profile.silent_aimbot               = true         -- lock-on without visible snap

-- Scope range tiers for CODM (per weapon category covered at each tier)
-- Tier 0: Hipfire / No-scope → 50m (AR/SMG/Shotgun hipfire)
profile.scope_tier0_range           = 50
profile.scope_tier0_aim_magnetism   = 1000
profile.scope_tier0_head_magnetism  = 1000
profile.scope_tier0_head_lock       = true
profile.hipfire_aim_lock            = true
profile.hipfire_headshot_lock       = true
profile.no_scope_head_snap          = true

-- Tier 1: 1x Red Dot / Holographic → 150m (AR/SMG close-mid)
profile.scope_tier1_range           = 150
profile.scope_tier1_aim_magnetism   = 1000
profile.scope_tier1_head_magnetism  = 1000
profile.scope_tier1_head_lock       = true
profile.scope_tier1_predictive_aim  = true
profile.scope_1x_aim_lock           = true
profile.scope_1x_headshot_force     = true
profile.scope_1x_ads_zero_delay     = true

-- Tier 2: 3x ACOG / 2x-4x variable → 250m (AR/DMR mid-range)
profile.scope_tier2_range           = 250
profile.scope_tier2_aim_magnetism   = 1000
profile.scope_tier2_head_magnetism  = 1000
profile.scope_tier2_head_lock       = true
profile.scope_tier2_predictive_aim  = true
profile.scope_tier2_bullet_drop     = true
profile.scope_3x_aim_lock           = true
profile.scope_3x_headshot_force     = true
profile.scope_3x_predictive_lead    = true

-- Tier 3: 6x / Sniper variable → 350m (Sniper/DMR long-range)
profile.scope_tier3_range           = 350
profile.scope_tier3_aim_magnetism   = 1000
profile.scope_tier3_head_magnetism  = 1000
profile.scope_tier3_head_lock       = true
profile.scope_tier3_predictive_aim  = true
profile.scope_tier3_bullet_drop     = true
profile.scope_tier3_gyro_lock       = true
profile.scope_6x_aim_lock           = true
profile.scope_6x_headshot_force     = true
profile.scope_6x_ballistic_comp     = true
profile.scope_6x_zero_sway          = true

-- Tier 4: 10x+ Long-range Sniper → 450m (Sniper max range)
profile.scope_tier4_range           = 450
profile.scope_tier4_aim_magnetism   = 1000
profile.scope_tier4_head_magnetism  = 1000
profile.scope_tier4_head_lock       = true
profile.scope_tier4_predictive_aim  = true
profile.scope_tier4_bullet_drop     = true
profile.scope_tier4_gyro_lock       = true
profile.scope_tier4_anti_breath     = true
profile.scope_10x_aim_lock          = true
profile.scope_10x_headshot_force    = true
profile.scope_10x_zero_bullet_drop  = true
profile.scope_10x_anti_breath       = true
profile.sniper_headshot_lock        = true
profile.sniper_zero_sway            = true
profile.sniper_zero_bullet_drop     = true

-- ── 💀 Auto Headshot Kill Mode ─────────────────────────────────────────────────
-- 3-bullet burst on head bone = confirmed kill
-- 5-bullet sweep anywhere = universal fallback kill
profile.auto_headshot               = true
profile.auto_headshot_all_scope     = true
profile.headshot_bullet_count       = 3            -- 3 bullets → head kill
profile.kill_bullet_count           = 5            -- 5 bullets → universal kill
profile.headshot_force_enabled      = true
profile.headshot_bone_index         = 0            -- head bone
profile.one_tap_headshot            = true
profile.first_bullet_accuracy       = 1.0          -- 100% first-bullet precision
profile.scope_headshot_lock         = true
profile.headshot_multiplier         = 999
profile.head_damage_max             = 99999

-- Fast Reload & Instant Chambering Sniper Overdrive
profile.fast_reload = true
profile.reload_speed_multiplier = 10.0
profile.sleight_of_hand = true
profile.instant_chambering = true
profile.bolt_cycle_instant = true
profile.bolt_action_cycle_ms = 0
profile.fast_bolt_pull_speed = 10.0
profile.quick_draw_factor = 10.0
profile.quick_swap = true
profile.draw_speed_boost = 10.0
profile.blank_scope_accuracy = 1.0
profile.quick_scope_accuracy_threshold = 1.0

-- Damage Boost & 3x Hitbox Armor Shredder
profile.damage_boost = true
profile.damage_multiplier = 10000
profile.damage_lock_max = 10000
profile.true_damage_boost = true
profile.headshot_multiplier = 999.0
profile.crit_rate_boost = 100
profile.penetration_boost = true
profile.vest_damage_bypass = true
profile.helmet_penetration_level3 = 1.0
profile.hitbox_multiplier = 3.0
profile.one_shot_kill_hitbox = 1
profile.hit_reg_sync_rate = 1000

-- Fast Run, Slide-Cancel & Mobility Overdrive
profile.fast_run = true
profile.sprint_speed_max = true
profile.sprint_acceleration = 10.0
profile.slide_delay_ms = 0
profile.slide_cancel_enabled = 1
profile.slide_speed_boost = 3.0
profile.jump_fatigue_removal = 1
profile.fast_mantle = 1
profile.joystick_zero_deadzone = true
profile.joystick_response_level = 3
profile.zero_input_lag = true
profile.touch_polling_rate = 1000
profile.touch_zero_delay = true

-- Ranked & Classic: All Map Coverage
profile.ranked_combat_suite = true
profile.classic_combat_suite = true
profile.map_reinjection = true

-- ── ⚔️ Season 8 "Against All Fate" — Sep 9 2026 ────────────────────────
profile.season                    = 8
profile.season_name               = "Against All Fate"
profile.season_start              = "2026-09-09"

-- Season 8 New Battle Pass Weapons
-- Static-HV SMG (high-voltage shock rounds)
profile.static_hv_recoil_scale    = 0     -- zero recoil
profile.static_hv_spread_scale    = 0     -- zero spread cone
profile.static_hv_aim_assist      = 1     -- aim assist max
-- ISO Hemlock AR (precision first-bullet accuracy)
profile.iso_hemlock_recoil_scale  = 0
profile.iso_hemlock_spread_scale  = 0
profile.iso_hemlock_bullet_spread = 0
profile.iso_hemlock_headshot_bonus = 999  -- guaranteed headshot burst kill

-- Season 8 Roguelike Mode (Honkai Impact 3rd Collab)
profile.roguelike_mode            = true
profile.roguelike_aim_assist      = 1
profile.roguelike_silent_aim      = 1
profile.roguelike_head_priority   = 1
profile.roguelike_aim_magnetism   = 1000
profile.roguelike_no_spread       = 1
profile.roguelike_aim_snap        = 10

-- Season 8 Isolated Map New POI Terrain Bypass
profile.wall_check_radius         = 0    -- zero wall-check radius
profile.occlusion_bypass_enabled  = 1    -- bypass new POI occlusion meshes
profile.wall_penetrate_range      = 450  -- covers max new POI range
profile.poi_visibility_override   = 1
profile.esp_clarity_boost         = 1

-- Season 8 full sweep flags (BR + MP + Ranked)
profile.s8_full_ranked_sweep      = true
profile.s8_new_weapons_override   = true
profile.s8_roguelike_mode         = true
profile.s8_isolated_poi_bypass    = true
-- stealth_write is CRITICAL for CODM anti-cheat (Warzone Mobile dead, AC focus 100% CODM)
profile.stealth_write             = true

-- ── 🎯 TIMI HEADSHOT RATIO GOVERNOR (TiMi behavioral engine — Sep 2026) ─────────────────
-- TiMi session-level behavioral engine flags >85% HS ratio per session for manual review.
-- Governor caps HS ratio at 72% by force-routing 28% of kills as bodyshots in config layer.
profile.headshot_ratio_cap        = 0.72        -- max 72% HS ratio (believable skilled player)
profile.bodyshot_injection_rate   = 0.28        -- force 28% bodyshots for ratio cover
profile.hs_session_cooldown_ms    = 3500        -- 3.5s forced interval between HS kill streaks
profile.hs_streak_cap_per_game    = 8           -- max 8-HS streak before bodyshot forced
profile.timi_ratio_bypass         = true        -- enable ratio governor layer
-- Reduce headshot_bone_index from pure 0 to alternating (0=head, 1=neck fallback for ratio)
profile.head_bone_index_alt       = 1           -- alt bone for 28% bodyshot injection
profile.headshot_multiplier       = 720         -- was 999, now 720 (believable ranked player)
profile.head_damage_max           = 1200        -- was 99999, now 1200 (TiMi-safe floor)

-- ── 🔫 STOCHASTIC RCS — TiMi Pattern Bypass (Sep 2026) ──────────────────────────────────
-- TiMi now detects perfectly linear recoil compensation. Stochastic noise makes it human-like.
profile.rcs_enabled               = true
profile.rcs_strength              = 0.88        -- 88% RCS (not 100% = linear pattern flag)
profile.rcs_stochastic_noise      = 0.07        -- 7% stochastic noise floor
profile.rcs_burst_delay_frames    = 2           -- 2-frame delay on burst fire start
profile.ar_recoil_scale           = 0.07        -- was 0.0, now 0.07 (TiMi-safe)
profile.smg_recoil_scale          = 0.05        -- TiMi-safe floor
profile.sniper_recoil_scale       = 0.04        -- TiMi-safe floor
profile.lmg_recoil_scale          = 0.09        -- TiMi-safe floor
profile.dmr_recoil_scale          = 0.06        -- TiMi-safe floor
profile.shotgun_recoil_scale      = 0.08        -- TiMi-safe floor
profile.pistol_recoil_scale       = 0.04        -- TiMi-safe floor
-- Per-weapon stochastic RCS tuning
profile.ak47_rcs                  = 0.86
profile.man_o_war_rcs             = 0.84
profile.kilo_141_rcs              = 0.90
profile.holger_rcs                = 0.88
profile.hvk_rcs                   = 0.92
profile.static_hv_rcs             = 0.89       -- S8 new weapon
profile.iso_hemlock_rcs           = 0.91       -- S8 new weapon
profile.spread_decay_rate         = 12         -- was 15, now 12 (TiMi-safe)

-- ── 🏃 BR SAFE MOVEMENT (position-delta checks tighter in BR than MP) ─────────────────
-- BR mode has stricter server position validation than MP mode.
profile.br_movement_boost         = 1.10        -- safe: 1.10x (1.15x = position-delta flag)
profile.br_loot_speed_boost       = 1.25        -- loot interaction (client-side UI only)
profile.br_healing_speed          = 1.0         -- keep at 1.0x (server-validated heal time)
profile.br_parachute_speed        = 1.05        -- minor parachute boost (safe)
profile.br_slide_cancel           = true        -- slide cancel still safe (animation-layer)
profile.br_vault_speed            = 1.08        -- vault animation speed (client only)
profile.sprint_speed_max          = false       -- disable position-affecting speed hack in BR

-- ── 🎖️ MP PERK OVERDRIVE (Multiplayer — TiMi perk behavior check less strict than BR) ──
profile.mp_perk_overdrive         = true
profile.ghost_perk_max            = true        -- movement audio suppressed (UAV immunity)
profile.tracker_perk_bypass       = true        -- enemy footprint override
profile.hardline_bonus_rate       = 2           -- 2x killstreak progression
profile.overkill_carry_instant    = true        -- instant secondary weapon access
profile.dead_silence_zero_cd      = true        -- Dead Silence field upgrade zero cooldown
profile.engineer_vision_range     = 80          -- Engineer perk ESP extended to 80m

-- ── 🔧 GUNSMITH ATTACHMENT SIMULATION (no asset hash touch) ──────────────────────────
-- Simulates max-attachment stat bonuses via CVar injection without modifying game APK assets.
-- Safe: config-only, no file hash touch.
profile.max_attachment_damage     = 1           -- MaxAttachmentDamage=1
profile.attachment_range_bonus    = 1           -- AttachmentRangeBonus=1
profile.attachment_recoil_ctrl    = 0.08        -- AttachmentRecoilControl (TiMi-safe floor)
profile.attachment_ads_speed_bonus= 1           -- AttachmentADSSpeedBonus=1
profile.attachment_mobility_bonus = 1           -- AttachmentMobilityBonus=1

-- ── 🔄 SESSION VALUE DRIFT (anti-fingerprint — TiMi session tracker bypass) ─────────────
profile.session_drift_enabled     = true
profile.session_drift_percent     = 0.05        -- 5% drift per session
profile.session_drift_rcs         = true        -- drift: RCS strength
profile.session_drift_hs_cap      = true        -- drift: HS ratio cap (±3%)
profile.session_drift_aim         = true        -- drift: aim magnetism level

-- ── 🚀 2026 CODM SOVEREIGN OVERDRIVE & ADVANCED BYPASS SUITE ───────────────
-- Combat & Ballistics Overdrive (Zero BSA, Laser Beam, Silent Aim)
profile.zero_bsa_lock                 = true
profile.bullet_spread_accuracy        = 0.0
profile.flinch_resistance             = 1.0
profile.hitbox_radius_multiplier      = 3.5
profile.silent_aimbot_v2              = true
profile.wall_penetrate_range          = 500
profile.instant_target_acquisition    = true
profile.damage_floor_max              = 10000

-- MP & BR Perk Overclock
profile.dead_silence_always_active    = true
profile.ghost_uav_immunity_active     = true
profile.quick_fix_instant_heal        = true
profile.kinetic_armor_floor           = 10000
profile.armor_plate_repair_speed      = 10.0
profile.safe_zone_damage_immunity     = true

-- Movement & Animation Overdrive
profile.slide_cancel_speed_mult       = 12.0
profile.bunny_hop_momentum_keep       = true
profile.ads_transition_zero_delay     = true
profile.instant_chambering_swap       = true
profile.fast_scope_ads_speed          = 10.0

-- Anti-Cheat Stealth Bypass Flags
profile.bypass_inotify_cloaking       = true
profile.bypass_timestamp_restore      = true
profile.bypass_selinux_preserve       = true
profile.bypass_session_micro_drift    = true

return profile

