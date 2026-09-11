package com.gamebooster.app.engine;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AndroidVersionSupportManagerTest {

    @Test
    public void testVersionDisplayNameNotNull() {
        String name = AndroidVersionSupportManager.getVersionDisplayName();
        assertNotNull(name);
        assertTrue(name.contains("Android"));
    }

    @Test
    public void testAndroid13Optimizations() {
        List<String> cmds = new ArrayList<>();
        AndroidVersionSupportManager.applyAndroid13Optimizations("com.mobile.legends", 120, cmds);

        assertFalse(cmds.isEmpty());
        boolean hasGameMode = false;
        boolean hasOverlay = false;
        boolean hasAngleDisabled = false;

        for (String c : cmds) {
            if (c.contains("cmd game mode performance com.mobile.legends")) hasGameMode = true;
            if (c.contains("device_config put game_overlay com.mobile.legends")) hasOverlay = true;
            if (c.contains("useAngle=false")) hasAngleDisabled = true;
        }

        assertTrue("Must configure performance game mode", hasGameMode);
        assertTrue("Must configure game_overlay", hasOverlay);
        assertTrue("Must explicitly disable ANGLE", hasAngleDisabled);
    }

    @Test
    public void testAndroid14Optimizations() {
        List<String> cmds = new ArrayList<>();
        AndroidVersionSupportManager.applyAndroid14Optimizations("com.tencent.ig", 144, cmds);

        assertFalse(cmds.isEmpty());
        boolean hasWindowRefresh = false;

        for (String c : cmds) {
            if (c.contains("cmd window set-app-refresh-rate com.tencent.ig 144")) hasWindowRefresh = true;
        }

        assertTrue("Must configure window set-app-refresh-rate", hasWindowRefresh);
    }

    @Test
    public void testAndroid15Optimizations() {
        List<String> cmds = new ArrayList<>();
        AndroidVersionSupportManager.applyAndroid15Optimizations("com.activision.callofduty.shooter", 120, cmds);

        assertFalse(cmds.isEmpty());
        boolean hasFixedPerf = false;
        boolean hasPowerMode = false;

        for (String c : cmds) {
            if (c.contains("cmd power set-fixed-performance-mode-enabled true")) hasFixedPerf = true;
            if (c.contains("cmd power set-mode")) hasPowerMode = true;
        }

        assertTrue("Must configure fixed performance mode", hasFixedPerf);
        assertTrue("Must configure sustained power modes", hasPowerMode);
    }

    @Test
    public void testAndroid16Optimizations() {
        List<String> cmds = new ArrayList<>();
        AndroidVersionSupportManager.applyAndroid16Optimizations("com.miHoYo.GenshinImpact", 185, cmds);

        assertFalse(cmds.isEmpty());
        boolean hasPerfClass = false;
        boolean hasAppImageCache = false;
        boolean hasBoostSched = false;

        for (String c : cmds) {
            if (c.contains("cmd game set --performance-class 3 com.miHoYo.GenshinImpact")) hasPerfClass = true;
            if (c.contains("use_app_image_startup_cache true")) hasAppImageCache = true;
            if (c.contains("boost_sched_priority true")) hasBoostSched = true;
        }

        assertTrue("Must set media performance class 3", hasPerfClass);
        assertTrue("Must enable runtime app image cache", hasAppImageCache);
        assertTrue("Must enable runtime boost sched priority", hasBoostSched);
    }
}
