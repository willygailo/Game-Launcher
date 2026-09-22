-- ============================================================================
-- MLBB Hero Script: Paquito | ID: 167 | Role: Fighter
-- Season 42 "Starward Decade" — 2026 New Patch Method
-- Script Team Pro ML Format — per-hero modifier dispatch
-- ============================================================================
script = {}

script.hero_id   = 167
script.hero_name = "Paquito"
script.role      = "Fighter"

-- ── Damage Modifiers ────────────────────────────────────────────────────────
script.s1_damage    = 10000   -- Champ Stance
script.s2_damage    = 10000   -- Jab
script.ult_damage   = 10000   -- KO Combo
script.basic_damage = 10000   -- Basic Attack true damage override

-- ── Cooldown Overrides (seconds) ────────────────────────────────────────────
script.s1_cd  = 0.001
script.s2_cd  = 0.001
script.ult_cd = 0.001

-- ── Attack Range ────────────────────────────────────────────────────────────
script.attack_range = 9999    -- Max melee/ranged range extension

-- ── Speed Modifiers ─────────────────────────────────────────────────────────
script.move_speed   = 500     -- Movement speed override
script.attack_speed = 10.0    -- Attack animation speed multiplier
script.anim_speed   = 10.0    -- General animation speed multiplier

-- ── Vision Range ────────────────────────────────────────────────────────────
script.vision_range = 9999    -- Hero sight radius max

-- ── 2026 NEW: Game Speed Multiplier ─────────────────────────────────────────
script.game_speed   = 2.0     -- Game tick rate multiplier (2026 new command)

-- ── Camera Lock (drone view pinned to this hero) ─────────────────────────────
script.camera_lock   = true
script.camera_height = 5.0    -- Drone elevation for pinned camera lock

return script
