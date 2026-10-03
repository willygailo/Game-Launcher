package com.gamebooster.app.config;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class CombatSystemEngineTest {

    @Test
    public void testStatModifiersAndCaching() {
        CombatSystemEngine.Stat stat = new CombatSystemEngine.Stat(100.0f);
        assertEquals(100.0f, stat.getValue(), 0.001f);

        // Flat: 100 + 20 = 120
        stat.addModifier(20.0f, CombatSystemEngine.ModifierType.FLAT, 1);
        assertEquals(120.0f, stat.getValue(), 0.001f);

        // Percent Add: 120 * (1 + 0.10) = 132
        stat.addModifier(0.10f, CombatSystemEngine.ModifierType.PERCENT_ADD, 2);
        assertEquals(132.0f, stat.getValue(), 0.001f);

        // Percent Mult: 132 * (1 + 0.50) = 198
        stat.addModifier(0.50f, CombatSystemEngine.ModifierType.PERCENT_MULT, 3);
        assertEquals(198.0f, stat.getValue(), 0.001f);

        // Remove source 2: (100 + 20) * 1.0 * 1.5 = 180
        stat.removeModifiersBySource(2);
        assertEquals(180.0f, stat.getValue(), 0.001f);

        // Clear: 100
        stat.clearModifiers();
        assertEquals(100.0f, stat.getValue(), 0.001f);
    }

    @Test
    public void testDamagePipelineDiminishingReturns() {
        CombatSystemEngine.Stat armor = new CombatSystemEngine.Stat(100.0f); // 100 armor -> 50% damage reduction
        CombatSystemEngine.DamagePacket packet = new CombatSystemEngine.DamagePacket(
                200.0f,
                CombatSystemEngine.DamageType.PHYSICAL,
                false,
                1.5f
        );

        CombatSystemEngine.DamageResult result = CombatSystemEngine.calculateDamage(packet, armor, false);
        // 200 * (100 / (100 + 100)) = 100 final damage
        assertEquals(100.0f, result.finalDamage, 0.5f);
        assertEquals(100.0f, result.mitigatedAmount, 0.5f);
        assertFalse(result.isDodged);

        // Test Dodge
        CombatSystemEngine.DamageResult dodgeResult = CombatSystemEngine.calculateDamage(packet, armor, true);
        assertEquals(0.0f, dodgeResult.finalDamage, 0.001f);
        assertTrue(dodgeResult.isDodged);

        // Test Crit
        packet.isCrit = true;
        packet.critMultiplier = 2.0f;
        // 200 * 2.0 = 400 incoming -> 400 * 0.5 = 200 final
        CombatSystemEngine.DamageResult critResult = CombatSystemEngine.calculateDamage(packet, armor, false);
        assertEquals(200.0f, critResult.finalDamage, 0.5f);
        assertTrue(critResult.isCrit);
    }

    @Test
    public void testAimAssistTargeting() {
        CombatSystemEngine.Vector3 origin = new CombatSystemEngine.Vector3(0.0f, 0.0f, 0.0f);
        CombatSystemEngine.Vector3 forward = new CombatSystemEngine.Vector3(0.0f, 0.0f, 1.0f); // Facing Z+

        List<CombatSystemEngine.TargetEntity> targets = new ArrayList<>();
        targets.add(new CombatSystemEngine.TargetEntity(0, new CombatSystemEngine.Vector3(15.0f, 0.0f, 10.0f), true)); // ~56 degrees
        targets.add(new CombatSystemEngine.TargetEntity(1, new CombatSystemEngine.Vector3(1.0f, 0.0f, 20.0f), true));  // ~2.8 degrees (dead center)
        targets.add(new CombatSystemEngine.TargetEntity(2, new CombatSystemEngine.Vector3(50.0f, 0.0f, 5.0f), true));  // Out of cone

        CombatSystemEngine.TargetEntity best = CombatSystemEngine.getBestTarget(origin, forward, targets, 50.0f, 30.0f);
        assertNotNull(best);
        assertEquals(1, best.id);
    }
}
