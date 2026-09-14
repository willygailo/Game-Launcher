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

return profile
