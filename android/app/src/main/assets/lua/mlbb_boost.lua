-- =============================================================================
-- MOBILE LEGENDS: BANG BANG (MLBB) GLOBAL OPTIMIZATION PROFILE (2026 Engine)
-- =============================================================================

profile = {}

profile.game_id = "mlbb"
profile.package_name = "com.mobile.legends"
profile.target_fps = 165
profile.graphics_tier = "ULTRA"
profile.force_vulkan = false
profile.touch_boost_hz = 1000
profile.cpu_governor = "performance"

-- PlayerPrefs Flags
profile.high_fps_mode = 3
profile.resolution_high = 1
profile.outline = 1
profile.shadow = 1
profile.zero_corruption = true

-- ── Combat Enhancement Suite 2026 ─────────────────────────────────────────────
-- No Recoil / Adaptive Recoil Compensation
profile.no_recoil = true
profile.adaptive_recoil_scale = 0.0          -- 0.0 = full zero recoil
profile.weapon_sway = 0
profile.bullet_spread_scale = 0

-- Aim Assist & Aim Lock
profile.aim_lock = true
profile.adaptive_aim_strength = 100          -- 0-100, max magnetism
profile.aim_magnetism = 3                    -- max tier (MLBB hero lock tier)
profile.head_magnetism = 1                   -- prioritize head bone
profile.aim_snap_speed = 10                  -- instant snap
profile.aim_smooth_factor = 0               -- zero smooth = hard lock
profile.aim_predict = true                   -- predictive tracking

-- Damage Boost
profile.damage_boost = true
profile.damage_multiplier = 10000            -- max damage lock
profile.true_damage_boost = true
profile.crit_rate_boost = 100                -- guaranteed crit
profile.penetration_boost = true             -- armor pen max
profile.hit_reg_sync_rate = 1000            -- 1000Hz hit registration

-- Fast Attack Speed
profile.fast_attack_speed = true
profile.attack_speed_boost = 10000
profile.fire_rate_multiplier = 10.0

-- Fast Skill Cooldown
profile.fast_skill_cd = true
profile.skill_cd_ratio = 0.001              -- near-instant reset
profile.ult_cd_reduction = 1.0
profile.zero_skill_cost = true
profile.max_ult_charge = true

-- Fast Run / Movement
profile.fast_run = true
profile.movement_speed_boost = true
profile.joystick_zero_deadzone = true
profile.zero_input_lag = true
profile.touch_zero_delay = true

-- Fast Reload (Beatrix / gun heroes)
profile.fast_reload = true
profile.reload_speed_multiplier = 10.0
profile.instant_chambering = true
profile.quick_swap = true

-- Ranked & Classic: All Map Coverage
profile.ranked_combat_suite = true
profile.classic_combat_suite = true
profile.map_reinjection = true               -- re-inject on every new map load

return profile
