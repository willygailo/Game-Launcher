package com.gamebooster.app.feature.performance.tweaks;

import java.util.ArrayList;
import java.util.List;

/**
 * TouchDisplayTweaksProvider — High-responsiveness display and touch sensitivity tweaks.
 */
public final class TouchDisplayTweaksProvider {
    private TouchDisplayTweaksProvider() { }

    public static List<TweakItem> getTweaks() {
        List<TweakItem> list = new ArrayList<>();

        list.add(new TweakItem(
                "super_fast_touch_165",
                "Super Fast Touch 165Hz Competitive Mode",
                "Ultra-low slop (0), max digitizer input rate 1000/s, min pressure scale — tuned for 165Hz competitive play",
                "setprop view.touch_slop 0; setprop persist.sys.touch.pressure.scale 0.0001; setprop debug.input.max_events_per_sec 1000; setprop sys.use_fifo 1; settings put system touch_slop_reduction 1",
                "setprop view.touch_slop 8; setprop persist.sys.touch.pressure.scale 1.0; setprop debug.input.max_events_per_sec 150; setprop sys.use_fifo 0; settings put system touch_slop_reduction 0",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        return list;
    }
}
