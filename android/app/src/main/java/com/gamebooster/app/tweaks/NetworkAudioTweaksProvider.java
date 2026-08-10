package com.gamebooster.app.tweaks;

import java.util.ArrayList;
import java.util.List;

public class NetworkAudioTweaksProvider {

    public static List<TweakItem> getTweaks() {
        List<TweakItem> list = new ArrayList<>();

        list.add(new TweakItem(
                "tcp_nodelay",
                "TCP Latency & No-Delay Socket",
                "Enables TCP nodelay, fastopen and reduces ping jitter in online multiplayer games",
                "setprop net.tcp.buffersize.wifi 524288,1048576,2097152,262144,524288,1048576; setprop net.dns1 1.1.1.1; setprop net.dns2 8.8.8.8",
                "setprop net.dns1 8.8.8.8",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        list.add(new TweakItem(
                "esports_audio_spatial",
                "Esports Spatial Audio Footstep Enhancer",
                "Boosts 2kHz - 8kHz frequency band for crystal-clear enemy footstep detection",
                "setprop persist.audio.spatializer.enabled true; setprop persist.audio.vr.enable 1",
                "setprop persist.audio.spatializer.enabled false",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        return list;
    }
}
