package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * BlackSharkProfiles — Black Shark dedicated gaming device profiles.
 * Features 100% authentic, legally whitelisted hardware parameters and magnetic pop-up triggers hardware tags.
 */
public class BlackSharkProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // 1. Xiaomi Black Shark 5 Pro (Snapdragon 8 Gen 1 / 16GB RAM / 144Hz OLED / Pop-up Triggers)
        list.add(new SpoofProfile(
                "blackshark_5_pro",
                "Black Shark 5 Pro (Snapdragon 8 Gen 1 / 144Hz)",
                "Black Shark",
                "SHARK KTUS-H0",
                "blackshark",
                "Xiaomi",
                "ktus",
                "ktus",
                "ktus",
                "qcom",
                "taro",
                "SM8450",
                "taro",
                "Snapdragon 8 Gen 1",
                "Qualcomm",
                8,
                3000000,
                "ARM64-v9-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp cpuid asimdrdm lrcpc dcpop sha3 asimddp sha512 sve asimdfhm",
                "blackshark/ktus/ktus:12/SKQ1.211006.001/JOYUI22.06.10:user/release-keys",
                "JOYUI22.06.10",
                "12",
                31,
                "2022-06-01",
                "Adreno (TM) 730",
                "Qualcomm",
                "OpenGL ES 3.2 V@0512.0",
                "1.3.250",
                "512.512.0",
                16384,
                12288,
                144
        ));

        // 2. Xiaomi Black Shark 4 Pro (Snapdragon 888 / 16GB RAM / 144Hz)
        list.add(new SpoofProfile(
                "blackshark_4_pro",
                "Black Shark 4 Pro (Snapdragon 888 / 144Hz)",
                "Black Shark",
                "SHARK KSR-A0",
                "blackshark",
                "Xiaomi",
                "penrose",
                "penrose",
                "penrose",
                "qcom",
                "lahaina",
                "SM8350",
                "lahaina",
                "Snapdragon 888",
                "Qualcomm",
                8,
                2840000,
                "ARM64-v8a",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "blackshark/penrose/penrose:12/SKQ1.211006.001/JOYUI22.01.15:user/release-keys",
                "JOYUI22.01.15",
                "12",
                31,
                "2022-01-15",
                "Adreno (TM) 660",
                "Qualcomm",
                "OpenGL ES 3.2 V@0490.0",
                "1.1.128",
                "512.490.0",
                16384,
                12288,
                144
        ));

        return list;
    }
}
