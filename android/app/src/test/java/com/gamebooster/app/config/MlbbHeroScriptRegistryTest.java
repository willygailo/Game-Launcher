package com.gamebooster.app.config;

import org.junit.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MlbbHeroScriptRegistryTest {

    @Test
    public void testMetaHeroesCountAndIntegrity() {
        MlbbHeroScriptRegistry.HeroEntry[] meta = MlbbHeroScriptRegistry.getMetaHeroes();
        assertNotNull(meta);
        assertEquals(20, meta.length);

        for (MlbbHeroScriptRegistry.HeroEntry hero : meta) {
            assertNotNull(hero);
            assertTrue(hero.id > 0);
            assertNotNull(hero.name);
            assertNotNull(hero.role);
            assertNotNull(hero.scriptFile);
            assertTrue(hero.hasScript);
            assertTrue(hero.assetPath().startsWith("lua/mlbb_heroes/"));
            assertTrue(hero.assetPath().endsWith(".lua"));
        }
    }

    @Test
    public void testLookupById() {
        MlbbHeroScriptRegistry.HeroEntry fanny = MlbbHeroScriptRegistry.getById(114);
        assertNotNull(fanny);
        assertEquals("Fanny", fanny.name);
        assertEquals("Assassin", fanny.role);

        MlbbHeroScriptRegistry.HeroEntry ling = MlbbHeroScriptRegistry.getById(154);
        assertNotNull(ling);
        assertEquals("Ling", ling.name);

        MlbbHeroScriptRegistry.HeroEntry beatrix = MlbbHeroScriptRegistry.getById(175);
        assertNotNull(beatrix);
        assertEquals("Beatrix", beatrix.name);
        assertEquals("Marksman", beatrix.role);
    }

    @Test
    public void testLookupByName() {
        MlbbHeroScriptRegistry.HeroEntry gusion = MlbbHeroScriptRegistry.getByName("gusion");
        assertNotNull(gusion);
        assertEquals(128, gusion.id);

        MlbbHeroScriptRegistry.HeroEntry chou = MlbbHeroScriptRegistry.getByName("Chou");
        assertNotNull(chou);
        assertEquals(107, chou.id);
    }

    @Test
    public void testAllHeroesRegistered() {
        int total = MlbbHeroScriptRegistry.totalRegistered();
        assertTrue("Expected at least 100 heroes registered, got: " + total, total >= 100);
    }

    @Test
    public void testAntiBanStealthDrift() {
        float base = 10000.0f;
        float drifted = AntiBanStealthEngine.driftValue(base);
        // Drift should be within ±10% of base
        assertTrue(drifted >= 9000.0f && drifted <= 11000.0f);
    }

    @Test
    public void testHeroScriptModifiersInjectionNullSafe() {
        assertFalse(NativeConfigInjector.injectHeroScriptModifiers(null, null));
        assertFalse(NativeConfigInjector.injectHeroScriptModifiers("", null));
        Map<String, String> mods = new HashMap<>();
        mods.put("Hero_114_S1Damage", "10000");
        assertFalse(NativeConfigInjector.injectHeroScriptModifiers("", mods));
    }
}
