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

return profile
