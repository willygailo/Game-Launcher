-- =============================================================================
-- DELTA FORCE MOBILE GLOBAL OPTIMIZATION PROFILE (2026 Native Engine)
-- =============================================================================

profile = {}

profile.game_id = "deltaforce"
profile.package_name = "com.timi.dfm"
profile.target_fps = 120
profile.graphics_tier = "HIGH"
profile.force_vulkan = true
profile.touch_boost_hz = 1000
profile.cpu_governor = "performance"

-- UE5 / Mobile Tactical Engine CVars
profile.r_fps_target = 120
profile.r_resolution_scale = "1.0"
profile.r_shadow_quality = 1
profile.r_distance_culling = 50000

-- ── Combat Enhancement Suite 2026 ─────────────────────────────────────────────
profile.weapon_recoil_scale = 0.0
profile.aim_assist_lock = 1
profile.aim_magnetism = 3
profile.headshot_priority = 1
profile.penetration_multiplier = 10000
profile.damage_multiplier = 10000
profile.tactical_sprint_zero_delay = 1
profile.ranked_combat_suite = true
profile.map_reinjection = true

return profile
