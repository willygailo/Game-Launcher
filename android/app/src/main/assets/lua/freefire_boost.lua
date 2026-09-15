-- =============================================================================
-- FREE FIRE / FREE FIRE MAX GLOBAL OPTIMIZATION PROFILE (2026 Native Engine)
-- =============================================================================

profile = {}

profile.game_id = "freefire"
profile.package_name = "com.dts.freefireth"
profile.target_fps = 120
profile.graphics_tier = "ULTRA"
profile.force_vulkan = true
profile.touch_boost_hz = 1000
profile.cpu_governor = "performance"

-- Engine & Display Keys
profile.high_fps_mode = 3
profile.shadow = 0
profile.high_res = 1
profile.vivid_graphics = 1
profile.zero_corruption = true

-- ── Combat Enhancement Suite 2026 ─────────────────────────────────────────────
-- Auto Drag Headshot Magnetism
profile.drag_headshot_assist = 1
profile.headshot_sensitivity_multiplier = 3.0
profile.crosshair_bloom = 0
profile.spread_zero = 1
profile.recoil_control_assist = 1
profile.head_magnetism = 1
profile.aim_bone_target = 0
profile.touch_polling_rate = 1000
profile.touch_zero_delay = 1

-- Instant 360 Gloo Wall
profile.instant_gloo_wall = 1
profile.gloo_wall_deploy_delay = 0
profile.fast_gloo_crouch = 1
profile.reload_speed_boost = 10.0
profile.weapon_switch_zero_delay = 1
profile.sprint_delay_zero = 1

-- Damage & Attack Speed
profile.damage_boost = true
profile.damage_multiplier = 10000
profile.gun_damage_multiplier = 10000
profile.fire_rate_overclock = 10000
profile.fire_rate_boost = 10.0
profile.auto_headshot_damage = 10000
profile.headshot_multiplier = 5.0
profile.quick_shot_zero_delay = 1

-- Ranked & Matchmaking
profile.ranked_combat_suite = true
profile.classic_combat_suite = true
profile.map_reinjection = true

return profile
