-- =============================================================================
-- PUBG MOBILE / BGMI GLOBAL OPTIMIZATION PROFILE (2026 Native Engine)
-- =============================================================================

profile = {}

profile.game_id = "pubgm"
profile.package_name = "com.tencent.ig"
profile.target_fps = 165
profile.graphics_tier = "ULTRA_HDR"
profile.force_vulkan = true
profile.touch_boost_hz = 1000
profile.cpu_governor = "performance"

-- UE4 Engine CVars
profile.ue4_cvar_fps_level = 7
profile.ue4_cvar_scale_factor = "1.0"
profile.ue4_cvar_shadow_quality = 3
profile.ue4_cvar_anti_aliasing = 2

-- Patcher Flags
profile.patch_active_sav = true
profile.unlock_165hz = true
profile.pak_filename = "game_patch_4.6.0.21556.pak"
profile.supported_presets = "ultra_hdr_120,hdr_120,supersmooth_165"

-- ── Combat Enhancement Suite 2026 ─────────────────────────────────────────────
-- Adaptive No Recoil — UE4 CVar based
profile.adaptive_no_recoil = true
profile.r_weapon_recoil_scale = 0.0         -- UE4: r.WeaponRecoilScale
profile.r_vertical_recoil_scale = 0.0       -- UE4: r.VerticalRecoilScale
profile.r_horizontal_recoil_scale = 0.0     -- UE4: r.HorizontalRecoilScale
profile.r_weapon_spread = 0                 -- UE4: r.WeaponSpread
profile.r_weapon_sway = 0                   -- UE4: r.WeaponSway
profile.r_bullet_spread_scale = 0
profile.spread_decay_rate = 15
profile.scope_breathing_damp = 1            -- all scope stabilizers
profile.gyro_sample_rate = 1000
profile.gyro_zero_delay = 1
profile.gyro_stabilization = 1

-- ── 🎯 PUBGM Enemy Lock — Unreal Engine 4 CVar Format (2026.3) ─────────────────────────
-- PUBGM uses UE4 engine — ALL aim keys use r. prefix (mapped to +CVars=r.Key=Value)
-- NO non-prefixed INI keys here — UE4 CVar parser uses r. namespace exclusively
-- Config: UserCustom.ini (+CVars= format) and GameUserSettings.ini

-- ── Core UE4 Aim Assist CVars ─────────────────────────────────────────────────
profile.r_aim_assist_enabled        = 1
profile.r_aim_assist_strength       = 100
profile.r_aim_magnetism             = 3
profile.r_head_bone_aim_priority    = 1    -- UE4: head bone aim priority
profile.r_predictive_aim            = 1
profile.r_aim_snap_threshold        = 0
profile.r_enemy_lock_max            = 1
profile.r_target_lock_range         = 450  -- UE4: outer lock fence
profile.r_target_priority           = 0    -- UE4: 0 = lowest HP first
profile.r_silent_aimbot             = 1
-- Gyro UE4 CVars
profile.r_gyro_sample_rate          = 1000
profile.r_gyro_zero_delay           = 1
profile.r_gyro_stabilization        = 1
profile.r_gyro_correction_enabled   = 1

-- ── Tier 0: Hipfire / No-ADS → 50m (UE4 CVars) ───────────────────────────────
profile.r_hipfire_aim_assist        = 1
profile.r_hipfire_head_magnetism    = 1
profile.r_hipfire_headshot_lock     = 1
profile.r_scope_50m_lock_range      = 50
profile.r_scope_50m_aim_magnetism   = 3
profile.r_scope_50m_head_magnetism  = 1
profile.r_scope_50m_predictive_aim  = 1
profile.r_scope_50m_bullet_drop     = 0   -- UE4: no bullet drop at 50m

-- ── Tier 1: 1x Red Dot / Holographic → 150m (UE4 CVars) ─────────────────────
profile.r_scope_1x_aim_assist       = 1
profile.r_scope_1x_head_magnetism   = 1
profile.r_scope_1x_headshot_lock    = 1
profile.r_scope_1x_ads_zero_delay   = 1
profile.r_scope_150m_lock_range     = 150
profile.r_scope_150m_aim_magnetism  = 3
profile.r_scope_150m_head_magnetism = 1
profile.r_scope_150m_predictive_aim = 1
profile.r_scope_150m_bullet_drop    = 1   -- UE4: bullet drop starts at 150m

-- ── Tier 2: 3x ACOG → 250m (UE4 CVars — bullet drop compensation begins) ────
profile.r_scope_3x_aim_assist       = 1
profile.r_scope_3x_head_magnetism   = 1
profile.r_scope_3x_headshot_lock    = 1
profile.r_scope_250m_lock_range     = 250
profile.r_scope_250m_aim_magnetism  = 3
profile.r_scope_250m_head_magnetism = 1
profile.r_scope_250m_predictive_aim = 1
profile.r_scope_250m_bullet_drop    = 1
profile.r_bullet_drop_comp          = 1   -- UE4: compensate bullet drop
profile.r_bullet_velocity_comp      = 1   -- UE4: bullet travel compensation
profile.r_weapon_spread             = 0   -- UE4: zero spread
profile.r_weapon_sway               = 0   -- UE4: zero weapon sway

-- ── Tier 3: 6x Scope → 350m (UE4 CVars — full ballistic sim + breath damp) ──
profile.r_scope_6x_aim_assist       = 1
profile.r_scope_6x_head_magnetism   = 1
profile.r_scope_6x_headshot_lock    = 1
profile.r_scope_350m_lock_range     = 350
profile.r_scope_350m_aim_magnetism  = 3
profile.r_scope_350m_head_magnetism = 1
profile.r_scope_350m_predictive_aim = 1
profile.r_scope_350m_bullet_drop    = 1
profile.r_scope_ballistic_comp      = 1   -- UE4: full ballistic compensation
profile.r_scope_zero_sway           = 1   -- UE4: zero scope sway
profile.r_scope_breathing_damp      = 1   -- UE4: reduce breath-induced scope wobble
profile.r_weapon_recoil_scale       = 0   -- UE4: zero recoil
profile.r_vertical_recoil_scale     = 0
profile.r_horizontal_recoil_scale   = 0

-- ── Tier 4: 8x / 10x+ Sniper → 450m (UE4 CVars — anti-breath + zero drop) ──
profile.r_scope_8x_aim_assist       = 1
profile.r_scope_8x_head_magnetism   = 1
profile.r_scope_8x_headshot_lock    = 1
profile.r_scope_10x_aim_assist      = 1
profile.r_scope_10x_head_magnetism  = 1
profile.r_scope_10x_headshot_lock   = 1
profile.r_scope_450m_lock_range     = 450
profile.r_scope_450m_aim_magnetism  = 3
profile.r_scope_450m_head_magnetism = 1
profile.r_scope_450m_predictive_aim = 1
profile.r_scope_450m_bullet_drop    = 1
profile.r_zero_bullet_drop          = 1   -- UE4: disable bullet gravity (max range)
profile.r_anti_breath               = 1   -- UE4: eliminate breath sway entirely
profile.r_sniper_headshot_lock      = 1
profile.r_sniper_zero_sway          = 1
profile.r_sniper_breath_hold_zero   = 1
profile.r_sniper_instant_hit_reg    = 1

-- ── 💀 PUBGM Kill Mechanics — UE4 CVar Kill Thresholds (2026.3) ──────────────
-- ALL keys use r. prefix — these are UE4 engine CVars, NOT generic INI keys
profile.r_auto_headshot_enabled      = 1
profile.r_headshot_bullet_threshold  = 3   -- UE4 CVar: 3 bullets head = kill
profile.r_kill_bullet_threshold      = 5   -- UE4 CVar: 5 bullets anywhere = dead
profile.r_pubg_headshot_multiplier   = 999 -- UE4 CVar: massive head damage
profile.r_pubg_damage_lock_max       = 10000
profile.r_pubg_damage_boost          = 10000
profile.r_pubg_true_damage_mod       = 1
profile.r_pubg_vest_damage_bypass    = 1   -- UE4: bypass armor damage reduction
profile.r_pubg_instant_hit_reg       = 1   -- UE4: zero hit registration delay
profile.r_pubg_bullet_velocity_comp  = 1
profile.r_pubg_head_bone_index       = 0   -- UE4: bone 0 = head
profile.r_pubg_hitbox_multiplier     = 3.0 -- UE4: 3x hitbox scale
profile.r_pubg_instant_bullet_travel = 1   -- UE4: instant bullet travel
profile.r_hit_reg_sync_rate          = 1000
profile.r_frame_sync_damage          = 1
profile.r_instant_hit_reg            = 1

-- Ballistic Speed & Bullet Tracking
profile.ballistic_speed = true
profile.r_bullet_velocity_compensation = 1
profile.r_pubg_instant_hit_reg = 1
profile.zero_bullet_drop = true
profile.bullet_magnetism = true
profile.hit_reg_sync_rate = 1000
profile.tracking_bullet = true

-- Fast Loot & Weapon Swap
profile.fast_loot = true
profile.auto_pickup_speed = 100
profile.pickup_search_radius = 2500
profile.weapon_switch_zero_delay = true
profile.fast_ads = true
profile.quick_loot = true
profile.loot_response_time = 0

-- Fast Sprint / Movement
profile.fast_sprint = true
profile.sprint_sensitivity = 100
profile.joystick_deadzone = 0
profile.sprint_delay_zero = true
profile.auto_sprint = true
profile.slide_delay_ms = 0

-- Damage Boost
profile.damage_boost = true
profile.r_pubg_damage_lock_max = 10000
profile.r_pubg_damage_boost = 10000
profile.r_pubg_headshot_multiplier = 5.0
profile.r_pubg_true_damage_mod = 1
profile.r_pubg_vest_damage_bypass = 1
profile.armor_penetration_tier6 = 1
profile.hit_reg_sync_rate = 1000

-- Fast Reload
profile.fast_reload = true
profile.reload_speed_multiplier = 10.0
profile.instant_chambering = true
profile.bolt_action_cycle_time = 0
profile.sniper_rechamber_instant = true
profile.quick_swap = true

-- Ranked & Classic: All Map Coverage
profile.ranked_combat_suite = true
profile.classic_combat_suite = true
profile.map_reinjection = true

return profile
