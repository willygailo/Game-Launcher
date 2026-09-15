-- =============================================================================
-- STANDOFF 2 GLOBAL OPTIMIZATION PROFILE (2026 Engine)
-- =============================================================================

profile = {}

profile.game_id = "standoff2"
profile.package_name = "com.axlebolt.standoff2"
profile.target_fps = 120
profile.graphics_tier = "HIGH"
profile.force_vulkan = true
profile.touch_boost_hz = 1000
profile.cpu_governor = "performance"

profile.fps_limit = 120
profile.tick_rate = 128
profile.zero_spread = 1
profile.crosshair_dynamic = 0
profile.headshot_magnetism = 1
profile.ranked_combat_suite = true
profile.map_reinjection = true

return profile
