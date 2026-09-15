-- =============================================================================
-- BLOOD STRIKE GLOBAL OPTIMIZATION PROFILE (2026 Native Engine)
-- =============================================================================

profile = {}

profile.game_id = "bloodstrike"
profile.package_name = "com.netease.bloodstrike"
profile.target_fps = 144
profile.graphics_tier = "ULTRA"
profile.force_vulkan = true
profile.touch_boost_hz = 1000
profile.cpu_governor = "performance"

-- Engine & Render
profile.max_fps = 144
profile.frame_pacing = 1
profile.anti_aliasing = 0
profile.shadow_quality = 0

-- ── Combat Enhancement Suite 2026 ─────────────────────────────────────────────
-- Movement & Tactical Slide
profile.tactical_slide_boost = 1
profile.slide_cancel_delay = 0
profile.sprint_to_fire_delay = 0
profile.jump_fatigue_disabled = 1

-- Zero Recoil & Spread
profile.weapon_recoil_scale = 0.0
profile.horizontal_recoil = 0.0
profile.vertical_recoil = 0.0
profile.bullet_spread = 0.0

-- Aim Assist & Snapping
profile.aim_assist_tier = 3
profile.aim_magnetism = 3
profile.head_tracking_priority = 1
profile.ads_transition_delay = 0

-- Damage & Hit Reg
profile.damage_boost = true
profile.damage_multiplier = 10000
profile.headshot_multiplier = 5.0
profile.hit_reg_sync_rate = 1000

-- Match & Map Coverage
profile.ranked_combat_suite = true
profile.map_reinjection = true

return profile
