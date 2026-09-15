-- =============================================================================
-- ARENA BREAKOUT GLOBAL OPTIMIZATION PROFILE (2026 Native Engine)
-- =============================================================================

profile = {}

profile.game_id = "arenabreakout"
profile.package_name = "com.proximabeta.mf.uamo"
profile.target_fps = 120
profile.graphics_tier = "BALANCED"
profile.force_vulkan = true
profile.touch_boost_hz = 1000
profile.cpu_governor = "performance"

-- UE4 Engine CVars
profile.ue4_fps_cap = 120
profile.audio_spatial_enhancement = 1
profile.footstep_clarity_boost = 1

-- ── Combat Enhancement Suite 2026 ─────────────────────────────────────────────
profile.recoil_compensation = 0.0
profile.weapon_sway_zero = 1
profile.aim_stabilization = 1
profile.armor_penetration_level6 = 1
profile.flesh_damage_multiplier = 3.0
profile.ads_instant_time = 0
profile.quick_heal_speed = 10.0
profile.ranked_combat_suite = true
profile.map_reinjection = true

return profile
