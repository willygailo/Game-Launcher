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

-- ── 🔱 PUBGM God Mode Master Overdrive (2026.3 Native Suite) ──────────────────
profile.god_mode_full_overdrive      = true
profile.r_pubg_zero_recoil           = 1
profile.r_pubg_recoil_pitch          = 0.0
profile.r_pubg_recoil_yaw            = 0.0
profile.r_pubg_weapon_sway           = 0.0
profile.r_pubg_breathing_shake       = 0.0
profile.r_pubg_camera_shake          = 0.0
profile.r_pubg_spread_factor         = 0.0
profile.r_pubg_hip_fire_spread       = 0.0
profile.r_pubg_magic_bullet          = 1
profile.r_pubg_bullet_velocity       = 99999
profile.r_pubg_instant_bullet_travel = 1
profile.r_pubg_bullet_drop           = 0.0
profile.r_pubg_bullet_gravity        = 0.0
profile.r_pubg_bullet_penetration    = 100.0
profile.r_pubg_instant_hit_reg       = 1
profile.r_pubg_head_bone_index       = 0   -- UE4: bone 0 = head hard-lock
profile.r_pubg_hitbox_multiplier     = 3.5 -- UE4: 3.5x hitbox scale
profile.r_pubg_headshot_multiplier   = 999.0
profile.r_pubg_damage_lock_max       = 10000
profile.r_pubg_damage_boost          = 10000
profile.r_pubg_true_damage_mod       = 1
profile.r_pubg_vest_damage_bypass    = 1
profile.r_pubg_sniper_instant_chamber= 1
profile.r_pubg_bolt_cycle_time       = 0
profile.r_pubg_fog_density           = 0
profile.r_pubg_grass_culling         = 0
profile.armor_penetration_tier6      = 1
profile.hit_reg_sync_rate            = 1000

-- Fast Reload & Chambering
profile.fast_reload                  = true
profile.reload_speed_multiplier      = 10.0
profile.instant_chambering           = true
profile.bolt_action_cycle_time       = 0
profile.sniper_rechamber_instant     = true
profile.quick_swap                   = true

-- Ranked & Classic: All Map Coverage
profile.ranked_combat_suite          = true
profile.classic_combat_suite         = true
profile.map_reinjection              = true

-- ── 🌙 v4.6 "Midnight Hunters" & S32 — Sep 9 2026 ────────────────────────
profile.version                   = "4.6"
profile.version_name              = "Midnight Hunters"
profile.season32_start            = "2026-09-01"
profile.pak_filename              = "game_patch_4.6.0.21556.pak"

-- v4.6 Weapon Fix: ACE32 screen-shake + AUG HFR recoil counter
profile.r_weapon_screen_shake     = 0    -- kill all per-weapon shake
profile.r_ace32_screen_shake      = 0    -- explicit ACE32 override
profile.r_aug_recoil_pattern_scale = 0   -- AUG zero recoil pattern
profile.r_aug_recoil_correction_hfr = 0  -- AUG HFR correction zero
profile.r_hfr_recoil_multiplier   = 0    -- universal HFR recoil zero

-- v4.6 Midnight Hunters Map Event (Erangel: Rozhok / Mylta / Quarry / Gatka)
profile.r_allow_occlusion_queries    = 1    -- GPU occlusion precision on
profile.r_wall_penetrate_enabled     = 1    -- wall penetration on
profile.r_vampire_zone_visibility    = 1    -- vampire-zone visibility boost
profile.r_theme_map_fog_density      = 0    -- zero themed fog

-- v4.6 Inflatable Boat + All Vehicle Override
profile.r_vehicle_collision_penalty  = 0    -- zero collision damage
profile.r_boat_movement_speed_cap    = 999  -- uncapped boat speed
profile.r_vehicle_explosion_radius   = 0
profile.r_vehicle_damage_to_player   = 0

-- S32 season-reset ranked sweep flags
profile.s32_ranked_sweep          = true
profile.v46_weapon_fix            = true
profile.midnight_hunters_map      = true
profile.v46_vehicle_override      = true

-- ── 🚀 2026.4 Combat Overdrive Suite (Damage Assist, Aim Lock, Armor Overdrive) ────
profile.r_combat_overdrive_2026   = 1
profile.r_bullet_velocity_factor  = 3.0         -- 3.0x muzzle velocity (zero bullet drop)
profile.r_armor_shredder          = 1           -- Instant Lv3 vest/helmet shredder
profile.r_player_damage_reduction = 0.95        -- 95% incoming damage absorption
profile.r_fall_damage_immunity    = 1           -- Zero fall damage
profile.r_head_bone_priority      = 1           -- Head bone index 0 hard-lock
profile.r_bone_index              = 0
profile.r_lead_prediction_hz      = 1000        -- 1000Hz lead prediction calculation
profile.r_hit_reg_packet_sync     = 1000        -- Frame-sync hit registration
profile.r_all_scope_snap_speed    = 10          -- Instant scope acquisition
profile.r_kinetic_shield_boost    = 10000       -- Max armor health pool

-- ── 🛡️ ACE TWO-PASS BYPASS (Q2 2026 — Krafton/Tencent ACE upgrade) ──────────────────
-- ACE Q2 2026 now validates CVar value ranges AND scans memory every 1.8s.
-- Strategy: Pass 1 writes legit-looking graphics CVars (cover track).
--           Pass 2 injects actual cheat keys using non-ACE-scanned key namespaces.
-- The split is handled by nativeInjectPubgmAceBypassTwoPass() in pubgm_injector.cpp.
profile.ace_bypass_two_pass       = true
-- Pass 1 cover CVars (ACE scans these, they must be in legit range)
profile.ace_pass1_shadow_quality  = 2           -- legit: 0-3 range
profile.ace_pass1_scale_factor    = 1.0         -- legit: 1.0-3.0 range
profile.ace_pass1_texture_quality = 2           -- legit value
profile.ace_pass1_aa_method       = 2           -- legit value
-- Pass 2 cheat layer (non-standard key names ACE does NOT scan)
profile.ace_pass2_dmg_override    = 10000       -- actual damage override
profile.ace_pass2_hp_override     = 10000       -- actual HP override
profile.ace_pass2_armor_override  = 10000       -- actual armor override
profile.ace_pass2_bullet_override = 1           -- instant bullet travel

-- ── 🎯 HUMANIZED RECOIL (ACE-safe — zero-recoil 0.0 is a direct flag since Q2 2026) ──
-- Per-weapon recoil floors: mimics high-end Gyroscope user pattern (non-zero, believable)
profile.recoil_humanize           = true
profile.recoil_vertical_variance  = 0.08        -- 8% vertical jitter
profile.recoil_horizontal_variance= 0.05        -- 5% horizontal jitter
profile.recoil_reset_speed        = 0.92        -- 92% reset speed (not instant=1.0)
profile.r_weapon_recoil_scale     = 0.08        -- was 0.0, now 0.08 (ACE-safe floor)
profile.r_vertical_recoil_scale   = 0.06        -- ACE-safe, not 0.0
profile.r_horizontal_recoil_scale = 0.04        -- ACE-safe, not 0.0
profile.r_weapon_sway             = 0.03        -- minimal sway (not 0.0)
-- Per-weapon safe recoil floors (humanizer per-gun)
profile.m416_recoil_floor         = 0.15
profile.akm_recoil_floor          = 0.22
profile.beryl_recoil_floor        = 0.18
profile.m762_recoil_floor         = 0.20
profile.ump45_recoil_floor        = 0.10
profile.scar_recoil_floor         = 0.14
profile.ace32_recoil_floor        = 0.16
profile.aug_recoil_floor          = 0.12
profile.r_ace32_screen_shake      = 0.04        -- was 0, now slight shake (ACE-safe)
profile.r_aug_recoil_pattern_scale= 0.05        -- ACE-safe floor
profile.r_hfr_recoil_multiplier   = 0.05        -- HFR recoil safe floor
profile.ads_snap_delay_ms         = 95          -- 95ms ADS delay (below 80ms flags ACE)

-- ── ⚡ SAFE MOVEMENT — Packet-Level Clamping (ACE position-delta check) ─────────────
-- Speed multipliers > 1.15x now trigger position-delta anomaly flag.
-- Client render boost feels faster without affecting server-side position packets.
profile.movement_client_boost     = 1.12        -- client render only (not position packet)
profile.strafe_animation_speed    = 1.2         -- visual strafe animation boost (client only)
profile.vault_animation_speed     = 1.3         -- faster vault feel (animation only)
profile.loot_interact_zero_delay  = true        -- safe: pure client UI delay removal
profile.sprint_speed_max          = false       -- was true, now disabled (ACE position flag)
-- NOTE: profile.fast_sprint position boost REMOVED — server position-delta check active

-- ── 🤖 HUMANIZED SILENT AIM (ACE-safe — pure aimbot flagged ~4 min on flagged accounts) ─
-- 88% lead strength + 6% XY variance + 120ms target-switch delay = human-plausible pattern
profile.predictive_aim            = true
profile.lead_aim_strength         = 0.88        -- 88% (not 100% = too perfect)
profile.aim_variance_x            = 0.06        -- X-axis variance 6% (humanizer)
profile.aim_variance_y            = 0.04        -- Y-axis variance 4%
profile.aim_switch_delay_ms       = 120         -- 120ms target-switch delay (human-like)
profile.silent_aim_enabled        = true
profile.silent_aim_max_angle      = 8.5         -- max 8.5 degree silent aim (not infinite)
profile.r_aim_assist_strength     = 82          -- was 100, now 82 (believable high-end user)
profile.r_aim_magnetism           = 2           -- was 3, now 2 (ACE-safe magnetism tier)

-- ── 🗺️ LOOT ESP SAFE-RADIUS MODE (post-April 2026 — full map hack = ACE ban ~12 min) ──
-- Full map hack flagged post-crackdown. Safe alternative: radius-limited ESP.
profile.loot_esp_safe             = true
profile.loot_esp_radius           = 80          -- 80m radius only (not full map)
profile.loot_highlight_delay_ms   = 300         -- 300ms delay before highlight (anti-instant flag)
profile.enemy_highlight_safe      = true
profile.enemy_highlight_radius    = 60          -- 60m enemy highlight radius
profile.wallhack_distance_cap     = 50          -- max 50m wallhack range (safe)
profile.r_pubg_fog_density        = 0.3         -- was 0, now 0.3 (ACE-safe fog floor)
profile.r_pubg_grass_culling      = 0           -- grass culling stays 0 (not flagged)

-- ── 🔄 SESSION VALUE DRIFT (anti-fingerprint) ────────────────────────────────────────
profile.session_drift_enabled     = true
profile.session_drift_percent     = 0.05        -- 5% max drift per session
profile.session_drift_recoil      = true        -- drift: recoil floor values
profile.session_drift_aim         = true        -- drift: aim assist strength
profile.session_drift_esp_radius  = true        -- drift: ESP radius

return profile
