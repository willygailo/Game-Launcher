package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class NubiaProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // Nubia REDMAGIC 10 Pro+ (Snapdragon 8 Elite / 24GB LPDDR5X / 144Hz-185Hz eSports)
        list.add(new SpoofProfile(
                "redmagic_10_pro",
                "Nubia REDMAGIC 10 Pro+ (185Hz eSports)",
                "Nubia",
                "NX789J",
                "nubia",
                "nubia",
                "NX789J",
                "NX789J",
                "NX789J",
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
                "nubia/NX789J/NX789J:15/UKQ1.231003.002/REDMAGICOS10.0:user/release-keys",
                "REDMAGICOS10.0",
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

        // Nubia REDMAGIC 9 Pro (Snapdragon 8 Gen 3 / 16GB RAM / 165Hz)
        list.add(new SpoofProfile(
                "redmagic_9_pro",
                "Nubia REDMAGIC 9 Pro (165Hz)",
                "Nubia",
                "NX769J",
                "nubia",
                "nubia",
                "NX769J",
                "NX769J",
                "NX769J",
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
                "nubia/NX769J/NX769J:14/UKQ1.230917.001/REDMAGICOS9.0:user/release-keys",
                "REDMAGICOS9.0",
                "14",
                34,
                "2024-04-01",
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
