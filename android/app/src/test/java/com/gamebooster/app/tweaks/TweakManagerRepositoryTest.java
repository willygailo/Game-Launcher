package com.gamebooster.app.tweaks;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TweakManagerRepositoryTest {

    @Test
    public void testTweakRepositoryIntegrity() {
        List<TweakItem> allTweaks = TweakManagerRepository.getAllTweaks();
        assertNotNull(allTweaks);
        assertTrue("Repository should have tweaks", allTweaks.size() >= 50);

        // Verify ID uniqueness
        Set<String> ids = new HashSet<>();
        for (TweakItem tweak : allTweaks) {
            assertNotNull("Tweak ID cannot be null", tweak.getId());
            assertFalse("Duplicate tweak ID detected: " + tweak.getId(), ids.contains(tweak.getId()));
            ids.add(tweak.getId());

            assertNotNull("Title cannot be null for " + tweak.getId(), tweak.getTitle());
            assertNotNull("Apply command cannot be null for " + tweak.getId(), tweak.getApplyCommand());
            assertNotNull("Revert command cannot be null for " + tweak.getId(), tweak.getRevertCommand());
            assertNotNull("Category cannot be null for " + tweak.getId(), tweak.getCategory());
        }
    }

    @Test
    public void testTweakCategoryLookup() {
        for (TweakCategory cat : TweakCategory.values()) {
            List<TweakItem> items = TweakManagerRepository.getTweaksByCategory(cat);
            assertNotNull(items);
            if (cat != TweakCategory.ALL) {
                for (TweakItem item : items) {
                    assertEquals(cat, item.getCategory());
                }
            }
        }
    }

    @Test
    public void testGetTweakById() {
        TweakItem item = TweakManagerRepository.getTweakById("adreno_turbo_boost");
        assertNotNull(item);
        assertEquals("adreno_turbo_boost", item.getId());
        assertEquals(TweakCategory.CPU_GPU, item.getCategory());

        TweakItem nullItem = TweakManagerRepository.getTweakById("non_existent_id");
        assertTrue(nullItem == null);
    }

    @Test
    public void testTweakItemEqualsAndHashCode() {
        TweakItem t1 = new TweakItem("test_id", "Title", "Desc", "cmd", "rev", TweakCategory.CPU_GPU, false);
        TweakItem t2 = new TweakItem("test_id", "Title 2", "Desc 2", "cmd 2", "rev 2", TweakCategory.TOUCH_DISPLAY, true);
        TweakItem t3 = new TweakItem("other_id", "Title", "Desc", "cmd", "rev", TweakCategory.CPU_GPU, false);

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
        assertFalse(t1.equals(t3));
    }

    @Test
    public void testTweakCategoryFromName() {
        assertEquals(TweakCategory.CPU_GPU, TweakCategory.fromName("CPU_GPU"));
        assertEquals(TweakCategory.TOUCH_DISPLAY, TweakCategory.fromName("TOUCH_DISPLAY"));
        assertEquals(TweakCategory.ALL, TweakCategory.fromName("INVALID_CAT"));
        assertEquals(TweakCategory.ALL, TweakCategory.fromName(null));
    }

    @Test
    public void testCategoryBatchOperationsAsync() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger progressCount = new AtomicInteger(0);
        AtomicInteger completedCount = new AtomicInteger(0);

        TweakManagerRepository.applyCategoryTweaksAsync(null, TweakCategory.CPU_GPU,
                (current, total, tweakTitle) -> progressCount.incrementAndGet(),
                appliedCount -> {
                    completedCount.set(appliedCount);
                    latch.countDown();
                });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(completedCount.get() > 0);
        assertEquals(completedCount.get(), progressCount.get());
    }
}
