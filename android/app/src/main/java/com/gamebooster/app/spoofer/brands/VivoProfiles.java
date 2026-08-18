package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class VivoProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // Vivo iQOO 13 (Snapdragon 8 Elite / 16GB LPDDR5X / 144Hz)
        list.add(new SpoofProfile(
                "iqoo_13",
                "Vivo iQOO 13 (Snapdragon 8 Elite)",
                "Vivo",
                "V2408A",
                "vivo",
                "vivo",
                "V2408A",
                "V2408A",
                "V2408A",
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
                "vivo/V2408A/V2408A:15/UKQ1.231003.002/OriginOS5.0:user/release-keys",
                "OriginOS5.0",
                "15",
                35,
                "2025-01-01",
                "Adreno (TM) 830",
                "Qualcomm",
                "OpenGL ES 3.2 V@0615.0",
                "1.3.280",
                "512.615.0",
                16384,
                12288,
        120
        ));

        // Vivo X200 Pro (MediaTek Dimensity 9400 / Immortalis-G925 MC12 / 16GB RAM)
        list.add(new SpoofProfile(
                "vivo_x200_pro",
                "Vivo X200 Pro (Dimensity 9400)",
                "Vivo",
                "V2413A",
                "vivo",
                "vivo",
                "V2413A",
                "V2413A",
                "V2413A",
                "mt6991",
                "mt6991",
                "MT6991",
                "k6991v1_64",
                "Dimensity 9400",
                "MediaTek",
                8,
                3630000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "vivo/V2413A/V2413A:15/UKQ1.231003.002/OriginOS5.0:user/release-keys",
                "OriginOS5.0",
                "15",
                35,
                "2025-01-01",
                "Immortalis-G925 MC12",
                "ARM",
                "OpenGL ES 3.2 v1.r48p0-01eac0.53d3b762ca1387d85c8e31e5f8ec4c27",
                "1.3.275",
                "20.0.0",
                16384,
                12288,
        120
        ));

        return list;
    }
}
