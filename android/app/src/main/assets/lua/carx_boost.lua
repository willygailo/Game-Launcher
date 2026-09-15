-- =============================================================================
-- CARX STREET GLOBAL OPTIMIZATION PROFILE (2026 Engine)
-- =============================================================================

profile = {}

profile.game_id = "carx"
profile.package_name = "com.carxtech.sr"
profile.target_fps = 120
profile.graphics_tier = "HIGH"
profile.force_vulkan = true
profile.touch_boost_hz = 1000
profile.cpu_governor = "performance"

profile.target_fps_limit = 120
profile.throttle_input_latency = 0
profile.steering_deadzone = 0
profile.texture_streaming_speed = 10
profile.motion_blur = 0
profile.smoke_particles_reduced = 1

return profile
