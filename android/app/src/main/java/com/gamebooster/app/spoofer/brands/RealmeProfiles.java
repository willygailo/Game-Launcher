package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class RealmeProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // Realme GT7 Pro (Snapdragon 8 Elite / 24GB LPDDR5X / realme UI 6.0)
        list.add(new SpoofProfile(
                "realme_gt7_pro",
                "Realme GT7 Pro (Snapdragon 8 Elite)",
                "Realme",
                "RMX5010",
                "realme",
                "realme",
                "RE5D1BL1",
                "RMX5010",
                "RMX5010",
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
                "realme/RMX5010/RE5D1BL1:15/UKQ1.231003.002/realmeUI6.0:user/release-keys",
                "realmeUI6.0",
                "15",
                35,
                "2025-01-01",
                "Adreno (TM) 830",
                "Qualcomm",
                "OpenGL ES 3.2 V@0615.0",
                "1.3.280",
                "512.615.0",
                24576,
                19660,
        185
        ));

        // Realme GT5 Pro (Snapdragon 8 Gen 3 / 16GB RAM / realme UI 5.0)
        list.add(new SpoofProfile(
                "realme_gt5_pro",
                "Realme GT5 Pro (Snapdragon 8 Gen 3)",
                "Realme",
                "RMX3888",
                "realme",
                "realme",
                "RE58A9L1",
                "RMX3888",
                "RMX3888",
                "qcom",
                "pineapple",
                "SM8650-AB",
                "pineapple",
                "Snapdragon 8 Gen 3",
                "Qualcomm",
                8,
                3300000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "realme/RMX3888/RE58A9L1:14/UKQ1.230917.001/realmeUI5.0:user/release-keys",
                "realmeUI5.0",
                "14",
                34,
                "2024-03-01",
                "Adreno (TM) 750",
                "Qualcomm",
                "OpenGL ES 3.2 V@0530.0",
                "1.3.275",
                "512.530.0",
                16384,
                12288,
        185
        ));

        return list;
    }
}
