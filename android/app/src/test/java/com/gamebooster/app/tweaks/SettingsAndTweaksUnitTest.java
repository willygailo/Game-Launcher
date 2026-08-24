package com.gamebooster.app.tweaks;

import com.gamebooster.app.booster.GpuTweaksChannel;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SettingsAndTweaksUnitTest {

    @Test
    public void testTweakManagerRepositoryLoading() {
        List<TweakItem> allTweaks = TweakManagerRepository.getAllTweaks();
        assertNotNull(allTweaks);
        assertFalse(allTweaks.isEmpty());
        assertTrue(allTweaks.size() >= 30);

        for (TweakItem item : allTweaks) {
            assertNotNull(item.getId());
            assertFalse(item.getId().trim().isEmpty());
            assertNotNull(item.getTitle());
            assertNotNull(item.getDescription());
            assertNotNull(item.getApplyCommand());
            assertFalse(item.getApplyCommand().trim().isEmpty());
            assertNotNull(item.getRevertCommand());
            assertFalse(item.getRevertCommand().trim().isEmpty());
            assertNotNull(item.getCategory());
        }
    }

    @Test
    public void testTweakCategoriesFiltering() {
        List<TweakItem> all = TweakManagerRepository.getTweaksByCategory(TweakCategory.ALL);
        assertNotNull(all);

        List<TweakItem> cpuGpu = TweakManagerRepository.getTweaksByCategory(TweakCategory.CPU_GPU);
        assertNotNull(cpuGpu);
        assertFalse(cpuGpu.isEmpty());

        List<TweakItem> touch = TweakManagerRepository.getTweaksByCategory(TweakCategory.TOUCH_DISPLAY);
        assertNotNull(touch);
        assertFalse(touch.isEmpty());

        List<TweakItem> shizuku = TweakManagerRepository.getTweaksByCategory(TweakCategory.SHIZUKU_SYSTEM);
        assertNotNull(shizuku);
        assertFalse(shizuku.isEmpty());

        List<TweakItem> network = TweakManagerRepository.getTweaksByCategory(TweakCategory.NETWORK_LATENCY);
        assertNotNull(network);
        assertFalse(network.isEmpty());

        assertEquals(all.size(), cpuGpu.size() + touch.size() + shizuku.size() + network.size());
    }

    @Test
    public void testGpuTweaksTargetGamesCsv() {
        String targetCsv = GpuTweaksChannel.getTargetGamesCsv();
        assertNotNull(targetCsv);
        assertFalse(targetCsv.trim().isEmpty());
        assertTrue(targetCsv.contains("com.tencent.ig"));
        assertTrue(targetCsv.contains("com.mobile.legends") || targetCsv.contains("com.mobilelegends.mi"));
        assertTrue(targetCsv.contains("com.activision.callofduty.shooter"));
        assertTrue(targetCsv.contains("com.dts.freefireth"));
    }
}
