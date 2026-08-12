package com.gamebooster.app.feature.performance.tweaks;

import java.util.Collections;
import java.util.List;

/**
 * Refresh rate is deliberately not a shell-tweak category.
 *
 * <p>The app has one capability-checked control for it: the per-game profile
 * backed by {@code DisplayOverrideController}. It only offers modes reported by
 * Android and never alters a game's files, rendering settings, input pipeline,
 * SurfaceFlinger internals, or thermal policy.</p>
 */
public final class TouchDisplayTweaksProvider {
    private TouchDisplayTweaksProvider() { }

    public static List<TweakItem> getTweaks() {
        return Collections.emptyList();
    }
}
