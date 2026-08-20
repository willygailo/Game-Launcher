package com.gamebooster.app.config;

import org.junit.Test;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import static org.junit.Assert.*;

public class AllGamePatchersAuditTest {

    private static final Class<?>[] PATCHER_CLASSES = {
        MlbbConfigPatcher.class,
        PubgConfigPatcher.class,
        CodmConfigPatcher.class,
        FreeFireConfigPatcher.class,
        GenshinConfigPatcher.class,
        WildRiftConfigPatcher.class,
        HokConfigPatcher.class,
        BloodStrikeConfigPatcher.class,
        Standoff2ConfigPatcher.class,
        CarXConfigPatcher.class,
        ArenaBreakoutConfigPatcher.class,
        SupercellConfigPatcher.class,
        RobloxConfigPatcher.class,
        ValorantConfigPatcher.class,
        FarlightConfigPatcher.class
    };

    private static final String[] REQUIRED_METHODS = {
        "applyDamageScriptConfig",
        "applyRecoilControlConfig",
        "applyTrackingBulletConfig",
        "applyAimAssistConfig",
        "applySuperFastTouch",
        "applyArmorDefConfig",
        "applyAntiLog"
    };

    @Test
    public void testAll15PatchersHaveRequiredMethods() {
        assertEquals("Must have exactly 15 game patchers", 15, PATCHER_CLASSES.length);

        for (Class<?> clazz : PATCHER_CLASSES) {
            for (String methodName : REQUIRED_METHODS) {
                try {
                    Method m = clazz.getMethod(methodName, String.class);
                    assertTrue("Method " + methodName + " in " + clazz.getSimpleName() + " must be public static",
                            Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers()));
                } catch (NoSuchMethodException e) {
                    fail("Missing required method: " + methodName + " in " + clazz.getSimpleName());
                }
            }

            // Verify patch and patchCompetitive exist
            try {
                Method mPatch = clazz.getMethod("patch", String.class, int.class);
                assertTrue(Modifier.isPublic(mPatch.getModifiers()) && Modifier.isStatic(mPatch.getModifiers()));

                Method mComp = clazz.getMethod("patchCompetitive", String.class, int.class);
                assertTrue(Modifier.isPublic(mComp.getModifiers()) && Modifier.isStatic(mComp.getModifiers()));
            } catch (NoSuchMethodException e) {
                fail("Missing patch or patchCompetitive method in " + clazz.getSimpleName());
            }
        }
    }
}
