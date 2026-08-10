package com.gamebooster.app.tweaks;

import java.util.ArrayList;
import java.util.List;

public class TouchDisplayTweaksProvider {

    public static List<TweakItem> getTweaks() {
        List<TweakItem> list = new ArrayList<>();

        list.add(new TweakItem(
                "scrolling_cache_boost",
                "Zero Scroll Cache Latency",
                "Disables scrolling cache compression to save CPU render cycles",
                "setprop persist.sys.scrollingcache 3",
                "setprop persist.sys.scrollingcache 1",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        list.add(new TweakItem(
                "aim_touch_precision",
                "0ms Zero-Delay Aim Touch Engine",
                "Eliminates input lag, sets 0px touch slop deadzone, 1000Hz digitizer event rate, and 0ms touch response time",
                "setprop view.touch_slop 0; setprop persist.sys.touch.response_time 0; setprop debug.input.max_events_per_sec 1000; settings put system touch_slop_reduction 1; device_config put input_native_boot touch_slop 0; setprop sys.use_fifo 1; setprop persist.sys.touch.pressure.scale 0.0001",
                "setprop view.touch_slop 8; setprop persist.sys.touch.response_time 10; setprop debug.input.max_events_per_sec 150; settings put system touch_slop_reduction 0; setprop sys.use_fifo 0; setprop persist.sys.touch.pressure.scale 1.0",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        list.add(new TweakItem(
                "hz_120_unlock",
                "120Hz Ultra Refresh Rate Lock",
                "Forces 120Hz via Shizuku: system settings + Game Mode + SurfaceFlinger + device_config",
                "settings put system peak_refresh_rate 120.0; settings put system min_refresh_rate 120.0; settings put system user_refresh_rate 120; settings put global peak_refresh_rate 120.0; settings put global min_refresh_rate 120.0; cmd game mode performance global; cmd window set-app-refresh-rate global 120; device_config put game_overlay global mode=2,fps=120:mode=3,fps=120; service call SurfaceFlinger 1035 i32 120; service call SurfaceFlinger 1036 i32 120; setprop debug.sf.fps_limit 120; setprop persist.sys.NV_FPSLIMIT 120; setprop persist.sys.NV_POWERMODE 1",
                "settings delete system peak_refresh_rate; settings delete system min_refresh_rate; settings delete system user_refresh_rate; settings delete global peak_refresh_rate; settings delete global min_refresh_rate",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        list.add(new TweakItem(
                "hz_144_unlock",
                "144Hz Extreme Refresh Rate Lock",
                "Forces 144Hz via Shizuku: system settings + Game Mode + SurfaceFlinger + device_config",
                "settings put system peak_refresh_rate 144.0; settings put system min_refresh_rate 144.0; settings put system user_refresh_rate 144; settings put global peak_refresh_rate 144.0; settings put global min_refresh_rate 144.0; cmd game mode performance global; cmd window set-app-refresh-rate global 144; device_config put game_overlay global mode=2,fps=144:mode=3,fps=144; service call SurfaceFlinger 1035 i32 144; service call SurfaceFlinger 1036 i32 144; setprop debug.sf.fps_limit 144; setprop persist.sys.NV_FPSLIMIT 144; setprop persist.sys.NV_POWERMODE 1",
                "settings delete system peak_refresh_rate; settings delete system min_refresh_rate; settings delete system user_refresh_rate; settings delete global peak_refresh_rate; settings delete global min_refresh_rate",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        list.add(new TweakItem(
                "hz_165_unlock",
                "165Hz Max Hardware Refresh Rate Lock",
                "Forces 165Hz via Shizuku: system settings + Game Mode + SurfaceFlinger + setprop zero-latency pipeline",
                "settings put system peak_refresh_rate 165.0; settings put system min_refresh_rate 165.0; settings put system user_refresh_rate 165; settings put global peak_refresh_rate 165.0; settings put global min_refresh_rate 165.0; cmd game mode performance global; cmd window set-app-refresh-rate global 165; device_config put game_overlay global mode=2,fps=165:mode=3,fps=165; service call SurfaceFlinger 1035 i32 165; service call SurfaceFlinger 1036 i32 165; setprop debug.sf.fps_limit 165; setprop persist.sys.NV_FPSLIMIT 165; setprop persist.sys.NV_POWERMODE 1; setprop debug.gr.swapinterval 0; setprop debug.egl.swapinterval 0; setprop debug.sf.latch_unsignaled 1; setprop debug.sf.disable_backpressure 1",
                "settings delete system peak_refresh_rate; settings delete system min_refresh_rate; settings delete system user_refresh_rate; settings delete global peak_refresh_rate; settings delete global min_refresh_rate",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        return list;
    }
}
