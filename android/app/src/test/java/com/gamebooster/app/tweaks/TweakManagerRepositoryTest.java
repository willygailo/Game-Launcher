package com.gamebooster.app.tweaks;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TweakManagerRepositoryTest {

    @Test
    public void testGetAllTweaksNotEmpty() {
        List<TweakItem> all = TweakManagerRepository.getAllTweaks();
        assertNotNull(all);
        assertFalse("Tweaks repository should contain curated tweaks", all.isEmpty());
        assertTrue("Tweaks count should be at least 50", all.size() >= 50);

        for (TweakItem item : all) {
            assertNotNull("Tweak id should not be null", item.getId());
            assertFalse("Tweak id should not be empty", item.getId().trim().isEmpty());
            assertNotNull("Tweak title should not be null", item.getTitle());
            assertNotNull("Tweak category should not be null", item.getCategory());
            assertNotNull("Apply command should not be null", item.getApplyCommand());
            assertNotNull("Revert command should not be null", item.getRevertCommand());
        }
    }

    @Test
    public void testTweakCategories() {
        List<TweakItem> cpuGpu = TweakManagerRepository.getTweaksByCategory(TweakCategory.CPU_GPU);
        assertNotNull(cpuGpu);
        assertFalse("CPU/GPU tweaks should not be empty", cpuGpu.isEmpty());

        List<TweakItem> touchDisplay = TweakManagerRepository.getTweaksByCategory(TweakCategory.TOUCH_DISPLAY);
        assertNotNull(touchDisplay);
        assertFalse("Touch/Display tweaks should not be empty", touchDisplay.isEmpty());

        List<TweakItem> shizuku = TweakManagerRepository.getTweaksByCategory(TweakCategory.SHIZUKU_SYSTEM);
        assertNotNull(shizuku);
        assertFalse("Shizuku/System tweaks should not be empty", shizuku.isEmpty());

        List<TweakItem> network = TweakManagerRepository.getTweaksByCategory(TweakCategory.NETWORK_LATENCY);
        assertNotNull(network);
        assertFalse("Network tweaks should not be empty", network.isEmpty());
    }

    @Test
    public void testTweakItemGettersAndSetters() {
        TweakItem item = new TweakItem(
                "test_id",
                "Test Title",
                "Test Desc",
                "setprop test.prop 1",
                "setprop test.prop 0",
                TweakCategory.CPU_GPU,
                true
        );

        assertEquals("test_id", item.getId());
        assertEquals("Test Title", item.getTitle());
        assertEquals("Test Desc", item.getDescription());
        assertEquals("setprop test.prop 1", item.getApplyCommand());
        assertEquals("setprop test.prop 0", item.getRevertCommand());
        assertEquals(TweakCategory.CPU_GPU, item.getCategory());
        assertFalse(item.isApplied());

        item.setApplied(true);
        assertTrue(item.isApplied());
    }
}
