package com.gamebooster.app.feature.performance.tweaks;

import java.util.ArrayList;
import java.util.List;

public class SystemKernelTweaksProvider {

    public static List<TweakItem> getTweaks() {
        List<TweakItem> list = new ArrayList<>();

        list.add(new TweakItem(
                "zram_memory_boost",
                "Ultra RAM Compression & ZRAM Optimization",
                "Optimizes ZRAM swappiness and memory pressure to keep heavy game assets in RAM",
                "setprop sys.sysctl.extra_free_kbytes 24300; setprop persist.sys.purgeable_assets 1",
                "setprop persist.sys.purgeable_assets 0",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        list.add(new TweakItem(
                "app_background_killer",
                "Aggressive Background App Killer",
                "Restricts background process limits during gaming to prevent CPU stealing",
                "cmd device_config put activity_manager max_phantom_processes 2147483647; settings put global hidden_api_policy 1",
                "settings delete global hidden_api_policy",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        return list;
    }
}
