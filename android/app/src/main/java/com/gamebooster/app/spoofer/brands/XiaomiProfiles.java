package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class XiaomiProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // 1. Xiaomi 15 Ultra (Snapdragon 8 Elite / 16GB RAM / 144Hz)
        list.add(new SpoofProfile(
                "xiaomi_15_ultra",
                "Xiaomi 15 Ultra (Snapdragon 8 Elite / 16GB RAM)",
                "Xiaomi",
                "24129PN74C",
                "Xiaomi",
                "Xiaomi",
                "xuanyuan",
                "xuanyuan",
                "xuanyuan",
                "qcom",
                "sun",
                "SM8750-AB",
                "sun",
                "Snapdragon 8 Elite",
                "Qualcomm",
                8,
                4320000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "Xiaomi/xuanyuan/xuanyuan:15/UKQ1.231003.002/HyperOS2.0:user/release-keys",
                "HyperOS2.0",
                "15",
                35,
                "2025-01-01",
                "Adreno (TM) 830",
                "Qualcomm",
                "OpenGL ES 3.2 V@0615.0",
                "1.3.280",
                "512.615.0",
                16384,
                12288
        ));

        // 2. Xiaomi POCO F7 Pro (Snapdragon 8 Gen 3 / 16GB RAM / 144Hz)
        list.add(new SpoofProfile(
                "poco_f7_pro",
                "Xiaomi POCO F7 Pro (Snapdragon 8 Gen 3 / 16GB RAM)",
                "Xiaomi",
                "24117RK2CC",
                "POCO",
                "Xiaomi",
                "vermeer",
                "vermeer_global",
                "vermeer",
                "qcom",
                "pineapple",
                "SM8650",
                "pineapple",
                "Snapdragon 8 Gen 3",
                "Qualcomm",
                8,
                3300000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "POCO/vermeer_global/vermeer:15/UKQ1.240810.001/HyperOS2.0:user/release-keys",
                "HyperOS2.0",
                "15",
                35,
                "2025-01-01",
                "Adreno (TM) 750",
                "Qualcomm",
                "OpenGL ES 3.2 V@0582.0",
                "1.3.275",
                "512.582.0",
                16384,
                12288
        ));

        // 3. Black Shark 5 Pro (Snapdragon 8 Gen 1 / 16GB RAM / 144Hz)
        list.add(new SpoofProfile(
                "blackshark_5_pro",
                "Xiaomi Black Shark 5 Pro (Snapdragon 8 Gen 1 / 144Hz)",
                "Xiaomi",
                "SHARK KTUS-H0",
                "blackshark",
                "blackshark",
                "patriot",
                "patriot_global",
                "patriot",
                "qcom",
                "taro",
                "SM8450",
                "taro",
                "Snapdragon 8 Gen 1",
                "Qualcomm",
                8,
                3000000,
                "ARM64-v9.0-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "blackshark/patriot_global/patriot:13/TQ3A.230805.001/JOYUI13:user/release-keys",
                "JOYUI13",
                "13",
                33,
                "2024-01-01",
                "Adreno (TM) 730",
                "Qualcomm",
                "OpenGL ES 3.2 V@0530.0",
                "1.3.260",
                "512.530.0",
                16384,
                12288
        ));

        return list;
    }
}
