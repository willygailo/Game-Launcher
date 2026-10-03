// language: C++, file: combat_system.hpp, runtime: C++17/20, target: Android NDK (ARM64 / ARMv7 / x86_64)
#pragma once

#include <algorithm>
#include <chrono>
#include <cmath>
#include <cstdint>
#include <limits>
#include <unordered_map>
#include <vector>

namespace combat {

// ============================================================================
// 1. ATTRIBUTES / STAT SYSTEM (DATA & LOGIC SEPARATION)
// ============================================================================

enum class ModifierType : uint8_t {
    Flat,       // Additive flat bonus (e.g., +25 Damage)
    PercentAdd, // Additive percentage (e.g., +10% + 15% = +25%)
    PercentMult // Compound multiplicative percentage (e.g., * 1.10)
};

struct StatModifier {
    float value;
    ModifierType type;
    uint32_t source_id;
};

struct Stat {
    float base_value = 0.0f;
    mutable float cached_value = 0.0f;
    mutable bool is_dirty = true;
    std::vector<StatModifier> modifiers;

    Stat() = default;
    explicit Stat(float base) : base_value(base), cached_value(base), is_dirty(false) {}

    void AddModifier(float val, ModifierType type, uint32_t src_id = 0) {
        modifiers.push_back({val, type, src_id});
        is_dirty = true;
    }

    void RemoveModifiersBySource(uint32_t src_id) {
        auto it = std::remove_if(modifiers.begin(), modifiers.end(), [src_id](const StatModifier& mod) {
            return mod.source_id == src_id;
        });
        if (it != modifiers.end()) {
            modifiers.erase(it, modifiers.end());
            is_dirty = true;
        }
    }

    void ClearModifiers() {
        if (!modifiers.empty()) {
            modifiers.clear();
            is_dirty = true;
        }
    }

    // O(1) when clean, O(n) recalculation only after modification
    float GetValue() const {
        if (!is_dirty) {
            return cached_value;
        }

        float flat_total = 0.0f;
        float percent_add_total = 0.0f;
        float percent_mult_total = 1.0f;

        for (const auto& mod : modifiers) {
            switch (mod.type) {
                case ModifierType::Flat:
                    flat_total += mod.value;
                    break;
                case ModifierType::PercentAdd:
                    percent_add_total += mod.value;
                    break;
                case ModifierType::PercentMult:
                    percent_mult_total *= (1.0f + mod.value);
                    break;
            }
        }

        cached_value = (base_value + flat_total) * (1.0f + percent_add_total) * percent_mult_total;
        is_dirty = false;
        return cached_value;
    }
};

// ============================================================================
// 2. DAMAGE PIPELINE (DIMINISHING RETURNS MITIGATION)
// ============================================================================

enum class DamageType : uint8_t {
    Physical,
    Magical,
    TrueDamage
};

struct DamagePacket {
    float raw_amount = 0.0f;
    DamageType type = DamageType::Physical;
    bool is_crit = false;
    float crit_multiplier = 1.5f;
};

struct DamageResult {
    float final_damage = 0.0f;
    float mitigated_amount = 0.0f;
    bool is_crit = false;
    bool is_dodged = false;
};

inline DamageResult CalculateDamage(const DamagePacket& packet, const Stat& defense_stat, bool dodge_roll = false) {
    if (dodge_roll) {
        return {0.0f, packet.raw_amount, false, true};
    }

    float incoming = packet.raw_amount;
    if (packet.is_crit) {
        incoming *= packet.crit_multiplier;
    }

    if (packet.type == DamageType::TrueDamage) {
        return {incoming, 0.0f, packet.is_crit, false};
    }

    float resistance = defense_stat.GetValue();
    float final_dmg = incoming;

    // Diminishing returns curve: Raw * (100 / (100 + Resistance))
    if (resistance >= 0.0f) {
        final_dmg = incoming * (100.0f / (100.0f + resistance));
    } else {
        // Negative armor amplification (capped up to 2x)
        final_dmg = incoming * (2.0f - (100.0f / (100.0f - resistance)));
    }

    float mitigated = incoming - final_dmg;
    return {final_dmg, mitigated, packet.is_crit, false};
}

// ============================================================================
// 3. COOLDOWN SYSTEM (HIGH-RESOLUTION MONOTONIC CLOCK)
// ============================================================================

class CooldownManager {
public:
    using Clock = std::chrono::steady_clock;
    using TimePoint = std::chrono::time_point<Clock>;
    using DurationSec = std::chrono::duration<float>;

private:
    struct CooldownState {
        TimePoint last_triggered;
        float cooldown_duration = 0.0f;
    };

    std::unordered_map<uint32_t, CooldownState> cd_map_;

public:
    // O(1) Trigger
    void Trigger(uint32_t action_id, float duration_seconds) {
        cd_map_[action_id] = {Clock::now(), duration_seconds};
    }

    // O(1) Check without sleeping threads or polling loops
    [[nodiscard]] bool IsReady(uint32_t action_id) const {
        auto it = cd_map_.find(action_id);
        if (it == cd_map_.end()) {
            return true;
        }

        const auto now = Clock::now();
        DurationSec elapsed = now - it->second.last_triggered;
        return elapsed.count() >= it->second.cooldown_duration;
    }

    [[nodiscard]] float GetRemaining(uint32_t action_id) const {
        auto it = cd_map_.find(action_id);
        if (it == cd_map_.end()) {
            return 0.0f;
        }

        const auto now = Clock::now();
        DurationSec elapsed = now - it->second.last_triggered;
        float remaining = it->second.cooldown_duration - elapsed.count();
        return remaining > 0.0f ? remaining : 0.0f;
    }

    [[nodiscard]] float GetNormalizedProgress(uint32_t action_id) const {
        auto it = cd_map_.find(action_id);
        if (it == cd_map_.end() || it->second.cooldown_duration <= 0.0f) {
            return 1.0f;
        }

        const auto now = Clock::now();
        DurationSec elapsed = now - it->second.last_triggered;
        float progress = elapsed.count() / it->second.cooldown_duration;
        return progress > 1.0f ? 1.0f : progress;
    }

    void Reset(uint32_t action_id) {
        cd_map_.erase(action_id);
    }
};

// ============================================================================
// 4. AIM ASSIST (ANGULAR DOT-PRODUCT TARGETING)
// ============================================================================

struct Vector3 {
    float x = 0.0f;
    float y = 0.0f;
    float z = 0.0f;

    [[nodiscard]] constexpr Vector3 operator-(const Vector3& o) const noexcept {
        return {x - o.x, y - o.y, z - o.z};
    }

    [[nodiscard]] constexpr Vector3 operator+(const Vector3& o) const noexcept {
        return {x + o.x, y + o.y, z + o.z};
    }

    [[nodiscard]] float LengthSquared() const noexcept {
        return x * x + y * y + z * z;
    }

    [[nodiscard]] float Length() const noexcept {
        return std::sqrt(LengthSquared());
    }

    [[nodiscard]] Vector3 Normalized() const noexcept {
        float len = Length();
        if (len <= std::numeric_limits<float>::epsilon()) {
            return {0.0f, 0.0f, 0.0f};
        }
        float inv = 1.0f / len;
        return {x * inv, y * inv, z * inv};
    }

    [[nodiscard]] static constexpr float Dot(const Vector3& a, const Vector3& b) noexcept {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }
};

struct TargetEntity {
    uint32_t id = 0;
    Vector3 position;
    bool is_alive = true;
};

// O(n) sweep through candidates using normalized dot-product angular alignment
inline const TargetEntity* GetBestTarget(
    const Vector3& origin,
    const Vector3& forward_unit,
    const std::vector<TargetEntity>& enemies,
    float max_range,
    float cone_angle_degrees
) {
    const TargetEntity* best_target = nullptr;
    float highest_dot = -1.0f;
    float max_range_sq = max_range * max_range;

    // Convert half-angle FOV to minimum dot product threshold
    const float min_dot_threshold = std::cos((cone_angle_degrees * 0.5f) * (3.14159265358979323846f / 180.0f));

    for (const auto& enemy : enemies) {
        if (!enemy.is_alive) {
            continue;
        }

        Vector3 to_target = enemy.position - origin;
        float dist_sq = to_target.LengthSquared();

        if (dist_sq > max_range_sq || dist_sq <= std::numeric_limits<float>::epsilon()) {
            continue;
        }

        float inv_dist = 1.0f / std::sqrt(dist_sq);
        Vector3 dir_to_target = {to_target.x * inv_dist, to_target.y * inv_dist, to_target.z * inv_dist};

        float dot = Vector3::Dot(forward_unit, dir_to_target);

        // Target must reside inside the assist cone and be closer to crosshair center
        if (dot >= min_dot_threshold && dot > highest_dot) {
            highest_dot = dot;
            best_target = &enemy;
        }
    }

    return best_target;
}

} // namespace combat
