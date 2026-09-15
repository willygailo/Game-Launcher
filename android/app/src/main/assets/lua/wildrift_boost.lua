-- =============================================================================
-- LEAGUE OF LEGENDS: WILD RIFT GLOBAL OPTIMIZATION PROFILE (2026 Engine)
-- =============================================================================

profile = {}

profile.game_id = "wildrift"
profile.package_name = "com.riotgames.league.wildrift"
profile.target_fps = 120
profile.graphics_tier = "ULTRA"
profile.force_vulkan = true
profile.touch_boost_hz = 1000
profile.cpu_governor = "performance"

profile.fps_limit = 120
profile.resolution_scale = 1.0
profile.smart_cast_instant = 1
profile.target_priority_hero = 1
profile.joystick_deadzone = 0
profile.skill_delay_zero = 1
profile.ranked_combat_suite = true
profile.map_reinjection = true

return profile
