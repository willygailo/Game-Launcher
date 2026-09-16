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

return profile
