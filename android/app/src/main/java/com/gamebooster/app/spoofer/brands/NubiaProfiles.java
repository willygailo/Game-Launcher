package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class NubiaProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // 1. REDMAGIC 10 Pro+ Golden Saga (Snapdragon 8 Elite / 24GB RAM / 185Hz/165Hz eSports)
        list.add(new SpoofProfile(
                "redmagic_10_pro",
                "Nubia REDMAGIC 10 Pro+ (Snapdragon 8 Elite / 24GB RAM / 165Hz)",
                "Nubia REDMAGIC",
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
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp sve sve2",
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
                19660
        ));

        // 2. REDMAGIC 9S Pro+ (Snapdragon 8 Gen 3 Leading Version / 16GB RAM / 165Hz)
        list.add(new SpoofProfile(
                "redmagic_9_pro",
                "Nubia REDMAGIC 9S Pro+ (Snapdragon 8 Gen 3 / 16GB RAM / 165Hz)",
                "Nubia REDMAGIC",
                "NX769J",
                "nubia",
                "nubia",
                "NX769J",
                "NX769J",
                "NX769J",
                "qcom",
                "pineapple",
                "SM8650-AC",
                "pineapple",
                "Snapdragon 8 Gen 3 Leading Version",
                "Qualcomm",
                8,
                3400000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "nubia/NX769J/NX769J:14/UKQ1.230917.001/REDMAGICOS9.5:user/release-keys",
                "REDMAGICOS9.5",
                "14",
                34,
                "2024-11-01",
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
