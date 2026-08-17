package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class OnePlusProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // 1. OnePlus 13 (Snapdragon 8 Elite / 24GB RAM / 120Hz-144Hz)
        list.add(new SpoofProfile(
                "oneplus_13",
                "OnePlus 13 (Snapdragon 8 Elite / 24GB RAM)",
                "OnePlus",
                "PJZ110",
                "OnePlus",
                "OnePlus",
                "OP5D1BL1",
                "PJZ110",
                "PJZ110",
                "qcom",
                "sun",
                "SM8750-AB",
                "sun",
                "Snapdragon 8 Elite",
                "Qualcomm",
                8,
                4320000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp sve sve2",
                "OnePlus/PJZ110/OP5D1BL1:15/UKQ1.231003.002/OxygenOS15.0:user/release-keys",
                "OxygenOS15.0",
                "15",
                35,
                "2025-01-01",
                "Adreno (TM) 830",
                "Qualcomm",
                "OpenGL ES 3.2 V@0615.0",
                "1.3.280",
                "512.615.0",
                24576,
                19660
        ));

        // 2. OnePlus 12 (Snapdragon 8 Gen 3 / 16GB RAM)
        list.add(new SpoofProfile(
                "oneplus_12",
                "OnePlus 12 (Snapdragon 8 Gen 3 / 16GB RAM)",
                "OnePlus",
                "CPH2583",
                "OnePlus",
                "OnePlus",
                "OP595DL1",
                "CPH2583",
                "CPH2583",
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
                "OnePlus/CPH2583/OP595DL1:14/UKQ1.230917.001/OxygenOS14.0:user/release-keys",
                "OxygenOS14.0",
                "14",
                34,
                "2024-10-01",
                "Adreno (TM) 750",
                "Qualcomm",
                "OpenGL ES 3.2 V@0582.0",
                "1.3.275",
                "512.582.0",
                16384,
                12288
        ));

        return list;
    }
}
