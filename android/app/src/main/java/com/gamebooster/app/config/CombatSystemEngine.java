package com.gamebooster.app.config;

import java.util.ArrayList;
import java.util.List;

/**
 * CombatSystemEngine — Modular Competitive Combat System for Game Launcher.
 *
 * Implements:
 * 1. Stat Attribute Model: BaseValue + Modifiers (Flat, PercentAdd, PercentMult) with dirty-flag caching.
 * 2. Damage Pipeline: Diminishing returns mitigation formula (Incoming * 100 / (100 + Armor)) with Crit and Dodge rolls.
 * 3. Cooldown System: High-resolution monotonic clock checks (zero thread sleeping).
 * 4. Aim Assist: 3D Vector angular Dot Product targeting within an assist cone.
 *
 * Backed by high-performance C++20 JNI acceleration in `gamebooster_native` with pure Java fallbacks.
 */
public final class CombatSystemEngine {

    public enum ModifierType {
        FLAT,
        PERCENT_ADD,
        PERCENT_MULT
    }

    public static class StatModifier {
        public final float value;
        public final ModifierType type;
        public final int sourceId;

        public StatModifier(float value, ModifierType type, int sourceId) {
            this.value = value;
            this.type = type;
            this.sourceId = sourceId;
        }
    }

    public static class Stat {
        private float baseValue;
        private float cachedValue;
        private boolean isDirty = true;
        private final List<StatModifier> modifiers = new ArrayList<>();

        public Stat(float baseValue) {
            this.baseValue = baseValue;
            this.cachedValue = baseValue;
            this.isDirty = false;
        }

        public synchronized void addModifier(float value, ModifierType type, int sourceId) {
            modifiers.add(new StatModifier(value, type, sourceId));
            isDirty = true;
        }

        public synchronized void removeModifiersBySource(int sourceId) {
            boolean changed = modifiers.removeIf(m -> m.sourceId == sourceId);
            if (changed) isDirty = true;
        }

        public synchronized void clearModifiers() {
            if (!modifiers.isEmpty()) {
                modifiers.clear();
                isDirty = true;
            }
        }

        public synchronized float getValue() {
            if (!isDirty) return cachedValue;

            float flatTotal = 0.0f;
            float percentAddTotal = 0.0f;
            float percentMultTotal = 1.0f;

            for (StatModifier mod : modifiers) {
                switch (mod.type) {
                    case FLAT:
                        flatTotal += mod.value;
                        break;
                    case PERCENT_ADD:
                        percentAddTotal += mod.value;
                        break;
                    case PERCENT_MULT:
                        percentMultTotal *= (1.0f + mod.value);
                        break;
                }
            }

            cachedValue = (baseValue + flatTotal) * (1.0f + percentAddTotal) * percentMultTotal;
            isDirty = false;
            return cachedValue;
        }
    }

    public enum DamageType {
        PHYSICAL(0),
        MAGICAL(1),
        TRUE_DAMAGE(2);

        public final int id;
        DamageType(int id) { this.id = id; }
    }

    public static class DamagePacket {
        public float rawAmount;
        public DamageType type = DamageType.PHYSICAL;
        public boolean isCrit;
        public float critMultiplier = 1.5f;

        public DamagePacket(float rawAmount, DamageType type, boolean isCrit, float critMultiplier) {
            this.rawAmount = rawAmount;
            this.type = type;
            this.isCrit = isCrit;
            this.critMultiplier = critMultiplier;
        }
    }

    public static class DamageResult {
        public final float finalDamage;
        public final float mitigatedAmount;
        public final boolean isCrit;
        public final boolean isDodged;

        public DamageResult(float finalDamage, float mitigatedAmount, boolean isCrit, boolean isDodged) {
            this.finalDamage = finalDamage;
            this.mitigatedAmount = mitigatedAmount;
            this.isCrit = isCrit;
            this.isDodged = isDodged;
        }
    }

    public static DamageResult calculateDamage(DamagePacket packet, Stat defenseStat, boolean dodgeRoll) {
        if (dodgeRoll) {
            return new DamageResult(0.0f, packet.rawAmount, false, true);
        }

        float defense = defenseStat != null ? defenseStat.getValue() : 0.0f;
        float finalDmg = NativeConfigInjector.calculateCombatDamage(
                packet.rawAmount,
                packet.type.id,
                defense,
                packet.isCrit,
                packet.critMultiplier,
                false
        );

        float incoming = packet.rawAmount * (packet.isCrit ? packet.critMultiplier : 1.0f);
        float mitigated = Math.max(0.0f, incoming - finalDmg);
        return new DamageResult(finalDmg, mitigated, packet.isCrit, false);
    }

    public static final class CooldownManager {
        public void trigger(int actionId, float durationSeconds) {
            NativeConfigInjector.triggerCombatCooldown(actionId, durationSeconds);
        }

        public boolean isReady(int actionId) {
            return NativeConfigInjector.isCombatCooldownReady(actionId);
        }

        public float getRemaining(int actionId) {
            return NativeConfigInjector.getCombatCooldownRemaining(actionId);
        }
    }

    public static class Vector3 {
        public float x, y, z;

        public Vector3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class TargetEntity {
        public final int id;
        public final Vector3 position;
        public final boolean isAlive;

        public TargetEntity(int id, Vector3 position, boolean isAlive) {
            this.id = id;
            this.position = position;
            this.isAlive = isAlive;
        }
    }

    public static TargetEntity getBestTarget(Vector3 origin, Vector3 forward, List<TargetEntity> enemies,
                                            float maxRange, float coneAngleDegrees) {
        if (enemies == null || enemies.isEmpty()) return null;

        float[] flatPositions = new float[enemies.size() * 3];
        for (int i = 0; i < enemies.size(); i++) {
            TargetEntity e = enemies.get(i);
            flatPositions[i * 3] = e.position.x;
            flatPositions[i * 3 + 1] = e.position.y;
            flatPositions[i * 3 + 2] = e.position.z;
        }

        int bestIndex = NativeConfigInjector.evaluateAimAssistTarget(
                origin.x, origin.y, origin.z,
                forward.x, forward.y, forward.z,
                flatPositions, enemies.size(),
                maxRange, coneAngleDegrees
        );

        if (bestIndex >= 0 && bestIndex < enemies.size()) {
            return enemies.get(bestIndex);
        }
        return null;
    }

    private CombatSystemEngine() {}
}
