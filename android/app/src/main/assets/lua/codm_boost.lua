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

-- ── Combat Enhancement Suite 2026 ─────────────────────────────────────────────
-- Adaptive No Recoil — per weapon category
profile.adaptive_no_recoil = true
profile.ar_recoil_scale = 0.0               -- AR: full zero
profile.smg_recoil_scale = 0.0              -- SMG: full zero
profile.sniper_recoil_scale = 0.0           -- Sniper: zero sway
profile.lmg_recoil_scale = 0.0             -- LMG: full zero
profile.shotgun_recoil_scale = 0.0
profile.dmr_recoil_scale = 0.0
profile.pistol_recoil_scale = 0.0
profile.spread_decay_rate = 15              -- fast spray recovery
profile.bullet_spread_scale = 0
profile.weapon_sway = 0
profile.gyro_stabilization = 1
profile.gyro_sample_rate = 1000

-- Aim Lock & Aimbot
profile.aim_lock = true
profile.scope_headshot = true               -- all scope tiers snap to head
profile.aim_magnetism = 3
profile.head_bone_priority = 1
profile.aim_snap_threshold = 0
profile.ads_zero_delay = true
profile.predictive_aim = true
profile.silent_aimbot = true               -- lock-on without visible snap

-- Fast Reload
profile.fast_reload = true
profile.reload_speed_multiplier = 10.0
profile.sleight_of_hand = true
profile.instant_chambering = true
profile.bolt_cycle_instant = true
profile.quick_swap = true
profile.draw_speed_boost = 10.0

-- Damage Boost
profile.damage_boost = true
profile.damage_multiplier = 10000
profile.true_damage_boost = true
profile.headshot_multiplier = 5.0
profile.crit_rate_boost = 100
profile.penetration_boost = true
profile.vest_damage_bypass = true
profile.hit_reg_sync_rate = 1000

-- Fast Run / Movement
profile.fast_run = true
profile.sprint_speed_max = true
profile.slide_speed_boost = true
profile.joystick_zero_deadzone = true
profile.zero_input_lag = true

-- Ranked & Classic: All Map Coverage
profile.ranked_combat_suite = true
profile.classic_combat_suite = true
profile.map_reinjection = true

return profile
