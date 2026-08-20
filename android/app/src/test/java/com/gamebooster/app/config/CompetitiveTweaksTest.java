package com.gamebooster.app.config;

import org.junit.Test;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import static org.junit.Assert.*;

public class CompetitiveTweaksTest {

    @Test
    public void testCompetitiveCfgProfileDefaultEnablesAllTweaks() {
        for (String gameKey : new String[]{
                CompetitiveCfgProfile.GAME_MLBB,
                CompetitiveCfgProfile.GAME_PUBGM,
                CompetitiveCfgProfile.GAME_CODM,
                CompetitiveCfgProfile.GAME_FREEFIRE,
                CompetitiveCfgProfile.GAME_GENSHIN,
                CompetitiveCfgProfile.GAME_HOK,
                CompetitiveCfgProfile.GAME_ROBLOX,
                CompetitiveCfgProfile.GAME_VALORANT,
                CompetitiveCfgProfile.GAME_FARLIGHT,
                CompetitiveCfgProfile.GAME_BLOODSTRIKE,
                CompetitiveCfgProfile.GAME_STANDOFF2,
                CompetitiveCfgProfile.GAME_WILDRIFT,
                CompetitiveCfgProfile.GAME_CARX,
                CompetitiveCfgProfile.GAME_ARENABREAKOUT,
                CompetitiveCfgProfile.GAME_SUPERCELL,
                CompetitiveCfgProfile.GAME_ALL
        }) {
            CompetitiveCfgProfile profile = CompetitiveCfgProfile.defaultCompetitive(gameKey);
            assertNotNull(profile);
            assertEquals(gameKey, profile.getGameKey());
            assertEquals(185, profile.getTargetFps());
            assertTrue("Damage script should be enabled", profile.isMlbbDamageScriptEnabled());
            assertTrue("Damage boost alias should be enabled", profile.isDamageBoostEnabled());
            assertTrue("Recoil control should be enabled", profile.isRecoilControlEnabled());
            assertTrue("Aim assist should be enabled", profile.isAimAssistEnabled());
            assertTrue("Armor def should be enabled", profile.isArmorDefEnabled());
            assertTrue("Super fast touch should be enabled", profile.isSuperFastTouchEnabled());
            assertTrue("Touch no delay should be enabled", profile.isTouchNoDelayEnabled());
        }
    }

    @Test
    public void testAllGamePatchersHaveCompetitiveMethods() throws Exception {
        Class<?>[] patchers = new Class<?>[]{
                MlbbConfigPatcher.class,
                PubgConfigPatcher.class,
                CodmConfigPatcher.class,
                FreeFireConfigPatcher.class,
                GenshinConfigPatcher.class,
                HokConfigPatcher.class,
                RobloxConfigPatcher.class,
                ValorantConfigPatcher.class,
                FarlightConfigPatcher.class,
                BloodStrikeConfigPatcher.class,
                Standoff2ConfigPatcher.class,
                WildRiftConfigPatcher.class,
                CarXConfigPatcher.class,
                ArenaBreakoutConfigPatcher.class,
                SupercellConfigPatcher.class
        };

        String[] requiredMethods = new String[]{
                "applyDamageScriptConfig",
                "applyRecoilControlConfig",
                "applyAimAssistConfig",
                "applyArmorDefConfig",
                "applySuperFastTouch"
        };

        for (Class<?> patcher : patchers) {
            for (String methodName : requiredMethods) {
                Method method = patcher.getMethod(methodName, String.class);
                assertNotNull("Patcher " + patcher.getSimpleName() + " missing method " + methodName, method);
                assertTrue("Method " + methodName + " in " + patcher.getSimpleName() + " should be public static",
                        Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers()));
            }
        }
    }
}
