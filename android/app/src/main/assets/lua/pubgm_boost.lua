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

-- Aim Lock — UE4 CVar + custom keys
profile.aim_lock = true
profile.r_aim_assist_enabled = 1
profile.r_aim_assist_strength = 100
profile.r_aim_magnetism = 3
profile.r_head_bone_aim_priority = 1
profile.r_predictive_aim = 1
profile.head_magnetism = 1
profile.ads_zero_delay = true

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
