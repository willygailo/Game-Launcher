-- =============================================================================
-- VALORANT MOBILE GLOBAL OPTIMIZATION PROFILE (2026 Engine)
-- =============================================================================

profile = {}

profile.game_id = "valorant"
profile.package_name = "com.riotgames.valorant.mobile"
profile.target_fps = 120
profile.graphics_tier = "COMPETITIVE"
profile.force_vulkan = true
profile.touch_boost_hz = 1000
profile.cpu_governor = "performance"

profile.fps_lock = 120
profile.head_level_crosshair_assist = 1
profile.first_bullet_spread = 0.0
profile.walking_accuracy_lock = 1
profile.ranked_combat_suite = true
profile.map_reinjection = true

return profile
