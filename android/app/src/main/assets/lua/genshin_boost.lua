-- =============================================================================
-- GENSHIN IMPACT GLOBAL OPTIMIZATION PROFILE (2026 Engine)
-- =============================================================================

profile = {}

profile.game_id = "genshin"
profile.package_name = "com.miHoYo.GenshinImpact"
profile.target_fps = 120
profile.graphics_tier = "HIGH"
profile.force_vulkan = true
profile.touch_boost_hz = 1000
profile.cpu_governor = "performance"

profile.fps_uncap = 120
profile.render_resolution = 1.0
profile.shadow_quality = 1
profile.vulkan_async_compute = 1
profile.shader_prewarm = 1
profile.camera_smoothing_zero = 1
profile.zero_input_latency = 1

return profile
